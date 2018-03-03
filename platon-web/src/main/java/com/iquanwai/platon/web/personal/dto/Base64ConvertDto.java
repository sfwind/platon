package com.iquanwai.platon.web.personal.dto;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("base64转换类")
public class Base64ConvertDto {
    @ApiModelProperty("类型")
    private Integer type;
    @ApiModelProperty("base64编码")
    private String base64Str;
    @ApiModelProperty("图片名称")
    private String imageName;

}
