package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.RiseCertificate;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 17/8/29.
 */
@Repository
public class RiseCertificateDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

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

}
