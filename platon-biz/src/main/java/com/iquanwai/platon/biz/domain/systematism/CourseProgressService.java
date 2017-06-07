package com.iquanwai.platon.biz.domain.systematism;

import com.iquanwai.platon.biz.po.systematism.ClassMember;

import java.util.List;

/**
 * Created by nethunder on 2017/6/7.
 */
public interface CourseProgressService {

    List<ClassMember> loadActiveCourse(String openid);
}
