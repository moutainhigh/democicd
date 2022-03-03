package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.JSONResultHandle;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.filter.*;
import com.uwallet.pay.main.model.dto.UserDTO;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Decoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 三方服务
 * </p>
 *
 * @description: 三方服务
 * @author: zhoutt
 * @date: Created in 2020-09-24 16:41:37
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */

@RestController
@RequestMapping("/externalService")
@Slf4j
@Api("外部三方服务专用交互controller")
public class ExternalServiceController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private QrcodeInfoService qrcodeInfoService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private StaticDataService staticDataService;

    @Autowired
    private QrPayFlowService qrPayFlowService;

    @Autowired
    private AccessPlatformService accessPlatformService;

    @Value("${spring.IntroductionPath}")
    private String IntroductionPath;

    @Value("${spring.paperPath}")
    private String paperPath;

    @Value("${apiOrder.url}")
    private String apiOrderUrl;

    @PassToken
    @ApiOperation(value = "接入方登陆")
    @PostMapping("/accessPartyLogin")
    public Object accessPartyLogin(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("access party login, data:{}", requestInfo);
        String token;
        try {
            token = accessPlatformService.apiEnter(requestInfo.getJSONObject("data"), request);
        } catch (Exception e) {
            log.info("access party login failed, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return token;
    }

    @SignVerify
    @AccessToken
    @ApiOperation(value = "API用户短信验证注册", notes = "API用户短信验证注册")
    @PostMapping("/userRegister")
    public Object userRegister(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("third user register DTO:{}", requestInfo);
        JSONObject userInfo = requestInfo.getJSONObject("data");
        try {
            UserDTO userDTO = JSONObject.parseObject(userInfo.toJSONString(), UserDTO.class);
            userDTO.setUserType(StaticDataEnum.USER_TYPE_10.getCode());
            userService.userRegisterParamsCheck(userDTO,request);
            userService.saveUser(userDTO, request);
        } catch (Exception e) {
            log.error("third user failed, userDTO: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }

        return R.success();
    }

    @SignVerify
    @AccessToken
    @ApiOperation(value = "用户身份验证（用户登录）")
    @PostMapping("/userValidation")
    public Object userValidation(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("externalService userValidation, data:{}", requestInfo);
        JSONObject result = null;
        try {
            result = userService.userValidation(requestInfo.getJSONObject("data"),request);
        } catch (Exception e) {
            log.info("externalService userValidation fail, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @AccessToken
    @ApiOperation(value = "三方发送验证码", notes = "三方发送验证码")
    @PostMapping("/sendSecurityCodeSMS/{phone}/{sendNode}")
    public Object sendSecurityCodeSMS(@PathVariable("phone") String phone,@PathVariable("sendNode") String sendNode, HttpServletRequest request) {
        log.info("externalService.thirdSendSecurityCodeSMS, phone:{},sendNode:{}", phone,sendNode);
        try {
            userService.phoneParamsCheck(phone,request);
            //此处用户类型只有用户
            if(sendNode.equals(StaticDataEnum.SEND_NODE_1.getCode()+"")||sendNode.equals(StaticDataEnum.SEND_NODE_3.getCode()+"")){
                userService.sendSecuritySMS(phone, sendNode, StaticDataEnum.USER_TYPE_10.getCode(), request);
            }else{
                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
            }

        } catch (Exception e) {
            log.error("externalService.thirdSendSecurityCodeSMS Exception:",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AccessToken
    @ApiOperation(value = "用户忘记密码", notes = "用户忘记密码")
    @PostMapping("/forgetPassword")
    public Object forgetPassword(@RequestBody JSONObject requestInfo, HttpServletRequest request) throws Exception {
        log.info("externalService.thirdForgetPassword DTO:{}", requestInfo);
        try {
            JSONObject userInfo = requestInfo.getJSONObject("data");
            UserDTO userDTO = JSONObject.parseObject(userInfo.toJSONString(),UserDTO.class);
            userDTO.setUserType(StaticDataEnum.USER_TYPE_10.getCode());
            userService.forgetPassword(userDTO, request);
        } catch (Exception e) {
            log.error("externalService.thirdForgetPassword failed, userDTO: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }

        return R.success();
    }

    @SignVerify
    @AccessToken
    @PostMapping("/invest/{id}")
    @ApiOperation(value = "开通/关闭分期付|开通理财业务(修改用户)", notes = "开通/关闭分期付|开通理财业务(修改用户)")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("externalService update user id:{}, user DTO:{}", id, requestInfo);
        try {
            UserDTO userDTO = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), UserDTO.class);
            userService.updateUser(id, userDTO, request);
        } catch (BizException e) {
            log.error("externalService update user failed, userDTO: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AccessToken
    @ApiToken
    @ApiOperation(value = "app获取卡列表", notes = "app获取卡列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", paramType = "path", dataType = "long", required = true),
            @ApiImplicitParam(name = "cardType", value = "卡类型 1：卡 0：账户", paramType = "query", dataType = "int", required = false)
    })
    @PostMapping("/getCardList/{userId}")
    public Object getCardList(@PathVariable("userId") Long userId, @RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("externalService getCardList user:{}", userId);
        JSONArray cardList = null;
        try {
            cardList = userService.getCardList(userId, requestInfo.getJSONObject("data").getInteger("cardType"), request);
        } catch (Exception e) {
            log.error("externalService getCardList failed, user: {}, error message:{}", userId, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed", getLang(request)));
        }

        return R.success(cardList);
    }

    @SignVerify
    @AccessToken
    @ApiToken
    @ApiOperation(value = "API绑定账户", notes = "API绑定账户")
    @PostMapping("/apiCardAdd")
    public Object apiCardAdd(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("externalService investCardAdd, data:{}", requestInfo);
        try {
            JSONObject data = requestInfo.getJSONObject("data");
            userService.tieOnCardParamsCheck(data,request);
            userService.tieOnCard(data, request);
        } catch (Exception e) {
            log.info("externalService investCardAdd failed, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            return  R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success();
    }


    @SignVerify
    @AccessToken
    @ApiToken
    @ApiOperation(value = "根据userId查询账户系统用户信息(全部)", notes = "根据userId查询账户系统用户信息(全部)")
    @PostMapping("/findUserInfoId")
    public Object findUserInfoId(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("externalService findUserInfoId , data:{}", requestInfo);
        JSONObject object;
        JSONObject data = requestInfo.getJSONObject("data");
        try {
            object = qrcodeInfoService.findUserInfoId(data, request);
        } catch (Exception e) {
            log.error("externalService findUserInfoId  failed: {}, error message:{}, e:{}", data, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed", getLang(request)));
        }
        JSONObject objects = JSONResultHandle.resultHandle(object);
        return R.success(objects);
    }




    @SignVerify
    @AccessToken
    @ApiToken
    @ApiOperation(value = "获取kyc剩余次数", notes = "获取kyc剩余次数")
    @PostMapping("/thirdKycResult")
    public Object thirdKycResult(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("externalService server kycResult DTO:{}", requestInfo);
        JSONObject result = new JSONObject();
        try {
            result = userService.getKycResult(requestInfo.getJSONObject("data").getString("userId"),request);
        } catch (Exception e) {
            log.error("externalService server kycResult, requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }

        return R.success(result);
    }

    @SignVerify
    @AccessToken
    @ApiToken
    @ApiOperation(value = "风控", notes = "风控")
    @PostMapping("/thirdRiskCheck")
    public Object thirdRiskCheck(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("externalService.thirdRiskCheck data, data:{}", requestInfo);
        JSONObject data = requestInfo.getJSONObject("data");
        JSONObject result = null;
        try {
            userService.riskCheckParamsCheck(data,request);
            result = userService.riskCheck(requestInfo.getJSONObject("data"), request).get();
        } catch (Exception e) {
            log.error("externalService.thirdRiskCheck check faile, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

//    @SignVerify
    @AccessToken
    @ApiOperation(value = "初始化订单", notes = "初始化订单")
    @PostMapping("/createApiOrder")
    public Object createApiOrder(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("create api order, data:{}", requestInfo);
        String token;
        try {
            token = qrPayFlowService.createApiOrder(requestInfo.getJSONObject("data"), request);
        } catch (Exception e) {
            log.info("create api order failed, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return apiOrderUrl + "?code=" + token;
    }

//    @SignVerify
    @AccessToken
//    @ApiToken
    @ApiOperation(value = "支付订单", notes = "支付订单")
    @PostMapping("/paymentApiOrder")
    public Object paymentApiOrder(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("payment api order, data:{}", requestInfo);
        JSONObject result = null;
        try {
            result = qrPayFlowService.paymentApiOrder(requestInfo.getJSONObject("data"), request);
        } catch (Exception e) {
            log.info("payment api order failed, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @AccessToken
    @ApiOperation(value = "折扣费率查询", notes = "折扣费率查询")
    @PostMapping("/discountRateSearch")
    public Object discountRateSearch(HttpServletRequest request) {
        log.info("discount rate search");
        JSONObject result = null;
        try {
            result = qrPayFlowService.discountRateSearch(request);
        } catch (Exception e) {
            log.info("discount rate search failed, error message:{}, e:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @AccessToken
    @ApiOperation(value = "订单计划展示", notes = "订单计划展示")
    @PostMapping("/apiOrderRepayment/{productId}")
    public Object apiOrderRepayment(@PathVariable("productId") Long productId, HttpServletRequest request) {
        log.info("api order repayment");
        JSONObject result = null;
        try {
            result = qrPayFlowService.apiOrderRepayment(productId, request);
        } catch (Exception e) {
            log.info("api order repayment failed, error message:{}, e:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @PassToken
    @SignVerify
    @ApiOperation(value = "通过多个code，查询数据字典数据", notes = "分页查询数据字典以及排序功能")
    @PostMapping(value = "/findByCodeList", name = "通过多个code，查询数据字典数据")
    public Object findByCodeList(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("code");
        if (jsonArray == null) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(),I18nUtils.get("parameters.error", getLang(request)));
        }
        List<String> list = JSONObject.parseArray(jsonArray.toJSONString(), String.class);
        String[] codeList = list.toArray(new String[list.size()]);
        Map<String, List<StaticData>> result = staticDataService.findByCodeList(codeList);
        return R.success(result);
    }

    @SignVerify
    @AccessToken
    @ApiToken
    @ApiOperation(value = "api验证用户是否激活分期付", notes = "api验证用户是否激活分期付")
    @PostMapping("/apiUserVerify")
    public Object apiUserVerify(HttpServletRequest request) {
        JSONObject result = null;
        try {
            result = serverService.apiUserVerify(request);
        } catch (Exception e) {
            log.error("api user verify faile, e:{}, error message:{}", e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @AccessToken
    @ApiToken
    @ApiOperation(value = "api激活分期付", notes = "api激活分期付")
    @PostMapping("/activationInstallment")
    public Object activationInstallment(HttpServletRequest request) {
        JSONObject result = null;
        try {
            result = serverService.activationInstallment(request);
        } catch (Exception e) {
            log.error("activationInstallment faile, e:{}, error message:{}", e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @AccessToken
    @ApiToken
    @ApiOperation(value = "api分期付绑账户", notes = "api分期付绑账户")
    @PostMapping("/creditTieOnCard")
    public Object creditTieOnCard(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("credit Tie On Card, data:{}", requestInfo);
        JSONObject data = requestInfo.getJSONObject("data");
        JSONObject result = null;
        try {
            result = serverService.creditTieOnCard(requestInfo.getJSONObject("data"), request);
        } catch (Exception e) {
            log.error("credit Tie On Card faile, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "获取银行logo", notes = "获取银行logo")
    @PostMapping("/getBankLogoList")
    public Object getBankLogo() throws Exception {
        JSONArray bankLogo = userService.getBankLogoList();
        return R.success(bankLogo);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "获取银行logo", notes = "获取银行logo")
    @GetMapping("/getBankLogo")
    public void getBankLogo(@RequestParam("bankName") String bankName, @RequestParam("type") Integer type, HttpServletResponse response) throws Exception {
        String img = userService.getBankLogo(bankName,type);
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(new BASE64Decoder().decodeBuffer(img));
        outputStream.flush();
        outputStream.close();
    }

    @PassToken
    @ApiOperation(value = "订单状态token获取", notes = "订单状态token获取")
    @PostMapping("/paymentApiOrderSearchToken")
    public Object paymentApiOrderSearchToken(@RequestBody JSONObject searchInfo, HttpServletRequest request) throws Exception {
        log.info("payment api order search token, data:{}", searchInfo);
        String token = null;
        try {
            token = qrPayFlowService.paymentApiOrderSearchToken(searchInfo, request);
        } catch (Exception e) {
            log.error("payment api order search token, data:{}, e:{}, error message:{}", searchInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(token);
    }

    @PassToken
    @ApiOperation(value = "订单状态查证", notes = "订单状态查证")
    @PostMapping("/paymentApiOrderSearch")
    public Object paymentApiOrderSearch(HttpServletRequest request) throws Exception {
        log.info("payment api order search");
        JSONObject result = null;
        try {
            result = qrPayFlowService.paymentApiOrderSearch(request);
        } catch (Exception e) {
            log.error("payment api order search, e:{}, error message:{}", e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }


}
