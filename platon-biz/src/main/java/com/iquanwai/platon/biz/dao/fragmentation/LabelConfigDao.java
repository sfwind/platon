package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.LabelConfig;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/3/10.
 */
@Repository
public class LabelConfigDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<LabelConfig> loadLabelConfigs(Integer problemId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from LabelConfig where ProblemId = ? and Del = 0";
        try{
            ResultSetHandler<List<LabelConfig>> h = new BeanListHandler<>(LabelConfig.class);
            return runner.query(sql,h,problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }


}
