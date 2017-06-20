package com.iquanwai.platon.biz.dao.forum;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.ForumDBUtil;
import com.iquanwai.platon.biz.po.forum.QuestionTag;
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
public class QuestionTagDao extends ForumDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(QuestionTag questionTag) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into QuestionTag(QuestionId, TagId, Del) " +
                "values(?,?,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    questionTag.getQuestionId(), questionTag.getTagId(), questionTag.getDel());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<QuestionTag> getQuestionTagsByTagId(Integer tagId){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<QuestionTag>> h = new BeanListHandler<>(QuestionTag.class);
        String sql = "SELECT * FROM QuestionTag where TagId=? and Del=0";
        try {
            List<QuestionTag> questionTags = runner.query(sql, h, tagId);
            return questionTags;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer getQuestionTagsCountByQuestionId(Integer tagId){
        QueryRunner runner = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<>();
        String sql = "SELECT COUNT(*) FROM QuestionTag where TagId=? and Del=0";
        try {
            return runner.query(sql, h, tagId).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }


    public List<QuestionTag> getQuestionTagsByQuestionId(Integer questionId){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<QuestionTag>> h = new BeanListHandler<>(QuestionTag.class);
        String sql = "SELECT * FROM QuestionTag where QuestionId=? and Del=0";
        try {
            List<QuestionTag> questionTags = runner.query(sql, h, questionId);
            return questionTags;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer reChooseTag(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<>();
        String sql = "UPDATE QuestionTag set Del = 0 where Id = ?";
        try{
            return runner.update(sql, h, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer deleteQuestionTag(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<>();
        String sql = "UPDATE QuestionTag set Del = 1 where Id = ?";
        try{
            return runner.update(sql, h, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
