package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by 三十文
 */
@Data
public class ProblemsFlow {

    private Integer id;
    private String abbreviation;
    private String name;
    private Boolean hot;
    private String thumbnail;
    private Boolean del;

}
