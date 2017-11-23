package com.iquanwai.platon.biz.po.apply;

import lombok.Data;

/**
 * @author nethunder
 * @version 2017-11-22
 * <p>
 * 商学院申请的选项
 */
@Data
public class BusinessApplyChoice {
    private Integer id;
    /**
     * 题干
     */
    private String subject;

    /**
     * 问题id
     */
    private Integer questionId;

    /**
     * 顺序
     */
    private Integer sequence;

    private Boolean del;
}
