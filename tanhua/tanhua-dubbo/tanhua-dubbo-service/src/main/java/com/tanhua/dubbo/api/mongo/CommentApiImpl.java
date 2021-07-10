package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@Service
public class CommentApiImpl implements CommentApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 动态-点赞/喜欢
     * @param comment
     * @return
     */
    @Override
    public long saveComment(Comment comment) {
        //动态点赞,喜欢,保存在评论表
        comment.setId(ObjectId.get()); //主键id
        comment.setCreated(System.currentTimeMillis()); //点赞时间
        if(comment.getPubType()==1){ //动态
            ObjectId publishId = comment.getPublishId();
            Publish publish = mongoTemplate.findById(publishId, Publish.class);
            if (publish !=null){
                comment.setPublishUserId(publish.getUserId());
            }
        }
        mongoTemplate.save(comment);
        //根据发布id更新发布表中点赞数量+1
        updateComment(comment,1);
        //根据发布id查询发布表点赞数量并返回
        long count = queryCount(comment);
        return count;
    }

    /**
     * 查询点赞/喜欢数量
     * @param comment
     * @return
     */
    private long queryCount(Comment comment) {
        if(comment.getPubType()==1){ //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(comment.getPublishId()));
            Publish publish = mongoTemplate.findOne(query, Publish.class);
            if (comment.getCommentType()==1){  //评论类型，1-点赞，2-评论，3-喜欢
                return  publish.getLikeCount();
            }
            if(comment.getCommentType()==2){
                return publish.getCommentCount();
            }
            if(comment.getCommentType()==3){
                return publish.getLoveCount();
            }
        }
        if(comment.getPubType()==3){
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(comment.getPublishId()));
            Comment comment1 = mongoTemplate.findOne(query, Comment.class);
            if (comment.getCommentType()==1){ //评论类型，1-点赞，2-评论，3-喜欢
                return comment1.getLikeCount();
            }
        }
        return 0;
    }

    /**
     * 根据发布id更新点赞/喜欢数量
     * @param comment
     * @param num
     */
    private void updateComment(Comment comment, int num) {
        if (comment.getPubType()==1 || comment.getPubType()==3){
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(comment.getPublishId())); //根据发布id更新
            Update update = new Update();
            update.inc(comment.getCol(),num); //inc:针对某个字段值增加
            Class myClass= Publish.class;
            if (comment.getPubType()==3){
                myClass=Comment.class;//如果是对评论点赞,则切换为更新评论表
            }
            mongoTemplate.updateFirst(query,update,myClass);
        }
    }

    /**
     * 动态-取消点赞/喜欢
     * @param comment
     * @return
     */
    @Override
    public long removeComment(Comment comment) {
        //评论表中删除动态点赞
        Query query = new Query();
        query.addCriteria(
                Criteria.where("publishId").is(comment.getPublishId()) //发布id
                .and("commentType").is(comment.getCommentType()) //评论类型
                .and("pubType").is(comment.getPubType()) //评论内容类型
                .and("userId").is(comment.getUserId()) //品论人
        );
        mongoTemplate.remove(query,Comment.class);
        //根据发布id更新发布表中点赞数量-1
        updateComment(comment,-1);
        //根据发布id查询发布表点赞数量并返回
        long count = queryCount(comment);
        return count;
    }

    /**
     * 评论列表
     * @param publishId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult<Comment> queryCommentsByPage(String publishId, int page, int pagesize) {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(2) ////评论类型，1-点赞，2-评论，3-喜欢
                .and("pubType").is(1) //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        );
        query.with(Sort.by(Sort.Direction.DESC,"created"));
        query.limit(pagesize).skip((page-1)*pagesize);
        long counts = mongoTemplate.count(query, Comment.class);
        List<Comment> commentList =mongoTemplate.find(query,Comment.class);
        long pages=counts/pagesize+(counts%pagesize==0?0:1);
        return new PageResult<>(counts,(long)pagesize,pages,(long)page,commentList);
    }

    /**
     * 点赞 喜欢 评论 列表
     * 评论类型，1-点赞，2-评论，3-喜欢
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult<Comment> queryCommentsUserIdPage(Long userId, long page, long pagesize,int num) {
        Query query = new Query();
        query.addCriteria(Criteria.where("commentType").is(num).and("publishUserId").is(userId));
        query.with(Sort.by(Sort.Direction.DESC,"created"));
        query.limit((int)pagesize).skip((page-1)*pagesize);//分页
        //总记录数
        long counts=mongoTemplate.count(query,Comment.class);
        //分页查询
        List<Comment> commentList = mongoTemplate.find(query, Comment.class);
        //总页数
        long pages=counts/pagesize+(counts%pagesize==0?0:1);
        return new PageResult<Comment>(counts,pagesize,pages,page,commentList);
    }
}
