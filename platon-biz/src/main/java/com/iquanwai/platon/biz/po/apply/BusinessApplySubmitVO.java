package com.iquanwai.platon.biz.po.apply;

import lombok.Data;

/**
 * @author nethunder
 * @version 2017-11-23
 * <p>
 * 商学院申请的提交记录
 */
@Data
public class BusinessApplySubmitVO {
    private Integer questionId;
    private String userValue;
    private Integer choiceId;
}
