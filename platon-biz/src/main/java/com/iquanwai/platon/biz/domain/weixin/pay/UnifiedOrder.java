package com.iquanwai.platon.biz.domain.weixin.pay;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by justin on 16/9/6.
 */
@XmlRootElement(name="xml")
@Data
public class UnifiedOrder {
    private String appid; //公众账号ID
    private String mch_id; //商户号
    private String device_info; //设备号
    private String nonce_str; //随机字符串
    private String sign; //签名
    private String body; //商品描述
    private String detail; //商品详情
    private String attach; //附加数据
    private String out_trade_no; //商户订单号
//    private String fee_type = "CNY";
    private Integer total_fee; //总金额
    private String spbill_create_ip; //终端IP
    private String time_start; //交易起始时间
    private String time_expire; //交易结束时间
    private String goods_tag; //商品标记
    private String notify_url; //通知地址
    private String trade_type; //交易类型
    private String product_id; //商品ID
//    private String limit_pay; //指定支付方式
    private String openid; //用户标识
}

