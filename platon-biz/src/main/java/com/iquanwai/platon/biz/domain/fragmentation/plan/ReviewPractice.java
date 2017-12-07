package com.iquanwai.platon.biz.domain.fragmentation.plan;

import lombok.Data;

/**
 * Created by justin on 2017/12/8.
 */
@Data
public class ReviewPractice {
    private int type; //类型
    private boolean unlocked; //是否解锁

    public static final int STUDY_REPORT = 1; //学习报告
    public static final int STUDY_EXTENSION = 2; //延伸学习
}
