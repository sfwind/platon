package com.iquanwai.platon.web.resolver;

import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by nethunder on 2017/9/7.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestUser {
    private String openId;
    private String weixinName;
    private String headimgUrl;
    private String realName;

    public static GuestUser defaultUser() {
        return new GuestUser(ConfigUtils.getDefaultOpenid(), "风之伤",
                Profile.DEFAULT_AVATAR, null);
    }

}