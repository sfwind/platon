package com.iquanwai.platon.biz.dao.wx;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.Region;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 16/11/2.
 */
@Repository
public class RegionDao extends DBUtil{
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<Region> loadAllProvinces() {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Region>> h = new BeanListHandler(Region.class);

        try {
            List<Region> provinces = run.query("SELECT * FROM Region where Type=20", h);
            return provinces;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public List<Region> loadAllCities() {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Region>> h = new BeanListHandler(Region.class);

        try {
            List<Region> regions = run.query("SELECT * FROM Region where Type=30", h);
            return regions;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
}
