package com.iquanwai.platon.biz.po.bible;

import lombok.Data;

/**
 * Created by nethunder on 2017/9/14.
 */
@Data
public class StudyNoteTag {
    private Integer id;
    private Integer profileId;
    private Integer studyNoteId;
    private Integer tagId;
    private Boolean del;
}
