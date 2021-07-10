package com.tanhua.server.controller;

import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.server.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评论控制层
 */
@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 评论列表
     * @param movementId
     * @param page
     * @param pagesize
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity queryCommentsByPage(String movementId, @RequestParam(defaultValue = "1") int page,@RequestParam(defaultValue = "10") int pagesize){
        PageResult<CommentVo> pageResult =commentService.queryCommentsByPage(movementId,page,pagesize); //movementId为发布publishId
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 评论-提交
     * @param params
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity saveComment(@RequestBody Map<String,String> params){
        String publishId = params.get("movementId");//发布id
        String content = params.get("comment"); //评论内容
        commentService.saveComment(publishId,content);
        return ResponseEntity.ok(null);
    }

    /**
     * 评论-点赞
     * @param commentId
     * @return
     */
    @RequestMapping(value = "/{id}/like",method = RequestMethod.GET)
    public ResponseEntity like(@PathVariable("id") String commentId){
        //根据发布id动态点赞并返回点赞数量
        long count=commentService.like(commentId);
        return ResponseEntity.ok(count);
    }

    /**
     * 评论-取消点赞
     * @param commentId
     * @return
     */
    @RequestMapping(value = "/{id}/dislike",method = RequestMethod.GET)
    public ResponseEntity dislike(@PathVariable("id") String commentId){
        //根据发布id动态取消点赞并返回点赞数量
        long count=commentService.dislike(commentId);
        return ResponseEntity.ok(count);
    }
}
