package com.iquanwai.platon.biz.po.common;

import com.iquanwai.platon.biz.domain.common.message.SMSDto;
import com.iquanwai.platon.biz.util.DateUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by justin on 17/6/28.
 */
@Data
@NoArgsConstructor
public class SMSValidCode {
    private String code;
    private Date expiredTime;
    private String phone;
    private Integer type;
    private Integer profileId;
    //验证码有效期30分钟
    private static final int TIMEOUT = 30;

    public SMSValidCode(SMSDto smsDto, String code, Integer type) {
        this.phone = smsDto.getPhone();
        this.profileId = smsDto.getProfileId();
        this.type = type;
        this.code = code;
        this.expiredTime = DateUtils.afterMinutes(new Date(), TIMEOUT);
    }
}
