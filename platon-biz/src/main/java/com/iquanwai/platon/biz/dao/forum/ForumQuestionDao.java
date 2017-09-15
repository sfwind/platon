package com.iquanwai.platon.biz.dao.forum;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.ForumDBUtil;
import com.iquanwai.platon.biz.po.forum.ForumQuestion;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by justin on 17/6/19.
 */
@Repository
public class ForumQuestionDao extends ForumDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(ForumQuestion forumQuestion) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ForumQuestion(Topic, Description, ProfileId, FollowCount, OpenCount, AnswerCount, Weight) " +
                "values(?,?,?,0,0,0,0)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    forumQuestion.getTopic(), forumQuestion.getDescription(), forumQuestion.getProfileId());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public void update(String description, String topic, Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ForumQuestion set Topic=?, Description=?, LastModifiedTime=CURRENT_TIMESTAMP where Id=?";
        try {

            runner.update(sql, topic, description, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void follow(Integer id, Integer point) {
        QueryRunner runner = new QueryRunner(getDataSource());
        if (point == null) {
            point = 0;
        }
        String sql = "update ForumQuestion set FollowCount=FollowCount+1,Weight=Weight+" + point + " where Id=?";
        try {

            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void unfollow(Integer id, Integer point) {
        QueryRunner runner = new QueryRunner(getDataSource());
        if (point == null) {
            point = 0;
        }
        String sql = "update ForumQuestion set FollowCount=FollowCount-1,Weight=Weight-" + point + " where Id=? and FollowCount>0";
        try {

            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void open(Integer id, Integer point) {
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(Executors.newSingleThreadExecutor(), runner);
        if (point == null) {
            point = 0;
        }
        String sql = "update ForumQuestion set OpenCount=OpenCount+1 , Weight=Weight+" + point + " where Id=?";
        try {
            asyncRun.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void answer(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ForumQuestion set AnswerCount=AnswerCount+1 where Id=?";
        try {

            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<ForumQuestion> getQuestionsById(List<Integer> questionIds, Page page) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return Lists.newArrayList();
        }
        String questionMark = produceQuestionMark(questionIds.size());
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<ForumQuestion>> h = new BeanListHandler<>(ForumQuestion.class);
        String sql = "SELECT * FROM ForumQuestion where Id in (" + questionMark + ") " +
                "order by Weight desc, AddTime desc limit " + page.getOffset() + "," + page.getLimit();
        try {
            List<ForumQuestion> forumQuestions = runner.query(sql, h, questionIds.toArray());
            return forumQuestions;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ForumQuestion> getQuestions(Page page) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<ForumQuestion>> h = new BeanListHandler<>(ForumQuestion.class);
//        String sql = "SELECT * FROM ForumQuestion " +
//                "order by Weight desc, AddTime desc limit " + page.getOffset() + "," + page.getLimit();
        String sql = "SELECT * FROM ForumQuestion " +
                "order by Stick desc, AddTime desc limit " + page.getOffset() + "," + page.getLimit();
        try {
            List<ForumQuestion> forumQuestions = runner.query(sql, h);
            return forumQuestions;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ForumQuestion> getQuestions(Integer profileId, Page page) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<ForumQuestion>> h = new BeanListHandler<>(ForumQuestion.class);
        String sql = "SELECT * FROM ForumQuestion where ProfileId = ? " +
                "order by Weight desc, AddTime desc limit " + page.getOffset() + "," + page.getLimit();
        try {
            List<ForumQuestion> forumQuestions = runner.query(sql, h, profileId);
            return forumQuestions;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer getQuestionsCount(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<>();
        String sql = "SELECT COUNT(*) FROM ForumQuestion where ProfileId=?";
        try {
            return runner.query(sql, h, profileId).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
