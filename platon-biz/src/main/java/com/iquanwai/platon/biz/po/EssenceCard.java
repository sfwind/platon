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
    private String tag; // 成为 XXX 力爆表的人
    private String essenceTitle; // 精华卡片标题
    private String essenceContent; // 精华卡片内容

    /**
     * 非 db 字段
     */
    private String thumbnail; // 缩略图
    private String thumbnailLock; // 锁定缩略图
    private Boolean completed; // 是否已经完成
    private String chapter; // 章节名称
    private String chapterNo; // 章节号

}
