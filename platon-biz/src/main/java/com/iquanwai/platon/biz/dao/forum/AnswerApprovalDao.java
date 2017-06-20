package com.iquanwai.platon.biz.dao.forum;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.ForumDBUtil;
import com.iquanwai.platon.biz.po.forum.AnswerApproval;
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
 * Created by justin on 17/6/19.
 */
@Repository
public class AnswerApprovalDao extends ForumDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 插入回答支持表
     */
    public int insert(AnswerApproval answerApproval) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into AnswerApproval(AnswerId, ProfileId) " +
                "values(?,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    answerApproval.getAnswerId(), answerApproval.getProfileId());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<AnswerApproval> load(Integer answerId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<AnswerApproval>> h = new BeanListHandler<>(AnswerApproval.class);
        String sql = "SELECT * FROM AnswerApproval where AnswerId=? and Del=0";
        try {
            List<AnswerApproval> approvalList = run.query(sql, h, answerId);
            return approvalList;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    /**
     * 加载回答支持数据
     * @param answerId 回答id
     * @param profileId 支持的人
     */
    public AnswerApproval load(Integer answerId,Integer profileId){
        QueryRunner run = new QueryRunner(getDataSource());
        BeanHandler<AnswerApproval> h = new BeanHandler<>(AnswerApproval.class);
        String sql = "SELECT * FROM AnswerApproval where AnswerId=? and Del=0 and ProfileId=?";

        try{
            return run.query(sql, h, answerId, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * 取消对回答的支持
     * @param profileId 进行取消操作的人
     * @param answerId 回答id
     * @return 修改的行数
     */
    public Integer delete(Integer profileId,Integer answerId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE AnswerApproval SET Del = 1 WHERE ProfileId = ? and AnswerId = ?";

        try {
            return runner.update(sql, profileId, answerId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }


}
