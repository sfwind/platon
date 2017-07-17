package com.iquanwai.platon.biz.domain.weixin.qrcode;

import lombok.Data;

/**
 * Created by justin on 16/8/13.
 */
@Data
public class QRTemporaryRequest {
    private int expire_seconds;
    private String action_name = "QR_STR_SCENE";

    private ActionInfo action_info;

    public QRTemporaryRequest(String scene, int expire_seconds){
        this.expire_seconds = expire_seconds;
        this.action_info = new ActionInfo(scene);
    }
}
