package com.uwallet.pay.main.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONNull;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.cloud.BaseService;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.model.entity.BaseEntity;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.HttpClientUtils;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.model.dto.GatewayDTO;
import com.uwallet.pay.main.model.dto.WithholdFlowDTO;
import com.uwallet.pay.main.service.GatewayService;
import com.uwallet.pay.main.service.ServerService;
import com.uwallet.pay.main.service.SplitService;
import com.uwallet.pay.main.service.WithholdFlowService;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author baixinyue
 * @createdDate 2020-11-25
 * @description split三方
 */

@Service
@Slf4j
public class SplitServiceImpl extends BaseServiceImpl implements SplitService {

    @Value("${split.token}")
    private String token;

    @Value("${split.url}")
    private String url;

    @Autowired
    private WithholdFlowService withholdFlowService;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    @Lazy
    private ServerService serverService;

    @Override
    public JSONObject splitTieAccount(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        String name = requestInfo.getString("name");
        String email = requestInfo.getString("email");
        String accountNo = requestInfo.getString("cardNo");
        String bsb = requestInfo.getString("bsb");
        checkAccoutnParams(requestInfo, request);
        //todo 集合定容量
        JSONObject authoriser = new JSONObject(4);
        authoriser.put("name", name);
        authoriser.put("email", email);
        JSONObject accountInfo = new JSONObject(6);
        accountInfo.put("branch_code", bsb);
        accountInfo.put("account_number", accountNo);
        authoriser.put("bank_account", accountInfo);

        JSONObject terms = new JSONObject(4);
        JSONObject perPayout = new JSONObject(4);
        perPayout.put("min_amount", null);
        perPayout.put("max_amount", null);
        JSONObject perFrequency = new JSONObject(4);
        perFrequency.put("days", null);
        perFrequency.put("max_amount", null);
        terms.put("per_payout", perPayout);
        terms.put("per_frequency", perFrequency);

        JSONObject splitRequestInfo = new JSONObject();
        splitRequestInfo.put("authoriser", authoriser);
        splitRequestInfo.put("terms", terms);

        JSONObject requestResult = null;
        try {
            log.info("split tie on card requestInfo, data:{}", splitRequestInfo.toJSONString());
            requestResult = JSONObject.parseObject(HttpClientUtils.postForSplitPay(url + "/agreements/kyc", token, JSONObject.toJSONString(splitRequestInfo, SerializerFeature.WriteMapNullValue)));
        } catch (Exception e) {
            log.info("split tie on card request failed, data:{}, error message:{}, e:{}", splitRequestInfo.toJSONString(), e.getMessage(), e);
            throw new BizException(I18nUtils.get("split.tie.on.card.fail", getLang(request)));
        }
        log.info("split tie on card requestResult, data:{}", requestResult.toJSONString());
        String errors = requestResult.getString("errors");
        if (!StringUtils.isEmpty(errors)) {
            throw new BizException(errors);
        }
        JSONObject data = requestResult.getJSONObject("data");
        String contactId = data.getString("contact_id");
        String ref = data.getString("ref");
        JSONObject result = new JSONObject();
        result.put("contactId", contactId);
        result.put("agreementRef", ref);
        return result;
    }

    @Override
    public void splitDeleteAccount(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        String agreementRef = requestInfo.getString("agreementRef");
        String contactId = requestInfo.getString("contactId");
        if (StringUtils.isEmpty(agreementRef) || StringUtils.isEmpty(contactId)) {
            throw new BizException(I18nUtils.get("split.account.delete.failed", getLang(request)));
        }
        try {
            StringBuilder deleteAgreementsUrl = new StringBuilder(url);
            HttpClientUtils.deleteForSplitPay(deleteAgreementsUrl.append("/agreements/").append(agreementRef).toString(), token);
            StringBuilder deleteContactUrl = new StringBuilder(url);
            HttpClientUtils.deleteForSplitPay(deleteContactUrl.append("/contacts/").append(contactId).toString(), token);
        } catch (Exception e) {
            log.error("delete account failed, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            throw new BizException(I18nUtils.get("split.account.delete.fail", getLang(request)));
        }
    }

    @Override
    public JSONObject  splitPayRequestInfo(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        JSONObject userInfo = requestInfo.getJSONObject("userInfo");
        JSONObject cardInfo = requestInfo.getJSONObject("cardInfo");
        BigDecimal amount = requestInfo.getBigDecimal("amount");
        // 请求信息、金额校验
        // todo 全参数单独校验,分别返回提示信息 金额校验可拆分,当前splitContactId是否属于当前登录用户的校验
        //todo 提示信息请报出参数错误
        //todo BigDecimal.ZERO
        //todo 调用tt的工具类校验金额
        if ( StringUtils.isEmpty(cardInfo.getString("splitContactId") )) {
            log.info("direct debit transaction failed, data:{}", requestInfo);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        // 查询路由是否可用
        Map<String, Object> params = new HashMap<>(16);
        params.put("type", StaticDataEnum.GATEWAY_TYPE_6.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        if (gatewayDTO.getId() == null || gatewayDTO.getState().intValue() == StaticDataEnum.STATUS_0.getCode()) {
            throw new BizException(I18nUtils.get("gateway.disabled", getLang(request)));
        }
        WithholdFlowDTO withholdFlowDTO = getWithholdFlowDTO(requestInfo, request, userInfo, cardInfo, amount, gatewayDTO);
        withholdFlowDTO.setSplitContactId(cardInfo.getString("splitContactId"));
        //计算发送三方的交易金额
        BigDecimal thirdTransAmount = (amount.add(withholdFlowDTO.getCharge()));
        withholdFlowDTO.setTransAmount(thirdTransAmount);
        Long withholdFlowId = withholdFlowService.saveWithholdFlow(withholdFlowDTO, request);



        JSONObject splitRequsetInfo = new JSONObject(12);
        splitRequsetInfo.put("description", "repayment");
        splitRequsetInfo.put("matures_at", LocalDateTime.now());
        //三方以分为单位
        splitRequsetInfo.put("amount", thirdTransAmount.multiply(new BigDecimal("100")));
        splitRequsetInfo.put("authoriser_contact_id", cardInfo.getString("splitContactId"));
        splitRequsetInfo.put("precheck_funds", false);
        JSONObject metadata = new JSONObject(4);
        metadata.put("orderNo", withholdFlowDTO.getOrdreNo());
        splitRequsetInfo.put("metadata", metadata);
        // 请求三方交易结果
        JSONObject requestResult;
        try {
            log.info("split payment request, data:{}", splitRequsetInfo);
            StringBuffer paymentRequestUrl = new StringBuffer(url);
            requestResult = JSONObject.parseObject(HttpClientUtils.postForSplitPay(paymentRequestUrl.append("/payment_requests").toString(), token, splitRequsetInfo.toJSONString()));
        } catch (Exception e) {
            log.error("split payment request failed, data:{}, error message:{}, e:{}", splitRequsetInfo.toJSONString(), e.getMessage(), e);
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            withholdFlowService.updateWithholdFlowForConcurrency(withholdFlowId, withholdFlowDTO, request);
            throw new BizException(I18nUtils.get("split.pay.request.fail", getLang(request)));
        }
        log.info("split payment request requestResult, data:{}", requestResult.toJSONString());
        String errors = requestResult.getString("errors");
        if (!StringUtils.isEmpty(errors)) {
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            withholdFlowDTO.setReturnMessage(errors);
            withholdFlowService.updateWithholdFlowForConcurrency(withholdFlowId, withholdFlowDTO, request);
            throw new BizException(requestResult.getString("errors"));
        }
        JSONObject data = requestResult.getJSONObject("data");
        String status = data.getString("status");
        String ref = data.getString("ref");
        // 返回给分期付交易结果，更新三方流水表
        JSONObject returnData = new JSONObject();
        returnData.put("flowId", requestInfo.getLong("flowId"));
        returnData.put("orderNo", withholdFlowDTO.getOrdreNo());
        returnData.put("charge",withholdFlowDTO.getCharge());
        withholdFlowDTO.setSplitNo(ref);
        if (status.equals(StaticDataEnum.SPLIT_PAY_REQUEST_APPROVED.getMessage())) {
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            returnData.put("code", ErrorCodeEnum.LAT_PAY_DOUBLE.getCode());
        } else {
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            returnData.put("code", ErrorCodeEnum.FAIL_CODE.getCode());
        }
        withholdFlowService.updateWithholdFlowForConcurrency(withholdFlowId, withholdFlowDTO, request);
        returnData.put("displayData",this.packCreditRepayResData(withholdFlowId,request));
        return returnData;
    }

    private JSONObject packCreditRepayResData(Long withholdFlowId, HttpServletRequest request) {
        WithholdFlowDTO withholdFlowDTO = withholdFlowService.findWithholdFlowById(withholdFlowId);
        JSONObject displayData = new JSONObject();
        BigDecimal transAmount = withholdFlowDTO.getTransAmount();
        //还款总金额 还款金额+手续费
        displayData.put("totalAmount", transAmount);
        displayData.put("orderAmount",withholdFlowDTO.getOrderAmount());
        BigDecimal charge = withholdFlowDTO.getCharge();
        displayData.put("tansFee", charge);
        displayData.put("repayAmount",transAmount.subtract(charge));
//        SimpleDateFormat format = new SimpleDateFormat("dd MMM at hh:mm a", Locale.US);
        Long createdDate = withholdFlowDTO.getCreatedDate();
//        displayData.put("dateStr",null != createdDate  ? format.format(createdDate): "");
        SimpleDateFormat monthFormat = new SimpleDateFormat("dd MMM", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        String monthStr = monthFormat.format(createdDate);
        String timeStr = timeFormat.format(createdDate);
        displayData.put("dateStr", monthStr + " at " + timeStr);

        return displayData;
    }


    private WithholdFlowDTO getWithholdFlowDTO(JSONObject requestInfo, HttpServletRequest request, JSONObject userInfo, JSONObject cardInfo, BigDecimal amonut, GatewayDTO gatewayDTO) {
        //        // 计算通道手续费

        BigDecimal charge = gatewayService.getGateWayFee(gatewayDTO,amonut);

        WithholdFlowDTO withholdFlowDTO = new WithholdFlowDTO();
        withholdFlowDTO.setFeeRate(gatewayDTO.getRate());
        withholdFlowDTO.setFeeType(gatewayDTO.getRateType());
        withholdFlowDTO.setUserId(userInfo.getLong("userId"));
        withholdFlowDTO.setGatewayMerchantId(gatewayDTO.getChannelMerchantId());
        withholdFlowDTO.setGatewayMerchantPassword(gatewayDTO.getPassword());
        withholdFlowDTO.setFlowId(requestInfo.getLong("flowId"));
        withholdFlowDTO.setGatewayId(gatewayDTO.getType());
        withholdFlowDTO.setOrderAmount(amonut);
        withholdFlowDTO.setOrdreNo(SnowflakeUtil.generateId().toString());
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
        withholdFlowDTO.setCharge(charge);
        return withholdFlowDTO;
    }

    @Override
    public JSONArray splitTransactionSearch(Map<String, Object> params, HttpServletRequest request) throws Exception {
        StringBuffer searchParams = new StringBuffer();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            searchParams.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        StringBuffer searchUrl = new StringBuffer(url).append("/transactions?").append(searchParams.substring(0, searchParams.lastIndexOf("&")));
        JSONArray resultList = null;
        JSONObject requestResult = null;
        try {
            log.info("split transaction search, data:{}", params);
            requestResult = JSONObject.parseObject(HttpClientUtils.getForSplitPay(searchUrl.toString(), token));
        } catch (Exception e) {
            log.info("split transaction search failed, request info:{}, error message:{}, e:{}", params, e.getMessage(), e);
            throw new BizException(I18nUtils.get("split.transaction.error", getLang(request)));
        }
        if (!StringUtils.isEmpty(requestResult.getString("errors"))) {
            throw new BizException(requestResult.getString("errors"));
        }
        resultList = requestResult.getJSONArray("data");
        return resultList;
    }

    @Override
    public void splitTransactionNotify(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        List<JSONObject> resultList = JSONArray.parseArray(requestInfo.getJSONArray("data").toJSONString(), JSONObject.class);
        if (CollectionUtil.isNotEmpty(resultList)) {
            Map<String, Object> params = new HashMap<>(16);
            Map<String ,Object > creditResult = new HashMap<>(4);
            for (JSONObject result: resultList) {
                String ref = result.getString("parent_ref");
                params.put("splitNo", ref);
                params.put("gatewayId", StaticDataEnum.GATEWAY_TYPE_6.getCode());
                params.put("state", StaticDataEnum.TRANS_STATE_3.getCode());
                params.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_7.getCode());
                WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(params);
                if (withholdFlowDTO.getId() != null) {
                    transactionResult(withholdFlowDTO, creditResult, result);
                }
            }
        }
    }

    @Override
    public void splitTransactionDoubleHandle() {
        Map<String, Object> params = new HashMap<>(16);
        params.put("gatewayId", StaticDataEnum.GATEWAY_TYPE_6.getCode());
        params.put("state", StaticDataEnum.TRANS_STATE_3.getCode());
        params.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_7.getCode());
        List<WithholdFlowDTO> withholdFlowDTOList = withholdFlowService.find(params, null, null);
        params.clear();
        if (CollectionUtil.isNotEmpty(withholdFlowDTOList)) {
            Map<String ,Object > creditResult = new HashMap<>(4);
            for (WithholdFlowDTO withholdFlowDTO : withholdFlowDTOList) {
                params.put("parent_ref", withholdFlowDTO.getSplitNo());
                JSONArray result = null;
                try {
                    result = splitTransactionSearch(params, null);
                } catch (Exception e) {
                    log.error("split transaction double handle failed, data:{}, error message:{}, e:{}", params, e.getMessage(), e);
                }
                if (CollectionUtil.isNotEmpty(result)) {
                    JSONObject orderInfo = result.getJSONObject(0);
                    transactionResult(withholdFlowDTO, creditResult, orderInfo);
                }
            }
        }
    }

    /**
     * 三方绑定账户参数校验
     * @param requestInfo
     * @param request
     * @throws Exception
     */
    private void checkAccoutnParams(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        String name = requestInfo.getString("name");
        String email = requestInfo.getString("email");
        String accountNo = requestInfo.getString("cardNo");
        String bsb = requestInfo.getString("bsb");
        if (StringUtils.isEmpty(name)) {
            throw new BizException(I18nUtils.get("split.name.null", getLang(request)));
        }
        if (StringUtils.isEmpty(email)) {
            throw new BizException(I18nUtils.get("split.email.null", getLang(request)));
        }
        if (StringUtils.isEmpty(accountNo)) {
            throw new BizException(I18nUtils.get("split.account.no.null", getLang(request)));
        }
        if (StringUtils.isEmpty(bsb)) {
            throw new BizException(I18nUtils.get("split.bsb.null", getLang(request)));
        }
    }

    /**
     * 通知分期付
     * @param map
     */
    private void transTypeToCredit(Map<String, Object> map) {
        try{
            JSONObject params =new JSONObject(map);
            serverService.transTypeToCredit(params);
        }catch (Exception e){
            log.error(" transTypeToCredit  Exception  flowId:{}, error message:{}, e:{} ",map.get("flowId"),e.getMessage(),e);
        }
    }

    /**
     * 接收三方参数，修改订单状态
     * @param withholdFlowDTO
     * @param creditResult
     * @param notifyResult
     */
    private void transactionResult(WithholdFlowDTO withholdFlowDTO, Map<String, Object> creditResult, JSONObject notifyResult) {
        creditResult.put("flowId",withholdFlowDTO.getFlowId());
        creditResult.put("splitNo", withholdFlowDTO.getSplitNo());
        creditResult.put("orderNo", withholdFlowDTO.getOrdreNo());
        creditResult.put("charge",withholdFlowDTO.getCharge());
        String status = notifyResult.getString("status");
        String failureReason = notifyResult.getString("failure_reason");
        String failureDetails = notifyResult.getString("failure_details");
        boolean failed = status.equals(StaticDataEnum.SPLIT_ORDER_STATE_REJECTED.getMessage())
                || status.equals(StaticDataEnum.SPLIT_ORDER_STATE_RETURNED.getMessage())
                || status.equals(StaticDataEnum.SPLIT_ORDER_STATE_PREFAILED.getMessage())
                || status.equals(StaticDataEnum.SPLIT_ORDER_STATE_VOIDED.getMessage());
        if (status.equals(StaticDataEnum.SPLIT_ORDER_STATE_CLEARED.getMessage())) {
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            creditResult.put("code", ErrorCodeEnum.SUCCESS_CODE.getCode());
            try {
                withholdFlowService.updateWithholdFlowForConcurrency(withholdFlowDTO.getId(), withholdFlowDTO, null);
                transTypeToCredit(creditResult);
            } catch (Exception e) {
                log.info("split withhold flow update failed, data:{}, error message:{}, e:{}", withholdFlowDTO, e.getMessage(), e);
            }
        } else if (failed) {
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            withholdFlowDTO.setReturnCode(status);
            withholdFlowDTO.setReturnMessage(failureReason + "," + failureDetails);
            creditResult.put("code", ErrorCodeEnum.FAIL_CODE.getCode());
            try {
                withholdFlowService.updateWithholdFlowForConcurrency(withholdFlowDTO.getId(), withholdFlowDTO, null);
                transTypeToCredit(creditResult);
            } catch (Exception e) {
                log.error("split withhold flow update failed, data:{}, error message:{}, e:{}", withholdFlowDTO, e.getMessage(), e);
            }
        }
    }

}
