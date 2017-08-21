package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.RiseMember;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/4/13.
 */
@Repository
public class RiseMemberDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public RiseMember validRiseMember(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseMember where ProfileId = ? and expired = 0";

        try {
            BeanHandler<RiseMember> handler = new BeanHandler<>(RiseMember.class);
            return runner.query(sql, handler, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
