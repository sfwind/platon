package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.WarmupPracticeDiscuss;
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
 * Created by justin on 17/2/8.
 */
@Repository
public class WarmupPracticeDiscussDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(WarmupPracticeDiscuss discuss){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into WarmupPracticeDiscuss(WarmupPracticeId, Openid, Profileid, RepliedId, Comment, " +
                "Priority, Del, RepliedOpenid, RepliedProfileid, RepliedComment, OriginDiscussId) " +
                "values(?,?,?,?,?,?,?,?,?,?,?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    discuss.getWarmupPracticeId(), discuss.getOpenid(), discuss.getProfileId(),
                    discuss.getRepliedId(), discuss.getComment(), discuss.getPriority(), discuss.getDel(),
                    discuss.getRepliedOpenid(), discuss.getRepliedProfileId(),
                    discuss.getRepliedComment(), discuss.getOriginDiscussId());

            return result.intValue();
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    public List<WarmupPracticeDiscuss> loadDiscuss(Integer practiceId, Page page) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<WarmupPracticeDiscuss>> h = new BeanListHandler<>(WarmupPracticeDiscuss.class);
        String sql = "SELECT * FROM WarmupPracticeDiscuss where WarmupPracticeId = ? and Del = 0 " +
                "order by Priority desc, AddTime desc limit " + page.getOffset() + "," + page.getLimit();
        try {
            return run.query(sql, h, practiceId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public void deleteComment(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update WarmupPracticeDiscuss set Del = 1 where Id = ?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void markRepliedCommentDelete(Integer repliedId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update WarmupPracticeDiscuss set RepliedDel = 1 where RepliedId = ?";
        try {
            runner.update(sql, repliedId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public void updateOriginDiscussId(Integer id, Integer originDiscussId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update WarmupPracticeDiscuss set OriginDiscussId = ? where Id = ?";
        try {
            runner.update(sql, originDiscussId, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
