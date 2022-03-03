package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.HttpClientUtils;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.model.dto.GatewayDTO;
import com.uwallet.pay.main.model.dto.WithholdFlowDTO;
import com.uwallet.pay.main.service.GatewayService;
import com.uwallet.pay.main.service.IntegraPayService;
import com.uwallet.pay.main.service.ServerService;
import com.uwallet.pay.main.service.WithholdFlowService;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class IntegraPayServiceImpl extends BaseServiceImpl implements IntegraPayService {

    @Value("${integraPay.businessId}")
    private String businessId;

    @Value("${integraPay.businessKey}")
    private String businessKey;

    @Value("${integraPay.accessUserName}")
    private String accessUserName;

    @Value("${integraPay.accessUserKey}")
    private String accessUserKey;

    /**
     * api权限请求
     */
    private static final String ACCESS_TOKEN_REQUEST_URL = "https://sandbox.auth.paymentsapi.io/login";

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private WithholdFlowService withholdFlowService;

    @Autowired
    private ServerService serverService;

    @Override
    public String apiAccessToken() throws Exception {
        String token = null;
        if (redisUtils.hasKey("integraPayAccessToken")) {
            token = redisUtils.get("integraPayAccessToken").toString();
        } else {
            JSONObject requestInfo = new JSONObject();
            requestInfo.put("Username", accessUserName);
            requestInfo.put("Password", accessUserKey);
            log.info("integraPay request info, data:{}", requestInfo);
            JSONObject requestResult = null;
            try {
                requestResult = JSONObject.parseObject(HttpClientUtils.postForIntegraPay(ACCESS_TOKEN_REQUEST_URL, null, requestInfo.toJSONString()));
            } catch (Exception e) {
                log.info("integraPay request info fail, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
                throw e;
            }
            log.info("integraPay request result, result:{}", requestResult);
            redisUtils.set("integraPayAccessToken", requestResult.getString("access_token"), requestResult.getLong("expires_in"));
            token = requestResult.getString("access_token");
        }
        return token;
    }

    @Override
    public JSONObject addPayer(JSONObject requestInfo, String token) throws Exception {
        log.info("add integraPay payer, data:{}", requestInfo);
        JSONObject requestResult = null;
        try {
            requestResult = JSONObject.parseObject(HttpClientUtils.postForIntegraPay("https://sandbox.rest.paymentsapi.io/businesses/" + businessId + "/payers", "Bearer " + token, requestInfo.toJSONString()));
        } catch (Exception e) {
            log.info("add integraPay payer fail, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            throw e;
        }
        log.info("add integraPay payer result, data:{}", requestResult);
        if (!StringUtils.isEmpty(requestResult.getString("errorCode"))) {
            log.info("add integraPay payer fail, data:{}, error message:{}, e:{}", requestInfo, requestResult.getString("errorMessage"), requestResult.getString("errorCode"));
            throw new BizException(requestResult.getString("errorCode"));
        }
        return requestResult;
    }

    @Override
    public String cardTokenGet(JSONObject requestInfo, String token) throws Exception {
        log.info("card token get, data:{}", requestInfo);
        JSONObject requestResult = null;
        try {
            requestResult = JSONObject.parseObject(HttpClientUtils.postForIntegraPay("https://sandbox.rest.paymentsapi.io/businesses/" + businessKey + "/services/tokenize-card", "Bearer " + token, requestInfo.toJSONString()));
        } catch (Exception e) {
            log.info("card token get fail, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            throw e;
        }
        log.info("card token get result, data:{}", requestResult);
        if (!StringUtils.isEmpty(requestResult.getString("errorCode"))) {
            log.info("add integraPay payer fail, data:{}, error message:{}, e:{}", requestInfo, requestResult.getString("errorMessage"), requestResult.getString("errorCode"));
            throw new BizException(requestResult.getString("errorCode"));
        }
        return requestResult.getString("token");
    }

    @Override
    public String payerTokenAdd(JSONObject requestInfo, String payerUnique, String token) throws Exception {
        log.info("payer token add, data:{}, payerUnique:{}", requestInfo, payerUnique);
        JSONObject requestResult = null;
        try {
            requestResult = JSONObject.parseObject(HttpClientUtils.postForIntegraPay("https://sandbox.rest.paymentsapi.io/businesses/" + businessId + "/payers/" + payerUnique + "/accounts/card/token", "Bearer " + token, requestInfo.toJSONString()));
        } catch (Exception e ) {
            log.info("payer token add fail, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            throw e;
        }
        log.info("payer token add result, data:{}", requestResult);
        if (!StringUtils.isEmpty(requestResult.getString("errorCode"))) {
            log.info("payer token add fail, data:{}, error message:{}, e:{}", requestInfo, requestResult.getString("errorMessage"), requestResult.getString("errorCode"));
            throw new BizException(requestResult.getString("errorCode"));
        }
        return requestResult.getString("accountId");
    }

    @Override
    public WithholdFlowDTO payByCard(WithholdFlowDTO withholdFlowDTO , String token) throws Exception {
        JSONObject requestInfo = new JSONObject();
        requestInfo.put("ProcessType", "COMPLETE");
        requestInfo.put("Reference", withholdFlowDTO.getOrdreNo());
        requestInfo.put("Amount", withholdFlowDTO.getTransAmount());
        requestInfo.put("CurrencyCode", "AUD");
        log.info("pay by card, data:{}", requestInfo);
        JSONObject requestResult = null;
        try {
            requestResult =  JSONObject.parseObject(HttpClientUtils.postForIntegraPay("https://sandbox.rest.paymentsapi.io/businesses/" + withholdFlowDTO.getGatewayMerchantId() + "/payers/" + withholdFlowDTO.getUniqueReference() + "/transactions/card", "Bearer " + token, requestInfo.toJSONString()));
        } catch (Exception e) {
            log.info("pay by card fail, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            throw e;
        }
        log.info("pay by card result, data:{}", requestResult);

        // 判断交易结果状态
        if (!StringUtils.isEmpty(requestResult.getString("errorCode"))) {
            log.info("pay by card fail, data:{}, error message:{}, e:{}", requestInfo, requestResult.getString("errorMessage"), requestResult.getString("errorCode"));
            withholdFlowDTO.setState( StaticDataEnum.TRANS_STATE_2.getCode());
            withholdFlowDTO.setReturnCode(requestResult.getString("errorCode"));
            withholdFlowDTO.setReturnMessage( requestResult.getString("errorMessage"));
            return withholdFlowDTO;
        } else {
            String statusCode = requestResult.getString("statusCode");
            Integer state = null;
            if (statusCode.equals(StaticDataEnum.INTEGRAPAY_STATUS_S.getMessage())) {
                state = StaticDataEnum.TRANS_STATE_1.getCode();
            } else if (statusCode.equals(StaticDataEnum.INTEGRAPAY_STATUS_F.getMessage()) || statusCode.equals(StaticDataEnum.INTEGRAPAY_STATUS_R.getMessage())) {
                state = StaticDataEnum.TRANS_STATE_2.getCode();
            } else {
                state = StaticDataEnum.TRANS_STATE_3.getCode();
            }
            withholdFlowDTO.setState(state);
            withholdFlowDTO.setReturnCode(requestResult.getString("errorCode"));
            withholdFlowDTO.setReturnMessage( requestResult.getString("errorMessage"));
            withholdFlowDTO.setLpsTransactionId( requestResult.getString("transactionId"));
            return withholdFlowDTO;
        }
    }

    @Override
    public Integer payByCardStatusCheck(String merchantBusinessId, String orderNo, String token) throws Exception {
        log.info("pay by card status check, businessId:{}, orderNo:{}", merchantBusinessId, orderNo);
        JSONObject requestResult = null;
        try {
            requestResult = JSONObject.parseObject(HttpClientUtils.getForIntegraPay("https://sandbox.rest.paymentsapi.io/businesses/" + merchantBusinessId + "/transactions/card-payments/?reference=" + orderNo, "Bearer " + token));
        } catch (Exception e) {
            log.info("pay by card status check fail, businessId:{}, orderNo:{}, error message:{}, e:{}", merchantBusinessId, orderNo, e.getMessage(), e);
            throw e;
        }
        Integer state = null;
        if (!StringUtils.isEmpty(requestResult.getString("errorCode"))) {
            log.info("pay by card status check, businessId:{}, orderNo:{}, error message:{}, e:{}", merchantBusinessId, orderNo, requestResult.getString("errorMessage"), requestResult.getString("errorCode"));
            throw new BizException(requestResult.getString("errorCode"));
        } else {
            String statusCode = requestResult.getString("statusCode");
            if (statusCode.equals(StaticDataEnum.INTEGRAPAY_STATUS_S.getMessage())) {
                state = StaticDataEnum.TRANS_STATE_1.getCode();
            } else if (statusCode.equals(StaticDataEnum.INTEGRAPAY_STATUS_F.getMessage()) || statusCode.equals(StaticDataEnum.INTEGRAPAY_STATUS_R.getMessage())) {
                state = StaticDataEnum.TRANS_STATE_2.getCode();
            } else {
                state = StaticDataEnum.TRANS_STATE_3.getCode();
            }
        }
        return state;
    }

    @Override
    public String payerAccountAdd(JSONObject requestInfo, String payerUnique, String token) throws Exception {
        log.info("payer account add, data:{}", requestInfo);
        JSONObject requestResult = null;
        try {
            requestResult = JSONObject.parseObject(HttpClientUtils.putForIntegraPay("https://sandbox.rest.paymentsapi.io/businesses/" + businessId + "/payers/" + payerUnique + "/accounts/bank-account", "Bearer " + token, requestInfo.toJSONString()));
        } catch (Exception e ) {
            log.info("payer account add fail, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            throw e;
        }
        log.info("payer account add result, data:{}", requestResult);
        if (!StringUtils.isEmpty(requestResult.getString("errorCode"))) {
            log.info("payer account add fail, data:{}, error message:{}, e:{}", requestInfo, requestResult.getString("errorMessage"), requestResult.getString("errorCode"));
            throw new BizException(requestResult.getString("errorCode"));
        }
        return requestResult.getString("accountId");
    }

    @Override
    public JSONObject payByAccount(JSONObject data, HttpServletRequest request) throws Exception {
        log.info("integra account pay, data:{}", data);
        JSONObject userInfo = data.getJSONObject("userInfo");
        JSONObject cardInfo = data.getJSONObject("cardInfo");
        BigDecimal amonut = data.getBigDecimal("amount");
        if (userInfo == null || cardInfo == null || (amonut != null && amonut.compareTo(new BigDecimal("0")) == 0)) {
            log.info("direct debit transaction failed, data:{}", data);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        Long orderNo = SnowflakeUtil.generateId();
        Map<String, Object> params = new HashMap<>(1);
        //匹配渠道
        params.clear();
        params.put("type", StaticDataEnum.GATEWAY_TYPE_5.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        //创建流水记录
        WithholdFlowDTO withholdFlowDTO = new WithholdFlowDTO();
        withholdFlowDTO.setUserId(userInfo.getLong("userId"));
        withholdFlowDTO.setGatewayMerchantId(gatewayDTO.getChannelMerchantId());
        withholdFlowDTO.setGatewayMerchantPassword(gatewayDTO.getPassword());
        withholdFlowDTO.setFlowId(data.getLong("flowId"));
        withholdFlowDTO.setGatewayId(gatewayDTO.getType());
        withholdFlowDTO.setTransAmount(amonut);
        withholdFlowDTO.setOrdreNo(orderNo.toString());
        withholdFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_7.getCode());
        withholdFlowDTO.setCustomerEmail(userInfo.getString("email"));
        withholdFlowDTO.setCustomerPhone(userInfo.getString("phone"));
        withholdFlowDTO.setCustomerFirstname(userInfo.getString("firstName"));
        withholdFlowDTO.setCustomerLastname(userInfo.getString("lastName"));
        withholdFlowDTO.setCustomerIpaddress(getIp(request));
        withholdFlowDTO.setBillFirstname(cardInfo.getString("firstName"));
        withholdFlowDTO.setBillLastname(cardInfo.getString("lastName"));
        withholdFlowDTO.setBillAddress1(cardInfo.getString("address1"));
        withholdFlowDTO.setBillCity(cardInfo.getString("city"));
        withholdFlowDTO.setBillCountry(cardInfo.getString("country"));
        withholdFlowDTO.setBillZip(cardInfo.getString("zip"));
        withholdFlowDTO.setCurrency(StaticDataEnum.CURRENCY_TYPE.getMessage());
        withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        withholdFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_7.getCode());
        Long id = withholdFlowService.saveWithholdFlow(withholdFlowDTO, request);
        // 调用三方查询
        String token = apiAccessToken();
        withholdFlowDTO = payByCard(withholdFlowDTO, token);
        // 更新流水
        withholdFlowService.updateWithholdFlow(id, withholdFlowDTO, request);
        // 拼装返回参数
        JSONObject returnData = new JSONObject();
        returnData.put("flowId", data.getLong("flowId"));
        returnData.put("orderNo", withholdFlowDTO.getOrdreNo());
        if (withholdFlowDTO.getState() == StaticDataEnum.TRANS_STATE_1.getCode()) {
            returnData.put("code", ErrorCodeEnum.SUCCESS_CODE.getCode());
        } else if (withholdFlowDTO.getState() == StaticDataEnum.TRANS_STATE_2.getCode()) {
            returnData.put("code", ErrorCodeEnum.FAIL_CODE.getCode());
        } else if (withholdFlowDTO.getState() == StaticDataEnum.TRANS_STATE_3.getCode()) {
            returnData.put("code", ErrorCodeEnum.LAT_PAY_DOUBLE.getCode());
        }
        return returnData;
    }

    @Override
    public void payByAccountStatusCheck() throws Exception {
        Map<String, Object> params = new HashMap<>(1);
        params.put("state", StaticDataEnum.TRANS_STATE_3.getCode());
        params.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_7.getCode());
        List<WithholdFlowDTO> withholdFlowDTOList = withholdFlowService.find(params, null, null);
        Integer status = null;
        for (WithholdFlowDTO withholdFlowDTO : withholdFlowDTOList) {
            try {
                String token = apiAccessToken();
                status = payByCardStatusCheck(withholdFlowDTO.getGatewayMerchantId(), withholdFlowDTO.getOrdreNo(), token);
            } catch (Exception e) {
                log.info("pay by account status check fail, data:{}, error message:{}, e:{}", withholdFlowDTO, e.getMessage(), e);
                status = StaticDataEnum.TRANS_STATE_3.getCode();
            }
            // 创建通知数据
            JSONObject notifyData = new JSONObject();
            notifyData.put("flowId",withholdFlowDTO.getFlowId());
            if (status == StaticDataEnum.TRANS_STATE_1.getCode()) {
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                notifyData.put("code", ErrorCodeEnum.SUCCESS_CODE.getCode());
            } else if (status == StaticDataEnum.TRANS_STATE_2.getCode()) {
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                notifyData.put("code", ErrorCodeEnum.FAIL_CODE.getCode());
            } else {
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                notifyData.put("code", ErrorCodeEnum.LAT_PAY_DOUBLE.getCode());
            }
            try{
                serverService.transTypeToCredit(notifyData);
            }catch (Exception e){
                log.info(" transTypeToCredit  Exception  flowId{} ", notifyData.get("flowId"),e.getMessage(),e);
            }
        }
    }

    @Override
    public void cardUnbundling(String payerUnique, String token) throws Exception {
        log.info("card unbundling, payerUnique:{}", payerUnique);
        JSONObject requestInfo = new JSONObject();
        requestInfo.put("NewStatus", "C");
        try {
            HttpClientUtils.putForIntegraPay("https://sandbox.rest.paymentsapi.io/businesses/" + businessId + "/payers/" + payerUnique + "/status", "Bearer " + token, requestInfo.toJSONString());
        } catch (Exception e ) {
            log.info("card unbundling fail, payerUnique:{}, error message:{}, e:{}", payerUnique, e.getMessage(), e);
            throw e;
        }
    }
}
