package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 2018/4/11.
 */
@Data
@ApiModel("课前预习")
public class ProblemPreview {
    private int id;
    @ApiModelProperty("内容")
    private String description; 
    @ApiModelProperty("章节id")
    private Integer problemScheduleId; 
    @ApiModelProperty("语音id")
    private Integer audioId;  
    @ApiModelProperty("视频id")
    private Integer videoId; 
    @ApiModelProperty("视频url")
    private String videoUrl; 
    @ApiModelProperty("视频第一帧")
    private String videoPoster;  
    @ApiModelProperty("语音文字")
    private String audioWords;  
    @ApiModelProperty("语音链接")
    private String audio;  
}
