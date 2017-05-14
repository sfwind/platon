package com.iquanwai.platon.biz.util;

import com.iquanwai.platon.biz.util.zk.ZKConfigUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
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
	static{
		loadConfig();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				loadConfig();
			}
		}, 0, 1000*60);
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

	public static String getValue(String key){
		if (config.hasPath(key)) {
			return config.getString(key);
		} else {
			String value = zkConfigUtils.getValue(key);
			if (value == null) {
				value = zkConfigUtils.getArchValue(key);
			}
			return value;
		}
	}

	public static Integer getIntValue(String key){
		if (config.hasPath(key)) {
			return config.getInt(key);
		} else {
			return zkConfigUtils.getIntValue(key);
		}
	}

	public static Boolean getBooleanValue(String key){
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

	public static String getAPIKey() {
		return getValue("api.key");
	}

	public static String getSecret() {
		return getValue("secret");
	}

	public static int getJsSignatureInterval() {
		return getIntValue("js.internal");
	}

	public static boolean isDebug(){
		return getBooleanValue("debug")||getBooleanValue("press.test");
	}

	public static boolean isFrontDebug(){
		return getBooleanValue("front.debug");
	}

	public static boolean logDetail(){
		return getBooleanValue("log.debug");
	}

	public static String adapterDomainName(){
		return getValue("adapter.domain");
	}


	public static String realDomainName(){
		return getValue("app.domainname");
	}

	public static String staticResourceUrl(){
		String url = getValue("static.resource.url");
		//测试环境防浏览器缓存，添加随机参数
		if(url.endsWith("?")){
			url = url.concat("_t=").concat(new Random().nextInt()+"");
		}

		return url;
	}

	public static Integer getChallengeScore(){
		return getIntValue("challenge.score");
	}

	public static String gaId(){
		return getValue("ga.id");
	}

	public static String getDefaultOpenid(){
		return getValue("default.openid");
	}

	public static Integer getVoteScore(){
		return getIntValue("vote.score");
	}

	public static String getUploadDomain(){
		return getValue("upload.image.domain");
	}

	@PreDestroy
	public void destroy(){
		zkConfigUtils.destroy();
	}

	public static String courseStartMsg(){
		return getValue("course.start.msg");
	}

	public static String productTrailMsg(){
		return getValue("product.trial.msg");
	}

	public static String courseCloseMsg(){
		return getValue("course.pass.msg");
	}

	public static String getPicturePrefix(){
		return getValue("qiniu.picture.prefix");
	}

	public static Integer preStudySerials(){
		return 3;
	}

	public static String getIntegratedPracticeIndex(){
		return getValue("integrated.practice.index");
	}

	public static Boolean prePublish(){
		return getBooleanValue("rise.pre.publish");
	}

	public static String getRabbitMQIp(){
		return getValue("rabbitmq.ip");
	}

	public static int getRabbitMQPort(){
		return getIntValue("rabbitmq.port");
	}

	public static Boolean isDevelopment(){
		return getBooleanValue("development");
	}

	public static Integer getProfileFullScore(){
		return getIntValue("profile.full.score");
	}

	public static Boolean isHttps(){
		String value = zkConfigUtils.getValue("/quanwai/config/arch/", "open.https");
		if(value == null){
			return false;
		}

		return Boolean.valueOf(value);
	}

}
