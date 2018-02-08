package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.NotifyMessage;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by justin on 17/2/27.
 */
@Repository
public class NotifyMessageDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<NotifyMessage> getMyMessages(Integer profileId, Page page){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from NotifyMessage where ToUser=? and Old=1 order by Id desc limit " +
                + page.getOffset() + "," + page.getLimit();
        try {
            ResultSetHandler<List<NotifyMessage>> h = new BeanListHandler<>(NotifyMessage.class);
            return runner.query(sql, h, profileId.toString());
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public Integer getMyMessagesCount(Integer profileId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select count(*) from NotifyMessage where ToUser=? and Old=1";
        try {
            ResultSetHandler<Long> h = new ScalarHandler<>();
            return runner.query(sql, h, profileId.toString()).intValue();
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    public Integer loadOldCount(Integer profileId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select count(*) from NotifyMessage where ToUser = ? and Old = 0";
        try{
            ResultSetHandler<Long> h = new ScalarHandler<>();
            return runner.query(sql, h, profileId.toString()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public void insert(NotifyMessage message){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into NotifyMessage(Message, FromUser, ToUser, Url, SendTime, IsRead, Old)" +
                "values(?,?,?,?,?,?,?)";
        try {
            runner.insert(sql, new ScalarHandler<>(),
                    message.getMessage(), message.getFromUser(), message.getToUser(),
                    message.getUrl(), message.getSendTime(), message.getIsRead(),
                    message.getOld());
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

    }

    public void read(int id){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update NotifyMessage set ReadTime=?, IsRead=1 where id=?";
        try {
            runner.update(sql, new Date(), id);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

    }


    public void markOld(Integer profileId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update NotifyMessage set Old=1 where ToUser=? and Old=0";
        try {
            runner.update(sql, profileId.toString());
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

    }

}
