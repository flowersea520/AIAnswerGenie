package com.lxc.AIAnswerGenien.model.dto.statistic;

import lombok.Data;

/**
 *  应用 答案结果统计DTO
 * @author mortal
 * @date 2024/8/2 23:05
 */
@Data
public class AppAnswerResultCountDTO {
	/**
	 *  结果名称
	 */
	private String resultName;

	/**
	 *  结果统计
	 */
	private Long resultCount;

}
