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
    private Integer Device; // 1-pc 2-mobile
    private Boolean openWelcome; //是否打开过欢迎页

    public static LoginUser defaultUser() {
        return new LoginUser(ConfigUtils.getDefaultProfileId(), ConfigUtils.getDefaultUnionId(), ConfigUtils.getDefaultOpenid(), "风之伤",
                Profile.DEFAULT_AVATAR, null, 5, null, true, true, 0, true, true, 1, true);
    }

    public enum Platform {
        PC(1), WE_MOBILE(2), WE_MINI(3);

        private int value;

        Platform(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /** 三种平台对应 header 中 platform 的值 */
    public interface PlatformHeaderValue {
        /** pc */
        public String PC_HEADER = "pc";
        /** 移动端 */
        public String WE_MOBILE_HEADER = "we_mobile";
        /** 小程序 */
        public String WE_MINI_HEADER = "we_mini";
    }

}
