package com.iquanwai.platon.biz.domain.fragmentation.practice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by 三十文
 */
@Data
@ApiModel("讨论区基础数据对象")
public class DiscussElement {

    @ApiModelProperty("评论id")
    private Integer id;
    @ApiModelProperty("昵称")
    private String nickname;
    @ApiModelProperty("头像")
    private String avatar;
    @ApiModelProperty("发布时间")
    private Date publishTime;
    @ApiModelProperty("评论内容")
    private String content;
    @ApiModelProperty("是否助教")
    private Boolean isAsst;
    @ApiModelProperty("自身是否点赞")
    private Boolean selfVoted;
    @ApiModelProperty("点赞总人数")
    private Integer voteCount;
    @ApiModelProperty("头衔图片链接")
    private String titleImageUrl;

}
