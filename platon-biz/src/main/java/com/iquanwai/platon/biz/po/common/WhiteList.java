package com.iquanwai.platon.biz.po.common;

import lombok.Data;

/**
 * Created by justin on 16/12/26.
 */
@Data
public class WhiteList {
    private int id;
    private String function;
    private String openid;
    private Integer profileId;

    //碎片化练习
    public final static String FRAG_PRACTICE = "FRAG_PRACTICE";
    public final static String TEST = "TEST";
    public final static String FORUM = "FORUM";
    public final static String FRAG_COURSE_PAY = "FRAG_COURSE_PAY";
    public final static String TRIAL = "RISE_PROBLEM_TRIAL";
    /**
     * 课程表功能
     */
    public final static String SCHEDULE = "SCHEDULE";


}
