package com.tanhua.dubbo.api.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.BlackList;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.mapper.BlacklistMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class BlacklistApiImpl implements BlacklistApi {

    @Autowired
    private BlacklistMapper blacklistMapper;

    /**
     * 黑名单 - 翻页列表
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult<UserInfo> queryBlacklist(int page, int pagesize, Long userId) {
        //封装分页对象
        Page pageRequest = new Page(page, pagesize);
        //黑名单分页查询
        IPage<UserInfo> userInfoIPage = blacklistMapper.queryBlacklist(pageRequest, userId);
        //封装结果
        return new PageResult<>(userInfoIPage.getTotal(),(long)pagesize,userInfoIPage.getPages(),(long)page,userInfoIPage.getRecords());

    }

    /**
     * 黑名单 - 移除
     * @param userId
     * @param blackUserId
     */
    @Override
    public void deleteBlacklist(Long userId, Long blackUserId) {
        QueryWrapper<BlackList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        queryWrapper.eq("black_user_id",blackUserId);
        blacklistMapper.delete(queryWrapper);
    }
}
