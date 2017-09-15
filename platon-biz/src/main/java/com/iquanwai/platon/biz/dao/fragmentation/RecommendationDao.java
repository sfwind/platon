package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.Recommendation;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class RecommendationDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 根据 ProblemId 获取所有相关推荐小课
     */
    public List<Recommendation> loadRecommendationByProblemId(Integer problemId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from Recommendation where ProblemId = ? and Del = 0 order by sequence";
        ResultSetHandler<List<Recommendation>> h = new BeanListHandler<>(Recommendation.class);
        try {
            return runner.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return Lists.newArrayList();
    }

}
