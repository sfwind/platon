package com.iquanwai.platon.web.resolver;

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

    public static LoginUser defaultUser(){
        return new LoginUser(ConfigUtils.getDefaultOpenid(),"风之伤", null, null,false);
    }
}
