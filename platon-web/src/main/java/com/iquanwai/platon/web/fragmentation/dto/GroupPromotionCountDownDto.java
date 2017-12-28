package com.iquanwai.platon.web.fragmentation.dto;

import lombok.Data;

@Data
public class GroupPromotionCountDownDto {

    private Boolean isGroupSuccess; // 是否组团成功
    private Boolean isLeader; // 当前登录用户是否是发起人
    private String leaderName; // 发起人名称
    private Integer remainderCount; // 剩余人数
    private String countDownDay; // 倒计时剩余天数
    private String groupCode; // 团队编号

}
