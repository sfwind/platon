package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by 三十文 on 2017/10/21
 */
@Data
public class MonthlyCampConfig {

    private Integer id;
    /**
     * 专项课的购买开关
     */
    private Boolean purchaseSwitch;
    /**
     * 售卖中课程的开营日期
     */
    private Date openDate;
    /**
     * 售卖中课程的结营日期
     */
    private Date closeDate;
    /**
     * 售卖中课程的对应年份
     */
    private Integer sellingYear;
    /**
     * 售卖中课程的对应月份
     */
    private Integer sellingMonth;
    /**
     * 该条数据记录是否生效
     */
    private Integer active;

}
