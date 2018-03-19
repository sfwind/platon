package com.iquanwai.platon.biz.dao.common;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.Announce;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by 三十文
 */
@Repository
public class AnnounceDao extends DBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<Announce> loadByMemberTypeId(int memberTypeId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM Announce WHERE MemberTypeId = ? AND Del = 0";
        ResultSetHandler<List<Announce>> h = new BeanListHandler<Announce>(Announce.class);
        try {
            return runner.query(sql, h, memberTypeId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public void delExpiredAnnounce(List<Integer> ids) {
        if (ids.size() == 0) {
            return;
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE Announce SET Del = 1 WHERE Id in (" + produceQuestionMark(ids.size()) + ");";
        try {
            runner.update(sql, ids.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}
