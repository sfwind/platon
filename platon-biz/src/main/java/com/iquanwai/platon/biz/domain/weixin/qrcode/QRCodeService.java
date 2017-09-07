package com.iquanwai.platon.biz.domain.weixin.qrcode;

import java.io.InputStream;

/**
 * Created by justin on 16/8/12.
 */
public interface QRCodeService {
    String loadQrBase64(String scene);

    QRResponse generatePermanentQRCode(String scene);

    QRResponse generateTemporaryQRCode(String scene, Integer expire_seconds);

    InputStream showQRCode(String ticket);

    String GEN_QRCODE_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token={access_token}";

    String SHOW_QRCODE_URL = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket={ticket}";

    int DEFAULT_EXPIRED_TIME = 60 * 60 * 24 * 30;
}
