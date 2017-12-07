package com.iquanwai.platon.biz.domain.fragmentation.plan.manager;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 17/4/5.
 */
@Data
public class Chapter {
    private Integer chapter; //章数
    private List<Section> sections = Lists.newArrayList(); //小节
    private String name; //章节名称
    private boolean isIntegrated; //是否是综合练习

    //非DB字段
    private Integer myWarmScore;
    private Integer totalWarmScore;
}
