package com.iquanwai.platon.biz.dao.bible;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.bible.StudyNote;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/9/14.
 */
@Repository
public class StudyNoteDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Integer insert(StudyNote studyNote) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO StudyNote(CatalogId, ProfileId, Name, Page,Source, Url, Note, Minute) VALUES (?,?,?,?,?,?,?,?)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(),
                    studyNote.getCatalogId(), studyNote.getProfileId(), studyNote.getName(),
                    studyNote.getPage(), studyNote.getSource(), studyNote.getUrl(),
                    studyNote.getNote(), studyNote.getMinute()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer update(StudyNote studyNote) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE StudyNote SET " +
                "CatalogId=?,  Name=?, Page=?,Source=?, Url=?, Note=?, Minute=? ,LastModifiedTime=CURRENT_TIMESTAMP " +
                " WHERE Id = ?";
        try {
            return runner.update(sql, studyNote.getCatalogId(),
                    studyNote.getName(), studyNote.getPage(), studyNote.getSource(),
                    studyNote.getUrl(), studyNote.getNote(),
                    studyNote.getMinute(), studyNote.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public List<StudyNote> loadNoteList(Page page) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM StudyNote WHERE Del = 0 limit " + page.getOffset() + "," + page.getLimit();
        try {
            return runner.query(sql, new BeanListHandler<>(StudyNote.class));
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public int count() {
        QueryRunner run = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<Long>();

        try {
            return run.query("SELECT count(*) FROM StudyNote where Del = 0", h).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return -1;
    }

}
