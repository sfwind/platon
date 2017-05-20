package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ApplicationSubmit;
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
 * Created by justin on 17/2/15.
 */
@Repository
public class ApplicationSubmitDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(ApplicationSubmit applicationSubmit){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ApplicationSubmit(Openid, ApplicationId, PlanId, ProblemId) " +
                "values(?,?,?,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    applicationSubmit.getOpenid(), applicationSubmit.getApplicationId(),
                    applicationSubmit.getPlanId(), applicationSubmit.getProblemId());
            return insertRs.intValue();
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 查询用户提交记录
     * @param applicationId 应用练习id
     * @param planId 计划id
     * @param openid  openid
     */
    public ApplicationSubmit load(Integer applicationId, Integer planId, String openid){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ApplicationSubmit> h = new BeanHandler<>(ApplicationSubmit.class);
        String sql = "SELECT * FROM ApplicationSubmit where Openid=? and ApplicationId=? and PlanId=?";
        try {
            return run.query(sql, h, openid, applicationId, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public ApplicationSubmit load(Integer applicationId, String openid){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ApplicationSubmit> h = new BeanHandler<>(ApplicationSubmit.class);
        String sql = "SELECT * FROM ApplicationSubmit where Openid=? and ApplicationId=?";
        try {
            return run.query(sql, h, openid, applicationId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public boolean firstAnswer(Integer id, String content, int length){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Content=?, Length=?, PublishTime = CURRENT_TIMESTAMP where Id=?";
        try {
            runner.update(sql, content, length, id);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public boolean answer(Integer id, String content, int length){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Content=?, Length=? where Id=?";
        try {
            runner.update(sql, content, length, id);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public boolean updatePointStatus(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set PointStatus=1 where Id=?";
        try {
            runner.update(sql, id);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public void asstFeedback(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Feedback=1 where Id=?";
        try {
            runner.update(sql, id);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<ApplicationSubmit> load(Integer applicationId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        // TODO: 写死了大小
        String sql = "SELECT * FROM ApplicationSubmit where ApplicationId=? and Length>=15 order by UpdateTime desc limit 50";
        try {
            return run.query(sql, h, applicationId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public void requestComment(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set RequestFeedback=1 where Id=?";
        try {
            runner.update(sql, id);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateContent(Integer id, String content){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Content=? where Id=?";
        try {
            runner.update(sql, content, id);
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
