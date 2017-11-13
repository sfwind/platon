package com.iquanwai.platon.biz.dao.fragmentation.schedule;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.schedule.ScheduleChoiceSubmit;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * @author nethunder
 * @version 2017-11-04
 */
@Repository
public class ScheduleChoiceSubmitDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void batchInsert(List<ScheduleChoiceSubmit> choiceSubmitList) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO ScheduleChoiceSubmit(ProfileId, ChoiceId) VALUES(?,?)";
        try {
            Object[][] param = new Object[choiceSubmitList.size()][];
            for (int i = 0; i < choiceSubmitList.size(); i++) {
                ScheduleChoiceSubmit courseSchedule = choiceSubmitList.get(i);
                param[i] = new Object[2];
                param[i][0] = courseSchedule.getProfileId();
                param[i][1] = courseSchedule.getChoiceId();
            }
            runner.batch(sql, param);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
