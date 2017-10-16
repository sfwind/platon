package com.iquanwai.platon.biz.dao.interlocution;

import com.iquanwai.platon.biz.dao.ForumDBUtil;
import com.iquanwai.platon.biz.po.interlocution.InterlocutionAnswer;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;

/**
 * Created by justin on 17/6/19.
 */
@Repository
public class InterlocutionAnswerDao extends ForumDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public InterlocutionAnswer load(Date date) {
        QueryRunner run = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM InterlocutionAnswer where InterlocutionDate = ?";
        try {
            return run.query(sql, new BeanHandler<InterlocutionAnswer>(InterlocutionAnswer.class), DateUtils.parseDateToString(date));
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
