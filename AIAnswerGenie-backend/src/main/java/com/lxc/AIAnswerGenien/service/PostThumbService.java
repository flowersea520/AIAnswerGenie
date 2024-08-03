package com.lxc.AIAnswerGenien.service;

import com.lxc.AIAnswerGenien.model.entity.PostThumb;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxc.AIAnswerGenien.model.entity.User;

/**
 * 帖子点赞服务
 *
 * @author <a href="https://github.com/flowersea520">程序员lxc</a>
 * @from <a href="https://lxc.icu">编程导航知识星球</a>
 */
public interface PostThumbService extends IService<PostThumb> {

    /**
     * 点赞
     *
     * @param postId
     * @param loginUser
     * @return
     */
    int doPostThumb(long postId, User loginUser);

    /**
     * 帖子点赞（内部服务）
     *
     * @param userId
     * @param postId
     * @return
     */
    int doPostThumbInner(long userId, long postId);
}
