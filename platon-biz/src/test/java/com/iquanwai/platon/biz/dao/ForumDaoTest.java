package com.iquanwai.platon.biz.dao;

import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.dao.forum.QuestionTagDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseCourseDao;
import com.iquanwai.platon.biz.po.RiseCourseOrder;
import com.iquanwai.platon.biz.po.forum.QuestionTag;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 17/6/19.
 */
public class ForumDaoTest extends TestBase {
    @Autowired
    private QuestionTagDao questionTagDao;
    @Autowired
    private RiseCourseDao riseCourseDao;

    @Test
    public void testRiseCourse() {
        RiseCourseOrder riseCourseOrder = riseCourseDao.loadEntryOrder(16443, 8);
        System.out.println(riseCourseOrder);

        System.out.print(riseCourseDao.loadOrder("dx2cii8dar0i28rh"));

    }

    @Test
    public void testInsert(){
        QuestionTag questionTag = new QuestionTag();
        questionTag.setQuestionId(1);
        questionTag.setTagId(1);
        questionTagDao.insert(questionTag);
    }
}
