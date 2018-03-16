package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by 三十文
 */
@ApiModel("富文本对象")
@Data
public class RichText {

    private Integer id;
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("文本内容")
    private String content;
    private Boolean del;

}
