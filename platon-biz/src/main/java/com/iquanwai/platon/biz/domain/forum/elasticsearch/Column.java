package com.iquanwai.platon.biz.domain.forum.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by nethunder on 2017/6/29.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Column {
    private String name;
    private String type;
    private Integer dataScale;
}
