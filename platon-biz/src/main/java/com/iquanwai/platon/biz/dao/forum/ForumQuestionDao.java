package com.iquanwai.platon.biz.dao.forum;

import com.iquanwai.platon.biz.dao.ForumDBUtil;
import com.iquanwai.platon.biz.po.forum.ForumQuestion;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 17/6/19.
 */
@Repository
public class ForumQuestionDao extends ForumDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(ForumQuestion forumQuestion) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ForumQuestion(Topic, Description, ProfileId, FollowCount, OpenCount, Weight) " +
                "values(?,?,?,0,0,0)";
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

    public void follow(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ForumQuestion set FollowCount=FollowCount+1 where Id=?";
        try {

            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void open(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ForumQuestion set OpenCount=OpenCount+1 where Id=?";
        try {

            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
