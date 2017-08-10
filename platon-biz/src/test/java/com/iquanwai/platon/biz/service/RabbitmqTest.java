package com.iquanwai.platon.biz.service;

import com.iquanwai.platon.biz.TestBase;
import org.junit.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by nethunder on 2017/8/8.
 */
public class RabbitmqTest extends TestBase {
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    @Qualifier(value = "amqpTestQueue")
    private Queue amqpTestQueue;
    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Test
    public void templateTest() {
        Exchange exchange = new FanoutExchange("test_fanout",false,true);
        Queue queue = new Queue("test_queue", true, true, true);
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(queue.getName()).noargs();
        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(binding);
        amqpAdmin.removeBinding(binding);
        System.out.println("over");
    }
}
