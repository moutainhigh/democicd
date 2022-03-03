package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONObject;
import com.stripe.model.Event;
import com.uwallet.pay.core.common.StripeAPIResponse;
import com.uwallet.pay.main.model.dto.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zhangzeyuan
 * @date 2022年01月10日 15:12
 */

public interface StripeAPIService {

    /**
     * 获取客户信息
     * @author zhangzeyuan
     * @date 2022/1/10 16:47
     * @param userId
     */
    StripeAPIResponse retrieveCustomer(@NotNull Long userId);


    /**
     * 创建客户
     * @author zhangzeyuan
     * @date 2022/1/10 16:47
     * @param customerDTO
     */
    StripeAPIResponse createCustomer(StripeCustomerDTO customerDTO);

     void createToken(String cardNo);

    /**
     * 创建卡
     * @author zhangzeyuan
     * @date 2022/1/10 16:47
     * @param cardToken
     */
    StripeAPIResponse createCardByCardToken(@NotNull Long userId, @NotBlank String cardToken);

    /**
     *
     * @author zhangzeyuan
     * @date 2022/1/18 16:19
     * @param userId
     * @param cardId
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     */
    StripeAPIResponse deleteCard(@NotNull Long userId, @NotNull String cardId);

    /**
     * 创建支付意向
     * @author zhangzeyuan
     * @date 2022/1/10 17:29
     * @param paymentIntentDTO
     */
    StripeAPIResponse createPaymentIntent(StripePaymentIntentDTO paymentIntentDTO);

    /**
     *  收款
     * @author zhangzeyuan
     * @date 2022/1/14 17:22
     * @param stripeChargeDTO
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     */
    StripeAPIResponse createCharge(StripeChargeDTO stripeChargeDTO);


    /**
     * 获取Charge
     * @author zhangzeyuan
     * @date 2022/1/19 11:19
     * @param id
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     */
    StripeAPIResponse retrieveCharge(@NotNull String id);


    /**
     * 创建设置未来付款信息
     * @author zhangzeyuan
     * @date 2022/1/19 11:19
     * @param setupIntentDTO
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     */
    StripeAPIResponse createSetupIntent(StripeSetupIntentDTO setupIntentDTO);

    /**
     * 获取支付意向
     * @author zhangzeyuan
     * @date 2022/1/19 11:19
     * @param id
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     */
    StripeAPIResponse retrievePaymentIntent(@NotNull String id);


    /**
     * 查询支付结果
     * @author zhangzeyuan
     * @date 2022/1/19 11:19
     * @param id PaymentIntent ID
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     */
/*    StripeAPIResponse queryPaymentResult(@NotNull String id);*/


    /**
     * 获取stripe客户卡列表
     * @author zhangzeyuan
     * @date 2022/1/25 10:29
     * @param userId
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     */
    StripeAPIResponse listAllCards(@NotNull Long userId, Integer limit);


    /**
     * 获取stripe卡
     * @author zhangzeyuan
     * @date 2022/1/27 11:13
     * @param userId
     * @param cardId
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     */
    StripeAPIResponse retrieveCard(@NotNull Long userId, @NotBlank String cardId);

    /**
     * 更新卡信息
     * @author zhangzeyuan
     * @date 2022/1/26 14:22
     * @param userId
     * @param cardId
     * @param cardDTO
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     */
    StripeAPIResponse updateCard(@NotNull Long userId, @NotNull String cardId, StripeCardDTO cardDTO);



    /**
     * stripe卡退款请求
     * @param requestInfo
     * @return
     * @throws Exception
     */
    void stripeRefundCheck(JSONObject requestInfo) throws Exception;

    /**
     * stripe 退款通知
     * @param event
     * @param request
     * @throws Exception
     */
    void stripeRefundNotice(Event event, HttpServletRequest request) throws Exception;
}
