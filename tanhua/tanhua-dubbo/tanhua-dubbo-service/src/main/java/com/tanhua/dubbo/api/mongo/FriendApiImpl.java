package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service
public class FriendApiImpl implements FriendApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 添加好友
     * @param userId
     * @param strangerUserId
     */
    @Override
    public void add(Long userId, Long strangerUserId) {
        //添加陌生人为好友
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId)
                .and("friendId").is(strangerUserId));
        if(! mongoTemplate.exists(query, Friend.class)){
            Friend friend = new Friend();
            friend.setUserId(userId);
            friend.setFriendId(strangerUserId);
            friend.setCreated(System.currentTimeMillis());
            friend.setId(ObjectId.get());
            mongoTemplate.save(friend);
        }

        //添加在陌生人中添加当前用户为好友
        Query query1 = new Query();
        //先查询是否存在好友
        query1.addCriteria(Criteria.where("userId").is(strangerUserId)
                .and("friendId").is(userId));
        //不存在好友则添加有好友
        if(! mongoTemplate.exists(query1, Friend.class)){
            Friend friend = new Friend();
            friend.setUserId(strangerUserId);
            friend.setFriendId(userId);
            friend.setCreated(System.currentTimeMillis());
            friend.setId(ObjectId.get());
            mongoTemplate.save(friend);
        }
    }

    /**
     * 分页查询好友列表
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult<Friend> findPage(Long userId, long page, long pagesize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        //总记录数
        long counts=mongoTemplate.count(query,Friend.class);
        //分页查询
        query.limit((int)pagesize).skip((page-1)*pagesize);
        List<Friend> friendList = mongoTemplate.find(query, Friend.class);
        //总页数
        long pages=counts/pagesize+(counts%pagesize==0?0:1);
        //封装返回数据
        return new PageResult<Friend>(counts,pagesize,pages,page,friendList);
    }
}
