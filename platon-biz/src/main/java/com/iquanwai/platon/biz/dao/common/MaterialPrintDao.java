package com.iquanwai.platon.biz.dao.common;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.MaterialPrint;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by 三十文
 */
@Repository
public class MaterialPrintDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<MaterialPrint> loadAll() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM MaterialPrint WHERE Del = 0";
        ResultSetHandler<List<MaterialPrint>> h = new BeanListHandler<>(MaterialPrint.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
