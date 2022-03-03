package com.uwallet.pay.core.service.impl;

import com.uwallet.pay.core.model.entity.BaseEntity;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.core.util.JwtUtils;
import com.uwallet.pay.core.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * @author faker
 */
public class BaseServiceImpl<T extends BaseEntity> implements BaseService<T> {

    @Override
    public T packAddBaseProps(T base, HttpServletRequest request) {
        long now = System.currentTimeMillis();
        if(null != request){
            Long currentLoginId = getUserId(request);
            base.setCreatedBy(currentLoginId);
            base.setModifiedBy(currentLoginId);
            base.setIp(getIp(request));
        }
        base.setId(SnowflakeUtil.generateId());
        base.setCreatedDate(now);
        base.setModifiedDate(now);
        base.setStatus(1);
        return base;
    }

    @Override
    public T packModifyBaseProps(T base, HttpServletRequest request) {
        if(null != request){
            base.setModifiedBy(getUserId(request));
            base.setIp(getIp(request));
        }
        base.setModifiedDate(System.currentTimeMillis());
        return base;
    }

    @Override
    public Long getUserId(HttpServletRequest request) {
        // 通过token获取操作人id
        String jwt = request.getHeader("Authorization");
        Long id = 0L;
        if (StringUtils.isNotEmpty(jwt)) {
            id = JwtUtils.getId(jwt.replace("Bearer ", ""));
        }
        return id;
    }

    @Override
    public Long getApiUserId(HttpServletRequest request) {
        // 通过token获取操作人id
        String jwt = request.getHeader("apiToken");
        Long id = 0L;
        if (StringUtils.isNotEmpty(jwt)) {
            id = JwtUtils.getId(jwt.replace("Bearer ", ""));
        }
        return id;
    }

    @Override
    public String getIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    /**
     * 获取当前语言，默认保持英文
     * @author faker
     * @param request
     * @return
     */
    public Locale getLang(HttpServletRequest request) {
        Locale lang = Locale.US;
        // 获取当前语言
        if (request!=null){
            String headerLang = request.getHeader("lang");
            Locale locale = LocaleContextHolder.getLocale();
            if (StringUtils.isNotEmpty(headerLang) && "zh-CN".equals(headerLang)) {
                lang = Locale.SIMPLIFIED_CHINESE;
            }
        }
        return lang;
    }
}
