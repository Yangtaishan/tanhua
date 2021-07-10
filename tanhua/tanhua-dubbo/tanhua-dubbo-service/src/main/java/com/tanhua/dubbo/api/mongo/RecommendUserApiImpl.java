package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service
public class RecommendUserApiImpl implements RecommendUserApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 今日佳人
     * @param userId
     * @return
     */
    @Override
    public RecommendUser queryWithMaxScore(Long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("toUserId").is(userId));//根据当前用户查询所有有缘人
        query.with(Sort.by(Sort.Direction.DESC,"score"));//按照分数降序
        query.limit(1);
        RecommendUser recommendUser = mongoTemplate.findOne(query, RecommendUser.class);
        return recommendUser;
    }

    /**
     * 推荐朋友
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult<RecommendUser> findPage(Integer page, Integer pagesize, Long userId) {
        Query query = new Query();
        //查询条件
        query.addCriteria(Criteria.where("toUserId").is(userId));
        //查询总记录数
        long counts = mongoTemplate.count(query, "recommend_user");
        //分页参数设置
        PageRequest pageRequest=PageRequest.of(page-1,pagesize,Sort.by(Sort.Order.desc("score")));
        query.with(pageRequest);
        //查询分页结果
        List<RecommendUser> list = mongoTemplate.find(query, RecommendUser.class, "recommend_user");
        //封装返回对象
        PageResult<RecommendUser> pageResult = new PageResult<>();
        pageResult.setItems(list);
        pageResult.setCounts(counts);
        pageResult.setPage((long)page);
        pageResult.setPagesize((long)pagesize);
        //计算分页
        long pages=counts/pagesize;
        pages +=(counts%pagesize)==0?0:1;
        pageResult.setPages(pages);

        return pageResult;
    }

    /**
     * 查询缘分值
     * @param userId 推荐用户
     * @param userId1 当前登录用户
     * @return
     */
    @Override
    public Double queryScore(Long userId, Long userId1) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId)
                .and("toUserId").is(userId1));
        query.with(Sort.by(Sort.Direction.DESC,"date"));
        RecommendUser recommendUser = mongoTemplate.findOne(query, RecommendUser.class);
        return recommendUser.getScore();
    }
}
