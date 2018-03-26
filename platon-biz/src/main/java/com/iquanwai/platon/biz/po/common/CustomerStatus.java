package com.iquanwai.platon.biz.po.common;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/9/6.
 */
@Data
public class CustomerStatus {
    private Integer profileId;
    private Integer statusId;
    private Boolean del;
    private Date addTime;

    /**
     * 开bible
     */
    public static final Integer OPEN_BIBLE = 1;
    /**
     * 选择tag
     */
    public static final Integer EDIT_TAG = 2;
    /**
     * 申请通过
     */
    public static final Integer APPLY_BUSINESS_SCHOOL_SUCCESS = 3;
    /**
     * 学过试听课
     * @deprecated
     */
    public static final Integer LEARNED_AUDITION = 4;
    /**
     * 是否是老课程表
     */
    public static final Integer OLD_SCHEDULE = 5;
}
