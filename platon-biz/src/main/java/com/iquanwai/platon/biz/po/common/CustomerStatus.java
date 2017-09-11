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

    public static final Integer OPEN_BIBLE = 1;
}
