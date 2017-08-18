package com.iquanwai.platon.web.fragmentation.dto;

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
}
