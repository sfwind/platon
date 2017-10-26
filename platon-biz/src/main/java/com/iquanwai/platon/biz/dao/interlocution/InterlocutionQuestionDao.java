package com.iquanwai.platon.biz.dao.interlocution;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.interlocution.InterlocutionQuestion;
import com.iquanwai.platon.biz.util.ThreadPool;
import com.iquanwai.platon.biz.util.page.Page;
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

@Repository
public class InterlocutionQuestionDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    public int insert(InterlocutionQuestion interlocutionQuestion) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into InterlocutionQuestion(Topic, ProfileId, FollowCount, OpenCount, AnswerCount, Weight, InterlocutionDate) " +
                "values(?,?,0,0,0,0,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    interlocutionQuestion.getTopic(), interlocutionQuestion.getProfileId(), interlocutionQuestion.getInterlocutionDate());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public void update(String topic, Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update InterlocutionQuestion set Topic=?, LastModifiedTime=CURRENT_TIMESTAMP where Id=? AND Del = 0";
        try {

            runner.update(sql, topic, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void follow(Integer id, Integer point) {
        QueryRunner runner = new QueryRunner(getDataSource());
        if (point == null) {
            point = 0;
        }
        String sql = "update InterlocutionQuestion set FollowCount=FollowCount+1,Weight=Weight+" + point + " where Id=? AND Del = 0";
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
        String sql = "update InterlocutionQuestion set FollowCount=FollowCount-1,Weight=Weight-" + point + " where Id=? and FollowCount>0 AND Del = 0";
        try {

            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void open(Integer id, Integer point) {
        QueryRunner runner = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(ThreadPool.createSingleThreadExecutor(), runner);
        if (point == null) {
            point = 0;
        }
        String sql = "update InterlocutionQuestion set OpenCount=OpenCount+1 , Weight=Weight+" + point + " where Id=? AND Del = 0";
        try {
            asyncRun.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<InterlocutionQuestion> getQuestions(String date, Page page) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<InterlocutionQuestion>> h = new BeanListHandler<>(InterlocutionQuestion.class);
        String sql = "SELECT * FROM InterlocutionQuestion WHERE InterlocutionDate = ? and Del = 0 " +
                "order by Stick desc, AddTime desc limit " + page.getOffset() + "," + page.getLimit();
        try {
            return runner.query(sql, h, date);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer count(String date) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select count(*) from InterlocutionQuestion WHERE InterlocutionDate = ? AND Del = 0";
        try {
            return runner.query(sql, new ScalarHandler<Long>(), date).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
