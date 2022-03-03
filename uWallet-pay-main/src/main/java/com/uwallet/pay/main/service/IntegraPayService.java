package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.WithholdFlowDTO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author baixinyue
 * @createDate 2020/06/12
 * @description IntegraPay 业务层
 */
public interface IntegraPayService {

    /**
     * ingtegraPay Access token request
     * @return
     * @throws Exception
     */
    String apiAccessToken() throws Exception;

    /**
     * 添加付款人
     * @param requestInfo
     * @param token
     * @return
     * @throws Exception
     */
    JSONObject addPayer(JSONObject requestInfo, String token) throws Exception;

    /**
     * 获取卡token
     * @param requestInfo
     * @param token
     * @return
     * @throws Exception
     */
    String cardTokenGet(JSONObject requestInfo, String token) throws  Exception;

    /**
     * 付款人添加卡
     * @param requestInfo
     * @param token
     * @param payerUnique
     * @return
     * @throws Exception
     */
    String payerTokenAdd(JSONObject requestInfo, String payerUnique, String token) throws Exception;

    /**
     * 卡支付
     * @param withholdFlowDTO
     * @param token
     * @return
     * @throws Exception
     */
    WithholdFlowDTO payByCard(WithholdFlowDTO withholdFlowDTO, String token) throws Exception;


    /**
     * 卡支付订单状态
     * @param merchantBusinessId
     * @param transactionId
     * @param token
     * @return
     * @throws Exception
     */
    Integer payByCardStatusCheck(String merchantBusinessId, String orderNo, String token) throws Exception;

    /**
     * 付款人添加银行账户
     * @param requestInfo
     * @param payerUnique
     * @param token
     * @return
     * @throws Exception
     */
    String payerAccountAdd(JSONObject requestInfo, String payerUnique, String token) throws Exception;

    /**
     * 账户支付
     * @param data
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject payByAccount(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * 账户支付状态查询
     * @throws Exception
     */
    void payByAccountStatusCheck() throws Exception;

    /**
     * 卡解绑
     * @param payerUnique
     * @param token
     * @throws Exception
     */
    void cardUnbundling (String payerUnique, String token) throws Exception;



}
