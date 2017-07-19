package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by xfduan on 2017/7/14.
 */
@Data
public class PromotionUser {

    private Integer id;
    private String openId; // 用户 openId
    private String source; // 用户来源 推广人
    private Integer action; // 0-新用户 1-已试用 2-已付费

}