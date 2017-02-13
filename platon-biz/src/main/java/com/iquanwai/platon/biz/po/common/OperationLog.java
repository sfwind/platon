package com.iquanwai.platon.biz.po.common;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 16/9/3.
 */
@Data
public class OperationLog {
    private int id;
    private String openid; //操作人openid
    private String module; //模块
    private String function; //功能
    private String action;   //操作
    private Date operateTime;  //操作时间
    private Date operateDate;  //操作日期
    private String memo;   // 其他信息

    public OperationLog openid(String openid){
        this.openid = openid;
        return this;
    }

    public OperationLog module(String module){
        this.module = module;
        return this;
    }

    public OperationLog function(String function){
        this.function = function;
        return this;
    }

    public OperationLog action(String action){
        this.action = action;
        return this;
    }

    public OperationLog memo(String memo){
        this.memo = memo;
        return this;
    }

    public static OperationLog create(){
        return new OperationLog();
    }
}
