package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ProblemActivity;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by xfduan on 2017/7/5.
 */
@Repository
public class ProblemActivityDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 根据 ProblemId 获取 ProblemActivity 信息
     */
    public List<ProblemActivity> loadProblemActivitiesByProblemId(Integer problemId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from ProblemActivity where problemId = ?";
        try {
            ResultSetHandler<List<ProblemActivity>> h = new BeanListHandler<>(ProblemActivity.class);
            return runner.query(sql, h, problemId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * ProblemActivity 填充数据
     */
    public Integer insertProblemActivity(ProblemActivity activity) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ProblemActivity (ProblemId, Description, Location, WorkShop, URI, Password, Type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), activity.getProblemId(), activity.getDescription(),
                    activity.getLocation(), activity.getWorkshop(), activity.getUri(), activity.getPassword(), activity.getType());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return -1;
    }


}
