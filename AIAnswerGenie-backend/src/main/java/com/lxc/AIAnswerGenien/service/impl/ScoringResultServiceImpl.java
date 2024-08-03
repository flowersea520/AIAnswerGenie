package com.lxc.AIAnswerGenien.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxc.AIAnswerGenien.common.ErrorCode;
import com.lxc.AIAnswerGenien.constant.CommonConstant;
import com.lxc.AIAnswerGenien.exception.ThrowUtils;
import com.lxc.AIAnswerGenien.mapper.ScoringResultMapper;
import com.lxc.AIAnswerGenien.model.dto.scoringResult.ScoringResultQueryRequest;
import com.lxc.AIAnswerGenien.model.entity.App;
import com.lxc.AIAnswerGenien.model.entity.ScoringResult;
import com.lxc.AIAnswerGenien.model.entity.User;
import com.lxc.AIAnswerGenien.model.vo.ScoringResultVO;
import com.lxc.AIAnswerGenien.model.vo.UserVO;
import com.lxc.AIAnswerGenien.service.AppService;
import com.lxc.AIAnswerGenien.service.ScoringResultService;
import com.lxc.AIAnswerGenien.service.UserService;
import com.lxc.AIAnswerGenien.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评分结果服务实现
 *
 * @author <a href="https://github.com/flowersea520">程序员lxc</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class ScoringResultServiceImpl extends ServiceImpl<ScoringResultMapper, ScoringResult> implements ScoringResultService {

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    /**
     * 校验数据
     *
     * @param scoringResult
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validScoringResult(ScoringResult scoringResult, boolean add) {
        ThrowUtils.throwIf(scoringResult == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = scoringResult.getId();
        String resultName = scoringResult.getResultName();
        String resultPicture = scoringResult.getResultPicture();
        String resultProp = scoringResult.getResultProp();
        Integer resultScoreRange = scoringResult.getResultScoreRange();
        Long appId = scoringResult.getAppId();
        Long userId = scoringResult.getUserId();

        // 创建数据时，参数不能为空
        if (add) {
            // 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(resultName), ErrorCode.PARAMS_ERROR, "结果名称为空");
            ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用ID为空");
        }

        // 修改数据时，有参数则校验
        // 校验 resultName
        if (StringUtils.isNotBlank(resultName)) {
            ThrowUtils.throwIf(resultName.length() > 128, ErrorCode.PARAMS_ERROR, "结果名称较长");
        }

        // 校验 resultPicture
        if (StringUtils.isNotBlank(resultPicture)) {
            ThrowUtils.throwIf(resultPicture.length() > 1024, ErrorCode.PARAMS_ERROR, "结果图片URL较长");
        }

        // 校验 resultProp
        if (StringUtils.isNotBlank(resultProp)) {
            ThrowUtils.throwIf(resultProp.length() > 128, ErrorCode.PARAMS_ERROR, "结果属性集合较长");
            // 校验 JSON 格式是否正确
            // parseArray(String json) 方法将输入的 JSON 字符串解析为一个 JSONArray 对象。
            // JSONArray 是 Hutool 中的一个类，用于表示 JSON 数组。
            JSONArray jsonArray = JSONUtil.parseArray(resultProp);
            ThrowUtils.throwIf(jsonArray.size() > 4, ErrorCode.PARAMS_ERROR, "结果选项过长");
        }

        // 校验 resultScoreRange
        if (resultScoreRange != null) {
            ThrowUtils.throwIf(resultScoreRange < 0 || resultScoreRange > 100, ErrorCode.PARAMS_ERROR, "结果得分范围不合法");
        }

        // 校验 appId
        if (appId != null) {
            // 从对应service获取其应用
            App app = appService.getById(appId);
            // 然后校验 从数据库是否获取对象成功
            ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "app应用为空");

        }

        // 校验 userId
        if (userId != null) {
            // 可以添加其他校验规则，如是否存在对应的用户ID
            User user = userService.getById(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户为空");
        }
    }


    /**
     * 获取查询条件
     *
     * @param scoringResultQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<ScoringResult> getQueryWrapper(ScoringResultQueryRequest scoringResultQueryRequest) {
        QueryWrapper<ScoringResult> queryWrapper = new QueryWrapper<>();
        if (scoringResultQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = scoringResultQueryRequest.getId();
        String resultName = scoringResultQueryRequest.getResultName();
        String resultDesc = scoringResultQueryRequest.getResultDesc();
        String resultPicture = scoringResultQueryRequest.getResultPicture();
        String resultProp = scoringResultQueryRequest.getResultProp();
        Integer resultScoreRange = scoringResultQueryRequest.getResultScoreRange();
        Long appId = scoringResultQueryRequest.getAppId();
        Long userId = scoringResultQueryRequest.getUserId();
        Long notId = scoringResultQueryRequest.getNotId();
        String searchText = scoringResultQueryRequest.getSearchText();
        String sortField = scoringResultQueryRequest.getSortField();
        String sortOrder = scoringResultQueryRequest.getSortOrder();

        // todo 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("resultName", resultName).or().like("resultDesc", searchText));
        }
        // 模糊查询 （这里就是构造查询条件）
        queryWrapper.like(StringUtils.isNotBlank(resultName), "resultName", resultName);
        queryWrapper.like(StringUtils.isNotBlank(resultDesc), "resultDesc", resultDesc);
        queryWrapper.like(StringUtils.isNotBlank(resultProp), "resultProp", resultProp);
        // 精确查询
        // 构建查询条件 id != notId
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(resultPicture), "resultPicture", resultPicture);
        queryWrapper.eq(ObjectUtils.isNotEmpty(resultScoreRange), "resultScoreRange", resultScoreRange);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "appId", appId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取评分结果封装
     *
     * @param scoringResult
     * @param request
     * @return
     */
    @Override
    public ScoringResultVO getScoringResultVO(ScoringResult scoringResult, HttpServletRequest request) {
        // 对象转封装类
        ScoringResultVO scoringResultVO = ScoringResultVO.objToVo(scoringResult);

        //  可以根据需要为封装对象补充值，不需要的内容可以删除
        // 1. 关联查询用户信息
        Long userId = scoringResult.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        scoringResultVO.setUser(userVO);
        return scoringResultVO;
    }

    /**
     * 分页获取评分结果封装
     *
     * @param scoringResultPage
     * @param request
     * @return
     */
    @Override
    public Page<ScoringResultVO> getScoringResultVOPage(Page<ScoringResult> scoringResultPage, HttpServletRequest request) {
        List<ScoringResult> scoringResultList = scoringResultPage.getRecords();
        Page<ScoringResultVO> scoringResultVOPage = new Page<>(scoringResultPage.getCurrent(), scoringResultPage.getSize(), scoringResultPage.getTotal());
        if (CollUtil.isEmpty(scoringResultList)) {
            return scoringResultVOPage;
        }
        // 对象列表 => 封装对象列表
        List<ScoringResultVO> scoringResultVOList = scoringResultList.stream().map(scoringResult -> {
            return ScoringResultVO.objToVo(scoringResult);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = scoringResultList.stream().map(ScoringResult::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        scoringResultVOList.forEach(scoringResultVO -> {
            Long userId = scoringResultVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            scoringResultVO.setUser(userService.getUserVO(user));
        });
        // endregion

        scoringResultVOPage.setRecords(scoringResultVOList);
        return scoringResultVOPage;
    }

}
