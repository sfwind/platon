package com.iquanwai.platon.biz.domain.weixin.qrcode;

import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * Created by justin on 16/8/12.
 */
public interface QRCodeService {

    /**
     * 生成永久二维码，永久二维码数量有限制
     * @param scene 场景值
     * @return 二维码的数据
     */
    QRResponse generatePermanentQRCode(String scene);

    /**
     * 生成临时二维码
     * @param scene 场景值
     * @param expire_seconds 过期时间，默认时间为30天
     * @return 二维码的数据
     */
    QRResponse generateTemporaryQRCode(String scene, Integer expire_seconds);

    /**
     *  获取二维码，场景值变化
     * @param scene 场景值
     * @return 图片流缓存
     */
    BufferedImage loadQrImage(String scene);

    /**
     * 用ticket得到图片的文件流
     * @param ticket 用于换取图片
     * @return 图片的文件流
     */
    InputStream showQRCode(String ticket);

    String GEN_QRCODE_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token={access_token}";

    String SHOW_QRCODE_URL = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket={ticket}";

    int DEFAULT_EXPIRED_TIME = 60*60*24*30;
}
