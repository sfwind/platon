package com.iquanwai.platon.biz.domain.weixin.qrcode;

import lombok.Data;

/**
 * Created by justin on 17/7/8.
 */
@Data
public class QRPermanentRequest {
    private String action_name = "QR_LIMIT_STR_SCENE";

    private ActionInfo action_info;

    public QRPermanentRequest(String scene){
        this.action_info = new ActionInfo(scene);
    }
}
