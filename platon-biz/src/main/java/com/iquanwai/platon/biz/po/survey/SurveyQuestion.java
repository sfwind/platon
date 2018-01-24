package com.iquanwai.platon.biz.po.survey;

import lombok.Data;

import java.util.List;

/**
 * @author nethunder
 * 问卷题目
 */
@Data
public class SurveyQuestion {
    private Integer id;
    /**
     * 题目分类
     */
    private String category;
    /**
     * 题目code
     */
    private String questionCode;
    /**
     * 题目题干
     */
    private String subject;
    /**
     * 版本
     */
    private Integer version;
    /**
     * 组内顺序
     */
    private Integer sequence;
    /**
     * 第几组
     */
    private Integer series;
    /**
     * 提示信息
     */
    private String tips;
    /**
     * Placeholder
     */
    private String placeholder;
    /**
     * 题目类型
     * <p/>
     * 1-picker，2-radio，3-填空题，4-多行填空题，5-地域，6-电话，7-一张图，8-上传图片，9-多选
     */
    private Integer type;
    /**
     * 是否必做
     */
    private Boolean request;
    /**
     * 前置选项，选了什么之后会出现这个题
     */
    private Integer preChoiceId;
    /**
     * 是否删除
     */
    private Boolean del;
    /**
     * Memo
     */
    private String memo;

    //非DB字段
    /**
     * 选项
     */
    private List<SurveyChoice> choices;


    public static final String EVALUATION_OTHER = "evaluation-other";

    public interface MEMO_TYPE {
        String IDENTIFY = "identity";
        String PHONE = "phone";
        String WECHAT_CODE = "wechat_code";
    }

}
