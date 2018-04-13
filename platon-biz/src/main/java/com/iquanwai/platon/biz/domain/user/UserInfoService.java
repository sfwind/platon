package com.iquanwai.platon.biz.domain.user;

import com.iquanwai.platon.biz.po.user.UserInfo;


public interface UserInfoService {

    /**
     * 加载用户信息
     * @param profileId
     * @return
     */
    UserInfo loadByProfileId(Integer profileId);

}