package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
@ApiModel("应用题")
public class  ApplicationPractice {
    private int id;
    @ApiModelProperty("任务标题")
    private String topic;
    @ApiModelProperty("题干")
    private String description;
    @ApiModelProperty("知识点id")
    private Integer knowledgeId;
    @ApiModelProperty("难易度（1-容易，2-普通，3-困难）")
    private Integer difficulty;
    @ApiModelProperty("课程id")
    private Integer problemId;
    @ApiModelProperty("出现顺序")
    private Integer sequence;
    @ApiModelProperty("是否删除")
    private Boolean del;
    @ApiModelProperty("任务唯一编号")
    private String practiceUid;
    @ApiModelProperty("题目图片")
    private String pic;
    @ApiModelProperty("题目类型(1-应用题,2-案例题)")
    private Integer type;
    @ApiModelProperty("应用题名字")
    private String name;
    @ApiModelProperty("提交内容")
    private String content;
    @ApiModelProperty("提交id")
    private Integer submitId;
    @ApiModelProperty("提交更新时间")
    private String submitUpdateTime;
    @ApiModelProperty("点赞数")
    private Integer voteCount;
    @ApiModelProperty("评论数")
    private Integer commentCount;
    @ApiModelProperty("应用题得分")
    private Integer applicationScore;
    @ApiModelProperty("点赞状态")
    private Integer voteStatus;
    @ApiModelProperty("计划id")
    private Integer planId;
    @ApiModelProperty("求点赞")
    private Integer requestCommentCount;
    @ApiModelProperty("是否已经求点评")
    private Boolean request;
    @ApiModelProperty("是否已经被教练点评")
    private Boolean feedback;
    @ApiModelProperty("草稿内容")
    private String draft;
    @ApiModelProperty("草稿 id")
    private Integer draftId;
    @ApiModelProperty("是否覆盖本地 localStorage")
    private Boolean overrideLocalStorage;
    @ApiModelProperty("是否是最后一题")
    private Boolean isLastApplication;
    @ApiModelProperty("草稿内容和提交内容是否相等")
    private Boolean isSynchronized;

}
