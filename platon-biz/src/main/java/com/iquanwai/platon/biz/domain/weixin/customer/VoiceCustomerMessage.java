package com.iquanwai.platon.biz.domain.weixin.customer;

import lombok.Data;

/**
 * Created by justin on 17/7/8.
 */
@Data
public class VoiceCustomerMessage {
    private String touser;
    private String msgtype = "voice";
    private Voice voice;

    public VoiceCustomerMessage(String openid, String mediaId){
        this.touser = openid;
        this.voice = new Voice(mediaId);
    }

    @Data
    public static class Voice {
        private String media_id;

        public Voice(String mediaId) {
            this.media_id = mediaId;
        }
    }
}
