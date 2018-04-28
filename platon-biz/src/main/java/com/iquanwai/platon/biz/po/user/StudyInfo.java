package com.iquanwai.platon.biz.po.user;

import lombok.Data;

@Data
public class StudyInfo {
    /**
     * 学习天数
     */
    private Integer learnedDay;

    /**
     * 掌握知识点数量
     */
    private Integer learnedKnowledge;

    /**
     * 打败比例
     */
    private Integer defeatPercent;
}
