package com.iquanwai.platon.biz.po.common;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/2/8.
 */
@Data
public class Profile {
    private int id;
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
    private String signature; //签名
    private Integer point;
    private Integer isFull;
    private String riseId;
    private Boolean openRise; // 是否打开过rise
    private String unionid;	//只有在用户将公众号绑定到微信开放平台帐号后，才会出现该字段。详见：获取用户个人信息（UnionID机制）
    private Date expireDate; // 过期时间
    private Boolean riseMember;
    private Boolean openApplication; // 是否打开过应用练习
    private Boolean openConsolidation; //  是否打开过巩固练习
    private Integer requestCommentCount; //求点赞次数
    private Integer role;//非db字段 用户角色id


    //默认头像
    public static final String DEFAULT_AVATAR = "https://www.iqycamp.com/images/default_avatar.png";
}
