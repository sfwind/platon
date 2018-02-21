package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ProblemScore;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/3/23.
 */
@Repository
public class ProblemScoreDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void gradeProblem(List<ProblemScore> problemScores) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO ProblemScore(ProfileId, ProblemId, Question, Choice, Comment) VALUES (?,?,?,?,?)";
        try {
            Object[][] param = new Object[problemScores.size()][];
            for (int i = 0; i < problemScores.size(); i++) {
                ProblemScore problemScore = problemScores.get(i);
                param[i] = new Object[5];
                param[i][0] = problemScore.getProfileId();
                param[i][1] = problemScore.getProblemId();
                param[i][2] = problemScore.getQuestion();
                param[i][3] = problemScore.getChoice();
                param[i][4] = problemScore.getComment();
            }
            runner.batch(sql, param);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public int userProblemScoreCount(Integer profileId, Integer problem) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select count(*) from ProblemScore where ProfileId = ? and ProblemId = ?";
        try {
            ResultSetHandler<Long> h = new ScalarHandler<>();
            return runner.query(sql, h, profileId, problem).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
