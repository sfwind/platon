package com.iquanwai.platon.biz.domain.common.customer;

import com.iquanwai.platon.biz.po.AnnualSummary;
import com.iquanwai.platon.biz.po.common.Feedback;

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
     * @param riseId 圈外id
     * @return 年度报告
     */
    AnnualSummary loadUserAnnualSummary(String riseId);

    /**
     * 学员提交意见反馈
     */
    void sendFeedback(Feedback feedback);
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

    /**
     * 获取该身份用户的收到的消息
     */
    String loadAnnounceMessage(Integer profileId);
}
