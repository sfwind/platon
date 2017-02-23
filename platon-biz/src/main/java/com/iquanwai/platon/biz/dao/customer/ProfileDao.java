package com.iquanwai.platon.biz.dao.customer;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.exception.ErrorConstants;
import com.iquanwai.platon.biz.po.common.Profile;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by nethunder on 2017/2/8.
 */
@Repository
public class ProfileDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Profile queryByOpenId(String openId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Profile> h = new BeanHandler(Profile.class);

        try {
            return run.query("SELECT * FROM Profile where Openid=?", h, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }


    public void updatePoint(String openId, int point) {
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        String sql = "UPDATE Profile SET Point = ? where Openid = ?";
        try {
            asyncRun.update(sql, point, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public int insertProfile(Profile profile) throws SQLException {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO Profile(Openid, Nickname, City, Country, Province, Headimgurl, MobileNo, Email, Industry, Function, WorkingLife, RealName, RiseId)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    profile.getOpenid(), profile.getNickname(),profile.getCity(),profile.getCountry(),profile.getProvince(),
                    profile.getHeadimgurl(),profile.getMobileNo(),profile.getEmail(),profile.getIndustry(),
                    profile.getFunction(),profile.getWorkingLife(),profile.getRealName(),profile.getRiseId());
            return insertRs.intValue();
        } catch (SQLException e) {
            if (e.getErrorCode() == ErrorConstants.DUPLICATE_CODE) {
                throw e;
            }
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
    public int updateMeta(String nickname, String headimgurl,String openId) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        String updateSql = "Update Profile Set Nickname=?, Headimgurl=? where Openid=?";
        try {
            Future<Integer> result = asyncRun.update(updateSql,
                    nickname, headimgurl, openId);
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

    public int updateOpenRise(String openId) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        String updateSql = "Update Profile Set OpenRise=1 where Openid=?";
        try {
            Future<Integer> result = asyncRun.update(updateSql, openId);
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
}
