package com.iquanwai.platon.biz.domain.weixin.qrcode;

import lombok.Data;

/**
 * Created by justin on 17/7/8.
 */
@Data
public class QRResponse {
    private String ticket;
    private String url;
    private int expire_seconds;
}
