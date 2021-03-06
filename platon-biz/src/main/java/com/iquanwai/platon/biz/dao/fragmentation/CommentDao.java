package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.Comment;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/1/20.
 */
@Repository
public class CommentDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public int insert(Comment comment) {
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "insert into Comment(ModuleId, Type, ReferencedId, CommentProfileId, Content, " +
                "RepliedId, RepliedProfileId, RepliedComment, RepliedDel, Device) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try {
            Long id = run.insert(insertSql, new ScalarHandler<>(),
                    comment.getModuleId(), comment.getType(), comment.getReferencedId(),
                    comment.getCommentProfileId(), comment.getContent(),
                    comment.getRepliedId(), comment.getRepliedProfileId(),
                    comment.getRepliedComment(), comment.getRepliedDel(), comment.getDevice());

            return id.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    public List<Comment> loadApplicationComments(Integer referId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Comment>> h = new BeanListHandler<>(Comment.class);
        String sql = "SELECT * FROM Comment where ReferencedId = ? and ModuleId = 2 and Del = 0 " +
                "order by Type desc, AddTime desc";
        try {
            return run.query(sql, h, referId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<Comment> loadComments(Integer referId, Page page) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Comment>> h = new BeanListHandler<>(Comment.class);
        String sql = "SELECT * FROM Comment where ReferencedId = ? and ModuleId = 2  and Del = 0 " +
                "order by Type desc, AddTime desc limit " + page.getOffset() + "," + page.getLimit();
        try {
            return run.query(sql, h, referId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<Comment> loadCommentsByProfileId(Integer referId, Integer commentProfileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM Comment WHERE ReferencedId = ? AND CommentProfileId = ? and ModuleId = 2 AND Del = 0";
        ResultSetHandler<List<Comment>> h = new BeanListHandler<>(Comment.class);
        try {
            return runner.query(sql, h, referId, commentProfileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer commentCount(Integer referId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<>();

        try {
            Long count = run.query("SELECT count(*) FROM Comment where ReferencedId=? and ModuleId=2 and Del=0",
                    h, referId);
            return count.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return 0;
    }

    public void deleteComment(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update Comment set Del=1 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public Comment loadByCommentId(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM Comment WHERE Id = ?";
        ResultSetHandler<Comment> h = new BeanHandler<>(Comment.class);
        try {
            return runner.query(sql, h, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<Comment> loadAllCommentsByReferenceIds(List<Integer> referencedIds) {
        if (CollectionUtils.isEmpty(referencedIds)) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<Comment>> h = new BeanListHandler<>(Comment.class);
        String sql = "select * from Comment where ReferencedId in (" + produceQuestionMark(referencedIds.size()) + ") " +
                "and ModuleId = 2 and Del=0";
        try {
            return runner.query(sql, h, referencedIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<Comment> loadAllCommentsByIds(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM Comment WHERE Id in (" + produceQuestionMark(ids.size()) + ") and Del=0";
        ResultSetHandler<List<Comment>> h = new BeanListHandler<>(Comment.class);
        try {
            return runner.query(sql, h, ids.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
