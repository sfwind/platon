package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class Problem {
    private int id;
    private String problem; // 工作生活中遇到的问题
    private String pic; //头图
    private Integer length; //问题训练天数
    private Integer warmupCount; //热身训练次数
    private Integer applicationCount; //应用训练次数
    private Integer challengeCount; //专题训练次数
    private String description; //富文本描述
}
