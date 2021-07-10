package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service
public class VideosApiImpl implements VideosApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 视频上传
     * @param video
     */
    @Override
    public void saveSmallVideos(Video video) {
        mongoTemplate.save(video);
    }

    /**
     * 小视频列表
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult<Video> querySmallVideos(int page, int pagesize) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC,"created"));
        query.limit(pagesize).skip((page-1)*pagesize);
        //总记录数
        long counts = mongoTemplate.count(query, Video.class);
        //分页查询
        List<Video> videoList = mongoTemplate.find(query, Video.class);
        //总页数
        long pages=counts/pagesize+(counts%pagesize==0?0:1);
        //封装返回数据
        return new PageResult<Video>(counts,(long)pagesize,pages,(long)page,videoList);
    }

    /**
     * 视频用户关注
     * @param followUser
     */
    @Override
    public void saveFollowUser(FollowUser followUser) {
        followUser.setId(ObjectId.get());
        followUser.setCreated(System.currentTimeMillis());
        mongoTemplate.save(followUser);
    }

    /**
     * 视频用户关注 - 取消
     * @param followUser
     */
    @Override
    public void removeFollowUser(FollowUser followUser) {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("userId").is(followUser.getUserId())
                        .and("followUserId").is(followUser.getFollowUserId()));
        mongoTemplate.remove(query,FollowUser.class);
    }
}
