package com.iquanwai.platon.web.personal.dto;

import lombok.Data;

import java.util.List;

@Data
public class CustomerInfoDto {
    private String riseId;
    private String headimgurl;
    private String nickname;
    private Integer roleName;
    private String className;
    private String groupId;
    private Boolean isAsst;
    private List<String> roleNames;
}
