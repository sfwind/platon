package com.iquanwai.platon.biz.dao.bible;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.bible.RelevantTag;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by justin on 2017/9/14.
 */
@Repository
public class RelevantTagDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<RelevantTag> load(int[] userTagIds) {
        if (userTagIds.length == 0) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RelevantTag WHERE TagId in (" + produceQuestionMark(userTagIds.length) + ")" +
                "and RelevantTagId not in (" + produceQuestionMark(userTagIds.length) + ")";
        List<Object> objects = Lists.newArrayList();
        for(int tagId: userTagIds){
            objects.add(tagId);
        }

        List<Object> params = Lists.newArrayList();
        params.addAll(objects);
        params.addAll(objects);

        try {
            return runner.query(sql, new BeanListHandler<RelevantTag>(RelevantTag.class), params.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
