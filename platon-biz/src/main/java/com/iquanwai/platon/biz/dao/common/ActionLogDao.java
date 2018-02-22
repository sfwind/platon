package com.iquanwai.platon.biz.dao.common;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.common.ActionLog;
import com.iquanwai.platon.biz.util.ThreadPool;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by justin on 2018/2/20.
 */
@Repository
public class ActionLogDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(ActionLog log){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(ThreadPool.getThreadExecutor(), run);
        try {
            String insertSql = "INSERT INTO ActionLog(ProfileId, Module, Function, Action, Memo, Platform, Channel) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?)";
            Future<Integer> result =  asyncRun.update(insertSql,
                    log.getProfileId(), log.getModule(), log.getFunction(),
                    log.getAction(), log.getMemo(), log.getPlatform(), log.getChannel());
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
}
