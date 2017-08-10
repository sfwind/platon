package com.iquanwai.platon.biz.util.rabbitmq;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

import java.io.IOException;

/**
 * Created by nethunder on 2017/8/9.
 */
public class RabbitMQConverter implements MessageConverter {
    @Override
    public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        if (messageProperties == null) {
            messageProperties = new MessageProperties();
        }
        Message message = createMessage(object, messageProperties);
        return message;
    }

    private String getDefaultCharset(){
        return "UTF-8";
    }


    protected Message createMessage(Object objectToConvert,
                                  MessageProperties messageProperties)
            throws MessageConversionException {
        byte[] bytes = null;
        try {
            String jsonString = JSON.toJSONString(objectToConvert);
            bytes = jsonString.getBytes(getDefaultCharset());
        }
        catch (IOException e) {
            throw new MessageConversionException(
                    "Failed to convert Message content", e);
        }
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        messageProperties.setContentEncoding(getDefaultCharset());
        messageProperties.setContentLength(bytes.length);
        return new Message(bytes, messageProperties);
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        RabbitMQDto ob = JSON.parseObject(message.getBody(), RabbitMQDto.class);
        System.out.println(JSON.toJSONString(ob));
        return ob;
    }
}
