package com.iquanwai.platon.biz.domain.common.whitelist;

import com.iquanwai.platon.biz.po.RiseMember;

import java.util.List;

/**
 * Created by justin on 16/12/26.
 */
public interface WhiteListService {
    boolean isInWhiteList(String function, Integer profileId);

    /**
     * 进入课程计划提示页面
     * @param profileId 用户id
     * @return 是否进入
     */
    boolean isGoToScheduleNotice(Integer profileId, List<RiseMember> riseMembers);

    /**
     * 点击商学院白名单
     * @param profileId 用户id
     */
    boolean checkRiseMenuWhiteList(Integer profileId);

    /**
     * 当前是否有未失效的身份
     */
    boolean checkRunningRiseMenuWhiteList(Integer profileId);

    /**
     * 点击专项课白名单
     * @param profileId 用户id
     */
    boolean checkCampMenuWhiteList(Integer profileId);

    /**
     * 进入新的学习页面
     * @param profileId 用户id
     * @param riseMembers 会员信息
     */
    boolean isGoToNewSchedulePlans(Integer profileId, List<RiseMember> riseMembers);

    /**
     * 判断一个人是否有正在学习的专项课
     * @param profileId
     * @return
     */
    boolean isStillLearningCamp(Integer profileId);
}
