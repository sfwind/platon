package com.iquanwai.platon.biz.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class DateUtils {
    private static DateTimeFormatter format1 = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static DateTimeFormatter format2 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static DateTimeFormatter format3 = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    public static String parseDateToString(Date date) {
        return format1.print(new DateTime(date));
    }

    public static Date parseStringToDate(String strDate) {
        return format1.parseDateTime(strDate).toDate();
    }

    public static String parseDateTimeToString(Date date) {
        return format2.print(new DateTime(date));
    }

    public static Date parseStringToDateTime(String strDate) {
        return format2.parseDateTime(strDate).toDate();
    }

    public static int interval(Date date) {
        long now = new Date().getTime();
        long thatTime = date.getTime();

        return Math.abs((int)(now - thatTime)/1000)/60/60/24;
    }

    public static int interval(Date date1, Date date2) {
        long thisTime = date1.getTime();
        long thatTime = date2.getTime();

        return Math.abs((int)(thisTime - thatTime)/1000)/60/60/24;
    }

    public static long currentTimestamp(){
        return System.currentTimeMillis()/1000;
    }

    public static String parseDateToString3(Date date) {
        return format3.print(new DateTime(date));
    }

    public static Date parseStringToDate3(String strDate) {
        return format3.parseDateTime(strDate).toDate();
    }

    public static Date afterMinutes(Date date, int increment){
        return new DateTime(date).plusMinutes(increment).toDate();
    }

    public static Date afterYears(Date date, int increment){
        return new DateTime(date).plusYears(increment).toDate();
    }

    public static Date afterDays(Date date, int increment){
        return new DateTime(date).plusDays(increment).toDate();
    }

    public static Date beforeDays(Date date, int increment){
        return new DateTime(date).minusDays(increment).toDate();
    }
}
