package com.iquanwai.platon.biz.domain.fragmentation.audition;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.RedisUtil;
import com.iquanwai.platon.biz.dao.fragmentation.AuditionClassMemberDao;
import com.iquanwai.platon.biz.po.AuditionClassMember;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by justin on 2017/11/23.
 */
@Service
public class AuditionServiceImpl implements AuditionService {
    @Autowired
    private AuditionClassMemberDao auditionClassMemberDao;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public AuditionClassMember loadAuditionClassMember(Integer profileId) {
        return auditionClassMemberDao.loadByProfileId(profileId);
    }

    @Override
    public String signupAudition(Integer profileId, String openid) {
        // 计算startTime／endTime,班号
        Date nextMonday = DateUtils.getNextMonday(new Date());
        String className = DateUtils.parseDateToFormat9(nextMonday) + generateAuditionClassSuffix();
        Date startDate = DateUtils.beforeDays(nextMonday, 1);
        AuditionClassMember auditionClassMember = new AuditionClassMember();
        auditionClassMember.setProfileId(profileId);
        auditionClassMember.setOpenid(openid);
        auditionClassMember.setClassName(className);
        auditionClassMember.setStartDate(startDate);
        auditionClassMember.setProblemId(ConfigUtils.getTrialProblemId());
        auditionClassMemberDao.insert(auditionClassMember);

        return className;
    }

    @Override
    public Integer openAuditionCourse(Integer id) {
        return auditionClassMemberDao.update(id);
    }

    @Override
    public void setProfileIdForAuditionMember(String openid, Integer profileId) {
        auditionClassMemberDao.updateProfileId(profileId, openid);
    }

    @Override
    public int generateAuditionClassSuffix() {
        List<Integer> classIds = Lists.newArrayList();
        String nextMonday = DateUtils.parseDateToString(DateUtils.getNextMonday(new Date()));
        redisUtil.lock("generate:audition:sequence", lock -> {
            String key = "audition:sequence:" + nextMonday;
            String sequenceStr = redisUtil.get(key);
            if (sequenceStr == null) {
                sequenceStr = "0";
            }
            int sequence = Integer.parseInt(sequenceStr);
            classIds.add(sequence / 300 + 1);
            redisUtil.set(key, sequence + 1, TimeUnit.DAYS.toSeconds(30));
        });
        return classIds.get(0);
    }

    @Override
    public void becomeCurrentAuditionMember(Integer id) {
        Date currentMonday = DateUtils.getThisMonday(new Date());
        auditionClassMemberDao.updateAuditionClass(id, DateUtils.beforeDays(currentMonday, 1));
    }
}
