package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.Choice;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 16/9/3.
 */
@Repository
public class ChoiceDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<Choice> getQuestionChoices(Integer questionId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Choice>> h = new BeanListHandler<>(Choice.class);
        String sql = "SELECT * FROM Choice where QuestionId = ?";
        try {
            return run.query(sql, h, questionId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
