package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.domain.fragmentation.plan.manager.Chapter;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/6/1.
 */
@Data
public class ImprovementReport {
    private String problem; //小课
    private Integer planId;
    private String pic; // 头图
    private Integer totalScore; //小课总得分
    private Integer percent; //打败了百分之多少的同学
    private Integer mustStudyDays;
    private Integer studyDays; //学习时长
    private List<Chapter> chapterList; //各个章的巩固练习得分
    private Integer applicationCompleteCount; //应用练习完成数
    private Integer applicationShouldCount;// 应用练习总份数
    private Integer applicationScore; //应用练习得分
    private Integer applicationTotalScore;//应用练习总分
    private Integer shareVoteCount; //送出多少赞
    private Integer receiveVoteCount;//收获多少赞
    private Integer votedScore; // 点赞收获多少分

    // data
    private Boolean showNextBtn;
    private Boolean doneAllApps;

}
