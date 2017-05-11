package com.iquanwai.platon.biz.po.common;

import lombok.Data;

/**
 * Created by justin on 16/12/26.
 */
@Data
public class WhiteList {
    private int id;
    private String function;
    private String openid;

    //碎片化练习
    public final static String FRAG_PRACTICE = "FRAG_PRACTICE";
    public final static String TEST = "TEST";

}
