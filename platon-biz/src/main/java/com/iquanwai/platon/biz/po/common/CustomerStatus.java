package com.iquanwai.platon.biz.po.common;

import lombok.Data;

/**
 * Created by nethunder on 2017/9/6.
 */
@Data
public class CustomerStatus {
    private Integer profileId;
    private Integer statusId;
    private Boolean Del;

    public static final Integer OPEN_BIBLE = 1; //开bible
    public static final Integer EDIT_TAG = 2; //选择tag
    public static final Integer PAY_BUSINESS = 3;// 购买商学院
    public static final Integer LEARNED_AUDITION = 4;// 学过试听课
}
