package com.iquanwai.platon.biz.domain.weixin.pay;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by justin on 16/9/14.
 */
@XmlRootElement(name="xml")
@Data
public class OrderCallbackReply {
    private String return_code; //返回状态码
    private String appid; //公众账号ID
    private String mch_id; //商户号
    private String nonce_str; //随机字符串
    private String prepay_id; //预支付ID
    private String result_code; //业务结果
    private String err_code_des; //错误描述
    private String sign; //签名
}
