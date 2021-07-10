package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;

public interface VideosApi {

    /**
     * 视频上传
     * @param video
     */
    void saveSmallVideos(Video video);

    /**
     * 小视频列表
     * @param page
     * @param pagesize
     * @return
     */
    PageResult<Video> querySmallVideos(int page, int pagesize);

    /**
     *视频用户关注
     * @param followUser
     */
    void saveFollowUser(FollowUser followUser);

    /**
     * 视频用户关注 - 取消
     * @param followUser
     */
    void removeFollowUser(FollowUser followUser);
}
