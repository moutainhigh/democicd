package com.uwallet.pay.main.util;

import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * 国际化工具类
 * @author faker
 */
@Component
public class I18nUtils {
    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private AdminService adminService;

    private static MessageSource messageSource;

    public I18nUtils(MessageSource messageSource) {
        I18nUtils.messageSource = messageSource;
    }

    /**
     * 获取单个国际化翻译值
     */
    public static String get(String msgKey, Locale locale) {
        try {
            return messageSource.getMessage(msgKey, null, locale);
        } catch (Exception e) {
            return msgKey;
        }
    }

    /**
     * 获取单个国际化翻译值
     */
    public static String get(String msgKey, Locale locale, String[] messages) {
        try {
            String returnMessage = messageSource.getMessage(msgKey, null, locale);
            if (messages != null) {
                return MessageFormat.format(returnMessage, messages);
            }
            return returnMessage;
        } catch (Exception e) {
            return msgKey;
        }
    }

}
