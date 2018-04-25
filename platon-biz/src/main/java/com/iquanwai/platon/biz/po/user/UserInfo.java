package com.iquanwai.platon.biz.po.user;

import lombok.Data;

@Data
public class UserInfo {

    private Integer profileId;

    /**
     * 行业
     */
    private String industry;

    /**
     * 职业
     */
    private String function;

    /**
     * 首次工作年份
     */
    private String workingYear;

    /**
     * 公司
     */
    private String company;

    /**
     * 毕业院校
     */
    private String college;

    /**
     * 联系人电话
     */
    private String mobile;
    /**
     * 个人简介
     */
    private String introduction;

    /**
     * 地址
     */
    private String address;

    /**
     * 收件电话
     */
    private String receiverMobile;
    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 收件人
     */
    private String receiver;

    /**
     * 提交完整比例（非DB字段）
     */
    private Integer rate;

    /**
     * 判断是否已经完整提交
     */
    private Integer isFull;
    /**
     * 是否优先
     */
    private Integer priority;


}