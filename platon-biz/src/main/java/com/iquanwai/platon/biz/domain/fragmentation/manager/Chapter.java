package com.iquanwai.platon.biz.domain.fragmentation.manager;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 17/4/5.
 */
@Data
@ApiModel("章节")
public class Chapter {
    @ApiModelProperty("章数")
    private Integer chapter; 
    @ApiModelProperty("小节")
    private List<Section> sections = Lists.newArrayList(); 
    @ApiModelProperty("章节名称")
    private String name;
    @ApiModelProperty("我的选择题得分")
    private Integer myWarmScore;
    @ApiModelProperty("选择题总分")
    private Integer totalWarmScore;
}
