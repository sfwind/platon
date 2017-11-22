package com.iquanwai.platon.biz.po.apply;

import lombok.Data;

/**
 * @author nethunder
 * @version 2017-11-22
 */
@Data
public class BusinessApplyQuestion {
    private Integer id;
    private Integer sequence;
    private Integer series;
    private String question;
    private String tips;
    private Integer type;
    private Boolean request;
    private Integer preChoiceId;
}
