package com.iquanwai.platon.biz.po.common;

import lombok.Data;

/**
 * Created by nethunder on 2017/9/6.
 */
@Data
public class CustomerStatus {
    private Integer profileId;
    private Integer statusId;
    private Boolean del;

    /**
     * 开bible
     */
    public static final Integer OPEN_BIBLE = 1;
    /**
     * 选择tag
     */
    public static final Integer EDIT_TAG = 2;
    /**
     * 购买商学院
     */
    public static final Integer PAY_BUSINESS = 3;
    /**
     * 学过试听课
     * @deprecated
     */
    public static final Integer LEARNED_AUDITION = 4;
    /**
     * 是否不用开课程表
     */
    public static final Integer SCHEDULE_LESS = 5;
}
