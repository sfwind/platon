package com.iquanwai.platon.biz.dao.common;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.common.SMSValidCode;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 17/6/28.
 */
@Repository
public class SMSValidCodeDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public SMSValidCode loadValidCode(Integer profileId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<SMSValidCode> h = new BeanHandler<>(SMSValidCode.class);
        String sql = "SELECT * FROM SMSValidCode where ProfileId=? order by Id desc";
        try {
            SMSValidCode SMSValidCode = run.query(sql, h, profileId);
            return SMSValidCode;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public int insert(SMSValidCode SMSValidCode) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO SMSValidCode(Code, ProfileId, Phone, Type, ExpiredTime)" +
                " VALUES (?, ?, ?, ?, ?)";

        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    SMSValidCode.getCode(), SMSValidCode.getProfileId(), SMSValidCode.getPhone(),
                    SMSValidCode.getType(), SMSValidCode.getExpiredTime());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
