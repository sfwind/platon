package com.iquanwai.platon.biz.domain.apply;

import com.iquanwai.platon.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplication;
import org.apache.commons.lang3.tuple.Pair;

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
     * 检查是否能够申请该项目
     *
     * @param profileId 用户id
     * @param project   项目id
     * @return 是否能够申请
     */
    boolean hasAvailableApply(Integer profileId, Integer project);

    /**
     * 检查是否能够申请该项目
     *
     * @param applyList 用户申请记录
     * @param project   项目id
     * @return 是否能够申请
     */
    boolean hasAvailableApply(List<BusinessSchoolApplication> applyList, Integer project);

    Pair<Long, Integer> loadRemainTimeMemberTypeId(Integer profileId);
}
