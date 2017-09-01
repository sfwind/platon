package com.iquanwai.platon.biz.domain.common.message;

import com.iquanwai.platon.biz.dao.common.MQDealLogDao;
import com.iquanwai.platon.biz.dao.common.MQSendLogDao;
import com.iquanwai.platon.biz.util.ThreadPool;
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
    private MQSendLogDao mqSendLogDao;
    @Autowired
    private MQDealLogDao mqDealLogDao;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public void saveMQSendOperation(MQSendLog mqSendLog) {
        // 插入mqSendOperation

        ThreadPool.execute(
            new Thread(() -> {
                String ip = null;
                try {
                    InetAddress localHost = InetAddress.getLocalHost();
                    ip = localHost.getHostAddress();
                } catch (UnknownHostException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
                mqSendLog.setPublisherIp(ip);
                mqSendLogDao.insert(mqSendLog);
            }), "thread-save-mqlog");
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
        MQDealLog mqDealLog = new MQDealLog();
        mqDealLog.setMsgId(msgId);
        mqDealLog.setTopic(dto.getTopic());
        mqDealLog.setQueue(dto.getQueue());
        mqDealLog.setConsumerIp(ip);
        mqDealLogDao.insert(mqDealLog);
    }

}
