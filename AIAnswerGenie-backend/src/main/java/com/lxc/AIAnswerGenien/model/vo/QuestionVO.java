package com.lxc.AIAnswerGenien.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.lxc.AIAnswerGenien.model.dto.question.QuestionContentDTO;
import com.lxc.AIAnswerGenien.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目视图
 *
 * @author <a href="https://github.com/flowersea520">程序员lxc</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Data
public class QuestionVO implements Serializable {

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

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息（这个是在question实体中新加的一个属性）
     */
    private UserVO user;

    /**
     * VO封装类转 实体 对象
     *
     * @param questionVO
     * @return
     */
    public static Question voToObj(QuestionVO questionVO) {
        if (questionVO == null) {
            return null;
        }
        Question question = new Question();
        // BeanUtils.copyProperties 只会拷贝名称和类型都相同的属性，
        // 所以如果 questionVO 中的 questionContent 是 QuestionContentDTO 类型，
        // 而 question 中的 questionContent 是 String 类型，
        // 它们将无法通过 BeanUtils.copyProperties 直接进行拷贝。
        BeanUtils.copyProperties(questionVO, question);
       // 将没拷贝到的我们将其转换为 对应的类型：字符串
        String questionContent = JSONUtil.toJsonStr(questionVO.getQuestionContent());
        question.setQuestionContent(questionContent);
        return question;
    }

    /**
     * 实体对象转 VO封装类
     *
     * @param question
     * @return
     */
    public static QuestionVO objToVo(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        questionVO.setQuestionContent(JSONUtil.toList(question.getQuestionContent(), QuestionContentDTO.class));
        return questionVO;
    }
}
