package com.lxc.AIAnswerGenien.scoring;


import com.lxc.AIAnswerGenien.common.ErrorCode;
import com.lxc.AIAnswerGenien.exception.BusinessException;
import com.lxc.AIAnswerGenien.model.entity.App;
import com.lxc.AIAnswerGenien.model.entity.UserAnswer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 评分策略执行器
 */
@Service
public class ScoringStrategyExecutor {
    // 策略列表 (注入了ScoringStrategy策略接口对象,以集合的形式存放多种策略)
    @Resource
    private List<ScoringStrategy> scoringStrategyList;

    /**
     * 评分方法
     *
     * @param choiceList 选择列表
     * @param app 应用信息
     * @return 用户答案
     * @throws Exception 可能抛出的异常
     */
    public UserAnswer doScore(List<String> choiceList, App app) throws Exception {
        // 获取应用类型和评分策略
        Integer appType = app.getAppType();
        Integer appScoringStrategy = app.getScoringStrategy();

        // 检查应用类型和评分策略是否为空
        if (appType == null || appScoringStrategy == null) {
            // 应用配置有误，抛出业务异常
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配的策略");
        }

        // 根据注解获取策略
        for (ScoringStrategy strategy : scoringStrategyList) {
            // 检查是否包含指定的注解
            // isAnnotationPresent(ScoringStrategyConfig.class) 被调用并且返回 true，
            // 那就意味着 当前类 或其 任何父类上 存在 ScoringStrategyConfig 注解。
            if (strategy.getClass().isAnnotationPresent(ScoringStrategyConfig.class)) {
                // 获取注解配置信息
                ScoringStrategyConfig scoringStrategyConfig = strategy.getClass().getAnnotation(ScoringStrategyConfig.class);
                // 检查是否匹配应用类型和评分策略
                if (scoringStrategyConfig.appType() == appType
                        && scoringStrategyConfig.scoringStrategy() == appScoringStrategy) {
                    // 执行评分策略
                    return strategy.doScore(choiceList, app);
                }
            }
        }

        // 没有找到匹配的策略，抛出业务异常
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配的策略");
    }
}