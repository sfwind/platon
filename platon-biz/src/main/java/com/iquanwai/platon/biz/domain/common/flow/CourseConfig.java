package com.iquanwai.platon.biz.domain.common.flow;

import lombok.Data;

import java.util.Date;

@Data
public class CourseConfig {
    /**
     * 售卖开关
     */
    private Boolean purchaseSwitch;
    /**
     * 开启日期
     */
    private Date openDate;
    /**
     * 当前售卖年份
     */
    private Integer sellingYear;
    /**
     * 当前售卖月份
     */
    private Integer sellingMonth;
    /**
     * 该条数据记录是否生效
     */
    private Boolean active;
    /**
     * 售卖中专项课结营日期
     */
    private Date closeDate;
}
