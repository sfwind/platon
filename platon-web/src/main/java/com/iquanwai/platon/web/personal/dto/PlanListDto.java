package com.iquanwai.platon.web.personal.dto;

import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.RiseCertificate;
import com.iquanwai.platon.biz.po.common.MemberType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/2/7.
 */
@Data
@ApiModel("课程列表")
public class PlanListDto {
    @ApiModelProperty("学员riseid")
    private String riseId;
    @ApiModelProperty("进行中的课程")
    private List<PlanDto> runningPlans;
    @ApiModelProperty("已完成的课程")
    private List<PlanDto> donePlans;
    @ApiModelProperty("收藏列表")
    private List<Problem> problemCollections;
    @ApiModelProperty("证书列表")
    private List<RiseCertificate> riseCertificates;
    @ApiModelProperty("证书列表")
    private MemberType memberType;
    @ApiModelProperty("分数")
    private Integer point;
}
