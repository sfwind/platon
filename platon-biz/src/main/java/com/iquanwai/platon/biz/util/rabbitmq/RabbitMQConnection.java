package com.iquanwai.platon.biz.util.rabbitmq;

import com.iquanwai.platon.biz.util.ConfigUtils;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by justin on 17/7/29.
 */
public class RabbitMQConnection {
    @Getter
    private Connection connection;

    private static RabbitMQConnection rabbitMQConnection = new RabbitMQConnection();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public RabbitMQConnection(){
        init();
    }

    public static RabbitMQConnection create(){
        return rabbitMQConnection;
    }

    public void init(){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(ConfigUtils.getRabbitMQIp());
        factory.setPort(ConfigUtils.getRabbitMQPort());
        factory.setUsername(ConfigUtils.getRabbitMQUser());
        factory.setPassword(ConfigUtils.getRabbitMQPasswd());
        try {
            connection = factory.newConnection();
        } catch (IOException e) {
            logger.error("connection error", e);
        } catch (TimeoutException e) {
            logger.error("connection timeout", e);
        }
    }

    public void destroy() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (IOException e) {
            logger.error("connection error", e);
        }
    }
}
