package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.main.exception.PosApiException;
import org.springframework.scheduling.annotation.Async;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * pos api
 * </p>
 *
 * @package: com.uwallet.pay.main.service
 * @description: POS apis ervice
 * @author: zhangzeyuan
 * @date: Created in 2021-03-19 16:45:03
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
public interface PosApiService extends BaseService {

    /**
     * 生成POS商户二维码信息
     * @author zhangzeyuan
     * @date 2021/3/24 16:22
     * @param requestInfo
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     */
    JSONObject createPosQrCode(JSONObject requestInfo, HttpServletRequest request) throws PosApiException;


    /**
     * 支付成功之后通知POS端
     *
     * @param merchantId
     * @param transNo
     * @param request
     * @author zhangzeyuan
     * @date 2021/3/23 16:19
     */

    void posPaySuccessNotice(Long merchantId, String transNo, HttpServletRequest request) throws PosApiException;


    /**
     * 获取POS订单历史交易信息
     *
     * @param requestInfo
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2021/3/23 14:27
     */
    JSONObject getPosTransactionRecord(JSONObject requestInfo, HttpServletRequest request) throws PosApiException;

}
