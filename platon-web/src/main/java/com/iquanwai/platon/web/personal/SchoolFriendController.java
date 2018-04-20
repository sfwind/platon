package com.iquanwai.platon.web.personal;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.personal.SchoolFriendService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.user.UserInfo;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.personal.dto.SchoolFriendDto;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.support.Assert;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/rise/personal")
@Api(description = "校友录功能")
public class SchoolFriendController {


    private static final Integer SCHOOL_FRIEND_SIZE = 10;


    @Autowired
    private SchoolFriendService schoolFriendService;
    @Autowired
    private AccountService accountService;

    @RequestMapping(value = "/school/friend", method = RequestMethod.GET)
    @ApiOperation("分页获得校友录名单")
    public ResponseEntity<Map<String, Object>> getSchoolFriends(UnionUser unionUser, @ModelAttribute Page page) {
        //TODO:只获取商学院、排序问题
        //TODO:有新的内容加进来时重复问题
        Assert.notNull(unionUser);
        page.setPageSize(SCHOOL_FRIEND_SIZE);
        List<UserInfo> userInfos = schoolFriendService.loadSchoolFriends(unionUser.getId(), page);
        List<Integer> profileIds = userInfos.stream().map(UserInfo::getProfileId).collect(Collectors.toList());
        List<Profile> profiles = accountService.getProfiles(profileIds);
        List<SchoolFriendDto> schoolFriendDtos = Lists.newArrayList();

        userInfos.forEach(userInfo -> {
            SchoolFriendDto schoolFriendDto = new SchoolFriendDto();
            Profile profile = profiles.stream().filter(profile1 -> profile1.getId() == userInfo.getProfileId()).findFirst().orElse(null);
            if (profile != null) {
                BeanUtils.copyProperties(profile, schoolFriendDto);
                schoolFriendDto.setNickName(profile.getNickname());
                schoolFriendDto.setHeadImgUrl(profile.getHeadimgurl());
            }
            BeanUtils.copyProperties(userInfo, schoolFriendDto);
            schoolFriendDtos.add(schoolFriendDto);
        });
        return WebUtils.result(schoolFriendDtos);
    }
}