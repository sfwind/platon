package com.iquanwai.platon.biz.domain.fragmentation.manager;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.fragmentation.plan.Practice;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 17/4/12.
 */
@Data
@ApiModel("小节")
public class Section {
    @ApiModelProperty("第几小节")
    private Integer section;  
    @ApiModelProperty("知识点")
    private Integer knowledgeId; 
    @ApiModelProperty("小节名称")
    private String name; 
    @ApiModelProperty("序号")
    private Integer series; 
    @ApiModelProperty("第几章")
    private Integer chapter; 
    @ApiModelProperty("是否是综合练习")
    private Boolean integrated; 
    @ApiModelProperty("章节名")
    private String chapterName; 
    @ApiModelProperty("练习")
    private List<Practice> practices = Lists.newArrayList(); 
    @ApiModelProperty("-1-锁定,0-未完成,1-已完成")
    private Integer status; 
    @ApiModelProperty("第一个练习的类型")
    private Integer type;  
    @ApiModelProperty("最近一个解锁练习的id")
    private Integer practicePlanId; 
    @ApiModelProperty("最近一个解锁练习的练习id")
    private String practiceId; 

    @ApiModelProperty("该小节总共多少道练习")
    private Integer totalPractices;  
    @ApiModelProperty("该小节已经完成的练习数")
    private Integer completePractices;  
}
