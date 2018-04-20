package com.iquanwai.platon.biz.domain.personal;

import com.iquanwai.platon.biz.po.user.UserInfo;
import com.iquanwai.platon.biz.util.page.Page;

import java.util.List;

public interface SchoolFriendService {

        /**
         * 分页加载校友信息
          * @param page
          * @return
          */
        List<UserInfo> loadSchoolFriends(Integer excludeId, Page page);
}
