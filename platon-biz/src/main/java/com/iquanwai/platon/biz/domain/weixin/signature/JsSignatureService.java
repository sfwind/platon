package com.iquanwai.platon.biz.domain.weixin.signature;

/**
 * Created by yangyuchen on 15-1-30.
 */
public interface JsSignatureService {
    JsSignature getJsSignature(String url, boolean refresh);

    String JS_API_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={access_token}&type=jsapi";
}
