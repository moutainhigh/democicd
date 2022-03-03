package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.uwallet.pay.core.common.PosApiCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.MD5FY;
import com.uwallet.pay.core.util.QRCodeUtil;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.exception.PosApiException;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.HttpClientUtilNew;
import com.uwallet.pay.main.util.RandomUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.*;

/**
 * <p>
 * POS API service实现类
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: pos api
 * @author: zhangzeyuan
 * @date: Created in 2021-03-19 16:59:51
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Service
@Slf4j
public class PosApiServiceImpl extends BaseServiceImpl implements PosApiService {

    @Resource
    private PosInfoService posInfoService;

    @Resource
    private PosQrPayFlowService posQrPayFlowService;

    @Resource
    @Lazy
    private MerchantService merchantService;

    @Resource
    private PosSecretConfigService posSecretConfigService;

    @Resource
    private QrPayFlowService qrPayFlowService;


    @Value("${spring.posQrCodeUrl}")
    private String posQrCodeUrl;


    /**
     * 生成POS商户二维码信息
     *
     * @param requestInfo
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2021/3/24 16:22
     */
    @Override
    public JSONObject createPosQrCode(JSONObject requestInfo, HttpServletRequest request) throws PosApiException {
        //参数信息
        long sysTimestamp = System.currentTimeMillis();
        Long timestamp = requestInfo.getLong("timestamp");
        String random = requestInfo.getString("random");
        String posId = requestInfo.getString("posId");
        String sign = requestInfo.getString("sign");
        Long merchantId = requestInfo.getLong("merchantId");

        //验签
        boolean verifySignResult = this.verifyPosSign(timestamp, sysTimestamp, random, posId, sign, merchantId);
        if (!verifySignResult) {
            throw new PosApiException(PosApiCodeEnum.VERIFY_SIGN_ERROR);
        }

        //验证该商户POS设备权限
        boolean posAuthorityResult = this.checkPosAuthority(posId, merchantId);
        if (!posAuthorityResult) {
            throw new PosApiException(PosApiCodeEnum.POS_NOT_REGISTERED);
        }

        //系统订单号
        Long paySysTransNo = SnowflakeUtil.generateId();
        requestInfo.put("showThirdTransNo", paySysTransNo);

        //生成二维码信息
        String base64PosQrCode = this.createPosQrCode(paySysTransNo);
        if (StringUtils.isBlank(base64PosQrCode)) {
            throw new PosApiException(PosApiCodeEnum.QR_CODE_GENERATE_ERROR);
        }
        requestInfo.put("base64QrCode", base64PosQrCode);

        //预生成POS支付订单
        boolean savePayFlowResult = this.createQrPayFlowRecord(requestInfo, request);
        if (!savePayFlowResult) {
            throw new PosApiException(PosApiCodeEnum.ORDER_CREATE_ERROR);
        }
        //封装返回数据
        JSONObject resultJson = new JSONObject(3);
        resultJson.put("qr_code", base64PosQrCode);
        resultJson.put("pos_trans_no", requestInfo.getString("pos_order_number"));
        resultJson.put("payo_trans_no", paySysTransNo);
        return resultJson;
    }


    /**
     * 测试生成二维码方法
     *
     * @param requestInfo
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2021/5/22 14:51
     */
    public JSONObject createPosQrCodeTest(JSONObject requestInfo, HttpServletRequest request) throws PosApiException {

        BigDecimal transAmount = new BigDecimal("5.12");

        for (int i = 1; i < 15; i++) {

            Long paySysTransNo = SnowflakeUtil.generateId();

            //pos 订单号
            String posOrderNumber = "202105221448000000" + i;
            //回调地址
            String notifyUrl = "http://localhost:6010/pos/v1/notifyTest/";
            //金额
            //货币类型
            String currencyTypeu = requestInfo.getString("currencyType");

            transAmount = new BigDecimal("0.01").add(transAmount);
            //商户ID
            Long merchantId = 555166400535678976L;
            //pos id
            String posId = "8216741415";

            String base64PosQrCode = this.createPosQrCode(paySysTransNo);

            //保存记录
            PosQrPayFlowDTO payFlow = new PosQrPayFlowDTO();
            payFlow.setMerchantId(merchantId);
            payFlow.setCurrencyType(currencyTypeu);
            payFlow.setNotifyUrl(notifyUrl);
            payFlow.setOrderStatus(StaticDataEnum.POS_ORDER_STATUS_CREATE_ORDER.getCode());
            payFlow.setPosId(posId);
            payFlow.setPosTransNo(posOrderNumber);
            payFlow.setShowThirdTransNo(paySysTransNo.toString());
            payFlow.setTransAmount(transAmount);
            try {
                posQrPayFlowService.savePosQrPayFlow(payFlow, request);
            } catch (BizException e) {
                log.error("保存POS支付订单出错," + e.getMessage());
            }

        }

        return null;
    }


    /**
     * 支付成功之后通知POS端 异步
     *
     * @param merchantId
     * @param transNo
     * @param request
     * @author zhangzeyuan
     * @date 2021/3/23 16:34
     */
    //todo  业务扩大 创建自己的线程池
    @Async("taskExecutor")
    @Override
    public void posPaySuccessNotice(Long merchantId, String transNo, HttpServletRequest request) throws PosApiException {
        if (StringUtils.isBlank(transNo) || Objects.isNull(merchantId)) {
            throw new PosApiException(PosApiCodeEnum.NOTIFY_ERROR);
        }
        log.info("回调通知开始执行，merchatId:" + merchantId + "|transNo:" + transNo);
        HashMap<String, Object> queryParamMap = Maps.newHashMapWithExpectedSize(2);
        queryParamMap.put("merchantId", merchantId);
        queryParamMap.put("sysTransNo", transNo);
        PosQrPayFlowDTO onePosQrPayFlow = posQrPayFlowService.findOnePosQrPayFlow(queryParamMap);
        //扫描POS二维码进行通知  取出通知地址、和订单信息
        if (Objects.nonNull(onePosQrPayFlow) && StringUtils.isNoneBlank(onePosQrPayFlow.getNotifyUrl())) {
            //通知
            log.info("POS订单进行通知");
            this.posNotifyThreeRequest(merchantId, transNo, onePosQrPayFlow.getNotifyUrl(), Constant.ORDER_TYPE_POS, request);
            return;
        }
        //普通二维码扫描出来的pos订单 取qrpayflow取订单信息 并查询商户的通知地址 进行通知
        String merchantNotifyUrl = merchantService.getMerchantNotifyUrl(merchantId);
        if (StringUtils.isBlank(merchantNotifyUrl)) {
            return;
        }
        //进行通知
        log.info("普通订单进行通知");
        this.posNotifyThreeRequest(merchantId, transNo, merchantNotifyUrl, Constant.ORDER_TYPE_NORMAL, request);
    }


    /**
     * POS订单支付成功进行回调通知
     *
     * @param merchantId
     * @param transNo
     * @param url
     * @param orderType
     * @param request
     * @author zhangzeyuan
     * @date 2021/3/24 16:23
     */
    private void posNotifyThreeRequest(Long merchantId, String transNo, String url, Integer orderType, HttpServletRequest request) throws PosApiException {
        //封装请求参数
        JSONObject requestJson = new JSONObject(4);
        //业务参数
        this.packageNotifyBusinessParams(orderType, transNo, requestJson);
        //签名参数
        requestJson.put("timestamp", System.currentTimeMillis());
        requestJson.put("merchantId", merchantId);
        requestJson.put("random", RandomUtils.generateString(20));
        for (int i = 0; i < 3; i++) {
            boolean result = posNotifysendPost(url, requestJson.toJSONString());
            if (result) {
                //通知成功
                log.info("商户ID为：" + merchantId + "的订单" + transNo + "发送回调通知成功");
                break;
            }
            if (i == 2) {
                //三次失败则
                log.info("商户ID为：" + merchantId + "的订单" + transNo + "发送回调通知失败");
            }
        }
    }


    /**
     * 封装支付成功回调通知业务参数
     *
     * @param orderType
     * @param transNo
     * @param requestJson
     * @author zhangzeyuan
     * @date 2021/3/24 16:09
     */
    private void packageNotifyBusinessParams(Integer orderType, String transNo, JSONObject requestJson) {
        //todo 返回通知参数修改
        HashMap<String, Object> queryParamMap = Maps.newHashMapWithExpectedSize(3);
        //POS订单
        if (orderType.equals(Constant.ORDER_TYPE_POS)) {
            //pos订单  查询pos_qr_pay_flow
            queryParamMap.put("transNo", transNo);
            queryParamMap.put("startIndex", Constant.POS_RECORD_DEFAULT_START_INDEX);
            queryParamMap.put("limit", Constant.POS_RECORD_DEFAULT_LIMIT);
            //实际只有一条
            List<PosTransactionRecordDTO> posInfo = posQrPayFlowService.listPosTransaction(queryParamMap);

            if (CollectionUtils.isNotEmpty(posInfo)) {
                //业务参数
                requestJson.put("data", posInfo.get(0));
            }
            return;
        }

        //普通订单
        if (orderType.equals(Constant.ORDER_TYPE_NORMAL)) {
            queryParamMap.put("transNo", transNo);
            QrPayFlowDTO oneQrPayFlow = qrPayFlowService.findOneQrPayFlow(queryParamMap);
            if (Objects.nonNull(oneQrPayFlow)) {
                JSONObject data = new JSONObject();
                data.put("payoTransNo", transNo);
                data.put("orderStatus", oneQrPayFlow.getState());
                data.put("transAmount", oneQrPayFlow.getTransAmount());
                data.put("saleType", oneQrPayFlow.getSaleType());
                data.put("platformFee", oneQrPayFlow.getPlatformFee().toString());
                data.put("payAmount", oneQrPayFlow.getPayAmount().toString());
                data.put("createDate", oneQrPayFlow.getCreatedDate().toString());
                //todo
                //订单支付时间
                //折扣金额
                requestJson.put("data", data);
            }
        }
    }

    /**
     * pos回调通知发送post请求
     *
     * @param url
     * @param json
     * @return boolean
     * @author zhangzeyuan
     * @date 2021/3/24 16:23
     */
    private boolean posNotifysendPost(String url, String json) {
        boolean result = false;
        //http调用
        try {
            String response = HttpClientUtilNew.sendJsonStrPost(url, json);
            if (StringUtils.isNotBlank(response)) {
                result = true;
            }
            //todo response转化json  取状态码  200 返回 true  其他返回false
        } catch (PosApiException e) {
            result = false;
        }
        return result;
    }


    /**
     * pos验签
     *
     * @param requestTimeStamp 请求时间
     * @param sysTimeStamp     系统时间
     * @param randomStr        随机字符串
     * @param posId            POS唯一标识
     * @param sign             加密后的签名
     * @return
     */
    private boolean verifyPosSign(Long requestTimeStamp, Long sysTimeStamp, String randomStr, String posId, String sign, Long merchantId) {
        if (Objects.isNull(requestTimeStamp) || Objects.isNull(sysTimeStamp) || Objects.isNull(merchantId)
                || StringUtils.isBlank(randomStr) || StringUtils.isBlank(randomStr) || StringUtils.isBlank(sign)) {
            return false;
        }
        //校验是否超出时间范围 15分钟
        boolean inDate = isInDate(requestTimeStamp, sysTimeStamp);
        if (!inDate) {
            return false;
        }
        //随机数位数校验
        int randomStrlength = randomStr.length();
        if (randomStrlength < Constant.POS_SIGN_RANDOM_LENGTH_MIN || randomStrlength > Constant.POS_SIGN_RANDOM_LENGTH_MAX) {
            return false;
        }
        //参数拼接 md5加密对比
        //根据商户ID查询出配置的秘钥信息 并对比 加密
        HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
        paramMap.put("merchantId", merchantId);
        PosSecretConfigDTO secretConfig = posSecretConfigService.findOnePosSecretConfig(paramMap);
        String privateSecret = secretConfig.getPrivateSecret();
        if (Objects.isNull(secretConfig) || StringUtils.isBlank(privateSecret)) {
            return false;
        }
        //判断过期时间
        Long expiredDate = secretConfig.getExpiredDate();
        long currentTimeMillis = System.currentTimeMillis();
        if (Objects.isNull(expiredDate) || expiredDate.longValue() < currentTimeMillis) {
            return false;
        }
        String paramsStr = merchantId + "&" + privateSecret + "&" + randomStr + "&" + requestTimeStamp;
        String md5Sign = MD5FY.MD5Encode(paramsStr);
        if (StringUtils.isBlank(md5Sign) || !md5Sign.equals(sign)) {
            return false;
        }
        return true;
    }


    /**
     * 校验POS机权限
     *
     * @param posId
     * @param merchantId
     * @return boolean
     * @author zhangzeyuan
     * @date 2021/3/24 16:23
     */
    private boolean checkPosAuthority(String posId, Long merchantId) {
        HashMap<String, Object> queryPosInfoMap = Maps.newHashMapWithExpectedSize(3);
        queryPosInfoMap.put("posId", posId);
        queryPosInfoMap.put("merchantId", merchantId);
        queryPosInfoMap.put("status", Constant.POS_RECORD_USABLE_STATUS);
        //todo 校验规则
        PosInfoDTO onePosInfo = posInfoService.findOnePosInfo(queryPosInfoMap);
        if (Objects.isNull(onePosInfo)) {
            return false;
        }
        return true;

    }

    /**
     * 创建POS支付订单
     *
     * @param requestInfo
     * @param request
     * @return boolean
     * @author zhangzeyuan
     * @date 2021/3/24 16:24
     */
    private boolean createQrPayFlowRecord(JSONObject requestInfo, HttpServletRequest request) throws PosApiException {
        //pos 订单号
        String posOrderNumber = requestInfo.getString("posTransNo");
        //系统订单号
        String paySysTransNo = requestInfo.getString("showThirdTransNo");
        //回调地址
        String notifyUrl = requestInfo.getString("notifyUrl");
        //金额
        BigDecimal transAmount = requestInfo.getBigDecimal("transAmount");
        //货币类型
        String currencyTypeu = requestInfo.getString("currencyType");
        //商户ID
        Long merchantId = requestInfo.getLong("merchantId");
        //pos id
        String posId = requestInfo.getString("posId");
        //qr 二维码信息
        String qrCode = requestInfo.getString("base64QrCode");

        //检验是否有重复订单信息
        //todo 先用redis去重，再查库校验 接口幂等性
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
        map.put("posTransNo", posOrderNumber);
        int count = posQrPayFlowService.count(map);
        if (count > 0) {
            return false;
        }
        //保存记录
        PosQrPayFlowDTO payFlow = new PosQrPayFlowDTO();
        payFlow.setMerchantId(merchantId);
        payFlow.setCurrencyType(currencyTypeu);
        payFlow.setNotifyUrl(notifyUrl);
        payFlow.setOrderStatus(StaticDataEnum.POS_ORDER_STATUS_CREATE_ORDER.getCode());
        payFlow.setPosId(posId);
        payFlow.setPosTransNo(posOrderNumber);
        payFlow.setShowThirdTransNo(paySysTransNo);
        payFlow.setQrCode(qrCode);
        payFlow.setTransAmount(transAmount);
        try {
            posQrPayFlowService.savePosQrPayFlow(payFlow, request);
        } catch (BizException e) {
            log.error("保存POS支付订单出错," + e.getMessage());
            return false;
        }
        return true;
    }


    /**
     * 生成 pos base64二维码信息
     *
     * @param transNo
     * @return java.lang.String
     * @author zhangzeyuan
     * @date 2021/3/24 16:24
     */
    private String createPosQrCode(Long transNo) throws PosApiException {
//        return QRCodeUtil.createQrCodeBase64(posQrCodeUrl + transNo, 300, 340);
        try {
            BufferedImage qrCode = QRCodeUtil.createQRCode(posQrCodeUrl + transNo, 300, 340, null);
            JSONObject object = QRCodeUtil.writeFile(qrCode, transNo.toString(), "D://QRCode//");
        } catch (Exception e) {

        }
        return "11";
    }

    /**
     * 判断是否在时间范围内
     *
     * @param requestTimestamp
     * @param sysTimeStamp
     * @return
     */
    private boolean isInDate(Long requestTimestamp, Long sysTimeStamp) {
        boolean result = false;
        if (Objects.isNull(requestTimestamp) || Objects.isNull(sysTimeStamp)) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, Constant.POS_REQUEST_EXPIRED_TIME_MIN);
        Long before15MinTimestamp = calendar.getTimeInMillis();
        if (requestTimestamp.compareTo(sysTimeStamp) <= 0 && requestTimestamp.compareTo(before15MinTimestamp) >= 0) {
            result = true;
        }
        return result;
    }

    /**
     * 获取POS订单历史交易信息
     *
     * @param requestInfo
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2021/3/23 14:27
     */
    @Override
    public JSONObject getPosTransactionRecord(JSONObject requestInfo, HttpServletRequest request) throws PosApiException {
        long sysTimestamp = System.currentTimeMillis();
        Long timestamp = requestInfo.getLong("timestamp");
        String random = requestInfo.getString("random");
        String posId = requestInfo.getString("posId");
        String sign = requestInfo.getString("sign");
        Long merchantId = requestInfo.getLong("merchantId");
        //验签
        boolean verifySignResult = this.verifyPosSign(timestamp, sysTimestamp, random, posId, sign, merchantId);
        if (!verifySignResult) {
            throw new PosApiException(PosApiCodeEnum.VERIFY_SIGN_ERROR);
        }
        //验证该商户POS是否有权限
        boolean posAuthorityResult = this.checkPosAuthority(posId, merchantId);
        if (!posAuthorityResult) {
            throw new PosApiException(PosApiCodeEnum.POS_NOT_REGISTERED);
        }
        //返回信息
        JSONObject result = new JSONObject(2);
        //分页处理
        Integer page = requestInfo.getInteger("page");
        Integer limit = requestInfo.getInteger("limit");

        if (Objects.isNull(page) || page.compareTo(0) <= 0) {
            page = Constant.POS_RECORD_DEFAULT_PAGE;
        }
        if (Objects.isNull(limit) || page.compareTo(0) <= 0 || page.compareTo(50) > 0) {
            limit = Constant.POS_RECORD_DEFAULT_LIMIT;
        }
        //封装查询参数
        HashMap<String, Object> queryParamsMap = Maps.newHashMapWithExpectedSize(5);
        queryParamsMap.put("startIndex", (page - 1) * limit);
        queryParamsMap.put("limit", limit);
        queryParamsMap.put("merchantId", merchantId);
        queryParamsMap.put("startTimeStamp", requestInfo.getLong("startTime"));
        queryParamsMap.put("endTimeStamp", requestInfo.getLong("endTime"));
        queryParamsMap.put("orderStatus", requestInfo.getLong("orderStatus"));
        queryParamsMap.put("posTransNo", requestInfo.getString("posTransNo"));

        Integer count = posQrPayFlowService.countPosTransaction(queryParamsMap);
        if (Objects.isNull(count) || count.compareTo(0) <= 0) {
            result.put("count", count);
            result.put("list", Collections.EMPTY_LIST);
            return result;
        }
        //todo 折扣金额 ？
        List<PosTransactionRecordDTO> posQrPayFlowDTOS = posQrPayFlowService.listPosTransaction(queryParamsMap);
        result.put("count", count);
        result.put("list", posQrPayFlowDTOS);

        return result;
    }

}
