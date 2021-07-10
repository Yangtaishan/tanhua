package com.tanhua.server.controller;

import com.tanhua.commons.vo.HuanXinUser;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/huanxin")
@Slf4j
public class HuanxinUserController {

    /**
     * 用户app后发送请求获取当前用户账号和密码,用于环信登录
     * @return
     */
    @RequestMapping(value = "/user",method = RequestMethod.GET)
    public ResponseEntity HuanxinUser(){
        HuanXinUser huanXinUser = new HuanXinUser(UserHolder.getUserId().toString(), "123456", "abc");
        log.debug("app获取用户信息成功了......"+huanXinUser.toString());
        return ResponseEntity.ok(huanXinUser);
    }
}
