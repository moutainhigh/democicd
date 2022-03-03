package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.api.client.util.ArrayMap;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.ResultCode;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.exception.LatpayFailedException;
import com.uwallet.pay.core.util.*;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.filter.AppToken;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.filter.SignVerify;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.model.entity.UserAction;
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
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * author: baixinyue
 * createDate: 2010/12/5
 * description: app专用交互controller
 */

@RestController
@RequestMapping("/app")
@Slf4j
@Api("app专用交互controller")
public class AppInteractiveController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private QrcodeInfoService qrcodeInfoService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private RechargeFlowService rechargeFlowService;

    @Autowired
    private QrPayFlowService qrPayFlowService;

    @Autowired
    private WithholdFlowService withholdFlowService;

    @Autowired
    private OrderRefundService orderRefundService;

    @Autowired
    private AppBannerService appBannerService;

    @Autowired
    private AppAboutUsService appAboutUsService;

    @Autowired
    private AppAggrementService appAggrementService;

    @Autowired
    @Lazy
    private QrPayService qrPayService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ServerService serverService;

    @Value("${spring.IntroductionPath}")
    private String IntroductionPath;

    @Value("${spring.paperPath}")
    private String paperPath;

    @Autowired
    private StaticDataService staticDataService;

    @Autowired
    private ParametersConfigService parametersConfigService;

    @Autowired
    private AdsService adsService;

    @Autowired
    private ActionService actionService;

    @Autowired
    private UserActionService userActionService;

    @Autowired
    private RefundFlowService refundFlowService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private TopDealService topDealService;

    @Autowired
    private AccountFlowService accountFlowService;

    @Autowired
    private WholeSalesFlowService wholeSalesFlowService;

    @Autowired
    private ClearBatchService clearBatchService;

    @Autowired
    private ClearFlowDetailService clearFlowDetailService;

    @Autowired
    private ClearDetailService clearDetailService;

    @Autowired
    private MarketingFlowService marketingFlowService;
    @Autowired
    private MarketingManagementService marketingManagementService;

    /**
     * 距离
     */
    private static String DISTANCE = "10";

    @Autowired
    private NfcCodeInfoService nfcCodeInfoService;

    private static final int KYC_CHANCE = 3;

    /**
     * 隐私协议
     */
    @Value("${spring.creditAgreement}")
    private String creditAgreement;

    /**
     * 客户协议
     */
    @Value("${spring.customer}")
    private String customer;
    @Autowired
    private AppVersionService appVersionService;


    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private TagService tagService;
    @Resource
    private MerchantListService merchantListService;

    @Autowired
    private IpLocationService ipLocationService;
    @Resource
    private TransRecordService transRecordService;
    @Autowired
    private AdminService adminService;
    @Value("${server.type}")
    private String serverType;

    @Autowired
    private IllionInstitutionsService illionInstitutionsService;


    @Resource
    private PosQrPayFlowService posQrPayFlowService;

    /**
     * 测试环境配置文件type,挡板用
     */
    private static final String TEST_SERVER_TYPE = "test";


    @SignVerify
    @PassToken
    @ApiOperation(value = "用户、商户注册", notes = "用户、商户注册")
    @PostMapping("/userRegister")
    public Object userRegister(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("app user register DTO:{}", requestInfo);
        JSONObject userInfo = requestInfo.getJSONObject("data");
        try {
            UserDTO userDTO = JSONObject.parseObject(userInfo.toJSONString(), UserDTO.class);
            userDTO.setChannel(StaticDataEnum.ACCOUNT_CHANNEL_0001.getCode() + "");
            userService.userRegisterParamsCheck(userDTO, request);
            userService.saveUser(userDTO, request);
        } catch (Exception e) {
            log.error("app user register failed, userDTO: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }

        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "用户、商户注册V1", notes = "用户、商户注册V1")
    @PostMapping("/userRegisterV1")
    public Object userRegisterV1(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("app user register V1 DTO:{}", requestInfo);
        JSONObject userInfo = requestInfo.getJSONObject("data");
        try {
            UserDTO userDTO = JSONObject.parseObject(userInfo.toJSONString(), UserDTO.class);
            userDTO.setChannel(StaticDataEnum.ACCOUNT_CHANNEL_0001.getCode() + "");
            userService.userRegisterParamsCheck(userDTO, request);
            userService.saveUserV1(userDTO, request);
        } catch (Exception e) {
            log.error("add user failed V1, userDTO: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }

        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "创建商户员工", notes = "创建商户员工")
    @PostMapping("/createMerchantStaff")
    public Object createMerchantStaff(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("create merchant staff DTO:{}", requestInfo);
        JSONObject userInfo = requestInfo.getJSONObject("data");
        Long userId = null;
        try {
            userId = userService.createMerchantStaff(JSONObject.parseObject(userInfo.toJSONString(), UserDTO.class), request);
        } catch (Exception e) {
            log.error("create merchant staff failed, userDTO: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(userId);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "发送验证码", notes = "发送验证码")
    @PostMapping("/sendSecurityCode/{email}/{sendNode}/{userType}")
    public Object sendSecurityCode(@PathVariable("email") String email, @PathVariable("sendNode") String sendNode, @PathVariable("userType") Integer userType, HttpServletRequest request) throws BizException {
        log.info("send security code, email:{}, sendNode:{}, userType:{}", email, sendNode, userType);
        if (!Validator.isEmail(email)){
            throw new BizException(I18nUtils.get("username.contains.invalid.characters", getLang(request)));
        }
        if (email == null || email.length() > Validator.TEXT_LENGTH_100 || !(Validator.isEmail(email))) {
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("email.format.error", getLang(request)));
        }
        try {
            //此处用户类型后期需要前台传过来
            userService.sendSecurityCode(sendNode, email, userType, request);
        } catch (Exception e) {
            log.error("AppInteractiveController.sendSecurityCode Exception:", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "发送验证码", notes = "发送验证码")
    @PostMapping("/sendSecurityCodeSMS/{phone}/{sendNode}/{userType}")
    public Object sendSecurityCodeSMS(@PathVariable("phone") String phone, @PathVariable("sendNode") String sendNode, @PathVariable("userType") Integer userType, HttpServletRequest request) {
        log.info("AppInteractiveController.sendSecurityCodeSMS, phone:{}, sendNode:{}, userType:{}", phone, sendNode, userType);
        try {
            userService.phoneParamsCheck(phone, request);
            //此处用户类型后期需要前台传过来
            userService.sendSecuritySMS(phone, sendNode, userType, request);
        } catch (Exception e) {
            log.error("AppInteractiveController.sendSecurityCodeSMS Exception:", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "向旧手机发送验证码", notes = "向旧手机发送验证码")
    @PostMapping("/sendSecurityCodeSMSToOld/{phone}/{sendNode}/{userType}")
    public Object sendSecurityCodeSMSToOld(@PathVariable("phone") String phone, @PathVariable("sendNode") String sendNode, @PathVariable("userType") Integer userType, HttpServletRequest request) {
        log.info("send security code SMS to old, phone:{}, sendNode:{}, userType:{}", phone, sendNode, userType);
//        String phoneCode  = new StringBuilder(phone).substring(0, 2);
//        String testPhone = new StringBuilder(phone).substring(2, phone.length());
//        if (phoneCode.equals(StaticDataEnum.CHN.getMessage())) {
//            if (testPhone.length() > Validator.PHONE_LENGTH) {
//                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("phone.length.error", getLang(request)));
//            }
//        } else {
//            if (testPhone.length() > Validator.PHONE_AUD_LENGTH) {
//                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("phone.length.error", getLang(request)));
//            }
//        }
        try {
            userService.phoneParamsCheck(phone, request);
            //此处用户类型后期需要前台传过来 10
            userService.sendSecuritySMSToOld(phone, sendNode, userType, request);
        } catch (Exception e) {
            log.error("AppInteractiveController.sendSecurityCodeSMSToOld Exception:", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "校验旧手机号验证码", notes = "校验旧手机号验证码")
    @PostMapping("/checkOldPhoneCode/{oldPhone}/{signCode}/{userType}")
    public Object checkOldPhoneCode(@PathVariable("oldPhone") String oldPhone, @PathVariable("signCode") Integer signCode, @PathVariable("userType") Integer userType, HttpServletRequest request) {
        log.info("check old phone code, phone:{}, sendNode:{}, userType:{}", oldPhone, signCode, userType);
//        String phoneCode  = new StringBuilder(oldPhone).substring(0, 2);
//        String testPhone = new StringBuilder(oldPhone).substring(2, oldPhone.length());
//        if (phoneCode.equals(StaticDataEnum.CHN.getMessage())) {
//            if (testPhone.length() > Validator.PHONE_LENGTH) {
//                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("phone.length.error", getLang(request)));
//            }
//        } else {
//            if (testPhone.length() > Validator.PHONE_AUD_LENGTH) {
//                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("phone.length.error", getLang(request)));
//            }
//        }
        try {
            userService.phoneParamsCheck(oldPhone, request);
            //此处用户类型后期需要前台传过来 10
            userService.checkOldPhoneCode(oldPhone, signCode, userType, request);
        } catch (Exception e) {
            log.error("AppInteractiveController.checkOldPhoneCode Exception:", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "向新手机发送验证码", notes = "向新手机发送验证码")
    @PostMapping("/sendSecurityCodeSMSToNew/{phone}/{sendNode}/{userType}")
    public Object sendSecurityCodeSMSToNew(@PathVariable("phone") String phone, @PathVariable("sendNode") String sendNode, @PathVariable("userType") Integer userType, HttpServletRequest request) {
        log.info("send security code SMS to new, phone:{}, sendNode:{}, userType:{}", phone, sendNode, userType);
//        String phoneCode  = new StringBuilder(phone).substring(0, 2);
//        String testPhone = new StringBuilder(phone).substring(2, phone.length());
//        if (phoneCode.equals(StaticDataEnum.CHN.getMessage())) {
//            if (testPhone.length() > Validator.PHONE_LENGTH) {
//                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("phone.length.error", getLang(request)));
//            }

//        } else {
//            if (testPhone.length() > Validator.PHONE_AUD_LENGTH) {
//                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("phone.length.error", getLang(request)));
//            }
//        }
        try {
            userService.phoneParamsCheck(phone, request);
            //此处用户类型后期需要前台传过来
            userService.sendSecuritySMSToNew(phone, sendNode, userType, request);
        } catch (Exception e) {
            log.error("AppInteractiveController.sendSecurityCodeSMSToNew Exception:", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "验证验证码并修改手机号", notes = "验证验证码并修改手机号")
    @PostMapping("/updatePhone/{phone}/{oldPhone}/{signCode}/{userType}")
    public Object updatePhone(@PathVariable("phone") String phone, @PathVariable("oldPhone") String oldPhone, @PathVariable("signCode") Integer signCode, @PathVariable("userType") Integer userType, HttpServletRequest request) {
        log.info("update phone, phone:{}, sendNode:{}, userType:{}", phone, signCode, userType);
        String token = null;
        try {
            token = userService.updatePhone(phone, oldPhone, signCode, userType, request);
        } catch (Exception e) {
            log.info("send security code failed, e:{}, error message:{}", e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(),  e.getMessage());
        }
        return R.success(token);
    }


    @SignVerify
    @PassToken
    @ApiOperation(value = "用户、商户忘记密码", notes = "用户、商户忘记密码")
    @PostMapping("/forgetPassword")
    public Object forgetPassword(@RequestBody JSONObject requestInfo, HttpServletRequest request) throws Exception {
        log.info("forgetPassword DTO:{}", requestInfo);
        try {
            JSONObject userInfo = requestInfo.getJSONObject("data");
            userService.forgetPassword(JSONObject.parseObject(userInfo.toJSONString(), UserDTO.class), request);
        } catch (Exception e) {
            log.error("forgetPassword failed, userDTO: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }

        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "用户、商户登陆", notes = "用户、商户登陆")
    @PostMapping("/login")
    public Object login(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("app user login info:{}", jsonObject);
        JSONObject msg = null;
        try {
            msg = userService.appLogin(jsonObject.getJSONObject("data"), request);
        } catch (Exception e) {
            log.error("app user login failed, userDTO: {}, error message:{}, error all:{}", jsonObject, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.LOGIN__ERROR.getCode(), e.getMessage());
        }

        return R.success(msg);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "支付个人信息完善", notes = "支付个人信息完善")
    @PostMapping("/infoSupplement")
    public Object informationSupplement(@RequestBody JSONObject requestInfo, HttpServletRequest request) throws Exception {
        log.info("info supplement info:{}", requestInfo);
        JSONObject data = requestInfo.getJSONObject("data");
        try {
            data.put("userId", getUserId(request));
            serverService.infoSupplement(data, request);
        } catch (Exception e) {
            log.error("info supplement info failed, info: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }

        return R.success();
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "获取二维码", notes = "获取二维码")
    @PostMapping("/codePath")
    public Object findQRCodePath(@RequestBody JSONObject requestInfo, HttpServletRequest request) throws Exception {
        log.info("find qrcode path, data:{}", requestInfo);
        String path = null;
        try {
            JSONObject data = requestInfo.getJSONObject("data");
            path = qrcodeInfoService.findQRCodePath(data.getLong("merchantId"), data.getLong("timestamp"), data.getString("oldPath"), request);
        } catch (BizException e) {
            log.error("query code path failed: {}, error message:{}", requestInfo, "查询失败");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed", getLang(request)));
        }

        return R.success(path);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "二维码查询用户", notes = "二维码查询用户")
    @ApiImplicitParam(name = "code", value = "用户id", dataType = "string", paramType = "path", required = true)
    @PostMapping("/findUserInfoByQRCode")
    public Object findUserInfoByQRCode(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("find user info bu qr code, data:{}", requestInfo);
        JSONObject object = null;
        JSONObject data = requestInfo.getJSONObject("data");
        try {
            object = qrcodeInfoService.findUserInfoByQRCode(data, request);
        } catch (Exception e) {
            log.error("query user info failed: {}, error message:{}, e:{}", data, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(JSONResultHandle.resultHandle(object));
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "根据userId查询账户系统用户信息(全部)", notes = "根据userId查询账户系统用户信息(全部)")
    @PostMapping("/findUserInfoId")
    public Object findUserInfoId(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("find user info by id, data:{}", requestInfo);
        JSONObject object;
        JSONObject data = requestInfo.getJSONObject("data");
        try {
            data.put("userId", getUserId(request));
            object = qrcodeInfoService.findUserInfoId(data, request);
        } catch (Exception e) {
            log.error("find user info failed: {}, error message:{}", data, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed", getLang(request)));
        }
        JSONObject objects = JSONResultHandle.resultHandle(object);
        return R.success(objects);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "消息查询", notes = "消息查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @PostMapping("/notice")
    public Object list(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        List<JSONObject> results = new ArrayList<>();
        Long userId = getUserId(request);
        if (StringUtils.isEmpty(requestInfo.getJSONObject("data").getString("userId")) || (!requestInfo.getJSONObject("data").getString("userId").equals(userId.toString()))) {
            return R.fail(ErrorCodeEnum.USER_NOT_EXIST_ERROR.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }
        Map<String, Object> params = requestInfo.getJSONObject("data");
        int total = noticeService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<NoticeDTO> list = new ArrayList<>();
        pc = getPagingContext(requestInfo.getJSONObject("data"), total);
        if (total > 0) {
            scs = getSortingContext(requestInfo.getJSONObject("data"));
            list = noticeService.find(params, scs, pc);
            for (NoticeDTO dto : list) {
                JSONObject object = JSONResultHandle.resultHandle(dto, NoticeDTO.class);
                Long createdDate = dto.getCreatedDate();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US);
                if (createdDate!=null){
                    String format = simpleDateFormat.format(new Date(createdDate));
                    object.put("createTimes",format);
                }else {
                    object.put("createTimes","");
                }
                results.add(object);
            }
        }
        return R.success(results, pc);
    }

    @SignVerify
    @AppToken
    @PostMapping("/noticeModify")
    @ApiOperation(value = "修改消息表", notes = "修改消息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object noticeModify(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("notice modify has read, data:{}", requestInfo);
        JSONObject data = requestInfo.getJSONObject("data");
        try {
            NoticeDTO noticeDTO = JSONObject.parseObject(data.toJSONString(), NoticeDTO.class);
            noticeDTO.setUserId(getUserId(request));
            noticeService.updateNotice(noticeDTO.getId(), noticeDTO, request);
        } catch (BizException e) {
            log.error("notice modify has read failed, data: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @PostMapping("/noticeDelete/{id}")
    @ApiOperation(value = "删除消息表", notes = "删除消息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object noticeDelete(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete notice, id:{}", id);
        try {
            noticeService.logicDeleteNotice(id, request);
        } catch (BizException e) {
            log.error("delete notice failed, id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @PostMapping("/allNoticeHasRead/{userId}")
    @ApiOperation(value = "是否全部已读", notes = "是否全部已读")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object allNoticeHasRead(@PathVariable("userId") Long userId, HttpServletRequest request) {
        log.info("all Notice Has Read, userId:{}", userId);
        Boolean  result;
        try {
            if (userId.longValue() != getUserId(request)) {
                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
            }
            result = noticeService.allNoticeHasRead(userId);
        } catch (Exception e) {
            log.error("all Notice Read failed, id: {}, error message:{}, error all:{}", userId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @AppToken
    @PostMapping("/allNoticeRead/{userId}")
    @ApiOperation(value = "修改全部已读", notes = "修改全部已读")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object allNoticeRead(@PathVariable("userId") Long userId, HttpServletRequest request) {
        log.info("delete notice, id:{}", userId);
        try {
            if (userId.longValue() != getUserId(request)) {
                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
            }
            noticeService.allNoticeRead(userId, request);
        } catch (Exception e) {
            log.error("all Notice Read failed, id: {}, error message:{}, error all:{}", userId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "app绑卡、帮账户操作", notes = "app绑卡、帮账户操作")
    @PostMapping("/tieOnCard")
    public Object tieOnCard(@RequestBody JSONObject object, HttpServletRequest request) {
        log.info("tie on card data:{}", object);
        JSONObject data = object.getJSONObject("data");
        Long id = null;
        try {
            userService.tieOnCardParamsCheck(data, request);
            id = userService.tieOnCard(data, request);
        } catch (Exception e) {
            String message = e.getMessage();
            /*if (!message.equals("The card has been bound.please change the card and try again")){
                message = I18nUtils.get("card.bind.failed",getLang(request));
            }*/
            log.error("tie on card failed: {}, error message:{}", object, message, e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), message);
        }

        return R.success(id);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "app获取卡列表", notes = "app获取卡列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", paramType = "path", dataType = "long", required = true),
            @ApiImplicitParam(name = "cardType", value = "卡类型 0：卡 1：账户", paramType = "query", dataType = "int", required = false)
    })
    @PostMapping("/getCardList/{userId}")
    public Object getCardList(@PathVariable("userId") Long userId, @RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("get user card list user-app:{}, data:{}", userId, requestInfo);
        JSONArray cardList = null;
        try {
            //参数校验
            if (userId == null) {
                throw new BizException(I18nUtils.get("user.id.empty", getLang(request)));
            }
            JSONObject data = requestInfo.getJSONObject("data");
            if (null == data){
                throw new BizException(I18nUtils.get("parameters.null",getLang(request)));
            }
            cardList = userService.getCardList(userId, data.getInteger("cardType"), request);
        } catch (Exception e) {
            log.error("get user card list failed, user: {}, error message:{}", userId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed", getLang(request)));
        }

        return R.success(cardList);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "app获取卡信息", notes = "app获取卡信息")
    @PostMapping("/getCardInfo/{id}")
    public Object getCardInfo(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("get user card info card:{}", id);
        JSONObject cardInfo = null;
        try {
            cardInfo = userService.getCardInfo(id, request);
        } catch (Exception e) {
            log.error("get user card info failed, card: {}, error message:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed", getLang(request)));
        }

        return R.success(cardInfo);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "充值", notes = "充值")
    @PostMapping("/recharge")
    public Object recharge(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("recharge : {}", requestInfo);
        JSONObject returnData = null;
        try {
            RechargeDTO rechargeDTO = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), RechargeDTO.class);
            rechargeFlowService.rechargeByLatPay(rechargeDTO, request);
        } catch (Exception e) {
            log.error("recharge failed data: {}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success(returnData);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "商户待结算金额/交易明细查询(包含已结算总金额)", notes = "商户待结算金额/交易明细查询(包含已结算总金额)")
    @PostMapping(value = "/appQrPayFlowListRecAmountTotal/{id}", name = "商户待结算金额/交易明细查询(包含已结算总金额)")
    public Object appQrPayFlowListRecAmountTotal(@PathVariable("id") Long id, @RequestBody JSONObject requestInfo) {
        log.info("get appQrPayFlowListRecAmountTotal Id:{},requestInfo{}", id, requestInfo);
        Map<String, Object> params = requestInfo.getJSONObject("data");
        params.put("merchantId", id);
        params.put("isNeedClear", StaticDataEnum.NEED_CLEAR_TYPE_1.getCode());
        params.put("clearState", StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
        params.put("state", StaticDataEnum.TRANS_STATE_31.getCode());
        int total = qrPayFlowService.countList(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        JSONObject qrPayFlowDTOS = null;
        pc = getPagingContext(requestInfo.getJSONObject("data"), total);
        if (total > 0) {
            scs = getSortingContext(requestInfo.getJSONObject("data"));
            qrPayFlowDTOS = qrPayFlowService.appQrPayFlowListRecAmountTotal(params, scs, pc);
        }
        JSONObject object = JSONResultHandle.resultHandle(qrPayFlowDTOS);
        if (object.size() == 0) {
            object.put("list", new ArrayList<>());
            object.put("recAmountTotal", new BigDecimal(0));
        }
        return R.success(object, pc);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "商户收款明细/用户收款记录", notes = "商户收款明细/用户收款记录")
    @PostMapping(value = "/appQrPayFlowList/{id}", name = "商户收款明细")
    public Object appQrPayFlowList(@PathVariable("id") Long id, @RequestBody JSONObject requestInfo) {
        log.info("get appQrPayFlowList Id:{} data:{}", id, requestInfo);
        Map<String, Object> params = requestInfo.getJSONObject("data");
        params.put("state", StaticDataEnum.TRANS_STATE_31.getCode());
        params.put("merchantId", id);
        int total = qrPayFlowService.countLists(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        JSONObject qrPayFlowDTOS = new JSONObject();
        pc = getPagingContext(requestInfo.getJSONObject("data"), total);
        if (total > 0) {
            scs = getSortingContext(requestInfo.getJSONObject("data"));
            qrPayFlowDTOS = qrPayFlowService.appQrPayFlowList(params, scs, pc);
        }
        JSONObject object = JSONResultHandle.resultHandle(qrPayFlowDTOS);
        return R.success(object, pc);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "商户待结算金额/交易明细查询(只包含支付待结算金额)", notes = "商户待结算金额/交易明细查询(只包含支付待结算金额)")
    @PostMapping(value = "/appQrPayFlowRecAmount/{id}", name = "商户待结算金额/交易明细查询(只包含支付待结算金额)")
    public Object appQrPayFlowRecAmount(@PathVariable("id") Long id) {
        log.info("app qr pay flow rec amount, Id:{}", id);
        QrPayFlowDTO qrPayFlowDTOS = qrPayFlowService.appQrPayFlowUnRecAmountTotal(id);
        BigDecimal bigDecimal = new BigDecimal(0);
        if (qrPayFlowDTOS != null) {
            bigDecimal = qrPayFlowDTOS.getRecAmountTotal();
        }
        return R.success(bigDecimal);
    }

    @SignVerify
    @AppToken
    @PostMapping(value = "/changePassword", name = "修改登陆密码")
    @ApiOperation(value = "修改登陆密码", notes = "修改登陆密码")
    public Object modifyPassword(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("modifyPassword adminDTO DTO:{}", requestInfo);
        try {
            UserDTO userDTO = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), UserDTO.class);
            userService.modifyPassword(userDTO, request);
        } catch (Exception e) {
            log.error("changePassword failed,  userDTO: {}, error message:{}", requestInfo,
                    e.getMessage());
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "查询支付密码", notes = "查询支付密码")
    @PostMapping(value = "/findPayPassword", name = "查询支付密码")
    public Object findPayPassword(@RequestBody JSONObject requestInfo) {
        log.info("find pay password, data:{}", requestInfo);
        UserDTO userDTO = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), UserDTO.class);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", userDTO.getId());
        UserDTO user = userService.findOneUser(params);
        return R.success(JSONResultHandle.resultHandle(user, UserDTO.class));
    }

    @SignVerify
    @AppToken
    @PostMapping(value = "/checkPayPassword", name = "支付密码校验")
    @ApiOperation(value = "支付密码校验", notes = "支付密码校验")
    public Object checkPayPassword(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("checkPayPassword adminDTO DTO:{}", requestInfo);
        try {
            UserDTO userDTO = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), UserDTO.class);
            userService.checkPayPassword(userDTO, request);
        } catch (Exception e) {
            log.error("checkPayPassword failed,  userDTO: {}, error message:{}", requestInfo,
                    e.getMessage());
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @PostMapping(value = "/updatePayPassword", name = "设置支付密码")
    @ApiOperation(value = "设置支付密码", notes = "设置支付密码")
    public Object updatePayPassword(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("updatePayPassword adminDTO DTO:{}", requestInfo);
        try {
            UserDTO userDTO = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), UserDTO.class);
            userService.updatePayPassword(userDTO, request);
        } catch (Exception e) {
            log.error("updatePayPassword failed,  userDTO: {}, error message:{}", requestInfo, e.getMessage());
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @PostMapping("/qrPayNew")
    @ApiOperation(value = "扫码支付交易", notes = "扫码支付交易")
    public Object qrPay(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("app/qrPayNew  PayDTO:{}", requestInfo);

        Object resObj = null;
        try {
            QrPayDTO qrPayDTO = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), QrPayDTO.class);
            // 入参校验、
            qrPayService.qrPayReqCheck(qrPayDTO, request);
            // 扫码支付交易
            resObj = qrPayService.doQrPay(qrPayDTO, request);
        } catch (Exception e) {
            // 扫码支付不抛错，需要返回成功的码
            // 如果是金额小于5元抛出异常，其他返回成功的码
            log.error("app/qrPay, qrPayDTO: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            if(I18nUtils.get("credit.amount.limit", getLang(request), new String[]{StaticDataEnum.INSTALLMENT_MIN_AMT.getMessage()}).equals(e.getMessage())){
                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
            }else{
                Map<String,Object> resMap = new HashMap<>();
                if(I18nUtils.get("trans.doubtful", getLang(request)).equals(e.getMessage())){
                    resMap.put("resultState",StaticDataEnum.TRANS_STATE_0.getCode());
                }else{
                    resMap.put("resultState",StaticDataEnum.TRANS_STATE_2.getCode());
                }
                resMap.put("errorMessage",e.getMessage());
                return R.success(resMap);
            }

        }

        return R.success(resObj);
    }


    @SignVerify
    @AppToken
    @PostMapping("/v2/qrPay")
    @ApiOperation(value = "点击PAY支付进行交易", notes = "点击PAY支付进行交易")
    public Object qrPayV2(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("app/qrPayV2  PayDTO:{}", requestInfo);

        Object resObj = null;
        try {
            QrPayDTO qrPayDTO = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), QrPayDTO.class);
            // 扫码支付交易
            resObj = qrPayService.doQrPayV2(qrPayDTO, request);
        } catch (Exception e) {
            // 如果是金额小于5元抛出异常，其他返回成功的码
            log.error("app/qrPay, qrPayDTO: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            if(I18nUtils.get("credit.amount.limit", getLang(request), new String[]{StaticDataEnum.INSTALLMENT_MIN_AMT.getMessage()}).equals(e.getMessage())){
                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
            }else{
                Map<String,Object> resMap = new HashMap<>();
                if(I18nUtils.get("trans.doubtful", getLang(request)).equals(e.getMessage())){
                    resMap.put("resultState",StaticDataEnum.TRANS_STATE_0.getCode());
                    resMap.put("errorMessage",  I18nUtils.get("pay.order.suspicious.message", getLang(request)));
                }else{
                    resMap.put("resultState",StaticDataEnum.TRANS_STATE_2.getCode());
                    resMap.put("errorMessage",e.getMessage());
                }
                return R.success(resMap);
            }

        }
        return R.success(resObj);
    }


    @SignVerify
    @AppToken
//    @PassToken
    @PostMapping("/v3/qrPay")
    @ApiOperation(value = "点击PAY支付进行交易", notes = "点击PAY支付进行交易")
    public Object qrPayV3(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("v3/qrPay  PayDTO:{}", requestInfo);

        Object resObj = null;
        try {
            QrPayDTO qrPayDTO = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), QrPayDTO.class);
            qrPayDTO.setPayUserId(getUserId(request));
            // 扫码支付交易
            resObj = qrPayService.doQrPayV3(qrPayDTO, request);
        } catch (Exception e) {

            Map<String,Object> resMap = new HashMap<>();

            if(e instanceof LatpayFailedException){
                Integer deleteCardStatus = ((LatpayFailedException) e).getDeleteCardStatus();
                Integer cardCount = ((LatpayFailedException) e).getCardCount();
                resMap.put("resultState",StaticDataEnum.TRANS_STATE_2.getCode());
                resMap.put("errorMessage",  e.getMessage());
                resMap.put("deleteCardPayState", null !=  deleteCardStatus ? deleteCardStatus.toString() : deleteCardStatus);
                resMap.put("cardCount",  null !=  cardCount ? cardCount.toString() :  cardCount);
                return R.success(resMap);
            }

            log.error("app/qrPay, qrPayDTO: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            // 如果是金额小于5元抛出异常，其他返回成功的码
            if(I18nUtils.get("credit.amount.limit", getLang(request), new String[]{StaticDataEnum.INSTALLMENT_MIN_AMT.getMessage()}).equals(e.getMessage())){
                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
            }else{
                resMap.clear();
                if(I18nUtils.get("trans.doubtful", getLang(request)).equals(e.getMessage())){
                    resMap.put("resultState",StaticDataEnum.TRANS_STATE_0.getCode());
                    resMap.put("errorMessage",  I18nUtils.get("pay.order.suspicious.message", getLang(request)));
                }else{
                    resMap.put("resultState",StaticDataEnum.TRANS_STATE_2.getCode());
                    resMap.put("errorMessage",e.getMessage());
                }
                return R.success(resMap);
            }

        }
        return R.success(resObj);
    }


    @SignVerify
    @AppToken
//    @PassToken
    @PostMapping("/v4/qrPay")
    @ApiOperation(value = "点击PAY支付进行交易", notes = "点击PAY支付进行交易")
    public Object qrPayV4(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("v4/qrPay  PayDTO:{}", requestInfo);

        Object resObj = null;
        try {
            QrPayDTO qrPayDTO = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), QrPayDTO.class);
            // todo 挡板
            qrPayDTO.setPayUserId(getUserId(request));
            // 扫码支付交易
            resObj = qrPayService.doQrPayV4(qrPayDTO, request);
        } catch (Exception e) {

            Map<String,Object> resMap = new HashMap<>();

            if(e instanceof LatpayFailedException){
                Integer deleteCardStatus = ((LatpayFailedException) e).getDeleteCardStatus();
                Integer cardCount = ((LatpayFailedException) e).getCardCount();
                resMap.put("resultState",StaticDataEnum.TRANS_STATE_2.getCode());
                resMap.put("errorMessage",  e.getMessage());
                resMap.put("deleteCardPayState", null !=  deleteCardStatus ? deleteCardStatus.toString() : deleteCardStatus);
                resMap.put("cardCount",  null !=  cardCount ? cardCount.toString() :  cardCount);
                return R.success(resMap);
            }

            log.error("app/qrPay, qrPayDTO: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            // 如果是金额小于5元抛出异常，其他返回成功的码
            if(I18nUtils.get("credit.amount.limit", getLang(request), new String[]{StaticDataEnum.INSTALLMENT_MIN_AMT.getMessage()}).equals(e.getMessage())){
                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
            }else{
                resMap.clear();
                if(I18nUtils.get("trans.doubtful", getLang(request)).equals(e.getMessage())){
                    resMap.put("resultState",StaticDataEnum.TRANS_STATE_0.getCode());
                    resMap.put("errorMessage",  I18nUtils.get("pay.order.suspicious.message", getLang(request)));
                }else{
                    resMap.put("resultState",StaticDataEnum.TRANS_STATE_2.getCode());
                    resMap.put("errorMessage",e.getMessage());
                }
                return R.success(resMap);
            }

        }
        return R.success(resObj);
    }


    @SignVerify
    @AppToken
//    @PassToken
    @PostMapping("/v3/getPayTransAmountDetail")
    @ApiOperation(value = "支付时获取交易金额信息", notes = "支付时获取交易金额信息")
    public Object getPayTransAmountDetailV3(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("支付时获取交易金额信息v3  requestInfo:{}", requestInfo);
        JSONObject result;
        try {
            result = qrPayService.getPayTransAmountDetailV3(requestInfo.getJSONObject("data"), request);
        } catch (Exception e) {
            log.error("支付时获取交易金额信息新接口 , requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @AppToken
    @PostMapping("/qrPay")
    @ApiOperation(value = "扫码支付交易", notes = "扫码支付交易")
    public Object qrPayOld(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("app/qrPay  PayDTO:{}", requestInfo);

        Object resObj = null;
        try {
            //老版本更新POS订单失败状态
            String transNo = requestInfo.getJSONObject("data").getString("transNo");
            if(StringUtils.isNotBlank(transNo)){
                posQrPayFlowService.updateOrderStatusBySysTransNo(transNo, StaticDataEnum.POS_ORDER_STATUS_FAIL.getCode(), getUserId(request), System.currentTimeMillis());
            }
            throw new BizException("Please update your APP version.");
//            QrPayDTO qrPayDTO = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), QrPayDTO.class);
//            //入参校验
//            qrPayService.qrPayReqCheck(qrPayDTO, request);
//            //扫码支付交易
//            resObj = qrPayService.doQrPay(qrPayDTO, request);
        } catch (Exception e) {
            log.error("app/qrPay, qrPayDTO: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
//        return R.success(resObj);
    }

    @SignVerify
    @AppToken
    @PostMapping("/qrPayStatusCheck/{flowId}")
    @ApiOperation(value = "扫码支付交易结果查询", notes = "扫码支付交易结果查询")
    public Object qrPayStatusCheck(@PathVariable("flowId") Long flowId, HttpServletRequest request) {
        log.info("order status check, flowId:{}", flowId);
        JSONObject result = null;
        try {
            //扫码支付交易
            result = qrPayService.aliOrWechatOrderStatusCheck(flowId, request);
        } catch (Exception e) {
            log.error("order status check, flowId:{}, error message:{}, error all:{}", flowId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(result);
    }


    @SignVerify
    @PassToken
    @ApiOperation(value = "获取banner", notes = "获取banner")
    @PostMapping("/getBannerImg")
    public Object getBannerImg() {
        List<AppBannerDTO> pathList = appBannerService.getBannerImg();
        List<JSONObject> list = new ArrayList<>(pathList.size());
        pathList.forEach(item -> {
            list.add(JSONResultHandle.resultHandle(item, AppBannerDTO.class));
        });
        return R.success(list);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "关于我们", notes = "关于我们")
    @PostMapping("/getAboutUs")
    public Object getAboutUs() {
        List<AppAboutUsDTO> dataList = appAboutUsService.find(null, null, null);
        return R.success(JSONResultHandle.resultHandle(dataList.get(0), AppAboutUsDTO.class));
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "获取协议", notes = "获取协议")
    @PostMapping("/getAggrement")
    public Object getAggrement() {
        List<AppAggrementDTO> dataList = appAggrementService.find(null, null, null);
        List<JSONObject> retuenMsg = new ArrayList<>(dataList.size());
        dataList.forEach(data -> {
            retuenMsg.add(JSONResultHandle.resultHandle(data, AppAggrementDTO.class));
        });
        return R.success(retuenMsg);
    }

    @PassToken
    @ApiOperation(value = "通过id查询商户信息表", notes = "通过id查询商户信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @PostMapping(value = "/merchant/view/{id}", name = "详情")
    public Object view(@PathVariable Long id) throws Exception {
        log.info("view merchant id:{}", id);
        MerchantDTO merchantDTO = merchantService.selectMerchantById(id);
        return R.success(JSONResultHandle.resultHandle(merchantDTO, MerchantDTO.class));
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "通过userId查询商户信息表", notes = "通过userId查询商户信息表")
    @ApiImplicitParam(name = "userId", value = "userId", dataType = "Long", required = true)
    @PostMapping(value = "/merchantDetail/getMerchant", name = "详情")
    public Object getMerchant(@RequestBody JSONObject jsonObject) {
        log.info("get merchant jsonObject:{}", jsonObject);
        MerchantDTO merchantDTO = JSONObject.parseObject(jsonObject.getJSONObject("data").toJSONString(), MerchantDTO.class);
        return R.success(merchantService.getMerchantByUserId(merchantDTO.getId()));
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "分页查询商户信息表", notes = "分页查询商户信息表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @PostMapping(name = "查询-商户信息表列表", value = "/merchant")
    public Object list(@RequestBody JSONObject jsonObject) throws Exception {
        log.info("get merchant list, data:{}", jsonObject);
        JSONObject data = jsonObject.getJSONObject("data");
        Map<String, Object> params = merchantService.getMerchantListQueryParams(data);
        int total = merchantService.countAppFindList(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<JSONObject> resultList = new ArrayList<>();
        pc = getPagingContext(data, total);
        if (total > 0) {
            scs = getSortingContext(data);
            resultList = merchantService.getMerchantList(data, params, scs, pc);
        }
        return R.success(resultList, pc);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "商户地图", notes = "商户地图")
    @PostMapping(name = "商户地图", value = "/merchantMaps")
    public Object merchantMaps(@RequestBody JSONObject jsonObject) {
        log.info("merchant map, data:{}", jsonObject);
        JSONObject data = jsonObject.getJSONObject("data");
        Map<String, Object> params = new HashMap<>(16);
        // 必要参数
        params.put("app", "app");
        List<MerchantDTO> list = merchantService.find(params, null, null);
        List<JSONObject> resultList = new ArrayList<>();
        list.forEach(item -> {
            //计算距离
            String lat = data.getString("lat");
            String lng = data.getString("lng");
            GlobalCoordinates source = new GlobalCoordinates(new Double(lat), new Double(lng));
            GlobalCoordinates target = new GlobalCoordinates(new Double(item.getLat()), new Double(item.getLng()));
            double distance = GeodesyUtil.getDistanceMeter(source, target, Ellipsoid.Sphere);
            if ((new BigDecimal(distance).divide(new BigDecimal("1000")).setScale(2, RoundingMode.HALF_UP).compareTo(new BigDecimal(DISTANCE)) == -1)) {
                JSONObject merchant = JSONResultHandle.resultHandle(item, MerchantDTO.class);
                resultList.add(merchant);
            }
        });
        return R.success(resultList);
    }

    @SignVerify
    @AppToken
    @PostMapping("/merchant/replenishDirectorAndOwner")
    @ApiOperation(value = "股东、董事添加", notes = "股东、董事添加")
    @ApiImplicitParam(name = "userId", value = "用户主表ID", dataType = "Long", required = true)
    public Object replenishDirectorAndOwner(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("app replenishDirectorAndOwner merchant DTO:{}", jsonObject);
        try {
            MerchantDTO merchantDTO = JSONObject.parseObject(jsonObject.getJSONObject("data").toJSONString(), MerchantDTO.class);
            merchantService.replenishDirectorAndOwner(merchantDTO, request);
        } catch (BizException e) {
            log.error("replenish merchant failed, merchantDTO: {}, error message:{}, error all:{}", jsonObject, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @PostMapping("/merchant/replenishMerchant")
    @ApiOperation(value = "完善商户基础信息", notes = "商户入网申请")
    @ApiImplicitParam(name = "userId", value = "用户主表ID", dataType = "Long", required = true)
    public Object replenishMerchant(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("app replenishMerchant merchant DTO:{}", jsonObject);
        try {
            MerchantDTO merchantDTO = JSONObject.parseObject(jsonObject.getJSONObject("data").toJSONString(), MerchantDTO.class);
            merchantService.replenishMerchant(merchantDTO, request);
        } catch (BizException e) {
            log.error("replenish merchant failed, merchantDTO: {}, error message:{}, error all:{}", jsonObject, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @PostMapping("/merchant/replenishBank")
    @ApiOperation(value = "完善商户银行信息", notes = "商户入网申请")
    @ApiImplicitParam(name = "userId", value = "用户主表ID", dataType = "Long", required = true)
    public Object replenishBank(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("app replenishBank merchant DTO:{}", jsonObject);
        try {
            MerchantDTO merchantDTO = JSONObject.parseObject(jsonObject.getJSONObject("data").toJSONString(), MerchantDTO.class);
            merchantService.replenishBank(merchantDTO, request);
        } catch (BizException e) {
            log.error("replenish merchant failed, merchantDTO: {}, error message:{}, error all:{}", jsonObject, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @PostMapping("/merchant/replenishLogo")
    @ApiOperation(value = "完善商户图片信息", notes = "商户入网申请")
    @ApiImplicitParam(name = "userId", value = "用户主表ID", dataType = "Long", required = true)
    public Object replenishLogo(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("app replenishLogo merchant DTO:{}", jsonObject);
        try {
            MerchantDTO merchantDTO = JSONObject.parseObject(jsonObject.getJSONObject("data").toJSONString(), MerchantDTO.class);
            merchantService.replenishLogo(merchantDTO, request);
        } catch (BizException e) {
            log.error("replenish merchant failed, merchantDTO: {}, error message:{}, error all:{}", jsonObject, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @PostMapping("/merchant/replenishContactInfo")
    @ApiOperation(value = "完善合同信息", notes = "完善合同信息")
    @ApiImplicitParam(name = "userId", value = "用户主表ID", dataType = "Long", required = true)
    public Object replenishContactInfo(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("app replenishContactInfo merchant DTO:{}", jsonObject);
        try {
            MerchantDTO merchantDTO = JSONObject.parseObject(jsonObject.getJSONObject("data").toJSONString(), MerchantDTO.class);
            merchantService.updateMerchant(merchantDTO.getId(), merchantDTO, request);
        } catch (BizException e) {
            log.error("replenish merchant failed, merchantDTO: {}, error message:{}, error all:{}", jsonObject, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @PostMapping("/merchant/submitAudit/{userId}")
    @ApiOperation(value = "提交审核", notes = "提交审核")
    @ApiImplicitParam(name = "userId", value = "用户主表ID", dataType = "Long", required = true)
    public Object submitAudit(@PathVariable("userId") Long userId, HttpServletRequest request) {
        log.info("app submitAudit userId :{}", userId);
        try {
            merchantService.submitAudit(userId, request);
        } catch (BizException e) {
            log.error("submitAudit merchant failed, userId: {}, error message:{}, error all:{}", userId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @PostMapping("/merchant/submitChange")
    @ApiOperation(value = "商户变更信息申请", notes = "商户变更信息申请")
    @ApiImplicitParam(name = "id", value = "商户ID", dataType = "Long", required = true)
    public Object submitChange(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("app submitChange merchant DTO:{}", jsonObject);
        try {
            MerchantDTO merchantDTO = JSONObject.parseObject(jsonObject.getJSONObject("data").toJSONString(), MerchantDTO.class);
            merchantService.submitChange(merchantDTO, request);
        } catch (BizException e) {
            log.error("submitChange merchant failed, merchantDTO: {}, error message:{}, error all:{}", jsonObject, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @AppToken
    @SignVerify
    @ApiOperation(value = "app 获取用户信息", notes = "app 获取用户信息")
    @PostMapping("/selectAccountUser/{id}")
    public Object selectAccountUser(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("get selectAccountUser:{}", id);
        JSONObject accountUser;
        try {
            accountUser = serverService.userInfoByQRCode(id);
        } catch (Exception e) {
            log.error("get selectAccountUser failed, card: {}, error message:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed", getLang(request)));
        }
        return R.success(JSONResultHandle.resultHandle(accountUser));
    }

    @AppToken
    @SignVerify
    @ApiOperation(value = "通过userId查询今日收款明细", notes = "通过userId查询今日收款明细")
    @ApiImplicitParam(name = "userId", value = "用户id", dataType = "Long", paramType = "path", required = true)
    @PostMapping(value = "/qrPayFlow/gathering/{userId}", name = "详情")
    public Object gathering(@PathVariable("userId") Long userId) {
        log.info("get gathering userId:{}", userId);
        return R.success(qrPayFlowService.gathering(userId));
    }

    @SignVerify
    @AppToken
    @PostMapping(name = "上传图片", value = "/multiUploadFile")
    @ApiOperation(value = "上传图片", notes = "上传图片")
    @ApiImplicitParam(name = "file", value = "图片", dataType = "String", required = true)
    public Object multiUploadFile(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
//        String fileStr = jsonObject.getJSONObject("data").getString("file");
//        MultipartFile file = BASE64DecodedMultipartFile.base64ToMultipart(fileStr);
//        String originalFilename = file.getOriginalFilename();
//        boolean flag = UploadFileUtil.checkImg(originalFilename);
//        if (flag) {
//            String fileName;
//            String sfx = originalFilename.substring(originalFilename.lastIndexOf(".")).trim().toLowerCase();
//            String key = this.IntroductionPath + "/" + SnowflakeUtil.generateId() + sfx;
//            try {
//                fileName = AmazonAwsUploadUtil.upload(file, key);
//            } catch (Exception e) {
//                log.error("upload multiUploadFile failed, error message:{}", e.getMessage(),e);
//                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
//            }
//            return R.success(fileName);
//        } else {
//            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("illegal.img", getLang(request)));
//        }
        String fileName;
        try {
            fileName = userService.multiUploadFile(jsonObject, this.IntroductionPath, request);
        } catch (Exception e) {
            log.error("upload multiUploadFile failed, error message:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("file.upload.failed", getLang(request)));
        }
        return R.success(fileName);
    }

    @SignVerify
    @AppToken
    @PostMapping(name = "上传证件图片", value = "/paperMultiUploadFile")
    @ApiOperation(value = "上传证件图片", notes = "上传证件图片")
    @ApiImplicitParam(name = "file", value = "图片", dataType = "String", required = true)
    public Object paperMultiUploadFile(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
//        String fileStr = jsonObject.getJSONObject("data").getString("file");
//        MultipartFile file = BASE64DecodedMultipartFile.base64ToMultipart(fileStr);
//        String originalFilename = file.getOriginalFilename();
//        boolean flag = UploadFileUtil.checkImg(originalFilename);
//        if (flag) {
//            String fileName;
//            String sfx = originalFilename.substring(originalFilename.lastIndexOf(".")).trim().toLowerCase();
//            String key = this.paperPath + "/" + SnowflakeUtil.generateId() + sfx;
//            try {
//                fileName = AmazonAwsUploadUtil.upload(file, key);
//            } catch (Exception e) {
//                log.error("upload paperMultiUploadFile failed, error message:{}", e.getMessage(),e);
//                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
//            }
//            return R.success(fileName);
//        } else {
//            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("illegal.img", getLang(request)));
//        }
        String fileName;
        try {
            fileName = userService.multiUploadFile(jsonObject, this.paperPath, request);
        } catch (Exception e) {
            log.error("upload paperMultiUploadFile failed, error message:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("file.upload.failed", getLang(request)));
        }
        return R.success(fileName);
    }

    @SignVerify
    @AppToken
    @PostMapping(name = "上传证件图片", value = "/asciMultiUploadFile")
    @ApiOperation(value = "上传证件图片", notes = "上传证件图片")
    @ApiImplicitParam(name = "file", value = "图片", dataType = "String", required = true)
    public Object asciMultiUploadFile(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
//        String fileStr = jsonObject.getJSONObject("data").getString("file");
//        MultipartFile file = BASE64DecodedMultipartFile.base64ToMultipart(fileStr);
//        String originalFilename = file.getOriginalFilename();
//        boolean flag = UploadFileUtil.checkImg(originalFilename);
//        if (flag) {
//            String fileName;
//            String sfx = originalFilename.substring(originalFilename.lastIndexOf(".")).trim().toLowerCase();
//            String key = this.paperPath + "/" + SnowflakeUtil.generateId() + sfx;
//            try {
//                fileName = AmazonAwsUploadUtil.upload(file, key);
//            } catch (Exception e) {
//                log.error("upload paperMultiUploadFile failed, error message:{}", e.getMessage(),e);
//                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
//            }
//            return R.success(fileName);
//        } else {
//            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("illegal.img", getLang(request)));
//        }

        String fileName;
        try {
            fileName = userService.multiUploadFile(jsonObject, this.paperPath, request);
        } catch (Exception e) {
            log.error("upload asciMultiUploadFile failed, error message:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("file.upload.failed", getLang(request)));
        }
        return R.success(fileName);
    }

    @PassToken
    @SignVerify
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

    @SignVerify
    @AppToken
    @ApiOperation(value = "根据userId查询用户信息|支付业务状态|分期付业务状态|理财业务状态", notes = "根据userId查询用户信息|支付业务状态|分期付业务状态|理财业务状态")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @PostMapping(value = "/user/{id}", name = "详情")
    public Object user(@PathVariable("id") Long id, HttpServletRequest request) {
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

    @SignVerify
    @AppToken
    @ApiOperation(value = "用户充值手续费", notes = "用户充值手续费")
    @PostMapping(value = "/serviceCharge", name = "用户充值手续费")
    public Object findOne() {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", 1);
        ParametersConfigDTO parametersConfigDTO = parametersConfigService.findOneParametersConfig(params);
        JSONObject object = JSONResultHandle.resultHandle(parametersConfigDTO, ParametersConfigDTO.class);
        return R.success(object);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "通过userId查询用户余额", notes = "通过userId查询用户余额")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @PostMapping(value = "/account/{id}")
    public Object getBalance(@PathVariable Long id) {
        log.info("getBalance user id:{}", id);
        BigDecimal balance;
        try {
            balance = userService.getBalance(id, null);
        } catch (Exception e) {
            log.error("getBalance failed, error message:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.HTTP_SEND_ERROR.getCode(), e.getMessage());
        }
        return R.success(balance != null ? balance : BigDecimal.valueOf(0));
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "查询app一条上架广告,list集合方便app处理结果", notes = "查询app一条上架广告,list集合方便app处理结果")
    @PostMapping(value = "/findOneAds", name = "查询app一条上架广告,list集合方便app处理结果")
    public Object appFindOneAds() {
        Map<String, Object> params = new ArrayMap<>();
        params.put("date", System.currentTimeMillis());
        List<AdsDTO> adsDTO = adsService.appFindOneAds(params);
        List<JSONObject> retuenMsg = new ArrayList<>(adsDTO.size());
        adsDTO.forEach(data -> {
            retuenMsg.add(JSONResultHandle.resultHandle(data, AdsDTO.class));
        });
        return R.success(retuenMsg);
    }


    @PassToken
    @ApiOperation(value = "h5页面调取 aboutUs.html", notes = "h5页面调取 aboutUs.html")
    @GetMapping(value = "/aboutUs", name = "h5页面调取 aboutUs.html")
    public Object list(HttpServletRequest request) {
        if (redisUtils.get("aboutUs") != null) {
            return R.success(JSONObject.parse(redisUtils.get("aboutUs").toString()), null);
        }
        Map<String, Object> params = getConditionsMap(request);
        int total = appAboutUsService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<AppAboutUsDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = appAboutUsService.find(params, scs, pc);
            redisUtils.set("aboutUs", JSONObject.toJSONString(list));
        }
        return R.success(list, pc);
    }

    @SignVerify
    @AppToken
    @PostMapping("/saveOmiPayOrderNo")
    @ApiOperation(value = "保存订单号", notes = "保存订单号")
    public Object saveOmiPayOrderNo(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("save omi pay order no, data:{}", requestInfo);
        JSONObject omiPayRequestInfo = requestInfo.getJSONObject("data");
        try {
            withholdFlowService.saveOmiPayOrderNo(omiPayRequestInfo, request);
        } catch (Exception e) {
            log.error("save omi pay order no, withholdFlow flowId: {}, error message:{}, error all:{}", omiPayRequestInfo.getLong("flowId"), e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
//    @PostMapping("/refund")
    @ApiOperation(value = "退款", notes = "退款")
    public Object refund(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("refund, data:{}", requestInfo);
        OrderRefundDTO orderRefundDTO = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), OrderRefundDTO.class);
        JSONObject result = null;
        try {
            if (orderRefundDTO.getGatewayType().equals(StaticDataEnum.GATEWAY_TYPE_0.getCode())) {
                result = orderRefundService.cardTotalRefundApply(orderRefundDTO, request);
            } else {
                orderRefundService.refundApply(orderRefundDTO, request);
            }
        } catch (Exception e) {
            log.error("refund failed, data: {}, error message:{}, error all:{}", orderRefundDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        log.info("refund result:{}", result);
        return R.success(result);
    }

    @SignVerify
    @AppToken
    @PostMapping("/findMerchantStaff/{merchantId}")
    @ApiOperation(value = "查询商户员工", notes = "查询商户员工")
    public Object setUserAction(@PathVariable("merchantId") Long merchantId, HttpServletRequest request) {
        log.info("find merchant staff, merchantId:{}", merchantId);
        List<JSONObject> list = null;
        try {
            list = userService.findMerchantStaff(merchantId, request);
        } catch (BizException e) {
            log.error("find merchant staff failed, merchantId: {}, error message:{}, error all:{}", merchantId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success(list);
    }

    @SignVerify
    @AppToken
    @PostMapping("/getAppActions")
    @ApiOperation(value = "查询APP权限", notes = "查询APP权限")
    public Object getAppActions(HttpServletRequest request) {
        log.info("find app action");
        Map<String, Object> params = new HashMap<>(1);
        params.put("appAction", StaticDataEnum.ACTION_TYPE_1.getCode());
        List<ActionDTO> list = actionService.find(params, null, null);
        List<JSONObject> results = new ArrayList<>(1);
        list.stream().forEach(actionDTO -> {
            results.add(JSONResultHandle.resultHandle(actionDTO, ActionDTO.class));
        });
        return R.success(results);
    }

    @SignVerify
    @AppToken
    @PostMapping("/setUserAction")
    @ApiOperation(value = "设置用户权限", notes = "设置用户权限")
    public Object setUserAction(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("set user action, data:{}", requestInfo);
        requestInfo = requestInfo.getJSONObject("data");
        try {
            userActionService.setUserAction(requestInfo, request);
        } catch (BizException e) {
            log.error("set user action failed, data: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success();
    }

    @SignVerify
    @AppToken
    @PostMapping("/getUserAction/{userId}")
    @ApiOperation(value = "获取用户权限", notes = "获取用户权限")
    public Object getUserAction(@PathVariable("userId") Long userId, HttpServletRequest request) {
        log.info("get user action, userId:{}", userId);
        List<UserAction> list = null;
        try {
            list = userActionService.getUserAction(userId, request);
        } catch (BizException e) {
            log.error("get user action failed, userId: {}, error message:{}, error all:{}", userId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success(list);
    }

    @SignVerify
    @AppToken
    @PostMapping("/resetMerchantStaffInfo")
    @ApiOperation(value = "重置商户员工信息, type为0重置密码,1修改电话", notes = "重置商户员工信息, type为0重置密码,1修改电话")
    public Object resetMerchantStaffInfo(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("reset merchant staff password, data:{}", requestInfo);
        try {
            userService.resetMerchantStaffInfo(requestInfo.getJSONObject("data"), request);
        } catch (BizException e) {
            log.error("reset merchant staff password, userId: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success();
    }

    @SignVerify
    @AppToken
    @PostMapping("merchantStaffDelete/{id}")
    @ApiOperation(value = "删除商户员工", notes = "删除商户员工")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("remove merchantStaff, id:{}", id);
        try {
            userService.logicDeleteUser(id, request);
        } catch (BizException e) {
            log.error("remove merchantStaff, user id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @PostMapping("/location")
    @ApiOperation(value = "定位", notes = "定位")
    public Object location(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("location data:{}", requestInfo);
        try {
            userService.location(requestInfo.getJSONObject("data"), request);
        } catch (Exception e) {
            log.error("location data:{}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "商户店铺选择", notes = "商户店铺选择")
    @PostMapping("/merchantLogin")
    public Object merchantLogin(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("app merchantLogin info:{}", jsonObject);
        JSONObject msg = null;
        try {
            msg = userService.merchantLogin(jsonObject.getJSONObject("data"), request);
        } catch (Exception e) {
            log.error("app merchantLogin failed, info: {}, error message:{}, error all:{}", jsonObject, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.LOGIN__ERROR.getCode(), e.getMessage());
        }

        return R.success(msg);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "获取商户登录列表", notes = "获取商户登录列表")
    @PostMapping("/getMerchantLoginList")
    public Object getMerchantLoginList(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("app getMerchantLoginList info:{}", jsonObject);
        JSONObject msg = null;
        try {
            msg = merchantService.getMerchantLoginList(jsonObject.getJSONObject("data"), request);
        } catch (Exception e) {
            log.error("app getMerchantLoginList failed, userDTO: {}, error message:{}, error all:{}", jsonObject, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.LOGIN__ERROR.getCode(), e.getMessage());
        }

        return R.success(msg);
    }

    /**
     * 查询pinNumber
     *
     * @param id
     * @param request
     * @return
     */
    @SignVerify
    @AppToken
    @ApiOperation(value = "查询是否有PINnumber", notes = "查询是否有PINnumber")
    @PostMapping("/queryPinNumber/{id}")
    public Object queryPinNumber(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("query pinNumber, id:{}", id);
        String result = null;
        try {
            result = userService.queryPinNumber(id, request);
        } catch (Exception e) {
            log.error("query pinNumber failed, user id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ResultCode.ERROR, e.getMessage());
        }
        return R.success(result);
    }

    /**
     * 修改pinNumber
     *
     * @param id
     * @param request
     * @return
     */
    @SignVerify
    @AppToken
    @ApiOperation(value = "修改pinNumber", notes = "修改pinNumber")
    @PostMapping("/updatePinNumber/{id}/{pinNumber}")
    public Object updatePinNumber(@PathVariable("id") Long id, @PathVariable("pinNumber") String pinNumber, HttpServletRequest request) {
        log.info("query pinNumber, id:{}, pin:{}", id, pinNumber);
        try {
            userService.updatePinNumber(id, pinNumber, request);
        } catch (Exception e) {
            log.error("query pinNumber failed, user id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ResultCode.ERROR, e.getMessage());
        }
        return R.success(I18nUtils.get("pin.save.success", getLang(request)));
    }

    /**
     * 校验pinNumber
     *
     * @param id
     * @param request
     * @return
     */
    @SignVerify
    @AppToken
    @ApiOperation(value = "校验PINnumber", notes = "校验PINnumber")
    @PostMapping("/checkPinNumber/{id}/{pinNumber}")
    public Object checkPinNumber(@PathVariable("id") Long id, @PathVariable("pinNumber") String pinNumber, HttpServletRequest request) {
        log.info("query pinNumber, id:{}, pin:{}", id, pinNumber);
        try {
            userService.checkPinNumber(id, pinNumber, request);
        } catch (Exception e) {
            log.error("query pinNumber failed, user id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "新增商户，返回商户号", notes = "新增商户，返回商户号")
    @PostMapping("/addMerchant")
    public Object addMerchant(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("app addMerchant info:{}", jsonObject);
        JSONObject msg = null;
        try {
            msg = userService.addMerchant(jsonObject.getJSONObject("data"), request);
        } catch (Exception e) {
            log.error("app addMerchant failed, userDTO: {}, error message:{}, error all:{}", jsonObject, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.LOGIN__ERROR.getCode(), e.getMessage());
        }

        return R.success(msg);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "分页查询支付订单", notes = "分页查支付订单以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单编号", paramType = "query", dataType = "long", required = false),
            @ApiImplicitParam(name = "email", value = "付款方账号", paramType = "query", dataType = "string", required = false),
            @ApiImplicitParam(name = "corporateName", value = "商户名", paramType = "query", dataType = "string", required = false),
            @ApiImplicitParam(name = "gatewayId", value = "渠道", paramType = "query", dataType = "long", required = false),
            @ApiImplicitParam(name = "state", value = "订单状态", paramType = "query", dataType = "int", required = false),
            @ApiImplicitParam(name = "clearState", value = "订单清算状态", paramType = "query", dataType = "int", required = false),
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @PostMapping("/payBorrowList")
    public Object PayBorrowList(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("pay borrow list, data:{}", requestInfo);
        Map<String, Object> params = requestInfo.getJSONObject("data");
        params.put("state", StaticDataEnum.TRANS_STATE_31.getCode());
        int total = qrPayFlowService.selectPayBorrowCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<PayBorrowDTO> list = new ArrayList<>();
        pc = getPagingContext(requestInfo.getJSONObject("data"), total);
        List<JSONObject> returnData = new ArrayList<>(1);
        if (total > 0) {
            scs = getSortingContext(requestInfo.getJSONObject("data"));
            list = qrPayFlowService.selectPayBorrow(params, scs, pc);
            list.stream().forEach(payBorrowDTO -> {
                JSONObject result = JSONResultHandle.resultHandle(payBorrowDTO, PayBorrowDTO.class);
                returnData.add(result);
            });
        }
        return R.success(returnData, pc);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "退款原因", notes = "退款原因")
    @PostMapping("/selectRefundReason/{flowId}")
    public Object selectRefundReason(@PathVariable("flowId") Long flowId, HttpServletRequest request) {
        List<RefundFlowDTO> list = refundFlowService.selectReason(flowId);
        List<JSONObject> returnData = new ArrayList<>(1);
        list.stream().forEach(refundFlowDTO -> {
            returnData.add(JSONResultHandle.resultHandle(refundFlowDTO, RefundFlowDTO.class));
        });
        return R.success(returnData);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "获取ToDeals", notes = "获取ToDeals")
    @PostMapping("/getToDealsImg")
    public Object getToDealsImg() {
        List<TopDealDTO> pathList = topDealService.getToDealsImg();
        List<JSONObject> list = new ArrayList<>(pathList.size());
        pathList.forEach(item -> {
            list.add(JSONResultHandle.resultHandle(item, TopDealDTO.class));
        });
        return R.success(list);
    }

    @SignVerify
    @AppToken
    @PostMapping("/bindNFC/{merchantId}/{code}")
    @ApiOperation(value = "绑定NFC", notes = "绑定NFC")
    public Object bindNFC(@PathVariable("merchantId") Long merchantId, @PathVariable("code") String code, HttpServletRequest request) {
        log.info("bindNFC merchantId:{},code :{}", merchantId, code);
        try {
            //查询商户信息和店长信息
            NfcCodeInfoDTO nfcCodeInfoDTO = new NfcCodeInfoDTO();
            nfcCodeInfoDTO.setMerchantId(merchantId);
            nfcCodeInfoDTO.setCode(code);
            nfcCodeInfoDTO = nfcCodeInfoService.checkMerchant(null, nfcCodeInfoDTO, request);
            nfcCodeInfoService.updateNfcCodeInfo(nfcCodeInfoDTO.getId(), nfcCodeInfoDTO, request);
        } catch (Exception e) {
            log.error("bindNFC code:{}, error message:{}, error all:{}", code, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @PostMapping("/removeBindNFC/{merchantId}/{code}")
    @ApiOperation(value = "NFC解除绑定", notes = "NFC解除绑定")
    public Object removeBindNFC(@PathVariable("merchantId") Long merchantId, @PathVariable("code") String code, HttpServletRequest request) {
        log.info("removeBindNFC merchantId:{},code :{}", merchantId, code);
        try {
            NfcCodeInfoDTO nfcCodeInfoDTO = nfcCodeInfoService.getRemoveBind(merchantId, code, null, request);
            nfcCodeInfoService.removeBindNfcCodeInfo(nfcCodeInfoDTO.getId(), nfcCodeInfoDTO, request);
        } catch (Exception e) {
            log.error("removeBindNFC code:{}, erromr message:{}, error all:{}", code, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), I18nUtils.get("nfc.remove.error", getLang(request)));
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @PostMapping("/getNFCList/{merchantId}")
    @ApiOperation(value = "NFC列表", notes = "NFC列表")
    public Object getNFCList(@PathVariable("merchantId") Long merchantId, HttpServletRequest request) {
        log.info("getNFCList merchantId:{}", merchantId);
        List<NfcCodeInfoDTO> list = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("merchantId", merchantId);
            list = nfcCodeInfoService.find(params, null, null);
        } catch (Exception e) {
            log.error("getNFCList merchantId:{}, error message:{}, error all:{}", merchantId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), I18nUtils.get("query.failed", getLang(request)));
        }
        return R.success(list);
    }


    @SignVerify
    @AppToken
    @ApiOperation(value = "风控", notes = "风控")
    @PostMapping("/riskCheck")
    public Object riskCheck(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("risk data, data:{}", requestInfo);
        JSONObject data = requestInfo.getJSONObject("data");
        JSONObject result = null;
        try {
            String birth = data.getString("birth");
            String processBirth = this.processBirth(birth);
            data.put("birth", processBirth);
            log.info("==============/riskCheck 修改后的的生日为: {}",processBirth);
            userService.riskCheckParamsCheck(data, request);
            result = userService.riskCheck(requestInfo.getJSONObject("data"), request).get();
        } catch (Exception e) {
            log.error("risk check faile, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    private String processBirth(String birth) {
        String[] birthList = birth.split("-");
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            String str = birthList[i];
            if (i<2){
                if (str.length()>2) {
                    str = str.substring(1);
                }
                str = str + "-";
            }
            res.append(str);
        }
        return res.toString();
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "nfc码查询用户", notes = "nfc查询用户")
    @ApiImplicitParam(name = "findUserInfoByNFCCode", value = "nfc查询用户", dataType = "string", paramType = "path", required = true)
    @PostMapping("/findUserInfoByNFCCode")
    public Object findUserInfoByNFCCode(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("find user info by nfc code, data:{}", requestInfo);
        JSONObject object = null;
        JSONObject data = requestInfo.getJSONObject("data");
        try {
            object = nfcCodeInfoService.findUserInfoByNFCCode(data, request);
        } catch (Exception e) {
            log.error("findUserInfoByNFCCode  failed: {}, error message:{}", data, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(),e.getMessage());
        }
        return R.success(JSONResultHandle.resultHandle(object));
    }

    @SignVerify
    @AppToken
    @PostMapping("/merchant/setDiscountRate")
    @ApiOperation(value = "设置折扣率", notes = "设置折扣率")
    @ApiImplicitParam(name = "userId", value = "用户主表ID", dataType = "Long", required = true)
    public Object setDiscountRate(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("app setDiscountRate merchant DTO:{}", jsonObject);
        try {
            MerchantDTO merchantDTO = JSONObject.parseObject(jsonObject.getJSONObject("data").toJSONString(), MerchantDTO.class);
            merchantService.setDiscountRate(merchantDTO, request);
        } catch (BizException e) {
            log.error("setDiscountRate merchant failed, merchantDTO: {}, error message:{}, error all:{}", jsonObject, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }


    @SignVerify
    @AppToken
    @ApiOperation(value = "查询理财标志位", notes = "查询理财标志位")
    @PostMapping("/getAgreementState")
    public Object getAgreementState(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("getAgreementState requestInfo:{}", requestInfo);
        JSONObject result = null;
        try {
            result = userService.getAgreementState(Long.parseLong(requestInfo.getJSONObject("data").getString("userId")));
        } catch (Exception e) {
            log.error("getAgreementState, requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "用户同意理财", notes = "用户同意理财")
    @PostMapping("/updateAgreementState")
    public Object updateAgreementState(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("updateAgreementState requestInfo:{}", requestInfo);
        try {
            userService.updateAgreementState(requestInfo.getJSONObject("data"), request);
        } catch (Exception e) {
            log.error("updateAgreementState, requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "获取银行logo", notes = "获取银行logo")
    @PostMapping("/getBankLogoList")
    public Object getBankLogo() throws Exception {
        JSONArray bankLogo = userService.getBankLogoList();
        return R.success(bankLogo);
    }

    @PassToken
    @ApiOperation(value = "获取银行logo", notes = "获取银行logo")
    @GetMapping("/getBankLogo")
    public void getBankLogo(@RequestParam("bankName") String bankName, @RequestParam("type") Integer type, HttpServletResponse response) throws Exception {
        log.info("get bank logo, bank name:{}, type:{}", bankName, type);
        String img = userService.getBankLogo(bankName, type);
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(new BASE64Decoder().decodeBuffer(img));
        outputStream.flush();
        outputStream.close();
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "获取kycResult", notes = "获取kycResult")
    @PostMapping("/kycResult")
    public Object kycResult(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("kyc result, data:{}", requestInfo);
        String userId = requestInfo.getJSONObject("data").getString("userId");
        JSONObject result = new JSONObject();
        try {
            result = userService.getKycResult(userId, request);
        } catch (Exception e) {
            log.error("kycResult, requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }

        return R.success(result);
    }

    @ApiOperation(value = "商户绑定二维码", notes = "商户绑定二维码")
    @AppToken
    @PostMapping("/merchantBindingQRCode")
    public Object merchantBindingQRCode(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("merchant binding QR code, data:{}", requestInfo);
        try {
            qrcodeInfoService.merchantBindingQRCode(requestInfo.getJSONObject("data"), request);
        } catch (Exception e) {
            log.info("merchant binding QR code failed, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("binding.qrcode.failed", getLang(request)));
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "修改邮箱", notes = "修改邮箱")
    @PostMapping("/modifyEmail")
    public Object modifyEmail(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("modify email , data:{}", requestInfo);
        //TODO 测试用
//        Long userId = requestInfo.getJSONObject("data").getLong("userId");
        Long userId = this.getUserId(request);
        try {
            userService.modifyEmail(requestInfo.getJSONObject("data"), userId, request);
        } catch (Exception e) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "商户docusign申请", notes = "商户docusign申请")
    @PostMapping("/merchantDocusignRequest/{merchantId}/{contractType}")
    public Object merchantDocusignRequest(@PathVariable("merchantId") Long merchantId, @PathVariable("contractType") Integer contractType, HttpServletRequest request) {
        log.info("merchant docusign request, merchantId:{}, contractType:{}", merchantId, contractType);
        JSONObject result = null;
        try {
            result = merchantService.docusignContract(merchantId, contractType, request);
        } catch (Exception e) {
            log.info("merchant docusign request failed, merchantId:{}, contractType:{}, error message:{}, e:{}", merchantId, contractType, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @PassToken
    @ApiOperation(value = "app获取时间戳", notes = "app获取时间戳")
    @PostMapping("/getTimeMillis")
    public Object getTimeMillis() {
        return R.success(System.currentTimeMillis());
    }

    @PassToken
    @ApiOperation(value = "协议地址", notes = "协议地址")
    @GetMapping("/protocolAddress")
    public Object protocolAddress() {
        JSONObject creditAgreementHtml = new JSONObject();
        creditAgreementHtml.put("name", "Direct Debit Terms and Conditions");
        creditAgreementHtml.put("html", creditAgreement);
        JSONObject customerHtml = new JSONObject();
        customerHtml.put("name", "Customers Terms And Conditions");
        customerHtml.put("html", customer);
        JSONObject[] address = new JSONObject[]{creditAgreementHtml, customerHtml};
        return R.success(address);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "获取邀请码", notes = "获取邀请码")
    @PostMapping("/getInviteCode")
    public Object getInviteCode(HttpServletRequest request) {
        JSONObject result = null;
        try {
            result = userService.getInviteCode(request);
        } catch (Exception e) {
            log.info("get invite code failed, error message:{}, e:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("get.invite.code.fail", getLang(request)));
        }
        return R.success(result);
    }

    @SignVerify
    @AppToken
    @PostMapping("/merchantRemove/{merchantId}")
    @ApiOperation(value = "删除商户信息表", notes = "删除商户信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object merchantRemove(@PathVariable("merchantId") Long merchantId, HttpServletRequest request) {
        log.info("delete merchant, id:{}", merchantId);
        try {
            MerchantDTO merchantDTO = merchantService.findMerchantById(merchantId);
            if (!StringUtils.isEmpty(merchantDTO.getDocusignEnvelopeid())) {
                throw new BizException(I18nUtils.get("merchant.delete.error", getLang(request)));
            }
            merchantService.logicDeleteMerchant(merchantId, request);
        } catch (BizException e) {
            log.error("delete failed, merchant id: {}, error message:{}, error all:{}", merchantId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "钱包支出、收入明细", notes = "钱包支出、收入明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @PostMapping("/selectWalletTransaction")
    public Object selectWalletTransaction(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        Map<String, Object> params = requestInfo.getJSONObject("data");
        int total = accountFlowService.selectWalletTransactionCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        pc = getPagingContext(requestInfo.getJSONObject("data"), total);
        List<JSONObject> list = null;
        if (total > 0) {
            scs = getSortingContext(requestInfo.getJSONObject("data"));
            list = accountFlowService.selectWalletTransaction(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @PassToken
    @SignVerify
    @ApiOperation(value = "州、城市二级数组", notes = "州、城市二级数组")
    @PostMapping(value = "/findArea", name = "州、城市二级数组")
    public Object findArea(HttpServletRequest request) {
        List<StaticDataDTO> states = staticDataService.findArea();
        return R.success(states);
    }

    @PassToken
    @SignVerify
    @ApiOperation(value = "财富页查询", notes = "财富页查询")
    @PostMapping(value = "/wealthPage/{merchantId}", name = "财富页查询")
    public Object wealthPage(@PathVariable("merchantId") Long merchantId, HttpServletRequest request) {
        log.info("wealthPage query merchant, id:{}", merchantId);
        Map<String, Object> result = null;
        try {
            result = qrPayFlowService.wealthPageQuery(request, merchantId);
        } catch (Exception e) {
            log.error("wealthPage Exception  merchantId: {}, error message:{}, error all:{}", merchantId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @PassToken
    @SignVerify
    @ApiOperation(value = "订单列表查询(按时间区间查询)", notes = "订单列表查询")
    @PostMapping(value = "/orderPageList", name = "订单列表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    public Object orderPageList(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("orderPageList query , requestInfo:{}", requestInfo);
        Map<String, Object> params = requestInfo.getJSONObject("data");
        if (params.containsKey("queryWholeSale") && Constant.ONE.equals(params.get("queryWholeSale").toString())) {
            params.put("isWholeSale", Constant.ONE);
        }
        int[] stateList = {StaticDataEnum.TRANS_STATE_31.getCode(), StaticDataEnum.TRANS_STATE_1.getCode()};
        params.put("stateList", stateList);

        int total = qrPayFlowService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        pc = getPagingContext(requestInfo.getJSONObject("data"), total);
        List<QrPayFlowDTO> list = null;
        Map<String, Object> result = new HashMap<>();
        if (total > 0) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM.dd HH:mm", Locale.US);
            scs = getSortingContext(requestInfo.getJSONObject("data"));
            list = qrPayFlowService.find(params, scs, pc);
            for(QrPayFlowDTO qrPayFlowDTO:list){
                if(qrPayFlowDTO.getSaleType().compareTo(StaticDataEnum.SALE_TYPE_1.getCode()) == 0){
                    //整体出售订单 清算状态改为已清算
                    qrPayFlowDTO.setClearState(StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
                }
                qrPayFlowDTO.setTransTime(simpleDateFormat.format(qrPayFlowDTO.getCreatedDate()));
            }
            BigDecimal totalTransAmount = list.stream().map(QrPayFlowDTO::getTransAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalClearAmount = list.stream().map(QrPayFlowDTO::getTransAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            result.put("list", list);
            result.put("totalTransAmount", totalTransAmount);
            result.put("totalClearAmount", totalClearAmount);
        }
        return R.success(result, pc);
    }

    @PassToken
    @SignVerify
    @ApiOperation(value = "财富-订单管理头部查询", notes = "财富-订单管理头部查询")
    @PostMapping(value = "/orderPageHead/{merchantId}", name = "财富-订单管理头部查询")
    public Object orderPageHead(@PathVariable("merchantId") Long merchantId, HttpServletRequest request) {
        log.info("orderPageHead query merchant, merchantId:{}", merchantId);
        Map<String, Object> result = null;
        try {
            result = qrPayFlowService.orderPageHeadQuery(request, merchantId);
        } catch (Exception e) {
            log.error("orderPageHead Exception  merchantId: {}, error message:{}, error all:{}", merchantId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @PassToken
    @SignVerify
    @ApiOperation(value = "订单列表查询(按月分组查询)", notes = "订单列表查询")
    @PostMapping(value = "/orderPageGroupByMonthList", name = "订单列表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    public Object orderPageGroupByMonthList(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("orderPageGroupByMonthList query , requestInfo:{}", requestInfo);
        Map<String, Object> params = requestInfo.getJSONObject("data");
        int[] stateList = {StaticDataEnum.TRANS_STATE_31.getCode(), StaticDataEnum.TRANS_STATE_1.getCode()};
        params.put("stateList", stateList);
        if (params.containsKey("queryWholeSale") && Constant.ONE.equals(params.get("queryWholeSale").toString())) {
            params.put("isWholeSale", Constant.ONE);
        }
        int total = qrPayFlowService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        pc = getPagingContext(requestInfo.getJSONObject("data"), total);
        List<Map<String, Object>> list = null;
        if (total > 0) {
            scs = getSortingContext(requestInfo.getJSONObject("data"));
            list = qrPayFlowService.getOrderPageGroupByMonthList(params, scs, pc);
        }

        return R.success(list, pc);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "整体出售申请", notes = "整体出售申请")
    @PostMapping("/wholeSaleApply")
    public Object wholeSaleApply(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("whole sale apply, data:{}", requestInfo);
        try {
            wholeSalesFlowService.wholeSaleApply(JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), WholeSalesFlowDTO.class), request);
        } catch (Exception e) {
            log.info("whole sale apply, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "商户整体出售详情", notes = "商户整体出售详情")
    @PostMapping("/wholeSaleMerchantOrderDetails/{merchantId}")
    public Object wholeSaleMerchantOrderDetails(@PathVariable("merchantId") Long merchantId, HttpServletRequest request) throws Exception {
        return R.success(wholeSalesFlowService.merchantOrderDetails(merchantId, request));
    }

    @AppToken
    @SignVerify
    @ApiOperation(value = "app整体出售订单", notes = "订单列表查询")
    @PostMapping(value = "/appWholeSaleOrder", name = "订单列表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    public Object appWholeSaleOrder(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("app whole sale order query , requestInfo:{}", requestInfo);
        Map<String, Object> params = requestInfo.getJSONObject("data");
        int total = wholeSalesFlowService.appWholeSaleOrderCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        pc = getPagingContext(requestInfo.getJSONObject("data"), total);
        List<JSONObject> list = null;
        if (total > 0) {
            scs = getSortingContext(requestInfo.getJSONObject("data"));
            list = wholeSalesFlowService.appWholeSaleOrder(params, scs, pc);
        }

        return R.success(list, pc);
    }

    @PassToken
    @SignVerify
    @ApiOperation(value = "订单列表查询(按日分组查询)", notes = "订单列表查询")
    @PostMapping(value = "/orderPageGroupByDateList", name = "订单列表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    public Object orderPageGroupByDateList(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("orderPageGroupByDateList query , requestInfo:{}", requestInfo);
        Map<String, Object> params = requestInfo.getJSONObject("data");
        int[] stateList = {StaticDataEnum.TRANS_STATE_31.getCode(), StaticDataEnum.TRANS_STATE_1.getCode()};
        params.put("stateList", stateList);
        if (params.containsKey("queryWholeSale") && Constant.ONE.equals(params.get("queryWholeSale").toString())) {
            params.put("isWholeSale", Constant.ONE);
        }
        int total = qrPayFlowService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        pc = getPagingContext(requestInfo.getJSONObject("data"), total);
        List<Map<String, Object>> list = null;
        if (total > 0) {
            scs = getSortingContext(requestInfo.getJSONObject("data"));
            list = qrPayFlowService.orderPageGroupByDayList(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @SignVerify
    @AppToken
    @PostMapping("/interestCredistOrder")
    @ApiOperation(value = "扫码支付交易", notes = "扫码支付交易")
    public Object interestCredistOrder(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("app/interestCredistOrder  PayDTO:{}",  requestInfo);

        Object resObj = null;
        try {
            QrPayDTO qrPayDTO = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), QrPayDTO.class);
            //入参校验
            qrPayService.qrPayReqCheck(qrPayDTO,request);
            //扫码支付交易
            resObj =qrPayService.interestCredistOrder(qrPayDTO,request);
        } catch (Exception e) {
            log.error("app/interestCredistOrder, qrPayDTO: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(resObj);
    }


    @PassToken
    @SignVerify
    @ApiOperation(value = "结算管理-订单列表查询(按日分组查询)", notes = "订单列表查询")
    @PostMapping(value = "/clearOrderPageGroupByDateList", name = "订单列表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    public Object clearOrderPageGroupByDateList(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("clearOrderPageGroupByDateList query , requestInfo:{}", requestInfo);
        Map<String, Object> params = requestInfo.getJSONObject("data");

        List<Map<String, Object>> list = new ArrayList<>();
        try {
            if (!params.containsKey("clearState") || !params.containsKey("merchantId")) {
                throw new BizException("params error");
            }
            params.put("userId",null);
            MerchantDTO merchantDTO = merchantService.findMerchantById(Long.parseLong(params.get("merchantId").toString()));
            int total = clearBatchService.getClearCount(params,merchantDTO);
            PagingContext pc;
            Vector<SortingContext> scs;
            pc = getPagingContext(requestInfo.getJSONObject("data"), total);
            if (total > 0) {
                scs = getSortingContext(requestInfo.getJSONObject("data"));
                list = clearBatchService.getClearList(params ,merchantDTO, scs, pc);
            }

            log.info("clearOrderPageGroupByDateList result:"+list);
            return R.success(list, pc);
        } catch (Exception e) {
            log.info("clearOrderPageGroupByDateList, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

    }


    @PassToken
    @SignVerify
    @ApiOperation(value = "已结算流水列表查询(按日分组查询)", notes = "订单列表查询")
    @PostMapping(value = "/clearFlowListGroupByDate", name = "订单列表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    public Object clearFlowListGroupByDate(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("clearFlowListGroupByDate query , requestInfo:{}", requestInfo);
        Map<String, Object> params = requestInfo.getJSONObject("data");

        List<Map<String,Object>> result = new ArrayList<>();
        try {

            params.put("state",StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
            int total = clearDetailService.count(params);
            PagingContext pc;
            Vector<SortingContext> scs;
            pc = getPagingContext(requestInfo.getJSONObject("data"), total);
            scs = getSortingContext(requestInfo.getJSONObject("data"));

            if (total > 0) {
                result = clearDetailService.clearFlowListGroupByDate(params,scs,pc);
            }
            return R.success(result, pc);
        } catch (Exception e) {
            log.info("clearFlowListGroupByDate, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

    }


    @PassToken
    @SignVerify
    @ApiOperation(value = "查询一笔结算详情列表", notes = "查询一笔结算详情列表")
    @PostMapping(value = "/getClearFlowDetailById/{merchantId}/{clearId}", name = "查询一笔结算详情列表")
    public Object getClearFlowDetailById(@PathVariable("merchantId") Long merchantId ,@PathVariable("clearId") Long clearId, HttpServletRequest request) {
        log.info("getClearFlowDetailById query , clearId:{}", clearId);
        Map<String, Object> result = null;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("scs","created_date(desc)");
            Vector<SortingContext> scs = getSortingContext(jsonObject);
            result = clearBatchService.getClearFlowDetailById(merchantId,clearId,scs,request);

        } catch (Exception e) {
            log.info("getClearFlowDetailById , clearId:{}, error message:{}, e:{}", clearId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success(result);
    }

    @PassToken
    @SignVerify
    @ApiOperation(value = "查询一笔订单详情", notes = "查询一笔订单详情")
    @PostMapping(value = "/getOneDetailById/{id}", name = "查询一笔结算详情列表")
    public Object getOneDetailById(@PathVariable("id") Long id , HttpServletRequest request) {
        log.info("getOneDetailById query , id:{}", id);
        try {
            Map<String,Object> map = new HashMap<>();
            QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(id);
            if(qrPayFlowDTO == null || qrPayFlowDTO.getId() == null ){
                Map<String, Object> params = new HashMap<>(1);
                params.put("id", id);
                WholeSalesFlowDTO wholeSalesFlowDTO = wholeSalesFlowService.findOneWholeSalesFlow(params);
                wholeSalesFlowDTO.setClearState(wholeSalesFlowDTO.getSettlementState());
                return R.success(wholeSalesFlowDTO);
            }else{
                return R.success(qrPayFlowDTO);
            }
        } catch (Exception e) {
            log.info("getOneDetailById query , id:{}, error message:{}, e:{}",  id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @PassToken
    @SignVerify
    @ApiOperation(value = "结算管理首页搜索框", notes = "结算管理首页搜索框")
    @PostMapping(value = "/queryMerchantTransFlowList/{merchantId}/{queryTransNo}", name = "结算管理首页搜索框")
    public Object queryMerchantTransFlowList(@RequestBody JSONObject requestInfo,@PathVariable("merchantId") Long merchantId ,@PathVariable("queryTransNo") String queryTransNo, HttpServletRequest request) {
        log.info("queryMerchantTransFlowList query , merchantId:{},saleType:{}", merchantId,queryTransNo);

        Map<String,Object> params = new HashMap<>();
        params.put("merchantId",merchantId);
        params.put("queryTransNo",queryTransNo);
        params.put("state",StaticDataEnum.TRANS_STATE_1.getCode());
        if(requestInfo.containsKey("clearState")){
            params.put("clearState",requestInfo.get("clearState"));
            params.put("settlementState",requestInfo.get("clearState"));
        }

        int[] stateList = {StaticDataEnum.TRANS_STATE_31.getCode(), StaticDataEnum.TRANS_STATE_1.getCode()};
        params.put("stateList", stateList);
        params.put("notWholeSale",1);

        int transNum = qrPayFlowService.count(params);
        int wholeSaleNum = wholeSalesFlowService.count(params);
        int total = transNum + wholeSaleNum;

        PagingContext pc;
        pc = getPagingContext(requestInfo.getJSONObject("data"), total);
        List<QrPayFlowDTO> list = null;
        if(total>0){
            list = qrPayFlowService.getAllTransFlowList(params,null,pc);
        }

        return R.success(list,pc);
    }

    @PassToken
    @SignVerify
    @ApiOperation(value = "结算管理-结算订单搜索框", notes = "结算管理-结算订单搜索框")
    @PostMapping(value = "/queryMerchantClearFlowList/{merchantId}/{queryTransNo}", name = "结算管理-结算订单搜索框")
    public Object queryMerchantClearFlowList(@RequestBody JSONObject requestInfo,@PathVariable("merchantId") Long merchantId ,@PathVariable("queryTransNo") String queryTransNo, HttpServletRequest request) {
        log.info("queryMerchantClearFlowList query , merchantId:{},saleType:{}", merchantId,queryTransNo);

        Map<String,Object> params = new HashMap<>();
        params.put("merchantId",merchantId);
        params.put("queryTransNo",queryTransNo);
        params.put("state",StaticDataEnum.TRANS_STATE_1.getCode());
        int total = clearDetailService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;

        pc = getPagingContext(requestInfo.getJSONObject("data"), total);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("scs","created_date(desc)");
        scs = getSortingContext(jsonObject);
        List<ClearDetailDTO> list = null;
        if(total>0){
            list = clearDetailService.find(params,scs,pc);
        }

        return R.success(list,pc);
    }

    @PassToken
    @SignVerify
    @ApiOperation(value = "输入营销码获得红包", notes = "输入营销码获得红包")
    @PostMapping(value = "/enterPromotionCode", name = "输入营销码获得红包")
    public Object enterPromotionCode(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("enterPromotionCode  , requestInfo:",requestInfo);
        try {
            userService.enterPromotionCode(requestInfo.getJSONObject("data"),request);
        } catch (Exception e) {
            log.info("enterPromotionCode  , requestInfo:{}, error message:{}, e:{}",  requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PassToken
    @SignVerify
    @ApiOperation(value = "查询获得的营销码红包列表", notes = "查询获得的营销码红包列表")
    @PostMapping(value = "/promotionRecords", name = "查询获得的营销码红包列表")
    public Object promotionRecords(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("promotionRecords  , requestInfo:{}",requestInfo);

        Map<String,Object> params = new HashMap<>();
        params.put("userId",getUserId(request));
//        params.put("userId",requestInfo.getJSONObject("data").getLong("userId"));
        int[]  transTypeList = {StaticDataEnum.ACC_FLOW_TRANS_TYPE_25.getCode(),StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode(),StaticDataEnum.ACC_FLOW_TRANS_TYPE_20.getCode()};
        params.put("transTypeList",transTypeList);
        params.put("state",StaticDataEnum.TRANS_STATE_1.getCode());
        int total = marketingFlowService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;

        pc = getPagingContext(requestInfo.getJSONObject("data"), total);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("scs","created_date(desc)");
        scs = getSortingContext(jsonObject);
        List<MarketingFlowDTO> list = new ArrayList<>();
        if(total>0){
            list = marketingFlowService.find(params,scs,pc);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM.dd HH:mm", Locale.US);
            for(MarketingFlowDTO oneData:list){
                oneData.setTransTime(simpleDateFormat.format(oneData.getCreatedDate()));
            }
        }

        return R.success(list,pc);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "app卡、账户信息更新操作", notes = "app卡、账户信息更新操作")
    @PostMapping("/changeCardMessage")
    public Object changeCardMessage(@RequestBody JSONObject object, HttpServletRequest request) {
        log.info("changeCardMessage data:{}", object);
        JSONObject data = object.getJSONObject("data");
        try {
            Long userId = getUserId(request);
            data.put("userId",userId);
            userService.changeCardMessageParamsCheck(data, request);
            userService.changeCardMessage(data, request);
        } catch (Exception e) {
            log.error("changeCardMessage failed: {}, error message:{}", object, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }

        return R.success();
    }


    @SignVerify
    @AppToken
    @ApiOperation(value = "绑定分期付还款账户", notes = "绑定分期付还款账户")
    @PostMapping("/creditTieOnCard")
    public Object creditTieOnCard(@RequestBody JSONObject object, HttpServletRequest request) {
        JSONObject data = object.getJSONObject("data");
        log.info("app credit Tie On Card data:{}", data);
        try {
            Long userId = getUserId(request);
            data.put("userId",userId);
            userService.creditTieOnCard(data,request);

        } catch (Exception e) {
            log.error("app credit Tie On Card failed, data:{}, e:{}, error message:{}", data, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PassToken
    @ApiOperation(value = "app检查->查询是否需要强制更新", notes = "app查询是否需要强制更新")
    @PostMapping("/appVersionVerify")
    public Object appVersionVerify(@NonNull @RequestBody JSONObject object, HttpServletRequest request) {
        JSONObject data = object.getJSONObject("data");
        if (data.isEmpty()){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.error",getLang(request)));
        }
        log.info("app查询是否需要强制更新 versionId:{}", data);
        try {
            JSONObject result = appVersionService.appVersionVerify(data, request);
            log.info("查询APP强制更新,结果信息:{}",result);
            return R.success(result);
        } catch (Exception e) {
            log.error("app查询是否需要强制更新失败, versionId:{}, e:{}, error message:{}", data, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @PassToken
    @ApiOperation(value = "app检查->查询是否需要强制更新", notes = "app查询是否需要强制更新")
    @PostMapping("/appVersionVerifyV2")
    public Object appVersionVerifyV2(@NonNull @RequestBody JSONObject object, HttpServletRequest request) {
        log.info("app查询是否需要强制更新V2 请求信息:{}", object);
        JSONObject data = object.getJSONObject("data");
        if (data.isEmpty()){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.error",getLang(request)));
        }
        try {
            JSONObject result = appVersionService.appVersionVerifyNewV2(data, request);
            log.info("查询APP强制更新V2,结果信息:{}",result);
            return R.success(result);
        } catch (Exception e) {
            log.error("app查询是否需要强制更新失败V2, versionId:{}, e:{}, error message:{}", data, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @PassToken
    @ApiOperation(value = "app检查->查询是否需要强制更新", notes = "app查询是否需要强制更新")
    @PostMapping("/appVersionVerifyV3")
    public Object appVersionVerifyV3(@NonNull @RequestBody JSONObject object, HttpServletRequest request) {
        log.info("app查询是否需要强制更新V3 请求信息:{}", object);
        JSONObject data = object.getJSONObject("data");
        if (data.isEmpty()){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.error",getLang(request)));
        }
        try {
            JSONObject result = appVersionService.appVersionVerifyNewV3(data, request);
            log.info("查询APP强制更新V3,结果信息:{}",result);
            return R.success(result);
        } catch (Exception e) {
            log.error("app查询是否需要强制更新失败V3, versionId:{}, e:{}, error message:{}", data, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @SignVerify
    @AppToken
    @ApiOperation(value = "查询交易费率", notes = "查询交易费率")
    @PostMapping("/getChannelFee")
    public Object getChannelFee(@NonNull @RequestBody JSONObject object, HttpServletRequest request) {
        JSONObject data = object.getJSONObject("data");
        log.info("getChannelFee request:{}", data);
        try {
            return R.success(gatewayService.getChannelFee(data,request));
        } catch (Exception e) {
            log.error("getChannelFee, request:{},  error message:{}", data, e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @SignVerify
    @AppToken
    @PostMapping("/getApiToken")
    @ApiOperation(value = "获取apiToken", notes = "获取apiToken")
    public Object getApiToken(HttpServletRequest request) {
        log.info("获取apiToken request:{}", request);
        try {
            String apiToken = EncryptUtil.encrypt(SnowflakeUtil.generateId().toString());
            //设置过期时间为5分钟( 60秒 * 5 )
            redisUtils.set(apiToken,apiToken,60*5);
            return R.success(apiToken);
        } catch (Exception e) {
            log.error("获取apiToken info:{}, error message:{}, error all:{}", request, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "运营管理", notes = "运营管理")
    @PostMapping("/getOperationData")
    public Object getOperationData(@NonNull @RequestBody JSONObject object, HttpServletRequest request) {
        JSONObject data = object.getJSONObject("data");
        log.info("getOperationData request:{}", data);
        try {
            Map<String,Object> result = qrPayFlowService.getOperationData(data,request);
            return R.success(result);
        } catch (Exception e) {
            log.error("getOperationData, request:{},  error message:{}", data, e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "获取tag,top10,全量表信息", notes = "获取tag信息")
    @PostMapping("/getTagInfo")
    public Object getTagInfo(HttpServletRequest request) {
        log.info("APP- get Tag Info request:{}",request );
        try {
            return R.success(tagService.getTagInfo(request));
        } catch (Exception e) {
            log.error("APP- get Tag Info, request:{},  error message:{}", request, e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "商户列表页搜索,返回商户列表", notes = "商户列表页搜索")
    @PostMapping("/merchantSearchList")
    public Object merchantSearchList(@NonNull @RequestBody JSONObject object, HttpServletRequest request) {
        JSONObject data = object.getJSONObject("data");
        log.info("APP- merchant Search List request:{}", data);
        try {
            JSONObject result = merchantListService.merchantSearchList(data, request);
            return R.success(result.get("list"),result.getJSONObject("pc").toJavaObject(PagingContext.class));
        } catch (Exception e) {
            log.error("APP- merchant Search List, request:{},  error message:{}", data, e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @SignVerify
    @PassToken
    @ApiOperation(value = "商户首页推荐列表", notes = "商户首页推荐列表")
    @PostMapping("/getTopTenList")
    public Object getTopTenList(@NonNull @RequestBody JSONObject object, HttpServletRequest request) {
        JSONObject data = object.getJSONObject("data");
        log.info("APP- getTopTenList request:{}", data);
        try {
            List<MerchantDTO> topTenList = merchantListService.getTopTenList(data, request);
            topTenList = merchantListService.processMerchantList(topTenList,false);
            return R.success(topTenList);
        } catch (Exception e) {
            log.error("APP- getTopTenList, request:{},  error message:{}", data, e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @SignVerify
    @PassToken
    @ApiOperation(value = "商户首页推荐列表-新增商户", notes = "商户首页推荐列表-新增商户")
    @PostMapping("/getNewVenus")
    public Object getNewVenus(@NonNull @RequestBody JSONObject object, HttpServletRequest request) {
        JSONObject data = object.getJSONObject("data");
        log.info("APP- get New Venus merchant list request:{}", data);
        try {
            List<MerchantDTO> newVenusList = merchantListService.getNewVenus(data, request);
            newVenusList = merchantListService.processMerchantList(newVenusList,false);
            return R.success(newVenusList);
        } catch (Exception e) {
            log.error("APP- get New Venus merchant list, request:{},  error message:{}", data, e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "邀请用户列表", notes = "邀请用户列表")
    @PostMapping("/invitedUserList")
    public Object invitedUserList(@NonNull @RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("APP- invited user list, data:{}", requestInfo);
        JSONObject data = requestInfo.getJSONObject("data");
        Integer hasPurchased = data.getInteger("hasPurchased");
        int total;
        PagingContext pc;
        Vector<SortingContext> scs;
        List resultList = null;
        Map<String, Object> params = data;
        // 0 邀请人列表
        if (hasPurchased.equals(StaticDataEnum.STATUS_0.getCode())) {
            total = userService.walletFriendsInvitedCount(params);
            pc = getPagingContext(data, total);
            if (total > 0) {
                scs = getSortingContext(data);
                resultList = userService.walletFriendsInvitedList(params, scs, pc);
            }
        } else {
//            params.put("firstDealState",StaticDataEnum.STATUS_1.getCode());
            total = userService.walletFriendsPurchaseCount(params);
            pc = getPagingContext(data, total);
            if (total > 0) {
                scs = getSortingContext(data);
                resultList = userService.walletFriendsPurchaseList(params, scs, pc);
            }
        }

        return R.success(resultList, pc);
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "ip定位", notes = "ip定位")
    @PostMapping(value = "/findLocationByIp", name="详情")
    public Object findLocationByIp(@RequestBody JSONObject requestInfo,HttpServletRequest request) {
        log.info("APP- find location by ip, data:{}", requestInfo);
        try {
            return R.success(merchantListService.findLocationByIp(requestInfo.getJSONObject("data").getString("ip"),request));
        }catch (Exception e) {
            log.error("APP- find location by ip, request:{},  error message:{}", requestInfo, e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @PassToken
    @ApiOperation(value = "通过经纬度,获取城市街道", notes = "通过经纬度,获取城市街道")
    @PostMapping(value = "/findCityStInfo", name="详情")
    public Object findCityStInfo(@RequestBody JSONObject requestInfo,HttpServletRequest request) {
        log.info("APP- find location by location info, data:{}", requestInfo);
        JSONObject data = requestInfo.getJSONObject("data");
        try {
            return R.success(merchantListService.findCityStInfo(data,request));
        } catch (Exception e) {
            log.error("APP- find location by location info, request:{},  error message:{}", data, e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @SignVerify
    @PassToken
    @ApiOperation(value = "获取商家列表,地图显示", notes = "获取商家列表,地图显示")
    @PostMapping(value = "/getMerchantLocationList", name="详情")
    public Object getMerchantLocationList(@RequestBody JSONObject requestInfo,HttpServletRequest request) {
        log.info("APP- get Merchant Location List, data:{}", requestInfo);
        JSONObject data = requestInfo.getJSONObject("data");
        try {
            return R.success(merchantListService.getMerchantLocationList(data,request));
        } catch (Exception e) {
            log.error("APP- get Merchant Location List, request:{},  error message:{}", data, e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @SignVerify
    @AppToken
    @ApiOperation(value = "获取商家详情", notes = "获取商家详情")
    @PostMapping(value = "/getMerchantDetail/{id}", name="获取商家详情")
    public Object getMerchantDetail(@PathVariable("id") Long merchantId,HttpServletRequest request) {
        log.info("APP- get Merchant detail, merchantId:{}", merchantId);
        try {
            return R.success(merchantListService.getMerchantDetail(merchantId,request));
        } catch (Exception e) {
            log.error("APP- get Merchant detail, merchantId:{},  error message:{}", merchantId, e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @PostMapping(value = "/transactionDetails/{id}", name = "用户交易明细、交易详情（app）")
    @ApiOperation(value = "用户交易明细、交易详情（app）", notes = "用户交易明细、交易详情（app）")
    public Object transactionDetails(@PathVariable("id") Long id, @RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("get transactionDetails id:{},requestInfo:{}", id, requestInfo);
        Map<String, Object> list = new HashMap<>(2);
        Map<String, Object> params = requestInfo.getJSONObject("data");
        params.put("userId", id);
        int total = qrPayFlowService.countDetails(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        pc = getPagingContext(requestInfo.getJSONObject("data"), total);
        if (total > 0) {
            scs = getSortingContext(requestInfo.getJSONObject("data"));
            list = qrPayFlowService.transactionDetails(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @SignVerify
    @AppToken
    @PassToken
    @ApiOperation(value = "获取交易明细-卡消费+分期付", notes = "获取交易明细-卡消费+分期付")
    @PostMapping("/getTransactionRecordNew")
    public Object getTransactionRecordNew(@NonNull @RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("APP- 获取交易明细-卡消费+分期付, data:{}", requestInfo);
        JSONObject data = requestInfo.getJSONObject("data");
        if (StringUtils.isBlank(data.getString("userId")) || !serverType.equals(TEST_SERVER_TYPE)) {
            data.put("userId", getUserId(request));
        }
        int total;
        PagingContext pc;
        Vector<SortingContext> scs;
        Object resultList = null;
        Map<String, Object> params = data;
        total = transRecordService.countTransactionRecord(data);
        pc = getPagingContext(data, total);
        if (total > 0) {
            scs = getSortingContext(data);
            try {
                resultList = transRecordService.transactionDetails(params, scs, pc,request);
            }catch (Exception e){
                log.info("APP- 获取交易明细-卡消费+分期付失败, data:{},error:{}", requestInfo,e.getMessage());
               return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
            }
        }
        return R.success(resultList, pc);
    }
    @SignVerify
    @AppToken
    @ApiOperation(value = "交易明细数据列表变为不可见", notes = "交易明细数据列表变为不可见")
    @PostMapping(value = "/updateRecordIsShow/{id}", name="交易明细数据列表变为不可见")
    public Object updateRecordIsShow(@PathVariable("id") Long id,HttpServletRequest request) {
        log.info("APP- 交易明细数据列表变为不可见, qrPayId:{}", id);
        try {
            return R.success(transRecordService.updateRecordIsShow(id,request));
        } catch (Exception e) {
            log.error("APP- 交易明细数据列表变为不可见, qrPayId:{},  error message:{}", id, e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @SignVerify
    @AppToken
//    @PassToken
    @ApiOperation(value = "卡支付交易详情", notes = "卡支付交易详情")
    @PostMapping(value = "/getRecordDetail/{id}", name="卡支付交易详情")
    public Object getRecordDetail(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("APP- 卡支付交易详情, qrPayId:{}", id);
        try {
            return R.success(transRecordService.getRecordDetail(id,request));
        } catch (Exception e) {
            log.error("APP- 卡支付交易详情失败, qrPayId:{},  error message:{}", id, e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
//    @PassToken
    @PostMapping("/getQrPayTransAmount")
    @ApiOperation(value = "获取扫码支付的交易金额", notes = "获取扫码支付的交易金额")
    public Object getQrPayTransAmount(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("app/getQrPayTransAmount  requestInfo:{}", requestInfo);

        JSONObject result ;
        try {
            result = qrPayService.getQrPayTransAmount(requestInfo.getJSONObject("data"), request);
        } catch (Exception e) {
            log.error("app/getQrPayTransAmount, requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(result);
    }


    @SignVerify
    @AppToken
//    @PassToken
    @PostMapping("/getPayTransAmountDetail")
    @ApiOperation(value = "支付时获取交易金额信息", notes = "支付时获取交易金额信息")
    public Object getPayTransAmountDetail(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("支付时获取交易金额信息新接口  requestInfo:{}", requestInfo);
        JSONObject result;
        try {
            result = qrPayService.getPayTransAmountDetail(requestInfo.getJSONObject("data"), request);
        } catch (Exception e) {
            log.error("支付时获取交易金额信息新接口 , requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(result);
    }






    @SignVerify
    @AppToken
    @ApiOperation(value = "卡解绑操作", notes = "卡解绑操作")
    @PostMapping("/cardUnbundling")
    public Object cardUnbundling(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("card unbundling, data:{}", requestInfo);
        JSONObject result = null;
        try {
            JSONObject data = requestInfo.getJSONObject("data");
            data.put("userId",getUserId(request));
            result = userService.cardUnbundling(data, request);
        } catch (Exception e) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }
    @SignVerify
    @AppToken
    @ApiOperation(value = "更新latpay卡信息", notes = "更新latpay卡信息")
    @PostMapping("/updateLatpayCardInfo")
    public Object updateLatpayCardInfo(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
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


    @SignVerify
    @AppToken
    @ApiOperation(value = "获取银行卡信息", notes = "获取银行卡信息")
    @PostMapping("/getCardDetails")
    public Object getCardDetails(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("获取银行卡信息, data:{}", requestInfo);
        JSONObject result = null;
        try {
            JSONObject data = requestInfo.getJSONObject("data");
            String token = (String)data.get("token");
            result = userService.getCardDetails(token, request);
        } catch (Exception e) {
            log.error("获取银行卡信息失败,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "获取店铺NFC,QR绑定详情信息", notes = "获取店铺NFC,QR绑定详情信息")
    @PostMapping("/getMerchantDetails")
    public Object getMerchantDetailsById(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("获取店铺NFC与QR绑定详情信息, data:{}", requestInfo);
        Map<String,Object> result = null;
        try {
            JSONObject data = requestInfo.getJSONObject("data");
            String merchantId = data.getString("id");
            result = merchantService.getMerchantDetailsById(merchantId, request);
        } catch (Exception e) {
            log.error("获取店铺NFC与QR绑定详情信息,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "根据名称查询店铺", notes = "根据名称查询店铺")
    @PostMapping("/getMerchantList")
    public Object getMerchantList(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("根据名称查询店铺, data:{}", requestInfo);
        JSONObject result = null;
        PagingContext pc=null;
        try{
            JSONObject data = requestInfo.getJSONObject("data");
            String name = data.getString("name");
            Long endTime = data.getLong("endTime");
            JSONObject param=new JSONObject(2);
            param.put("name",name);
//            param.put("isAvailable",1);
            param.put("isAvailable",StaticDataEnum.AVAILABLE_1.getCode());
            param.put("endTime",endTime);
            int count = merchantService.count(param);
             pc = getPagingContext(data,count);
            result = merchantService.getMerchantList(endTime,name,request,pc);
        }catch (Exception e){
            log.error("根据名称查询店铺,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result,pc);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "APP绑定Nfc码", notes = "App绑定Nfc码")
    @PostMapping("/bingNfcCode")
    public Object bingNfcCode(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("APP绑定Nfc码, data:{}", requestInfo);
        Boolean result = null;
        try{
            JSONObject data = requestInfo.getJSONObject("data");
            String nfcCode = data.getString("nfcCode");
            String merchantId = data.getString("merchantId");
            result = merchantService.bingNfcCode(merchantId, nfcCode, request);
        }catch (Exception e){
            log.error("APP绑定Nfc码,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }
    @SignVerify
    @AppToken
    @ApiOperation(value = "获取nfc绑定信息", notes = "获取nfc绑定信息")
    @PostMapping("/getBingNfcInfo")
    public Object getBingNfcInfo(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("获取nfc绑定信息, data:{}", requestInfo);
        Map<String,Object> result=null ;
        try{
            JSONObject data = requestInfo.getJSONObject("data");
            String nfcCode = data.getString("nfcCode");
            result = nfcCodeInfoService.findMerchantByNfcCode(nfcCode, request);
        }catch (Exception e){
            log.error("获取nfc绑定信息,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }
    @SignVerify
    @AppToken
    @ApiOperation(value = "解绑NFC", notes = "解绑NFC")
    @PostMapping("/unBingNfc")
    public Object unBingNfc(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("解绑NFC, data:{}", requestInfo);
        Map<String,Object> result= new HashMap<>();
        try{
            JSONObject data = requestInfo.getJSONObject("data");
            String nfcCode = data.getString("nfcCode");
            Boolean results = nfcCodeInfoService.unBindNfcCode(nfcCode, request);
            result.put("result",results);
        }catch (Exception e){
            log.error("解绑NFC,error message:{},e:{}",e.getMessage(),e);
            result.put("result",false);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage(),result);
        }
        return R.success(result);
    }



    @SignVerify
    @PassToken
    @ApiOperation(value = "商户管理员登陆", notes = "商户管理员登陆")
    @PostMapping("/adminLogin")
    public Object adminLogin(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("app admin login info:{}", jsonObject);
        JSONObject msg ;
        try {
            msg = adminService.appAdminLogin(jsonObject.getJSONObject("data"), request);
        } catch (Exception e) {
            log.error("app admin login failed, userDTO: {}, error message:{}, error all:{}", jsonObject, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.LOGIN__ERROR.getCode(), e.getMessage());
        }

        return R.success(msg);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "获取QR绑定信息", notes = "获取QR绑定信息")
    @PostMapping("/getBingQrInfo")
    public Object getBingQRInfo(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("获取QR绑定信息, data:{}", requestInfo);
        Map<String,Object> result=null ;
        try{
            JSONObject data = requestInfo.getJSONObject("data");
            String qrCode = data.getString("qrCode");
            result = qrcodeInfoService.findMerchantByqrCode(qrCode, request);
        }catch (Exception e){
            log.error("获取QR绑定信息,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "绑定QR码", notes = "绑定QR码")
    @PostMapping("/bingQrCode")
    public Object bingQrCode(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("绑定QR码, data:{}", requestInfo);
        JSONObject result=new JSONObject() ;
        try{
            JSONObject data = requestInfo.getJSONObject("data");
            String qrCode = data.getString("qrCode");
            Long merchantId = data.getLong("merchantId");
            result = qrcodeInfoService.bindQrCode(qrCode,merchantId, request);
        }catch (Exception e){
            result.put("result",false);
            log.error("绑定QR码,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage(),result);
        }
        return R.success(result);
    }
    @SignVerify
    @AppToken
    @ApiOperation(value = "解除QR码绑定", notes = "解除QR码绑定")
    @PostMapping("/unbingQrCode")
    public Object unbingQrCode(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("解除QR码绑定, data:{}", requestInfo);
        JSONObject result=new JSONObject() ;
        try{
            JSONObject data = requestInfo.getJSONObject("data");
            String qrCode = data.getString("qrCode");
            result = qrcodeInfoService.unbindQrCode(qrCode, request);
        }catch (Exception e){
            result.put("result",false);
            log.error("解除QR码绑定,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage(),result);
        }
        return R.success(result);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "查询商户QR码信息", notes = "查询商户QR码信息")
    @PostMapping("/getMerchantQrCode")
    public Object getMerchantQrCodeById(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("查询商户QR码信息, data:{}", requestInfo);
        JSONObject result=new JSONObject() ;
        try{
            JSONObject data = requestInfo.getJSONObject("data");
            Long merchantId = data.getLong("merchantId");
            result = merchantService.getMenchatQrCodeById(merchantId, request);
        }catch (Exception e){
            result.put("result",false);
            log.error("查询商户QR码信息,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @AppToken
    @PostMapping("/getAllNoticeHasRead/{userId}")
    @ApiOperation(value = "是否全部已读(新)", notes = "是否全部已读")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object getAllNoticeHasRead(@PathVariable("userId") Long userId, HttpServletRequest request) {
        log.info("all Notice Has Read, userId:{}", userId);
        JSONObject  result;
        try {
            if (userId.longValue() != getUserId(request)) {
                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
            }
            result = noticeService.getAllNoticeHasRead(userId);
        } catch (Exception e) {
            log.error("all Notice Read failed, id: {}, error message:{}, error all:{}", userId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "latpay获取卡类型详情", notes = "latpay获取卡类型详情")
    @PostMapping("/latpayGetCardType")
    public Object latpayGetCardType(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("latpay获取卡类型详情, data:{}", requestInfo);
        try{
            return R.success(userService.latpayGetCardType(requestInfo.getJSONObject("data"), request));
        }catch (Exception e){
            log.error("latpay获取卡类型详情,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "获取机构列表", notes = "获取机构列表")
    @PostMapping("/getInstitutions")
    public Object getInstitutions(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
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

    @SignVerify
    @AppToken
    @ApiOperation(value = "预加载机构信息", notes = "预加载机构信息")
    @PostMapping("/preload")
    public Object preload(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("预加载机构信息, data:{}", requestInfo);
        try{
            return R.success(illionInstitutionsService.preload(requestInfo.getJSONObject("data"), request));
        }catch (Exception e){
            log.error("预加载机构信息,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "登录并获取报告", notes = "登录并获取报告")
    @PostMapping("/fetchAll")
    public Object fetchAll(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("登录并获取报告,userId:{}",getUserId(request));
        try{
            return R.success(illionInstitutionsService.fetchAll(requestInfo.getJSONObject("data"), request));
        }catch (Exception e){
            log.error("登录并获取报告,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @SignVerify
    @AppToken
    @ApiOperation(value = "将卡设置为默认卡", notes = "将卡设置为默认卡")
    @PostMapping("/presetCard")
    public Object defaultCard(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("将卡设置为默认卡, data:{}", requestInfo);
        try{
            userService.presetCard(requestInfo.getJSONObject("data"),getUserId(request), request);
            return R.success();
        }catch (Exception e){
            log.error("将卡设置为默认卡,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @SignVerify
    @AppToken
    @ApiOperation(value = "风控", notes = "风控")
    @PostMapping("/riskCheckNew")
    public Object riskCheckNew(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("risk data, data:{}", requestInfo);
        JSONObject data = requestInfo.getJSONObject("data");
        JSONObject result = null;
        try {
            String birth = data.getString("birth");
            String processBirth = this.processBirth(birth);
            data.put("birth", processBirth);
            log.info("==============/riskCheck 修改后的的生日为: {}",processBirth);
            userService.riskCheckParamsCheck(data, request);
            result = userService.riskCheckNew(requestInfo.getJSONObject("data"), request).get();
        } catch (Exception e) {
            log.error("risk check faile, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success(result);
    }
    @SignVerify
    @AppToken
    @ApiOperation(value = "在UBiz商户登出后清空pushToken", notes = "在UBiz商户登出后清空pushToken")
    @PostMapping("/clearPushToken/{userId}")
    public Object clearPushToken(@PathVariable("userId") Long userId, HttpServletRequest request) {
        log.info("在UBiz商户登出后清空pushToken, data:{}", userId);
        try {

            userService.clearPushToken(userId,request);
        } catch (Exception e) {
            log.error("在UBiz商户登出后清空pushToken, userId:{}, e:{}, error message:{}", userId, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "二次验证获取illion报告", notes = "二次验证获取illion报告")
    @PostMapping("/mfaSubmit")
    public Object mfaRequest(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("二次验证获取illion报告,userId:{}",getUserId(request));
        try {
            return R.success(illionInstitutionsService.mfaInfoSubmit(jsonObject.getJSONObject("data"),request));
        } catch (Exception e) {
            log.error("二次验证获取illion报告异常,e:{}, error message:{}", e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "扫描POS二维码获取订单金额", notes = "扫描POS二维码获取订单金额")
    @PostMapping("/showPosTransAmount")
    public Object showPosTransAmount(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("扫描POS二维码获取订单金额, data:{}", requestInfo);
        try{
            return R.success(posQrPayFlowService.showPosTransAmount(requestInfo, request));
        }catch (Exception e){
            log.error("扫描POS二维码获取订单金额,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "illion异常发送短信接口", notes = "illion异常发送短信接口")
    @PostMapping("/sendIllionMessage")
    public void sendIllionMessage(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("illion异常发送短信接口, data:{}", requestInfo);
        try{
            JSONObject data = requestInfo.getJSONObject("data");
            illionInstitutionsService.sendIllionMessage(data, request);
        }catch (Exception e){
            log.error("illion异常发送短信接口,error message:{},e:{}",e.getMessage(),e);
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "判断是否在商家附近支付", notes = "判断是否在商家附近支付")
    @PostMapping("/checkPayDistance")
    public Object checkPayDistance(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("判断是否在商家附近支付, data:{}", requestInfo);
        try{
            boolean b = merchantService.checkPayDistance(requestInfo.getJSONObject("data"), request);
            return R.success(b);
        }catch (Exception e){
            log.error("判断是否在商家附近支付,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "判断是否在商家附近支付V2", notes = "判断是否在商家附近支付V2")
    @PostMapping("/checkPayDistanceV2")
    public Object checkPayDistanceV2(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("判断是否在商家附近支付V2, data:{}", requestInfo);
        try{
            JSONObject result = merchantService.checkPayDistanceV2(requestInfo.getJSONObject("data"), request);
            return R.success(result);
        }catch (Exception e){
            log.error("判断是否在商家附近支付V2,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }



    @SignVerify
    @AppToken
    @ApiOperation(value = "获取用户已省金额", notes = "获取用户已省金额")
    @PostMapping("/getUserSavedAmount")
    public Object getUserSavedAmount(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("获取用户已省金额, data:{}", this.getUserId(request));
        try{
            Long userId = this.getUserId(request);
//            Long userId = requestInfo.getJSONObject("data").getLong("userId");
            JSONObject result = qrPayFlowService.getUserSavedAmount(userId, request);
            return R.success(result);
        }catch (Exception e){
            log.error("获取用户已省金额,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "查询营销码信息", notes = "查询营销码信息")
    @PostMapping("/getPromotionCodeMessage")
    public Object getPromotionCodeMessage(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("查询营销码信息, data:{}",requestInfo);
        try{
            Long userId = this.getUserId(request);
//            Long userId = requestInfo.getJSONObject("data").getLong("userId");
            JSONObject result = marketingManagementService.getCodeMessage(userId,requestInfo.getJSONObject("data"), request);
            return R.success(result);
        }catch (Exception e){
            log.error("查询营销码信息,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @SignVerify
    @AppToken
    @ApiOperation(value = "查询营销码信息", notes = "查询营销码信息")
    @PostMapping("/getStripeState")
    public Object getStripeState(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("查询用户stripe弹窗标识, data:{}",requestInfo);
        try{
            Integer result = userService.getStripeState(request);
            return R.success(result);
        }catch (Exception e){
            log.error("查询用户stripe弹窗标识,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
}
