package com.iquanwai.platon.biz.domain.fragmentation.operation;

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
    int createGroup(Integer profileId);

    /**
     * 以队员身份参加团队<br/>
     * 给自己开发小课<br/>
     * 如果是参加一个新建团队，额外给团长开课
     */
    boolean participateGroup(Integer profileId, String groupCode);

    boolean participateGroup(String openId, String groupCode);
}
