package com.lxc.AIAnswerGenien.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxc.AIAnswerGenien.annotation.AuthCheck;
import com.lxc.AIAnswerGenien.common.BaseResponse;
import com.lxc.AIAnswerGenien.common.DeleteRequest;
import com.lxc.AIAnswerGenien.common.ErrorCode;
import com.lxc.AIAnswerGenien.common.ResultUtils;
import com.lxc.AIAnswerGenien.constant.UserConstant;
import com.lxc.AIAnswerGenien.exception.BusinessException;
import com.lxc.AIAnswerGenien.exception.ThrowUtils;
import com.lxc.AIAnswerGenien.manager.AiManager;
import com.lxc.AIAnswerGenien.model.dto.question.*;
import com.lxc.AIAnswerGenien.model.entity.App;
import com.lxc.AIAnswerGenien.model.entity.Question;
import com.lxc.AIAnswerGenien.model.entity.User;
import com.lxc.AIAnswerGenien.model.enums.AppTypeEnum;
import com.lxc.AIAnswerGenien.model.vo.QuestionVO;
import com.lxc.AIAnswerGenien.service.AppService;
import com.lxc.AIAnswerGenien.service.QuestionService;
import com.lxc.AIAnswerGenien.service.UserService;
import com.zhipu.oapi.service.v4.model.ModelData;
import io.reactivex.Flowable;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 题目接口
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

	@Resource
	private QuestionService questionService;

	@Resource
	private UserService userService;

	@Resource
	private AppService appService;

	@Resource
	private AiManager aiManager;

	/**
	 * 注入：管理vip线程执行任务对象（里面指定了10个核心线程数）
	 */
	@Resource
	private Scheduler vipScheduler;


	// region 增删改查

	/**
	 * 创建题目
	 *
	 * @param questionAddRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/add")
	public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(questionAddRequest == null, ErrorCode.PARAMS_ERROR);
		// todo 在此处将实体类和 DTO 进行转换
		Question question = new Question();
		BeanUtils.copyProperties(questionAddRequest, question);
		// 有一个属性类型不同，没拷贝上，所以我们单独拷贝
		question.setQuestionContent(JSONUtil.toJsonStr(questionAddRequest.getQuestionContent()));
		// 数据校验
		questionService.validQuestion(question, true);
		// todo 填充默认值
		User loginUser = userService.getLoginUser(request);
		question.setUserId(loginUser.getId());
		// 写入数据库
		boolean result = questionService.save(question);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		// 返回新写入的数据 id
		long newQuestionId = question.getId();
		return ResultUtils.success(newQuestionId);
	}

	/**
	 * 删除题目
	 *
	 * @param deleteRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.getLoginUser(request);
		long id = deleteRequest.getId();
		// 判断是否存在
		Question oldQuestion = questionService.getById(id);
		ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可删除
		if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = questionService.removeById(id);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 更新题目（仅管理员可用）
	 *
	 * @param questionUpdateRequest
	 * @return
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
		if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		Question question = new Question();
		BeanUtils.copyProperties(questionUpdateRequest, question);
		// 有一个属性类型不同，没拷贝上，所以我们单独拷贝
		question.setQuestionContent(JSONUtil.toJsonStr(questionUpdateRequest.getQuestionContent()));
		// 数据校验
		questionService.validQuestion(question, false);
		// 判断是否存在
		long id = questionUpdateRequest.getId();
		Question oldQuestion = questionService.getById(id);
		ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
		// 操作数据库
		boolean result = questionService.updateById(question);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 根据 id 获取题目（封装类）
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/get/vo")
	public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Question question = questionService.getById(id);
		ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(questionService.getQuestionVO(question, request));
	}

	/**
	 * 分页获取题目列表（仅管理员可用）
	 *
	 * @param questionQueryRequest
	 * @return
	 */
	@PostMapping("/list/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
		long current = questionQueryRequest.getCurrent();
		long size = questionQueryRequest.getPageSize();
		// 查询数据库
		Page<Question> questionPage = questionService.page(new Page<>(current, size),
				questionService.getQueryWrapper(questionQueryRequest));
		return ResultUtils.success(questionPage);
	}

	/**
	 * 分页获取题目列表（封装类） -- 像这种封装的VO类型，我们都是用在前端的page页面上（非管理员）
	 *
	 * @param questionQueryRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
															   HttpServletRequest request) {
		long current = questionQueryRequest.getCurrent();
		long size = questionQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<Question> questionPage = questionService.page(new Page<>(current, size),
				questionService.getQueryWrapper(questionQueryRequest));
		// 获取封装类
		return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
	}

	/**
	 * 分页获取当前登录用户创建的题目列表
	 *
	 * @param questionQueryRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/my/list/page/vo")
	public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
																 HttpServletRequest request) {
		ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);
		// 补充查询条件，只查询当前登录用户的数据
		User loginUser = userService.getLoginUser(request);
		questionQueryRequest.setUserId(loginUser.getId());
		long current = questionQueryRequest.getCurrent();
		long size = questionQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<Question> questionPage = questionService.page(new Page<>(current, size),
				questionService.getQueryWrapper(questionQueryRequest));
		// 获取封装类
		return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
	}

	/**
	 * 编辑题目（给用户使用）
	 *
	 * @param questionEditRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/edit")
	public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
		if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		Question question = new Question();
		BeanUtils.copyProperties(questionEditRequest, question);
		// 有一个属性类型不同，没拷贝上，所以我们单独拷贝
		question.setQuestionContent(JSONUtil.toJsonStr(questionEditRequest.getQuestionContent()));
		// 数据校验
		questionService.validQuestion(question, false);
		User loginUser = userService.getLoginUser(request);
		// 判断是否存在
		long id = questionEditRequest.getId();
		Question oldQuestion = questionService.getById(id);
		ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可编辑
		if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = questionService.updateById(question);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	// endregion


	// region AI生成题目功能

	/**
	 * AI生成问题的预设条件
	 */
	// 来个system prompt的常量（就是角色设定）
	private static final String Question_SYSTEM_PROMPT = "你是一位严谨的出题专家，我会给你如下信息：\n" +
			"```\n" +
			"应用名称，\n" +
			"【【【应用描述】】】，\n" +
			"应用类别，\n" +
			"要生成的题目数，\n" +
			"每个题目的选项数\n" +
			"```\n" +
			"\n" +
			"请你根据上述信息，按照以下步骤来出题：\n" +
			"1. 要求：题目和选项尽可能地短，题目不要包含序号，每题的选项数以我提供的为主，题目不能重复\n" +
			"2. 严格按照下面的 json 格式输出题目和选项\n" +
			"```\n" +
			"[{\"options\":[{\"value\":\"选项内容\",\"key\":\"A\"},{\"value\":\"\",\"key\":\"B\"}],\"title\":\"题目标题\"}]\n" +
			"```\n" +
			"title 是题目，options 是选项，每个选项的 key 按照英文字母序（比如 A、B、C、D）以此类推，value 是选项内容\n" +
			"3. 检查题目是否包含序号，若包含序号则去除序号\n" +
			"4. 返回的题目列表格式必须为 JSON 数组";

	/**
	 * 生成题目的用户 prompt
	 * （所谓的用户提词就是给ai模型发送的消息 -- 所以肯定是动态的，我们才定义方法，而不是作为一个常量写死）
	 *
	 * @param app            前端传过来到DTO中的appId
	 * @param questionNumber 前端传过来到DTO中的题目数
	 * @param optionNumber   前端传过来到DTO中的 选项数
	 * @return
	 */
	// 一般涉及到 user_prompt用户提词的时候，那个格式最好定一个一个方法，
	// 因为要加 \n，而且是动态的获取（不是固定的内容，和系统提词不一样）
	private String getGenerateQuestionUserPrompt(App app, int questionNumber, int optionNumber) {
		StringBuilder userMessage = new StringBuilder();
		// 参考用户prompt的模板，写下面的代码（逗号可省）：
		// MBTI 性格测试，
		//【【【快来测测你的 MBTI 性格】】】，
		//测评类，
		//10，
		//3
		userMessage.append(app.getAppName()).append("\n");
		userMessage.append(app.getAppDesc()).append("\n");
		userMessage.append(AppTypeEnum.getEnumByValue(app.getAppType()).getText() + "类").append("\n");
		userMessage.append(questionNumber).append("\n");
		userMessage.append(optionNumber);
		return userMessage.toString();
	}

	@PostMapping("/ai_generate")
	public BaseResponse<List<QuestionContentDTO>> aiGenerateQuestion(@RequestBody AiGenerateQuestionRequest aiGenerateQuestionRequest) {
		ThrowUtils.throwIf(aiGenerateQuestionRequest == null, ErrorCode.PARAMS_ERROR);
		// 获取参数
		Long appId = aiGenerateQuestionRequest.getAppId();
		int questionNumber = aiGenerateQuestionRequest.getQuestionNumber();
		int optionNumber = aiGenerateQuestionRequest.getOptionNumber();
		App app = appService.getById(appId);
		ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
		// 封装 Prompt
		String userMessage = getGenerateQuestionUserPrompt(app, questionNumber, optionNumber);
		// AI 生成
		String result = aiManager.doSyncUnstableRequest(Question_SYSTEM_PROMPT, userMessage);
		// 结果处理
		int start = result.indexOf("[");
		int end = result.lastIndexOf("]");
		String json = result.substring(start, end + 1);
		List<QuestionContentDTO> questionContentDTOList = JSONUtil.toList(json, QuestionContentDTO.class);
		return ResultUtils.success(questionContentDTOList);
	}

	/**
	 * 向前端发送sse (就是对应的sse对象的send方法), 前端可以实时接收数据 (注意一定要用get请求)
	 * 前端这样和后端建立sse连接: const eventSource = new EventSource('http://localhost:8080/sse');
	 *
	 * @param aiGenerateQuestionRequest
	 * @param request  根据登录态判断当前 用户是否为 vip
	 * @return
	 */
	@GetMapping("/ai_generate/sse")
	public SseEmitter aiGenerateQuestionSSE(AiGenerateQuestionRequest aiGenerateQuestionRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(aiGenerateQuestionRequest == null, ErrorCode.PARAMS_ERROR);
		// 获取参数
		Long appId = aiGenerateQuestionRequest.getAppId();
		int questionNumber = aiGenerateQuestionRequest.getQuestionNumber();
		int optionNumber = aiGenerateQuestionRequest.getOptionNumber();
		App app = appService.getById(appId);
		ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
		// 封装 Prompt
		String userMessage = getGenerateQuestionUserPrompt(app, questionNumber, optionNumber);
		// 调用ai前，创建sse对象,确保一旦 AI 服务完成处理，`SseEmitter` 已经处于可用状态。
		SseEmitter sseEmitter = new SseEmitter();
		// AI 生成
		Flowable<ModelData> modelDataFlowable = aiManager.doStreamRequest(Question_SYSTEM_PROMPT, userMessage, null);
//		用于处理原子操作的整数值。原子操作是指在多线程环境下执行的操作不会被其他线程干扰的操作，
		// 这个对象是一个计数器, 用来算括号的,如果是 {左括号就 + 1, 右括号 -1的操作
		AtomicInteger counter = new AtomicInteger(0);
		StringBuilder stringBuilder = new StringBuilder();
		// 获取登录用户
		User loginUser = userService.getLoginUser(request);
		// 默认的全局线程池，给普通用户用的（给其一个核心线程）Scheduler对象就是管理线程执行任务的对象
		Scheduler scheduler = Schedulers.single();
		// 我们user数据表的角色为：'用户角色：user/admin/ban',
		// todo 我们其实可以给其添加一个 vip角色，这里我们还是用vip模拟一下，实际可以自己扩展
		boolean isVip = loginUser.getUserRole().equals("vip");
		if (isVip) {
			// 如果是 vip，那么我们就将默认的线程管理对象，改成我们自定义的vip（有10个核心线程）
			scheduler = vipScheduler;
		}
		// 给观察者绑定线程池
		modelDataFlowable.observeOn(scheduler)
				// 使用map数据转换，就是取出：第一个响应对象的文本内容
				// choices 是一个包含多个响应对象的数组。每个响应对象表示模型生成的一个可能输出。
				//● 每个 choice 对象 代表数组中的一个具体响应对象，包括一个 delta 对象，而 delta 对象包含实际的生成内容 content。
				.map(modelData -> modelData.getChoices().get(0).getDelta().getContent())
//				message 是 modelData 对象中 getContent() 方法返回的内容。(因为是流式操作，所以拿到了数据流）
//				将 message 字符串中的所有空白字符（包括空格、制表符和换行符等）都替换为空字符串，即删除这些空白字符。
				.map(message -> message.replaceAll("\\s", ""))
				// 将我们需要的非空字符串过滤出来
				.filter(message -> StrUtil.isNotBlank(message))
				// flatMap： 一种“分流”的操作  ，然后将其合并
				// 适用于将一个数据项转换为多个数据项的情况，例如拆分一个字符串为多个字符。
				.flatMap(message -> {
//					Character 是一个封装类，用于表示字符数据类型 (char)
					ArrayList<Character> characters = new ArrayList<>();
					// 遍历字符串的方法：是将其转换为字符数组，然后使用for遍历
					for (char c : message.toCharArray()) {
						// 将每个字符添加到字符集合中去
						characters.add(c);
					}
					// （Flowable.fromIterable 方法的主要作用就是将一个集合（实现了 Iterable 接口的对象）
					// 转换为一个数据流（Flowable）。
					Flowable<Character> characterFlowable = Flowable.fromIterable(characters);
					// 返回字符 数据流
					return characterFlowable;
				}).doOnNext(c -> {
//					// 【算法思路：定义一个计数器，有左括号 + 1，有右括号 -1，当计数器的值等于0的时候，就是左括号等于右括号】
					if (c == '{') {
						// 如果是 左括号, 那么我们的计数器就 + 1
						counter.addAndGet(1);
					}
					// 如果计数器 > 0 那么说明可以一直拼接字符串了
					if (counter.get() > 0) {
						stringBuilder.append(c);
					}
					if (c == '}') {
						// 如果 是 右括号,那么计数器就 -1
						counter.addAndGet(-1);
						// 如果发现 -1 之后  == 0了,说明 括号有效 (匹配成功) 就是完整的一道题了
						if (counter.get() == 0) {
							// 输出当前线程名称（这里就是为了测试 普通用户/vip用户 的异步线程）
							System.out.println(Thread.currentThread().getName());
							// 模拟普通用户阻塞
							if (isVip) {
								Thread.sleep(10000L);
							}
							String question = stringBuilder.toString();
							// 将题目发送给前端 (通过sse,数据可以是任意对象)
							// 这样可以做到,智谱ai每给我们返回一道题,我们就可以返回给前端了
							sseEmitter.send(question);
							// 重置字符串拼接器,准备拼接下一道题
							// setLength(0)方法将字符串构造器的长度设置为0
							// 这个操作会将 StringBuilder 的内容清空，变成一个空的字符串
							stringBuilder.setLength(0);
						}
					}

				}).doOnError((e) -> log.error("sse, error", e))
				.doOnComplete(() -> {
//					sse对象的 complete()方法: 完成 SSE 连接(返回给前端用的)
					sseEmitter.complete();
				})
				// SUBSCRIBE() 方法: 调用 subscribe() 方法来订阅 Flowable 对象时，你实际上是创建了一个订阅者对象（Observer）
				// 作用: 将一个观察者 / 订阅者（Observer）连接到一个 Flowable 对象，
				// 使其能够接收数据项、错误通知或完成通知。
				// 返回值: 返回一个 Disposable 对象，可以用于取消订阅。
				.subscribe();
		return sseEmitter;
	}

	/**
	 *  废弃的方法，仅供测试用
	 * @param aiGenerateQuestionRequest
	 * @param isVip
	 * @return
	 */
	@Deprecated
	@GetMapping("/ai_generate/sse/test")
	public SseEmitter aiGenerateQuestionSSETest(AiGenerateQuestionRequest aiGenerateQuestionRequest,
												boolean isVip) {
		ThrowUtils.throwIf(aiGenerateQuestionRequest == null, ErrorCode.PARAMS_ERROR);
		// 获取参数
		Long appId = aiGenerateQuestionRequest.getAppId();
		int questionNumber = aiGenerateQuestionRequest.getQuestionNumber();
		int optionNumber = aiGenerateQuestionRequest.getOptionNumber();
		App app = appService.getById(appId);
		ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
		// 封装 Prompt
		String userMessage = getGenerateQuestionUserPrompt(app, questionNumber, optionNumber);
		// 调用ai前，创建sse对象,确保一旦 AI 服务完成处理，`SseEmitter` 已经处于可用状态。
		SseEmitter sseEmitter = new SseEmitter();
		// AI 生成
		Flowable<ModelData> modelDataFlowable = aiManager.doStreamRequest(Question_SYSTEM_PROMPT, userMessage, null);
//		用于处理原子操作的整数值。原子操作是指在多线程环境下执行的操作不会被其他线程干扰的操作，
		// 这个对象是一个计数器, 用来算括号的,如果是 {左括号就 + 1, 右括号 -1的操作
		AtomicInteger counter = new AtomicInteger(0);
		StringBuilder stringBuilder = new StringBuilder();
		// 默认的全局线程池，给普通用户用的（给其一个核心线程）Scheduler对象就是管理线程执行任务的对象
		Scheduler scheduler = Schedulers.single();
		if (isVip) {
			// 如果是 vip，那么我们就将默认的线程管理对象，改成我们自定义的vip（有10个核心线程）
			scheduler = vipScheduler;
		}
		// 给观察者绑定线程池
		modelDataFlowable.observeOn(scheduler)
				// 使用map数据转换，就是取出：第一个响应对象的文本内容
				// choices 是一个包含多个响应对象的数组。每个响应对象表示模型生成的一个可能输出。
				//● 每个 choice 对象 代表数组中的一个具体响应对象，包括一个 delta 对象，而 delta 对象包含实际的生成内容 content。
				.map(modelData -> modelData.getChoices().get(0).getDelta().getContent())
//				message 是 modelData 对象中 getContent() 方法返回的内容。(因为是流式操作，所以拿到了数据流）
//				将 message 字符串中的所有空白字符（包括空格、制表符和换行符等）都替换为空字符串，即删除这些空白字符。
				.map(message -> message.replaceAll("\\s", ""))
				// 将我们需要的非空字符串过滤出来
				.filter(message -> StrUtil.isNotBlank(message))
				// flatMap： 一种“分流”的操作  ，然后将其合并
				// 适用于将一个数据项转换为多个数据项的情况，例如拆分一个字符串为多个字符。
				.flatMap(message -> {
//					Character 是一个封装类，用于表示字符数据类型 (char)
					ArrayList<Character> characters = new ArrayList<>();
					// 遍历字符串的方法：是将其转换为字符数组，然后使用for遍历
					for (char c : message.toCharArray()) {
						// 将每个字符添加到字符集合中去
						characters.add(c);
					}
					// （Flowable.fromIterable 方法的主要作用就是将一个集合（实现了 Iterable 接口的对象）
					// 转换为一个数据流（Flowable）。
					Flowable<Character> characterFlowable = Flowable.fromIterable(characters);
					// 返回字符 数据流
					return characterFlowable;
				}).doOnNext(c -> {
//					// 【算法思路：定义一个计数器，有左括号 + 1，有右括号 -1，当计数器的值等于0的时候，就是左括号等于右括号】
					if (c == '{') {
						// 如果是 左括号, 那么我们的计数器就 + 1
						counter.addAndGet(1);
					}
					// 如果计数器 > 0 那么说明可以一直拼接字符串了
					if (counter.get() > 0) {
						stringBuilder.append(c);
					}
					if (c == '}') {
						// 如果 是 右括号,那么计数器就 -1
						counter.addAndGet(-1);
						// 如果发现 -1 之后  == 0了,说明 括号有效 (匹配成功) 就是完整的一道题了
						if (counter.get() == 0) {
							// 输出当前线程名称（这里就是为了测试 普通用户/vip用户 的异步线程）
							System.out.println(Thread.currentThread().getName());
							// 模拟普通用户阻塞
							if (!isVip) {
								Thread.sleep(10000L);
							}
							String question = stringBuilder.toString();
							// 将题目发送给前端 (通过sse,数据可以是任意对象)
							// 这样可以做到,智谱ai每给我们返回一道题,我们就可以返回给前端了
							sseEmitter.send(question);
							// 重置字符串拼接器,准备拼接下一道题
							// setLength(0)方法将字符串构造器的长度设置为0
							// 这个操作会将 StringBuilder 的内容清空，变成一个空的字符串
							stringBuilder.setLength(0);
						}
					}

				}).doOnError((e) -> log.error("sse, error", e))
				.doOnComplete(() -> {
//					sse对象的 complete()方法: 完成 SSE 连接(返回给前端用的)
					sseEmitter.complete();
				})
				// SUBSCRIBE() 方法: 调用 subscribe() 方法来订阅 Flowable 对象时，你实际上是创建了一个订阅者对象（Observer）
				// 作用: 将一个观察者 / 订阅者（Observer）连接到一个 Flowable 对象，
				// 使其能够接收数据项、错误通知或完成通知。
				// 返回值: 返回一个 Disposable 对象，可以用于取消订阅。
				.subscribe();
		return sseEmitter;
	}


// endregion
}