package com.iquanwai.platon.biz.domain.common.message;

import com.iquanwai.platon.biz.dao.common.MessageQueueDao;
import com.iquanwai.platon.biz.po.common.MessageQueue;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by nethunder on 2017/7/22.
 */
@Service
public class MQServiceImpl implements MQService {
    @Autowired
    private MessageQueueDao messageQueueDao;
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public void saveMQSendOperation(MessageQueue queue){
        messageQueueDao.insert(queue);
    }


    @Override
    public void updateAfterDealOperation(RabbitMQDto dto) {
        String msgId = dto.getMsgId();
        String ip = null;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            ip = localHost.getHostAddress();
        } catch (UnknownHostException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        MessageQueue load = messageQueueDao.load(msgId);
        if (load != null) {
            String consumerIp = ip;
            String queue = null;
            if (load.getConsumerIp() != null) {
                consumerIp = load.getConsumerIp() + "," + ip;
            }
            if (load.getQueue() != null) {
                // 已有队列处理过
                if (dto.getQueue() != null) {
                    queue = load.getQueue() + dto.getQueue();
                } else {
                    queue = load.getQueue();
                }
            } else {
                queue = dto.getQueue();
            }
            messageQueueDao.update(load.getId(), consumerIp, queue);
        } else {
            logger.error("异常，没有改消息记录");
        }
    }

}
