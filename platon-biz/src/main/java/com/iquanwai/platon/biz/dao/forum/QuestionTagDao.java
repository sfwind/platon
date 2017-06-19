package com.iquanwai.platon.biz.dao.forum;

import com.iquanwai.platon.biz.dao.ForumDBUtil;
import com.iquanwai.platon.biz.po.forum.QuestionTag;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 17/6/19.
 */
@Repository
public class QuestionTagDao extends ForumDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(QuestionTag questionTag) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into QuestionTag(QuestionId, TagId, Del) " +
                "values(?,?,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    questionTag.getQuestionId(), questionTag.getTagId(), questionTag.getDel());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
