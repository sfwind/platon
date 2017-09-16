package com.iquanwai.platon.biz.domain.bible;

import com.iquanwai.platon.biz.po.bible.StudyNote;
import com.iquanwai.platon.biz.util.page.Page;

import java.util.List;

/**
 * Created by nethunder on 2017/9/14.
 */
public interface StudyNoteService {
    List<StudyNote> loadStudyNoteList(Integer profileId, Page page);

    StudyNote loadStudyNote(Integer profileId, Integer studyNoteId);

    Integer createOrUpdateStudyNote(Integer profileId, StudyNote studyNote);
}
