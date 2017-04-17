package com.iquanwai.platon.web.fragmentation.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/4/14.
 */
@Data
public class OpenStatusDto {
    private Boolean openRise;
    private Boolean openApplication; // 非db字段 是否打开过应用训练
    private Boolean openConsolidation; // 非db字段 是否打开过巩固训练
}
