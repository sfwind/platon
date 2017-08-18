package com.iquanwai.platon.biz.dao.forum;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.ForumDBUtil;
import com.iquanwai.platon.biz.po.forum.ForumAnswer;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 17/6/19.
 */
@Repository
public class ForumAnswerDao extends ForumDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(ForumAnswer forumAnswer) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ForumAnswer(Answer, QuestionId, ProfileId, ApprovalCount, PublishTime, LastModifiedTime) " +
                "values(?,?,?,?,now(),now())";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    forumAnswer.getAnswer(), forumAnswer.getQuestionId(), forumAnswer.getProfileId(),
                    forumAnswer.getApprovalCount());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public void update(String content, Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ForumAnswer set Answer=?, LastModifiedTime=CURRENT_TIMESTAMP where Id=?";
        try {

            runner.update(sql, content, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void approve(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ForumAnswer set ApprovalCount=ApprovalCount+1 where Id=?";
        try {

            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void cancelApprove(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ForumAnswer set ApprovalCount=ApprovalCount-1 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<ForumAnswer> load(Integer questionId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ForumAnswer>> h = new BeanListHandler<>(ForumAnswer.class);
        String sql = "SELECT * FROM ForumAnswer where QuestionId=? order by ApprovalCount desc, PublishTime desc";
        try {
            List<ForumAnswer> answerList = run.query(sql, h, questionId);
            return answerList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ForumAnswer> loadUserAnswers(Integer profileId, Page page) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ForumAnswer>> h = new BeanListHandler<>(ForumAnswer.class);
        String sql = "SELECT * FROM ForumAnswer where ProfileId=? order by ApprovalCount desc, PublishTime desc limit  " + page.getOffset() + "," + page.getLimit();
        try {
            List<ForumAnswer> answerList = run.query(sql, h, profileId);
            return answerList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer loadUserAnswersCount(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<>();
        String sql = "SELECT COUNT(*) FROM ForumAnswer where ProfileId=?";
        try {
            return runner.query(sql, h, profileId).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<ForumAnswer> loadUserQuestionAnswers(Integer questionId, Integer profileId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ForumAnswer>> h = new BeanListHandler<>(ForumAnswer.class);
        String sql = "select * from ForumAnswer where QuestionId = ? and ProfileId = ?";
        try {
            List<ForumAnswer> answers = run.query(sql, h, questionId, profileId);
            return answers;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
