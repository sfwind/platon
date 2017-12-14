package com.iquanwai.platon.biz.dao.common;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.exception.ErrorConstants;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ThreadPool;
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
import java.util.concurrent.Future;

/**
 * Created by nethunder on 2017/2/8.
 */
@Repository
public class ProfileDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Profile queryByOpenId(String openId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Profile> h = new BeanHandler<>(Profile.class);

        try {
            return run.query("SELECT * FROM Profile where Openid=?", h, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public Profile queryByRiseId(String riseId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<Profile> h = new BeanHandler<>(Profile.class);

        try {
            return run.query("SELECT * FROM Profile where RiseId=?", h, riseId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public void updatePoint(Integer id, int point) {
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(ThreadPool.getThreadExecutor(), runner);
        String sql = "UPDATE Profile SET Point = ? where Id = ?";
        try {
            asyncRun.update(sql, point, id);
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
                    profile.getOpenid(), profile.getNickname(), profile.getCity(), profile.getCountry(), profile.getProvince(),
                    profile.getHeadimgurl(), profile.getMobileNo(), profile.getEmail(), profile.getIndustry(),
                    profile.getFunction(), profile.getWorkingLife(), profile.getRealName(), profile.getRiseId(), profile.getUnionid());
            return insertRs.intValue();
        } catch (SQLException e) {
            if (e.getErrorCode() == ErrorConstants.DUPLICATE_CODE) {
                throw e;
            }
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public int updateOpenRise(Integer id) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(ThreadPool.getThreadExecutor(), run);
        String updateSql = "Update Profile Set OpenRise=1 where Id=?";
        try {
            Future<Integer> result = asyncRun.update(updateSql, id);
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

    public int updateOpenNavigator(Integer id) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(ThreadPool.getThreadExecutor(), run);
        String updateSql = "Update Profile Set OpenNavigator=1 where Id=?";
        try {
            Future<Integer> result = asyncRun.update(updateSql, id);
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

    public int updateOpenApplication(Integer id) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(ThreadPool.getThreadExecutor(), run);
        String updateSql = "Update Profile Set OpenApplication=1 where Id=?";
        try {
            Future<Integer> result = asyncRun.update(updateSql, id);
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

    public int updateOpenConsolidation(Integer id) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(ThreadPool.getThreadExecutor(), run);
        String updateSql = "Update Profile Set OpenConsolidation=1 where Id=?";
        try {
            Future<Integer> result = asyncRun.update(updateSql, id);
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

    public List<Profile> queryAccounts(List<Integer> profileIds) {
        if (CollectionUtils.isEmpty(profileIds)) {
            return Lists.newArrayList();
        }
        String questionMarks = produceQuestionMark(profileIds.size());
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Profile>> h = new BeanListHandler<>(Profile.class);
        String sql = "SELECT * FROM Profile where Id in (" + questionMarks + ")";
        try {
            return run.query(sql, h, profileIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public boolean submitPersonalCenterProfile(Profile profile) {
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update Profile Set Industry=?, Function=?, WorkingYear=?, City=?, Province=?,Receiver=? where id=?";
        try {
            run.update(updateSql,
                    profile.getIndustry(),
                    profile.getFunction(),
                    profile.getWorkingYear(),
                    profile.getCity(),
                    profile.getProvince(),
                    profile.getReceiver(),
                    profile.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public boolean submitPersonalCenterProfileWithMoreDetail(Profile profile) {
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update Profile Set Industry=?, Function=?, WorkingYear=?, City=?, Province=?,RealName=?,Address=?,Receiver=? where id=?";
        try {
            run.update(updateSql,
                    profile.getIndustry(),
                    profile.getFunction(),
                    profile.getWorkingYear(),
                    profile.getCity(),
                    profile.getProvince(),
                    profile.getRealName(),
                    profile.getAddress(),
                    profile.getReceiver(),
                    profile.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public boolean submitCertificateProfile(Profile profile) {
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update Profile Set Industry=?, Function=?, WorkingLife=?, City=?, Province=?, RealName=? where id=?";
        try {
            run.update(updateSql,
                    profile.getIndustry(),
                    profile.getFunction(),
                    profile.getWorkingLife(),
                    profile.getCity(),
                    profile.getProvince(),
                    profile.getRealName(),
                    profile.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public void completeProfile(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE Profile SET IsFull = 1 where Id = ?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateMobile(String mobileNo, Integer id) {
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update Profile Set MobileNo=? where Id=?";
        try {
            run.update(updateSql,
                    mobileNo, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public int updateOpenWelcome(Integer id) {
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(ThreadPool.getThreadExecutor(), run);
        String updateSql = "Update Profile Set OpenWelcome=1 where Id=?";
        try {
            Future<Integer> result = asyncRun.update(updateSql, id);
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

    public int updateHeadImgUrl(Integer profileId, String headImgUrl) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE Profile SET HeadImgUrl = ? WHERE Id = ?";
        try {
            return runner.update(sql, headImgUrl, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public int updateNickName(Integer profileId, String nickName) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE Profile SET NickName = ? WHERE Id = ?";
        try {
            return runner.update(sql, nickName, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Boolean updateLearningNotifyStatus(Integer profileId, Integer status) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String updateSql = "Update Profile set LearningNotify = ? where Id = ?";
        try {
            int update = runner.update(updateSql, status, profileId);
            return update > 0;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

    public Boolean updateWeixinId(Integer profileId, String weixinId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String updateSql = "Update Profile set WeixinId = ? where Id = ?";
        try {
            int update = runner.update(updateSql, weixinId, profileId);
            return update > 0;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return false;
    }
}
