package com.iquanwai.platon.web.resolver;

import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {
    private Integer id;
    private String unionId;
    private String openId;
    private String weixinName;
    private String headimgUrl;
    private String realName;
    private Integer role;
    private String signature;
    private Boolean openRise;// 是否打开过课程详情页
    private Boolean openNavigator; // 是否打开过课程列表页
    private Integer riseMember;   //0-免费用户,1-会员,2-课程单买用户
    private Boolean openApplication; // 是否打开过应用练习
    private Boolean openConsolidation; // 是否打开过巩固练习
    private Integer device; // 1-pc 2-mobile
    private Boolean openWelcome; //是否打开过欢迎页

    public static LoginUser defaultUser() {
        return new LoginUser(ConfigUtils.getDefaultProfileId(), ConfigUtils.getDefaultUnionId(), ConfigUtils.getDefaultOpenid(), "风之伤",
                Profile.DEFAULT_AVATAR, null, 5, null, true, true, 0, true, true, 1, true);
    }

}
