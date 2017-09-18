package com.iquanwai.platon.biz.domain.bible;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.bible.StudyNoteDao;
import com.iquanwai.platon.biz.dao.bible.StudyNoteTagDao;
import com.iquanwai.platon.biz.dao.bible.SubscribeArticleTagDao;
import com.iquanwai.platon.biz.po.bible.StudyNote;
import com.iquanwai.platon.biz.po.bible.StudyNoteTag;
import com.iquanwai.platon.biz.po.bible.SubscribeArticleTag;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2017/9/14.
 */
@Service
public class StudyNoteServiceImpl implements StudyNoteService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private StudyNoteDao studyNoteDao;
    @Autowired
    private StudyNoteTagDao studyNoteTagDao;
    @Autowired
    private SubscribeArticleTagDao subscribeArticleTagDao;

    @Override
    public List<StudyNote> loadStudyNoteList(Integer profileId, Page page) {
        List<StudyNote> noteList = studyNoteDao.loadNoteList(profileId, page);
        page.setTotal(studyNoteDao.count(profileId));
        return noteList;
    }

    @Override
    public StudyNote loadStudyNote(Integer profileId, Integer studyNoteId) {
        StudyNote studyNote = studyNoteDao.load(StudyNote.class, studyNoteId);
        List<StudyNoteTag> noteTags = studyNoteTagDao.loadArticleTagList(studyNoteId);
        List<SubscribeArticleTag> noteTagGroup = noteTags.stream().map(item -> subscribeArticleTagDao.load(SubscribeArticleTag.class, item.getTagId())).collect(Collectors.toList());
        studyNote.setTags(noteTagGroup);
        return studyNote;
    }

    private Double calculateTagPoint(StudyNote note) {
        switch (note.getCatalogId()) {
            case StudyNote.BOOK: {
                Integer page = note.getPage();
                Double pagePoint = page == null ? 20d : page * 0.15d;
                return pagePoint / note.getTagIds().size();
            }
            case StudyNote.COURSE: {
                return 20d / note.getTagIds().size();
            }
            case StudyNote.VIDEO:
            case StudyNote.AUDIO: {
                Integer minute = note.getMinute();
                Double minutePoint = minute == null ? 20d : minute * 0.05d;
                return minutePoint / note.getTagIds().size();
            }
            case StudyNote.CHAIR: {
                Integer minute = note.getMinute();
                Double minutePoint = minute == null ? 120d : minute * 0.05d;
                return minutePoint / note.getTagIds().size();
            }
            case StudyNote.PROJECT: {
                return 30d / note.getTagIds().size();
            }
            default:
                return 0d;
        }
    }

    @Override
    public Integer createOrUpdateStudyNote(Integer profileId, StudyNote studyNote) {
        Integer noteId;
        Double tagPoint = new BigDecimal(this.calculateTagPoint(studyNote)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (studyNote.getId() == null) {
            // 新增
            studyNote.setProfileId(profileId);
            noteId = studyNoteDao.insert(studyNote);
            List<Integer> tagIds = studyNote.getTagIds();
            if (CollectionUtils.isNotEmpty(tagIds)) {
                // 插入标签
                List<Integer> tags = subscribeArticleTagDao.loadAll(SubscribeArticleTag.class).stream().filter(item -> !item.getDel()).map(SubscribeArticleTag::getId).collect(Collectors.toList());
                tagIds.stream().filter(tags::contains).forEach(item -> {
                    StudyNoteTag studyNoteTag = new StudyNoteTag();
                    studyNoteTag.setProfileId(profileId);
                    studyNoteTag.setStudyNoteId(noteId);
                    studyNoteTag.setPoint(tagPoint);
                    studyNoteTag.setTagId(item);
                    studyNoteTagDao.insertStudyNoteTag(studyNoteTag);
                });
            }
        } else {
            noteId = studyNote.getId();
            List<Integer> tags = subscribeArticleTagDao.loadAll(SubscribeArticleTag.class).stream().filter(item -> !item.getDel()).map(SubscribeArticleTag::getId).collect(Collectors.toList());
            // 修改
            StudyNote existNote = studyNoteDao.load(StudyNote.class, studyNote.getId());
            Assert.notNull(existNote, "文章记录不能为空");
            Assert.isTrue(existNote.getProfileId().equals(profileId), "不能修改其他人的笔记");
            // 更新内容
            studyNoteDao.update(studyNote);
            // 老标签
            List<StudyNoteTag> existTags = studyNoteTagDao.loadArticleExistTagList(studyNote.getId());
            // 新选的标签
            List<Integer> newTagIds = studyNote.getTagIds() != null ? studyNote.getTagIds().stream().filter(tags::contains).collect(Collectors.toList()) : Lists.newArrayList();

            // 需要插入的tag
            List<StudyNoteTag> insertTags = Lists.newArrayList();
            // 需要重新选择的tag
            List<Integer> reChooseTags = Lists.newArrayList();
            // 需要删除的tag
            List<Integer> delTags = Lists.newArrayList();

            newTagIds.forEach(item -> {
                StudyNoteTag oldTag = existTags.stream().filter(tag -> item.equals(tag.getTagId())).findFirst().orElse(null);
                if (oldTag == null) {
                    // 需要新插入
                    StudyNoteTag tag = new StudyNoteTag();
                    tag.setTagId(item);
                    tag.setPoint(tagPoint);
                    tag.setProfileId(profileId);
                    tag.setStudyNoteId(noteId);
                    insertTags.add(tag);
                } else if (oldTag.getDel()) {
                    //  需要重新选择
                    reChooseTags.add(oldTag.getId());
                }
                // ignore oldTag.del = false
            });
            existTags.forEach(item -> {
                if (!newTagIds.contains(item.getTagId()) && !item.getDel()) {
                    // 新选的标签不包含这个老的，并且老的没被取消
                    delTags.add(item.getId());
                }
            });
            // 处理标签
            insertTags.forEach(studyNoteTagDao::insertStudyNoteTag);
            reChooseTags.forEach((item) -> studyNoteTagDao.reChooseStudyNoteTag(item, tagPoint));
            delTags.forEach(studyNoteTagDao::delStudyNoteTag);
            // 加分
            studyNoteTagDao.updateNotePoint(noteId, tagPoint);
        }
        return noteId;
    }

}
