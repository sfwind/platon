package com.iquanwai.platon.biz.domain.common.customer;

import com.iquanwai.platon.biz.po.AnnualSummary;

import java.io.InputStream;

public interface CustomerService {
    /**
     * 更新学员头像
     */
    String uploadHeadImage(Integer profileId, String fileName, InputStream inputStream);

    int updateHeadImageUrl(Integer profileId, String headImgUrl);

    /**
     * 更新学员昵称
     */
    int updateNickName(Integer profileId, String nickName);

    AnnualSummary loadUserAnnualSummary(Integer profileId);
}
