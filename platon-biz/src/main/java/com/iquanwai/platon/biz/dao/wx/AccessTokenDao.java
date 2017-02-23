package com.iquanwai.platon.biz.dao.wx;

import com.iquanwai.platon.biz.dao.DBUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 17/2/22.
 */
@Repository
public class AccessTokenDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());


    public void insertOrUpdate(String accessToken){
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "INSERT INTO AccessToken (Id,AccessToken) VALUES (1,?) ON DUPLICATE KEY UPDATE AccessToken=?";

        try {
            run.update(insertSql, accessToken, accessToken);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
