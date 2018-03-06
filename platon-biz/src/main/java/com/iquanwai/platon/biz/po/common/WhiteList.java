package com.iquanwai.platon.biz.po.common;

import lombok.Data;

/**
 * Created by justin on 16/12/26.
 */
@Data
public class WhiteList {
    private int id;
    private String function;
    private Integer profileId;

    //碎片化练习
    public final static String TEST = "TEST";
    public final static String TRIAL = "RISE_PROBLEM_TRIAL";


}
