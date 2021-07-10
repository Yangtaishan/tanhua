package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;

public interface BlacklistApi {

    /**
     * 黑名单 - 翻页列表
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult<UserInfo> queryBlacklist(int page, int pagesize, Long userId);

    /**
     * 黑名单 - 移除
     * @param userId
     * @param blackUserId
     */
    void deleteBlacklist(Long userId, Long blackUserId);
}
