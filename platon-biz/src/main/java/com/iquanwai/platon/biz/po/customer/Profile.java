package com.iquanwai.platon.biz.po.customer;

import lombok.Data;

/**
 * Created by nethunder on 2017/2/8.
 */
@Data
public class Profile {
    private String openid;	//用户的标识，对当前公众号唯一
    private String nickname; //用户的昵称
    private String city;	//用户所在城市
    private String country;	//用户所在国家
    private String province; //	用户所在省份
    private String headimgurl;	//用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像），用户没有头像时该项为空。若用户更换头像，原有头像URL将失效。
    private String mobileNo;  //手机号
    private String email;  //邮箱
    private String industry; //行业
    private String function; //职业
    private String workingLife; //工作年限
    private String realName; //真名
    private Integer point;
    private Integer isFull;
    private String riseId;
}
