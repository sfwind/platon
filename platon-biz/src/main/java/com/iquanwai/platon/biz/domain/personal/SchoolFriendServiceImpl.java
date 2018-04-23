package com.iquanwai.platon.biz.domain.personal;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.user.UserInfoDao;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.domain.user.UserInfoService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.SchoolFriend;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.user.UserInfo;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchoolFriendServiceImpl implements SchoolFriendService {

    @Autowired
    private AccountService accountService;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private UserInfoService userInfoService;

    public List<SchoolFriend> schoolFriends = Lists.newArrayList();

    @PostConstruct
    public void init() {
        //获得所有的商学院用户
        List<Integer> riseMemberIds = riseMemberManager.getAllValidElites().stream().filter(riseMember -> riseMember.getVip() == 0).map(RiseMember::getProfileId).distinct().collect(Collectors.toList());
        List<UserInfo> userInfos = userInfoService.loadByProfileIds(riseMemberIds).stream().filter(userInfo -> userInfo.getIndustry() != null && userInfo.getCompany() != null).sorted(Comparator.comparing(UserInfo::getPriority).reversed()).collect(Collectors.toList());
        List<Integer> profileIds = userInfos.stream().map(UserInfo::getProfileId).collect(Collectors.toList());
        List<Profile> profiles = accountService.getProfiles(profileIds);
        if (CollectionUtils.isNotEmpty(schoolFriends)) {
            schoolFriends.clear();
        }
        userInfos.forEach(userInfo -> {
            SchoolFriend schoolFriendDto = new SchoolFriend();
            Profile profile = profiles.stream().filter(profile1 -> profile1.getId() == userInfo.getProfileId()).findFirst().orElse(null);
            if (profile != null && profile.getMemberId() != null && profile.getCity() != null) {
                BeanUtils.copyProperties(profile, schoolFriendDto);
                schoolFriendDto.setNickName(profile.getNickname());
                schoolFriendDto.setHeadImgUrl(profile.getHeadimgurl());
                BeanUtils.copyProperties(userInfo, schoolFriendDto);
                schoolFriends.add(schoolFriendDto);
            }
        });
    }

    @Override
    public List<SchoolFriend> loadSchoolFriends() {
        return schoolFriends;
    }

    @Override
    public void reload() {
        init();
    }
}
