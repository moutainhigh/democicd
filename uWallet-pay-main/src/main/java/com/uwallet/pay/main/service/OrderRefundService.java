package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.OrderRefundDTO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author baixinyue
 * @description 退款交易业务层
 * @createDate 2020/02/07
 */

public interface OrderRefundService {

    /**
     * 卡支付全额退款
     * @param request
     * @throws Exception
     */
    JSONObject cardTotalRefundApply(OrderRefundDTO orderRefundDTO, HttpServletRequest request) throws Exception;

    /**
     * 退款申请
     * @param data
     * @param request
     * @return
     * @throws Exception
     */
    void refundApply(OrderRefundDTO orderRefundDTO, HttpServletRequest request) throws Exception;

    /**
     * 退款状态查询并修改状态
     * @throws Exception
     */
    void refundStatusCheck() throws Exception;

    /**
     * 出账回滚失败在处理
     * @throws Exception
     */
    void refundFailedAccountRollbackFailedHandle() throws Exception;

    /**
     * 出账回滚可疑处理
     */
    void refundFailedAccountRollbackDoubtHandle() throws Exception;

    /**
     * 退款出账可疑操作
     * @throws Exception
     */
    void refundFailedAccountOut() throws Exception;

}
