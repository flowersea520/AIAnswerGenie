package com.lxc.AIAnswerGenien.model.dto.statistic;

import lombok.Data;

/**
 *  统计 应用回答的结果记录DTO类
 *  DTO用于在不同层或不同系统之间传输数据。DTO这里用户数据层的传输
 *  应用回答分布统计
 * 根据应用 id，统计同一个应用内用户答题结果中每个评分结果对应的数量。
 * @author mortal
 * @date 2024/8/2 21:48
 */
@Data
public class AppAnswerCountDTO {

	/**
	 *  应用id
	 */
	private Long appId;

	/**
	 * 用户答案记录统计数
	 */
	private Long answerCount;



}
