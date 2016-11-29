package com.iquanwai.platon.biz.domain.weixin.qrcode;

import lombok.Data;

/**
 * Created by justin on 16/8/13.
 */
@Data
public class QRRequest {
    private int expire_seconds;
    private String action_name = "QR_SCENE";

    private ActionInfo action_Info;

    public QRRequest(String scene, int expire_seconds){
        this.expire_seconds = expire_seconds;
        this.action_Info = new ActionInfo(scene);
    }

    @Data
    public static class ActionInfo{
        public ActionInfo(String scene){
            this.scene = new Scene(scene);
        }
        private Scene scene;

        @Data
        class Scene{
            public Scene(String scene){
                this.scene_str = scene;
            }
            private String scene_str;
        }
    }

}
