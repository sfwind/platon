package com.iquanwai.platon.biz.po.bible;

import lombok.Data;

/**
 * Created by nethunder on 2017/9/6.
 */
@Data
public class SubscribePointCompare {
    private Integer tagId;
    private String tagName;
    private Double yesterdayPoint;
    private Double todayPoint;
    private Double totalPoint;
    private String today;
}
