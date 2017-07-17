package com.iquanwai.platon.biz.domain.weixin.qrcode;

import lombok.Data;

/**
 * Created by justin on 17/7/8.
 */
@Data
public class ActionInfo {
    public ActionInfo(String scene){
        this.scene = new Scene(scene);
    }
    private Scene scene;
}
