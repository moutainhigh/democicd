package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.WithholdFlowDTO;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * @author baixinyue
 * @date 2019/12/30
 * @description latpay接口调用
 */

public interface LatPayService {

    /**
     * 提供给其他系统调用
     * @param data
     * @return
     * @throws Exception
     */
    JSONObject thirdSystem(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * latpay卡支付请求
     * @param withholdFlowDTO
     * @param amount
     * @param cardInfo
     * @param request
     * @param ip
     * @return
     * @throws Exception
     */
    WithholdFlowDTO latPayRequest(WithholdFlowDTO withholdFlowDTO, BigDecimal amount, BigDecimal fee, JSONObject cardInfo, HttpServletRequest request, String ip) throws Exception;

    /**
     * latpay账户支付请求
     * @param requestInfo
     * @param request
     * @return
     * @throws Exception
     */
    WithholdFlowDTO latPayDirectDebitRequest(JSONObject requestInfo, WithholdFlowDTO withholdFlowDTO, HttpServletRequest request) throws Exception;

    /**
     * latpay退款请求
     * @param requestInfo
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject latPayRefundRequest(JSONObject requestInfo, HttpServletRequest request) throws Exception;

    /**
     * latpay账户支付异步通知
     * @param requestInfo
     * @param request
     * @throws BizException
     */
    void directDebitNotify(JSONObject requestInfo, HttpServletRequest request) throws BizException;

    /**
     * latpay支付结果查证
     * @param withholdFlowDTO
     * @return
     * @throws Exception
     */
    WithholdFlowDTO latPayDoubtHandle(WithholdFlowDTO withholdFlowDTO) throws Exception;

    /**
     * latpay账户支付跑批查询
     * @throws Exception
     */
    void directDebitStatusCheck() throws Exception;

    /**
     * lapay卡退款请求
     * @param requestInfo
     * @return
     * @throws Exception
     */
    JSONObject latPayRefundCheck(JSONObject requestInfo) throws Exception;
}
