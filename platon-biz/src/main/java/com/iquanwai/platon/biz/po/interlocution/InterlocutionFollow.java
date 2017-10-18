package com.iquanwai.platon.biz.po.interlocution;

import lombok.Data;

/**
 * Created by justin on 17/6/19.
 */
@Data
public class InterlocutionFollow {
    private int id;
    private Integer questionId; //提问id
    private Integer profileId; //投票者id
    private String openid;
    private Boolean del; //是否删除（0-未删除，1-已删除）
}
