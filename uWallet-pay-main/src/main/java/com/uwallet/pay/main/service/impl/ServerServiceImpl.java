package com.uwallet.pay.main.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.*;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.WithholdFlowDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.FireBaseUtil;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.smartcardio.Card;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author baixinyue
 * @description 调用外部系统业务层
 * @createDate 2019/12/16
 */

@Service
@Slf4j
public class ServerServiceImpl extends BaseServiceImpl implements ServerService {

    @Value("${uWallet.account}")
    private String accountUrl;

    @Value("${uWallet.credit}")
    private String creditMerchantUrl;

    @Value("${uWallet.data}")
    private String dataUrl;

    @Value("${uWallet.invest}")
    private String investUrl;

    @Value("${uWallet.risk}")
    private String riskUrl;

    @Value("${spring.pushFirebaseFilePath}")
    private String pushFirebaseFilePath;

    @Value("${spring.pushFirebaseUrl}")
    private String pushFirebaseUrl;

    @Value("${spring.pushFirebaseFilePathForMerchant}")
    private String pushFirebaseFilePathForMerchant;

    @Value("${spring.pushFirebaseUrlForMerchant}")
    private String pushFirebaseUrlForMerchant;

    @Autowired
    @Lazy
    private UserService userService;

    @Resource
    private RefundFlowService refundFlowService;

    /**
     * 支付系统
     */
    @Value("${engine.app-id}")
    private String appId;

    /**
     * 分期付系统kyc
     */
    @Value("${engine.value}")
    private String value;


    @Value("${uWallet.sysEmail}")
    private String sysEmail;

    @Value("${uWallet.sysEmailPwd}")
    private String sysEmailPwd;


    @Autowired
    RedisUtils redisUtils;

    @Autowired
    private UserStepService userStepService;


    @Autowired
    @Lazy
    private LatPayService latPayService;

    @Autowired
    @Lazy
    private SplitService splitService;

    @Autowired
    private GatewayService gatewayService;

    @Resource
    private WithholdFlowDAO withholdFlowDAO;
    @Autowired
    private ChannelFeeConfigService channelFeeConfigService;

    @Resource
    private WithholdFlowService withholdFlowService;

    @Value("${latpay.merchantRefundId}")
    private String merchantRefundId;

    @Value("${latpay.merchantRefundPassword}")
    private String merchantRefundPassword;


    /**
     * 同步用户信息 支付 账户 分期付 firstName lastName birthDay birth
     **/
    @Override
    public void infoSupplement(JSONObject userInfo, HttpServletRequest request) throws Exception {
        //查询用户
        //todo 直接getLong()
        UserDTO user = userService.findUserById(Long.valueOf(userInfo.getString("userId")));
        if(user==null || user.getId() == null){
            //查询无此用户
            throw new BizException(I18nUtils.get("query.failed", getLang(request)));
        }
        // 修改用户姓名首字母大写
        String userLastName = userInfo.getString("userLastName");
        String userFirstName = userInfo.getString("userFirstName");
        log.info("转换用户姓名之前,userLastName:{},userFirstName:{}",userLastName,userFirstName);
        if (!StringUtils.isAllBlank(userLastName,userLastName)) {
            String trimLast = userLastName.trim();
            if (trimLast.length() > 0) {
                String trim = trimLast.trim();
                userLastName = trim.substring(0, 1).toUpperCase() + trim.substring(1);
            }
            String trimFirst = userFirstName.trim();
            if (trimFirst.length() > 0) {
                String trimStr = trimFirst.trim();
                userFirstName = trimStr.substring(0, 1).toUpperCase() + trimStr.substring(1);
            }
            userInfo.put("userLastName", userLastName);
            userInfo.put("userFirstName", userFirstName);
            log.info("转换用户姓名之后,userLastName:{},userFirstName:{}", userLastName, userFirstName);
        }
        //向账户系统同步信息
        String infoSupplementUrl = accountUrl + "/server/replenishUser";
        String data = HttpClientUtils.sendPost(infoSupplementUrl, userInfo.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        if (returnMsg.getString(Constant.CODE).equals(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode())) {
            log.info("info supplement faile, data:{}, error message:{}", userInfo, returnMsg.getString("message"));
            throw new BizException(I18nUtils.get("incorrect.information", getLang(request)));
        }
    }

    @Override
    public JSONObject getAccountInfo(Long userId) throws Exception {
        String getCardListUrl = accountUrl + "/server/selectAccount";
        JSONObject sendData = new JSONObject();
        sendData.put("userId", userId);
        String data = HttpClientUtils.sendPost(getCardListUrl, sendData.toJSONString());
        JSONObject msg = JSONObject.parseObject(data);
        if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(msg.getString("message"));
        }
        return msg.getJSONObject("data");
    }
    @Override
    public JSONObject getAccountInfoJson(Long userId,Integer cardType) throws Exception {
        String getCardListUrl = accountUrl + "/server/selectAccount";
        JSONObject sendData = new JSONObject();
        sendData.put("userId", userId);
        sendData.put("type",cardType);
        String data = HttpClientUtils.sendPost(getCardListUrl, sendData.toJSONString());
        JSONObject msg = JSONObject.parseObject(data);
        if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(msg.getString("message"));
        }
        return msg.getJSONObject("data");
    }

    @Override
    public void saveAccount(JSONObject accountInfo) throws Exception {
        String openAccount = accountUrl + "/server";
        String data = HttpClientUtils.sendPost(openAccount, accountInfo.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
    }

    @Override
    public Long tieOnCard(JSONObject cardInfo) throws Exception {
        String tieOnCardUrl = accountUrl + "/server/tieOnCard";
        String data = HttpClientUtils.sendPost(tieOnCardUrl, cardInfo.toJSONString());
        JSONObject msg = JSONObject.parseObject(data);
        if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(msg.getString("message"));
        }
        return msg.getLong("data");
    }

    @Override
    public JSONObject getCardInfo(Long cardId) throws Exception {
        String getCardInfoUrl =  accountUrl + "/server/findCard/" + cardId;
        JSONObject msg = JSONObject.parseObject(HttpClientUtils.sendPost(getCardInfoUrl, null));
        if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(msg.getString("message"));
        }
        return msg.getJSONObject("data");
    }


    @Override
    public JSONObject getCardNoAndTypeByCardId(Long cardId) throws Exception {
        String getCardInfoUrl =  accountUrl + "/server/getCardNoAndTypeByCardId/" + cardId;
        JSONObject msg = JSONObject.parseObject(HttpClientUtils.sendPost(getCardInfoUrl, null));
        if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(msg.getString("message"));
        }
        return msg.getJSONObject("data");
    }

    @Override
    public JSONObject userInfoByQRCode(Long userId) throws Exception {
        String infoUrl = accountUrl + "/server/findOne";
        JSONObject sendData = new JSONObject();
        sendData.put("userId", userId);
        String data = HttpClientUtils.sendPost(infoUrl, JSONObject.toJSONString(sendData));
        JSONObject msg = JSONObject.parseObject(data);
        if (msg.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(msg.getString("message"));
        }
        JSONObject userInfo = msg.getJSONObject("data");
        return userInfo;
    }

    @Override
    public JSONObject amountIn(JSONObject amountIn) throws Exception {

        String amountInUrl = accountUrl + "/server/amountIn";
        log.info("do amountIn request url:"+amountInUrl+",data："+amountIn.toJSONString());
        String data = HttpClientUtils.sendPost(amountInUrl, amountIn.toJSONString());
        log.info("do amountIn response data："+data);
        JSONObject msg = JSONObject.parseObject(data);
        if (msg.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(msg.getString("message"));
        }
        return msg;
    }

    @Override
    public JSONObject accountTransfer(JSONObject transInfo) {
        String tieOnCardUrl = accountUrl + "/server/accountTransfer";
        JSONObject msg = null;
        try{
            String data = HttpClientUtils.sendPost(tieOnCardUrl, transInfo.toJSONString());
            msg = JSONObject.parseObject(data);
            if (msg.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
                throw new BizException(msg.getString("message"));
            }
        }catch(Exception e){
            return null;
        }
        return msg;
    }

    @Override
    public JSONObject transactionInfo(String channelSerialNumber) throws Exception {
        String trsactionUrl = accountUrl + "/server/findOneTransactionFlow";
        JSONObject sendData = new JSONObject();
        sendData.put("channelSerialnumber", channelSerialNumber);
        String data = HttpClientUtils.sendPost(trsactionUrl, JSONObject.toJSONString(sendData));
        JSONObject msg = JSONObject.parseObject(data);
        if (msg.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(msg.getString("message"));
        }
        JSONObject transactionInfo = msg.getJSONObject("data");
        return transactionInfo;
    }

    @Override
    public void saveMerchant(@NonNull JSONObject jsonObject) throws BizException {
        StringBuilder url = new StringBuilder(creditMerchantUrl).append("/payremote/addmerchant");
        String result;
        try {
            result = HttpClientUtils.sendPost(url.toString(), jsonObject.toJSONString());
        } catch (Exception e) {
            throw new BizException();
        }
        if (StringUtils.isBlank(result)) {
            throw new BizException();
        }
        JSONObject json = JSONObject.parseObject(result);
        if (!ErrorCodeEnum.SUCCESS_CODE.getCode().equals(json.getString("code"))) {
            throw new BizException();
        }
    }

    @Override
    public JSONObject getMerchantByMerchantId(Long merchantId) {
        StringBuilder url = new StringBuilder(creditMerchantUrl).append("/payremote/findOne/").append(merchantId);
        String result;
        try {
            result = HttpClientUtils.sendPost(url.toString(), new JSONObject().toJSONString());
        } catch (Exception e) {
            return null;
        }
        if (result == null) {
            return null;
        }
        JSONObject json = JSONObject.parseObject(result);
        if (!ErrorCodeEnum.SUCCESS_CODE.getCode().equals(json.getString("code"))) {
            return null;
        }
        return json;
    }

    @Override
    public void updateMerchant(@NonNull Long id, @NonNull JSONObject jsonObject, HttpServletRequest request) throws BizException {
        StringBuilder url = new StringBuilder(creditMerchantUrl).append("/payremote/updateMerchant/").append(id);
        String result;
        try {
            result = HttpClientUtils.sendPut(url.toString(), jsonObject.toJSONString());
        } catch (Exception e) {
            throw new BizException();
        }
        if (StringUtils.isBlank(result)) {
            throw new BizException();
        }
        JSONObject json = JSONObject.parseObject(result);
        if (!ErrorCodeEnum.SUCCESS_CODE.getCode().equals(json.getString("code"))) {
            if (request != null) {
                throw new BizException(I18nUtils.get("credit.system.update.error", getLang(request)));
            }
        }
    }

    @Override
    public Object selectAccountUser(Long id, HttpServletRequest request) throws Exception {
        String getAccountUserInfoUrl =  accountUrl + "/server/findOne";
        JSONObject sendData = new JSONObject();
        sendData.put("userId", id);
        String value = HttpClientUtils.sendPost(getAccountUserInfoUrl, JSONObject.toJSONString(sendData));
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(value)) {
            throw new BizException();
        }
        JSONObject jsonObject = JSONObject.parseObject(value);
        JSONObject cardInfo = jsonObject.getJSONObject("data");
        return cardInfo;
    }

    @Override
    public Object selectUserAndCard(@NonNull JSONObject accountInfo) throws Exception {
        String getUserAndCardInfoUrl = accountUrl + "/server/userAndCardInfo";
        String value = HttpClientUtils.sendPost(getUserAndCardInfoUrl,accountInfo.toJSONString());
        JSONObject jsonObject = JSONObject.parseObject(value);
        if (jsonObject.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(jsonObject.getString("message"));
        }
        return jsonObject;
    }

    @Override
    public JSONArray selectAccountCard(Long id, HttpServletRequest request) throws Exception {
        String getAccountCardInfoUrl =  accountUrl + "/server/cardInfo/" + id;
        String value = HttpClientUtils.sendPost(getAccountCardInfoUrl, null);
        JSONObject jsonObject = JSONObject.parseObject(value);
        if (jsonObject.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(jsonObject.getString("message"));
        }
        JSONArray cardInfo = jsonObject.getJSONArray("data");
        return cardInfo;
    }

    @Override
    public JSONObject amountOut(@NonNull JSONObject amountOut) throws Exception {
        String amountInUrl = accountUrl + "/server/amountOut";
        String data = HttpClientUtils.sendPost(amountInUrl, amountOut.toJSONString());
        JSONObject msg = JSONObject.parseObject(data);
        if (msg == null || msg.getString("code") == null ) {
            throw new BizException(msg.getString("message"));
        }
        return msg;
    }
//    @Async("taskExecutor")
    @Override
    public void pushFirebase(@NonNull FirebaseDTO firebaseDTO,HttpServletRequest request) throws Exception {
        //设置Java代理,端口号是代理软件开放的端口，这个很重要。
        System.setProperty("proxyHost", "localhost");
        System.setProperty("proxyPort", "1080");
        //如果FirebaseApp没有初始化
        UserDTO userDTO = userService.findUserById(firebaseDTO.getUserId());
        // 推送消息
        Map<String, String> data = new HashMap<>(8);
        firebaseDTO.setToken(userDTO.getPushToken());
        if (userDTO.getUserType().equals(StaticDataEnum.USER_TYPE_20.getCode())) {
            firebaseDTO.setAppName(StaticDataEnum.U_BIZ.getMessage());
        } else {
            firebaseDTO.setAppName(StaticDataEnum.U_WALLET.getMessage());
        }
        if (!FireBaseUtil.isInit(firebaseDTO.getAppName())) {
            if (userDTO.getUserType().equals(StaticDataEnum.USER_TYPE_20.getCode())) {
                FireBaseUtil.initSDK(pushFirebaseFilePathForMerchant, pushFirebaseUrlForMerchant, firebaseDTO.getAppName());
            } else {
                FireBaseUtil.initSDK(pushFirebaseFilePath, pushFirebaseUrl, firebaseDTO.getAppName());
            }
        }
        // 单推
        log.info("push token final data:{}", firebaseDTO);
        if (firebaseDTO.getVoice().equals(StaticDataEnum.VOICE_1.getCode())) {
            data.put("title", firebaseDTO.getTitle());
            data.put("body", firebaseDTO.getBody());
        }
        data.put("merchantId", userDTO.getMerchantId() != null ? userDTO.getMerchantId().toString(): "");
        data.put("voice", firebaseDTO.getVoice() != null ? firebaseDTO.getVoice().toString() : "0");
        data.put("route", firebaseDTO.getRoute() != null ? firebaseDTO.getRoute().toString() : "0");
        FireBaseUtil.pushSingle(firebaseDTO.getAppName(), firebaseDTO.getToken(), firebaseDTO.getTitle(), firebaseDTO.getBody(), data);
    }

    @Override
    public void pushFirebaseList(@NonNull FirebaseDTO firebaseDTO,HttpServletRequest request) throws Exception {
        List<String> tokens = firebaseDTO.getTokens();
        String appName = firebaseDTO.getAppName();
        String topic = firebaseDTO.getTopic();
        String title = firebaseDTO.getTitle();
        String body = firebaseDTO.getBody();
        //设置Java代理,端口号是代理软件开放的端口，这个很重要。
        System.setProperty("proxyHost", "localhost");
        System.setProperty("proxyPort", "1080");
        //如果FirebaseApp没有初始化
        if(!FireBaseUtil.isInit(appName)) {
            //初始化FirebaseApp
            FireBaseUtil.initSDK(pushFirebaseFilePath, pushFirebaseUrl, appName);
        }
        // 设置主题
        FireBaseUtil.registrationTopic(appName, tokens, topic);

        // 按主题推送
        FireBaseUtil.sendTopicMes(appName, topic, title, body);

        // 取消主题
        FireBaseUtil.cancelTopic(appName, tokens, topic);
    }

    @Override
    public void updateParametersConfig(@NonNull JSONObject amountOut) throws Exception {
        String amountInUrl = creditMerchantUrl + "/payremote/modifiedConfig";
        try {
            HttpClientUtils.sendPut(amountInUrl, amountOut.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException();
        }
    }

    @Override
    public JSONObject findInvestProducts() throws Exception {
//        JSONObject params = new JSONObject();
//        String findInvestProductUrl = investUrl + "/invest/SimWithImInfoList?state=1";
//        String data = HttpClientUtils.sendPost(findInvestProductUrl, params.toJSONString());
//        JSONObject msg = JSONObject.parseObject(data);
//        if (msg.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
//            throw new BizException(msg.getString("message"));
//        }
//        return msg;
        return  null;
    }

    @Override
    public JSONObject findInvestProductInfo(Long productId) throws Exception {
//        String findInvestProductUrl = investUrl + "invest/financialDetails";
//        JSONObject params = new JSONObject();
//        params.put("id", productId);
//        String data = HttpClientUtils.sendPost(findInvestProductUrl, params.toJSONString());
//        JSONObject msg = JSONObject.parseObject(data);
//        if (msg.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
//            throw new BizException(msg.getString("message"));
//        }
//        return msg.getJSONObject("data");
        return null;
    }

    @Override
    public JSONArray sendNoticeUser(JSONObject params) throws Exception {
        String findInvestProductUrl = accountUrl + "/server/sendNoticeUser";
        String data = HttpClientUtils.sendPost(findInvestProductUrl, params.toJSONString());
        JSONObject msg = JSONObject.parseObject(data);
        if (msg.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(msg.getString("message"));
        }
        JSONArray array = msg.getJSONArray("data");
        return array;
    }

    /**
     * 修改其它系统手机号
     * @param updateInfo 要更新的参数 [userId:1,phone:手机号]
     * @throws Exception
     */
    @Override
    public void updatePhone(JSONObject updateInfo) throws Exception {
        log.info("update user phone, user info:{}", updateInfo);
        String jsonString = updateInfo.toJSONString();
        // 账户系统
        String updatePhoneAccountUrl = accountUrl + "/server/updatePhone";
        String data = HttpClientUtils.sendPost(updatePhoneAccountUrl, jsonString);
        JSONObject msg = JSONObject.parseObject(data);
        if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(msg.getString("message"));
        }
//        // 理财系统
//        String updatePhoneInvesUrl = investUrl + "/server/updatePhone";
//        data = HttpClientUtils.sendPost(updatePhoneInvesUrl, jsonString);
//        msg = JSONObject.parseObject(data);
//        if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
//            throw new BizException(msg.getString("message"));
//        }
        // 分期付系统
        String updatePhoneCreditUrl = creditMerchantUrl + "/payremote/updatePhone";
        data = HttpClientUtils.sendPost(updatePhoneCreditUrl, updateInfo.toJSONString());
        msg = JSONObject.parseObject(data);
        if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(msg.getString("message"));
        }
    }

    @Override
    public void transTypeToCredit(JSONObject params) throws Exception {
        String creditUrl = creditMerchantUrl + "/payremote/transactionResultNotify";
        String data = HttpClientUtils.sendPost(creditUrl, params.toJSONString());

        JSONObject msg = JSONObject.parseObject(data);
        if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(msg.getString("message"));
        }
    }

    @Override
    public JSONObject bankLogoList(JSONObject params) throws Exception {
        String bankLogoListUrl = accountUrl + "/server/bankLogoList";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(bankLogoListUrl, params.toJSONString()));
        if (result.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(result.getString("message"));
        }
        return result;
    }

    @Override
    public JSONObject getBankLogoList() throws Exception {
        String bankLogoListUrl = accountUrl + "/server/getBankLogoList";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(bankLogoListUrl, null));
        if (result.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(result.getString("message"));
        }
        return result;
    }

    @Override
    public JSONObject bankLogoInfo(Long id) throws Exception {
        String bankLogoInfo =  accountUrl + "/server/bankLogo/" + id;
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(bankLogoInfo, null));
        if (result.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(result.getString("message"));
        }
        return result;
    }

    @Override
    public void bankLogoSave(JSONObject params) throws Exception {
        String bankLogoSave = accountUrl + "/server/bankLogoCreate";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(bankLogoSave, params.toJSONString()));
        if (result.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(result.getString("message"));
        }
    }

    @Override
    public void updateBankLogo(Long id, JSONObject params) throws Exception {
        String updateBankLogo = accountUrl + "/server/bankLogoUpdate/" + id;
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(updateBankLogo, params.toJSONString()));
        if (result.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(result.getString("message"));
        }
    }

    @Override
    public void bankLogoDelete(Long id) throws Exception {
        String bankLogoDeleteUrl = accountUrl + "/server/bankLogoDelete/" + id;
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(bankLogoDeleteUrl, null));
        if (result.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(result.getString("message"));
        }
    }

    @Override
    public void cardUnbundling(JSONObject unBundlingInfo) throws Exception {
        String cardUnbundlingUrl = accountUrl + "/server/cardUnbundling";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(cardUnbundlingUrl, unBundlingInfo.toJSONString()));
        if (result.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(result.getString("message"));
        }
    }
    @Override
    public void cardInfoUpdate(JSONObject cardInfoUpdateInfo) throws Exception {
        String cardInfoUpdateUrl = accountUrl + "/server/cardInfoUpdate";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(cardInfoUpdateUrl, cardInfoUpdateInfo.toJSONString()));
        if (result.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(result.getString("message"));
        }
    }
    @Override
    public void cardInfoUpdateTemp(JSONObject cardInfoUpdateInfo) throws Exception {
        String cardInfoUpdateUrl = accountUrl + "/server/cardInfoUpdateTemp";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(cardInfoUpdateUrl, cardInfoUpdateInfo.toJSONString()));
        if (result.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(result.getString("message"));
        }
    }

    @Override
    public JSONObject findInvestCardInfo(Long userId, String cardNo) throws Exception {
//        String findInvestCardInfoUrl = investUrl + "/server/findOneCardInfo";
//        JSONObject searchInfo = new JSONObject();
//        searchInfo.put("accountNo", cardNo);
//        searchInfo.put("userId", userId);
//        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(findInvestCardInfoUrl, searchInfo.toJSONString()));
//        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(result.getString("code"))) {
//            throw new BizException(result.getString("message"));
//        }
//        return result.getJSONObject("data");
        return null;
    }

    @Override
    public JSONObject findCreditUserInfo(JSONObject requestInfo) throws Exception {
        String findCreditUserInfoUrl = creditMerchantUrl + "/payremote/findUser";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(findCreditUserInfoUrl, requestInfo.toJSONString()));
        if (result.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(result.getString("message"));
        }
        return result.getJSONObject("data");
    }

    @Override
    public JSONObject findCreditUserDetail(JSONObject requestInfo) throws Exception {
        String findCreditUserInfoUrl = creditMerchantUrl + "/payremote/getCreditUserDetail";
//        String findCreditUserInfoUrl = "http://localhost:6030" + "/payremote/getCreditUserDetail";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(findCreditUserInfoUrl, requestInfo.toJSONString()));
        if (result.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(result.getString("message"));
        }
        return result.getJSONObject("data");
    }


    @Override
    public JSONObject findOneUserInfo(JSONObject requestInfo) throws Exception {
        String findOneUserInfoUrl = accountUrl + "/server/findOneUser";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(findOneUserInfoUrl, requestInfo.toJSONString()));
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(result.getString("code"))) {
            throw new BizException(result.getString("message"));
        }
        return result.getJSONObject("data");
    }

    @Override
    public JSONArray findRiskLog(String batchNo) throws Exception {
        JSONObject requestInfo = new JSONObject();
        requestInfo.put("batchNo", batchNo);
        String findRiskLogUrl = riskUrl + "/logsNew";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.post(findRiskLogUrl, EncryptUtil.encrypt(requestInfo.toJSONString())));
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(result.getString("code"))) {
            throw new BizException(result.getString("message"));
        }
        JSONArray dataArray = JSONArray.parseArray(EncryptUtil.decrypt(result.getString("data"), EncryptUtil.aesKey, EncryptUtil.aesIv));
        return dataArray;
    }

    @Override
    public JSONObject findInstallmentRiskLog(Long userId) throws Exception {
        JSONObject requestInfo = new JSONObject();
        requestInfo.put("userId", userId);
        String findInstallmentRiskLogUrl = creditMerchantUrl + "/payremote/user/riskInfo";
//        String findInstallmentRiskLogUrl = "http://localhost:6030/payremote/user/riskInfo";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(findInstallmentRiskLogUrl, requestInfo.toJSONString()));
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(result.getString("code"))) {
            return null;
        }
        return result.getJSONObject("data");
    }

    @Override
    public void installmentRecertification(Long userId) throws Exception {
        JSONObject requestInfo = new JSONObject();
        requestInfo.put("userId", userId);
        String installmentRecertificationUrl = creditMerchantUrl + "/payremote/user/review";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(installmentRecertificationUrl, requestInfo.toJSONString()));
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(result.getString("code"))) {
            throw new BizException(result.getString("message"));
        }
    }

    @Override
    public void updateEmail(JSONObject updateInfo) throws Exception {
        log.info("update user email, user info:{}", updateInfo);
        String jsonString = updateInfo.toJSONString();
        // 账户系统
        String updatePhoneAccountUrl = accountUrl + "/server/updateEmail";
        String data = HttpClientUtils.sendPost(updatePhoneAccountUrl, jsonString);
        JSONObject msg = JSONObject.parseObject(data);
        if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(msg.getString("message"));
        }
//        // TODO 理财系统
//        String updatePhoneInvesUrl = investUrl + "/server/updateEmail";
//        data = HttpClientUtils.sendPost(updatePhoneInvesUrl, jsonString);
//        msg = JSONObject.parseObject(data);
//        if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
//            throw new BizException(msg.getString("message"));
//        }
//        // TODO分期付系统
//        String updatePhoneCreditUrl = creditMerchantUrl + "/payremote/updateEmail";
//        data = HttpClientUtils.sendPost(updatePhoneCreditUrl, updateInfo.toJSONString());
//        msg = JSONObject.parseObject(data);
//        if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
//            throw new BizException(msg.getString("message"));
//        }

    }

    @Override
    public void installmentAuditNotice(JSONObject result, HttpServletRequest request) throws Exception {
        // 保存分期付审核通知
        Long userId = result.getLong("userId");
        Integer type = result.getInteger("type");
        Integer state = result.getInteger("state");
        String kycInfo = result.getString("kycInfo");
        String reason = result.getString("reason");
        String batchNo = result.getString("batchNo");
        if (StaticDataEnum.USER_STEP_1.getCode() == type) {
            if (StaticDataEnum.USER_STEP_STATE_1.getCode() ==  state.intValue()) {
                userStepService.userStepModifyAndUserStepLogSave(userId, StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_11.getCode(), reason, batchNo, kycInfo, request);
            } else if (StaticDataEnum.USER_STEP_STATE_2.getCode() ==  state.intValue()) {
                userStepService.userStepModifyAndUserStepLogSave(userId, StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_12.getCode(), reason, batchNo, kycInfo, request);
            } else if (StaticDataEnum.USER_STEP_STATE_3.getCode() ==  state.intValue()) {
                userStepService.userStepModifyAndUserStepLogSave(userId, StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_13.getCode(), reason, batchNo, kycInfo, request);
            }
        } else if (StaticDataEnum.USER_STEP_2.getCode() == type) {
            if (StaticDataEnum.USER_STEP_STATE_1.getCode() ==  state.intValue()) {
                userStepService.userStepModifyAndUserStepLogSave(userId, StaticDataEnum.USER_STEP_2.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_21.getCode(), reason, batchNo, null, request);
            } else {
                userStepService.userStepModifyAndUserStepLogSave(userId, StaticDataEnum.USER_STEP_2.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_22.getCode(), reason, batchNo, null, request);
            }
        } else if (StaticDataEnum.USER_STEP_3.getCode() == type) {
            if (StaticDataEnum.USER_STEP_STATE_1.getCode() ==  state.intValue()) {
                userStepService.userStepModifyAndUserStepLogSave(userId, StaticDataEnum.USER_STEP_3.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_31.getCode(), reason, batchNo, null, request);
            } else if (StaticDataEnum.USER_STEP_STATE_2.getCode() ==  state.intValue()) {
                userStepService.userStepModifyAndUserStepLogSave(userId, StaticDataEnum.USER_STEP_3.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_32.getCode(), reason, batchNo, null, request);
            } else if (StaticDataEnum.USER_STEP_STATE_3.getCode() ==  state.intValue()) {
                userStepService.userStepModifyAndUserStepLogSave(userId, StaticDataEnum.USER_STEP_3.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_33.getCode(), reason, batchNo, null, request);
            } else {
                userStepService.userStepModifyAndUserStepLogSave(userId, StaticDataEnum.USER_STEP_3.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_33.getCode(), reason, batchNo, null, request);
            }
        }
    }

    @Override
    public JSONObject apiCreditOrder(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        log.info("api credit order, data:{}", requestInfo);
        String apiCreditOrderUrl = creditMerchantUrl + "/payremote/user/createBorrow";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(apiCreditOrderUrl, requestInfo.toJSONString()));
        log.info("api credit order, result:{}", result);
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(result.getString("code"))) {
            throw new BizException(result.getString("message"));
        }
        return result.getJSONObject("data");
    }

    @Override
    public JSONObject apiCreditOrderOld(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        log.info("api credit order, data:{}", requestInfo);
        String apiCreditOrderUrl = creditMerchantUrl + "/payremote/user/createBorrowOld";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(apiCreditOrderUrl, requestInfo.toJSONString()));
        log.info("api credit order, result:{}", result);
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(result.getString("code"))) {
            throw new BizException(result.getString("message"));
        }
        return result.getJSONObject("data");
    }

    @Override
    public JSONObject apiCreditOrderSearch(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        log.info("api credit order search, data:{}", requestInfo);
        String apiCreditOrderUrl = creditMerchantUrl + "/payremote/user/transactionRecord";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(apiCreditOrderUrl, requestInfo.toJSONString()));
        log.info("api credit order search, result:{}", result);
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(result.getString("code"))) {
            throw new BizException(result.getString("message"));
        }
        return result.getJSONObject("data");
    }

    @Override
    public JSONObject userInfoCredit(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        log.info("user info credit, data:{}", requestInfo);
        String apiCreditOrderUrl = creditMerchantUrl + "/payremote/user/userInfoCredit";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(apiCreditOrderUrl, requestInfo.toJSONString()));
        log.info("user info credit, result:{}", result);
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(result.getString("code"))) {
            throw new BizException(result.getString("message"));
        }
        return result.getJSONObject("data");
    }

    @Override
    public JSONObject apiUserVerify(HttpServletRequest request) throws Exception {
        JSONObject requestInfo = new JSONObject();
        requestInfo.put("userId", getApiUserId(request));
        log.info("api user verify, data:{}", requestInfo);
        String apiCreditOrderUrl = creditMerchantUrl + "/payremote/user/verify";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(apiCreditOrderUrl, requestInfo.toJSONString()));
        log.info("api user verify, result:{}", result);
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(result.getString("code"))) {
            throw new BizException(result.getString("message"));
        }
        return result.getJSONObject("data");
    }

    @Override
    public JSONObject activationInstallment(HttpServletRequest request) throws Exception {
        JSONObject requestInfo = new JSONObject();
        requestInfo.put("userId", getApiUserId(request));
        log.info("activationInstallment, data:{}", requestInfo);
        String apiCreditOrderUrl = creditMerchantUrl + "/payremote/user/activationInstallment";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(apiCreditOrderUrl, requestInfo.toJSONString()));
        log.info("activationInstallment, result:{}", result);
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(result.getString("code"))) {
            throw new BizException(result.getString("message"));
        }
        return result.getJSONObject("data");
    }

    @Override
    public JSONObject creditTieOnCard(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        log.info("credit tie on card, data:{}", requestInfo);
        requestInfo.put("userId", getApiUserId(request));
        String apiCreditOrderUrl = creditMerchantUrl + "/payremote/user/bankcardBinding";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(apiCreditOrderUrl, requestInfo.toJSONString()));
        log.info("credit tie on card, result:{}", result);
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(result.getString("code"))) {
            throw new BizException(result.getString("message"));
        }
        return result.getJSONObject("data");
    }

    @Override
    public void orderStateRollback(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        log.info("order state roll back, data:{}", requestInfo);
        String apiCreditOrderUrl = creditMerchantUrl + "/payremote/user/orderStateRollback";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(apiCreditOrderUrl, requestInfo.toJSONString()));
        log.info("order state roll back, result:{}", result);
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(result.getString("code"))) {
            throw new BizException(result.getString("message"));
        }
    }

    @Override
    public void createSubAccount(JSONObject accountInfo) throws Exception {
        String openAccount = accountUrl + "/server/createSubAccount";
        String data = HttpClientUtils.sendPost(openAccount, accountInfo.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
    }

    @Override
    public JSONObject creditSystemRepayment(JSONObject requestData, HttpServletRequest request) throws Exception {
        if (requestData.getJSONObject("userInfo") == null || requestData.getJSONObject("cardInfo") == null || (requestData.getBigDecimal("amount") != null && requestData.getBigDecimal("amount").compareTo(new BigDecimal("0")) == 0)) {
            log.info("direct debit transaction failed, data:{}", requestData);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        Map<String, Object> params = new HashMap<>(16);
        params.put("gatewayType", StaticDataEnum.PAY_TYPE_4.getCode());
        params.put("state", StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        JSONObject result = null;
        if (gatewayDTO.getType().intValue() == StaticDataEnum.GATEWAY_TYPE_3.getCode()) {
            result = latPayService.thirdSystem(requestData, request);
        } else if (gatewayDTO.getType().intValue() == StaticDataEnum.GATEWAY_TYPE_6.getCode()) {
            result = splitService.splitPayRequestInfo(requestData, request);
        }
        return result;
    }
    @Override
    public JSONObject getCardByMessage(JSONObject info) throws Exception {
        String getCardByMessageUrl = accountUrl + "/server/getCardByMessage";
        String data = HttpClientUtils.sendPost(getCardByMessageUrl, info.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
        return  returnMsg.getJSONObject("data");
    }

    @Override
    public void cardMessageModify(JSONObject info) throws Exception {
        String cardMessageModifyUrl = accountUrl + "/server/cardMessageModify";
        String data = HttpClientUtils.sendPost(cardMessageModifyUrl, info.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
    }

    @Override
    public void appCreditTieOnCard(JSONObject requestInfo) throws Exception {
        String creditOrderUrl = creditMerchantUrl + "/payremote/user/bankcardBinding";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(creditOrderUrl, requestInfo.toJSONString()));
        log.info("app credit tie on card, result:{}", result);
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(result.getString("code"))) {
            throw new BizException(result.getString("message"));
        }
    }

    @Override
    public JSONArray getSubAccountBalanceList(JSONObject requestData) throws Exception {
        String url = accountUrl + "/server/getSubAccountBalanceList";
        log.info("getSubAccountBalanceList, data:{}", requestData);
        String data = HttpClientUtils.sendPost(url, requestData.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        log.info("getSubAccountBalanceList, result:{}", returnMsg);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
        return  returnMsg.getJSONArray("data");
    }

    @Override
    public JSONObject batchChangeBalance(JSONObject requestData) throws Exception {
        String url = accountUrl + "/server/batchChangeBalance";
        log.info("batchChangeBalance, data:{}", requestData);
        String data = HttpClientUtils.sendPost(url, requestData.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        log.info("batchChangeBalance, result:{}", returnMsg);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            //如果是交易失败，返回信息和特殊code
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("errorState","2");
            jsonObject.put("message",returnMsg.getString("message"));
            return jsonObject;
        }
        return  returnMsg.getJSONObject("data");
    }

    @Override
    public JSONArray getRepayList(String userId, String borrowId) throws Exception {
        JSONObject queryParam = new JSONObject(4);
        queryParam.put("userId",userId);
        queryParam.put("borrowId",borrowId);
        //请求参数 userId, qr_pay_flow 的 creditOrderNo 字段,封装到 borrowId 字段
        String url = creditMerchantUrl + "/payremote/user/repayList";
        log.info("请求分期付,获取分期付还款计划, data:{}", queryParam);
        String data = HttpClientUtils.sendPost(url, queryParam.toJSONString());
        log.info("请求分期付,获取分期付还款计划, result:{}", data);
        JSONObject returnMsg = JSONObject.parseObject(data);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
        return  returnMsg.getJSONArray("data");
    }

    @Override
    public JSONArray getBorrowList(List<Long> borrowIdList, int isExcel) throws Exception {
        JSONObject queryParam = new JSONObject(4);
        queryParam.put("borrowIdList",borrowIdList);
        queryParam.put("isExcel", isExcel);
        String url = creditMerchantUrl + "/payremote/user/borrowList";
        log.info("批量获取分期付订单 borrow list, data:{}", queryParam);
        String data = HttpClientUtils.sendPost(url, queryParam.toJSONString());
        log.info("批量获取分期付订单 borrow list, result:{}", data);
        JSONObject returnMsg = JSONObject.parseObject(data);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
        JSONArray result = returnMsg.getJSONArray("data");
        /*
          添加逻辑,遍历borrowList中的 每个对象中都携带的repayList数据, 如果transactionId不为null
          查询with_hold_flow表数据,transactionId = flowId  拿出orderNo就是 结算交易流水号
          存入结果集repayList中的对象中, 变量名: transFlowId
         */
        for (int i = 0; i < result.size(); i++) {
            JSONArray repayList = result.getJSONObject(i).getJSONArray("repayList");
            if (CollectionUtil.isNotEmpty(repayList)){
                for (int j = 0; j < repayList.size(); j++) {
                    JSONObject repayData = repayList.getJSONObject(j);
                    Long transactionId = repayData.getLong("transactionId");
                    if (null != transactionId){
                        //数据库做了判断, 如果是null 返回空字符串
                        repayData.put("transFlowId",withholdFlowDAO.getOrderNoByTransId(transactionId));
                    }
                }
            }
        }
        return  result;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public JSONArray getUsernameList(JSONObject requestData) throws Exception {
        String url = accountUrl + "/server/getUsernameList";
        log.info("getUsernameList, data:{}", requestData);
        String data = HttpClientUtils.sendPost(url, requestData.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        log.info("getUsernameList, result:{}", returnMsg);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
        return  returnMsg.getJSONArray("data");
    }

    @Override
    public JSONObject testVerifyAccountServer(JSONObject requestInfo) throws Exception {
        String url = accountUrl + "/account/testVerifyAccountServer";
        String data = HttpClientUtils.sendPost(url, requestInfo.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        log.info("getUsernameList, result:{}", returnMsg);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
        return  returnMsg.getJSONObject("data");
    }

    @Override
    public List<JSONObject> getAll() throws Exception {
        String url = accountUrl + "/server/getAll";
        JSONObject jsonObject = new JSONObject();
        String data = HttpClientUtils.sendPost(url,jsonObject.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        log.info("初始化卡类型数据, result:{}", returnMsg);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
        return JSONArray.parseArray( returnMsg.get("data").toString(), JSONObject.class);
    }

    @Override
    public void presetCard(JSONObject requestData) throws Exception {
        String url = accountUrl + "/server/presetCard";
        String data = HttpClientUtils.sendPost(url,requestData.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        log.info("设置用户的默认卡, result:{}", returnMsg);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
    }

    @Override
    public void updateUserCreditState(Long userId, HttpServletRequest request) throws Exception {
        JSONObject queryParam = new JSONObject(2);
        queryParam.put("userId",userId);
        String url = creditMerchantUrl + "/payremote/user/updateAfterIllionSuccess";
        log.info("请求分期付,illion成功后 更新分期付User 状态, data:{}", queryParam);
        String data = HttpClientUtils.sendPost(url, queryParam.toJSONString());
        log.info("请求分期付,illion成功后 更新分期付User 状态, result:{}", data);
        JSONObject returnMsg = JSONObject.parseObject(data);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
    }

    @Override
    public void illionService(String referralCode, HttpServletRequest request) throws Exception {
        JSONObject queryParam = new JSONObject(2);
        queryParam.put("referrerCode",referralCode);
        //illion授权状态为完成
        queryParam.put("status","COMPLETE");
        String url = creditMerchantUrl + "/illion/service";
        log.info("请求分期付,illion成功后 通知分期付illion授权结果 , data:{}", queryParam);
        String data = HttpClientUtils.sendPost(url, queryParam.toJSONString());
        log.info("请求分期付,illion成功后 通知分期付illion授权结果 , result:{}", data);
        JSONObject returnMsg = JSONObject.parseObject(data);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
    }

    @Override
    public void updateFailedIllionUserState(JSONObject data, HttpServletRequest request) throws Exception {
        Long userId = data.getLong("userId");
        Integer state = data.getInteger("state");
        if (userId == null || null == state){
            //系统间调用 parameters.error
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        String url = creditMerchantUrl + "/payremote/user/updateFailedIllionUserState";
        log.info("illion异常 更新用户状态 , data:{}", data);
        String res = HttpClientUtils.sendPost(url, data.toJSONString());
        log.info("illion异常 更新用户状态 , result:{}", res);
        JSONObject returnMsg = JSONObject.parseObject(res);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
    }

    @Override
    public void infoSupplementCredit(JSONObject userInfo, HttpServletRequest request) throws Exception {
        UserDTO user = userService.findUserById(Long.valueOf(userInfo.getString("userId")));
        if(user==null || user.getId() == null){
            //查询无此用户
            throw new BizException(I18nUtils.get("query.failed", getLang(request)));
        }
        String birth = userInfo.getString("birth");
        if (!StringUtils.isBlank(birth)){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date parse = simpleDateFormat.parse(birth);
            long time = parse.getTime();
            userInfo.put("birth",time);
        }
        String infoSupplementUrl = creditMerchantUrl + "/user/replenishUser";
        String data = HttpClientUtils.sendPost(infoSupplementUrl, userInfo.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        if (returnMsg.getString(Constant.CODE).equals(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode())) {
            log.info("同步分期付用户信息失败, data:{}, error message:{}", userInfo, returnMsg.getString("message"));
            throw new BizException(I18nUtils.get("incorrect.information", getLang(request)));
        }
    }

    @Override
//    @Async("taskExecutor")
    public void updateCreditCard(JSONObject data, HttpServletRequest request) throws Exception {
        UserDTO user = userService.findUserById(data.getLong("userId"));
        if(user==null || user.getId() == null){
            //查询无此用户
            throw new BizException(I18nUtils.get("query.failed", getLang(request)));
        }
        String infoSupplementUrl = creditMerchantUrl + "/user/updateUserBindCard";
        String result = HttpClientUtils.sendPost(infoSupplementUrl, data.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(result);
        if (returnMsg.getString(Constant.CODE).equals(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode())) {
            log.info("同步分期付用户卡信息失败, data:{}, error message:{}", result, returnMsg.getString("message"));
            throw new BizException(I18nUtils.get("incorrect.information", getLang(request)));
        }
    }

    @Override
    public List<JSONObject> getOverdueFeeList(JSONObject data) throws Exception {
        if (data==null||data.getLong("userId")==null){
            throw new BizException("param error");
        }
        List<JSONObject> list=new ArrayList<>();
        String infoSupplementUrl = creditMerchantUrl + "/overdueFee/getOverdueFeeList";
        String result = HttpClientUtils.sendPost(infoSupplementUrl, data.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(result);
        if (returnMsg.getString(Constant.CODE).equals(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode())) {
            log.error("查询用户分期付逾期费订单失败, data:{}, error message:{}", result, returnMsg.getString("message"));
            throw new BizException("incorrect.information");
        }
        JSONArray dataList = returnMsg.getJSONArray("data");
        if (dataList==null||dataList.size()==0){
            return list;
        }
        for (Object o : dataList) {
            try{
                JSONObject overdueFee=new JSONObject();
                if (o instanceof JSONObject){
                    overdueFee=(JSONObject)o;
                }else {
                    continue;
                }
                BigDecimal tradingFee = overdueFee.getBigDecimal("tradingFee");
                BigDecimal amount = overdueFee.getBigDecimal("amount");
                String createdDateStr = overdueFee.getString("createdDate");
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                SimpleDateFormat ausSimp=new SimpleDateFormat("EEE dd MMM yyyy", Locale.US);
                Long id = overdueFee.getLong("id");
                overdueFee.clear();
                overdueFee.put("createdDate","");
                overdueFee.put("displayDate","");
                if (createdDateStr!=null){
                    Date parse = simpleDateFormat.parse(createdDateStr);
                    overdueFee.put("createdDate",parse.getTime());
                    overdueFee.put("displayDate",ausSimp.format(parse));
                }
                overdueFee.put("payAmount",amount);
                overdueFee.put("totalAmount",(amount==null?BigDecimal.ZERO:amount).add(tradingFee==null?BigDecimal.ZERO:tradingFee));
                overdueFee.put("id",id);
                overdueFee.put("transType",StaticDataEnum.ACC_FLOW_TRANS_TYPE_33.getCode());
                overdueFee.put("tradingName","");
                list.add(overdueFee);
            }catch (Exception e){
                log.error("获取逾期费订单异常,e:{}",e);
                continue;
            }

        }
        return list;
    }

    @Override
    public JSONObject getGateWayFeeData(JSONObject data, HttpServletRequest request) throws BizException {


        BigDecimal amount = data.getBigDecimal("amount");
        // 查询当前通道是否可以用
        Map<String,Object> params = new HashMap<>();
        params.put("gatewayType",data.getInteger("gatewayType"));
        if(StringUtils.isNotEmpty(data.getJSONObject("cardInfo").getString("stripeToken"))){
            params.put("type",StaticDataEnum.GATEWAY_TYPE_8.getCode());
        }else if(StringUtils.isNotEmpty(data.getJSONObject("cardInfo").getString("crdStrgToken"))){
            params.put("type",StaticDataEnum.GATEWAY_TYPE_0.getCode());
        }else{
            params.put("state",StaticDataEnum.STATUS_1.getCode());
        }


        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        if(gatewayDTO == null || gatewayDTO.getId() == null){
            throw new BizException("unknown payType");
        }
        JSONObject result = new JSONObject();
        if(gatewayDTO.getType() == StaticDataEnum.GATEWAY_TYPE_0.getCode() ||gatewayDTO.getType() == StaticDataEnum.GATEWAY_TYPE_8.getCode() ){
            CardDTO cardDTO = JSONObject.parseObject(data.getJSONObject("cardInfo").toJSONString(), CardDTO.class);
            params.clear();
            params.put("gatewayId",gatewayDTO.getType());
            params.put("code",Integer.valueOf(cardDTO.getCustomerCcType()) + 50);
            ChannelFeeConfigDTO channelFeeConfigDTO = channelFeeConfigService.findOneChannelFeeConfig(params);

            if(channelFeeConfigDTO == null || channelFeeConfigDTO.getId() == null ){
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            GatewayDTO gatewayDTO1 = new GatewayDTO();
            gatewayDTO1.setRate(channelFeeConfigDTO.getRate());
            gatewayDTO1.setRateType(channelFeeConfigDTO.getType());
            BigDecimal gateWayFee = gatewayService.getGateWayFee(gatewayDTO1,amount);
            result.put("fee",gateWayFee);
            result.put("feeType",channelFeeConfigDTO.getType());
            result.put("feeRate",channelFeeConfigDTO.getRate());
        }else{
            BigDecimal gateWayFee = gatewayService.getGateWayFee(gatewayDTO,amount);
            result.put("fee",gateWayFee);
            result.put("feeType",gatewayDTO.getRateType());
            result.put("feeRate",gatewayDTO.getRate());
        }

        return result;
    }

    @Override
    public List<UserDTO> findUserInfoByParam(JSONObject userData) throws Exception {
        String url = accountUrl + "/server/findUserInfoByParam";
        String data = HttpClientUtils.sendPost(url,userData.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        log.info("根据用户信息查询用户列表, result:{}", returnMsg);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
        JSONArray data1 = returnMsg.getJSONArray("data");
        return data1.toJavaList(UserDTO.class);
    }
    @Override
    public JSONObject findCreditUserState(@NonNull Long userId) throws Exception {
        String url = creditMerchantUrl + "/user/findUserState";
//        String url = "http://localhost:6030" + "/user/findUserState";
        JSONObject userData=new JSONObject();
        userData.put("userId",userId);
        String data = HttpClientUtils.sendPost(url,userData.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        log.info("根据用户信息查询用户state, result:{}", returnMsg);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
        JSONObject data1 = returnMsg.getJSONObject("data");
        return data1;

    }


    @Override
    public JSONObject creditRefund(JSONObject requestData, HttpServletRequest request) throws Exception {
        String url = creditMerchantUrl + "/payremote/refunds";
        String data = HttpClientUtils.sendPost(url,requestData.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        log.info("分期付退款, result:{}", returnMsg);
//        JSONObject data1 = returnMsg.getJSONObject("data");
        return returnMsg;
    }

    @Override
    public JSONObject creditRefundDoubt(Long id) throws Exception {

        String url = creditMerchantUrl + "/payremote/refundsCheck";
        JSONObject userData=new JSONObject();
        userData.put("id",id);
        String data = HttpClientUtils.sendPost(url,userData.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        log.info("分期付退款查询, result:{}", returnMsg);
        return returnMsg;
    }

    /**
     * 三方退款
     *
     * @param flowId
     * @param refundAmount
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2021/8/23 10:49
     */
    @Override
    public JSONObject creditThirdRefund(Long flowId, BigDecimal refundAmount, String reason, String orderNo, HttpServletRequest request) throws Exception {

        // 查询三方交易流水
        HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);

        paramMap.put("flowId", flowId);

        WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(paramMap);

        if(Objects.isNull(withholdFlowDTO) || Objects.isNull(withholdFlowDTO.getId())){
            //三方交易流水为空
            throw new BizException(I18nUtils.get("refund.fail", getLang(request)));
        }
        //查询订单

        BigDecimal orderPayAmount = withholdFlowDTO.getTransAmount();
        if(refundAmount.compareTo(BigDecimal.ZERO) <= 0 || orderPayAmount.compareTo(BigDecimal.ZERO) == 0){
            //金额校验不通过
            throw new BizException(I18nUtils.get("refund.fail", getLang(request)));
        }

        if(refundAmount.compareTo(orderPayAmount) > 0){
            //退款金额 大于 实付金额
            throw new BizException(I18nUtils.get("refund.fail", getLang(request)));
        }
        if(withholdFlowDTO.getGatewayId().intValue()  != StaticDataEnum.GATEWAY_TYPE_0.getCode()&&withholdFlowDTO.getGatewayId().intValue()  != StaticDataEnum.GATEWAY_TYPE_8.getCode()){
            //不支持的通道
            throw new BizException(I18nUtils.get("refund.fail", getLang(request)));
        }

        int diffDays = (int) ((System.currentTimeMillis() - withholdFlowDTO.getCreatedDate()) / (1000 * 3600 * 24));

        if(diffDays > 90){
            //超过90天 不予退款
            throw new BizException(I18nUtils.get("refund.fail", getLang(request)));
        }



        //退款类型
        Integer refundType = StaticDataEnum.LAT_PAY_REFUND_TYPE_1.getCode();
        //记录退款流水
        RefundFlowDTO refundFlowDTO = createAndSaveRefundFlow(withholdFlowDTO, refundAmount, orderNo, reason, request);

        //调用latpay进行退款
        // todo 支付通道修改
        JSONObject resultJsonObj = new JSONObject();
        if(refundFlowDTO.getGatewayId() == StaticDataEnum.GATEWAY_TYPE_0.getCode()){
            resultJsonObj = latpayRefund(withholdFlowDTO, refundFlowDTO, refundType, request);
        }else if (refundFlowDTO.getGatewayId() == StaticDataEnum.GATEWAY_TYPE_8.getCode()){
            resultJsonObj=stripeRefund(withholdFlowDTO,refundFlowDTO,refundType,request);
        } else{
            refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            refundFlowService.updateRefundFlow(refundFlowDTO.getId(),refundFlowDTO,request);
            throw new BizException(I18nUtils.get("refund.fail", getLang(request)));
        }

        return resultJsonObj;
    }




    /**
     *  封装退款流水并保存
     * @author zhangzeyuan
     * @date 2021/8/24 14:24
     * @param refundAmount
     * @param request
     * @return com.uwallet.pay.main.model.dto.RefundFlowDTO
     */
    private RefundFlowDTO createAndSaveRefundFlow(WithholdFlowDTO withholdFlowDTO, BigDecimal  refundAmount, String orderNo, String reason, HttpServletRequest request) throws BizException {

        Map<String,Object> params = new HashMap<>();
        params.put("type",withholdFlowDTO.getGatewayId());

        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        int transType ;
        if(gatewayDTO.getGatewayType() == StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode()){
            transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_12.getCode();
        }
//        else if(gatewayDTO.getGatewayType() == StaticDataEnum.CHANNEL_PAY_TYPE_2.getCode()){
//            transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_13.getCode();
//        }else if(gatewayDTO.getGatewayType() == StaticDataEnum.CHANNEL_PAY_TYPE_3.getCode()){
//            transType = StaticDataEnum.ACC_FLOW_TRANS_TYPE_14.getCode();
//        }
        else {

            throw new BizException(I18nUtils.get("refund.fail", getLang(request)));
        }

        RefundFlowDTO refundFlowDTO = new RefundFlowDTO();
        refundFlowDTO.setGatewayId(withholdFlowDTO.getGatewayId());
        refundFlowDTO.setFlowId(withholdFlowDTO.getId());
        refundFlowDTO.setOrgRecUserId(withholdFlowDTO.getUserId());
        refundFlowDTO.setRefundAmount(refundAmount);
        refundFlowDTO.setOrdreNo(SnowflakeUtil.generateId()+"");
        refundFlowDTO.setRefundNo(orderNo);
        refundFlowDTO.setTransType(transType);
        refundFlowDTO.setCurrency("AUD");

        String stripeId = withholdFlowDTO.getStripeId();
        if (StringUtils.isNotBlank(stripeId)){
            refundFlowDTO.setStripeRefundNo(withholdFlowDTO.getStripeId());
            refundFlowDTO.setOrgThirdNo(withholdFlowDTO.getStripeId());
        }else{
            refundFlowDTO.setOrgThirdNo(withholdFlowDTO.getOrdreNo());
        }
        refundFlowDTO.setReason(reason);
        refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        log.info("create refund flow, refundFlowDTO:{}", refundFlowDTO);
        Long id = refundFlowService.saveRefundFlow(refundFlowDTO, request);
        refundFlowDTO.setId(id);
        return refundFlowDTO;
    }


    /**
     * latPay退款
     * @author zhangzeyuan
     * @date 2021/8/24 14:43
     * @param withholdFlowDTO
     * @param refundFlowDTO
     * @param refundType
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     */
    @Override
    public JSONObject latpayRefund(WithholdFlowDTO withholdFlowDTO,   RefundFlowDTO refundFlowDTO, Integer refundType, HttpServletRequest request) throws Exception{

        //封装退款请求参数
        JSONObject refundRequestInfo = new JSONObject();
        refundRequestInfo.put("merchant_refund_id", merchantRefundId);
        refundRequestInfo.put("merchant_pwd", merchantRefundPassword);
        refundRequestInfo.put("lps_transaction_id", withholdFlowDTO.getLpsTransactionId());
        refundRequestInfo.put("merchant_ref_number", withholdFlowDTO.getOrdreNo().toString());
        refundRequestInfo.put("refund_amount", refundFlowDTO.getRefundAmount().toString());
        refundRequestInfo.put("refund_type", refundType.toString());
        refundRequestInfo.put("refund_comment", refundFlowDTO.getReason());
        long now = System.currentTimeMillis();
        Long transactionDay = withholdFlowDTO.getCreatedDate();
        long days = ((now - transactionDay) / (1000*3600*24));
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
        //退款结果处理
        return latPayRefundRequestResultHandle(returnData, refundFlowDTO, refundType, request);


    }




    /**
     * latPay退款结果处理
     * @param returnData
     * @param refundFlowDTO
     * @param request
     * @throws Exception
     */
    public JSONObject latPayRefundRequestResultHandle(JSONObject returnData, RefundFlowDTO refundFlowDTO,  Integer refundType, HttpServletRequest request) throws Exception {
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
            if(returnData.getInteger("RefundSubmit_status") == null ){
                refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
                result.put("refundState", StaticDataEnum.TRANS_STATE_3.getCode());
                result.put("refundMessage", I18nUtils.get("refund.processing", getLang(request)));
            }else if (returnData.getInteger("RefundSubmit_status").equals(StaticDataEnum.LP_RESPONSE_TYPE_0.getCode())) {
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
                } else if (returnData.getString("Bank_status").equals(StaticDataEnum.LAT_PAY_BANK_STATUS_7.getMessage())
                        ||returnData.getString("Bank_status").equals(StaticDataEnum.LAT_PAY_BANK_STATUS_5.getMessage())) {
                    //05,07失败
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
    @Override
    public JSONObject getOneConfigById(@NonNull Long id) {
        log.info("查询分期付配置, id:{}", id);
        String url = creditMerchantUrl + "/config/getOneConfigById/"+id;
        String data = HttpClientUtils.sendGet(url);
        JSONObject returnMsg = JSONObject.parseObject(data);
        log.info("查询分期付配置, result:{}", returnMsg);
        return returnMsg;
    }


    /**
     *
     *
     * @param data
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2021/9/8 18:56
     */
    @Override
    public JSONObject getRiskScoreGradeList(JSONObject data, HttpServletRequest request) throws BizException {
        String url = creditMerchantUrl + "/payremote/scoreGradeList";
        JSONObject returnMsg = null;
        try{
            String resData = HttpClientUtils.sendPost(url, "");
            returnMsg = JSONObject.parseObject(resData);
        }catch (Exception e){
            log.info("获取评分等级列表,e:{}",e.getMessage());
        }
        return returnMsg;
    }

    @Override
    public JSONObject updateUserCreditStateV1(JSONObject creditUserInfo, HttpServletRequest request) throws Exception {
        log.info("修改分期付状态,data:{}",creditUserInfo);
        String url = creditMerchantUrl + "/user/updateUserState";
//        String url = "http://localhost:6030" + "/user/updateUserState";
        String data = HttpClientUtils.sendPost(url,creditUserInfo.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        log.info("修改分期付状态, result:{}", returnMsg);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
        return returnMsg;
    }

    @Override
    public void updateUserRepayTimes(Long userId, HttpServletRequest request) throws Exception {
        log.info("重置用户主动还款失败累计次数,userId:{}",userId);
        JSONObject param=new JSONObject();
//        String url = "http://localhost:6030" + "/user/resetUserRepayTimes/"+userId;
        String url = creditMerchantUrl + "/user/resetUserRepayTimes/"+userId;
        String data = HttpClientUtils.sendPost(url,param.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        log.info("重置用户主动还款失败累计次数, result:{}", returnMsg);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
    }

    @Override
    public JSONObject findUserUseRepay(JSONObject param, HttpServletRequest request) throws Exception {
        log.info("查询用户最近一次还款数据,param:{}",param);
        String url = creditMerchantUrl + "/user/findUserUseRepay";
        // todo
//        String url = "http://localhost:6030" + "/user/findUserUseRepay";
        String data = HttpClientUtils.sendPost(url,param.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        log.info("查询用户最近一次还款数据, result:{}", returnMsg);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
        return returnMsg.getJSONObject("data");
    }

    @Override
    public JSONObject getMarketingMessage(Long id,Long userId) throws Exception {
        String getMarketingMessageUrl = accountUrl + "/server/findOneMarketingById";
        JSONObject sendData = new JSONObject();
        sendData.put("id", id);
        sendData.put("userId",userId);
        String data = HttpClientUtils.sendPost(getMarketingMessageUrl, sendData.toJSONString());
        JSONObject msg = JSONObject.parseObject(data);
        if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(msg.getString("message"));
        }
        return msg.getJSONObject("data");
    }

    @Override
    public void updateUserCreditAmount(JSONObject paramButton, HttpServletRequest request) throws Exception {
        log.info("用户额度调整,param:{}",paramButton);
        String url = creditMerchantUrl + "/user/updateUserCreditAmount";
//        String url = "http://localhost:6030" + "/user/updateUserCreditAmount";
        String data = HttpClientUtils.sendPost(url,paramButton.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        log.info("用户额度调整, result:{}", returnMsg);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
    }

    @Override
    public void delayPayment(JSONObject param, HttpServletRequest request) throws Exception {
        log.info("延迟还款,param:{}",param);
        String url = creditMerchantUrl + "/payremote/user/delayPayment";
//        String url = "http://localhost:6030" + "/payremote/user/delayPayment";
        String data = HttpClientUtils.sendPost(url,param.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        log.info("延迟还款, result:{}", returnMsg);
        if (returnMsg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(returnMsg.getString("message"));
        }
    }

    /**
     * 根据card token 查询 卡信息
     *
     * @param cardId
     * @return
     * @throws Exception
     */
    @Override
    public JSONObject getCardInfoByStripeToken(String cardId) throws Exception {
        JSONObject dataJson = new JSONObject();
        dataJson.put("stripe_token", cardId);
        dataJson.put("type", StaticDataEnum.TIE_CARD_1.getCode());
        String url = accountUrl + "/server/getCardInfoByToken";
        String respone = HttpClientUtils.sendPost(url, dataJson.toJSONString());
        JSONObject resJsonObj = JSONObject.parseObject(respone);
        if (resJsonObj.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(resJsonObj.getString("message"));
        }

        return resJsonObj.getJSONObject("data");
    }

    /**
     * 根据卡后四位查询数量
     *
     * @param userId
     * @param last4
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2022/1/24 17:21
     */
    @Override
    public Integer countByCardNoLast4(String userId, String last4) throws Exception {
        JSONObject dataJson = new JSONObject();
        dataJson.put("userId", userId);
        dataJson.put("last4", last4);
        String url = accountUrl + "/server/countByCardLast4";
        String respone = HttpClientUtils.sendPost(url, dataJson.toJSONString());
        JSONObject resJsonObj = JSONObject.parseObject(respone);
        if (resJsonObj.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(resJsonObj.getString("message"));
        }
        return resJsonObj.getInteger("data");
    }

    /**
     * 获取卡列表
     *
     * @param
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2022/1/25 9:24
     */
    @Override
    public List<CardDTO> getStripeCardList(String userId) throws Exception {
        String url = accountUrl + "/server/stripeCardList";
        JSONObject data = new JSONObject();
        data.put("userId", userId);

        String respone = HttpClientUtils.sendPost(url, data.toJSONString());
        JSONObject resJsonObj = JSONObject.parseObject(respone);
        if (resJsonObj.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(resJsonObj.getString("message"));
        }
        return JSONObject.parseArray(resJsonObj.getString("data"), CardDTO.class);
    }

    /**
     * 获取latpay卡列表
     *
     * @param userId
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2022/1/25 9:24
     */
    @Override
    public List<CardDTO> getLatpayCardList(String userId) throws Exception {
        String url = accountUrl + "/server/latpayCardList";
        JSONObject data = new JSONObject();
        data.put("userId", userId);

        String respone = HttpClientUtils.sendPost(url, data.toJSONString());
        JSONObject resJsonObj = JSONObject.parseObject(respone);
        if (resJsonObj.getString("code").equals(ErrorCodeEnum.FAIL_CODE.getCode())) {
            throw new BizException(resJsonObj.getString("message"));
        }
        return JSONObject.parseArray(resJsonObj.getString("data"), CardDTO.class);
    }


    @Override
    public JSONObject useMarketing(JSONObject param,HttpServletRequest request) throws Exception {
        String useMarketingUrl = accountUrl + "/server/useMarketing";
        String data = HttpClientUtils.sendPost(useMarketingUrl, param.toJSONString());
        JSONObject msg = JSONObject.parseObject(data);
        if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(msg.getString("message"));
        }
        return msg.getJSONObject("data");
    }

    @Override
    public JSONObject marketingRollBack(Long id) throws Exception {
        String marketingRollBackUrl = accountUrl + "/server/refundMarketing";
        JSONObject sendData = new JSONObject();
        sendData.put("channelSerialnumber", id);
        String data = HttpClientUtils.sendPost(marketingRollBackUrl, sendData.toJSONString());
        JSONObject msg = JSONObject.parseObject(data);
        if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(msg.getString("message"));
        }
        return msg.getJSONObject("data");
    }
    /**
     * 添加卡券
     *
     * @param param
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/10/29 9:44
     */
    @Override
    public JSONObject addPromotionCode(JSONObject param, HttpServletRequest request) throws Exception{
        //向账户系统同步信息
        String url = accountUrl + "/server/addMarketing";
        String data = HttpClientUtils.sendPost(url, param.toJSONString());
        JSONObject returnMsg = JSONObject.parseObject(data);
        if (!returnMsg.getString(Constant.CODE).equals(ErrorCodeEnum.SUCCESS_CODE.getCode())) {
            log.info("添加卡券send post failed, data:{}, error message:{}", param, returnMsg.toJSONString());
            throw new BizException(returnMsg.getString("message"));
        }
        return returnMsg.getJSONObject("data");
    }



    @Override
    public JSONObject setCardSuccessPay(JSONObject jsonObject) throws Exception {
        String setCardSuccessPayUrl = accountUrl + "/server/setCardSuccessPay";
        String data = HttpClientUtils.sendPost(setCardSuccessPayUrl, jsonObject.toJSONString());
        JSONObject msg = JSONObject.parseObject(data);
        if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            throw new BizException(msg.getString("message"));
        }
        return msg.getJSONObject("data");
    }

    @Override
    public JSONObject stripeRefund(WithholdFlowDTO withholdFlowDTO, RefundFlowDTO refundFlowDTO, Integer refundType, HttpServletRequest request) throws Exception {
//        Stripe.apiKey="pk_live_51JN8U4LucMncOTu4v5hodWtxows8sNbgkUDjc3vL1iiA1sW4bNVhNPtrUEBi3HVy8eBOMoQinW1ExeGVwIw0qBk800t2vIcyvv";
        Map<String, Object> params = new HashMap<>();
        params.put("amount",refundFlowDTO.getRefundAmount().multiply(new BigDecimal(100)).intValue());
        String stripeRefundNo = refundFlowDTO.getStripeRefundNo();

        if (stripeRefundNo.startsWith("pi")){
            params.put("payment_intent",stripeRefundNo);
        }else {
            params.put("charge",stripeRefundNo);
        }
        Map<String, Object> paramNew = new HashMap<>();
        // 交易唯一退款id
        paramNew.put("refundId",refundFlowDTO.getId());
        params.put("metadata",paramNew);
        JSONObject result=new JSONObject();
        try{
            Refund refund = Refund.create(params);
            log.info("退款对象",refund);
        }catch (Exception e){
            log.error("退款失败请求失败,withholdFlowDTO:{},refundFlowDTO:{},e:{}",withholdFlowDTO,refundFlowDTO,e);
            refundFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
//            refundFlowService.updateRefundFlow(refundFlowDTO.getId(), refundFlowDTO, request);
            throw new BizException(I18nUtils.get("refund.fail", getLang(request)));
        }
        result.put("refundState", StaticDataEnum.TRANS_STATE_3.getCode());
        result.put("refundMessage", I18nUtils.get("refund.processing", getLang(request)));
        return result;
    }
}
