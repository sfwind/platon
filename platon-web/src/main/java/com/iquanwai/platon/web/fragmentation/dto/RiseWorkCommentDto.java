package com.iquanwai.platon.web.fragmentation.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by nethunder on 2017/1/20.
 */
@Data
@ApiModel("作业评论")
public class RiseWorkCommentDto {
    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("评论内容")
    private String comment;
    @ApiModelProperty("评论者昵称")
    private String name;
    @ApiModelProperty("评论时间")
    private String discussTime;
    @ApiModelProperty("评论者头像链接")
    private String avatar;
    @ApiModelProperty("评论者签名")
    private String signature;
    @ApiModelProperty("评论者角色id")
    private Integer role;
    @ApiModelProperty("是否是浏览文章用户的评论")
    private Boolean isMine;
    @ApiModelProperty("被回复的id")
    private Integer repliedId;
    @ApiModelProperty("被回复的评论内容")
    private String repliedComment;
    @ApiModelProperty("被回复人的名字")
    private String repliedName;
    @ApiModelProperty("被回复人评论是否删除")
    private Integer repliedDel;

}
