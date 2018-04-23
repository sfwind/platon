package com.iquanwai.platon.biz.domain.user;

import com.iquanwai.platon.biz.po.user.UserInfo;

import java.util.List;


public interface UserInfoService {

    /**
     * 加载用户信息
     * @param profileId
     * @return
     */
    UserInfo loadByProfileId(Integer profileId);

    /**
     * 加载用户
     * @param profileIds
     * @return
     */
    List<UserInfo> loadByProfileIds(List<Integer> profileIds);

}