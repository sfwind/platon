package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.CommentEvaluation;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by xfduan on 2017/8/2.
 */
@Repository
public class CommentEvaluationDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void initCommentEvaluation(CommentEvaluation evaluation) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO CommentEvaluation (ProfileId, CommentId, TargetId) VALUES (?, ?, ?)";
        try {
            runner.insert(sql, new ScalarHandler<>(), evaluation.getProfileId(), evaluation.getCommentId(), evaluation.getTargetId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
