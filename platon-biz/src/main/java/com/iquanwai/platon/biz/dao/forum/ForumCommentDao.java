package com.iquanwai.platon.biz.dao.forum;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.ForumDBUtil;
import com.iquanwai.platon.biz.po.forum.ForumComment;
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
public class ForumCommentDao extends ForumDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int comment(ForumComment forumComment) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ForumComment(AnswerId, commentProfileId, comment, Del, " +
                "RepliedId, RepliedProfileId, RepliedDel) " +
                "values (?,?,?,?,?,?,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    forumComment.getAnswerId(), forumComment.getCommentProfileId(), forumComment.getComment(),
                    forumComment.getDel(), forumComment.getRepliedId(),
                    forumComment.getRepliedProfileId(), forumComment.getRepliedDel());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<ForumComment> getComments(Integer answerId){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<ForumComment>> h = new BeanListHandler<>(ForumComment.class);
        String sql = "SELECT * FROM ForumComment where AnswerId=? and Del=0";
        try {
            return runner.query(sql, h, answerId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer deleteComment(Integer commentId){
        QueryRunner runner = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<>();
        String sql = "update ForumComment set Del = 1 where Id = ?";
        try{
            return runner.update(sql, commentId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer updateRepliedDel(Integer commentId){
        QueryRunner runner = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<>();
        String sql = "update ForumComment set RepliedDel = 1 where RepliedId = ?";
        try{
            return runner.update(sql, commentId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
