package com.tanhua.server.service;

import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.SmsTemplate;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.Settings;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.SettingsVo;
import com.tanhua.dubbo.api.db.BlacklistApi;
import com.tanhua.dubbo.api.db.QuestionApi;
import com.tanhua.dubbo.api.db.SettingsApi;
import com.tanhua.dubbo.api.db.UserApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SettingsService {

    @Reference
    private QuestionApi questionApi;

    @Reference
    private SettingsApi settingsApi;

    @Reference
    private BlacklistApi blacklistApi;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Value("${tanhua.redisValidateCodeKeyPrefixUpdate}")
    private String redisValidateCodeKeyPrefixUpdate;

    @Autowired
    private SmsTemplate smsTemplate;

    @Reference
    private UserApi userApi;

    @Value("${tanhua.tokenKey}")
    private String tokenKey;

    /**
     * 用户通用设置 - 读取
     * @return
     */
    public SettingsVo querySettings() {
        //定义返回对象
        SettingsVo settingsVo=new SettingsVo();

        //从本线程中获取id
        Long userId = UserHolder.getUserId();
        String mobile = UserHolder.getUser().getMobile();
        //查询问题表
        Question question =questionApi.queryByUserId(userId);
        String strangerQuestion="约么....";//设置默认问题
        if(question!=null && !StringUtils.isEmpty(question.getTxt())){
            strangerQuestion=question.getTxt();
        }

        //根据用户id查询通用设置表,记录不存在默认不通知
        Settings settings = settingsApi.queryByUserId(userId);
        if(settings==null){
            settingsVo.setLikeNotification(false);
            settingsVo.setPinglunNotification(false);
            settingsVo.setGonggaoNotification(false);
        }else {
            //复制
            BeanUtils.copyProperties(settings,settingsVo);
        }
        //封装其他数据
        settingsVo.setStrangerQuestion(strangerQuestion);
        settingsVo.setPhone(mobile);

        return settingsVo;
    }

    /**
     * 通知设置 - 保存
     * @param settingsVo
     */
    public void updateSettings(SettingsVo settingsVo) {
        Long userId = UserHolder.getUserId();
        //先查询是否已存在,确定是更新还是新增
        Settings settings = settingsApi.queryByUserId(userId);
        //不存在则保存
        if (settings==null){
            settings=new Settings();
            BeanUtils.copyProperties(settingsVo,settings);
            settings.setUserId(userId);
            settingsApi.saveSettings(settings);
        }else {
            //存在则更新
            Long id = settings.getId();
            BeanUtils.copyProperties(settingsVo,settings);
            settings.setId(id);
            settingsApi.updateSettings(settings);
        }
    }

    /**
     * 设置陌生人问题 - 保存
     * @param txt
     */
    public void updateQuestions(String txt) {
        Long userId = UserHolder.getUserId();
        Question question = questionApi.queryByUserId(userId);
        //判断是否存在,确定时保存还是更新
        if(question==null){//保存
            question = new Question();
            question.setUserId(userId);
            question.setTxt(txt);
            questionApi.saveQuestion(question);
        }else {//更新
            question.setTxt(txt);
            questionApi.updateQuestion(question);
        }
    }

    /**
     * 黑名单 - 翻页列表
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult queryBlacklist(int page, int pagesize) {
        Long userId = UserHolder.getUserId();
        PageResult<UserInfo> pageResult=blacklistApi.queryBlacklist(page,pagesize,userId);
        return pageResult;
    }

    /**
     * 黑名单 - 移除
     * @param blackUserId
     */
    public void deleteBlacklist(Long blackUserId) {
        Long userId = UserHolder.getUserId();
        blacklistApi.deleteBlacklist(userId,blackUserId);
    }

    /**
     * 修改手机号- 1 发送短信验证码
     */
    public void sendValidateCode() {
        //拦截器校验token,从本地线程中获取手机号
        String mobile = UserHolder.getUser().getMobile();
        //先查询redis中是否存在
        String redisCode=redisTemplate.opsForValue().get(redisValidateCodeKeyPrefixUpdate+mobile);
        if (!StringUtils.isEmpty(redisCode)){//不为空,则表示上次的验证码还未失效
            throw new TanHuaException(ErrorResult.duplicate());
        }

        //为空则发送验证码
        //随机生成验证码
        String validateCode="123456";

        //发送验证码
       /* validateCode = RandomStringUtils.randomNumeric(6);
        //调用短信平台发送验证码
        Map<String, String> stringStringMap = smsTemplate.sendValidateCode(mobile, validateCode);
        if (stringStringMap !=null){//返回值不为空代表发送失败
            throw new TanHuaException(ErrorResult.fail());
        }*/

        //将验证码存入redis中,有效时间5分钟
        redisTemplate.opsForValue().set(redisValidateCodeKeyPrefixUpdate+mobile,validateCode,5, TimeUnit.MINUTES);
    }

    /**
     * 修改手机号 - 2 校验验证码
     * @param verificationCode
     * @return
     */
    public Boolean checkVerificationCode(String verificationCode) {
        //拦截器校验token,从本地线程中获取用户信息
        String mobile = UserHolder.getUser().getMobile();
        String redisCode = redisTemplate.opsForValue().get(redisValidateCodeKeyPrefixUpdate + mobile);
        if (StringUtils.isEmpty(redisCode)){//如果为空则说明验证码失效了
            throw new TanHuaException(ErrorResult.loginError());
        }
        if(! redisCode.equals(verificationCode)){//验证码不正确
            return false;
        }
        //验证成功后删除验证码
        redisTemplate.delete(redisValidateCodeKeyPrefixUpdate+mobile);
        log.info("返回值为true");
        return true;
    }

    /**
     * 修改手机号 - 3 保存
     * @param newMobile
     */
    public void updateMobile(String newMobile,String token) {
        //拦截器判断token,获取本地线程中的用户id
        Long userId = UserHolder.getUserId();
        String oldMobile = UserHolder.getUser().getMobile();
        //更新user表
        userApi.updateMobile(userId,newMobile);
        //删除redis中的token数据,让用户重新登录
        redisTemplate.delete(tokenKey+token);
    }
}
