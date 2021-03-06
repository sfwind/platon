package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.RiseClassMember;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class RiseClassMemberDao extends PracticeDBUtil {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Deprecated
    public RiseClassMember loadLatestRiseClassMember(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseClassMember WHERE ProfileId = ? AND Del = 0 ORDER BY AddTime DESC";
        ResultSetHandler<RiseClassMember> h = new BeanHandler<>(RiseClassMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
