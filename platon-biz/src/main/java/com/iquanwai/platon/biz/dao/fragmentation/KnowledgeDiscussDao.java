package com.iquanwai.platon.biz.dao.fragmentation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.KnowledgeDiscuss;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/5/3.
 */
@Repository
public class KnowledgeDiscussDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public int insert(KnowledgeDiscuss discuss){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into KnowledgeDiscuss(KnowledgeId, Openid, RepliedId, Comment, " +
                "Priority, Del, RepliedOpenid, RepliedComment) " +
                "values(?,?,?,?,?,?,?,?)";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(),
                    discuss.getKnowledgeId(), discuss.getOpenid(), discuss.getRepliedId(),
                    discuss.getComment(), discuss.getPriority(), discuss.getDel(),
                    discuss.getRepliedOpenid(), discuss.getRepliedComment());

            return result.intValue();
        }catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

    public List<KnowledgeDiscuss> loadDiscuss(Integer practiceId, Page page) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<KnowledgeDiscuss>> h = new BeanListHandler(KnowledgeDiscuss.class);
        String sql = "SELECT * FROM KnowledgeDiscuss where knowledgeId = ? and Del = 0 " +
                "order by Priority desc, AddTime desc limit " + page.getOffset() + "," + page.getLimit();
        try {
            return run.query(sql, h, practiceId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
