package com.iquanwai.platon.biz.domain.common.message;

import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQDto;

/**
 * Created by nethunder on 2017/7/22.
 */
public interface MQService {

    void saveMQSendOperation(MQSendLog queue);

    void updateAfterDealOperation(RabbitMQDto msgId);
}
