package com.tanhua.server.controller;

import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.RecommendUserQueryParam;
import com.tanhua.domain.vo.TodayBestVo;
import com.tanhua.server.service.TodayBestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/tanhua")
public class TodayBestController {

    @Autowired
    private TodayBestService todayBestService;

    /**
     * 今日佳人
     * @return
     */
    @RequestMapping(value = "todayBest",method = RequestMethod.GET)
    public ResponseEntity todayBest(){
        TodayBestVo todayBestVo =todayBestService.todayBest();
        return ResponseEntity.ok(todayBestVo);
    }

    /**
     * 推荐朋友
     * @return
     */
    @RequestMapping(value = "/recommendation",method = RequestMethod.GET)
    public ResponseEntity recommendList(RecommendUserQueryParam recommendUserQueryParam){
        PageResult<TodayBestVo> pageResult =todayBestService.recommendList(recommendUserQueryParam);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 佳人信息
     * @param userId
     * @return
     */
    @RequestMapping(value = "/{id}/personalInfo",method = RequestMethod.GET)
    public ResponseEntity queryUserDetail(@PathVariable("id") Long userId){
        TodayBestVo userInfoVo=todayBestService.getUserInfo(userId);
        return ResponseEntity.ok(userInfoVo);
    }

    /**
     * 查询陌生人问题
     * @param userId
     * @return
     */
    @RequestMapping(value = "/strangerQuestions",method = RequestMethod.GET)
    public ResponseEntity querystrangerQuestions( Long userId){
        String questions=todayBestService.querystrangerQuestions(userId);
        return ResponseEntity.ok(questions);
    }

    /**
     * 回复陌生人问题
     * @param paramMap
     * @return
     */
    @RequestMapping(value = "/strangerQuestions",method = RequestMethod.POST)
    public ResponseEntity replystrangerQuestions(@RequestBody Map<String,Object> paramMap){
        Long recommendUserId=Long.parseLong(paramMap.get("userId").toString());
        String content=(String) (paramMap.get("reply"));
        todayBestService.replystrangerQuestions(recommendUserId,content);
        return ResponseEntity.ok(null);
    }
}
