package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.constant.DataEnum;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.filter.AppToken;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.filter.SignVerify;
import com.uwallet.pay.main.model.dto.AppExclusiveBannerDTO;
import com.uwallet.pay.main.model.dto.UserEnterAppPageLogDTO;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.service.impl.TransRecordService;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * author: aaron S
 * createDate: 2021/04/07
 * description: 2021-4-07 UI 大改版 controller
 */
@RestController
@RequestMapping("/appEpoch")
@Slf4j
@Api("app专用交互controller")
public class AppEpochController extends BaseController {

    @Resource
    private UserService userService;
    @Resource
    private NoticeService noticeService;
    @Resource
    private FavoriteMerchantService favoriteMerchantService;
    @Resource
    private MerchantListService merchantListService;
    @Resource
    private GatewayService gatewayService;

    @Resource
    private AppExclusiveBannerService appExclusiveBannerService;

    @Resource
    private AppCustomCategoryDisplayStateService appCustomCategoryDisplayStateService;


    @Resource
    private TransRecordService transRecordService;


    @Resource
    private EnterKycPageLogService enterKycPageLogService;

    @Resource
    private UserEnterAppPageLogService userEnterAppPageLogService;

    @Resource
    private MarketingManagementService marketingManagementService ;

    @Resource
    private StripeBusinessService stripeBusinessService ;
    @Resource
    private CardService cardService ;


    @SignVerify
    @AppToken
    @ApiOperation(value = "用户新增/删除favorite商户数据", notes = "用户新增/删除favorite商户数据")
    @PostMapping("/addNewFavorite")
    public Object addNewFavorite(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("用户新增/删除favorite商户数据, data:{},", requestInfo);
        try {
            favoriteMerchantService.addNewFavorite(requestInfo.getJSONObject("data"), request);
        } catch (Exception e) {
            log.error("用户新增/删除favorite商户数据.error msg:{}, error:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
//    @PassToken
    @ApiOperation(value = "获取APP主页banner数据", notes = "获取APP主页banner数据")
    @PostMapping("/getAppHomePageTopBanner")
    @ResponseBody
    public Object getAppHomePageTopBanner(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("获取APP主页banner数据, data:{},", requestInfo);
        try {
            return R.success(appExclusiveBannerService.getAppHomePageTopBanner(requestInfo, request));
        } catch (Exception e) {
            log.error("获取APP主页banner数据 ,error msg:{}, error:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @SignVerify
    @AppToken
//    @PassToken
    @ApiOperation(value = "获取APP主页市场推广、自定义分类展示数据", notes = "获取APP主页市场推广、自定义分类展示数据")
    @PostMapping("/getAppHomePageBottomData")
    @ResponseBody
    public Object getAppHomePageBottomData(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("获取APP主页市场推广、自定义分类展示数据, data:{},", requestInfo);
        try {
            return R.success(appExclusiveBannerService.listAllAppHomePageBottomData(requestInfo, request));
        } catch (Exception e) {
            log.error("获取APP主页市场推广、自定义分类展示数据 error msg:{}, error:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @SignVerify
    @AppToken
//    @PassToken
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @ApiOperation(value = "获取APP主页市场推广view all数据", notes = "获取APP主页市场推广view all数据")
    @PostMapping("/getAppHomePageViewAllExclusiveData")
    @ResponseBody
    public Object getAppHomePageViewAllExclusiveData(HttpServletRequest request, @RequestBody JSONObject requestInfo) {
        Map<String, Object> params = getConditionsMap(request);
        params.put("state", 1);
        params.put("displayType", DataEnum.APP_DISPLAY_TYPE_EXCLUSIVE.getCode());
        int total = appExclusiveBannerService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<AppExclusiveBannerDTO> list = new ArrayList<>();
        //设置状态为可用
        pc = getPagingContext(requestInfo.getJSONObject("data"), total);
        if (total > 0) {
            scs = getSortingContext(request);
            SortingContext s = new SortingContext("display_order", "asc");
            scs.add(s);
            list = appExclusiveBannerService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }


    @SignVerify
    @AppToken
//    @PassToken
    @ApiOperation(value = "获取APP主页自定义分类view all图片数据", notes = "获取APP主页自定义分类view all图片数据")
    @PostMapping("/getAppHomePageCategoryAllImgData")
    @ResponseBody
    public Object getAppHomePageCategoryAllImgData(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("获取APP主页自定义分类view all图片数据, data:{},", requestInfo);
        try {
            return R.success(appCustomCategoryDisplayStateService.listAllAppHomePageCategoryAllImgData(requestInfo, request));
        } catch (Exception e) {
            log.error("获取APP主页自定义分类view all图片数据 error msg:{}, error:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "获取分页展示我的收藏列表", notes = "获取分页展示我的收藏列表")
    @PostMapping("/favoriteList")
    public Object favoriteList(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("获取分页展示我的收藏列表, data:{},", requestInfo);
        List<JSONObject> results = new ArrayList<>();
        Long userId = getUserId(request);
        JSONObject data = requestInfo.getJSONObject("data");
        data.put("userId", userId);
        int total = favoriteMerchantService.countFavorite(data);
        PagingContext pc;
        Vector<SortingContext> scs;
        pc = getPagingContext(data, total);
        if (total > 0) {
            scs = getSortingContext(data);
            results = favoriteMerchantService.findListWithMerchantInfo(data, scs, pc);
        }
        return R.success(results, pc);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "首页一键清除所有通知信息", notes = "首页一键清除所有通知信息")
    @PostMapping("/noticeClearAll")
    public Object addNewFavorite(HttpServletRequest request) {
        Long userId = getUserId(request);
        log.info("首页一键清除所有通知信息, 用户Id:{},", userId);
        try {
            noticeService.noticeClearAll(userId, request);
        } catch (Exception e) {
            log.error("首页一键清除所有通知信息, error msg:{}, error:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "商户搜索列表", notes = "商户搜索列表")
    @PostMapping("/merchantList")
    public Object merchantList(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("商户搜索列表v2新版, data:{},", requestInfo);
        try {
            JSONObject res = merchantListService.merchantList(requestInfo.getJSONObject("data"), request);
            return R.success(res.get("list"), JSONObject.toJavaObject(res.getJSONObject("pc"), PagingContext.class));
        } catch (Exception e) {
            log.error("商户搜索列表.error msg:{}, error:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "app获取卡列表", notes = "app获取卡列表")
    @PostMapping("/getCardList")
    public Object getCardList(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("get user card list v2新版, desc排序版 , data:{}", requestInfo);
        List<JSONObject> cardList = null;
        try {
            JSONObject data = requestInfo.getJSONObject("data");
            if (data.isEmpty()) {
                throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
            }
            Long userId = getUserId(request);
            cardList = userService.getCardListEpoch(userId, data.getInteger("cardType"), request);
        } catch (Exception e) {
            log.error("get user card list failed, error msg:{}, error:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed", getLang(request)));
        }
        return R.success(cardList);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "通过订单查询交易详情", notes = "通过订单查询交易详情")
    @PostMapping(value = "/getRecordDetail/{transNo}", name = "通过订单查询交易详情")
    public Object getRecordDetailByTransNo(@PathVariable("transNo") String transNo, HttpServletRequest request) {
        log.info("APP- 通过订单查询交易详情, qrPayId:{}", transNo);
        try {

            return R.success(transRecordService.getRecordDetailByTransNo(transNo, request));
        } catch (Exception e) {
            log.error("APP- 通过订单查询交易详情, qrPayId:{},  error message:{}", transNo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @SignVerify
    @AppToken
//    @PassToken
    @ApiOperation(value = "通过订单查询交易详情", notes = "通过订单查询交易详情")
    @PostMapping(value = "/v2/getRecordDetail/{transNo}", name = "通过订单查询交易详情")
    public Object getRecordDetailByTransNoV2(@PathVariable("transNo") String transNo, HttpServletRequest request) {
        log.info("APP- 通过订单查询交易详情, qrPayId:{}", transNo);
        try {
            return R.success(transRecordService.getRecordDetailByTransNoV2(transNo, request));
        } catch (Exception e) {
            log.error("APP- 通过订单查询交易详情, qrPayId:{},  error message:{}", transNo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @SignVerify
    @AppToken
    @PassToken
    @ApiOperation(value = "获取交易明细-卡消费+分期付", notes = "获取交易明细-卡消费+分期付")
    @PostMapping("/getRecordNew")
    public Object getTransactionRecordNew(@NonNull @RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("APP- 获取交易明细-卡消费+分期付, data:{}", requestInfo);
        try {
            JSONObject data = requestInfo.getJSONObject("data");
            data.put("userId", getUserId(request));
            // pack/re-format search param
            data = transRecordService.formatParamJson(data);
            int total;
            PagingContext pc;
            Vector<SortingContext> scs;
            Object resultList = null;
            total = transRecordService.countRecordNew(data);
            pc = getPagingContext(data, total);
            if (total > 0) {
                scs = getSortingContext(data);
                resultList = transRecordService.transactionDetailsNew(data, scs, pc, request);
            }
            return R.success(resultList, pc);
        } catch (Exception e) {
            log.info("APP- 获取交易明细-卡消费+分期付失败, data:{},error:{}", requestInfo, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "验证当前用户 密码是否正确", notes = "验证当前用户 密码是否正确")
    @PostMapping(value = "/verifyUserPassword/{pwd}", name = "验证当前用户 密码是否正确")
    public Object verifyUserPassword(@PathVariable("pwd") String pwd, HttpServletRequest request) {
        log.info("APP- 验证当前用户 密码是否正确, pwd:{}", pwd);
        try {
            return R.success(userService.verifyUserPassword(pwd, request));
        } catch (Exception e) {
            log.error("APP- 验证当前用户 密码是否正确, pwd:{},  error message:{}", pwd, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "查询当前用户邀请所获得的红包", notes = "查询当前用户邀请所获得的红包")
    @PostMapping(value = "/getReceived", name = "查询当前用户邀请所获得的红包")
    public Object getReceived(@RequestBody JSONObject param, HttpServletRequest request) {
        log.info("查询当前用户邀请所获得的红包, param:{}", param);
        try {
            JSONObject data = userService.getReceived(param.getJSONObject("data"), request);
            return R.success(data);
        } catch (Exception e) {
            log.error("查询当前用户邀请所获得的红包, pwd:{},  error message:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "修改邀请查询记录表中状态为已读", notes = "修改邀请查询记录表中状态为已读")
    @PostMapping(value = "/saveReceivedIsShow", name = "修改邀请查询记录表中状态为已读")
    public Object saveReceivedIsShow(@RequestBody JSONObject param, HttpServletRequest request) {
        log.info("修改邀请查询记录表中状态为已读, param:{}", param);
        try {
            JSONObject data = userService.saveReceivedIsShow(param.getJSONObject("data"), request);
            return R.success(data);
        } catch (Exception e) {
            log.error("修改邀请查询记录表中状态为已读, pwd:{},  error message:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "计算商户搜索页面, 是否有数据, 0:无数据, 1:有数据", notes = "计算商户搜索页面, 是否有数据, 0:无数据, 1:有数据")
    @PostMapping(value = "/haveData", name = "计算商户搜索页面, 是否有数据, 0:无数据, 1:有数据")
    public Object haveData(@RequestBody JSONObject param, HttpServletRequest request) {
        log.info("计算商户搜索页面, 是否有数据, 0:无数据, 1:有数据, param:{}", param);
        try {
            return R.success(merchantListService.merchantListHaveData(param.getJSONObject("data"), request));
        } catch (Exception e) {
            log.error("计算商户搜索页面, 是否有数据, 0:无数据, 1:有数据, data:{},  error message:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "查询用户是否收藏该商户", notes = "查询用户是否收藏该商户")
    @PostMapping(value = "/isUserFavorite/{merchantId}", name = "查询用户是否收藏该商户")
    public Object isUserFavorite(@PathVariable("merchantId") Long merchantId, HttpServletRequest request) {
        log.info("查询用户是否收藏该商户, merchantId:{}", merchantId);
        try {
            return R.success(favoriteMerchantService.isUserFavorite(merchantId, request));
        } catch (Exception e) {
            log.error("查询用户是否收藏该商户, merchantId:{},  error message:{}", merchantId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @SignVerify
    @PassToken
    @ApiOperation(value = "用户注册校验验证码是否正确", notes = "用户注册校验验证码是否正确")
    @PostMapping(value = "/verifyCode", name = "用户注册校验验证码是否正确")
    public Object verifyCode(@RequestBody JSONObject param, HttpServletRequest request) {
        log.info("用户注册校验验证码是否正确, param:{}", param);
        try {
            JSONObject jsonObject = userService.verifyCode(param.getJSONObject("data"), request);
            return R.success();
        } catch (Exception e) {
            log.error("用户注册校验验证码是否正确, param:{},  error message:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "获取交易明细-卡消费+分期付+逾期费 第二版本", notes = "获取交易明细-卡消费+分期付+逾期费 第二版本")
    @PostMapping("/getRecordTwo")
    public Object getTransactionRecordTwo(@NonNull @RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("APP- 获取交易明细-卡消费+分期付+逾期费 第二版本, data:{}", requestInfo);
        try {
            JSONObject data = requestInfo.getJSONObject("data");
            data.put("userId", getUserId(request));
//            data.put("userId", data.getLong("userId"));
            data = transRecordService.formatParamJson(data);
            int total;
            PagingContext pc;
            Vector<SortingContext> scs;
            Object resultList = null;
            total = transRecordService.countRecordTwo(data, request);
            pc = getPagingContext(data, total);
            if (total > 0) {
                scs = getSortingContext(data);
                resultList = transRecordService.transactionDetailsTwo(data, scs, pc, request);
            }
            return R.success(resultList, pc);
        } catch (Exception e) {
            log.info("APP- 获取交易明细-卡消费+分期付+逾期费 第二版本失败, data:{},error:{}", requestInfo, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @SignVerify
    @AppToken
    @ApiOperation(value = "查询用户是否收藏该商户", notes = "查询用户是否收藏该商户")
    @PostMapping(value = "/saveUserToKycPageLog", name = "查询用户是否收藏该商户")
    public Object isUserFavorite(@NonNull @RequestBody JSONObject requestInfo, HttpServletRequest request) {
        Long userId = requestInfo.getJSONObject("data").getLong("userId");
        if (null == userId) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }
        try {
            enterKycPageLogService.upsertLog(userId, request);
            return R.success();
        } catch (Exception e) {
            log.error("记录用户进入KYC页面次数出错, param:{},  error message:{}", userId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @SignVerify
    @PassToken
    @ApiOperation(value = "发送验证码", notes = "发送验证码")
    @PostMapping("/sendSMSVerificationCode/{phoneNumber}/{nodeType}")
    public Object sendSecurityCode(@PathVariable("nodeType") Integer nodeType, @PathVariable("phoneNumber") String phoneNumber, HttpServletRequest request) throws BizException {
        log.info("sendSMSVerificationCode, nodeType:{}, phoneNumber:{}, userType:{}", nodeType, phoneNumber);
        try {
            //此处用户类型后期需要前台传过来
            userService.sendSMSVerificationCode(nodeType, phoneNumber, request);
        } catch (Exception e) {
            log.error("AppInteractiveController.sendSecurityCode Exception:", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success();
    }


    @SignVerify
    @PassToken
    @ApiOperation(value = "校验验证码", notes = "校验验证码")
    @PostMapping("/checkVerificationCode")
    public Object checkVerificationCode(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("checkVerificationCode, jsonObject:{}", jsonObject.toJSONString());
        try {
            //参数校验
            if (null == jsonObject.getJSONObject("data")) {
                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
            }
            return R.success(userService.checkVerificationCode(jsonObject.getJSONObject("data"), request));
        } catch (Exception e) {
            log.error("AppInteractiveController.sendSecurityCode Exception:", e.getMessage(), e);
            if(I18nUtils.get("verification.code.expired", getLang(request)).equals(e.getMessage())){
                return R.fail(ErrorCodeEnum.APP_SMS_EXPIRED_CODE_ERROR.getCode(), e.getMessage());
            }else{
                return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
            }
        }
    }


    @SignVerify
    @PassToken
    @ApiOperation(value = "用户注册", notes = "用户注册")
    @PostMapping("/userRegister")
    public Object userRegister(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("userRegister-new, jsonObject:{}", jsonObject.toJSONString());
        try {
            return R.success(userService.appUserRegisterNew(jsonObject.getJSONObject("data"), request));
        } catch (Exception e) {
            log.error("userRegister-new Exception:", e.getMessage(), e);
            if(I18nUtils.get("verification.code.expired", getLang(request)).equals(e.getMessage())){
                return R.fail(ErrorCodeEnum.APP_SMS_EXPIRED_CODE_ERROR.getCode(), e.getMessage());
            }else{
                return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
            }
        }
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "用户注册", notes = "用户注册")
    @PostMapping("/userRegisterV2")
    public Object userRegisterV2(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("userRegister-newv2, jsonObject:{}", jsonObject.toJSONString());
        try {
            return R.success(userService.appUserRegisterNewV2(jsonObject.getJSONObject("data"), request));
        } catch (Exception e) {
            log.error("userRegister-newv2 Exception:", e.getMessage(), e);
            if(I18nUtils.get("verification.code.expired", getLang(request)).equals(e.getMessage())){
                return R.fail(ErrorCodeEnum.APP_SMS_EXPIRED_CODE_ERROR.getCode(), e.getMessage());
            }else{
                return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
            }
        }
    }

    @SignVerify
    @AppToken
//    @PassToken
    @ApiOperation(value = "用户进入页面信息记录", notes = "用户进入页面信息记录")
    @PostMapping("/saveUserPageLog")
    public Object saveUserEnterPageInfo(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("saveUserPageLog, jsonObject:{}", jsonObject.toJSONString());
        try {
            Long userId = jsonObject.getJSONObject("data").getLong("userId");
            Integer pageType = jsonObject.getJSONObject("data").getInteger("type");
            if (null == userId || null == pageType) {
                throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
            }
            //此处用户类型后期需要前台传过来
            UserEnterAppPageLogDTO saveLog = new UserEnterAppPageLogDTO();
            saveLog.setPageType(pageType);
            saveLog.setUserId(userId);
            userEnterAppPageLogService.saveUserEnterAppPageLog(saveLog, request);
            return R.success();
        } catch (Exception e) {
            log.error("userRegister-new Exception:", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }



    @SignVerify
    @AppToken
    @ApiOperation(value = "上送KYC信息", notes = "上送KYC信息")
    @PostMapping("/riskCheckNew")
    public Object riskCheckNew(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("app-kyc-data-submit, data:{}", requestInfo);
        JSONObject data = requestInfo.getJSONObject("data");
        JSONObject result = null;
        try {
            Long userId  = data.getLong("userId");

            Integer updateStatus  = data.getInteger("haveDataStatus");

            if(null == userId || null == updateStatus){
                throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
            }

            if(updateStatus.intValue() == StaticDataEnum.NEED_UPDATE_USER_DATA_STATE.getCode()){
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
            }

            userService.riskCheckParamsCheck(data, request);
            result = userService.riskCheckNew(data, request).get();
        } catch (Exception e) {
            log.error("risk check faile, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success(result);
    }


    @SignVerify
    @AppToken
//    @PassToken
    @ApiOperation(value = "获取用户业务状态信息", notes = "获取用户业务状态信息")
    @PostMapping("/getUserBusinessStatus")
    public Object getUserBusinessStatus(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("getUserBusinessStatus, data:{}", requestInfo);
        try {
            return R.success(userService.getUserBusinessStatus(requestInfo.getJSONObject("data"), request));
        } catch (Exception e) {
            log.error("risk check faile, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }



    @SignVerify
    @AppToken
//    @PassToken
    @ApiOperation(value = "获取APP首页协议弹窗状态", notes = "获取APP首页协议弹窗状态")
    @PostMapping("/getUserAgreementState")
    public Object getUserAgreementState(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("获取APP首页协议弹窗状态, data:{}", requestInfo);

        JSONObject dataJsonObj = requestInfo.getJSONObject("data");
        if(null == dataJsonObj || null == dataJsonObj.getLong("userId")){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }

        try {
            return R.success(userService.getUserAgreementState(dataJsonObj.getLong("userId"), request));
        } catch (Exception e) {
            log.error("获取APP首页协议弹窗状态 failed, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }



    @SignVerify
    @AppToken
//    @PassToken
    @ApiOperation(value = "更新APP首页协议弹窗状态", notes = "更新APP首页协议弹窗状态")
    @PostMapping("/updateUserAgreementState")
    public Object updateUserAgreementState(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("更新APP首页协议弹窗状态, data:{}", requestInfo);

        JSONObject dataJsonObj = requestInfo.getJSONObject("data");
        if(null == dataJsonObj || null == dataJsonObj.getLong("userId")){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }

        try {
            userService.updateUserAgreementState(dataJsonObj.getLong("userId"), request);
            return R.success();
        } catch (Exception e) {
            log.error("更新APP首页协议弹窗状态 failed, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }



    @SignVerify
    @AppToken
//    @PassToken
    @ApiOperation(value = "更新用户分期付卡还款协议状态、并设置默认卡", notes = "更新用户分期付卡还款协议状态、并设置默认卡")
    @PostMapping("/updateUserCreditCardAgreementState")
    public Object updateUserCreditCardAgreementState(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("更新用户分期付卡还款协议状态, data:{}", requestInfo);

        JSONObject dataJsonObj = requestInfo.getJSONObject("data");
        if(null == dataJsonObj || null == dataJsonObj.getLong("userId") || StringUtils.isBlank(dataJsonObj.getString("cardId"))){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }
        try {
            //更新协议
            userService.updateUserCreditCardAgreementState(dataJsonObj.getLong("userId"), request);
            //更新分期付已绑卡状态
            dataJsonObj.put("isCreditCard", "1");
            //设置默认卡
            userService.presetCard(dataJsonObj, dataJsonObj.getLong("userId"),request);
            return R.success();
        } catch (Exception e) {
            log.error("更新用户分期付卡还款协议状态 failed, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @SignVerify
    @AppToken
    @ApiOperation(value = "app获取默认卡信息", notes = "app获取默认卡信息")
    @PostMapping("/defaultCardInfo")
//    @PassToken
    public Object getDefaultCardInfo(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("app获取默认卡信息 , data:{}", requestInfo);
        JSONObject dataJsonObj = requestInfo.getJSONObject("data");
        if(null == dataJsonObj || null == dataJsonObj.getLong("userId")){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }
        try {
            return R.success(userService.getDefaultCardInfo(getUserId(request), request));
//            return R.success(userService.getDefaultCardInfo(646214090148630528L, request));
        } catch (Exception e) {
            log.error("get user card list failed, error msg:{}, error:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed", getLang(request)));
        }
    }

    @SignVerify
    @AppToken
//    @PassToken
    @ApiOperation(value = "获取APP首页弹窗提醒信息", notes = "获取APP首页弹窗提醒信息")
    @PostMapping("/getAppHomePageReminder")
    public Object updateUserCredit1CardAgreementState(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("更新用户分期付卡还款协议状态, data:{}", requestInfo);

        JSONObject dataJsonObj = requestInfo.getJSONObject("data");
        if(null == dataJsonObj || null == dataJsonObj.getLong("userId")){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }
        try {
            return R.success(userService.getAppHomePageReminder(dataJsonObj.getLong("userId"), request));
        } catch (Exception e) {
            log.error("更新用户分期付卡还款协议状态 failed, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @SignVerify
    @AppToken
//    @PassToken
    @ApiOperation(value = "营销券码列表", notes = "营销券码列表")
    @PostMapping(value = "/promotionList", name = "营销券码列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
    })
    public Object getPromotionList(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("营销券码列表 query , requestInfo:{}", requestInfo);
        JSONObject data = requestInfo.getJSONObject("data");

        if(null == data || StringUtils.isBlank(data.getString("listType"))){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }
        data.put("userId", getUserId(request));

        if(data.getString("listType").equals("1")){
            if(null == data.getBigDecimal("transAmount")){
                return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
            }
            if(null == data.getLong("merchantId")){
                return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
            }

            //可用
            try {
                JSONObject availableResult = marketingManagementService.getAvailablePromotionList(data, request);
                return R.success(availableResult.getJSONArray("data"), JSONObject.parseObject(availableResult.getJSONObject("pc").toJSONString(), PagingContext.class));
            } catch (Exception e) {
                log.error("营销券码列表 failed, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
                return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
            }
        }else if(data.getString("listType").equals("0")){
            //所有
            try {
                JSONObject result = marketingManagementService.getAllPromotionList(data, request);
                return R.success(result.getJSONArray("data"), JSONObject.parseObject(result.getJSONObject("pc").toJSONString(), PagingContext.class));
            } catch (Exception e) {
                log.error("营销券码列表 failed, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
                return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
            }
        }
        return null;
    }


    @SignVerify
    @AppToken
    @ApiOperation(value = "根据输入字符搜索营销码", notes = "根据输入字符搜索营销码")
    @PostMapping(value = "/findPromotionCode", name = "根据输入字符搜索营销码")
    public Object findPromotionCode(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("根据输入字符搜索营销码  , requestInfo:",requestInfo);
        JSONObject dataJsonObj = requestInfo.getJSONObject("data");
        if(null == dataJsonObj || StringUtils.isBlank(dataJsonObj.getString("code")) || null == getUserId(request)){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }
        try {
            return R.success(marketingManagementService.appPromotionSearch(dataJsonObj.getString("code"), getUserId(request), request));
        } catch (Exception e) {
            log.error("根据输入字符搜索营销码 failed, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @SignVerify
    @AppToken
    @ApiOperation(value = "添加卡券", notes = "添加卡券")
    @PostMapping(value = "/addPromotionCode", name = "添加卡券")
    public Object addPromotionCode(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("添加卡券  , requestInfo:",requestInfo);
        JSONObject dataJsonObj = requestInfo.getJSONObject("data");
        if(null == dataJsonObj || null == dataJsonObj.getLong("marketingId")
                || null == getUserId(request) || StringUtils.isBlank(dataJsonObj.getString("inputCode"))){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }
        try {
            return R.success(marketingManagementService.appAddPromotionCode(getUserId(request), dataJsonObj.getLong("marketingId"),
                    dataJsonObj.getString("inputCode"), dataJsonObj.getBigDecimal("transAmount"), dataJsonObj.getLong("merchantId"), request));
        } catch (Exception e) {
            log.error("添加卡券 failed, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


//    @PassToken
    @SignVerify
    @AppToken
    @ApiOperation(value = "stripe通过卡token绑卡", notes = "stripe通过卡token绑卡")
    @PostMapping(value = "/stripeBindCardByToken", name = "stripe通过卡token绑卡")
    public Object stripeBindCardByToken(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("stripe通过卡token绑卡  , requestInfo:",requestInfo);
        JSONObject dataJsonObj = requestInfo.getJSONObject("data");
        if(null == getUserId(request) || StringUtils.isBlank(dataJsonObj.getString("cardToken"))){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }
        try {
            return R.success(cardService.bindCard(dataJsonObj.getString("cardToken"), getUserId(request), dataJsonObj.getInteger("creditCardAgreementState"), request));
        } catch (Exception e) {
            log.error("stripe通过卡token绑卡 failed, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

//    @PassToken
    @SignVerify
    @AppToken
    @ApiOperation(value = "stripe卡号后四位判重", notes = "stripe卡号后四位判重")
    @PostMapping(value = "/stripeCheckCardNoRedundancy", name = "stripe卡号后四位判重")
    public Object stripeCheckCardNoRedundancy(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("stripe卡号后四位判重  , requestInfo:",requestInfo);
        JSONObject dataJsonObj = requestInfo.getJSONObject("data");
        if(null == getUserId(request) || StringUtils.isBlank(dataJsonObj.getString("last4"))
                || dataJsonObj.getString("last4").length() != 4){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }
        try {
            return R.success(cardService.stripeCheckCardNoRedundancy(dataJsonObj.getString("last4"), getUserId(request), request));
//            return R.success(cardService.stripeCheckCardNoRedundancy(dataJsonObj.getString("last4"), 617990103128166400L, request));
        } catch (Exception e) {
            log.error("stripe卡号后四位判重 failed, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @SignVerify
    @AppToken
//    @PassToken
    @ApiOperation(value = "根据用户是否迁移stripe来展示不同平台的卡列表", notes = "根据用户是否迁移stripe来展示不同平台的卡列表")
    @PostMapping(value = "/allCardList", name = "根据用户是否迁移stripe来展示不同平台的卡列表")
    public Object getAllCardList(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("allCardList query , requestInfo:{}", requestInfo);
        try {
//            return R.success(cardService.getAllCardList(617864817036316672L, request));
            return R.success(cardService.getAllCardList(getUserId(request), request));
        } catch (Exception e) {
            log.error("allCardList failed, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @SignVerify
    @AppToken
//    @PassToken
    @ApiOperation(value = "stripe通道卡列表", notes = "stripe通道卡列表")
    @PostMapping(value = "/stripeCardList", name = "stripe通道卡列表")
    public Object stripeCardList(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("stripe通道卡列表 query , requestInfo:{}", requestInfo);
        try {
            return R.success(cardService.getStripeCardList(getUserId(request), request));
//            return R.success(cardService.getStripeCardList(628735310341611520L, request));
        } catch (Exception e) {
            log.error("stripe通道卡列表 failed, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @SignVerify
    @AppToken
//    @PassToken
    @ApiOperation(value = "支付通道获取", notes = "支付通道获取")
    @PostMapping(value = "/getPayGateWay", name = "支付通道获取")
    public Object getPayGateWay(HttpServletRequest request) {
        log.info("支付通道获取:{}",getUserId(request));
        try {
            return R.success(gatewayService.getPayGateWay(request));
        } catch (Exception e) {
            log.error("支付通道获取异常 failed, e:{}, error message:{}",e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
}
