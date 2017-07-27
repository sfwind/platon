package com.iquanwai.platon.biz.util.rabbitmq;

import lombok.Data;

/**
 * Created by nethunder on 2017/7/23.
 */
@Data
public class RabbitMQDto {
    private String msgId;
    private Object message;
    private String queue;
    private String topic;
}
