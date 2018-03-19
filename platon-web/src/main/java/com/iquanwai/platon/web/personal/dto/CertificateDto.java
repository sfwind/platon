package com.iquanwai.platon.web.personal.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("证书信息")
public class CertificateDto {

    @ApiModelProperty("月份")
    private Integer month;
    @ApiModelProperty("证书类型")
    private String typeName;
    @ApiModelProperty("课程缩略图")
    private String abbreviation;
    @ApiModelProperty("课程名字")
    private String problemName;
    @ApiModelProperty("证书编号")
    private String certificateNo;
}
