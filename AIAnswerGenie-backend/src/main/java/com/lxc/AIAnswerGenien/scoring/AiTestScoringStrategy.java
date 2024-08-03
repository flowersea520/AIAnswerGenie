package com.lxc.AIAnswerGenien.scoring;


import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.lxc.AIAnswerGenien.manager.AiManager;
import com.lxc.AIAnswerGenien.model.dto.question.QuestionAnswerDTO;
import com.lxc.AIAnswerGenien.model.dto.question.QuestionContentDTO;
import com.lxc.AIAnswerGenien.model.entity.App;
import com.lxc.AIAnswerGenien.model.entity.Question;
import com.lxc.AIAnswerGenien.model.entity.UserAnswer;
import com.lxc.AIAnswerGenien.model.vo.QuestionVO;
import com.lxc.AIAnswerGenien.service.QuestionService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI测评类 应用评分策略
 *
 * @author mortal
 * @date 2024/7/14 3:40
 */
// 这里可以根据这个注解的参数,来执行对应的doScore方法
@ScoringStrategyConfig(appType = 1, scoringStrategy = 1)
// appType = 1 测评类应用；   scoringStrategy = 0'评分策略（0-自定义，1-AI）',
public class AiTestScoringStrategy implements ScoringStrategy {
	// 分布式锁key的前缀，实际使用 + 缓存的key（这种设计的目的是确保锁与缓存数据之间的一一对应关系）
	public static final String AI_ANSWER_LOCK = "AI_ANSWER_LOCK";

	@Resource
	private QuestionService questionService;
	@Resource
	private AiManager aiManager;

	@Resource
	private RedissonClient redissonClient;

	/**
	 *  AI 评分结果的本地缓存	(配置 Caffeine 本地缓存)
	 */
	private static final Cache<String, String> answerCacheMap = Caffeine.newBuilder()
			// 配置5分钟后过期
			.expireAfterWrite(5L, TimeUnit.MINUTES)
			.maximumSize(100)
			.build();

	/**
	 * 系统的提词prompt，
	 */
	private static final String AI_TEST_SCORING_SYSTEM_MESSAGE = "你是一位严谨的判题专家，我会给你如下信息：\n" +
			"```\n" +
			"应用名称，\n" +
			"【【【应用描述】】】，\n" +
			"题目和用户回答的列表：格式为 [{\"title\": \"题目\",\"answer\": \"用户回答\"}]\n" +
			"```\n" +
			"\n" +
			"请你根据上述信息，按照以下步骤来对用户进行评价：\n" +
			"1. 要求：需要给出一个明确的评价结果，包括评价名称（尽量简短）和评价描述（尽量详细，大于 2     字）\n" +
			"2. 严格按照下面的 json 格式输出评价名称和评价描述\n" +
			"```\n" +
			"{\"resultName\": \"评价名称\", \"resultDesc\": \"评价描述\"}\n" +
			"```\n" +
			"3. 返回格式必须为 JSON 对象";

	/**
	 * 构建用户的提词prompt，
	 *
	 * @param app                    应用实体
	 * @param questionContentDTOList 题目 内容列表
	 * @param choices                用户答案选项
	 * @return
	 */
	private String getAiTestScoringUserPrompt(App app, List<QuestionContentDTO> questionContentDTOList, List<String> choices) {
		StringBuilder userMessage = new StringBuilder();
		userMessage.append(app.getAppName()).append("\n");
		userMessage.append(app.getAppDesc()).append("\n");
		// 因为我们要向ai发送的prompt（用户提示词）为：（一个案例模板）
		// MBTI 性格测试，
		//【【【快来测测你的 MBTI 性格】】】，
		//[{"title": "你通常更喜欢","answer": "独自工作"}, {"title": "当安排活动时","answer": "更愿意随机应变"}]
		List<QuestionAnswerDTO> questionAnswerDTOList = new ArrayList<>();
		for (int i = 0; i < questionContentDTOList.size(); i++) {
			QuestionAnswerDTO questionAnswerDTO = new QuestionAnswerDTO();
			// 把前端传过来的 题目答案集合，分别遍历给对应的DTO中，然后放到我们的集合中去
			questionAnswerDTO.setTitle(questionContentDTOList.get(i).getTitle());
			questionAnswerDTO.setUserAnswer(choices.get(i));
			questionAnswerDTOList.add(questionAnswerDTO);
		}
		userMessage.append(JSONUtil.toJsonStr(questionAnswerDTOList));
		return userMessage.toString();
	}

	/**
	 *  用户答题的时候，会调用这个评分模块的方法
	 *       UserAnswer userAnswerWithResult = scoringStrategyExecutor.doScore(choices, app);
	 *  （会根据注解的参数进行匹配，匹配执行就是执行这个类的评分策略，
	 *  匹配失败就执行别的类的评分方法）
	 * @param choices
	 * @param app
	 * @return
	 * @throws Exception
	 */
	@Override
	public UserAnswer doScore(List<String> choices, App app) throws Exception {
		Long appId = app.getId();
		// 用户答题的时候，走后端的答题接口：然后会将userAnswer用户答题记录的选项单独的取出来
		// userAnswer.setChoices(JSONUtil.toJsonStr(choices));
		// 根据传过来的choices用户答案和app应用的id,构建缓存key
		String choicesStr = JSONUtil.toJsonStr(choices);
		// 判断是否为同一道题目，这里就是通过缓存的key判断，因为构建key就是通过appId和用户答案构建的key
		String cacheKey = buildCacheKey(appId, choicesStr);
		// 调用本地缓存Caffeine对象,这个Caffeine实例对象就是对应着redisson的命名空间(一个实例对象就是一个命名空间)
		// 来判断其内部是否有指定key 的缓存: answerJson就是就是这个key对应的缓存的值
		String answerJson = answerCacheMap.getIfPresent(cacheKey);
		// 如果 缓存值 存在, 我们就直接从缓存中去取,然后返回给前端
		if (StrUtil.isNotBlank(answerJson)) {
			// 存在的话,我们直接取就好了,将 json的缓存值转换为对应的Java对象,
			// 然后将我们接口层 的choices, App app的属性设置给 json缓存解析后的Java对象
			UserAnswer userAnswer = JSONUtil.toBean(answerJson, UserAnswer.class);
			// ai生成完之后,我们截取json代码部分,然后存入缓存
			// 由于种入的缓存没有关于app的属性,例如userAnswer中的appId属性就没有,
			// 所以我们获取缓存后,记得设置其appId等属性
			userAnswer.setAppId(appId);
			userAnswer.setAppType(app.getAppType());
			userAnswer.setScoringStrategy(app.getScoringStrategy());
			// 设置用户的答案属性对象: 例如:["D","C","B","D","A","C","C","B","C","A"]
			userAnswer.setChoices(choicesStr);
			return userAnswer;
		}

		// 在实际开发中，当分布式锁与缓存结合使用时，通常会将分布式锁的 key 设计成：包含缓存 key 的形式。
		// 这种设计的目的是确保锁与缓存数据之间的一一对应关系，从而避免不同操作之间的冲突。
		// 1. 先获取锁对象（没有就创建)
		RLock lock = redissonClient.getLock(AI_ANSWER_LOCK + cacheKey);


		try {

		// 2. 指定 抢锁时间和过期时间（抢到了锁，在指定的leaseTime后会自动过期），尝试获取锁
			boolean res = lock.tryLock(3, 15, TimeUnit.SECONDS);
			if (!res) {
				// 如果res为false，代表在3秒内获取锁失败
				return null;
			}
			// 为true，代表抢到了锁，执行后面的逻辑

			// 如果缓存不存在,那么 就调AI,然后将userAnswer用户答题记录对象生成出来
			// ai生成完之后,我们截取json代码部分,然后存入缓存

			// 1. 根据题目的id查询到题目和题目的 评分结果信息
			Question question = questionService.getOne(
					// Question::getAppId一个方法引用，
					// 注意：mybatisplus的方法应用就是定位字段名的（不是和集合一样取值）
					Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId));
			QuestionVO questionVO = QuestionVO.objToVo(question);
			// 创建QuestionVO就是为了方便那 答案列表
			List<QuestionContentDTO> questionContentDTOList = questionVO.getQuestionContent();
			// 调用ai获取结果
			// 构建用户输入的prompt，然后发送给ai
			String aiTestScoringUserPrompt = getAiTestScoringUserPrompt(app, questionContentDTOList, choices);
			String result = aiManager.doSyncStableRequest(AI_TEST_SCORING_SYSTEM_MESSAGE, aiTestScoringUserPrompt);
			// 截取返回的json数据 （刚刚在ai控制面板中看到返回的json是对象，而不是数组类型
			int start = StrUtil.indexOf(result, '{');
			int end = StrUtil.lastIndexOf(result, "}", result.length() - 1, true);
			String json = StrUtil.sub(result, start, end + 1);

			// ai生成完之后,我们截取json代码部分,然后存入缓存
			// 由于种入的缓存没有关于app的属性,例如userAnswer中的appId属性就没有,
			// 所以我们获取缓存后,记得设置其appId等属性
			answerCacheMap.put(cacheKey, json);

			// 转换为 UserAnswer对象，
			UserAnswer userAnswer = JSONUtil.toBean(json, UserAnswer.class);
//
			userAnswer.setAppId(appId);
			userAnswer.setAppType(app.getAppType());
			userAnswer.setChoices(JSONUtil.toJsonStr(choices));
			return userAnswer;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			// 即使在锁的使用过程中抛出了异常，也能够确保锁被正确释放，避免出现死锁的情况
			// 注意释放锁的时候：要判断
			// 如果锁对象不为空，且锁对象是被锁的状态，然后判断锁对象是本人能释放
			if (lock != null && lock.isLocked()) {
//				这个判断是为了确保当前线程确实持有该锁。
//				如果锁是被其他线程持有的，那么当前线程不应该尝试解锁，因为解锁可能导致其他线程的同步问题
				if (lock.isHeldByCurrentThread()) {
					lock.unlock();
				}
			}

		}
	}

	/**
	 *  构建缓存key，key一样就是同一道题和同样的答案
	 * @param appId
	 * @param choices
	 * @return
	 */
	public String buildCacheKey(Long appId, String choices) {
		// 需求：“用户对同样的题目做出同样的选择，理论会得到一样的解答”
		//所以可以将应用 id 和用户的答案列表作为 key。
		return DigestUtil.md5Hex(appId + ":" + choices);
}
















}
