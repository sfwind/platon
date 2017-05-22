package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by nethunder on 2017/2/24.
 */
@Data
public class ProblemCatalog {
    private Integer id;
    private String name;
    private String description;
    private Integer sequence;
    private String pic;
    private String color;
    private Boolean del;
}
