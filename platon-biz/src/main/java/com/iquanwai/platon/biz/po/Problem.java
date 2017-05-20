package com.iquanwai.platon.biz.po;

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
    private String description; //富文本描述
    private Integer catalogId; // 分类
    private Integer subCatalogId; // 子目录分类
    private String author; // 讲师
    private Double difficultyScore;// 难度
    private String subjectDesc; //小课论坛介绍
    private String descPic; // 描述图片
    private String audio; //语音
    private String who; //适合人群
    private String what; //学习内容
    private String how; //如何学习
    private String why; //为什么学习
    private Boolean del; //是否删除(0-否,1-是)
    private Boolean trial; //试用版（0-否,1-是）

    private Boolean done; // 非db字段 用户是否做过
    private Integer status; // 非db字段 用户选过小课（0-未选过,1-正在进行,2-已完成）
    private Boolean hasProblemScore; // 非db字段 是否已评分
    private List<Chapter> chapterList; // 非db字段 课程表
}
