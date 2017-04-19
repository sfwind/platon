package com.iquanwai.platon.web.fragmentation.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/2/24.
 */
@Data
public class ProblemCatalogDto {
    private String name;
    private List<ProblemCatalogListDto> catalogList;
    private Boolean riseMember;
}
