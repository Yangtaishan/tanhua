package com.tanhua.server.interceptor;

import com.alibaba.fastjson.JSON;
import com.tanhua.domain.db.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * 自定义拦截器,判断token
 */
@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

    @Value("${tanhua.tokenKey}")
    private String tokenKey;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("请求进入前置拦截器......");
        //获取请求中的token
        String token = request.getHeader("Authorization");
        if (!StringUtils.isEmpty(token)) {
            //判断redis中是否有对应token的数据
            String userStr = redisTemplate.opsForValue().get(tokenKey + token);
            if (userStr==null){
                response.setStatus(401);//无权限访问
                return false;
            }
            //用户信息存入到当前线程中
            UserHolder.setUser(JSON.parseObject(userStr, User.class));//字符串类型转为User类型
            //根据最后登录时间续期1天
            redisTemplate.expire(tokenKey + token,1, TimeUnit.DAYS);

            return true;//放行
        }
        response.setStatus(401);//无权限访问
        return false;
    }
}
