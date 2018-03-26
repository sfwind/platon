package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.domain.common.flow.LandingPageBanner;
import com.iquanwai.platon.biz.po.ActivitiesFlow;
import com.iquanwai.platon.biz.po.ArticlesFlow;
import com.iquanwai.platon.biz.po.LivesFlow;
import com.iquanwai.platon.biz.po.ProblemsFlow;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by 三十文
 */
@Data
public class LandingPageDto {

    @ApiModelProperty("是否是商学院会员")
    private Boolean isBusinessMember;
    @ApiModelProperty("个人中心通知图标是否显示红点")
    private Boolean notify;
    @ApiModelProperty("是否显示商学院申请通过通知")
    private Boolean isShowPassNotify;
    @ApiModelProperty("申请有效期剩余时间")
    private Long remainTime;
    @ApiModelProperty("首页上面的 Banner 配置")
    private List<LandingPageBanner> pageBanners;
    @ApiModelProperty("课程数据")
    private List<ProblemsFlow> problemsFlows;
    @ApiModelProperty("直播数据")
    private List<LivesFlow> livesFlows;
    @ApiModelProperty("文章数据")
    private List<ArticlesFlow> articlesFlows;
    @ApiModelProperty("活动数据")
    private List<ActivitiesFlow> activitiesFlows;

}
