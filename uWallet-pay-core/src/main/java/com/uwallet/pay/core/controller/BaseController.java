package com.uwallet.pay.core.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.model.entity.BaseEntity;
import com.uwallet.pay.core.util.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author faker
 * @param <T>
 */
public class BaseController<T extends BaseEntity> {

    private static final Pattern SC_EXTRACTION = Pattern.compile("(?<field>[0-9a-zA-z]+)\\((?<sort>[0-9a-zA-z]+)\\)", 2);
    private static final int MAX_PAGE_SIZE = 1000;
    private static final String SEPARATOR = ",";

    /**
     * 获取排序字段
     * 采用固定格式：scs=type(asc),enabled(desc)表示先按type升序排，再按enabled降序排
     */
    public Vector<SortingContext> getSortingContext(HttpServletRequest req) {
        String scsValue = req.getParameter("scs");
        return sortingContext(scsValue);
    }

    /**
     * 获取排序字段
     * 采用固定格式：scs=type(asc),enabled(desc)表示先按type升序排，再按enabled降序排
     */
    public Vector<SortingContext> getSortingContext(JSONObject jsonObject) {
        String scsValue = jsonObject.getString("scs");
        return sortingContext(scsValue);
    }

    private Vector<SortingContext> sortingContext(String scsValue) {
        Vector<SortingContext> scs = new Vector<>();
        if (StringUtils.isNotEmpty(scsValue)) {
            String[] sortFields = scsValue.split(SEPARATOR);

            for (String scStr : sortFields) {
                Matcher matcher = SC_EXTRACTION.matcher(scStr);
                if (matcher.matches()) {
                    String field = matcher.group("field");
                    String o = matcher.group("sort");
                    String order = "asc".equalsIgnoreCase(o) ? "ASC" : "DESC";
                    if (StringUtils.isNotEmpty(field) && StringUtils.isNotEmpty(order)) {
                        SortingContext sc = new SortingContext(field, order);
                        scs.add(sc);
                    }
                }
            }
        }
        return scs;
    }

    /**
     * p = pageIndex 第几页
     * s = pageSize  一页几条
     */
    public PagingContext getPagingContext(JSONObject jsonObject, Integer total) {
        String s = jsonObject.getString("s");
        String p = jsonObject.getString("p");
        return pagingContext(s, p, total);
    }

    /**
     * p = pageIndex 第几页
     * s = pageSize  一页几条
     */
    public PagingContext getPagingContext(HttpServletRequest req, Integer total) {
        String s = req.getParameter("s");
        String p = req.getParameter("p");
        return pagingContext(s, p, total);
    }

    private PagingContext pagingContext(String s, String p, Integer total) {
        PagingContext pc = new PagingContext();
        pc.setTotal(total);
        if (StringUtils.isNumeric(s)) {
            int size = Integer.parseInt(s);
            if (size > 0 && size < MAX_PAGE_SIZE) {
                pc.setPageSize(size);
            } else {
                pc.setPageSize(total <= MAX_PAGE_SIZE ? total : MAX_PAGE_SIZE);
            }
        } else {
            // s传all的时候
            pc.setPageSize(total);
        }
        if (StringUtils.isNumeric(p)) {
            pc.setPageIndex(Integer.parseInt(p));
        } else {
            pc.setPageIndex(1);
        }
        return pc;
    }

    public Map<String, Object> getConditionsMap(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>(15);
        Enumeration enu = request.getParameterNames();
        while (enu.hasMoreElements()) {
            String paraName = (String) enu.nextElement();
            String parameterValue = request.getParameter(paraName).trim();
            if (StringUtils.isNotEmpty(paraName) && StringUtils.isNotEmpty(parameterValue)) {
                map.put(paraName, parameterValue);
            }
        }
        return map;
    }

    public Long getUserId(HttpServletRequest request) {
        //通过token获取操作人id
        String jwt = request.getHeader("Authorization");
        Long id = 1L;
        if(StringUtils.isNotEmpty(jwt)){
            id = JwtUtils.getId(jwt.replace("Bearer ", ""));
        }
        return id;
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
        String headerLang = request.getHeader("lang");
        Locale locale = LocaleContextHolder.getLocale();
        if (StringUtils.isNotEmpty(headerLang) && "zh-CN".equals(headerLang)) {
            lang = Locale.SIMPLIFIED_CHINESE;
        }
        return lang;
    }

}
