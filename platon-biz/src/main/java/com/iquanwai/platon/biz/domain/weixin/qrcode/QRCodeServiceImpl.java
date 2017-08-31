package com.iquanwai.platon.biz.domain.weixin.qrcode;

import com.google.gson.Gson;
import com.iquanwai.platon.biz.util.ImageUtils;
import com.iquanwai.platon.biz.util.RestfulHelper;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * Created by justin on 16/8/12.
 */
@Service
public class QRCodeServiceImpl implements QRCodeService {
    @Autowired
    private RestfulHelper restfulHelper;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public QRResponse generateTemporaryQRCode(String scene, Integer expire_seconds) {
        if(expire_seconds==null){
            expire_seconds = DEFAULT_EXPIRED_TIME;
        }
        QRTemporaryRequest qrRequest = new QRTemporaryRequest(scene, expire_seconds);
        String json = new Gson().toJson(qrRequest);
        return generate(json);
    }

    @Override
    public BufferedImage loadQrImage(String scene) {
        // 绘图数据
        QRResponse response = generateTemporaryQRCode(scene, null);
        InputStream inputStream = showQRCode(response.getTicket());
        return ImageUtils.getBufferedImageByInputStream(inputStream);
    }

    @Override
    public InputStream showQRCode(String ticket) {
        String url = SHOW_QRCODE_URL.replace("{ticket}", ticket);
        ResponseBody body = restfulHelper.getPlain(url);
        if(body == null){
            return null;
        }
        return body.byteStream();
    }

    public QRResponse generatePermanentQRCode(String scene) {
        QRPermanentRequest qrRequest = new QRPermanentRequest(scene);
        String json = new Gson().toJson(qrRequest);
        return generate(json);
    }

    private QRResponse generate(String json) {
        String body = restfulHelper.post(GEN_QRCODE_URL, json);
        Gson gson = new Gson();
        return gson.fromJson(body, QRResponse.class);
    }
}
