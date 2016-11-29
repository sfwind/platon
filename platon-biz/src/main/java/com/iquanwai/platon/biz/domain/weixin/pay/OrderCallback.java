package com.iquanwai.platon.biz.domain.weixin.pay;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by justin on 16/9/14.
 */
@XmlRootElement(name="xml")
@Data
public class OrderCallback {
    private String appid; //公众账号ID
    private String openid; //用户标识
    private String mch_id; //商户号
    private String is_subscribe; //是否关注公众账号
    private String nonce_str; //随机字符串
    private String product_id; //商品ID
    private String sign; //签名


}
