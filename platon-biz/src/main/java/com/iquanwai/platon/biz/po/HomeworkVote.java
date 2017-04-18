package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by nethunder on 2017/1/2.
 */
@Data
public class HomeworkVote {
    private Integer id;
    private Integer referencedId;// 依赖的id
    private Integer type; //1:小目标,2:应用练习
    private String voteOpenId;//点赞人
    private Integer del;//是否删除，1代表取消点赞
    private String votedOpenid; //被点赞人
    private Integer device; //设备
}
