package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.dao.fragmentation.CourseScheduleDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.po.CourseSchedule;
import com.iquanwai.platon.biz.po.MonthlyCampConfig;
import com.iquanwai.platon.biz.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by justin on 2017/11/3.
 */
@Service
public class BusinessPlanServiceImpl implements BusinessPlanService{
    @Autowired
    private CourseScheduleDao courseScheduleDao;
    @Autowired
    private CacheService cacheService;

    @Override
    public List<CourseSchedule> getPlan(Integer profileId) {
        List<CourseSchedule> courseSchedules = courseScheduleDao.getAllScheduleByProfileId(profileId);
        return courseSchedules;
    }

    @Override
    public List<CourseSchedule> getOpeningPlan(Integer profileId) {
        List<CourseSchedule> courseSchedules = courseScheduleDao.getAllScheduleByProfileId(profileId);

        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
        //拿到开营日
        Date date = monthlyCampConfig.getOpenDate();

        courseSchedules = courseSchedules.stream().filter(courseSchedule -> {
            int month = DateUtils.getMonth(date);
            int year = DateUtils.getYear(date);

            return courseSchedule.getYear()<=year && courseSchedule.getMonth()<=month;
        }).collect(Collectors.toList());
        return courseSchedules;
    }
}
