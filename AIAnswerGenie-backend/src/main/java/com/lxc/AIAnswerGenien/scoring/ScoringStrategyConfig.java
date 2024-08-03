package com.lxc.AIAnswerGenien.scoring;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// ElementType.TYPE: 该接口 可以应用于类、接口（包括注解类型）或枚举声明。这是默认值
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ScoringStrategyConfig {
// 这些都是注解的元素
    /**
     * 应用类型
     * @return
     */
    int appType();

    /**
     * 评分策略
     * @return
     */
    int scoringStrategy();
}