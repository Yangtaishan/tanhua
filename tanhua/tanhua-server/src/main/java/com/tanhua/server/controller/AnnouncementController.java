package com.tanhua.server.controller;

import com.tanhua.domain.vo.AnnouncementVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.server.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/messages")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 公告列表
     * @param page
     * @param pagesize
     * @return
     */
    @RequestMapping(value = "/announcements",method = RequestMethod.GET)
    public ResponseEntity announcementList(@RequestParam(value = "1") int page,@RequestParam(value = "10") int pagesize){
        PageResult<AnnouncementVo> pageResultVo=announcementService.announcementList(page,pagesize);
        return ResponseEntity.ok(pageResultVo);
    }
}
