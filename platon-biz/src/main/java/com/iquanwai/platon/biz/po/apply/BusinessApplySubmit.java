package com.iquanwai.platon.biz.po.apply;

import lombok.Data;

/**
 * @author nethunder
 * @version 2017-11-23
 * <p>
 * 申请提交记录
 */
@Data
public class BusinessApplySubmit {
    private Integer id;
    private Integer applyId;
    private Integer questionId;
    private Integer choiceId;
    private String choiceText;
    private String userValue;
}
