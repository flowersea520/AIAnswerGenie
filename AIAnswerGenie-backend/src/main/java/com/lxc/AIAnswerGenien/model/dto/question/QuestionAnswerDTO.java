package com.lxc.AIAnswerGenien.model.dto.question;

import lombok.Data;

/**
 *  题目答案封装类 （用于AI评分）
 * @author mortal
 * @date 2024/7/25 7:25
 */
@Data
public class QuestionAnswerDTO {
	/**
	 *  题目标题（就是题目本身）
	 */
	private String title;

	/**
	 *  用户答案
	 */
	private String userAnswer;
}
