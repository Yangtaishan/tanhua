package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.PageResult;

public interface FriendApi {

    /**
     * 添加好友
     * @param userId
     * @param strangerUserId
     */
    void add(Long userId, Long strangerUserId);

    /**
     * 分页查询好友列表
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult<Friend> findPage(Long userId, long page, long pagesize);
}
