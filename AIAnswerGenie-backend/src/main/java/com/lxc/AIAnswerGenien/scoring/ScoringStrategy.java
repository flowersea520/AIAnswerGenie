package com.lxc.AIAnswerGenien.scoring;

import com.lxc.AIAnswerGenien.model.entity.App;
import com.lxc.AIAnswerGenien.model.entity.UserAnswer;

import java.util.List;

/**
 *  评分策略
 */
interface ScoringStrategy {
    /**
     *  执行评分
     * @param choices
     * @param app
     * @return
     * @throws Exception
     */
    UserAnswer doScore(List<String> choices, App app) throws Exception;
}