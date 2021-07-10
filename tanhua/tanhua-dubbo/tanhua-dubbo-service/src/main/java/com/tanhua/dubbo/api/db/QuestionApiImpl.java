package com.tanhua.dubbo.api.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.Question;
import com.tanhua.dubbo.mapper.QuestionMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class QuestionApiImpl implements QuestionApi {

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 根据id查问题表
     * @param userId
     * @return
     */
    @Override
    public Question queryByUserId(Long userId) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return questionMapper.selectOne(queryWrapper);
    }

    /**
     * 设置陌生人问题 - 保存
     * @param question
     */
    @Override
    public void saveQuestion(Question question) {
        questionMapper.insert(question);
    }

    /**
     * 设置陌生人问题 - 保存 - 更新
     * @param question
     */
    @Override
    public void updateQuestion(Question question) {
        questionMapper.updateById(question);
    }
}
