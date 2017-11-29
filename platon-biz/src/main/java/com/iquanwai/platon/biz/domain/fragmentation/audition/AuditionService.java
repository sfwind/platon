package com.iquanwai.platon.biz.domain.fragmentation.audition;

import com.iquanwai.platon.biz.po.AuditionClassMember;

/**
 * Created by justin on 2017/11/23.
 */
public interface AuditionService {
    AuditionClassMember loadAuditionClassMember(Integer profileId);

    /**
     * 报名试听课
     * */
    String signupAudition(Integer profileId, String openid);

    /**
     * 开试听课
     * */
    Integer openAuditionCourse(Integer id);

    void setProfileIdForAuditionMember(String openid, Integer profileId);

    int generateAuditionClassSuffix();

    /**
     * 刷成当前开课的情况
     * @param id RiseClassMember的Id
     */
    void becomeCurrentAuditionMember(Integer id);
}
