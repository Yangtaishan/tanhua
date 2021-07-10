package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;

public interface RecommendUserApi {

    /**
     * 今日佳人
     * @param userId
     * @return
     */
    RecommendUser queryWithMaxScore(Long userId);


    /**
     * 推荐朋友
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult<RecommendUser> findPage(Integer page, Integer pagesize, Long userId);


    /**
     * 查询缘分值
     * @param userId 推荐用户
     * @param userId1 当前登录用户
     * @return
     */
    Double queryScore(Long userId, Long userId1);
}
