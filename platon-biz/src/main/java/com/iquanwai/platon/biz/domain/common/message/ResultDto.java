package com.iquanwai.platon.biz.domain.common.message;

import lombok.Data;

/**
 * Created by justin on 17/6/28.
 */
@Data
public class ResultDto {
    private MessageDto msg;
    private int code;
}
