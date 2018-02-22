package com.iquanwai.platon.biz.po.common;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 2018/2/20.
 */
@Data
public class ActionLog {
    private Integer profileId; //操作人profileId
    private String module; //模块
    private String function; //功能
    private String action;   //操作
    private String memo;   // 其他信息
    private Integer platform; //1-PC,2-服务号,3-小程序
    private String channel; //漏斗渠道


    public ActionLog uid(Integer profileId){
        this.profileId = profileId;
        return this;
    }

    public ActionLog module(String module){
        this.module = module;
        return this;
    }

    public ActionLog function(String function){
        this.function = function;
        return this;
    }

    public ActionLog action(String action){
        this.action = action;
        return this;
    }

    public ActionLog memo(String memo){
        this.memo = memo;
        return this;
    }

    public ActionLog channel(String channel){
        this.channel = channel;
        return this;
    }

    public ActionLog platform(Integer platform){
        this.platform = platform;
        return this;
    }

    public static ActionLog create(){
        return new ActionLog();
    }
}
