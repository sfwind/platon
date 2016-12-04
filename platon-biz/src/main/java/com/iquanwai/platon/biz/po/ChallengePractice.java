package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class ChallengePractice {
    private int id;
    private String question; // 题干
    private String pic; // 图片链接
    private Integer problemId; //问题id
}
