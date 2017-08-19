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
    private Boolean openRise;// 是否打开过小课详情页
    private Boolean openNavigator; // 是否打开过小课列表页
    private Integer riseMember;   //0-免费用户,1-会员,2-小课用户
    private Boolean openApplication; // 是否打开过应用练习
    private Boolean openConsolidation; // 是否打开过巩固练习
    private Integer Device; // 1-pc 2-mobile

    public static LoginUser defaultUser(){
        return new LoginUser(ConfigUtils.getDefaultProfileId(), ConfigUtils.getDefaultOpenid(), "风之伤",
                Profile.DEFAULT_AVATAR, null, 5, null, true, true, 0, true, true, 1);
    }

}
