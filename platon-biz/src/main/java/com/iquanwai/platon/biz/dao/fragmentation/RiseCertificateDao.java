package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.RiseCertificate;
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
 * Created by justin on 17/8/29.
 */
@Repository
public class RiseCertificateDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(RiseCertificate riseCertificate) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO RiseCertificate (ProfileId, Type, CertificateNo, Year, Month, GroupNo, ProblemName) VALUES " +
                "( ?, ?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    riseCertificate.getProfileId(),
                    riseCertificate.getType(),
                    riseCertificate.getCertificateNo(),
                    riseCertificate.getYear(),
                    riseCertificate.getMonth(),
                    riseCertificate.getGroupNo(),
                    riseCertificate.getProblemName());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 根据 ProblemId 获取所有相关推荐小课
     */
    public RiseCertificate loadByCertificateNo(String certificateNo) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseCertificate where CertificateNo = ?";
        ResultSetHandler<RiseCertificate> h = new BeanHandler<>(RiseCertificate.class);
        try {
            return runner.query(sql, h, certificateNo);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

    public List<RiseCertificate> loadUnNotifiedByMonthAndYear(Integer year, Integer month) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseCertificate where Year = ? and Month = ? and Notified=0 and Del=0";
        ResultSetHandler<List<RiseCertificate>> h = new BeanListHandler<>(RiseCertificate.class);
        try {
            return runner.query(sql, h, year, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return Lists.newArrayList();
    }

    public List<RiseCertificate> loadByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseCertificate where ProfileId = ? and Del=0";
        ResultSetHandler<List<RiseCertificate>> h = new BeanListHandler<>(RiseCertificate.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return Lists.newArrayList();
    }

    public List<RiseCertificate> loadGraduates(Integer year, Integer month) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseCertificate where Year = ? and Month = ? and Type in (1,2,3,5) and Del=0";
        ResultSetHandler<List<RiseCertificate>> h = new BeanListHandler<>(RiseCertificate.class);
        try {
            return runner.query(sql, h, year, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return Lists.newArrayList();
    }

    /**
     * 根据年份、月份、ProfileId 获取单个用户的证书信息
     */
    public RiseCertificate loadSingleGraduateByProfileId(Integer year, Integer month, Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseCertificate where ProfileId = ? AND Year = ? and Month = ? and Type = 5 and Del=0";
        ResultSetHandler<RiseCertificate> h = new BeanHandler<>(RiseCertificate.class);
        try {
            return runner.query(sql, h, profileId, year, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

    public void notify(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update RiseCertificate set Notified = 1 where id = ?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
