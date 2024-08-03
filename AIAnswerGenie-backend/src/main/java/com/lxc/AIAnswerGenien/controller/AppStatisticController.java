package com.lxc.AIAnswerGenien.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxc.AIAnswerGenien.annotation.AuthCheck;
import com.lxc.AIAnswerGenien.common.*;
import com.lxc.AIAnswerGenien.constant.UserConstant;
import com.lxc.AIAnswerGenien.exception.BusinessException;
import com.lxc.AIAnswerGenien.exception.ThrowUtils;
import com.lxc.AIAnswerGenien.mapper.UserAnswerMapper;
import com.lxc.AIAnswerGenien.model.dto.app.AppAddRequest;
import com.lxc.AIAnswerGenien.model.dto.app.AppEditRequest;
import com.lxc.AIAnswerGenien.model.dto.app.AppQueryRequest;
import com.lxc.AIAnswerGenien.model.dto.app.AppUpdateRequest;
import com.lxc.AIAnswerGenien.model.dto.statistic.AppAnswerCountDTO;
import com.lxc.AIAnswerGenien.model.dto.statistic.AppAnswerResultCountDTO;
import com.lxc.AIAnswerGenien.model.entity.App;
import com.lxc.AIAnswerGenien.model.entity.User;
import com.lxc.AIAnswerGenien.model.enums.ReviewStatusEnum;
import com.lxc.AIAnswerGenien.model.vo.AppVO;
import com.lxc.AIAnswerGenien.service.AppService;
import com.lxc.AIAnswerGenien.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * 应用app统计分析接口
 */
@RestController
@RequestMapping("/app/statistic")
@Slf4j
public class AppStatisticController {

	// 理论上是要调用 service层的，但是呢，我们这里为了省事，就不封装了，直接调mapper
	@Resource
	private UserAnswerMapper userAnswerMapper;

	/**
	 *  热门应用及回答数统计（top10 -- 这个前端做，就展示10条）
	 * @return
	 */
	@GetMapping("/answer_count")
	public BaseResponse<List<AppAnswerCountDTO>> getAppAnswerCount() {
		List<AppAnswerCountDTO> appAnswerCount = userAnswerMapper.getAppAnswerCount();
		return ResultUtils.success(appAnswerCount);
	}

	/**
	 * 应用回答分布统计
	 * 查询指定应用app：用户答题结果中 每个  评分结果名称resultName对应的数量。
	 * @return
	 */
	@GetMapping("answer_result_count")
	public BaseResponse<List<AppAnswerResultCountDTO>> getAnswerResultCount(Long appId) {
		List<AppAnswerResultCountDTO> appAnswerResultCount = userAnswerMapper.getAppAnswerResultCount(appId);
		return ResultUtils.success(appAnswerResultCount);
	}

























}
