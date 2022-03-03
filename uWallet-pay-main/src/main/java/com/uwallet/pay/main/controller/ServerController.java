package com.uwallet.pay.main.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stripe.model.Event;
import com.stripe.model.EventData;
import com.stripe.net.Webhook;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.JSONResultHandle;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.filter.AppToken;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.filter.SignVerify;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

import static spark.Spark.post;


/**
 * <p>
 * 外部服务
 * </p>
 *
 * @description: 外部服务
 * @author: Rainc
 * @date: Created in 2019-12-19 10:41:37
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */

@RestController
@RequestMapping("/server")
@Slf4j
@Api("外部服务专用交互controller")
public class ServerController extends BaseController {

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private WithholdFlowService withholdFlowService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private QrcodeInfoService qrcodeInfoService;

    @Autowired
    private AccountFlowService accountFlowService;

    @Autowired
    private ServerService serverService;
    @Value("${spring.IntroductionPath}")
    private String IntroductionPath;

    @Value("${spring.paperPath}")
    private String paperPath;

    @Autowired
    private QrPayFlowService qrPayFlowService;

    @Autowired
    private QrPayService qrPayService;

    @Autowired
    private IllionInstitutionsService illionInstitutionsService;

    @Autowired
    private IllionSubmitStatusLogService illionSubmitStatusLogService;
    @Autowired
    private RefundFlowService refundFlowService;
    @Autowired
    private StripeAPIService stripeAPIService;

    @Value("${Stripe.endpointSecret}")
    private String endpointSecret;

    @SignVerify
    @PassToken
    @ApiOperation(value = "理财用户注册", notes = "理财用户注册")
    @PostMapping("/investLogin")
    public Object investLogin(@RequestBody JSONObject userInfo, HttpServletRequest request) {
        log.info("app user register info:{}", userInfo);
        Long userId = null;
        try {
            userId = userService.investLogin(userInfo, request);
        } catch (Exception e) {
            log.error("add user failed, info: {}, error message:{}, error all:{}", userInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(userId);
    }

    @SignVerify
    @PassToken
    @PostMapping("/invest/{id}")
    @ApiOperation(value = "开通/关闭分期付|开通理财业务(修改用户)", notes = "开通/关闭分期付|开通理财业务(修改用户)")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("put modify id:{}, user DTO:{}", id, requestInfo);
        try {
            UserDTO userDTO = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), UserDTO.class);
            userService.updateUser(id, userDTO, request);
        } catch (BizException e) {
            log.error("update user failed, userDTO: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "app获取卡列表", notes = "app获取卡列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", paramType = "path", dataType = "long", required = true),
            @ApiImplicitParam(name = "cardType", value = "卡类型 1：卡 0：账户", paramType = "query", dataType = "int", required = false)
    })
    @PostMapping("/getCardList/{userId}")
    public Object getCardList(@PathVariable("userId") Long userId, @RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("get user card list user:{}", userId);
        JSONArray cardList = null;
        try {
            cardList = userService.getCardList(userId, requestInfo.getJSONObject("data").getInteger("cardType"), request);
        } catch (Exception e) {
            log.error("get user card list failed, user: {}, error message:{}", userId, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed", getLang(request)));
        }

        return R.success(cardList);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "理财绑定账户", notes = "理财绑定账户")
    @PostMapping("/investCardAdd")
    public Object investCardAdd(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("investCardAdd, data:{}", requestInfo);
        try {
            JSONObject data = requestInfo.getJSONObject("data");
            userService.tieOnCardParamsCheck(data, request);
            userService.tieOnCard(data, request);
        } catch (Exception e) {
            log.info("investCardAdd failed, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "理财解绑账户", notes = "理财解绑账户")
    @PostMapping("/investCardDel")
    public Object investCardDel(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("investCardDel, data:{}", requestInfo);
        try {
            userService.cardUnbundling(requestInfo.getJSONObject("data"), request);
        } catch (Exception e) {
            log.info("investCardDel, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success();
    }

    @SignVerify
    @PassToken
    @PostMapping("/createNotice")
    @ApiOperation(value = "新增消息表", notes = "新增消息表")
    public Object createNotice(@RequestBody JSONObject data, HttpServletRequest request) {
        log.info("add notice DTO:{}", data);
        try {
            noticeService.saveNotice(JSONObject.parseObject(data.getJSONObject("data").toJSONString(), NoticeDTO.class), request);
        } catch (BizException e) {
            log.error("add notice failed, noticeDTO: {}, error message:{}, error all:{}", data, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "还款请求", notes = "还款请求")
    @PostMapping("/repay")
    public Object repayRequest(@RequestBody JSONObject data, HttpServletRequest request) throws Exception {
        log.info("add latPayRequest data:{}", data);
        JSONObject msg = null;
        try {
            msg = serverService.creditSystemRepayment(data.getJSONObject("data"), request);
        } catch (BizException e) {
            log.error("latPayRequest failed, data: {}, error message:{}, error all:{}", data, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(msg);
    }

    @SignVerify
    @PassToken
    @PutMapping("/merchant/{id}")
    @ApiOperation(value = "修改商户信息表", notes = "修改商户信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object updateMerchant(@PathVariable("id") Long id, @RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("updateMerchant id:{}, jsonObject :{}", id, jsonObject);
        try {
            MerchantDTO merchantDTO = JSONObject.parseObject(jsonObject.getJSONObject("data").toJSONString(), MerchantDTO.class);
            merchantService.updateMerchant(id, merchantDTO, request);
        } catch (BizException e) {
            log.error("update merchant failed, jsonObject: {}, error message:{}, error all:{}", jsonObject, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "根据userId查询账户系统用户信息(全部)", notes = "根据userId查询账户系统用户信息(全部)")
    @PostMapping("/findUserInfoId")
    public Object findUserInfoId(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("query user info, data:{}", requestInfo);
        JSONObject object;
        JSONObject data = requestInfo.getJSONObject("data");
        try {
            object = qrcodeInfoService.findUserInfoId(data, request);
        } catch (Exception e) {
            log.error("query user info failed: {}, error message:{}, e:{}", data, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed", getLang(request)));
        }
        JSONObject objects = JSONResultHandle.resultHandle(object);
        return R.success(objects);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "查询三方交易流水", notes = "查询三方交易流水")
    @PostMapping("/findWithholdFlow")
    public Object findOneWithholdFlow(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        List<Long> flowIds = JSONObject.parseArray(requestInfo.getJSONObject("data").getJSONArray("flowIds").toJSONString(), Long.class);
        log.info("get withholdFlow findList flowIds:{}", flowIds);
        List<Map> result = null;
        try {
            result = withholdFlowService.selectResults(flowIds.toArray(new Long[flowIds.size()]), request);
        } catch (BizException e) {
            log.info("get withholdFlow findList flowIds:{}, error message:{}, e:{}", flowIds, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "单设备推送", notes = "单设备推送")
    @PostMapping("/pushFirebase")
    public Object pushFirebase(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("push message, data:{}", requestInfo);
        FirebaseDTO firebaseDTO = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), FirebaseDTO.class);
        try {
            serverService.pushFirebase(firebaseDTO, request);
        } catch (Exception e) {
            log.error(": {}, error message:{}", requestInfo, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.firebase.failed", getLang(request)));
        }
        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "订单初次逾期多设备推送", notes = "订单初次逾期多设备推送")
    @PostMapping("/pushFirebaseListFistOverdue")
    public Object pushFirebaseListFistOverdue(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("push list message, data:{}", requestInfo);
        JSONObject data = requestInfo.getJSONObject("data");
        if (null == data || data.isEmpty()) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.firebase.failed", getLang(request)));
        }
        JSONArray pushInfoList = data.getJSONArray("list");
        if (CollectionUtil.isEmpty(pushInfoList)) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.firebase.failed", getLang(request)));
        }
        List<FirebaseDTO> firebaseDTOList = JSONArray.parseArray(pushInfoList.toJSONString(), FirebaseDTO.class);
        List<FirebaseDTO> failedList = new ArrayList<>();
        firebaseDTOList.forEach(firebaseDTO -> {
            try {
                serverService.pushFirebase(firebaseDTO, request);
            } catch (Exception e) {
                failedList.add(firebaseDTO);
                log.error("订单初次逾期多设备推送 数据推送异常,第一次发送,数据: {}, error message:{}", firebaseDTO, e.getMessage());
            }
        });
        if (CollectionUtil.isNotEmpty(failedList)) {
            failedList.forEach(dto -> {
                try {
                    serverService.pushFirebase(dto, request);
                } catch (Exception e) {
                    log.error("订单初次逾期多设备推送 数据推送异常,第二次发送,数据: {}, error message:{}", dto, e.getMessage());
                }
            });
        }
        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "多设备按主题推送", notes = "多设备按主题推送")
    @PostMapping("/pushFirebaseList")
    public Object pushFirebaseList(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("push message, data:{}", requestInfo);
        FirebaseDTO firebaseDTO = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), FirebaseDTO.class);
        try {
            serverService.pushFirebaseList(firebaseDTO, request);
        } catch (Exception e) {
            log.error("pushFirebase info failed: {}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.firebaseList.failed", getLang(request)));
        }
        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "分期付审核通知")
    @PostMapping("/installmentAuditNotice")
    public Object installmentAuditNotice(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("installment audit notice, data:{}", requestInfo);
        try {
            serverService.installmentAuditNotice(requestInfo.getJSONObject("data"), request);
        } catch (Exception e) {
            log.info("installment audit notice fail, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "用户身份验证")
    @PostMapping("/userValidation")
    public Object userValidation(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("userValidation, data:{}", requestInfo);
        JSONObject result = null;
        try {
            result = userService.userValidation(requestInfo.getJSONObject("data"), request);
        } catch (Exception e) {
            log.info("userValidation fail, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "三方发送验证码", notes = "三方发送验证码")
    @PostMapping("/thirdSendSecurityCodeSMS/{phone}/{sendNode}")
    public Object thirdSendSecurityCodeSMS(@PathVariable("phone") String phone, @PathVariable("sendNode") String sendNode, HttpServletRequest request) {
        log.info("ServerController.thirdSendSecurityCodeSMS, phone:{},sendNode:{}", phone, sendNode);
        try {
            userService.phoneParamsCheck(phone, request);
            //此处用户类型只有用户
            if (sendNode.equals(StaticDataEnum.SEND_NODE_1.getCode() + "") || sendNode.equals(StaticDataEnum.SEND_NODE_3.getCode() + "")) {
                userService.sendSecuritySMS(phone, sendNode, StaticDataEnum.USER_TYPE_10.getCode(), request);
            } else {
                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
            }

        } catch (Exception e) {
            log.error("ServerController.thirdSendSecurityCodeSMS Exception:", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "三方用户短信验证注册", notes = "三方用户短信验证注册")
    @PostMapping("/thirdUserRegister")
    public Object thirdUserRegister(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("third user register DTO:{}", requestInfo);
        JSONObject userInfo = requestInfo.getJSONObject("data");
        try {
            UserDTO userDTO = JSONObject.parseObject(userInfo.toJSONString(), UserDTO.class);
            userDTO.setUserType(StaticDataEnum.USER_TYPE_10.getCode());
            userService.userRegisterParamsCheck(userDTO, request);
            userService.saveUser(userDTO, request);
        } catch (Exception e) {
            log.error("third user failed, userDTO: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }

        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "获取kyc剩余次数", notes = "获取kyc剩余次数")
    @PostMapping("/thirdKycResult")
    public Object thirdKycResult(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("third server kycResult DTO:{}", requestInfo);
        JSONObject result = new JSONObject();
        try {
            result = userService.getKycResult(requestInfo.getJSONObject("data").getString("userId"), request);
        } catch (Exception e) {
            log.error("third server kycResult, requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }

        return R.success(result);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "风控", notes = "风控")
    @PostMapping("/thirdRiskCheck")
    public Object thirdRiskCheck(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("thirdRiskCheck data, data:{}", requestInfo);
        JSONObject data = requestInfo.getJSONObject("data");
        JSONObject result = null;
        try {
            userService.riskCheckParamsCheck(data, request);
            result = userService.riskCheck(requestInfo.getJSONObject("data"), request).get();
        } catch (Exception e) {
            log.error("thirdRiskCheck check faile, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "用户忘记密码", notes = "用户忘记密码")
    @PostMapping("/thirdForgetPassword")
    public Object thirdForgetPassword(@RequestBody JSONObject requestInfo, HttpServletRequest request) throws Exception {
        log.info("thirdForgetPassword DTO:{}", requestInfo);
        try {
            JSONObject userInfo = requestInfo.getJSONObject("data");
            UserDTO userDTO = JSONObject.parseObject(userInfo.toJSONString(), UserDTO.class);
            userDTO.setUserType(StaticDataEnum.USER_TYPE_10.getCode());
            userService.forgetPassword(userDTO, request);
        } catch (Exception e) {
            log.error("thirdForgetPassword failed, userDTO: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }

        return R.success();
    }

    @SignVerify
    @PassToken
    @PostMapping(name = "上传图片", value = "/thirdMultiUploadFile")
    @ApiOperation(value = "上传图片", notes = "上传图片")
    @ApiImplicitParam(name = "file", value = "图片", dataType = "String", required = true)
    public Object thirdMultiUploadFile(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        String fileName;
        try {
            fileName = userService.multiUploadFile(jsonObject, this.IntroductionPath, request);
        } catch (Exception e) {
            log.error("upload thirdMultiUploadFile failed, error message:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(fileName);
    }

    @SignVerify
    @PassToken
    @PostMapping(name = "上传证件图片", value = "/thirdPaperMultiUploadFile")
    @ApiOperation(value = "上传证件图片", notes = "上传证件图片")
    @ApiImplicitParam(name = "file", value = "图片", dataType = "String", required = true)
    public Object thirdPaperMultiUploadFile(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        String fileName;
        try {
            fileName = userService.multiUploadFile(jsonObject, this.paperPath, request);
        } catch (Exception e) {
            log.error("upload thirdPaperMultiUploadFile failed, error message:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(fileName);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "红包入账", notes = "红包入账")
    @PostMapping("/walletBookedConsumption")
    public Object walletBookedConsumption(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("wallet booked consumption , data:{}", requestInfo);
        JSONObject data = requestInfo.getJSONObject("data");
        JSONObject result = null;
        try {
            userService.walletBookedConsumption(requestInfo.getJSONObject("data"), request);
        } catch (Exception e) {
            log.error("wallet booked consumption failed, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "查询入账记录", notes = "查询入账记录")
    @PostMapping("/walletBookedConsumptionCheck")
    public Object walletBookedConsumptionCheck(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("wallet booked consumption check , data:{}", requestInfo);
        Map<String, Object> params = requestInfo.getJSONObject("data");
        params.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode());
        return R.success(accountFlowService.findOneAccountFlow(params));
    }


    @SignVerify
    @PassToken
    @ApiOperation(value = "变更分期付清算状态", notes = "变更分期付清算状态")
    @PostMapping("/updateClearState")
    public Object updateClearState(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("updateClearState , data:{}", requestInfo);
        UpdateClearStateRequestDTO req = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), UpdateClearStateRequestDTO.class);
        qrPayFlowService.updateClearState(req.getList(), request);
        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "分期付清算", notes = "分期付清算")
    @PostMapping("/creditClear")
    public Object creditClear(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("creditClear , data:{}", requestInfo);
        UpdateClearStateRequestDTO req = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), UpdateClearStateRequestDTO.class);
        try {
            qrPayFlowService.creditClear(req.getList(), request);
        } catch (BizException e) {
            log.error("creditClear failed, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "通过商户id集合获取商户商户信息", notes = "通过商户id集合获取商户商户信息")
    @PostMapping("/getMerchantListInfo")
    public Object getMerchantListInfo(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("通过商户id集合获取商户商户信息 , data:{}", requestInfo);
        try {
            return R.success(merchantService.getMerchantListInfo(requestInfo.getJSONObject("data"), request));
        } catch (BizException e) {
            log.error("通过商户id集合获取商户商户信息异常, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "分期付进行卡支付接口", notes = "分期付进行卡支付接口")
    @PostMapping("/doPayByCard")
    public Object doPayByCard(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("分期付进行卡支付接口 , data:{}", requestInfo);
        try {
            return R.success(qrPayService.doPayByCard(requestInfo.getJSONObject("data"), request));
        } catch (Exception e) {
            log.error("分期付进行卡支付接口, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @SignVerify
    @PassToken
    @ApiOperation(value = "分期付进行卡支付接口V2", notes = "分期付进行卡支付接口")
    @PostMapping("/doPayByCardV2")
    public Object doPayByCardV2(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("分期付进行卡支付接口V2 , data:{}", requestInfo);
        try {
            return R.success(qrPayService.doPayByCardV2(requestInfo.getJSONObject("data"), request));
        } catch (Exception e) {
            log.error("分期付进行卡支付接口, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @PassToken
    @ApiOperation(value = "illion异常发送短信接口", notes = "illion异常发送短信接口")
    @PostMapping("/sendIllionMessage")
    public Object sendIllionMessage(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("illion异常发送短信接口 , data:{}", requestInfo);
        try {
            JSONObject data = requestInfo.getJSONObject("data");
            illionInstitutionsService.sendIllionMessage(data, request);
        } catch (Exception e) {
            log.error("illion异常发送短信接口,error message:{},e:{}", e.getMessage(), e);
        }
        return R.success();
    }

    @PassToken
    @PostMapping("/addLog")
    @ApiOperation(value = "新增", notes = "新增")
    public Object addLog(@RequestBody JSONObject param, HttpServletRequest request) {
        log.info("add illionSubmitStatusLog DTO:{}", param);
        try {
            illionSubmitStatusLogService.addSubmitStatusLog(param.getJSONObject("data"), request);
        } catch (Exception e) {
            log.error("add illionSubmitStatusLog failed, param: {}, error message:{}, error all:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }


    @PassToken
    @PostMapping("/getGateWayFeeData")
    @ApiOperation(value = "查询支付通道信息", notes = "查询支付通道信息")
    public Object getGateWayFeeData(@RequestBody JSONObject param, HttpServletRequest request) {
        log.info("getGateWayFeeData DTO:{}", param);
        JSONObject result;
        try {
            result = serverService.getGateWayFeeData(param.getJSONObject("data"), request);
        } catch (Exception e) {
            log.error("getGateWayFeeData, param: {}, error message:{}, error all:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(result);
    }


    @PassToken
    @PostMapping("/creditThirdRefund")
    @ApiOperation(value = "分期付三方退款", notes = "分期付三方退款")
    public Object creditThirdRefund(@RequestBody JSONObject param, HttpServletRequest request) {
        log.info("分期付三方退款接口 DTO:{}", param);
        JSONObject result;
        try {
            JSONObject data = param.getJSONObject("data");
            Long flowId = data.getLong("flowId");
            BigDecimal refundAmount = data.getBigDecimal("refundAmount");
            String reason = data.getString("reason");
            String orderNo = data.getString("orderNo");
            if (Objects.isNull(flowId) || Objects.isNull(refundAmount) || Objects.isNull(orderNo)) {
                return R.fail(ErrorCodeEnum.HTTP_SEND_ERROR.getCode(), I18nUtils.get("parameters.null", getLang(request)));
            }

            result = serverService.creditThirdRefund(flowId, refundAmount, reason, orderNo, request);
        } catch (Exception e) {
            log.error("creditThirdRefund, param: {}, error message:{}, error all:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(result);
    }


    @PassToken
    @PostMapping("/refundCheck")
    @ApiOperation(value = "分期付三方退款交易结果查询", notes = "分期付三方退款交易结果查询")
    public Object creditThirdRefundCheck(@RequestBody JSONObject param, HttpServletRequest request) {
        log.info("分期付三方退款交易结果查询 DTO:{}", param);
        JSONObject result;
        try {
            String refundNo = param.getJSONObject("data").getString("refundNo");

            if (refundNo == null ) {
                return R.fail(ErrorCodeEnum.HTTP_SEND_ERROR.getCode(), I18nUtils.get("parameters.null", getLang(request)));
            }

            result = refundFlowService.creditThirdRefundCheck(refundNo, request);
        } catch (Exception e) {
            log.error("getGateWayFeeData, param: {}, error message:{}, error all:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(result);
    }


    @PassToken
    @PostMapping("/updateUserCreditSuccess")
    @ApiOperation(value = "更新用户开通分期付状态成功", notes = "更新用户开通分期付状态成功")
    public Object updateUserCreditSuccess(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("更新用户开通分期付状态成功 jsonObject :{}", jsonObject);
        try {
            Long userId = jsonObject.getJSONObject("data").getLong("userId");
            if(null == userId){
                return R.fail(ErrorCodeEnum.HTTP_SEND_ERROR.getCode(), I18nUtils.get("parameters.null", getLang(request)));
            }
            UserDTO updateUser = new UserDTO();
            updateUser.setInstallmentState(1);
            userService.updateUser(userId, updateUser, request);
        } catch (BizException e) {
            log.error("update merchant failed, jsonObject: {}, error message:{}, error all:{}", jsonObject, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }


    @SignVerify
    @PassToken
    @PostMapping("/getOnePayUser")
    @ApiOperation(value = "获取支付用户状态", notes = "获取支付用户状态")
    public Object getOnePayUser(@RequestBody JSONObject data, HttpServletRequest request) {
        log.info("获取支付用户状态:{}", data);
        if(null == data || null == data.getJSONObject("data") || null == data.getJSONObject("data").getLong("userId")){
            return R.fail(ErrorCodeEnum.HTTP_SEND_ERROR.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }
        try {
            return R.success(userService.getUserCardStateAndBindCardDate(data.getJSONObject("data").getLong("userId"), request));
        } catch (Exception e) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @PassToken
    @PostMapping("/getCardExpiredTimeData")
    @ApiOperation(value = "获取卡的过期日信息", notes = "获取卡的过期日信息")
    public Object getCardExpiredTimeData(@RequestBody JSONObject param, HttpServletRequest request) {
        log.info("getCardExpiredTimeData DTO:{}", param);
        JSONObject result;
        if(null == param.getJSONObject("data") || StringUtils.isBlank(param.getJSONObject("data").getString("token"))){
            return R.fail(ErrorCodeEnum.HTTP_SEND_ERROR.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }
        try {
            result = userService.getCardDetails(param.getJSONObject("data").getString("token"), request);
        } catch (Exception e) {
            log.error("getCardExpiredTimeData, param: {}, error message:{}, error all:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.HTTP_SEND_ERROR.getCode(), e.getMessage());
        }
        return R.success(result);
    }
    @PassToken
    @PostMapping("/stripeWebhook")
    @ApiOperation(value = "监听stripe推送", notes = "监听stripe推送",consumes="application/json",produces="application/json")
    public Object stripeWebhook(@RequestBody String param, HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("监听stripe推送:{}", param);
        String sigHeader = request.getHeader("Stripe-Signature");
        Event event = null;
        try {
            event = Webhook.constructEvent(
                    param, sigHeader, endpointSecret
            );
            stripeAPIService.stripeRefundNotice(event,request);
        } catch (Exception e) {
            log.error("校验推送异常:{}",e);
            response.setStatus(400);
            throw new BizException("Invalid signature");
        }
        return R.success();
    }




    @SignVerify
    @PassToken
    @ApiOperation(value = "app获取默认卡信息 lapay获取latpay stripe获取stripe", notes = "app获取默认卡信息")
    @PostMapping("/defaultCardInfo")
//    @PassToken
    public Object getDefaultCardInfo(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("app获取默认卡信息 , data:{}", requestInfo);
        JSONObject dataJsonObj = requestInfo.getJSONObject("data");
        if(null == dataJsonObj || null == dataJsonObj.getLong("userId")){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }
        try {
            return R.success(userService.getDefaultCardInfo(dataJsonObj.getLong("userId"), request));
//            return R.success(userService.getDefaultCardInfo(646214090148630528L, request));
        } catch (Exception e) {
            log.error("get user card list failed, error msg:{}, error:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed", getLang(request)));
        }
    }


}
