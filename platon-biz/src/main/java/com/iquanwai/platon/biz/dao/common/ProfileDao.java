package com.iquanwai.platon.biz.dao.common;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.exception.ErrorConstants;
import com.iquanwai.platon.biz.po.common.Profile;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
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
        String sql = "INSERT INTO Profile(Openid, Nickname, City, Country, Province, Headimgurl, MobileNo, Email, Industry, Function, WorkingLife, RealName, RiseId, UnionId)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    profile.getOpenid(), profile.getNickname(),profile.getCity(),profile.getCountry(),profile.getProvince(),
                    profile.getHeadimgurl(),profile.getMobileNo(),profile.getEmail(),profile.getIndustry(),
                    profile.getFunction(),profile.getWorkingLife(),profile.getRealName(),profile.getRiseId(),profile.getUnionid());
            return insertRs.intValue();
        } catch (SQLException e) {
            if (e.getErrorCode() == ErrorConstants.DUPLICATE_CODE) {
                throw e;
            }
            logger.error(e.getLocalizedMessage(), e);
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

    public int updateOpenApplication(String openId){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        String updateSql = "Update Profile Set OpenApplication=1 where Openid=?";
        try {
            Future<Integer> result = asyncRun.update(updateSql, openId);
            return result.get();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public int updateOpenConsolidation(String openId){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), run);
        String updateSql = "Update Profile Set OpenConsolidation=1 where Openid=?";
        try {
            Future<Integer> result = asyncRun.update(updateSql, openId);
            return result.get();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<Profile> queryAccounts(List<String> openids) {
        if(CollectionUtils.isEmpty(openids)){
            return Lists.newArrayList();
        }
        String questionMarks = produceQuestionMark(openids.size());
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Profile>> h = new BeanListHandler(Profile.class);
        String sql = "SELECT * FROM Profile where Openid in ("+ questionMarks +")";
        try {
            return run.query(sql, h, openids.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public boolean submitPersonalCenterProfile(Profile profile) {
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update Profile Set Industry=?, Function=?, WorkingLife=?, City=?, Province=? where Openid=?";
        try {
            run.update(updateSql,
                    profile.getIndustry(),
                    profile.getFunction(),
                    profile.getWorkingLife(),
                    profile.getCity(),
                    profile.getProvince(),
                    profile.getOpenid());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public void completeProfile(String openId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE Profile SET IsFull = 1 where Openid = ?";
        try {
            runner.update(sql, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public Boolean submitPersonalProfile(Profile account) {
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update Profile Set MobileNo=?, Email=?, Industry=?, Function=?, WorkingLife=?, " +
                "RealName=?, City=?, Province=? where Openid=?";
        try {
            run.update(updateSql,
                    account.getMobileNo(), account.getEmail(),
                    account.getIndustry(), account.getFunction(),
                    account.getWorkingLife(), account.getRealName(),
                    account.getCity(), account.getProvince(),
                    account.getOpenid());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }
}
