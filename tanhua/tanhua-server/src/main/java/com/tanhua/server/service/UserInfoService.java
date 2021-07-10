package com.tanhua.server.service;

import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.FaceTemplate;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.GetAgeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
public class UserInfoService {

    @Value("${tanhua.tokenKey}")
    private String tokenKey;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private FaceTemplate faceTemplate;

    @Autowired
    private OssTemplate ossTemplate;

    /**
     * 查询用户信息
     * @param userID
     * @param huanxinID
     * @param
     * @return
     */
    public UserInfoVo findUserInfo(Long userID, Long huanxinID) {
        //拦截器判断token是否存在(是否登录)
        //从当前线程中获取userId
        Long userId = UserHolder.getUserId();

        //调用服务提供者,根据id查询tb_userInfo数据
        UserInfo userInfo = userInfoApi.findById(userId);
        //将数据复制到userInfoVo中
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo,userInfoVo);
        //设置年龄,两者间数据类型不同
        if(!StringUtils.isEmpty(userInfo.getAge())){
            userInfoVo.setAge(String.valueOf(userInfo.getAge()));
        }else {
            //调用工具类,将生日转为年龄
            userInfoVo.setAge(String.valueOf(GetAgeUtil.getAge(userInfo.getBirthday())));
        }
        return userInfoVo;
    }

    /**
     * 更新用户信息
     * @param userInfoVo
     * @param
     */
    public void updateUserInfo(UserInfoVo userInfoVo) {
        //拦截器判断token是否存在(是否登录)
        //从当前线程中获取userId
        Long userId = UserHolder.getUserId();

        //调用服务提供者,根据id更新tb_userInfo数据
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(userInfoVo,userInfo);
        if(!StringUtils.isEmpty(userInfoVo.getBirthday())){
            //生日不为空,更新年龄
            userInfo.setAge(GetAgeUtil.getAge(userInfoVo.getBirthday()));
        }
        //设置用户id
        userInfo.setId(userId);
        userInfoApi.updateUserInfo(userInfo);
    }

    /**
     * 用户资料--保存头像
     * @param headPhoto
     * @param
     */
    public void updateHeaderPhoto(MultipartFile headPhoto) {
        try {
            log.info("更新用户头像{},token{}",headPhoto.getOriginalFilename());
            //通过拦截器验证token,在本地线程中获取用户信息,ThreadLocal
            Long userId = UserHolder.getUserId();
            //查询用户信息
            UserInfo userInfo = userInfoApi.findById(userId);
            String oldAvatar = userInfo.getAvatar();//旧的头像地址
            //人脸识别
            boolean detect = faceTemplate.detect(headPhoto.getBytes());
            if (!detect){
                throw new TanHuaException(ErrorResult.faceError());
            }

            //人脸识别成功,上传头像
            String filename=headPhoto.getOriginalFilename();
            String newAvatar = ossTemplate.upload(filename, headPhoto.getInputStream());//上传头像,返回地址

            //更新数据库中头像
            userInfo.setAvatar(newAvatar);
            userInfoApi.updateUserInfo(userInfo);

            //删除阿里云中旧的头像
            ossTemplate.deleteFile(oldAvatar);

        } catch (IOException e) {
            throw new TanHuaException(ErrorResult.error());
        }
    }
}
