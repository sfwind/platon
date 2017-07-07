package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 16/12/4.
 */
@Repository
public class ProblemDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 根据 ProblemId 插入 Recommendation
     */
    public Integer insertRecommendationById(Integer problemId, String recommendation) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update Problem set Recommendation = ? where id = ?";
        try {
            Integer result = runner.update(sql, recommendation, problemId);
            return result;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return -1;
    }

}
