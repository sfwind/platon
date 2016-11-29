package com.iquanwai.platon.biz.domain.weixin.pay;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by justin on 16/9/14.
 */
@XmlRootElement(name="xml")
@Data
public class PayCloseReply {
    private String return_code; //返回状态码
    private String return_msg; //返回信息
    private String appid; //公众账号ID
    private String mch_id; //商户号
    private String nonce_str; //随机字符串
    private String sign; //签名
    private String result_code; //业务结果
    private String err_code; //错误代码
    private String err_code_des; //错误代码描述

}
