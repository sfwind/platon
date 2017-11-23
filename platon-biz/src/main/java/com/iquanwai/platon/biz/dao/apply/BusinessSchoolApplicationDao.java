package com.iquanwai.platon.biz.dao.apply;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * @author nethunder
 * @version 2017/9/27
 */
@Repository
public class BusinessSchoolApplicationDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public BusinessSchoolApplication loadByOpenId(String openId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM BusinessSchoolApplication WHERE Openid = ? AND Del = 0";
        try {
            return runner.query(sql, new BeanHandler<>(BusinessSchoolApplication.class), openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public BusinessSchoolApplication loadCheckingApplication(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM BusinessSchoolApplication WHERE ProfileId = ? AND Del = 0 AND Status = 0 Order by Id desc";
        try {
            return runner.query(sql, new BeanHandler<>(BusinessSchoolApplication.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public BusinessSchoolApplication loadLastApproveApplication(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM BusinessSchoolApplication WHERE ProfileId = ? AND Del = 0 AND Status = 1 Order by Id desc";
        try {
            return runner.query(sql, new BeanHandler<>(BusinessSchoolApplication.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<BusinessSchoolApplication> loadList(Page page) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM BusinessSchoolApplication WHERE Status = 0 AND Del =0 LIMIT " + page.getOffset() + "," + page.getLimit();
        try {
            return runner.query(sql, new BeanListHandler<>(BusinessSchoolApplication.class));
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer loadCount() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT count(*) from BusinessSchoolApplication WHERE Status = 0 AND Del =0";
        try {
            return runner.query(sql, new ScalarHandler<Long>()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer reject(Integer id, String comment) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE BusinessSchoolApplication SET Status = 2,Comment = ?,CheckTime = CURRENT_TIMESTAMP WHERE Id = ?";
        try {
            return runner.update(sql, comment, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer approve(Integer id, Double coupon, String comment) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE BusinessSchoolApplication SET Status = 1,Coupon = ?,Comment = ?,CheckTime = CURRENT_TIMESTAMP WHERE Id = ?";
        try {
            return runner.update(sql, coupon, comment, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer ignore(Integer id, String comment) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE BusinessSchoolApplication SET Status = 3,Comment = ?,CheckTime = CURRENT_TIMESTAMP WHERE Id = ?";
        try {
            return runner.update(sql, comment, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer insert(BusinessSchoolApplication businessSchoolApplication) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO BusinessSchoolApplication(SubmitId, ProfileId, Openid, Status, CheckTime, IsDuplicate, Deal, OriginMemberType,SubmitTime,DealTime,Comment) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), businessSchoolApplication.getSubmitId(), businessSchoolApplication.getProfileId(), businessSchoolApplication.getOpenid(),
                    businessSchoolApplication.getStatus(), businessSchoolApplication.getCheckTime(), businessSchoolApplication.getIsDuplicate(),
                    businessSchoolApplication.getDeal(), businessSchoolApplication.getOriginMemberType(), businessSchoolApplication.getSubmitTime(), businessSchoolApplication.getDealTime(), businessSchoolApplication.getComment()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}