package com.iquanwai.platon.web.fragmentation.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by nethunder on 2017/1/2.
 */
@Data
@ApiModel("点赞model")
public class HomeworkVoteDto {
    @ApiModelProperty("点赞的文章类型")
    private Integer type;
    @ApiModelProperty("文章id")
    private Integer referencedId;
    @ApiModelProperty("状态 1 点赞 2 取消")
    private Integer status; // 1 点赞 2 取消
}
