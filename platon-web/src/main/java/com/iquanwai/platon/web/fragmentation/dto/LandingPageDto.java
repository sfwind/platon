package com.iquanwai.platon.web.fragmentation.dto;

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

    List<ProblemsFlow> problemsFlows;
    List<LivesFlow> livesFlows;
    List<ArticlesFlow> articlesFlows;
    List<ActivitiesFlow> activitiesFlows;

}
