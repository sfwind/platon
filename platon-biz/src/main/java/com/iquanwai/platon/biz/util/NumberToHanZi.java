package com.iquanwai.platon.biz.util;

/**
 * Created by justin on 16/11/2.
 */
public class NumberToHanZi {

    static String[] units = {"", "十", "百", "千", "万", "十万", "百万", "千万", "亿",
            "十亿", "百亿", "千亿", "万亿"};
    static char[] numArray = {'零', '一', '二', '三', '四', '五', '六', '七', '八', '九'};

    @Deprecated
    public static String formatInteger2(int num) {
        char[] val = String.valueOf(num).toCharArray();
        int len = val.length;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            String m = val[i] + "";
            int n = Integer.valueOf(m);
            boolean isZero = n == 0;
            String unit = units[(len - 1) - i];
            if (isZero) {
                if ('0' == val[i - 1]) {
                    continue;
                } else {
                    sb.append(numArray[n]);
                }
            } else {
                sb.append(numArray[n]);
                sb.append(unit);
            }
        }
        return sb.toString();
    }

    public static String formatInteger(int num) {
        char[] numArr = String.valueOf(num).toCharArray();
        StringBuilder builder = new StringBuilder();
        if (num < 10) {
            builder.append(numArray[num]);
        } else {
            for (int i = 0; i < numArr.length; i++) {
                int c = Integer.parseInt(String.valueOf(numArr[i]));
                if (i != numArr.length - 1) {
                    if (c != 0) {
                        if (c == 1) {
                            if (numArr.length == 2) {
                                builder.append(units[numArr.length - i - 1]);
                            } else {
                                builder.append(numArray[c]);
                                builder.append(units[numArr.length - i - 1]);
                            }
                        } else {
                            builder.append(numArray[c]);
                            builder.append(units[numArr.length - i - 1]);
                        }
                    } else {
                        boolean tag = true;
                        for (int j = i + 1; j < numArr.length; j++) {
                            if (Integer.parseInt(String.valueOf(numArr[j])) != 0) {
                                if (tag && i > 0 && Integer.parseInt(String.valueOf(numArr[i - 1])) != 0) {
                                    builder.append("零");
                                    tag = false;
                                }
                            }
                        }
                    }
                } else {
                    if (c != 0) {
                        builder.append(numArray[c]);
                    }
                }
            }
        }
        return builder.toString();
    }

    public static String formatDecimal(double decimal) {
        String decimals = String.valueOf(decimal);
        int decIndex = decimals.indexOf(".");
        int integ = Integer.valueOf(decimals.substring(0, decIndex));
        int dec = Integer.valueOf(decimals.substring(decIndex + 1));
        String result = formatInteger(integ) + "." + formatFractionalPart(dec);
        return result;
    }

    public static String formatFractionalPart(int decimal) {
        char[] val = String.valueOf(decimal).toCharArray();
        int len = val.length;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int n = Integer.valueOf(val[i] + "");
            sb.append(numArray[n]);
        }
        return sb.toString();
    }

}
