package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.*;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class PublishApiImpl implements PublishApi{

   @Autowired
   private MongoTemplate mongoTemplate;

    /**
     * 动态-发布
     * @param publishVo
     */
    @Override
    public void savePublish(PublishVo publishVo) {

        //当前用户id
        Long userId = publishVo.getUserId();
        //发布动态时间
        long nowTime = System.currentTimeMillis();

        //将数据存入发表
        //封装数据并存到发布表
        Publish publish = new Publish();
        BeanUtils.copyProperties(publishVo,publish);
        publish.setId(ObjectId.get());//设置自动生成的id
        publish.setPid(666l);//推荐系统
        publish.setLocationName(publishVo.getLocation());//设置位置
        publish.setSeeType(1);//默认公开
        publish.setCreated(nowTime);//设置发布时间
        mongoTemplate.save(publish);

        //往相册表中插入动态数据
        Album album = new Album();
        album.setId(ObjectId.get());//设置主键id
        album.setPublishId(publish.getId());//设置发布id
        album.setCreated(nowTime);
        mongoTemplate.save(album,"quanzi_album_"+userId);

        //将发布id存到好友的时间线表中
        //查询好友表,找到好友ids
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        List<Friend> friendList = mongoTemplate.find(query, Friend.class);
        if (!CollectionUtils.isEmpty(friendList)){
            for (Friend friend : friendList) {
                Long friendId = friend.getFriendId();//好友id
                TimeLine timeLine = new TimeLine();
                timeLine.setId(ObjectId.get());//主键id
                timeLine.setUserId(userId);//当前发布动态的用户id
                timeLine.setPublishId(publish.getId());//发布id
                timeLine.setCreated(nowTime);
                mongoTemplate.save(timeLine,"quanzi_time_line_"+friendId);
            }
        }
    }

    /**
     * 好友动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult<Publish> queryPublishByTimeLinePage(int page, int pagesize, Long userId) {
        //分页查询时间线表
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC,"created"));//根据创建时间降序
        query.limit(pagesize).skip((page-1)*pagesize);
        //查询总记录数
        long counts = mongoTemplate.count(query, "quanzi_time_line_" + userId);
        //分页查询时间线表
        List<TimeLine> timeLineList = mongoTemplate.find(query, TimeLine.class, "quanzi_time_line_" + userId);

        //遍历时间线表,获得好友发布id,查询发布表
        ArrayList<Publish> publishList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(timeLineList)){
            for (TimeLine timeLine : timeLineList) {
                ObjectId publishId = timeLine.getPublishId();//获得发布id
                if(!StringUtils.isEmpty(publishId)){
                    Publish publish = mongoTemplate.findById(publishId, Publish.class);
                    if (publish!=null){
                        publishList.add(publish);
                    }
                }
            }
        }
        //总页数
        long pages=counts/pagesize+(counts%pagesize==0?0:1);
        //封装数据返回
        return new PageResult<Publish>(counts,(long)pagesize,pages,(long)page,publishList);
    }

    /**
     * 推荐动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult<Publish> recommendPublish(int page, int pagesize, Long userId) {
        //分页查询推荐圈子表
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.with(Sort.by(Sort.Direction.DESC,"created"));//根据创建时间降序
        query.limit(pagesize).skip((page-1)*pagesize);
        //查询总记录数
        long counts = mongoTemplate.count(query, RecommendQuanzi.class);
        //分页查询推荐圈子表
        List<RecommendQuanzi> recommendQuanziList = mongoTemplate.find(query,RecommendQuanzi.class);

        //遍历时间线表,获得好友发布id,查询发布表
        ArrayList<Publish> publishList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(recommendQuanziList)){
            for (RecommendQuanzi recommendQuanzi : recommendQuanziList) {
                ObjectId publishId = recommendQuanzi.getPublishId();//获得发布id
                if(!StringUtils.isEmpty(publishId)){
                    Publish publish = mongoTemplate.findById(publishId, Publish.class);
                    if (publish!=null){
                        publishList.add(publish);
                    }
                }
            }
        }
        //总页数
        long pages=counts/pagesize+(counts%pagesize==0?0:1);
        //封装数据返回
        return new PageResult<Publish>(counts,(long)pagesize,pages,(long)page,publishList);
    }

    /**
     * 用户动态(我的动态)
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult<Publish> queryMyPublish(int page, int pagesize, Long userId) {
        //分页查询相册表
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC,"created"));//根据创建时间降序
        query.limit(pagesize).skip((page-1)*pagesize);
        //查询总记录数
        long counts = mongoTemplate.count(query, "quanzi_album_" + userId);
        //分页查询相册表
        List<Album> albumList = mongoTemplate.find(query, Album.class, "quanzi_album_" + userId);

        //遍历时间线表,获得好友发布id,查询发布表
        ArrayList<Publish> publishList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(albumList)){
            for (Album album : albumList) {
                ObjectId publishId = album.getPublishId();//获得发布id
                if(!StringUtils.isEmpty(publishId)){
                    Publish publish = mongoTemplate.findById(publishId, Publish.class);
                    if (publish!=null){
                        publishList.add(publish);
                    }
                }
            }
        }
        //总页数
        long pages=counts/pagesize+(counts%pagesize==0?0:1);
        //封装数据返回
        return new PageResult<Publish>(counts,(long)pagesize,pages,(long)page,publishList);
    }

    /**
     * 单条动态
     * @param publishId
     * @return
     */
    @Override
    public Publish queryPublish(String publishId) {
        return mongoTemplate.findById(new ObjectId(publishId),Publish.class);
    }
}
