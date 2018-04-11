package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.KnowledgeDiscuss;
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
 * Created by nethunder on 2017/5/3.
 */
@Repository
public class KnowledgeDiscussDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public int insert(KnowledgeDiscuss discuss) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into KnowledgeDiscuss (" +
                "KnowledgeId, Comment, ProfileId, Priority, RepliedId, RepliedProfileId, RepliedComment, Del) VALUES (" +
                "?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    discuss.getKnowledgeId(), discuss.getComment(),
                    discuss.getProfileId(), discuss.getPriority(), discuss.getRepliedId(),
                    discuss.getRepliedProfileId(), discuss.getRepliedComment(), discuss.getDel());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    public List<KnowledgeDiscuss> loadDiscussesByProfileId(Integer profileId, Integer knowledgeId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM KnowledgeDiscuss WHERE ProfileId = ? AND KnowledgeId = ? AND Del = 0";
        ResultSetHandler<List<KnowledgeDiscuss>> h = new BeanListHandler<KnowledgeDiscuss>(KnowledgeDiscuss.class);
        try {
            return runner.query(sql, h, profileId, knowledgeId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<KnowledgeDiscuss> loadDiscuss(Integer practiceId, Page page) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<KnowledgeDiscuss>> h = new BeanListHandler<>(KnowledgeDiscuss.class);
        String sql = "SELECT * FROM KnowledgeDiscuss where knowledgeId = ? and Del = 0 " +
                "order by Priority desc, AddTime desc limit " + page.getOffset() + "," + page.getLimit();
        try {
            return run.query(sql, h, practiceId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<KnowledgeDiscuss> loadPriorityKnowledgeDiscuss(Integer practiceId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<KnowledgeDiscuss>> h = new BeanListHandler<>(KnowledgeDiscuss.class);
        String sql = "SELECT * FROM KnowledgeDiscuss WHERE KnowledgeId = ? AND Priority = 1 and Del = 0";
        try {
            return runner.query(sql, h, practiceId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<KnowledgeDiscuss> loadKnowledgeDiscussByIds(List<Integer> ids) {
        if (ids.size() == 0) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM KnowledgeDiscuss WHERE Id IN (" + produceQuestionMark(ids.size()) + " ) AND Del = 0";
        ResultSetHandler<List<KnowledgeDiscuss>> h = new BeanListHandler<>(KnowledgeDiscuss.class);
        try {
            return runner.query(sql, h, ids.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<KnowledgeDiscuss> loadKnowledgeDiscussByRepliedIds(List<Integer> repliedIds) {
        if (repliedIds.size() == 0) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM KnowledgeDiscuss WHERE RepliedId in (" + produceQuestionMark(repliedIds.size()) + ") AND Del = 0";
        ResultSetHandler<List<KnowledgeDiscuss>> h = new BeanListHandler<>(KnowledgeDiscuss.class);
        try {
            return runner.query(sql, h, repliedIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    /**
     * 根据id更新该条记录的del字段
     */
    public int updateDelById(Integer delValue, Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update KnowledgeDiscuss set del = ? where id = ?";
        try {
            return runner.update(sql, delValue, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public void markRepliedCommentDelete(Integer repliedId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update KnowledgeDiscuss set RepliedDel = 1 where RepliedId = ?";
        try {
            runner.update(sql, repliedId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
