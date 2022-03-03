package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.exception.BizException;

/**
 * @author: liming
 * @Date: 2019/10/29 09:42
 * @Description: 阿里云短信服务
 */

public interface AliyunSmsService {

    /**
     * 批量发送短信
     *
     * @param phone 手机号 格式：13100000000
     * @param modelCode 阿里云模版code
     * @param templateParamJson 模版占位符参数 格式：[{"code":"xxx"}]
     * @throws BizException 调用失败抛出异常
     */
    void sendChinaSms(String phone, String modelCode, JSONObject templateParams) throws BizException;

    /**
     * 国际短信
     *
     * @param phone
     * @param message
     * @return
     */
    void sendInternationalSms(String phone, String message) throws BizException;


    /**
     * 国际短信
     *
     * @param phone
     * @param message
     * @return
     */
    boolean sendInternationalSmsV2(String phone, String message);
}
