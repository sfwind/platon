package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.domain.fragmentation.plan.ExploreBanner;
import com.iquanwai.platon.biz.po.Problem;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/2/24.
 */
@Data
public class ProblemCatalogDto {
    private String name;
    private List<ProblemCatalogListDto> catalogList;
    private List<Problem> hotList;
    private Boolean riseMember;

    // 发现页面 banner 配置
    private List<ExploreBanner> banners;
}
