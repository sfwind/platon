package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.CourseReductionActivity;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/8/16.
 * description: 课程减免活动相关
 */
@Repository
public class CourseReductionActivityDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<CourseReductionActivity> loadReductions(List<String> activities){
        if (CollectionUtils.isEmpty(activities)) {
            return Lists.newArrayList();
        }

        QueryRunner runner = new QueryRunner(getDataSource());
        try{
            String mask = produceQuestionMark(activities.size());
            List<Object> params = Lists.newArrayList();
            params.addAll(activities);

            String sql = "select * from CourseReductionActivity where Activity in (" + mask + ") and Del = 0";
            BeanListHandler<CourseReductionActivity> handler = new BeanListHandler<>(CourseReductionActivity.class);
            return runner.query(sql, handler, activities.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
