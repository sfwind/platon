package com.iquanwai.platon.biz.domain.weixin.message;

/**
 * Created by justin on 16/8/10.
 */
public interface TemplateMessageService {
    /**
     * 发送非主动推送模板消息
     * @return 返回发送结果
     */
    boolean sendMessage(TemplateMessage templateMessage);

    /**
     * 根据模板库中的编号获取模板真实id
     * @param templateShortId 模板库中模板的编号，有“TM**”和“OPENTMTM**”等形式
     */
    String getTemplateId(String templateShortId);

    String SEND_MESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token={access_token}";
}
