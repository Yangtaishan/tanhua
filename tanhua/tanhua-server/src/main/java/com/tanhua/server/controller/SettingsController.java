package com.tanhua.server.controller;

import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.SettingsVo;
import com.tanhua.server.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知设置
 */
@RestController
@RequestMapping("/users")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    /**
     * 用户通用设置 - 读取
     * @return
     */
    @RequestMapping(value = "/settings",method = RequestMethod.GET)
    public ResponseEntity querySettings(){
        SettingsVo settingsVo =settingsService.querySettings();
        return ResponseEntity.ok(settingsVo);
    }

    /**
     * 通知设置 - 保存
     * @return
     */
    @RequestMapping(value = "/notifications/setting",method = RequestMethod.POST)
    public ResponseEntity updateSettings(@RequestBody SettingsVo settingsVo ){
        settingsService.updateSettings(settingsVo);
        return ResponseEntity.ok(null);
    }

    /**
     * 设置陌生人问题 - 保存
     * @param params
     * @return
     */
    @RequestMapping(value = "/questions",method = RequestMethod.POST)
    public ResponseEntity updateQuestions(@RequestBody Map<String,String> params){
        String txt=params.get("content");
        settingsService.updateQuestions(txt);
        return ResponseEntity.ok(null);
    }

    /**
     * 黑名单 - 翻页列表
     * @param page
     * @param pagesize
     * @return
     */
    @RequestMapping(value = "/blacklist",method = RequestMethod.GET)
    public ResponseEntity queryBlacklist(@RequestParam(defaultValue = "1") int page,@RequestParam(defaultValue = "10") int pagesize){
        PageResult pageResult =settingsService.queryBlacklist(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 黑名单 - 移除
     * @param blackUserId
     * @return
     */
    @RequestMapping(value = "/blacklist/{uid}",method = RequestMethod.DELETE)
    public ResponseEntity deleteBlacklist(@PathVariable("uid") Long blackUserId) {
        settingsService.deleteBlacklist(blackUserId);
        return ResponseEntity.ok(null);
    }

    /**
     * 修改手机号- 1 发送短信验证码
     * @return
     */
    @RequestMapping(value = "/phone/sendVerificationCode",method = RequestMethod.POST)
    public ResponseEntity sendValidateCode(){
        settingsService.sendValidateCode();
        return ResponseEntity.ok(null);
    }

    /**
     * 修改手机号 - 2 校验验证码
     * @param params
     * @return
     */
    @RequestMapping(value = "/phone/checkVerificationCode",method = RequestMethod.POST)
    public ResponseEntity checkVerificationCode(@RequestBody Map<String,String> params){
        Boolean flag =settingsService.checkVerificationCode(params.get("verificationCode"));

        //将返回值封装到map集合中
        HashMap<String, Boolean> result = new HashMap<>();
        result.put("verification",flag);

        return ResponseEntity.ok(result);
    }

    /**
     * 修改手机号 - 3 保存
     * @param params
     * @return
     */
    @RequestMapping(value = "/phone",method = RequestMethod.POST)
    public ResponseEntity updateMobile(@RequestBody Map<String,String> params,@RequestHeader("Authorization") String token){
        String newMobile = params.get("phone");
        settingsService.updateMobile(newMobile,token);
        return ResponseEntity.ok(null);
    }
}
