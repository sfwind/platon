package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ProblemExtension;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by xfduan on 2017/7/5.
 */
@Repository
public class ProblemExtensionDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 更新 ProblemExtension 数据
     */
    public Integer insert(ProblemExtension extension) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ProblemExtension " +
                "(Catalog, SubCatalog, Problem, ProblemId, Extension) values (?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    extension.getCatalog(), extension.getSubCatalog(), extension.getProblem(), extension.getProblemId(),
                    extension.getExtension());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return -1;
    }

    /**
     * 根据 ProblemId 获取课程延伸信息
     */
    public ProblemExtension loadByProblemId(Integer problemId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from ProblemExtension where ProblemId = ? and del = 0";
        try {
            ResultSetHandler<ProblemExtension> h = new BeanHandler<>(ProblemExtension.class);
            return runner.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

}
