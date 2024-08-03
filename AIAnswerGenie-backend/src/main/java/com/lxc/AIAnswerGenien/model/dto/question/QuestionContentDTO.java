package com.lxc.AIAnswerGenien.model.dto.question;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *  题目内容（json格式）所对应的Java对象DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionContentDTO {
    private String title;
    private List<Option> options;

    /**
     * 在 Question 类内部定义了一个静态嵌套类（静态内部类） Option，用于表示选项。
     * 每个 Option 对象包含 result, score, value 和 key 字段。
     *  嵌套的结构对应着 json 结构（一层一层嵌套一个道理）
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Option {
        private String result; // 如果是测评类，则用 result 来保存答案属性 "result": "I",
        private int score;     // 如果是得分类，则用 score 来设置本题分数  "score": 1,
        private String value;  // 选项内容
        private String key;    // 选项 key -- 对应着用户的选项A, B, C
    }
}
