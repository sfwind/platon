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
    private List<PracticePlan> preview;
    private List<Chapter> chapters;
    private List<ReviewPractice> review;
    private Integer problemId;
    private String problemName;
    private String headPic;
}
