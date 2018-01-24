package com.iquanwai.platon.web.fragmentation.controller.operation.dto;

import lombok.Data;

@Data
public class SurveyResultDto {
    private Boolean subscribe;
    private String subscribeQrCode;
    private Integer resultId;
}
