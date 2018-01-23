package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by xfduan on 2017/7/14.
 */
@Data
public class PromotionLevel {

    private Integer id; // id
    private Integer profileId; // 新人 profileId
    private Integer promoterId; // 推广人 profileId
    private String activity; // 活动类型
    private Integer valid; // 是否有效 0 - 无效， 1 - 有效
    private Integer level; // 层级

}
