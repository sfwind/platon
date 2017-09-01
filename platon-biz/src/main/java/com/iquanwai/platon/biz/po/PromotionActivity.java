package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by xfduan on 2017/8/11.
 */
@Data
public class PromotionActivity {

    private Integer id;
    private Integer profileId; // 用户 id
    private String activity;  // 活动类型
    private Integer action; // 行为分类

    private Date AddTime;

}
