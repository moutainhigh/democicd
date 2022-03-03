package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.stripe.model.*;
import com.uwallet.pay.core.common.StripeAPIResponse;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.HttpClientUtils;
import com.uwallet.pay.core.util.JSONResultHandle;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * @author zhangzeyuan
 * @date 2022年01月10日 15:13
 */
@Slf4j
@Service
public class CardServiceImpl extends BaseServiceImpl implements CardService {

    @Resource
    private UserService userService;
    @Resource
    private ServerService serverService;
    @Resource
    private TieOnCardFlowService tieOnCardFlowService;

    @Resource
    private StripeAPIService stripeAPIService;

    @Resource
    private StripeBusinessService stripeBusinessService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private GatewayService gatewayService;

    @Value("${latpay.tieOnCardUrl}")
    private String tieOnCardUrl;

    @Autowired
    private StaticDataService staticDataService;

    /**
     * @param cardToken
     * @param userId
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2022/1/20 17:30
     */
    @Override
    public Long bindCard(@NotBlank String cardToken, @NotNull Long userId, Integer creditCardAgreementState, HttpServletRequest request) throws Exception {

        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
        map.put("id", userId);
        //获取用户
        UserDTO user = userService.findOneUser(map);

        if(null == user || null == user.getId()){
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }

        //记录绑卡流水 todo ? 是否需要
        /*TieOnCardFlowDTO tieOnCardFlowDTO = new TieOnCardFlowDTO();
        tieOnCardFlowDTO.setUserId(userId);
        tieOnCardFlowService.saveTieOnCardFlow(tieOnCardFlowDTO, request);*/
        //stripe  绑卡
        Card card = stripeBusinessService.bindCard(cardToken, userId, request);
        //后四位
        String last4 = card.getLast4();

        //同步信息到账户系统
        JSONObject cardInfo = new JSONObject();
        cardInfo.put("gateway", "Stripe");

        cardInfo.put("type", StaticDataEnum.TIE_CARD_1.getCode());
        cardInfo.put("firstName", user.getUserFirstName());
        cardInfo.put("lastName", user.getUserLastName());
        cardInfo.put("userId", userId);
        cardInfo.put("phone", user.getPhone());
        cardInfo.put("stripe_token", card.getId());

        //国家
        String country = card.getCountry();
        if(country.toLowerCase().equals("AU")){
            cardInfo.put("country", "1");
        }
        //查询国家仅使用H5 START
        String countryValue = staticDataService.selectCountry(country);
        cardInfo.put("country", null == country? "1":countryValue);

        String brand = card.getBrand();

        //卡类型
        String customerCcType = "";
        if(brand.equals(StaticDataEnum.STRIPE_CARD_BRAND_VISA.getMessage())){
            customerCcType = String.valueOf(StaticDataEnum.STRIPE_CARD_BRAND_VISA.getCode());
        }else if(brand.equals(StaticDataEnum.STRIPE_CARD_BRAND_AMEX.getMessage())){
            customerCcType = String.valueOf(StaticDataEnum.STRIPE_CARD_BRAND_AMEX.getCode());
        }else if(brand.equals(StaticDataEnum.STRIPE_CARD_BRAND_MAST.getMessage())){
            customerCcType = String.valueOf(StaticDataEnum.STRIPE_CARD_BRAND_MAST.getCode());
        }else if(brand.equals(StaticDataEnum.STRIPE_CARD_BRAND_DC.getMessage())){
            customerCcType = String.valueOf(StaticDataEnum.STRIPE_CARD_BRAND_DC.getCode());
        }else if(brand.equals(StaticDataEnum.STRIPE_CARD_BRAND_DISCOVER.getMessage())){
            customerCcType = String.valueOf(StaticDataEnum.STRIPE_CARD_BRAND_DISCOVER.getCode());
        }else if(brand.equals(StaticDataEnum.STRIPE_CARD_BRAND_JCB.getMessage())){
            customerCcType = String.valueOf(StaticDataEnum.STRIPE_CARD_BRAND_JCB.getCode());
        }else if(brand.equals(StaticDataEnum.STRIPE_CARD_BRAND_CUP.getMessage())){
            customerCcType = String.valueOf(StaticDataEnum.STRIPE_CARD_BRAND_CUP.getCode());
        }else {
            customerCcType = String.valueOf(StaticDataEnum.STRIPE_CARD_BRAND_UNKNOWN.getCode());
        }
        cardInfo.put("customerCcType", customerCcType);

        //付款先后类型
        String funding = card.getFunding();
        String cardPaytype = "";
        if(funding.equals(StaticDataEnum.STRIPE_CARD_FUNDING_CREDIT.getMessage())){
            cardPaytype = String.valueOf(StaticDataEnum.STRIPE_CARD_FUNDING_CREDIT.getCode());
        }else if(funding.equals(StaticDataEnum.STRIPE_CARD_FUNDING_DEBIT.getMessage())){
            cardPaytype = String.valueOf(StaticDataEnum.STRIPE_CARD_FUNDING_DEBIT.getCode());
        }else if(funding.equals(StaticDataEnum.STRIPE_CARD_FUNDING_PREPAID.getMessage())){
            cardPaytype = String.valueOf(StaticDataEnum.STRIPE_CARD_FUNDING_PREPAID.getCode());
        }else {
            cardPaytype = String.valueOf(StaticDataEnum.STRIPE_CARD_FUNDING_UNKNOWN.getCode());
        }
        cardInfo.put("cardPayType", cardPaytype);
//        cardInfo.put("cardCategory", "");

        cardInfo.put("cardNo", Constant.STRIPE_CARD_NUMBER_PREFIX + last4);
        cardInfo.put("last4", last4);

        //系统绑卡

        Long cardId = null;
        try {
            cardId = serverService.tieOnCard(cardInfo);
        }catch (Exception e){
            card.delete();
            throw new BizException(e.getMessage());
        }

        //更新用户状态
        boolean update = false;
        UserDTO updateUser = new UserDTO();
        //绑卡状态
        if(!user.getCardState().equals(StaticDataEnum.USER_CARD_STATE_1.getCode())){
            updateUser.setCardState(StaticDataEnum.USER_CARD_STATE_1.getCode());
            update = true;
        }
        //卡支付状态
        if(!user.getPaymentState().equals(StaticDataEnum.USER_CARD_STATE_1.getCode())){
            updateUser.setPaymentState(StaticDataEnum.USER_CARD_STATE_1.getCode());
            update = true;
        }
        //是否同意分期付绑卡协议
        if(null != creditCardAgreementState){
            updateUser.setCreditCardAgreementState(creditCardAgreementState);
            update = true;
        }

        //stripe老用户状态
        if(null != user.getStripeState() && user.getStripeState().intValue() == StaticDataEnum.USER_STRIPE_STATE_1.getCode()){
            updateUser.setStripeState(StaticDataEnum.USER_STRIPE_STATE_0.getCode());
            update = true;
        }
        if(update){
            userService.updateUser(userId, updateUser, request);
        }
        return cardId;
    }

    /**
     * stripe 卡号后四位判重
     *
     * @param last4
     * @param request
     * @author zhangzeyuan
     * @date 2022/1/24 17:06
     */
    @Override
    public JSONObject stripeCheckCardNoRedundancy(@NotBlank String last4, Long userId, HttpServletRequest request) throws Exception{
        JSONObject result = new JSONObject();
        Integer count = serverService.countByCardNoLast4(userId.toString(), last4);
        if(count > 0){
            //重复
            result.put("redundancyState",  1);
        }else {
            result.put("redundancyState",  0);
            //获取支持的卡类型
            HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
            map.put("code", "cardType");
            List<String> cardType = staticDataService.getSupportedCardList("cardType");
            result.put("supportedList",  cardType);
        }
        return result;
    }


    @Override
    public JSONArray getAllCardList(@NotNull Long userId, HttpServletRequest request) throws Exception {

        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(2);
        map.put("state", 1);
        map.put("gatewayType", 0);
        GatewayDTO oneGateway = gatewayService.findOneGateway(map);
        if(null == oneGateway || null == oneGateway.getId()){
            throw new BizException(I18nUtils.get("gateway.disabled", getLang(request)));
        }

        UserDTO user = userService.findUserById(userId);
        if(null == user || null == user.getId()){
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }

        JSONArray cardListRes = new JSONArray();

        if(oneGateway.getType().intValue() == StaticDataEnum.GATEWAY_TYPE_8.getCode()){
            int stripeState = user.getStripeState();

            if(stripeState == StaticDataEnum.USER_STRIPE_STATE_1.getCode()){
                //latpay卡列表
                List<JSONObject> cardListEpoch = userService.getCardListEpoch(userId, StaticDataEnum.TIE_CARD_1.getCode(), request);

                if(CollectionUtils.isNotEmpty(cardListEpoch)){
                    cardListRes = JSONArray.parseArray(JSON.toJSONString(cardListEpoch));
                }
            }else{
                //stripe卡列表
                cardListRes = getStripeCardList(userId, request);
            }
        }else{
            //latpay卡列表
            JSONArray latpayCardList = this.getLatpayCardList(userId, request);
            if(null != latpayCardList && StringUtils.isNotBlank(latpayCardList.toJSONString())){
                cardListRes = latpayCardList;
            }
        }
        return cardListRes;
    }


    @Override
    public JSONArray getStripeCardList(@NotNull Long userId, HttpServletRequest request) throws Exception {

        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(2);
        map.put("state", 1);
        map.put("gatewayType", 0);
        GatewayDTO oneGateway = gatewayService.findOneGateway(map);
        if(null == oneGateway || null == oneGateway.getId()){
            throw new BizException(I18nUtils.get("gateway.disabled", getLang(request)));
        }

        JSONArray cardListRes = new JSONArray();
        if(oneGateway.getType().intValue() == StaticDataEnum.GATEWAY_TYPE_8.getCode()){
            //查询stripe
            List<CardDTO> stripeCardList = serverService.getStripeCardList(userId.toString());
            if(CollectionUtils.isEmpty(stripeCardList)){
                //返回空
                return new JSONArray();
            }

            int presetState = 0;

            HashMap<String, Object> stripeTokenMap = Maps.newHashMapWithExpectedSize(stripeCardList.size());

            CardDTO defaultCard = null;
            int defalutCardIndex = 0;
            for(int i = 0; i < stripeCardList.size(); i ++){
                CardDTO cardDTO = stripeCardList.get(i);

                if(cardDTO.getPreset().equals(1)){
                    presetState = 1;
                    defaultCard = cardDTO;
                    defalutCardIndex = i;
                }
                stripeTokenMap.put(cardDTO.getStripeToken(), i);
            }

            StripeAPIResponse stripeAPIResponse = stripeAPIService.listAllCards(userId, stripeCardList.size());
            if(!stripeAPIResponse.isSuccess()){
                return JSONArray.parseArray(JSON.toJSONString(stripeCardList));
            }

            PaymentSourceCollection cards = (PaymentSourceCollection) stripeAPIResponse.getData();
            List<PaymentSource> data = cards.getData();

            for(PaymentSource source : data){
                Card card = (Card) source;
                Long expMonth = card.getExpMonth();
                Long expYear = card.getExpYear();

                if(null != stripeTokenMap.get(card.getId())){
                    int index = (int) stripeTokenMap.get(card.getId());
                    //月份补0
                    String tempExpMonth = "";
                    if(expMonth.toString().length() == 1){
                        tempExpMonth = "0" + expMonth;
                    }else{
                        tempExpMonth = expMonth.toString();
                    }
                    stripeCardList.get(index).setCustomerCcExpyr(expYear.toString());
                    stripeCardList.get(index).setCustomerCcExpmo(tempExpMonth);
                }
            }
            List<CardDTO> result = new ArrayList<>();

            //是否有默认卡
            if(presetState == 0){
                result = stripeCardList;
                //没有默认卡 第一张设置为默认卡
                result.get(0).setPreset(1);
            }else{
                //没有默认卡 默认卡不在第一位
                if(defalutCardIndex != 0){
                    //默认卡放第一位
                    stripeCardList.remove(defalutCardIndex);

                    result.add(defaultCard);
                    result.addAll(stripeCardList);
                }else{
                    result = stripeCardList;
                }
            }
            cardListRes = JSONArray.parseArray(JSON.toJSONString(result));
        }else{
            //latpay卡列表
            JSONArray latpayCardList = this.getLatpayCardList(userId, request);
            if(null != latpayCardList && StringUtils.isNotBlank(latpayCardList.toJSONString())){
                cardListRes = latpayCardList;
            }
        }
        return cardListRes;
    }

    /**
     * @param userId
     * @param request
     * @return java.lang.Integer
     * @author 获取stripe卡数量
     * @date 2022/2/8 10:20
     */
    @Override
    public Integer getStripeCardCount(@NotNull Long userId, HttpServletRequest request) throws Exception {
        List<CardDTO> stripeCardList = serverService.getStripeCardList(userId.toString());
        if(CollectionUtils.isEmpty(stripeCardList)){
            //返回空
            return 0;
        }
        return stripeCardList.size();
    }

    /**
     * 获取latpay卡数量
     *
     * @param userId
     * @param request
     * @return com.alibaba.fastjson.JSONArray
     * @author zhangzeyuan
     * @date 2022/2/18 14:30
     */
    @Override
    public JSONArray getLatpayCardList(@NotNull Long userId, HttpServletRequest request) throws Exception {
        List<CardDTO> latpayCardList = serverService.getLatpayCardList(userId.toString());
        if(CollectionUtils.isEmpty(latpayCardList)){
            //返回空
            return new JSONArray();
        }

        int presetState = 0;
        CardDTO defaultCard = null;
        int defalutCardIndex = 0;

        int tempIndex = 0;
        for(CardDTO cardDTO : latpayCardList){
            if(cardDTO.getPreset().equals(1)){
                presetState = 1;
                defaultCard = cardDTO;
                defalutCardIndex = tempIndex;
            }
            tempIndex += 1;
            try{
                JSONObject cardExpireDate = this.queryLatpayCardInfo(cardDTO.getCrdStrgToken(), request);
                cardDTO.setCustomerCcExpmo(cardExpireDate.getString("customerCcExpmo"));
                cardDTO.setCustomerCcExpyr(cardExpireDate.getString("customerCcExpyr"));
            }catch (Exception e){
                log.error("获取latpay卡过期日出错，", cardDTO.getCrdStrgToken());
            }
        }
        List<CardDTO> result = new ArrayList<>();

        //是否有默认卡
        if(presetState == 0){
            result = latpayCardList;
            //没有默认卡 第一张设置为默认卡
            result.get(0).setPreset(1);
        }else{
            //没有默认卡 默认卡不在第一位
            if(defalutCardIndex != 0){
                //默认卡放第一位
                latpayCardList.remove(defalutCardIndex);

                result.add(defaultCard);
                result.addAll(latpayCardList);
            }else{
                result = latpayCardList;
            }
        }
        return JSONArray.parseArray(JSON.toJSONString(result));
    }


    @Override
    public Integer getLatpayCardCount(@NotNull Long userId, HttpServletRequest request) throws Exception {
        int count = 0;
        List<CardDTO> latpayCardList = serverService.getLatpayCardList(userId.toString());
        if(!CollectionUtils.isEmpty(latpayCardList)){
            //返回空
            count = latpayCardList.size();
        }
        return count;
    }


    /**
     * stripe修改卡信息
     *
     * @param userId
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2022/1/26 13:38
     */
    @Override
    public JSONObject stripeUpdateCard(@NotBlank String cardId, @NotNull Long userId, String expYear, String expMonth,HttpServletRequest request) throws Exception {
        StripeCardDTO cardDTO = new StripeCardDTO();
        cardDTO.setExp_year(expYear);
        cardDTO.setExp_month(expMonth);
        StripeAPIResponse stripeAPIResponse = stripeAPIService.updateCard(userId, cardId, cardDTO);
        JSONObject result = new JSONObject();
        if(!stripeAPIResponse.isSuccess()){
            result.put("state", StaticDataEnum.CARD_UPDATE_STATE_FAIL.getCode());
        }else {
            result.put("state", StaticDataEnum.CARD_UPDATE_STATE_SUCCESS.getCode());
        }
        return result;
    }

    /**
     * stripe修改卡信息
     *
     * @param latPayToken
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2022/1/26 13:38
     */
    @Override
    public JSONObject latpayUpdateCard(@NotBlank String latPayToken, JSONObject requestInfo, JSONObject cardInfo, HttpServletRequest request) throws Exception {
        //latpay卡Token校验
        if (StringUtils.isBlank(latPayToken)) {
            throw new BizException(I18nUtils.get("no.latpay.card.to.update", getLang(request)));
        }
        Long cardId = requestInfo.getLong("cardId");
        // 返回结果
        JSONObject result = new JSONObject(2);
        //校验参数, 封装被修改的卡信息参数,返回需要提交三方修改的信息
        JSONObject latpayUpdateInfo = this.verifyUpdateLatpayCardInfo(requestInfo, cardInfo, latPayToken, request);
        //无信息被更新, 返回state = 12
        /*String customerCcCvcNew = requestInfo.getString("customerCcCvc");
        boolean cvcChangedMark = customerCcCvcNew.equals(cardInfo.getString("customerCcCvc"));
        if (latpayUpdateInfo.isEmpty() && cvcChangedMark) {
            result.put("state", StaticDataEnum.CARD_UPDATE_STATE_SUCCESS.getCode());
            return result;
        }*/
        boolean cvcChangedMark = false;
        //保存一条修改记录
        TieOnCardFlowDTO tieOnCardFlowDTO = this.packAndSaveTieOnCardFlowData(latpayUpdateInfo, requestInfo, cardInfo, cvcChangedMark, request);
        //更新状态
        int updateState;
        try {
            // 如果有三方信息被更新(除了cvc外的)调用三方接口,修改卡信息
            JSONObject accountUpdateParam = new JSONObject(6);
            if (!latpayUpdateInfo.isEmpty()) {
                updateLatpayCardInfo(tieOnCardFlowDTO, latpayUpdateInfo, latPayToken, request);

                //封装用户更新账户系统的参数
                if (StringUtils.isNotBlank(latpayUpdateInfo.getString("Customer_cc_type"))) {
                    accountUpdateParam.put("Customer_cc_type", requestInfo.get("customerCcType"));
                }
                if (StringUtils.isNotBlank(latpayUpdateInfo.getString("Bill_country"))) {
                    accountUpdateParam.put("Bill_country", requestInfo.get("country"));
                }
            }
            //封装账户系统更新信息
            accountUpdateParam.put("cardId", cardId);
            //如果cvc也被修改了 则更新账户系统cvc记录
//            accountUpdateParam.put("customerCcCvc", cvcChangedMark ? null : customerCcCvcNew);
            // 账户卡信息更新
            serverService.cardInfoUpdate(accountUpdateParam);
            updateState = StaticDataEnum.CARD_UPDATE_STATE_SUCCESS.getCode();
        } catch (Exception e) {
            log.info("更新Latpay卡信息失败, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            updateState = StaticDataEnum.CARD_UPDATE_STATE_FAIL.getCode();
        }
        tieOnCardFlowDTO.setUnBundlingState(updateState);
        tieOnCardFlowService.updateTieOnCardFlow(tieOnCardFlowDTO.getId(), tieOnCardFlowDTO, request);
        // 清空缓存卡信息
        redisUtils.del(requestInfo.getLong("userId") + "_card");
        result.put("state", updateState);
        return result;
    }

    /**
     * 获取stripe 卡的过期年月
     *
     * @param cardId
     * @param userId
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2022/2/14 14:32
     */
    @Override
    public JSONObject getStripeCardExpirationDate(@NotBlank String cardId, @NotNull Long userId, HttpServletRequest request) throws Exception {
        //获取卡过期年月
        StripeAPIResponse stripeAPIResponse = stripeAPIService.retrieveCard(userId, cardId);

        JSONObject result = new JSONObject();
        String expMonth = "";
        String expYear = "";
        if(stripeAPIResponse.isSuccess()){
            Card card = (Card) stripeAPIResponse.getData();
            Long tempExpMonth = card.getExpMonth();

            if(tempExpMonth.toString().length() == 1){
                expMonth = "0" + card.getExpMonth();
            }else{
                expMonth = card.getExpMonth().toString();
            }

            expYear = card.getExpYear().toString();
        }

        result.put("customerCcExpyr", expYear);
        result.put("customerCcExpmo", expMonth);
        return result;
    }


    private JSONObject verifyUpdateLatpayCardInfo(JSONObject info,
                                                  JSONObject cardInfo,
                                                  String latPayToken,
                                                  HttpServletRequest request) throws Exception {
        /*String customerCcCvc = info.getString("customerCcCvc");
        if (StringUtils.isBlank(customerCcCvc)) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (customerCcCvc.length() > StaticDataEnum.CVC_MAX.getCode() || !StringUtils.isNumeric(customerCcCvc)) {
            throw new BizException(I18nUtils.get("cvc.illegal", getLang(request)));
        }*/

        JSONObject param = new JSONObject(7);

        //判断 customerCcType是否被修改
        /*String customerCcType = info.getString("customerCcType");
        if (StringUtils.isNotBlank(customerCcType) && !customerCcType.equals(cardInfo.getString("customerCcType"))) {
            JSONObject queryParam = new JSONObject(4);
            queryParam.put("code", "cardType");
            queryParam.put("value", customerCcType);
            StaticDataDTO cardTypeStaticData = staticDataService.findOneStaticData(queryParam);
            if (null == cardTypeStaticData || null == cardTypeStaticData.getId()) {
                throw new BizException(I18nUtils.get("card.type.not.found", getLang(request)));
            }
            param.put("Customer_cc_type", cardTypeStaticData.getName());
        }*/
        //请求Latpay 获取卡过期 年/月
        JSONObject cardDateInfo = queryLatpayCardInfo(latPayToken, request);
        String latpayExpmo = cardDateInfo.getString("customerCcExpmo");
        String latpayExpyr = cardDateInfo.getString("customerCcExpyr");
        //本次上送的卡过期的 月/年
        String customerCcExpmo = info.getString("customerCcExpmo");
        String customerCcExpyr = info.getString("customerCcExpyr");
        //日期-->月 校验
        if (StringUtils.isNotBlank(customerCcExpmo)) {
            this.verifyExpDate(customerCcExpmo,customerCcExpyr,request);
            if (!latpayExpmo.equals(customerCcExpmo)) {
                param.put("Customer_cc_expyr", customerCcExpyr);
                param.put("Customer_cc_expmo", customerCcExpmo);
            }
        }
        // 年校验
        if (StringUtils.isNotBlank(customerCcExpyr)) {
            this.verifyExpDate(customerCcExpmo,customerCcExpyr,request);
            if (!latpayExpyr.equals(customerCcExpyr)) {
                param.put("Customer_cc_expyr", customerCcExpyr);
                param.put("Customer_cc_expmo", customerCcExpmo);
            }
        }
        String country = info.getString("country");
        if (StringUtils.isNotBlank(country) && !country.equals(cardInfo.getString("country"))) {
            Map<String, Object> params = new HashMap<>(5);
            //查询国家ISO列表
            params.put("code", "county");
            params.put("value", country);
            StaticDataDTO countryStaticDto = staticDataService.findOneStaticData(params);
            if (null == countryStaticDto) {
                throw new BizException(I18nUtils.get("bank.country", getLang(request)));
            }
            CountryIsoDTO countryIsoDTO = tieOnCardFlowService.selectCountryIso(countryStaticDto.getEnName());
            if (null == countryIsoDTO) {
                throw new BizException(I18nUtils.get("bank.country", getLang(request)));
            }
            param.put("Bill_country", countryIsoDTO.getTwoLettersCoding());
        }
        return param;
    }

    private TieOnCardFlowDTO packAndSaveTieOnCardFlowData(JSONObject latpayUpdateInfo, JSONObject requestInfo, JSONObject cardInfo, Boolean cvcChangedMark, HttpServletRequest request) throws BizException {
        // 记录修改卡信息流水
        TieOnCardFlowDTO tieOnCardFlowDTO = new TieOnCardFlowDTO();
        tieOnCardFlowDTO.setUserId(getUserId(request));
        tieOnCardFlowDTO.setCardNo(cardInfo.getString("cardNo"));
        tieOnCardFlowDTO.setCardId(requestInfo.getLong("cardId"));
        tieOnCardFlowDTO.setCountry(latpayUpdateInfo.getString("Bill_country"));
        tieOnCardFlowDTO.setCustomerCcExpmo(latpayUpdateInfo.getString("Customer_cc_expmo"));
        tieOnCardFlowDTO.setCustomerCcExpyr(latpayUpdateInfo.getString("Customer_cc_expyr"));
        tieOnCardFlowDTO.setCustomerCcType(latpayUpdateInfo.getString("Customer_cc_type"));
        tieOnCardFlowDTO.setUnBundlingState(StaticDataEnum.CARD_UPDATE_STATE_PROCESSING.getCode());
        //如果cvc被更新了
        if (!cvcChangedMark) {
            tieOnCardFlowDTO.setCustomerCcCvc(requestInfo.getString("customerCcCvc"));
        }
        tieOnCardFlowDTO.setId(tieOnCardFlowService.saveTieOnCardFlow(tieOnCardFlowDTO, request));
        return tieOnCardFlowDTO;
    }


    private void updateLatpayCardInfo(TieOnCardFlowDTO tieOnCardFlowDTO, JSONObject latpayUpdateInfo, String latPayToken, HttpServletRequest request) throws Exception {
        tieOnCardFlowDTO.setCrdStrgToken(latPayToken);
        Map<String, Object> params = new HashMap<>(2);
        params.put("type", StaticDataEnum.GATEWAY_TYPE_0.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        JSONObject requestParam = new JSONObject(8);
        requestParam.put("Merchant_id", gatewayDTO.getChannelMerchantId());
        requestParam.put("merchantpwd", gatewayDTO.getPassword());
        requestParam.put("AccountType", "A");
        //更新卡信息的表示位 SCSS_U
        requestParam.put("RequestType", "SCSS_U");
        requestParam.put("CrdStrg_Token", latPayToken);
        requestParam.putAll(latpayUpdateInfo);
        JSONObject latpayResult = null;
        try {
            log.info("latpay修改卡信息三方接口请求-参数, data:{}", requestParam);
            latpayResult = JSONResultHandle.resultHandle(HttpClientUtils.sendPostForm(tieOnCardUrl, requestParam));
            log.info("latpay修改卡信息三方接口请求-结果, result:{}", latpayResult);
            Integer status = latpayResult.getJSONObject("data").getInteger("Status");
            Integer responseType = latpayResult.getJSONObject("data").getInteger("ResponseType");
            if (status != StaticDataEnum.CARD_UPDATE_CARD_INFO_SUCCESS_STATUS.getCode() || responseType != StaticDataEnum.CARD_MANAGE_CARD_RESPONSE.getCode()) {
                throw new BizException(I18nUtils.get("card.update.info.failed", getLang(request)));
            }
        } catch (Exception e) {
            log.info("latpay修改卡信息三方接口请求-失败, data:{}, latpayResult:{}, error message:{}, e:{}", requestParam, latpayResult, e.getMessage(), e);
            throw new BizException(I18nUtils.get("card.update.info.failed", getLang(request)));
        }
    }


    private JSONObject queryLatpayCardInfo(String latPayToken, HttpServletRequest request) throws Exception {
        Map<String, Object> params = new HashMap<>(1);
        params.put("type", StaticDataEnum.GATEWAY_TYPE_0.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        JSONObject requestParam = new JSONObject();
        requestParam.put("Merchant_id", gatewayDTO.getChannelMerchantId());
        requestParam.put("merchantpwd", gatewayDTO.getPassword());
        requestParam.put("AccountType", "A");
        //更新卡信息的表示位 SCSS_U
        requestParam.put("RequestType", "SCSS_Q");
        requestParam.put("CrdStrg_Token", latPayToken);
        JSONObject latpayResult = null;
        try {
            log.info("latpay查询卡信息三方接口请求-参数, data:{}", requestParam);
            latpayResult = JSONResultHandle.resultHandle(HttpClientUtils.sendPostFormQueryLatpayCardInfo(tieOnCardUrl, requestParam));
            log.info("latpay查询卡信息三方接口请求-结果, result:{}", latpayResult);
            JSONObject data = latpayResult.getJSONObject("data");
            Integer status = data.getInteger("Status");
            Integer responseType = latpayResult.getJSONObject("data").getInteger("ResponseType");
            if (status != StaticDataEnum.CARD_UPDATE_CARD_INFO_SUCCESS_STATUS.getCode() || responseType != StaticDataEnum.CARD_MANAGE_CARD_RESPONSE.getCode()) {
                if(status == StaticDataEnum.CARD_LATPAY_NOT_FOUND.getCode()){
                    log.info("通过卡token获取卡详情失败, 卡token在latpay系统无效,返回9010, token:{}",latPayToken);
                }
                throw new BizException(I18nUtils.get("card.fetch.info.failed", getLang(request)));
            }
            requestParam.clear();
            requestParam.put("customerCcExpyr", data.get("Customer_cc_expyr") == null ? "" : data.get("Customer_cc_expyr"));
            requestParam.put("customerCcExpmo", data.get("Customer_cc_expmo") == null ? "" : data.get("Customer_cc_expmo"));
            requestParam.put("bin", data.get("CardBin"));
            return requestParam;
        } catch (Exception e) {
            log.info("latpay查询卡信息三方接口请求-失败, data:{}, latpayResult:{}, error message:{}, e:{}", requestParam, latpayResult, e.getMessage(), e);
            throw new BizException(I18nUtils.get("card.fetch.info.failed", getLang(request)));
        }
    }

    private void verifyExpDate(String customerCcExpmo, String customerCcExpyr, HttpServletRequest request) throws BizException{
        if (customerCcExpyr.length() != StaticDataEnum.YEAR_FORMAT_YYYY.getCode() || !StringUtils.isNumeric(customerCcExpyr)) {
            throw new BizException(I18nUtils.get("card.exp.info.illegal", getLang(request)));
        }
        if (customerCcExpmo.length() != StaticDataEnum.MONTH_FORMAT_MM.getCode() || !StringUtils.isNumeric(customerCcExpmo)) {
            throw new BizException(I18nUtils.get("card.exp.info.illegal", getLang(request)));
        }
        Calendar currentCal = Calendar.getInstance();
        int currentYear = currentCal.get(Calendar.YEAR);
        //过期年份必须大于等于当年
        if (Integer.parseInt(customerCcExpyr)<currentYear){
            throw new BizException(I18nUtils.get("card.exp.info.illegal", getLang(request)));
        }
    }

    /**
     * 根据用户实体封装创建stripe需要的customer
     * @author zhangzeyuan
     * @date 2022/1/19 10:31
     * @param userDTO
     * @return com.uwallet.pay.main.model.dto.StripeCustomerDTO
     */
    private StripeCustomerDTO packageStripeCustomerByUserDTO(UserDTO userDTO){
        StripeCustomerDTO customerDTO = new StripeCustomerDTO();
        customerDTO.setId(userDTO.getId().toString());
        customerDTO.setEmail(userDTO.getEmail());
        customerDTO.setName(userDTO.getUserFirstName() + " " + userDTO.getUserLastName());
        customerDTO.setPhone(userDTO.getPhone());
        return customerDTO;
    }


    /**
     * 得到一个customer  没有则创建
     *  customer可能为空！
     * @author zhangzeyuan
     * @date 2022/1/20 17:35
     * @param userId
     * @return com.stripe.model.Customer
     */
    private Customer getOrCreateCustomer(Long userId){
        //获取客户并绑卡
        Customer customer = null;
        //根据用户ID获取customer
        StripeAPIResponse retrieveUserRes = stripeAPIService.retrieveCustomer(userId);
        if(!retrieveUserRes.isSuccess()){
            //查询用户信息
            HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
            map.put("id", userId);
            UserDTO oneUser = userService.findOneUser(map);
            //封装用户信息
            StripeCustomerDTO stripeCustomer = packageStripeCustomerByUserDTO(oneUser);
            StripeAPIResponse createUserRes = stripeAPIService.createCustomer(stripeCustomer);
            customer = (Customer) createUserRes.getData();
        }else{
            customer = (Customer) retrieveUserRes.getData();
        }
        return customer;
    }


    /**
     * 获取当前语言，默认保持英文
     * @author faker
     * @param request
     * @return
     */
    @Override
    public Locale getLang(HttpServletRequest request) {
        Locale lang = Locale.US;
        // 获取当前语言
        String headerLang = request.getHeader("lang");
        Locale locale = LocaleContextHolder.getLocale();
        if (StringUtils.isNotEmpty(headerLang) && "zh-CN".equals(headerLang)) {
            lang = Locale.SIMPLIFIED_CHINESE;
        }
        return lang;
    }

}
