package com.lxc.AIAnswerGenien.scoring;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lxc.AIAnswerGenien.model.dto.question.QuestionContentDTO;
import com.lxc.AIAnswerGenien.model.entity.App;
import com.lxc.AIAnswerGenien.model.entity.Question;
import com.lxc.AIAnswerGenien.model.entity.ScoringResult;
import com.lxc.AIAnswerGenien.model.entity.UserAnswer;
import com.lxc.AIAnswerGenien.model.vo.QuestionVO;
import com.lxc.AIAnswerGenien.service.QuestionService;
import com.lxc.AIAnswerGenien.service.ScoringResultService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义测评类 应用评分策略
 *
 * @author mortal
 * @date 2024/7/14 3:40
 */
@ScoringStrategyConfig(appType = 1, scoringStrategy = 0)
public class CustomTestScoringStrategy implements ScoringStrategy {
	@Resource
	private QuestionService questionService;

	@Resource
	private ScoringResultService scoringResultService;


	@Override
	public UserAnswer doScore(List<String> choices, App app) throws Exception {
		Long appId = app.getId();
		// 1. 根据题目的id查询到题目和题目的 评分结果信息
		Question question = questionService.getOne(
				// Question::getAppId一个方法引用，
				// 注意：mybatisplus的方法应用就是定位字段名的（不是和集合一样取值）
				Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
		);
		// 根据应用id，查评分结果表可以查到多条记录，这里用集合
		List<ScoringResult> scoringResultList = scoringResultService.list(
				Wrappers.lambdaQuery(ScoringResult.class).eq(ScoringResult::getAppId, appId)
		);
		// 2. 统计用户每个选择对应的属性个数，如：I = 10哥， E = 5个
		// 初始化一个对象，用于存储每个选项的计数
		Map<String, Integer> optionCount = new HashMap<>();
		QuestionVO questionVO = QuestionVO.objToVo(question);
		// 创建QuestionVO就是为了方便那 答案列表
		List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();
		// 每个DTO中的题目对象（嵌套了一个Options数组）：    {
		//        "options": [
		//            {
		//                "result": "I",
		//                "value": "独自工作",
		//                "key": "A"
		//            },
		//            {
		//                "result": "E",
		//                "value": "与他人合作",
		//                "key": "B"
		//            }
		//        ],
		//        "title": "1. 你通常更喜欢"
		//    },
		// 遍历题目列表（增强for遍历）
		for (QuestionContentDTO questionContentDTO : questionContent) {
			// 遍历答案列表
			for (String answer : choices) {
				// 遍历题目中的选项（每个元素的类型是DTO中嵌套类的类型
				for (QuestionContentDTO.Option option : questionContentDTO.getOptions()) {
					// 如果用户的答案answer  和选项列表中元素的 选项key匹配
					if (option.getKey().equals(answer)) {
						// 获取选项的result属性
						String result = option.getResult();

						// 如果result属性不在optionCount中，初始化为0
						if (!optionCount.containsKey(result)) {
							optionCount.put(result, 0);
						}

						// 在optionCount中增加计数
						optionCount.put(result, optionCount.get(result) + 1);
					}
				}
			}
		}


		// 3. 遍历每种评分结果，计算哪个结果的得分更高
		// 初始化最高分数和最高分数对应的评分结果
		int maxScore = 0;
		ScoringResult maxScoringResult = scoringResultList.get(0);

		// 遍历评分结果列表
		for (ScoringResult result : scoringResultList) {
			List<String> resultProps = JSONUtil.toList(result.getResultProp(), String.class);
			// 计算当前评分结果的分数
			int score = resultProps.stream()
					// 集合流中的 mapToInt用于将Stream中的元素转换为int类型
					// Map集合中的：getOrDefault这个方法尝试从Map中获取与prop键对应的值。
					// 如果找到了，就返回该值；如果没有找到，就返回默认值0。
					// optionCount是存储每个选项key的 一个计数器，I = 10个， E = 5个
					// sum()：对经过上述转换后的所有int值进行求和，得到最终的总分数。，例如 [I,E] => [10,5]  => 15
					.mapToInt(prop -> optionCount.getOrDefault(prop, 0)).sum();

			// 如果分数高于当前最高分数，更新最高分数和最高分数对应的评分结果
			if (score > maxScore) {
				maxScore = score;
				maxScoringResult = result;
			}
		}
		// 4. 构造返回值，填充答案对象的属性
		UserAnswer userAnswer = new UserAnswer();
		// 注意这里不要setScore，因为这个上面的score是计数器的score用来计算resultProp出现多少次
		userAnswer.setAppId(appId);
		userAnswer.setAppType(app.getAppType());
		userAnswer.setScoringStrategy(app.getScoringStrategy());
		userAnswer.setChoices(JSONUtil.toJsonStr(choices));
		userAnswer.setResultId(maxScoringResult.getId());
		userAnswer.setResultName(maxScoringResult.getResultName());
		userAnswer.setResultDesc(maxScoringResult.getResultDesc());
		userAnswer.setResultPicture(maxScoringResult.getResultPicture());
		return userAnswer;
	}
}
