package com.iquanwai.platon.biz.domain.fragmentation.practice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by 三十文
 */
@Data
@ApiModel("个人评论")
public class PersonalDiscuss {

    @ApiModelProperty("用户本人评论内容")
    private DiscussElement discuss;
    @ApiModelProperty("针对本人评论的评论集合")
    private List<DiscussElement> comments;

}
