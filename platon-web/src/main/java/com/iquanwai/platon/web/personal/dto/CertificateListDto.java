package com.iquanwai.platon.web.personal.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("优秀证书和完课证书")
public class CertificateListDto {
    @ApiModelProperty("优秀证书")
    private List<CertificateDto> gradeDto;
    @ApiModelProperty("完课证书")
    private List<CertificateDto> finishDto;
}
