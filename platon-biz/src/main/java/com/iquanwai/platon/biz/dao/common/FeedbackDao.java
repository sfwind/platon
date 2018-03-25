package com.iquanwai.platon.biz.dao.common;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.common.Feedback;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 2018/3/15.
 */
@Repository
public class FeedbackDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public int insert(Feedback feedback) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO Feedback(Words, Contact, PicIds, ProfileId)" +
                " VALUES (?, ?, ?, ?)";

        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    feedback.getWords(), feedback.getContact(), feedback.getPicIds(), feedback.getProfileId());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
