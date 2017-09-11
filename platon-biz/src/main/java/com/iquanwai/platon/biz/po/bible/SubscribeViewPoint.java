package com.iquanwai.platon.biz.po.bible;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/9/6.
 */
@Data
public class SubscribeViewPoint {
    private Integer id;
    private Integer profileId;
    private Integer tagId;
    private Double point;
    private Date LearnDate;
}
