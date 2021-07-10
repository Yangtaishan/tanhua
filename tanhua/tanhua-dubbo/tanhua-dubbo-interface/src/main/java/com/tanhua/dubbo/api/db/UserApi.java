package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.User;

public interface UserApi {

    /**
     * 添加用户
     * @param user
     * @return
     */
    Long saveUser(User user);

    /**
     * 根据手机号查询用户
     * @param mobile
     * @return
     */
    User findByMobile(String mobile);

    /**
     * 修改手机号 - 3 保存
     * @param userId
     * @param newMobile
     */
    void updateMobile(Long userId, String newMobile);
}
