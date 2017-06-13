package com.iquanwai.platon.biz.domain.systematism;

import com.iquanwai.platon.biz.dao.systematism.ClassDao;
import com.iquanwai.platon.biz.dao.systematism.ClassMemberDao;
import com.iquanwai.platon.biz.dao.systematism.CourseDao;
import com.iquanwai.platon.biz.po.systematism.ClassMember;
import com.iquanwai.platon.biz.po.systematism.Course;
import com.iquanwai.platon.biz.po.systematism.QuanwaiClass;
import com.iquanwai.platon.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2017/6/7.
 */
@Service
public class CourseProgressServiceImpl implements CourseProgressService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ClassDao classDao;

    @Autowired
    private ClassMemberDao classMemberDao;

    @Autowired
    private CourseDao courseDao;

    @Override
    public List<ClassMember> loadActiveCourse(Integer profileId) {
        List<ClassMember> classMemberList = classMemberDao.classMember(profileId);

        return classMemberList.stream().filter(classMember -> !isOver(classMember))
                .map(classMember -> {
                    classProgress(classMember);
                    return classMember;
                }).sorted((classMember1, classMember2) -> {
                    // 在我的训练中，从上到下，按照课程结束时间，顺序排列（越早结束的，越在上方）
                    try {
                        long leftTime = classMember1.getCloseDate().getTime();
                        long rightTime = classMember2.getCloseDate().getTime();
                        return leftTime - rightTime == 0 ? 0 : leftTime - rightTime > 0 ? 1 : -1;
                    } catch (NullPointerException e) {
                        logger.error(e.getLocalizedMessage());
                        return 0;
                    }
                }).collect(Collectors.toList());
    }



    private boolean isOver(ClassMember classMember) {
        if(classMember==null){
            return true;
        }
        if(classMember.getCloseDate().before(DateUtils.startDay(new Date()))){
            if(!classMember.getGraduate()){
                Course course = courseDao.load(Course.class, classMember.getCourseId());
                //短课程或者试听课程关闭以后,如果用户还未毕业,强制设置成毕业
                if(course.getType()==Course.SHORT_COURSE || course.getType() == Course.AUDITION_COURSE) {
                    classMemberDao.graduate(classMember.getId());
                }
            }
            return true;
        }
        return false;
    }



    private void classProgress(ClassMember classMember){
        Assert.notNull(classMember, "classMember不能为空");
        QuanwaiClass quanwaiClass = classDao.load(QuanwaiClass.class, classMember.getClassId());
        if(quanwaiClass==null){
            return;
        }
        if(classMember.getGraduate()){
            logger.info("{} has no active course {}", classMember.getOpenId(), classMember.getCourseId());
            return;
        }

        classMember.setClassProgress(quanwaiClass.getProgress());
    }
}
