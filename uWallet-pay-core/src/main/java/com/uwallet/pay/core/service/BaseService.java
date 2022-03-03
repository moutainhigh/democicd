package com.uwallet.pay.core.service;


import com.uwallet.pay.core.model.entity.BaseEntity;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author faker
 */
public interface BaseService<T extends BaseEntity> {

    T packAddBaseProps(T base, HttpServletRequest request);

    T packModifyBaseProps(T base, HttpServletRequest request);

    Long getUserId(HttpServletRequest request) ;

    Long getApiUserId(HttpServletRequest request);

    String getIp(HttpServletRequest request);
}
