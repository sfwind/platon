package com.iquanwai.platon.web.personal.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/2/4.
 */
@Data
public class ProfileDto {
    private String industry; //行业
    private String function; //职业
    private String workingLife; //工作年限
    private String workingYear; // 参加工作年份
    private Integer workingTimeId; //工作年份id
    private String city; //城市
    private Integer cityId; //城市id
    private String province; //省份
    private Integer provinceId; //省份id
    private Boolean isFull; //是否已经填完整
    private Boolean bindMobile; //是否绑定手机号或微信
    private String realName; //真名
    private String address;
    private String phone;
    private String weixinId;
    private String receiver;//收件人
}
