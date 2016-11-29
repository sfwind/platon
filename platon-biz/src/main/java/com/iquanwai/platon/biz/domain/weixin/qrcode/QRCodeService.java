package com.iquanwai.platon.biz.domain.weixin.qrcode;

/**
 * Created by justin on 16/8/12.
 */
public interface QRCodeService {
    String generateQRCode(String scene);

    String generateQRCode(String scene, int expire_seconds);

    int DEFAULT_EXPIRED_TIME = 60*60*24;

    String GEN_QRCODE_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token={access_token}";
}
