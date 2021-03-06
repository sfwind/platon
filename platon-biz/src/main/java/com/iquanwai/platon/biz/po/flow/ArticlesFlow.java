package com.iquanwai.platon.biz.po.flow;

import com.iquanwai.platon.biz.po.FlowData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by 三十文
 */
@Data
@ApiModel("文章流")
public class ArticlesFlow extends FlowData {

    private Integer id;
    @ApiModelProperty("文章标题")
    private String title;
    @ApiModelProperty("文章描述")
    private String description;
    @ApiModelProperty("缩略图地址")
    private String thumbnail;
    @ApiModelProperty("链接地址")
    private String linkUrl;
    @ApiModelProperty("文章链接参数")
    private String linkParam;
    @ApiModelProperty("标签")
    private String tag;
    @ApiModelProperty("标签列表")
    private List<String> tags;

}
