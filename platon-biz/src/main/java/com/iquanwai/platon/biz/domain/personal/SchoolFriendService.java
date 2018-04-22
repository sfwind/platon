package com.iquanwai.platon.biz.domain.personal;

import com.iquanwai.platon.biz.po.SchoolFriend;
import com.iquanwai.platon.biz.po.user.UserInfo;
import com.iquanwai.platon.biz.util.page.Page;

import java.util.List;

public interface SchoolFriendService {
        /**
         * 加载校友信息
         * @return
         */
        List<SchoolFriend> loadSchoolFriends();

        void reload();
}
