package com.iquanwai.platon.biz.domain.weixin.pay;

import lombok.Data;

/**
 * Created by justin on 16/9/7.
 */
@Data
public class GoodsDetail {
    private String goods_id; // 必填 32 商品的编号
//    private String wxpay_goods_id; // 可选 32 微信支付定义的统一商品编号
    private String goods_name; // 必填 256 商品名称
    private Integer goods_num; // 必填 商品数量
    private Integer price; // 必填 商品单价，单位为分
//    private String goods_category; // 可选 32 商品类目ID
//    private String body; // 可选 1000 商品描述信息
}
