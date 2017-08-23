package com.iquanwai.platon.biz.util;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.util.zk.ZKConfigUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class ConfigUtils {
    private static Config config;
    private static Config localconfig;
    private static Config fileconfig;
    private static ZKConfigUtils zkConfigUtils;

//	private static boolean zk_switch = false;

    private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    private static Timer timer;

    static {
        loadConfig();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                loadConfig();
            }
        }, 0, 1000 * 60);
        zkConfigUtils = new ZKConfigUtils();
    }

    private static void loadConfig() {
        config = ConfigFactory.load("localconfig");
//		localconfig = ConfigFactory.load("localconfig");
//		config = ConfigFactory.load("platon");
        fileconfig = ConfigFactory.parseFile(new File("/data/config/localconfig"));
//		config = localconfig.withFallback(config);
        config = fileconfig.withFallback(config);
//		zk_switch = config.getBoolean("zk.open");
    }

    public static String getValue(String key) {
        String value = null;
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
        return getBooleanValue("debug") || getBooleanValue("press.test");
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

    public static String getPicturePrefix() {
        return getValue("qiniu.picture.prefix");
    }

    public static Integer preStudySerials() {
        return 3;
    }

    public static String getIntegratedPracticeIndex() {
        return getValue("integrated.practice.index");
    }

    public static Boolean prePublish() {
        return getBooleanValue("rise.pre.publish");
    }

    public static String getRabbitMQIp() {
        return getValue("rabbitmq.ip");
    }

    public static String getRabbitMQUser() {
        return getValue("rabbitmq.user");
    }

    public static String getRabbitMQPasswd() {
        return getValue("rabbitmq.password");
    }

    public static int getRabbitMQPort() {
        return getIntValue("rabbitmq.port");
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
     * 获取训练营小课安排 json
     */
    public static String getRequiredClassSchedule() {
        return getValue("requiredClass.schedule");
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

}

