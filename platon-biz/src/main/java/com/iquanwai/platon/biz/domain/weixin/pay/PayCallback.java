package com.iquanwai.platon.biz.domain.weixin.pay;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by justin on 16/9/7.
 */
@Data
@XmlRootElement(name="xml")
public class PayCallback {
    private String appid; //公众账号ID
    private String attach; //商家数据包
    private String bank_type; //付款银行
    private String fee_type; //货币种类
    private String is_subscribe; //是否关注公众账号
    private String mch_id; //商户号
    private String device_info; //设备号
    private String nonce_str; //随机字符串
    private String openid; //用户标识
    private String out_trade_no; //商户订单号
    private String result_code; //业务结果
    private String return_code; //返回状态码
    private String return_msg; //返回信息
    private String sign; //签名
    private String time_end; //支付完成时间
    private Integer total_fee; //订单金额
    private String trade_type; //交易类型
    private String transaction_id; //微信支付订单号
    private String err_code; //错误代码
    private String err_code_des; //错误代码描述
}

