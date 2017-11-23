package com.iquanwai.platon.web.fragmentation.dto.plan;

import lombok.Data;

@Data
public class AuditionChooseDto {
    private Integer planId;
    private Boolean goSuccess;
    private String className;
    private String errMsg;
    private String startTime;
    private String endTime;
    private Boolean subscribe;
}
