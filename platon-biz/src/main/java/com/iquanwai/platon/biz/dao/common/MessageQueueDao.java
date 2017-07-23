package com.iquanwai.platon.biz.dao.common;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.common.MessageQueue;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by nethunder on 2017/7/22.
 */
@Repository
public class MessageQueueDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(MessageQueue message){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        try {
            String insertSql = "INSERT INTO MessageQueue(MsgId, Topic, Queue, Message) VALUES (?,?,?,?)";
            Future<Integer> result = asyncRun.update(insertSql, message.getMsgId(),
                    message.getTopic(), message.getQueue(), message.getMessage());
            return result.get();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            // ignore
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }

    public MessageQueue load(String msgId){
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "Select * from MessageQueue where MsgId = ?";
        try{
            return run.query(sql, new BeanHandler<MessageQueue>(MessageQueue.class), msgId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public void update(Integer id, String ip, String queue) {
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "UPDATE MessageQueue SET Status = 1, ConsumerIp=?,Queue = ? where Id = ?";
        try {
            run.update(sql, ip, queue, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
