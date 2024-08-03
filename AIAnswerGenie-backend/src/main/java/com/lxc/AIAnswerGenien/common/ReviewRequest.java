package com.lxc.AIAnswerGenien.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 审核请求  -- 通用的dto我们直接放在 common包下
 * 这里其实还有两个审核的标准字段
 * （一共四件套，但是呢，我们就写了两个，一个审核状态，一个审核信息，前端只要传这两个就OK
 * 剩下的审核人和审核时间由我们后续代码编写生成）
 *
 * @author <a href="https://github.com/flowersea520">程序员lxc</a>
 * @from <a href="https://lxc.icu">编程导航知识星球</a>
 */
@Data
public class ReviewRequest implements Serializable {

    /**
     * 要审核的app应用id
     */
    private Long appId;

    /**
     *  审核状态  0-待审核， 1-通过 ， 2-拒绝
     */
    private Integer reviewStatus;

    /**
     *  审核信息
     */
    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}