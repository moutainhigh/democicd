package com.uwallet.pay.main.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.stripe.model.Refund;
import com.stripe.model.RefundCollection;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.HttpClientUtils;
import com.uwallet.pay.core.util.JSONResultHandle;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.TestEnvUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * LatPay相关方法
 */
@Service
@Slf4j
public class LatPayServiceImpl extends BaseServiceImpl implements LatPayService {

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private WithholdFlowService withholdFlowService;

    @Value("${latpay.payUrl}")
    private String payUrl;

    @Value("${latpay.accountPayUrl}")
    private String accountPayUrl;

    @Value("${latpay.refundUrl}")
    private String refundUrl;

    @Value("${latpay.refundStatusCheckUrl}")
    private String refundStatusCheckUrl;

    @Value("${latpay.statusCheckUrl}")
    private String statusCheck;

    @Value("${latpay.ip}")
    private String ip;

    @Value("${latpay.notifyUrl}")
    private String notifyUrl;

    /**
     * DD交易storie_id
     */
    private static final String STORIE_ID = "001";

    /**
     * DD交易device_id
     */
    private static final String DEVICE_ID = "01";

    @Value("${latpay.terminalsecret}")
    private String terminalsecret;

    @Value("${latpay.accountCheckUrl}")
    private String accountCheckUrl;

    @Autowired
    private StaticDataService staticDataService;

    @Autowired
    private TieOnCardFlowService tieOnCardFlowService;

    @Autowired
    @Lazy
    private ServerService serverService;

    @Autowired
    private HolidaysConfigService holidaysConfigService;

    @Override
    public JSONObject thirdSystem(JSONObject data, HttpServletRequest request) throws Exception {
        JSONObject userInfo = data.getJSONObject("userInfo");
        JSONObject cardInfo = data.getJSONObject("cardInfo");
        //LatPay 交易固定参数
        cardInfo.put("address1", "5 GA, 199 George St");
        cardInfo.put("city", "Brisbane");
        cardInfo.put("state", "20");
        cardInfo.put("country", "1");
        cardInfo.put("zip", "4000");
        BigDecimal amonut = data.getBigDecimal("amount");

        Long orderNo = SnowflakeUtil.generateId();
        Map<String, Object> params = new HashMap<>(1);
        params.put("code", "merchantState");
        params.put("value", cardInfo.getString("state"));
        StaticDataDTO state = staticDataService.findOneStaticData(params);
        params.clear();
        params.put("code", "county");
        params.put("value", cardInfo.getString("country"));
        StaticDataDTO country = staticDataService.findOneStaticData(params);
        CountryIsoDTO countryIsoDTO = tieOnCardFlowService.selectCountryIso(country.getEnName());
        //匹配渠道
        params.clear();
        params.put("type", StaticDataEnum.GATEWAY_TYPE_3.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        if (gatewayDTO.getId() == null && gatewayDTO.getState().intValue() == StaticDataEnum.STATUS_0.getCode()) {
            throw new BizException(I18nUtils.get("gateway.disabled", getLang(request)));
        }
        //密钥
        String key = amonut.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "AUD" + DEVICE_ID + terminalsecret;
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = messageDigest.digest(key.getBytes());
        key = Hex.encodeHexString(bytes);
        //装配请求数据
        JSONObject requestInfo = new JSONObject();
        requestInfo.put("accountid", gatewayDTO.getChannelMerchantId());
        requestInfo.put("storeid", STORIE_ID);
        requestInfo.put("deviceid", DEVICE_ID);
        requestInfo.put("merchantkey", key.toUpperCase());
        //consumer
        JSONObject consumer = new JSONObject();
        consumer.put("firstname", userInfo.getString("userFirstName"));
        consumer.put("lastname", userInfo.getString("userLastName"));
        consumer.put("phone", new StringBuilder(userInfo.getString("phone")).substring(2, userInfo.getString("phone").length()));
        consumer.put("email", userInfo.getString("email"));
        requestInfo.put("consumer", consumer);
        //order
        JSONObject order = new JSONObject();
        order.put("reference", orderNo.toString());
        order.put("currency", "AUD");
        order.put("amount", amonut.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        order.put("purchasesummary", "uwallet purchase by account");
        requestInfo.put("order", order);
        JSONObject billing = new JSONObject();
        //billing
        billing.put("type", "dd");
        JSONObject directDebit = new JSONObject();
        directDebit.put("bsb", cardInfo.getString("bsb"));
        directDebit.put("accountnumber", cardInfo.getString("cardNo"));
        directDebit.put("accountname", cardInfo.getString("accountName"));
        billing.put("directentry", directDebit);
        JSONObject address = new JSONObject();
        address.put("line1", cardInfo.getString("address1"));
        address.put("line2", cardInfo.getString("address2"));
        address.put("city", cardInfo.getString("city"));
        address.put("state", state.getEnName());
        address.put("country", countryIsoDTO.getTwoLettersCoding());
        address.put("zipcode", cardInfo.getString("zip"));
        billing.put("address", address);
        JSONObject fees = new JSONObject();
        fees.put("processingfee", new BigDecimal("0.00"));
        billing.put("fees", fees);
        requestInfo.put("billing", billing);
        //call_param
        JSONObject callParams = new JSONObject();
        callParams.put("orderNo", orderNo.toString());
        requestInfo.put("callback_params", callParams);
        //notify
        requestInfo.put("notifyurl", notifyUrl);
        //创建流水记录
        WithholdFlowDTO withholdFlowDTO = new WithholdFlowDTO();
        withholdFlowDTO.setUserId(userInfo.getLong("userId"));
        withholdFlowDTO.setGatewayMerchantId(gatewayDTO.getChannelMerchantId());
        withholdFlowDTO.setGatewayMerchantPassword(gatewayDTO.getPassword());
        withholdFlowDTO.setFlowId(data.getLong("flowId"));
        withholdFlowDTO.setGatewayId(gatewayDTO.getType());
        withholdFlowDTO.setTransAmount(amonut);
        withholdFlowDTO.setOrderAmount(amonut);
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
        //请求latpay
        withholdFlowDTO = latPayDirectDebitRequest(requestInfo, withholdFlowDTO, request);
        withholdFlowService.updateWithholdFlow(id, withholdFlowDTO, request);

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
    public WithholdFlowDTO latPayRequest(WithholdFlowDTO withholdFlowDTO, BigDecimal amount, BigDecimal fee, JSONObject cardInfo, HttpServletRequest request, String ip) throws Exception {
        //todo  三方挡板
        Map<String, Object> params111 = new HashMap<>(2);
        params111.put("code", "latpayState");
        StaticDataDTO staticDataDTO = staticDataService.findOneStaticData(params111);
        if(null != staticDataDTO && null != staticDataDTO.getId() ){
            if(withholdFlowDTO.getUserId().toString().equals("610067872276369408") || withholdFlowDTO.getUserId().toString().equals("602384778673082368")
                    || withholdFlowDTO.getUserId().toString().equals("620776802396753920")){
                withholdFlowDTO.setState(Integer.valueOf(staticDataDTO.getEnName()));
                return withholdFlowDTO;
            }
            if(staticDataDTO.getEnName().equals("realpay")){

            }else if(staticDataDTO.getEnName().equals("responeseTest")){
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                withholdFlowDTO.setReturnCode("05");
                withholdFlowDTO.setReturnMessage(staticDataDTO.getIp());
                return withholdFlowDTO;
            }else {
                withholdFlowDTO.setState(Integer.valueOf(staticDataDTO.getValue()));
                return withholdFlowDTO;
            }
        }

        JSONObject requestInfo = new JSONObject();
        //请求第三方接口
        requestInfo.put("merchant_User_Id", withholdFlowDTO.getGatewayMerchantId());
        requestInfo.put("merchantpwd", withholdFlowDTO.getGatewayMerchantPassword());
        requestInfo.put("merchant_ipaddress", this.ip);
        requestInfo.put("customer_firstname", cardInfo.getString("firstName"));
        requestInfo.put("customer_lastname", cardInfo.getString("lastName"));
        requestInfo.put("customer_phone", cardInfo.getString("phone"));
        requestInfo.put("customer_email", cardInfo.getString("email"));
        requestInfo.put("customer_ipaddress", ip);
        requestInfo.put("bill_firstname", cardInfo.getString("firstName"));
        requestInfo.put("bill_lastname", cardInfo.getString("lastName"));
        requestInfo.put("bill_address1", cardInfo.getString("address1"));
        requestInfo.put("bill_address2", cardInfo.getString("address2"));
        requestInfo.put("bill_city", cardInfo.getString("city"));
        requestInfo.put("bill_country", withholdFlowDTO.getBillCountry());
        requestInfo.put("bill_zip", cardInfo.getString("zip"));
        requestInfo.put("CrdStrg_Token", cardInfo.getString("crdStrgToken"));
        requestInfo.put("customer_cc_cvc", cardInfo.getString("customerCcCvc"));
        requestInfo.put("currencydesc", withholdFlowDTO.getCurrency());
        requestInfo.put("merchant_ref_number", withholdFlowDTO.getOrdreNo());
        requestInfo.put("amount", withholdFlowDTO.getTransAmount().toString());
//        if (fee != null) {
//            requestInfo.put("fees", fee.toString());
//        }
        requestInfo.put("scsscheck", "D");
        log.info("send msg to latpay, data:{}", requestInfo);
        Map params = requestInfo;
        String data = HttpClientUtils.sendPostForm(payUrl, params);
        // 解析响应结果
        JSONObject returnData = JSONResultHandle.resultHandle(data);
        log.info("get msg from latpay, data:{}", returnData);
        // 请求异常
        if (returnData.getString("transactionFailed").equals(ErrorCodeEnum.LAT_PAY_FAILED.getMessage())) {
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
        }
        // 获取请求返回信息
        JSONObject latPayMsg = returnData.getJSONObject("data");
        // 返回状态处于反欺诈阶段则交易失败，处于银行阶段记录银行阶段相关记录相关阶段
        if (latPayMsg.getInteger("ResponseType") == StaticDataEnum.LP_RESPONSE_TYPE_0.getCode()) {
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            withholdFlowDTO.setReturnCode(latPayMsg.getInteger("ResponseType").toString());
            withholdFlowDTO.setReturnMessage(latPayMsg.getString("Fraudscreening_status"));

//            withholdFlowDTO.setErrorMessage(latPayMsg.getString("bank_message"));


        } else {
            if (latPayMsg.getString("Bank_status").equals(StaticDataEnum.LAT_PAY_BANK_STATUS_0.getMessage())) {
                withholdFlowDTO.setLpsTransactionId(latPayMsg.getString("LPS_transaction_id"));
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                withholdFlowDTO.setReturnCode(StaticDataEnum.LAT_PAY_BANK_STATUS_0.getMessage());
            } else if (latPayMsg.getString("Bank_status").equals(StaticDataEnum.LAT_PAY_BANK_STATUS_5.getMessage())) {
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                withholdFlowDTO.setReturnCode(latPayMsg.getString("Bank_status"));
                withholdFlowDTO.setReturnMessage(latPayMsg.getString("bank_code"));
                withholdFlowDTO.setErrorMessage(latPayMsg.getString("bank_message"));
            } else {
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                withholdFlowDTO.setReturnCode(StaticDataEnum.LAT_PAY_BANK_STATUS_9.getMessage());
            }
        }
        return withholdFlowDTO;
    }

    @Override
    public WithholdFlowDTO latPayDirectDebitRequest(JSONObject requestInfo, WithholdFlowDTO withholdFlowDTO, HttpServletRequest request) throws Exception {
        log.info("latpay direct debit requestInfo, info:{}", requestInfo);
        JSONObject returnData = JSONObject.parseObject(HttpClientUtils.post(accountPayUrl, requestInfo.toJSONString()));
        log.info("latpay direct debit return data, returnData:{}", returnData);
        JSONObject status = returnData.getJSONObject("status");
        //根据返回值，判断订单受理结果
        if (status.getInteger("responsetype") == StaticDataEnum.LAT_PAY_DD_2.getCode()
                && status.getInteger("statuscode") == StaticDataEnum.LAT_PAY_DD_2.getCode()) {
            withholdFlowDTO.setLpsTransactionId(returnData.getString("transactionid"));
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            return withholdFlowDTO;
        } else {
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            withholdFlowDTO.setReturnCode(returnData.getString("errorcode"));
            withholdFlowDTO.setReturnMessage(returnData.getString("errordesc"));
            return withholdFlowDTO;
        }
    }

    @Override
    public JSONObject latPayRefundRequest(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        log.info("latpay refund requestInfo, info:{}", requestInfo);
        HashMap<String, Object> maps = Maps.newHashMapWithExpectedSize(1);
        maps.put("code", "latpayRefundState");
        StaticDataDTO staticData = staticDataService.findOneStaticData(maps);
        if(null != staticData && null != staticData.getId()){
            JSONObject result = new JSONObject();
            if(staticData.getValue().equals("1")){
                //成功
                result.put("RefundSubmit_status", 0);
                result.put("Bank_status", "00");
            }else if(staticData.getValue().equals("2")){
                //失败
                result.put("RefundSubmit_status", 0);
                result.put("Bank_status", "05");
            }else {
                //可疑
                result.put("RefundSubmit_status", 0);
                result.put("Bank_status", "90");
            }
            return result;
        }

        Map params = requestInfo;
        JSONObject returnData = new JSONObject();
        String[] latpayDatas = HttpClientUtils.sendPostForm(refundUrl, params).split("&");
        for (String latpayData : latpayDatas) {
            String[] unit = latpayData.split("=");
            if (unit.length == 2) {
                returnData.put(unit[0], unit[1]);
            } else {
                returnData.put(unit[0], "");
            }
        }
        log.info("latpay refund returnData, data:{}", returnData);
        return returnData;
    }

    @Override
    public void directDebitNotify(JSONObject requestInfo, HttpServletRequest request) throws BizException {
        JSONObject status = requestInfo.getJSONObject("status");
        String responsetype = status.getString("responsetype");
        String statuscode = status.getString("statuscode");
        String errorcode = status.getString("errorcode");
        String errordesc = status.getString("errordesc");
        String reference  =requestInfo.getString("transactionid") ;
        //查询原交易
        Map<String, Object> params = new HashMap<>(2);
        params.put("lpsTransactionId",reference);
        WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(params);
        if(withholdFlowDTO==null || withholdFlowDTO.getId() == null){
            return ;
        }
        Map<String ,Object > map = new HashMap<>(4);
        map.put("flowId",withholdFlowDTO.getFlowId());
        map.put("latPayTransactionId", withholdFlowDTO.getLpsTransactionId());
        map.put("orderNo", withholdFlowDTO.getOrdreNo());
        // 异步通知全部接收订单接收通知不在接收return通知，return判断全部移到状态查询
        if (String.valueOf(StaticDataEnum.LAT_PAY_DD_RESPONSE_TYPE.getCode()).equals(responsetype)) {
            if (String.valueOf(StaticDataEnum.LAT_PAY_DD_0.getCode()).equals(statuscode)){
                // 成功
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                withholdFlowDTO.setReturnCode(errorcode);
                withholdFlowDTO.setReturnMessage(errordesc);
                withholdFlowService.updateWithholdFlowForConcurrency(withholdFlowDTO.getId(),withholdFlowDTO,null);
            } else if (String.valueOf(StaticDataEnum.LAT_PAY_DD_1.getCode()).equals(statuscode)){
                // 失败
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                withholdFlowDTO.setReturnCode(errorcode);
                withholdFlowDTO.setReturnMessage(errordesc);
                withholdFlowService.updateWithholdFlowForConcurrency(withholdFlowDTO.getId(),withholdFlowDTO,null);
                if(withholdFlowDTO.getTransType()==StaticDataEnum.ACC_FLOW_TRANS_TYPE_7.getCode()){
                    //  主动通知到分期付
                    map.put("code", ErrorCodeEnum.FAIL_CODE.getCode());
                    transTypeToCredit(map);
                }
            }
        }
    }

    private void transTypeToCredit(Map<String, Object> map) {
        try{
            JSONObject params =new JSONObject(map);
            serverService.transTypeToCredit(params);
        }catch (Exception e){
            log.info(" transTypeToCredit  Exception  flowId{} ",map.get("flowId"),e.getMessage(),e);
        }
    }

    @Override
    public WithholdFlowDTO latPayDoubtHandle(WithholdFlowDTO withholdFlowDTO) throws Exception {

        //todo  三方挡板
        Map<String, Object> params111 = new HashMap<>(2);
        params111.put("code", "latpayState");
        StaticDataDTO staticDataDTO = staticDataService.findOneStaticData(params111);
        if(null != staticDataDTO && null != staticDataDTO.getId() ){
            if(staticDataDTO.getEnName().equals("realpay")){

            }else if(staticDataDTO.getEnName().equals("responeseTest")){
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                withholdFlowDTO.setReturnCode("05");
                withholdFlowDTO.setReturnMessage(staticDataDTO.getIp());
                return withholdFlowDTO;
            }else {
                withholdFlowDTO.setState(Integer.valueOf(staticDataDTO.getValue()));
                return withholdFlowDTO;
            }
        }

        //三方查证参数
        JSONObject requestInfo = new JSONObject();
        requestInfo.put("merchant_user_id", withholdFlowDTO.getGatewayMerchantId());
        requestInfo.put("merchantpwd", withholdFlowDTO.getGatewayMerchantPassword());
        requestInfo.put("merchant_ipaddress", ip);
        requestInfo.put("merchant_ref_number", withholdFlowDTO.getOrdreNo());
        requestInfo.put("lps_transaction_id", withholdFlowDTO.getLpsTransactionId());
        requestInfo.put("realtime","Y");
        requestInfo.put("currencydesc", withholdFlowDTO.getCurrency());
        requestInfo.put("amount", withholdFlowDTO.getTransAmount().toString());
        Map params = requestInfo;
        log.info("requestInfo:{}", requestInfo);
        String data = HttpClientUtils.sendPostForm(statusCheck, params);
        // 解析响应结果
        JSONObject returnData = JSONResultHandle.resultHandle(data);
        log.info("return data:{}", returnData);
        if (returnData.getString("transactionFailed").equals(ErrorCodeEnum.LAT_PAY_FAILED.getMessage())) {
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
        }
        // 获取请求返回信息
        JSONObject latPayMsg = returnData.getJSONObject("data");
        // 返回状态处于反欺诈阶段则交易失败，处于银行阶段记录银行阶段相关记录相关阶段
        if (latPayMsg.getInteger("responsetype") == StaticDataEnum.LP_RESPONSE_TYPE_0.getCode()) {
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            withholdFlowDTO.setReturnCode(latPayMsg.getInteger("responsetype").toString());
            withholdFlowDTO.setReturnMessage(latPayMsg.getString("authstatuscheck_status"));
        } else {
            if (latPayMsg.getString("transaction_status").equals(StaticDataEnum.LAT_PAY_BANK_STATUS_0.getMessage())) {
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                withholdFlowDTO.setReturnCode(StaticDataEnum.LAT_PAY_BANK_STATUS_0.getMessage());
                withholdFlowDTO.setReturnMessage(latPayMsg.getString("transaction_status"));
            } else if (latPayMsg.getString("transaction_status").equals(StaticDataEnum.LAT_PAY_BANK_STATUS_5.getMessage())) {
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                withholdFlowDTO.setReturnCode(StaticDataEnum.LAT_PAY_BANK_STATUS_5.getMessage());
                withholdFlowDTO.setReturnMessage(latPayMsg.getString("transaction_status"));
            } else {
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                withholdFlowDTO.setReturnCode(StaticDataEnum.LAT_PAY_BANK_STATUS_9.getMessage());
                withholdFlowDTO.setReturnMessage(latPayMsg.getString("transaction_status"));
            }
        }
        return withholdFlowDTO;
    }

    @Override
    public void directDebitStatusCheck() throws Exception {
        log.info("direct debit status check");
        Map<String, Object> params = new HashMap<>(1);
        params.put("gatewayId", StaticDataEnum.GATEWAY_TYPE_3.getCode());
        params.put("state", StaticDataEnum.TRANS_STATE_3.getCode());
        params.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_7.getCode());
        List<WithholdFlowDTO> withholdFlowDTOList = withholdFlowService.find(params, null, null);
        for (WithholdFlowDTO withholdFlowDTO : withholdFlowDTOList) {
            // 密钥
            String key = withholdFlowDTO.getLpsTransactionId() + withholdFlowDTO.getTransAmount().setScale(2, BigDecimal.ROUND_HALF_UP) + "AUD" + DEVICE_ID + terminalsecret;
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = messageDigest.digest(key.getBytes());
            key = Hex.encodeHexString(bytes);
            // 拼装请求数据
            JSONObject requestInfo = new JSONObject();
            requestInfo.put("accountid", withholdFlowDTO.getGatewayMerchantId());
            requestInfo.put("storeid", STORIE_ID);
            requestInfo.put("deviceid", DEVICE_ID);
            requestInfo.put("merchantkey", key);
            requestInfo.put("transactionid", withholdFlowDTO.getLpsTransactionId());
            JSONObject order = new JSONObject();
            order.put("reference", withholdFlowDTO.getOrdreNo());
            order.put("currency", "AUD");
            order.put("amount", withholdFlowDTO.getTransAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            order.put("purchasesummary", "uwallet purchase by account");
            requestInfo.put("order", order);
            JSONObject bill = new JSONObject();
            bill.put("type", "dd");
            requestInfo.put("billing", bill);
            String resultInfo = "";
            log.info("request info:{}", requestInfo);
            try {
                resultInfo = HttpClientUtils.post(accountCheckUrl, requestInfo.toJSONString());
            } catch (Exception e) {
                log.info("data:{}, message:{}, e:{}", requestInfo, e.getMessage(), e);
                continue;
            }
            log.info("result info:{}", resultInfo);
            JSONObject result = JSONObject.parseObject(resultInfo);
            JSONObject status = result.getJSONObject("status");
            JSONObject returnStatus = result.getJSONObject("returnstatus");
            // 创建通知数据
            Map<String ,Object > map = new HashMap<>(4);
            map.put("flowId",withholdFlowDTO.getFlowId());
            map.put("latPayTransactionId", withholdFlowDTO.getLpsTransactionId());
            map.put("orderNo", withholdFlowDTO.getOrdreNo());
            // 结果是银行返回
            if (String.valueOf(StaticDataEnum.LAT_PAY_DD_0.getCode()).equals(status.getString("statuscode"))){
                // 成功
                // 订单超过六天未接收到return结果通知则视为交易成功
                String returnCode = "";
                if (returnStatus != null) {
                    returnCode = returnStatus.getString("returncode");
                }
                if (StringUtils.isEmpty(returnCode)) {
                    if (itIsEnoughForSixDays(withholdFlowDTO.getCreatedDate().longValue(), System.currentTimeMillis())) {
                        // 成功
                        withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                        withholdFlowDTO.setReturnCode(status.getString("errorcode"));
                        withholdFlowDTO.setReturnMessage(status.getString("errordesc"));
                        withholdFlowService.updateWithholdFlowForConcurrency(withholdFlowDTO.getId(),withholdFlowDTO,null);
                        if(withholdFlowDTO.getTransType()==StaticDataEnum.ACC_FLOW_TRANS_TYPE_7.getCode()){
                            //主动通知分期付
                            map.put("code", ErrorCodeEnum.SUCCESS_CODE.getCode());
                            transTypeToCredit(map);
                        }
                    }
                } else {
                    // 失败
                    withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    withholdFlowDTO.setReturnCode(status.getString("errorcode"));
                    withholdFlowDTO.setReturnMessage(status.getString("errordesc"));
                    withholdFlowService.updateWithholdFlowForConcurrency(withholdFlowDTO.getId(),withholdFlowDTO,null);
                    if(withholdFlowDTO.getTransType()==StaticDataEnum.ACC_FLOW_TRANS_TYPE_7.getCode()){
                        //  主动通知到分期付
                        map.put("code", ErrorCodeEnum.FAIL_CODE.getCode());
                        transTypeToCredit(map);
                    }
                }
            } else if (String.valueOf(StaticDataEnum.LAT_PAY_DD_1.getCode()).equals(status.getString("statuscode"))){
                // 失败
                String errorCode = status.getString("errorcode");
                String returnCode = "";
                if (returnStatus != null) {
                    returnCode = returnStatus.getString("returncode");
                }
                if (errorCode.equals(StaticDataEnum.LAT_PAY_DD_CHECK_9001.getMessage())
                        || errorCode.equals(StaticDataEnum.LAT_PAY_DD_CHECK_6003.getMessage())
                        || errorCode.equals(StaticDataEnum.LAT_PAY_DD_CHECK_5011.getMessage())
                        || errorCode.equals(StaticDataEnum.LAT_PAY_DD_CHECK_5012.getMessage())
                        || errorCode.equals(StaticDataEnum.LAT_PAY_DD_CHECK_5013.getMessage())
                        || errorCode.equals(StaticDataEnum.LAT_PAY_DD_CHECK_5021.getMessage())
                        || !StringUtils.isEmpty(returnCode)) {
                    withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    withholdFlowDTO.setReturnCode(status.getString("errorcode"));
                    withholdFlowDTO.setReturnMessage(status.getString("errordesc"));
                    withholdFlowService.updateWithholdFlowForConcurrency(withholdFlowDTO.getId(),withholdFlowDTO,null);
                    if(withholdFlowDTO.getTransType()==StaticDataEnum.ACC_FLOW_TRANS_TYPE_7.getCode()){
                        // 主动通知到分期付
                        map.put("code", ErrorCodeEnum.FAIL_CODE.getCode());
                        transTypeToCredit(map);
                    }
                }
            }
        }
    }

    @Override
    public JSONObject latPayRefundCheck(JSONObject requestInfo) throws Exception {
        log.info("latpay refund check requestInfo, info:{}", requestInfo);
        HashMap<String, Object> maps = Maps.newHashMapWithExpectedSize(1);
        maps.put("code", "latpayRefundState");
        StaticDataDTO staticData = staticDataService.findOneStaticData(maps);
        if(null != staticData && null != staticData.getId()){
            JSONObject result = new JSONObject();
            if(staticData.getValue().equals("1")){
                //成功
                result.put("RefundSubmit_status", "0");
                result.put("Bank_status", "00");
                result.put("RefundStatusCheck_status","0");
            }else if(staticData.getValue().equals("2")){
                //失败
                result.put("RefundSubmit_status", "0");
                result.put("Bank_status", "05");
                result.put("RefundStatusCheck_status","0");
            }else {
                //可疑
                result.put("RefundSubmit_status","0");
                result.put("Bank_status", "90");
                result.put("RefundStatusCheck_status","0");
            }
            return result;
        }

        Map params = requestInfo;
        JSONObject returnData = new JSONObject();
        String[] latpayDatas = HttpClientUtils.sendPostForm(refundStatusCheckUrl, params).split("&");

        log.info("return:"+latpayDatas);
//        returnData = JSONObject.parseObject(HttpClientUtils.post(refundStatusCheckUrl, requestInfo.toJSONString()));
        for (String latpayData : latpayDatas) {
            String[] unit = latpayData.split("=");
            if (unit.length == 2) {
                returnData.put(unit[0], unit[1]);
            } else {
                returnData.put(unit[0], "");
            }
        }
        log.info("latpay refund check returnData, data:{}", returnData);
        return returnData;
    }

    /**
     * 判断分期付订单是否足够六天
     * @param start
     * @param end
     * @return
     */
    public boolean itIsEnoughForSixDays (long start, long end) {
        boolean result = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String beginTime = sdf.format(new Date(start));
        String endTime = sdf.format(new Date(end));
        List<String> list = new ArrayList<>();
        // LocalDate默认的时间格式为yyyy-MM-dd
        LocalDate startDate = LocalDate.parse(beginTime);
        LocalDate endDate = LocalDate.parse(endTime);
        long distance = ChronoUnit.DAYS.between(startDate, endDate);
        List<String> finalList = list;
        Stream.iterate(startDate, d -> d.plusDays(1)).limit(distance + 1).forEach(f -> finalList.add(f.toString()));
        if (!list.isEmpty()) {
            list = finalList.stream()
                    .filter(date -> !holidaysConfigService.isItAHoliday(date))
                    .filter(date -> {
                        boolean isWeekend = false;
                        Calendar calendar = Calendar.getInstance();
                        try {
                            calendar.setTime(sdf.parse(date));
                            isWeekend = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return !isWeekend;
                    })
                    .collect(Collectors.toList());
            if (!list.isEmpty() && list.size() >= 6) {
                result = true;
            }
        }
        return result;
    }

}
