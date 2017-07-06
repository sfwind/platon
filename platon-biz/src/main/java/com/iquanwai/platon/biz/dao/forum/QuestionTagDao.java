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

    /**
     * 插入问题tag
     * @param questionTag 问题tag pojo
     * @return 插入的id
     */
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

    /**
     * 根据tag获取问题tag列表
     * @param tagId tagId
     * @return 问题tag列表
     */
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

    /**
     * 获取某个tag下的问题数量
     * @param tagId tagId
     * @return 该tag下的问题数量
     */
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


    /**
     * 获取这个问题的未删除tag
     * @param questionId 问题id
     * @return tag列表
     */
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

    /**
     * 获取这个问题的所有tag，包括已删除的
     * @param questionId 问题id
     * @return tag列表
     */
    public List<QuestionTag> getAllQuestionTagsByQuestionId(Integer questionId){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<QuestionTag>> h = new BeanListHandler<>(QuestionTag.class);
        String sql = "SELECT * FROM QuestionTag where QuestionId=?";
        try {
            List<QuestionTag> questionTags = runner.query(sql, h, questionId);
            return questionTags;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    /**
     * 重新选择tag
     * @param id tagId
     * @return 修改行数
     */
    public Integer reChooseTag(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE QuestionTag set Del = 0 where Id = ?";
        try{
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 删除问题tag
     * @param id tagId
     * @return 修改行数
     */
    public Integer deleteQuestionTag(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<>();
        String sql = "UPDATE QuestionTag set Del = 1 where Id = ?";
        try{
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
