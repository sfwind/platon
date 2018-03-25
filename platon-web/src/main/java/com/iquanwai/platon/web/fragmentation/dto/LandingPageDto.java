package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.domain.common.flow.LandingPageBanner;
import com.iquanwai.platon.biz.po.ActivitiesFlow;
import com.iquanwai.platon.biz.po.ArticlesFlow;
import com.iquanwai.platon.biz.po.LivesFlow;
import com.iquanwai.platon.biz.po.ProblemsFlow;
import lombok.Data;

import java.util.List;

/**
 * Created by 三十文
 */
@Data
public class LandingPageDto {

    private Boolean isBusinessMember;
    private Boolean notify;
    private List<LandingPageBanner> pageBanners;
    private List<ProblemsFlow> problemsFlows;
    private List<LivesFlow> livesFlows;
    private List<ArticlesFlow> articlesFlows;
    private List<ActivitiesFlow> activitiesFlows;

}
