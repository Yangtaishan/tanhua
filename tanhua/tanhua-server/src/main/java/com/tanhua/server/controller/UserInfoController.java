package com.tanhua.server.controller;

import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.server.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户资料 -读取和保存
 */
@RestController
@RequestMapping("/users")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 用户资料--读取
     * @param userID
     * @param huanxinID
     * @param
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)//请求方式不同,执行的方法不同
    public ResponseEntity findUserInfo(Long userID, Long huanxinID){
        UserInfoVo userInfoVo =userInfoService.findUserInfo(userID,huanxinID);
        return ResponseEntity.ok(userInfoVo);
    }

    /**
     * 用户资料--保存
     * @param userInfoVo
     * @param
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity updateUserInfo(@RequestBody UserInfoVo userInfoVo){
        userInfoService.updateUserInfo(userInfoVo);
        return ResponseEntity.ok(null);
    }

    /**
     * 用户资料--保存头像
     * @param headPhoto
     * @param
     * @return
     */
    @RequestMapping(value = "/header",method = RequestMethod.POST)
    public ResponseEntity updateHeaderPhoto(MultipartFile headPhoto){
        userInfoService.updateHeaderPhoto(headPhoto);
        return ResponseEntity.ok(null);
    }
}
