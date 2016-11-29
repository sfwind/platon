package com.iquanwai.platon.biz.po;

import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * Created by justin on 16/8/13.
 */
@Data
@Alias("callback")
public class Callback {
    private String callbackUrl;
    private String state;
    private String accessToken;
    private String refreshToken;
    private String openid;
}
