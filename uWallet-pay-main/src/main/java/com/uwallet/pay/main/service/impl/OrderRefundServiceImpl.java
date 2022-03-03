package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.MathUtils;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.QrPayFlowDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.MailUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author baixinyue
 * @description 退款交易业务层
 * @createDate 2020/02/07
 */

@Service
@Slf4j
public class OrderRefundServiceImpl implements OrderRefundService {

    @Autowired
    OmiPayService omiPayService;

    @Autowired
    private QrPayFlowService qrPayFlowService;

    @Autowired
    private WithholdFlowService withholdFlowService;

    @Autowired
    private RefundFlowService refundFlowService;

    @Autowired
    private AccountFlowService accountFlowService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private UserService userService;

    @Autowired
    private MailTemplateService mailTemplateService;

    @Resource
    private QrPayFlowDAO qrPayFlowDAO;

    @Autowired
    private SecondMerchantGatewayInfoService secondMerchantGatewayInfoService;

    @Autowired
    private StripeAPIService stripeAPIService;

    @Autowired
    LatPayService latPayService;

    @Value("${uWallet.sysEmail}")
    private String sysEmail;

    @Value("${uWallet.sysEmailPwd}")
    private String sysEmailPwd;

    @Value("${latpay.merchantRefundId}")
    private String merchantRefundId;

    @Value("${latpay.merchantRefundPassword}")
    private String merchantRefundPassword;

    @Autowired
    private NoticeService noticeService;

    /**
     * 卡退款订单创建天数
     */
    private static final int DAYS_LIMIT_60 = 60;

    /**
     * 卡退款订单创建天数
     */
    private static final int DAYS_LIMIT_90 = 90;

    /**
     * 卡退款订单限制24小时
     */
    private static final int HOURS_LIMIT_24 = 24;

    @Autowired
    private UserActionService userActionService;

    @Override
    public JSONObject cardTotalRefundApply(OrderRefundDTO orderRefundDTO, HttpServletRequest request) throws Exception {
        JSONObject result = null;
        log.info("lat pay refund data:{}", orderRefundDTO);
        Map<String, Object> params = new HashMap<>(1);
        QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(orderRefundDTO.getFlowId());
        // 判断订单是否是成功的
        if(qrPayFlowDTO==null || qrPayFlowDTO.getId() == null || qrPayFlowDTO.getState()!=StaticDataEnum.TRANS_STATE_31.getCode()){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        // 判断是否进行过退款操作
        params.put("flowId", qrPayFlowDTO.getId());
        List<RefundFlowDTO> refundFlowDTOList = refundFlowService.find(params, null, null);
        if (refundFlowDTOList != null) {
            List<RefundFlowDTO> filterResult = refundFlowDTOList
                    .stream()
                    .filter(refundFlowDTO -> refundFlowDTO.getState().equals(StaticDataEnum.TRANS_STATE_1.getCode()) || refundFlowDTO.getState().equals(StaticDataEnum.TRANS_STATE_3.getCode()))
                    .collect(Collectors.toList());
            if (filterResult != null && !filterResult.isEmpty()) {
                throw new BizException(I18nUtils.get("has.been.refund", getLang(request)));
            }
        }
        // 判断交易是否在90天以内，90天以外则不能退款
        Long now = System.currentTimeMillis();
        Long transactionDay = qrPayFlowDTO.getCreatedDate();
        long days = ((now - transactionDay) / (1000*3600*24));
        long hours = ((now - transactionDay) / (1000*3600));

        boolean overDays = hours > HOURS_LIMIT_24;
        if (overDays) {
            throw new BizException(I18nUtils.get("latpay.refund.Inadequate.conditions", getLang(request)));
        }
        // 若交易超过24小时，则订单撤销失败
        if (overDays && orderRefundDTO.getLatPayRefundType().equals(StaticDataEnum.STATUS_0.getCode())) {
            // 订单撤销失败
            result = new JSONObject();
            result.put("refundState", StaticDataEnum.TRANS_STATE_6.getCode());
            result.put("refundMessage", I18nUtils.get("latpay.refund.order.cancel.fail", getLang(request)));
        }
        int refundType = orderRefundDTO.getLatPayRefundType().equals(StaticDataEnum.STATUS_0.getCode()) ? StaticDataEnum.LAT_PAY_REFUND_TYPE_3.getCode() : StaticDataEnum.LAT_PAY_REFUND_TYPE_1.getCode();
        // 查询三方交易流水
        params.clear();
        params.put("flowId", qrPayFlowDTO.getId());
        WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(params);
        //记录退款流水
        RefundFlowDTO refundFlowDTO = createRefundFlowDTO(orderRefundDTO, qrPayFlowDTO, refundType, request);
        //latPay退款
        JSONObject refundRequestInfo = new JSONObject();
        refundRequestInfo.put("merchant_refund_id", merchantRefundId);
        refundRequestInfo.put("merchant_pwd", merchantRefundPassword);
        refundRequestInfo.put("lps_transaction_id", withholdFlowDTO.getLpsTransactionId());
        refundRequestInfo.put("merchant_ref_number", withholdFlowDTO.getOrdreNo());
        refundRequestInfo.put("refund_amount", withholdFlowDTO.getTransAmount().toString());
        refundRequestInfo.put("refund_type", new Integer(refundType).toString());
        refundRequestInfo.put("refund_comment", orderRefundDTO.getReason());
        if (days < 60) {
            refundRequestInfo.put("realtime", "Y");
        } else if (days > 60 && days < 90) {
            refundRequestInfo.put("realtime", "Y");
        }
        // 调用三方，请求退款
        JSONObject returnData = null;
        try {
            returnData = latPayService.latPayRefundRequest(refundRequestInfo, request);
        } catch (Exception e) {
            log.info("lat pay refund double, data:{}, error message:{}, e:{}", returnData, e.getMessage(), e);
            refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
            throw new BizException(I18nUtils.get("refund.processing", getLang(request)));
        }
        // 若取消交易失败，则进行全额退款
        if (refundType ==  StaticDataEnum.LAT_PAY_REFUND_TYPE_3.getCode()) {
            if (returnData.getInteger("RefundSubmit_status").equals(StaticDataEnum.LP_RESPONSE_TYPE_4008.getCode())) {
                // 订单撤销失败
                result = new JSONObject();
                result.put("refundState", StaticDataEnum.TRANS_STATE_6.getCode());
                result.put("refundMessage", I18nUtils.get("latpay.refund.order.cancel.fail", getLang(request)));
            } else {
                // 交易结果处理
                result = latPayRefundRequestResultHandle(returnData, refundFlowDTO, qrPayFlowDTO, refundType, request);
            }
        } else {
            result = latPayRefundRequestResultHandle(returnData, refundFlowDTO, qrPayFlowDTO, refundType, request);
        }
        return result;
    }

    @Override
    public void refundApply(OrderRefundDTO orderRefundDTO, HttpServletRequest request) throws Exception {
        log.info("omi pay refund, data:{}", orderRefundDTO);
        QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(orderRefundDTO.getFlowId());
        //判断订单是否是成功的
        if(qrPayFlowDTO==null || qrPayFlowDTO.getId() == null || qrPayFlowDTO.getState()!=StaticDataEnum.TRANS_STATE_31.getCode()){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //latpay卡退款必须是已清算且清算日期到退款时间在九十天内
        //LatPay不做退款交易
        if (StaticDataEnum.GATEWAY_TYPE_0.getCode() == qrPayFlowDTO.getGatewayId().intValue()) {
//            if (((int)((System.currentTimeMillis() - qrPayFlowDTO.getClearTime())/(1000*3600*24)) > 90)) {
//                throw new BizException(I18nUtils.get("latpay.refund.Inadequate.conditions", getLang(request)));
//            }

            //TODO Latpay 暂时不开放退款
            throw new BizException(I18nUtils.get("latpay.refund.Inadequate.conditions", getLang(request)));
        }else{
            //判断退款金额是否充足
            if (!checkBalance(qrPayFlowDTO, orderRefundDTO.getAmount(), request)) {
                throw new BizException(I18nUtils.get("limit.over", getLang(request)));
            }
        }
        //记录退款流水
        RefundFlowDTO refundFlowDTO = createRefundFlowDTO(orderRefundDTO, qrPayFlowDTO, null, request);

        //根据交易渠道调用相应退款接口
        if (StaticDataEnum.GATEWAY_TYPE_1.getCode() == qrPayFlowDTO.getGatewayId().intValue()
                || StaticDataEnum.GATEWAY_TYPE_2.getCode() == qrPayFlowDTO.getGatewayId().intValue()) {
            //支付渠道为支付宝、微信
            //调用账户系统进行账户出账操作
            JSONObject amountOut = createAccountFlowDTO(refundFlowDTO, qrPayFlowDTO, StaticDataEnum.AMOUNT_OUT.getCode(), request);
            //根据渠道判断出账交易类型
            Integer transType = null;
            if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_12.getCode()) {
                transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_12.getCode();
            }
            if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_13.getCode()) {
                transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_13.getCode();
            }
            if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_14.getCode()) {
                transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_14.getCode();
            }
            //账户出账
            try {
                // TODO 返回参数修改，如果以后要用需要重新测试修改
                serverService.amountOut(amountOut);
                updateAccountFlowDTO(Long.valueOf(refundFlowDTO.getOrdreNo()), StaticDataEnum.TRANS_STATE_1.getCode(), transType, request);
            } catch (Exception e) {
                refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_40.getCode());
                refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
                updateAccountFlowDTO(Long.valueOf(refundFlowDTO.getOrdreNo()), StaticDataEnum.TRANS_STATE_3.getCode(), transType, request);
                throw new BizException(I18nUtils.get("refund.fail", getLang(request)));
            }


            Map<String, Object> params = new HashMap<>(1);
            params.put("flowId", orderRefundDTO.getFlowId());
            WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(params);
            //拼装请求数据
            JSONObject refundInfo = new JSONObject();
            refundInfo.put("order_no", withholdFlowDTO.getOmiPayOrderNo());
            refundInfo.put("out_refund_no", refundFlowDTO.getOrdreNo());
            refundInfo.put("amount", orderRefundDTO.getAmount());
            log.info("omi pay refund apply, data:{}", refundInfo);
            JSONObject data = omiPayService.refundApply(refundInfo);
            log.info("omi pay refund result, data:{}", data);
            //omipay受理成功则保存返回的omipay退款号，失败则退款失败
            if (data.getString("return_code").equals(StaticDataEnum.OMI_PAY_SUCCESS.getMessage())) {
                refundFlowDTO.setOmiPayRefundOrderNo(data.getString("refund_no"));
                refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
                sendMessage(refundFlowDTO.getOrgRecUserId(), new Integer(StaticDataEnum.SEND_NODE_14.getCode()));
            } else {
                refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
                //退款失败，账户系统出账回滚
                JSONObject amountIn = createAccountFlowDTO(refundFlowDTO, qrPayFlowDTO, StaticDataEnum.AMOUNT_OUT_ROLL_BACK.getCode(), null);
                //判断回滚交易类型
                if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_12.getCode()) {
                    transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_15.getCode();
                }
                if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_13.getCode()) {
                    transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_16.getCode();
                }
                if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_14.getCode()) {
                    transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_17.getCode();
                }
                try {
                    serverService.amountIn(amountIn);
                    //交易金额回滚
                  //  transAmountRollback(refundFlowDTO.getFlowId(), refundFlowDTO.getRefundAmount(), request);
                    //更新回滚流水
                    updateAccountFlowDTO(amountIn.getLong("channelSerialnumber"), StaticDataEnum.TRANS_STATE_1.getCode(), transType, null);
                } catch (Exception e) {
                    refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_43.getCode());
                    refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
                    //更新回滚流水
                    updateAccountFlowDTO(amountIn.getLong("channelSerialnumber"), StaticDataEnum.TRANS_STATE_3.getCode(), transType, null);
                }
                throw new BizException(I18nUtils.get("refund.fail", getLang(request)));
            }
        } else {
            Map<String, Object> params = new HashMap<>(1);
            params.put("merchantId", qrPayFlowDTO.getMerchantId());
            params.put("gatewayId", qrPayFlowDTO.getGatewayId());
            SecondMerchantGatewayInfoDTO secondMerchantGatewayInfoDTO = secondMerchantGatewayInfoService.findOneSecondMerchantGatewayInfo(params);
            params.clear();
            params.put("flowId", qrPayFlowDTO.getId());
            WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(params);
            //latPay退款
            JSONObject refundInfo = new JSONObject();
            refundInfo.put("merchant_refund_id", merchantRefundId);
            refundInfo.put("merchant_pwd", merchantRefundPassword);
            refundInfo.put("lps_transaction_id", withholdFlowDTO.getLpsTransactionId());
            refundInfo.put("merchant_ref_number", secondMerchantGatewayInfoDTO.getGatewayMerchantId());
            refundInfo.put("refund_type", new Integer(1));
            refundInfo.put("refund_amount", orderRefundDTO.getAmount());
            refundInfo.put("refund_comment", orderRefundDTO.getReason());
            if (((System.currentTimeMillis() - qrPayFlowDTO.getCreatedDate())/(1000*3600)) <= 1) {
                refundInfo.put("realtime", "y");
            }
            JSONObject returnData = latPayService.latPayRefundRequest(refundInfo, request);
            // 交易结果处理
//            latPayRefundRequestResultHandle(returnData, refundFlowDTO, qrPayFlowDTO, request);
//            if (returnData.getInteger("RefundSubmit_status").equals(StaticDataEnum.LP_RESPONSE_TYPE_0.getCode())) {
//                refundFlowDTO.setLpsRefundId(returnData.getString("LPS_refund_id"));
//                refundFlowDTO.setRequestId(returnData.getString("Request_Id"));
//                refundFlowDTO.setReplyPwd(returnData.getString("LPSReply_pwd"));
//                //银行状态00为成功，90为可疑，其他为退款失败
//                if (returnData.getString("Bank_status").equals(StaticDataEnum.LAT_PAY_BANK_STATUS_0.getMessage())) {
//                    refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
//                    //退款成功，计算订单中应退回的手续费，补充到收款方
//                    refundFlowDTO.setMakeUpFee(gatewayFeeAdd(qrPayFlowDTO, refundFlowDTO, request));
//                    refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
//                    sendMessage(refundFlowDTO.getOrgRecUserId(), new Integer(StaticDataEnum.SEND_NODE_14.getCode()));
//                } else if (returnData.getString("Bank_status").equals(StaticDataEnum.LAT_PAY_BANK_STATUS_9.getMessage())
//                ||returnData.getString("Bank_status").equals(StaticDataEnum.LAT_PAY_BANK_STATUS_5.getMessage())
//                ) {
//                    //05,90失败
//                    refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
//                    refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
//                    throw new BizException(I18nUtils.get("refund.fail", getLang(request)));
//
//                } else {
//                    refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
//
//                }
//            } else {
//                refundFlowDTO.setLpsRefundId(returnData.getString("LPS_refund_id"));
//                refundFlowDTO.setRequestId(returnData.getString("Request_Id"));
//                refundFlowDTO.setReplyPwd(returnData.getString("LPSReply_pwd"));
//                refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
//                refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
//                throw new BizException(I18nUtils.get("refund.fail", getLang(request)));
//            }
        }

    }


    @Override
    public void refundStatusCheck() throws Exception {
        //查询状态为退款交易中和退款交易可疑的订单
        List<RefundFlowDTO> list = refundFlowService.selectFlowDTO();
        if(list == null || list.size() == 0){
            return;
        }
        for (RefundFlowDTO refundFlowDTO : list) {
            try {
                //根据渠道调用退款状态查询接口
                if (StaticDataEnum.GATEWAY_TYPE_1.getCode() == refundFlowDTO.getGatewayId().intValue()
                        || StaticDataEnum.GATEWAY_TYPE_2.getCode() == refundFlowDTO.getGatewayId().intValue()) {
//                //查询账户出账流水
//                Map<String, Object> params = new HashMap<>(1);
//                params.put("orderNo", refundFlowDTO.getOrdreNo());
//                params.put("transType", refundFlowDTO.getTransType());
//                AccountFlowDTO accountFlowDTO = accountFlowService.findOneAccountFlow(params);
//                try {
//                    //查询退款交易状态
//                    Integer status = omiPayService.refundStatusCheck(refundFlowDTO.getOmiPayRefundOrderNo());
//                    //根据三方状态改变流水结果
//                    if (status.intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
//                        refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
//                        //退款成功，计算订单中应退回的手续费，补充到收款方
//                        refundFlowDTO.setMakeUpFee(gatewayFeeAdd(qrPayFlowDTO, refundFlowDTO, null));
//                        refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);
//                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
//                        accountFlowService.updateAccountFlow(accountFlowDTO.getId(), accountFlowDTO, null);
//                        sendMessage(refundFlowDTO.getOrgRecUserId(), new Integer(StaticDataEnum.SEND_NODE_14.getCode()));
//                    } else if (status.intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
//                        //修改交易状态为回退中
//                        refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_40.getCode());
//                        refundFlowService.updateRefundFlow(refundFlowDTO.getId(),refundFlowDTO,null);
//                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
//                        accountFlowService.updateAccountFlow(accountFlowDTO.getId(), accountFlowDTO, null);
//                        //退款失败，账户系统出账回滚
//                        JSONObject amountIn = createAccountFlowDTO(refundFlowDTO, qrPayFlowDTO, StaticDataEnum.AMOUNT_OUT_ROLL_BACK.getCode(), null);
//                        //判断退款回滚交易类型
//                        Integer transType = null;
//                        if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_12.getCode()) {
//                            transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_15.getCode();
//                        }
//                        if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_13.getCode()) {
//                            transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_16.getCode();
//                        }
//                        if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_14.getCode()) {
//                            transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_17.getCode();
//                        }
//                        try {
//                            serverService.amountIn(amountIn);
//                            //更新回滚流水
//                            updateAccountFlowDTO(amountIn.getLong("channelSerialnumber"), StaticDataEnum.TRANS_STATE_1.getCode(), transType, null);
//                        } catch (Exception e) {
//                            //更新回滚流水
//                            updateAccountFlowDTO(amountIn.getLong("channelSerialnumber"), StaticDataEnum.TRANS_STATE_3.getCode(), transType, null);
//                            continue;
//                        }
//                        //修改退款交易状态
//                        //查证失败可能退款金额已经清算，此情况将清算状态改未回退
//                        refundFlowService.updateRefundFlowToCheckFail(refundFlowDTO.getId(), System.currentTimeMillis());
//
//                        //交易金额回滚
//                  //      transAmountRollback(refundFlowDTO.getFlowId(), refundFlowDTO.getRefundAmount(), null);
//                        //发送退款失败消息
////                        sendMessage(refundFlowDTO.getOrgRecUserId(), new Integer(StaticDataEnum.SEND_NODE_17.getCode()));
//                    } else {
//                        refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
//                        refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);
//                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_33.getCode());
//                        accountFlowService.updateAccountFlow(accountFlowDTO.getId(), accountFlowDTO, null);
//                    }
//                } catch (Exception e) {
//                    refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
//                    refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);
//                    continue;
//                }
                } else if(StaticDataEnum.GATEWAY_TYPE_0.getCode() == refundFlowDTO.getGatewayId().intValue()) {


                    //latpay卡支付退款状态查询
                    JSONObject requestInfo = new JSONObject();
                    requestInfo.put("merchant_refund_id", merchantRefundId);
                    requestInfo.put("merchant_pwd", merchantRefundPassword);
                    requestInfo.put("merchant_ref_number",refundFlowDTO.getOrgThirdNo());
                    requestInfo.put("refund_id",refundFlowDTO.getLpsRefundId());
                    requestInfo.put("refund_currency",refundFlowDTO.getCurrency());
                    requestInfo.put("refund_amount",refundFlowDTO.getRefundAmount().toString());

                    //请求
                    JSONObject returnData = latPayService.latPayRefundCheck(requestInfo);

                    // 如果没有返回，等待查询
                    if(returnData == null ){
                        return;
                    }
                    // RefundStatusCheck_status = 0 查证请求成功
                    if(StringUtils.isEmpty(returnData.getString("RefundStatusCheck_status"))){
                        // 无请求结果
                        return;
                    }else if(returnData.getString("RefundStatusCheck_status").equals(StaticDataEnum.LAT_PAY_REFUND_REQUEST_STATUS_4003.getMessage())){
                        // 无单号
                        refundFlowDTO.setReturnMessage(returnData.getString("RefundStatusCheck_status"));
                        refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                        refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);
                        return;
                    }else if(!returnData.getString("RefundStatusCheck_status").equals(StaticDataEnum.LAT_PAY_REFUND_REQUEST_STATUS_0.getMessage())){
                        // 其他请求失败
                        return;
                    }


                    // RefundSubmit_status Latpay退款请求成功
                    if(returnData.getString("RefundSubmit_status").equals(StaticDataEnum.LP_RESPONSE_TYPE_0.getMessage())){
                        //银行状态00为成功，05失败，其他可疑
                        refundFlowDTO.setReturnMessage(returnData.getString("Bank_status"));
                        if (returnData.getString("Bank_status").equals(StaticDataEnum.LAT_PAY_BANK_STATUS_0.getMessage())) {
                            refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                            //退款成功，计算订单中应退回的手续费，补充到收款方
                            refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);
                            // 发送退款成功消息
//                        sendMessage(refundFlowDTO.getOrgRecUserId(), new Integer(StaticDataEnum.SEND_NODE_14.getCode()));
                        } else if (returnData.getString("Bank_status").equals(StaticDataEnum.LAT_PAY_BANK_STATUS_5.getMessage())) {

                            //查证失败可能退款金额已经清算，此情况将清算状态改未回退
//                        refundFlowService.updateRefundFlowToCheckFail(refundFlowDTO.getId(), System.currentTimeMillis());
                            // 银行方未通过失败
                            refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                            refundFlowDTO.setReturnMessage(returnData.getString("Bank_status"));
                            refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);

                        } else {
                            refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                            refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);
                        }
                    }else{
                        // Latpay未通过失败
                        refundFlowDTO.setReturnMessage(returnData.getString("RefundSubmit_status"));
                        refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                        refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);
                    }

                }else if(StaticDataEnum.GATEWAY_TYPE_8.getCode() == refundFlowDTO.getGatewayId().intValue()){
                        // todo stripe查证
                    String stripeRefundNo = refundFlowDTO.getStripeRefundNo();
                    Long id = refundFlowDTO.getId();
                    JSONObject param=new JSONObject();
                    param.put("id",id);
                    stripeAPIService.stripeRefundCheck(param);
                }


            }catch (Exception e){
                log.error("refund status check exception ,id : "+refundFlowDTO.getId() + e.getMessage(),e);
                continue;
            }

        }
    }

    @Override
    public void refundFailedAccountRollbackFailedHandle() throws Exception {
        //查询回滚失败状态流水
        Map<String, Object> params = new HashMap<>(1);
        params.put("state", StaticDataEnum.TRANS_STATE_42.getCode());
        List<RefundFlowDTO> list = refundFlowService.find(params, null, null);
        for (RefundFlowDTO refundFlowDTO : list) {
            QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(refundFlowDTO.getFlowId());
            //判断退款回滚交易类型
            Integer transType = null;
            if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_12.getCode()) {
                transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_15.getCode();
            }
            if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_13.getCode()) {
                transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_16.getCode();
            }
            if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_14.getCode()) {
                transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_17.getCode();
            }
            JSONObject amountIn = createAccountFlowDTO(refundFlowDTO, qrPayFlowDTO, StaticDataEnum.AMOUNT_OUT_ROLL_BACK.getCode(), null);
            try {
                serverService.amountIn(amountIn);
                refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);
                //交易金额回滚
         //       transAmountRollback(refundFlowDTO.getFlowId(), refundFlowDTO.getRefundAmount(), null);
                //更新回滚流水
                updateAccountFlowDTO(amountIn.getLong("channelSerialnumber"), StaticDataEnum.TRANS_STATE_1.getCode(), transType, null);
            } catch (Exception e) {
                refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);
                //更新回滚流水
                updateAccountFlowDTO(amountIn.getLong("channelSerialnumber"), StaticDataEnum.TRANS_STATE_3.getCode(), transType, null);
            }
        }
    }

    @Override
    public void refundFailedAccountRollbackDoubtHandle() throws Exception {
        List<AccountFlowDTO> list = accountFlowService.selectAccountRollbackDoubtFlow();
        for (AccountFlowDTO accountFlowDTO : list) {
            Map<String, Object> params = new HashMap<>(1);
            params.put("ordreNo", accountFlowDTO.getOrderNo().toString());
            RefundFlowDTO refundFlowDTO =  refundFlowService.findOneRefundFlow(params);
            QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(accountFlowDTO.getFlowId());
            //判断退款回滚交易类型
            Integer transType = null;
            if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_12.getCode()) {
                transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_15.getCode();
            }
            if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_13.getCode()) {
                transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_16.getCode();
            }
            if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_14.getCode()) {
                transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_17.getCode();
            }
            //调用账户系统查询是否存在入账信息，存在则查询设置状态，不存在则进行入账操作
            try {
                JSONObject accountResult = serverService.transactionInfo(accountFlowDTO.getRollBackNo().toString());
                if (accountResult != null) {
                    if (accountResult.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                        refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                        refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);
                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                        accountFlowService.updateAccountFlow(accountFlowDTO.getId(), accountFlowDTO, null);
                        //交易金额回滚
                       // transAmountRollback(refundFlowDTO.getFlowId(), refundFlowDTO.getRefundAmount(), null);
                        //退款失败发送消息
//                        sendMessage(refundFlowDTO.getOrgRecUserId(), new Integer(StaticDataEnum.SEND_NODE_17.getCode()));
                    } else if (accountResult.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                        //将退款流水状态更新为回滚失败状态
                        refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_42.getCode());
                        refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);
                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                        accountFlowService.updateAccountFlow(accountFlowDTO.getId(), accountFlowDTO, null);
                    } else {
                        refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_43.getCode());
                        refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);
                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                        accountFlowService.updateAccountFlow(accountFlowDTO.getId(), accountFlowDTO, null);
                    }
                } else {
                    JSONObject accountParams = new JSONObject();
                    accountParams.put("amountInUserId", qrPayFlowDTO.getRecUserId());
                    accountParams.put("subAccountType", StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
                    accountParams.put("channel", StaticDataEnum.ACCOUNT_CHANNEL_0001.getMessage());
                    accountParams.put("channelSerialnumber", accountFlowDTO.getRollBackNo());
                    accountParams.put("transAmount", accountFlowDTO.getTransAmount());
                    try {
                        serverService.amountIn(accountParams);
                        //交易金额回滚
                     //   transAmountRollback(refundFlowDTO.getFlowId(), refundFlowDTO.getRefundAmount(), null);
                        //更新回滚流水
                        updateAccountFlowDTO(accountFlowDTO.getRollBackNo(), StaticDataEnum.TRANS_STATE_1.getCode(), transType, null);

                    } catch (Exception e) {
                        //更新回滚流水
                        updateAccountFlowDTO(accountFlowDTO.getRollBackNo(), StaticDataEnum.TRANS_STATE_3.getCode(), transType, null);
                    }

                    refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);
//                    sendMessage(refundFlowDTO.getOrgRecUserId(), new Integer(StaticDataEnum.SEND_NODE_17.getCode()));

                }
            } catch (Exception e) {
                //更新回滚流水
                updateAccountFlowDTO(accountFlowDTO.getRollBackNo(), StaticDataEnum.TRANS_STATE_3.getCode(), transType, null);
                continue;
            }
        }
    }

    @Override
    public void refundFailedAccountOut() throws Exception {
        List<AccountFlowDTO> list = accountFlowService.selectAccountOutDoubtFlow();
        for (AccountFlowDTO accountFlowDTO : list) {
            try {
                JSONObject accountResult = serverService.transactionInfo(accountFlowDTO.getOrderNo().toString());
                if (accountResult != null) {
                    //查询出账交易是否成功
                    if (accountResult.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                        //更新出账交易状态
                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                        accountFlowService.updateAccountFlow(accountFlowDTO.getFlowId(), accountFlowDTO, null);
                        //回滚
                        RefundFlowDTO refundFlowDTO = refundFlowService.findRefundFlowById(accountFlowDTO.getFlowId());
                        QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(refundFlowDTO.getFlowId());
                        Integer transType = null;
                        JSONObject amountIn = createAccountFlowDTO(refundFlowDTO, qrPayFlowDTO, StaticDataEnum.AMOUNT_OUT_ROLL_BACK.getCode(), null);
                        //判断回滚交易类型
                        if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_12.getCode()) {
                            transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_15.getCode();
                        }
                        if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_13.getCode()) {
                            transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_16.getCode();
                        }
                        if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_14.getCode()) {
                            transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_17.getCode();
                        }
                        try {
                            serverService.amountIn(amountIn);
                            //交易金额回滚
                       //     transAmountRollback(refundFlowDTO.getFlowId(), refundFlowDTO.getRefundAmount(), null);
                            //更新回滚流水
                            updateAccountFlowDTO(amountIn.getLong("channelSerialnumber"), StaticDataEnum.TRANS_STATE_1.getCode(), transType, null);
                        } catch (Exception e) {
                            refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_43.getCode());
                            refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, null);
                            //更新回滚流水
                            updateAccountFlowDTO(amountIn.getLong("channelSerialnumber"), StaticDataEnum.TRANS_STATE_3.getCode(), transType, null);
                        }
                    }
                }
            } catch (Exception e) {
                //发生异常不进行任何修改
            }
        }
    }

    /**
     * 创建退款流水
     * @param orderRefundDTO
     * @param qrPayFlowDTO
     * @param request
     * @return
     * @throws BizException
     */
    public RefundFlowDTO createRefundFlowDTO(OrderRefundDTO orderRefundDTO, QrPayFlowDTO qrPayFlowDTO, Integer refundType, HttpServletRequest request) throws BizException {
        RefundFlowDTO refundFlowDTO = new RefundFlowDTO();
        refundFlowDTO.setGatewayId(qrPayFlowDTO.getGatewayId());
        refundFlowDTO.setFlowId(orderRefundDTO.getFlowId());
        refundFlowDTO.setOrgRecUserId(qrPayFlowDTO.getRecUserId());
        refundFlowDTO.setRefundAmount(orderRefundDTO.getAmount());
        refundFlowDTO.setOrdreNo(SnowflakeUtil.generateId().toString());
        refundFlowDTO.setBatchId(qrPayFlowDTO.getBatchId());
        refundFlowDTO.setClearState(qrPayFlowDTO.getClearState());
        refundFlowDTO.setClearTime(qrPayFlowDTO.getClearTime());
        refundFlowDTO.setReason(orderRefundDTO.getReason());
        refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        refundFlowDTO.setNotSettlementAmount(orderRefundDTO.getAmount());
        if (qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode()
                || qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_4.getCode()) {
            if (refundType.equals(StaticDataEnum.LAT_PAY_REFUND_TYPE_3.getCode())) {
                refundFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_18.getCode());
            } else {
                refundFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_12.getCode());
            }
        }
        if (qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_8.getCode()
                || qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_10.getCode()) {
            refundFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_13.getCode());
        }
        if (qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_9.getCode()
                || qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_11.getCode()) {
            refundFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_14.getCode());
        }
        log.info("create refund flow, refundFlowDTO:{}", refundFlowDTO);
        Long id = refundFlowService.saveRefundFlow(refundFlowDTO, request);
        refundFlowDTO.setId(id);
        return refundFlowDTO;
    }

    /**
     * 创建出账流水
     * @param refundFlowDTO
     * @param qrPayFlowDTO
     * @param state 0: 出账 1：出账回滚
     * @param request
     * @return
     * @throws BizException
     */
    public JSONObject createAccountFlowDTO(RefundFlowDTO refundFlowDTO, QrPayFlowDTO qrPayFlowDTO, Integer state, HttpServletRequest request) throws BizException {
        //创建流水
        AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
        accountFlowDTO.setFlowId(refundFlowDTO.getFlowId());
        accountFlowDTO.setUserId(qrPayFlowDTO.getRecUserId());
        accountFlowDTO.setOppositeUserId(qrPayFlowDTO.getPayUserId());
        accountFlowDTO.setAccountType(StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
        accountFlowDTO.setTransAmount(refundFlowDTO.getRefundAmount());
        accountFlowDTO.setOppositeAccountType(StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
        accountFlowDTO.setOrderNo(Long.valueOf(refundFlowDTO.getOrdreNo()));
        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
        //创建账户流水参数，入账操作，出账操作
        JSONObject accountParams = new JSONObject();
        if (state == StaticDataEnum.AMOUNT_OUT.getCode()) {
//            if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_12.getCode()) {
//                accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_12.getCode());
//            }
//            if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_13.getCode()) {
//                accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_13.getCode());
//            }
//            if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_14.getCode()) {
//                accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_14.getCode());
//            }
            accountFlowDTO.setTransType(refundFlowDTO.getTransType());
            accountParams.put("userId", qrPayFlowDTO.getRecUserId());
            accountParams.put("channel", StaticDataEnum.ACCOUNT_CHANNEL_0001.getMessage());
            accountParams.put("channelSerialnumber", accountFlowDTO.getOrderNo());
            accountParams.put("transAmount", accountFlowDTO.getTransAmount());
            accountParams.put("transType", accountFlowDTO.getTransType());
        } else {
            Long no = SnowflakeUtil.generateId();
            if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_12.getCode()) {
                accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_15.getCode());
            }
            if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_13.getCode()) {
                accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_16.getCode());
            }
            if (refundFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_14.getCode()) {
                accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_17.getCode());
            }
            accountParams.put("amountInUserId", qrPayFlowDTO.getRecUserId());
            accountParams.put("subAccountType", StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
            accountParams.put("channel", StaticDataEnum.ACCOUNT_CHANNEL_0001.getMessage());
            accountParams.put("channelSerialnumber", no);
            accountParams.put("transAmount", accountFlowDTO.getTransAmount());
            accountParams.put("transType", accountFlowDTO.getTransType());
            accountFlowDTO.setRollBackNo(no);
        }
        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        log.info("create account flow, accountFlowDTO:{}", accountFlowDTO);
        accountFlowService.saveAccountFlow(accountFlowDTO, request);
        log.info("create account system params, accountParams:{}", accountParams);
        return accountParams;
    }

    /**
     * 更新账户流水
     * @param orderNo
     * @param state
     * @param transType
     * @param request
     * @throws BizException
     */
    public void updateAccountFlowDTO(Long orderNo, Integer state, Integer transType, HttpServletRequest request) throws BizException {
        Map<String, Object> params = new HashMap<>(1);
        params.put("transType", transType);
        if (transType == StaticDataEnum.ACC_FLOW_TRANS_TYPE_15.getCode()
                || transType == StaticDataEnum.ACC_FLOW_TRANS_TYPE_16.getCode()
                || transType == StaticDataEnum.ACC_FLOW_TRANS_TYPE_17.getCode()) {
            params.put("rollBackNo", orderNo);
        } else {
            params.put("orderNo", orderNo);
        }
        AccountFlowDTO accountFlowDTO = accountFlowService.findOneAccountFlow(params);
        accountFlowDTO.setState(state);
        log.info("updata account flow, accountFlowDTO:{}", accountFlowDTO);
        accountFlowService.updateAccountFlow(accountFlowDTO.getId(), accountFlowDTO, request);
    }

    /**
     * 退款失败，交易金额回滚
     * @param flowId
     * @param amount
     * @param request
     * @throws BizException
     */
    public void transAmountRollback(Long flowId, BigDecimal amount, HttpServletRequest request) throws BizException {
        QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(flowId);
        BigDecimal banlance = qrPayFlowDTO.getRecAmount();
        banlance = MathUtils.add(banlance, amount);
        qrPayFlowDTO.setRecAmount(banlance);
        qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(), qrPayFlowDTO, request);
    }

    /**
     * 查询订单交易金额、收款人未清算金额是否充足
     * @param qrPayFlowDTO
     * @param amount
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean checkBalance(QrPayFlowDTO qrPayFlowDTO, BigDecimal amount, HttpServletRequest request) throws BizException {
        BigDecimal banlance = qrPayFlowDTO.getPayAmount();
        BigDecimal refundTotal = qrPayFlowDAO.selectOrderRefund(qrPayFlowDTO.getId());
        if (refundTotal == null) {
            refundTotal = new BigDecimal("0");
        }

        //若退款金额大于交易金额，则退款失败
        if (banlance.subtract(refundTotal).setScale(2, BigDecimal.ROUND_HALF_UP).compareTo(amount) == -1) {
            log.info("balance:{}, refund amount:{}", banlance, amount);
            return false;
        }
//        banlance = MathUtils.subtract(banlance, amount);
//        qrPayFlowDTO.setRecAmount(banlance);
//        qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(), qrPayFlowDTO, request);
        //判断当前渠道下未清算金额是否足够退款
        BigDecimal unClearAmount = qrPayFlowDAO.selectUnClearByGateway(qrPayFlowDTO.getGatewayId(), qrPayFlowDTO.getRecUserId());
        if (unClearAmount.compareTo(amount) == -1) {
            log.info("unClearAmount:{}, refund amount:{}", unClearAmount, amount);
            //将交易资金跟新回之前资金
//            banlance = MathUtils.add(banlance, amount);
//            qrPayFlowDTO.setRecAmount(banlance);
//            qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(), qrPayFlowDTO, request);
            return false;
        }
        return true;
    }

    /**
     * 手续费添加
     * @param qrPayFlowDTO
     * @param refundFlowDTO
     * @param request
     * @throws BizException
     */
    public BigDecimal gatewayFeeAdd(QrPayFlowDTO qrPayFlowDTO, RefundFlowDTO refundFlowDTO, HttpServletRequest request) throws BizException {
        BigDecimal payFee = qrPayFlowDTO.getFee();
        BigDecimal platformFee = qrPayFlowDTO.getPlatformFee();
        BigDecimal payAmount = qrPayFlowDTO.getPayAmount();
        BigDecimal refundAmount = refundFlowDTO.getRefundAmount();
        //计算退回手续费
        //TODO 因为退款交易时，用户付手续费也可以全额退款，所以补充资金的时候，资金总额为实付金额，如果退款逻辑修改，这个地方要相应修改
        BigDecimal complementFee = null;
//        if (qrPayFlowDTO.getFeeDirection().intValue() == StaticDataEnum.FEE_DIRECTION_1.getCode()) {
//            //(平台手续费-渠道手续费)*退款金额/（实付金额-渠道手续费）
//            complementFee = platformFee.subtract(payFee).multiply(refundAmount.divide(payAmount.subtract(payFee), 2, BigDecimal.ROUND_HALF_UP)).setScale(2, RoundingMode.HALF_UP);
//        } else {
            //(平台手续费-渠道手续费)*退款金额/实付金额
            complementFee = platformFee.subtract(payFee).multiply(refundAmount.divide(payAmount, 2, BigDecimal.ROUND_HALF_UP)).setScale(2, RoundingMode.HALF_UP);
//        }
        log.info("make up fee cal, payFee:{}, platformFee:{}, payAmount:{}, refundAmount：");
        return complementFee;
    }

    public void sendMessage(Long userId, Integer sendNode) {
        UserDTO userDTO = userService.findUserById(userId);
        Map<String, Object> userSearchParams = new HashMap<>(4);
        userSearchParams.put("merchantId", userDTO.getMerchantId());
        List<UserDTO> userDTOList = userService.find(userSearchParams, null, null);
        if (!CollectionUtils.isEmpty(userDTOList)) {
            MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(sendNode.toString());
            userSearchParams.clear();
            for (UserDTO userDTO1 : userDTOList) {
                long wholeSaleAction = 28L;
                userSearchParams.put("userId", userDTO1.getId());
                userSearchParams.put("actionId", wholeSaleAction);
                UserActionDTO userActionDTO = userActionService.findOneUserAction(userSearchParams);
                if (userActionDTO.getId() != null) {
                    //获取邮件模板
                    try {
                        //notice
                        NoticeDTO noticeDTO = new NoticeDTO();
                        noticeDTO.setUserId(userDTO1.getId());
                        noticeDTO.setTitle(mailTemplateDTO.getEnMailTheme());
                        noticeDTO.setContent(mailTemplateDTO.getEnSendContent());
                        noticeService.saveNotice(noticeDTO, null);
                        if (StringUtils.isNotEmpty(userDTO1.getPushToken())) {
                            //push
                            FirebaseDTO firebaseDTO = new FirebaseDTO();
                            firebaseDTO.setAppName("UwalletM");
                            firebaseDTO.setUserId(userDTO1.getId());
                            firebaseDTO.setTitle(mailTemplateDTO.getEnMailTheme());
                            firebaseDTO.setBody(mailTemplateDTO.getEnSendContent());
                            firebaseDTO.setVoice(StaticDataEnum.VOICE_0.getCode());
                            serverService.pushFirebase(firebaseDTO,null);
                        }
                    } catch (Exception e) {
                        log.info("send mail faild, userId:{}, error message:{}", userId, e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * latPay退款结果处理
     * @param returnData
     * @param refundFlowDTO
     * @param request
     * @throws Exception
     */
    public JSONObject latPayRefundRequestResultHandle(JSONObject returnData, RefundFlowDTO refundFlowDTO, QrPayFlowDTO qrPayFlowDTO, Integer refundType, HttpServletRequest request) throws Exception {
        JSONObject result = new JSONObject();
        refundFlowDTO.setLpsRefundId(returnData.getString("LPS_refund_id"));
        refundFlowDTO.setRequestId(returnData.getString("Request_Id"));
        refundFlowDTO.setReplyPwd(returnData.getString("LPSReply_pwd"));
        if (refundType.equals(StaticDataEnum.LAT_PAY_REFUND_TYPE_3.getCode())) {
            if (returnData.getInteger("RefundSubmit_status").equals(StaticDataEnum.LP_RESPONSE_TYPE_0.getCode())) {
                refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
                result.put("refundState", StaticDataEnum.TRANS_STATE_3.getCode());
                result.put("refundMessage", I18nUtils.get("refund.processing", getLang(request)));
//                sendMessage(refundFlowDTO.getOrgRecUserId(), new Integer(StaticDataEnum.SEND_NODE_14.getCode()));
            } else {
                refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
                result.put("refundState", StaticDataEnum.TRANS_STATE_2.getCode());
                result.put("refundMessage", I18nUtils.get("refund.fail", getLang(request)));
            }
        } else {
            if (returnData.getInteger("RefundSubmit_status").equals(StaticDataEnum.LP_RESPONSE_TYPE_0.getCode())) {
                //银行状态00为成功，90为可疑，其他为退款失败
                if (returnData.getString("Bank_status").equals(StaticDataEnum.LAT_PAY_BANK_STATUS_0.getMessage())) {
                    refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                    //退款成功，计算订单中应退回的手续费，补充到收款方
                    //TODO: 补款不明确,
//                if (refundFlowDTO.getTransType().equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_12.getCode())) {
//                    refundFlowDTO.setMakeUpFee(gatewayFeeAdd(qrPayFlowDTO, refundFlowDTO, request));
//                }
                    refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
                    result.put("refundState", StaticDataEnum.TRANS_STATE_1.getCode());
                    sendMessage(refundFlowDTO.getOrgRecUserId(), new Integer(StaticDataEnum.SEND_NODE_14.getCode()));
                } else if (returnData.getString("Bank_status").equals(StaticDataEnum.LAT_PAY_BANK_STATUS_9.getMessage())
                        ||returnData.getString("Bank_status").equals(StaticDataEnum.LAT_PAY_BANK_STATUS_5.getMessage())) {
                    //05,90失败
                    refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
                    result.put("refundState", StaticDataEnum.TRANS_STATE_2.getCode());
                    result.put("refundMessage", I18nUtils.get("refund.fail", getLang(request)));
                } else {
                    refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                    refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
                    result.put("refundState", StaticDataEnum.TRANS_STATE_3.getCode());
                    result.put("refundMessage", I18nUtils.get("refund.processing", getLang(request)));
                }
            } else {
                refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
                result.put("refundState", StaticDataEnum.TRANS_STATE_2.getCode());
                result.put("refundMessage", I18nUtils.get("refund.fail", getLang(request)));
            }
        }

        return result;
    }

    /**
     * 获取当前语言，默认保持英文
     * @author faker
     * @param request
     * @return
     */
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
