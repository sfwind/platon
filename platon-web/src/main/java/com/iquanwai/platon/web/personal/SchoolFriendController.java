package com.iquanwai.platon.web.personal;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.domain.personal.SchoolFriendService;
import com.iquanwai.platon.biz.domain.user.UserInfoService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.SchoolFriend;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.user.UserInfo;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.personal.dto.SchoolFriendDto;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.support.Assert;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/rise/personal")
@Api(description = "校友录功能")
public class SchoolFriendController {

    private static final Integer SCHOOL_FRIEND_SIZE = 10;

    @Autowired
    private AccountService accountService;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private UserInfoService userInfoService;

    public List<SchoolFriend> schoolFriends = Lists.newArrayList();

    @PostConstruct
    public void init(){
        //TODO:刷新mq
        //获得所有的商学院用户
        List<Integer> riseMemberIds = riseMemberManager.getAllValidElites().stream().map(RiseMember::getProfileId).distinct().collect(Collectors.toList());;
        List<UserInfo> userInfos = userInfoService.loadByProfileIds(riseMemberIds).stream().sorted(Comparator.comparing(UserInfo::getPriority).reversed()).collect(Collectors.toList());
        List<Integer> profileIds = userInfos.stream().map(UserInfo::getProfileId).collect(Collectors.toList());;
        List<Profile> profiles = accountService.getProfiles(profileIds);
        if(CollectionUtils.isNotEmpty(schoolFriends)){
            schoolFriends.clear();
        }
        userInfos.forEach(userInfo -> {
            SchoolFriend schoolFriendDto = new SchoolFriend();
            Profile profile = profiles.stream().filter(profile1 -> profile1.getId() == userInfo.getProfileId()).findFirst().orElse(null);
            if (profile != null) {
                BeanUtils.copyProperties(profile, schoolFriendDto);
                schoolFriendDto.setNickName(profile.getNickname());
                schoolFriendDto.setHeadImgUrl(profile.getHeadimgurl());
            }
            BeanUtils.copyProperties(userInfo, schoolFriendDto);
            schoolFriends.add(schoolFriendDto);
        });
    }



    @RequestMapping(value = "/school/friend", method = RequestMethod.GET)
    @ApiOperation("分页获得校友录名单")
    public ResponseEntity<Map<String, Object>> getSchoolFriends(UnionUser unionUser, @ModelAttribute Page page) {
        Assert.notNull(unionUser);
        //过滤自己
        List<SchoolFriend> excludeFriends = schoolFriends.stream().filter(schoolFriend -> !schoolFriend.getProfileId().equals(unionUser.getId())).collect(Collectors.toList());
        List<SchoolFriendDto> schoolFriendDtos = Lists.newArrayList();
        excludeFriends.forEach(schoolFriend -> {
            SchoolFriendDto schoolFriendDto = new SchoolFriendDto();
            BeanUtils.copyProperties(schoolFriend,schoolFriendDto);
            schoolFriendDtos.add(schoolFriendDto);
        });
        Integer pageId = page.getPage();
        if((pageId-1)*SCHOOL_FRIEND_SIZE>schoolFriendDtos.size()){
            return WebUtils.result(Lists.newArrayList());
        }
        else if(page.getPage()*SCHOOL_FRIEND_SIZE>schoolFriendDtos.size()){
            return WebUtils.result(schoolFriendDtos.subList((page.getPage()-1)*SCHOOL_FRIEND_SIZE,schoolFriendDtos.size()));
        }else{
            return WebUtils.result(schoolFriendDtos.subList((page.getPage()-1)*SCHOOL_FRIEND_SIZE,page.getPage()*SCHOOL_FRIEND_SIZE));
        }
    }
}