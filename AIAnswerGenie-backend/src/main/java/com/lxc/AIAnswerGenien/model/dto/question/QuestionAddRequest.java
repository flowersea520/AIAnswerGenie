package com.lxc.AIAnswerGenien.model.dto.question;


import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * 创建题目请求
 *
 * @author <a href="https://github.com/flowersea520">程序员lxc</a>
 *
 */
@Data
public class QuestionAddRequest implements Serializable {

    /**
     * 题目内容（json格式） -- 多道题，记得用集合
     * -- 返回给前端最好返回给其Java对象，
     * 这样前端不需要再解析嵌套的 JSON 数据，只需使用接收到的对象
     */
    private List<QuestionContentDTO> questionContent;

    /**
     * 应用 id (创建题目的时候，用户必填，因为这个题目对应着某个应用app）
     */
    private Long appId;

    private static final long serialVersionUID = 1L;
}