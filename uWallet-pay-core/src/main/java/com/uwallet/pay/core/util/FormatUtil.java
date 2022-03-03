package com.uwallet.pay.core.util;

import com.amazonaws.services.dynamodbv2.xspec.S;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: liming
 * @Date: 2019/9/27 15:53
 * @Description: 格式化时间
 */
public class FormatUtil {

    private static Pattern linePattern = Pattern.compile("_(\\w)");

    /**
     * 根据 format 格式化时间
     * @param date 时间
     * @param format 格式化模版
     * @return
     */
    public static String getTodayTimeField(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    /**
     * 根据 format 格式化时间
     * @param date
     * @param format
     * @return
     */
    public static String getDate(String date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        Date getDate = null;
        try {
            getDate = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return sdf.format(getDate);
    }

    /**
     * 截取字符串
     * @param data 待截取的数据
     * @param endIndex 结束索引
     * @return
     */
    public static String getStringFormat(String data, Integer endIndex) {
        String result = data;
        if (result == null) {
            result = " ";
        }
        if (result.length() > endIndex) {
            result = result.substring(0, endIndex);
        }
        return result;
    }

    /**
     * 下划线转驼峰
     * @param str
     * @return
     */
    public static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
