package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.vo.PageResult;

public interface CommentApi {

    /**
     *动态-点赞
     * @param comment
     * @return
     */
    long saveComment(Comment comment);

    /**
     * 动态-取消点赞
     * @param comment
     * @return
     */
    long removeComment(Comment comment);

    /**
     * 评论列表
     * @param publishId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult<Comment> queryCommentsByPage(String publishId, int page, int pagesize);

    /**
     * 点赞 喜欢 评论 列表
     * 评论类型，1-点赞，2-评论，3-喜欢
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult<Comment> queryCommentsUserIdPage(Long userId, long page, long pagesize,int num);
}
