package com.iquanwai.platon.web.personalCenter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by nethunder on 2017/2/4.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AreaDto {
    private String id;
    private String value;
    private String parentId;
}
