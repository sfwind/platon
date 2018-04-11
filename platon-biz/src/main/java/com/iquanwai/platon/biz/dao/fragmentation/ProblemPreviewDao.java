package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ProblemPreview;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 2018/4/11.
 */
@Repository
public class ProblemPreviewDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public ProblemPreview loadProblemPreview(Integer problemScheduleId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ProblemPreview> h = new BeanHandler<>(ProblemPreview.class);
        String sql = "SELECT * FROM ProblemPreview where ProblemScheduleId=? and Del=0";
        try {
            return run.query(sql, h, problemScheduleId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }
}
