package com.iquanwai.platon.biz.domain.user;

import com.iquanwai.platon.biz.dao.user.UserInfoDao;
import com.iquanwai.platon.biz.po.user.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Autowired
    private UserInfoDao userInfoDao;

    @Override
    public UserInfo loadByProfileId(Integer profileId) {
        return userInfoDao.loadByProfileId(profileId);
    }
}