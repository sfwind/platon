package com.iquanwai.platon.biz.dao.wx;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.common.Callback;
import com.iquanwai.platon.biz.util.ThreadPool;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.concurrent.*;

/**
 * Created by justin on 16/8/13.
 */
@Repository
public class CallbackDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(Callback callback) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(ThreadPool.getThreadExecutor(), run);
        String insertSql = "INSERT INTO Callback(Openid, Accesstoken, CallbackUrl, RefreshToken, State) " +
                "VALUES(?, ?, ?, ?, ?)";
        try {
            Future<Integer> result = asyncRun.update(insertSql,
                    callback.getOpenid(), callback.getAccessToken(), callback.getCallbackUrl(),
                    callback.getRefreshToken(), callback.getState());
            return result.get();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            // ignore
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        }

        return -1;
    }

    public void updateUserInfo(Callback callback) {
        QueryRunner run = new QueryRunner(getDataSource());
        try {
            run.update("UPDATE Callback Set AccessToken = ?, RefreshToken = ?, Openid = ?, UnionId = ? where State = ?",
                    callback.getAccessToken(), callback.getRefreshToken(), callback.getOpenid(), callback.getUnionId(), callback.getState());

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public Callback queryByPcAccessToken(String accessToken) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Callback> h = new BeanHandler<>(Callback.class);

        try {
            return run.query("SELECT * FROM Callback where PcAccessToken = ?", h, accessToken);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public Callback queryByAccessToken(String accessToken) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Callback> h = new BeanHandler(Callback.class);

        try {
            Callback callback = run.query("SELECT * FROM Callback where AccessToken=?", h, accessToken);
            return callback;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public Callback queryByState(String state) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM Callback WHERE State = ?";
        ResultSetHandler<Callback> h = new BeanHandler<>(Callback.class);
        try {
            return runner.query(sql, h, state);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
