package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.RiseMember;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/4/13.
 */
@Repository
public class RiseMemberDao extends DBUtil {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    public int insert(RiseMember riseMember){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into RiseMember(Openid, OrderId, MemberTypeId, ExpireDate) " +
                " VALUES (?, ?, ?, ?)";

        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                   riseMember.getOpenId(),riseMember.getOrderId(),riseMember.getMemberTypeId(),riseMember.getExpireDate());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public RiseMember validRiseMember(String openId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseMember where OpenId = ? and expired = 0";

        try{
            BeanHandler<RiseMember> handler = new BeanHandler<RiseMember>(RiseMember.class);
            return runner.query(sql, handler, openId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
