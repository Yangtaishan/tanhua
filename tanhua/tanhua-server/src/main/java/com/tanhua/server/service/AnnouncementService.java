package com.tanhua.server.service;

import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.vo.AnnouncementVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.db.AnnouncementApi;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnnouncementService {

    @Reference
    private AnnouncementApi announcementApi;

    /**
     * 公告列表
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult<AnnouncementVo> announcementList(int page, int pagesize) {
        //调用服务提供者分页查询公告
        PageResult<Announcement> pageResult=announcementApi.findPage(page,pagesize);
        //获取所有的公告
        List<Announcement> items = pageResult.getItems();
        ArrayList<AnnouncementVo> list = new ArrayList<>();
        for (Announcement item : items) {
            AnnouncementVo announcementVo = new AnnouncementVo();
            BeanUtils.copyProperties(item,announcementVo);
            if(item.getCreated()!=null){
                announcementVo.setCreateDate(new SimpleDateFormat("yyyy-MM-dd hh:mm").format(item.getCreated()));
            }
            list.add(announcementVo);
        }
        PageResult<AnnouncementVo> pageResultVo = new PageResult<>(pageResult.getCounts(), pageResult.getPagesize(), pageResult.getPages(), pageResult.getPage(), list);
        return pageResultVo;
    }
}
