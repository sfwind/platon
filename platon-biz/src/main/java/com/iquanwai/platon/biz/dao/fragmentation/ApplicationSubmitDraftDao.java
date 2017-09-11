package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ApplicationSubmitDraft;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by xfduan on 2017/6/5.
 */
@Repository
public class ApplicationSubmitDraftDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * ApplicationSubmitDraft 保存一条新纪录
     */
    public Integer insertSubmitDraft(ApplicationSubmitDraft applicationSubmitDraft) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ApplicationSubmitDraft (Openid, ProfileId, ApplicationId, PlanId, Content, Length)" +
                " values (?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    applicationSubmitDraft.getOpenid(),
                    applicationSubmitDraft.getProfileId(),
                    applicationSubmitDraft.getApplicationId(),
                    applicationSubmitDraft.getPlanId(),
                    applicationSubmitDraft.getContent(),
                    applicationSubmitDraft.getContent() != null ? applicationSubmitDraft.getContent().length() : null
            );
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 根据 id 对草稿进行保存更新
     */
    public Integer updateApplicationSubmitDraft(Integer draftId, String content) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmitDraft set Content = ?, Length = ?, Priority = 0 where id = ?";
        try {
            return runner.update(sql, content, content.length(), draftId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 查询 ApplicationSubmitDraft 记录
     */
    public ApplicationSubmitDraft loadApplicationSubmitDraft(Integer profileId, Integer applicationId, Integer planId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from ApplicationSubmitDraft where ProfileId = ? and ApplicationId = ? and PlanId = ?";
        try {
            return runner.query(sql, new BeanHandler<>(ApplicationSubmitDraft.class), profileId, applicationId, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public ApplicationSubmitDraft loadApplicationSubmitDraft(Integer applicationId, Integer planId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ApplicationSubmitDraft WHERE ApplicationId = ? AND PlanId = ?";
        ResultSetHandler<ApplicationSubmitDraft> h = new BeanHandler<ApplicationSubmitDraft>(ApplicationSubmitDraft.class);
        try {
            return runner.query(sql, h, applicationId, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
