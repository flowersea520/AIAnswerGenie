package com.lxc.AIAnswerGenien.model.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑题目请求
 *
 * @author <a href="https://github.com/flowersea520">程序员lxc</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Data
public class QuestionEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 题目内容（json格式） -- 多道题，记得用集合
     * -- 返回给前端最好返回给其Java对象，
     * 这样前端不需要再解析嵌套的 JSON 数据，只需使用接收到的对象
     */
    private List<QuestionContentDTO> questionContent;

    private static final long serialVersionUID = 1L;
}