package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.RecommendUserQueryParam;
import com.tanhua.domain.vo.TodayBestVo;
import com.tanhua.dubbo.api.db.QuestionApi;
import com.tanhua.dubbo.api.mongo.RecommendUserApi;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.lang3.RandomUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TodayBestService {

    @Reference
    private RecommendUserApi recommendUserApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private QuestionApi questionApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    /**
     * 今日佳人
     * @return
     */
    public TodayBestVo todayBest() {
        //拦截器校验token
        //从本地线程中获取用户id
        Long userId = UserHolder.getUserId();
        //调通服务提供者,通过用户id查询数据库得到与用户有缘分的人,并找到缘分值最大的,如果没有则推荐默认有缘人
        RecommendUser recommendUser =recommendUserApi.queryWithMaxScore(userId);
        if(recommendUser==null){//如果没有推荐默认有缘用户
            recommendUser=new RecommendUser();
            recommendUser.setUserId(2l);
            recommendUser.setScore(95d);
        }
        //根据查询的推荐用户,查询推荐用户的信息
        UserInfo recommendUserInfo = userInfoApi.findById(recommendUser.getUserId());
        //构造返回前端的vo对象
        TodayBestVo todayBestVo = new TodayBestVo();
        BeanUtils.copyProperties(recommendUserInfo,todayBestVo);
        todayBestVo.setFateValue(recommendUser.getScore().intValue());
        if (!StringUtils.isEmpty(recommendUserInfo.getTags())){
            todayBestVo.setTags(recommendUserInfo.getTags().split(","));
        }

        return todayBestVo;
    }

    /**
     * 推荐朋友
     * @param recommendUserQueryParam
     * @return
     */
    public PageResult<TodayBestVo> recommendList(RecommendUserQueryParam recommendUserQueryParam) {
        //拦截器校验token,在当前线程中获取用户信息
        Long userId = UserHolder.getUserId();
        PageResult<RecommendUser> pageResult=recommendUserApi.findPage(recommendUserQueryParam.getPage(),recommendUserQueryParam.getPagesize(),userId);
        List<RecommendUser> items = pageResult.getItems();
        //如果没有推荐用户,使用默认的推荐表
        if(CollectionUtils.isEmpty(items)){
            pageResult=new PageResult<RecommendUser>(10l,recommendUserQueryParam.getPagesize().longValue(),1l,1l,null);
            items=defaultRecommend();
        }
        //查询用户推荐用户基本信息userInfo
        ArrayList<TodayBestVo> todayBestVos = new ArrayList<>();
        for (RecommendUser item : items) {
            UserInfo userInfo = userInfoApi.findById(item.getUserId());
            TodayBestVo todayBestVo = new TodayBestVo();
            BeanUtils.copyProperties(userInfo,todayBestVo);
            todayBestVo.setId(item.getUserId().intValue());
            todayBestVo.setFateValue(item.getScore().intValue());
            todayBestVo.setTags(StringUtils.split(userInfo.getTags(),","));
            todayBestVos.add(todayBestVo);
        }
        //替换分页结果中的列表
        return new PageResult<TodayBestVo>(pageResult.getCounts(),pageResult.getPagesize(),pageResult.getPages(),pageResult.getPage(),todayBestVos);

    }

    /**
     * 设置默认推荐列表
     * @return
     */
    private List<RecommendUser> defaultRecommend(){
        String ids="1,2,3,4,5,6,7,8,9,10";
        List<RecommendUser> list=new ArrayList<>();
        for (String id : ids.split(",")) {
            RecommendUser recommendUser = new RecommendUser();
            recommendUser.setUserId(Long.valueOf(id));
            recommendUser.setScore(RandomUtils.nextDouble(70,98));
            list.add(recommendUser);
        }
        return list;
    }

    /**
     * 佳人信息
     * @param userId
     * @return
     */
    public TodayBestVo getUserInfo(Long userId) {
        UserInfo userInfo = userInfoApi.findById(userId);
        TodayBestVo todayBestVo = new TodayBestVo();
        BeanUtils.copyProperties(userInfo,todayBestVo,"tags");//不复制tags属性
        Double score=recommendUserApi.queryScore(userId,UserHolder.getUserId());
        if (score==null){
            todayBestVo.setFateValue(80);
        }else{
            todayBestVo.setFateValue(score.intValue());
        }
        todayBestVo.setTags(userInfo.getTags().split(","));
        return todayBestVo;
    }

    /**
     * 查询陌生人问题
     * @param userId
     * @return
     */
    public String querystrangerQuestions(Long userId) {
        Question question = questionApi.queryByUserId(userId);
        //如果用户没有设置陌生人问题,默认设置如下
        if(question==null || StringUtils.isEmpty(question.getTxt())){
            return "你喜欢我吗?";
        }
        return question.getTxt();
    }

    /**
     * 回复陌生人问题
     * @param recommendUserId
     * @param content
     */
    public void replystrangerQuestions(Long recommendUserId, String content) {
        UserInfo currentUserInfo = userInfoApi.findById(UserHolder.getUserId()); //当前登录用户的信息
        Question question = questionApi.queryByUserId(recommendUserId); //根据推荐用户id查询推荐用户的陌生人问题
        //构建消息内容
        Map<String,String> map =new HashMap<String,String>();
        map.put("userId",currentUserInfo.getId().toString());
        map.put("nickname",currentUserInfo.getNickname());
        map.put("strangerQuestion",question==null?"你喜欢我吗?":question.getTxt());
        map.put("reply",content);
        String msg= JSON.toJSONString(map);
        //发送消息
        huanXinTemplate.sendMsg(recommendUserId.toString(),msg);
    }
}
