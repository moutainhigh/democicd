package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 交易详情
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 接入方平台表
 * @author: SHAO
 * @date: Created in 2021-01-20 18:55:53
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: SHAO
 */
public interface TransRecordService extends BaseService {

    /**
     * 计算该User 支付记录条数
     * @param param
     * @return
     */
    int countTransactionRecord(JSONObject param);

    /**
     * 获取交易明细列表
     * @param params
     * @param scs
     * @param pc
     * @return
     */
    Object transactionDetails(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc,HttpServletRequest request) throws Exception;

    /**
     * 交易明细数据列表变为不可见
     * @param id
     * @param request
     * @return
     */
    Object updateRecordIsShow(Long id, HttpServletRequest request);

    /**
     * 卡支付交易详情
     * @param id
     * @return
     */
    Object getRecordDetail(Long id,HttpServletRequest request) throws Exception;

    /**
     * 通过订单查询交易详情
     * @param transNo
     * @param request
     * @return
     * @throws Exception
     */
    Object getRecordDetailByTransNo(String transNo, HttpServletRequest request) throws Exception;

    /**
     * 通过订单查询交易详情
     * @param transNo
     * @param request
     * @return
     * @throws Exception
     */
    Object getRecordDetailByTransNoV2(String transNo, HttpServletRequest request) throws Exception;

    /**
     * 计算交易记录条数 (有条件)
     * @param data
     * @return
     */
    int countRecordNew(JSONObject data);

    /**
     * 列表+条件搜索 交易列表数据: qr_pay_flow
     * @param params
     * @param scs
     * @param pc
     * @param request
     * @return
     */
    Object transactionDetailsNew(JSONObject params, Vector<SortingContext> scs, PagingContext pc, HttpServletRequest request);

    /**
     * 封装起始日期时间戳
     * @param params
     * @return
     */
    Map<String, Object> formatMonth(Map<String, Object> params);
    /**
     * 封装起始日期时间戳
     * @param params
     * @return
     */
    JSONObject formatParamJson(JSONObject params);

    /**
     *  计算交易记录条数新版 (有条件)
     * @param data
     * @return
     */
    int countRecordTwo(JSONObject data,HttpServletRequest request) throws Exception;

    /**
     * 列表+条件搜索 交易列表数据 新版: qr_pay_flow
     * @param data
     * @param scs
     * @param pc
     * @param request
     * @return
     */
    Object transactionDetailsTwo(JSONObject data, Vector<SortingContext> scs, PagingContext pc, HttpServletRequest request) throws Exception;
}
