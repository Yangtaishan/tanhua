package com.tanhua.server.service;

import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.PublishApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 圈子业务处理
 */
@Service
public class MomentService {

    @Autowired
    private OssTemplate ossTemplate;

    @Reference
    private PublishApi publishApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private CommentApi commentApi;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 动态-发布
     * @param publishVo
     * @param imageContent
     */
    public void savePublish(PublishVo publishVo, MultipartFile[] imageContent) {
        try {
            //将文件上传到文件系统,并把地址存到list集合中
            List<String> medias = new ArrayList<>();
            if (imageContent !=null && imageContent.length>0){
                for (MultipartFile multipartFile : imageContent) {
                    String imgUrl = ossTemplate.upload(multipartFile.getOriginalFilename(), multipartFile.getInputStream());
                    medias.add(imgUrl);
                }
                //文件地址设置到publishVo中
                publishVo.setMedias(medias);
                //设置当前用户id
                publishVo.setUserId(UserHolder.getUserId());
                //调用服务提供者-发布动态
                publishApi.savePublish(publishVo);
            }
        } catch (IOException e) {
            throw new TanHuaException(ErrorResult.error());
        }
    }

    /**
     * 好友动态
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult<MomentVo> queryFriendPublish(int page, int pagesize) {
        //定义返回值
        PageResult<MomentVo> pageResultVo = new PageResult<>();
        Long userId = UserHolder.getUserId();//当前登录用户id

        //根据当前用户的时间线表查好友动态的发布id,再查发布表
        PageResult<Publish> publishPageResult=publishApi.queryPublishByTimeLinePage(page,pagesize,userId);
        if(publishPageResult==null || CollectionUtils.isEmpty(publishPageResult.getItems())){
            //前端未处理好,如果为空则需如下设置
            pageResultVo = new PageResult<>(0l, 10l, 0l, 1l, null);
            return pageResultVo;
        }
        //将集合转化为list<MomentVo>
        List<MomentVo> momentVoList = new ArrayList<>();
        //根据发布表中用户id查询用户信息
        for (Publish publish : publishPageResult.getItems()) {
            MomentVo momentVo = new MomentVo();
            Long userId1 = publish.getUserId();
            UserInfo userInfo = userInfoApi.findById(userId1);
            BeanUtils.copyProperties(userInfo,momentVo);
            BeanUtils.copyProperties(publish,momentVo);
            if(!StringUtils.isEmpty(userInfo.getTags())){
                momentVo.setTags(userInfo.getTags().split(","));
            }
            momentVo.setId(publish.getId().toHexString());
            momentVo.setImageContent(publish.getMedias().toArray(new String[]{}));
            momentVo.setDistance("1米");
            momentVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));

            String key="publish_like"+userId+"_"+publish.getId().toHexString();
            if(StringUtils.isEmpty(redisTemplate.opsForValue().get(key))){
                momentVo.setHasLiked(0);//是否点赞
            }else {
                momentVo.setHasLiked(1);//是否点赞
            }
            String key2="publish_love"+userId+"_"+publish.getId().toHexString();
            if(StringUtils.isEmpty(redisTemplate.opsForValue().get(key2))){
                momentVo.setHasLoved(0);//是否喜欢
            }else {
                momentVo.setHasLoved(1);//是否喜欢
            }

            momentVoList.add(momentVo);
        }
        BeanUtils.copyProperties(publishPageResult,pageResultVo);//分页数据
        pageResultVo.setItems(momentVoList);//设置集合数据
        return pageResultVo;
    }

    /**
     * 推荐动态
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult<MomentVo> recommendPublish(int page, int pagesize) {
        //定义返回值
        PageResult<MomentVo> pageResultVo = new PageResult<>();
        Long userId = UserHolder.getUserId();//当前登录用户id

        //查询当前用户的推荐圈子表得到的发布id,再查发布表
        PageResult<Publish> publishPageResult=publishApi.recommendPublish(page,pagesize,userId);
        if(publishPageResult==null || CollectionUtils.isEmpty(publishPageResult.getItems())){
            //前端未处理好,如果为空则需如下设置
            pageResultVo = new PageResult<>(0l, 10l, 0l, 1l, null);
            return pageResultVo;
        }
        //将集合转化为list<MomentVo>
        List<MomentVo> momentVoList = new ArrayList<>();
        //根据发布表中用户id查询用户信息
        for (Publish publish : publishPageResult.getItems()) {
            MomentVo momentVo = new MomentVo();
            Long userId1 = publish.getUserId();
            UserInfo userInfo = userInfoApi.findById(userId1);
            BeanUtils.copyProperties(userInfo,momentVo);
            BeanUtils.copyProperties(publish,momentVo);
            if(!StringUtils.isEmpty(userInfo.getTags())){
                momentVo.setTags(userInfo.getTags().split(","));
            }
            momentVo.setId(publish.getId().toHexString());
            momentVo.setImageContent(publish.getMedias().toArray(new String[]{}));
            momentVo.setDistance("1米");
            momentVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));

            String key="publish_like"+userId+"_"+publish.getId().toHexString();
            if(StringUtils.isEmpty(redisTemplate.opsForValue().get(key))){
                momentVo.setHasLiked(0);//是否点赞
            }else {
                momentVo.setHasLiked(1);//是否点赞
            }
            String key2="publish_love"+userId+"_"+publish.getId().toHexString();
            if(StringUtils.isEmpty(redisTemplate.opsForValue().get(key2))){
                momentVo.setHasLoved(0);//是否喜欢
            }else {
                momentVo.setHasLoved(1);//是否喜欢
            }

            momentVoList.add(momentVo);
        }
        BeanUtils.copyProperties(publishPageResult,pageResultVo);//分页数据
        pageResultVo.setItems(momentVoList);//设置集合数据
        return pageResultVo;
    }

    /**
     * 用户动态(我的动态)
     * @param page
     * @param pagesize
     * @param userId2
     * @return
     */
    public PageResult<MomentVo> queryMyPublish(int page, int pagesize, Long userId2) { //此userId2时当前动态的用户id
        //定义返回值
        PageResult<MomentVo> pageResultVo = new PageResult<>();
        Long userId = UserHolder.getUserId();//当前登录用户id

        //查询自己的相册表,得到发布id,再查发布表
        PageResult<Publish> publishPageResult=publishApi.queryMyPublish(page,pagesize,userId2);
        if(publishPageResult==null || CollectionUtils.isEmpty(publishPageResult.getItems())){
            //前端未处理好,如果为空则需如下设置
            pageResultVo = new PageResult<>(0l, 10l, 0l, 1l, null);
            return pageResultVo;
        }
        //将集合转化为list<MomentVo>
        List<MomentVo> momentVoList = new ArrayList<>();
        //根据发布表中用户id查询用户信息
        for (Publish publish : publishPageResult.getItems()) {
            MomentVo momentVo = new MomentVo();
            Long userId1 = publish.getUserId();
            UserInfo userInfo = userInfoApi.findById(userId1);
            BeanUtils.copyProperties(userInfo,momentVo);
            BeanUtils.copyProperties(publish,momentVo);
            if(!StringUtils.isEmpty(userInfo.getTags())){
                momentVo.setTags(userInfo.getTags().split(","));
            }
            momentVo.setId(publish.getId().toHexString());
            momentVo.setImageContent(publish.getMedias().toArray(new String[]{}));
            momentVo.setDistance("1米");
            momentVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));

            String key="publish_like"+userId+"_"+publish.getId().toHexString();
            if(StringUtils.isEmpty(redisTemplate.opsForValue().get(key))){
                momentVo.setHasLiked(0);//是否点赞
            }else {
                momentVo.setHasLiked(1);//是否点赞
            }
            String key2="publish_love"+userId+"_"+publish.getId().toHexString();
            if(StringUtils.isEmpty(redisTemplate.opsForValue().get(key2))){
                momentVo.setHasLoved(0);//是否喜欢
            }else {
                momentVo.setHasLoved(1);//是否喜欢
            }

            momentVoList.add(momentVo);
        }
        BeanUtils.copyProperties(publishPageResult,pageResultVo);//分页数据
        pageResultVo.setItems(momentVoList);//设置集合数据
        return pageResultVo;
    }

    /**
     * 动态-点赞
     * @param publishId
     * @return
     */
    public long like(String publishId) {
        Long currentUserId = UserHolder.getUserId();
        //封装评论对象
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId)); //转换发布id类型
        comment.setCommentType(1); //评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1); //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(currentUserId); //评论人
        long count=commentApi.saveComment(comment);
        //将点赞记录到redis中(后续查询动态冲redis中查询是否点赞)
        String key="publish_like"+currentUserId+"_"+publishId;
        redisTemplate.opsForValue().set(key,publishId);
        return count;
    }

    /**
     * 动态-取消点赞
     * @param publishId
     * @return
     */
    public long dislike(String publishId) {
        Long currentUserId = UserHolder.getUserId();
        //封装评论对象
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId)); //转换发布id类型
        comment.setCommentType(1); //评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1); //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(currentUserId); //评论人
        long count=commentApi.removeComment(comment);
        //将点赞从redis中删除(后续查询动态冲redis中查询是否点赞)
        String key="publish_like"+currentUserId+"_"+publishId;
        redisTemplate.delete(key);
        return count;
    }

    /**
     * 动态-喜欢
     * @param publishId
     * @return
     */
    public long love(String publishId) {
        Long currentUserId = UserHolder.getUserId();
        //封装评论对象
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId)); //转换发布id类型
        comment.setCommentType(3); //评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1); //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(currentUserId); //评论人
        long count=commentApi.saveComment(comment);
        //将喜欢记录到redis中(后续查询动态冲redis中查询是否点赞)
        String key="publish_love"+currentUserId+"_"+publishId;
        redisTemplate.opsForValue().set(key,publishId);
        return count;
    }

    /**
     * 动态-取消喜欢
     * @param publishId
     * @return
     */
    public long unlove(String publishId) {
        Long currentUserId = UserHolder.getUserId();
        //封装评论对象
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId)); //转换发布id类型
        comment.setCommentType(3); //评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1); //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(currentUserId); //评论人
        long count=commentApi.removeComment(comment);
        //将喜欢从redis中删除(后续查询动态冲redis中查询是否点赞)
        String key="publish_love"+currentUserId+"_"+publishId;
        redisTemplate.delete(key);
        return count;
    }

    /**
     * 单条动态
     * @param publishId
     * @return
     */
    public MomentVo onePublish(String publishId) {
        MomentVo momentVo = new MomentVo();
        Publish publish=publishApi.queryPublish(publishId);//获取单条动态
        Long publishUserId = publish.getUserId();//获得该动态用户id
        UserInfo userInfo = userInfoApi.findById(publishUserId);
        //将数据封装到momentVo中
        BeanUtils.copyProperties(userInfo,momentVo);
        BeanUtils.copyProperties(publish,momentVo);
        if (!StringUtils.isEmpty(userInfo.getTags())){
            momentVo.setTags(userInfo.getTags().split(","));
        }
        momentVo.setId(publish.getId().toHexString());//发布id
        momentVo.setImageContent(publish.getMedias().toArray(new String[]{}));//集合转数组
        momentVo.setDistance("1米");//距离
        momentVo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));//发布时间
        return momentVo;
    }
}
