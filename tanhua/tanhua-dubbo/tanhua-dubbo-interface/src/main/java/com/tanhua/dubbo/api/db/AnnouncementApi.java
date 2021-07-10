package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.vo.PageResult;

/**
 * 公告列表
 */
public interface AnnouncementApi {

    /**
     * 公告列表
     * @param page
     * @param pagesize
     * @return
     */
    PageResult<Announcement> findPage(int page, int pagesize);
}
