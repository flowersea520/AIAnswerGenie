package com.lxc.AIAnswerGenien.controller;

import cn.hutool.core.util.IdUtil;
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
import com.lxc.AIAnswerGenien.model.dto.useranswer.UserAnswerAddRequest;
import com.lxc.AIAnswerGenien.model.dto.useranswer.UserAnswerEditRequest;
import com.lxc.AIAnswerGenien.model.dto.useranswer.UserAnswerQueryRequest;
import com.lxc.AIAnswerGenien.model.dto.useranswer.UserAnswerUpdateRequest;
import com.lxc.AIAnswerGenien.model.entity.App;
import com.lxc.AIAnswerGenien.model.entity.UserAnswer;
import com.lxc.AIAnswerGenien.model.entity.User;
import com.lxc.AIAnswerGenien.model.enums.ReviewStatusEnum;
import com.lxc.AIAnswerGenien.model.vo.UserAnswerVO;
import com.lxc.AIAnswerGenien.scoring.ScoringStrategyExecutor;
import com.lxc.AIAnswerGenien.service.AppService;
import com.lxc.AIAnswerGenien.service.UserAnswerService;
import com.lxc.AIAnswerGenien.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户答案接口
 *
 * @author <a href="https://github.com/flowersea520">程序员lxc</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@RestController
@RequestMapping("/userAnswer")
@Slf4j
public class UserAnswerController {

    @Resource
    private UserAnswerService userAnswerService;

    @Resource
    private UserService userService;

    @Resource
    private ScoringStrategyExecutor scoringStrategyExecutor;

    @Resource
    private AppService appService;


    // region 增删改查

    /**
     * 创建用户答案（做完题目，然后查看结果后就会调用这个接口，用于将用户的答题记录添加到我们的userAnswer表
     *
     *
     * @param userAnswerAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUserAnswer(@RequestBody UserAnswerAddRequest userAnswerAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userAnswerAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 在此处将实体类和 DTO 进行转换
        UserAnswer userAnswer = new UserAnswer();
        BeanUtils.copyProperties(userAnswerAddRequest, userAnswer);
        List<String> choices = userAnswerAddRequest.getChoices();
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        // 数据校验
        userAnswerService.validUserAnswer(userAnswer, true);
        // 判断app是否存在（其实在service里面我们校验了，但是这里在校验一遍也可以）
        Long appId = userAnswerAddRequest.getAppId();
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取app应用的审核状态，如果审核状态不为1，说明该应用在审核中0，或者审核被拒绝了2
        Integer reviewStatus = app.getReviewStatus();
        if (!ReviewStatusEnum.getEnumByValue(reviewStatus).equals(ReviewStatusEnum.PASS)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "app应用审核未通过");
        }
        // 填充默认值
        User loginUser = userService.getLoginUser(request);
        userAnswer.setUserId(loginUser.getId());
        // 修改提交用户答案接口，将 id 填充到回答对象中并插入到数据库
        // （默认会通过 BeanUtils.copyProperties 填充）。并且捕获主键重复的异常，直接忽略即可。
        // 写入数据库（做完题目，然后查看结果后就会调用这个接口，用于将用户的答题记录添加到我们的userAnswer表）
        try {
            // userAnswer实体的id是主键，唯一的，如果插入了重复的id，那么就会抛异常，直接忽略，执行后面的逻辑
            boolean result = userAnswerService.save(userAnswer);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        } catch (DuplicateKeyException e) {
            // 如果插入操作因唯一约束失败，意味着数据已经存在，这通常是预期行为，因此可以安全地忽略这个异常。
            // ignore error
        }
        // 返回新写入的数据 id
        long newUserAnswerId = userAnswer.getId();
        // 调用评分模块
		try {
            // 调用评分模块，查看其是否有缓存
            UserAnswer userAnswerWithResult = scoringStrategyExecutor.doScore(choices, app);
            userAnswerWithResult.setId(newUserAnswerId);
//            确保在更新操作时，不对 appId（或其他分片键字段）进行修改。
//            可以在更新时保持分片键的原始值，从而避免更新分片键导致的异常。
            userAnswerWithResult.setAppId(null);
            // 更新数据库，将AI答题生成的的新的实体（原来的基础上多一些属性值）
            // 例如会多：ISTJ（物流师） -- resultName  ,
            // 忠诚可靠，被公认为务实，注重细节。-- resultDesc
            // 主要是更新这两个属性值
            userAnswerService.updateById(userAnswerWithResult);
        } catch (Exception e) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "评分错误");
		}
		return ResultUtils.success(newUserAnswerId);
    }

    /**
     * 删除用户答案
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUserAnswer(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserAnswer oldUserAnswer = userAnswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldUserAnswer.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = userAnswerService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新用户答案（仅管理员可用）
     *
     * @param userAnswerUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserAnswer(@RequestBody UserAnswerUpdateRequest userAnswerUpdateRequest) {
        if (userAnswerUpdateRequest == null || userAnswerUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        UserAnswer userAnswer = new UserAnswer();
        BeanUtils.copyProperties(userAnswerUpdateRequest, userAnswer);
        userAnswer.setChoices(JSONUtil.toJsonStr(userAnswerUpdateRequest.getChoices()));
        // 数据校验
        userAnswerService.validUserAnswer(userAnswer, false);
        // 判断是否存在
        long id = userAnswerUpdateRequest.getId();
        UserAnswer oldUserAnswer = userAnswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = userAnswerService.updateById(userAnswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户答案（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserAnswerVO> getUserAnswerVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        UserAnswer userAnswer = userAnswerService.getById(id);
        ThrowUtils.throwIf(userAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(userAnswerService.getUserAnswerVO(userAnswer, request));
    }

    /**
     * 分页获取用户答案列表（仅管理员可用）
     *
     * @param userAnswerQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserAnswer>> listUserAnswerByPage(@RequestBody UserAnswerQueryRequest userAnswerQueryRequest) {
        long current = userAnswerQueryRequest.getCurrent();
        long size = userAnswerQueryRequest.getPageSize();
        // 查询数据库
        Page<UserAnswer> userAnswerPage = userAnswerService.page(new Page<>(current, size),
                userAnswerService.getQueryWrapper(userAnswerQueryRequest));
        return ResultUtils.success(userAnswerPage);
    }

    /**
     * 分页获取用户答案列表（封装类）
     *
     * @param userAnswerQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest userAnswerQueryRequest,
                                                               HttpServletRequest request) {
        long current = userAnswerQueryRequest.getCurrent();
        long size = userAnswerQueryRequest.getPageSize();
        // 限制爬虫（非管理员也就是用户，必须限制）
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserAnswer> userAnswerPage = userAnswerService.page(new Page<>(current, size),
                userAnswerService.getQueryWrapper(userAnswerQueryRequest));
        // 获取封装类
        return ResultUtils.success(userAnswerService.getUserAnswerVOPage(userAnswerPage, request));
    }

    /**
     * 分页获取当前登录用户创建的用户答案列表
     *
     * @param userAnswerQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listMyUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest userAnswerQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(userAnswerQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        userAnswerQueryRequest.setUserId(loginUser.getId());
        long current = userAnswerQueryRequest.getCurrent();
        long size = userAnswerQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserAnswer> userAnswerPage = userAnswerService.page(new Page<>(current, size),
                userAnswerService.getQueryWrapper(userAnswerQueryRequest));
        // 获取封装类
        return ResultUtils.success(userAnswerService.getUserAnswerVOPage(userAnswerPage, request));
    }

    /**
     * 编辑用户答案（给用户使用）
     *
     * @param userAnswerEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editUserAnswer(@RequestBody UserAnswerEditRequest userAnswerEditRequest, HttpServletRequest request) {
        if (userAnswerEditRequest == null || userAnswerEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        UserAnswer userAnswer = new UserAnswer();
        BeanUtils.copyProperties(userAnswerEditRequest, userAnswer);
        userAnswer.setChoices(JSONUtil.toJsonStr(userAnswerEditRequest.getChoices()));
        // 数据校验
        userAnswerService.validUserAnswer(userAnswer, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = userAnswerEditRequest.getId();
        UserAnswer oldUserAnswer = userAnswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldUserAnswer.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = userAnswerService.updateById(userAnswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion

    /**
     *  生成用户答案 的 id（用于幂等性）
     *  某个操作无论执行一次还是多次，其效果都是一样的
     *  在本项目的用户答题功能中，如果用户选择完答案后，快速点击答题接口，
     *  数据库可能会产生多条相同的回答记录，并且调用多次 AI。
     * 虽然调用多次 AI 的问题已经在之前通过缓存解决，
     * 但仍然要防止用户不小心点击多次导致产生多条回答记录，因此需要对接口进行幂等处理。
     * @return
     */
    @GetMapping("/generate/id")
    public BaseResponse<Long> generateUserAnswerId() {
        return ResultUtils.success(IdUtil.getSnowflakeNextId());
    }
















}
