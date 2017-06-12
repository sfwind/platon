package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.AsstCoachComment;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/1/13.
 */
@Repository
public class AsstCoachCommentDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public AsstCoachComment loadAsstCoachComment(Integer problemId, Integer profileId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<AsstCoachComment> h = new BeanHandler<>(AsstCoachComment.class);
        String sql = "SELECT * FROM AsstCoachComment where ProblemId=? and ProfileId=?";
        try {
            return run.query(sql, h, problemId, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public void insert(AsstCoachComment asstCoachComment){
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "insert into AsstCoachComment(Openid, ProfileId, Count, ProblemId) " +
                "VALUES (?,?,?,?)";
        try {
            run.insert(insertSql, new ScalarHandler<>(),
                    asstCoachComment.getOpenid(), asstCoachComment.getProfileId(),
                    asstCoachComment.getCount(), asstCoachComment.getProblemId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateCount(AsstCoachComment asstCoachComment){
        QueryRunner run = new QueryRunner(getDataSource());
        String updateSql = "Update AsstCoachComment set Count=? where Id=?";
        try {
            run.update(updateSql, asstCoachComment.getCount(), asstCoachComment.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
