package com.iquanwai.platon.biz.po;

import lombok.Data;

@Data
public class EssenceCard {

    /**
     * db 字段
     */
    private Integer id;
    private Integer problemId; // 小课id
    private Integer chapterId; // 章节id
    private String essenceContent; // 精华卡片文案

    /**
     * 非 db 字段
     */
    private Boolean completed; // 是否已经完成
    private String chapter; // 章节名称
    private String chapterNo; // 章节号
    private String essenceImgBase; // Base64编码




}
