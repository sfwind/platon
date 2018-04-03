package com.iquanwai.platon.web.daily;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("每日圈语信息")
public class DailyTalkDto {

    @ApiModelProperty("头像链接")
    private String headImg;
    @ApiModelProperty("昵称")
    private String nickName;
    @ApiModelProperty("学习天数")
    private Integer learnedDay;
    @ApiModelProperty("学习知识点个数")
    private Integer learnedKnowledge;
    @ApiModelProperty("打败百分比")
    private Integer percent;
    @ApiModelProperty("圈语图片")
    private String imgUrl;
    @ApiModelProperty("圈语内容")
    private String content;
    @ApiModelProperty("作者")
    private String author;
    @ApiModelProperty("标题")
    private String title;
}
