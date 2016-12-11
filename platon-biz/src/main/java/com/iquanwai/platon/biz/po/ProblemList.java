package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class ProblemList {
    private int id;
    private String openid; //openid
    private Integer problemId; //问题id
    private Integer status; //问题状态（0-待解决，1-解决中，2-已解决）
    private String problem; //非db字段
    
}
