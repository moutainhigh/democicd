package com.uwallet.pay.main.service;

import javax.servlet.http.HttpServletRequest;
import com.alibaba.fastjson.JSONObject;

/**
 * @author baixinyue
 * @date 2020/02/03
 * @description OmiPay接口调用
 */
public interface OmiPayService {

    /**
     * 订单支付结果查询
     * @param orderNo
     * @throws Exception
     */
    Integer statusCheck(String orderNo) throws Exception;

    /**
     * 异步通知获取订单支付结果
     * @param data
     * @throws Exception
     */
    void getPaidOrderInfo(JSONObject data, HttpServletRequest request) throws Exception;

    /**
     * 退款申请
     * @param data
     * @return
     * @throws Exception
     */
    JSONObject refundApply(JSONObject data) throws Exception;

    /**
     * 退款状态查询
     * @param refundNo
     * @return
     * @throws Exception
     */
    Integer refundStatusCheck(String refundNo) throws Exception;

}
