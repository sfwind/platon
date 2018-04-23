package com.iquanwai.platon.biz.dao.user;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.user.UserInfo;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.collections.CollectionUtils;
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

@Repository
public class UserInfoDao extends DBUtil {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public UserInfo loadByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<UserInfo> h = new BeanHandler<>(UserInfo.class);
        String sql = "SELECT * FROM UserInfo WHERE ProfileId = ? AND DEL = 0";

        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<UserInfo> loadByProfileIds(List<Integer> profileIds){
        if(CollectionUtils.isEmpty(profileIds)){
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String mask = produceQuestionMark(profileIds.size());
        ResultSetHandler<List<UserInfo>> h = new BeanListHandler<UserInfo>(UserInfo.class);
        String sql = "SELECT * FROM UserInfo WHERE ProfileId in (" + mask +" ) AND DEL = 0";

        try {
            return runner.query(sql,h,profileIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return Lists.newArrayList();
    }



    public List<UserInfo> loadList(Integer profileId, Page page) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM UserInfo WHERE Industry is NOT NULL AND COMPANY IS NOT NULL AND profileId !=? AND DEL = 0  ORDER BY Priority desc LIMIT " + page.getOffset() + "," + page.getLimit();
        try {
            return runner.query(sql, new BeanListHandler<>(UserInfo.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer loadCount(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT count(*) from UserInfo WHERE Industry IS NOT NULL AND COMPANY IS NOT NULL AND profileId !=? AND DEL = 0";
        try {
            return runner.query(sql, new ScalarHandler<Long>(), profileId).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer insert(UserInfo userInfo) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO UserInfo(ProfileId,Industry,Function,WorkingYear,Company,College,Introduction,Address,ReceiverMobile,RealName,Receiver) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), userInfo.getProfileId(), userInfo.getIndustry(), userInfo.getFunction(),
                    userInfo.getWorkingYear(), userInfo.getCompany(), userInfo.getCollege(), userInfo.getIntroduction(),
                    userInfo.getAddress(), userInfo.getReceiverMobile(), userInfo.getRealName(), userInfo.getReceiver());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }


    public Integer update(UserInfo userInfo) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update UserInfo SET Industry=?,Function=?,WorkingYear=?,Company = ?,College=?,Introduction=?,Address = ?,ReceiverMobile=?,RealName=?,Receiver=? WHERE ProfileId = ?";
        try {
            return runner.update(sql, userInfo.getIndustry(), userInfo.getFunction(),
                    userInfo.getWorkingYear(), userInfo.getCompany(), userInfo.getCollege(),
                    userInfo.getIntroduction(), userInfo.getAddress(), userInfo.getReceiverMobile(),
                    userInfo.getRealName(), userInfo.getReceiver(), userInfo.getProfileId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer updateMobile(String mobile, Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update UserInfo SET Mobile = ? WHERE ProfileId = ?";

        try {
            return runner.update(sql, mobile, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer updateIsFull(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update UserInfo set IsFull = 1 where ProfileId = ? AND DEL = 0";

        try {
            return runner.update(sql, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}