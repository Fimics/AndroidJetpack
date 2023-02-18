package com.hnradio.common.util;

import android.content.Context;
import android.provider.Settings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * author: zhaole
 * data: 2018/10/20 20:07
 */
public class ZGDate extends Date {

    // java的int是有符号的，以秒为单位，最多也就表示不到70年。再多会有溢出
    public static final long T_SECOND = 1, T_MINUTE = 60, T_HOUR = T_MINUTE * 60, T_DAY = T_HOUR * 24,
            T_WEEK = T_DAY * 7, T_MONTH = T_DAY * 30, T_YEAR = T_DAY * 365;

    // java的int是有符号的，以毫秒为单位，最多也就表示不到一个月。再多会有溢出，因此使用long
    public static final long T_MS_SECOND = T_SECOND * 1000, T_MS_MINUTE = T_MINUTE * 1000, T_MS_HOUR = T_HOUR * 1000,
            T_MS_DAY = T_DAY * 1000, T_MS_WEEK = T_WEEK * 1000, T_MS_MONTH = T_MONTH * 1000, T_MS_YEAR = T_YEAR * 1000;

    private static SimpleDateFormat dateFormat = null;

    // 格式化字符串，带时间
    public static String toDateTimeString() {
        return toFormatString("yyyy-MM-dd HH:mm:ss");
    }

    // 按照传入字符串格式化
    public static String toFormatString(final String format) {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(format, Locale.CHINA);
        } else {
            dateFormat.applyPattern(format);
        }
        return dateFormat.format(new Date());
    }

    /**
     * 根据一个日期，返回是星期几的字符串
     */
    public static String getIndexOfWeek(Date date) {
        String[] weeks = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (week_index < 0) {
            week_index = 0;
        }
        return weeks[week_index];
    }

    //根据开始时间 得出 hh:mm:ss 格式的时间差
    public static String getDateDiffByDuration(long duration, long maxDuration, boolean isProgress) {
        if (duration <= 0) {
            if (maxDuration >= 216000) {
                return "00:00:00";
            } else {
                return "00:00";
            }
        }
        long diff, day, hour, min, sec;
        String hourStr, minStr, secStr;
        String strData;
        // 获得两个时间的毫秒时间差异
        diff = duration;
        day = diff / T_MS_DAY;// 计算差多少天
        hour = diff % T_MS_DAY / T_MS_HOUR + day * 24;// 计算差多少小时
        min = diff % T_MS_DAY % T_MS_HOUR / T_MS_MINUTE;// 计算差多少分钟
        sec = diff % T_MS_DAY % T_MS_HOUR % T_MS_MINUTE / T_MS_SECOND;// 计算差多少秒
        hourStr = String.valueOf(hour);
        minStr = String.valueOf(min);
        secStr = String.valueOf(sec);
        if ((!isProgress || maxDuration < 3600) && hour == 0) {
            hourStr = "";
        } else if (hour < 10) {
            hourStr = "0".concat(hourStr);
        }
        if (min < 10) {
            minStr = "0".concat(minStr);
        }
        if (sec < 10) {
            secStr = "0".concat(secStr);
        }
        if (hourStr.isEmpty()) {
            strData = hourStr.concat(":").concat(minStr).concat(":").concat(secStr);
        } else {
            strData = minStr.concat(":").concat(secStr);
        }
        return strData;
    }

    //根据开始时间 得出 hh:mm:ss 格式的时间差
    public static String getDateDiff(long startTime) {
        if (startTime <= 0) {
            return "00:00:00";
        }
        long diff, day, hour, min, sec;
        String hourStr, minStr, secStr;
        String strData;
        // 获得两个时间的毫秒时间差异
        diff = System.currentTimeMillis() - (startTime * 1000);
        day = diff / T_MS_DAY;// 计算差多少天
        hour = diff % T_MS_DAY / T_MS_HOUR + day * 24;// 计算差多少小时
        min = diff % T_MS_DAY % T_MS_HOUR / T_MS_MINUTE;// 计算差多少分钟
        sec = diff % T_MS_DAY % T_MS_HOUR % T_MS_MINUTE / T_MS_SECOND;// 计算差多少秒
        hourStr = String.valueOf(hour);
        minStr = String.valueOf(min);
        secStr = String.valueOf(sec);
        if (hour < 10) {
            hourStr = "0".concat(hourStr);
        }
        if (min < 10) {
            minStr = "0".concat(minStr);
        }
        if (sec < 10) {
            secStr = "0".concat(secStr);
        }
        // 输出结果
//        String aaa = "时间相差：" + day + "天" + (hour - day * 24) + "小时"
//                + (min - day * 24 * 60) + "分钟" + sec + "秒。";
//        String bbb = "day=" + day + "hour=" + hour + ",min=" + min;
//        Log.e(TAG , "aaa "+aaa);
//        Log.e(TAG , "bbb "+bbb);
        strData = hourStr.concat(":").concat(minStr).concat(":").concat(secStr);
        return strData;
    }

    //默认的年月日
    public static String formatStrDate2Ymd(String timeStr) {
        long time = 0;
        try {
            time = Long.parseLong(timeStr);
            return formatDate(time, "yyyy-MM-dd", false);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "";
        }

    }

    public static String formatStrDate(String timeStr, String pattern) {
        long time = 0;
        try {
            time = Long.parseLong(timeStr);
            return formatDate(time, pattern, false);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String formatStrDate2Ymd(String timeStr, boolean isHostMain) {
        long time = 0;
        try {
            time = Long.parseLong(timeStr);
            return formatDate(time, "yyyy-MM-dd", isHostMain);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "";
        }

    }

    public static String formatStrDate(String timeStr, String pattern, boolean isHostMain) {
        long time = 0;
        try {
            time = Long.parseLong(timeStr);
            return formatDate(time, pattern, isHostMain);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "";
        }

    }

    public static String formatLongDate2Ymd(long time) {
        return formatDate(time, "yyyy-MM-dd", false);
    }

    public static String formatLongDate2Ymd(long time, boolean isHostMain) {
        return formatDate(time, "yyyy-MM-dd", isHostMain);
    }

    public static String formatLongDate(long time, String pattern) {
        if (time == 0)
            return "00:00";
        return formatDate(time, pattern, false);
    }

    public static String formatLongDate(long time, String pattern, boolean isHostMain) {
        return formatDate(time, pattern, isHostMain);
    }

    // 按照传入字符串格式化
    public static String toFormatString(final String format, long time) {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(format, Locale.CHINA);
        } else {
            dateFormat.applyPattern(format);
        }
        return dateFormat.format(new Date(time));
    }

    //需要 几天前  几小时前的 就传入true 需要  单独格式化时间就传入 pattern
    private static String formatDate(long time, String pattern, boolean isHostMain) {
        if (time > 0) {
            long transmittedLongTime = time;
            if (String.valueOf(transmittedLongTime).length() == 10) {
                transmittedLongTime = time * 1000;
            }
//            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
//            Calendar transmittedTime = Calendar.getInstance();
//            transmittedTime.setTimeInMillis(transmittedLongTime);
            if (!isHostMain) {
//                return format.format(transmittedTime.getTime());
                return toFormatString(pattern, transmittedLongTime);
            }
            long diff, month, day, hour, min, sec;
            String strData = "";
            // 获得两个时间的毫秒时间差异
            diff = System.currentTimeMillis() - transmittedLongTime;
            month = diff / T_MS_MONTH;// 计算差多少月
            day = diff % T_MS_MONTH / T_MS_DAY + month * 30;// 计算差多少天(总)
            hour = diff % T_MS_DAY / T_MS_HOUR + day * 24;// 计算差多少小时(总)
//            min = diff % T_MS_DAY % T_MS_HOUR / T_MS_MINUTE + day * 24 * 60;// 计算差多少分钟
            min = diff % T_MS_DAY % T_MS_HOUR / T_MS_MINUTE + hour * 60;// 计算差多少分钟(总)
            sec = diff / T_MS_SECOND;// 计算差多少秒
            if (sec < 60) {
                strData = "刚刚";
            } else if (min > 0 && min < 60) {
                strData = min + "分钟前";
            } else if (hour > 0 && hour < 24) {
                strData = hour + "小时前";
            } else if (day > 0 && day < 30) {
                strData = day + "天前";
            } else if (month > 0 && month <= 6) {
                strData = month + "个月前";
            } else {
//                strData = format.format(transmittedTime.getTime());
                strData = toFormatString(pattern, transmittedLongTime);
            }
            return strData;
        } else {
            return "";
        }
    }

    private static final int HOUR_SECOND = 60 * 60;
    private static final int MINUTE_SECOND = 60;

    public static String formatTime(long second) {
        if (second <= 0) {
            return "00:00:00";
        }
        StringBuilder sb = new StringBuilder();
        long hours = second / HOUR_SECOND;
        if (hours > 0) {
            second -= hours * HOUR_SECOND;
        }
        long minutes = second / MINUTE_SECOND;
        if (minutes > 0) {
            second -= minutes * MINUTE_SECOND;
        }

        return (hours >= 10 ? (hours + "")
                : ("0" + hours) + ":" + (minutes >= 10 ? (minutes + "") : ("0" + minutes)) + ":"
                + (second >= 10 ? (second + "") : ("0" + second)));
    }

    public static String formatterLongToHMS(long ms, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        String hms = formatter.format(ms);
        return hms;
    }


    private static int judgeDate(Date date) {
        Calendar calendarToday = Calendar.getInstance();
        calendarToday.set(Calendar.HOUR_OF_DAY, 0);
        calendarToday.set(Calendar.MINUTE, 0);
        calendarToday.set(Calendar.SECOND, 0);
        calendarToday.set(Calendar.MILLISECOND, 0);
        Calendar calendarYesterday = Calendar.getInstance();
        calendarYesterday.add(Calendar.DAY_OF_MONTH, -1);
        calendarYesterday.set(Calendar.HOUR_OF_DAY, 0);
        calendarYesterday.set(Calendar.MINUTE, 0);
        calendarYesterday.set(Calendar.SECOND, 0);
        calendarYesterday.set(Calendar.MILLISECOND, 0);
        Calendar calendarBeforeYesterday = Calendar.getInstance();
        calendarBeforeYesterday.add(Calendar.DAY_OF_MONTH, -2);
        calendarBeforeYesterday.set(Calendar.HOUR_OF_DAY, 0);
        calendarBeforeYesterday.set(Calendar.MINUTE, 0);
        calendarBeforeYesterday.set(Calendar.SECOND, 0);
        calendarBeforeYesterday.set(Calendar.MILLISECOND, 0);
        Calendar calendarTomorrow = Calendar.getInstance();
        calendarTomorrow.add(Calendar.DAY_OF_MONTH, 1);
        calendarTomorrow.set(Calendar.HOUR_OF_DAY, 0);
        calendarTomorrow.set(Calendar.MINUTE, 0);
        calendarTomorrow.set(Calendar.SECOND, 0);
        calendarTomorrow.set(Calendar.MILLISECOND, 0);
        Calendar calendarTarget = Calendar.getInstance();
        calendarTarget.setTime(date);
        if (calendarTarget.before(calendarBeforeYesterday)) {
            return 2014;
        } else if (calendarTarget.before(calendarYesterday)) {
            return 14;
        } else if (calendarTarget.before(calendarToday)) {
            return 15;
        } else {
            return calendarTarget.before(calendarTomorrow) ? 6 : 2014;
        }
    }


    public static boolean isTime24Hour(Context context) {
        String timeFormat = Settings.System.getString(context.getContentResolver(), "time_12_24");
        return timeFormat != null && timeFormat.equals("24");
    }

    public static String formatDate(Date date, String fromat) {
        SimpleDateFormat sdf = new SimpleDateFormat(fromat);
        return sdf.format(date);
    }

    /**
     * 判断是否在指定分钟内
     *
     * @param time
     * @param otherTime
     * @param minute
     * @return
     */
    public static boolean isSameDateHours(long time, long otherTime, int minute) {
        if (time == 0 || otherTime == 0) {
            return false;
        }
        long diff = Math.abs(otherTime - time);
        long minuteTime = minute * 60 * 1000;
        if (diff <= minuteTime) {
            return true;
        }
        return false;
    }

}


