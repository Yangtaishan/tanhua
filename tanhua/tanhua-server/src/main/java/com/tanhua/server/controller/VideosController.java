package com.tanhua.server.controller;

import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.server.service.VideosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * 小视频控制层
 */
@RestController
@RequestMapping("/smallVideos")
public class VideosController {

    @Autowired
    private VideosService videosService;

    /**
     * 视频上传
     * @param videoThumbnail
     * @param videoFile
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity saveSmallVideos(MultipartFile videoThumbnail,MultipartFile videoFile){
        videosService.saveSmallVideos(videoThumbnail,videoFile);
        return ResponseEntity.ok(null);
    }

    /**
     * 小视频列表
     * @param page
     * @param pagesize
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity querySmallVideos(@RequestParam(defaultValue = "1") int page,@RequestParam(defaultValue = "10") int pagesize){
        page=page<1?1:page;
        PageResult<VideoVo> pageResult =videosService.querySmallVideos(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 视频用户关注
     * @param followUserId
     * @return
     */
    @RequestMapping(value = "/{uid}/userFocus",method = RequestMethod.POST)
    public ResponseEntity userFocus(@PathVariable("uid") Long followUserId){
        videosService.userFocus(followUserId);
        return ResponseEntity.ok(null);
    }

    /**
     * 视频用户关注 - 取消
     * @param followUserId
     * @return
     */
    @RequestMapping(value = "/{uid}/userUnFocus",method = RequestMethod.POST)
    public ResponseEntity userUnFocus(@PathVariable("uid") Long followUserId){
        videosService.userUnFocus(followUserId);
        return ResponseEntity.ok(null);
    }


}
