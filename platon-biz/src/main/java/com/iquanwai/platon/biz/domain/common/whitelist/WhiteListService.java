package com.iquanwai.platon.biz.domain.common.whitelist;

import com.iquanwai.platon.biz.po.RiseMember;

import java.util.List;

/**
 * Created by justin on 16/12/26.
 */
public interface WhiteListService {
    boolean isInWhiteList(String function, Integer profileId);

    /**
     * 进入倒计时页面
     * <p/>
     * 开营时间 OpenDate:2017-11-05<br/>
     * 则在2017-11-05 00:00:01 起可以开营
     * @param profileId 用户id
     * @return 是否进入到倒计时页面
     */
    boolean isGoToCountDownNotice(Integer profileId, List<RiseMember> riseMembers);

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
     * 是否显示发现页面
     * @param profileId 用户id
     */
    Boolean isShowExploreTab(Integer profileId, List<RiseMember> riseMembers);

    /**
     * 是否进入倒计时页面 <br/>
     * 只有未开课的专项课的用户才能进
     */
    boolean isGoCampCountDownPage(Integer profileId);

    boolean isGoGroupPromotionCountDownPage(Integer profileId);

    /**
     * 判断一个人是否有正在学习的专项课
     * @param profileId
     * @return
     */
    boolean isStillLearningCamp(Integer profileId);

    /**
     * 判断参加一带二活动或者领取礼品卡的人是否到学习时间
     * @param profileId
     * @return
     */
    boolean isProOrCardOnDate(Integer profileId);
}
