package com.iquanwai.platon.web.resolver;

import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by tomas on 3/17/16.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {
    private Integer id;
    private String openId;
    private String weixinName;
    private String headimgUrl;
    private String realName;
    private Integer role;
    private String signature;
    private Boolean openRise;
    private Boolean riseMember;
    private Boolean openApplication; // 非db字段 是否打开过应用练习
    private Boolean openConsolidation; // 非db字段 是否打开过巩固练习
    private Integer Device; // 1-pc 2-mobile

    public static LoginUser defaultUser(){
        return new LoginUser(ConfigUtils.getDefaultProfileId(), ConfigUtils.getDefaultOpenid(), "风之伤",
                Profile.DEFAULT_AVATAR, null, 1, null, false, true, false, false, 1);
    }
}
