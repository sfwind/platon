package com.iquanwai.platon.biz.util;

import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.common.RiseUserLoginDao;
import com.iquanwai.platon.biz.po.common.Profile;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SensorsTest extends TestBase {
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private RiseUserLoginDao riseUserLoginDao;

    @Test
    public void importTest() throws InvalidArgumentException {
        // 从 Sensors Analytics 获取的数据接收的 URL
        final String SA_SERVER_URL = "http://quanwai.cloud.sensorsdata.cn:8006/sa?project=default&token=0a145b5e1c9814f4";
        // 当缓存的数据量达到50条时，批量发送数据
        final int SA_BULK_SIZE = 50;

        // 使用 BatchConsumer 初始化 SensorsAnalytics
        // 不要在任何线上的服务中使用此 Consumer
        final SensorsAnalytics sa = new SensorsAnalytics(
                new SensorsAnalytics.BatchConsumer(SA_SERVER_URL, SA_BULK_SIZE));


        List<Profile> profiles = profileDao.loadAll(Profile.class);
        profiles.forEach(profile -> {
            // 使用 Sensors Analytics 记录用户行为数据
            String distinctId = profile.getRiseId();
/**
 * 昵称	nickname
 姓名	realname
 性别	sex
 所在省份	province
 所在城市	city
 详细地址
 邮箱
 手机
 首次访问来源	firstChannel
 首次访问时间	firstTime
 注册渠道
 用户角色	rolename
 总订单量	totalOrderCount
 订单总金额	totalOrderAmount
 订单ID	orderList
 总刷题量	totalWarumupCount
 正确题目数量	rightWarmupCount
 错误题目数量	errorWarmupCount
 微信
 累计登录天数
 累积学习体验课次数	experienceCount
 是否完整填完信息
 总积分
 婚恋情况
 收件人
 工作年限
 参加工作年份
 班级	className
 小组	groupId

 */
            // 设置用户性别属性（Sex）为男性
            try {
                if (profile.getReceiver() != null) {
                    sa.profileSet(distinctId, true, "receiver", profile.getReceiver());
                }
                if (profile.getPoint() != null) {
                    sa.profileSet(distinctId, true, "point", profile.getPoint());
                }
                if (profile.getMobileNo() != null) {
                    sa.profileSet(distinctId, true, "mobileNo", profile.getMobileNo());

                }
                if (profile.getProvince() != null) {
                    sa.profileSet(distinctId, true, "province", profile.getProvince());

                }
                if (profile.getAddress() != null) {
                    sa.profileSet(distinctId, true, "address", profile.getAddress());

                }
                if (profile.getAddTime() != null) {
                    sa.profileSet(distinctId, true, "subscribeTime", profile.getAddTime());

                }
                if (profile.getIsFull() != null) {
                    sa.profileSet(distinctId, true, "isFull", profile.getIsFull());

                }
                if (profile.getWorkingLife() != null) {
                    sa.profileSet(distinctId, true, "workingLife", profile.getWorkingLife());

                }
                if (profile.getWorkingYear() != null) {
                    sa.profileSet(distinctId, true, "workingYear", profile.getWorkingYear());

                }
                if (profile.getMarried() != null) {
                    sa.profileSet(distinctId, true, "married", profile.getMarried());

                }
                sa.profileSet(distinctId, true, "loginDays", riseUserLoginDao.loadByProfileId(profile.getId()).size());
                if (profile.getWeixinId() != null) {
                    sa.profileSet(distinctId, true, "weixinid", profile.getWeixinId());

                }
                if (profile.getCity() != null) {
                    sa.profileSet(distinctId, true, "city", profile.getCity());
                }
                if (profile.getRealName() != null) {
                    sa.profileSet(distinctId, true, "realname", profile.getRealName());
                }

                if (profile.getEmail() != null) {
                    sa.profileSet(distinctId, true, "email", profile.getEmail());
                }


            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        });


        // 程序结束前，停止 Sensors Analytics SDK 所有服务
        sa.shutdown();

    }
}
