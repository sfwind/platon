package com.iquanwai.platon.web.fragmentation.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/4/14.
 */
@Data
public class OpenStatusDto {
    private Boolean openRise; // 非db字段 是否打开过RISE
    private Boolean openApplication; // 非db字段 是否打开过应用练习
    private Boolean openConsolidation; // 非db字段 是否打开过巩固练习
    private Boolean openNavigator; // 非db字段 是否打开过课程列表页
}
