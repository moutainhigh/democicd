package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import lombok.NonNull;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

/**
 * <p>
 * DocuSign服务
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 董事信息表
 * @author: aaronS
 * @date: Created in 2020-07-28 10:23:38
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: aaronS
 */
public interface DocuSignService extends BaseService {
    /**
     * 通过用户信息,合同文件生成签署链接
     * @param jsonObject
     * @param request
     * @return
     */
    JSONObject genSignUrl(@NonNull  JSONObject jsonObject, HttpServletRequest request)  throws Exception;

    /**
     * 获取用户签署的合同文件 上传到亚马逊文件服务器并返回存储链接地址
     * @param jsonObject
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject getDocument(@NotNull JSONObject jsonObject, HttpServletRequest request) throws Exception;
}
