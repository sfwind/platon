package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.HomeworkVote;
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
 * Created by nethunder on 2017/1/2.
 */
@Repository
public class HomeworkVoteDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 被点赞的次数
     */
    public int votedCount(Integer referencedId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<Long>();
        try {
            Long number = run.query("select count(1) from HomeworkVote where referencedId=? and Type=2 and Del=0", h, referencedId);
            return number.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 进行点赞
     *
     * @return 插入结果
     */
    public void vote(HomeworkVote homeworkVote) {
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "INSERT INTO HomeworkVote(Type,ReferencedId,VoteProfileId,VotedProfileId,Device) " +
                "VALUES(?, ?, ?, ?, ?)";
        try {
            run.insert(insertSql, new ScalarHandler<>(), homeworkVote.getType(), homeworkVote.getReferencedId(),
                    homeworkVote.getVoteProfileId(), homeworkVote.getVotedProfileId(), homeworkVote.getDevice());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * voteProfileId对某条记录的点赞记录
     *
     * @param referencedId  被依赖的id
     * @param voteProfileId 点赞的人
     * @return
     */
    public HomeworkVote loadVoteRecord(Integer referencedId, Integer voteProfileId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<HomeworkVote> h = new BeanHandler<>(HomeworkVote.class);
        try {
            HomeworkVote vote = run.query("SELECT * FROM HomeworkVote where ReferencedId=? and VoteProfileId=? and Type=2 AND del=0 ",
                    h, referencedId, voteProfileId);
            return vote;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * 重新点赞
     *
     * @param id 点赞的id
     */
    public void reVote(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update HomeworkVote set Del=0 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }


    public List<HomeworkVote> getHomeworkVotesByReferenceIds(List<Integer> referencedIds) {
        if (CollectionUtils.isEmpty(referencedIds)) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<HomeworkVote>> h = new BeanListHandler<>(HomeworkVote.class);
        String questionMark = produceQuestionMark(referencedIds.size());
        String sql = "select * from HomeworkVote where ReferencedId in (" + questionMark + ") AND Type=2 AND DEL=0";
        try {
            return runner.query(sql, h, referencedIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<HomeworkVote> votedList(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from HomeworkVote where VotedProfileId = ? AND Type=2 AND DEL=0";
        try {
            return runner.query(sql, new BeanListHandler<>(HomeworkVote.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }


    public List<HomeworkVote> voteList(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from HomeworkVote where VoteProfileId = ? AND Type=2 AND DEL=0";
        try {
            return runner.query(sql, new BeanListHandler<>(HomeworkVote.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
