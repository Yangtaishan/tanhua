package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.FaceTemplate;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.commons.templates.SmsTemplate;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.dubbo.api.db.UserApi;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserService {

    @Reference
    private UserApi userApi;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Value("${tanhua.redisValidateCodeKeyPrefix}")
    private String redisValidateCodeKeyPrefix;

    @Value("${tanhua.tokenKey}")
    private String tokenKey;

    @Autowired
    private SmsTemplate smsTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FaceTemplate faceTemplate;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    /**
     * 保存用户并返回id
     * @param user
     * @return
     */
    public Long saveUser(User user) {
        return userApi.saveUser(user);
    }

    /**
     * 根据手机号查询用户
     * @param mobile
     * @return
     */
    public User findByMobile(String mobile) {
        return userApi.findByMobile(mobile);
    }

    /**
     * 登录注册时发送验证码
     * @param mobile
     */
    public void sendValidateCode(String mobile) {
//        在日志中打印手机号
        log.debug("登录注册发送验证码,手机号****{}",mobile);
//        根据手机号查询redis中验证码是否失效
        String redisCode = redisTemplate.opsForValue().get(redisValidateCodeKeyPrefix + mobile);

//        如果存在,返回错误信息,告知"验证码还未失效"
        if(!StringUtils.isEmpty(redisCode)){
            throw new TanHuaException(ErrorResult.duplicate());
        }

//        如果为空则表示不存在,需要调用短信平台发送验证码
        String validateCode="123456";

//        工具类随机生成6位验证码,先将验证码写死,便于后面测试
      /*  String randomNum= RandomStringUtils.randomNumeric(6);
        validateCode=randomNum;

        Map<String, String> stringStringMap = smsTemplate.sendValidateCode(mobile, validateCode);
        if (stringStringMap !=null){//发送失败
            throw new TanHuaException(ErrorResult.error());
        }*/

        log.debug("登录注册发送验证码,手机号****{}******",mobile,validateCode);
//        发送成功,将验证码写入redis中,并设置失效时长和单位
        redisTemplate.opsForValue().set(redisValidateCodeKeyPrefix + mobile,validateCode,5, TimeUnit.MINUTES);
    }

    /**
     *登录时:校验手机号和验证码
     * @param mobile
     * @param verificationCode
     * @return
     */
    public Map loginVerification(String mobile, String verificationCode) {
        //定义返回map集合
        HashMap<String, Object> map = new HashMap<>();
        //默认为登录
        map.put("isNew",false);
        //根据手机号查询redis中是否有验证码
        String redisCode = redisTemplate.opsForValue().get(redisValidateCodeKeyPrefix + mobile);
        //如果不存在说明校验码失效
        if(StringUtils.isEmpty(redisCode)){
            throw new TanHuaException(ErrorResult.loginError());
        }
        //如果存在则校验验证码
        if(!redisCode.equals(verificationCode)){
            //校验码错误抛异常
            throw new TanHuaException(ErrorResult.validateCodeError());
        }
        //没抛异常则说明验证码校验成功
        //调用服务提供者,判断手机号是否存在,判断是否时新用户
        User user = userApi.findByMobile(mobile);
        //如果用户不存在,保存用户,生成token保存到redis中
        if(user==null){//注册
            user = new User();
            user.setMobile(mobile);
            user.setPassword(DigestUtils.md5Hex(mobile.substring(mobile.length()-6)));
            //保存用户并返回id
            Long userId = userApi.saveUser(user);
            user.setId(userId);
            //设置为新用户
            map.put("isNew",true);
            //新用户在环信中注册
            huanXinTemplate.register(userId);
        }

        //验证成功,则直接生成token存入redis
        String token = jwtUtils.createJwt(mobile, user.getId());
        map.put("token",token);

        //将user,token存到redis中
        String userStr = JSON.toJSONString(user);
        redisTemplate.opsForValue().set(tokenKey+token,userStr,1,TimeUnit.DAYS);

        //登录成功后删除redis中验证码
        redisTemplate.delete(redisValidateCodeKeyPrefix+mobile);

        return map;
    }

    /**
     * 新用户-1.填写资料
     * @param userInfoVo
     * @param
     */
    public void loginReginfo(UserInfoVo userInfoVo) {
        //拦截器判断token是否存在(是否登录)
        //从当前线程中获取userId
        Long userId = UserHolder.getUserId();

        //调用服务提供者保存用户信息
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(userInfoVo,userInfo);//相同信息拷贝
        userInfo.setId(userId);

        userInfoApi.saveUserInfo(userInfo);

    }

    /**
     * 新用户---2.选取头像
     * @param headPhoto
     * @param
     */
    public void loginReginfoHead(MultipartFile headPhoto) {
        try {
            //拦截器判断token是否存在(是否登录)
            //从当前线程中获取userId
            Long userId = UserHolder.getUserId();

            //人脸识别,调用百度云组件
            boolean detect = faceTemplate.detect(headPhoto.getBytes());
            if(!detect){
                throw new TanHuaException(ErrorResult.faceError());
            }
            //人脸识别成功后上传头像,调用阿里云oss组件
            String filename = headPhoto.getOriginalFilename();
            String avatar = ossTemplate.upload(filename, headPhoto.getInputStream());
            //更新用户头像
            UserInfo userInfo = new UserInfo();
            userInfo.setAvatar(avatar);
            userInfo.setId(userId);//根据id更新头像
            userInfoApi.updateUserInfo(userInfo);
        } catch (IOException e) {
            throw new TanHuaException(ErrorResult.error());
        }
    }

}
