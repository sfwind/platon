package com.iquanwai.platon.web.fragmentation.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/4/14.
 */
@Data
public class OpenStatusDto {
    private Boolean openRise;
    private Boolean openComprehension; // 非db字段 是否打开过应用练习
    private Boolean openConsolidation; // 非db字段 是否打开过巩固练习
}
