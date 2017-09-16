package com.iquanwai.platon.biz.dao.bible;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.bible.StudyNoteTag;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/9/14.
 */
@Repository
public class StudyNoteTagDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Integer insertStudyNoteTag(StudyNoteTag studyNoteTag) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO StudyNoteTag(StudyNoteId,ProfileId,Point, TagId) VALUES(?,?,?,?)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), studyNoteTag.getStudyNoteId(), studyNoteTag.getProfileId(),
                    studyNoteTag.getPoint(), studyNoteTag.getTagId()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<StudyNoteTag> loadArticleTagList(Integer studyNoteId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM StudyNoteTag WHERE StudyNoteId = ? and Del = 0";
        try {
            return runner.query(sql, new BeanListHandler<StudyNoteTag>(StudyNoteTag.class), studyNoteId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<StudyNoteTag> loadArticleExistTagList(Integer studyNoteId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM StudyNoteTag WHERE StudyNoteId = ?";
        try {
            return runner.query(sql, new BeanListHandler<StudyNoteTag>(StudyNoteTag.class), studyNoteId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public Integer delStudyNoteTag(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE StudyNoteTag SET Del = 1 WHERE Id = ?";
        try {
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer reChooseStudyNoteTag(Integer id, Double point) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE StudyNoteTag SET Del = 0,Point = ? WHERE Id = ?";
        try {
            return runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<StudyNoteTag> loadCertainDayNote(Integer profileId, Date date) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM StudyNoteTag WHERE ProfileId = ? and DEL = 0 and DATE(AddTime) = ?";
        try {
            return runner.query(sql, new BeanListHandler<StudyNoteTag>(StudyNoteTag.class), profileId, DateUtils.parseDateToString(date));
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}
