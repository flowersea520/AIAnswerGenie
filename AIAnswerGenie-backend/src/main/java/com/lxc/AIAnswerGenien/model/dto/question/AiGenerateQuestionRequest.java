package com.lxc.AIAnswerGenien.model.dto.question;

import lombok.Data;

import java.io.Serializable;

/**
 *  AI生成题目的DTO请求
 * @author mortal
 * @date 2024/7/24 7:11
 */
@Data
public class AiGenerateQuestionRequest implements Serializable {

	/**
	 *  应用id（在dto中都是用 id拿到对应的实体信息对象）
	 */
	private Long appId;

	/**
	 *  题目数 (给其默认10道）
	 */
	private int questionNumber = 10;

	/**
	 *  选项数（选项数默认为2，因为有些选项让其选  是 or 否
	 */
	private int optionNumber = 2;

	private static final long serialVersionUID = 131813957244496808L;
}
