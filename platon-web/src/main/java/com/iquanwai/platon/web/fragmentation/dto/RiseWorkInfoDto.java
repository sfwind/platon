package com.iquanwai.platon.web.fragmentation.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/1/14.
 */
@Data
@ApiModel("文章")
public class RiseWorkInfoDto {
    @ApiModelProperty("文章标题")
    private String title;
    @ApiModelProperty("作者名称")
    private String userName; 
    @ApiModelProperty("提交更新时间")
    private String submitUpdateTime; 
    @ApiModelProperty("作者头像")
    private String headImage; 
    @ApiModelProperty("文章内容")
    private String content;  
    @ApiModelProperty("赞数")
    private Integer voteCount;   
    @ApiModelProperty("评论数")
    private Integer commentCount; 
    @ApiModelProperty("提交id")
    private Integer submitId; 
    @ApiModelProperty("类型（1-小目标,2-应用练习,3-小课分享）")
    private Integer type; 
    @ApiModelProperty("赞状态")
    private Integer voteStatus; 
    @ApiModelProperty("发布时间")
    private Date publishTime; 
    @ApiModelProperty("文章描述")
    private String desc; 
    @ApiModelProperty("排序优先级")
    private Integer priority; 
    @ApiModelProperty("是否是精华")
    private Boolean perfect; 
    @ApiModelProperty("课程id")
    private Integer problemId; 
    @ApiModelProperty("作者类型")
    private Integer authorType; 
    @ApiModelProperty("是否是本人文章")
    private Boolean isMine; 
    @ApiModelProperty("作者角色")
    private Integer role; 
    @ApiModelProperty("作者签名")
    private String signature; 
    @ApiModelProperty("求点评次数")
    private Integer requestCommentCount; 
    @ApiModelProperty("是否已经求点评")
    private Boolean request; 
    @ApiModelProperty("是否已经点评")
    private Boolean feedback;
}
