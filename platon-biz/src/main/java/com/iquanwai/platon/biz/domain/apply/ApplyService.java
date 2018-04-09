package com.iquanwai.platon.biz.domain.apply;

import com.iquanwai.platon.biz.exception.ApplyException;
import com.iquanwai.platon.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.platon.biz.po.apply.BusinessApplySubmit;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplication;

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
     * 获取用户的所有审核信息
     *
     * @param profileId 用户id
     * @return 所有审核记录
     */
    List<BusinessSchoolApplication> loadApplyList(Integer profileId);

    /**
     * 提交商学院申请
     *
     * @param profileId        用户id
     * @param userApplySubmits 用户的申请记录
     * @param valid            是否有效
     */
    void submitBusinessApply(Integer profileId, List<BusinessApplySubmit> userApplySubmits, Boolean valid,Integer project);

    /**
     * 检查是否有申请权限
     *
     * @param profileId 用户id
     * @throws ApplyException 申请异常
     */
    void checkApplyPrivilege(Integer profileId,Integer project) throws ApplyException;
}
