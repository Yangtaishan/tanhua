package com.tanhua.server.interceptor;

import com.tanhua.domain.db.User;

/**
 * 通过threadLocal方式存储用户数据
 */
public class UserHolder {
    private static ThreadLocal<User> userThreadLocal=new ThreadLocal<User>();

    /**
     * 将用户信息存入到当前线程中
     * @param user
     */
    public static void setUser(User user){
        userThreadLocal.set(user);
    }

    /**
     * 从当前线程中获取用户信息
     * @return
     */
    public static User getUser(){
        return userThreadLocal.get();
    }

    /**
     * 获取用户的id
     * @return
     */
    public static Long getUserId(){
        return userThreadLocal.get().getId();
    }
}
