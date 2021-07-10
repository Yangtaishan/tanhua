package com.tanhua.server.service;

import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.FriendApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ImService {

    @Reference
    private FriendApi friendApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private CommentApi commentApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    /**
     * 联系人添加
     * “聊一下”一键添加好友
     * @param strangerUserId
     */
    public void addContacts(Long strangerUserId) {
        Long userId = UserHolder.getUserId();
        friendApi.add(userId,strangerUserId);
        //环信上加为好友
        huanXinTemplate.makeFriends(userId,strangerUserId);
    }

    /**
     * 联系人列表
     * @param page
     * @param pagesize
     * @param keyword
     * @return
     */
    public PageResult<ContactVo> queryContactsList(long page, long pagesize, String keyword) {
        PageResult<ContactVo> voPageResult = new PageResult<>();

        //查询当前用户好友列表
        PageResult<Friend> pageResult=friendApi.findPage(UserHolder.getUserId(),page,pagesize);
        //获取数据列表
        List<Friend> friendList = pageResult.getItems();
        //构造列表vo
        ArrayList<ContactVo> contactVoList = new ArrayList<>();
        long myId=1;
        for (Friend friend : friendList) {
            UserInfo friendUserInfo = userInfoApi.findById(friend.getFriendId());
            ContactVo contactVo = new ContactVo();
            BeanUtils.copyProperties(friendUserInfo,contactVo);
            contactVo.setUserId(friendUserInfo.getId().toString());
            contactVo.setId(myId);//
            contactVoList.add(contactVo);
            myId ++;
        }
        BeanUtils.copyProperties(pageResult,voPageResult);
        voPageResult.setItems(contactVoList);

        return voPageResult;
    }

    /**
     * 点赞 喜欢 评论 列表
     * 评论类型，1-点赞，2-评论，3-喜欢
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult<MessageVo> queryCommentPage(long page, long pagesize,int num) {
        PageResult<MessageVo> voPageResult = new PageResult<>();
        Long userId = UserHolder.getUserId();
       PageResult<Comment> pageResult= commentApi.queryCommentsUserIdPage(userId,page,pagesize,num);
       if(pageResult==null || CollectionUtils.isEmpty(pageResult.getItems())){
           voPageResult=new PageResult<>(0l,10l,0l,1l,null);
           return voPageResult;
       }
        List<MessageVo> messageVoList = new ArrayList<>();
        for (Comment comment : pageResult.getItems()) {
            MessageVo messageVo = new MessageVo();
            Long userId1 = comment.getUserId();//评论人id
            UserInfo userInfo = userInfoApi.findById(userId1);//评论人基本信息
            BeanUtils.copyProperties(userInfo,messageVo);
            messageVo.setId(userId1.toString());
            Long created = comment.getCreated();
            messageVo.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(created)));
            messageVoList.add(messageVo);
        }
        BeanUtils.copyProperties(pageResult,voPageResult);
        voPageResult.setItems(messageVoList);
        return voPageResult;
    }
}
