package com.iquanwai.platon.biz.domain.weixin.customer;

import lombok.Data;

/**
 * Created by justin on 17/7/8.
 */
@Data
public class TextCustomerMessage {
    private String touser;
    private String msgtype = "text";
    private Text text;

    public TextCustomerMessage(String openid, String content){
        this.touser = openid;
        this.text = new Text(content);
    }

    @Data
    public static class Text {
        private String content;

        public Text(String content) {
            this.content = content;
        }
    }
}
