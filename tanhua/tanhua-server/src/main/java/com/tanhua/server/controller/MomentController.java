package com.tanhua.server.controller;

import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.server.service.MomentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/movements")
public class MomentController {

    @Autowired
    private MomentService momentService;

    /**
     * 动态-发布
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity savePublish(PublishVo publishVo, MultipartFile[] imageContent ){
        momentService.savePublish(publishVo,imageContent);
        return ResponseEntity.ok(null);
    }

    /**
     * 好友动态
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity queryFriendPublish(@RequestParam(defaultValue = "1") int page,@RequestParam(defaultValue = "10") int pagesize){
        PageResult<MomentVo> pageResult =momentService.queryFriendPublish(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 推荐动态
     * @param page
     * @param pagesize
     * @return
     */
    @RequestMapping(value = "/recommend",method = RequestMethod.GET)
    public ResponseEntity recommendPublish(@RequestParam(defaultValue = "1") int page,@RequestParam(defaultValue = "10") int pagesize){
        PageResult<MomentVo> pageResult =momentService.recommendPublish(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 用户动态(我的动态)
     * @param page
     * @param pagesize
     * @return
     */
    @RequestMapping(value = "/all",method = RequestMethod.GET)
    public ResponseEntity queryMyPublish(@RequestParam(defaultValue = "1") int page,@RequestParam(defaultValue = "10") int pagesize,Long userId){
        PageResult<MomentVo> pageResult =momentService.queryMyPublish(page,pagesize,userId);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 动态-点赞
     * @param publishId
     * @return
     */
    @RequestMapping(value = "/{id}/like",method = RequestMethod.GET)
    public ResponseEntity like(@PathVariable("id") String publishId){
        //根据发布id动态点赞并返回点赞数量
        long count=momentService.like(publishId);
        return ResponseEntity.ok(count);
    }

    /**
     * 动态-取消点赞
     * @param publishId
     * @return
     */
    @RequestMapping(value = "/{id}/dislike",method = RequestMethod.GET)
    public ResponseEntity dislike(@PathVariable("id") String publishId){
        //根据发布id动态取消点赞并返回点赞数量
        long count=momentService.dislike(publishId);
        return ResponseEntity.ok(count);
    }

    /**
     * 动态-喜欢
     * @param publishId
     * @return
     */
    @RequestMapping(value = "/{id}/love",method = RequestMethod.GET)
    public ResponseEntity love(@PathVariable("id") String publishId){
        //根据发布id动态喜欢并返回点赞数量
        long count=momentService.love(publishId);
        return ResponseEntity.ok(count);
    }

    /**
     * 动态-取消喜欢
     * @param publishId
     * @return
     */
    @RequestMapping(value = "/{id}/unlove",method = RequestMethod.GET)
    public ResponseEntity unlove(@PathVariable("id") String publishId){
        //根据发布id动态取消喜欢并返回点赞数量
        long count=momentService.unlove(publishId);
        return ResponseEntity.ok(count);
    }

    /**
     * 单条动态
     * @param publishId
     * @return
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public ResponseEntity onePublish(@PathVariable("id") String publishId){
        //根据发布id动态取消喜欢并返回点赞数量
        MomentVo momentVo=momentService.onePublish(publishId);
        return ResponseEntity.ok(momentVo);
    }
}
