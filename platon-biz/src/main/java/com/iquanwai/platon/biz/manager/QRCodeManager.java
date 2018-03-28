package com.iquanwai.platon.biz.manager;


import com.google.gson.Gson;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRResponse;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRTemporaryRequest;
import com.iquanwai.platon.biz.util.ImageUtils;
import com.iquanwai.platon.biz.util.RestfulHelper;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;


public class QRCodeManager {

    @Autowired
    private RestfulHelper restfulHelper;

    int DEFAULT_EXPIRED_TIME = 60 * 60 * 24 * 30;

    private  final Logger logger = LoggerFactory.getLogger(getClass());

    String GEN_QRCODE_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token={access_token}";

    String SHOW_QRCODE_URL = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket={ticket}";


    public  BufferedImage loadQrImage(String scene) {
        // 绘图数据
        QRResponse response = generateTemporaryQRCode(scene, null);
        InputStream inputStream = showQRCode(response.getTicket());
        try {
            return ImageUtils.getBufferedImageByInputStream(inputStream);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("is close failed", e);
            }
        }
    }

    /**
     * 生成临时二维码
     * @param scene 场景值
     * @param expire_seconds 过期时间，默认时间为30天
     * @return 二维码的数据
     */
    public  QRResponse generateTemporaryQRCode(String scene, Integer expire_seconds) {
        if (expire_seconds == null) {
            expire_seconds = DEFAULT_EXPIRED_TIME;
        }
        QRTemporaryRequest qrRequest = new QRTemporaryRequest(scene, expire_seconds);
        String json = new Gson().toJson(qrRequest);
        return generate(json);
    }



    public InputStream showQRCode(String ticket) {
        String url = SHOW_QRCODE_URL.replace("{ticket}", ticket);
        ResponseBody body = restfulHelper.getPlain(url);
        if (body == null) {
            return null;
        }
        return body.byteStream();
    }


    private QRResponse generate(String json) {
        String body = restfulHelper.post(GEN_QRCODE_URL, json);
        Gson gson = new Gson();
        return gson.fromJson(body, QRResponse.class);
    }
}
