package com.iquanwai.platon.biz.dao.common;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.RichText;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by 三十文
 */
@Repository
public class RichTextDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public RichText loadByUUID(String uuid) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RichText WHERE uuid = ?";
        try {
            return runner.query(sql, new BeanHandler<>(RichText.class), uuid);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
