package com.iquanwai.platon.biz.domain.apply;

import com.iquanwai.platon.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.platon.biz.po.apply.BusinessApplySubmit;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplicationOrder;

import java.util.List;

/**
 * @author nethunder
 * @version 2017-11-22
 */
public interface ApplyService {
    /**
     * 获取商学院问题
     *
     * @param profileId 用户id
     * @return 商学院问卷的问题
     */
    List<BusinessApplyQuestion> loadBusinessApplyQuestions(Integer profileId);

    /**
     * 获取正在审批中的记录
     *
     * @param profileId 用户id
     * @return 审批记录
     */
    BusinessSchoolApplication loadCheckingApply(Integer profileId);

    BusinessSchoolApplicationOrder loadUnAppliedOrder(Integer profileId);

    /**
     * 提交商学院申请
     *
     * @param profileId        用户id
     * @param userApplySubmits 用户的申请记录
     * @param orderId          订单id
     */
    void submitBusinessApply(Integer profileId, List<BusinessApplySubmit> userApplySubmits, String orderId);
}
