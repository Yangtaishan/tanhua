package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.Question;

public interface QuestionApi {

    /**
     * 根据id查问题表
     * @param userId
     * @return
     */
    Question queryByUserId(Long userId);

    /**
     * 设置陌生人问题 - 保存
     * @param question
     */
    void saveQuestion(Question question);

    /**
     * 设置陌生人问题 - 保存 - 更新
     * @param question
     */
    void updateQuestion(Question question);
}
