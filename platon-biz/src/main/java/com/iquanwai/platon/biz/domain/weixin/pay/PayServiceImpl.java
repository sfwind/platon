package com.iquanwai.platon.biz.domain.weixin.pay;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.iquanwai.platon.biz.dao.wx.CourseOrderDao;
import com.iquanwai.platon.biz.po.CourseOrder;
import com.iquanwai.platon.biz.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/9/14.
 */
@Service
public class PayServiceImpl implements PayService{
    @Autowired
    private CourseOrderDao courseOrderDao;

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private RestfulHelper restfulHelper;

    private static final String WEIXIN = "NATIVE";

    private static final String PAY_CALLBACK_PATH = "/wx/pay/result/callback";

    public String unifiedOrder(String orderId) {
        Assert.notNull(orderId, "订单号不能为空");
        CourseOrder courseOrder = courseOrderDao.loadOrder(orderId);
        if(courseOrder==null){
            logger.error("order id {} not existed", orderId);
            return "";
        }

        UnifiedOrder unifiedOrder = buildOrder(courseOrder);

        String response = restfulHelper.postXML(UNIFIED_ORDER_URL, XMLHelper.createXML(unifiedOrder));
        UnifiedOrderReply reply = XMLHelper.parseXml(UnifiedOrderReply.class, response);
        if(reply!=null){
            String prepay_id = reply.getPrepay_id();
            if(prepay_id!=null){
                courseOrderDao.updatePrepayId(prepay_id, orderId);
                return prepay_id;
            }
            if(reply.getErr_code_des()!=null){
                logger.error("response is------\n"+response);
                logger.error(reply.getErr_code_des()+", orderId="+orderId);
                if(!ignoreCode(reply.getErr_code())) {
                    courseOrderDao.payError(reply.getErr_code_des(), orderId);
                }
            }

        }
        return "";
    }

    private boolean ignoreCode(String err_code) {
        return SYSTEM_ERROR.equals(err_code)||DUP_PAID.equals(err_code)||ORDER_CLOSE.equals(err_code);
    }

    public OrderCallbackReply callbackReply(String result, String errMsg, String prepayId) {
        Assert.notNull(result, "支付结果不能为空");
        Assert.notNull(errMsg, "描述不能为空");
        OrderCallbackReply orderCallbackReply = new OrderCallbackReply();

        Map<String, String> map = Maps.newHashMap();
        map.put("result_code", result);
        map.put("err_code_des", errMsg);
        map.put("prepay_id", prepayId);
        String return_code="SUCCESS";
        map.put("return_code", return_code);
        String appid = ConfigUtils.getAppid();
        map.put("appid", appid);
        String mch_id = ConfigUtils.getMch_id();
        map.put("mch_id", mch_id);
        String nonce_str = CommonUtils.randomString(16);
        map.put("nonce_str", nonce_str);

        String sign = CommonUtils.sign(map);
        orderCallbackReply.setSign(sign);
        orderCallbackReply.setAppid(appid);
        orderCallbackReply.setErr_code_des(errMsg);
        orderCallbackReply.setMch_id(mch_id);
        orderCallbackReply.setNonce_str(nonce_str);
        orderCallbackReply.setPrepay_id(prepayId);
        orderCallbackReply.setResult_code(result);
        orderCallbackReply.setReturn_code(return_code);

        return orderCallbackReply;
    }

    public void handlePayResult(PayCallback payCallback) {
        Assert.notNull(payCallback, "支付结果不能为空");
        String orderId = payCallback.getOut_trade_no();
        if(payCallback.getErr_code_des()!=null){
            logger.error(payCallback.getErr_code_des()+", orderId="+orderId);
            if(!ignoreCode(payCallback.getErr_code())) {
                courseOrderDao.payError(payCallback.getErr_code_des(), orderId);
            }
            return;
        }

        String transactionId = payCallback.getTransaction_id();
        String paidTimeStr = payCallback.getTime_end();
        Date paidTime = DateUtils.parseStringToDate3(paidTimeStr);
        courseOrderDao.paySuccess(paidTime, transactionId, orderId);
    }

    public void closeOrder() {
        //点开付费的保留30分钟
        Date date = DateUtils.afterMinutes(new Date(), 0-ConfigUtils.getBillOpenMinute());
        //临时的只保留3分钟
        Date date2 = DateUtils.afterMinutes(new Date(), -3);
        List<CourseOrder> underCloseOrders = courseOrderDao.queryUnderCloseOrders(date);
        List<CourseOrder> underCloseOrdersRecent = courseOrderDao.queryUnderCloseOrders(date2);
        //点报名未扫描二维码的直接close
        for(CourseOrder courseOrder:underCloseOrdersRecent){
            if(courseOrder.getPrepayId()==null){
                underCloseOrders.add(courseOrder);
            }
        }
        for(CourseOrder courseOrder:underCloseOrders){
            String orderId = courseOrder.getOrderId();
            PayClose payClose = buildPayClose(orderId);
            try {
                String response = restfulHelper.postXML(CLOSE_ORDER_URL, XMLHelper.createXML(payClose));
                PayCloseReply payCloseReply = XMLHelper.parseXml(PayCloseReply.class, response);
                if(payCloseReply!=null){
                    if(SUCCESS_CODE.equals(payCloseReply.getReturn_code())) {
                        if (ERROR_CODE.equals(payCloseReply.getErr_code()) && payCloseReply.getErr_code_des()!=null){
                            logger.error(payCloseReply.getErr_code_des()+", orderId="+orderId);
                        }
                        logger.info("orderId: {} closed automatically", orderId);
                    }
                }
            }catch (Exception e){
                logger.error("orderId: {} close failed", orderId);
            }
        }
    }

    private PayClose buildPayClose(String orderId) {
        PayClose payClose = new PayClose();
        Map<String, String> map = Maps.newHashMap();
        map.put("out_trade_no", orderId);
        String appid = ConfigUtils.getAppid();
        map.put("appid", appid);
        String mch_id = ConfigUtils.getMch_id();
        map.put("mch_id", mch_id);
        String nonce_str = CommonUtils.randomString(16);
        map.put("nonce_str", nonce_str);
        String sign = CommonUtils.sign(map);

        payClose.setNonce_str(nonce_str);
        payClose.setMch_id(mch_id);
        payClose.setAppid(appid);
        payClose.setOut_trade_no(orderId);
        payClose.setSign(sign);

        return payClose;
    }


    private UnifiedOrder buildOrder(CourseOrder courseOrder){
        UnifiedOrder unifiedOrder = new UnifiedOrder();
        Map<String, String> map = Maps.newHashMap();
        String appid = ConfigUtils.getAppid();
        map.put("appid", appid);
        String mch_id = ConfigUtils.getMch_id();
        map.put("mch_id", mch_id);
        String nonce_str = CommonUtils.randomString(16);
        map.put("nonce_str", nonce_str);
        String body = GOODS_BODY;
        map.put("body", body);
        String openid = courseOrder.getOpenid();
        map.put("openid", openid);
        String notify_url = ConfigUtils.domainName()+PAY_CALLBACK_PATH;
        map.put("notify_url", notify_url);
        String out_trade_no = courseOrder.getOrderId();
        map.put("out_trade_no", out_trade_no);
        String trade_type = WEIXIN;
        map.put("trade_type", trade_type);
        String spbill_create_ip = ConfigUtils.getExternalIP();
        map.put("spbill_create_ip", spbill_create_ip);
        String time_start = DateUtils.parseDateToString3(new Date());
        map.put("time_start", time_start);
        String time_expire = DateUtils.parseDateToString3(
                DateUtils.afterMinutes(new Date(), ConfigUtils.getBillOpenMinute()));
        map.put("time_expire", time_expire);
        Integer total_fee = (int)(courseOrder.getPrice()*100);
        map.put("total_fee", total_fee.toString());

        String detail = buildOrderDetail(courseOrder, total_fee);
        map.put("detail", detail);

        String sign = CommonUtils.sign(map);

        unifiedOrder.setAppid(appid);
        unifiedOrder.setMch_id(mch_id);
        unifiedOrder.setNonce_str(nonce_str);
        unifiedOrder.setBody(body);
        unifiedOrder.setOpenid(openid);
        unifiedOrder.setNotify_url(notify_url);
        unifiedOrder.setOut_trade_no(out_trade_no);
        unifiedOrder.setTrade_type(trade_type);
        unifiedOrder.setSpbill_create_ip(spbill_create_ip);
        unifiedOrder.setTime_start(time_start);
        unifiedOrder.setTime_expire(time_expire);
        unifiedOrder.setTotal_fee(total_fee);
        //加CDATA标签
        unifiedOrder.setDetail(XMLHelper.appendCDATA(detail));
        unifiedOrder.setSign(sign);

        return unifiedOrder;
    }

    private String buildOrderDetail(CourseOrder courseOrder, Integer total_fee) {
        OrderDetail orderDetail = new OrderDetail();
        List<GoodsDetail> goodsDetailList = Lists.newArrayList();
        orderDetail.setGoodsDetail(goodsDetailList);
        GoodsDetail goodsDetail = new GoodsDetail();
        goodsDetail.setPrice(total_fee);
        goodsDetail.setGoods_id(courseOrder.getCourseId()+"");
        goodsDetail.setGoods_name(courseOrder.getCourseName());
        goodsDetail.setGoods_num(1);
        goodsDetailList.add(goodsDetail);
        return new Gson().toJson(orderDetail);
    }
}
