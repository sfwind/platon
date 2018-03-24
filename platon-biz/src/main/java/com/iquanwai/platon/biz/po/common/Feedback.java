package com.iquanwai.platon.biz.po.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 2018/3/15.
 */
@Data
@ApiModel("意见反馈model")
public class Feedback {
    @ApiModelProperty("意见反馈内容")
    private String words;
    @ApiModelProperty("联系方式")
    private String contact;
    @ApiModelProperty("图片id列表,用逗号分割")
    private String picIds;
    @ApiModelProperty("用户id")
    private Integer profileId;
}
