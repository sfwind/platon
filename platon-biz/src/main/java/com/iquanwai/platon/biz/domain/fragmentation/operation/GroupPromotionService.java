package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.iquanwai.platon.biz.po.GroupPromotion;

import java.util.List;

public interface GroupPromotionService {
    /**
     * 查看一个用户是否有资格参加组团学习活动<br/>
     * 参加资格：<br/>
     * 1. 从未关注过服务号的新人<br/>
     * 2. 没有过付费行为并且没有参加过试听课的人
     */
    boolean checkGroupPromotionAuthority(String openId);

    /**
     * 建立团队，并且担任团长
     */
    GroupPromotion createGroup(Integer profileId);

    /**
     * 以队员身份参加团队<br/>
     * 给自己开发小课<br/>
     * 如果是参加一个新建团队，额外给团长开课
     */
    boolean participateGroup(Integer profileId, String groupCode);

    boolean participateGroup(String openId, String groupCode);

    /**
     * 查看一个用户是否已经参加团队学习活动
     */
    GroupPromotion hasParticipateGroup(Integer profileId);

    /**
     * 查看当前人员所在团队是否达到活动人数要求
     */
    List<GroupPromotion> loadGroupPromotions(Integer profileId);

    /**
     * 查看一个用户是否是团队 Leader
     */
    boolean isGroupLeader(Integer profileId);

    /**
     * 获取某个用户所在小组的团长昵称
     */
    String loadLeaderName(Integer profileId);
}
