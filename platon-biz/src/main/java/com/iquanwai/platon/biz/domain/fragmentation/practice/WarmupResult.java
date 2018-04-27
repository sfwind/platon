package com.iquanwai.platon.biz.domain.fragmentation.practice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 16/12/11.
 */
@Data
@ApiModel("选择题答题结果")
public class WarmupResult {
    @ApiModelProperty("得分")
    private Integer point;
    @ApiModelProperty("正确题数")
    private Integer rightNumber;
    @ApiModelProperty("总题数")
    private Integer total;
}
