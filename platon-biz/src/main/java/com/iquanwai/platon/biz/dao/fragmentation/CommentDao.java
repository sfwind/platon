package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.Comment;
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
 * Created by nethunder on 2017/1/20.
 */
@Repository
public class CommentDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void insert(Comment comment) {
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "insert into Comment(ModuleId, Type, ReferencedId, CommentOpenId, Content, Device) " +
                "VALUES (?,?,?,?,?,?)";
        try {
            run.insert(insertSql, new ScalarHandler<>(),
                    comment.getModuleId(),comment.getType(), comment.getReferencedId(), comment.getCommentOpenId(), comment.getContent(),comment.getDevice());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<Comment> loadComments(Integer moduleId, Integer referId, Page page) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Comment>> h = new BeanListHandler<>(Comment.class);
        String sql = "SELECT * FROM Comment where ReferencedId = ? and ModuleId = ?  and Del = 0 order by Type desc, AddTime desc limit " + page.getOffset() + "," + page.getLimit();
        try {
            return run.query(sql, h, referId,moduleId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer commentCount(Integer moduleId,Integer referId){
        QueryRunner run = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<Long>();

        try {
            Long count = run.query("SELECT count(*) FROM Comment where ReferencedId=? and ModuleId=? and Del=0",
                    h,referId, moduleId);
            return count.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return 0;
    }

    public void deleteComment(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update Comment set Del=1 where Id=?";
        try {

            runner.update(sql, id);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
