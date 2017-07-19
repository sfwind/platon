package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.EssenceCard;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class EssenceCardDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 根据 ProblemId 获取 EssenceCard 数据
     */
    public List<EssenceCard> loadEssenceCards(Integer problemId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<EssenceCard>> h = new BeanListHandler<>(EssenceCard.class);
        String sql = "SELECT * FROM EssenceCard WHERE ProblemId = ?";
        try {
            return runner.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return Lists.newArrayList();
    }

    /**
     * 根据 ProblemId 和 KnowledgeId 获取 EssenceCard
     * 如果是小课的总结，则默认的 knowledgeId 为 0
     */
    public EssenceCard loadEssenceCard(Integer problemId, Integer knowledgeId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<EssenceCard> h = new BeanHandler<>(EssenceCard.class);
        String sql = "SELECT * FROM EssenceCard WHERE ProblemId = ? and KnowledgeId = ?";
        try {
            return runner.query(sql, h, problemId, knowledgeId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

}
