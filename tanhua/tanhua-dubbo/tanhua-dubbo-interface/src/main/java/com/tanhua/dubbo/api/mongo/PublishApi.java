package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;

public interface PublishApi {

    /**
     * 动态-发布
     * @param publishVo
     */
    void savePublish(PublishVo publishVo);


    /**
     * 好友动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult<Publish> queryPublishByTimeLinePage(int page, int pagesize, Long userId);

    /**
     * 推荐动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult<Publish> recommendPublish(int page, int pagesize, Long userId);

    /**
     * 用户动态(我的动态)
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult<Publish> queryMyPublish(int page, int pagesize, Long userId);


    /**
     * 单条动态
     * @param publishId
     * @return
     */
    Publish queryPublish(String publishId);
}
