package com.iquanwai.platon.biz.domain.fragmentation.message;

/**
 * Created by xfduan on 2017/7/5.
 */
public class MessageURL {

    public static String getPCUrl(String originUrl) {
        String targetUrl = "";
        if (originUrl != null) {
            if (originUrl.contains("/rise/static/message/warmup/reply")) {
                targetUrl = originUrl.replace("/rise/static/message/warmup/reply", "/fragment/message/warmup/reply");
            } else if (originUrl.contains("/rise/static/message/knowledge/reply")) {
                targetUrl = originUrl.replace("/rise/static/message/knowledge/reply", "/fragment/message/knowledge/reply");
            } else if (originUrl.contains("/rise/static/message/comment/reply")) {
                targetUrl = originUrl.replace("/rise/static/message/comment/reply", "/fragment/message/comment/reply");
            } else if (originUrl.contains("/rise/static/message/application/reply")) {
                targetUrl = originUrl.replace("/rise/static/message/application/reply", "/fragment/application/comment");
            } else if (originUrl.contains("/survey/wjx")) {
                targetUrl = originUrl.replace("/survey/wjx", "/pc/survey/wjx");
            } else if (!originUrl.contains("http") && !originUrl.contains("https")) {
                targetUrl = "/fragment/message";
            }
        }
        return targetUrl;
    }

}
