package com.iquanwai.platon.web.personal.dto;

import lombok.Data;

@Data
public class CustomerInfoDto {
    private String riseId;
    private String headimgurl;
    private String nickname;
    private Integer roleName;
    private String className;
    private String groupId;
    private Boolean isAsst;
}
