package com.iquanwai.platon.biz.domain.personal;

import com.iquanwai.platon.biz.dao.user.UserInfoDao;
import com.iquanwai.platon.biz.po.user.UserInfo;
import com.iquanwai.platon.biz.util.page.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolFriendServiceImpl implements SchoolFriendService{

    @Autowired
    private UserInfoDao userInfoDao;

    @Override
    public List<UserInfo> loadSchoolFriends(Integer profileId, Page page) {
        List<UserInfo> userInfos = userInfoDao.loadList(profileId, page);
        page.setTotal(userInfoDao.loadCount(profileId));
        return userInfos;
    }
}
