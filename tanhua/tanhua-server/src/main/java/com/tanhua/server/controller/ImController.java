package com.tanhua.server.controller;

import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;

import com.tanhua.server.service.ImService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/messages")
public class ImController {

    @Autowired
    private ImService imService;

    /**
     * 联系人添加
     * “聊一下”一键添加好友
     * @param paramMap
     * @return
     */
    @RequestMapping(value = "/contacts",method = RequestMethod.POST)
    public ResponseEntity addContacts(@RequestBody Map<String,Long> paramMap){
        Long StrangerUserId=paramMap.get("userId");
        imService.addContacts(StrangerUserId);
        return ResponseEntity.ok(null);
    }

    /**
     * 联系人列表
     * @param page
     * @param pagesize
     * @param keyword
     * @return
     */
    @RequestMapping(value = "/contacts",method = RequestMethod.GET)
    public ResponseEntity queryContactsList(@RequestParam(defaultValue = "1")  long page,
                                            @RequestParam(defaultValue = "10") long pagesize,
                                            @RequestParam(required = false) String keyword){

        PageResult<ContactVo> pageResult =imService.queryContactsList(page,pagesize,keyword);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 喜欢列表
     * @param page
     * @param pagesize
     * @return
     */
    @RequestMapping(value = "/loves",method = RequestMethod.GET)
    public ResponseEntity loves(@RequestParam(defaultValue = "1") long page,
                                @RequestParam(defaultValue = "10") long pagesize){
        PageResult<MessageVo> pageResult=imService.queryCommentPage(page,pagesize,3);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 点赞列表
     * @param page
     * @param pagesize
     * @return
     */
    @RequestMapping(value = "/likes",method = RequestMethod.GET)
    public ResponseEntity likes(@RequestParam(defaultValue = "1") long page,
                                @RequestParam(defaultValue = "10") long pagesize){
        PageResult<MessageVo> pageResult=imService.queryCommentPage(page,pagesize,1);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 评论列表
     * @param page
     * @param pagesize
     * @return
     */
    @RequestMapping(value = "/comments",method = RequestMethod.GET)
    public ResponseEntity comments(@RequestParam(defaultValue = "1") long page,
                                @RequestParam(defaultValue = "10") long pagesize){
        PageResult<MessageVo> pageResult=imService.queryCommentPage(page,pagesize,2);
        return ResponseEntity.ok(pageResult);
    }
}
