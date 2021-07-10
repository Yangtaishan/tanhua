package com.tanhua.dubbo.api.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.User;
import com.tanhua.dubbo.mapper.UserMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class UserApiImpl implements UserApi {

    @Autowired
    private UserMapper userMapper;

    /**
     * 添加用户
     * @param user
     * @return
     */
    @Override
    public Long saveUser(User user) {
       /* user.setCreated(new Date());
        user.setUpdated(new Date());*/
        userMapper.insert(user);
        return user.getId();
    }

    /**
     * 通过手机查询用户
     * @param mobile
     * @return
     */
    @Override
    public User findByMobile(String mobile) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile",mobile);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 修改手机号 - 3 保存
     * @param userId
     * @param newMobile
     */
    @Override
    public void updateMobile(Long userId, String newMobile) {
        User user = new User();
        user.setId(userId);
        user.setMobile(newMobile);
        userMapper.updateById(user);
    }
}
