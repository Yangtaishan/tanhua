package com.tanhua.server.service;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CommentService {

    @Reference
    private CommentApi commentApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 评论列表
     * @param publishId
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult<CommentVo> queryCommentsByPage(String publishId, int page, int pagesize) {
        Long currentUserId = UserHolder.getUserId();
        PageResult<CommentVo> voPageResult = new PageResult<>();
        PageResult<Comment> pageResult=commentApi.queryCommentsByPage(publishId,page,pagesize);
        if (pageResult==null || CollectionUtils.isEmpty(pageResult.getItems())){
            voPageResult=new PageResult<>(0l,10l,0l,1l,null);
            return voPageResult;
        }
        List<CommentVo> commentVoList = new ArrayList<>();
        for (Comment comment : pageResult.getItems()) {
            CommentVo commentVo = new CommentVo();
            Long userId = comment.getUserId();//评论人id
            UserInfo userInfo = userInfoApi.findById(userId);
            BeanUtils.copyProperties(userInfo,commentVo);
            BeanUtils.copyProperties(comment,commentVo);
            commentVo.setCreateDate(new DateTime(comment.getCreated()).toString("yyyy年MM月dd日 HH:mm"));
            commentVo.setId(comment.getId().toHexString());
            String key2 ="comment_like"+currentUserId+"_"+comment.getId().toHexString();
            if (StringUtils.isEmpty(redisTemplate.opsForValue().get(key2))){
                commentVo.setHasLiked(0);
            }else {
                commentVo.setHasLiked(1);
            }
            commentVoList.add(commentVo);
        }
        BeanUtils.copyProperties(pageResult,voPageResult);
        voPageResult.setItems(commentVoList);
        return voPageResult;
    }

    /**
     * 评论-提交
     * @param publishId
     * @param content
     */
    public void saveComment(String publishId, String content) {
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));//评论对应的发布id
        comment.setCommentType(2); //评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1); //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setContent(content);//评论内容
        comment.setUserId(UserHolder.getUserId());//当前评论用户id
        commentApi.saveComment(comment);
    }

    /**
     * 评论-点赞
     * @param commentId
     * @return
     */
    public long like(String commentId) {
        Long currentUserId = UserHolder.getUserId();
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(commentId));//评论对应的发布id
        comment.setCommentType(1); //评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(3); //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(currentUserId);
        long counts = commentApi.saveComment(comment);

        String key2 ="comment_like"+currentUserId+"_"+commentId;
        redisTemplate.opsForValue().set(key2,commentId);
        return counts;
    }

    /**
     * 评论-取消点赞
     * @param commentId
     * @return
     */
    public long dislike(String commentId) {
        Long currentUserId = UserHolder.getUserId();
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(commentId));//评论对应的发布id
        comment.setCommentType(1); //评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(3); //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(currentUserId);
        long counts = commentApi.removeComment(comment);

        String key2 ="comment_like"+currentUserId+"_"+commentId;
        redisTemplate.delete(key2);
        return counts;
    }
}
