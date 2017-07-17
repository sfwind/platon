package com.iquanwai.platon.biz.domain.weixin.customer;

import lombok.Data;

/**
 * Created by justin on 17/7/8.
 */
@Data
public class ImageCustomerMessage {
    private String touser;
    private String msgtype = "image";
    private Image image;

    public ImageCustomerMessage(String openid, String mediaId){
        this.touser = openid;
        this.image = new Image(mediaId);
    }

    @Data
    public static class Image {
        private String media_id;

        public Image(String mediaId) {
            this.media_id = mediaId;
        }
    }
}
