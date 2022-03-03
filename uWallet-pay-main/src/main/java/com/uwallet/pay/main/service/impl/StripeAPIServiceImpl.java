package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.stripe.Stripe;
import com.stripe.model.*;
import com.uwallet.pay.core.common.StripeAPICodeEnum;
import com.uwallet.pay.core.common.StripeAPIResponse;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.service.RefundFlowService;
import com.uwallet.pay.main.service.StripeAPIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangzeyuan
 * @date 2022年01月10日 15:13
 */
@Slf4j
@Service
public class StripeAPIServiceImpl implements StripeAPIService {
    @Resource
    private RefundFlowService refundFlowService;


    static {
//        Stripe.apiKey = "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";
          Stripe.apiKey = "sk_test_51K90efAgx3Fd2j3edhhoySYxbHfvGl9f34MVNuTso05aFfLX8t5su3OXoIXjWDW6zMEgNpe54m5Cw87dmUq5iNQG00MTDR1BXL";

    }


    private Customer getCustomer(Long userId) throws Exception {
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        List<String> expandList = new ArrayList<>();
        expandList.add("sources");
        params.put("expand", expandList);
        return Customer.retrieve(userId.toString(), params, null);
    }


    private Card getCard(Customer customer, String cardId) throws Exception {
        return (Card) customer.getSources().retrieve(cardId);
    }

    /*public static void main(String[] args) throws StripeException {
        *//*Stripe.apiKey = "sk_test_51K90efAgx3Fd2j3edhhoySYxbHfvGl9f34MVNuTso05aFfLX8t5su3OXoIXjWDW6zMEgNpe54m5Cw87dmUq5iNQG00MTDR1BXL";
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        List<String> expandList = new ArrayList<>();
        expandList.add("sources");
        params.put("expand", expandList);
        Customer retrieve = Customer.retrieve("646214090148630528", params, null);*//*
        System.out.println(1);
        System.out.println(2);
    }*/
    /**
     * 获取客户信息
     *
     * @param userId
     * @author zhangzeyuan
     * @date 2022/1/10 16:47
     */
    @Override
    public StripeAPIResponse retrieveCustomer(@NotNull Long userId) {
        //获取客户 todo提取
        try {
            Customer customer = getCustomer(userId);
            return StripeAPIResponse.success(StripeAPICodeEnum.SUCCESS_CODE, customer);
        } catch (Exception e) {
            log.error("获取stripe customer异常,e:{}", e);
            return StripeAPIResponse.fail(StripeAPICodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @Override
    public StripeAPIResponse createCustomer(StripeCustomerDTO customerDTO) {
        //todo? 参数 业务逻辑校验？
        Map<String, Object> params = BeanUtil.objectToMap(customerDTO);
        try {
            Customer customer = Customer.create(params);

            return StripeAPIResponse.success(StripeAPICodeEnum.SUCCESS_CODE, customer);
        } catch (Exception e) {
            log.error("创建stripe customer异常", e.getMessage());
            return StripeAPIResponse.fail(StripeAPICodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    /**
     * 创建卡
     *
     * @param cardNo
     * @author zhangzeyuan
     * @date 2022/1/10 16:47
     */
    @Override
    public void createToken(String cardNo) {
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        //获取客户
        try {
            Map<String, Object> cardp = new HashMap<>();
            cardp.put("number", cardNo);
            cardp.put("exp_month", 12);
            cardp.put("exp_year", 2023);
            cardp.put("cvc", "666");
            cardp.put("object", "card");
            params.put("card", cardp);
            Token token = Token.create(params);
            log.info("token:" + token);
        } catch (Exception e) {
            log.error("创建token customer异常,e:{}", e);
        }


    }


    /**
     * 创建卡
     *
     * @param userId
     * @param cardToken
     * @author zhangzeyuan
     * @date 2022/1/10 16:47
     */
    @Override
    public StripeAPIResponse createCardByCardToken(Long userId, @NotBlank String cardToken) {
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        //获取客户
        Customer customer = null;
        try {
            customer = getCustomer(userId);
        } catch (Exception e) {
            log.error("获取stripe customer异常,e:{}", e);
            return StripeAPIResponse.fail(StripeAPICodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        //创建卡
        params.clear();
        params.put("source", cardToken);
        Card card = null;
        try {
            card = (Card) customer.getSources().create(params);
        } catch (Exception e) {
            log.error("创建stripe card异常, e:{}", e);

            return StripeAPIResponse.fail(StripeAPICodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return StripeAPIResponse.success(StripeAPICodeEnum.SUCCESS_CODE, card);
    }


    /**
     * 删除卡
     *
     * @param userId
     * @param cardId
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     * @author zhangzeyuan
     * @date 2022/1/18 16:20
     */
    @Override
    public StripeAPIResponse deleteCard(@NotNull Long userId, @NotNull String cardId) {
        //获取客户
        Customer customer = null;
        try {
            customer = getCustomer(userId);
        } catch (Exception e) {
            log.error("获取stripe customer异常,e:{}", e);
            return StripeAPIResponse.fail(StripeAPICodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        Card card = null;
        try {
            card = getCard(customer, cardId);
        } catch (Exception e) {
            log.error("获取stripe card异常,e:{}", e);
            return StripeAPIResponse.fail(StripeAPICodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        try {
            card.delete();
            return StripeAPIResponse.success(StripeAPICodeEnum.SUCCESS_CODE, card);
        } catch (Exception e) {
            log.error("获取stripe customer异常,e:{}", e);
            return StripeAPIResponse.fail(StripeAPICodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    /**
     * 创建支付意向
     *
     * @param paymentIntentDTO
     * @author zhangzeyuan
     * @date 2022/1/10 17:29
     */
    @Override
    public StripeAPIResponse createPaymentIntent(StripePaymentIntentDTO paymentIntentDTO) {
        Map<String, Object> params = BeanUtil.objectToMap(paymentIntentDTO);
        try {
            PaymentIntent paymentIntent =
                    PaymentIntent.create(params);
            return StripeAPIResponse.success(StripeAPICodeEnum.SUCCESS_CODE, paymentIntent);
        } catch (Exception e) {
            log.error("创建stripe PaymentIntent异常,e:{}", e.getMessage());
            return StripeAPIResponse.fail(StripeAPICodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    /**
     * 收款
     *
     * @param stripeChargeDTO
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     * @author zhangzeyuan
     * @date 2022/1/14 17:22
     */
    @Override
    public StripeAPIResponse createCharge(StripeChargeDTO stripeChargeDTO) {
        Map<String, Object> params = BeanUtil.objectToMap(stripeChargeDTO);

        try {
            Charge charge = Charge.create(params);
            return StripeAPIResponse.success(StripeAPICodeEnum.SUCCESS_CODE, charge);
        } catch (Exception e) {
            log.error("创建stripe Charge异常, e:{}", e);

            return StripeAPIResponse.fail(StripeAPICodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    /**
     * 获取Charge
     *
     * @param id
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     * @author zhangzeyuan
     * @date 2022/1/19 11:19
     */
    @Override
    public StripeAPIResponse retrieveCharge(@NotNull String id) {
        try {
            Charge charge =
                    Charge.retrieve(id);
            return StripeAPIResponse.success(StripeAPICodeEnum.SUCCESS_CODE, charge);
        } catch (Exception e) {
            log.error("retrieve Charge异常, e:{}", e);
            return StripeAPIResponse.fail(StripeAPICodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    /**
     * 创建设置未来付款信息
     *
     * @param setupIntentDTO
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     * @author zhangzeyuan
     * @date 2022/1/19 11:19
     */
    @Override
    public StripeAPIResponse createSetupIntent(StripeSetupIntentDTO setupIntentDTO) {
        Map<String, Object> params = BeanUtil.objectToMap(setupIntentDTO);

        try {
            SetupIntent setupIntent = SetupIntent.create(params);
            return StripeAPIResponse.success(StripeAPICodeEnum.SUCCESS_CODE, setupIntent);
        } catch (Exception e) {
            log.error("创建stripe setupIntent异常, e:{}", e);
            return StripeAPIResponse.fail(StripeAPICodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    /**
     * 获取支付意向
     *
     * @param id
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     * @author zhangzeyuan
     * @date 2022/1/19 11:19
     */
    @Override
    public StripeAPIResponse retrievePaymentIntent(@NotNull String id) {
        try {
            log.info("retrievePaymentIntent stripeId:" + id);
            PaymentIntent paymentIntent =
                    PaymentIntent.retrieve(id);
            log.info("retrievePaymentIntent result:" + paymentIntent);
            return StripeAPIResponse.success(StripeAPICodeEnum.SUCCESS_CODE, paymentIntent);
        } catch (Exception e) {
            log.error("创建stripe PaymentIntent异常,e:{}", e.getMessage());
            return StripeAPIResponse.fail(StripeAPICodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    /**
     * 查询支付结果
     *
     * @param id PaymentIntent ID
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     * @author zhangzeyuan
     * @date 2022/1/19 11:19
     */
   /* @Override
    public StripeAPIResponse queryPaymentResult(@NotNull String id) {
        StripeAPIResponse stripeAPIResponse = retrievePaymentIntent(id);
        if(!stripeAPIResponse.isSuccess()){
            return stripeAPIResponse;
        }
        PaymentIntent paymentIntent =  (PaymentIntent) stripeAPIResponse.getData();

        String status = paymentIntent.getStatus();
        if(status.equals(StripeAPICodeEnum.PAY_RESPONSE_STATUS_REQUIRES_ACTION.getCode())){
            //需要验证
            String url  = (String) paymentIntent.getNextAction().getUseStripeSdk().get("stripe_js");

            HashMap<ObStripeSetupIntentDTOject, Object> map = Maps.newHashMapWithExpectedSize(2);
            map.put("url", url);
            map.put("clientSecret", paymentIntent.getClientSecret());
            return StripeAPIResponse.success(StripeAPICodeEnum.PAYMENTINTENT_CHECK_3DS, map);
        }else if(status.equals(StripeAPICodeEnum.PAY_RESPONSE_STATUS_SUCCEEDED.getCode())){
            //成功
            return StripeAPIResponse.success(StripeAPICodeEnum.CREATE_PAYMENTINTENT_SUNCCESS, paymentIntent);
        }else{
            //其他？
            return StripeAPIResponse.fail(StripeAPICodeEnum.CREATE_PAYMENT_INTENT_ERROR.getCode(), "");
        }
    }*/

    /**
     * 获取stripe卡列表
     *
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     * @author zhangzeyuan
     * @date 2022/1/25 10:29
     */
    @Override
    public StripeAPIResponse listAllCards(@NotNull Long userId, Integer limit) {
        try {
            Customer customer = getCustomer(userId);

            Map<String, Object> params = new HashMap<>();
            params.put("object", "card");
            params.put("limit", limit);
            PaymentSourceCollection cards =
                    customer.getSources().list(params);
            return StripeAPIResponse.success(StripeAPICodeEnum.SUCCESS_CODE, cards);
        } catch (Exception e) {
            log.error("list all cards异常,e:{}", e.getMessage());
            return StripeAPIResponse.fail(StripeAPICodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    /**
     * 获取stripe卡
     *
     * @param userId
     * @param cardId
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     * @author zhangzeyuan
     * @date 2022/1/27 11:13
     */
    @Override
    public StripeAPIResponse retrieveCard(@NotNull Long userId, @NotBlank String cardId) {

        try {
            Customer customer = getCustomer(userId);

            Card card = getCard(customer, cardId);
            return StripeAPIResponse.success(StripeAPICodeEnum.SUCCESS_CODE, card);
        } catch (Exception e) {
            log.error("retrieve a card异常,e:{}", e.getMessage());
            return StripeAPIResponse.fail(StripeAPICodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    /*public static void main(String[] args) throws StripeException {
        Stripe.apiKey = "sk_test_51K90efAgx3Fd2j3edhhoySYxbHfvGl9f34MVNuTso05aFfLX8t5su3OXoIXjWDW6zMEgNpe54m5Cw87dmUq5iNQG00MTDR1BXL";

        Token token =
                Token.retrieve("tok_1KRvZjAgx3Fd2j3elcRhT6P4");


        *//*Map<String, Object> card = new HashMap<>();
        card.put("number", "378282246310005");
        card.put("exp_month", 1);
        card.put("exp_year", 2023);
        card.put("cvc", "314");
        Map<String, Object> params = new HashMap<>();
        params.put("card", card);

        Token token = Token.create(params);


        HashMap<String, Object> params22 = Maps.newHashMapWithExpectedSize(2);
        List<String> expandList = new ArrayList<>();
        expandList.add("sources");
        params22.put("expand", expandList);
        Customer retrieve = Customer.retrieve("651586120645693440", params22, null);

        Map<String, Object> params111 = new HashMap<>();
        params111.put("source", token.getId());
        Card car2222d =
                (Card) retrieve.getSources().create(params111);*//*

        System.out.println(1);
    }*/
    /**
     * 更新卡信息
     *
     * @param userId
     * @param cardId
     * @param cardDTO
     * @return com.uwallet.pay.core.common.StripeAPIResponse
     * @author zhangzeyuan
     * @date 2022/1/26 14:22
     */
    @Override
    public StripeAPIResponse updateCard(@NotNull Long userId, @NotNull String cardId, StripeCardDTO cardDTO) {
        Map<String, Object> params = BeanUtil.objectToMap(cardDTO);

        try {
            Customer customer = getCustomer(userId);

            Card card = getCard(customer, cardId);

            Card updatedCard = (Card) card.update(params);

            return StripeAPIResponse.success(StripeAPICodeEnum.SUCCESS_CODE, updatedCard);
        } catch (Exception e) {
            log.error("update card异常,e:{}", e.getMessage());
            return StripeAPIResponse.fail(StripeAPICodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @Override
    public void stripeRefundCheck(JSONObject requestInfo) throws Exception {
        log.info("stripe refund check requestInfo, info:{}", requestInfo);
        HashMap<String, Object> maps = Maps.newHashMapWithExpectedSize(1);
        String paymentIntent = requestInfo.getString("paymentIntent");
        Long id = requestInfo.getLong("id");
        RefundFlowDTO refundFlowById = refundFlowService.findRefundFlowById(id);
        if (refundFlowById==null){
            throw new BizException("stripe refundCheck param error");
        }
        if (paymentIntent.startsWith("pi")){
            maps.put("payment_intent",paymentIntent);
        }else {
            maps.put("charge",paymentIntent);
        }
        try{
            RefundCollection refunds = Refund.list(maps);
            log.info("三方查证返回:{}",refunds);
            List<Refund> data = refunds.getData();
            for (Refund datum : data) {
                String status = datum.getStatus();
                Map<String, String> metadata = datum.getMetadata();
                String refundId = metadata.get("refundId");
                if (id.toString().equals(refundId)){
                    if (StripeAPICodeEnum.CHARGE_RES_STATUS_SUCCEEDED.getCode().equals(status)){
                        // 处理
                        refundFlowById.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                    }else if(StripeAPICodeEnum.CHARGE_RES_STATUS_PENDING.getCode().equals(status)){
                        refundFlowById.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                    }else if (StripeAPICodeEnum.CHARGE_RES_STATUS_FAILED.getCode().equals(status)||StripeAPICodeEnum.CHARGE_RES_STATUS_CANCELED.getCode().equals(status)){
                        refundFlowById.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                        refundFlowById.setReturnMessage(datum.getFailureReason());
                    }else {
                        throw new BizException("status error");
                    }
                    refundFlowService.updateRefundFlow(refundFlowById.getId(), refundFlowById, null);
                }
            }
        }catch (Exception e){
            log.error("stripe refundCheck param:{},error:{}",requestInfo,e);
            throw new BizException("stripe refundCheck error");
        }

    }

    @Override
    public void stripeRefundNotice(@NotNull Event event, HttpServletRequest request) throws Exception {
        log.info("三方请求参数:{}",event);
        String type = event.getType();
        if (StripeAPICodeEnum.CHARGE_REFUNDED.getCode().equals(type)){
            // 退款成功
            EventData data = event.getData();
            if (data==null){
                throw new BizException("params error");
            }
            Map<String, Object> previousAttributes = data.getPreviousAttributes();
            if (previousAttributes==null){
                throw new BizException("params error");
            }
            StripeObject object = data.getObject();
            String s2 = object.toJson();
            JSONObject jsonObject = JSONObject.parseObject(s2);
            JSONObject refunds = jsonObject.getJSONObject("refunds");
            JSONArray dataList = refunds.getJSONArray("data");
            for (Object o : dataList) {
                String s1 = JSONObject.toJSONString(o);
                JSONObject jsonObject1 = JSONObject.parseObject(s1);
                JSONObject metadata = jsonObject1.getJSONObject("metadata");
                if (metadata==null){
                    continue;
                }
                Long refundId = metadata.getLong("refundId");
                if (refundId==null){
                    continue;
//                    throw new BizException("refundId error");
                }
                String payment_intent = jsonObject1.getString("payment_intent");
                String charge = jsonObject1.getString("charge");

                String status = jsonObject1.getString("status");
                RefundFlowDTO refundFlowById = refundFlowService.findRefundFlowById(refundId);
                if (refundFlowById==null){
                    continue;
//                    throw new BizException("refundId error");
                }
                String stripeRefundNo = refundFlowById.getStripeRefundNo();
                if (!stripeRefundNo.startsWith("pi")){
                    payment_intent=charge;
                }
                if (stripeRefundNo==null){
                    continue;
                }
                if (!stripeRefundNo.equals(payment_intent)){
                    // 退款id应和payment_intent对应
                    continue;
                }
                if (status==null){
                    throw new BizException("params error");
                }
                if (StripeAPICodeEnum.CHARGE_RES_STATUS_SUCCEEDED.getCode().equals(status)){
                    // 处理
                    refundFlowById.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                }else if(StripeAPICodeEnum.CHARGE_RES_STATUS_PENDING.getCode().equals(status)){
                    refundFlowById.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                }else if (StripeAPICodeEnum.CHARGE_RES_STATUS_FAILED.getCode().equals(status)||StripeAPICodeEnum.CHARGE_RES_STATUS_CANCELED.getCode().equals(status)){
                    refundFlowById.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    refundFlowById.setReturnMessage(jsonObject1.getString("failure_reason"));
                }else {
                    throw new BizException("status error");
                }
                refundFlowService.updateRefundFlow(refundFlowById.getId(), refundFlowById, null);
            }

        }
    }


}
