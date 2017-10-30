package com.iquanwai.platon.biz.po;

import com.alibaba.fastjson.JSON;
import com.iquanwai.platon.biz.domain.fragmentation.plan.Chapter;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class Problem {
    private int id;
    private String problem; // 工作生活中遇到的问题
    private String pic; //头图
    private Integer length; //训练节数
    private Integer catalogId; // 分类
    private Integer subCatalogId; // 子目录分类
    private String author; // 讲师
    private String authorDesc; //讲师介绍
    private String authorPic; // 讲师图片
    private Double difficultyScore;// 难度
    private Double usefulScore; // 实用度
    private String descPic; // 描述图片
    private String audio; //语音
    private Integer audioId;
    private String audioWords;
    private String who; //适合人群
    private String how; //如何学习
    private String why; //为什么学习
    private Boolean del; //是否删除(0-否,1-是)
    private Boolean newProblem; // 是否是新课程
    private Boolean trial; //试用版（0-否,1-是）
    private String categoryPic; //分类图示
    private String abbreviation; // 缩略名

    private Boolean done; // 非db字段 用户是否做过
    private Integer status; // 非db字段 用户选过小课（0-未选过,1-正在进行,2-已完成）
    private Boolean hasProblemScore; // 非db字段 是否已评分
    private List<Chapter> chapterList; // 非db字段 课程表
    private String subCatalog; // 非db字段 字类名
    private String catalog; // 非db字段 类名
    private Integer chosenPersonCount; // 该门小课学习的人数
    private Integer monthlyCampMonth; // 小课对应训练营月份


    public Problem simple() {
        Problem problem = new Problem();
        problem.setId(this.id);
        problem.setProblem(this.problem);
        problem.setPic(this.pic);
        problem.setDel(this.del);
        problem.setNewProblem(this.newProblem);
        problem.setTrial(this.trial);
        problem.setCatalogId(this.catalogId);
        problem.setSubCatalogId(this.subCatalogId);
        problem.setDifficultyScore(this.difficultyScore);
        problem.setStatus(this.status);
        problem.setDone(this.done);
        problem.setHasProblemScore(this.hasProblemScore);
        problem.setSubCatalog(this.subCatalog);
        problem.setCatalog(this.catalog);
        problem.setChosenPersonCount(this.chosenPersonCount);
        problem.setAbbreviation(this.abbreviation);
        return problem;
    }

    public Problem copy() {
        return JSON.parseObject(JSON.toJSONString(this), this.getClass());
    }

}
