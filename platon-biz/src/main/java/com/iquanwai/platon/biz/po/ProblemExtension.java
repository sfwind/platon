package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

@Data
public class ProblemExtension {
    /**
     * db 字段
     */
    private Integer id;
    private String catalog; // 分类名称
    private String subCatalog; // 子类名称
    private String problem; // 小课名称
    private Integer problemId; // 小课 Id
    private String extension; // 延伸阅读
    private String recommendation; // 推荐学习
    private String online; // 线上活动
    private String offline; // 线下活动
    private Date addTime; // 添加时间
    private Date updateTime; // 更新时间

    /**
     * 非 db 字段
     */
}
