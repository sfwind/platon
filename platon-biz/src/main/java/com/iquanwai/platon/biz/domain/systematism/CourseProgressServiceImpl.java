package com.iquanwai.platon.biz.domain.systematism;

import com.iquanwai.platon.biz.dao.systematism.ClassMemberDao;
import com.iquanwai.platon.biz.po.systematism.ClassMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by nethunder on 2017/6/7.
 */
@Service
public class CourseProgressServiceImpl implements CourseProgressService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ClassMemberDao classMemberDao;


    @Override
    public List<ClassMember> loadActiveCourse(Integer profileId) {
        return classMemberDao.classMember(profileId);
    }

}
