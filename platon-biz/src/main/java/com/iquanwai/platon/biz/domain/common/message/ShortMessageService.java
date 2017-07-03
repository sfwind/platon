package com.iquanwai.platon.biz.domain.common.message;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by justin on 17/6/28.
 */
public interface ShortMessageService {
    /**
     * 发送短消息
     * */
    Pair<Boolean, String> sendShortMessage(SMSDto smsDto);

    /**
     * 是否能发送短消息
     * */
    boolean canSend(Integer profileId);
}
