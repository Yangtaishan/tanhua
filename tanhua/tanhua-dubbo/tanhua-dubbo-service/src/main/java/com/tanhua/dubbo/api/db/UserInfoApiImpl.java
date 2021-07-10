package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.dubbo.mapper.UserInfoMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class UserInfoApiImpl implements UserInfoApi {

    @Autowired
    private UserInfoMapper userInfoMapper;

    /**
     * 保存用户基础信息
     * @param userInfo
     */
    @Override
    public void saveUserInfo(UserInfo userInfo) {
        userInfoMapper.insert(userInfo);
    }

    /**
     * 根据id更新用户基础信息
     * @param userInfo
     */
    @Override
    public void updateUserInfo(UserInfo userInfo) {
        userInfoMapper.updateById(userInfo);
    }

    /**
     * 通过id查询用户信息
     * @param userId
     * @return
     */
    @Override
    public UserInfo findById(Long userId) {
        return userInfoMapper.selectById(userId);
    }
}
