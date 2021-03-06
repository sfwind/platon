package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.Problem;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/2/24.
 */
@Data
public class ProblemCatalogListDto {
    private Integer catalogId;
    private String name;
    private String description;
    private Integer sequence;
    private String pic;
    private String color;
    private List<Problem> problemList;
}
