package com.iquanwai.platon.biz.dao.forum;

import com.iquanwai.platon.biz.dao.ForumDBUtil;
import com.iquanwai.platon.biz.po.forum.ForumComment;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by justin on 17/6/19.
 */
@Repository
public class ForumCommentDao extends ForumDBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public int comment(ForumComment forumComment) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ForumComment(AnswerId, commentProfileId, comment, Del, " +
                "RepliedId, RepliedProfileId, RepliedDel) " +
                "values (?,?,?,?,?,?,?)";
        try {
            Long insertRs = runner.insert(sql, new ScalarHandler<>(),
                    forumComment.getAnswerId(), forumComment.getCommentProfileId(), forumComment.getComment(),
                    forumComment.getDel(), forumComment.getRepliedId(),
                    forumComment.getRepliedProfileId(), forumComment.getRepliedDel());
            return insertRs.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
