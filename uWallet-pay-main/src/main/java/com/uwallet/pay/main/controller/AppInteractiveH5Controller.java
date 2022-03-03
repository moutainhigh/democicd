package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.ec2.model.UpdateSecurityGroupRuleDescriptionsIngressRequest;
import com.google.common.collect.Maps;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.JSONResultHandle;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.filter.AppToken;
import com.uwallet.pay.main.filter.H5SignVerify;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.ApiQrPayFlowDTO;
import com.uwallet.pay.main.model.dto.StaticDataDTO;
import com.uwallet.pay.main.model.dto.UserDTO;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.docx4j.wml.P;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: baixinyue
 * createDate: 2010/12/5
 * description: app专用交互controller
 */

@SuppressWarnings("AlibabaUndefineMagicConstant")
@RestController
@RequestMapping("/h5/app")
@Slf4j
@Api("用户开通分期付流程需要的后端接口")
public class AppInteractiveH5Controller extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private IllionInstitutionsService illionInstitutionsService;
    @Autowired
    private StaticDataService staticDataService;
    @Autowired
    private ApiQrPayFlowService apiQrPayFlowService;
    @Autowired
    private ServerService serverService;

    @Resource
    private CardService cardService ;


    @H5SignVerify
    @AppToken
    @ApiOperation(value = "根据userId查询用户信息|支付业务状态|分期付业务状态|理财业务状态", notes = "根据userId查询用户信息|支付业务状态|分期付业务状态|理财业务状态")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @PostMapping(value = "/user/{id}", name = "详情")
    public Object userH5(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("get user Id:{}", id);
        //todo 换原生方法
//        UserDTO userDTO = userService.findUserById(id);
        UserDTO userDTO = null;

        //新增分期付绑卡状态
        Long versionId = Long.valueOf(request.getHeader("versionId"));
        if(versionId.compareTo(574146348944281601L) >= 0){
            userDTO = userService.findUserInfoV2(id);
        }else {
            userDTO = userService.findUserById(id);
        }

        JSONObject object = JSONResultHandle.resultHandle(userDTO, UserDTO.class);
        return R.success(object);
    }
    @H5SignVerify
    @AppToken
    @ApiOperation(value = "风控-上送客户证件-KYC信息, 出风口结果接口", notes = "风控-上送客户证件-KYC信息, 出风口结果接口")
    @PostMapping("/riskCheckNew")
    public Object riskCheckNewH5(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("risk data, data:{}", requestInfo);
        JSONObject data = requestInfo.getJSONObject("data");
        JSONObject result = null;
        try {
            // 查询账户系统信息
            JSONObject userInfo = userService.findUserInfo(request);
            if (userInfo==null){
                return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), "System Error");
            }
            data.put("userLastName",userInfo.getString("userLastName"));
            data.put("userMiddleName",userInfo.getString("userMiddleName"));
            data.put("email",userInfo.getString("email"));
            data.put("sex",userInfo.getInteger("sex"));
            data.put("birth",userInfo.getString("birth"));
            data.put("userFirstName",userInfo.getString("userFirstName"));
            data.put("userId",userInfo.getLong("userId"));
            userService.riskCheckParamsCheck(data, request);
            result = userService.riskCheckNew(data, request).get();
        } catch (Exception e) {
            log.error("risk check faile, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success(result);
    }
    @H5SignVerify
    @AppToken
    @ApiOperation(value = "获取机构列表", notes = "获取机构列表")
    @PostMapping("/getInstitutions")
    public Object getInstitutionsH5(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("获取机构列表, data:{}", requestInfo);
        try{
            JSONObject data = requestInfo.getJSONObject("data");
            JSONObject institutions = illionInstitutionsService.getInstitutions(data,request);
            PagingContext pc = institutions.getObject("pc", PagingContext.class);
            institutions.remove("pc");
            return R.success(institutions,pc);
        }catch (Exception e){
            log.error("获取机构列表,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @H5SignVerify
    @AppToken
    @ApiOperation(value = "预加载机构信息", notes = "预加载机构信息")
    @PostMapping("/preload")
    public Object preloadH5(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("预加载机构信息, data:{}", requestInfo);
        try{
            return R.success(illionInstitutionsService.preload(requestInfo.getJSONObject("data"), request));
        }catch (Exception e){
            log.error("预加载机构信息,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @H5SignVerify
    @AppToken
    @ApiOperation(value = "登录并获取报告", notes = "登录并获取报告")
    @PostMapping("/fetchAll")
    public Object fetchAllH5(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("登录并获取报告,userId:{}",getUserId(request));
        try{
            return R.success(illionInstitutionsService.fetchAll(requestInfo.getJSONObject("data"), request));
        }catch (Exception e){
            log.error("登录并获取报告,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @H5SignVerify
    @AppToken
    @ApiOperation(value = "二次验证获取illion报告", notes = "二次验证获取illion报告")
    @PostMapping("/mfaSubmit")
    public Object mfaRequestH5(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("二次验证获取illion报告,userId:{}",getUserId(request));
        try {
            return R.success(illionInstitutionsService.mfaInfoSubmit(jsonObject.getJSONObject("data"),request));
        } catch (Exception e) {
            log.error("二次验证获取illion报告异常,e:{}, error message:{}", e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @H5SignVerify
//    @PassToken
    @AppToken
    @ApiOperation(value = "将卡设置为默认卡", notes = "将卡设置为默认卡")
    @PostMapping("/presetCard")
    public Object defaultCardH5(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("将卡设置为默认卡, data:{}", requestInfo);
        try{
            userService.presetCard(requestInfo.getJSONObject("data"), getUserId(request), request);
            return R.success();
        }catch (Exception e){
            log.error("将卡设置为默认卡,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @H5SignVerify
    @AppToken
    @ApiOperation(value = "添加银行卡接口", notes = "添加银行卡接口")
    @PostMapping("/tieOnCard")
    public Object tieOnCardH5(@RequestBody JSONObject object, HttpServletRequest request) {
        log.info("tie on card data:{}", object);
        JSONObject data = object.getJSONObject("data");
        Long id = null;
        try {
            data.put("isBindCard",StaticDataEnum.CREAT_BIND_CARD_1.getCode());
            userService.tieOnCardParamsCheck(data, request);
            id = userService.tieOnCard(data, request);
        } catch (Exception e) {
            String message = e.getMessage();
            log.error("tie on card failed: {}, error message:{}", object, message, e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), message);
        }

        return R.success(id.toString());
    }
//    @H5SignVerify
//    @AppToken
//    @ApiOperation(value = "用户卡列表查询接口", notes = "用户卡列表查询接口")
//    @PostMapping("/getCardList")
//    public Object getCardListH5(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
//        log.info("get user card list v2新版, desc排序版 , data:{}", requestInfo);
//        List<JSONObject> cardList = null;
//        try {
//            JSONObject data = requestInfo.getJSONObject("data");
//            if (data.isEmpty()){
//                throw new BizException(I18nUtils.get("parameters.null",getLang(request)));
//            }
//            Long userId = getUserId(request);
//            cardList = userService.getCardListEpoch(userId, data.getInteger("cardType"), request);
//
//            if ( cardList.size() > 0){
//                for (int i = 0; i < cardList.size(); i ++){
//                    JSONObject object = cardList.get(i);
//                    try{
//                        JSONObject result = userService.getCardDetails(object.getString("crdStrgToken"), request);
//                        String customerCcExpyr = result.getString("customerCcExpyr");
//                        String customerCcExpmo = result.getString("customerCcExpmo");
//                        object.put("customerCcExpyr",customerCcExpyr);
//                        object.put("customerCcExpmo",customerCcExpmo);
//                    }catch (Exception e){
//                        log.error("获取获取卡过期日异常,e:{}",e.getMessage());
//                    }
//                }
//
//            }
//
//        } catch (Exception e) {
//            log.error("get user card list failed, error msg:{}, error:{}", e.getMessage(), e);
//            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed", getLang(request)));
//        }
//        return R.success(cardList);
//    }

    @H5SignVerify
    @AppToken
    @ApiOperation(value = "更新latpay卡信息", notes = "更新latpay卡信息")
    @PostMapping("/updateLatpayCardInfo")
    public Object updateLatpayCardInfoH5(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("更新latpay卡信息, data:{}", requestInfo);
        JSONObject result = null;
        try {
            JSONObject data = requestInfo.getJSONObject("data");
            data.put("userId",getUserId(request));
            result = userService.updateLatpayCardInfo(data, request);
        } catch (Exception e) {
            log.error("更新latpay卡信息失败,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @H5SignVerify
//    @PassToken
    @AppToken
    @ApiOperation(value = "订单商户详情查询接口", notes = "订单商户详情查询接口")
    @PostMapping("/postCheckOrder")
    public Object postCheckOrder(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("订单商户详情查询接口, data:{}", requestInfo);
        JSONObject result = null;
        try {
            JSONObject data = requestInfo.getJSONObject("data");
            Long orderId = Long.valueOf(data.getString("orderId")).longValue();
            result = apiQrPayFlowService.postCheckOrderImpl(orderId, request);
            if( null == result || result.isEmpty()){
                return R.h5Fail(ErrorCodeEnum.H5_FAIL_CODE_ERROR.getCode(),ErrorCodeEnum.H5_FAIL_CODE_ERROR.getMessage());
            }
        } catch (Exception e) {
            log.error("订单商户详情查询接口,error message:{},e:{}",e.getMessage(),e);
            return R.h5Fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }
    @H5SignVerify
    @AppToken
    @ApiOperation(value = "老用户确认订单", notes = "老用户确认订单")
    @PostMapping("/postConfirmOrder")
    public Object postConfirmOrder(@RequestBody JSONObject requestInfo, HttpServletRequest request) {

        log.info("老用户确认订单, data:{}", requestInfo);
        int count = 1;
        try {
            JSONObject data = requestInfo.getJSONObject("data");
            String orderId = data.getString("orderId");
            Long id = Long.valueOf(orderId);
            String donateAmount = data.getString("donateAmount");
            String orderStatus = data.getString("orderStatus");
            String cardId = data.getString("cardId");
            Long cardIds = Long.valueOf(cardId);
            Long userId = getUserId(request);

            ApiQrPayFlowDTO dto = new ApiQrPayFlowDTO();
            dto.setDonateAmount(new BigDecimal(donateAmount));
            dto.setUserId(userId);
            dto.setCardId(cardIds);

            ApiQrPayFlowDTO apiQrPayFlowApi = apiQrPayFlowService.findApiQrPayFlowById(id);

            if(apiQrPayFlowApi.getOrderStatus() == StaticDataEnum.H5_ORDER_TYPE_2.getCode() || apiQrPayFlowApi.getOrderStatus() == StaticDataEnum.H5_ORDER_TYPE_3.getCode()){
                count = 2;
                return R.success(count);
            }

            Long exps= apiQrPayFlowApi.getExpirationTime();
            Long now = System.currentTimeMillis();
            Boolean expDate = exps.longValue() < now.longValue();
            if(expDate){
                dto.setOrderStatus(StaticDataEnum.H5_ORDER_TYPE_2.getCode());
                apiQrPayFlowService.updateApiQrPayFlow(id, dto, request);
                count = 2;
            }else{
                dto.setOrderStatus(Integer.valueOf(orderStatus));
                apiQrPayFlowService.updateApiQrPayFlow(id, dto, request);
            }
        } catch (Exception e) {
            log.error("老用户确认订单,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(count);
    }
    @H5SignVerify
    @PassToken
    @ApiOperation(value = "发送验证码接口", notes = "发送验证码接口")
    @PostMapping("/sendMessage")
    public Object sendMessage(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("发送验证码接口, data:{}", requestInfo);
        JSONObject result = null;
        try {
            JSONObject data = requestInfo.getJSONObject("data");
            userService.sendMessage(data, request);
        } catch (Exception e) {
            log.error("更新latpay卡信息失败,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }
    @H5SignVerify
    @PassToken
    @ApiOperation(value = "h5登陆接口", notes = "h5登陆接口")
    @PostMapping("/h5Login")
    public Object h5Login(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("h5登陆接口, data:{}", requestInfo);
        JSONObject result = null;
        try {
            JSONObject data = requestInfo.getJSONObject("data");
            result =userService.h5Login(data, request);
        } catch (Exception e) {
            log.error("h5登陆接口异常,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }
    @H5SignVerify
    @PassToken
    @ApiOperation(value = "通过多个code，查询数据字典数据", notes = "分页查询数据字典以及排序功能")
    @PostMapping(value = "/findByCodeList", name = "通过多个code，查询数据字典数据")
    public Object findByCodeList(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("code");
        if (jsonArray == null) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.error", getLang(request)));
        }

        List<String> list = JSONObject.parseArray(jsonArray.toJSONString(), String.class);
        String[] codeList = list.toArray(new String[list.size()]);
        Map<String, List<StaticData>> result = staticDataService.findByCodeList(codeList);
        return R.success(result);
    }
    @H5SignVerify
    @AppToken
    @ApiOperation(value = "获取用户分期付进行状态", notes = "获取用户分期付进行状态")
    @PostMapping("/verify")
    public Object verify(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("获取用户分期付进行状态, data:{}", requestInfo);
        JSONObject result=null;
        try {
            result=userService.verify(requestInfo, request);
        } catch (Exception e) {
            log.error("获取用户分期付进行状态异常,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }
    @H5SignVerify
    @AppToken
    @ApiOperation(value = "查询用户信息", notes = "查询用户信息")
    @PostMapping("/findUserInfo")
    public Object findUserInfo(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("查询用户信息, data:{}", requestInfo);
        JSONObject userDTO=null;
        try {
            userDTO=userService.findUserInfo(request);
        } catch (Exception e) {
            log.error("获取用户分期付进行状态异常,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(userDTO);
    }
    @H5SignVerify
    @PassToken
    @ApiOperation(value = "h5注册", notes = "h5注册")
    @PostMapping("/userRegister")
    public Object userRegister(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("h5注册, data:{}", requestInfo);
        JSONObject result=null;
        try {

            result= userService.userRegister(requestInfo.getJSONObject("data"),request);
        } catch (Exception e) {
            log.error("h5注册异常,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @H5SignVerify
    @AppToken
    @ApiOperation(value = "新用户确认订单+绑卡", notes = "新用户确认订单+绑卡")
    @PostMapping("/postNewUserConfirmOrder")
    public Object postNewUserConfirmOrder(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("老用户确认订单, data:{}", requestInfo);
        int count = 1;
        Long userId = getUserId(request);
        try {
            String orderId = requestInfo.getString("orderId");
            Long id = Long.valueOf(orderId);
            String donateAmount = requestInfo.getString("donateAmount");
            String orderStatus = requestInfo.getString("orderStatus");
            String cardId = requestInfo.getString("cardId");
            Long cardIds = Long.valueOf(cardId);

            ApiQrPayFlowDTO dto = new ApiQrPayFlowDTO();
            dto.setDonateAmount(new BigDecimal(donateAmount));
            dto.setUserId(userId);
            dto.setCardId(cardIds);

            ApiQrPayFlowDTO apiQrPayFlowApi = apiQrPayFlowService.findApiQrPayFlowById(id);

            if(apiQrPayFlowApi.getOrderStatus() == StaticDataEnum.H5_ORDER_TYPE_2.getCode() || apiQrPayFlowApi.getOrderStatus() == StaticDataEnum.H5_ORDER_TYPE_2.getCode()){
                count = 2;
                return R.success(count);
            }

            Long exps= apiQrPayFlowApi.getExpirationTime();
            Long now = System.currentTimeMillis();
            Boolean expDate = exps.longValue() < now.longValue();
            if(expDate){
                dto.setOrderStatus(2);
                apiQrPayFlowService.updateApiQrPayFlow(id, dto, request);
                count = 2;
            }else{
                dto.setOrderStatus(Integer.valueOf(orderStatus));
                apiQrPayFlowService.updateApiQrPayFlow(id, dto, request);
            }
        } catch (Exception e) {
            log.error("老用户确认订单,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(count);
    }
    @AppToken
    @H5SignVerify
    @ApiOperation(value = "获取用户分期付额度", notes = "获取用户分期付额度")
    @PostMapping("/userCreditMessage")
    public Object userCreditMessage(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("获取用户分期付额度, data:{}", requestInfo);
        JSONObject result=null;
        try {
            result=userService.userCreditMessage(requestInfo, request);
        } catch (Exception e) {
            log.error("获取用户分期付额度异常,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }
    @AppToken
    @H5SignVerify
    @ApiOperation(value = "手动激活用户分期付", notes = "手动激活用户分期付")
    @PostMapping("/activationInstallment")
    public Object activationInstallment(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("手动激活用户分期付, data:{}", requestInfo);
        JSONObject result=null;
        try {
            result=userService.activationInstallment(requestInfo, request);
        } catch (Exception e) {
            log.error("手动激活用户分期付异常,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }
    @AppToken
    @H5SignVerify
    @ApiOperation(value = "查询是否接收到银行报告", notes = "查询是否接收到银行报告")
    @PostMapping("/bankStatementsIsResult")
    public Object bankStatementsIsResult(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("查询是否接收到银行报告, data:{}", requestInfo);
        JSONObject result=null;
        try {
                result=userService.bankStatementsIsResult(requestInfo, request);
        } catch (Exception e) {
            log.error("查询是否接收到银行报告异常,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @H5SignVerify
    @AppToken
    @ApiOperation(value = "成功失败跳转接口", notes = "成功失败跳转接口")
    @PostMapping("/successFailUrl/{id}")
    public Object successFailUrl(@PathVariable("id") String id, HttpServletRequest request) {
        log.info("成功失败跳转接口, data:{}", id);
        JSONObject params = new JSONObject(5);
        try {
            String ids = id.replace(" ","");
            ApiQrPayFlowDTO apiQrPayFlowDTO = apiQrPayFlowService.findApiQrPayFlowById(Long.valueOf(ids));
            params.put("reference", apiQrPayFlowDTO.getApiTransNo());
            params.put("confirmation_url", apiQrPayFlowDTO.getConfirmationUrl());
            params.put("cancellation_url", apiQrPayFlowDTO.getCancellationUrl());
        } catch (Exception e) {
            log.error("成功失败跳转接口,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(params);
    }

    @H5SignVerify
    @PassToken
    @ApiOperation(value = "订单状态", notes = "订单状态")
    @PostMapping("/selectOrderSattus")
    public Object selectOrderSattus(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("成功失败跳转接口, data:{}", requestInfo);
        JSONObject params = new JSONObject(5);
        try {
            JSONObject data = requestInfo.getJSONObject("data");
            Long id = Long.valueOf(data.getString("orderId").replace(" ",""));
            ApiQrPayFlowDTO apiQrPayFlowDTO = apiQrPayFlowService.findApiQrPayFlowById(Long.valueOf(id));
            if(null == apiQrPayFlowDTO){
                return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(),"order not exist");
            }
            params.put("orderStatus", apiQrPayFlowDTO.getOrderStatus());
            params.put("reference", apiQrPayFlowDTO.getApiTransNo());
            params.put("confirmation_url", apiQrPayFlowDTO.getConfirmationUrl());
            params.put("cancellation_url", apiQrPayFlowDTO.getCancellationUrl());
        } catch (Exception e) {
            log.error("成功失败跳转接口,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(params);
    }

    @H5SignVerify
    @PassToken
    @ApiOperation(value = "关闭订单", notes = "关闭订单")
    @PostMapping("/orderClose")
    public Object orderClose(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("关闭订单接口, data:{}", requestInfo);
        JSONObject params = new JSONObject(5);
        try {
            String orderId = requestInfo.getString("orderId").replace(" ","");
            ApiQrPayFlowDTO dto = new ApiQrPayFlowDTO();
            dto.setOrderStatus(2);
            apiQrPayFlowService.updateApiQrPayFlow(Long.valueOf(orderId), dto, request);
            ApiQrPayFlowDTO apiQrPayFlowDTO = apiQrPayFlowService.findApiQrPayFlowById(Long.valueOf(orderId));
            params.put("orderStatus", apiQrPayFlowDTO.getOrderStatus());
            params.put("reference", apiQrPayFlowDTO.getApiTransNo());
            params.put("confirmation_url", apiQrPayFlowDTO.getConfirmationUrl());
            params.put("cancellation_url", apiQrPayFlowDTO.getCancellationUrl());
        } catch (Exception e) {
            log.error("关闭订单接口,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(params);
    }


    @PassToken
    @ApiOperation(value = "获取文件流信息", notes = "获取文件流信息")
    @GetMapping("/getFileStream")
    public void getFileStream(@RequestParam String code, HttpServletRequest request, HttpServletResponse response) {
        log.info("获取文件流信息, data:{}", code);
        if(StringUtils.isBlank(code)){
            return ;
        }

        JSONObject params = new JSONObject(5);
        params.put("code", code);
        StaticDataDTO staticData = staticDataService.findOneStaticData(params);
        if(null == staticData || null == staticData.getId() || StringUtils.isBlank(staticData.getName())){
            return ;
        }

        try{
            String filePath = staticData.getName();
            String[] split = staticData.getName().split("/");
            String fileName = split[split.length - 1];

            response.setCharacterEncoding("UTF-8");
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("utf-8"), "ISO8859-1"));
//        response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
            response.setHeader("FileName", fileName);

            URL url = new URL(filePath);
            URLConnection conn = url.openConnection();
            InputStream inputStream = conn.getInputStream();

            byte[] buffer = new byte[1024];
            int len;
            OutputStream outputStream = response.getOutputStream();
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            outputStream.close();
        }catch (IOException e){
            log.error("获取文件流信息出错,error message:{},e:{}",e.getMessage(),e);

        }
    }


    //对接Stripe----将原有Latpay支付通道更换为Stripe支付通道

//        @PassToken
    @H5SignVerify
    @AppToken
    @ApiOperation(value = "stripe通过卡token绑卡", notes = "stripe通过卡token绑卡")
    @PostMapping(value = "/h5stripeBindCardByToken", name = "stripe通过卡token绑卡")
    public Object stripeBindCardByToken(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("stripe通过卡token绑卡  , data:{}",requestInfo);
        JSONObject dataJsonObj = requestInfo.getJSONObject("data");

//        if(null == getUserId(request) || StringUtils.isBlank(dataJsonObj.getString("cardToken"))){
//            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
//        }

        if(null == getUserId(request) || StringUtils.isBlank(dataJsonObj.getString("cardToken"))
        || StringUtils.isBlank(dataJsonObj.getString("cardType"))){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }

        HashMap<String, Object> categoryMap = Maps.newHashMapWithExpectedSize(3);
        String cardTypes = dataJsonObj.getString("cardType");
        categoryMap.put("value", cardTypes);
        categoryMap.put("code", "cardType");
        StaticDataDTO staticData = staticDataService.findOneStaticData(categoryMap);
        if( null == staticData){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }
        try {
            Long carId = cardService.bindCard(dataJsonObj.getString("cardToken"), getUserId(request), dataJsonObj.getInteger("creditCardAgreementState"), request);
            String carIds = String.valueOf(carId);
            log.info("success,data{}", carIds);
            return R.success(carIds);
//            return R.success(cardService.bindCard(dataJsonObj.getString("cardToken"), getUserId(request), dataJsonObj.getInteger("creditCardAgreementState"), request));
        } catch (Exception e) {
            log.error("stripe通过卡token绑卡 failed, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @H5SignVerify
    @AppToken
//    @PassToken
    @ApiOperation(value = "stripe通道卡列表", notes = "stripe通道卡列表")
    @PostMapping(value = "/getCardList", name = "stripe通道卡列表")
    public Object stripeCardList(HttpServletRequest request) {
        log.info("stripe通道卡列表 query , request,userId", request ,getUserId(request));
        try {
//            Long userId = 656022324187320320L;
//            JSONArray cardListRes = cardService.getStripeCardList(userId, request);
            JSONArray cardListRes = cardService.getStripeCardList(getUserId(request), request);
            for(int i = 0; i < cardListRes.size(); i ++){
                JSONObject obj = cardListRes.getJSONObject(i);
                String accountId = obj.get("accountId").toString();
                String id = obj.get("id").toString();
                cardListRes.getJSONObject(i).put("accountId", accountId);
                cardListRes.getJSONObject(i).put("id", id);
            }

            return R.success(cardListRes);
        } catch (Exception e) {
            log.error("stripe通道卡列表 failed,request,userId, e:{}, error message:{}", request,getUserId(request), e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


}
