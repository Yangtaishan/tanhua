package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.UserInfo;

public interface UserInfoApi {
    /**
     * 保存用户基础信息
     * @param userInfo
     */
    void saveUserInfo(UserInfo userInfo);

    /**
     * 根据id更新用户基础信息
     * @param userInfo
     */
    void updateUserInfo(UserInfo userInfo);

    /**
     * 通过id查询用户信息
     * @param userId
     * @return
     */
    UserInfo findById(Long userId);
}
