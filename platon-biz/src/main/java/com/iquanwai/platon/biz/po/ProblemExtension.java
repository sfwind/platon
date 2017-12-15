package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ProblemExtension {
    /**
     * db 字段
     */
    private Integer id;
    private String catalog; // 分类名称
    private String subCatalog; // 子类名称
    private String problem; // 课程名称
    private Integer problemId; // 课程 Id
    private String extension; // 延伸阅读
    private Date addTime; // 添加时间
    private Date updateTime; // 更新时间

    /**
     * 非 db 字段
     */
    private String recommendation; // 推荐阅读，数据库补充数据用
    private List<ProblemActivity> activities; // 相关活动
    private List<ProblemActivity> onlineActivities; // 线上活动
    private List<ProblemActivity> offlineActivities; // 线下活动
}
