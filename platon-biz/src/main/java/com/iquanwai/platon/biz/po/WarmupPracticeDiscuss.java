package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 17/2/8.
 */
@Data
@ApiModel("选择题讨论")
public class WarmupPracticeDiscuss extends AbstractComment {
    @ApiModelProperty("选择题id")
    private Integer warmupPracticeId;
    @ApiModelProperty("讨论最早发起的评论id")
    private Integer originDiscussId;
}
