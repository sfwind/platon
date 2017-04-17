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
    private String openId;
    private String weixinName;
    private String headimgUrl;
    private String realName;
    private Boolean openRise;
    private Boolean riseMember;
    private Boolean openComprehension; // 非db字段 是否打开过应用训练
    private Boolean openConsolidation; // 非db字段 是否打开过巩固训练

    public static LoginUser defaultUser(){
        return new LoginUser(ConfigUtils.getDefaultOpenid(),"风之伤",
                Profile.DEFAULT_AVATAR, null, false, false,false,false);
    }
}
