package com.uwallet.pay.main.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author xuchenglong
 * 时间工具类
 * @date 2021/4/16
 */
public class TimeUtils {
    /**
     * 悉尼时区
     * */
    private static  String SYDNEY="Australia/Sydney";

    private static  String UTC="UTC";

    /**
     * 时间撮转换悉尼时间
     * */
    public static String timeTransfer(Long time){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimeZone timeZone = TimeZone.getTimeZone(SYDNEY);
        simpleDateFormat.setTimeZone(timeZone);
        String format = simpleDateFormat.format(new Date(time));
        return format;
    }
    /**
     * 时间撮转换悉尼时间 T Z
     * */
    public static String timeTransferTZ(Long time){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        TimeZone timeZone = TimeZone.getTimeZone(UTC);
        simpleDateFormat.setTimeZone(timeZone);
        String format = simpleDateFormat.format(new Date(time));
        return format;
    }
}

