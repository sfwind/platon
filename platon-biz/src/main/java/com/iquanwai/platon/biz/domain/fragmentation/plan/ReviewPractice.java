package com.iquanwai.platon.biz.domain.fragmentation.plan;

import lombok.Data;

/**
 * Created by justin on 2017/12/8.
 */
@Data
public class ReviewPractice {
    private int type; //类型
    private int status; //-3 过期 -1 锁定 0 解锁

    public static final int STUDY_REPORT = 101; //学习报告
    public static final int STUDY_EXTENSION = 102; //延伸学习
}
