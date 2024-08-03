package com.lxc.AIAnswerGenien.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用类型枚举
 *
 * @author <a href="https://github.com/flowersea520">程序员lxc</a>
 */
public enum AppScoringStrategyEnum {
    // 枚举的有参构造器;
    //  每个枚举常量/对象都可以有自己的属性和 行为。【属性就是 有参构造器的参数，行为就是获取其参数的方法】
    CUSTOM("自定义", 0),
    AI("AI", 1);
 // 枚举构造器的 参数
    private final String text;

    private final int value;

    AppScoringStrategyEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static AppScoringStrategyEnum getEnumByValue(int value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (AppScoringStrategyEnum anEnum : AppScoringStrategyEnum.values()) {
            if (anEnum.value == value) {
                return anEnum;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
