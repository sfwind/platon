package com.iquanwai.platon.biz.domain.weixin.pay;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by justin on 16/9/14.
 */
@XmlRootElement(name="xml")
@Data
public class PayClose {
    private String appid; //公众账号ID
    private String mch_id; //商户号
    private String out_trade_no; //商户订单号
    private String nonce_str; //随机字符串
    private String sign; //签名
}
