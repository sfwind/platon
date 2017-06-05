package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ApplicationSubmit;
import org.apache.commons.dbutils.QueryRunner;
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
    public Integer insertSubmitDraft(ApplicationSubmit applicationSubmit) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ApplicationSubmitDraft (Openid, ProfileId, ApplicationId, PlanId)" +
                " values (?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), applicationSubmit.getOpenid(), applicationSubmit.getProfileId(),
                    applicationSubmit.getApplicationId(), applicationSubmit.getPlanId());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer queryApplicationSubmitDraft(String openId, Integer applicationId, Integer planId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from ApplicationSubmitDraft where openId = ? and applicationId = ? and planId = ?";
        try {
            ApplicationSubmit applicationSubmit = runner.query(sql, new BeanHandler<>(ApplicationSubmit.class), openId, applicationId, planId);
            if (applicationSubmit != null) {
                return applicationSubmit.getId();
            }
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 根据 id 对草稿进行保存更新
     *
     * @param draftId
     * @param content
     * @return
     */
    public Integer updateApplicationSubmitDraft(Integer draftId, String content) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmitDraft set Content = ?, length = ? where id = ?";
        try {
            return runner.update(sql, content, content.length(), draftId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public ApplicationSubmit loadApplicationSubmit(String openId, Integer ApplicationId, Integer planId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from ApplicationSubmitDraft where OpenId = ? and ApplicationId = ? and PlanId = ?";
        try {
            return runner.query(sql, new BeanHandler<>(ApplicationSubmit.class), openId, ApplicationId, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
