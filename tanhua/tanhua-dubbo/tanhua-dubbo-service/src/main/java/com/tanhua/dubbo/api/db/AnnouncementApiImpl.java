package com.tanhua.dubbo.api.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.mapper.AnnouncementMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 公告列表
 */
@Service
public class AnnouncementApiImpl implements AnnouncementApi {

    @Autowired
    private AnnouncementMapper announcementMapper;

    /**
     * 公告列表
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult<Announcement> findPage(int page, int pagesize) {
        Page<Announcement> pages = new Page<>(page, pagesize);
        IPage<Announcement> iPage = announcementMapper.selectPage(pages, new QueryWrapper<>());
        PageResult<Announcement> pageResult = new PageResult<>(iPage.getTotal(), iPage.getSize(), iPage.getPages(), iPage.getCurrent(), iPage.getRecords());
        return pageResult;
    }
}
