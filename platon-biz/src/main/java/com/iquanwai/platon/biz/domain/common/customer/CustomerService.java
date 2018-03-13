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

    /**
     * 获取年度报告
     * @param profileId 用户id
     * @return 年度报告
     */
    AnnualSummary loadUserAnnualSummary(Integer profileId);

    /**
     * 获取年度报告
     * @param riseId 圈外id
     * @return 年度报告
     */
    AnnualSummary loadUserAnnualSummary(String riseId);

    /**
     * 查看一个人是否有权限查看年度总结报告
     */
    Boolean hasAnnualSummaryAuthority(Integer profileId);

    /**
     * 获取用户连续登录天数
     * @param profileId
     * @return
     */
    int loadContinuousLoginCount(Integer profileId);

    /**
     * 获取加入圈外时间
     */
    int loadJoinDays(Integer profileId);

    int loadPersonalTotalPoint(Integer profileId);
}
