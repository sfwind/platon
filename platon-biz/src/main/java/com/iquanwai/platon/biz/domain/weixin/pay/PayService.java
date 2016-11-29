package com.iquanwai.platon.biz.domain.weixin.pay;

/**
 * Created by justin on 16/9/14.
 */
public interface PayService {
    /**
     * 调用微信统一下单接口
     * @return 返回PrepayId
     * */
    String unifiedOrder(String orderId);
    /**
     * 生成微信支付回调返回
     * */
    OrderCallbackReply callbackReply(String result, String errMsg, String prepayId);
    /**
     * 处理支付结果
     * */
    void handlePayResult(PayCallback payCallback);

    /**
     * 定期关闭过期订单
     * */
    void closeOrder();


    String UNIFIED_ORDER_URL ="https://api.mch.weixin.qq.com/pay/unifiedorder";

    String CLOSE_ORDER_URL ="https://api.mch.weixin.qq.com/pay/closeorder";

    String GOODS_BODY = "圈外-线上课程";

    String ERROR_CODE = "FAIL";
    String SUCCESS_CODE = "SUCCESS";

    String DUP_PAID = "ORDERPAID";
    String ORDER_CLOSE = "ORDERCLOSED";
    String SYSTEM_ERROR = "SYSTEMERROR";
}
