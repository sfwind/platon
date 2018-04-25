package com.iquanwai.platon.biz.domain.common.flow;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by 三十文
 */
@Data
@ApiModel("首页banner")
public class LandingPageBanner {
    @ApiModelProperty("banner图片链接")
    private String imageUrl;
    @ApiModelProperty("banner跳转链接")
    private String linkUrl;

}
