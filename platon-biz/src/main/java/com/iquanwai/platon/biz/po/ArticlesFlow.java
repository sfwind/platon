package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by 三十文
 */
@Data
@ApiModel("文章流")
public class ArticlesFlow {

    private Integer id;
    @ApiModelProperty("文章标题")
    private String title;
    @ApiModelProperty("文章描述")
    private String description;
    @ApiModelProperty("文章内容")
    private String content;
    @ApiModelProperty("缩略图地址")
    private String thumbnail;
    @ApiModelProperty("文章链接参数")
    private String linkParam;

}
