package com.tanhua.server.service;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.VideosApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;



import java.io.IOException;
import java.util.ArrayList;

/**
 * 小视频业务层
 */
@Service
@Slf4j
public class VideosService {

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FastFileStorageClient client;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private VideosApi videosApi;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 视频上传
     * @param videoThumbnail
     * @param videoFile
     */
    @CacheEvict(value = "Video",allEntries = true) //当执行此方法后清除redis中Video下的所有键值对
    public void saveSmallVideos(MultipartFile videoThumbnail, MultipartFile videoFile) {
        try {
            //视频封面文件存在阿里云分布式文件系统中
            String fileName =videoThumbnail.getOriginalFilename();
            //封面地址
            String picUrl = ossTemplate.upload(fileName, videoThumbnail.getInputStream());
            //视频存在FastDFS中
            String VideoFilename = videoFile.getOriginalFilename();
            String videoSuffix = VideoFilename.substring(VideoFilename.lastIndexOf(".") + 1);
            StorePath storePath = client.uploadFile(videoFile.getInputStream(), videoFile.getSize(), videoSuffix, null);
            //视频地址
            String videoUrl=fdfsWebServer.getWebServerUrl()+storePath.getFullPath();
            //封装数据
            Video video = new Video();
            video.setUserId(UserHolder.getUserId());
            video.setText("探花交友");
            video.setPicUrl(picUrl);
            video.setVideoUrl(videoUrl);
            video.setId(ObjectId.get());
            video.setCreated(System.currentTimeMillis());
            videosApi.saveSmallVideos(video);
        } catch (IOException e) {
            throw new TanHuaException(ErrorResult.error());
        }
    }

    /**
     * 小视频列表
     * @param page
     * @param pagesize
     * @return
     * redis中 key为Video::1_10 值为方法的返回值
     */

    @Cacheable(value = "Video",key = "#page+'_'+#pagesize")
    public PageResult<VideoVo> querySmallVideos(int page, int pagesize) {
        log.debug("查询小视频***************");
        Long userId = UserHolder.getUserId();
        PageResult<VideoVo> voPageResult = new PageResult<>();
        //分页查询
        PageResult<Video> pageResult=videosApi.querySmallVideos(page,pagesize);
        if (pageResult==null || CollectionUtils.isEmpty(pageResult.getItems())){
            //无数据时设置返回空值
            voPageResult=new PageResult<VideoVo>(0l,10l,0l,1l,null);
            return voPageResult;
        }
        ArrayList<VideoVo> videoVoList = new ArrayList<>();
        for (Video video : pageResult.getItems()) {
            VideoVo videoVo = new VideoVo();
            Long userId1 = video.getUserId();//小视频发布人id
            UserInfo userInfo = userInfoApi.findById(userId1);
            BeanUtils.copyProperties(userInfo,videoVo);
            BeanUtils.copyProperties(video,videoVo);
            if (StringUtils.isEmpty(video.getText())){
                videoVo.setSignature("小花");
            }else {
                videoVo.setSignature(video.getText());//签名
            }
            videoVo.setHasLiked(0); //点赞
            videoVo.setCover(video.getPicUrl()); //视频封面

            //关注
            String key ="follower_user_"+userId+"_"+video.getUserId();
            if(redisTemplate.hasKey(key)){
                videoVo.setHasFocus(1);//关注
            }else {
                videoVo.setHasFocus(0);
            }

            videoVo.setUserId(userId);//发布视频的用户id
            videoVo.setId(video.getId().toHexString());//主键id
            videoVoList.add(videoVo);
        }
        BeanUtils.copyProperties(pageResult,voPageResult);
        voPageResult.setItems(videoVoList);

        return voPageResult;
    }

    /**
     * 视频用户关注
     * @param followUserId
     */
    public void userFocus(Long followUserId) {
        Long userId = UserHolder.getUserId();//当前用户id
        //保存数据关注表
        FollowUser followUser = new FollowUser();
        followUser.setUserId(userId);//当前用户id
        followUser.setFollowUserId(followUserId);//视频用户id
        videosApi.saveFollowUser(followUser);
        //将关注记录到redis中
        String key ="follower_user_"+userId+"_"+followUserId;
        redisTemplate.opsForValue().set(key,"true");
    }

    /**
     * 视频用户关注 - 取消
     * @param followUserId
     */
    public void userUnFocus(Long followUserId) {

        Long userId = UserHolder.getUserId();//当前用户id
        //保存数据关注表
        FollowUser followUser = new FollowUser();
        followUser.setUserId(userId);//当前用户id
        followUser.setFollowUserId(followUserId);//视频用户id
        videosApi.removeFollowUser(followUser);
        //将关注记录到redis中
        String key ="follower_user_"+userId+"_"+followUserId;
        redisTemplate.delete(key);
    }
}
