package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by xfduan on 2017/8/2.
 */
@Data
@ApiModel("评价教练点评model")
public class CommentEvaluation {

    // db 字段
    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("作业id")
    private Integer submitId;
    @ApiModelProperty("点评id")
    private Integer commentId;
    @ApiModelProperty("是否有用")
    private Integer useful;
    @ApiModelProperty("评价的依据")
    private String reason;
    @ApiModelProperty("是否已评价")
    private Integer evaluated;

    // 非 db 字段
    @ApiModelProperty("该条评论记录的评论人昵称")
    private String nickName;

}
