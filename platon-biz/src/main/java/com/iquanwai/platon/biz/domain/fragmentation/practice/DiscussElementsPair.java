package com.iquanwai.platon.biz.domain.fragmentation.practice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by 三十文
 */
@Data
@ApiModel("精华讨论区")
public class DiscussElementsPair {

    @ApiModelProperty("精华内容针对的评论")
    private DiscussElement originDiscuss;
    @ApiModelProperty("精华评论")
    private DiscussElement priorityDiscuss;
    @ApiModelProperty("多条评论（针对应用题回复）")
    private List<DiscussElement> multiComments;

}
