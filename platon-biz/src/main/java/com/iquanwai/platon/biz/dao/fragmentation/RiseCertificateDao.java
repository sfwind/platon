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
import java.util.Date;
import java.util.List;

/**
 * Created by justin on 17/8/29.
 */
@Repository
public class RiseCertificateDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int insert(RiseCertificate riseCertificate) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO RiseCertificate (ProfileId, MemberTypeId, Type, CertificateNo, Year, Month, GroupNo, ProblemId, ProblemName) VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    riseCertificate.getProfileId(),
                    riseCertificate.getMemberTypeId(),
                    riseCertificate.getType(),
                    riseCertificate.getCertificateNo(),
                    riseCertificate.getYear(),
                    riseCertificate.getMonth(),
                    riseCertificate.getGroupNo(),
                    riseCertificate.getProblemId(),
                    riseCertificate.getProblemName());
            return result.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

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

    public RiseCertificate loadNextCertificateNoById(Integer certificateId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseCertificate WHERE Id > ? AND Del = 0 LIMIT 1";
        ResultSetHandler<RiseCertificate> h = new BeanHandler<>(RiseCertificate.class);
        try {
            return runner.query(sql, h, certificateId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public int updateDownloadTime(String certificateNo) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE RiseCertificate SET DownloadTime = ? WHERE CertificateNo = ?";
        try {
            return runner.update(sql, new Date(), certificateNo);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<RiseCertificate> loadUnNotifiedByMonthAndYearAndMemberTypeId(Integer year, Integer month, Integer memberTypeId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseCertificate where Year = ? and Month = ? and MemberTypeId = ? AND Notified = 0 and Del=0";
        ResultSetHandler<List<RiseCertificate>> h = new BeanListHandler<>(RiseCertificate.class);
        try {
            return runner.query(sql, h, year, month, memberTypeId);
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
        String sql = "select * from RiseCertificate where ProfileId = ? AND Year = ? and Month = ? and Type in (5) and Del = 0";
        ResultSetHandler<RiseCertificate> h = new BeanHandler<>(RiseCertificate.class);
        try {
            return runner.query(sql, h, profileId, year, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 获取还没有上传证书图片的证书
     */
    public List<RiseCertificate> loadUnUploadImageCertificates() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseCertificate WHERE ImageUrl IS NULL AND Del = 0";
        ResultSetHandler<List<RiseCertificate>> h = new BeanListHandler<RiseCertificate>(RiseCertificate.class);
        try {
            return runner.query(sql, h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public int updateImageUrl(Integer certificateId, String imageUrl) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE RiseCertificate SET ImageUrl = ? WHERE Id = ?";
        try {
            return runner.update(sql, imageUrl, certificateId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
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

    public RiseCertificate loadGraduateByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseCertificate where ProfileId = ? and Type in (1,2,3,5) AND Del = 0";
        ResultSetHandler<RiseCertificate> h = new BeanHandler<>(RiseCertificate.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

    public List<RiseCertificate> loadExistRiseCertificates(Integer profileId, Integer year, Integer month) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseCertificate WHERE ProfileId = ? AND Year = ? AND Month = ? AND Del = 0";
        ResultSetHandler<List<RiseCertificate>> h = new BeanListHandler<>(RiseCertificate.class);
        try {
            return runner.query(sql, h, profileId, year, month);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

}
