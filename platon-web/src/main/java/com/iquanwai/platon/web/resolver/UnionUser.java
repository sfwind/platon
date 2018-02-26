package com.iquanwai.platon.web.resolver;

import com.iquanwai.platon.biz.util.ConfigUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by 三十文
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnionUser {

    private Integer id;
    private String openId;
    private String unionId;
    private String nickName;
    private String headImgUrl;

    public static UnionUser defaultUser() {
        return new UnionUser(
                ConfigUtils.getDefaultProfileId(),
                ConfigUtils.getDefaultOpenid(),
                ConfigUtils.getDefaultUnionId(),
                "三十文",
                "http://wx.qlogo.cn/mmopen/Q3auHgzwzM7wkhob9zgicD3IJxG1tLVSSe9qdzR1qUGXz6BwPv73sr67iaTEibcA1sNic3Roib4DgXCVG4IWe0zPAKJnlo5r4NibezssS6naic6dkM/0"
        );
    }

    public enum Platform {
        PC(1), MOBILE(2), MINI(3);
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
        String PC_HEADER = "pc";
        /** 移动端 */
        String MOBILE_HEADER = "we_mobile";
        /** 小程序 */
        String MINI_HEADER = "we_mini";
    }

}
