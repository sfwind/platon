package com.iquanwai.platon.biz.dao.apply;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplication;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
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

    /**
     * 获得用户的有效申请
     * @param profileId 用户id
     * @return 有效申请列表
     */
    public List<BusinessSchoolApplication> loadApplyList(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM BusinessSchoolApplication WHERE ProfileId = ? AND Del = 0 AND Valid = 1 Order by Id desc";
        try {
            return runner.query(sql, new BeanListHandler<>(BusinessSchoolApplication.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    /**
     * 进行私信，忽略申请
     * @param id 申请id
     * @param comment 备注
     * @return
     */
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



    /**
     * 获得最近一次被审批过的商学院申请
     *
     * @param profileId 用户id
     * @return 获得最新一次被审核的记录
     */
    public BusinessSchoolApplication getLastVerifiedByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = " select * from BusinessSchoolApplication where profileId = ? and status != 0 and del = 0 and Valid = 1  order by UpdateTime desc";

        try {
            return runner.query(sql, new BeanHandler<>(BusinessSchoolApplication.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }
}
