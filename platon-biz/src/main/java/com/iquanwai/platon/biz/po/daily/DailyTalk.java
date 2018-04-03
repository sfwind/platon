package com.iquanwai.platon.biz.po.daily;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("每日圈语")
public class DailyTalk {

    @ApiModelProperty("圈语图片链接")
    private String imgUrl;
    @ApiModelProperty("圈语作者")
    private String author;
    @ApiModelProperty("圈语内容")
    private String content;
    @ApiModelProperty("显示日期")
    private Date showDate;

}
