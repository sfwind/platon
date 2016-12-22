package com.iquanwai.platon.biz.domain.weixin.signature;

import lombok.Data;

/**
 * Created by yangyuchen on 15-1-30.
 */
@Data
public class JsSignature {
    private String appId;
    private String timestamp;
    private String nonceStr;
    private String signature;

    public JsSignature(String appId, String timestamp, String nonceStr, String signature) {
        this.appId = appId;
        this.timestamp = timestamp;
        this.nonceStr = nonceStr;
        this.signature = signature;
    }

}
