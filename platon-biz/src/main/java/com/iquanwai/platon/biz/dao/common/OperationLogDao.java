package com.iquanwai.platon.biz.dao.common;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.common.OperationLog;
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
 * Created by justin on 16/9/3.
 */
@Repository
public class OperationLogDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(OperationLog log){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(ThreadPool.getThreadExecutor(), run);
        try {
            String insertSql = "INSERT INTO OperatingLog(Openid, Module, Function, Action, OperateTime, OperateDate, Memo) " +
                    "VALUES(?, ?, ?, ?, now(), curdate(), ?)";
            Future<Integer> result =  asyncRun.update(insertSql,
                    log.getOpenid(), log.getModule(), log.getFunction(),
                    log.getAction(), log.getMemo());
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
