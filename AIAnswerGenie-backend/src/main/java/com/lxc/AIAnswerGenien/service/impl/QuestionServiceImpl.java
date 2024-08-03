package com.lxc.AIAnswerGenien.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxc.AIAnswerGenien.common.ErrorCode;
import com.lxc.AIAnswerGenien.constant.CommonConstant;
import com.lxc.AIAnswerGenien.exception.ThrowUtils;
import com.lxc.AIAnswerGenien.mapper.QuestionMapper;
import com.lxc.AIAnswerGenien.model.dto.question.QuestionQueryRequest;
import com.lxc.AIAnswerGenien.model.entity.App;
import com.lxc.AIAnswerGenien.model.entity.Question;
import com.lxc.AIAnswerGenien.model.entity.User;
import com.lxc.AIAnswerGenien.model.vo.QuestionVO;
import com.lxc.AIAnswerGenien.model.vo.UserVO;
import com.lxc.AIAnswerGenien.service.AppService;
import com.lxc.AIAnswerGenien.service.QuestionService;
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
 * 题目服务实现
 *
 * @author <a href="https://github.com/flowersea520">程序员lxc</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    /**
     * 校验数据
     *
     * @param question
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        Long id = question.getId();
        String questionContent = question.getQuestionContent();
        Long appId = question.getAppId();
        Long userId = question.getUserId();
        Date createTime = question.getCreateTime();
        Date updateTime = question.getUpdateTime();

        // 创建数据时，参数不能为空
        if (add) {
            // 补充校验规则
            ThrowUtils.throwIf(id != null && id < 0, ErrorCode.PARAMS_ERROR, "appId非法");
            ThrowUtils.throwIf(StringUtils.isBlank(questionContent), ErrorCode.PARAMS_ERROR, "题目内容不能为空");
            ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "appId非法");
        }

        // 修改数据时，有参数则校验
        // 补充校验规则
        if (appId != null) {
            // appId不为空，我们调用其service获取其app对象
            App app = appService.getById(appId);
            ThrowUtils.throwIf(BeanUtil.isEmpty(app), ErrorCode.PARAMS_ERROR, "应用不存在");
        }
        if (userId != null) {
            ThrowUtils.throwIf(userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID非法");
        }
        if (createTime != null) {
            ThrowUtils.throwIf(createTime.after(new Date()), ErrorCode.PARAMS_ERROR, "创建时间不能在未来");
        }
        if (updateTime != null) {
            ThrowUtils.throwIf(updateTime.after(new Date()), ErrorCode.PARAMS_ERROR, "更新时间不能在未来");
        }
    }

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = questionQueryRequest.getId();
        String questionContent = questionQueryRequest.getQuestionContent();
        Long appId = questionQueryRequest.getAppId();
        Long userId = questionQueryRequest.getUserId();
        Long notId = questionQueryRequest.getNotId();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();

        // 补充需要的查询条件
        // 模糊查询
       queryWrapper.like(StringUtils.isNotBlank(questionContent), "questionContent",questionContent);
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "appId", appId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题目封装
     *
     * @param question
     * @param request
     * @return
     */
    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        // 对象转封装类
        QuestionVO questionVO = QuestionVO.objToVo(question);

        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUser(userVO);
        // endregion
        return questionVO;
    }

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            return QuestionVO.objToVo(question);
        }).collect(Collectors.toList());

        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        questionVOList.forEach(questionVO -> {
            Long userId = questionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionVO.setUser(userService.getUserVO(user));
        });
        // endregion
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }
}
