package com.iquanwai.platon.biz.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.util.zk.ZKConfigUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class ConfigUtils {
    private static Config config;
    private static Config fileconfig;
    private static ZKConfigUtils zkConfigUtils;

    private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    static {
        loadLocalConfig();
        zkConfigUtils = new ZKConfigUtils();
    }

    private static void loadLocalConfig() {
        logger.info("load local config");
        config = ConfigFactory.load("localconfig");
        fileconfig = ConfigFactory.parseFile(new File("/data/config/localconfig"));
        config = fileconfig.withFallback(config);
    }

    public static String getValue(String key) {
        String value;
        if (config.hasPath(key)) {
            return config.getString(key);
        } else {
            value = zkConfigUtils.getValue(key);
            if (value == null) {
                value = zkConfigUtils.getArchValue(key);
            }
        }

        if (value != null) {
            // 去掉回车，换行，tab键
            value = value.replaceAll("\r|\n|\t", "");
        }
        return value;
    }

    public static Integer getIntValue(String key) {
        if (config.hasPath(key)) {
            return config.getInt(key);
        } else {
            return zkConfigUtils.getIntValue(key);
        }
    }

    public static Boolean getBooleanValue(String key) {
        if (config.hasPath(key)) {
            return config.getBoolean(key);
        } else {
            return zkConfigUtils.getBooleanValue(key);
        }
    }

    public static String getAppid() {
        return getValue("appid");
    }

    public static boolean logSwitch() {
        return getBooleanValue("open.log");
    }

    public static String getSecret() {
        return getValue("secret");
    }

    public static int getJsSignatureInterval() {
        return getIntValue("js.internal");
    }

    public static boolean isDebug() {
        return getBooleanValue("debug");
    }

    public static boolean isFrontDebug() {
        return getBooleanValue("front.debug");
    }

    public static boolean logDetail() {
        return getBooleanValue("log.debug");
    }

    public static String adapterDomainName() {
        return getValue("adapter.domain");
    }

    public static String realDomainName() {
        return getValue("app.domainname");
    }

    public static String staticResourceUrl() {
        String url = getValue("static.resource.url");
        //测试环境防浏览器缓存，添加随机参数
        if (url.endsWith("?")) {
            url = url.concat("_t=").concat(new Random().nextInt() + "");
        }

        return url;
    }

    public static String staticNoteResourceUrl() {
        String url = getValue("static.note.resource.url");
        //测试环境防浏览器缓存，添加随机参数
        if (url.endsWith("?")) {
            url = url.concat("_t=").concat(new Random().nextInt() + "");
        }

        return url;
    }

    public static Integer getChallengeScore() {
        return getIntValue("challenge.score");
    }

    public static String gaId() {
        return getValue("ga.id");
    }

    public static String getDefaultOpenid() {
        return getValue("default.openid");
    }

    public static Integer getDefaultProfileId() {
        return getIntValue("default.profile.id");
    }

    public static Integer getVoteScore() {
        return getIntValue("vote.score");
    }

    public static String getUploadDomain() {
        return getValue("upload.image.domain");
    }

    @PreDestroy
    public void destroy() {
        zkConfigUtils.destroy();
    }

    public static String courseStartMsg() {
        return getValue("course.start.msg");
    }

    public static String courseCloseMsg() {
        return getValue("course.pass.msg");
    }

    public static String incompleteTaskMsg() {
        return getValue("incomplete.task.msg");
    }

    public static String getPicturePrefix() {
        return getValue("qiniu.picture.prefix");
    }

    public static String getIntegratedPracticeIndex() {
        return getValue("integrated.practice.index");
    }

    public static Boolean isDevelopment() {
        return getBooleanValue("development");
    }

    public static Integer getProfileFullScore() {
        return getIntValue("profile.full.score");
    }

    public static Boolean isHttps() {
        return getBooleanValue("open.https");
    }

    public static String domainName() {
        return getValue("app.domain");
    }

    public static Boolean isForumTest() {
        return getBooleanValue("rise.forum.test");
    }

    public static Integer getForumQuestionOpenPoint() {
        return getIntValue("forum.question.open.point");
    }

    public static Integer getForumQuestionFollowPoint() {
        return getIntValue("forum.question.follow.point");
    }

    public static String sendShortMessageUrl() {
        return getValue("send.sms.url");
    }

    /**
     * 获取限免小课 ProblemId
     */
    public static Integer getTrialProblemId() {
        return getIntValue("rise.trial.problem.id");
    }

    public static Double getRiseCourseFee() {
        return getDoubleValue("rise.course.fee");
    }

    public static Double getDoubleValue(String key) {
        if (config.hasPath(key)) {
            return config.getDouble(key);
        } else {
            Double value = zkConfigUtils.getDoubleValue(key);
            if (value == null) {
                value = zkConfigUtils.getDoubleValue(key);
            }
            return value;
        }
    }

    public static String getWelcomeMsg() {
        return getValue("login.welcome.msg");
    }

    /**
     * 获取精华卡片背景图
     */
    public static String getEssenceCardBackImgs() {
        return getValue("rise.problem.cards.essence");
    }

    /**
     * 获取精华卡片缩略图
     */
    public static String getEssenceCardThumbnails() {
        return getValue("rise.problem.cards.thumbnail");
    }

    /**
     * 获取精华卡片锁定图
     */
    public static String getEssenceCardThumbnailsLock() {
        return getValue("rise.problem.cards.thumbnail.lock");
    }

    /**
     * 1、分享卡片成功推送
     */
    public static String getShareCodeSuccessMsg() {
        return getValue("share.card.msg");
    }

    /**
     * 1、领取卡券通知
     */
    public static String getReceiveCouponMsg() {
        return getValue("card.coupon.msg");
    }

    /**
     * 获得优惠券领奖人数
     */
    public static Integer getFreeLimitSuccessCnt() {
        return getIntValue("free.limit.success.cnt");
    }

    /**
     * 成功报名模板消息
     */
    public static String getSignUpSuccessMsg() {
        return getValue("signUp.success");
    }

    /**
     * 获取应用练习得分列表
     */
    public static Map<Integer, Integer> getWorkScoreMap() {
        String scores = getValue("work.difficulty.score");
        String[] split = scores.split(",");
        Map<Integer, Integer> scoreMap = Maps.newHashMap();
        for (int i = 0; i < split.length; i++) {
            scoreMap.put(i + 1, Integer.parseInt(split[i]));
        }
        return scoreMap;
    }

    /**
     * 读取热门小课配置
     */
    public static List<Integer> loadHotProblemList() {
        String idStr = getValue("problem.hot.list");
        String[] idStrs = idStr.split(",");

        List<Integer> problemIds = Lists.newArrayList();
        for (String idStr1 : idStrs) {
            try {
                Integer id = Integer.parseInt(idStr1);
                problemIds.add(id);
            } catch (NumberFormatException e) {
                logger.error("错误的数字:" + idStr1, e);
            }
        }
        return problemIds;
    }

    public static Date getRiseMemberSplitDate() {
        String splitDateStr = getValue("risemember.elite.splitdate");
        return DateUtils.parseStringToDate(splitDateStr);
    }

    /**
     * 获取发现页面 Banner 配置
     */
    public static String getExploreBannerString() {
        return getValue("explore.banner");
    }

    public static String sendCertificateMsg() {
        return getValue("send.certificate.msg");
    }

    public static String productChangeMsg() {
        return getValue("product.change.msg");
    }

    public static String getAuditionPushMsg() {
        return getValue("audition.choose.msg.media.id");
    }

    public static List<String> getDevelopOpenIds() {
        String openIdsStr = getValue("sms.alarm.openids");
        return Lists.newArrayList(openIdsStr.split(","));
    }

    /**
     * 账户变动提醒
     */
    public static String getAccountChangeMsg() {
        return getValue("account.change.message");
    }

    public static Integer getLearningYear() {
        return getIntValue("learning.year");
    }

    public static Integer getLearningMonth() {
        return getIntValue("learning.month");
    }

    public static String getCertificateSaveFolder() {
        return getValue("certificate.local.save.folder");
    }

    /**
     * 获取训练营宣传 banner 连接
     */
    public static String getCampProblemBanner() {
        return getValue("camp.problem.banner");
    }

}

