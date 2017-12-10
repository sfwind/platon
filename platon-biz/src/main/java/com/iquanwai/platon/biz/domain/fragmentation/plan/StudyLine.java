package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.domain.fragmentation.plan.manager.Chapter;
import com.iquanwai.platon.biz.po.PracticePlan;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 2017/12/7.
 */
@Data
public class StudyLine {
    private List<PracticePlan> preview; //课前准备
    private List<Chapter> chapters; //课程章节
    private List<ReviewPractice> review; //课后复习
    private Integer problemId;  //课程id
    private String problemName; //课程名称
    private String headPic; //课程头图
    private Integer problemType; //课程类型 1-主修 2-辅修
}
