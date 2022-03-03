package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.enumeration.StatusEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.JwtUtils;
import com.uwallet.pay.main.constant.SignErrorCode;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.exception.SignException;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.ParametersConfig;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.uwallet.pay.main.util.UploadFileUtil.getLang;
import static com.uwallet.pay.main.util.UploadFileUtil.uploadFile;

/**
 * <p>
 * 退款服务
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 退款服务
 * @author: zhoutt
 * @date: Created in 2021-08-19 10:37:47
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class RefundServiceImpl  implements RefundService {
    @Autowired
    private QrPayFlowService qrPayFlowService;
    @Autowired
    private ApiQrPayFlowService apiQrPayFlowService;
    @Autowired
    private RefundOrderService refundOrderService;
    @Autowired
    private ServerService serverService;

    @Autowired
    private  ParametersConfigService parametersConfigService;
    @Autowired
    private QrPayService qrPayService;
    @Autowired
    private ApiMerchantService apiMerchantService;
    @Autowired
    private LoginMissService loginMissService;
    @Lazy
    @Autowired
    private UserService userService;


    @Override
    public JSONObject h5Refund(H5RefundsRequestDTO requestInfo, HttpServletRequest request) throws Exception {
        JSONObject result = new JSONObject();
        // 返回url
        String requestUrl = "https"+"://" + request.getServerName()+ request.getContextPath()+ request.getServletPath()+"/";
        String authHeader = request.getHeader("Authorization");
        String idempotencyKey = request.getHeader("Idempotency-Key");

        if(StringUtils.isEmpty(idempotencyKey)){
            throw new BizException("Necessary parameters missing.");
        }

        requestInfo.setIdKey(idempotencyKey);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SignException(SignErrorCode.SIGN_ERROR.getCode(),"100403");
        }
        String tokens = authHeader.replaceFirst("Bearer ", "").trim();
        DecodedJWT verifyCode = JwtUtils.verifyCode(tokens);
        String apiMerchantId = verifyCode.getSubject();

        ApiMerchantDTO apiMerchantDTO = apiMerchantService.findApiMerchantById(Long.valueOf(apiMerchantId));

        if (apiMerchantDTO == null || apiMerchantDTO.getId() == null || 0 == apiMerchantDTO.getIsAvailable()){
            throw  new BizException( "Merchant unavailable");
        }


        // 请求参数校验
        h5RefundRequestCheck(requestInfo,request);
        BigDecimal refundAmount = BigDecimal.valueOf(requestInfo.getAmount().getAmount()).divide(new BigDecimal("100"));

        // 查询初始交易
        Map<String,Object> params = new HashMap<>();
        ApiQrPayFlowDTO apiQrPayFlowDTO = apiQrPayFlowService.findApiQrPayFlowById(Long.parseLong(requestInfo.getPayment()));
        // 交易不存在，或者商户不匹配
        if(apiQrPayFlowDTO == null || apiQrPayFlowDTO.getId() == null || !apiQrPayFlowDTO.getApiMerchantId().equals(apiMerchantDTO.getId())){
            throw  new BizException( "Transaction does not exist");
        }


        // 获取配置
        params.clear();
        ParametersConfigDTO parametersConfigDTO = parametersConfigService.findOneParametersConfig(params);

        // 查询订单是否已经存在
        params.clear();
        params.put("idempotencyKey" ,idempotencyKey);
        RefundOrderDTO checkOrder = refundOrderService.findOneRefundOrder(params);

        // 如果查询到有相同幂等值的订单，直接返回结果
        if(checkOrder != null && checkOrder.getId() != null){
            result.put("$self",requestUrl); //请求的相对url
            result.put("$type","refund");
            JSONObject charge = new JSONObject();
            charge.put("id",checkOrder.getId());
            charge.put("uri",requestUrl + "/refunds"+"/"+checkOrder.getId());
            result.put("payment",charge);

            JSONObject amount = new JSONObject();
            amount.put("amount",checkOrder.getRefundAmount().multiply(new BigDecimal("100")));
            amount.put("currency",checkOrder.getCurrency().toLowerCase());
            result.put("amount",amount);

            JSONObject merchant = new JSONObject();
            merchant.put("reference",checkOrder.getReference());
            result.put("merchant",merchant);
            result.put("status",this.getStatus(checkOrder.getState()));
            return result;
        }

        // 判断订单状态
        if(apiQrPayFlowDTO.getOrderStatus() != StaticDataEnum.H5_ORDER_TYPE_1.getCode()){
            throw  new BizException( "TXN can't refund");
        }

        params.clear();
        params.put("transNo",apiQrPayFlowDTO.getTransNo());
        QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findOneQrPayFlow(params);
        // 判断订单是否是成功的
        if(qrPayFlowDTO==null || qrPayFlowDTO.getId() == null || qrPayFlowDTO.getState()!= StaticDataEnum.TRANS_STATE_31.getCode()){
            throw  new BizException( "TXN can't refund");
        }

        // 判断是否已经退款
        if(qrPayFlowDTO.getRefundState() == StaticDataEnum.REFUND_STATE_1.getCode() ){
            throw  new BizException( "Order refund processing.");
        }else if(qrPayFlowDTO.getRefundState() != StaticDataEnum.REFUND_STATE_0.getCode() ){
            throw  new BizException( "The payment is already fully refunded.");
        }

        // 判断是否超过最大退款金额
        if( (qrPayFlowDTO.getPayAmount().add(qrPayFlowDTO.getRedEnvelopeAmount()).subtract(qrPayFlowDTO.getRefundAmount())).compareTo(refundAmount) < 0 ){
            throw  new BizException( "The refund amount is greater than the remaining total.");
        }

        // 有红包的退款必须全退
        if(qrPayFlowDTO.getRedEnvelopeAmount() .compareTo(BigDecimal.ZERO) > 0){
            if((qrPayFlowDTO.getPayAmount().add(qrPayFlowDTO.getRedEnvelopeAmount()).compareTo(refundAmount))!= 0){
                throw  new BizException( "Full refund TXN.");
            }
        }

        UserDTO userDTO = userService.findUserById(qrPayFlowDTO.getPayUserId());
        if(userDTO == null  || userDTO.getId() == null ){
            throw  new BizException( "The account is not currently active.");
        }

        // 查询用户是否被冻结
        params.clear();
        params.put("userId",qrPayFlowDTO.getPayUserId());
        LoginMissDTO loginMissDTO = loginMissService.findOneLoginMiss(params);
        if(loginMissDTO == null || loginMissDTO.getId() == null){
            throw  new BizException( "The account is not currently active.");
        }


        int date  = 90;
        if(parametersConfigDTO != null && parametersConfigDTO.getRefundsLimitDate() != null){
            date = parametersConfigDTO.getRefundsLimitDate();
        }
        // 判断超过90天的交易不能退款
        Long now = System.currentTimeMillis();
        if( qrPayFlowDTO.getCreatedDate() == null || (now - qrPayFlowDTO.getCreatedDate() < date * 24 * 60 * 60 * 1000)){
            throw  new BizException( "TXN can't refund");
        }


        // 记录退款订单，原订单修改退款状态
        RefundOrderDTO refundOrderDTO = refundOrderService.createH5RefundOrderDTO(requestInfo,apiQrPayFlowDTO,qrPayFlowDTO,request);

        if(refundOrderDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_36.getCode()){
            // 分期付订单退款
            refundOrderDTO = this.creditRefund(requestInfo,refundOrderDTO,qrPayFlowDTO,request);
        }else{
            // 不支持的退款类型，直接失败
            refundOrderDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            refundOrderService.updateRefundOrder(refundOrderDTO.getId(),refundOrderDTO,request);
        }

        if(refundOrderDTO.getState() == StaticDataEnum.TRANS_STATE_1.getCode()){
            //更新流水表状态
            params.clear();
            if( qrPayFlowDTO.getPayAmount().add(qrPayFlowDTO.getRedEnvelopeAmount()).subtract(qrPayFlowDTO.getRefundAmount()).compareTo(refundAmount) == 0 ){
                params.put("refundState",StaticDataEnum.REFUND_STATE_3.getCode());
            }else{
                params.put("refundState",StaticDataEnum.REFUND_STATE_2.getCode());
            }
            params.put("state",StaticDataEnum.TRANS_STATE_104.getCode());
            params.put("modifiedDate",now);
            params.put("refundAmount",refundAmount);
            params.put("id",qrPayFlowDTO.getId());
            qrPayFlowService.updateRefundData(params,request);

        }else if(refundOrderDTO.getState() == StaticDataEnum.TRANS_STATE_2.getCode()){
            //更新流水表状态
            params.clear();
            params.put("refundState",StaticDataEnum.REFUND_STATE_0.getCode());
            params.put("modifiedDate",now);
            params.put("id",qrPayFlowDTO.getId());
            qrPayFlowService.updateRefundData(params,request);
//            throw new BizException(I18nUtils.get("refund.fail", getLang(request)));
        }


        result.put("$self",requestUrl); //请求的相对url
        result.put("$type","refund");
        JSONObject charge = new JSONObject();
        charge.put("id",refundOrderDTO.getId());
        charge.put("uri",requestUrl +refundOrderDTO.getId());
        result.put("payment",charge);

        JSONObject amount = new JSONObject();
        amount.put("amount",requestInfo.getAmount().getAmount());
        amount.put("currency",requestInfo.getAmount().getCurrency());
        result.put("amount",amount);

        JSONObject merchant = new JSONObject();
        merchant.put("reference",requestInfo.getMerchant().getReference());
        result.put("merchant",merchant);
        result.put("status",this.getStatus(refundOrderDTO.getState()));
        result.put("reason",refundOrderDTO.getReason());
        result.put("id",refundOrderDTO.getId());
        return result;
    }

    private String getStatus(Integer state) {

        if(state == StaticDataEnum.TRANS_STATE_1.getCode()){
            return  "complete";
        }else if(state == StaticDataEnum.TRANS_STATE_2.getCode()){
            return "failed";
        }else{
            return "pending";
        }
    }

    @Override
    public void creditRefundDoubtHandle() {
        log.info("---- 分期付退款查证 ----");
        // 分期付退款查证
        List<RefundOrderDTO> refundOrderDTOS = refundOrderService.findCreditRefundDoubt();
        if(refundOrderDTOS == null || refundOrderDTOS.size() == 0){
            return;
        }

        for (RefundOrderDTO refundOrderDTO: refundOrderDTOS){
            try {
                JSONObject result = new JSONObject();
                try {
                    result = serverService.creditRefundDoubt(refundOrderDTO.getId());
                }catch (Exception e){
                    log.info("分期付退款查证,流水号：" + refundOrderDTO.getId() +
                            "异常:"+e.getMessage() ,e);
                    continue;
                }
                if(result == null || result.getString("code")== null || result.getJSONObject("data") == null){
                    continue;
                }
                String code  = result.getString("code");
                if(ErrorCodeEnum.SUCCESS_CODE.getCode().equals(code)){

                    Integer state = result.getJSONObject("data").getInteger("state");
                    Map<String,Object> params = new HashMap<>();
                    if(state == null){
                        continue;
                    }else if(state == StaticDataEnum.TRANS_STATE_1.getCode()){
                        // 成功
                        refundOrderDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                        refundOrderService.updateRefundOrder(refundOrderDTO.getId(),refundOrderDTO,null);

                        params.put("transNo",refundOrderDTO.getTransNo());
                        QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findOneQrPayFlow(params);

                        params.clear();
                        if( qrPayFlowDTO.getPayAmount().add(qrPayFlowDTO.getRedEnvelopeAmount()).subtract(qrPayFlowDTO.getRefundAmount()).compareTo(refundOrderDTO.getRefundAmount()) == 0 ){
                            params.put("refundState",StaticDataEnum.REFUND_STATE_3.getCode());
                        }else{
                            params.put("refundState",StaticDataEnum.REFUND_STATE_2.getCode());
                        }

                        params.put("state",StaticDataEnum.TRANS_STATE_104.getCode());
                        params.put("modifiedDate",System.currentTimeMillis());
                        params.put("refundAmount",refundOrderDTO.getRefundAmount());
                        params.put("id",qrPayFlowDTO.getId());
                        qrPayFlowService.updateRefundData(params,null);

//                        if(qrPayFlowDTO != null && qrPayFlowDTO.getId() != null) {
//                            if(refundOrderDTO.getRefundAmount() .compareTo(qrPayFlowDTO.getPayAmount()) == 0 && qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0){
//                                // 全额退款并且使用了红包,需要退卡券和红包
//                                try {
//                                    if(qrPayFlowDTO.getMarketingId() == null){
//                                        qrPayService.doBatchAmountOutRollBack(qrPayFlowDTO,null);
//                                    }else{
//                                        qrPayService.oneMarketingRollback(qrPayFlowDTO,null);
//                                    }
//                                }catch (Exception e){
//                                    log.error("分期付退款查证，卡券红包回退异常，message："+e.getMessage(),e);
//                                }
//                            }
//                        }
                    }else if(state == StaticDataEnum.TRANS_STATE_2.getCode()){
                        refundOrderDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                        refundOrderService.updateRefundOrder(refundOrderDTO.getId(),refundOrderDTO,null);
                        params.put("transNo",refundOrderDTO.getTransNo());
                        QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findOneQrPayFlow(params);
                        if(qrPayFlowDTO != null && qrPayFlowDTO.getId() != null) {
                            qrPayFlowDTO.setRefundState(StaticDataEnum.REFUND_STATE_0.getCode());
                            qrPayFlowDTO.setRefundAmount( BigDecimal.ZERO);
                            qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,null);
                        }
                    }

                }

            }catch (Exception e){
                log.info("分期付退款查证,流水号：" + refundOrderDTO.getId() +
                        "异常:"+e.getMessage() ,e);
            }

        }

    }

    @Override
    public JSONObject getPayments(String id, HttpServletRequest request) throws Exception{
        String requestUrl = "https"+"://" + request.getServerName()+ request.getContextPath()+ request.getServletPath();
        Map<String,Object > params = new HashMap<>(8);
        params.put("id",id);
//        params.put("state",StaticDataEnum.TRANS_STATE_1.getCode());
        RefundOrderDTO refundOrderDTO = refundOrderService.findOneRefundOrder(params);

        if(refundOrderDTO == null || refundOrderDTO.getId() == null){
            throw new BizException("Payment Not Found");
        }else{
            params.clear();
            params.put("transNo",refundOrderDTO.getTransNo());
            QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findOneQrPayFlow(params);
            JSONObject result  = new JSONObject();

            result.put("$self",requestUrl); //请求的相对url
            result.put("$type","refund");
            result.put("id",id);
            result.put("reason",refundOrderDTO.getReason());

            JSONObject charge = new JSONObject();
            charge.put("id", qrPayFlowDTO.getId());
            charge.put("uri",requestUrl );
            result.put("payment",charge);

            JSONObject amount = new JSONObject();
            amount.put("amount",refundOrderDTO.getRefundAmount().multiply(new BigDecimal("100")));
            amount.put("currency",refundOrderDTO.getCurrency().toLowerCase());
            result.put("amount",amount);

            JSONObject merchant = new JSONObject();
            merchant.put("reference",refundOrderDTO.getReference());
            result.put("merchant",merchant);
            result.put("status",this.getStatus(refundOrderDTO.getState()));

            return result;
        }

    }

    @Override
    public int getH5MerchantRefundCount(Map<String, Object> params) {

        Map<String,Object> params1 = new HashMap<>();
        params1.put("merchantIdList",params.get("merchantIdList"));
        params1.put("start",params.get("start"));
        return refundOrderService.getH5MerchantRefundUnclearedCount(params1);
    }

    @Override
    public void addH5ClearBatchId(Map<String, Object> params, ClearBatchDTO clearBatchDTO, HttpServletRequest request) {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("orgClearState",  StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        updateMap.put("clearState", StaticDataEnum.CLEAR_STATE_TYPE_2.getCode());
        updateMap.put("state", StaticDataEnum.TRANS_STATE_1.getCode());
        updateMap.put("start", params.get("start"));
        updateMap.put("end", params.get("end"));
        updateMap.put("batchId",clearBatchDTO.getId());
        updateMap.put("orderSource",StaticDataEnum.ORDER_SOURCE_1.getCode());

        // 新版本清算交易
        updateMap.put("merchantIdList",params.get("merchantIdList"));
        refundOrderService.addClearBatchId(updateMap,request);
    }

    @Override
    public void clearData(Map<String, Object> map) {
        Map<String,Object>  updateData = new HashMap<>();
        updateData.put("settlementState",map.get("clearState"));
        updateData.put("settlementTime",map.get("clearTime"));
        updateData.put("modifiedDate",map.get("modifiedDate"));
        updateData.put("merchantId",map.get("merchantId"));
        updateData.put("batchId",map.get("orgBatchId"));
        refundOrderService.clearData(updateData);
    }

    @Override
    public List<RefundOrderDTO> merchantClearMessageList(Map<String, Object> params) {
        return refundOrderService.merchantClearMessageList(params);
    }

    @Override
    public int rollbackSettlement(Long clearBatchId, Long merchantId, HttpServletRequest request) {
        return refundOrderService.rollbackSettlement(clearBatchId,merchantId,request);
    }


    private RefundOrderDTO creditRefund(H5RefundsRequestDTO requestInfo, RefundOrderDTO refundOrderDTO, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException {
        JSONObject requestData = new JSONObject();
        requestData.put("creditOrderNo",qrPayFlowDTO.getCreditOrderNo());
        requestData.put("reason",refundOrderDTO.getReason());
        requestData.put("orderNo",refundOrderDTO.getId());
        // 带红包的订单全额退，分期付要退款的金额为付款金额
        if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0){
            requestData.put("amount",qrPayFlowDTO.getPayAmount());
        }else{
            requestData.put("amount",BigDecimal.valueOf(requestInfo.getAmount().getAmount()).divide(new BigDecimal("100")));
        }
        JSONObject result = null;
        try {
            result = serverService.creditRefund(requestData,request);
        }catch (Exception e){
            // 直接可疑处理
            log.error("creditRefund Exception:"+e.getMessage(),e);
            return  refundOrderDTO;
        }

        if(result == null){
            refundOrderDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            refundOrderService.updateRefundOrder(refundOrderDTO.getId(),refundOrderDTO,request);
            return  refundOrderDTO;
        }
        String code = result.getString("code");
        if(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode().equals(code)){
            // 失败
            refundOrderDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            refundOrderDTO.setReturnCode(code);
            refundOrderDTO.setReturnMessage(result.getString("message"));
        }else if(ErrorCodeEnum.SUCCESS_CODE.getCode().equals(code)){
            int state = result.getJSONObject("data").getInteger("state");
            if(state == StaticDataEnum.TRANS_STATE_1.getCode()){
                // 成功
                refundOrderDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                refundOrderDTO.setReturnCode(code);
            }else if(state == StaticDataEnum.TRANS_STATE_2.getCode()){
                refundOrderDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                refundOrderDTO.setReturnCode(code);
//                refundOrderDTO.setReturnMessage(result.getString("message"));
            }else{
                refundOrderDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            }

        }else{
            // 可疑
            refundOrderDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
        }
        refundOrderService.updateRefundOrder(refundOrderDTO.getId(),refundOrderDTO,request);
        return  refundOrderDTO;
    }

    private void h5RefundRequestCheck(H5RefundsRequestDTO requestInfo, HttpServletRequest request) throws Exception{
        if(StringUtils.isEmpty(requestInfo.getPayment())){
            throw new BizException("Necessary parameters missing.");
        }
        if(StringUtils.isEmpty(requestInfo.getReason())){
            throw new BizException("Necessary parameters missing.");
        }
        if(requestInfo.getAmount() == null){
            throw new BizException("Necessary parameters missing.");
        }
        if(requestInfo.getMerchant() == null){
            throw new BizException("Necessary parameters missing.");
        }
        if(requestInfo.getAmount().getAmount() == null || requestInfo.getAmount().getAmount().equals(0L)){
            throw new BizException("Necessary parameters missing.");
        }
        if(StringUtils.isEmpty(requestInfo.getAmount().getCurrency())){
            throw new BizException("Necessary parameters missing.");
        }
        // 当前只支持澳币
        if(!"AUD".equals(requestInfo.getAmount().getCurrency().toUpperCase())){
            throw new BizException("Currency Error.");
        }
        if(StringUtils.isEmpty(requestInfo.getMerchant().getReference())){
            throw new BizException("Necessary parameters missing.");
        }
    }
}
