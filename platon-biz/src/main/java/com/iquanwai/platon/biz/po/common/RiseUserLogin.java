package com.iquanwai.platon.biz.po.common;

import lombok.Data;

import java.util.Date;

/**
 * Created by 三十文
 */
@Data
public class RiseUserLogin {

    private Integer id;
    private Integer profileId;
    private Date loginDate;
    private Integer diffDay;

}
