package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.ClearBatchDTO;
import com.uwallet.pay.main.model.dto.H5RefundsRequestDTO;
import com.uwallet.pay.main.model.dto.RefundOrderDTO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 退款服务
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 退款服务
 * @author: zhoutt
 * @date: Created in 2021-08-19 10:36:47
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */

public interface   RefundService {

    /**
     * h5退款
     * @param requestInfo
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject h5Refund(H5RefundsRequestDTO requestInfo, HttpServletRequest request) throws Exception;

    /**
     * 分期付退款查证
     */
    void creditRefundDoubtHandle();

    /**
     * 退款交易查询
     * @param id
     * @param request
     * @return
     */
    JSONObject getPayments(String id, HttpServletRequest request) throws Exception;

    /**
     * 查询时间点之前未清算的退款交易数量
     * @param params
     * @return
     */
    int getH5MerchantRefundCount(Map<String, Object> params);

    /**
     * 退款订单添加批次号
     * @param params
     * @param clearBatchDTO
     * @param request
     */
    void addH5ClearBatchId(Map<String, Object> params, ClearBatchDTO clearBatchDTO, HttpServletRequest request);

    /**
     * 修改清算状态
     * @param map
     */
    void clearData(Map<String, Object> map);

    /**
     * 查询退款数据
     * @param params
     * @return
     */
    List<RefundOrderDTO> merchantClearMessageList(Map<String, Object> params);


    /**
     * 清算回退
     * @param clearBatchId
     * @param merchantId
     * @param request
     * @return
     */
    int rollbackSettlement(Long clearBatchId, Long merchantId, HttpServletRequest request);
}
