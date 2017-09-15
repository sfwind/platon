package com.iquanwai.platon.biz.dao.bible;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.bible.StudyNote;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/9/14.
 */
@Repository
public class StudyNoteDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Integer insert(StudyNote studyNote) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO StudyNote(Name, Page, Url, Note, Minute) VALUES (?,?,?,?,?)";
        try {
            return runner.insert(sql, new ScalarHandler<Long>(), studyNote.getName(), studyNote.getPage(), studyNote.getUrl(),
                    studyNote.getNote(), studyNote.getMinute()).intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    public Integer update(StudyNote studyNote) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE StudyNote SET Name=?,Page=?,Url=?,Note=?,Minute=?,LastModifiedTime=CURRENT_TIMESTAMP WHERE Id = ?";
        try {
            return runner.update(sql, studyNote.getName(), studyNote.getPage(), studyNote.getUrl(), studyNote.getNote(),
                    studyNote.getMinute(), studyNote.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
