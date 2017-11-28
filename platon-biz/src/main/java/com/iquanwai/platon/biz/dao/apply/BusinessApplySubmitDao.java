package com.iquanwai.platon.biz.dao.apply;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.apply.BusinessApplySubmit;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * @author nethunder
 * @version 2017-11-23
 * <p>
 * 商学院申请提交记录
 */
@Repository
public class BusinessApplySubmitDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void batchInsertApplySubmit(List<BusinessApplySubmit> submits) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO BusinessApplySubmit(ApplyId, QuestionId, ChoiceId, ChoiceText, UserValue) " +
                "VALUES (?,?,?,?,?) ";
        try {
            Object[][] param = new Object[submits.size()][];
            for (int i = 0; i < submits.size(); i++) {
                BusinessApplySubmit applySubmit = submits.get(i);
                param[i] = new Object[5];
                param[i][0] = applySubmit.getApplyId();
                param[i][1] = applySubmit.getQuestionId();
                param[i][2] = applySubmit.getChoiceId();
                param[i][3] = applySubmit.getChoiceText();
                param[i][4] = applySubmit.getUserValue();
            }
            runner.batch(sql, param);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
