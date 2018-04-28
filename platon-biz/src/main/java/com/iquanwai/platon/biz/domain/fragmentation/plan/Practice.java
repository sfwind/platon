package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/12/11.
 */
@Data
@ApiModel("练习")
public class Practice {
    @ApiModelProperty("练习类型")
    private Integer type;
    @ApiModelProperty("练习完成状态")
    private Integer status;
    @ApiModelProperty("是否解锁")
    private Boolean unlocked;
    @ApiModelProperty("练习id列表")
    private List<Integer> practiceIdList = Lists.newArrayList();
    @ApiModelProperty("练习小节")
    private Integer series;
    @ApiModelProperty("练习顺序")
    private Integer sequence;
    @ApiModelProperty("练习id列表")
    private Integer practicePlanId;
    @ApiModelProperty("是否选做题")
    private Boolean optional;
    @ApiModelProperty("课程训练id")
    private Integer planId;
}
