package com.iquanwai.platon.biz.domain.common.message;

import lombok.Data;

import java.util.Map;

/**
 * Created by nethunder on 2017/6/15.
 * 短信信息，发送给单个用户
 */
@Data
public class SMSDto {
    private Integer profileId;
    private String phone;
    private String content;
    private Map<String,String> replace;
}
