package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.AuditionClassMember;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;

/**
 * @author nethunder
 * @version 2017-11-01
 */
@Repository
public class AuditionClassMemberDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public AuditionClassMember loadByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from AuditionClassMember where ProfileId = ? and Del = 0 ";
        try {
            return runner.query(sql, new BeanHandler<>(AuditionClassMember.class), profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public Integer insert(AuditionClassMember member) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO AuditionClassMember(ProfileId,Openid,ClassName,StartDate) VALUES(?,?,?,?)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), member.getProfileId(), member.getOpenid(), member.getClassName(),
                    member.getStartDate()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer update(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE AuditionClassMember SET Active = 0 WHERE Id = ?";
        try {
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer updateAuditionClass(Integer id, Date monday) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE AuditionClassMember SET StartDate = ? WHERE Id = ?";
        try {
            return runner.update(sql, monday, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
