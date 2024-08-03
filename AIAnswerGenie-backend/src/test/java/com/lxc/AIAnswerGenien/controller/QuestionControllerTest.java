package com.lxc.AIAnswerGenien.controller;

import com.lxc.AIAnswerGenien.model.dto.question.AiGenerateQuestionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author mortal
 * @date 2024/8/2 18:15
 */
@SpringBootTest
class QuestionControllerTest {

	@Resource
	private QuestionController questionController;

	@Test
	void aiGenerateQuestionSSETest() throws InterruptedException {
		AiGenerateQuestionRequest aiGenerateQuestionRequest = new AiGenerateQuestionRequest();
		aiGenerateQuestionRequest.setAppId(5L);
		aiGenerateQuestionRequest.setQuestionNumber(5);
		aiGenerateQuestionRequest.setOptionNumber(2);
		// 模拟非会员（线程1-- 异步的线程，因为我们的代码里面有rxjava）
		questionController.aiGenerateQuestionSSETest(aiGenerateQuestionRequest, false);
		// 模拟非会员（线程2 -- 每个线程都是执行任务-- 也就是对应的逻辑代码）
		questionController.aiGenerateQuestionSSETest(aiGenerateQuestionRequest, false);
		// 模拟会员
		questionController.aiGenerateQuestionSSETest(aiGenerateQuestionRequest, true);
//Thread.sleep 确保主线程在测试完成之前不会提前结束，从而允许异步任务有足够的时间完成。
//		这里的“主线程”指的是执行 aiGenerateQuestionSSETest 方法的线程
		Thread.sleep(1000000);
	}
}