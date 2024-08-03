package com.lxc.AIAnswerGenien.mapper;

import com.lxc.AIAnswerGenien.model.dto.statistic.AppAnswerCountDTO;
import com.lxc.AIAnswerGenien.model.dto.statistic.AppAnswerResultCountDTO;
import com.lxc.AIAnswerGenien.model.entity.UserAnswer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author lxc
 * @description 针对表【user_answer(用户答题记录)】的数据库操作Mapper
 * @createDate 2024-07-10 23:32:06
 * @Entity com.lxc.AIAnswerGenien.model.entity.UserAnswer
 */
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {
	// 实施分库分表后，实际上你会操作分表后的数据，而不是直接操作原始的表。
	// 在用户答题记录表中，查询 相同的appId 对应的 用户次数（就是用户答了多少次 -- 所以不去重），然后降序排
	@Select("select appId, count(userId) as answerCount from user_answer " +
			"group by appId order by answerCount desc")
	List<AppAnswerCountDTO> getAppAnswerCount();

	// 这里不用管分表，shardingSpare会帮我们自动往我们的appId分表逻辑去查，我们啥也不用管
	// 应用回答分布统计
	//根据应用 id，统计同一个应用内用户答题结果中每个评分结果对应的数量。
	@Select("select resultName, count(resultName) as resultCount from user_answer " +
			"where appId = #{appId} group by resultName order by resultCount desc")
	List<AppAnswerResultCountDTO> getAppAnswerResultCount(Long appId);

}




