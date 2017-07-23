package com.iquanwai.platon.biz.domain.common.message;

import com.iquanwai.platon.biz.dao.common.MessageQueueDao;
import com.iquanwai.platon.biz.po.common.MessageQueue;
import com.iquanwai.platon.biz.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by nethunder on 2017/7/22.
 */
@Service
public class MQServiceImpl implements MQService {
    @Autowired
    private MessageQueueDao messageQueueDao;

    @Override
    public void saveMQSendOperation(MessageQueue queue){
        String msgId = CommonUtils.randomString(32);
        queue.setMsgId(msgId);
        queue.setStatus(0);

        MessageQueue msg = new MessageQueue();
        msg.setMsgId(msgId);
        messageQueueDao.insert(queue);
    }
}
