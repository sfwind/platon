package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.RiseMember;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class RiseMemberDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<RiseMember> loadValidRiseMemberByMemberTypeId(Integer profileId, List<Integer> memberTypes) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String mask = produceQuestionMark(memberTypes.size());
        List<Object> params = Lists.newArrayList();
        params.add(profileId);
        params.addAll(memberTypes);
        String sql = "SELECT * FROM RiseMember WHERE ProfileId = ? AND memberTypeId in ("
                + mask + ") AND Expired=0 AND Del = 0";
        ResultSetHandler<List<RiseMember>> h = new BeanListHandler<>(RiseMember.class);
        try {
            return runner.query(sql, h, params.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<RiseMember> loadRiseMembersByProfileId(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseMember WHERE ProfileId = ? AND Del = 0";
        ResultSetHandler<List<RiseMember>> h = new BeanListHandler<>(RiseMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }


    public List<RiseMember> loadSyncRiseMembers(String currentDate, Integer memberTypeId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseMember WHERE MemberTypeId = ? AND  OpenDate like ? AND EXPIRED = 0   AND DEL = 0 ";
        ResultSetHandler<List<RiseMember>> h = new BeanListHandler<>(RiseMember.class);

        try {
            return runner.query(sql, h, memberTypeId, currentDate + "%");
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<RiseMember> loadAllByMembers(List<Integer> memberTypeIds){
        QueryRunner runner = new QueryRunner(getDataSource());
        String mask = produceQuestionMark(memberTypeIds.size());
        String sql = "SELECT * FROM RiseMember WHERE MemberTypeId in (" + mask + ") AND EXPIRED = 0 AND DEL = 0";
        ResultSetHandler<List<RiseMember>> h = new BeanListHandler<RiseMember>(RiseMember.class);

        try {
           return runner.query(sql,h,memberTypeIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return Lists.newArrayList();
    }


}
