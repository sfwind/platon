package com.iquanwai.platon.biz.po.forum;

import lombok.Data;

/**
 * Created by justin on 17/6/19.
 */
@Data
public class QuestionTag {
    private int id;
    private Integer questionId; //提问id
    private Integer tagId; //标签id
    private Boolean del; //是否删除（0-未删除，1-已删除）
}
