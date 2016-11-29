package com.iquanwai.platon.biz.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class ConfigUtils {
	private static Config config;
	private static Config localconfig;
	private static Config fileconfig;

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
	}

	private static void loadConfig() {
		localconfig = ConfigFactory.load("localconfig");
		config = ConfigFactory.load("platon");
		fileconfig = ConfigFactory.parseFile(new File("/data/config/localconfig"));
		config = localconfig.withFallback(config);
		config = fileconfig.withFallback(config);
	}

	public static String getAppid() {
		return config.getString("appid");
	}

	public static boolean logSwitch() {
		return config.getBoolean("open.log");
	}

	public static boolean pressTestSwitch(){
		return config.getBoolean("press.test");
	}

	public static String getAPIKey() {
		return config.getString("api.key");
	}

	public static String getSecret() {
		return config.getString("secret");
	}

	public static int getJsSignatureInterval() {
		return config.getInt("js.internal");
	}

	public static boolean isDebug(){
		return config.getBoolean("debug")||config.getBoolean("press.test");
	}

	public static boolean isFrontDebug(){
		return config.getBoolean("front.debug");
	}

	public static boolean logDetail(){
		return config.getBoolean("log.debug");
	}

	public static String getMch_id(){
		return config.getString("mch_id");
	}

	public static String getExternalIP(){
		return config.getString("external.ip");
	}

	public static Integer getBillOpenMinute(){
		return config.getInt("bill.open.minute");
	}

	public static String signupSuccessMsgKey(){
		return config.getString("signup.success.msg");
	}

	public static String angelMsgKey(){
		return config.getString("angel.msg");
	}

	public static String incompleteTaskMsgKey(){
		return config.getString("incomplete.task.msg");
	}

	public static boolean messageSwitch(){
		return config.getBoolean("message.switch");
	}

	public static String coursePassMsgKey(){
		return config.getString("course.pass.msg");
	}

	public static String domainName(){
		return config.getString("app.domain");
	}

	public static String realDomainName(){
		return config.getString("app.domainname");
	}

	public static String staticResourceUrl(){
		String url = config.getString("static.resource.url");
		//测试环境防浏览器缓存，添加随机参数
		if(url.endsWith("?")){
			url = url.concat("_t=").concat(new Random().nextInt()+"");
		}

		return url;
	}

	public static String gaId(){
		return config.getString("ga.id");
	}

	public static String getDefaultOpenid(){
		return config.getString("default.openid");
	}

	public static String getValue(String key){
		return config.getString(key);
	}
}
