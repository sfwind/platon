package com.iquanwai.platon.biz.po.common;

import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * Created by justin on 16/8/13.
 */
@Data
@Alias("callback")
public class Callback {

    private Integer id;
    private String state;
    private String callbackUrl;
    private String accessToken;
    private String pcAccessToken;
    private String refreshToken;
    private String unionId;
    private String openId;
    private String pcOpenId;
    private String weMiniOpenId;

}
