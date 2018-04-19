package com.iquanwai.platon.web.personal.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CustomerInfoDto {
    private String riseId;
    private String headimgurl;
    private String nickname;
    private Boolean isAsst;
    private List<String> roleNames;
    private Map<String, String> classGroupMaps;
}
