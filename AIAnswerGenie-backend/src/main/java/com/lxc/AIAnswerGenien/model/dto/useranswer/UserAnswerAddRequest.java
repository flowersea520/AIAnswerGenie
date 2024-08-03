package com.lxc.AIAnswerGenien.model.dto.useranswer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 创建用户答案请求
 *
 * @author <a href="https://github.com/flowersea520">程序员lxc</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Data
public class UserAnswerAddRequest implements Serializable {

    /**
     *  用户答案记录id（用于幂等性设计）
     */
    private Long id;

    /**
     * 应用 id -- 例：用户在哪个应用回答的
     */
    private Long appId;

    /**
     * 用户选项答案（JSON 数组）
     */
    private List<String> choices;

    private static final long serialVersionUID = 1L;
}