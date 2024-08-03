package com.lxc.AIAnswerGenien.manager;


import com.lxc.AIAnswerGenien.common.ErrorCode;
import com.lxc.AIAnswerGenien.exception.BusinessException;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用 AI 调用能力
 */
@Component
public class AiManager {
	// 智谱AI的客户端对象，用来调用智谱的API接口
	@Resource
	private ClientV4 clientV4;

	// 稳定的随机数
	private static final float STABLE_TEMPERATURE = 0.05f;

	// 不稳定的随机数
	private static final float UNSTABLE_TEMPERATURE = 0.99f;

	/**
	 * 通用流式请求（简化消息传递）
	 * 这个方法就是将 用户的prompt和系统的prompt放到了chatMessageList集合中去，然后传给大模型ai
	 *
	 * @param systemMessage
	 * @param userMessage
	 * @param temperature：如果传入null，则取默认值为 0.95，偏随机
	 * @return
	 */
	public Flowable<ModelData> doStreamRequest(String systemMessage, String userMessage, Float temperature) {
		// 构造请求
		List<ChatMessage> messages = new ArrayList<>();
		ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
		ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
		messages.add(systemChatMessage);
		messages.add(userChatMessage);
		return doStreamRequest(messages, temperature);
	}

	/**
	 * 通用流式请求
	 *
	 * @param messages
	 * @param temperature
	 * @return 9914545659747067
	 */
	public Flowable<ModelData> doStreamRequest(List<ChatMessage> messages, Float temperature) {
		// 构造请求
		ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
				.model(Constants.ModelChatGLM4)
				.stream(Boolean.TRUE)
				.invokeMethod(Constants.invokeMethod)
				.temperature(temperature)
				.messages(messages)
				.build();
		ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
		return invokeModelApiResp.getFlowable();
	}

	/**
	 * 同步请求（答案不稳定）
	 *
	 * @param systemMessage
	 * @param userMessage
	 * @return
	 */
	public String doSyncUnstableRequest(String systemMessage, String userMessage) {
		return doRequest(systemMessage, userMessage, Boolean.FALSE, UNSTABLE_TEMPERATURE);
	}

	/**
	 * 同步请求（答案较稳定）
	 *
	 * @param systemMessage
	 * @param userMessage
	 * @return
	 */
	public String doSyncStableRequest(String systemMessage, String userMessage) {
		return doRequest(systemMessage, userMessage, Boolean.FALSE, STABLE_TEMPERATURE);
	}

	/**
	 * 同步请求
	 *
	 * @param systemMessage
	 * @param userMessage
	 * @param temperature
	 * @return
	 */
	public String doSyncRequest(String systemMessage, String userMessage, Float temperature) {
		return doRequest(systemMessage, userMessage, Boolean.FALSE, temperature);
	}

	/**
	 * 通用请求（简化消息传递）
	 *  这个方法就是将 用户的prompt和系统的prompt放到了chatMessageList集合中去，然后传给大模型ai
	 *
	 * @param systemMessage
	 * @param userMessage
	 * @param stream
	 * @param temperature
	 * @return
	 */
	public String doRequest(String systemMessage, String userMessage, Boolean stream, Float temperature) {
		List<ChatMessage> chatMessageList = new ArrayList<>();
		ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
		chatMessageList.add(systemChatMessage);
		ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
		chatMessageList.add(userChatMessage);
		return doRequest(chatMessageList, stream, temperature);
	}

	/**
	 * 通用请求
	 *
	 * @param messages
	 * @param stream
	 * @param temperature
	 * @return
	 */
	public String doRequest(List<ChatMessage> messages, Boolean stream, Float temperature) {
		// 构建请求
		ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
				.model("glm-4-flash")
				.stream(stream)
//				temperature参数是用来调整生成文本的随机性的一个设置
//				 取值范围是：[0.0, 1.0]，默认值为 0.95，值越大，会使输出更随机，更具创造性；值越小，输出会更加稳定或确定
				.temperature(temperature)
				.invokeMethod(Constants.invokeMethod)
				.messages(messages)
				.build();
		try {
			ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
			return invokeModelApiResp.getData().getChoices().get(0).toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
		}
	}
}