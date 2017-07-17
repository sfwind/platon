package com.iquanwai.platon.biz.domain.weixin.qrcode;

import lombok.Data;

/**
 * Created by justin on 17/7/8.
 */
@Data
public class Scene {
    public Scene(String scene){
        this.scene_str = scene;
    }
    private String scene_str;
}
