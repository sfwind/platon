package com.iquanwai.platon.biz.domain.common.message;

import lombok.Data;

/**
 * Created by justin on 17/6/30.
 */
@Data
public class MessageDto {
    private String msgid;
    private String result;
    private String desc;
    private String failPhones;
    private String status;
}
