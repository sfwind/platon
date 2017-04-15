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
		localconfig = ConfigFactory.load("localconfig");
		config = ConfigFactory.load("platon");
		fileconfig = ConfigFactory.parseFile(new File("/data/config/localconfig"));
		config = localconfig.withFallback(config);
		config = fileconfig.withFallback(config);
//		zk_switch = config.getBoolean("zk.open");
	}

	public static String getValue(String key){
		String value = zkConfigUtils.getValue(key);

		if(value==null){
			value = config.getString(key);
		}
		return value;
	}

	public static Integer getIntValue(String key){
		Integer value = zkConfigUtils.getIntValue(key);
		if(value==null){
			value = config.getInt(key);
		}
		return value;
	}

	public static Boolean getBooleanValue(String key){
		Boolean value = zkConfigUtils.getBooleanValue(key);
		if(value==null){
			value = config.getBoolean(key);
		}
		return value;
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
		return config.getString("course.start.msg");
	}

	public static String courseCloseMsg(){
		return config.getString("course.pass.msg");
	}

	public static String getPicturePrefix(){
		return config.getString("qiniu.picture.prefix");
	}

	public static Integer preStudySerials(){
		return 3;
	}

	public static String getIntegratedPracticeIndex(){
		return config.getString("integrated.practice.index");
	}
}
