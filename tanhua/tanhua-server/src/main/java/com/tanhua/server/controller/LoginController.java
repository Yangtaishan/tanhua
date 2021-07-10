package com.tanhua.server.controller;

import com.tanhua.domain.db.User;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j//日志注解
public class LoginController {
    @Autowired
    private UserService userService;

    /**
     * 保存用户返回用户id
     * @param user
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    public ResponseEntity saveUser(@RequestBody User user){
        Long userId =userService.saveUser(user);
        log.debug("用户保存成功......"+userId);
        return ResponseEntity.ok(userId);
    }

    /**
     * 根据手机号查询用户
     * @param mobile
     * @return
     */
    @RequestMapping(value = "/findByMobile",method = RequestMethod.GET)
    public ResponseEntity findByMobile(String mobile){
        User user=userService.findByMobile(mobile);
        log.debug("根据手机号查询用户......"+user);
        return ResponseEntity.ok(user);
    }

    /**
     * 登录时输入手机号发送验证码
     * @param params
     * @return
     */
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public ResponseEntity sendValidateCode(@RequestBody Map<String,String> params){
        String mobile = params.get("phone");
        userService.sendValidateCode(mobile);
        return ResponseEntity.ok(null);
    }

    /**
     * 登录时:校验手机号和验证码
     * @param params
     * @return
     */
    @RequestMapping(value = "/loginVerification",method = RequestMethod.POST)
    public ResponseEntity loginVerification(@RequestBody Map<String,String> params){
        String mobile = params.get("phone");
        String verificationCode = params.get("verificationCode");
        //调用业务层登录注册
        Map map=userService.loginVerification(mobile,verificationCode);
//        map集合中包含token和isNew:token作为登录凭证,isNew用于确定时跳转到个人信息完善页面还是首页
        return ResponseEntity.ok(map);
    }

    /**
     * 新用户-1.填写资料
     * @param userInfoVo
     * @param
     * @return
     */
    @RequestMapping(value = "/loginReginfo",method = RequestMethod.POST)
    public ResponseEntity loginReginfo(@RequestBody UserInfoVo userInfoVo){
        userService.loginReginfo(userInfoVo);
        return ResponseEntity.ok(null);
    }

    /**
     * 新用户--选取头像
     * @param headPhoto
     * @param
     * @return
     */
    @RequestMapping(value = "/loginReginfo/head",method = RequestMethod.POST)
    public ResponseEntity loginReginfoHead(MultipartFile headPhoto){
        userService.loginReginfoHead(headPhoto);
        return ResponseEntity.ok(null);
    }
}
