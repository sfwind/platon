package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.EssenceCard;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class EssenceCardDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 根据 ProblemId 和 KnowledgeId 获取 EssenceCard
     * 如果是课程的总结，则默认的 knowledgeId 为 0
     */
    public EssenceCard loadEssenceCard(Integer problemId, Integer chapterId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<EssenceCard> h = new BeanHandler<>(EssenceCard.class);
        String sql = "SELECT * FROM EssenceCard WHERE ProblemId = ? and ChapterId = ? And Del=0";
        try {
            return runner.query(sql, h, problemId, chapterId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

}
