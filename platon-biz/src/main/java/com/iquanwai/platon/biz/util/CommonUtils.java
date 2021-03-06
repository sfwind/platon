package com.iquanwai.platon.biz.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.iquanwai.platon.biz.exception.ErrorConstants;
import com.iquanwai.platon.biz.exception.WeixinException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by justin on 16/8/7.
 */
public class CommonUtils {
    public static String placeholderReplace(String content, Map<String, String> replacer) {
        if (StringUtils.isNotEmpty(content) && replacer != null) {
            for (Map.Entry<String, String> entry : replacer.entrySet()) {
                content = StringUtils.replace(content, "{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return content;
    }

    public static Map<String, Object> jsonToMap(String json) {
        if (StringUtils.isEmpty(json)) {
            return Maps.newHashMap();
        }
        Map<String, Object> gsonMap = new Gson().fromJson(json,
                new TypeToken<Map<String, Object>>() {
                }.getType());
        return gsonMap;
    }

    public static String mapToJson(Map<String, Object> map) {
        if (MapUtils.isEmpty(map)) {
            return "";
        }
        String json = new Gson().toJson(map,
                new TypeToken<Map<String, Object>>() {
                }.getType());
        return json;
    }

    public static boolean isError(String json) throws WeixinException {
        if (StringUtils.isEmpty(json)) {
            return false;
        }
        Map<String, Object> gsonMap = jsonToMap(json);
        if (gsonMap.get("errcode") != null && gsonMap.get("errmsg") != null) {
            Integer errcode;
            try {
                errcode = ((Double) gsonMap.get("errcode")).intValue();
            } catch (Exception e) {
                errcode = Integer.valueOf((String) gsonMap.get("errcode"));
            }
            if (errcode.equals(ErrorConstants.ACCESS_TOKEN_EXPIRED)) {
                throw new WeixinException(ErrorConstants.ACCESS_TOKEN_EXPIRED, "accessToken过期了");
            }
            if (errcode.equals(ErrorConstants.ACCESS_TOKEN_INVALID)) {
                throw new WeixinException(ErrorConstants.ACCESS_TOKEN_INVALID, "accessToken失效了");
            }
            return errcode != 0;
        }
        return false;
    }

    public static boolean isErrorNoException(String json) {
        if (StringUtils.isEmpty(json)) {
            return false;
        }
        Map<String, Object> gsonMap = jsonToMap(json);
        if (gsonMap.get("errcode") != null && gsonMap.get("errmsg") != null) {
            Integer errcode;
            try {
                errcode = ((Double) gsonMap.get("errcode")).intValue();
            } catch (Exception e) {
                errcode = Integer.valueOf((String) gsonMap.get("errcode"));
            }

            return errcode != 0;
        }
        return false;
    }

    public static String jsSign(final Map<String, String> map) {
        if (map == null) {
            return "";
        }
        List<String> list = new ArrayList(map.keySet());
        Collections.sort(list);

        List<String> kvList = Lists.transform(list, input -> input + "=" + map.get(input));

        String digest = StringUtils.join(kvList.iterator(), "&");
        return MessageDigestHelper.getSHA1String(digest);
    }

    public static String randomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static String randomNumber(int length) {
        String base = "0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    //保留两位小数
    public static Double substract(Double a, Double b) {
        if (a == null || b == null) {
            return null;
        }

        return new BigDecimal(a).subtract(new BigDecimal(b)).
                setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }

    public static String removeStyle(String content) {
        if (content == null) {
            return null;
        }
        // 正则表达式
        String regEx = " style=\"(.*?)\"";
        String regEx2 = " style='(.*?)'";
        Pattern p = Pattern.compile(regEx);
        Pattern p2 = Pattern.compile(regEx2);
        Matcher m = p.matcher(content);
        String okContent = null;
        if (m.find()) {
            okContent = m.replaceAll("");
        } else {
            okContent = content;
        }
        Matcher m2 = p2.matcher(okContent);
        String result = null;
        if (m2.find()) {
            result = m2.replaceAll("");
        } else {
            result = okContent;
        }
        return result;
    }

    public static String removeHTMLTag(String html) {
        if (html == null) {
            return "";
        }
        return StringUtils.removePattern(html, "<[^>]*>");
    }

    public static String replaceHttpsDomainName(String content) {
        String temp = StringUtils.replace(content, "http://www.iqycamp.com", "https://www.iqycamp.com");
        return StringUtils.replace(temp, "http://static.iqycamp.com", "https://static.iqycamp.com");
    }

    private static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) ||
                (codePoint == 0x9) ||
                (codePoint == 0xA) ||
                (codePoint == 0xD) ||
                ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
                ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
    }

    /**
     * 过滤emoji 或者 其他非文字类型的字符
     */
    public static String filterEmoji(String source) {
        //到这里铁定包含
        StringBuilder buf = null;
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (isEmojiCharacter(codePoint)) {
                if (buf == null) {
                    buf = new StringBuilder(source.length());
                }
                buf.append(codePoint);
            }
        }
        if (buf == null) {
            return source;//如果没有找到 emoji表情，则返回源字符串
        } else {
            if (buf.length() == len) {//这里的意义在于尽可能少的toString，因为会重新生成字符串
                return source;
            } else {
                return buf.toString();
            }
        }

    }

}
