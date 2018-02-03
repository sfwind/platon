package com.iquanwai.platon.biz.domain.fragmentation.plan.manager;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.fragmentation.plan.Practice;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 17/4/12.
 */
@Data
public class Section {
    private Integer section;  //第几小节
    private Integer knowledgeId; //知识点
    private String name; //小节名称
    private Integer series; //序号
    private Integer chapter; //第几章
    private Boolean integrated; //是否是综合练习
    private String chapterName; //章节名
    private List<Practice> practices = Lists.newArrayList(); //练习
    private Integer status; //-1锁定,0-未完成,1-已完成

    private Integer type; // 第一个练习的类型
    private Integer practicePlanId; //最近一个解锁练习的id
    private String practiceId; //最近一个解锁练习的练习id


    private Integer totalPractices; // 该小节总共多少道练习
    private Integer completePractices; // 该小节已经完成的练习数
}
