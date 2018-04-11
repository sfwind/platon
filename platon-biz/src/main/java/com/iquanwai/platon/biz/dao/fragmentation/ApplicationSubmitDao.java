package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ApplicationSubmit;
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
 * Created by justin on 17/2/15.
 */
@Repository
public class ApplicationSubmitDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(ApplicationSubmit applicationSubmit) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ApplicationSubmit(ProfileId, ApplicationId, PlanId, ProblemId) " +
                "values(?,?,?,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    applicationSubmit.getProfileId(),
                    applicationSubmit.getApplicationId(),
                    applicationSubmit.getPlanId(), applicationSubmit.getProblemId());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 查询用户提交记录
     * @param applicationId 应用练习id
     * @param planId 计划id
     */
    public ApplicationSubmit load(Integer applicationId, Integer planId, Integer profileId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ApplicationSubmit> h = new BeanHandler<>(ApplicationSubmit.class);
        String sql = "SELECT * FROM ApplicationSubmit where ProfileId=? and ApplicationId=? and PlanId=? and Del=0";
        try {
            return run.query(sql, h, profileId, applicationId, planId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public ApplicationSubmit load(Integer applicationId, Integer profileId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ApplicationSubmit> h = new BeanHandler<>(ApplicationSubmit.class);
        String sql = "SELECT * FROM ApplicationSubmit where ProfileId=? and ApplicationId=? and Del=0";
        try {
            return run.query(sql, h, profileId, applicationId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public boolean firstAnswer(Integer id, String content, int length, boolean hasImage) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Content=?, Length=?, hasImage=?, PublishTime = CURRENT_TIMESTAMP, LastModifiedTime = CURRENT_TIMESTAMP where Id=?";
        try {
            runner.update(sql, content, length, id, hasImage);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public boolean answer(Integer id, String content, int length, boolean hasImage) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Content=?, Length=?, hasImage=?, LastModifiedTime = CURRENT_TIMESTAMP where Id=?";
        try {
            runner.update(sql, content, length, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public boolean updatePointStatus(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set PointStatus=1 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public void asstFeedback(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Feedback=1 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void asstFeedBackAndTime(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Feedback=1,FeedBackTime=CURRENT_TIMESTAMP where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<ApplicationSubmit> loadSubmits(Integer applicationId, Page page) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        String sql = "SELECT * FROM ApplicationSubmit where ApplicationId=? and Length>=15 and Del=0 "
                + "order by Priority desc, Feedback desc, PublishTime desc limit " + page.getOffset() + "," + page.getLimit();
        try {
            List<ApplicationSubmit> submits = run.query(sql, h, applicationId);
            return submits;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public int count(Integer applicationId) {
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "SELECT count(*) FROM ApplicationSubmit where ApplicationId=? and Length>=15 and Del=0";
        try {
            Long result = run.query(sql, new ScalarHandler<>(), applicationId);
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return 0;
    }

    public void requestComment(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set RequestFeedback=1,RequestTime= CURRENT_TIMESTAMP where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateContent(Integer id, String content) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update ApplicationSubmit set Content=? where Id=?";
        try {
            runner.update(sql, content, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<ApplicationSubmit> loadBatchApplicationSubmits(Integer problemId, List<Integer> refers) {
        if (CollectionUtils.isEmpty(refers)) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String mask = produceQuestionMark(refers.size());
        List<Object> params = Lists.newArrayList();
        params.add(problemId);
        params.addAll(refers);
        String sql = "select Id, ProfileId, ApplicationId, PlanId, ProblemId, PointStatus, PublishTime, LastModifiedTime, " +
                "Priority, HighlightTime, RequestFeedback, Feedback, Length, Del, AddTime, UpdateTime " +
                "from ApplicationSubmit where ProblemId = ? and Id in (" + mask + ") and Del=0";

        try {
            return runner.query(sql, new BeanListHandler<>(ApplicationSubmit.class), params.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<ApplicationSubmit> loadApplicationSubmitsByApplicationIds(List<Integer> applicationIds, Integer planId) {
        if (applicationIds.size() == 0) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ApplicationSubmit WHERE ApplicationId in (" + produceQuestionMark(applicationIds.size())
                + ") AND PlanId = ? AND Del = 0";
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        List<Object> objects = Lists.newArrayList();
        objects.addAll(applicationIds);
        objects.add(planId);
        try {
            return runner.query(sql, h, objects.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<ApplicationSubmit> loadPriorityApplicationSubmitsByApplicationId(Integer applicationId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM ApplicationSubmit WHERE ApplicationId = ? AND Priority = 1 AND Del = 0";
        ResultSetHandler<List<ApplicationSubmit>> h = new BeanListHandler<>(ApplicationSubmit.class);
        try {
            return runner.query(sql, h, applicationId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
