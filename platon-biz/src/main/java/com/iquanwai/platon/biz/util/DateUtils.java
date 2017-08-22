package com.iquanwai.platon.biz.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    private static DateTimeFormatter format1 = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static DateTimeFormatter format2 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static DateTimeFormatter format3 = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private static DateTimeFormatter format4 = DateTimeFormat.forPattern("yyyy.MM.dd");
    private static DateTimeFormatter format5 = DateTimeFormat.forPattern("yyyy年MM月dd日");
    private static DateTimeFormatter format6 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
    private static DateTimeFormatter timeFormat = DateTimeFormat.forPattern("HH:mm");

    public static String parseDateToFormat5(Date date){
        return format5.print(new DateTime(date));
    }
    public static String parseDateToFormat6(Date date){
        return format6.print(new DateTime(date));
    }
    public static String parseDateToString(Date date) {
        return format1.print(new DateTime(date));
    }
    public static String parseDateToStringByCommon(Date date){
        return format4.print(new DateTime(date));
    }
    public static String parseDateToTimeFormat(Date date){
        return timeFormat.print(new DateTime(date));
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

        return (int)Math.abs((now - thatTime)/1000)/60/60/24;
    }

    public static int interval(Date date1, Date date2) {
        long thisTime = date1.getTime();
        long thatTime = date2.getTime();

        return (int)Math.abs((thisTime - thatTime)/1000)/60/60/24;
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

    public static Date startDay(Date date){
        return new DateTime(date).withTimeAtStartOfDay().toDate();
    }

    public static Date afterYears(Date date, int increment){
        return new DateTime(date).plusYears(increment).toDate();
    }

    public static Date afterMonths(Date date,int increment){
        return new DateTime(date).plusMonths(increment).toDate();
    }

    public static Date afterDays(Date date, int increment){
        return new DateTime(date).plusDays(increment).toDate();
    }

    public static Date beforeDays(Date date, int increment){
        return new DateTime(date).minusDays(increment).toDate();
    }

    public static boolean isSameDate(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        boolean isSameYear = cal1.get(Calendar.YEAR) == cal2
                .get(Calendar.YEAR);
        boolean isSameMonth = isSameYear
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        boolean isSameDate = isSameMonth
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2
                .get(Calendar.DAY_OF_MONTH);

        return isSameDate;
    }

    public static Date endDateOfMonth(Integer month) {
        DateTime date = new DateTime().monthOfYear().setCopy(month).dayOfMonth().withMaximumValue();
        return date.toDate();
    }

}
