package com.iquanwai.platon.biz.dao.survey;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.survey.SurveyQuestionSubmit;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * @author nethunder
 * 问卷题目详情
 */
@Repository
public class SurveyQuestionSubmitDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 批量添加
     *
     * @param submits 题目答案
     */
    public Integer batchInsert(List<SurveyQuestionSubmit> submits) {
        if (CollectionUtils.isEmpty(submits)) {
            return -1;
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO SurveyQuestionSubmit(Category, QuestionCode, ChoiceId, ChoiceIds, UserValue,SubmitId) VALUES(?,?,?,?,?,?)";
        try {
            Object[][] param = new Object[submits.size()][];
            for (int i = 0; i < submits.size(); i++) {
                SurveyQuestionSubmit submit = submits.get(i);
                param[i] = new Object[6];
                param[i][0] = submit.getCategory();
                param[i][1] = submit.getQuestionCode();
                param[i][2] = submit.getChoiceId();
                param[i][3] = submit.getChoiceIds();
                param[i][4] = submit.getUserValue();
                param[i][5] = submit.getSubmitId();
            }
            runner.batch(sql, param);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return -1;
        }
        return 1;
    }
}
