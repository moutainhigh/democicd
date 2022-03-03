package com.uwallet.pay.main.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stripe.model.Card;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.QrPayFlowDTO;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/** 卡 相关service
 * @author zhangzeyuan
 * @date 2022年01月20日 18:32
 */
public interface CardService {

    /**
     * stripe 绑卡
     * @author zhangzeyuan
     * @date 2022/1/20 17:30
     * @param userId
     * @return com.alibaba.fastjson.JSONObject
     */
    Long bindCard(@NotBlank String cardToken, @NotNull Long userId ,Integer creditCardAgreementState, HttpServletRequest request) throws Exception;

    /**
     * stripe 卡号后四位判重
     * @author zhangzeyuan
     * @date 2022/1/24 17:06
     * @param last4
     * @param request
     * @param userId
     */
    JSONObject stripeCheckCardNoRedundancy(@NotBlank String last4, @NotNull Long userId, HttpServletRequest request) throws Exception;

    /**
     * 获取所有卡列表 未迁移stripe查询latpay卡列表，否则查询stripe卡列表
     * @author zhangzeyuan
     * @date 2022/1/26 8:52
     * @param userId
     * @param request
     * @return com.alibaba.fastjson.JSONArray
     */
    JSONArray getAllCardList(@NotNull Long userId, HttpServletRequest request) throws Exception;


    /**
     * 获取stripe卡列表
     * @author zhangzeyuan
     * @date 2022/1/26 9:19
     * @param userId
     * @param request
     * @return com.alibaba.fastjson.JSONArray
     */
    JSONArray getStripeCardList(@NotNull Long userId, HttpServletRequest request) throws Exception;



    /**
     *
     * @author 获取stripe卡数量
     * @date 2022/2/8 10:20
     * @param userId
     * @param request
     * @return java.lang.Integer
     */
    Integer getStripeCardCount(@NotNull Long userId, HttpServletRequest request) throws Exception;


    /**
     * 获取latpay卡
     * @author zhangzeyuan
     * @date 2022/2/18 14:30
     * @param userId
     * @param request
     * @return com.alibaba.fastjson.JSONArray
     */
    JSONArray getLatpayCardList(@NotNull Long userId, HttpServletRequest request) throws Exception;

    /**
     * 获取latpay卡数量
     * @author zhangzeyuan
     * @date 2022/2/18 14:30
     * @param userId
     * @param request
     * @return com.alibaba.fastjson.JSONArray
     */
    Integer getLatpayCardCount(@NotNull Long userId, HttpServletRequest request) throws Exception;


    /**
     * stripe修改卡信息
     * @author zhangzeyuan
     * @date 2022/1/26 13:38
     * @param userId
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     */
    JSONObject stripeUpdateCard(@NotBlank String cardId, @NotNull Long userId, String expYear, String expMonth, HttpServletRequest request) throws Exception;


    /**
     * stripe修改卡信息
     * @author zhangzeyuan
     * @date 2022/1/26 13:38
     * @param latpayCardToken
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     */
    JSONObject latpayUpdateCard(@NotBlank String latpayCardToken, JSONObject requestInfo, JSONObject cardInfo, HttpServletRequest request) throws Exception;



    /**
     * 获取stripe 卡的过期年月
     * @author zhangzeyuan
     * @date 2022/2/14 14:32
     * @param cardId
     * @param userId
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     */
    JSONObject getStripeCardExpirationDate(@NotBlank String cardId, @NotNull Long userId, HttpServletRequest request) throws Exception;

}

