package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.domain.common.flow.LandingPageBanner;
import com.iquanwai.platon.biz.po.flow.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by 三十文
 */
@Data
@ApiModel("首页对象")
public class LandingPageDto {

    @ApiModelProperty("是否是商学院会员")
    private Boolean isBusinessMember;
    @ApiModelProperty("个人中心通知图标是否显示红点")
    private Boolean notify;
    @ApiModelProperty("首页上面的 Banner 配置")
    private List<LandingPageBanner> pageBanners;
    @ApiModelProperty("课程数据")
    private List<ProblemsFlow> problemsFlows;
    @ApiModelProperty("项目数据")
    private List<ProgramsFlow> programsFlows;
    @ApiModelProperty("直播数据")
    private List<LivesFlow> livesFlows;
    @ApiModelProperty("文章数据")
    private List<ArticlesFlow> articlesFlows;
    @ApiModelProperty("活动数据")
    private List<ActivitiesFlow> activitiesFlows;
    @ApiModelProperty("申请通过记录")
    private ApplySuccessDto applySuccess;
}
