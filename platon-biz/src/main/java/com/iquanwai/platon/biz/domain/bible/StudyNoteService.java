package com.iquanwai.platon.biz.domain.bible;

import com.iquanwai.platon.biz.po.bible.StudyNote;

/**
 * Created by nethunder on 2017/9/14.
 */
public interface StudyNoteService {
    StudyNote loadStudyNote(Integer profileId, Integer studyNoteId);

    Integer createOrUpdateStudyNote(Integer profileId, StudyNote studyNote);
}
