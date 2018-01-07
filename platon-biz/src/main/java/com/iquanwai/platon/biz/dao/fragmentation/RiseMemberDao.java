package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.RiseMember;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class RiseMemberDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public RiseMember loadValidRiseMember(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseMember where ProfileId = ? and Expired = 0 AND Del = 0";

        try {
            BeanHandler<RiseMember> handler = new BeanHandler<>(RiseMember.class);
            return runner.query(sql, handler, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<RiseMember> loadRiseMembersByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseMember WHERE ProfileId = ? AND Del = 0";
        ResultSetHandler<List<RiseMember>> h = new BeanListHandler<>(RiseMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    /**
     * 获取所有有效的商学院用户
     * @return
     */
    public List<RiseMember> loadValidRiseMembers(){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseMember WHERE DEL = 0";
        ResultSetHandler<List<RiseMember>> h = new BeanListHandler<>(RiseMember.class);

        try {
            return runner.query(sql,h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return Lists.newArrayList();
    }

}
