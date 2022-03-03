package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.service.BaseService;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author baixinyue
 * @createdDate 2020-11-25
 * @description split三方
 */

public interface SplitService extends BaseService {

    /**
     * split账户绑定
     * @param requestInfo
     * @param request
     * @return
     */
    JSONObject splitTieAccount(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * split账户删除
     * @param requestInfo
     * @param request
     */
    void splitDeleteAccount(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * split还款请求
     * @param requestInfo
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject splitPayRequestInfo(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * split 交易查询
     * @param requestInfo
     * @param request
     * @return
     * @throws Exception
     */
    JSONArray splitTransactionSearch(Map<String, Object> params, HttpServletRequest request) throws Exception;

    /**
     * split 交易通知
     * @param requestInfo
     * @param request
     * @throws Exception
     */
    void splitTransactionNotify(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * split 交易中跑批查询
     */
    void splitTransactionDoubleHandle();

}
