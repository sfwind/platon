package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ProblemExtension;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Created by xfduan on 2017/7/5.
 */
public class ProblemExtensionDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * 更新 ProblemExtension 数据
     */
    public Integer insert(ProblemExtension extension) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ProblemExtension " +
                "(Catalog, SubCatalog, Problem, ProblemId, Extension, Recommendation, Online, Offline) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    extension.getCatalog(), extension.getSubCatalog(), extension.getProblem(), extension.getProblemId(),
                    extension.getExtension(), extension.getRecommendation(), extension.getOnline(), extension.getOffline());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return -1;
    }

}
