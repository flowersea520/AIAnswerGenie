package com.lxc.AIAnswerGenien;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lxc.AIAnswerGenien.model.entity.UserAnswer;
import com.lxc.AIAnswerGenien.service.UserAnswerService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class UserAnswerShardingTest {

    @Resource
    private UserAnswerService userAnswerService;

    @Test
    void test() {

        UserAnswer userAnswer1 = new UserAnswer();
        // appId为奇数，会放到user_answer_1这张表（因为我们设置了配置）
        userAnswer1.setAppId(1L);
        userAnswer1.setUserId(1L);
        userAnswer1.setChoices("1");
        userAnswerService.save(userAnswer1);

        UserAnswer userAnswer2 = new UserAnswer();
         // appId为偶数，会放到user_answer_0这张表（因为我们设置了配置）
        userAnswer2.setAppId(2L);
        userAnswer2.setUserId(1L);
        userAnswer2.setChoices("2");
        userAnswerService.save(userAnswer2);

        UserAnswer userAnswerOne = userAnswerService.getOne(Wrappers.lambdaQuery(UserAnswer.class).eq(UserAnswer::getAppId, 1L));
        System.out.println(JSONUtil.toJsonStr(userAnswerOne));

        UserAnswer userAnswerTwo = userAnswerService.getOne(Wrappers.lambdaQuery(UserAnswer.class).eq(UserAnswer::getAppId, 2L));
        System.out.println(JSONUtil.toJsonStr(userAnswerTwo));
    }
}