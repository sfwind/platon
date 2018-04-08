package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.WarmupPractice;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("选择题数据")
public class WarmupPracticeDto {

    @ApiModelProperty("选择题页面基本数据")
    private List<WarmupPractice> practice;

}
