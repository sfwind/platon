package com.iquanwai.platon.biz.po.apply;

import lombok.Data;

import java.util.List;

/**
 * @author nethunder
 * @version 2017-11-22
 * <p>
 * 商学院申请的题目信息
 */
@Data
public class BusinessApplyQuestion {
    private Integer id;
    /**
     * 第几题，具体的题目顺序
     * <p/>
     * 题目前的序号可以写到题目里
     */
    private Integer sequence;
    /**
     * 第几节（组）
     * 多个题目可以合并成一组
     */
    private Integer series;
    /**
     * 题干
     */
    private String question;
    /**
     * 提示语
     */
    private String tips;
    /**
     * Placeholder
     */
    private String placeholder;
    /**
     * 题目类型
     * <p/>
     * 1-picker，2-radio，3-填空题，4-多行填空题，5-地域,6-手机号
     */
    private Integer type;
    /**
     * 是否必做
     * <p/>
     * 当不满足preChoiceId的时候，这个request不起作用
     */
    private Boolean request;
    /**
     * 前置选项
     * <p/>
     * 当用户的选择中包含这个choiceId的时候，显示这道题，否则不显示
     */
    private Integer preChoiceId;
    /**
     * 选项
     * <p/>
     * 当类型为 picker／radio 的时候有这个字段
     */
    private List<BusinessApplyChoice> choices;
    private Boolean del;


    /**
     * 滚动单选
     */
    public static final int PICKER = 1;
    /**
     * 普通单选
     */
    public static final int RADIO = 2;
    /**
     * 填空题
     */
    public static final int BLANK = 3;
    /**
     * 多行填空题
     */
    public static final int MULTI_BLANK = 4;
    /**
     * 地域
     */
    public static final int AREA = 5;
    /**
     * 手机号
     */
    public static final int PHONE = 6;
}
