package com.lxc.AIAnswerGenien.service.impl;
import java.util.Date;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxc.AIAnswerGenien.common.ErrorCode;
import com.lxc.AIAnswerGenien.constant.CommonConstant;
import com.lxc.AIAnswerGenien.exception.ThrowUtils;
import com.lxc.AIAnswerGenien.mapper.AppMapper;
import com.lxc.AIAnswerGenien.model.dto.app.AppQueryRequest;
import com.lxc.AIAnswerGenien.model.entity.App;
import com.lxc.AIAnswerGenien.model.entity.User;
import com.lxc.AIAnswerGenien.model.enums.AppScoringStrategyEnum;
import com.lxc.AIAnswerGenien.model.enums.AppTypeEnum;
import com.lxc.AIAnswerGenien.model.enums.ReviewStatusEnum;
import com.lxc.AIAnswerGenien.model.vo.AppVO;
import com.lxc.AIAnswerGenien.model.vo.UserVO;
import com.lxc.AIAnswerGenien.service.AppService;
import com.lxc.AIAnswerGenien.service.UserService;
import com.lxc.AIAnswerGenien.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用服务实现
 *
 * @author <a href="https://github.com/flowersea520">程序员lxc</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param app
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validApp(App app, boolean add) {

        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值 (有些字段不要校验，例如在一些接口层的dto中，没有让用户传id，而我们校验是否为空，
        // 肯定是为空的，因为用户传不了，所以有些字段校验没有意义）
        String appName = app.getAppName();
        String appDesc = app.getAppDesc();
        String appIcon = app.getAppIcon();
        // 这个枚举状态码，获取其枚举对象
        Integer appType = app.getAppType();
        // 这个枚举状态码，获取其枚举对象
        Integer scoringStrategy = app.getScoringStrategy();
        // 这个枚举状态码，获取其枚举对象
        Integer reviewStatus = app.getReviewStatus();
        String reviewMessage = app.getReviewMessage();
        Long reviewerId = app.getReviewerId();
        Date reviewTime = app.getReviewTime();
        Long userId = app.getUserId();
        Date createTime = app.getCreateTime();
        Date updateTime = app.getUpdateTime();
        // 创建数据时，参数不能为空
        if (add) {
            // 校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(appName), ErrorCode.PARAMS_ERROR, "应用名称不能为空");
            ThrowUtils.throwIf(StringUtils.isBlank(appDesc), ErrorCode.PARAMS_ERROR, "应用描述不能为空");
            ThrowUtils.throwIf(StringUtils.isBlank(appIcon), ErrorCode.PARAMS_ERROR, "应用图标不能为空");
            AppScoringStrategyEnum appScoringStrategyEnum = AppScoringStrategyEnum.getEnumByValue(scoringStrategy);
            ThrowUtils.throwIf(appScoringStrategyEnum == null, ErrorCode.PARAMS_ERROR, "应用评分策略非法");
            AppTypeEnum appTypeEnum = AppTypeEnum.getEnumByValue(appType);
            ThrowUtils.throwIf(appTypeEnum == null, ErrorCode.PARAMS_ERROR, "应用类型非法");
        }
        // 修改数据时，有参数则校验
        // 校验规则
        if (StringUtils.isNotBlank(appName)) {
            // 这里用断言也可以，不过纠结这个没有意义，你有自己的一套校验规则就ok
            ThrowUtils.throwIf(appName.length() > 80, ErrorCode.PARAMS_ERROR, "应用名称过长");
        }
        if (StringUtils.isNotBlank(appDesc)) {
            ThrowUtils.throwIf(appDesc.length() > 255, ErrorCode.PARAMS_ERROR, "应用描述过长");
        }
        if (StringUtils.isNotBlank(appIcon)) {
            ThrowUtils.throwIf(appIcon.length() > 255, ErrorCode.PARAMS_ERROR, "应用图标路径过长");
        }
        if (reviewerId != null) {
            ThrowUtils.throwIf(reviewerId <= 0, ErrorCode.PARAMS_ERROR, "审核人ID有错误");
        }
        if (reviewMessage != null) {
            ThrowUtils.throwIf(reviewMessage.length() > 255, ErrorCode.PARAMS_ERROR, "审核信息过长");
        }
        if (reviewTime != null) {
            ThrowUtils.throwIf(reviewTime.after(new Date()), ErrorCode.PARAMS_ERROR, "审核时间不能在未来");
        }
        if (userId != null) {
            ThrowUtils.throwIf(userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID有错误");
        }
        if (createTime != null) {
            ThrowUtils.throwIf(createTime.after(new Date()), ErrorCode.PARAMS_ERROR, "创建时间不能在未来");
        }
        if (updateTime != null) {
            ThrowUtils.throwIf(updateTime.after(new Date()), ErrorCode.PARAMS_ERROR, "更新时间不能在未来");
        }

        // 记住，这里只有当用户填了审核状态，我们才去判断
        if (reviewStatus != null) {
            // 枚举类校验: 拿到枚举的状态码，获的其枚举对象，判断其枚举对象不为空即可
            ReviewStatusEnum reviewStatusEnum = ReviewStatusEnum.getEnumByValue(reviewStatus);
            ThrowUtils.throwIf(reviewStatusEnum == null, ErrorCode.PARAMS_ERROR, "审核状态非法");
        }

    }

    /**
     * 获取查询条件
     *
     * @param appQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest) {
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        if (appQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String appDesc = appQueryRequest.getAppDesc();
        String appIcon = appQueryRequest.getAppIcon();
        Integer appType = appQueryRequest.getAppType();
        Integer scoringStrategy = appQueryRequest.getScoringStrategy();
        Integer reviewStatus = appQueryRequest.getReviewStatus();
        String reviewMessage = appQueryRequest.getReviewMessage();
        Long reviewerId = appQueryRequest.getReviewerId();
        Long userId = appQueryRequest.getUserId();
        Long notId = appQueryRequest.getNotId();
        String searchText = appQueryRequest.getSearchText();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        // 从多字段中搜索 （例如下面的代码就是 这个搜索词，既能在 appName字段中搜，还能再appDesc中搜
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("appName", searchText).or().like("appDesc", searchText).or().like("appIcon", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(appName), "appName", appName);
        queryWrapper.like(StringUtils.isNotBlank(appDesc), "appDesc", appDesc);
        queryWrapper.like(StringUtils.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appType), "appType", appType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy), "scoringStrategy", scoringStrategy);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则 （这里默认升序）
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取应用封装
     *
     * @param app
     * @param request
     * @return
     */
    @Override
    public AppVO getAppVO(App app, HttpServletRequest request) {
        // 对象转封装类
        AppVO appVO = AppVO.objToVo(app);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = app.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        appVO.setUser(userVO);
        // endregion
        return appVO;
    }

    /**
     * 分页获取应用封装
     *
     * @param appPage
     * @param request
     * @return
     */
    @Override
    public Page<AppVO> getAppVOPage(Page<App> appPage, HttpServletRequest request) {
        List<App> appList = appPage.getRecords();
        Page<AppVO> appVOPage = new Page<>(appPage.getCurrent(), appPage.getSize(), appPage.getTotal());
        if (CollUtil.isEmpty(appList)) {
            return appVOPage;
        }
        // 对象列表 => 封装对象列表
        List<AppVO> appVOList = appList.stream().map(app -> {
            return AppVO.objToVo(app);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = appList.stream().map(App::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        appVOList.forEach(appVO -> {
            Long userId = appVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            appVO.setUser(userService.getUserVO(user));
        });
        // endregion

        appVOPage.setRecords(appVOList);
        return appVOPage;
    }

}
