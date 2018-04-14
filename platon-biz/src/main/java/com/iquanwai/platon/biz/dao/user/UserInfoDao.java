package com.iquanwai.platon.biz.dao.user;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.user.UserInfo;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class UserInfoDao extends DBUtil {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public UserInfo loadByProfileId(Integer profileId){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<UserInfo> h = new BeanHandler<>(UserInfo.class);
        String sql = "SELECT * FROM UserInfo WHERE ProfileId = ? AND DEL = 0";

        try {
            return runner.query(sql,h,profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return null;
    }

    public Integer insert(UserInfo userInfo){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO UserInfo(ProfileId,Industry,Function,WorkingYear,Company,College,Mobile,Introduction,Address,ReceiverMobile,RealName,Receiver) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            Long result =  runner.insert(sql,new ScalarHandler<>(),userInfo.getProfileId(),userInfo.getIndustry(),userInfo.getFunction(),
                    userInfo.getWorkingYear(),userInfo.getCompany(),userInfo.getCollege(),userInfo.getMobile(),userInfo.getIntroduction(),
                    userInfo.getAddress(),userInfo.getReceiverMobile(),userInfo.getRealName(),userInfo.getReceiver());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return -1;
    }


    public Integer update(UserInfo userInfo){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update UserInfo SET Industry=?,Function=?,WorkingYear=?,Company = ?,College=?,Mobile=?,Introduction=?,Address = ?,ReceiverMobile=?,RealName=?,Receiver=? WHERE ProfileId = ?";
        try {
            return runner.update(sql,userInfo.getIndustry(),userInfo.getFunction(),
                    userInfo.getWorkingYear(),userInfo.getCompany(),userInfo.getCollege(),
                    userInfo.getMobile(), userInfo.getIntroduction(), userInfo.getAddress(),
                    userInfo.getReceiverMobile(),userInfo.getRealName(),userInfo.getReceiver(),userInfo.getProfileId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return -1;
    }

    public Integer updateMobile(String mobile,Integer profileId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update UserInfo SET Mobile = ? WHERE ProfileId = ?";

        try {
            return runner.update(sql,mobile,profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return -1;
    }
}