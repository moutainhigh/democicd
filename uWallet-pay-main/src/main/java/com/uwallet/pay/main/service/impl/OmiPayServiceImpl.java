package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.core.util.HttpClientUtils;
import com.uwallet.pay.core.util.MD5FY;
import com.uwallet.pay.core.util.UuidUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.model.dto.AccountFlowDTO;
import com.uwallet.pay.main.model.dto.QrPayFlowDTO;
import com.uwallet.pay.main.model.dto.RechargeFlowDTO;
import com.uwallet.pay.main.model.dto.WithholdFlowDTO;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.uwallet.pay.main.util.UploadFileUtil.getLang;

@Service
@Slf4j
public class OmiPayServiceImpl implements OmiPayService {

    @Value("${omipay.mNumber}")
    private String mNumber;

    @Value("${omipay.secretKey}")
    private String secretKey;

    @Value("${omipay.statusCheckUrl}")
    private String statusCheckUrl;

    @Value("${omipay.refundApplyUrl}")
    private String refundApplyUrl;

    @Value("${omipay.refundStatusCheckUrl}")
    private String refundStatusCheckUrl;

    @Autowired
    @Lazy
    private QrPayFlowService qrPayFlowService;

    @Autowired
    @Lazy
    private QrPayService qrPayService;

    @Autowired
    private RechargeFlowService rechargeFlowService;

    @Autowired
    private WithholdFlowService withholdFlowService;

    @Autowired
    private AccountFlowService accountFlowService;

    @Autowired
    private ServerService serverService;

    @Override
    public Integer statusCheck(String orderNo) throws Exception {
        log.info("omi pay check order status, orderNo:{}", orderNo);
        Integer orderStatus = null;
        if (StringUtils.isEmpty(orderNo)) {
            orderStatus = StaticDataEnum.TRANS_STATE_3.getCode();
            return orderStatus;
        }
        Long timestamp = System.currentTimeMillis();
        String nonceStr = UuidUtil.get32UUID();
        //生成验证签
        String sign = MD5FY.MD5Encode(mNumber + "&" + timestamp + "&"
                + nonceStr + "&" + secretKey);
        //拼装请求路由
        String requestUrl = statusCheckUrl + "?m_number=" + mNumber + "&timestamp=" + timestamp
                + "&nonce_str=" + nonceStr + "&sign=" + sign.toUpperCase() + "&order_no=" + orderNo;
        //请求订单结果
        log.info("omi pay check order status request url, url:{}", requestUrl);
        JSONObject returnData = JSONObject.parseObject(HttpClientUtils.post(requestUrl, ""));
        log.info("omi pay check order return data, data:{}", returnData);
        //判断返回结果,判断当前交易状态：交易中、成功、失败、可疑
        if (returnData.getString("return_code").equals(StaticDataEnum.OMI_PAY_SUCCESS.getMessage())) {
            String resultCode = returnData.getString("result_code");
            if (resultCode.equals(StaticDataEnum.OMI_PAY_PAID.getMessage()) || resultCode.equals(StaticDataEnum.OMI_PAY_CLOSED.getMessage())) {
                orderStatus = StaticDataEnum.TRANS_STATE_1.getCode();
            } else if (resultCode.equals(StaticDataEnum.OMI_PAY_FAILED.getMessage()) || resultCode.equals(StaticDataEnum.OMI_PAY_CANCELLED.getMessage())) {
                orderStatus = StaticDataEnum.TRANS_STATE_2.getCode();
            } else if (resultCode.equals(StaticDataEnum.OMI_PAY_READY.getMessage()) || resultCode.equals(StaticDataEnum.OMI_PAY_PAYING.getMessage())) {
                orderStatus = StaticDataEnum.TRANS_STATE_3.getCode();
            }
        } else {
            orderStatus = StaticDataEnum.TRANS_STATE_2.getCode();
        }
        log.info("omi pay order status, status:{}", orderStatus);
        //返回订单状态
        return orderStatus;

    }

    @Override
    public void getPaidOrderInfo(JSONObject data, HttpServletRequest request) throws Exception {
        log.info("get paid order info, data:{}", data);
        String orderNo = data.getString("out_order_no");
        Map<String, Object> params = new HashMap<>(1);
        params.put("flowId", orderNo);
        WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(params);
        //获取用户账户
        JSONObject accountData = serverService.getAccountInfo(withholdFlowDTO.getUserId());
        //更新三方流水交易状态为成功
        withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
        //根据交易类型查询相应流水记录并生成对应的账务流水
        AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
        JSONObject amountIn = new JSONObject();
        if (withholdFlowDTO.getTransType().intValue() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_0.getCode()) {
            RechargeFlowDTO rechargeFlowDTO = rechargeFlowService.findRechargeFlowById(withholdFlowDTO.getFlowId());
            rechargeFlowDTO.setState(StaticDataEnum.TRANS_STATE_21.getCode());
            log.info("recharge flow updata, rechargeFlowDTO:{}", rechargeFlowDTO);
            rechargeFlowService.withholdFlowStateChange(rechargeFlowDTO, withholdFlowDTO, request);
            //生成账务流水
            accountFlowDTO.setFlowId(rechargeFlowDTO.getId());
            accountFlowDTO.setUserId(rechargeFlowDTO.getUserId());
            accountFlowDTO.setAccountType(rechargeFlowDTO.getAccountType());
            accountFlowDTO.setTransAmount(rechargeFlowDTO.getTransAmount());
            accountFlowDTO.setFeeDirection(rechargeFlowDTO.getFeeDirection());
            accountFlowDTO.setFee(rechargeFlowDTO.getFee());
            accountFlowDTO.setOppositeAccountType(rechargeFlowDTO.getAccountType());
            accountFlowDTO.setOrderNo(Long.valueOf(withholdFlowDTO.getOrdreNo()));
            accountFlowDTO.setTransType(rechargeFlowDTO.getTransType());
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
            //生成账户流信息
            amountIn.put("userId", accountFlowDTO.getUserId());
            amountIn.put("amountInUserId", accountFlowDTO.getUserId());
            amountIn.put("channel", StaticDataEnum.ACCOUNT_CHANNEL_0001.getMessage());
            amountIn.put("channelSerialnumber", orderNo);
            amountIn.put("accountId", accountData.getLongValue("id"));
            amountIn.put("cardNo", null);
            amountIn.put("transAmount", accountFlowDTO.getTransAmount());
            amountIn.put("transType", accountFlowDTO.getTransType());
            amountIn.put("feeAmount", accountFlowDTO.getFee());
            amountIn.put("feeDirection", accountFlowDTO.getFeeDirection());
            //保存账户流信息
            log.info("account flow dto, dto:{}", accountFlowDTO);
            accountFlowService.saveAccountFlow(accountFlowDTO, request);
            //调用账户系统信息更新用户账户信息
            try {
                //根据结果判断交易状态
                JSONObject msg = serverService.amountIn(amountIn);
                String code = msg.getString("code");
                if (code.equals(ErrorCodeEnum.SUCCESS_CODE.getCode())) {
                    rechargeFlowService.accountFlowStateChange(rechargeFlowDTO, msg, StaticDataEnum.TRANS_STATE_31.getCode(), request);
                } else if (code.equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
                    rechargeFlowService.accountFlowStateChange(rechargeFlowDTO, msg, StaticDataEnum.TRANS_STATE_32.getCode(), request);
                    log.info("account flow insert failed, info:{}", msg);
                    throw new BizException(I18nUtils.get("recharge.failed", getLang(request)));
                } else {
                    rechargeFlowService.accountFlowStateChange(rechargeFlowDTO, msg, StaticDataEnum.TRANS_STATE_33.getCode(), request);
                    log.info("account flow insert doubtful, info:{}", msg);
                    throw new BizException(I18nUtils.get("recharge.doubtful", getLang(request)));
                }
            } catch (Exception e) {
                rechargeFlowService.accountFlowStateChange(rechargeFlowDTO, null, StaticDataEnum.TRANS_STATE_33.getCode(), request);
                log.error("account flow insert failed message:{}", e.getMessage());
                throw new BizException(I18nUtils.get("recharge.doubtful", getLang(request)));
            }
        } else {
            QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(withholdFlowDTO.getFlowId());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_21.getCode());
            log.info("update success status flow dto, qrPayFlowDTO:{}, withholdFlowDTO:{}", qrPayFlowDTO, withholdFlowDTO);
            try{
                qrPayService.updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
            } catch (Exception e){
                log.info("OmiPayService.getPaidOrderInfo 更新交易状态并发，跳过后续流程");
                throw e;
            }

            //创建账户交易流水
            accountFlowDTO = qrPayService.getAccountFlowDTO(qrPayFlowDTO);
            try {
                //保存账户交易流水
                accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO, request));
            } catch (Exception e) {
                //交易失败
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                qrPayService.updateFlow(qrPayFlowDTO, null, null, request);
            }
            qrPayService.doAmountTrans(accountFlowDTO, qrPayFlowDTO, request);
        }
    }

    @Override
    public JSONObject refundApply(JSONObject data) throws Exception {
        String orderNo = data.getString("order_no");
        String outRefundNo = data.getString("out_refund_no");
        BigDecimal amount = data.getBigDecimal("amount").multiply(new BigDecimal("100"));
        Long timestamp = System.currentTimeMillis();
        String nonceStr = UuidUtil.get32UUID();
        //生成验证签
        String sign = MD5FY.MD5Encode(mNumber + "&" + timestamp + "&"
                + nonceStr + "&" + secretKey);
        //拼装请求路由
        String requestUrl = refundApplyUrl + "?m_number=" + mNumber + "&timestamp=" + timestamp
                + "&nonce_str=" + nonceStr + "&sign=" + sign.toUpperCase() + "&order_no=" + orderNo
                + "&out_refund_no=" + outRefundNo + "&amount=" + amount.intValue();
        //请求订单结果
        log.info("omi pay refund order apply request url, url:{}", requestUrl);
        JSONObject returnData = JSONObject.parseObject(HttpClientUtils.post(requestUrl, ""));
        log.info("omi pay refund order apply result data, data:{}", returnData);
        return returnData;
    }

    @Override
    public Integer refundStatusCheck(String refundNo) throws Exception {
        Long timestamp = System.currentTimeMillis();
        String nonceStr = UuidUtil.get32UUID();
        Integer status = null;
        if (StringUtils.isEmpty(refundNo)) {
            status = StaticDataEnum.TRANS_STATE_3.getCode();
            return status;
        }
        //生成验证签
        String sign = MD5FY.MD5Encode(mNumber + "&" + timestamp + "&"
                + nonceStr + "&" + secretKey);
        //拼装请求路由
        String requestUrl = refundStatusCheckUrl + "?m_number=" + mNumber + "&timestamp=" + timestamp
                + "&nonce_str=" + nonceStr + "&sign=" + sign.toUpperCase() + "&refund_no=" + refundNo;
        //请求订单结果
        log.info("omi pay check refund order status request url, url:{}", requestUrl);
        JSONObject returnData = JSONObject.parseObject(HttpClientUtils.post(requestUrl, ""));
        log.info("omi pay check refund order return data, data:{}", returnData);
        //判断返回结果,判断当前交易状态：交易中、成功、失败、可疑
        if (returnData.getString("return_code").equals(StaticDataEnum.OMI_PAY_SUCCESS.getMessage())) {
            String resultCode = returnData.getString("result_code");
            if (resultCode.equals(StaticDataEnum.OMI_REFUND_ORGANIZATION_PAYBACK.getMessage())
                    || resultCode.equals(StaticDataEnum.OMI_REFUND_CLOSED.getMessage())
                    || resultCode.equals(StaticDataEnum.OMI_REFUND_PAYMENT_CHANNEL_CONFIRMED.getMessage())) {
                status = StaticDataEnum.TRANS_STATE_1.getCode();
            } else if (resultCode.equals(StaticDataEnum.OMI_MERCHANT_REJECTED.getMessage())
                    || resultCode.equals(StaticDataEnum.OMI_TIME_OUT_CLOSED.getMessage())
                    || resultCode.equals(StaticDataEnum.OMI_ORGANIZATION_FAILED.getMessage())
                    || resultCode.equals(StaticDataEnum.OMI_CUSTOMER_CANCELLED.getMessage())) {
                status = StaticDataEnum.TRANS_STATE_2.getCode();
            } else {
                status = StaticDataEnum.TRANS_STATE_3.getCode();
            }
        } else {
            status = StaticDataEnum.TRANS_STATE_2.getCode();
        }
        return status;
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
