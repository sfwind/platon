package com.iquanwai.platon.biz.service;

import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.domain.weixin.pay.PayService;
import com.iquanwai.platon.biz.util.RestfulHelper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 16/9/15.
 */
public class RestfulHelperTest extends TestBase {
    @Autowired
    private RestfulHelper restfulHelper;

    @Test
    public void testPost(){
        String xml="<xml>\n" +
                "    <appid>wx6d7641af1b854a21</appid>\n" +
                "    <body>圈外-线上课程</body>\n" +
                "    <detail><![CDATA[{\"goodsDetail\":[{\"goods_id\":\"1\",\"goods_name\":\"结构化思维\",\"goods_num\":1,\"price\":1000}]}]]></detail>\n" +
                "    <mch_id>1388290502</mch_id>\n" +
                "    <nonce_str>2hsx2nhbt3dk23re</nonce_str>\n" +
                "    <notify_url>http://www.confucius.mobi/wx/pay/result/callback</notify_url>\n" +
                "    <openid>oK881wQekezGpw6rq790y_vAY_YY</openid>\n" +
                "    <out_trade_no>jdhc2d90l57hqt8b</out_trade_no>\n" +
                "    <sign>7E3C77818073BDC6E45EF6070282CFE9</sign>\n" +
                "    <spbill_create_ip>121.43.177.170</spbill_create_ip>\n" +
                "    <time_expire>20160915082733</time_expire>\n" +
                "    <time_start>20160915075733</time_start>\n" +
                "    <total_fee>1000</total_fee>\n" +
                "    <trade_type>JSAPI</trade_type>\n" +
                "</xml>";
        restfulHelper.postXML(PayService.UNIFIED_ORDER_URL, xml);
    }

}
