package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.Problem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by nethunder on 2017/7/18.
 */
@Data
@ApiModel("课程数据")
public class RiseCourseDto {
    @ApiModelProperty("课程")
    private Problem problem;
    @ApiModelProperty("按钮状态")
    private Integer buttonStatus;

}
