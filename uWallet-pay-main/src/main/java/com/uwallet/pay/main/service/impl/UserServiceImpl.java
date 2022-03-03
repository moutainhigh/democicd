package com.uwallet.pay.main.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.stripe.model.Card;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.ResultCode;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.StripeAPIResponse;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.*;
import com.uwallet.pay.main.constant.CardEnum;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.KycSubmitLogDAO;
import com.uwallet.pay.main.dao.UserDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.*;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 用户
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 用户
 * @author: baixinyue
 * @date: Created in 2019-12-10 17:57:14
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class UserServiceImpl extends BaseServiceImpl implements UserService {

    @Resource
    private UserDAO userDAO;

    @Value("${uWallet.data}")
    private String dataUrl;

    @Value("${redisTime.sendMessageCode}")
    private Long sendMessageCode;

    @Value("${redisTime.sendMessageTwo}")
    private Long sendMessageTwo;

    @Autowired
    @Lazy
    private QrcodeInfoService qrcodeInfoService;

    @Autowired
    @Lazy
    private MerchantService merchantService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    @Lazy
    private ServerService serverService;

    @Autowired
    private LoginMissService loginMissService;

    @Autowired
    private MailTemplateService mailTemplateService;

    @Autowired
    private TieOnCardFlowService tieOnCardFlowService;

    @Autowired
    private MailLogService mailLogService;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private KycSubmitLogService kycSubmitLogService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    @Lazy
    private MessageBatchSendLogService messageBatchSendLogService;
    @Autowired
    @Lazy
    private UserLocationRecordService userLocationRecordService;

    @Autowired
    private IllionSubmitStatusLogService  illionSubmitStatusLogService;

    @Lazy
    @Autowired
    private UserActionButtonService userActionButtonService;

    @Lazy
    @Autowired
    private UserInfoUpdateLogService userInfoUpdateLogService;

    @Resource
    @Lazy
    private CardService cardService;

    @Resource
    @Lazy
    private StripeAPIService stripeAPIService;

    @Value("${uWallet.account}")
    private String accountUrl;

    @Value("${uWallet.sysEmail}")
    private String sysEmail;

    @Value("${uWallet.sysEmailPwd}")
    private String sysEmailPwd;

    @Value("${latpay.tieOnCardUrl}")
    private String tieOnCardUrl;
    @Value("${latpay.cardBinLookupUrl}")
    private String cardBinLookupUrl;

    @Value("${google.mapGeocodingAPI}")
    private String googleMapsAPI;

    @Value("${google.mapGeocodingAPIKey}")
    private String googleMapsAPIKey;

    @Value("${uWallet.risk}")
    private String riskUrl;

    @Value("${uWallet.invest}")
    private String investUrl;

    @Value("${uWallet.credit}")
    private String creditUrl;

    @Value("${walletConsumption}")
    private String walletConsumption;
    /*
    @Value("${walletRegister}")
    private String walletRegister;

    @Value("${walletConsumption}")
    private String walletConsumption;*/

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

    @Value("${spring.investAgreement}")
    private String investAgreement;

    @Value("${spring.creditAgreement}")
    private String creditAgreement;

    @Value("${spring.imgRequestHost}")
    private String imgRequestHost;
/*
    @Value("${walletRegister}")
    private String walletRegister;

    @Value("${walletConsumption}")
    private String walletConsumption;*/

    @Value("${split.contractVersion}")
    private String splitContractVersion;

    /**
     * 默认密码
     */
    private static final String DEFAULT_PASSWORD = "123456";
    /**
     * 默认邀请返回字符串
     * */
    private static final String RECEVIED_STR_1 = "Your Friend";
    private static final String RECEVIED_STR_2 = " has made their first purchase with Payo. As a reward you and your friend will get a ";
    private static final String RECEVIED_STR_3 = " credit which will be deducted on your next purchase. Woot!";

    /**
     * kyc初始化每日机会
     */
    private static final int KYC_CHANCE = 3;

    @Autowired
    private AliyunSmsService aliyunSmsService;

    @Autowired
    private MerchantLoginService merchantLoginService;

    @Autowired
    private UserActionService userActionService;

    @Autowired
    private StaticDataService staticDataService;

    @Autowired
    private ActionService actionService;

    @Autowired
    @Lazy
    private RiskApproveLogService riskApproveLogService;

    @Autowired
    private AppAboutUsService appAboutUsService;

    @Autowired
    private IntegraPayService integraPayService;

    @Autowired
    @Lazy
    private UserStepService userStepService;
    @Autowired
    @Lazy
    private UserStepLogService userStepLogService;

    @Autowired
    private ParametersConfigService parametersConfigService;

    @Autowired
    private AccountFlowService accountFlowService;

    @Autowired
    @Lazy
    private MarketingFlowService marketingFlowService;
    @Lazy
    @Autowired
    private MarketingManagementService marketingManagementService;
    @Autowired
    private SplitService splitService;
    @Autowired
    private MarketingLogService marketingLogService;



    @Resource
    private QrPayFlowService qrPayFlowService;

    /**
     * 风控产品、事件
     */
    private static final long RISK_PRODUCT_ID = 399764288373870592L;

    private static final int EVENT_ID = 14;

    private static final int WATCH_LIST_EVENT_ID = 16;

    private static final String INVITE_CONTENT = "·Invite your friends to join us by using your referral code before they make the first transaction on Payo.\n" +
            "\n" +
            "·They will get $20 in their pocket money and you will get $20 as well after they have made the first transaction.";

    @Override
    public void saveUser(@NonNull UserDTO userDTO, HttpServletRequest request) throws Exception {
        String code = "";

        // 查询输入该用户类型下注册号是否已存在，用户查手机号，商户查邮箱
        if (userDTO.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
            code = userDTO.getPhone() + "_" + userDTO.getUserType();
        } else {
            //2021-02-04添加逻辑账号全部小写
            userDTO.setEmail(userDTO.getEmail().toLowerCase());
            code = userDTO.getEmail() + "_" + userDTO.getUserType();
        }
        // 验证验证码
        Object emailRedis = redisUtils.get(code);
        log.info("saveUser redis message : " + emailRedis);
        if (emailRedis == null) {
            throw new BizException(I18nUtils.get("verification.code.expired", getLang(request)));
        }
        String securityCode = emailRedis.toString();
        if (!userDTO.getSecurityCode().equals(securityCode)) {
            throw new BizException(I18nUtils.get("incorrect.verification.code", getLang(request)));
        }

        // 用户注册
        synchronized (userDTO) {
            doUserRegister(userDTO, request);
        }

        //如果是用户发短信，商户发Email(2021/05/07/新需求改为邮件发送 区分qld 用户与非qld 用户)
        sendLoginMessage(userDTO, request);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long doUserRegister(UserDTO userDTO, HttpServletRequest request) throws Exception {
        Map<String, Object> params = new HashMap<>(16);
        // 查询输入该用户类型下注册号是否已存在，用户查手机号，商户查邮箱
        if (userDTO.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
            params.put("phone", userDTO.getPhone());
            params.put("userType", userDTO.getUserType());
            if (findOneUser(params).getId() != null) {
                throw new BizException(I18nUtils.get("user.exist", getLang(request)));
            }
        } else {
            params.put("email", userDTO.getEmail());
            if (merchantLoginService.findOneMerchantLogin(params).getId() != null) {
                throw new BizException(I18nUtils.get("user.exist", getLang(request)));
            }
        }
        // 如果类型是商户，则绑定二维码; 用户则生成二维码
        // 修改：商户注册只添加登录表信息和登录失败表信息，其他信息添加商户时增加
        if (userDTO.getUserType() == StaticDataEnum.USER_TYPE_20.getCode()) {
            //密码加密
            String password = MD5FY.MD5Encode(userDTO.getPassword());
            //添加商户登录表信息
            MerchantLoginDTO merchantLoginDTO = new MerchantLoginDTO();
            merchantLoginDTO.setEmail(userDTO.getEmail());
            merchantLoginDTO.setPassword(password);
            merchantLoginDTO.setUserType(StaticDataEnum.USER_TYPE_20.getCode());
            merchantLoginDTO.setPhone(userDTO.getPhone());
            //创建一条用户失败次数记录
            LoginMissDTO loginMissDTO = new LoginMissDTO();
            loginMissDTO.setUserId(merchantLoginService.saveMerchantLogin(merchantLoginDTO, request));
            loginMissDTO.setChance(StaticDataEnum.LOGIN_MISS_TIME.getCode());
            loginMissService.saveLoginMiss(loginMissDTO, request);
            return null;
        } else {
            // 创建用户
            Boolean isH5 = userDTO.getIsH5();
            String password = null;
            // 20210817 pwp需求 判断是否h5注册用户，如果是不需要密码
            if (isH5==null||!isH5){
                password=MD5FY.MD5Encode(userDTO.getPassword());
            }
            userDTO.setPassword(password);
            userDTO.setPaymentState(StaticDataEnum.USER_BUSINESS_1.getCode());
            userDTO.setInvestState(StaticDataEnum.USER_BUSINESS_1.getCode());
            Long userId = userCreate(userDTO, request);
            // 调用账户系统，开基本账户
            JSONObject accountInfo = new JSONObject();
            accountInfo.put("userId", userId);
            accountInfo.put("phone", userDTO.getPhone());
            accountInfo.put("email", userDTO.getEmail());
            accountInfo.put("firstName", userDTO.getUserFirstName());
            accountInfo.put("lastName", userDTO.getUserLastName());
            accountInfo.put("birth", userDTO.getBirth());
            accountInfo.put("accountType", userDTO.getUserType());
            accountInfo.put("channel", userDTO.getChannel());
            accountInfo.put("postcode", userDTO.getPostcode());
            accountInfo.put("middleName",userDTO.getUserMiddleName());
            accountInfo.put("sex",userDTO.getSex());
            serverService.saveAccount(accountInfo);
            //创建一条用户失败次数记录
            LoginMissDTO loginMissDTO = new LoginMissDTO();
            loginMissDTO.setUserId(userId);
            loginMissDTO.setChance(StaticDataEnum.LOGIN_MISS_TIME.getCode());
            loginMissService.saveLoginMiss(loginMissDTO, request);
            // 创建认证记录
            userStepService.createUserStep(userId, request);
            // 调用理财系统同步数据
            accountInfo.remove("firstName");
            accountInfo.remove("lastName");
            accountInfo.remove("accountType");
            accountInfo.remove("channel");
            accountInfo.put("userFirstName", userDTO.getUserFirstName());
            accountInfo.put("userLastName", userDTO.getUserLastName());
//            String investInfoSupplementUrl = investUrl + "/server/userSynchronization";
//            JSONObject returnMsg = JSONObject.parseObject(HttpClientUtils.sendPost(investInfoSupplementUrl, accountInfo.toJSONString()));
//            if (ErrorCodeEnum.FAIL_CODE.getCode().equals(returnMsg.getString("code"))) {
//                throw new BizException(I18nUtils.get("incorrect.information", getLang(request)));
//            }
            //设置kyc错误次数
            Map<String, Object> kyc = new HashMap<>(2);
            kyc.put("chance", KYC_CHANCE);
            kyc.put("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            redisUtils.hmset(userId + "_kyc", kyc);
            return userId;
        }
    }

    @Override
    public void saveUserV1(UserDTO userDTO, HttpServletRequest request) throws Exception {
        String code = "";
        // 查询输入该用户类型下注册号是否已存在，用户查手机号，商户查邮箱
        if (userDTO.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
            code = userDTO.getPhone() + "_" + userDTO.getUserType();
        } else {
            //2021-02-04添加逻辑账号全部小写
            userDTO.setEmail(userDTO.getEmail().toLowerCase());
            code = userDTO.getEmail() + "_" + userDTO.getUserType();
        }
        // 验证验证码
        Object emailRedis = redisUtils.get(code);
        if (emailRedis == null) {
            throw new BizException(I18nUtils.get("verification.code.expired", getLang(request)));
        }
        String securityCode = emailRedis.toString();
        if (!userDTO.getSecurityCode().equals(securityCode)) {
            throw new BizException(I18nUtils.get("incorrect.verification.code", getLang(request)));
        }
        // 修改用户姓名首字母大写
        String userLastName = userDTO.getUserLastName();
        String userFirstName = userDTO.getUserFirstName();
        log.info("转换用户姓名之前,userLastName:{},userFirstName:{}",userLastName,userFirstName);
        if (!StringUtils.isAllBlank(userLastName,userLastName)){
            String trimLast = userLastName.trim();
            if (trimLast.length()>0){
                String trim = trimLast.trim();
                userLastName = trim.substring(0,1).toUpperCase()+trim.substring(1);
            }
            String trimFirst = userFirstName.trim();
            if (trimFirst.length()>0){
                String trimStr = trimFirst.trim();
                userFirstName = trimStr.substring(0,1).toUpperCase()+trimStr.substring(1);
            }
            userDTO.setUserFirstName(userFirstName);
            userDTO.setUserLastName(userLastName);
            log.info("转换用户姓名之后,userLastName:{},userFirstName:{}",userLastName,userFirstName);
        }
        // 用户注册
        synchronized (userDTO) {
            doUserRegisterV1(userDTO, request);
        }

        //如果是用户发短信，商户发Email
        sendLoginMessage(userDTO, request);
    }

    @Override
    public void doUserRegisterV1(UserDTO userDTO, HttpServletRequest request) throws Exception {
        Map<String, Object> params = new HashMap<>(16);
        // 查询输入该用户类型下注册号是否已存在，用户查手机号，商户查邮箱
        if (userDTO.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
            params.put("phone", userDTO.getPhone());
            params.put("userType", userDTO.getUserType());
            if (findOneUser(params).getId() != null) {
                throw new BizException(I18nUtils.get("user.exist", getLang(request)));
            }
        } else {
            params.put("email", userDTO.getEmail());
            if (merchantLoginService.findOneMerchantLogin(params).getId() != null) {
                throw new BizException(I18nUtils.get("user.exist", getLang(request)));
            }
        }
        // 如果类型是商户，则绑定二维码; 用户则生成二维码
        // 修改：商户注册只添加登录表信息和登录失败表信息，其他信息添加商户时增加
        if (userDTO.getUserType() == StaticDataEnum.USER_TYPE_20.getCode()) {
            //密码加密
            String password = MD5FY.MD5Encode(userDTO.getPassword());
            //添加商户登录表信息
            MerchantLoginDTO merchantLoginDTO = new MerchantLoginDTO();
            merchantLoginDTO.setEmail(userDTO.getEmail());
            merchantLoginDTO.setPassword(password);
            merchantLoginDTO.setUserType(StaticDataEnum.USER_TYPE_20.getCode());
            merchantLoginDTO.setPhone(userDTO.getPhone());
            //创建一条用户失败次数记录
            LoginMissDTO loginMissDTO = new LoginMissDTO();
            loginMissDTO.setUserId(merchantLoginService.saveMerchantLogin(merchantLoginDTO, request));
            loginMissDTO.setChance(StaticDataEnum.LOGIN_MISS_TIME.getCode());
            loginMissService.saveLoginMiss(loginMissDTO, request);
        } else {
            // 创建用户
            String password = MD5FY.MD5Encode(userDTO.getPassword());
            userDTO.setPassword(password);
            userDTO.setPaymentState(StaticDataEnum.USER_BUSINESS_1.getCode());
            userDTO.setInvestState(StaticDataEnum.USER_BUSINESS_1.getCode());
            // 初始化用户邀请码
            Map<String, Object> checkInviteCodeExist = new HashMap<>(16);
            while (true) {
                String inviteCode = InviteUtil.getBindNum(6);
                checkInviteCodeExist.put("inviteCode", inviteCode);
                UserDTO exist = findOneUser(checkInviteCodeExist);
                if (exist.getId() == null) {
                    userDTO.setInviteCode(inviteCode);
                    break;
                }
            }
            Long userId = userCreate(userDTO, request);
            // 调用账户系统，开账户
            JSONObject accountInfo = new JSONObject();
            accountInfo.put("userId", userId);
            accountInfo.put("phone", userDTO.getPhone());
            accountInfo.put("email", userDTO.getEmail());
            accountInfo.put("firstName", userDTO.getUserFirstName());
            accountInfo.put("lastName", userDTO.getUserLastName());
            accountInfo.put("birth", userDTO.getBirth());
            accountInfo.put("accountType", userDTO.getUserType());
            accountInfo.put("channel", userDTO.getChannel());
            accountInfo.put("postcode", userDTO.getPostcode());
            serverService.saveAccount(accountInfo);
            // 创建一条用户失败次数记录
            LoginMissDTO loginMissDTO = new LoginMissDTO();
            loginMissDTO.setUserId(userId);
            loginMissDTO.setChance(StaticDataEnum.LOGIN_MISS_TIME.getCode());
            loginMissService.saveLoginMiss(loginMissDTO, request);
            // 创建认证记录
            userStepService.createUserStep(userId, request);
            // 调用理财系统同步数据
            accountInfo.remove("firstName");
            accountInfo.remove("lastName");
            accountInfo.remove("accountType");
            accountInfo.remove("channel");
            accountInfo.put("userFirstName", userDTO.getUserFirstName());
            accountInfo.put("userLastName", userDTO.getUserLastName());
//            String investInfoSupplementUrl = investUrl + "/server/userSynchronization";
//            JSONObject returnMsg = JSONObject.parseObject(HttpClientUtils.sendPost(investInfoSupplementUrl, accountInfo.toJSONString()));
//            if (ErrorCodeEnum.FAIL_CODE.getCode().equals(returnMsg.getString("code"))) {
//                throw new BizException(I18nUtils.get("incorrect.information", getLang(request)));
//            }
            // 设置kyc错误次数
            Map<String, Object> kyc = new HashMap<>(2);
            kyc.put("chance", KYC_CHANCE);
            kyc.put("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            redisUtils.hmset(userId + "_kyc", kyc);
            // 存储用户邀请码至缓存
            redisUtils.set(userId + "_inviteCode", userDTO.getInviteCode());
            // 注册成功用户初始化红包金额 edit by zhangzeyuan 需求变更 针对红包漏洞修改
            /*ParametersConfigDTO parametersConfigDTO = parametersConfigService.findParametersConfigById(1L);
            if (parametersConfigDTO.getWalletFavorable().intValue() == StaticDataEnum.STATUS_1.getCode() && !StringUtils.isEmpty(userDTO.getEnterInviteCode())) {
                // 红包入账
                try {
                    walletBooked(userId, StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode(), userDTO.getEnterInviteCode().toUpperCase(), request, null);
                } catch (Exception e) {
                    log.info("wallet booked faile, user:{}, error message:{}, e:{}", userDTO, e.getMessage(), e);
                }
//                }
            }*/

            //关联用户邀请人ID
            associatedRegisteredUser(userId, userDTO.getEnterInviteCode(), request);
        }
    }




    /**
     * 关联新注册用户
     * @author zhangzeyuan
     * @date 2021/8/26 10:27
     * @param userId
     * @param code
     * @param request
     */
    private void associatedRegisteredUser(Long userId, String code, HttpServletRequest request) throws Exception {

        if(StringUtils.isBlank(code)){
            return;
        }

        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(2);

        //查询注册人
        params.put("id", userId);
        UserDTO userDTO = userDAO.selectOneDTO(params);
        if(userDTO == null || userDTO.getId() == null){
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }

        //查询推荐人
        params.clear();
        params.put("inviteCode", code.toUpperCase());
        UserDTO inviteUser = userDAO.selectOneDTO(params);
        if(inviteUser == null || inviteUser.getId() == null ){
            throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
        }

        //输入了自己的码
        if(userDTO.getInviteCode().equals(code) ){
            throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
        }

        //查询是否已绑定推荐人
        if(!(userDTO.getInviterId() == null || StringUtils.isEmpty(userDTO.getInviterId()+""))){
            throw new BizException(I18nUtils.get("referrer.exist", getLang(request)));
        }

        //关联邀请人ID
        log.info("userid:"+ userId + "aaaaa:" + inviteUser.getId());
        userDAO.updateInviteId(userId, inviteUser.getId());

    }



    @Override
//    @Async("taskExecutor")
    public void firstPaidSuccessAmountIn(Long userId, Long inviterId, Integer transType, Long flowId, HttpServletRequest request) throws Exception {
        //单次红包金额
        BigDecimal redAmount = new BigDecimal(walletConsumption);

        //被邀请人
        UserDTO payUser = this.findUserById(userId);
        if(payUser == null || payUser.getId() == null){
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }

        //是否是首次支付
        if(payUser.getFirstDealState() == StaticDataEnum.STATUS_1.getCode()){
            return;
        }

        // 更新用户状态为首次成功
        this.updateFirstDealState(payUser.getId());

        //首次交易2小时后发送邮件
        redisUtils.set(Constant.getFirstPaySendMailAfter2HoursRedisKey(userId.toString()), userId, 2 * 60 * 60);

        //是否有推荐人
        if(null == payUser.getInviterId()){
            return;
        }

        //查询推荐人
        UserDTO inviteUser = this.findUserById(inviterId);
        if(inviteUser == null || inviteUser.getId() == null ){
            throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
        }

        //红包入账  被邀请人首次支付成功时 邀请人 及 被邀请人 都获得10元红包

        //邀请人 红包入账
        //更新 邀请人累计消费人数和实得红包
        this.updateConsumption(inviterId);
        this.updateWalletGrandTotal(inviterId, null, redAmount);

        //被邀请人 红包入账
        MarketingFlowDTO marketingFlowDTO = new MarketingFlowDTO();
        marketingFlowDTO.setAmount(redAmount);
        marketingFlowDTO.setFlowId(flowId);
        marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        marketingFlowDTO.setDirection(StaticDataEnum.DIRECTION_0.getCode());
        marketingFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode());
        marketingFlowDTO.setUserId(userId);
        marketingFlowDTO.setId(marketingFlowService.saveMarketingFlow(marketingFlowDTO,request));

        //入账
        doMarketingAmountIn(marketingFlowDTO, request);

        //创建红包流水
        MarketingFlowDTO inviterFlow = new MarketingFlowDTO();
        inviterFlow.setAmount(redAmount);
        inviterFlow.setFlowId(flowId);
        inviterFlow.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        inviterFlow.setDirection(StaticDataEnum.DIRECTION_0.getCode());
        inviterFlow.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_20.getCode());
        inviterFlow.setUserId(inviterId);
        inviterFlow.setId(marketingFlowService.saveMarketingFlow(inviterFlow,request));

        //入账
        doMarketingAmountIn(inviterFlow, request);
    }


    @Override
    public void firstPaidSuccessAmountInV2(Long userId, Long inviterId, Integer transType, Long flowId, HttpServletRequest request) throws Exception {
        //被邀请人
        UserDTO payUser = this.findUserById(userId);
        if(payUser == null || payUser.getId() == null){
            return;
        }

        //是否是首次支付
        if(payUser.getFirstDealState() == StaticDataEnum.STATUS_1.getCode()){
            return;
        }
        // 更新用户状态为首次成功
        this.updateFirstDealState(payUser.getId());

        //首次交易2小时后发送邮件
        redisUtils.set(Constant.getFirstPaySendMailAfter2HoursRedisKey(userId.toString()), userId, 2 * 60 * 60);

        //首次支付推荐奖励
        if(null == inviterId){
            return;
        }

        //查询推荐人
        UserDTO inviteUser = this.findUserById(inviterId);
        if(inviteUser == null || inviteUser.getId() == null ){
            return;
        }

        //更新 邀请人累计消费人数和实得红包
        this.updateConsumption(inviterId);
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(2);

        // 查询当前活动
        params.put("type", 2);
        params.put("activityState", 1);
        params.put("status",StaticDataEnum.STATUS_1.getCode());

        MarketingManagementDTO oneMarketingManagement = marketingManagementService.findOneMarketingManagement(params);
        if(null == oneMarketingManagement ||  null == oneMarketingManagement.getId()){
            return;
        }

        // 查询邀请券获得记录
        params.clear();
        params.put("userId", userId);
        params.put("state", StaticDataEnum.TRANS_STATE_1.getCode());
        params.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode());
        MarketingFlowDTO marketingFlow = marketingFlowService.findOneMarketingFlow(params);

        if(marketingFlow == null  || marketingFlow.getId() == null){
            // 老用户已绑定推荐人，未获得红包的
            // 直接发放一张可用的
            marketingManagementService.addInvitationPromotionCode(userId, inviterId, StaticDataEnum.ADD_INVITATION_CODE_TYPE_1.getCode(), null, oneMarketingManagement, false, request);
            //更新 邀请人累计消费人数和实得红包
//            this.updateWalletGrandTotal(inviterId, null, marketingFlow.getAmount());

        }else{
            //查询该用户的邀请券 如果是未激活 则激活 如果没有 则发放一张可用的邀请券
            JSONObject jsonParams  = new JSONObject();
            jsonParams.put("pageStatus", "2");
            // 查询的时候按照当时发放的id查询
            jsonParams.put("markingId", marketingFlow.getMarketingId());
            jsonParams.put("userId", userId);
            JSONObject couponJsonResult = this.getMarketingCouponAccount(jsonParams, request);
            JSONArray couponJsonArray = couponJsonResult.getJSONArray("data");
            if(CollectionUtils.isEmpty(couponJsonArray)){
                marketingManagementService.addInvitationPromotionCode(userId, inviterId, StaticDataEnum.ADD_INVITATION_CODE_TYPE_1.getCode(), null, oneMarketingManagement, false, request);
            }else {
                //更新该券为可用状态
                List<MarketingAccountDTO> couponList = JSONArray.parseArray(couponJsonArray.toJSONString(), MarketingAccountDTO.class);
                if(couponList.get(0).getState().equals(StaticDataEnum.USER_MARKETING_COUPON_STATE_NOT_ACTIVATED.getCode())){
                    jsonParams.clear();
                    jsonParams.put("markingId", couponList.get(0).getMarkingId());
                    jsonParams.put("userId", userId);
                    this.activateMarketing(jsonParams, request);
                }
            }
        }

        //给邀请人发一张可用的邀请券
        /*boolean notActiveStatus = false;
        if(inviteUser.getFirstDealState() == StaticDataEnum.STATUS_1.getCode()){
            notActiveStatus = false;
        }else{
            notActiveStatus = true;
        }*/
        marketingManagementService.addInvitationPromotionCode(userId, inviterId, StaticDataEnum.ADD_INVITATION_CODE_TYPE_2.getCode(), flowId, oneMarketingManagement,false, request);
    }

    /**
     * 红包获取入账
     *
     * @param amountInUserId
     * @param transType
     * @param request
     * @param flowId
     * @throws Exception
     */
    @Override
    public void walletBooked(Long amountInUserId, Integer transType, String markingCode, HttpServletRequest request, Long flowId) throws Exception {
        log.info("walletBooked request message: amountInUserId" + amountInUserId + ",transType:" + transType + ",markingCode:" + markingCode + ",flowId" + flowId);
        //如果是注册红包和营销红包需要有券码
        if (transType == StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode() || transType == StaticDataEnum.ACC_FLOW_TRANS_TYPE_25.getCode()) {
            if (markingCode == null) {
                throw new BizException(I18nUtils.get("get.invite.code.fail", getLang(request)));
            }
        }

        MarketingFlowDTO marketingFlowDTO = marketingFlowService.createMarketingFlow(amountInUserId, transType, markingCode, flowId, request);
        //入账流程作为单独方法，以复用
        doMarketingAmountIn(marketingFlowDTO, request);
    }

    @Override
    public void enterPromotionCode(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        Long userId = getUserId(request);
//        Long userId = requestInfo.getLong("userId");
        String code = requestInfo.getString("code");
        if (StringUtils.isEmpty(code)) {
            throw new BizException(I18nUtils.get("get.invite.code.fail", getLang(request)));
        }

        code = code.toUpperCase();
        //查询红包券
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("status",StaticDataEnum.STATUS_1.getCode());
        MarketingManagementDTO marketingManagementDTO = marketingManagementService.findOneMarketingManagement(params);
        if (marketingManagementDTO != null && marketingManagementDTO.getId() != null) {
            walletBooked(userId, StaticDataEnum.ACC_FLOW_TRANS_TYPE_25.getCode(), code, request, null);
        } else {
            params.put("inviteCode", code);
            UserDTO inviteUser = this.findOneUser(params);
            if (inviteUser == null || inviteUser.getId() == null) {
                throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
            }
            UserDTO userDTO = this.findUserById(userId);
            if (userDTO.getFirstDealState() == StaticDataEnum.STATUS_1.getCode()) {
                throw new BizException(I18nUtils.get("not.new.user", getLang(request)));
            }

            if (userDTO.getFirstDealState() == StaticDataEnum.STATUS_1.getCode()) {
                throw new BizException(I18nUtils.get("can.not.bind.references", getLang(request)));
            }

//            walletBooked(userId, StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode(), code, request, null);

            if (null != userDTO.getInviterId()) {
                throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
            }

            if (userDTO.getFirstDealState() == StaticDataEnum.STATUS_1.getCode()) {
                throw new BizException(I18nUtils.get("can.not.bind.references", getLang(request)));
            }

            if(userDTO.getId().equals(inviteUser.getId())){
                throw new BizException(I18nUtils.get("promotion.code.not.exist", getLang(request)));
            }

            //之前领过红包 则不绑定
            params.clear();
            params.put("userId", userId);
            int[] stateList = {StaticDataEnum.TRANS_STATE_1.getCode(),StaticDataEnum.TRANS_STATE_0.getCode(),StaticDataEnum.TRANS_STATE_3.getCode()};
            params.put("stateList",stateList);
            MarketingFlowDTO marketingFlowDTO = marketingFlowService.findOneMarketingFlow(params);
            if(marketingFlowDTO != null &&marketingFlowDTO.getId() != null){
                throw new BizException(I18nUtils.get("can.not.bind.references", getLang(request)));
            }


            //注册邀请 有过成功的交易 且没领过

            //查询该用户是否已经入账过该邀请码的红包
            /*HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
            paramMap.put("userId", userId);
//            paramMap.put("code", code);
            paramMap.put("transType", 19);
            paramMap.put("direction", 0);
            paramMap.put("state", 1);
            MarketingFlowDTO oneMarketingFlow = marketingFlowService.findOneMarketingFlow(paramMap);
            if(null != oneMarketingFlow && null != oneMarketingFlow.getId()){
                throw new BizException(I18nUtils.get("not.new.user", getLang(request)));
            }
            Integer paidSuccesCount = qrPayFlowService.countPaidSuccessByUserId(userId);
            if(paidSuccesCount < 1){
                throw new BizException(I18nUtils.get("not.new.user", getLang(request)));
            }*/

            BigDecimal amount = new BigDecimal(walletConsumption);

            //累加推荐人金额
            this.updateRegister(inviteUser.getId());
            this.updateWalletGrandTotal(inviteUser.getId(), amount, null);

            userDAO.updateInviteId(userId, inviteUser.getId());

           /* MarketingFlowDTO marketingFlowDTO = new MarketingFlowDTO();
            marketingFlowDTO.setAmount(amount);
            marketingFlowDTO.setCode(code);
            marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
            marketingFlowDTO.setDirection(StaticDataEnum.DIRECTION_0.getCode());
            marketingFlowDTO.setTransType(19);
            marketingFlowDTO.setUserId(userId);
            marketingFlowDTO.setId(marketingFlowService.saveMarketingFlow(marketingFlowDTO,request));

            marketingFlowDTO.setFlowId(marketingFlowDTO.getId());
            marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(),marketingFlowDTO,request);

            //入账
            doMarketingAmountIn(marketingFlowDTO, request);*/
        }

    }

    @Override
    public void changeCardMessageParamsCheck(JSONObject data, HttpServletRequest request) throws Exception {
        if (StringUtils.isEmpty(data.getString("cardId"))) {
            throw new BizException(I18nUtils.get("card.id.empty", getLang(request)));
        }
        cardInfoNotNullCheck(data, request);
    }

    @Override
    public void changeCardMessage(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        log.info("changeCardMessage requestInfo:" + requestInfo);
        //查询账户绑定 信息
        JSONObject cardInfo = serverService.getCardInfo(requestInfo.getLong("cardId"));
        if (StringUtils.isEmpty(cardInfo.getString("id"))) {
            throw new BizException(I18nUtils.get("card.id.empty", getLang(request)));
        }
        //类型不能换
        if (!requestInfo.getInteger("type").equals(requestInfo.getInteger("type"))) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }

        //类型是账户
        if (cardInfo.getInteger("type").intValue() == StaticDataEnum.TIE_CARD_0.getCode()) {
            //查询账户是否已经有相同的账户
            JSONObject cardCheckReq = new JSONObject();
            cardCheckReq.put("userId", requestInfo.getString("userId"));
            cardCheckReq.put("cardNo", requestInfo.getString("cardNo"));
            cardCheckReq.put("type", StaticDataEnum.TIE_CARD_0.getCode());
            cardCheckReq.put("bsb", requestInfo.getString("bsb"));
            boolean cardExit = false;
            JSONObject exitCardInfo = serverService.getCardByMessage(cardCheckReq);
            if (StringUtils.isNotEmpty(exitCardInfo.getString("id"))) {
                cardExit = true;
            }

            // 查询卡支付方式，走相应绑账户api
            Map<String, Object> params = new HashMap<>(1);
            params.put("gatewayType", StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode());
            params.put("state", StaticDataEnum.STATUS_1.getCode());
            List<GatewayDTO> gatewayDTOList = gatewayService.find(params, null, null);
            if (gatewayDTOList == null || gatewayDTOList.size() == 0) {
                throw new BizException(I18nUtils.get("tie.on.card.not.supposed", getLang(request)));
            }
            JSONObject card = new JSONObject();

            boolean bindCard = false;
            for (GatewayDTO gatewayDTO : gatewayDTOList) {
                if (gatewayDTO.getType().equals(Long.valueOf(StaticDataEnum.GATEWAY_TYPE_6.getCode()))) {
                    // 三方绑卡标志
                    boolean thirdReq = false;
                    // 如果已存在卡信息
                    // todo 之后可以加入三方更新卡信息请求
                    if (cardExit) {
                        // 已存在的卡已经有contactId，取用已绑卡必要信息，绑卡的姓名和email
                        // 无需请求三方
                        if (StringUtils.isNotEmpty(exitCardInfo.getString("splitContactId"))) {
                            card.put("splitContactId", exitCardInfo.getString("contactId"));
                            card.put("splitAgreementId", exitCardInfo.getString("agreementRef"));
                            card.put("splitSignState", StaticDataEnum.STATUS_1.getCode());
                            card.put("splitContractVersion", splitContractVersion);
                            card.put("splitContractTime", System.currentTimeMillis());
                        } else {
                            // 需要请求三方
                            thirdReq = true;
                        }

                        if (StringUtils.isNotEmpty(cardInfo.getString("splitContactId"))) {
                            // 如果原有卡已经绑定split，更新已存在卡的其他信息
                            // 更新卡id为已存在卡id
                            card.put("id", exitCardInfo.getString("id"));
                        } else {
                            // 如果原有卡未绑定split，更新已存在卡的其他信息，原有卡删除
                            card.put("id", cardInfo.getString("id"));
                        }

                    } else {
                        // 无已绑定信息时（原卡信息与绑定信息不一致）
                        // 三方绑卡
                        thirdReq = true;
                        // 如果原卡信息已有splitid
                        if (StringUtils.isNotEmpty(cardInfo.getString("splitContactId"))) {
                            // 账户绑卡
                            bindCard = true;
                        } else {
                            // 如果原卡信息无splitid
                            card.put("id", cardInfo.getString("id"));
                        }

                    }
                    if (thirdReq) {
                        try {
                            JSONObject result = splitTieOnAccount(requestInfo, request);
                            card.put("splitContactId", result.getString("contactId"));
                            card.put("splitAgreementId", result.getString("agreementRef"));
                            card.put("splitSignState", StaticDataEnum.CHANNEL_BIND_STATUS_1.getCode());
                            card.put("splitContractVersion", splitContractVersion);
                            card.put("splitContractTime", System.currentTimeMillis());
                        } catch (Exception e) {
                            log.error("changeCardMessage.split.bind.error ,Exception:" + e, e.getMessage());
//                            card.put("id",requestInfo.getString("cardId"));
                            card.put("splitSignState", StaticDataEnum.CHANNEL_BIND_STATUS_2.getCode());
                            card.put("userId", requestInfo.getString("userId"));
                            card.put("cardNo", requestInfo.getString("cardNo"));
                            card.put("type", StaticDataEnum.TIE_CARD_0.getCode());
                            card.put("bsb", requestInfo.getString("bsb"));
                            if (!bindCard) {
                                serverService.cardMessageModify(card);
                            }
//                            serverService.cardMessageModify(card);
                            throw new BizException(I18nUtils.get("split.tie.on.card.fail", getLang(request)));
                        }
                    }


                } else if (gatewayDTO.getType().equals(Long.valueOf(StaticDataEnum.GATEWAY_TYPE_5.getCode()))) {
                    //TODO integraPay通道暂不可用
//                JSONObject integraPayAccountResult = integraPayTieOnAccount(cardInfo, request);
//                card.put("uniqueReference", integraPayAccountResult.getString("uniqueReference"));
//                card.put("payerId", integraPayAccountResult.getString("payerId"));
//                card.put("integraPayAccountId", integraPayAccountResult);
                } else if (gatewayDTO.getType().equals(Long.valueOf(StaticDataEnum.GATEWAY_TYPE_3.getCode()))) {
                    //LatPay通道无三方绑定
                }
            }
            card.put("userId", requestInfo.getString("userId"));
            card.put("cardNo", requestInfo.getString("cardNo"));
            card.put("type", StaticDataEnum.TIE_CARD_0.getCode());
            card.put("bsb", requestInfo.getString("bsb"));
            card.put("bankName", requestInfo.getString("bankName"));
            card.put("accountName", requestInfo.getString("accountName"));
            card.put("name", requestInfo.getString("name"));
            card.put("email", requestInfo.getString("email"));
            if (bindCard) {
                this.tieOnCard(card, request);
            } else {
                serverService.cardMessageModify(card);
            }

        } else {
            //目前只有账户可以更新信息
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }

    }

    @Override
    public void creditTieOnCard(JSONObject data, HttpServletRequest request) throws Exception {
        //查询用户信息
        UserDTO userDTO = this.findUserById(data.getLong("userId"));
        if (userDTO == null || userDTO.getId() == null) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }
        //尝试从账户系统获取绑卡列表
        JSONArray cardDTOListFromServer = null;
        try {
            JSONObject cardInfo = serverService.getAccountInfo(data.getLong("userId"));
            cardDTOListFromServer = cardInfo.getJSONArray("cardDTOList");
        } catch (Exception e) {
            log.error("调用账户系统获取用户卡列表异常, userId:{},errorMsg:{}", data.getLong("userId"), e.getMessage());
        }
        boolean cardNotExit = true;
        String cardNo = data.getJSONObject("activateCreditMessageDTO").getString("bankcardNumber");
        String bsb = data.getJSONObject("activateCreditMessageDTO").getString("bankcardCode");
        JSONObject cardMessage = null;
        for (int i = 0; i < cardDTOListFromServer.size(); i++) {
            if (cardDTOListFromServer.getJSONObject(i).getInteger("type").intValue() == StaticDataEnum.TIE_CARD_0.getCode()) {
                if (cardDTOListFromServer.getJSONObject(i).getString("cardNo").equals(cardNo)
                        && cardDTOListFromServer.getJSONObject(i).getString("bsb").equals(bsb)) {
                    cardMessage = cardDTOListFromServer.getJSONObject(i);
                    cardNotExit = false;
                }
            }
        }
        //卡列表中此卡
        if (cardNotExit) {
            throw new BizException(I18nUtils.get("account.error", getLang(request)));
        }
        JSONObject creditTieOnCardReq = new JSONObject();
//        JSONObject cardMsgReq = new JSONObject();

        creditTieOnCardReq.put("accountId", cardMessage.getLong("id"));
        creditTieOnCardReq.put("bankcardNumber", cardMessage.getString("cardNo"));
        creditTieOnCardReq.put("bankcardCode", cardMessage.getString("bsb"));
        creditTieOnCardReq.put("bankcardBank", cardMessage.getString("bankName"));
        creditTieOnCardReq.put("bankcardHolderName", cardMessage.getString("name"));
        creditTieOnCardReq.put("userId", data.getLong("userId"));
//        creditTieOnCardReq.put("activateCreditMessageDTO",cardMsgReq);

        serverService.appCreditTieOnCard(creditTieOnCardReq);
        UserDTO userDTO1 = new UserDTO();
        userDTO1.setSplitAddInfoState(StaticDataEnum.STATUS_0.getCode());
        this.updateUser(userDTO.getId(), userDTO1, request);
    }

    private void doMarketingAmountIn(MarketingFlowDTO marketingFlowDTO, HttpServletRequest request) throws Exception {
        //如果是失败处理，需要先将流水改为处理中
        if (marketingFlowDTO.getState() != StaticDataEnum.TRANS_STATE_0.getCode() && marketingFlowDTO.getState() != StaticDataEnum.TRANS_STATE_3.getCode()) {
            marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
            marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(), marketingFlowDTO, request);
        }
        //查询是否有处理中和交易成功的流水
        Map<String, Object> params = new HashMap<>();
        params.put("flowId", marketingFlowDTO.getFlowId());
        params.put("transType", marketingFlowDTO.getTransType());
        List<AccountFlowDTO> transList = accountFlowService.find(params, null, null);
        //可疑交易返回
        List<AccountFlowDTO> doubtList = transList.stream().filter(list -> list.getState() == StaticDataEnum.TRANS_STATE_0.getCode() ||
                list.getState() == StaticDataEnum.TRANS_STATE_3.getCode()).collect(Collectors.toList());
        if (doubtList != null && doubtList.size() > 0) {
            return;
        }
        // 成功交易返回
        List<AccountFlowDTO> successList = transList.stream().filter(list -> list.getState() == StaticDataEnum.TRANS_STATE_1.getCode()).collect(Collectors.toList());
        if (successList != null && successList.size() > 0) {
            marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(), marketingFlowDTO, request);
            return;
        }
        //        JSONObject accountData = serverService.getAccountInfo(amountInUserId);
        AccountFlowDTO accountFlowDTO = createAccountFlow(marketingFlowDTO, request);
        JSONObject amountIn = new JSONObject();
        amountIn.put("amountInUserId", accountFlowDTO.getUserId());
        amountIn.put("channel", StaticDataEnum.ACCOUNT_CHANNEL_0001.getMessage());
        amountIn.put("channelSerialnumber", accountFlowDTO.getOrderNo());
        amountIn.put("transAmount", accountFlowDTO.getTransAmount());
        amountIn.put("transType", accountFlowDTO.getTransType());

        String code = null;
        try {
            // 调用账户系统入账，根据结果判断交易状态
            JSONObject msg = serverService.amountIn(amountIn);
            code = msg.getString("code");
        } catch (Exception e) {
            log.error("doMarketingAmountIn  failed message:{}", e.getMessage());
//            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
////            marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
////            //记录红包流水
////            accountFlowService.updateAccountFlow(accountFlowDTO.getId(), accountFlowDTO, request);
////            marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(),marketingFlowDTO,request);
            return;
        }
        int resultCode = 0;
        if (code.equals(ErrorCodeEnum.SUCCESS_CODE.getCode())) {
            resultCode = StaticDataEnum.TRANS_STATE_1.getCode();
        } else if (code.equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            resultCode = StaticDataEnum.TRANS_STATE_2.getCode();
        } else {
            resultCode = StaticDataEnum.TRANS_STATE_3.getCode();
        }
        //处理红包结果
        marketingFlowService.doMarketingAmountInResult(accountFlowDTO, marketingFlowDTO, resultCode, request);
    }


    /**
     * 创建账户交易流水
     */
    private AccountFlowDTO createAccountFlow(MarketingFlowDTO marketingFlowDTO, HttpServletRequest request) throws Exception {
        AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
        accountFlowDTO.setFlowId(marketingFlowDTO.getFlowId());
        accountFlowDTO.setUserId(marketingFlowDTO.getUserId());
        accountFlowDTO.setAccountType(StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
        accountFlowDTO.setTransAmount(marketingFlowDTO.getAmount());
        accountFlowDTO.setOrderNo(SnowflakeUtil.generateId());
        accountFlowDTO.setTransType(marketingFlowDTO.getTransType());
        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        Long id = accountFlowService.saveAccountFlow(accountFlowDTO, request);
        accountFlowDTO.setId(id);
        return accountFlowDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserList(@NonNull List<User> userList, HttpServletRequest request) throws BizException {
        if (userList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = userDAO.insertList(userList);
        if (rows != userList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, userList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateUser(@NonNull Long id, @NonNull UserDTO userDTO, HttpServletRequest request) throws BizException {
        log.info("full update userDTO:{}", userDTO);
        User user = BeanUtil.copyProperties(userDTO, new User());
        user.setId(id);
        int cnt = userDAO.update((User) this.packModifyBaseProps(user, request));
        if (cnt != 1) {
            log.error("update error, data:{}", userDTO);
            throw new BizException("update user Error!");
        }
    }

    @Override
    public void updateUserSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        userDAO.updatex(params);
    }

    @Override
    public void logicDeleteUser(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = userDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteUser(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = userDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public UserDTO findUserById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        UserDTO userDTO = userDAO.selectOneDTO(params);
        return userDTO;
    }

    @Override
    public UserDTO findOneUser(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        User user = userDAO.selectOne(params);
        UserDTO userDTO = new UserDTO();
        if (null != user) {
            BeanUtils.copyProperties(user, userDTO);
        }
        return userDTO;
    }

    @Override
    public List<UserDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<UserDTO> resultList = userDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<UserExcelDTO> findList(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<UserExcelDTO> resultList = userDAO.findList(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return userDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return userDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = userDAO.groupCount(conditions);
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Map<String, Object> m : maps) {
            String key = m.get("group") != null ? m.get("group").toString() : "group";
            Object value = m.get("count");
            int count = 0;
            if (StringUtils.isNotBlank(value.toString())) {
                count = Integer.parseInt(value.toString());
            }
            map.put(key, count);
        }
        return map;
    }

    @Override
    public Double sum(String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("sumfield", sumField);
        return userDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = userDAO.groupSum(conditions);
        Map<String, Double> map = new LinkedHashMap<>();
        for (Map<String, Object> m : maps) {
            String key = m.get("group") != null ? m.get("group").toString() : "group";
            Object value = m.get("sum");
            double sum = 0d;
            if (StringUtils.isNotBlank(value.toString())) {
                sum = Double.parseDouble(value.toString());
            }
            map.put(key, sum);
        }
        return map;
    }

    @Override
    public Long createMerchantStaff(UserDTO userDTO, HttpServletRequest request) throws BizException {
        //校验必填参数
        if (!Validator.isEmail(userDTO.getEmail())){
            throw new BizException(I18nUtils.get("username.contains.invalid.characters", getLang(request)));
        }
        if (StringUtils.isEmpty(userDTO.getEmail()) || userDTO.getEmail().length() > Validator.TEXT_LENGTH_100 || !(Validator.isEmail(userDTO.getEmail()))) {
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }

        if (StringUtils.isEmpty(userDTO.getUserType() + "")) {
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        if (StringUtils.isEmpty(userDTO.getMerchantId() + "")) {
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }

        // 查询输入该用户类型下注册号是否已存在
        Map<String, Object> params = new HashMap<>(5);
        userDTO.setEmail(userDTO.getEmail().toLowerCase());
        params.put("email", userDTO.getEmail());
        params.put("userType", userDTO.getUserType());
        params.put("merchantId", userDTO.getMerchantId());

        // 查询登录表,用户表，如果一个存在相同用户，则抛出异常，不允许注册
        MerchantLoginDTO merchantLogin = merchantLoginService.findOneMerchantLogin(params);
        if (findOneUser(params).getId() != null) {
            throw new BizException(I18nUtils.get("user.exist", getLang(request)));
        }

        // 用户表创建数据
        Long userId = userCreate(userDTO, request);

        //设置默认密码
        Long id = null;
        if (merchantLogin == null || merchantLogin.getId() == null) {
            merchantLogin = new MerchantLoginDTO();
            merchantLogin.setPassword(MD5FY.MD5Encode("admin123"));
            merchantLogin.setEmail(userDTO.getEmail());
            merchantLogin.setUserType(StaticDataEnum.USER_TYPE_20.getCode());
            id = merchantLoginService.saveMerchantLogin(merchantLogin, request);
        } else {
            id = merchantLogin.getId();
        }


        //创建一条用户失败次数记录
        LoginMissDTO loginMissDTO = new LoginMissDTO();
        loginMissDTO.setUserId(id);
        loginMissService.saveLoginMiss(loginMissDTO, request);

        return userId;
    }

    @Override
    public void sendSecurityCode(String sendNode, String email, Integer userType, HttpServletRequest request) throws Exception {
        email = email.toLowerCase();
        // 查询用户是否已存在，注册节点消息，存在则不发送消息，非注册节点，不存在则不发送消息
        if (Integer.valueOf(sendNode) == StaticDataEnum.SEND_NODE_0.getCode()) {
            Map<String, Object> params = new HashMap<>(3);
            params.put("email", email);
            params.put("userType", userType);
            UserDTO user = findOneUser(params);
            if (user.getId() != null) {
                throw new BizException(I18nUtils.get("user.exist", getLang(request)));
            }
        }
        // 生成验证码 测试环境写死666666
        int securityCode = TestEnvUtil.isTestEnv() ? 666666 : (int) ((Math.random() * 9 + 1) * 100000);
        redisUtils.set(email + "_" + userType, securityCode, 30 * 60);
        // 根据节点选择昵称
        String nickName = null;
        if (userType == StaticDataEnum.USER_TYPE_10.getCode()) {
            nickName = StaticDataEnum.U_WALLET.getMessage();
        } else {
            nickName = StaticDataEnum.U_BIZ.getMessage();
        }
        //获取邮件模板
        MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(sendNode);
        //设置模板参数
        String[] param = {securityCode + ""};
        //邮件内容
        String sendMsg = null;
        String sendTitle = null;
        sendMsg = templateContentReplace(param, mailTemplateDTO.getEnSendContent());
        sendTitle = templateContentReplace(param, mailTemplateDTO.getEnMailTheme());
        //发送邮件
        try {
            Session session = MailUtil.getSession(sysEmail);
            MimeMessage mimeMessage = MailUtil.getMimeMessage(nickName, sysEmail, email, sendTitle, sendMsg, null, session);
            MailUtil.sendMail(session, mimeMessage, sysEmail, sysEmailPwd);
            //记录邮件流水
            saveMailLog(email, sendMsg, 0, request);
        } catch (Exception e) {
            log.error("send email failed, info:{}, error msg:{}", sendMsg, e.getMessage());
            throw new BizException(I18nUtils.get("mail.send.failed", getLang(request)));
        }
    }

    @Override
    public void sendSecuritySMS(String phone, String sendNode, Integer userType, HttpServletRequest request) throws Exception {
        // 查询用户是否已存在，注册节点消息，存在则不发送消息，非注册节点，不存在则不发送消息
        Map<String, Object> params = new HashMap<>(2);
        params.put("phone", phone);
        params.put("userType", userType);
        if (Integer.parseInt(sendNode) == StaticDataEnum.SEND_NODE_1.getCode()) {
            UserDTO user = findOneUser(params);
            if (user.getId() != null) {
                throw new BizException(I18nUtils.get("user.exist", getLang(request)));
            }
        }else if (Integer.parseInt(sendNode) == StaticDataEnum.SEND_NODE_3.getCode()){
            UserDTO userDTO = findOneUser(params);
            if (userDTO == null || userDTO.getId() == null) {
                throw new BizException(I18nUtils.get("user.rule.userNameNotPresence", getLang(request)));
            }
        }
        // 生成验证码 todo 恢复正常验证码测试
        int securityCode = TestEnvUtil.isTestEnv() ? 666666 : (int) ((Math.random() * 9 + 1) * 100000);
//        int securityCode = (int) ((Math.random() * 9 + 1) * 100000);
        // 用户修改pin码 节点38
        if (Integer.parseInt(sendNode) == StaticDataEnum.SEND_NODE_38.getCode()){
            redisUtils.set(phone + "_" + userType+"_"+StaticDataEnum.SEND_NODE_38.getCode(), securityCode, 15 * 60);
        }else {
            redisUtils.set(phone + "_" + userType, securityCode, 15 * 60);
        }
        //获取邮件模板
        MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(sendNode);
        //判断手机区号
        String phoneCode = new StringBuilder(phone).substring(0, 2);
        if (phoneCode.equals(StaticDataEnum.CHN.getMessage())) {
            JSONArray phoneArray = new JSONArray();
            phoneArray.add(phone);
            JSONObject sendParams = new JSONObject();
            sendParams.put("code", securityCode);
            aliyunSmsService.sendChinaSms(phone, mailTemplateDTO.getAliCode(), sendParams);
        } else {
            //设置模板参数
            String[] param = {securityCode + ""};
            //邮件内容
            String sendMsg = templateContentReplace(param, mailTemplateDTO.getEnSendContent());
            aliyunSmsService.sendInternationalSms(phone, sendMsg);
        }
    }

    @Override
    public void forgetPassword(UserDTO userDTO, HttpServletRequest request) throws Exception {
        // 密码规则校验
        if (!Validator.isAdminPassword(userDTO.getPassword())) {
            throw new BizException(I18nUtils.get("user.rule.password", getLang(request)));
        }
        // 查询用户是否存在
        Map<String, Object> params = new HashMap<>(2);
        String code = "";
        Long id = null;
        //用户查询user表；商户查询merchant login 表
        if (StaticDataEnum.USER_TYPE_10.getCode() == userDTO.getUserType()) {
            if (StringUtils.isEmpty(userDTO.getPhone())) {
                throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
            }
            params.put("phone", userDTO.getPhone());
            params.put("userType", userDTO.getUserType());
            UserDTO hasDTO = findOneUser(params);
            if (hasDTO.getId() == null) {
                throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
            }
            //密码规则校验
//            if (hasDTO.getPassword().equals(DigestUtils.md5Hex(userDTO.getPassword()))) {
//                throw new BizException(I18nUtils.get("old.password.same", getLang(request)));
//            }
            code = userDTO.getPhone();
            id = hasDTO.getId();
        } else if (StaticDataEnum.USER_TYPE_20.getCode() == userDTO.getUserType()) {
            userDTO.setEmail(userDTO.getEmail().toLowerCase());
            if (StringUtils.isEmpty(userDTO.getEmail())) {
                throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
            }
            params.put("email", userDTO.getEmail());
            params.put("userType", userDTO.getUserType());
            MerchantLoginDTO merchantLoginDTO = merchantLoginService.findOneMerchantLogin(params);
            if (merchantLoginDTO.getId() == null) {
                throw new BizException(I18nUtils.get("user.doesnt.exist", getLang(request)));
            }
            //密码规则校验
//            if (merchantLoginDTO.getPassword().equals(DigestUtils.md5Hex(userDTO.getPassword()))) {
//                throw new BizException(I18nUtils.get("old.password.same", getLang(request)));
//            }
            code = userDTO.getEmail();
            id = merchantLoginDTO.getId();
        } else {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }

        //1、验证验证码
        if (redisUtils.get(code + "_" + userDTO.getUserType()) != null) {
            String securityCode = redisUtils.get(code + "_" + userDTO.getUserType()).toString();
            if (!userDTO.getSecurityCode().equals(securityCode)) {
                throw new BizException(I18nUtils.get("incorrect.verification.code", getLang(request)));
            }
        } else {
            throw new BizException(I18nUtils.get("verification.code.expired", getLang(request)));
        }
        if (StaticDataEnum.USER_TYPE_10.getCode() == userDTO.getUserType()) {
            userDTO.setId(id);
            userDTO.setPassword(MD5FY.MD5Encode(userDTO.getPassword()));
            updateUser(userDTO.getId(), userDTO, request);
        } else {
            MerchantLoginDTO updateDto = new MerchantLoginDTO();
            updateDto.setId(id);
            updateDto.setPassword(MD5FY.MD5Encode(userDTO.getPassword()));
            merchantLoginService.updateMerchantLogin(id, updateDto, request);
        }
    }

    @Override
    public synchronized Integer verifyUserPassword(String loginPassword, HttpServletRequest request) throws Exception {
        if (StringUtils.isBlank(loginPassword)) {
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        UserDTO user = this.findUserById(getUserId(request));
        if (null == user || null == user.getId()){
            throw new BizException(I18nUtils.get("user.rule.userNameNotPresence", getLang(request)));
        }
        String loginPasswordMd5 = MD5FY.MD5Encode(loginPassword);
        return loginPasswordMd5.equalsIgnoreCase(user.getPassword()) ? 1 : 0;
    }

    @Override
    public synchronized JSONObject appLogin(JSONObject loginData, HttpServletRequest request) throws Exception {
        log.info("log in info, data:{}", loginData);

        String loginName = loginData.getString("loginName");
        if (StringUtils.isEmpty(loginName)) {
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        String loginPassword = loginData.getString("loginPassword");
        loginPassword = MD5FY.MD5Encode(loginPassword);
        Integer userType = loginData.getInteger("userType");
        JSONObject msg = new JSONObject();
        String token = null;
        String password;
        UserDTO userDTO = null;
        Long id;
        String lat = loginData.getString("lat");
        String lng = loginData.getString("lng");
        String pushToken = loginData.getString("pushToken");
        String imeiNo = loginData.getString("imeiNo");
        //查询用户是否存在
        Map<String, Object> params = new HashMap<>(2);
        if (userType == StaticDataEnum.USER_TYPE_20.getCode()) {
            params.put("email", loginName);
            params.put("userType", userType);
            MerchantLoginDTO loginMsg = merchantLoginService.findOneMerchantLogin(params);
            if (loginMsg == null || loginMsg.getId() == null) {
                throw new BizException(I18nUtils.get("user.rule.userNameNotPresence", getLang(request)));
            }
            password = loginMsg.getPassword();
            //查询登陆错误记录表,如果登陆时间与锁定时间相差仍在一小时内，则拒绝登陆
            id = loginMsg.getId();
        } else {
            params.put("phone", loginName);
            params.put("userType", userType);
            userDTO = findOneUser(params);
            if (userDTO == null || userDTO.getId() == null) {
                throw new BizException(I18nUtils.get("user.rule.userNameNotPresence", getLang(request)));
            }
            password = userDTO.getPassword();
            id = userDTO.getId();
        }
        //查询登陆错误记录表,如果登陆时间与锁定时间相差仍在一小时内，则拒绝登陆
        params.clear();
        params.put("userId", id);
        LoginMissDTO loginMissDTO = loginMissService.findOneLoginMiss(params);
        Long lastErrorTime = loginMissDTO.getLastErrorTime();
        this.countFrozenTime(loginMissDTO, request);
        //判断登陆密码是否正确
        if (password.equals(loginPassword)) {
            log.info("user info , user data:{}, log in info:{}", id, loginData);
            //验证通过后重置登陆机会
            if (loginMissDTO.getChance() != StaticDataEnum.LOGIN_MISS_TIME.getCode()) {
                loginMissDTO.setChance(5);
                loginMissService.updateLoginMiss(loginMissDTO.getId(), loginMissDTO, request);
            }
            if (StaticDataEnum.USER_TYPE_10.getCode() == userType) {
                //如果是用户登录，生成token值
                token = JwtUtils.signApp(id, loginName, loginPassword, userType, redisUtils);
                String firstName = null;
                String middleName = null;
                String lastName = null;
                //如果是用户，返回用户信息
                JSONObject data = serverService.userInfoByQRCode(id);
                log.info("user info , user data:{}", data);
                firstName = data.getString("userFirstName");
                middleName = data.getString("userMiddleName");
                lastName = data.getString("userLastName");
                msg.put("Authorization", token);
                msg.put("userInfo", userDTO);
                msg.put("firstName", firstName);
                msg.put("middleName", middleName);
                msg.put("lastName", lastName);
                msg.put("lat", lat);
                msg.put("lng", lng);
                //更新经纬度
                userDTO.setLat(lat);
                userDTO.setLng(lng);
                userDTO.setPushToken(pushToken);
                userDTO.setImeiNo(imeiNo);
                // 更新登陆时间
                userDTO.setLoginTime(System.currentTimeMillis());
                updateUser(userDTO.getId(), userDTO, request);
            } else {
                //如果是商户登录，查询商户列表
                params.clear();
                params.put("email", loginName);
                params.put("userType", userType);
                List<MerchantDTO> merList = merchantService.findMerchantLogInList(params);
                msg.put("merList", merList);
                msg.put("loginName", loginName);
            }
        } else {
            //判断登陆机会是否还有1次，不是则减少登陆机会重新登陆，是则将登陆机会重置为5次
            if (loginMissDTO.getChance() != StaticDataEnum.LOGIN_MISS_TIME_LEFT.getCode()) {
                loginMissService.loginMissRecord(loginMissDTO, request);
                log.info("user info , user data:{}, log in info:{}", userDTO, loginData);
                throw new BizException(I18nUtils.get("incorrect.username.password", getLang(request), new String[]{" " + (loginMissDTO.getChance() - 1)}));
            } else {

                loginMissDTO.setChance(5);
                loginMissDTO.setLastErrorTime(System.currentTimeMillis());
                loginMissService.updateLoginMiss(loginMissDTO.getId(), loginMissDTO, request);
                log.info("user info , user data:{}, log in info:{}", userDTO, loginData);
                throw new BizException(I18nUtils.get("login.lock", getLang(request), new String[]{(60 - (System.currentTimeMillis() - (lastErrorTime != null?lastErrorTime:System.currentTimeMillis())) / (1000 * 60)) + " min"}));
            }
        }
        msg = JSONResultHandle.resultHandle(msg);
        return msg;
    }


    /**
     * 当前时间与loginMiss表数据,最后一次登录失败时间比较
     * 如果小于1小时,则仍然被冻结,并提示剩余等待时间
     *
     * @param loginMissDTO
     * @param request
     * @throws BizException
     */
    @Override
    public void countFrozenTime(LoginMissDTO loginMissDTO, HttpServletRequest request) throws BizException {
        Long lastErrorTime = loginMissDTO.getLastErrorTime();
        if (null != lastErrorTime) {
            long currentTimeMillis = System.currentTimeMillis();
            BigDecimal remainTime = (new BigDecimal(currentTimeMillis).subtract(new BigDecimal(lastErrorTime)));
            if (remainTime.compareTo(BigDecimal.ZERO) >= 0 && remainTime.compareTo(new BigDecimal(1000 * 60 * 60)) <= 0) {
                log.error("用户登录失败,已经被锁定, login Info:{}", loginMissDTO);
                //剩余冻结时间
                long frozenTimeMin = 60 - (currentTimeMillis - lastErrorTime) / (1000 * 60);
                String message = I18nUtils.get("login.lock", getLang(request), new String[]{frozenTimeMin + " min"});
                throw new BizException(message);
            }
        }
    }

    @Override
    public Long tieOnCard(JSONObject cardInfo, HttpServletRequest request) throws Exception {
        log.info("tie on card, data:{}", cardInfo);
        Long id;
        if (cardInfo.getString("userId")==null){
            cardInfo.put("userId",getUserId(request)+"");
        }
        UserDTO userDTO = findUserById(getUserId(request));
        if (cardInfo.getInteger("type") == StaticDataEnum.TIE_CARD_1.getCode()) {
            // 查询卡支付方式，走相应绑卡api
//            Map<String, Object> params = new HashMap<>(1);
//            params.put("gatewayType", StaticDataEnum.PAY_TYPE_0.getCode());
//            List<PaymentChannelDTO> paymentChannelDTOList = paymentChannelService.find(params, null, null);
//            boolean latPayExist = false;
//            boolean integraPayExist = false;
//            for (PaymentChannelDTO paymentChannelDTO : paymentChannelDTOList) {
//                if (paymentChannelDTO.getId().equals(Long.valueOf(StaticDataEnum.GATEWAY_TYPE_0.getCode()))) {
//                    latPayExist = true;
//                } else if (paymentChannelDTO.getId().equals(Long.valueOf(StaticDataEnum.GATEWAY_TYPE_4.getCode()))) {
//                    integraPayExist = true;
//                }
//            }
//            if (!latPayExist && !integraPayExist) {
//                throw new BizException(I18nUtils.get("tie.on.card.not.supposed", getLang(request)));
//            }
            // latpay 绑卡
//            if (latPayExist) {
            /*
             旧版本适应逻辑, 新app会在请求该接口之前获取以下三个参数: cardCategory,cardPayType,cardType
             旧版本, 只会携带cardType, 如果检查其他两个参数没有 需要请求latpay获取
             */
            try {
                JSONObject param = new JSONObject(2);
                param.put("cardBin", cardInfo.get("cardNo"));
                JSONObject cardTypeRes = this.latpayGetCardType(param, request);
                // 返回参数 "cardType"-->对应,库表customerCcType字段, "cardPayType", "cardCategory" 对应的数据字典值
                cardInfo.put("customerCcType",cardTypeRes.get("cardType"));
                cardTypeRes.remove("cardType");
                cardInfo.putAll(cardTypeRes);
            } catch (Exception e) {
                log.error("静默获取卡类型详情异常,cardInfo:{},error msg:{},error:{}", cardInfo, e.getMessage(), e);
                throw new BizException(e.getMessage());
            }
            JSONObject tieOnCardRes = latPayTieOnCard(cardInfo, userDTO, request);
            //封装账户系统要的入参
            cardInfo.put("crdStrgToken", tieOnCardRes.getString("CrdStrg_Token"));

//            }
            // integraPay 绑卡
//            if (integraPayExist) {
//                JSONObject integraPayCardResult = integraPayTieOnCard(cardInfo, requ
//                cardInfo.put("uniqueReference", integraPayCardResult.getString("uniqueReference"));
//                cardInfo.put("payerId", integraPayCardResult.getString("payerId"));
//                cardInfo.put("integraPayAccountId", integraPayCardResult.getString("integraPayAccountId"));
//            }
            //4、调用账户系统进虚户绑卡操作，敏感信息不记录
//        cardInfo.remove("customerCcCvc");
            cardInfo.remove("customerCcExpmo");
            cardInfo.remove("customerCcExpyr");
            cardInfo.put("phone", userDTO.getPhone());
            cardInfo.put("firstName", userDTO.getUserFirstName());
            cardInfo.put("lastName", userDTO.getUserLastName());
            id = serverService.tieOnCard(cardInfo);
            //2021-01-19添加逻辑, 新建账户默认卡支付不可用, 绑卡后更新user表数据payment_state = 1
            if (null != id) {
                Long userId = cardInfo.getLong("userId");
                this.updateUser(userId, UserDTO.builder().cardState(StaticDataEnum.USER_CARD_STATE_1.getCode()).build(), request);
            }
            //2021-07-06添加逻辑, 绑卡就设置为默认卡
            JSONObject setPreset = new JSONObject(2);
            setPreset.put("cardId",id);
            //add by zhangzeyuan 2021年9月15日15:52:46 新版本绑卡通知分期付绑卡状态

            setPreset.put("isCreditCard", cardInfo.getString("isCreditCard"));
            this.presetCard(setPreset,getUserId(request),request);

            //新版本绑卡 同意分期付绑卡协议
            Integer creditCardAgreementState = cardInfo.getInteger("creditCardAgreementState");
            if(null != creditCardAgreementState){
                UserDTO updateRecord = new UserDTO();
                updateRecord.setCreditCardAgreementState(creditCardAgreementState);
                this.updateUser(cardInfo.getLong("userId"), updateRecord, request);
            }
        } else {
            // 查询卡支付方式，走相应绑账户api
            Map<String, Object> params = new HashMap<>(1);
            params.put("gatewayType", StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode());
            params.put("state", StaticDataEnum.STATUS_1.getCode());
            List<GatewayDTO> gatewayDTOList = gatewayService.find(params, null, null);
            if (gatewayDTOList == null || gatewayDTOList.size() == 0) {
                throw new BizException(I18nUtils.get("tie.on.card.not.supposed", getLang(request)));
            }
            JSONObject card = new JSONObject();
            card.put("userId", cardInfo.getString("userId"));
            card.put("cardNo", cardInfo.getString("cardNo"));
            card.put("type", StaticDataEnum.TIE_CARD_0.getCode());
            card.put("bsb", cardInfo.getString("bsb"));
            card.put("bankName", cardInfo.getString("bankName"));
            card.put("accountName", cardInfo.getString("accountName"));
            card.put("name", cardInfo.getString("name"));
            card.put("email", cardInfo.getString("email"));
            for (GatewayDTO gatewayDTO : gatewayDTOList) {
                if (gatewayDTO.getType().equals((long) StaticDataEnum.GATEWAY_TYPE_6.getCode())) {
                    JSONObject result = splitTieOnAccount(cardInfo, request);
                    //split通道可用
                    card.put("splitContactId", result.getString("contactId"));
                    card.put("splitAgreementId", result.getString("agreementRef"));
                    card.put("splitSignState", StaticDataEnum.STATUS_1.getCode());
                    card.put("splitContractVersion", splitContractVersion);
                    card.put("splitContractTime", System.currentTimeMillis());
                } else if (gatewayDTO.getType().equals((long) StaticDataEnum.GATEWAY_TYPE_5.getCode())) {
                    //TODO integraPay通道暂时不可用
//                JSONObject integraPayAccountResult = integraPayTieOnAccount(cardInfo, request);
//                card.put("uniqueReference", integraPayAccountResult.getString("uniqueReference"));
//                card.put("payerId", integraPayAccountResult.getString("payerId"));
//                card.put("integraPayAccountId", integraPayAccountResult);
                } else if (gatewayDTO.getType().equals(Long.valueOf(StaticDataEnum.GATEWAY_TYPE_3.getCode()))) {
                    //LatPay通道无需三方绑卡
                }
            }
            id = serverService.tieOnCard(card);
        }

        // 清空缓存卡信息
        redisUtils.del(cardInfo.getLong("userId") + "_card");
        return id;
    }

    /**
     * split绑定账户操作
     *
     * @param cardInfo
     * @param request
     * @return
     * @throws Exception
     */
    private JSONObject splitTieOnAccount(JSONObject cardInfo, HttpServletRequest request) throws Exception {
        //绑卡交易流水记录
        int channelId = StaticDataEnum.GATEWAY_TYPE_6.getCode();
        TieOnCardFlowDTO tieOnCardFlowDTO = new TieOnCardFlowDTO();
        tieOnCardFlowDTO.setUserId(cardInfo.getLong("userId"));
        tieOnCardFlowDTO.setCardNo(cardInfo.getString("cardNo"));
        tieOnCardFlowDTO.setName(cardInfo.getString("name"));
        tieOnCardFlowDTO.setEmail(cardInfo.getString("email"));
        tieOnCardFlowDTO.setBsb(cardInfo.getString("bsb"));
        tieOnCardFlowDTO.setChannelId((long) channelId);
        JSONObject result;
        try {
            result = splitService.splitTieAccount(cardInfo, request);
        } catch (Exception e) {
            tieOnCardFlowDTO.setErrorCode(StaticDataEnum.TRANS_STATE_2.getCode() + "");
            tieOnCardFlowService.saveTieOnCardFlow(tieOnCardFlowDTO, request);
            throw e;
        }
        tieOnCardFlowDTO.setErrorCode(StaticDataEnum.TRANS_STATE_1.getCode() + "");
        tieOnCardFlowDTO.setSplitContactId(result.getString("contactId"));
        tieOnCardFlowDTO.setSplitAgreementId(result.getString("agreementRef"));
        tieOnCardFlowService.saveTieOnCardFlow(tieOnCardFlowDTO, request);
        return result;
    }

    /**
     * latPay绑卡操作
     *
     * @param cardInfo
     * @param request
     * @return
     * @throws Exception
     */
    private JSONObject latPayTieOnCard(JSONObject cardInfo, UserDTO userDTO, HttpServletRequest request) throws Exception {
        int channelId = StaticDataEnum.GATEWAY_TYPE_0.getCode();
        Map<String, Object> params = new HashMap<>(1);
        params.put("type", StaticDataEnum.GATEWAY_TYPE_0.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        //查询国家ISO列表
        params.clear();
        params.put("code", "county");
        params.put("value", cardInfo.getString("country"));
        StaticDataDTO country = staticDataService.findOneStaticData(params);
        CountryIsoDTO countryIsoDTO = tieOnCardFlowService.selectCountryIso(country.getEnName());
        if (countryIsoDTO == null) {
            throw new BizException(I18nUtils.get("bank.country", getLang(request)));
        }
        params.clear();
        params.put("code", "cardType");
        params.put("value", cardInfo.getString("customerCcType"));
        StaticDataDTO cardType = staticDataService.findOneStaticData(params);

        //1、调用latpay绑定卡信息，接受返回token
        JSONObject requestTokenInfo = new JSONObject();
        requestTokenInfo.put("Merchant_id", gatewayDTO.getChannelMerchantId());
        requestTokenInfo.put("merchantpwd", gatewayDTO.getPassword());
        requestTokenInfo.put("AccountType", "A");
        requestTokenInfo.put("RequestType", "SCSS_A");
        requestTokenInfo.put("Customer_ccno", cardInfo.getString("cardNo"));
        requestTokenInfo.put("Bill_firstname", userDTO.getUserFirstName());
        requestTokenInfo.put("Bill_lastname", userDTO.getUserLastName());
        requestTokenInfo.put("Bill_address1", cardInfo.getString("address1"));
        requestTokenInfo.put("Bill_city", cardInfo.getString("city"));
        requestTokenInfo.put("Bill_country", countryIsoDTO.getTwoLettersCoding());
        requestTokenInfo.put("Bill_zip", cardInfo.getString("zip"));
        requestTokenInfo.put("Customer_cc_type", cardType.getName());
        String customerCcExpmo = cardInfo.getString("customerCcExpmo");
        String customerCcExpyr = cardInfo.getString("customerCcExpyr");
        this.verifyExpDate(customerCcExpmo,customerCcExpyr,request);
        requestTokenInfo.put("Customer_cc_expmo", customerCcExpmo);
        requestTokenInfo.put("Customer_cc_expyr", customerCcExpyr);
        //2、绑卡交易流水记录
        TieOnCardFlowDTO tieOnCardFlowDTO = new TieOnCardFlowDTO();
        tieOnCardFlowDTO.setUserId(cardInfo.getLong("userId"));
        tieOnCardFlowDTO.setCardNo(cardInfo.getString("cardNo"));
        tieOnCardFlowDTO.setFirstName(cardInfo.getString("firstName"));
        tieOnCardFlowDTO.setLastName(cardInfo.getString("lastName"));
        tieOnCardFlowDTO.setAddress1(cardInfo.getString("address1"));
        tieOnCardFlowDTO.setCity(cardInfo.getString("city"));
        tieOnCardFlowDTO.setCountry(countryIsoDTO.getTwoLettersCoding());
        tieOnCardFlowDTO.setZip(cardInfo.getString("zip"));
        tieOnCardFlowDTO.setCustomerCcType(cardInfo.getString("customerCcType"));
        tieOnCardFlowDTO.setChannelId(new Long(channelId));
        //3、获取交易结果
        params.clear();
        params = requestTokenInfo;
        log.info("request token info, data:{}", requestTokenInfo);
        JSONObject data = JSONResultHandle.resultHandle(HttpClientUtils.sendPostForm(tieOnCardUrl, params));
        log.info("request token info, result:{}", data);
        if (data.getString("transactionFailed").equals(ErrorCodeEnum.LAT_PAY_FAILED.getMessage())) {
            tieOnCardFlowDTO.setErrorMessage(data.getString("transactionFailed"));
            tieOnCardFlowService.saveTieOnCardFlow(tieOnCardFlowDTO, request);
            throw new BizException(ErrorCodeEnum.LAT_PAY_FAILED.getMessage());
        }
        JSONObject latpayResponseData = data.getJSONObject("data");
        if (latpayResponseData.getInteger("Status") != StaticDataEnum.LAT_PAY_SUCCESS_CODE_0.getCode() && latpayResponseData.getInteger("Status") != StaticDataEnum.LAT_PAY_SUCCESS_CODE_9011.getCode()) {
            tieOnCardFlowDTO.setErrorCode(latpayResponseData.getString("status"));
            tieOnCardFlowService.saveTieOnCardFlow(tieOnCardFlowDTO, request);
            throw new BizException(I18nUtils.get("tie.on.card.fail", getLang(request)));
        }
        tieOnCardFlowDTO.setCrdStrgToken(latpayResponseData.getString("CrdStrg_Token"));
        tieOnCardFlowDTO.setErrorCode(latpayResponseData.getString("Status"));
        tieOnCardFlowService.saveTieOnCardFlow(tieOnCardFlowDTO, request);
        return latpayResponseData;
    }

    /**
     * integraPay绑卡操作
     *
     * @param cardInfo
     * @param request
     * @return
     * @throws Exception
     */
    private JSONObject integraPayTieOnCard(JSONObject cardInfo, HttpServletRequest request) throws Exception {
        JSONObject integraPayCardResult = new JSONObject();
        // 查询国家ISO列表
        Map<String, Object> params = new HashMap<>(1);
        params.put("code", "county");
        params.put("value", cardInfo.getString("country"));
        StaticDataDTO country = staticDataService.findOneStaticData(params);
        CountryIsoDTO countryIsoDTO = tieOnCardFlowService.selectCountryIso(country.getEnName());
        if (countryIsoDTO == null) {
            throw new BizException(I18nUtils.get("bank.country", getLang(request)));
        }
        // 绑卡交易流水记录
        TieOnCardFlowDTO tieOnCardFlowDTO = new TieOnCardFlowDTO();
        tieOnCardFlowDTO.setUserId(cardInfo.getLong("userId"));
        tieOnCardFlowDTO.setCardNo(AnonymityUtil.hideCardNo(cardInfo.getString("cardNo")));
        tieOnCardFlowDTO.setFirstName(cardInfo.getString("firstName"));
        tieOnCardFlowDTO.setLastName(cardInfo.getString("lastName"));
        tieOnCardFlowDTO.setAddress1(cardInfo.getString("address1"));
        tieOnCardFlowDTO.setCity(cardInfo.getString("city"));
        tieOnCardFlowDTO.setCountry(countryIsoDTO.getTwoLettersCoding());
        tieOnCardFlowDTO.setZip(cardInfo.getString("zip"));
        tieOnCardFlowDTO.setCustomerCcType(cardInfo.getString("customerCcType"));
        try {
            // 获取账户信息
            JSONObject accountInfo = serverService.getAccountInfo(cardInfo.getLong("userId"));
            // 获取api请求token
            String apiAccessToken = integraPayService.apiAccessToken();
            // 和获取卡token
            JSONObject cardTokenRquest = new JSONObject();
            cardTokenRquest.put("CardNumber", cardInfo.getString("cardNo"));
            cardTokenRquest.put("CardholderName", cardInfo.getString("lastName") + " " + cardInfo.getString("firstName"));
            cardTokenRquest.put("ExpiryYear", cardInfo.getString("customerCcExpyr"));
            cardTokenRquest.put("ExpiryMonth", cardInfo.getString("customerCcExpmo"));
            cardTokenRquest.put("Ccv", cardInfo.getString("customerCcCvc"));
            JSONObject cardRequestInfo = new JSONObject();
            cardRequestInfo.put("Card", cardTokenRquest);
            String cardToken = integraPayService.cardTokenGet(cardRequestInfo, apiAccessToken);
            // 添加付款人
            JSONObject payerRequest = new JSONObject();
            payerRequest.put("UniqueReference", SnowflakeUtil.generateId());
            payerRequest.put("GroupReference", accountInfo.getLong("id"));
            payerRequest.put("FamilyOrBusinessName", cardInfo.getString("firstName"));
            payerRequest.put("GivenName", cardInfo.getString("lastName"));
            payerRequest.put("Email", cardInfo.getString("email"));
            payerRequest.put("Phone", cardInfo.getString("phone"));
            JSONObject addPayerResult = integraPayService.addPayer(payerRequest, apiAccessToken);
            integraPayCardResult.put("uniqueReference", addPayerResult.getString("uniqueReference"));
            integraPayCardResult.put("payerId", addPayerResult.getString("payerId"));
            // 付款人绑定卡
            JSONObject payerTokenAddRequest = new JSONObject();
            payerTokenAddRequest.put("CardToken", cardToken);
            String integraAccountId = integraPayService.payerTokenAdd(payerTokenAddRequest, addPayerResult.getString("uniqueReference"), apiAccessToken);
            integraPayCardResult.put("integraPayAccountId", integraAccountId);
        } catch (Exception e) {
            log.info("integraPay tie on card fail, data:{}, error message:{}, e:{}", cardInfo, e.getMessage(), e);
            tieOnCardFlowDTO.setErrorMessage(e.getMessage());
            tieOnCardFlowService.saveTieOnCardFlow(tieOnCardFlowDTO, request);
            throw new BizException(I18nUtils.get("tie.on.card.fail", getLang(request)));
        }
        tieOnCardFlowDTO.setUniqueReference(integraPayCardResult.getString("uniqueReference"));
        tieOnCardFlowDTO.setPayerId(integraPayCardResult.getString("payerId"));
        tieOnCardFlowDTO.setIntegraPayAccountId(integraPayCardResult.getString("integraPayAccountId"));
        tieOnCardFlowService.saveTieOnCardFlow(tieOnCardFlowDTO, request);
        return integraPayCardResult;
    }

    /**
     * integraPay绑定账户操作
     *
     * @param cardInfo
     * @return
     */
    private JSONObject integraPayTieOnAccount(JSONObject cardInfo, HttpServletRequest request) throws Exception {
        JSONObject integraPayAccountResult = new JSONObject();
        // 获取账户信息
        JSONObject accountInfo = serverService.getAccountInfo(cardInfo.getLong("userId"));
        try {
            // 获取api请求token
            String apiAccessToken = integraPayService.apiAccessToken();
            // 添加付款人
            JSONObject payerRequest = new JSONObject();
            payerRequest.put("UniqueReference", SnowflakeUtil.generateId());
            payerRequest.put("GroupReference", accountInfo.getLong("id"));
            payerRequest.put("FamilyOrBusinessName", cardInfo.getString("firstName"));
            payerRequest.put("GivenName", cardInfo.getString("lastName"));
            payerRequest.put("Email", cardInfo.getString("email"));
            payerRequest.put("Phone", cardInfo.getString("phone"));
            JSONObject addPayerResult = integraPayService.addPayer(payerRequest, apiAccessToken);
            integraPayAccountResult.put("uniqueReference", addPayerResult.getString("uniqueReference"));
            integraPayAccountResult.put("payerId", addPayerResult.getString("payerId"));
            // 绑定账户
            JSONObject payerAccountAddRequest = new JSONObject();
            payerAccountAddRequest.put("accountBranch", cardInfo.getString("bsb"));
            payerAccountAddRequest.put("accountNumber", cardInfo.getString("cardNo"));
            payerAccountAddRequest.put("accountName", cardInfo.getString("bankName"));
            String integraAccountId = integraPayService.payerTokenAdd(payerAccountAddRequest, addPayerResult.getString("uniqueReference"), apiAccessToken);
            integraPayAccountResult.put("integraPayAccountId", integraAccountId);
        } catch (Exception e) {
            log.info("integraPay tie on account fail, data:{}, error message:{}, e:{}", cardInfo, e.getMessage(), e);
            throw new BizException(I18nUtils.get("tie.on.account.fail", getLang(request)));
        }
        return integraPayAccountResult;
    }

    @Override
    public JSONArray getCardList(Long userId, Integer cardType, HttpServletRequest request) throws Exception {
        /**
         * todo 这个接口逻辑重写!!!
         */
        Long gatewayType = this.getGatewayType();
        //尝试从账户系统获取绑卡列表, 如果没有,则请求dp系统获取illion卡列表
        JSONArray cardDTOListFromServer = this.getCardListFromServer(userId);
        JSONArray cardList = new JSONArray();
        // 如果账户系统返回结果,则对结果进行处理并返回, 如果结果集为空 则请求data系统 获取illion 账号列表
        if (cardType.equals(StaticDataEnum.TIE_CARD_0.getCode())){
            List<JSONObject> accountList = cardDTOListFromServer.toJavaList(JSONObject.class).stream().filter(card -> card.getInteger("type").equals(StaticDataEnum.TIE_CARD_0.getCode())).collect(Collectors.toList());
            cardList = accountList.size() > 0 ?  this.packCardDTOListFromServer(JSONArray.parseArray(accountList.toString()), gatewayType) : this.requestDataSystemIllionCardList(userId, request);
        }else {
            if (CollectionUtil.isNotEmpty(cardDTOListFromServer)) {
                cardList = this.packCardDTOListFromServer(cardDTOListFromServer, gatewayType);
            }
        }

        JSONArray returnCardList = new JSONArray();
        if (CollectionUtil.isNotEmpty(cardList)) {
            Stream<JSONObject> cardJsonStream = cardList.toJavaList(JSONObject.class).stream();
            if (null != cardType) {
                cardJsonStream = cardJsonStream.filter(card -> card.getInteger("type").equals(cardType));
            }
            //排序 绑卡时间正序
            if (null != cardType && cardType == StaticDataEnum.TIE_CARD_1.getCode()) {
                try {
                    cardJsonStream = cardJsonStream.sorted(Comparator.comparing(json -> json.getLong("id")));
                } catch (Exception e) {
                    log.error("卡列表排序失败, 无创建时间数据,已经try/catch 不抛异常,放行处理,error:{}", e.getMessage());
                }
            }
            List<JSONObject> jsonObjectList = cardJsonStream.collect(Collectors.toList());
            Integer maxOrderNo = null;
            //用户设置的默认卡
            Integer presetIndex = null;
            if (cardType == StaticDataEnum.TIE_CARD_1.getCode()) {
                if (CollectionUtil.isNotEmpty(jsonObjectList)) {
                    maxOrderNo = jsonObjectList.stream().mapToInt(JSONObject -> JSONObject.getInteger("order")).max().getAsInt();
                    //如果客户设置了默认卡
                    for (int i = 0; i < jsonObjectList.size(); i++) {
                        //如果当前卡是默认卡, 则记录卡的index
                        JSONObject card = jsonObjectList.get(i);
                        if (card.getInteger("preset") == 1){
                            System.out.println(card.toJSONString());
                            presetIndex = i;
                        }
                    }
                } else {
                    //如果卡列表为空
                    UserDTO updateUser = new UserDTO();
                    updateUser.setCardState(0);
                    this.updateUser(userId, updateUser, request);
                }
            }
            for (int i = 0; i < jsonObjectList.size(); i++) {
                JSONObject card = jsonObjectList.get(i);
                //2020-02-24 添加逻辑 如果是卡列表 其中添加 月-年 信息
                String crdStrgToken = card.getString("crdStrgToken");
                Integer order = card.getInteger("order");
                //如果是默认卡,或者用户没有设置默认卡, 则使用order字段最大的卡作为默认卡, 放在列表最后, 并查询过期日期
                if ((null != presetIndex && i==presetIndex )||
                        (presetIndex == null && null != maxOrderNo && order.equals(maxOrderNo) && cardType == StaticDataEnum.TIE_CARD_1.getCode() && StringUtils.isNotBlank(crdStrgToken))) {
                    try {
                        JSONObject cardExpInfo = this.queryLatpayCardInfo(crdStrgToken, request);
                        card.putAll(cardExpInfo);
                    } catch (Exception e) {
                        log.error("卡列表-cardList: 获取latpay卡信息失败,error:{},error message:{}", e, e.getMessage());
                        continue;
                    }
                }
                if (cardType == StaticDataEnum.TIE_CARD_1.getCode()) {
                    card.put("typeStr",this.processDisplayCardType(card,request));
                }
                //cvc 信息脱敏
                card.remove("customerCcCvc");
                //是否需要做新增处理的标识位
                card.put("needInsert", StringUtils.isBlank(card.getString("needInsert")) ? false : card.getBoolean("needInsert"));
                //判断当前账户是否通过当前渠道的验证,split看是否有contract_id,其他渠道 默认通过
                card.put("gatewayType", gatewayType);
                returnCardList.add(card);
            }
            if (null != presetIndex){
                //将默认卡放到卡列表的最后
                Object presetCard = returnCardList.get(presetIndex);
                returnCardList.remove(presetCard);
                returnCardList.add(presetCard);
            }
        }


        log.info("card list, list:{}", returnCardList);
        return returnCardList;
    }

    @Override
    public List<JSONObject> getCardListEpoch(Long userId, Integer cardType, HttpServletRequest request) throws Exception {
        /**
         * todo 这个接口逻辑重写!!!
         */
        Long gatewayType = this.getGatewayType();
        //尝试从账户系统获取绑卡列表, 如果没有,则请求dp系统获取illion卡列表
        JSONArray cardDTOListFromServer = this.getCardListFromServer(userId);
        JSONArray cardList = new JSONArray();
        // 如果账户系统返回结果,则对结果进行处理并返回, 如果结果集为空 则请求data系统 获取illion 账号列表
        if (cardType.equals(StaticDataEnum.TIE_CARD_0.getCode())){
            List<JSONObject> accountList = cardDTOListFromServer.toJavaList(JSONObject.class).stream().filter(card -> card.getInteger("type").equals(StaticDataEnum.TIE_CARD_0.getCode())).collect(Collectors.toList());
            cardList = accountList.size() > 0 ?  this.packCardDTOListFromServer(JSONArray.parseArray(accountList.toString()), gatewayType) : this.requestDataSystemIllionCardList(userId, request);
        }else {
            if (CollectionUtil.isNotEmpty(cardDTOListFromServer)){
                cardList = this.packCardDTOListFromServer(cardDTOListFromServer, gatewayType);
            }
        }


        List<JSONObject> returnCardList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(cardList)) {
            Stream<JSONObject> cardJsonStream = cardList.toJavaList(JSONObject.class).stream();
            if (null != cardType) {
                cardJsonStream = cardJsonStream.filter(card -> card.getInteger("type").equals(cardType));
            }
            //排序 绑卡时间正序
            if (null != cardType && cardType == StaticDataEnum.TIE_CARD_1.getCode()) {
                cardJsonStream = cardJsonStream.sorted(Comparator.comparing(json -> json.getInteger("order")));
            }
            List<JSONObject> jsonObjectList = cardJsonStream.collect(Collectors.toCollection(LinkedList::new));
            //按照order大小 倒序排序
            for (int i = jsonObjectList.size()-1; i >=0; i--) {
                JSONObject card = jsonObjectList.get(i);
                if (cardType == StaticDataEnum.TIE_CARD_1.getCode()) {
                    card.put("typeStr",this.processDisplayCardType(card,request));
                    String crdStrgToken = card.getString("crdStrgToken");
                    try{
                        JSONObject jsonObject = this.queryLatpayCardInfo(crdStrgToken, request);
                        card.put("customerCcExpyr", jsonObject.getString("customerCcExpyr"));
                        card.put("customerCcExpmo", jsonObject.getString("customerCcExpmo"));
                    }catch (Exception e){
                        card.put("customerCcExpyr", "");
                        card.put("customerCcExpmo", "");
                    }
                }
                //cvc 信息脱敏
                card.remove("customerCcCvc");
                //是否需要做新增处理的标识位
                card.put("needInsert", StringUtils.isBlank(card.getString("needInsert")) ? false : card.getBoolean("needInsert"));
                //判断当前账户是否通过当前渠道的验证,split看是否有contract_id,其他渠道 默认通过
                card.put("gatewayType", gatewayType);
                returnCardList.add(card);
            }
            returnCardList = returnCardList.stream().sorted(Comparator.comparing(json->json.getInteger("order"))).collect(Collectors.toCollection(LinkedList::new));
            returnCardList = (LinkedList<JSONObject>)CollectionUtil.reverse(returnCardList);
            //用户设置的默认卡
            Integer presetIndex = null;
            if (cardType == StaticDataEnum.TIE_CARD_1.getCode()) {
                presetIndex = this.getPresetCard(returnCardList,userId,request);
            }
            if (null != presetIndex){
                //将默认卡放到卡列表的最后
                JSONObject presetCard = returnCardList.get(presetIndex);
                returnCardList.remove(presetCard);
                List<JSONObject> result = new ArrayList<>();
                presetCard.put("preset",1);
                result.add(presetCard);
                result.addAll(returnCardList);

                return result;
            }
        }
        log.info("card list, list:{}", returnCardList);
        return returnCardList;
    }

    /**
     * 获取默认卡index
     * @param jsonObjectList
     * @param userId
     * @param request
     * @return
     * @throws BizException
     */
    private Integer getPresetCard(List<JSONObject> jsonObjectList, Long userId, HttpServletRequest request) throws BizException{
        if (CollectionUtil.isNotEmpty(jsonObjectList)) {
            //如果客户设置了默认卡
            for (int i = 0; i < jsonObjectList.size(); i++) {
                //如果当前卡是默认卡, 则记录卡的index
                JSONObject card = jsonObjectList.get(i);
                Integer preset = card.getInteger("preset");
                if (null != preset && preset == 1){
                    return i;
                }
            }
            return 0;
        } else {
            //如果卡列表为空
            UserDTO updateUser = new UserDTO();
            updateUser.setCardState(0);
            this.updateUser(userId, updateUser, request);
        }
        return null;
    }

    private JSONArray getCardListFromServer(Long userId) {
        try {
            JSONObject data = serverService.getAccountInfo(userId);
            return data.getJSONArray("cardDTOList");
        } catch (Exception e) {
            log.error("调用账户系统获取用户卡列表异常, userId:{},errorMsg:{}", userId, e.getMessage());
        }
        return null;
    }


    private Long getGatewayType() {
        //11月27添加逻辑 获取当前支付渠道,判断 卡是否通过当前渠道验证信息
        JSONObject param = new JSONObject();
        param.put("gatewayType", StaticDataEnum.PAY_TYPE_4.getCode());
        param.put("state", StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(param);
        return gatewayDTO.getType();
    }

    private JSONArray packCardDTOListFromServer(JSONArray cardDTOListFromServer, Long gatewayType) {
        JSONArray cardList = cardDTOListFromServer;
        for (int i = 0; i < cardList.size(); i++) {
            Map<String, Object> params = new HashMap<>(1);
            JSONObject cardInfo = cardList.getJSONObject(i);
            params.put("cardId", cardInfo.getLong("id"));
            TieOnCardFlowDTO tieOnCardFlowDTO = tieOnCardFlowService.findOneTieOnCardFlow(params);
            if (tieOnCardFlowDTO.getId() != null && tieOnCardFlowDTO.getUnBundlingState() == StaticDataEnum.CARD_UNBUNDLING_STATE_3.getCode()) {
                cardList.remove(i);
                i--;
                continue;
            }
            if (null != gatewayType && gatewayType.equals((long) StaticDataEnum.GATEWAY_TYPE_6.getCode())) {
                String splitSignState = cardInfo.getString("splitSignState");
                cardInfo.put("verified", StringUtils.isNotBlank(splitSignState) ? splitSignState : Constant.ZERO);
            } else {
                cardInfo.put("verified", StaticDataEnum.VERIFIED_1.getCode());
            }
        }
        return cardList;
    }

    /**
     * 请求Data系统 获取用户从Illion返回的账户列表数据
     *
     * @param userId
     */
    private JSONArray requestDataSystemIllionCardList(Long userId, HttpServletRequest request) {
        JSONObject param = new JSONObject(Constant.THREE);
        param.put("appId", Constant.PAY_SYSTEM_CODE);
        param.put("value", Constant.DATA_SYSTEM_SERVICE_VALUE);
        param.put("userId", userId);
        JSONArray res = new JSONArray();
        try {
            String url = dataUrl + "/thirdParty/share/";
            String encryptParamStr = EncryptUtil.encrypt(param.toJSONString());
            String resString = HttpClientUtils.post(url, encryptParamStr);
            JSONObject msg = JSONObject.parseObject(resString);
            if (msg.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
                throw new BizException(msg.getString("message"));
            }
            JSONObject data = msg.getJSONObject("data");
            //如果没有数据返回 直接返回空集合 让用户绑卡, 不能影响主流程
            if (null != data) {
                JSONArray cardList = data.getJSONArray("bankAccounts");
                if (null != cardList && cardList.size() > Constant.ZERO) {
                    String bankName = data.getString("bankName");
                    UserDTO userDTO = this.findUserById(userId);
                    if (null == userDTO) {
                        throw new BizException(I18nUtils.get("user.not.found", getLang(request)));
                    }
                    Pattern pattern = Pattern.compile("[A-Za-z0-9]*");
                    cardList.toJavaList(JSONObject.class).forEach(info -> {
                        JSONObject card = new JSONObject();
                        card.put("type", CardEnum.CARD_TYPE_ACCOUNT.getCode());
                        card.put("userId", userId);
                        card.put("phone", userDTO.getPhone());
                        card.put("firstName", userDTO.getUserFirstName());
                        card.put("lastName", userDTO.getUserLastName());
                        card.put("email", userDTO.getEmail());
                        card.put("city", userDTO.getUserCity());
                        card.put("order",0);
                        card.put("preset",0);
                        //illion 返回的账户信息
                        card.put("bankName", bankName);
                        char[] bsbCharArray = info.getString("bsb").trim().toCharArray();
                        StringBuffer bsb = new StringBuffer();
                        for (int i = 0; i < bsbCharArray.length; i++) {
                            if (pattern.matcher(String.valueOf(bsbCharArray[i])).matches()) {
                                bsb.append(bsbCharArray[i]);
                            }
                        }
                        card.put("bsb", bsb.toString());
                        card.put("cardNo", info.getString("accountNumber").trim());
                        card.put("accountName", info.get("accountName"));
                        card.put("name", info.getString("accountHolder"));
                        card.put("needInsert", true);
                        card.put("verified", StaticDataEnum.VERIFIED_0.getCode());
                        res.add(card);
                    });
                }
            }
            return res;
        } catch (Exception e) {
            /**
             * 调用失败 返回空列表 app接收空列表有逻辑提示客户绑卡
             */
            log.error("调用data系统获取卡列表失败, 用户Id:{},errorMsg:{}", userId, e.getMessage());
            return res;
        }
    }

    @Override
    public JSONObject getCardInfo(Long id, HttpServletRequest request) throws Exception {
        JSONObject cardInfo = serverService.getCardInfo(id);
        if (cardInfo != null) {
            cardInfo.remove("customerCcCvc");
        }
        cardInfo = JSONResultHandle.resultHandle(cardInfo);
        return cardInfo;
    }


    @Override
    public void modifyPassword(@NonNull UserDTO userDTO, HttpServletRequest request) throws BizException {
        log.info("full update userDTO:{}", userDTO);
        // 根据电话查询用户
        String headTokenValue = request.getHeader("Authorization");
        Long id = JwtUtils.getId(headTokenValue.replace("Bearer ", ""));
        userDTO.setId(id);
        if (StringUtils.isEmpty(userDTO.getOldPassword())) {
            throw new BizException(I18nUtils.get("old.rule.password", getLang(request)));
        }
        if (StringUtils.isEmpty(userDTO.getNewPassword())) {
            throw new BizException(I18nUtils.get("new.rule.password", getLang(request)));
        }
        if (StringUtils.isEmpty(userDTO.getConfirmPassword())) {
            throw new BizException(I18nUtils.get("confirm.rule.password", getLang(request)));
        }
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("id", userDTO.getId());
        User user = userDAO.selectOne(params);

        if (user == null) {
            throw new BizException(I18nUtils.get("user.rule.userNameNotPresence", getLang(request)));
        }
        if (!userDTO.getNewPassword().equals(userDTO.getConfirmPassword())) {
            throw new BizException(I18nUtils.get("confirmPassword.password.notMatch", getLang(request)));
        }
        if (!Validator.isAdminPassword(userDTO.getNewPassword())) {
            throw new BizException(I18nUtils.get("user.rule.password", getLang(request)));
        }
        if (StaticDataEnum.USER_TYPE_10.getCode() == user.getUserType()) {
            //用户，修改user表密码
            if (!user.getPassword().equals(DigestUtils.md5Hex(userDTO.getOldPassword()))) {
                throw new BizException(I18nUtils.get("old.password.notMatch", getLang(request)));
            }
            if (user.getPassword().equals(DigestUtils.md5Hex(userDTO.getNewPassword()))) {
                throw new BizException(I18nUtils.get("old.password.same", getLang(request)));
            }

            userDTO.setId(user.getId());
            User user1 = new User();
            user1.setId(user.getId());
            // 支付密码MD5加密
            userDTO.setPassword(DigestUtils.md5Hex(userDTO.getNewPassword()));
            updateUser(user.getId(), userDTO, request);
        } else {
            //查询商户登录信息表
            params.clear();
            params.put("email", user.getEmail());
            MerchantLoginDTO merchantLogin = merchantLoginService.findOneMerchantLogin(params);
            if (merchantLogin.getPassword().equals(DigestUtils.md5Hex(userDTO.getNewPassword()))) {
                throw new BizException(I18nUtils.get("old.password.same", getLang(request)));
            }
            if (!userDTO.getNewPassword().equals(userDTO.getConfirmPassword())) {
                throw new BizException(I18nUtils.get("confirmPassword.password.notMatch", getLang(request)));
            }
            merchantLogin.setPassword(DigestUtils.md5Hex(userDTO.getNewPassword()));
            merchantLoginService.updateMerchantLogin(merchantLogin.getId(), merchantLogin, request);
        }

    }

    @Override
    public void checkPayPassword(@NonNull UserDTO userDTO, HttpServletRequest request) throws BizException {
        log.info("full update userDTO:{}", userDTO);
        Long id = getId(request);
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        User user = userDAO.selectOne(params);
        if (!DigestUtils.md5Hex(userDTO.getPayPassword()).equals(user.getPayPassword())) {
            throw new BizException(I18nUtils.get("rule.payPassword.error", getLang(request)));
        }
    }

    private Long getId(HttpServletRequest request) throws BizException {
        String headTokenValue = request.getHeader("Authorization");
        Long id;
        try {
            id = JwtUtils.getId(headTokenValue.replace("Bearer ", ""));
        } catch (Exception e) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        return id;
    }

    @Override
    public void updatePayPassword(@NonNull UserDTO userDTO, HttpServletRequest request) throws BizException {
        log.info("full update userDTO:{}", userDTO);
        Long id = getId(request);
        userDTO.setPayPassword(DigestUtils.md5Hex(userDTO.getPayPassword()));
        updateUser(id, userDTO, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Long investLogin(JSONObject userInfo, HttpServletRequest request) throws Exception {
        userInfo = userInfo.getJSONObject("data");
        //查询输入该用户类型下注册号是否已存在
        Map<String, Object> params = new HashMap<>(1);
        params.put("email", userInfo.getString("email"));
        params.put("phone", userInfo.getString("phone"));
        params.put("userType", StaticDataEnum.USER_TYPE_10.getCode());
        if (findOneUser(params).getId() != null) {
            throw new BizException(I18nUtils.get("user.exist", getLang(request)));
        }

        //2、创建用户
        UserDTO userDTO = JSONObject.parseObject(userInfo.toJSONString(), UserDTO.class);
        userDTO.setPassword(MD5FY.MD5Encode(DEFAULT_PASSWORD));
        userDTO.setPaymentState(StaticDataEnum.STATUS_1.getCode());
        userDTO.setInvestState(StaticDataEnum.STATUS_1.getCode());
        Long userId = userCreate(userDTO, request);

        //创建一条用户失败次数记录
        LoginMissDTO loginMissDTO = new LoginMissDTO();
        loginMissDTO.setUserId(userId);
        loginMissService.saveLoginMiss(loginMissDTO, request);

        // 创建认证记录
        userStepService.createUserStep(userId, request);

        //调用账户系统，开账户
        userInfo.put("userId", userId);
        userInfo.put("accountType", StaticDataEnum.USER_TYPE_10.getCode());
        userInfo.put("channel", StaticDataEnum.ACCOUNT_CHANNEL_0001.getCode());
        serverService.saveAccount(userInfo);

        //风控审核
//        userInfo.put("userId", userId);
//        userInfo.put("checkType", StaticDataEnum.RISK_CHECK_TYPE_1.getCode());
//        String result = riskCheck(userInfo, request).get().getString("result");
//        if (result.equals(StaticDataEnum.RISK_CHECK_STATE_1.getMessage())) {
//            throw new BizException(I18nUtils.get("kyc.failed", getLang(request)));
//        }
        return userId;
    }

    @Override
    public BigDecimal getBalance(@NonNull Long id, Integer accountType) throws Exception {
        if (accountType == null) {
            //账户默认红包子户， 商户默认月子户
            accountType = 0;
        }
        // 用户钱包余额
        String url = accountUrl + "/server/getOneSubAccount";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", id);
        jsonObject.put("type", accountType);
        String data = HttpClientUtils.sendPost(url, JSONObject.toJSONString(jsonObject));
        if (StringUtils.isBlank(data)) {
            throw new BizException();
        }
        JSONObject jsonObject1 = JSONObject.parseObject(data);
        String code = jsonObject1.getString("code");
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(code)) {
            throw new BizException();
        }
        JSONObject jsonObject2 = jsonObject1.getJSONObject("data");
        BigDecimal balance = jsonObject2.getBigDecimal("balance");
        return balance;
    }

    /**
     * 记录邮件
     *
     * @param email
     * @param sendMsg
     * @param sendType
     * @param request
     * @throws BizException
     */
    @Override
    public void saveMailLog(String email, String sendMsg, int sendType, HttpServletRequest request) throws BizException {
        MailLogDTO mailLogDTO = new MailLogDTO();
        mailLogDTO.setAddress(email);
        mailLogDTO.setContent(sendMsg);
        mailLogDTO.setSendType(sendType);
        mailLogService.saveMailLog(mailLogDTO, request);
    }

    @Override
    public List<JSONObject> findMerchantStaff(Long merchantId, HttpServletRequest request) throws BizException {
        Map<String, Object> params = new HashMap<>(1);
        params.put("merchantId", merchantId);
        params.put("role", StaticDataEnum.MERCHANT_ROLE_TYPE_0.getCode());
        List<UserDTO> list = find(params, null, null);
        List<JSONObject> returnData = new ArrayList<>(1);
        list.stream().forEach(userDTO -> {
            returnData.add(JSONResultHandle.resultHandle(userDTO, UserDTO.class));
        });
        return returnData;
    }

    @Override
    public void resetMerchantStaffInfo(JSONObject requestInfo, HttpServletRequest request) throws BizException {
        Long userId = requestInfo.getLong("userId");
        UserDTO userDTO = findUserById(userId);
        if (userDTO == null || userDTO.getId() == null || userDTO.getStatus() == 0) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }
        //获取登录信息表
        Map<String, Object> map = new HashMap<>(2);
        map.put("email", userDTO.getEmail());
        map.put("userType", StaticDataEnum.USER_TYPE_20.getCode());
        MerchantLoginDTO merchantLogin = merchantLoginService.findOneMerchantLogin(map);

        if (requestInfo.getInteger("type").intValue() == StaticDataEnum.RESET_PASSWORD.getCode()) {
            merchantLogin.setPassword(MD5FY.MD5Encode("admin123"));
            merchantLoginService.updateMerchantLogin(merchantLogin.getId(), merchantLogin, request);
//            userDTO.setPassword(MD5FY.MD5Encode("123456"));
//            userService.updateUser(userId, userDTO, request);
        } else {
            map.clear();
            map.put("oldEmail", userDTO.getEmail());
            map.put("userType", StaticDataEnum.USER_TYPE_20.getCode());
            map.put("email", requestInfo.getString("email"));
            merchantLogin.setEmail(requestInfo.getString("email"));
            merchantLoginService.updateMerchantLogin(merchantLogin.getId(), merchantLogin, request);
            updateEmail(map, request);
//            userDTO.setPhone(requestInfo.getString("phone"));
//            userService.updateUser(userId, userDTO, request);
        }
    }

    @Override
    public void location(JSONObject data, HttpServletRequest request) throws Exception {
        Integer type = data.getInteger("userType");
        String lat = data.getString("lat");
        String lng = data.getString("lng");
        String pushToken = data.getString("pushToken");
        String imeiNo = data.getString("imeiNo");
        if (type == StaticDataEnum.USER_TYPE_10.getCode()) {
            //更新用户定位
            UserDTO userDTO = findUserById(data.getLong("userId"));
            userDTO.setLat(lat);
            userDTO.setLng(lng);
            userDTO.setPushToken(pushToken);
            userDTO.setImeiNo(imeiNo);
            updateUser(userDTO.getId(), userDTO, request);
        } else {
            //更新商户定位
            MerchantDTO merchantDTO = merchantService.findMerchantById(data.getLong("merchantId"));
            merchantDTO.setLat(lat);
            merchantDTO.setLng(lng);
            merchantService.updateMerchant(merchantDTO.getId(), merchantDTO, request);
        }
    }

    @Override
    public Long userCreate(UserDTO userDTO, HttpServletRequest request) throws BizException {
        //2、创建用户
        User user = BeanUtil.copyProperties(userDTO, new User());
        user = (User) this.packAddBaseProps(user, request);
        log.info("save User:{}", user);
        if (userDAO.insert(user) != 1) {
            log.error("insert error, data:{}", user);
            throw new BizException("Insert user Error!");
        }

        return user.getId();
    }

    /**
     * 创建商户权限
     *
     * @param userId
     * @param request
     */
    @Override
    public List<UserAction> createMerchantUserAction(Long userId, HttpServletRequest request) throws BizException {
        Map<String, Object> params = new HashMap<>(1);
        params.put("appAction", StaticDataEnum.ACTION_TYPE_1.getCode());
        List<ActionDTO> actionDTOList = actionService.find(params, null, null);
        List<UserAction> userActionList = new ArrayList<>(1);
        actionDTOList.stream().forEach(actionDTO -> {
            UserAction userAction = new UserAction();
            userAction.setUserId(userId);
            userAction.setActionId(actionDTO.getId());
            userAction = (UserAction) this.packAddBaseProps(userAction, request);
            userActionList.add(userAction);
        });
        userActionService.saveUserActionList(userActionList, request);
        //设置成功后，将权限放入缓存中
        Map<String, Object> userAction = new HashMap<>(1);
        userAction.put("action", userActionList);
        redisUtils.hmset(userId + "_action", userAction);
        return userActionList;
    }
    @Async("batchTaskExecutor")
    @Override
    public void batchSendMessage(List<UserDTO> list,MessageBatchSendLogDTO messageBatchSendLogDTO) {
        Integer sendType = messageBatchSendLogDTO.getSendType();
        //SEND_TYPE_APP_MESSAGE(1,"app消息"),
        if (sendType==StaticDataEnum.SEND_TYPE_APP_MESSAGE.getCode()){
            String title = messageBatchSendLogDTO.getTitle();
            String content = messageBatchSendLogDTO.getContent();
            List<Notice> noticeList=new ArrayList<>();
            for (UserDTO userDTO : list) {
                Notice notice = new Notice();
                /**
                 * 0：未读 1：已读
                 */
                notice.setIsRead(0);
                notice.setStatus(1);
                notice.setId(SnowflakeUtil.generateId());
                notice.setContent(content);
                notice.setUserId(userDTO.getId());
                notice.setTitle(title);
                notice.setCreatedBy(userDTO.getId());
                notice.setCreatedDate(System.currentTimeMillis());
                noticeList.add(notice);
            }
            try{
                noticeService.saveNoticeListNew(noticeList);
                JSONObject param =new JSONObject(3);
                param.put("sendSuccessNumber",list.size());
                param.put("id",messageBatchSendLogDTO.getId());
                messageBatchSendLogService.updateBatchNumber(param);
            }catch (Exception e){
                try{
                    log.error("发送站内信消息第一次异常,e:{},批量记录id:{},",e,messageBatchSendLogDTO.getId());
                    noticeService.saveNoticeListNew(noticeList);
                    JSONObject param =new JSONObject(3);
                    param.put("sendSuccessNumber",list.size());
                    param.put("id",messageBatchSendLogDTO.getId());
                    messageBatchSendLogService.updateBatchNumber(param);
                }catch (Exception ex){
                    log.error("发送站内信消息第二次异常,e:{},批量记录id:{},",ex,messageBatchSendLogDTO.getId());
                    // todo 记录失败
                    JSONObject param =new JSONObject(3);
                    param.put("sendSuccessNumber",0);
                    param.put("id",messageBatchSendLogDTO.getId());
                    messageBatchSendLogService.updateBatchNumber(param);
                }
            }
        }
    }




    @Override
    public String templateContentReplace(Object[] replaceContentParams, String content) {
        if (replaceContentParams != null && replaceContentParams.length > 0) {
            content = MessageFormat.format(content, replaceContentParams);
        }

        return content;
    }

    /**
     * 向旧手机发送验证码(修改手机号)
     *
     * @param phone
     * @param sendNode
     * @param userType
     * @param request
     * @throws Exception
     */
    @Override
    public void sendSecuritySMSToOld(String phone, String sendNode, Integer userType, HttpServletRequest request) throws Exception {
        log.info("send security to old phone, phone:{}", phone);
        //校验手机号
        if (!RegexUtils.checkPhone(phone)) {
            throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
        }
        //1:验证手机号是否注册,
        Map params = new HashMap(3);
        params.put("phone", phone);
        params.put("userType", userType);
        UserDTO userDTO = userDAO.selectOneDTO(params);
        if (userDTO == null || userDTO.getId() == null) {
            throw new BizException(I18nUtils.get("account.error", getLang(request)));
        }
        //2:发送验证码
        Integer securityCode = (int) ((Math.random() * 9 + 1) * 100000);
        if (!redisUtils.set(phone + "_" + userType + "_" + StaticDataEnum.SEND_NODE_15.getCode(), securityCode, 15 * 60)) {
            throw new BizException(I18nUtils.get("SMS.send.failed", getLang(request)));
        }
        //获取邮件模板
        MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(sendNode);
        //判断手机区号
        String phoneCode = new StringBuilder(phone).substring(0, 2);
        if (phoneCode.equals(StaticDataEnum.CHN.getMessage())) {
            JSONArray phoneArray = new JSONArray();
            phoneArray.add(phone);
            JSONObject messageParam = new JSONObject();
            messageParam.put("code", securityCode);
            aliyunSmsService.sendChinaSms(phone, mailTemplateDTO.getAliCode(), messageParam);
        } else {
            //设置模板参数
            String[] param = {securityCode + ""};
            //邮件内容
            String sendMsg = templateContentReplace(param, mailTemplateDTO.getEnSendContent());
            aliyunSmsService.sendInternationalSms(phone, sendMsg);
        }
    }

    /**
     * 验证旧手机验证码
     *
     * @param oldPhone
     * @param signCode
     * @param userType
     * @param request
     * @throws Exception
     */
    @Override
    public void checkOldPhoneCode(String oldPhone, Integer signCode, Integer userType, HttpServletRequest request) throws Exception {
        Object securityCode = redisUtils.get(oldPhone + "_" + userType + "_" + StaticDataEnum.SEND_NODE_15.getCode());
        if (securityCode == null) {
            throw new BizException(I18nUtils.get("verification.code.expired", getLang(request)));
        }
        if (!signCode.toString().equals(securityCode.toString())) {
            throw new BizException(I18nUtils.get("incorrect.verification.code", getLang(request)));
        }
    }

    /**
     * 向新手机发送验证码（修改手机号）
     *
     * @param phone
     * @param sendNode
     * @param userType
     * @param request
     * @throws Exception
     */
    @Override
    public void sendSecuritySMSToNew(String phone, String sendNode, Integer userType, HttpServletRequest request) throws Exception {
        log.info("send security to new phone, phone:{}", phone);
        // 查询新手机是否存在用户
        Map<String, Object> params = new HashMap<>(1);
        params.put("phone", phone);
        params.put("userType", userType);
        UserDTO isExistUserDTO = userDAO.selectOneDTO(params);
        if (isExistUserDTO != null) {
            throw new BizException(I18nUtils.get("user.exist", getLang(request)));
        }
        //校验手机号
        if (!RegexUtils.checkPhone(phone)) {
            throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
        }
        Integer securityCode = (int) ((Math.random() * 9 + 1) * 100000);
        if (!redisUtils.set(phone + "_" + userType + "_" + StaticDataEnum.SEND_NODE_16.getCode(), securityCode, 15 * 60)) {
            throw new BizException(I18nUtils.get("SMS.send.failed", getLang(request)));
        }
        //获取邮件模板
        MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(sendNode);
        //判断手机区号
        String phoneCode = new StringBuilder(phone).substring(0, 2);
        if (phoneCode.equals(StaticDataEnum.CHN.getMessage())) {
            JSONArray phoneArray = new JSONArray();
            phoneArray.add(phone);
            JSONObject messageParam = new JSONObject();
            messageParam.put("code", securityCode);
            aliyunSmsService.sendChinaSms(phone, mailTemplateDTO.getAliCode(), messageParam);
        } else {
            //设置模板参数
            String[] param = {securityCode + ""};
            //邮件内容
            String sendMsg = templateContentReplace(param, mailTemplateDTO.getEnSendContent());
            aliyunSmsService.sendInternationalSms(phone, sendMsg);
        }
    }

    /**
     * 用户修改手机号
     *
     * @param phone
     * @param signCode
     * @param userType
     * @param request
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updatePhone(String phone, String oldPhone, Integer signCode, Integer userType, HttpServletRequest request) throws Exception {
        log.info("update user phone, new phone:{}, old phone:{}", phone, oldPhone);
        //校验手机号
        if (!RegexUtils.checkPhone(phone, oldPhone)) {
            throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
        }
        //1: 查询用户
        Map params = new HashMap(3);
        params.put("phone", oldPhone);
        params.put("userType", userType);
        UserDTO userDTO = userDAO.selectOneDTO(params);
        if (userDTO == null) {
            throw new BizException(I18nUtils.get("account.error", getLang(request)));
        }
        // 2: 验证验证码校验
        Object securityCode = redisUtils.get(phone + "_" + userDTO.getUserType() + "_" + StaticDataEnum.SEND_NODE_16.getCode());
        if (securityCode == null) {
            throw new BizException(I18nUtils.get("verification.code.expired", getLang(request)));
        }
        if (!signCode.toString().equals(securityCode.toString())) {
            throw new BizException(I18nUtils.get("incorrect.verification.code", getLang(request)));
        }
        // 4：修改手机  params.put("phone", oldPhone);
        params.clear();
        params.put("id", userDTO.getId());
        params.put("phone", phone);
        if (userDAO.updatePhone(params) != 1) {
            throw new BizException(I18nUtils.get("saving.exception", getLang(request)));
        }
        //重置当前用户token
        String token = JwtUtils.signApp(userDTO.getId(), phone, null, userType, redisUtils);

        //3:同步信息
        JSONObject updateInfo = new JSONObject(2);
        updateInfo.put("userId", userDTO.getId());
        updateInfo.put("phone", phone);
        serverService.updatePhone(updateInfo);

        return token;
    }

    /**
     * 查询用户PINnumber
     *
     * @param id      用户表主键id
     * @param request
     * @return
     */
    @Override
    public String queryPinNumber(Long id, HttpServletRequest request) throws BizException {
        String result = "";
        String pinNumber = userDAO.queryPinNumber(id);
        if (pinNumber != null) {
            result = pinNumber;
        }
        return result;
    }

    /**
     * 修改pinnumber
     *
     * @param id
     * @param request
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePinNumber(Long id, String pinNumber, HttpServletRequest request) throws Exception {
        log.info("full update user pinNumber id:{}", id);
        if ("0".equals(pinNumber)) {
            pinNumber = "";
        } else {
            pinNumber = MD5FY.MD5Encode(pinNumber);
        }
        int cnt = userDAO.updatePinNumber(id, pinNumber, getUserId(request), System.currentTimeMillis());
        if (cnt != 1) {
            log.error("update error, data id:{},pinNumber:{}", id, pinNumber);
            throw new BizException("update pinNumber Error!");
        }
    }

    /**
     * 校验pinNumber
     *
     * @param id
     * @param pinNumber
     * @param request
     * @throws Exception
     */
    @Override
    public void checkPinNumber(Long id, String pinNumber, HttpServletRequest request) throws Exception {
        String savePin = userDAO.queryPinNumber(id);
        if (StringUtils.isEmpty(savePin)) {
            throw new BizException(I18nUtils.get("pinNumber.query.empty", getLang(request)));
        } else {
            String valiStr = savePin;
            if (!MD5FY.MD5Encode(pinNumber).equals(valiStr)) {
                throw new BizException(I18nUtils.get("pinNumber.query.notEqual", getLang(request)));
            }
        }
    }

    @Override
    public JSONObject addMerchant(JSONObject data, HttpServletRequest request) throws Exception {
        //二维码
        String code = data.getString("code");
        //登录用户名
        String userName = data.getString("loginName");

        // 查询二维码是否存在
        QrcodeInfoDTO qrcodeInfoDTO = null;

        if (!StringUtils.isEmpty(code)) {
            Map<String, Object> params = new HashMap<>(3);
            params.put("code", code);
            params.put("state", StaticDataEnum.QRCODE_STATE_0.getCode());
            qrcodeInfoDTO = qrcodeInfoService.findOneQrcodeInfo(params);
            if (qrcodeInfoDTO.getId() == null) {
                throw new BizException(I18nUtils.get("qrcode.not.exist", getLang(request)));
            }
        }

        Map<String, Object> params = new HashMap<>(16);
        params.put("email", userName);
        //查询商户列表
        List<MerchantDTO> list = merchantService.findMerchantLogInList(params);
        for (MerchantDTO merchantDTO : list) {
            if (merchantDTO.getState().intValue() == StaticDataEnum.MERCHANT_STATE_0.getCode()) {
                throw new BizException(I18nUtils.get("have.no.submit.merchant", getLang(request)));
            }
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setCode(code);
        userDTO.setEmail(userName);
        userDTO.setUserType(StaticDataEnum.USER_TYPE_20.getCode());
        userDTO.setRole(StaticDataEnum.MERCHANT_ROLE_TYPE_1.getCode());
        // 创建用户
        Long userId = userCreate(userDTO, request);
        userDTO.setId(userId);
        // 调用账户系统，开基本账户

        JSONObject accountInfo = new JSONObject();
        accountInfo.put("userId", userId);
        accountInfo.put("phone", userDTO.getPhone());
        accountInfo.put("email", userDTO.getEmail());
        accountInfo.put("accountType", userDTO.getUserType());
        accountInfo.put("channel", StaticDataEnum.ACCOUNT_CHANNEL_0001.getCode());
        serverService.saveAccount(accountInfo);

        // 调用账户系统，开整体销售账户
        accountInfo.clear();
        accountInfo.put("userId", userId);
        accountInfo.put("type", 1);
        serverService.createSubAccount(accountInfo);

        // 商户插入一条空数据
        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setUserId(userId);
        merchantDTO.setLat(data.getString("lat"));
        merchantDTO.setLng(data.getString("lng"));
        Long merId = merchantService.saveMerchant(merchantDTO, request);
        MerchantDTO newMerchant = merchantService.findMerchantById(merId);
        userDTO.setMerchantId(merId);
        updateUser(userId, userDTO, request);

        // 若二维码不存在则不绑定
        if (qrcodeInfoDTO != null
                && qrcodeInfoDTO.getId() != null
                && qrcodeInfoDTO.getState().intValue() != StaticDataEnum.QRCODE_STATE_1.getCode()) {
            qrcodeInfoDTO.setUserId(userId);
            qrcodeInfoDTO.setMerchantId(merId);
            qrcodeInfoDTO.setCorrelationTime(System.currentTimeMillis());
            qrcodeInfoDTO.setState(StaticDataEnum.QRCODE_STATE_1.getCode());
            qrcodeInfoService.updateQrcodeInfo(qrcodeInfoDTO.getId(), qrcodeInfoDTO, request);
        }

        //创建用户权限
        List<UserAction> actions = createMerchantUserAction(userDTO.getId(), request);

        //生成token值
        String token = JwtUtils.signApp(userDTO.getId(), userName, null, userDTO.getUserType(), redisUtils);
        JSONObject msg = new JSONObject();
        msg.put("Authorization", token);
        msg.put("userInfo", userDTO);
        msg.put("firstName", newMerchant.getLiaisonFirstName());
        msg.put("middleName", newMerchant.getLiaisonMiddleName());
        msg.put("lastName", newMerchant.getLiaisonLastName());
        msg.put("merchantId", newMerchant.getId());
        msg.put("isAvailable", newMerchant.getIsAvailable());
        msg.put("merchantState", newMerchant.getState());
        msg.put("actions", actions);
        msg = JSONResultHandle.resultHandle(msg);
        return msg;
    }

    @Override
    public JSONObject merchantLogin(JSONObject data, HttpServletRequest request) throws BizException {
        //查询登录用户
        Map<String, Object> params = new HashMap<>(3);
        params.put("email", data.getString("loginName"));
        params.put("merchantId", data.getString("merchantId"));
        UserDTO userDTO = findOneUser(params);
        String token = JwtUtils.signApp(userDTO.getId(), userDTO.getEmail(), null, StaticDataEnum.USER_TYPE_20.getCode(), redisUtils);
        String firstName = null;
        String middleName = null;
        String lastName = null;
        Long merchantId = null;
        Integer isAvailable = null;
        Integer merchantState = null;
        MerchantDTO merchantDTO = merchantService.findMerchantById(data.getLong("merchantId"));
        log.info("merchant info , user data:{}", merchantDTO);
        if (merchantDTO.getIsAvailable() == StaticDataEnum.AVAILABLE_0.getCode() && merchantDTO.getState() == StaticDataEnum.MERCHANT_STATE_1.getCode()) {
            throw new BizException(I18nUtils.get("merchant.forbidden", getLang(request)));
        }

        params.clear();
        params.put("merchantId", data.getString("merchantId"));
        QrcodeInfoDTO qrcodeInfoDTO = qrcodeInfoService.findOneQrcodeInfo(params);

        // 放置返回信息
        List<UserAction> actions = userActionService.getUserAction(userDTO.getId(), request);
        merchantId = merchantDTO.getId();
        firstName = merchantDTO.getLiaisonFirstName();
        middleName = merchantDTO.getLiaisonMiddleName();
        lastName = merchantDTO.getLiaisonLastName();
        isAvailable = merchantDTO.getIsAvailable();
        merchantState = merchantDTO.getState();
        JSONObject msg = new JSONObject();
        msg.put("Authorization", token);
        msg.put("userInfo", userDTO);
        msg.put("firstName", firstName);
        msg.put("middleName", middleName);
        msg.put("lastName", lastName);
        msg.put("merchantId", merchantId);
        msg.put("isAvailable", isAvailable);
        msg.put("merchantState", merchantState);
        msg.put("wholeSaleApproveState", merchantDTO.getWholeSaleApproveState());
        msg.put("actions", actions);
        msg.put("businessPhone", merchantDTO.getBusinessPhone());
        msg.put("qrCode", qrcodeInfoDTO.getCode());
        msg = JSONResultHandle.resultHandle(msg);

        // 存储pushToken
//        userDTO = findUserById(merchantDTO.getUserId());
        userDTO.setPushToken(data.getString("pushToken"));
        updateUser(userDTO.getId(), userDTO, request);
        return msg;
    }

    @Override
    public int updateEmail(Map<String, Object> map, HttpServletRequest request) {
        if (request != null) {
            Long id = this.getUserId(request);
            String ip = this.getIp(request);
            map.put("modifiedBy", id);
            map.put("ip", ip);
        }
        map.put("modifiedDate", System.currentTimeMillis());
        return userDAO.updateEmail(map);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @Async("taskExecutor")
    public Future<JSONObject> riskCheck(JSONObject data, HttpServletRequest request) throws Exception {
        JSONObject returnData = new JSONObject();
        List<AppAboutUsDTO> appAboutUsDTOS = appAboutUsService.find(null, null, null);
        //查询用户信息
        UserDTO userDTO = findUserById(data.getLong("userId"));
        if (userDTO.getInstallmentState().equals(StaticDataEnum.USER_BUSINESS_1.getCode())) {
            throw new BizException(I18nUtils.get("kyc.has.passed", getLang(request)));
        }
        //判断kyc次数
        int chance;
        long userKycChancePeriod = 86400L;
        String userKycKey = userDTO.getId() + "_kyc_" + LocalDate.now();
        if (redisUtils.hasKey(userKycKey)) {
            chance = (int) redisUtils.get(userKycKey);
            if (chance == 0) {
                returnData.put("chance", chance);
                returnData.put("phone", appAboutUsDTOS.get(0).getPhone());
                returnData.put("message", I18nUtils.get("kyc.no.chance", getLang(request)));
                return new AsyncResult<>(returnData);
            }
        } else {
            chance = KYC_CHANCE;
            redisUtils.set(userKycKey, chance, userKycChancePeriod);
        }
        //信息补充
        data.put("phone", userDTO.getPhone());
        //请求数据拼装
        JSONObject requestInfo = getRiskRequestInfo(data, userDTO);
        userDTO.setEmail(data.getString("email"));
        userDTO.setUserFirstName(data.getString("userFirstName"));
        userDTO.setUserLastName(data.getString("userLastName"));
        // kyc风控校验
        JSONObject riskResult = kycRisk(requestInfo, request);
        log.info("risk result : {}", riskResult);
        if (ErrorCodeEnum.DATA_SYSTEM_FAILED.getCode().equals(riskResult.getString("code"))
                || ErrorCodeEnum.FAIL_CODE.getCode().equals(riskResult.getString("code"))) {
            throw new BizException(I18nUtils.get("risk.failed", getLang(request)));
        }
        ;
        JSONObject riskResultData = JSONObject.parseObject(EncryptUtil.decrypt(riskResult.getString("data"), EncryptUtil.aesKey, EncryptUtil.aesIv));
        log.info("risk result data : {}", riskResultData);
        // 风控审核批次
        String riskBatch = riskResultData.getString("batchNo");
        String result = riskResultData.getJSONObject("rule").getString("decision");
        if (StaticDataEnum.RISK_CHECK_STATE_0.getMessage().equals(result)) {
            userDTO.setInstallmentState(StaticDataEnum.USER_BUSINESS_5.getCode());
            //同步用户信息
            serverService.infoSupplement(data, request);
            log.info("kyc update user info, user:{}", userDTO);
            // 更新用户状态
            updateUser(userDTO.getId(), userDTO, request);
            // 记录阶段日志
            userStepService.userStepModifyAndUserStepLogSave(userDTO.getId(), StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_11.getCode(), null, riskBatch, data.toJSONString(), request);
            // 清空kyc机会
            redisUtils.del(userKycKey);
            sendMessage(userDTO, StaticDataEnum.SEND_NODE_4.getCode(), request);
        } else if (StaticDataEnum.RISK_CHECK_STATE_1.getMessage().equals(result)) {
            log.info("kyc update user info, user:{}", userDTO);
            // 失败设置kyc次数
            chance = chance - 1;
            redisUtils.set(userKycKey, chance, userKycChancePeriod);
            // 记录阶段日志
            userStepService.userStepModifyAndUserStepLogSave(userDTO.getId(), StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_12.getCode(), null, riskBatch, data.toJSONString(), request);
            sendMessage(userDTO, StaticDataEnum.SEND_NODE_5.getCode(), request);
        } else if (StaticDataEnum.RISK_CHECK_STATE_2.getMessage().equals(result)) {
            // 创建理财审核记录
            RiskApproveLogDTO riskApproveLogDTO = new RiskApproveLogDTO();
            riskApproveLogDTO.setUserId(data.getLong("userId"));
            riskApproveLogDTO.setData(data.toJSONString());
            riskApproveLogService.saveRiskApproveLog(riskApproveLogDTO, request);
            userDTO.setInstallmentState(StaticDataEnum.USER_BUSINESS_3.getCode());
            // 同步用户信息
            log.info("kyc update user info, user:{}", userDTO);
            serverService.infoSupplement(data, request);
            updateUser(userDTO.getId(), userDTO, request);
            // 记录阶段日志
            userStepService.userStepModifyAndUserStepLogSave(userDTO.getId(), StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_13.getCode(), null, riskBatch, data.toJSONString(), request);
            // 清空kyc机会
            redisUtils.del(userKycKey);
            sendMessage(userDTO, StaticDataEnum.SEND_NODE_4.getCode(), request);
        }
        //返回结果
        returnData.put("chance", chance);
        returnData.put("phone", appAboutUsDTOS.get(0).getPhone());
        if (result.equals(StaticDataEnum.RISK_CHECK_STATE_0.getMessage())) {
            returnData.put("message", I18nUtils.get("kyc.success", getLang(request)));
        } else {
            if (chance != 0) {
                returnData.put("message", I18nUtils.get("kyc.chance", getLang(request)));
            } else {
                returnData.put("message", I18nUtils.get("kyc.no.chance", getLang(request)));
            }
        }
        returnData.put("result", result);
        return new AsyncResult<>(returnData);
    }

    private JSONObject getRiskRequestInfo(JSONObject data, UserDTO userDTO) throws Exception {
        JSONObject requestInfo = new JSONObject();
        requestInfo.put("userId", data.getLong("userId"));
        requestInfo.put("imeiNo", userDTO.getImeiNo());
        requestInfo.put("fullName", data.getString("fullName"));
        // 上送给kyc订单号
        requestInfo.put("kycNo",data.getLong("kycNo"));
        //查询国籍
        Map<String, Object> map = new HashMap<>();
        map.put("code", "county");
        map.put("value", data.getInteger("userCitizenship"));
        StaticDataDTO StaticDataDTO = staticDataService.findOneStaticData(map);
        //获取ISO编码
        CountryIsoDTO countryIsoDTO = tieOnCardFlowService.selectCountryIso(StaticDataDTO.getEnName());
        Date date = new SimpleDateFormat("dd-MM-yyyy").parse(data.getString("birth"));
        requestInfo.put("birth", new SimpleDateFormat("yyyy-MM-dd").format(date));
        requestInfo.put("sex", data.getInteger("sex"));
        if (StringUtils.isNotEmpty(data.getString("medicare"))) {
            requestInfo = getMedicareReq(requestInfo, data);
        } else if (StringUtils.isNotEmpty(data.getString("driverLicence"))) {
            requestInfo = getDriverLicenceReq(requestInfo, data);
        } else if (StringUtils.isNotEmpty(data.getString("passport"))) {
            requestInfo = getPassportReq(requestInfo, data, countryIsoDTO);
        } else if (StringUtils.isNotEmpty(data.getString("idNo"))) {
            requestInfo = getIdentityCardReq(requestInfo, data);
        }
        return requestInfo;
    }

    private JSONObject getIdentityCardReq(JSONObject requestInfo, JSONObject data) throws Exception {
        requestInfo.put("userFirstName", data.getString("userFirstName"));
        requestInfo.put("userMiddleName", data.getString("userMiddleName"));
        requestInfo.put("userLastName", data.getString("userLastName"));
        requestInfo.put("idNo", data.getString("idNo"));
        requestInfo.put("dataSources", 1);
        return requestInfo;
    }

    private JSONObject getPassportReq(JSONObject requestInfo, JSONObject data, CountryIsoDTO countryIsoDTO) throws Exception {
        requestInfo.put("userFirstName", data.getString("userFirstName"));
        requestInfo.put("userMiddleName", data.getString("userMiddleName"));
        requestInfo.put("userLastName", data.getString("userLastName"));
        requestInfo.put("passport", data.getString("passport"));
        if ("AUS".equals(countryIsoDTO.getThreeLettersCoding())) {
            //澳洲护照
            requestInfo.put("dataSources", 0);
        } else {
            //其他
            requestInfo.put("passportCountry", countryIsoDTO.getThreeLettersCoding());
            requestInfo.put("dataSources", 2);
        }
        return requestInfo;
    }

    private JSONObject getDriverLicenceReq(JSONObject requestInfo, JSONObject data) throws Exception {
        requestInfo.put("userFirstName", data.getString("userFirstName"));
        requestInfo.put("userMiddleName", data.getString("userMiddleName"));
        requestInfo.put("userLastName", data.getString("userLastName"));
        requestInfo.put("driverLicence", data.getString("driverLicence"));
        //驾驶证州
        Map<String, Object> params = new HashMap<>(2);
        params.put("code", "merchantState");
        params.put("value", data.getString("driverLicenceState"));
        StaticDataDTO state = staticDataService.findOneStaticData(params);
        requestInfo.put("driverLicenceState", state.getEnName());
        requestInfo.put("dataSources", 0);
        return requestInfo;
    }

    private JSONObject getMedicareReq(JSONObject requestInfo, JSONObject data) throws Exception {
        //数据校验
        requestInfo.put("userFirstName", data.getString("userFirstName"));
        requestInfo.put("userMiddleName", data.getString("userMiddleName"));
        requestInfo.put("userLastName", data.getString("userLastName"));
        Date date = new SimpleDateFormat("dd-MM-yyyy").parse(data.getString("birth"));
        requestInfo.put("birth", new SimpleDateFormat("yyyy-MM-dd").format(date));
        requestInfo.put("sex", data.getInteger("sex"));
        requestInfo.put("medicare", data.getString("medicare"));
        requestInfo.put("medicareType", data.getString("medicareType"));
        requestInfo.put("medicareRefNo", data.getString("medicareRefNo"));
        //医保卡到期日期格式转换
        SimpleDateFormat sdf = new SimpleDateFormat("MM-yyyy");
        Date turnDate = sdf.parse(data.getString("medicareIndate"));
        requestInfo.put("medicareIndate", new SimpleDateFormat("yyyy-MM").format(turnDate));
        requestInfo.put("dataSources", 0);
        return requestInfo;
    }

    @Override
    public JSONObject kycRisk(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        //设置风控产品
        requestInfo.put("isWatchlist", true);
        requestInfo.put("riskProductId", RISK_PRODUCT_ID);
        requestInfo.put("eventId", WATCH_LIST_EVENT_ID);
        log.info("risk request,data:{}", requestInfo);
        //发送数据
        JSONObject result = null;
        try {
            result = JSONObject.parseObject(HttpClientUtils.post(riskUrl, EncryptUtil.encrypt(requestInfo.toJSONString())));
        } catch (Exception e) {
            log.info("risk request fail,data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            throw new BizException(I18nUtils.get("risk.failed", request.getLocale()));
        }
        return result;
    }

    @Override
    public JSONObject getAgreementState(Long userId) throws Exception {
        UserDTO userDTO = findUserById(userId);
        JSONObject result = new JSONObject();
        result.put("isInvestAgree", userDTO.getIsInvestAgree());
        result.put("isCreditAgree", userDTO.getIsCreditAgree());
        result.put("investAgreement", investAgreement);
        result.put("creditAgreement", creditAgreement);
        return result;
    }

    @Override
    public void updateAgreementState(JSONObject data, HttpServletRequest request) throws Exception {
        UserDTO userDTO = findUserById(data.getLong("userId"));
        if (data.getInteger("isInvestAgree") != null) {
            userDTO.setIsInvestAgree(data.getInteger("isInvestAgree"));
        }
        if (data.getInteger("isCreditAgree") != null) {
            userDTO.setIsCreditAgree(data.getInteger("isCreditAgree"));
        }
        updateUser(userDTO.getId(), userDTO, request);
    }

    @Override
    public void riskCheckParamsCheck(JSONObject data, HttpServletRequest request) throws Exception {
        //数据校验
        if (StringUtils.isEmpty(data.getString("userId"))) {
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        // 地址信息
        if(StringUtils.isNotEmpty(data.getString("isAddAddress"))){
            if (StringUtils.isAllBlank(data.getString("address"),data.getString("city"))){
                throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
            }
            // 州 城市
            if (data.getInteger("postcode")==null||data.getInteger("state")==null){
                throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
            }
        }
        /*if (StringUtils.isEmpty(data.getString("userFirstName")) || data.getString("userFirstName").length() > Validator.TEXT_LENGTH_100||!Validator.isName(data.getString("userFirstName"))) {
            throw new BizException(I18nUtils.get("name.length.error", getLang(request)));
        }*/

        if (StringUtils.isEmpty(data.getString("userFirstName")) || data.getString("userFirstName").length() > Validator.TEXT_LENGTH_100) {
            throw new BizException(I18nUtils.get("name.length.error", getLang(request)));
        }

        if (StringUtils.isEmpty(data.getString("userLastName")) || data.getString("userLastName").length() > Validator.TEXT_LENGTH_100) {
            throw new BizException(I18nUtils.get("name.length.error", getLang(request)));
        }
        /*if (StringUtils.isEmpty(data.getString("userLastName")) || data.getString("userLastName").length() > Validator.TEXT_LENGTH_100||!Validator.isName(data.getString("userLastName"))) {
            throw new BizException(I18nUtils.get("name.length.error", getLang(request)));
        }*/
        if (StringUtils.isNotEmpty(data.getString("userMiddleName"))) {
            if (data.getString("userMiddleName").length() > Validator.TEXT_LENGTH_100){
                throw new BizException(I18nUtils.get("name.length.error", getLang(request)));
            }
        }
        /*if (StringUtils.isNotEmpty(data.getString("userMiddleName"))) {
            if (data.getString("userMiddleName").length() > Validator.TEXT_LENGTH_100||!Validator.isName(data.getString("userMiddleName"))){
                throw new BizException(I18nUtils.get("name.length.error", getLang(request)));
            }
        }*/
//        if (data.getString("email") != null && data.getString("email").length() > Validator.TEXT_LENGTH_100) {
//            throw new BizException(I18nUtils.get("email.length", getLang(request)));
//        }
        if (StringUtils.isNotEmpty(data.getString("medicare"))) {
            if (data.getString("medicare").length() > Validator.TEXT_LENGTH_10) {
                throw new BizException(I18nUtils.get("medical.length", getLang(request)));
            }
            if (StringUtils.isEmpty(data.getString("medicareRefNo")) || data.getString("medicareRefNo").length() > Validator.TEXT_LENGTH_3) {
                throw new BizException(I18nUtils.get("medical.ref.no.length", getLang(request)));
            }
        } else if (StringUtils.isNotEmpty(data.getString("driverLicence"))) {
            if (data.getString("driverLicence").length() > Validator.DRIVER_LICENSE_LENGTH) {
                throw new BizException(I18nUtils.get("driver.license.length", getLang(request)));
            }
        } else if (StringUtils.isNotEmpty(data.getString("passport"))) {
            if (data.getString("passport").length() > Validator.PASSPORT_LENGTH) {
                throw new BizException(I18nUtils.get("passport.length", getLang(request)));
            }
        } else if (StringUtils.isNotEmpty(data.getString("idNo"))) {
            if (StringUtils.isEmpty(data.getString("idNo")) || data.getString("idNo").length() != Validator.ID_NO) {
                throw new BizException(I18nUtils.get("id.length", getLang(request)));
            }
        }

        //性别不能为空
        if (null == data.getInteger("sex")) {
            throw new BizException(I18nUtils.get("sex.not.empty", getLang(request)));
        }
    }

    @Async("taskExecutor")
    public void sendLoginMessage(UserDTO userDTO, HttpServletRequest request) throws Exception {
        if (userDTO.getUserType() == StaticDataEnum.USER_TYPE_20.getCode()) {
            //发邮件
            //查询模板
            MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(StaticDataEnum.SEND_NODE_7.getCode() + "");
            //设置模板参数
            String[] param = {userDTO.getEmail()};
            //邮件内容
            String sendMsg = templateContentReplace(param, mailTemplateDTO.getEnSendContent());
            String sendTitle = templateContentReplace(param, mailTemplateDTO.getEnMailTheme());
            try {
                //发送邮件
                Session session = MailUtil.getSession(sysEmail);
                MimeMessage mimeMessage = MailUtil.getMimeMessage(StaticDataEnum.U_BIZ.getMessage(), sysEmail, userDTO.getEmail(), sendTitle, sendMsg, null, session);
                MailUtil.sendMail(session, mimeMessage, sysEmail, sysEmailPwd);
                //记录邮件流水
                saveMailLog(userDTO.getEmail(), sendMsg, 0, request);
            } catch (Exception e) {
                log.info("UserService.saveUser,发送邮件异常" + e.getMessage(), e);
            }
        } else {
            //发短信 注册成功不发送短信
            //查询模板
            /*MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(StaticDataEnum.SEND_NODE_2.getCode() + "");
            //设置模板参数
            String[] param = {userDTO.getUserFirstName() + (userDTO.getUserLastName() == null ? "" : (" " + userDTO.getUserLastName()))};
            //内容
            String sendMsg = templateContentReplace(param, mailTemplateDTO.getEnSendContent());
            //判断手机区号
            String phoneCode = new StringBuilder(userDTO.getPhone()).substring(0, 2);
            if (phoneCode.equals(StaticDataEnum.CHN.getMessage())) {
                JSONArray phoneArray = new JSONArray();
                phoneArray.add(userDTO.getPhone());
                JSONObject aliyunContentParams = new JSONObject();
                if (userDTO.getUserType().intValue() == StaticDataEnum.USER_TYPE_20.getCode()) {
                    aliyunContentParams.put("code", userDTO.getEmail());
                } else {
                    aliyunContentParams.put("code", userDTO.getUserFirstName() + (userDTO.getUserLastName() == null ? "" : (" " + userDTO.getUserLastName())));
                }
                aliyunSmsService.sendChinaSms(userDTO.getPhone(), mailTemplateDTO.getAliCode(), aliyunContentParams);
            } else {
                aliyunSmsService.sendInternationalSms(userDTO.getPhone(), sendMsg);
            }*/

            //用户注册成功发送邮件
            this.registerSuccessToSendEmail(userDTO,request);
        }
    }

    /**
     * 发送信息
     *
     * @param userDTO
     */
    @Async("taskExecutor")
    public void sendMessage(UserDTO userDTO, Integer sendNode, HttpServletRequest request) {
        FirebaseDTO firebaseDTO = new FirebaseDTO();
        //获取邮件模板
        MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(sendNode.toString());
        //判断手机区号
        String phoneCode = new StringBuilder(userDTO.getPhone()).substring(0, 2);
        String phone = new StringBuilder(userDTO.getPhone()).substring(2, userDTO.getPhone().length());
        if (phoneCode.equals(StaticDataEnum.CHN.getMessage())) {
            JSONArray phoneArray = new JSONArray();
            phoneArray.add(phone);
            JSONObject params = new JSONObject();
            //notice
            NoticeDTO noticeDTO = new NoticeDTO();
            noticeDTO.setUserId(userDTO.getId());
            noticeDTO.setTitle(mailTemplateDTO.getEnMailTheme());
            noticeDTO.setContent(mailTemplateDTO.getEnSendContent());
            //push信息封装
            firebaseDTO.setAppName("UWallet");
            firebaseDTO.setToken(userDTO.getPushToken());
            firebaseDTO.setTitle(mailTemplateDTO.getEnMailTheme());
            firebaseDTO.setBody(mailTemplateDTO.getEnSendContent());
            firebaseDTO.setVoice(StaticDataEnum.VOICE_0.getCode());
            try {
                aliyunSmsService.sendChinaSms(userDTO.getPhone(), mailTemplateDTO.getAliCode(), params);
                noticeService.saveNotice(noticeDTO, null);
                if (!StringUtils.isEmpty(firebaseDTO.getToken())) {
                    serverService.pushFirebase(firebaseDTO,request);
                }
            } catch (Exception e) {
                log.info("send risk check message error , error message:{}", e.getMessage());
            }
        } else {
            //notice
            NoticeDTO noticeDTO = new NoticeDTO();
            noticeDTO.setUserId(userDTO.getId());
            noticeDTO.setTitle(mailTemplateDTO.getEnMailTheme());
            noticeDTO.setContent(mailTemplateDTO.getEnSendContent());
            //push信息封装
            firebaseDTO.setAppName("UWallet");
            firebaseDTO.setToken(userDTO.getPushToken());
            firebaseDTO.setTitle(mailTemplateDTO.getEnMailTheme());
            firebaseDTO.setBody(mailTemplateDTO.getEnSendContent());
            firebaseDTO.setVoice(StaticDataEnum.VOICE_0.getCode());
            try {
                aliyunSmsService.sendInternationalSms(userDTO.getPhone(), mailTemplateDTO.getEnSendContent());
                noticeService.saveNotice(noticeDTO, null);
                if (!StringUtils.isEmpty(firebaseDTO.getToken())) {
                    serverService.pushFirebase(firebaseDTO,request);
                }
            } catch (Exception e) {
                log.info("send risk check message error , error message:{}", e.getMessage());
            }
        }
    }



    /**
     * kyc成功发送消息 push、站内信
     *  成功节点 push 27 站内信 28
     * @author zhangzeyuan
     * @date 2021/5/11 22:24
     * @param userDTO
     * @param request
     */
    public void kycSuccessSendMessage(UserDTO userDTO, HttpServletRequest request) {
        //push
        MailTemplateDTO pushMsgTemplate = mailTemplateService.findMailTemplateBySendNode(String.valueOf(StaticDataEnum.SEND_NODE_27.getCode()));
        if(Objects.nonNull(pushMsgTemplate)
                && StringUtils.isNotBlank(pushMsgTemplate.getEnSendContent()) && StringUtils.isNotBlank(userDTO.getPushToken())){
            FirebaseDTO firebaseDTO = new FirebaseDTO();
            firebaseDTO.setAppName("Payo");
            firebaseDTO.setUserId(userDTO.getId());
            firebaseDTO.setToken(userDTO.getPushToken());
            firebaseDTO.setTitle(pushMsgTemplate.getEnMailTheme());
            firebaseDTO.setBody(pushMsgTemplate.getEnSendContent());
            firebaseDTO.setVoice(StaticDataEnum.VOICE_0.getCode());
            firebaseDTO.setTopic("KYC SUCCESS");
            try {
                serverService.pushFirebase(firebaseDTO,request);
            } catch (Exception e) {
                log.error("kyc成功发送push消息失败, error message:{}", e.getMessage());
            }
            log.error("kyc成功发送push消息成功");
        }

        //站内信
        MailTemplateDTO noticeMsgTemplate = mailTemplateService.findMailTemplateBySendNode(String.valueOf(StaticDataEnum.SEND_NODE_28.getCode()));
        if(Objects.nonNull(noticeMsgTemplate)
                && StringUtils.isNotBlank(noticeMsgTemplate.getEnSendContent()) && StringUtils.isNotBlank(noticeMsgTemplate.getEnMailTheme())){
            NoticeDTO noticeDTO = new NoticeDTO();
            noticeDTO.setUserId(userDTO.getId());
            noticeDTO.setTitle(noticeMsgTemplate.getEnMailTheme());
            noticeDTO.setContent(noticeMsgTemplate.getEnSendContent());
            try{
                noticeService.saveNotice(noticeDTO, null);
            }catch (Exception e){
                log.error("kyc成功发送站内信消息失败, error message:{}", e.getMessage());
            }
            log.info("kyc成功发送站内信成功");
        }
    }



    /**
     * kyc失败发送消息 push、站内信
     *  失败节点 push 29 站内信 30
     * @author zhangzeyuan
     * @date 2021/5/11 22:24
     * @param userDTO
     * @param request
     */
    public void kycFailSendMessage(UserDTO userDTO, HttpServletRequest request) {
        //push
        MailTemplateDTO pushMsgTemplate = mailTemplateService.findMailTemplateBySendNode(String.valueOf(StaticDataEnum.SEND_NODE_29.getCode()));
        if(Objects.nonNull(pushMsgTemplate)
                && StringUtils.isNotBlank(pushMsgTemplate.getEnSendContent()) && StringUtils.isNotBlank(userDTO.getPushToken())){
            FirebaseDTO firebaseDTO = new FirebaseDTO();
            firebaseDTO.setAppName("Payo");
            firebaseDTO.setUserId(userDTO.getId());
            firebaseDTO.setToken(userDTO.getPushToken());
            firebaseDTO.setTitle(pushMsgTemplate.getEnMailTheme());
            firebaseDTO.setBody(pushMsgTemplate.getEnSendContent());
            firebaseDTO.setVoice(StaticDataEnum.VOICE_0.getCode());
            firebaseDTO.setTopic("KYC FAILED");
            try {
                serverService.pushFirebase(firebaseDTO,request);
            } catch (Exception e) {
                log.error("kyc失败 发送push消息失败, error message:{}", e.getMessage());
            }
            log.error("kyc失败发送push消息成功");
        }

        //站内信
        MailTemplateDTO noticeMsgTemplate = mailTemplateService.findMailTemplateBySendNode(String.valueOf(StaticDataEnum.SEND_NODE_30.getCode()));
        if(Objects.nonNull(noticeMsgTemplate)
                && StringUtils.isNotBlank(noticeMsgTemplate.getEnSendContent()) && StringUtils.isNotBlank(noticeMsgTemplate.getEnMailTheme())){
            NoticeDTO noticeDTO = new NoticeDTO();
            noticeDTO.setUserId(userDTO.getId());
            noticeDTO.setTitle(noticeMsgTemplate.getEnMailTheme());
            noticeDTO.setContent(noticeMsgTemplate.getEnSendContent());
            try{
                noticeService.saveNotice(noticeDTO, null);
            }catch (Exception e){
                log.error("kyc失败 发送站内信消息失败, error message:{}", e.getMessage());
            }
            log.error("kyc失败发送站内信成功");
        }
    }



    @Override
    public JSONObject cardUnbundling(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        log.info("card unbundling, data:{}", requestInfo);
        // 返回结果
        JSONObject result = new JSONObject();
        // 查询卡信息
        JSONObject cardInfo = serverService.getCardInfo(requestInfo.getLong("cardId"));
        String latPayToken = cardInfo.getString("crdStrgToken");
        String integraPayUnique = cardInfo.getString("uniqueReference");
        String splitContactId = cardInfo.getString("splitContactId");
        String stripeToken = cardInfo.getString("stripeToken");
        // 记录解绑流水
        TieOnCardFlowDTO tieOnCardFlowDTO = new TieOnCardFlowDTO();
        Long userId = requestInfo.getLong("userId");
        tieOnCardFlowDTO.setUserId(userId);
        tieOnCardFlowDTO.setCardNo(cardInfo.getString("cardNo"));
        tieOnCardFlowDTO.setCardId(requestInfo.getLong("cardId"));
        tieOnCardFlowDTO.setUnBundlingReason(requestInfo.getString("reason"));
        tieOnCardFlowDTO.setUnBundlingState(StaticDataEnum.CARD_UNBUNDLING_STATE_0.getCode());

        // 卡解绑
        if (cardInfo.getInteger("type") == StaticDataEnum.TIE_CARD_1.getCode()) {
             /*
              2021-0604加入校验
              如果用户开通了分期付, 最后一张卡不能删除
              */
            //2021-11-01 分期付开通也可以删除卡
//            this.verifyCardState(getUserId(request),request);
            tieOnCardFlowDTO.setId(tieOnCardFlowService.saveTieOnCardFlow(tieOnCardFlowDTO, request));
            // 根据绑定渠道，进行相对应的卡删除
            if (!StringUtils.isEmpty(stripeToken)){
                // stripe解绑
                StripeAPIResponse stripeAPIResponse = stripeAPIService.deleteCard(userId, stripeToken);
                if(!stripeAPIResponse.isSuccess()){
                    throw new BizException(I18nUtils.get("card.unbundling.fail", getLang(request)));
                }
            } else if (!StringUtils.isEmpty(latPayToken)) {
                // latpay解绑
                latPayUnbundle(tieOnCardFlowDTO, latPayToken, request);
            } else if (!StringUtils.isEmpty(integraPayUnique)) {
                // integraPay解绑
                try {
                    String token = integraPayService.apiAccessToken();
                    integraPayService.cardUnbundling(integraPayUnique, token);
                } catch (Exception e) {
                    throw new BizException(I18nUtils.get("card.unbundling.fail", getLang(request)));
                }
            }

        } else {
            // 账户解绑
            // 查询分期付还款账户信息
            JSONObject searchInfo = new JSONObject();
            searchInfo.put("userId", userId);
            JSONObject creditUserInfo = serverService.findCreditUserInfo(searchInfo);
            //如果是分期付的还款账户，不可删除
            if (creditUserInfo.getLong("accountId") != null && creditUserInfo.getLong("accountId").equals(requestInfo.getLong("cardId"))) {
                tieOnCardFlowDTO.setUnBundlingState(StaticDataEnum.CARD_UNBUNDLING_STATE_2.getCode());
                tieOnCardFlowService.updateTieOnCardFlow(tieOnCardFlowDTO.getId(), tieOnCardFlowDTO, request);
                result.put("state", StaticDataEnum.CARD_UNBUNDLING_STATE_2.getCode());
                return result;
            }
            tieOnCardFlowDTO.setId(tieOnCardFlowService.saveTieOnCardFlow(tieOnCardFlowDTO, request));
            //如果有intergraPay绑定信息
//        if (!StringUtils.isEmpty(integraPayUnique)) {
//            try {
//                String token = integraPayService.apiAccessToken();
//                integraPayService.cardUnbundling(integraPayUnique, token);
//            } catch (Exception e) {
//                throw new BizException(I18nUtils.get("card.unbundling.fail", getLang(request)));
//            }
//        }
            //如果绑定了split，到split删除记录
            if (!StringUtils.isEmpty(splitContactId)) {
                splitUnbundle(tieOnCardFlowDTO, cardInfo, request);
            }

        }
        // 账户卡信息清除
        try {
            serverService.cardUnbundling(requestInfo);
            if (cardInfo.getInteger("type") == StaticDataEnum.TIE_CARD_1.getCode()) {
                //如果卡列表为空 更新user cardState 为0

                //stripe删卡 查询stripe卡列表
                if (StringUtils.isNotBlank(stripeToken)){
                    Integer stripeCardCount = cardService.getStripeCardCount(userId, request);
                    if(0 == stripeCardCount){
                        UserDTO updateUser = new UserDTO();
                        updateUser.setCardState(0);
                        this.updateUser(userId, updateUser, request);
                    }
                } else if (StringUtils.isNotBlank(latPayToken)) {
                    //latpay删卡 查询 latpay卡列表
                    Integer latpayCardCount = cardService.getLatpayCardCount(userId, request);
                    if(0 == latpayCardCount){
                        //如果卡列表为空 设置卡列表为不可用
                        UserDTO updateUser = new UserDTO();
                        updateUser.setCardState(0);
                        this.updateUser(userId, updateUser, request);
                    }
                }
            }
        } catch (Exception e) {
            log.info("account unbundling card fail, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            tieOnCardFlowDTO.setUnBundlingState(StaticDataEnum.CARD_UNBUNDLING_STATE_3.getCode());
            tieOnCardFlowService.updateTieOnCardFlow(tieOnCardFlowDTO.getId(), tieOnCardFlowDTO, request);
            // 清空缓存卡信息
            redisUtils.del(userId + "_card");
            result.put("state", StaticDataEnum.CARD_UNBUNDLING_STATE_3.getCode());
            return result;
        }
        tieOnCardFlowDTO.setUnBundlingState(StaticDataEnum.CARD_UNBUNDLING_STATE_4.getCode());
        tieOnCardFlowService.updateTieOnCardFlow(tieOnCardFlowDTO.getId(), tieOnCardFlowDTO, request);
        // 清空缓存卡信息
        redisUtils.del(userId + "_card");
        result.put("state", StaticDataEnum.CARD_UNBUNDLING_STATE_4.getCode());
        return result;
    }

    /**
     * 2021-0604添加校验, 如果用户已经开通分期付, 最后一张卡不允许删除
     *
     * @param userId
     * @param request
     * @throws BizException
     */
    public void verifyCardState(Long userId,HttpServletRequest request) throws BizException{
        try {
            JSONObject result = userDAO.verifyCardState(userId);
            //如果有用户的分期付系统user数据
            if (null != result && StringUtils.isNotBlank(result.getString("id"))) {
                BigDecimal creditAmount = result.getBigDecimal("creditAmount");
                //有授信额度, 说明开通了分期付, 现在获取卡的数量
                if (null != creditAmount && creditAmount.compareTo(BigDecimal.ZERO) > 0) {
                    //获取当前用户的可用卡数量
                    int cardCount = userDAO.countCard(userId);
                    if (cardCount == 1) {
                        throw new BizException(I18nUtils.get("can.not.delete.last.card", getLang(request)));
                    }
                }
            }
        }catch (Exception e){
            log.error("删除卡, 最后一张卡不允许删除, 校验异常,error:{},errorMsg:{},userId:{}",e,e.getMessage(),userId);
        }
    }

    @Override
    public JSONObject updateLatpayCardInfo(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        Long cardId = requestInfo.getLong("cardId");
        if (null == cardId) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        // 查询卡信息
        JSONObject cardInfo = serverService.getCardInfo(cardId);

        if (cardInfo.isEmpty()) {
            throw new BizException(I18nUtils.get("no.such.card.info", getLang(request)));
        }
        String latPayToken = cardInfo.getString("crdStrgToken");
        String stripeToken = cardInfo.getString("stripeToken");

        JSONObject result = null;
        if(StringUtils.isNotBlank(latPayToken)){
            //latepay修改卡信息
            result = cardService.latpayUpdateCard(latPayToken, requestInfo, cardInfo, request);
        }else if(StringUtils.isNotBlank(stripeToken)){
            //stripe卡修改信息
            result = cardService.stripeUpdateCard(stripeToken, getUserId(request), requestInfo.getString("customerCcExpyr"), requestInfo.getString("customerCcExpmo"), request);
        }else{
            throw new BizException(I18nUtils.get("no.such.card.info", getLang(request)));
        }
        return result;
    }

    private TieOnCardFlowDTO packAndSaveTieOnCardFlowData(JSONObject latpayUpdateInfo, JSONObject requestInfo, JSONObject cardInfo, Boolean cvcChangedMark, HttpServletRequest request) throws BizException {
        // 记录修改卡信息流水
        TieOnCardFlowDTO tieOnCardFlowDTO = new TieOnCardFlowDTO();
        tieOnCardFlowDTO.setUserId(getUserId(request));
        tieOnCardFlowDTO.setCardNo(cardInfo.getString("cardNo"));
        tieOnCardFlowDTO.setCardId(requestInfo.getLong("cardId"));
        tieOnCardFlowDTO.setCountry(latpayUpdateInfo.getString("Bill_country"));
        tieOnCardFlowDTO.setCustomerCcExpmo(latpayUpdateInfo.getString("Customer_cc_expmo"));
        tieOnCardFlowDTO.setCustomerCcExpyr(latpayUpdateInfo.getString("Customer_cc_expyr"));
        tieOnCardFlowDTO.setCustomerCcType(latpayUpdateInfo.getString("Customer_cc_type"));
        tieOnCardFlowDTO.setUnBundlingState(StaticDataEnum.CARD_UPDATE_STATE_PROCESSING.getCode());
        //如果cvc被更新了
        if (!cvcChangedMark) {
            tieOnCardFlowDTO.setCustomerCcCvc(requestInfo.getString("customerCcCvc"));
        }
        tieOnCardFlowDTO.setId(tieOnCardFlowService.saveTieOnCardFlow(tieOnCardFlowDTO, request));
        return tieOnCardFlowDTO;
    }

    /**
     * 校验请求参数, 并封装要调用三方更新的卡信息参数
     * 测试用cardId: 441022112998313984
     *
     * @param info     请求参数
     * @param cardInfo 账户系统卡信息
     * @param cardInfo 账户系统卡信息
     * @param request
     * @throws BizException
     */
    private JSONObject verifyUpdateLatpayCardInfo(JSONObject info,
                                                  JSONObject cardInfo,
                                                  String latPayToken,
                                                  HttpServletRequest request) throws Exception {
        String customerCcCvc = info.getString("customerCcCvc");
        if (StringUtils.isBlank(customerCcCvc)) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (customerCcCvc.length() > StaticDataEnum.CVC_MAX.getCode() || !StringUtils.isNumeric(customerCcCvc)) {
            throw new BizException(I18nUtils.get("cvc.illegal", getLang(request)));
        }

        JSONObject param = new JSONObject(7);

        //判断 customerCcType是否被修改
        /*String customerCcType = info.getString("customerCcType");
        if (StringUtils.isNotBlank(customerCcType) && !customerCcType.equals(cardInfo.getString("customerCcType"))) {
            JSONObject queryParam = new JSONObject(4);
            queryParam.put("code", "cardType");
            queryParam.put("value", customerCcType);
            StaticDataDTO cardTypeStaticData = staticDataService.findOneStaticData(queryParam);
            if (null == cardTypeStaticData || null == cardTypeStaticData.getId()) {
                throw new BizException(I18nUtils.get("card.type.not.found", getLang(request)));
            }
            param.put("Customer_cc_type", cardTypeStaticData.getName());
        }*/
        //请求Latpay 获取卡过期 年/月
        JSONObject cardDateInfo = queryLatpayCardInfo(latPayToken, request);
        String latpayExpmo = cardDateInfo.getString("customerCcExpmo");
        String latpayExpyr = cardDateInfo.getString("customerCcExpyr");
        //本次上送的卡过期的 月/年
        String customerCcExpmo = info.getString("customerCcExpmo");
        String customerCcExpyr = info.getString("customerCcExpyr");
        //日期-->月 校验
        if (StringUtils.isNotBlank(customerCcExpmo)) {
            this.verifyExpDate(customerCcExpmo,customerCcExpyr,request);
            if (!latpayExpmo.equals(customerCcExpmo)) {
                param.put("Customer_cc_expyr", customerCcExpyr);
                param.put("Customer_cc_expmo", customerCcExpmo);
            }
        }
        // 年校验
        if (StringUtils.isNotBlank(customerCcExpyr)) {
            this.verifyExpDate(customerCcExpmo,customerCcExpyr,request);
            if (!latpayExpyr.equals(customerCcExpyr)) {
                param.put("Customer_cc_expyr", customerCcExpyr);
                param.put("Customer_cc_expmo", customerCcExpmo);
            }
        }
        String country = info.getString("country");
        if (StringUtils.isNotBlank(country) && !country.equals(cardInfo.getString("country"))) {
            Map<String, Object> params = new HashMap<>(5);
            //查询国家ISO列表
            params.put("code", "county");
            params.put("value", country);
            StaticDataDTO countryStaticDto = staticDataService.findOneStaticData(params);
            if (null == countryStaticDto) {
                throw new BizException(I18nUtils.get("bank.country", getLang(request)));
            }
            CountryIsoDTO countryIsoDTO = tieOnCardFlowService.selectCountryIso(countryStaticDto.getEnName());
            if (null == countryIsoDTO) {
                throw new BizException(I18nUtils.get("bank.country", getLang(request)));
            }
            param.put("Bill_country", countryIsoDTO.getTwoLettersCoding());
        }
        return param;
    }
    /**
     * 卡过期日期校验 必须为本月及其之后的月份
     * @param customerCcExpmo 过期月份 MM
     * @param customerCcExpyr 过期年份 yyyy
     * @param request
     */
    private void verifyExpDate(String customerCcExpmo, String customerCcExpyr, HttpServletRequest request) throws BizException{
        if (customerCcExpyr.length() != StaticDataEnum.YEAR_FORMAT_YYYY.getCode() || !StringUtils.isNumeric(customerCcExpyr)) {
            throw new BizException(I18nUtils.get("card.exp.info.illegal", getLang(request)));
        }
        if (customerCcExpmo.length() != StaticDataEnum.MONTH_FORMAT_MM.getCode() || !StringUtils.isNumeric(customerCcExpmo)) {
            throw new BizException(I18nUtils.get("card.exp.info.illegal", getLang(request)));
        }
        Calendar currentCal = Calendar.getInstance();
        int currentYear = currentCal.get(Calendar.YEAR);
        //过期年份必须大于等于当年
        if (Integer.parseInt(customerCcExpyr)<currentYear){
            throw new BizException(I18nUtils.get("card.exp.info.illegal", getLang(request)));
        }
    }

    private void splitUnbundle(TieOnCardFlowDTO tieOnCardFlowDTO, JSONObject cardInfo, HttpServletRequest request) throws Exception {
        tieOnCardFlowDTO.setSplitContactId(cardInfo.getString("splitContactId"));
        tieOnCardFlowDTO.setSplitAgreementId(cardInfo.getString("splitAgreementId"));
        JSONObject splitRequestInfo = new JSONObject();
        splitRequestInfo.put("agreementRef", cardInfo.getString("splitAgreementId"));
        splitRequestInfo.put("contactId", cardInfo.getString("splitContactId"));
        try {
            splitService.splitDeleteAccount(splitRequestInfo, request);
        } catch (Exception e) {
            tieOnCardFlowDTO.setUnBundlingState(StaticDataEnum.CARD_UNBUNDLING_STATE_6.getCode());
            tieOnCardFlowService.updateTieOnCardFlow(tieOnCardFlowDTO.getId(), tieOnCardFlowDTO, request);
            log.info("delete card contactId info, data:{}, error message:{}, e:{}", splitRequestInfo, e.getMessage(), e);
            throw new BizException(I18nUtils.get("card.unbundling.fail", getLang(request)));
        }
    }

    private void latPayUnbundle(TieOnCardFlowDTO tieOnCardFlowDTO, String latPayToken, HttpServletRequest request) throws Exception {
        tieOnCardFlowDTO.setCrdStrgToken(latPayToken);
        Map<String, Object> params = new HashMap<>(1);
        params.put("type", StaticDataEnum.GATEWAY_TYPE_0.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        JSONObject cardTokenUpdateInfo = new JSONObject();
        cardTokenUpdateInfo.put("Merchant_id", gatewayDTO.getChannelMerchantId());
        cardTokenUpdateInfo.put("merchantpwd", gatewayDTO.getPassword());
        cardTokenUpdateInfo.put("AccountType", "A");
        cardTokenUpdateInfo.put("RequestType", "SCSS_D");
        cardTokenUpdateInfo.put("CrdStrg_Token", latPayToken);

        JSONObject latpayResult = null;
        try {
            log.info("delete card token info, data:{}", cardTokenUpdateInfo);
            latpayResult = JSONResultHandle.resultHandle(HttpClientUtils.sendPostForm(tieOnCardUrl, cardTokenUpdateInfo));
            log.info("delete card token info, result:{}", latpayResult);
            Integer status = latpayResult.getJSONObject("data").getInteger("Status");
            Integer responseType = latpayResult.getJSONObject("data").getInteger("ResponseType");
            if (!(status == StaticDataEnum.CARD_UNBUNDLING_STATUS.getCode() || status== StaticDataEnum.CARD_LATPAY_NOT_FOUND.getCode()) || responseType != StaticDataEnum.CARD_MANAGE_CARD_RESPONSE.getCode()) {
                throw new BizException(I18nUtils.get("card.unbundling.fail", getLang(request)));
            }
        } catch (Exception e) {
            tieOnCardFlowDTO.setUnBundlingState(StaticDataEnum.CARD_UNBUNDLING_STATE_6.getCode());
            tieOnCardFlowService.updateTieOnCardFlow(tieOnCardFlowDTO.getId(), tieOnCardFlowDTO, request);
            log.info("delete card token info, data:{},latpayResult:{}, error message:{}, e:{}", cardTokenUpdateInfo, latpayResult, e.getMessage(), e);
            throw new BizException(I18nUtils.get("card.unbundling.fail", getLang(request)));
        }
    }

    private void updateLatpayCardInfo(TieOnCardFlowDTO tieOnCardFlowDTO, JSONObject latpayUpdateInfo, String latPayToken, HttpServletRequest request) throws Exception {
        tieOnCardFlowDTO.setCrdStrgToken(latPayToken);
        Map<String, Object> params = new HashMap<>(2);
        params.put("type", StaticDataEnum.GATEWAY_TYPE_0.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        JSONObject requestParam = new JSONObject(8);
        requestParam.put("Merchant_id", gatewayDTO.getChannelMerchantId());
        requestParam.put("merchantpwd", gatewayDTO.getPassword());
        requestParam.put("AccountType", "A");
        //更新卡信息的表示位 SCSS_U
        requestParam.put("RequestType", "SCSS_U");
        requestParam.put("CrdStrg_Token", latPayToken);
        requestParam.putAll(latpayUpdateInfo);
        JSONObject latpayResult = null;
        try {
            log.info("latpay修改卡信息三方接口请求-参数, data:{}", requestParam);
            latpayResult = JSONResultHandle.resultHandle(HttpClientUtils.sendPostForm(tieOnCardUrl, requestParam));
            log.info("latpay修改卡信息三方接口请求-结果, result:{}", latpayResult);
            Integer status = latpayResult.getJSONObject("data").getInteger("Status");
            Integer responseType = latpayResult.getJSONObject("data").getInteger("ResponseType");
            if (status != StaticDataEnum.CARD_UPDATE_CARD_INFO_SUCCESS_STATUS.getCode() || responseType != StaticDataEnum.CARD_MANAGE_CARD_RESPONSE.getCode()) {
                throw new BizException(I18nUtils.get("card.update.info.failed", getLang(request)));
            }
        } catch (Exception e) {
            log.info("latpay修改卡信息三方接口请求-失败, data:{}, latpayResult:{}, error message:{}, e:{}", requestParam, latpayResult, e.getMessage(), e);
            throw new BizException(I18nUtils.get("card.update.info.failed", getLang(request)));
        }
    }


    private JSONObject queryLatpayCardInfo(String latPayToken, HttpServletRequest request) throws Exception {
        Map<String, Object> params = new HashMap<>(1);
        params.put("type", StaticDataEnum.GATEWAY_TYPE_0.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        JSONObject requestParam = new JSONObject();
        requestParam.put("Merchant_id", gatewayDTO.getChannelMerchantId());
        requestParam.put("merchantpwd", gatewayDTO.getPassword());
        requestParam.put("AccountType", "A");
        //更新卡信息的表示位 SCSS_U
        requestParam.put("RequestType", "SCSS_Q");
        requestParam.put("CrdStrg_Token", latPayToken);
        JSONObject latpayResult = null;
        try {
            log.info("latpay查询卡信息三方接口请求-参数, data:{}", requestParam);
            latpayResult = JSONResultHandle.resultHandle(HttpClientUtils.sendPostFormQueryLatpayCardInfo(tieOnCardUrl, requestParam));
            log.info("latpay查询卡信息三方接口请求-结果, result:{}", latpayResult);
            JSONObject data = latpayResult.getJSONObject("data");
            Integer status = data.getInteger("Status");
            Integer responseType = latpayResult.getJSONObject("data").getInteger("ResponseType");
            if (status != StaticDataEnum.CARD_UPDATE_CARD_INFO_SUCCESS_STATUS.getCode() || responseType != StaticDataEnum.CARD_MANAGE_CARD_RESPONSE.getCode()) {
                if(status == StaticDataEnum.CARD_LATPAY_NOT_FOUND.getCode()){
                    log.info("通过卡token获取卡详情失败, 卡token在latpay系统无效,返回9010, token:{}",latPayToken);
                }
                throw new BizException(I18nUtils.get("card.fetch.info.failed", getLang(request)));
            }
            requestParam.clear();
            requestParam.put("customerCcExpyr", data.get("Customer_cc_expyr") == null ? "" : data.get("Customer_cc_expyr"));
            requestParam.put("customerCcExpmo", data.get("Customer_cc_expmo") == null ? "" : data.get("Customer_cc_expmo"));
            requestParam.put("bin", data.get("CardBin"));
            return requestParam;
        } catch (Exception e) {
            log.info("latpay查询卡信息三方接口请求-失败, data:{}, latpayResult:{}, error message:{}, e:{}", requestParam, latpayResult, e.getMessage(), e);
            throw new BizException(I18nUtils.get("card.fetch.info.failed", getLang(request)));
        }
    }

    @Override
    public void cardAccountSystemUnbundlingHandle() throws Exception {
        Map<String, Object> params = new HashMap<>(1);
        params.put("unBundlingState", StaticDataEnum.CARD_UNBUNDLING_STATE_3.getCode());
        List<TieOnCardFlowDTO> tieOnCardFlowDTOS = tieOnCardFlowService.find(params, null, null);
        if (tieOnCardFlowDTOS != null && !tieOnCardFlowDTOS.isEmpty()) {
            for (TieOnCardFlowDTO tieOnCardFlowDTO : tieOnCardFlowDTOS) {
                JSONObject cardUnbundlingInfo = new JSONObject();
                cardUnbundlingInfo.put("cardId", tieOnCardFlowDTO.getCardId());
                cardUnbundlingInfo.put("reason", tieOnCardFlowDTO.getUnBundlingReason());
                // 账户卡信息清除
                try {
                    serverService.cardUnbundling(cardUnbundlingInfo);
                } catch (Exception e) {
                    log.info("account unbundling card fail, data:{}, error message:{}, e:{}", cardUnbundlingInfo, e.getMessage(), e);
                    tieOnCardFlowDTO.setUnBundlingState(StaticDataEnum.CARD_UNBUNDLING_STATE_3.getCode());
                    tieOnCardFlowService.updateTieOnCardFlow(tieOnCardFlowDTO.getId(), tieOnCardFlowDTO, null);
                    continue;
                }
                tieOnCardFlowDTO.setUnBundlingState(StaticDataEnum.CARD_UNBUNDLING_STATE_1.getCode());
                tieOnCardFlowService.updateTieOnCardFlow(tieOnCardFlowDTO.getId(), tieOnCardFlowDTO, null);
            }
        }
    }

    @Override
    public JSONObject findOneUserInfo(Long userId) throws Exception {
        JSONObject requestInfo = new JSONObject();
        requestInfo.put("userId", userId);
        // 从账户获取用户信息
        JSONObject userInfo = serverService.findOneUserInfo(requestInfo);
        // 获取用户认证阶段
        List<UserStepDTO> userStepDTOList = userStepService.findUserStepByUserId(userId);
        userInfo.put("userStep", userStepDTOList);
        // 获取kyc认证记录
        UserStepLogDTO kycLog = userStepService.findUserStepLatestLog(userId, StaticDataEnum.USER_STEP_1.getCode());
        if (kycLog != null && !StringUtils.isEmpty(kycLog.getRiskBatchNo())) {
            JSONArray kycRiskLogs = serverService.findRiskLog(kycLog.getRiskBatchNo());
            userInfo.put("kycDate", new SimpleDateFormat("HH:mm dd/MM/yyyy").format(new Date(kycLog.getCreatedDate())));
            userInfo.put("kycRiskLogs", kycRiskLogs);
        }
        // 获取分期付风控记录
        JSONObject installmentRiskLog = serverService.findInstallmentRiskLog(userId);
        userInfo.put("installmentRiskLog", installmentRiskLog);
        return userInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void installmentRecertification(Long userId, HttpServletRequest request) throws Exception {
        UserDTO userDTO = findUserById(userId);
        Map<String, Object> params = new HashMap<>(1);
        params.put("userId", userId);
        params.put("step", StaticDataEnum.USER_STEP_2.getCode());
        UserStepDTO illionStep = userStepService.findOneUserStep(params);
        params.put("step", StaticDataEnum.USER_STEP_3.getCode());
        UserStepDTO installmentRiskStep = userStepService.findOneUserStep(params);
        // 修改用户认证状态
        userDTO.setInstallmentState(StaticDataEnum.USER_BUSINESS_5.getCode());
        illionStep.setStepState(StaticDataEnum.USER_STEP_STATE_0.getCode());
        installmentRiskStep.setStepState(StaticDataEnum.USER_STEP_STATE_0.getCode());
        updateUser(userId, userDTO, request);
        userStepService.updateUserStep(illionStep.getId(), illionStep, request);
        userStepService.updateUserStep(installmentRiskStep.getId(), installmentRiskStep, request);
        // 调用分期付接口
        serverService.installmentRecertification(userId);

    }

    @Override
    public void modifyEmail(JSONObject data, Long userId, HttpServletRequest request) throws Exception {
        log.info("modifyEmail,userId :{},data:{}", userId, data);
        //校验邮箱
        String email = data.getString("email");
        if (!RegexUtils.isEmail(email)) {
            throw new BizException(I18nUtils.get("email.format.error", getLang(request)));
        }
        //查询用户
        UserDTO userDTO = findUserById(userId);
        if (userDTO == null || userDTO.getId() == null) {
            throw new BizException(I18nUtils.get("user.rule.userNameNotPresence", getLang(request)));
        }
        if (email.equals(userDTO.getEmail())) {
            throw new BizException(I18nUtils.get("old.email.same", getLang(request)));
        }
        //同步信息
        JSONObject updateInfo = new JSONObject(2);
        updateInfo.put("userId", userDTO.getId());
        updateInfo.put("email", email);
        serverService.updateEmail(updateInfo);
        //修改本地
        userDTO.setEmail(email);
        updateUser(userDTO.getId(), userDTO, request);
    }

    @Override
    public void tieOnCardParamsCheck(JSONObject data, HttpServletRequest request) throws Exception {
        //非空逻辑校验
        cardInfoNotNullCheck(data, request);
        //查询用户是否存在
        Long userId = data.getLong("userId");
        if (userId==null){
            userId=getUserId(request);
        }
        UserDTO userDTO = findUserById(userId);
        if (userDTO == null || userDTO.getId() == null) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }
        JSONArray cardDTOListFromServer = null;
        //尝试从账户系统获取绑卡列表, 如果没有,则请求dp系统获取illion卡列表
        try {
            JSONObject cardInfo = serverService.getAccountInfo(userDTO.getId());
            cardDTOListFromServer = cardInfo.getJSONArray("cardDTOList");
        } catch (Exception e) {
            log.error("调用账户系统获取用户卡列表异常, userId:{},errorMsg:{}", userDTO.getId(), e.getMessage());
        }
        String cardNo = data.getString("cardNo");
        String bsb = data.getString("bsb");
        for (int i = 0; i < cardDTOListFromServer.size(); i++) {
            if (data.getInteger("type") == StaticDataEnum.TIE_CARD_0.getCode()) {
                if (cardDTOListFromServer.getJSONObject(i).getString("cardNo").equals(cardNo)
                        && cardDTOListFromServer.getJSONObject(i).getString("bsb").equals(bsb)) {
                    throw new BizException(I18nUtils.get("bank.card.repetition", getLang(request)));
                }
            }
        }
    }

    private void cardInfoNotNullCheck(JSONObject data, HttpServletRequest request) throws Exception {
        if (data.getString("bsb") != null && data.getString("bsb").length() > Validator.BSB_NO_LENGTH) {
            throw new BizException(I18nUtils.get("bsb.length", getLang(request)));
        }
        if (data.getString("bankName") != null && data.getString("bankName").length() > Validator.TEXT_LENGTH_100) {
            throw new BizException(I18nUtils.get("bankName.length", getLang(request)));
        }
        if (data.getInteger("type") == StaticDataEnum.TIE_CARD_0.getCode()) {
            if (data.getString("bsb") == null) {
                throw new BizException(I18nUtils.get("bsb.length", getLang(request)));
            }
            if (data.getString("bankName") == null) {
                throw new BizException(I18nUtils.get("bankName.length", getLang(request)));
            }
            if (data.getString("cardNo") == null
                    || data.getString("cardNo").length() > Validator.BANK_ACCOUNT_NAME_MAX_LENGTH
                    || data.getString("cardNo").length() < Validator.BANK_ACCOUNT_NAME_MIN_LENGTH) {
                throw new BizException(I18nUtils.get("bank.account.no", getLang(request)));
            }
            if (data.getString("accountName") != null && data.getString("accountName").length() > Validator.TEXT_LENGTH_100) {
                throw new BizException(I18nUtils.get("account.name.length", getLang(request)));
            }
            if (data.getString("name") == null || data.getString("name").length() > Validator.TEXT_LENGTH_255) {
                throw new BizException(I18nUtils.get("name.error", getLang(request)));
            }
        } else {
            if (data.getString("cardNo") == null
                    || data.getString("cardNo").length() > Validator.BANK_CARD_MAX_LENGTH
                    || data.getString("cardNo").length() < Validator.BANK_CARD_MIN_LENGTH) {
                throw new BizException(I18nUtils.get("bank.card.no", getLang(request)));
            }
            if (data.getString("address1") != null && data.getString("address1").length() > Validator.TEXT_LENGTH_1000) {
                throw new BizException(I18nUtils.get("address.length", getLang(request)));
            }
            if (data.getString("zip") != null && data.getString("zip").length() > Validator.BANK_ZIP_LENGTH) {
                throw new BizException(I18nUtils.get("zip.length", getLang(request)));
            }
            if (data.getString("customerCcCvc") == null
                    || data.getString("customerCcCvc").length() > Validator.CC_CVC_MAX_LENGTH
                    || data.getString("customerCcCvc").length() < Validator.CC_CVC_MIN_LENGTH) {
                throw new BizException(I18nUtils.get("cc.cvc.length", getLang(request)));
            }
        }
    }

    @Override
    public JSONObject userValidation(JSONObject data, HttpServletRequest request) throws Exception {
        String loginName = data.getString("loginName");
        String loginPassword = data.getString("password");
        if (StringUtils.isEmpty(loginName)) {
            throw new BizException(I18nUtils.get("user.rule.usernameIsNull", getLang(request)));
        }
        if (StringUtils.isEmpty(loginPassword)) {
            throw new BizException(I18nUtils.get("user.rule.passwordIsNull", getLang(request)));
        }
        loginPassword = MD5FY.MD5Encode(loginPassword);
        Map<String, Object> params = new HashMap<>(3);
        //客户类型为用户
        Integer userType = StaticDataEnum.USER_TYPE_10.getCode();

        params.put("phone", loginName);
        params.put("userType", userType);
        UserDTO userDTO = findOneUser(params);
        if (userDTO == null || userDTO.getId() == null) {
            throw new BizException(I18nUtils.get("user.rule.userNameNotPresence", getLang(request)));
        }
        params.clear();
        params.put("userId", userDTO.getId());
        LoginMissDTO loginMissDTO = loginMissService.findOneLoginMiss(params);
        // 查询登陆错误记录表,如果登陆时间与锁定时间相差仍在一小时内，则拒绝登陆
        this.countFrozenTime(loginMissDTO, request);
        //验证密码
        if (userDTO.getPassword().equals(loginPassword)) {
            JSONObject result = new JSONObject();
            String token = JwtUtils.signApi(userDTO.getId(), userDTO.getPhone(), null, userDTO.getUserType(), redisUtils);
            //如果是用户，返回用户信息
            JSONObject userInfo = serverService.userInfoByQRCode(userDTO.getId());
            userDTO.setUserFirstName(data.getString("userFirstName"));
            userDTO.setUserLastName(data.getString("userLastName"));
            result.put("userInfo", userDTO);
            result.put("token", token);
            return result;
        } else {
            // 判断登陆机会是否还有1次，不是则减少登陆机会重新登陆，是则将登陆机会重置为5次
            if (loginMissDTO.getChance() != StaticDataEnum.LOGIN_MISS_TIME_LEFT.getCode()) {
                loginMissService.loginMissRecord(loginMissDTO, request);
                log.info("userValidation info , user data:{}, log in info:{}", userDTO, data);
                throw new BizException(I18nUtils.get("incorrect.username.password", getLang(request), new String[]{" " + (loginMissDTO.getChance() - 1)}));
            } else {
                loginMissDTO.setChance(5);
                loginMissDTO.setLastErrorTime(System.currentTimeMillis());
                loginMissService.updateLoginMiss(loginMissDTO.getId(), loginMissDTO, request);
                log.info("userValidation info , user data:{}, log in info:{}", userDTO, data);
                throw new BizException(I18nUtils.get("login.lock", getLang(request), new String[]{(60 - (System.currentTimeMillis() - loginMissDTO.getLastErrorTime()) / (1000 * 60)) + " min"}));
            }
        }
    }

    @Override
    public void userRegisterParamsCheck(UserDTO userDTO, HttpServletRequest request) throws BizException {
        //判断是用户还是商户
        int userType = userDTO.getUserType();
        if (StaticDataEnum.USER_TYPE_20.getCode() == userType) {
            //商户注册
            String email = userDTO.getEmail();
            if (!Validator.isEmail(email)){
                throw new BizException(I18nUtils.get("username.contains.invalid.characters", getLang(request)));
            }
            if (StringUtils.isBlank(email) || email.length() > Validator.TEXT_LENGTH_100 || !(Validator.isEmail(email))) {
//                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("email.format.error", getLang(request)));
                throw new BizException(I18nUtils.get("email.format.error", getLang(request)));
            }

        } else if (StaticDataEnum.USER_TYPE_10.getCode() == userType) {
            //用户注册
            if (userDTO.getPhone() != null) {
                String phone = userDTO.getPhone();
                String phoneCode = new StringBuilder(phone).substring(0, 2);
                phone = new StringBuilder(phone).substring(2, phone.length());
                if (phoneCode.equals(StaticDataEnum.CHN.getMessage())) {
                    if (phone.length() > Validator.PHONE_LENGTH) {
//                        return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("phone.length.error", getLang(request)));
                        throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
                    }
                } else {
                    if (phone.length() > Validator.PHONE_AUD_LENGTH) {
//                        return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("phone.length.error", getLang(request)));
                        throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
                    }
                }
            }
            String email = userDTO.getEmail();
            if (!Validator.isEmail(email)){
                throw new BizException(I18nUtils.get("email.format.error", getLang(request)));
            }
            if (email == null || email.length() > Validator.TEXT_LENGTH_100 || !(Validator.isEmail(email))) {
//                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("email.format.error", getLang(request)));
                throw new BizException(I18nUtils.get("email.format.error", getLang(request)));
            }
            String postcode = userDTO.getPostcode();
            if(postcode != null  ){
                if(!RegexUtils.isAuPostcode(postcode)){
                    throw new BizException(I18nUtils.get("postcode.format.error", getLang(request)));
                }
            }
            // TODO: 用户姓名校验
        } else {
//            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("password.format.error", getLang(request)));
            throw new BizException(I18nUtils.get("password.format.error", getLang(request)));
        }
        // 密码校验
        String password = userDTO.getPassword();
        if (!Validator.isAdminPassword(password)) {
//            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("password.format.error", getLang(request)));
            throw new BizException(I18nUtils.get("password.format.error", getLang(request)));
        }
    }

    @Override
    public void phoneParamsCheck(String phone, HttpServletRequest request) throws BizException {
        String phoneCode = new StringBuilder(phone).substring(0, 2);
        String testPhone = new StringBuilder(phone).substring(2, phone.length());
        if (phoneCode.equals(StaticDataEnum.CHN.getMessage())) {
            if (testPhone.length() > Validator.PHONE_LENGTH) {
//                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("phone.length.error", getLang(request)));
                throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
            }
        } else {
            if (testPhone.length() > Validator.PHONE_AUD_LENGTH) {
//                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("phone.length.error", getLang(request)));
                throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
            }
        }
    }

    @Override
    public JSONArray getBankLogoList() throws Exception {
        if (redisUtils.hasKey("bankLogo")) {
            List<Object> bankLogo = redisUtils.lGet("bankLogo", 0L, -1L);
            JSONArray bankArr = new JSONArray();
            for (Object object : bankLogo) {
                JSONObject logo = JSONObject.parseObject(object.toString());
                logo.put("bigLogo", imgRequestHost + logo.getString("bigLogo"));
                logo.put("smallLogo", imgRequestHost + logo.getString("smallLogo"));
                if (!"Other".equals(logo.getString("bankName"))) {
                    bankArr.add(logo);
                }
            }
            return bankArr;
        } else {
            JSONArray bankLogo = serverService.getBankLogoList().getJSONArray("data");
            redisUtils.lSet("bankLogo", bankLogo);
            for (int i = 0; i < bankLogo.size(); i++) {
                JSONObject logo = bankLogo.getJSONObject(i);
                if (!"Other".equals(logo.getString("bankName"))) {
                    bankLogo.remove(i);
                }
                logo.put("bigLogo", imgRequestHost + logo.getString("bigLogo"));
                logo.put("smallLogo", imgRequestHost + logo.getString("smallLogo"));
            }
            return bankLogo;
        }
    }

    @Override
    public String getBankLogo(String bankName, Integer type) throws Exception {
        String img = null;
        if (redisUtils.hasKey("bankLogo")) {
            List<Object> bankLogo = redisUtils.lGet("bankLogo", 0L, -1L);
            for (Object object : bankLogo) {
                JSONObject logo = JSONObject.parseObject(object.toString());
                if (logo.getString("bankName").equals(bankName)) {
                    if (StaticDataEnum.STATUS_0.getCode() == type.intValue()) {
                        img = (String) redisUtils.get(bankName + "_big");
                    } else {
                        img = (String) redisUtils.get(bankName + "_small");
                    }
                }
            }
        } else {
            JSONArray bankLogo = serverService.getBankLogoList().getJSONArray("data");
            redisUtils.lSet("bankLogo", bankLogo);
            for (int i = 0; i < bankLogo.size(); i++) {
                JSONObject logo = bankLogo.getJSONObject(i);
                if (logo.getString("bankName").equals(bankName)) {
                    if (StaticDataEnum.STATUS_0.getCode() == type.intValue()) {
                        img = (String) redisUtils.get(bankName + "_big");
                    } else {
                        img = (String) redisUtils.get(bankName + "_small");
                    }
                }
            }
        }
        if (img == null) {
            List<Object> bankLogo = redisUtils.lGet("bankLogo", 0L, -1L);
            for (Object object : bankLogo) {
                JSONObject logo = JSONObject.parseObject(object.toString());
                if ("Other".equals(logo.getString("bankName"))) {
                    if (StaticDataEnum.STATUS_0.getCode() == type.intValue()) {
                        img = (String) redisUtils.get("Other_big");
                    } else {
                        img = (String) redisUtils.get("Other_small");
                    }
                }
            }
        }
        return img;
    }

    @Override
    public JSONObject getKycResult(String userId, HttpServletRequest request) throws Exception {
        List<AppAboutUsDTO> appAboutUsDTOS = appAboutUsService.find(null, null, null);
        int chance;
        String key = userId + "_kyc_" + LocalDate.now();
        if (redisUtils.hasKey(key)) {
            chance = (int) redisUtils.get(key);
        } else {
            chance = KYC_CHANCE;
            redisUtils.set(key, chance, 86400);
        }
        JSONObject result = new JSONObject();
        result.put("chance", chance);
        result.put("phone", appAboutUsDTOS.get(0).getPhone());
        if (chance != 0) {
            result.put("message", I18nUtils.get("kyc.chance", getLang(request)));
        } else {
            result.put("message", I18nUtils.get("kyc.no.chance", getLang(request)));
        }
        return result;
    }

    @Override
    public String multiUploadFile(JSONObject jsonObject, String path, HttpServletRequest request) throws Exception {
        String fileStr = jsonObject.getJSONObject("data").getString("file");
        MultipartFile file = BASE64DecodedMultipartFile.base64ToMultipart(fileStr);
        String originalFilename = file.getOriginalFilename();
        boolean flag = UploadFileUtil.checkImg(originalFilename);
        boolean fileFlag = UploadFileUtil.checkFile(originalFilename);
        if (flag || fileFlag) {
            String fileName;
            String sfx = originalFilename.substring(originalFilename.lastIndexOf(".")).trim().toLowerCase();
            String key = path + "/" + SnowflakeUtil.generateId() + sfx;
            try {
                fileName = AmazonAwsUploadUtil.upload(file, key);
            } catch (Exception e) {
                throw e;
            }
            return fileName;
        } else {
            throw new BizException(I18nUtils.get("illegal.img", getLang(request)));
        }
    }

    @Override
    public JSONObject getInviteCode(HttpServletRequest request) throws Exception {
        JSONObject result = new JSONObject();
        Long userId = getId(request);
        String inviteCode = "";
        UserDTO userDTO = findUserById(userId);
        if (redisUtils.hasKey(userId + "inviteCode")) {
            inviteCode = (String) redisUtils.get(userId + "inviteCode");
        } else {
            if (!StringUtils.isEmpty(userDTO.getInviteCode())) {
                inviteCode = userDTO.getInviteCode();
            } else {
                // 初始化用户邀请码
                Map<String, Object> checkInviteCodeExist = new HashMap<>(16);
                while (true) {
                    inviteCode = InviteUtil.getBindNum(6);
                    checkInviteCodeExist.put("inviteCode", inviteCode);
                    UserDTO exist = findOneUser(checkInviteCodeExist);
                    if (exist.getId() == null) {
                        userDTO.setInviteCode(inviteCode);
                        break;
                    }
                }
                updateUser(userId, userDTO, request);
                redisUtils.set(userId + "inviteCode", inviteCode);
            }
        }
        result.put("inviteCode", inviteCode);
        result.put("content", INVITE_CONTENT);
//        result.put("url", "Join Payo to get $10 off and enjoy pay later meals, why not? Use this code to get the deal:" + inviteCode + " https://invite.payo.com.au?inviteCode=" + inviteCode);
        result.put("url", "Join payo and enjoy a new way to pay in restaurants, bars and cafes. Get $20 off your next bill with the code " + inviteCode + " https://invite.payo.com.au?inviteCode=" + inviteCode);

        result.put("invitationToRegister", userDTO.getInvitationToRegister() - userDTO.getInviteConsumption());
        result.put("inviteConsumption", userDTO.getInviteConsumption());
        result.put("expect", userDTO.getExpectAmount().subtract(userDTO.getActualAmount()));
        result.put("accumulate", userDTO.getActualAmount());
        return result;
    }

    @Override
    public void walletBookedDoubleHandle() throws Exception {
        log.info("check wallet booked doubtful flow");
        //查询充值交易流水中账户操作阶段可疑的流水
        Map<String, Object> params = new HashMap<>(16);
        int[] stateList = {StaticDataEnum.TRANS_STATE_3.getCode(), StaticDataEnum.TRANS_STATE_0.getCode()};
        int[] transTypeList = {StaticDataEnum.ACC_FLOW_TRANS_TYPE_20.getCode(), StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode(), StaticDataEnum.ACC_FLOW_TRANS_TYPE_25.getCode()};
        params.put("stateList", stateList);
        params.put("transTypeList", transTypeList);
        List<MarketingFlowDTO> marketingFlowDTOList = marketingFlowService.findAbnormal(params);
        if (marketingFlowDTOList == null || marketingFlowDTOList.size() == 0) {
            return;
        }
        for (MarketingFlowDTO marketingFlowDTO : marketingFlowDTOList) {
            params.clear();
            params.put("stateList", stateList);
            params.put("flowId", marketingFlowDTO.getFlowId());
            params.put("transType", marketingFlowDTO.getTransType());
            AccountFlowDTO accountFlowDTO = accountFlowService.findOneAccountFlow(params);
            JSONObject data = null;
            // 查询账户流水状态
            try {
                data = serverService.transactionInfo(accountFlowDTO.getOrderNo().toString());
            } catch (Exception e) {
                log.error("check doubtful flow failed message:{}", e.getMessage());
                return;
            }
            int code = 0;
            if (data != null) {
                if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                    code = StaticDataEnum.TRANS_STATE_1.getCode();
                } else if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                    code = StaticDataEnum.TRANS_STATE_2.getCode();
                } else {
                    code = StaticDataEnum.TRANS_STATE_3.getCode();
                }
            } else {
                code = StaticDataEnum.TRANS_STATE_2.getCode();
            }
            marketingFlowService.doMarketingAmountInResult(accountFlowDTO, marketingFlowDTO, code, null);
//            marketingFlowDTO.setState(accountFlowDTO.getState());
//            accountFlowService.updateAccountFlow(accountFlowDTO.getId(), accountFlowDTO, null);
//            marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(),marketingFlowDTO,null);

        }
    }

    @Override
    public void walletBookedFailedHandle() throws Exception {
        log.info("check wallet booked failed flow");
        //查询充值交易流水中账户操作阶段可疑的流水
        Map<String, Object> params = new HashMap<>(2);
        params.put("state", StaticDataEnum.TRANS_STATE_2.getCode());
        int[] transTypeList = {StaticDataEnum.ACC_FLOW_TRANS_TYPE_20.getCode(), StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode()};
        params.put("transTypeList", transTypeList);
        List<MarketingFlowDTO> marketingFlowDTOList = marketingFlowService.findAbnormal(params);
        if (marketingFlowDTOList == null || marketingFlowDTOList.size() == 0) {
            return;
        }

        for (MarketingFlowDTO marketingFlowDTO : marketingFlowDTOList) {
            // 红包入账
            try {
                doMarketingAmountIn(marketingFlowDTO, null);
            } catch (Exception e) {
                log.info("wallet booked faile, account:{}", marketingFlowDTO);
            }
        }
    }

    @Override
    public void walletBookedConsumption(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        Long userId = requestInfo.getLong("userId");
        Long flowId = requestInfo.getLong("flowId");
        Long orderNo = requestInfo.getLong("orderNo");
        BigDecimal transAmount = requestInfo.getBigDecimal("transAmount");
        Integer transState = requestInfo.getInteger("transState");
        Integer isFirstConsumption = requestInfo.getInteger("isFirstConsumption");
        UserDTO userDTO = findUserById(userId);
        log.info("walletBookedConsumption message: userId" + userId + ",transState:" + transState + ",flowId" + flowId);
        // 第一次消费向邀请人新增红包
        if (transState.intValue() == StaticDataEnum.TRANS_STATE_1.getCode() && userDTO.getFirstDealState() == StaticDataEnum.STATUS_0.getCode()) {
            ParametersConfigDTO parametersConfigDTO = parametersConfigService.findParametersConfigById(1L);
            if (parametersConfigDTO.getWalletFavorable().intValue() == StaticDataEnum.STATUS_1.getCode()) {

                try {
                    this.updateFirstDealState(userDTO.getId());
                    walletBooked(userDTO.getInviterId(), StaticDataEnum.ACC_FLOW_TRANS_TYPE_20.getCode(), null, request, flowId);
                } catch (Exception e) {
                    log.info("uwallet booked consumption failed, userInfo:{}, error message:{}, e:{}", userDTO, e.getMessage(), e);
                }
            }
        }
        //如果红包金额>0 记录红包出账流水
        if (transAmount.compareTo(BigDecimal.ZERO) > 0) {
            // 钱包出账记录
            MarketingFlowDTO marketingFlowDTO = new MarketingFlowDTO();
            marketingFlowDTO.setState(transState);
            marketingFlowDTO.setUserId(userId);
            marketingFlowDTO.setFlowId(flowId);
            marketingFlowDTO.setDirection(StaticDataEnum.DIRECTION_1.getCode());
            marketingFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode());
            marketingFlowDTO.setAmount(transAmount);
            marketingFlowService.saveMarketingFlow(marketingFlowDTO, request);

            AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
            accountFlowDTO.setFlowId(flowId);
            accountFlowDTO.setUserId(userId);
            accountFlowDTO.setAccountType(StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
            accountFlowDTO.setTransAmount(transAmount);
            accountFlowDTO.setOrderNo(Long.valueOf(orderNo));
            accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode());
            accountFlowDTO.setState(transState);
            accountFlowService.saveAccountFlow(accountFlowDTO, request);
        }

    }

    @Override
    public int updateRegister(Long id) {
        return userDAO.updateRegister(id);
    }

    @Override
    public int updateWalletGrandTotal(Long id, BigDecimal expectAmount, BigDecimal actualAmount) {
        return userDAO.updateWalletGrandTotal(id, expectAmount, actualAmount);
    }

    @Override
    public int updateConsumption(Long id) {
        return userDAO.updateConsumption(id);
    }

    @Override
    public int walletFriendsInvitedCount(Map<String, Object> params) {
        return userDAO.walletFriendsInvitedCount(params);
    }

    @Override
    public List<JSONObject> walletFriendsInvitedList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<UserDTO> users = userDAO.walletFriendsInvitedList(params);
        if (CollectionUtil.isNotEmpty(users)) {
            List<JSONObject> resultList = new ArrayList<>(users.size());
            for (UserDTO userDTO : users) {
                JSONObject userInfo = new JSONObject(4);
                userInfo.put("userId", userDTO.getId());
//                if(userDTO.getFirstDealState() == StaticDataEnum.STATUS_1.getCode()){
//                    userInfo.put("info", "+ $10");
//                }else{
                userInfo.put("info", "Pending");
//                }

                userInfo.put("name", userDTO.getUserFirstName() + " " + userDTO.getUserLastName());
                resultList.add(userInfo);
            }
//            userIds.forEach(userId -> {
//                try {
//                    JSONObject data = serverService.userInfoByQRCode(userId);
//                    JSONObject userInfo = new JSONObject(4);
//                    userInfo.put("userId", userId);
//                    userInfo.put("info", "Pending");
//                    userInfo.put("name", data.getString("userFirstName") + " " + data.getString("userLastName"));
//                    resultList.add(userInfo);
//                } catch (Exception e) {
//                    log.info("wallet friends invited list find user info failed, userId:{}, error message:{}, error:{}", userId, e.getMessage(), e);
//                }
//            });
            return resultList;
        }
        return new ArrayList<>();
    }

    @Override
    public int walletFriendsPurchaseCount(Map<String, Object> params) {
        return userDAO.walletFriendsPurchaseCount(params);
    }

    @Override
    public List<JSONObject> walletFriendsPurchaseList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<JSONObject> users = userDAO.walletFriendsPurchaseList(params);
        if (CollectionUtil.isNotEmpty(users)) {
            /*List<JSONObject> resultList = new ArrayList<>(users.size());
            for (UserDTO userDTO : users) {
                JSONObject userInfo = new JSONObject(4);
                userInfo.put("userId", userDTO.getId());
                userInfo.put("info", "+ $10");
                userInfo.put("name", userDTO.getUserFirstName() + " " + userDTO.getUserLastName());
                resultList.add(userInfo);
            }*/
//            userIds.forEach(userId -> {
//                try {
//                    JSONObject data = serverService.userInfoByQRCode(userId);
//
//                } catch (Exception e) {
//                    log.info("wallet friends invited list find user info failed, userId:{}, error message:{}, error:{}", userId, e.getMessage(), e);
//                }
//            });
            return users;
        }
        return new ArrayList<>();
    }

    @Override
    public int updateFirstDealState(Long id) throws BizException {
        int cnt = userDAO.updateFirstDealState(id);
        if (cnt != 1) {
            log.error("updateFirstDealState error, userId:{}", id);
            throw new BizException("update FirstDealState Error!");
        }
        return cnt;
    }

    @Override
    public JSONObject getCardDetails(String latPayToken, HttpServletRequest request) throws BizException {
        if (latPayToken == null || latPayToken.isEmpty()) {
            throw new BizException(I18nUtils.get("card.get.info.failed", getLang(request)));
        }
        Map<String, Object> params = new HashMap<>(1);
        params.put("type", StaticDataEnum.GATEWAY_TYPE_0.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        JSONObject requestParam = new JSONObject();
        requestParam.put("Merchant_id", gatewayDTO.getChannelMerchantId());
        requestParam.put("merchantpwd", gatewayDTO.getPassword());
        requestParam.put("AccountType", "A");
        //更新卡信息的表示位
        requestParam.put("RequestType", "SCSS_Q");
        requestParam.put("CrdStrg_Token", latPayToken);
        JSONObject latpayResult = null;
        try {
            log.info("latpay查询卡信息三方接口请求-参数, data:{}", requestParam);
            latpayResult = JSONResultHandle.resultHandle(HttpClientUtils.sendPostForm(tieOnCardUrl, requestParam));
            log.info("latpay查询卡信息三方接口请求-结果, result:{}", latpayResult);
            JSONObject data = latpayResult.getJSONObject("data");
            Integer status = data.getInteger("Status");
            Integer responseType = latpayResult.getJSONObject("data").getInteger("ResponseType");
            if (status != StaticDataEnum.CARD_UPDATE_CARD_INFO_SUCCESS_STATUS.getCode() && responseType != StaticDataEnum.CARD_MANAGE_CARD_RESPONSE.getCode()) {
                throw new BizException(I18nUtils.get("card.update.info.failed", getLang(request)));
            }
            requestParam.clear();
            requestParam.put("customerCcExpyr", data.get("Customer_cc_expyr") == null ? "" : data.get("Customer_cc_expyr"));
            requestParam.put("customerCcExpmo", data.get("Customer_cc_expmo") == null ? "" : data.get("Customer_cc_expmo"));
            return requestParam;
        } catch (Exception e) {
            log.info("latpay查询卡信息三方接口请求-失败, data:{}, latpayResult:{}, error message:{}, e:{}", requestParam, latpayResult, e.getMessage(), e);
            throw new BizException(I18nUtils.get("card.update.info.failed", getLang(request)));
        }
    }

    /**
     * 返回参数:
     * typeStr 卡类型(3个字段拼起来的显示名), string类型 你们直接显示这个字段
     * 新增卡, 传参传后面这3个
     * cardType : int值 卡类型: visa/万事达/美国运通
     * cardPayType : int值 卡付款先后类型: 信用卡/借记卡
     * cardCategory: 卡分类: 标准/传统/美国运通/标准预付费/商务卡
     *
     * @param requestInfo
     * @param request
     * @return
     * @throws BizException
     */
    @Override
    public JSONObject latpayGetCardType(JSONObject requestInfo, HttpServletRequest request) throws BizException {
        String cardBin = requestInfo.getString("cardBin");
        if (StringUtils.isBlank(cardBin) || !StringUtils.isNumeric(cardBin) || cardBin.length() < StaticDataEnum.CARD_BIN_LENGTH.getCode()) {
            throw new BizException(I18nUtils.get("card.bin.no.good", getLang(request)));
        }
        cardBin = cardBin.length() > StaticDataEnum.CARD_BIN_LENGTH.getCode() ? cardBin.substring(0, StaticDataEnum.CARD_BIN_LENGTH.getCode()) : cardBin;
        JSONObject latpayRes = this.queryLatpayCardTypeInfo(cardBin, request);
        //封装并处理结果集, 本方法上方有返回参数样例
        return this.processLatpayCardTypeRes(latpayRes, request);
    }


    @Override
    public void logCardFailedTime(@NonNull Long userId, @NonNull Long cardId, Boolean isSuccess, HttpServletRequest request) {
        if (isSuccess) {
            redisUtils.del(this.cardPayFailedRedisKeyBuilder(userId, cardId, true));
        } else {
            //单卡失败次数redis key
            String singleCardFailedKey = this.cardPayFailedRedisKeyBuilder(userId, cardId, true);
            //用户总失败次数 redis key
            String userFailedTotalKey = this.cardPayFailedRedisKeyBuilder(userId, cardId, false);
            int singleFailedCount = redisUtils.hasKey(singleCardFailedKey) ? (Integer) redisUtils.get(singleCardFailedKey) : 0;
            int totalFailedCount = redisUtils.hasKey(userFailedTotalKey) ? (Integer) redisUtils.get(userFailedTotalKey) : 0;
            //获取到当天23:59:59秒 还有多少秒
            long expTimeSec = this.secUntilEndOfToday();
            redisUtils.set(singleCardFailedKey, singleFailedCount + 1, expTimeSec);
            redisUtils.set(userFailedTotalKey, totalFailedCount + 1, expTimeSec);
        }
    }

    /**
     * 到当天结束还有多少秒
     *
     * @return
     */
    private long secUntilEndOfToday() {
        //获取当天结束时时间戳
        long time = DateUtil.endOfDay(new Date(System.currentTimeMillis())).getTime();
        long currentTimeMillis = System.currentTimeMillis();
        if (time > currentTimeMillis) {
            //到当天结束还有多少秒
            return (time - currentTimeMillis) / 1000;
        }
        return 0;
    }

    /**
     * 生产卡交易失败次数计数 redis key
     *
     * @param userId
     * @param cardId
     * @param type   true: 单卡失败key, false: user 当天卡交易总的失败次数
     * @return
     */
    private String cardPayFailedRedisKeyBuilder(Long userId, Long cardId, Boolean type) {
        if (type) {
            return Constant.CARD_PAY_FAILED_TIME_COUNT + "-" + userId + "-" + cardId;
        } else {
            return Constant.USER_CARD_PAY_FAILED_TIME_TOTAL + "-" + userId;
        }
    }

    @Override
    public void verifyCardFailedTime(@NonNull Long userId, @NonNull Long cardId, HttpServletRequest request) throws BizException {
        JSONObject failedConfig = this.getFailedConfig();
        //用户总失败次数 redis key
        String userFailedTotalKey = this.cardPayFailedRedisKeyBuilder(userId, cardId, false);
        if (redisUtils.hasKey(userFailedTotalKey)) {
            Integer userFailedMaxConfig = failedConfig.getInteger("userMax");
            if ((Integer) redisUtils.get(userFailedTotalKey) >= userFailedMaxConfig) {
                String message = I18nUtils.get("user.total.exceed", getLang(request));
                message = message.replace(Constant.MESSAGE_MARKER, userFailedMaxConfig.toString());
                throw new BizException(message);
            }
        }
        //单卡失败次数redis key
        String singleCardFailedKey = this.cardPayFailedRedisKeyBuilder(userId, cardId, true);
        if (redisUtils.hasKey(singleCardFailedKey)) {
            Integer singleCardMax = failedConfig.getInteger("cardMax");
            if ((Integer) redisUtils.get(singleCardFailedKey) >= singleCardMax) {
                String message = I18nUtils.get("single.card.exceed", getLang(request));
                message = message.replace(Constant.MESSAGE_MARKER, singleCardMax.toString());
                throw new BizException(message);
            }
        }
    }

    private JSONObject getFailedConfig() {
        String userMaxKey = Constant.USER_PAY_FAILED_MAX_CONFIG;
        String cardMaxKey = Constant.CARD_PAY_FAILED_SINGLE_CONFIG;
        Object userMaxConfig = redisUtils.get(userMaxKey);
        Object cardMaxConfig = redisUtils.get(cardMaxKey);
        if (null == userMaxConfig || null == cardMaxConfig) {
            //配置表就一条数据, id为 1
            ParametersConfigDTO configById = parametersConfigService.findParametersConfigById(1L);
            userMaxConfig = configById.getUserCardFailedMax();
            cardMaxConfig = configById.getCardFailedMax();
            redisUtils.set(userMaxKey, userMaxConfig);
            redisUtils.set(cardMaxKey, cardMaxConfig);
        }
        JSONObject result = new JSONObject(3);
        result.put("userMax", userMaxConfig);
        result.put("cardMax", cardMaxConfig);
        return result;
    }

    /**
     * 初始化库表卡类型信息
     *
     * @throws BizException
     */
    /*
    public Object initCardTypeInfo(HttpServletRequest request) throws BizException {
        List<String> failedRes= new ArrayList<>();
        try {
            List<JSONObject> accountInfoJson = serverService.getAll();
            if (CollectionUtils.isNotEmpty(accountInfoJson)) {
                log.info("初始化卡信息,获取到需要初始化的数据:{}",accountInfoJson);
                for (JSONObject card : accountInfoJson) {
                    try {
                        String crdStrToken = card.getString("crdStrgToken");
                        log.info("开始初始化, Latpay-Token:{}",crdStrToken);
                        if (StringUtils.isNotBlank(crdStrToken)) {
                            //通过token获取卡信息, 其中包含 card bin 信息
                            JSONObject infoRes = this.queryLatpayCardInfo(crdStrToken, request);
                            String bin = infoRes.getString("bin");
                            log.info("通过Token获取到的卡Bin信息, Latpay-Bin:{}",bin);
                            if (StringUtils.isNotBlank(bin) && bin.length() == 6) {
                                //latpay接口,通过卡bin获取 卡类型详情
                                JSONObject latpayRes = queryLatpayCardTypeInfo(bin, request);
                                JSONObject typeInfoRes = processLatpayCardTypeRes(latpayRes,request);
                                log.info("通过Latpay返回的卡详情, 匹配本地数据字典值结果:{}",typeInfoRes);
                                //数据字典int值的Key: "cardType", "cardPayType", "cardCategory"
                                Integer cardType = typeInfoRes.getInteger("cardType");
                                Integer cardPayType = typeInfoRes.getInteger("cardPayType");
                                Integer cardCategory = typeInfoRes.getInteger("cardCategory");
                                if (null == cardType || null == cardPayType || null == cardCategory) {
                                    log.error("初始化卡类型信息,匹配本地数据字典失败,本地匹配结果:{}",typeInfoRes);
                                    throw new BizException("获取卡信息失败");
                                }
                                *//*if (!card.getInteger("cardType").equals(cardType)) {
                                    log.error("初始化卡类型---> 获取bin信息失败,卡信息:{},latpay查询结果:{}", card, typeInfoRes);
                                    throw new BizException("原数据库字段: 卡类型 cardType与latpay获取到的卡类型不一样! cardId: " + card.getString("id"));
                                }*//*
                                //封装并调用 账户系统更新 卡类型信息字段
                                JSONObject updateInfo = new JSONObject(5);
                                updateInfo.put("cardPayType", cardPayType);
                                updateInfo.put("cardCategory", cardCategory);
                                updateInfo.put("id", card.get("id"));
                                log.info("初始化卡类型信息,更新账户系统,更新数据:{}",updateInfo);
                                serverService.cardInfoUpdateTemp(updateInfo);
                            } else {
                                log.error("初始化卡类型---> 获取bin信息失败,卡token:{},卡信息:{}", crdStrToken, card);
                                failedRes.add(card.getString("id"));
                            }
                        }
                    } catch (Exception e) {
                        failedRes.add(card.getString("id"));
                        log.error("初始化卡类型---> 获取bin信息失败,卡信息:{},eMsg:{},error:{}", card, e.getMessage(), e);
                    }
                }
                failedRes.forEach(System.out::println);
            }
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
        return failedRes;
    }*/

    private JSONObject queryLatpayCardTypeInfo(String cardBin, HttpServletRequest request) throws BizException {
        //获取通道路由信息
        JSONObject params = new JSONObject(2);
        params.put("type", StaticDataEnum.GATEWAY_TYPE_0.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        //封装请求参数 三方接口文档地址: https://docs.qq.com/pdf/DWlptcFVzanlIdEh4
        JSONObject requestParam = new JSONObject(8);
        requestParam.put("Merchant_User_Id", gatewayDTO.getChannelMerchantId());
        requestParam.put("MerchantPwd", gatewayDTO.getPassword());
        requestParam.put("TransactionType", "BNLKP");
        requestParam.put("Merchant_Ref_Number", SnowflakeUtil.generateId().toString());
        requestParam.put("BinNumber", cardBin);
        try {
            log.info("latpay获取卡类型详情-参数,请求url:{}, data:{}",cardBinLookupUrl, requestParam);
            JSONObject latpayResult = JSONResultHandle.resultHandle(HttpClientUtils.sendPostForm(cardBinLookupUrl, requestParam));
            log.info("latpay获取卡类型详情-结果, result:{}", latpayResult);
            JSONObject data = latpayResult.getJSONObject("data");
            if (data.isEmpty() || data.getInteger("Status") != StaticDataEnum.CARD_TYPE_INFO_SUCCESS_STATUS.getCode()
                    || data.getInteger("ResponseType") != StaticDataEnum.CARD_TYPE_INFO_RESPONSE_TYPE.getCode()) {
                throw new BizException(I18nUtils.get("get.card.type.error", getLang(request)));
            }
            return data;
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
    }
    /*public static void main(String[] args) {
        //456468
        JSONObject requestParam = new JSONObject();
        requestParam.put("Merchant_id", "610055901");
        requestParam.put("merchantpwd", "XSEdRGXjPO9tPq5M0L49");
        requestParam.put("AccountType", "A");
        //更新卡信息的表示位 SCSS_U
        requestParam.put("RequestType", "SCSS_Q");
        requestParam.put("CrdStrg_Token", "0x01368a65f535f0502ff887753498714a9ba153ca");
        try {
            log.info("latpay查询卡信息三方接口请求-参数, data:{}", requestParam);
            JSONObject jsonObject = JSONResultHandle.resultHandle(HttpClientUtils.sendPostFormQueryLatpayCardInfo("https://l4p2s7p2r4o3c9e3ss.com/ManageSCSS/ProcessRequest_SCSSMngServices.aspx", requestParam));
            System.out.println(jsonObject.toJSONString());
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }*/

//    public static void main(String[] args) {
//        JSONObject requestParam = new JSONObject(8);
//        requestParam.put("Merchant_User_Id", "610055901");
//        requestParam.put("MerchantPwd", "XSEdRGXjPO9tPq5M0L49");
//        requestParam.put("TransactionType", "BNLKP");
//        requestParam.put("Merchant_Ref_Number", SnowflakeUtil.generateId().toString());
//        requestParam.put("BinNumber", "456468");
//        try {
//            JSONObject latpayResult = JSONResultHandle.resultHandle(HttpClientUtils.sendPostForm("https://l4p2s7p2r4o3c9e3ss.com/ManageRisk/binlookupv2.aspx", requestParam));
//            System.out.println(latpayResult);
//        }catch (Exception e){
//            System.out.println(e.getMessage());
//        }
//    }

    private JSONObject processLatpayCardTypeRes(JSONObject latpayResult, HttpServletRequest request) throws BizException {
        JSONObject result = new JSONObject(8);
        /*
          CardBrand(AMEX DISCOVER MAESTRO MASTERCARD VISA)
          对应 账户系统原有字段 cardType字段 数据字典: cardType
         */
        String cardType = this.dealWithThirdPartyError(latpayResult);
        //卡付款先后类型: 信用卡/借记卡 (CREDIT/DEBIT)
        String cardPayType = latpayResult.getString("CardType");
        //卡分类: 标准/传统/美国运通/标准预付费/商务卡(STANDARD TRADITIONAL AMERICAN EXPRESS STANDARD PREPAID BUSINESS )
        String cardCategory = latpayResult.getString("CardCategory");

        //获取数据字典信息;
        Map<String, List<StaticData>> staticDataMap = staticDataService.findByCodeList(new String[]{"cardType", "cardPayType", "cardCategory"});
        JSONObject codeValue = new JSONObject(5);
        codeValue.put("cardType",cardType);
        codeValue.put("cardCategory",cardCategory);
        codeValue.put("cardPayType",cardPayType);

        for (String key : codeValue.keySet()) {
            //数据字典name字段为latpay 字段, enName为前端显示字段
            Map<String, StaticData> dataMap = staticDataMap.get(key).stream().collect(Collectors.toMap(StaticData::getName, staticData -> staticData));
            StaticData staticData = dataMap.get(codeValue.getString(key));
            //如果cardCategory在数据字典没有配置 则赋值unknow类型 发起支付时会判断
            if (key.equals("cardCategory") && (null == staticData || null == staticData.getId())){
                //如果cardCategory 我们的数据字典没配置, 则设置为unknow
                staticData = dataMap.get("Unknow");
            }
            if (key.equals("cardType") && (null == staticData || null == staticData.getId())){
                throw new BizException(I18nUtils.get("card.type.not.support",getLang(request)));
            }
            //如果数据字典中未找到卡类型信息, 报错
            if (null == staticData || null == staticData.getId()){
                throw new BizException(I18nUtils.get("card.fall.look.up",getLang(request)));
            }
            result.put(key,staticData.getValue());
        }
        return result;
    }

    /**
     * 三方api文档 和实际返回参数不一致, 需要做特殊处理
     * @param latpayResult
     * @return
     */
    private String dealWithThirdPartyError(JSONObject latpayResult) {
        String cardType = latpayResult.getString("Cardbrand");
        if (StringUtils.isNotBlank(cardType)) {
            //三方坑我们!!! 接口文档 Card Brand 字段为: CardBrand, 但是实际返回Cardbrand, 防止对方不通知就改接口参数
            cardType = StringUtils.isNotBlank(cardType) ? cardType : latpayResult.getString("CardBrand");
            //三方坑我们!!! 说好了如果是美国运通卡返回AMEX 其实是 AMERICAN EXPRESS
            cardType = cardType.equals("MASTERCARD") ? "MAST" : cardType;
            cardType = cardType.equals("AMERICAN EXPRESS") ? "AMEX" : cardType;
        }
        return cardType;
    }

    private String processDisplayCardType(JSONObject card, HttpServletRequest request) {
        String cardType = card.getString("customerCcType");
        //卡付款先后类型: 信用卡/借记卡 (CREDIT/DEBIT)
        String cardPayType = card.getString("cardPayType");
        //卡分类: 标准/传统/美国运通/标准预付费/商务卡(STANDARD TRADITIONAL AMERICAN EXPRESS STANDARD PREPAID BUSINESS )
        String cardCategory = card.getString("cardCategory");
        //获取数据字典信息;
        Map<String, List<StaticData>> staticDataMap = staticDataService.findByCodeList(new String[]{"cardType", "cardPayType", "cardCategory"});
        JSONObject codeValue = new JSONObject(5);
        codeValue.put("cardType",cardType);
        codeValue.put("cardCategory",cardCategory);
        codeValue.put("cardPayType",cardPayType);
        // 前端显示的排序顺序为: cardType cardPayType cardCategory
        String template = "-{1}- -{2}- -{3}-";
        for (String key : codeValue.keySet()) {
            //数据字典name字段为latpay 字段, enName为前端显示字段
            Map<String, StaticData> dataMap = staticDataMap.get(key).stream().collect(Collectors.toMap(StaticData::getValue, staticData -> staticData));
            String valueString = codeValue.getString(key);
            StaticData staticData = StringUtils.isNotBlank(valueString) ? dataMap.get(valueString) : null;
            template = this.makeDisplayType(key,template,null != staticData ? staticData.getEnName() : "");
        }
        return template;
    }

    /**
     * 封装 前端显示用card type 信息, 有顺序要求
     * 前端显示的排序顺序为: cardType cardPayType cardCategory
     * @param key
     * @param template
     * @param enName
     * @return
     */
    private String makeDisplayType(String key, String template, String enName) {
        if (key.equals("cardType")){
            template = template.replace("-{1}-",enName);
        }else if (key.equals("cardPayType")){
            template = template.replace("-{2}-",enName);
        }else {
            //如果cardCategory为Unknow, 则前端不显示
            enName = !enName.equals("Unknow") ? enName : "";
            template = template.replace("-{3}-",enName);
        }
        return template;
    }
    @Override
    public Object queryCardInfoByToken(String tokenList, HttpServletRequest request) {
        JSONObject result = new JSONObject();
        List<String> binList = new ArrayList<>();
        String[] tokenStr = tokenList.split(",");
        Map<String, Object> params = new HashMap<>(1);
        params.put("type", StaticDataEnum.GATEWAY_TYPE_0.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        JSONObject requestParam = new JSONObject();
        requestParam.put("Merchant_id", gatewayDTO.getChannelMerchantId());
        requestParam.put("merchantpwd", gatewayDTO.getPassword());
        requestParam.put("AccountType", "A");
        //更新卡信息的表示位 SCSS_U
        requestParam.put("RequestType", "SCSS_Q");
        for (String latPayToken : tokenStr) {
            requestParam.put("CrdStrg_Token", latPayToken);
            JSONObject latpayResult = null;
            try {
                latpayResult = JSONResultHandle.resultHandle(HttpClientUtils.sendPostForm(tieOnCardUrl, requestParam));
                String bin = latpayResult.getJSONObject("data").getString("CardBin");
                if (StringUtils.isNotBlank(bin)){
                    binList.add(bin);
                }
                result.put(latPayToken,latpayResult);
            } catch (Exception e) {
                log.info("latpay查询卡信息三方接口请求-失败, CC", requestParam, latpayResult, e.getMessage(), e);
            }
        }
        result.put("binList",binList);
        return result;
    }

    @Override
    public Object queryCardTypeByBin(List<String> binList, HttpServletRequest request) {
        JSONObject result = new JSONObject();
        //获取通道路由信息
        JSONObject params = new JSONObject(2);
        params.put("type", StaticDataEnum.GATEWAY_TYPE_0.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
        //封装请求参数 三方接口文档地址: https://docs.qq.com/pdf/DWlptcFVzanlIdEh4
        JSONObject requestParam = new JSONObject(8);
        requestParam.put("Merchant_User_Id", gatewayDTO.getChannelMerchantId());
        requestParam.put("MerchantPwd", gatewayDTO.getPassword());
        requestParam.put("TransactionType", "BNLKP");
        requestParam.put("Merchant_Ref_Number", SnowflakeUtil.generateId().toString());
        for (String bin : binList) {
            requestParam.put("BinNumber", bin);
            try {
                JSONObject latpayResult = JSONResultHandle.resultHandle(HttpClientUtils.sendPostForm(cardBinLookupUrl, requestParam));
                result.put(bin,latpayResult);
            } catch (Exception e) {

            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void presetCard(JSONObject data, Long userId, HttpServletRequest request) throws Exception{
        String cardId = data.getString("cardId");
        if (StringUtils.isBlank(cardId)){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
//        Long userId = getUserId(request);
        String isCreditCard = data.getString("isCreditCard");
        JSONObject param=new JSONObject(3);
        param.put("userId",userId);
        serverService.presetCard(data);

        // 通知分期付已绑定卡 异步 edit zhangzeyuan 不能用异步  app端调完接口会立即调用app/user 查询状态
        if (StringUtils.isNotEmpty(isCreditCard)){
            param.put("isBindCard",StaticDataEnum.CREAT_BIND_CARD_1.getCode());
            try{
                serverService.updateCreditCard(param,request);
            }catch (Exception e){
                log.error("通知分期付已绑定卡 出错,e:{}", e.getMessage());
            }
            // 修改用户installmentState为 1
            UserDTO userDTO=new UserDTO();
            userDTO.setInstallmentState(StaticDataEnum.USER_BUSINESS_1.getCode());
            this.updateUser(userId,userDTO,request);
            // 添加用户分期付进行状态 2021-6-15 需求 开通分期付成功
            JSONObject stateParam=new JSONObject();
            stateParam.put("userId",userId);
            stateParam.put("state",StaticDataEnum.ILLION_SUBMIT_LOG_STATUS_11.getCode());
            illionSubmitStatusLogService.addSubmitStatusLog(stateParam,request);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Async("taskExecutor")
    public Future<JSONObject> riskCheckNew(JSONObject data, HttpServletRequest request) throws Exception {
        JSONObject returnData = new JSONObject();
        List<AppAboutUsDTO> appAboutUsDTOS = appAboutUsService.find(null, null, null);
        //查询用户信息
        UserDTO userDTO = findUserById(data.getLong("userId"));
        if (userDTO.getInstallmentState().equals(StaticDataEnum.USER_BUSINESS_1.getCode())) {
            throw new BizException(I18nUtils.get("kyc.has.passed", getLang(request)));
        }
        if(StringUtils.isNotEmpty(data.getString("isAddAddress"))){
            HashMap<String, Object> objectObjectHashMap = Maps.newHashMapWithExpectedSize(2);
            objectObjectHashMap.put("code", "kysSelectUser");
            //todo 重复姓名生日挡板
            StaticDataDTO staticDataById11 = staticDataService.findOneStaticData(objectObjectHashMap);
            if(null != staticDataById11 && null != staticDataById11.getId() && StringUtils.isNotBlank(staticDataById11.getValue())){

                if(staticDataById11.getValue().equals("1")){
                    // 202108-02需求用户姓名生日重复为直接返回
                    String userFirstName =this.toCaseName(data.getString("userFirstName")) ;
                    String userLastName =this.toCaseName(data.getString("userLastName")) ;
                    String birth = data.getString("birth");
                    JSONObject userData=new JSONObject();
                    userData.put("userFirstName",userFirstName);
                    userData.put("userLastName",userLastName);
                    userData.put("birth",birth);
                    List<UserDTO> userDTOS=serverService.findUserInfoByParam(userData);
                    Long userId = getUserId(request);
                    if(userDTOS!=null&&userDTOS.size()>0) {
                        for (UserDTO dto : userDTOS) {
                            Long id = dto.getId();
                            if (id != null) {
                                if (!userId.equals(id)) {
                                    // 做过kyc驾驶证或者护照医保卡数据不为空
                                    if (dto.getMedicare() != null || dto.getDriverLicence() != null || dto.getPassport() != null) {
                                        returnData.put("status", StaticDataEnum.STATUS_REPEAT_USER.getCode());
                                        returnData.put("chanceNum", KYC_CHANCE);
                                        returnData.put("chance", 0);
                                        returnData.put("phone", appAboutUsDTOS.get(0).getPhone());
                                        return new AsyncResult<>(returnData);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }



        //判断kyc次数
        int chance;
        long userKycChancePeriod = 86400L;
        String userKycKey = userDTO.getId() + "_kyc_" + LocalDate.now();
        HashMap<String, Object> objectObjectHashMap = Maps.newHashMapWithExpectedSize(1);
        objectObjectHashMap.put("code", "KYCTest");
        StaticDataDTO staticDataById = staticDataService.findOneStaticData(objectObjectHashMap);
        if (null != staticDataById && null!=staticDataById.getId()){
            String value = staticDataById.getValue();
            String name = staticDataById.getName();
            String enName = staticDataById.getEnName();
            if (StringUtils.isNoneEmpty(new String[]{value,name,enName})) {
                returnData.put("status", value);
                returnData.put("chanceNum", name);
                returnData.put("chance", enName);
                returnData.put("phone", appAboutUsDTOS.get(0).getPhone());
                return new AsyncResult<>(returnData);
            }
        }
        if (redisUtils.hasKey(userKycKey)) {
            chance = (int) redisUtils.get(userKycKey);
            if (chance == 0) {
                // 次数上限
                returnData.put("status", StaticDataEnum.STATUS_UPPER_LIMIT.getCode());
                returnData.put("chanceNum", KYC_CHANCE);
                returnData.put("chance", chance);
                returnData.put("phone", appAboutUsDTOS.get(0).getPhone());
                return new AsyncResult<>(returnData);
            }
        } else {
            chance = KYC_CHANCE;
            redisUtils.set(userKycKey, chance, userKycChancePeriod);
        }
        //信息补充
        data.put("phone", userDTO.getPhone());
        // 上送KYC订单号
        Long kycNo = SnowflakeUtil.generateId();
        // 下一层需要kycno
        data.put("kycNo",kycNo);
        // 创建日志
        KycSubmitLogDTO kycSubmitLogDTO = new KycSubmitLogDTO();
        kycSubmitLogDTO.setId(kycNo);
        kycSubmitLogDTO.setKycData(JSONObject.toJSONString(data));
        kycSubmitLogDTO.setAccountSubmittedTimes(KYC_CHANCE-chance+1);
        // 设置默认未请求三方，
        kycSubmitLogDTO.setIsRequest(StaticDataEnum.KYC_SUBMIT_STATUS0.getCode());
        kycSubmitLogDTO.setPhone(userDTO.getPhone());
        kycSubmitLogDTO.setUserId(userDTO.getId());
        //请求数据拼装
        JSONObject requestInfo = getRiskRequestInfo(data, userDTO);
        userDTO.setEmail(data.getString("email"));
        userDTO.setUserFirstName(data.getString("userFirstName"));
        userDTO.setUserLastName(data.getString("userLastName"));
        // 设置日志连接时间
        kycSubmitLogDTO.setDate(System.currentTimeMillis());
        // kyc风控校验
        try{
            log.info("初始化kyc记录对象,kycSubmitLogDTO:{}",kycSubmitLogDTO);
            kycSubmitLogService.insertKycSubmitLog(kycSubmitLogDTO,request);
        }catch (Exception e){
            log.error("记录kyc初始化对象异常:{}",e);
        }
        JSONObject riskResult =null;
        try{
            riskResult = kycRisk(requestInfo, request);
        }catch (Exception e){
            log.error("请求risk系统做kyc异常,e:{}",e);
            // 设置日志参数
            kycSubmitLogDTO.setIsRequest(StaticDataEnum.KYC_SUBMIT_STATUS0.getCode());
            kycSubmitLogDTO.setIsWatchlist(StaticDataEnum.WATCHLIST_STATUS_2.getCode());
            kycSubmitLogDTO.setKycStatus(StaticDataEnum.KYC_CHECK_STATE_4.getCode());
            kycSubmitLogDTO.setStatus(-1);
            this.updateKycSubmitLog(kycSubmitLogDTO,request);
            userStepService.userStepModifyAndUserStepLogSave(userDTO.getId(), StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_14.getCode(), null, null, data.toJSONString(), request);
            throw e;
        }
        //添加一个记录
        log.info("risk result : {}", riskResult);
        //todo 撤除挡板
        HashMap<String, Object> kycFailed = Maps.newHashMapWithExpectedSize(1);
        kycFailed.put("code", "kyc(Failed)");
        StaticDataDTO blockerNew = staticDataService.findOneStaticData(kycFailed);
        if(null != blockerNew && StringUtils.isNotBlank(blockerNew.getName())){
            riskResult.put("code",blockerNew.getName());
        }

        if (ErrorCodeEnum.DATA_SYSTEM_FAILED.getCode().equals(riskResult.getString("code"))
                || ErrorCodeEnum.FAIL_CODE.getCode().equals(riskResult.getString("code"))) {
            //系统异常
            returnData.put("status", StaticDataEnum.STATUS_SYSTEM_ERROR.getCode());
            returnData.put("chanceNum", KYC_CHANCE);
            returnData.put("chance", chance);
            returnData.put("phone", appAboutUsDTOS.get(0).getPhone());
            // 设置日志参数
            kycSubmitLogDTO.setIsRequest(StaticDataEnum.KYC_SUBMIT_STATUS0.getCode());
            kycSubmitLogDTO.setIsWatchlist(StaticDataEnum.WATCHLIST_STATUS_2.getCode());
            kycSubmitLogDTO.setKycStatus(StaticDataEnum.KYC_CHECK_STATE_4.getCode());
            kycSubmitLogDTO.setStatus(-1);
            this.updateKycSubmitLog(kycSubmitLogDTO,request);
            userStepService.userStepModifyAndUserStepLogSave(userDTO.getId(), StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_14.getCode(), null, null, data.toJSONString(), request);
            return new AsyncResult<>(returnData);
        }
        ;
        JSONObject riskResultData = JSONObject.parseObject(EncryptUtil.decrypt(riskResult.getString("data"), EncryptUtil.aesKey, EncryptUtil.aesIv));
        log.info("risk result data : {}", riskResultData);
        // 风控审核批次
        String riskBatch = riskResultData.getString("batchNo");
        String result;
        HashMap<String, Object> objectObjectHashMap1 = Maps.newHashMapWithExpectedSize(1);
        objectObjectHashMap1.put("code", "kyc(REJECT/ACCEPT)");
        StaticDataDTO blocker = staticDataService.findOneStaticData(objectObjectHashMap1);
        if(null != blocker && StringUtils.isNotBlank(blocker.getName())){
            result = blocker.getName();
        }else {
            result = riskResultData.getJSONObject("rule").getString("decision");
        }
        if (StaticDataEnum.RISK_CHECK_STATE_0.getMessage().equals(result)) {
            userDTO.setInstallmentState(StaticDataEnum.USER_BUSINESS_5.getCode());
            //同步用户信息 账户系统
            serverService.infoSupplement(data, request);
            //同步用户信息 分期付系统 未开通分期付不能同步信息
//            serverService.infoSupplementCredit(data, request);
            log.info("kyc update user info, user:{}", userDTO);
            // 更新用户状态
            updateUser(userDTO.getId(), userDTO, request);
            // 记录阶段日志
            userStepService.userStepModifyAndUserStepLogSave(userDTO.getId(), StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_11.getCode(), null, riskBatch, data.toJSONString(), request);
            // 清空kyc机会
            redisUtils.del(userKycKey);

            //kyc成功 发送消息
            kycSuccessSendMessage(userDTO, request);

            // 添加用户分期付进行状态 2021-6-15 需求 KYC通过设置状态未录入信息
            JSONObject stateParam=new JSONObject();
            stateParam.put("userId",getUserId(request));
            stateParam.put("state",StaticDataEnum.ILLION_SUBMIT_LOG_STATUS_0.getCode());
            illionSubmitStatusLogService.addSubmitStatusLog(stateParam,request);

        } else if (StaticDataEnum.RISK_CHECK_STATE_1.getMessage().equals(result)) {
            log.info("kyc update user info, user:{}", userDTO);
            // 失败设置kyc次数
            chance = chance - 1;
            redisUtils.set(userKycKey, chance, userKycChancePeriod);
            // 记录阶段日志
            userStepService.userStepModifyAndUserStepLogSave(userDTO.getId(), StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_12.getCode(), null, riskBatch, data.toJSONString(), request);
//            sendMessage(userDTO, StaticDataEnum.SEND_NODE_5.getCode(), request);
            // 记录日志
            //发送失败消息
            kycFailSendMessage(userDTO, request);

            // 剩1次机会
            if (chance==1){
                returnData.put("status", StaticDataEnum.STATUS_ONE_MORE.getCode());
                returnData.put("chanceNum", KYC_CHANCE);
                returnData.put("chance", chance);
                returnData.put("phone", appAboutUsDTOS.get(0).getPhone());
                return new AsyncResult<>(returnData);
            }
        } else if (StaticDataEnum.RISK_CHECK_STATE_2.getMessage().equals(result)) {
            // 创建理财审核记录
            RiskApproveLogDTO riskApproveLogDTO = new RiskApproveLogDTO();
            riskApproveLogDTO.setUserId(data.getLong("userId"));
            riskApproveLogDTO.setData(data.toJSONString());
            riskApproveLogService.saveRiskApproveLog(riskApproveLogDTO, request);
            userDTO.setInstallmentState(StaticDataEnum.USER_BUSINESS_3.getCode());
            // 同步用户信息
            log.info("kyc update user info, user:{}", userDTO);
            serverService.infoSupplement(data, request);
            updateUser(userDTO.getId(), userDTO, request);
            // 记录阶段日志
            userStepService.userStepModifyAndUserStepLogSave(userDTO.getId(), StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_13.getCode(), null, riskBatch, data.toJSONString(), request);
            // 清空kyc机会
            redisUtils.del(userKycKey);
            //跟产品确认 人审 暂不发送消息
//            sendMessage(userDTO, StaticDataEnum.SEND_NODE_4.getCode(), request);
        }
        //返回结果
        returnData.put("chance", chance);
        returnData.put("phone", appAboutUsDTOS.get(0).getPhone());
        returnData.put("chanceNum", KYC_CHANCE);
        if (result.equals(StaticDataEnum.RISK_CHECK_STATE_0.getMessage())) {
            returnData.put("status", StaticDataEnum.STATUS_SUCCESS.getCode());
        }else if (result.equals(StaticDataEnum.RISK_CHECK_STATE_2.getMessage())){
            // 人审
            returnData.put("status", StaticDataEnum.STATUS_MANUAL_AUDIT.getCode());
        } else {
            if (chance == 1) {
                returnData.put("status", StaticDataEnum.STATUS_ONE_MORE.getCode());
            } else if (chance == 0){
                returnData.put("status", StaticDataEnum.STATUS_UPPER_LIMIT.getCode());
            }else {
                returnData.put("status", StaticDataEnum.STATUS_GENERAL_ERROR.getCode());
            }
        }
        returnData.put("result", result);
        return new AsyncResult<>(returnData);
    }

    @Override
    public void clearPushToken(Long userId, HttpServletRequest request) throws BizException{
        if (null == userId ){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        UserDTO userById = this.findUserById(userId);
        if (null == userById || null == userById.getId()){
            throw new BizException(I18nUtils.get("user.rule.userNameNotPresence", getLang(request)));
        }
        //将pushToken置为空值, 发送位置判断为 非null, 非空, 所以空值不会触发发送操作
        this.updateUser(userId,UserDTO.builder().pushToken(" ").build(),request);
    }
    void updateKycSubmitLog(KycSubmitLogDTO kycSubmitLogDTO, HttpServletRequest request){
        try{
            kycSubmitLogDTO.setUserId(getUserId(request));
            kycSubmitLogService.updateKycSubmitLog(kycSubmitLogDTO.getId(),kycSubmitLogDTO,request);
        }catch (Exception e){
            log.error("记录kyc记录异常:{}",e);
        }
    }

    @Override
    public JSONObject getReceived(JSONObject data, HttpServletRequest request) throws BizException {
        JSONObject result=new JSONObject();
        Long userId = getUserId(request);
//        Long userId = data.getLong("userId");
        UserDTO userById = this.findUserById(userId);
        if (userById==null){
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }
        Long startTime = 0L;
        long endTime = System.currentTimeMillis();
        // 查询记录表如果没有查询到，以当前时间之前的所有数据，查询条件最大时间撮，已读一条记录 取时间撮
        JSONObject params=new JSONObject();
        params.put("userId",userById.getId());
        params.put("isShow",StaticDataEnum.MARKETING_IS_SHOW_1.getCode());
        // 查询最大时间撮未读记录
        MarketingLogDTO maxTimedTO = marketingLogService.findMaxTime(params);
        if (maxTimedTO!=null){
            startTime=maxTimedTO.getTime();
        }
        JSONObject param=new JSONObject();
        BigDecimal money=new BigDecimal(0.00);
        param.put("id",userId);
        param.put("start",startTime);
        param.put("end",endTime);
        List<String> userNameList=new ArrayList<>();
        // 根据时间撮查询多条记录
        List<JSONObject> received = userDAO.getReceived(param);
        for (JSONObject jsonObject : received) {
            BigDecimal amount = jsonObject.getBigDecimal("amount");
            if (amount!=null){
                money=amount.add(money);
            }
            if (userNameList.size()<3){
                String lastName = jsonObject.getString("lastName");
                String firstName = jsonObject.getString("firstName");
                userNameList.add((firstName==null?"":firstName)+" "+(lastName==null?"":lastName));
            }
        }
        // 封装前端需要用户姓名 大于三个后面+...
        String name="";
        for (String s : userNameList) {
            name=(name==""?s:(name+", "+s));
        }
        String str=(StringUtils.isBlank(name)?"":(userNameList.size()<3?name:name+"..."));
        result.put("info",str);
        result.put("money",money.intValue());
        MarketingLogDTO marketingLogDTO = new MarketingLogDTO();
        marketingLogDTO.setAmount(money);
        marketingLogDTO.setUserId(userById.getId());
        marketingLogDTO.setTime(endTime);
        marketingLogDTO.setUserNameList(name);
        // 保存一条记录 状态为未读
        marketingLogService.saveMarketingLog(marketingLogDTO,request);
        return result;
    }

    @Override
    public JSONObject saveReceivedIsShow(JSONObject data, HttpServletRequest request) throws BizException {
        Long userId = getUserId(request);
        // 该接口不需要返回数据，返回null即可
//        Long userId = data.getLong("userId");
        UserDTO userById = this.findUserById(userId);
        if (userById==null){
            // 抛异常前端不做处理，无需关心异常
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }
        JSONObject params=new JSONObject();
        params.put("userId",userById.getId());
        params.put("isShow",StaticDataEnum.MARKETING_IS_SHOW_2.getCode());
        MarketingLogDTO maxTimedTO = marketingLogService.findMaxTime(params);
        if (maxTimedTO==null){
            return null;
        }
        maxTimedTO.setIsShow(StaticDataEnum.MARKETING_IS_SHOW_1.getCode());
        marketingLogService.updateMarketingLog(maxTimedTO.getId(),maxTimedTO,request);
        return null;
    }

    /**
     * 根据传入经纬度添加用户所在州城市（州为数据字典territoryState的value） city 为三方返回字符串
     * @param param
     * @param request
     * @throws Exception
     */
    @Async("taskExecutor")
    @Override
    public void getUserStateCityByLongitude(JSONObject param, HttpServletRequest request)  {
        try{
            String stateName = param.getString("stateName");
            String cityName = param.getString("cityName");
            String street= param.getString("street");
            Long userId = getUserId(request);
            String lat = param.getString("lat");
            String lng = param.getString("lng");
            JSONArray fullAddress = param.getJSONArray("fullAddress");
//        Long userId = param.getLong("userId");
            UserDTO oneUser = this.findUserById(userId);
            if (oneUser==null){
                throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
            }
            // 判断用户所在城市州是否存在 不存在则修改
            if (StringUtils.isBlank(oneUser.getUserCity())||StringUtils.isBlank(oneUser.getUserState())){
                if (StringUtils.isAllBlank(stateName,cityName)){
                    throw new BizException(I18nUtils.get("google.find.state.error", getLang(request)));
                }
                JSONObject params =new JSONObject(2);
                params.put("code","territoryState");
                // 查一个
                List<StaticDataDTO> staticDataDTOS = staticDataService.find(params,null,null);
                x: for (StaticDataDTO staticDataDTO : staticDataDTOS) {
                    String enName = staticDataDTO.getEnName();
                    if (stateName.equals(enName)){
                        oneUser.setUserState(staticDataDTO.getValue());
                        break x;
                    }
                }
                oneUser.setUserCity(cityName);
                this.updateUser(userId,oneUser,request);
            }
            UserLocationRecordDTO userLocationRecordDTO = new UserLocationRecordDTO();
            userLocationRecordDTO.setLat(lat);
            userLocationRecordDTO.setLng(lng);
            userLocationRecordDTO.setStreet(street);
            userLocationRecordDTO.setUserCity(cityName);
            userLocationRecordDTO.setUserState(stateName);
            userLocationRecordDTO.setUserId(userId);
            userLocationRecordDTO.setFulladdress(fullAddress==null?"":fullAddress.toJSONString());
            userLocationRecordService.saveUserLocationRecord(userLocationRecordDTO,request);
            // 往记录表里新增与一条用户位置信息记录表
        }catch (Exception e){
            log.error("修改用户所在州市位置信息异常,e:{}",e);
        }


    }

    @Override
    public JSONObject verifyCode(JSONObject param, HttpServletRequest request) throws BizException {
        if (param==null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        String phone = param.getString("phone");
        String code = param.getString("code");
        if (StringUtils.isAllBlank(phone,code)){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        String phoneCode = new StringBuilder(phone).substring(0, 2);
        String phones = new StringBuilder(phone).substring(2, phone.length());
        if (phoneCode.equals(StaticDataEnum.CHN.getMessage())) {
            if (phones.length() > Validator.PHONE_LENGTH) {
                throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
            }
        } else {
            if (phones.length() > Validator.PHONE_AUD_LENGTH) {
                throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
            }
        }
        String key = phone + "_" + StaticDataEnum.USER_TYPE_10.getCode();
        String sendNode = param.getString("sendNode");
        // 用户修改pin码 节点38
        if (StringUtils.isNotBlank(sendNode)){
            if (Integer.parseInt(sendNode) == StaticDataEnum.SEND_NODE_38.getCode()){
                key=phone + "_" + StaticDataEnum.USER_TYPE_10.getCode()+"_"+StaticDataEnum.SEND_NODE_38.getCode();
            }
        }
        Object emailRedis = redisUtils.get(key);
        log.info("saveUser redis message : " + emailRedis);
        if (emailRedis == null) {
            throw new BizException(I18nUtils.get("verification.code.expired", getLang(request)));
        }
        String securityCode = emailRedis.toString();
        if (!code.equals(securityCode)) {
            throw new BizException(I18nUtils.get("incorrect.verification.code", getLang(request)));
        }
        JSONObject result=new JSONObject(2);
        result.put("isCode",true);
        return result;
    }

    public void sendEmail(UserDTO userDTO, HttpServletRequest request){
        JSONObject param=new JSONObject(1);
        param.put("postcode",userDTO.getPostcode());
        JSONObject jsonObject = FindStateByPostCodeUtils.processStateInfo(param);
        String state = jsonObject.getString("state");
        if ("QLD".equals(state)){
            // qld用户
            // 查询邮件模板
            MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(StaticDataEnum.SEND_NODE_36.getCode() + "");
            String[] params = {userDTO.getEmail()};
            //邮件内容
            String sendMsg = mailTemplateDTO.getEnSendContent();
            String sendTitle = mailTemplateDTO.getEnMailTheme();
            try {
                //发送邮件
                Session session = MailUtil.getSession(sysEmail);
                MimeMessage mimeMessage = MailUtil.getMimeMessage(StaticDataEnum.U_WALLET.getMessage(), sysEmail, userDTO.getEmail(), sendTitle, sendMsg, null, session);
                MailUtil.sendMail(session, mimeMessage, sysEmail, sysEmailPwd);
                //记录邮件流水
                saveMailLog(userDTO.getEmail(), sendMsg, 0, request);
            } catch (Exception e) {
                log.info("UserService.saveUser,发送邮件异常" + e.getMessage(), e);
            }
        }else {
            MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(StaticDataEnum.SEND_NODE_37.getCode() + "");
            String[] params = {userDTO.getEmail()};
            //邮件内容
            String sendMsg = mailTemplateDTO.getEnSendContent();
            String sendTitle = mailTemplateDTO.getEnMailTheme();
            try {
                //发送邮件
                Session session = MailUtil.getSession(sysEmail);
                MimeMessage mimeMessage = MailUtil.getMimeMessage(StaticDataEnum.U_WALLET.getMessage(), sysEmail, userDTO.getEmail(), sendTitle, sendMsg, null, session);
                MailUtil.sendMail(session, mimeMessage, sysEmail, sysEmailPwd);
                //记录邮件流水
                saveMailLog(userDTO.getEmail(), sendMsg, 0, request);
            } catch (Exception e) {
                log.info("UserService.saveUser,发送邮件异常" + e.getMessage(), e);
            }
        }
    }


    /**
     * 用户注册发送新邮件
     * @author zhangzeyuan
     * @date 2021/7/1 15:41
     * @param userDTO
     * @param request
     */
    public void registerSuccessToSendEmail(UserDTO userDTO, HttpServletRequest request){
        // 查询邮件模板
        MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(StaticDataEnum.SEND_NODE_40.getCode() + "");
        if(null == mailTemplateDTO || null == mailTemplateDTO.getId() || StringUtils.isBlank(mailTemplateDTO.getEnSendContent())){
            log.info("用户注册成功获取模板内容异常");
            return;
        }
        //邮件内容
        String sendMsg = mailTemplateDTO.getEnSendContent().replace("{userNameReplaceParam}",userDTO.getUserFirstName() + " " + userDTO.getUserLastName());

        String sendTitle = mailTemplateDTO.getEnMailTheme();
        try {
            //发送邮件
            Session session = MailUtil.getSession(sysEmail);
            MimeMessage mimeMessage = MailUtil.getMimeMessage(StaticDataEnum.U_WALLET.getMessage(), sysEmail, userDTO.getEmail(), sendTitle, sendMsg, null, session);
            MailUtil.sendMail(session, mimeMessage, sysEmail, sysEmailPwd);
            //记录邮件流水
            saveMailLog(userDTO.getEmail(), sendMsg, 0, request);
        } catch (Exception e) {
            log.info("UserService.saveUser,发送邮件异常" + e.getMessage(), e);
        }

    }

    /**
     * 获取用户信息 新增分期付绑卡状态
     * @author zhangzeyuan
     * @date 2021/6/30 18:21
     * @param id
     * @return com.uwallet.pay.main.model.dto.UserDTO
     */
    @Override
    public UserDTO findUserInfoV2(Long id){
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        UserDTO userDTO = userDAO.selectOneDTO(params);
        //查询分期付绑卡状态
        if(Objects.nonNull(userDTO) && Objects.nonNull(userDTO.getId()) && userDTO.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()){
            int creditCardState = 0;
            //分期付绑卡状态
            String url = creditUrl + "/user/getCreditBindCardState/";
            JSONObject paramJson = new JSONObject(1);
            paramJson.put("userId", id);
            try {
                String result = HttpClientUtils.post(url, paramJson.toJSONString());
                JSONObject resultJSON = JSONObject.parseObject(result);
                String code = resultJSON.getString("code");

                if(StringUtils.isNotBlank(code) && code.equals(ResultCode.OK.getCode())){
                    Integer data = resultJSON.getInteger("data");
                    if(Objects.nonNull(data)){
                        creditCardState = data;
                    }
                }
            }catch (Exception e){
                log.error("获取分期付绑卡状态出错",e.getMessage());
            }

            if(userDTO.getInstallmentState().equals(1) && creditCardState == 0){
                userDTO.setInstallmentState(0);
                creditCardState = 0;
            }

            if(creditCardState == 2){
                //老用户未绑卡
                creditCardState = 0;
            }

            userDTO.setCreditCardState(creditCardState);
        }
        return userDTO;
    }

    /**
     * 获取用户信息 新增分期付绑卡状态
     * @author caisj
     * @date 2021/9/14 18:21
     * @param id
     * @return com.uwallet.pay.main.model.dto.UserDTO
     */
    @Override
    public UserDTO findUserInfoV3(Long id){
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        UserDTO userDTO = userDAO.selectOneDTO(params);
        return userDTO;
    }

    @Override
    public void sendMessage(JSONObject param, HttpServletRequest request) throws BizException {
        String phone = param.getString("phone");
        if (StringUtils.isEmpty(phone)){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        // todo key换
        String key=phone + "_" + StaticDataEnum.USER_TYPE_10;
        if (redisUtils.hasKey(key)) {
            // 存在判断是否大于20秒直接返回
            long expire = redisUtils.getExpire(key);
            if ((sendMessageCode-expire)<=sendMessageTwo){
                throw new BizException(I18nUtils.get("sms.code.repeat.send", getLang(request)));
            }
        }
        // 不存在发送验证码
        String phoneCode = new StringBuilder(phone).substring(0, 2);
        if (phoneCode.equals(StaticDataEnum.AU.getMessage())) {
            String phones = new StringBuilder(phone).substring(2, phone.length());
            if (phones.length() > Validator.PHONE_AUD_LENGTH) {
                throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
            }
        } else {
            throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
        }
        int securityCode = 666666;
        MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(StaticDataEnum.SEND_NODE_1.getCode()+"");
        if (phoneCode.equals(StaticDataEnum.CHN.getMessage())) {
            JSONArray phoneArray = new JSONArray();
            phoneArray.add(phone);
            JSONObject sendParams = new JSONObject();
            sendParams.put("code", securityCode);
            aliyunSmsService.sendChinaSms(phone, mailTemplateDTO.getAliCode(), sendParams);
            redisUtils.set(key, securityCode, sendMessageCode);
        } else {
            //设置模板参数
            String[] paramSend = {securityCode + ""};
            //短信内容
            String sendMsg = this.templateContentReplace(paramSend, mailTemplateDTO.getEnSendContent());
            aliyunSmsService.sendInternationalSms(phone, sendMsg);
            redisUtils.set(key, securityCode, sendMessageCode);
        }

    }

    @Override
    public JSONObject h5Login(JSONObject data, HttpServletRequest request) throws Exception {
        // 参数校验
        String phone = data.getString("phone");
        String code = data.getString("code");
        if (StringUtils.isAllBlank(phone,code)){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        String phoneCode = new StringBuilder(phone).substring(0, 2);
        if (phoneCode.equals(StaticDataEnum.CHN.getMessage())) {
            if (phoneCode.length() > Validator.PHONE_LENGTH) {
                throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
            }
        } else {
            if (phoneCode.length() > Validator.PHONE_AUD_LENGTH) {
                throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
            }
        }
        String key=phone + "_" + StaticDataEnum.USER_TYPE_10;
        if (!redisUtils.hasKey(key)) {
            throw new BizException(I18nUtils.get("verification.code.expired", getLang(request)));
        }
        // 判断验证码
        String messageCode = redisUtils.get(key).toString();
        if (!messageCode.equals(code)){
            throw new BizException(I18nUtils.get("incorrect.verification.code", getLang(request)));
        }
        // 查询用户是否存在，存在返回用户分期付状态，
        Map<String, Object> params = new HashMap<>(2);
        params.put("phone", phone);
        params.put("userType", StaticDataEnum.USER_TYPE_10.getCode());
        UserDTO oneUser = this.findOneUser(params);
        // 添加账号冻结逻辑
        JSONObject result=new JSONObject();
        result.put("frozenState",0);
        if (oneUser.getId()!=null){
            Long userId = oneUser.getId();
            JSONObject param=new JSONObject();
            param.put("userId",userId);
            LoginMissDTO oneLoginMiss = loginMissService.findOneLoginMiss(param);
            if (oneLoginMiss==null||oneLoginMiss.getId()==null){
                result.put("frozenState",1);
            }
        }
        if (oneUser.getId()!=null){
            String token = JwtUtils.signH5(oneUser.getId(), phone, redisUtils);
            result.put("Authorization", token);
            result.put("isOld",StaticDataEnum.USER_TYPE_OLD.getCode());
            result.put("installmentState",oneUser.getInstallmentState());
            redisUtils.del(key);
//            if (oneUser.getInstallmentState()!=null){
//                int i = oneUser.getInstallmentState().intValue();
//                if (i!=0){
//
//                }
//            }
            // 删除验证码
        }else {
            result.put("installmentState",0);
            result.put("isOld",StaticDataEnum.USER_TYPE_NEW.getCode());
        }
        return result;
    }

    @Override
    public JSONObject verify(JSONObject data, HttpServletRequest request) throws Exception {
        Long userId = getUserId(request);
        if (userId==0||userId==null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        JSONObject creditUserState = serverService.findCreditUserState(userId);
//        UserDTO userById = this.findUserById(userId);
//        Integer bindCardStatus=0;
//        if (userById!=null){
//            bindCardStatus= userById.getCardState()==null?0:userById.getCardState();
//        }

        List<CardDTO> stripeCardList = serverService.getStripeCardList(userId.toString());
        if(CollectionUtils.isEmpty(stripeCardList)){
            //返回空
            creditUserState.put("bindCardStatus",0);
            return creditUserState;
        }
        creditUserState.put("bindCardStatus",1);
        return creditUserState;
    }

    @Override
    public JSONObject findUserInfo(HttpServletRequest request) throws Exception {
        Long userId = getUserId(request);
        if (userId==null||userId==0){
            throw new BizException(I18nUtils.get("query.failed", getLang(request)));
        }
        return serverService.userInfoByQRCode(userId);
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public JSONObject userRegister(JSONObject requestInfo, HttpServletRequest request) throws Exception {


        if (requestInfo==null) {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        // 参数校验
        String userLastName = requestInfo.getString("userLastName");
        String userMiddleName = requestInfo.getString("userMiddleName");
        String email = requestInfo.getString("email");
        String birth = requestInfo.getString("birth");
        String userFirstName = requestInfo.getString("userFirstName");
        String phone = requestInfo.getString("phone");
        String code = requestInfo.getString("code");
        Integer gender = requestInfo.getInteger("sex");
        if (StringUtils.isAllBlank(userLastName,email,birth,userFirstName)){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        birth = requestInfo.getString("birth").replaceAll("/", "-");
        requestInfo.put("birth", birth);
        if (StringUtils.isEmpty(userFirstName) || userFirstName.length() > Validator.TEXT_LENGTH_100) {
            throw new BizException(I18nUtils.get("name.length.error", getLang(request)));
        }
        if (StringUtils.isEmpty(userLastName) || userLastName.length() > Validator.TEXT_LENGTH_100) {
            throw new BizException(I18nUtils.get("name.length.error", getLang(request)));
        }
        if (!Validator.isEmail(email)){
            throw new BizException(I18nUtils.get("email.format.error", getLang(request)));
        }
        if (phone!=null){
            String phoneCode = new StringBuilder(phone).substring(0, 2);
            String phones = new StringBuilder(phone).substring(2, phone.length());
            if (phoneCode.equals(StaticDataEnum.CHN.getMessage())) {
                if (phones.length() > Validator.PHONE_LENGTH) {
                    throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
                }
            } else {
                if (phones.length() > Validator.PHONE_AUD_LENGTH) {
                    throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
                }
            }
        }
        //2021-02-04添加逻辑账号全部小写
        email=email.toLowerCase();
        // 查询用户id 判断是否已经注册的 如果已经注册更新用户信息,未注册将注册该用户
        // 新用户需要判断重复邮箱注册
        Long userId = getUserId(request);
        UserDTO userDTO=new UserDTO();
        // 判断当前通道
        JSONObject payGateWay = gatewayService.getPayGateWay(request);
        Integer type = payGateWay.getInteger("type");
        if (type==null){
            throw new BizException(I18nUtils.get("register.failed", getLang(request)));
        }
        if (type.intValue()==StaticDataEnum.GATEWAY_TYPE_0.getCode()){
            userDTO.setStripeState(StaticDataEnum.USER_STRIPE_STATE_1.getCode());
        }else if (type.intValue()==StaticDataEnum.GATEWAY_TYPE_8.getCode()){
            userDTO.setStripeState(StaticDataEnum.USER_STRIPE_STATE_0.getCode());
        }

        userDTO.setUserType(StaticDataEnum.USER_TYPE_10.getCode());
        userDTO.setBirth(birth);
        userDTO.setUserFirstName(userFirstName);
        userDTO.setUserLastName(userLastName);
        userDTO.setEmail(email);
        userDTO.setUserMiddleName(userMiddleName);
        userDTO.setIsH5(true);
        userDTO.setRegisterFrom(StaticDataEnum.USER_REGISTER_H5.getCode());
        userDTO.setChannel(StaticDataEnum.ACCOUNT_CHANNEL_0001.getCode()+"");
        userDTO.setSex(gender);
        //H5注册用户默认勾选卡
        userDTO.setCreditCardAgreementState(1);
        // 同步账户系统值
        JSONObject data=new JSONObject();
        data.put("email",email);
        data.put("userFirstName",userFirstName);
        data.put("userLastName",userLastName);
        data.put("birth",birth);
        data.put("userMiddleName",userMiddleName);
        data.put("sex",gender);
        // 返回值
        JSONObject result=new JSONObject();
        if (userId==null||userId==0){
            // 校验验证码
            String key=phone + "_" + StaticDataEnum.USER_TYPE_10;
            if (!redisUtils.hasKey(key)) {
                throw new BizException(I18nUtils.get("verification.code.expired", getLang(request)));
            }
            // 判断验证码
            String messageCode = redisUtils.get(key).toString();
            if (!messageCode.equals(code)){
                throw new BizException(I18nUtils.get("incorrect.verification.code", getLang(request)));
            }
            redisUtils.del(key);
            userDTO.setPhone(phone);
            Long id =null;
            synchronized (userDTO) {
                id= doUserRegister(userDTO, request);
            }
            String token = JwtUtils.signH5(id, phone, redisUtils);
            result.put("Authorization", token);
            return result;
        }else {
            // 如果token中取不出手机号则用前端传入的作为手机号
            String jwt = request.getHeader("Authorization");
            if (StringUtils.isNotEmpty(jwt)) {
                phone = JwtUtils.getUsername(jwt.replace("Bearer ", ""));
            }
            userDTO.setPhone(phone);
            // 同步支付系统
            updateUser(userId,userDTO,request);
            // 同步账户
            data.put("userId",userId);
            serverService.infoSupplement(data, request);
            return result;
        }
    }

    @Override
    public JSONObject userCreditMessage(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        JSONObject param=new JSONObject(2);
        param.put("userId",getUserId(request));
        JSONObject creditUserInfo = serverService.findCreditUserInfo(param);
        return creditUserInfo;
    }

    @Override
    public JSONObject activationInstallment(JSONObject requestInfo, HttpServletRequest request) throws Exception {
        Long userId = getUserId(request);
        log.info("激活分期付",userId);
        JSONObject dataParam=new JSONObject();
        dataParam.put("userId", userId);
        log.info("activationInstallment, data:{}", dataParam);
        String apiCreditOrderUrl = creditUrl + "/user/activationInstallment";
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(apiCreditOrderUrl, dataParam.toJSONString()));
        log.info("activationInstallment, result:{}", result);
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(result.getString("code"))) {
            throw new BizException(result.getString("message"));
        }
        return null;
    }

    @Override
    public JSONObject bankStatementsIsResult(JSONObject requestInfo, HttpServletRequest request) {
        Long userId = getUserId(request);
        int state = -1;
        log.info("bankStatementsIsResult, userId: {}", userId);
        String s;
        JSONObject jsonObject = new JSONObject();
        try {
            s = HttpClientUtils.sendPost(dataUrl + "/service/isResult/" + userId, null);
            jsonObject = JSONObject.parseObject(s);
            if (ResultCode.OK.getCode().equals(jsonObject.getString("code"))) {
                state = jsonObject.getJSONObject("data").getInteger("state");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        jsonObject.clear();
        // 2021-07-13 前后端商量修改为0跳转等待页面
        if (state==-1){
            state=0;
        }
        jsonObject.put("state", state);
        return jsonObject;
    }

    @Override
    public List<UserListDTO> findUserList(JSONObject param, HttpServletRequest request) {
        List<UserListDTO> data=userDAO.findUserList(param);
        return data;
    }

    @Override
    public int countUserList(JSONObject param) {
        List<Long> i=userDAO.countUserList(param);
        return i.size();
    }



    private String toCaseName(String name){
        if (!StringUtils.isAllBlank(name)) {
            String trimLast = name.trim();
            if (trimLast.length() > 0) {
                String trim = trimLast.trim();
                name = trim.substring(0, 1).toUpperCase() + trim.substring(1);
            }
        }
        return name;
    }




    @Override
    public void sendSMSVerificationCode(Integer nodeType, String phoneNumber,HttpServletRequest request) throws Exception {
        if(null == nodeType){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        //手机号校验
        verifyPhoneNumberFormat(phoneNumber, request);

        //20秒内不允许重复发送
        String sendPhoneRedisKey = Constant.getSendMobileRedisKey(phoneNumber, nodeType, StaticDataEnum.USER_TYPE_10.getCode());
        if (redisUtils.hasKey(sendPhoneRedisKey)) {
            throw new BizException(I18nUtils.get("sms.code.repeat.send", getLang(request)));
        }

        //todo  限制每天单个用户发送次数

        if(nodeType.equals(StaticDataEnum.SEND_NODE_1.getCode())){
            //用户注册
            MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(nodeType.toString());
            if(null == mailTemplateDTO || null == mailTemplateDTO.getId() || StringUtils.isBlank(mailTemplateDTO.getEnSendContent())){
                throw new BizException(I18nUtils.get(" SMS.send.failed", getLang(request)));
            }
            //生成验证码
            int securityCode = TestEnvUtil.isTestEnv() ? 666666 : (int) ((Math.random() * 9 + 1) * 100000);
            //设置模板参数
            String[] paramSend = {String.valueOf(securityCode)};
            //短信内容
            String sendMsg = this.templateContentReplace(paramSend, mailTemplateDTO.getEnSendContent());
            //todo ios提审账号
            if(phoneNumber.equals("61132569311")){
                //如果之前未使用验证码 发送之前删掉之前的信息
                deleteRedisInfoByLoginSuccess(phoneNumber, StaticDataEnum.SEND_NODE_1.getCode(), StaticDataEnum.USER_TYPE_10.getCode());
                //20S重复发送验证码
                redisUtils.set(sendPhoneRedisKey, 1, 20);
                //验证码失效时间
                String sendCodeExpireRedisKey = Constant.getSendCodeRedisKey(phoneNumber, "EXPIRE", nodeType, StaticDataEnum.USER_TYPE_10.getCode());
                redisUtils.set(sendCodeExpireRedisKey, 666666, 900);
                //验证码
                String sendCodeRedisKey = Constant.getSendCodeRedisKey(phoneNumber, "", nodeType, StaticDataEnum.USER_TYPE_10.getCode());
                redisUtils.set(sendCodeRedisKey, 666666, 7200);
            }else {
                if(aliyunSmsService.sendInternationalSmsV2(phoneNumber, sendMsg)){
                    //如果之前存在验证码 删除
                    deleteRedisInfoByLoginSuccess(phoneNumber, StaticDataEnum.SEND_NODE_1.getCode(), StaticDataEnum.USER_TYPE_10.getCode());
                    //20S重复发送验证码
                    redisUtils.set(sendPhoneRedisKey, 1, 20);
                    //验证码失效时间
                    String sendCodeExpireRedisKey = Constant.getSendCodeRedisKey(phoneNumber, "EXPIRE", nodeType, StaticDataEnum.USER_TYPE_10.getCode());
                    redisUtils.set(sendCodeExpireRedisKey, securityCode, 900);
                    //验证码
                    String sendCodeRedisKey = Constant.getSendCodeRedisKey(phoneNumber, "", nodeType, StaticDataEnum.USER_TYPE_10.getCode());
                    redisUtils.set(sendCodeRedisKey, securityCode, 7200);
                }else {
                    throw new BizException(I18nUtils.get("SMS.send.failed", getLang(request)));
                }
            }
        }else {
            throw new BizException(I18nUtils.get("SMS.send.failed", getLang(request)));
        }
    }

    /**
     * 校验验证码
     * @author zhangzeyuan
     * @date 2021/8/30 20:55
     * @param dataJsonObj
     * @param request
     */
    @Override
    public Object checkVerificationCode(JSONObject dataJsonObj, HttpServletRequest request) throws Exception {

        String phoneNumber = dataJsonObj.getString("phoneNumber");
        String code = dataJsonObj.getString("verificationCode");
        Integer nodeType = dataJsonObj.getInteger("nodeType");

        //参数校验
        if(null == nodeType){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }

        //手机号校验
        verifyPhoneNumberFormat(phoneNumber, request);

        //验证码校验
        verifyCodeFormat(code, phoneNumber, nodeType, true, request);

        if(nodeType.equals(StaticDataEnum.SEND_NODE_1.getCode())){
            //验证码登录
            return loginByVerificationCode(phoneNumber, dataJsonObj.getString("lat"), dataJsonObj.getString("lng"), dataJsonObj.getString("pushToken"),
                    dataJsonObj.getString("imeiNo"), dataJsonObj.getString("appVersionId"),dataJsonObj.getString("phoneModel"),dataJsonObj.getInteger("phoneSystem"),dataJsonObj.getString("phoneSystemVersion"),dataJsonObj.getString("mobileModel"),request);
        }else {
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
    }

    /**
     * 用户注册
     *
     * @param jsonObject
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/8/31 19:09
     */
    @Override
    public JSONObject appUserRegisterNew(JSONObject jsonObject, HttpServletRequest request) throws Exception {
        //数据校验

        //手机号校验
        String phoneNumber = jsonObject.getString("phoneNumber");
        verifyPhoneNumberFormat(phoneNumber, request);

        //用户验证码
        String verificationCode = jsonObject.getString("verificationCode");
        //验证码格式校验
        if(StringUtils.isBlank(verificationCode)){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if(verificationCode.length() != 6 || !StringUtils.isNumeric(verificationCode)){
            throw new BizException(I18nUtils.get("incorrect.verification.code", getLang(request)));
        }

        //验证码是否正确
        String sendCodeRedisKey = Constant.getSendCodeRedisKey(phoneNumber, "", StaticDataEnum.SEND_NODE_1.getCode(), StaticDataEnum.USER_TYPE_10.getCode());
        if(!redisUtils.hasKey(sendCodeRedisKey)){
            throw new BizException(I18nUtils.get("verification.code.expired", getLang(request)));
        }
        String redisCodeValue = redisUtils.get(sendCodeRedisKey).toString();
        if(!redisCodeValue.equals(redisCodeValue)){
            throw new BizException(I18nUtils.get("incorrect.verification.code", getLang(request)));
        }

        //姓名
        String userFirstName = jsonObject.getString("userFirstName");
        String userLastName = jsonObject.getString("userLastName");
        if(StringUtils.isBlank(userFirstName) || StringUtils.isBlank(userLastName)){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        //首字母大写
        userFirstName = userFirstName.trim().substring(0,1).toUpperCase() + userFirstName.trim().substring(1);
        userLastName = userLastName.trim().substring(0,1).toUpperCase() + userLastName.trim().substring(1);

        //邮箱
        String email = jsonObject.getString("email");
        if(StringUtils.isBlank(email)){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        if (!(email.length() <= Validator.TEXT_LENGTH_100) || !Validator.isEmail(email)){
            throw new BizException(I18nUtils.get("email.format.error", getLang(request)));
        }

        //生日
        String birthStr = jsonObject.getString("birth");
        if(StringUtils.isBlank(birthStr)){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }

        //性别
        Integer sex = jsonObject.getInteger("sex");
        if(null == sex){
            throw new BizException(I18nUtils.get("sex.not.empty", getLang(request)));
        }

        /*Long birthDate = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            birthDate = simpleDateFormat.parse(birthStr).getTime();
        }catch (Exception e){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }*/

        //营销码/注册码
        String promotionCode = jsonObject.getString("promotionCode");

        //用户是否存在
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(2);
        map.put("phone", phoneNumber);
        map.put("userType", StaticDataEnum.USER_TYPE_10.getCode());
        if (userDAO.count(map) > 0) {
            throw new BizException(I18nUtils.get("user.exist", getLang(request)));
        }

        // 用户注册

        //封装userDTO
        UserDTO saveUser = new UserDTO();
        saveUser.setUserFirstName(userFirstName);
        saveUser.setUserLastName(userLastName);
        saveUser.setEmail(email);
        saveUser.setPhone(phoneNumber);
        saveUser.setBirth(birthStr);
        saveUser.setPaymentState(StaticDataEnum.USER_BUSINESS_1.getCode());
        saveUser.setInvestState(StaticDataEnum.USER_BUSINESS_1.getCode());
        saveUser.setImeiNo(jsonObject.getString("imeiNo"));
        saveUser.setUserMiddleName(jsonObject.getString("userMiddleName"));
        saveUser.setUserType(StaticDataEnum.USER_TYPE_10.getCode());
        saveUser.setChannel(String.valueOf(StaticDataEnum.ACCOUNT_CHANNEL_0001.getCode()));
        saveUser.setPushToken(jsonObject.getString("pushToken"));
        saveUser.setLat(jsonObject.getString("lat"));
        saveUser.setLng(jsonObject.getString("lng"));
        saveUser.setSex(sex);

        //新版本注册阅读协议
        Integer readAgreementState = jsonObject.getInteger("readAgreementState");
        if(null != readAgreementState){
            saveUser.setReadAgreementState(readAgreementState);
        }
        //记录机型 版本号
        if(StringUtils.isNotBlank(jsonObject.getString("appVersionId"))){
            saveUser.setAppVersionId(jsonObject.getString("appVersionId"));
        }
        if(StringUtils.isNotBlank(jsonObject.getString("phoneModel"))){
            saveUser.setPhoneModel(jsonObject.getString("phoneModel"));
        }
        // 手机系统
        if(jsonObject.getInteger("phoneSystem")!=null){
            saveUser.setPhoneSystem(jsonObject.getInteger("phoneSystem"));
        }
        // 手机系统版本
        if(StringUtils.isNotBlank(jsonObject.getString("phoneSystemVersion"))){
            saveUser.setPhoneSystemVersion(jsonObject.getString("phoneSystemVersion"));
        }
        // 手机型号
        if(StringUtils.isNotBlank(jsonObject.getString("mobileModel"))){
            saveUser.setMobileModel(jsonObject.getString("mobileModel"));
        }

        //用户邀请码
        String inviteCode = "";
        while (true) {
            //邀请码是否重复
            map.clear();
            inviteCode = InviteUtil.getBindNum(6);
            map.put("inviteCode", inviteCode);
            if(userDAO.count(map) == 0){
                break;
            }
        }
        saveUser.setInviteCode(inviteCode);

        //用户入库
        Long userId = null;
        try {
            userId = appRegisterUserNew(saveUser, request);
        }catch (Exception e){
            throw new BizException(I18nUtils.get("register.failed", getLang(request)));
        }
        saveUser.setId(userId);

        //设置kyc错误次数
        map.clear();
        map.put("chance", KYC_CHANCE);
        map.put("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        redisUtils.hmset(userId + "_kyc", map);
        // 存储用户邀请码至缓存
        redisUtils.set(userId + "_inviteCode", saveUser.getInviteCode());
        //关联用户邀请人ID
        /*associatedRegisteredUser(userId, inviteCode, request);*/

        //如果是用户发短信，商户发Email
        sendLoginMessage(saveUser, request);

        //注册输入 营销码/邀请码处理
        try{
            handleEnterCodeByUserRegister(saveUser, promotionCode, request);
        }catch (Exception e){
            log.error("注册输入 营销码/邀请码处理， 出错， e:{}" ,e);
        }


        JSONObject resultJsonObj = new JSONObject(5);
        String token = JwtUtils.signApp(userId, phoneNumber, null, StaticDataEnum.USER_TYPE_10.getCode(), redisUtils);
        resultJsonObj.put("Authorization", token);
        resultJsonObj.put("userId", userId.toString());
        resultJsonObj.put("phoneNumber", phoneNumber);

        //登录成功  删除短信验证码 删除验证次数
        deleteRedisInfoByLoginSuccess(phoneNumber, StaticDataEnum.SEND_NODE_1.getCode(), StaticDataEnum.USER_TYPE_10.getCode());
        return resultJsonObj;

    }

    /**
     * 用户注册
     *
     * @param jsonObject
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/8/31 19:09
     */
    @Override
    public JSONObject appUserRegisterNewV2(JSONObject jsonObject, HttpServletRequest request) throws Exception {
        //数据校验

        //手机号校验
        String phoneNumber = jsonObject.getString("phoneNumber");
        verifyPhoneNumberFormat(phoneNumber, request);

        //用户验证码
        String verificationCode = jsonObject.getString("verificationCode");
        //验证码格式校验
        if(StringUtils.isBlank(verificationCode)){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if(verificationCode.length() != 6 || !StringUtils.isNumeric(verificationCode)){
            throw new BizException(I18nUtils.get("incorrect.verification.code", getLang(request)));
        }

        //验证码是否正确
        String sendCodeRedisKey = Constant.getSendCodeRedisKey(phoneNumber, "", StaticDataEnum.SEND_NODE_1.getCode(), StaticDataEnum.USER_TYPE_10.getCode());
        if(!redisUtils.hasKey(sendCodeRedisKey)){
            throw new BizException(I18nUtils.get("verification.code.expired", getLang(request)));
        }
        String redisCodeValue = redisUtils.get(sendCodeRedisKey).toString();
        if(!redisCodeValue.equals(redisCodeValue)){
            throw new BizException(I18nUtils.get("incorrect.verification.code", getLang(request)));
        }

        //姓名
        String userFirstName = jsonObject.getString("userFirstName");
        String userLastName = jsonObject.getString("userLastName");
        if(StringUtils.isBlank(userFirstName) || StringUtils.isBlank(userLastName)){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        //首字母大写
        userFirstName = userFirstName.trim().substring(0,1).toUpperCase() + userFirstName.trim().substring(1);
        userLastName = userLastName.trim().substring(0,1).toUpperCase() + userLastName.trim().substring(1);

        //邮箱
        String email = jsonObject.getString("email");
        if(StringUtils.isBlank(email)){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        if (!(email.length() <= Validator.TEXT_LENGTH_100) || !Validator.isEmail(email)){
            throw new BizException(I18nUtils.get("email.format.error", getLang(request)));
        }

        //生日
        String birthStr = jsonObject.getString("birth");
        if(StringUtils.isBlank(birthStr)){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }

        //性别
        Integer sex = jsonObject.getInteger("sex");
        if(null == sex){
            throw new BizException(I18nUtils.get("sex.not.empty", getLang(request)));
        }

        /*Long birthDate = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            birthDate = simpleDateFormat.parse(birthStr).getTime();
        }catch (Exception e){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }*/

        //营销码/注册码
        String promotionCode = jsonObject.getString("promotionCode");

        //用户是否存在
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(2);
        map.put("phone", phoneNumber);
        map.put("userType", StaticDataEnum.USER_TYPE_10.getCode());
        if (userDAO.count(map) > 0) {
            throw new BizException(I18nUtils.get("user.exist", getLang(request)));
        }

        // 用户注册

        //封装userDTO
        UserDTO saveUser = new UserDTO();
        saveUser.setUserFirstName(userFirstName);
        saveUser.setUserLastName(userLastName);
        saveUser.setEmail(email);
        saveUser.setPhone(phoneNumber);
        saveUser.setBirth(birthStr);
        saveUser.setPaymentState(StaticDataEnum.USER_BUSINESS_1.getCode());
        saveUser.setInvestState(StaticDataEnum.USER_BUSINESS_1.getCode());
        saveUser.setImeiNo(jsonObject.getString("imeiNo"));
        saveUser.setUserMiddleName(jsonObject.getString("userMiddleName"));
        saveUser.setUserType(StaticDataEnum.USER_TYPE_10.getCode());
        saveUser.setChannel(String.valueOf(StaticDataEnum.ACCOUNT_CHANNEL_0001.getCode()));
        saveUser.setPushToken(jsonObject.getString("pushToken"));
        saveUser.setLat(jsonObject.getString("lat"));
        saveUser.setLng(jsonObject.getString("lng"));
        saveUser.setSex(sex);
        //新版本注册阅读协议
        Integer readAgreementState = jsonObject.getInteger("readAgreementState");
        if(null != readAgreementState){
            saveUser.setReadAgreementState(readAgreementState);
        }
        //记录机型 版本号
        if(StringUtils.isNotBlank(jsonObject.getString("appVersionId"))){
            saveUser.setAppVersionId(jsonObject.getString("appVersionId"));
        }
        if(StringUtils.isNotBlank(jsonObject.getString("phoneModel"))){
            saveUser.setPhoneModel(jsonObject.getString("phoneModel"));
        }

        //用户邀请码
        String inviteCode = "";
        while (true) {
            //邀请码是否重复
            map.clear();
            inviteCode = InviteUtil.getBindNum(6);
            map.put("inviteCode", inviteCode);
            if(userDAO.count(map) == 0){
                break;
            }
        }
        saveUser.setInviteCode(inviteCode);
        //设置stripe state状态 新版本新加stripeState该参数
//        String stripeStateParam = jsonObject.getString("stripeState");
//        if(StringUtils.isNotBlank(stripeStateParam)){
//            saveUser.setStripeState(StaticDataEnum.USER_STRIPE_STATE_0.getCode());
//        }else{
//            saveUser.setStripeState(StaticDataEnum.USER_STRIPE_STATE_1.getCode());
//        }
        // 判断当前通道
        JSONObject payGateWay = gatewayService.getPayGateWay(request);
        Integer type = payGateWay.getInteger("type");
        if (type==null){
            throw new BizException(I18nUtils.get("register.failed", getLang(request)));
        }
        if (type.intValue()==StaticDataEnum.GATEWAY_TYPE_0.getCode()){
            saveUser.setStripeState(StaticDataEnum.USER_STRIPE_STATE_1.getCode());
        }else if (type.intValue()==StaticDataEnum.GATEWAY_TYPE_8.getCode()){
            saveUser.setStripeState(StaticDataEnum.USER_STRIPE_STATE_0.getCode());
        }

        //用户入库
        Long userId = null;
        try {
            userId = appRegisterUserNew(saveUser, request);
        }catch (Exception e){
            throw new BizException(I18nUtils.get("register.failed", getLang(request)));
        }
        saveUser.setId(userId);

        //设置kyc错误次数
        map.clear();
        map.put("chance", KYC_CHANCE);
        map.put("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        redisUtils.hmset(userId + "_kyc", map);
        // 存储用户邀请码至缓存
        redisUtils.set(userId + "_inviteCode", saveUser.getInviteCode());
        //关联用户邀请人ID
        /*associatedRegisteredUser(userId, inviteCode, request);*/

        //如果是用户发短信，商户发Email
        sendLoginMessage(saveUser, request);

        //注册输入 营销码/邀请码处理
        try{
            handleEnterCodeByUserRegisterV2(saveUser, promotionCode, request);
        }catch (Exception e){
            log.error("注册输入 营销码/邀请码处理， 出错， e:{}" ,e);
        }


        JSONObject resultJsonObj = new JSONObject(5);
        String token = JwtUtils.signApp(userId, phoneNumber, null, StaticDataEnum.USER_TYPE_10.getCode(), redisUtils);
        resultJsonObj.put("Authorization", token);
        resultJsonObj.put("userId", userId.toString());
        resultJsonObj.put("phoneNumber", phoneNumber);

        //登录成功  删除短信验证码 删除验证次数
        deleteRedisInfoByLoginSuccess(phoneNumber, StaticDataEnum.SEND_NODE_1.getCode(), StaticDataEnum.USER_TYPE_10.getCode());
        return resultJsonObj;

    }



    /**
     * 验证码登录
     * @author zhangzeyuan
     * @date 2021/8/30 20:52
     * @param phoneNumber
     * @param lat
     * @param lng
     * @param pushToken
     * @param imeiNo
     * @param request
     * @return java.lang.Object
     */
    private Map<String, Object> loginByVerificationCode(String phoneNumber, String lat, String lng, String pushToken, String imeiNo,
                                                        String appVersionId,String phoneModel,Integer phoneSystem,String phoneSystemVersion,String mobileModel, HttpServletRequest request) throws Exception{

        //累加验证码校验次数
        String smsCodeCheckTimesRedisKey = Constant.getSMSCodeCheckTimesRedisKey(phoneNumber, StaticDataEnum.SEND_NODE_1.getCode(), StaticDataEnum.USER_TYPE_10.getCode());
        redisUtils.incr(smsCodeCheckTimesRedisKey, 1, 900);

        //查询用户
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(7);
        map.put("phone", phoneNumber);
        map.put("userType", StaticDataEnum.USER_TYPE_10.getCode());
        UserDTO userDTO = findOneUser(map);
        map.clear();
        if (userDTO == null || userDTO.getId() == null) {
            //新用户 返回注册标识
            map.put("registrationStatus", StaticDataEnum.USER_REGISTER_STATUS_UNREGISTERED.getCode());
            return map;
        }

        //注册过的用户直接登录

        //查询该用户是否被冻结
        map.clear();
        map.put("userId", userDTO.getId());
        LoginMissDTO loginMissDTO = loginMissService.findOneLoginMiss(map);
        if(null == loginMissDTO || null == loginMissDTO.getId()){
            throw new BizException(I18nUtils.get("pay.user.freeze", getLang(request)));
        }

        //生成token值
        String token = JwtUtils.signApp(userDTO.getId(), phoneNumber, null, StaticDataEnum.USER_TYPE_10.getCode(), redisUtils);
        //查询account 用户姓名相关信息
        JSONObject data = serverService.userInfoByQRCode(userDTO.getId());
        //封装返回信息
        map.put("userInfo", userDTO);
        map.put("Authorization", token);
        map.put("firstName", data.getString("userFirstName"));
        map.put("middleName", data.getString("userMiddleName"));
        map.put("lastName", data.getString("userLastName"));
        map.put("lat", lat);
        map.put("lng", lng);
        //更新用户信息
        userDTO.setLat(lat);
        userDTO.setLng(lng);
        userDTO.setPushToken(pushToken);
        userDTO.setImeiNo(imeiNo);
        userDTO.setLoginTime(System.currentTimeMillis());
        //记录机型 版本号
        userDTO.setAppVersionId(appVersionId);
        userDTO.setPhoneModel(phoneModel);
        // 手机系统
        userDTO.setPhoneSystem(phoneSystem);
        userDTO.setPhoneSystemVersion(phoneSystemVersion);
        userDTO.setMobileModel(mobileModel);

        updateUser(userDTO.getId(), userDTO, request);
        //用户登录 删除验证码 删除校验次数
        deleteRedisInfoByLoginSuccess(phoneNumber, StaticDataEnum.SEND_NODE_1.getCode(), StaticDataEnum.USER_TYPE_10.getCode());

        return map;
    }


    /**
     * 登录成功删除验证码和验证码校验次数
     * @author zhangzeyuan
     * @date 2021/9/13 10:59
     * @param phoneNumber
     * @param nodeType
     * @param userType
     */
    private  void deleteRedisInfoByLoginSuccess(String phoneNumber, Integer nodeType, Integer userType){
        //删除验证码校验次数
        String smsCodeCheckTimesRedisKey = Constant.getSMSCodeCheckTimesRedisKey(phoneNumber, nodeType, userType);
        redisUtils.del(smsCodeCheckTimesRedisKey);

        //删除验证码
        String sendCodeRedisKey = Constant.getSendCodeRedisKey(phoneNumber, "", nodeType, userType);
        redisUtils.del(sendCodeRedisKey);

        String sendCodeExpireRedisKey = Constant.getSendCodeRedisKey(phoneNumber, "EXPIRE", nodeType, userType);
        redisUtils.del(sendCodeExpireRedisKey);
    }

    /**
     * 注册用户数据入库
     * @author zhangzeyuan
     * @date 2021/8/31 20:45
     * @param saveUser
     * @param request
     * @return java.lang.Long
     */
    public synchronized Long appRegisterUserNew(UserDTO saveUser, HttpServletRequest request) throws Exception{
        //支付 保存user
        Long userId = userCreate(saveUser, request);

        //账户 保存user
        JSONObject accountInfo = new JSONObject(13);
        accountInfo.put("userId", userId);
        accountInfo.put("phone", saveUser.getPhone());
        accountInfo.put("email", saveUser.getEmail());
        accountInfo.put("firstName", saveUser.getUserFirstName());
        accountInfo.put("lastName", saveUser.getUserLastName());
        accountInfo.put("middleName", saveUser.getUserMiddleName());
        accountInfo.put("birth", saveUser.getBirth());
        accountInfo.put("accountType", saveUser.getUserType());
        accountInfo.put("channel", saveUser.getChannel());
        accountInfo.put("postcode", saveUser.getPostcode());
        accountInfo.put("sex", saveUser.getSex());
        serverService.saveAccount(accountInfo);

        //支付 保存登陆错误次数
        LoginMissDTO loginMissDTO = new LoginMissDTO();
        loginMissDTO.setUserId(userId);
        loginMissDTO.setChance(StaticDataEnum.LOGIN_MISS_TIME.getCode());
        loginMissService.saveLoginMiss(loginMissDTO, request);

        //支付 保存登陆错误次数
        userStepService.createUserStep(userId, request);

        // 调用理财系统同步数据
        /*accountInfo.clear();
        accountInfo.put("userFirstName", saveUser.getUserFirstName());
        accountInfo.put("userLastName", saveUser.getUserLastName());
        String investInfoSupplementUrl = investUrl + "/server/userSynchronization";
        JSONObject returnMsg = JSONObject.parseObject(HttpClientUtils.sendPost(investInfoSupplementUrl, accountInfo.toJSONString()));
        if (ErrorCodeEnum.FAIL_CODE.getCode().equals(returnMsg.getString("code"))) {
            throw new BizException(I18nUtils.get("incorrect.information", getLang(request)));
        }*/

        return userId;
    }


    @Transactional(rollbackFor = Exception.class)
    public void handleEnterCodeByUserRegister(UserDTO user, String code, HttpServletRequest request) throws Exception {
        Long userId = user.getId();

        //判断验证码是否存在
        if(StringUtils.isBlank(code)){
            return;
        }

        //查询是否是营销券码
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(2);


        //查询是否是邀请码
        map.clear();
        map.put("inviteCode", code);
        UserDTO inviteUser = userDAO.selectOneDTO(map);

        //邀请码
        if(null != inviteUser && null != inviteUser.getId()){

            //用户绑定邀请人
            userDAO.updateInviteId(userId, inviteUser.getId());

            //更新邀请人 邀请人数、累计邀请金额信息
            this.updateRegister(inviteUser.getId());
            this.updateWalletGrandTotal(inviteUser.getId(), new BigDecimal(walletConsumption), null);

            //营销红包入账
            /*MarketingFlowDTO marketingFlowDTO = new MarketingFlowDTO();
            marketingFlowDTO.setAmount(amount);
            marketingFlowDTO.setCode(code);
            marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
            marketingFlowDTO.setDirection(StaticDataEnum.DIRECTION_0.getCode());
            marketingFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode());
            marketingFlowDTO.setUserId(userId);
            marketingFlowDTO.setId(marketingFlowService.saveMarketingFlow(marketingFlowDTO,request));

            marketingFlowDTO.setFlowId(marketingFlowDTO.getId());
            marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(),marketingFlowDTO,request);

            //入账
            doMarketingAmountIn(marketingFlowDTO, request);*/
            return;
        }

        map.clear();
        map.put("code", code);
        map.put("status",StaticDataEnum.STATUS_1.getCode());

        MarketingManagementDTO marketingManagementDTO = marketingManagementService.findOneMarketingManagement(map);

        //营销券码
        if(null != marketingManagementDTO && null != marketingManagementDTO.getId()){
            walletBooked(userId, StaticDataEnum.ACC_FLOW_TRANS_TYPE_25.getCode(), code, request, null);
        }

    }

    /**
     * 注册新增营销券/邀请券
     * @author zhangzeyuan
     * @date 2021/11/9 19:50
     * @param user
     * @param code
     * @param request
     */
    public void handleEnterCodeByUserRegisterV2(UserDTO user, String code, HttpServletRequest request) throws Exception {
        Long userId = user.getId();
        //判断验证码是否存在
        if(StringUtils.isBlank(code)){
            return;
        }
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(2);

        //查询是否是邀请码
        map.clear();
        map.put("inviteCode", code);
        UserDTO inviteUser = userDAO.selectOneDTO(map);
        if(null != inviteUser && null != inviteUser.getId()){
            //是邀请码
            //查询邀请码规则
            HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(2);
            params.put("type", 2);
            params.put("activityState", 1);
            params.put("status",StaticDataEnum.STATUS_1.getCode());
            MarketingManagementDTO oneMarketingManagement = marketingManagementService.findOneMarketingManagement(params);
            if(null != oneMarketingManagement && null != oneMarketingManagement.getId()){
                //用户绑定邀请人
                userDAO.updateInviteId(userId, inviteUser.getId());

                //添加一张未激活的券
                marketingManagementService.addInvitationPromotionCode(userId,  inviteUser.getId(), StaticDataEnum.ADD_INVITATION_CODE_TYPE_1.getCode(), null, oneMarketingManagement,true, request);
            }
            return;
        }

        //营销码
        MarketingManagementDTO promotionCodeByCode = marketingManagementService.findPromotionCodeByCode(code.toLowerCase());
        if(null != promotionCodeByCode && null != promotionCodeByCode.getId()){
            //给用户添加一张营销券
            marketingManagementService.addMarketingPromotionCode(userId, promotionCodeByCode, StaticDataEnum.ACC_FLOW_TRANS_TYPE_25.getCode(), false, request);
        }
    }


    /**
     * 校验手机号
     * @author zhangzeyuan
     * @date 2021/9/1 17:55
     * @param phoneNumber
     * @param request
     */
    private void verifyPhoneNumberFormat(String phoneNumber, HttpServletRequest request) throws Exception{
        if(StringUtils.isBlank(phoneNumber)){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }

        if(!phoneNumber.startsWith("61")){
            throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
        }

        if(phoneNumber.length() != Validator.PHONE_LENGTH){
            throw new BizException(I18nUtils.get("sms.code.repeat.send", getLang(request)));
        }
    }



    /**
     * 校验验证码
     * @author zhangzeyuan
     * @date 2021/9/1 17:56
     * @param code
     * @param phoneNumber
     * @param nodeType
     * @param request
     */
    private void verifyCodeFormat(String code, String phoneNumber, Integer nodeType, boolean checkTimeStatus, HttpServletRequest request) throws Exception{
        //验证码格式校验
        if(StringUtils.isBlank(code)){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }

        if(code.length() != 6 || !StringUtils.isNumeric(code)){
            throw new BizException(I18nUtils.get("incorrect.verification.code", getLang(request)));
        }

        //验证码是否有效
        String sendCodeExpireRedisKey = Constant.getSendCodeRedisKey(phoneNumber, "EXPIRE", nodeType, StaticDataEnum.USER_TYPE_10.getCode());
        if(!redisUtils.hasKey(sendCodeExpireRedisKey)){
            throw new BizException(I18nUtils.get("verification.code.expired", getLang(request)));
        }

        if(checkTimeStatus){
            //校验验证次数
            String smsCodeCheckTimesRedisKey = Constant.getSMSCodeCheckTimesRedisKey(phoneNumber, nodeType, StaticDataEnum.USER_TYPE_10.getCode());
            if(redisUtils.hasKey(smsCodeCheckTimesRedisKey)){
                throw new BizException(I18nUtils.get("sms.code.check.times.error", getLang(request)));
            }
        }

        //验证码是否正确
        String sendCodeRedisKey = Constant.getSendCodeRedisKey(phoneNumber, "", nodeType, StaticDataEnum.USER_TYPE_10.getCode());
        String redisCodeValue = redisUtils.get(sendCodeRedisKey).toString();
        if(!code.equals(redisCodeValue)){
            throw new BizException(I18nUtils.get("incorrect.verification.code", getLang(request)));
        }
    }



    /**
     * 获取用户业务状态
     *
     * @param jsonObject
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/2 14:33
     */
    @Override
    public Object getUserBusinessStatus(JSONObject jsonObject, HttpServletRequest request) throws Exception {

        Long userId = jsonObject.getLong("userId");
        if(null == userId){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }

        //查询 支付用户状态
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(3);
        map.put("id", userId);
        UserDTO userDTO = userDAO.selectOneDTO(map);

        if(null == userDTO || null == userDTO.getId()){
            throw new BizException(I18nUtils.get("user.doesnt.exist", getLang(request)));
        }

        log.info("获取支付用户信息： userId:{}, cardState:{},installment_state:{}", userDTO.getId(), userDTO.getCardState(), userDTO.getInstallmentState());

        JSONObject result = new JSONObject();

        //是否同意过分期付绑卡协议
        result.put("creditCardAgreementState",  userDTO.getCreditCardAgreementState());

        int tempCardState = userDTO.getCardState();

        //新版本新加stripeState该参数 新版本未迁移到stripe平台 绑卡状态为0
        map.clear();
        map.put("state", 1);
        map.put("gatewayType", 0);
        GatewayDTO oneGateway = gatewayService.findOneGateway(map);
        if(null == oneGateway || null == oneGateway.getId()){
            throw new BizException(I18nUtils.get("gateway.disabled", getLang(request)));
        }
            if(oneGateway.getType().intValue() == StaticDataEnum.GATEWAY_TYPE_8.getCode()){
                String stripeStateParam = jsonObject.getString("stripeState");
                if(StringUtils.isNotBlank(stripeStateParam)){
                    if(userDTO.getStripeState().equals(StaticDataEnum.USER_STRIPE_STATE_1.getCode())){
                        //用户没有迁移到stripe 绑卡状态为0
                        tempCardState = 0;
                    }
                }
        }

        HashMap<String, Object> striepCodeMap = Maps.newHashMapWithExpectedSize(1);
        striepCodeMap.put("code", "stripeState");
        StaticDataDTO staticData = staticDataService.findOneStaticData(striepCodeMap);
        if(null != staticData && null != staticData.getId()){
           if(staticData.getValue().equals("1")){
               //没有切换  绑卡状态正常
           }else{
               //正式切换到stripe
               int stripeState = userDTO.getStripeState();
               if(stripeState == 1){
                   //用户没有迁移 绑卡状态为0
                   tempCardState = 0;
               }
           }
        }
        result.put("cardState",  tempCardState);

        //kyc状态
        Integer installmentState = userDTO.getInstallmentState();

        //kyc失败次数
        int kycTimes = KYC_CHANCE;
        String userKycKey = userDTO.getId() + "_kyc_" + LocalDate.now();
        if (redisUtils.hasKey(userKycKey)) {
            kycTimes = (int) redisUtils.get(userKycKey);
        } else {
            kycTimes = KYC_CHANCE;
        }
        result.put("kycTimes",  kycTimes);

        //未开通
        if(installmentState == 0){
            //kyc未开通
            result.put("kycState",  0);
            result.put("installmentState",  0);
            result.put("creditCardState",  0);
        }else if(installmentState ==  1 || installmentState ==  3 || installmentState == 5 || installmentState == 2 ){
            //kyc成功
            result.put("kycState",  1);

            //查询分期付用户
            JSONObject searchInfo = new JSONObject();
            searchInfo.put("userId", userId);
            JSONObject creditUserInfo = serverService.findCreditUserInfo(searchInfo);
            if(null == creditUserInfo || null == creditUserInfo.getLong("userId")){
                //未查询到分期付用户 分期付状态为未开通
                result.put("installmentState",  0);
                result.put("creditCardState",  0);
            }else{
                log.info("获取分期付用户信息： userId:{}, userState:{},installment_state:{}", userDTO.getId(), creditUserInfo.getString("state"));

                result.put("creditCardState",  creditUserInfo.getInteger("bindCardState"));

                //分期付用户状态
                int creditUserState = creditUserInfo.getInteger("state");

                if (0 != creditUserInfo.getInteger("disableDate")) {
                    // 用户分期付处于禁用状态
                    result.put("installmentState",  5);
//                    result.put("installmentState",  2);
                }else if (30 == creditUserState || 40 == creditUserState || 50 == creditUserState){
                    // 等待人工审核
                    result.put("installmentState",  4);
                } else if (0 == creditUserInfo.getInteger("illionState")) {
                    // 用户illion 未授权
                    result.put("installmentState",  3);
                } else if (10 == creditUserState) {
                    // 分期付风控未触发
                    result.put("installmentState",  9);
                }else if (11 == creditUserState) {
                    // 用户分期付已冻结
//                    result.put("installmentState",  1);
                    result.put("installmentState",  2);
                } else if (31 == creditUserState || 20 == creditUserState) {
                    // 用户分期付已开通
                    result.put("installmentState",  2);
                    //额度
                    result.put("creditAmount",  creditUserInfo.getBigDecimal("creditAmount").toString());

                } else if (22 == creditUserState) {
                    // 机审中
                    result.put("installmentState",  7);
                } else if (32 == creditUserState || 21 == creditUserState) {
                    // 审批拒绝
                    result.put("installmentState",  8);
                } else {
                    throw new BizException(I18nUtils.get("data.exception", getLang(request)));
                }
            }
        }
        return result;
    }

    /**
     * 获取用户详情
     *
     * @param userId
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/9 14:40
     */
    @Override
    public Object getUserDetailData(Long userId, HttpServletRequest request) throws Exception {
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
        map.put("id",  userId);
        UserDTO payUser = userDAO.selectOneDTO(map);
        if(null == payUser || null == payUser.getId()){
            throw new BizException(I18nUtils.get("user.doesnt.exist", getLang(request)));
        }
        //返回结果封装
        JSONObject result = new JSONObject();
        //基本信息
        result.put("phoneSystem",payUser.getPhoneSystem());
        result.put("mobileModel",payUser.getMobileModel());
        result.put("email", payUser.getEmail());
        result.put("phone", payUser.getPhone());
        //推荐用户数
        result.put("recommendedUsersCount", payUser.getInvitationToRegister());
        //分期付状态
        result.put("installmentState", payUser.getInstallmentState());
        //KYC认证结果
        String kycResult = "";
        int installmentState = payUser.getInstallmentState().intValue();
        if(installmentState == 1 || installmentState == 3 || installmentState == 5||installmentState == 2){
            kycResult = "Accept";
        }else if (installmentState == 0){
            // 判断是否提交过kyc 如果提交过则返回失败
            JSONObject param=new JSONObject();
            param.put("userId",userId);
            // -1 kyc错误和系统错误
            param.put("status","-1");
            KycSubmitLogDTO oneKycSubmitLog = kycSubmitLogService.findOneKycSubmitLog(param);
            if (oneKycSubmitLog.getId()!=null){
                kycResult = "Failed";
            }
            Map<String, Object> params = new HashMap<>(1);
            params.put("userId", userId);
            params.put("step", StaticDataEnum.USER_STEP_1.getCode());
            UserStepDTO userStepDTO =userStepService.findOneUserStep(params);
            if (userStepDTO.getId()!=null){
                if (userStepDTO.getStepState().intValue()==StaticDataEnum.USER_STEP_STATE_2.getCode()){
                    kycResult = "Reject";
                }else if(userStepDTO.getStepState().intValue()==StaticDataEnum.USER_STEP_STATE_1.getCode()){
                    kycResult = "Accept";
                }else if(userStepDTO.getStepState().intValue()==StaticDataEnum.USER_STEP_STATE_5.getCode()){
                    kycResult = "Failed";
                }
            }

        }
        result.put("kycResult", kycResult);

        //查询account 用户数据
        try{
            JSONObject accountInfo = serverService.userInfoByQRCode(userId);
            result.put("userFirstName", accountInfo.getString("userFirstName"));
            result.put("userLastName", accountInfo.getString("userLastName"));
            result.put("userMiddleName", accountInfo.getString("userMiddleName"));
            result.put("sex", accountInfo.getInteger("sex"));
            result.put("birth", accountInfo.getString("birth"));
            result.put("postCode", accountInfo.getString("postcode"));
            //地址
            result.put("country", accountInfo.getString("userCitizenship"));
            result.put("state", accountInfo.getString("state"));
            result.put("city", accountInfo.getString("city"));
            result.put("streetName",accountInfo.getString("streetName"));
            result.put("streetNumber",accountInfo.getString("streetNumber"));
            result.put("suburb", accountInfo.getString("suburb"));
            result.put("aptSuiteEtc", accountInfo.getString("aptSuiteEtc"));
            result.put("address", accountInfo.getString("address"));
            //证件信息
            Integer idType = null;
            String idNumber = "";
            String date = "";
            String driverLicence = accountInfo.getString("driverLicence");
            Integer driverLicenceState = accountInfo.getInteger("driverLicenceState");

            String passport = accountInfo.getString("passport");


            String medicare = accountInfo.getString("medicare");
            Integer passportCountry = accountInfo.getInteger("passportCountry");
            Integer medicareType = accountInfo.getInteger("medicareType");
            String medicareIndate = accountInfo.getString("medicareIndate");
            String medicareRefNo = accountInfo.getString("medicareRefNo");
            if(StringUtils.isNotBlank(driverLicence)){
                idType = 1;
                idNumber = driverLicence;
            }
            if(StringUtils.isNotBlank(passport)){
                idType = 0;
                idNumber = passport;
                date = accountInfo.getString("passportIndate");
                result.put("passportCountry", passportCountry);
            }
            if(StringUtils.isNotBlank(medicare)){
                idType = 2;
                idNumber = medicare;
                date= accountInfo.getString("medicareIndate");
                result.put("medicareType", medicareType);
                result.put("medicareRefNo", medicareRefNo);
            }
            result.put("idType", idType);
            result.put("driverLicenceState", driverLicenceState);
            result.put("date", date);
            result.put("idNumber", idNumber);



        }catch (Exception e){
            log.error("用户详情获取账户user出错，userId:{}, e:{}", userId, e.getMessage());
        }

        //查询credit 用户数据
        try{
            JSONObject searchInfo = new JSONObject();
            searchInfo.put("userId", userId);
            JSONObject creditUserInfo = serverService.findCreditUserDetail(searchInfo);
            //评分等级
            result.put("grade", creditUserInfo.getString("grade"));
            //授信额度
            result.put("creditAmount", creditUserInfo.getString("creditAmount"));
            result.put("availableCredit", creditUserInfo.getString("availableCredit"));
            result.put("temporaryQuota", creditUserInfo.getString("temporaryQuota"));

            //总欠款
            BigDecimal add=BigDecimal.ZERO;
            String overdueAmount = creditUserInfo.getString("overdueAmount");
            String creditAmount = creditUserInfo.getString("creditAmount");
            String availableCredit = creditUserInfo.getString("availableCredit");
            BigDecimal overdueAmounts = new BigDecimal(StringUtils.isNotEmpty(overdueAmount)?overdueAmount:"0.00");
            BigDecimal creditAmounts = new BigDecimal(StringUtils.isNotEmpty(creditAmount)?creditAmount:"0.00");
            BigDecimal availableCredits = new BigDecimal(StringUtils.isNotEmpty(availableCredit)?availableCredit:"0.00");
            add=overdueAmounts.add(creditAmounts).subtract(availableCredits);
            result.put("allRepayAmount", add);
            //账户还款中
            result.put("accountRepayAmount", creditUserInfo.getString("accountRepayAmount"));
            //逾期费
            result.put("overdueCount", creditUserInfo.getString("overdueCount"));
            result.put("overdueAmount", creditUserInfo.getString("overdueAmount"));
            // 查询用户分期付状态是否正常
            result.put("creditState",creditUserInfo.getInteger("state"));

            //分期付申请结果
            int creditUserState = creditUserInfo.getInteger("state").intValue();
            String illionResult = "";
            if(creditUserState == 21 || creditUserState == 32){
                illionResult = "Reject";
            }else if(creditUserState == 20||creditUserState==11){
                illionResult = "Accept";
            }
            result.put("illionResult", illionResult);

        }catch (Exception e){
            log.error("用户详情获取分期付user出错，userId:{}, e:{}", userId, e.getMessage());
        }
        JSONObject jsonParams=new JSONObject();
        jsonParams.put("userId", userId);
        jsonParams.put("state", 1);
        JSONObject couponJsonResult = this.getMarketingCouponAccount(jsonParams, request);
        JSONArray couponJsonArray = couponJsonResult.getJSONArray("data");
        List<JSONObject> list = couponJsonArray.toJavaList(JSONObject.class);
        BigDecimal payoMoneyAmount=BigDecimal.ZERO;
        for (JSONObject jsonObject : list) {
            BigDecimal balance = jsonObject.getBigDecimal("balance");
            if (balance!=null){
                payoMoneyAmount=payoMoneyAmount.add(balance);
            }
        }
        // 查询用户卡券余额
        BigDecimal redAmount = marketingFlowService.getRedAmountByUserId(userId, request);
        //可用红包金额
//        BigDecimal payoMoneyAmount = getBalance(userId, StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
        //已使用红包金额
        BigDecimal useRedAmount = marketingFlowService.getUseRedAmountByUserId(userId, request);

        result.put("useRedAmount", useRedAmount.toString());
        result.put("payoMoneyAmount",redAmount);
        // 查询可用卡券数量
        JSONObject params=new JSONObject();
        params.put("type",1);
        params.put("userId",userId);
       List<JSONObject> promotionList= marketingFlowService.getUseAvailablePromotionByUserId(params, request);
        result.put("availablePromotion", promotionList.size());
        BigDecimal availableAmount=BigDecimal.ZERO;
        for (JSONObject jsonObject : promotionList) {
            BigDecimal balance = jsonObject.getBigDecimal("balance");
            if (balance!=null){
                availableAmount=availableAmount.add(balance);
            }
        }
        result.put("availableAmount", availableAmount);
        // 查询可用过期和已使用
        params.put("type",2);
        List<JSONObject> usedPromotion= marketingFlowService.getUseAvailablePromotionByUserId(params, request);
        result.put("usedPromotion",usedPromotion.size());
        BigDecimal usedAmount=BigDecimal.ZERO;
        for (JSONObject jsonObject : usedPromotion) {
            BigDecimal balance = jsonObject.getBigDecimal("balance");
            if (balance!=null){
                usedAmount=usedAmount.add(balance);
            }
        }
        result.put("usedAmount", usedAmount);
        //卡支付订单数
        Integer cardPayCount = qrPayFlowService.countPaidSuccessByUserId(userId, StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode());
        //分期付订单数
        Integer creditPayCount = qrPayFlowService.countPaidSuccessByUserId(userId, StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());
        result.put("cardPayCount", cardPayCount.toString());
        result.put("creditPayCount", creditPayCount.toString());

        //绑卡数
        int bindedCardCount = 0;

        JSONArray allCardList = cardService.getAllCardList(userId, request);
        if(CollectionUtils.isNotEmpty(allCardList)){
            bindedCardCount = allCardList.size();
        }

        /*JSONObject cardInfo = serverService.getAccountInfo(userId);
        JSONArray cardDTOListFromServer = cardInfo.getJSONArray("cardDTOList");
        int bindedCardCount = 0;
        for (int i = 0; i < cardDTOListFromServer.size(); i++ ) {
            JSONObject resultJsonObj = new JSONObject();
            JSONObject cardJsonObj = cardDTOListFromServer.getJSONObject(i);
            if(cardJsonObj.getInteger("type").intValue() == 1){
                //卡
                bindedCardCount += 1;
            }
        }*/
        result.put("tiedCardCount", bindedCardCount);

        //成功推荐注册用户总数
        result.put("invitationToRegister", payUser.getInvitationToRegister());
        params.clear();
        params.put("operationType",2);
        params.put("userId",userId);
        int total = this.inviteUserCount(params);
        //推荐注册已消费用户数
        result.put("inviteConsumption", total);
        params.put("operationType",3);
        int totals = this.inviteUserCount(params);
        //推荐注册未消费用户数
        result.put("inviteUnConsumption", totals);


        // 查询用户支付状态是否正常
        JSONObject param=new JSONObject();
        param.put("userId",userId);
        LoginMissDTO oneLoginMiss = loginMissService.findOneLoginMiss(param);
        if (oneLoginMiss==null||oneLoginMiss.getId()==null){
            result.put("payState",0);
        }else {
            result.put("payState",1);
        }
        // 获取用户认证阶段
        List<UserStepDTO> userStepDTOList = userStepService.findUserStepByUserId(userId);
        List<JSONObject> userSept=new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            JSONObject stepObject=new JSONObject();
            if (i==0){
                UserStepDTO userStepDTO = userStepDTOList.get(0);
                stepObject.put("stepState",userStepDTO.getStepState());
                stepObject.put("step",1);
                userSept.add(stepObject);
                if (userStepDTOList.get(1)!=null){
                    stepObject.put("id",userStepDTOList.get(0).getId().toString());
                }
            }
            if (i==1){
                UserDTO userById = this.findUserById(userId);
                Integer cardState = userById.getCardState();
                if (cardState!=null&&cardState.intValue()==0){
                    // 未绑卡
                    JSONObject cardStepData=new JSONObject();
                    cardStepData.put("stepState",0);
                    cardStepData.put("step",2);
                    userSept.add(cardStepData);
                }else {
                    JSONObject cardStepData=new JSONObject();
                    cardStepData.put("stepState",1);
                    cardStepData.put("step",2);
                    userSept.add(cardStepData);
                    // 已绑卡
                }
            }
            if (i==2){
                UserStepDTO userStepDTO = userStepDTOList.get(1);
                UserStepDTO userStepDTO1 = userStepDTOList.get(2);
                if (userStepDTOList.get(1)!=null){
                    stepObject.put("id",userStepDTOList.get(2).getId().toString());
                }
                if (userStepDTO.getStepState()==0){
                    stepObject.put("stepState",0);
                    stepObject.put("step",3);
                    userSept.add(stepObject);
                }else if (userStepDTO.getStepState()==2||userStepDTO1.getStepState()==2){
                    stepObject.put("stepState",2);
                    stepObject.put("step",3);
                    userSept.add(stepObject);
                }else if (userStepDTO.getStepState()==1&&userStepDTO1.getStepState()==1){
                    stepObject.put("stepState",1);
                    stepObject.put("step",3);
                    userSept.add(stepObject);
                }else {
                    stepObject.put("stepState",0);
                    stepObject.put("step",3);
                    userSept.add(stepObject);
                }
            }
        }
        result.put("userStep", userSept);
        // 获取kyc认证记录
        UserStepLogDTO kycLog = userStepService.findUserStepLatestLog(userId, StaticDataEnum.USER_STEP_1.getCode());
        if (kycLog != null && !StringUtils.isEmpty(kycLog.getRiskBatchNo())) {
            JSONArray kycRiskLogs = serverService.findRiskLog(kycLog.getRiskBatchNo());
            result.put("kycDate", new SimpleDateFormat("HH:mm dd/MM/yyyy").format(new Date(kycLog.getCreatedDate())));
            result.put("kycRiskLogs", kycRiskLogs);
        }
        // 获取分期付风控记录
        JSONObject installmentRiskLog = serverService.findInstallmentRiskLog(userId);
        result.put("installmentRiskLog", installmentRiskLog);
        // 获取用户KYC历史数据
        param.clear();
        param.put("userId",userId);
        param.put("update_text",StaticDataEnum.USER_INFO_UPDATE_16.getCode()+",");
        UserInfoUpdateLogDTO oneUserInfoUpdateLog = userInfoUpdateLogService.findOneUserInfoUpdateLogMax(param);
        JSONObject kycLogS=new JSONObject();
        if (oneUserInfoUpdateLog!=null){
            if (StringUtils.isNotEmpty(oneUserInfoUpdateLog.getData())){
                kycLogS=JSONObject.parseObject(oneUserInfoUpdateLog.getData());
            }
        }
        result.put("kycLog",kycLogS);
        // 20220216 新增逾期费订单数逾期费
        List<JSONObject> overdueOrders= userDAO.findUserOverdueOrders(userId);
        BigDecimal overdueOrderAmount=BigDecimal.ZERO;
        for (JSONObject overdueOrder : overdueOrders) {
            BigDecimal borrow_amount = overdueOrder.getBigDecimal("borrow_amount");
            overdueOrderAmount=overdueOrderAmount.add(borrow_amount);
        }
        BigDecimal overdueFeeAmount=BigDecimal.ZERO;
        result.put("overdueOrderAmount",overdueOrderAmount);
        result.put("overdueOrderAmountSize",overdueOrders.size());
        return result;
    }

    /**
     * @param userId
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/10 16:38
     */
    @Override
    public Object getCardAndAccountList(Long userId, HttpServletRequest request) throws Exception {
        JSONObject accountInfo = serverService.getAccountInfo(userId);
        if(null == accountInfo || null == accountInfo.getJSONArray("cardDTOList")
                || accountInfo.getJSONArray("cardDTOList").size() == 0){
            return Collections.emptyList();
        }
        JSONArray dataList = accountInfo.getJSONArray("cardDTOList");
        List<JSONObject> dataLists = this.getCardListEpoch(userId, StaticDataEnum.TIE_CARD_1.getCode(), request);
        JSONObject result = new JSONObject();
        JSONArray resultAccountArray = new JSONArray();
        for (int i = 0; i < dataList.size(); i++ ) {
            JSONObject resultJsonObj = new JSONObject();
            JSONObject cardJsonObj = dataList.getJSONObject(i);
            if(cardJsonObj.getInteger("type").intValue() == 0){
                //账户
                resultJsonObj.put("bsb", cardJsonObj.getString("bsb"));
                resultJsonObj.put("bankName", cardJsonObj.getString("bankName"));
                resultJsonObj.put("accountName", cardJsonObj.getString("accountName"));
                resultJsonObj.put("cardNo", cardJsonObj.getString("cardNo"));
                resultAccountArray.add(resultJsonObj);
            }
        }

        JSONArray resultCardArray = cardService.getAllCardList(userId, request);
        /*tring[] arr={"cardType"};
        for (JSONObject list : dataLists) {
            JSONObject resultJsonObj = new JSONObject();
            JSONObject cardJsonObj = list;
            if(cardJsonObj.getInteger("type").intValue() == 1){
                //卡
                resultJsonObj.put("cardNo", cardJsonObj.getString("cardNo"));
                resultJsonObj.put("cardId", cardJsonObj.getString("id"));
                resultJsonObj.put("preset", cardJsonObj.getInteger("preset"));
                resultJsonObj.put(" 0   ", cardJsonObj.getInteger("order"));
                String customerCcType = cardJsonObj.getString("customerCcType");
                resultJsonObj.put("customerCcType", customerCcType);
                resultCardArray.add(resultJsonObj);

            }
        }*/

        result.put("card", resultCardArray);
        result.put("account", resultAccountArray);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void frozenUser(JSONObject param, HttpServletRequest request) throws Exception {
        // 查询用户修改表是否有该用户，没有直接返回，有则对该用户进行解冻冻结操作
        Long userId = param.getLong("userId");
        UserDTO userById = this.findUserById(userId);
        if (userById==null){
            throw new BizException(I18nUtils.get("user.doesnt.exist", getLang(request)));
        }
        JSONObject params=new JSONObject();
        Integer state = param.getInteger("state");
        params.put("userId",userId);
        params.put("type",state);
        JSONObject paramButton=new JSONObject();
        paramButton.put("userId",getUserId(request));
        paramButton.put("type",state);
        List<UserActionButtonDTO> userActionButtonDTOS = userActionButtonService.find(paramButton, null, null);
        if (userActionButtonDTOS==null||userActionButtonDTOS.size()==0){
            throw new BizException(I18nUtils.get("action.rule.sign", getLang(request)));
        }
        UserInfoUpdateLogDTO userInfoUpdateLog=new UserInfoUpdateLogDTO();
        userInfoUpdateLog.setUserId(userId);
        if (state.intValue()==StaticDataEnum.USER_FROZEN_TYPE_0.getCode()){
            // 账户 查询账户状态
            params.clear();
            params.put("userId",userId);
            params.put("status",0);
            LoginMissDTO oneLoginMiss = loginMissService.findOneLoginMiss(params);
            params.clear();
            params.put("userId",userId);
            LoginMissDTO oneLoginMissStatus = loginMissService.findOneLoginMiss(params);
            if (oneLoginMiss!=null&&oneLoginMiss.getId()!=null){
                // 解冻
                loginMissService.updateLoginMissStatus(oneLoginMiss.getId(),oneLoginMiss,request);
                userInfoUpdateLog.setUpdateId("10,");
            }else if (oneLoginMissStatus!=null&&oneLoginMissStatus.getId()!=null){
                // 冻结
                String key=userId + "_" + userById.getPhone() + "_" + StaticDataEnum.USER_TYPE_10.getCode();
                loginMissService.logicDeleteLoginMiss(oneLoginMissStatus.getId(),request);
                userInfoUpdateLog.setUpdateId("9,");
                redisUtils.del(key);
            }else {
                // 用户不存在 查询用户表是否有该用户没有报错，有则解冻（插入一条loginmiss）
                if (userById!=null){
                    LoginMissDTO loginMissDTO=new LoginMissDTO();
                    loginMissDTO.setUserId(userId);
                    loginMissDTO.setChance(StaticDataEnum.LOGIN_MISS_TIME.getCode());
                    loginMissService.saveLoginMiss(loginMissDTO,request);
                }else {
                    throw new BizException(I18nUtils.get("user.doesnt.exist", getLang(request)));
                }
            }
        }else if (state.intValue()==StaticDataEnum.USER_FROZEN_TYPE_1.getCode()){
            // 分期付 查询分期付状态
            params.clear();
            params.put("userId",userId);
            JSONObject creditUserInfo = serverService.findCreditUserInfo(params);
            if (creditUserInfo==null||creditUserInfo.getLong("userId")==null){
                throw new BizException("The user has not activated the installment payment");
            }
            Integer states = creditUserInfo.getInteger("state");
            if (states!=null){
                if (states.intValue()!=StaticDataEnum.CREAT_USER_STATE_11.getCode()){
                    // 冻结
                    creditUserInfo.put("state",StaticDataEnum.CREAT_USER_STATE_11.getCode());
                    userInfoUpdateLog.setUpdateId("8,");
                }else {
                    creditUserInfo.put("state",StaticDataEnum.CREAT_USER_STATE_20.getCode());
                    userInfoUpdateLog.setUpdateId("11,");
                    // 解冻
                }
                try{
                    JSONObject jsonObject = serverService.updateUserCreditStateV1(creditUserInfo, request);
                    String userId1 = jsonObject.getString("userId");
                }catch (Exception e){
                    log.error("同步分期付状态异常",e);
                    throw e;
                }

            }
        }
        userInfoUpdateLogService.saveUserInfoUpdateLog(userInfoUpdateLog,request);
    }

    @Override
    public int countUserUpdateLog(JSONObject param) {
        return userInfoUpdateLogService.count(param);
    }

    @Override
    public List<UserInfoUpdateLogDTO> findUserUpdateLog(JSONObject param, Vector<SortingContext> scs, PagingContext pc) {
        return userInfoUpdateLogService.find(param,scs,pc);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUserInfo(JSONObject param, HttpServletRequest request) throws Exception {
        // 参数校验
        if (param==null){
            throw new BizException(I18nUtils.get("parameters.error",getLang(request)));
        }
        Long userId = param.getLong("userId");
        String email = param.getString("email");
        String phone = param.getString("phone");
        Integer state = param.getInteger("state");
        String city = param.getString("city");
        String address = param.getString("address");
        String etc = param.getString("etc");
        String postCode = param.getString("postCode");
        if (userId==null){
            throw new BizException(I18nUtils.get("parameters.error",getLang(request)));
        }
        if (phone != null) {
            String phoneCode = new StringBuilder(phone).substring(0, 2);
            String phones = new StringBuilder(phone).substring(2, phone.length());
            if (phoneCode.equals(StaticDataEnum.CHN.getMessage())) {
                if (phones.length() > Validator.PHONE_LENGTH) {
                    throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
                }
            } else {
                if (phones.length() > Validator.PHONE_AUD_LENGTH) {
                    throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
                }
            }
        }
        if (!Validator.isEmail(email)){
            throw new BizException(I18nUtils.get("email.format.error", getLang(request)));
        }
        if (email == null || email.length() > Validator.TEXT_LENGTH_100 || !(Validator.isEmail(email))) {
            throw new BizException(I18nUtils.get("email.format.error", getLang(request)));
        }
        if(postCode != null  ){
            if(!RegexUtils.isAuPostcode(postCode)){
                throw new BizException(I18nUtils.get("postcode.format.error", getLang(request)));
            }
        }
        // 查询用户信息
        UserDTO userById = this.findUserById(userId);
        JSONObject params=new JSONObject();
        params.put("userId",userId);
        JSONObject oneUserInfo = serverService.findOneUserInfo(params);
        // 判断手机号是否重复
        params.clear();
        params.put("phone",phone);
        UserDTO oneUser = this.findOneUser(params);
        Long userId1 = oneUser.getId();
        if (userId1!=null&&userId1.longValue()!=userId.longValue()){
            throw new BizException(I18nUtils.get("phone.is.been.registered",getLang(request)));
        }
        // 获取对比值
        Integer stateOld = oneUserInfo.getInteger("state");
        String stateStrOld = "";
        String cityOld = oneUserInfo.getString("city");
        String addressOld = oneUserInfo.getString("address");
        String etcOld = oneUserInfo.getString("aptSuiteEtc");
        String postCodeOld = oneUserInfo.getString("postcode");
        String phoneOld = userById.getPhone();
        String emailOld = userById.getEmail();
        UserInfoUpdateLogDTO userInfoUpdateLog=new UserInfoUpdateLogDTO();
        StringBuilder updateId=new StringBuilder();
        StringBuilder updateFeild=new StringBuilder();
        boolean isDelToken=false;
        // 判断对比值
        // 州
        JSONObject paramCode=new JSONObject();
        paramCode.put("code","merchantState");
        List<StaticDataDTO> staticDataDTOS = staticDataService.find(paramCode,null,null);
        for (StaticDataDTO staticDataDTO : staticDataDTOS) {
            if (stateOld!=null){
                if (staticDataDTO.getValue().equals(stateOld.toString())){
                    if (getLang(request).getLanguage().equals(Locale.SIMPLIFIED_CHINESE.getLanguage())){
                        stateStrOld=staticDataDTO.getName();
                    }else {
                        stateStrOld=staticDataDTO.getEnName();
                    }
                }
            }
        }
        if (state!=null){
            if ((stateOld!=null&&stateOld.intValue()!=state.intValue())||stateOld==null){
                updateId.append("1,");
                updateFeild.append(stateStrOld+",");
            }
        }else {
            if (stateOld!=null){
                updateId.append("1,");
                updateFeild.append(stateStrOld+",");
            }
        }
        // 城市
        if (StringUtils.isNotEmpty(city)){
            if ((StringUtils.isNotEmpty(cityOld)&&(!cityOld.equals(city)))||StringUtils.isEmpty(cityOld)){
                updateId.append("2,");
                updateFeild.append(cityOld+",");
            }
        }else {
            if (StringUtils.isNotEmpty(cityOld)){
                updateId.append("2,");
                updateFeild.append(cityOld+",");
            }
        }
        // address
        if (StringUtils.isNotEmpty(address)){
            if ((StringUtils.isNotEmpty(addressOld)&&(!addressOld.equals(address)))||StringUtils.isEmpty(addressOld)){
                updateId.append("3,");
                updateFeild.append(addressOld+",");
            }
        }else {
            if (StringUtils.isNotEmpty(addressOld)){
                updateId.append("3,");
                updateFeild.append(addressOld+",");
            }
        }
        // etc
        if (StringUtils.isNotEmpty(etc)){
            if ((StringUtils.isNotEmpty(etcOld)&&(!etcOld.equals(etc)))||StringUtils.isEmpty(etcOld)){
                updateId.append("4,");
                updateFeild.append(etcOld+",");
            }
        }else {
            if (StringUtils.isNotEmpty(etcOld)){
                updateId.append("4,");
                updateFeild.append(etcOld+",");
            }
        }
        // postCode
        if (StringUtils.isNotEmpty(postCode)){
            if ((StringUtils.isNotEmpty(postCodeOld)&&(!postCodeOld.equals(postCode)))||StringUtils.isEmpty(postCodeOld)){
                updateId.append("5,");
                updateFeild.append(postCodeOld+",");
            }
        }else {
            if (StringUtils.isNotEmpty(postCodeOld)){
                updateId.append("5,");
                updateFeild.append(postCodeOld+",");
            }
        }
        // phone
        if (StringUtils.isNotEmpty(phone)){
            if ((StringUtils.isNotEmpty(phoneOld)&&(!phoneOld.equals(phone)))||StringUtils.isEmpty(phoneOld)){
                updateId.append("6,");
                updateFeild.append(phoneOld+",");
                isDelToken=true;
            }
        }else {
            if (StringUtils.isNotEmpty(phoneOld)){
                updateId.append("6,");
                updateFeild.append(phoneOld+",");
                isDelToken=true;
            }
        }
        // email
        if (StringUtils.isNotEmpty(email)){
            if ((StringUtils.isNotEmpty(emailOld)&&(!emailOld.equals(email)))||StringUtils.isEmpty(emailOld)){
                updateId.append("7,");
                updateFeild.append(emailOld+",");
            }
        }else {
            if (StringUtils.isNotEmpty(emailOld)){
                updateId.append("7,");
                updateFeild.append(emailOld+",");
            }
        }
        if (StringUtils.isEmpty(updateFeild.toString())){
            return;
        }
        userInfoUpdateLog.setUpdateId(updateId.toString());
        userInfoUpdateLog.setUpdateText(updateFeild.toString());
        userInfoUpdateLog.setUserId(userId);
        // 修改支付系统
        userById.setPhone(phone);
        userById.setEmail(email);
        this.updateUser(userId,userById,request);
        // 添加修改记录
        userInfoUpdateLogService.saveUserInfoUpdateLog(userInfoUpdateLog,request);
        // 修改账户系统
        oneUserInfo.clear();
        oneUserInfo.put("userId",userId);
        oneUserInfo.put("phone",phone);
        oneUserInfo.put("userId",userId);
        oneUserInfo.put("state",state);
        oneUserInfo.put("city",city);
        oneUserInfo.put("address",address);
        oneUserInfo.put("aptSuiteEtc",etc);
        oneUserInfo.put("postcode",postCode);
        oneUserInfo.put("email",email);
        serverService.infoSupplement(oneUserInfo,request);
        oneUserInfo.clear();
        oneUserInfo.put("userId",userId);
        oneUserInfo.put("phone",phone);
        oneUserInfo.put("email",email);
        oneUserInfo.put("address",address+" "+postCode);
        // 修改分期付系统
        try{
            serverService.infoSupplementCredit(oneUserInfo,request);
        }catch (Exception e){
            log.error("同步分期付失败");
        }
        // 同步braze
        BrazeUtil.trackUser(userById);
        // 删除token
        if (isDelToken){
            String key=userById.getId() + "_" + phoneOld + "_" + StaticDataEnum.USER_TYPE_10.getCode();
            redisUtils.del(key);
        }

    }

    /**
     * 获取用户绑卡状态和绑卡时间
     *
     * @param userId
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/15 10:57
     */
    @Override
    public Object getUserCardStateAndBindCardDate(Long userId, HttpServletRequest request) throws Exception {
        JSONObject result = new JSONObject();

        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
        map.put("id", userId);

        UserDTO userDTO = userDAO.selectOneDTO(map);
        if(null == userDTO || null == userDTO.getId()){
            throw new BizException(I18nUtils.get("user.doesnt.exist", getLang(request)));
        }
        Integer cardState = userDTO.getCardState();
        result.put("cardState", cardState);

        JSONObject resultJson = serverService.getAccountInfo(userId);
        if(null == resultJson || null == resultJson.getJSONArray("cardDTOList")
                || resultJson.getJSONArray("cardDTOList").size() == 0){
            result.put("cardState", 0);
            return result;
        }

        JSONArray accountJsonArray = resultJson.getJSONArray("cardDTOList");
        List<CardDTO>  allAccountList = JSONArray.parseArray(accountJsonArray.toJSONString(), CardDTO.class);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        long maxBindCardTime = 0;
        for(CardDTO cardDTO: allAccountList){
            if(cardDTO.getType() == 1){
                //卡
                String createdDate = cardDTO.getCreatedDate();
                Date createDate = simpleDateFormat.parse(createdDate);
                long time = createDate.getTime();

                if(time > maxBindCardTime){
                    maxBindCardTime = time;
                }
            }
        }

        if(maxBindCardTime == 0){
            //没有数据
            result.put("cardState", 0);
            return result;
        }

        result.put("bindCardTime", String.valueOf(maxBindCardTime));
        return result;
    }

    /**
     * 获取用户欠款列表
     *
     * @param requestInfo
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/16 20:10
     */
    @Override
    public Object getUserDetailRepayList(JSONObject requestInfo, HttpServletRequest request) throws Exception {


//        return JSONObject.parseObject(HttpClientUtils.sendPost(creditUrl + "/payremote/borrowByPaydayV2", requestInfo.toJSONString()));
        String s = HttpClientUtils.sendPost(creditUrl+ "/payremote/borrowByPaydayV2", requestInfo.toJSONString());
        JSONObject jsonObject = JSONObject.parseObject(s);
        JSONObject data = jsonObject.getJSONObject("data");
        if (data!=null){
            JSONArray list1 = data.getJSONArray("list");
            List<JSONObject> list2 = list1.toJavaList(JSONObject.class);
            for (JSONObject jsonObject1 : list2) {
                JSONArray inJSONArray = jsonObject1.getJSONArray("list");
                List<JSONObject> list = inJSONArray.toJavaList(JSONObject.class);
                for (JSONObject object : list) {
                    String borrowId = object.getString("borrowId")+"";
                    object.put("borrowId",borrowId);
                }
            }
        }
        return data;
    }

    /**
     * 获取订单的还款计划
     *
     * @param borrowId
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/17 16:11
     */
    @Override
    public Object getUserDetailRepayListById(Long borrowId, HttpServletRequest request){
        return userDAO.getUserDetailRepayListById(borrowId);
    }

    @Override
    public JSONObject findUserUseAPP(@NonNull Long userId, HttpServletRequest request) throws Exception {
        JSONObject  result=new JSONObject();
        JSONObject  param=new JSONObject();
        // 查询首次登陆时间
        UserDTO userById = this.findUserById(userId);
        if (userById==null||userById.getId()==null){
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        // 第一次登陆时间
        result.put("userInfo",simpleDateFormat.format(new Date(userById.getCreatedDate())));
        // 最近一次登陆时间
        if (userById.getLoginTime()!=null){
            result.put("lastLoginTime",simpleDateFormat.format(new Date(userById.getLoginTime())));
        }
        // 最近一次使用分期付时间
        param.put("userId",userId);
        param.put("transType",StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());
        QrPayFlowDTO qrPayFlowDTOCredit=qrPayFlowService.findMaxUserUseById(param,request);
        if (qrPayFlowDTOCredit!=null){
            Long createdDate = qrPayFlowDTOCredit.getCreatedDate();
            result.put("creditInfo",simpleDateFormat.format(new Date(createdDate)));
        }
        // 最近一次卡支付时间
        param.clear();
        param.put("userId",userId);
        param.put("transType",StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode());
        QrPayFlowDTO qrPayFlowDTOCard=qrPayFlowService.findMaxUserUseById(param,request);
        if (qrPayFlowDTOCard!=null){
            Long createdDate = qrPayFlowDTOCard.getCreatedDate();
            result.put("cardInfo",simpleDateFormat.format(new Date(createdDate)));
        }
        // 最近一次还款时间
        JSONObject data=new JSONObject();
        try{
            data= serverService.findUserUseRepay(param,request);
            if (data!=null){
                String createdDate = data.getString("createdDate");
                result.put("repayInfo",createdDate);
            }
        }catch (Exception e){
            log.error("查询最近一次还款数据失败:e{}",e);
        }
        result.put("lastRepay",data);
        // 最近一次使用时长
        UserDTO userDTO = this.findOneUserAppInfoById(userId);
        result.put("user",userDTO);
        return result;
    }

    private UserDTO findOneUserAppInfoById(Long userId) {
        return userDAO.findOneUserAppInfoById(userId);
    }

    /**
     * 用户详情 - 用户使用红包记录数量
     *
     * @param params
     * @return int
     * @author zhangzeyuan
     * @date 2021/9/13 16:09
     */
    @Override
    public int usedPayoMoneyCount(Map<String, Object> params) {
        return userDAO.usedPayoMoneyCount(params);
    }

    /**
     * 用户详情= 用户使用红包记录列表
     *
     * @param params
     * @param scs
     * @param pc
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDetailInviteUserDTO>
     * @author zhangzeyuan
     * @date 2021/9/22 14:06
     */
    @Override
    public List<UserDetailUsedPayoMoneyDTO> usedPayoMoneyList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        return userDAO.usedPayoMoneyList(params);
    }

    /**
     * 用户详情 - 用户还款记录数量
     *
     * @param params
     * @return int
     * @author zhangzeyuan
     * @date 2021/9/22 16:52
     */
    @Override
    public int repaymentHistoryCount(Map<String, Object> params) {
        return userDAO.repaymentHistoryCount(params).size();
    }

    /**
     * @param params
     * @param scs
     * @param pc
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDetailUsedPayoMoneyDTO>
     * @author zhangzeyuan
     * @date 2021/9/22 16:53
     */
    @Override
    public List<UserDetailRepaymentHistoryDTO> repaymentHistoryList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        return userDAO.repaymentHistoryList(params);
    }

    /**
     * 用户详情 - 用户还款记录详情数量
     *
     * @param params
     * @return int
     * @author zhangzeyuan
     * @date 2021/9/22 16:52
     */
    @Override
    public int repaymentHistoryDetailCount(Map<String, Object> params) {
        return userDAO.repaymentHistoryDetailCount(params).size();
    }

    /**
     * 用户详情 - 用户还款记录详情列表
     *
     * @param params
     * @param scs
     * @param pc
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDetailUsedPayoMoneyDTO>
     * @author zhangzeyuan
     * @date 2021/9/22 16:57
     */
    @Override
    public List<UserDetailRepaymentHistoryDetailDTO> repaymentHistoryDetailList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        return userDAO.repaymentHistoryDetailList(params);
    }

    /**
     * 获取用户 未还欠款 还款日 数量
     *
     * @param params
     * @return int
     * @author zhangzeyuan
     * @date 2021/9/23 10:49
     */
    @Override
    public int userDetailRepayDateCount(Map<String, Object> params) {
        return userDAO.userDetailRepayDateCount(params);
    }

    /**
     * 用户详情 - 分期付未还欠款列表
     *
     * @param params
     * @param scs
     * @param pc
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDetailUsedPayoMoneyDTO>
     * @author zhangzeyuan
     * @date 2021/9/22 16:57
     */
    @Override
    public List<UserDetailRepaymentDTO> userDetailRepayListGroupByDate(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        //获取还款日数据
        params = getUnionParams(params, scs, pc);
        List<Long> repayDateList = userDAO.userDetailRepayDateList(params);

        if(CollectionUtils.isEmpty(repayDateList)){
            return Collections.emptyList();
        }
        //返回数据
        List<UserDetailRepaymentDTO> resultList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE dd MMM", Locale.ENGLISH);
        //用户ID
        String userId = params.get("userId").toString();
        //根据还款日查询订单详情数据
        for(Long expectRepayTime: repayDateList){
            UserDetailRepaymentDTO repaymentDTO = new UserDetailRepaymentDTO();
            repaymentDTO.setExpectRepayTime(expectRepayTime);
            repaymentDTO.setExpectRepayTimeStr(simpleDateFormat.format(new Date(expectRepayTime)));
            List<UserDetailRepaymentDetailDTO> userDetailRepaymentDetailDTOS = userDAO.userRepaymentDetailByDate(userId, expectRepayTime);
            BigDecimal amount=BigDecimal.ZERO;
            for (UserDetailRepaymentDetailDTO userDetailRepaymentDetailDTO : userDetailRepaymentDetailDTOS) {
                userDetailRepaymentDetailDTO.setExpectRepayTimeStr(simpleDateFormat.format(new Date(expectRepayTime)));
                amount=amount.add(userDetailRepaymentDetailDTO.getShouldPayAmt());
            }
            repaymentDTO.setShouldPayAmount(amount);
            repaymentDTO.setDetailList(userDetailRepaymentDetailDTOS);
            resultList.add(repaymentDTO);
        }
        return resultList;
    }

    /**
     * 新用户8点邮件发送
     *
     * @author zhangzeyuan
     * @date 2021/9/24 15:42
     */
    @Override
    public void newUser8amEmailScheduled() {
        if(TestEnvUtil.isTestEnv()){
            return;
        }
        //day3
        long day3StartTime = getBeforeDaysStartEndTimestamp(2, 1);
        long day3EndTime = getBeforeDaysStartEndTimestamp(2, 2);

        HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        paramMap.put("start", day3StartTime);
        paramMap.put("end", day3EndTime);
        List<UserDTO> day3UserList = userDAO.userListBySendMail(paramMap);

        try {
            sendNewUserEmail(day3UserList, String.valueOf(StaticDataEnum.SEND_NODE_41.getCode()), false, true);
        }catch (Exception e){
            log.error("定时任务---发送新用户days3邮件失败============!!!!, e:{}", e.getMessage());
        }

        //day8
        paramMap.clear();
        long day8StartTime = getBeforeDaysStartEndTimestamp(7, 1);
        long day8EndTime = getBeforeDaysStartEndTimestamp(7, 2);

        paramMap.put("start", day8StartTime);
        paramMap.put("end", day8EndTime);
        List<UserDTO> day8UserList = userDAO.userListBySendMail(paramMap);
        try {
            sendNewUserEmail(day8UserList, String.valueOf(StaticDataEnum.SEND_NODE_45.getCode()), false, true);
        }catch (Exception e){
            log.error("定时任务---发送新用户days8邮件失败============!!!!, e:{}", e.getMessage());
        }

        //day13 有交易
        paramMap.clear();
        long day13StartTime = getBeforeDaysStartEndTimestamp(12, 1);
        long day13EndTime = getBeforeDaysStartEndTimestamp(12, 2);

        paramMap.put("start", day13StartTime);
        paramMap.put("end", day13EndTime);
        paramMap.put("firstDealState", 1);
        List<UserDTO> day13HasTransUserList = userDAO.userListBySendMail(paramMap);

        try {
            sendNewUserEmail(day13HasTransUserList, String.valueOf(StaticDataEnum.SEND_NODE_47.getCode()), true, true);
        }catch (Exception e){
            log.error("定时任务---发送新用户days13有交易邮件失败============!!!!, e:{}", e.getMessage());
        }


        //day13 无交易
        paramMap.put("firstDealState", 0);
        List<UserDTO> day13HasNotTransUserList = userDAO.userListBySendMail(paramMap);

        try {
            sendNewUserEmail(day13HasNotTransUserList, String.valueOf(StaticDataEnum.SEND_NODE_48.getCode()), true, true);
        }catch (Exception e){
            log.error("定时任务---发送新用户days13无交易邮件失败============!!!!, e:{}", e.getMessage());
        }


        //day20
        paramMap.clear();
        long day20StartTime = getBeforeDaysStartEndTimestamp(19, 1);
        long day20EndTime = getBeforeDaysStartEndTimestamp(19, 2);
        paramMap.put("start", day20StartTime);
        paramMap.put("end", day20EndTime);
        List<UserDTO> day20UserList = userDAO.userListBySendMail(paramMap);
        try {
            sendNewUserEmail(day20UserList, String.valueOf(StaticDataEnum.SEND_NODE_49.getCode()), false, true);
        }catch (Exception e){
            log.error("定时任务---发送新用户days8邮件失败============!!!!, e:{}", e.getMessage());
        }
    }

    /**
     * 新用户 4am 邮件发送
     *
     * @author zhangzeyuan
     * @date 2021/9/26 14:12
     */
    @Override
    public void newUser4pmEmailScheduled() {
        if(TestEnvUtil.isTestEnv()){
            return;
        }
        //day4 sms
        try {
            sendNewUserDay4SMS();
        }catch (Exception e){
            log.error("定时任务---发送新用户day4 sms 失败============!!!!, e:{}", e.getMessage());
        }

        //day6 push
        try {
            HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
            long day6StartTime = getBeforeDaysStartEndTimestamp(5, 1);
            long day6EndTime = getBeforeDaysStartEndTimestamp(5, 2);

            paramMap.put("start", day6StartTime);
            paramMap.put("end", day6EndTime);
            paramMap.put("pushToken", 1);
            List<UserDTO> day6UserList = userDAO.userListBySendMail(paramMap);

            sendNewUserPush(day6UserList, String.valueOf(StaticDataEnum.SEND_NODE_43.getCode()));
        }catch (Exception e){
            log.error("定时任务---发送新用户days6 push失败============!!!!, e:{}", e.getMessage());
        }


        //day7 push
        try {
            HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
            long day7StartTime = getBeforeDaysStartEndTimestamp(6, 1);
            long day7EndTime = getBeforeDaysStartEndTimestamp(6, 2);

            paramMap.put("start", day7StartTime);
            paramMap.put("end", day7EndTime);
            paramMap.put("pushToken", 1);
            List<UserDTO> day7UserList = userDAO.userListBySendMail(paramMap);

            sendNewUserPush(day7UserList, String.valueOf(StaticDataEnum.SEND_NODE_44.getCode()));
        }catch (Exception e){
            log.error("定时任务---发送新用户days7 push失败============!!!!, e:{}", e.getMessage());
        }


    }

    /**
     * 新用户 1pm 邮件发送
     *
     * @author zhangzeyuan
     * @date 2021/9/26 14:12
     */
    @Override
    public void newUser1pmEmailScheduled() {

        if(TestEnvUtil.isTestEnv()){
            return;
        }

        //day10 push
        try {
            HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
            long day7StartTime = getBeforeDaysStartEndTimestamp(9, 1);
            long day7EndTime = getBeforeDaysStartEndTimestamp(9, 2);

            paramMap.put("start", day7StartTime);
            paramMap.put("end", day7EndTime);
            paramMap.put("pushToken", 1);
            List<UserDTO> day10UserList = userDAO.userListBySendMail(paramMap);

            sendNewUserPush(day10UserList, String.valueOf(StaticDataEnum.SEND_NODE_46.getCode()));
        }catch (Exception e){
            log.error("定时任务---发送新用户days10 push失败============!!!!, e:{}", e.getMessage());
        }
    }

    /**
     * 发送新用户首次交易2小时后邮件
     *
     * @param userId
     * @author zhangzeyuan
     * @date 2021/9/26 15:43
     */
    @Override
    public void sendNewUserFirstPayAfter2HoursEmail(Long userId) {

        if(null == userId){
            return;
        }

        HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
        paramMap.put("userId", userId);
        List<UserDTO> UserList = userDAO.userListBySendMail(paramMap);

        try {
            sendNewUserEmail(UserList, String.valueOf(StaticDataEnum.SEND_NODE_50.getCode()), true, true);
        }catch (Exception e){
            log.error("定时任务---发送新用户days3邮件失败============!!!!, e:{}", e.getMessage());
        }
    }

    /**
     * 获取用户协议状态
     *
     * @param userId
     * @author zhangzeyuan
     * @date 2021/9/28 15:17
     */
    @Override
    public Object getUserAgreementState(Long userId, HttpServletRequest request) throws Exception {
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
        map.put("id", userId);
        UserDTO userDTO = userDAO.selectOneDTO(map);
        if(null == userDTO || null == userDTO.getId()){
            throw new BizException(I18nUtils.get("user.exist", getLang(request)));
        }
        return userDTO.getReadAgreementState();
    }

    /**
     * 更新用户协议状态
     *
     * @param userId
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/28 15:33
     */
    @Override
    public void updateUserAgreementState(Long userId, HttpServletRequest request) throws Exception {
        userDAO.updateUserAgreementState(userId);
    }

    /**
     * 更新用户分期付卡还款协议状态
     *
     * @param userId
     * @param request
     * @author zhangzeyuan
     * @date 2021/9/28 16:14
     */
    @Override
    public void updateUserCreditCardAgreementState(Long userId, HttpServletRequest request) throws Exception {
        userDAO.updateUserCreditCardAgreementState(userId);
    }

    /**
     * 获取用户默认卡信息
     *
     * @param userId
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/29 14:14
     */
    @Override
    public JSONObject getDefaultCardInfo(Long userId, HttpServletRequest request) throws Exception {
        JSONObject cardResult = new JSONObject();

        //查询用户状态
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(2);
        map.put("id", userId);
        UserDTO oneUser = findOneUser(map);
        if(null == oneUser || null == oneUser.getId()){
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }

        // 获取当前配置通道
        map.clear();
        map.put("gatewayType",StaticDataEnum.PAY_TYPE_0.getCode());
        map.put("state",StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(map);
        if(gatewayDTO == null || gatewayDTO.getId() == null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        // latpay通道标识
        boolean latPayState = false;

        // 如果当前通道是latpay查询latpay默认卡
        // 如果当前通道是stripe按用户状态查询默认卡
        if(gatewayDTO.getType() == StaticDataEnum.GATEWAY_TYPE_0.getCode()){
            latPayState = true;
        }else if(oneUser.getStripeState() == StaticDataEnum.USER_STRIPE_STATE_1.getCode()){
            latPayState = true;
        }else{
            latPayState = false;
        }

        if(latPayState){
            //latpay 默认卡
            CardDTO defaultCard = new CardDTO();
            List<CardDTO> cardDTOList = new ArrayList<>();
            //查询默认卡
            JSONObject cardInfo = serverService.getAccountInfo(userId);
            JSONArray cardDTOListFromServer = cardInfo.getJSONArray("cardDTOList");
            for (int i = 0; i < cardDTOListFromServer.size(); i ++) {
                if (cardDTOListFromServer.getJSONObject(i).getInteger("type").intValue() == StaticDataEnum.TIE_CARD_1.getCode() && StringUtils.isNotBlank(cardDTOListFromServer.getJSONObject(i).getString("crdStrgToken"))) {
                    CardDTO cardDTO = JSONObject.parseObject(cardDTOListFromServer.getJSONObject(i).toJSONString(), CardDTO.class);
                    cardDTOList.add(cardDTO);
                }
            }
            if(cardDTOList.size() > 0){
                List<CardDTO> listPreset = cardDTOList.stream().filter(dto->dto.getPreset() == StaticDataEnum.STATUS_1.getCode()).collect(Collectors.toList());
                if(listPreset != null && listPreset.size() > 0 ){
                    defaultCard = listPreset.get(0);
                }else{
                    List<CardDTO> list = cardDTOList.stream().sorted(Comparator.comparing(CardDTO::getOrder,Comparator.reverseOrder())).collect(Collectors.toList());
                    defaultCard = list.get(0);
                }
            }

            if(StringUtils.isNotBlank(defaultCard.getCrdStrgToken())){
                cardResult.put("cardNo", defaultCard.getCardNo());
                cardResult.put("customerCcType", defaultCard.getCustomerCcType());
                cardResult.put("cardId", defaultCard.getId());
                cardResult.put("crdStrgToken", defaultCard.getCrdStrgToken());
                try{
                    JSONObject result = this.getCardDetails(defaultCard.getCrdStrgToken(), request);
                    String customerCcExpyr = result.getString("customerCcExpyr");
                    String customerCcExpmo = result.getString("customerCcExpmo");
                    cardResult.put("customerCcExpyr",customerCcExpyr);
                    cardResult.put("customerCcExpmo",customerCcExpmo);
                }catch (Exception e){
                    cardResult.put("customerCcExpyr", "");
                    cardResult.put("customerCcExpmo", "");
                    log.error("获取获取卡过期日异常,e:{}",e.getMessage());
                }
            }
        }else{

            //获取stripe的卡
            List<CardDTO> stripeCardList = serverService.getStripeCardList(userId.toString());
            if(CollectionUtils.isEmpty(stripeCardList)){
                throw new BizException(I18nUtils.get("card.fetch.info.failed", getLang(request)));
            }

            //stripe默认卡
            boolean defalutState = false;
            CardDTO defaultCard = null;
            for(CardDTO cardDTO : stripeCardList){
                if(cardDTO.getPreset().equals(1)){
                    defalutState = true;

                    defaultCard = cardDTO;
                }
            }

            if(!defalutState){
                defaultCard = stripeCardList.get(0);
            }

            cardResult.put("cardNo", defaultCard.getCardNo());
            cardResult.put("customerCcType", defaultCard.getCustomerCcType());
            cardResult.put("cardId", defaultCard.getId());
            cardResult.put("stripeToken", defaultCard.getStripeToken());

            //查询有效期
            StripeAPIResponse stripeAPIResponse = stripeAPIService.retrieveCard(userId, defaultCard.getStripeToken());
            String expMonth = "";
            String expYear = "";
            if(stripeAPIResponse.isSuccess()){
                Card card = (Card) stripeAPIResponse.getData();
                Long tempExpMonth = card.getExpMonth();

                if(tempExpMonth.toString().length() == 1){
                    expMonth = "0" + card.getExpMonth();
                }else{
                    expMonth = card.getExpMonth().toString();
                }

                expYear = card.getExpYear().toString();
            }

            cardResult.put("customerCcExpyr", expYear);
            cardResult.put("customerCcExpmo", expMonth);
        }
        return cardResult;
    }

    /**
     * 获取APP首页弹窗提醒
     *
     * @param userId
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/9/29 16:57
     */
    @Override
    public Object getAppHomePageReminder(Long userId, HttpServletRequest request) throws Exception {
        JSONObject result = new JSONObject();
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(3);

        map.put("id", userId);
        UserDTO user = userDAO.selectOneDTO(map);

        if (null == user || null == user.getId()) {
            throw new BizException(I18nUtils.get("user.doesnt.exist", getLang(request)));
        }

        //设置pin
        int setPinState = 0;
        if (StringUtils.isBlank(user.getPinNumber())) {
            setPinState = 1;
        }
        //首页协议
        int readAgreementState = 1;
        if(0 != user.getReadAgreementState().intValue()){
            readAgreementState =  0;
        }

        //逾期
        int overdueState = 0;
        JSONObject overdueDetailResult = new JSONObject();
        overdueDetailResult.put("userId", userId);
        JSONObject overdueJsonObj = new JSONObject();
        overdueJsonObj.put("data", overdueDetailResult);
        try {
            String overdueResult = HttpClientUtils.post(creditUrl + "/payremote/user/queryRepayInfo", overdueJsonObj.toJSONString());
            overdueDetailResult.clear();
            if (StringUtils.isNotBlank(overdueResult)) {
                JSONObject resultJSON = JSONObject.parseObject(overdueResult);
                String code = resultJSON.getString("code");
                if (StringUtils.isNotBlank(code) && code.equals(ResultCode.OK.getCode())) {
                    JSONObject overdueDetail = resultJSON.getJSONObject("data");
                    overdueState = 1;
                    overdueDetailResult = overdueDetail;
                }
            }
        }catch (Exception e){
            log.error("获取分期付绑卡状态出错",e.getMessage());
        }

        //邀请数据
        int invitedState = 0;
        JSONObject invitedDetailResult = new JSONObject();


        map.clear();
        map.put("userId",userId);
        map.put("isShow",StaticDataEnum.MARKETING_IS_SHOW_1.getCode());
        MarketingLogDTO maxTimedTO = marketingLogService.findMaxTime(map);
        //查询最近已读时间
        long startTime = 0;
        if (null != maxTimedTO && null != maxTimedTO.getId()){
            startTime = maxTimedTO.getTime();
        }
        long endTime = System.currentTimeMillis();

        //查询该时间段内邀请消费数据
        map.clear();
        map.put("id",userId);
        map.put("start",startTime);
        map.put("end",endTime);
        List<JSONObject> receivedList = userDAO.getReceived(map);

        if(CollectionUtils.isNotEmpty(receivedList)){

            //封装数据
            BigDecimal money = BigDecimal.ZERO;
            List<String> userNameList=new ArrayList<>();

            for (JSONObject jsonObject : receivedList) {

                String lastName = jsonObject.getString("lastName");
                String firstName = jsonObject.getString("firstName");

                if(StringUtils.isBlank(lastName) || StringUtils.isBlank(firstName)){
                    continue;
                }

                BigDecimal amount = null == jsonObject.getBigDecimal("amount") ? BigDecimal.ZERO : jsonObject.getBigDecimal("amount");
                userNameList.add(firstName + " " + lastName);
                money=amount.add(money);
            }

            if(CollectionUtils.isNotEmpty(userNameList)){

                int size = userNameList.size();

                invitedState = 1;

                String allUserName = "";
                String showName = "";
                for (int i = 0; i < size; i++ ) {
                    if(i == size - 1){
                        allUserName =  allUserName + userNameList.get(i);
                    }else{
                        allUserName =  allUserName  + userNameList.get(i) + ",";
                    }
                }

                if(size > 3){
                    showName = userNameList.get(0) + ", " +  userNameList.get(1) + ", " + userNameList.get(2) + "...";
                }else{
                    showName = allUserName;
                }

                invitedDetailResult.put("info",showName);
                invitedDetailResult.put("money",money.intValue());

                MarketingLogDTO marketingLogDTO = new MarketingLogDTO();
                marketingLogDTO.setAmount(money);
                marketingLogDTO.setUserId(userId);
                marketingLogDTO.setTime(endTime);
                marketingLogDTO.setUserNameList(allUserName);
                // 保存一条记录 状态为未读
                marketingLogService.saveMarketingLog(marketingLogDTO,request);
            }
        }

        result.put("invitedState",  invitedState);
        result.put("invitedDetail",  invitedDetailResult);

        result.put("overdueState",  overdueState);
        result.put("overdueDetail",  overdueDetailResult);

        result.put("pinState",  setPinState);

        result.put("agreementState",  readAgreementState);

        int tempStripeState = 0;
        map.clear();
        map.put("state", 1);
        map.put("gatewayType", 0);
        GatewayDTO oneGateway = gatewayService.findOneGateway(map);
        if(null == oneGateway || null == oneGateway.getId()){
            throw new BizException(I18nUtils.get("gateway.disabled", getLang(request)));
        }

        if(oneGateway.getType().intValue() == StaticDataEnum.GATEWAY_TYPE_8.getCode()){
            tempStripeState = user.getStripeState();
        }
        result.put("stripeState",tempStripeState);

        return result;
    }

    /**
     * 获取用户营销账户卡券列表
     *
     * @param params
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/10/28 9:54
     */
    @Override
    public JSONObject getMarketingCouponAccount(JSONObject params, HttpServletRequest request) throws Exception {
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(accountUrl + "/server/marketingCouponList" , params.toJSONString()));
        if (!result.getString("code").equals(ErrorCodeEnum.SUCCESS_CODE.getCode())) {
            throw new BizException(result.getString("message"));
        }
        return result;
    }


    /**
     * 激活卡券
     * @author zhangzeyuan
     * @date 2021/11/9 20:33
     * @param params
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     */
    @Override
    public JSONObject activateMarketing(JSONObject params, HttpServletRequest request) throws Exception {
        JSONObject result = JSONObject.parseObject(HttpClientUtils.sendPost(accountUrl + "/server/activateMarketing" , params.toJSONString()));
        if (!result.getString("code").equals(ErrorCodeEnum.SUCCESS_CODE.getCode())) {
            throw new BizException(result.getString("message"));
        }
        return result;
    }



    /**
     * 用户详情 - 推荐用户数量
     *
     * @param params
     * @return int
     * @author zhangzeyuan
     * @date 2021/9/13 16:09
     */
    @Override
    public int inviteUserCount(Map<String, Object> params) {
        return userDAO.inviteUserCount(params).size();
    }

    /**
     * 用户详情- 推荐用户列表
     * @author zhangzeyuan
     * @date 2021/9/22 10:39
     * @param params
     * @param scs
     * @param pc
     * @return java.util.List<com.uwallet.pay.main.model.dto.UserDetailInviteUserDTO>
     */
    @Override
    public List<UserDetailInviteUserDTO> inviteUserList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        return userDAO.inviteUserList(params);
    }


    /**
     * 新用户发送邮件
     * @author zhangzeyuan
     * @date 2021/9/26 13:49
     * @param userList
     * @param sendNode
     * @param replaceTitleStatus
     * @param replaceContentStatus
     */
    public void sendNewUserEmail( List<UserDTO> userList, String sendNode, boolean replaceTitleStatus, boolean replaceContentStatus) throws Exception {
        if(CollectionUtils.isEmpty(userList)){
            return;
        }
        // 查询邮件模板
        MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(sendNode);
        if(null == mailTemplateDTO || null == mailTemplateDTO.getId()
                || StringUtils.isBlank(mailTemplateDTO.getEnSendContent()) || StringUtils.isBlank(mailTemplateDTO.getEnMailTheme())){
            log.info("获取新用户邮件模板内容异常, node:{}",sendNode);
            return;
        }

        //邮件内容 标题
        String sendMsg = mailTemplateDTO.getEnSendContent();
        String sendTitle = mailTemplateDTO.getEnMailTheme();

        //邮件记录列表
        List<MailLog> mailLogList = new ArrayList<>();

        for(UserDTO user: userList){

            if(StringUtils.isBlank(user.getEmail())){
                continue;
            }
            //替换邮件标题
            String replaceTitle = sendTitle;
            if(replaceTitleStatus){
                replaceTitle = sendTitle.replace("{replaceUserFirstName}", user.getUserFirstName());
            }

            //替换邮件内容
            String replaceSendMsg = sendMsg;
            if(replaceContentStatus){
                replaceSendMsg = sendMsg.replace("{replaceUserFullName}", user.getUserFirstName());
            }

            //发送邮件
            try{
                Session session = MailUtil.getSession(sysEmail);
                MimeMessage mimeMessage = MailUtil.getMimeMessage(StaticDataEnum.U_WALLET.getMessage(), sysEmail, user.getEmail(), replaceTitle, replaceSendMsg, null, session);
                MailUtil.sendMail(session, mimeMessage, sysEmail, sysEmailPwd);
            }catch (Exception e){
                log.error("发送邮件失败, email:{}, e:{}",user.getEmail(), e.getMessage());
                continue;
            }

            //邮件记录封装
            MailLog saveLog = new MailLog();
            saveLog.setId(SnowflakeUtil.generateId());
            saveLog.setAddress(user.getEmail());
            saveLog.setContent(replaceSendMsg);
            saveLog.setSendType(0);
            mailLogList.add(saveLog);
        }

        //保存邮件记录
        if(CollectionUtils.isNotEmpty(mailLogList)){
            mailLogService.saveMailLogList(mailLogList, null);
        }
    }


    /**
     * 新用户发送PUSH
     * @author zhangzeyuan
     * @date 2021/9/26 14:44
     */
    public void sendNewUserPush(List<UserDTO> userList, String sendNode) throws Exception {

        if(CollectionUtils.isEmpty(userList)){
            return;
        }

        // 查询邮件模板
        MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(sendNode);
        if(null == mailTemplateDTO || null == mailTemplateDTO.getId()
                || StringUtils.isBlank(mailTemplateDTO.getEnSendContent()) || StringUtils.isBlank(mailTemplateDTO.getEnSendContent())){
            log.info("获取新用户邮件模板内容异常, node:{}", sendNode);
            return;
        }

        //PUSH内容
        String sendMsg = mailTemplateDTO.getEnSendContent();

        //token列表
        List<String> tokenList = userList.stream().map(UserDTO :: getPushToken).collect(Collectors.toList());

        FirebaseDTO firebaseDTO = new FirebaseDTO();
        firebaseDTO.setTopic("a");
        firebaseDTO.setTokens(tokenList);
        firebaseDTO.setRoute(0);
        firebaseDTO.setAppName("Payo");
        firebaseDTO.setVoice(StaticDataEnum.VOICE_0.getCode());
        firebaseDTO.setBody(sendMsg);

        //发送push
        serverService.pushFirebaseList(firebaseDTO,null);
    }


    /**
     * 新用户DAY4发送短信
     * @author zhangzeyuan
     * @date 2021/9/26 14:44
     */
    public void sendNewUserDay4SMS() throws Exception {

        HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        long day4StartTime = getBeforeDaysStartEndTimestamp(3, 1);
        long day4EndTime = getBeforeDaysStartEndTimestamp(3, 2);

        paramMap.put("start", day4StartTime);
        paramMap.put("end", day4EndTime);
        List<UserDTO> day4UserList = userDAO.userListBySendMail(paramMap);

        if(CollectionUtils.isEmpty(day4UserList)){
            return;
        }
        // 查询邮件模板
        MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(String.valueOf(StaticDataEnum.SEND_NODE_42.getCode()));
        if(null == mailTemplateDTO || null == mailTemplateDTO.getId()
                || StringUtils.isBlank(mailTemplateDTO.getEnSendContent()) || StringUtils.isBlank(mailTemplateDTO.getEnSendContent())){
            log.info("获取新用户邮件模板内容异常, node:{}", StaticDataEnum.SEND_NODE_42.getCode());
            return;
        }

        //短信内容
        String sendMsg = mailTemplateDTO.getEnSendContent();

        for(UserDTO user: day4UserList){

            if(StringUtils.isBlank(user.getPhone())){
                continue;
            }
            //替换短信内容
            String replaceSendMsg = sendMsg.replace("{replaceUserFirstName}", user.getUserFirstName());

            if(!user.getPhone().startsWith("61")){
                continue;
            }

            try{
                aliyunSmsService.sendInternationalSms(user.getPhone(), replaceSendMsg);
            }catch (Exception e){
                log.error("定时任务新用户发送短信失败, phone:{}, content:{}", user.getPhone(), replaceSendMsg);
            }
        }

    }

    /**
     *
     * @author zhangzeyuan
     * @date 2021/9/24 17:28
     * @param days
     * @param type
     * @return java.lang.Long
     */
    private Long getBeforeDaysStartEndTimestamp(int days, int type){
        if(days <= 0){
            return System.currentTimeMillis();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if(type == 1){
            calendar.add(Calendar.DAY_OF_MONTH, -days);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        }else if(type == 2){
            calendar.add(Calendar.DAY_OF_MONTH, -days);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 59);
        }else {
            return System.currentTimeMillis();
        }
        return calendar.getTimeInMillis();
    }



    @Override
    public int userPromotionCount(Map<String, Object> params) {
        return marketingManagementService.userPromotionCount(params);
    }
    @Override
    public List<UserPromotionDTO> userPromotionList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        int pageIndex = pc.getPageIndex();
        int pageSize = pc.getPageSize();
        List<UserPromotionDTO> userPromotionDTOS = marketingManagementService.userPromotionList(params);
        //排序
        // 未使用
        List<UserPromotionDTO> unUseCollect = userPromotionDTOS.stream().filter(value -> value.getPromotionState() == 1).collect(Collectors.toList());
        // 已使用
        List<UserPromotionDTO> useCollect = userPromotionDTOS.stream().filter(value -> value.getPromotionState() == 2).sorted(Comparator.comparing(UserPromotionDTO::getLastMoveDate).reversed()).collect(Collectors.toList());
        // 已过期
        List<UserPromotionDTO> exprieCollect = userPromotionDTOS.stream().filter(value -> value.getPromotionState() == 3).collect(Collectors.toList());
        // 已结束
        List<UserPromotionDTO> endedCollect = userPromotionDTOS.stream().filter(value -> value.getPromotionState() == 4).collect(Collectors.toList());
        // 其余
        List<UserPromotionDTO> otherCollect = userPromotionDTOS.stream().filter(value -> value.getPromotionState() != 1&&value.getPromotionState()!=2&&value.getPromotionState()!=3&&value.getPromotionState()!=4).collect(Collectors.toList());
        userPromotionDTOS.clear();
        userPromotionDTOS.addAll(unUseCollect);
        userPromotionDTOS.addAll(useCollect);
        userPromotionDTOS.addAll(exprieCollect);
        userPromotionDTOS.addAll(endedCollect);
        userPromotionDTOS.addAll(otherCollect);
        List<UserPromotionDTO> collect = userPromotionDTOS.stream().skip(pageSize * (pageIndex - 1)).limit(pageSize).collect(Collectors.toList());
        return collect;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void AssistRegistrationUser(@NonNull JSONObject param, HttpServletRequest request) throws Exception {
        String userFirstName = param.getString("userFirstName");
        String userLastName = param.getString("userLastName");
        Integer sex = param.getInteger("sex");
        String userMiddleName = param.getString("userMiddleName");
        String referralCode = param.getString("referralCode");
        String email = param.getString("email");
        String birth = param.getString("birth");
        String phone = param.getString("phone");
        // 校验必填
        if (StringUtils.isAllBlank(userFirstName,userLastName,email,phone,birth)){
            // 参数错误
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (sex==null){
            // 参数错误
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        if (!Validator.isEmail(email)){
            throw new BizException(I18nUtils.get("email.format.error", getLang(request)));
        }
        if(!phone.startsWith("61")){
            throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
        }
        // 判断用户是否存在
        JSONObject params=new JSONObject();
        params.put("phone",phone);
        params.put("userType", StaticDataEnum.USER_TYPE_10.getCode());
        UserDTO oneUser = this.findOneUser(params);
        if (oneUser.getId()!=null){
            throw new BizException(I18nUtils.get(" user.exist", getLang(request)));
        }
        UserDTO saveUser = new UserDTO();
        saveUser.setUserFirstName(userFirstName);
        saveUser.setUserLastName(userLastName);
        saveUser.setEmail(email);
        saveUser.setPhone(phone);
        saveUser.setBirth(birth);
        saveUser.setPaymentState(StaticDataEnum.USER_BUSINESS_1.getCode());
        saveUser.setInvestState(StaticDataEnum.USER_BUSINESS_1.getCode());
        saveUser.setUserMiddleName(userMiddleName);
        saveUser.setUserType(StaticDataEnum.USER_TYPE_10.getCode());
        saveUser.setChannel(String.valueOf(StaticDataEnum.ACCOUNT_CHANNEL_0001.getCode()));
        saveUser.setSex(sex);
        saveUser.setReadAgreementState(1);
        saveUser.setRegisterFrom(StaticDataEnum.USER_REGISTER_BACK_SYSTEM.getCode());
        //用户邀请码
        String inviteCode = "";
        while (true) {
            //邀请码是否重复
            params.clear();
            inviteCode = InviteUtil.getBindNum(6);
            params.put("inviteCode", inviteCode);
            if(userDAO.count(params) == 0){
                break;
            }
        }
        saveUser.setInviteCode(inviteCode);

        JSONObject payGateWay = gatewayService.getPayGateWay(request);
        Integer type = payGateWay.getInteger("type");
        if (type==null){
            throw new BizException(I18nUtils.get("register.failed", getLang(request)));
        }
        if (type.intValue()==StaticDataEnum.GATEWAY_TYPE_0.getCode()){
            saveUser.setStripeState(StaticDataEnum.USER_STRIPE_STATE_1.getCode());
        }else if (type.intValue()==StaticDataEnum.GATEWAY_TYPE_8.getCode()){
            saveUser.setStripeState(StaticDataEnum.USER_STRIPE_STATE_0.getCode());
        }
//        //stripe新用户
//        saveUser.setStripeState(StaticDataEnum.USER_STRIPE_STATE_0.getCode());


        //用户入库
        Long userId = null;
        try {
            userId = appRegisterUserNew(saveUser, request);
        }catch (Exception e){
            throw new BizException(I18nUtils.get("register.failed", getLang(request)));
        }
        saveUser.setId(userId);

        //设置kyc错误次数
        params.clear();
        params.put("chance", KYC_CHANCE);
        params.put("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        redisUtils.hmset(userId + "_kyc", params);
        // 存储用户邀请码至缓存
        redisUtils.set(userId + "_inviteCode", saveUser.getInviteCode());
        //关联用户邀请人ID
//        associatedRegisteredUser(userId, inviteCode, request);

        //如果是用户发短信，商户发Email
        sendLoginMessage(saveUser, request);

        //注册输入 营销码/邀请码处理
        try {
            handleEnterCodeByUserRegisterV2(saveUser, referralCode, request);
        }catch (Exception e){
            log.error("协助注册 营销码处理出错，usreId:{}， e:{}", userId, e.getMessage());
        }
        // 添加操作记录
        UserInfoUpdateLogDTO userInfoUpdateLog=new UserInfoUpdateLogDTO();
        userInfoUpdateLog.setUserId(userId);
        userInfoUpdateLog.setPhone(phone);
        userInfoUpdateLog.setUpdateId(StaticDataEnum.USER_INFO_UPDATE_15.getCode()+",");
        userInfoUpdateLog.setUpdateText(I18nUtils.get("assist.region",getLang(request))+"ACCEPT");
        userInfoUpdateLogService.saveUserInfoUpdateLog(userInfoUpdateLog, request);
        // 同步braze
        BrazeUtil.trackUser(saveUser);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUserAmount(JSONObject param, HttpServletRequest request) throws Exception {
        // transType 1 提额 2 降额
        Integer transType = param.getInteger("transType");
        Integer state = param.getInteger("state");
        Long userId = param.getLong("userId");
        // 校验权限
        JSONObject paramButton=new JSONObject();
        paramButton.put("userId",getUserId(request));
        paramButton.put("type",state);
//        List<UserActionButtonDTO> userActionButtonDTOS = userActionButtonService.find(paramButton, null, null);
//        if (userActionButtonDTOS==null||userActionButtonDTOS.size()==0){
//            throw new BizException(I18nUtils.get("action.rule.sign", getLang(request)));
//        }
        if (userId==null||state==null||transType==null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        // 校验用户
        UserDTO userById = this.findUserById(userId);
        if (userById==null){
            throw new BizException(I18nUtils.get("user.doesnt.exist", getLang(request)));
        }
        JSONObject params=new JSONObject();
        params.put("userId",userId);
        LoginMissDTO loginMissDTO = loginMissService.findOneLoginMiss(params);
        if(loginMissDTO.getId()==null){
            throw new BizException("The customer account status is freeze.");
        }
        paramButton.clear();
        paramButton.put("userId",userId);
        JSONObject creditUserState = serverService.findCreditUserInfo(paramButton);
        Integer creditUserStateInteger = creditUserState.getInteger("state");
        if (creditUserStateInteger==null){
            throw new BizException("The customer is not open instalment payment.");
        }
        if(creditUserStateInteger==11||creditUserStateInteger==21){
            throw new BizException("The customer instalment status is freeze.");
        }
        if (!(creditUserStateInteger==20||creditUserStateInteger==31)){
            throw new BizException("The customer is not open instalment payment.");
        }

        // 添加记录
        BigDecimal creditAmount = creditUserState.getBigDecimal("creditAmount");
        UserInfoUpdateLogDTO userInfoUpdateLog=new UserInfoUpdateLogDTO();
        userInfoUpdateLog.setUserId(userId);
        userInfoUpdateLog.setPhone(userById.getPhone());
        paramButton.clear();
        paramButton.put("code","userUpdateId");
        if (transType==1){
            paramButton.put("value",StaticDataEnum.USER_INFO_UPDATE_12.getCode());
            StaticDataDTO oneStaticData1 = staticDataService.findOneStaticData(paramButton);
            userInfoUpdateLog.setUpdateText("Increase from "+"$"+creditAmount+" to"+" $"+state);
            userInfoUpdateLog.setUpdateId(StaticDataEnum.USER_INFO_UPDATE_12.getCode()+",");
        }else if (transType==2){
            paramButton.put("value",StaticDataEnum.USER_INFO_UPDATE_13.getCode());
            StaticDataDTO oneStaticData1 = staticDataService.findOneStaticData(paramButton);
            userInfoUpdateLog.setUpdateText("Deduction from "+"$"+creditAmount+""+" to"+" $"+state);
            userInfoUpdateLog.setUpdateId(StaticDataEnum.USER_INFO_UPDATE_13.getCode()+",");
        }
        Long aLong = userInfoUpdateLogService.saveUserInfoUpdateLog(userInfoUpdateLog, request);
        // 修改用户额度
        paramButton.clear();
        paramButton.put("code","userAmountUpdate");
        paramButton.put("value",state);
        StaticDataDTO oneStaticData = staticDataService.findOneStaticData(paramButton);
        paramButton.clear();
        paramButton.put("id",aLong);
        if (oneStaticData.getId()!=null){
            String value = oneStaticData.getValue();
            paramButton.put("userId",userId);
            paramButton.put("amount",value);
            if (transType==1){
                paramButton.put("type",1);
            }else if (transType==2){
                paramButton.put("type",2);
            }
            serverService.updateUserCreditAmount(paramButton,request);
        }
    }

    @Override
    public void updateUserInfoRemarks(@NonNull JSONObject param, HttpServletRequest request) throws BizException {
        Long id = param.getLong("id");
        String remarks = param.getString("remarks");
        if (id==null||StringUtils.isBlank(remarks)){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        UserInfoUpdateLogDTO userInfoUpdateLogById = userInfoUpdateLogService.findUserInfoUpdateLogById(id);
        if (userInfoUpdateLogById==null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        userInfoUpdateLogById.setRemarks(remarks);
        userInfoUpdateLogService.updateUserInfoUpdateLog(id,userInfoUpdateLogById,request);
    }

    @Override
    public JSONObject assistKyc(@NonNull JSONObject data, HttpServletRequest request) throws Exception {
        Long userId = data.getLong("userId");
        String birth = data.getString("birth");
        String phone = data.getString("phone");
        String email = data.getString("email");
        JSONObject resultOut=new JSONObject();
        UserInfoUpdateLogDTO userInfoUpdateLogDTO=new UserInfoUpdateLogDTO();
        userInfoUpdateLogDTO.setUserId(userId);
        userInfoUpdateLogDTO.setPhone(phone);
        userInfoUpdateLogDTO.setUpdateId(StaticDataEnum.USER_INFO_UPDATE_16.getCode()+",");
        userInfoUpdateLogDTO.setData(JSONObject.toJSONString(data));

        // 判断是否修改手机号
        UserDTO userDTO = findUserById(userId);
        if (userDTO==null){
            throw new BizException(I18nUtils.get("user.doesnt.exist", getLang(request)));
        }
        // 判断是否通过KYC
        Integer installmentState = userDTO.getInstallmentState();
        if (installmentState!=null){
            if (installmentState.intValue()==1||installmentState.intValue()==2||installmentState.intValue()==5){
                // kyc 已通过
                throw new BizException(I18nUtils.get("kyc.passed", getLang(request)));
            }else if (installmentState.intValue()==3){
                // kyc 人工审核中
                throw new BizException(I18nUtils.get("application.is.review", getLang(request)));
            }
        }
        boolean isModifyPhone=false;
        String phone1 = userDTO.getPhone();
        String userFirstName =this.toCaseName(data.getString("userFirstName")) ;
        String userLastName =this.toCaseName(data.getString("userLastName")) ;
        String phoneOld=phone1;
        if (!phone.equals(phone1)){
            isModifyPhone=true;
            userDTO.setPhone(phone);
            userDTO.setEmail(email);
            userDTO.setUserLastName(userLastName);
            userDTO.setUserLastName(userLastName);
        }

        // 判断重复身份信息
        if (userDTO.getInstallmentState().equals(StaticDataEnum.USER_BUSINESS_1.getCode())) {
            throw new BizException(I18nUtils.get("kyc.has.passed", getLang(request)));
        }
        if(StringUtils.isNotEmpty(data.getString("isAddAddress"))){
            HashMap<String, Object> objectObjectHashMap = Maps.newHashMapWithExpectedSize(2);
            objectObjectHashMap.put("code", "kysSelectUser");
            //todo 重复姓名生日挡板
            StaticDataDTO staticDataById11 = staticDataService.findOneStaticData(objectObjectHashMap);
            if(null != staticDataById11 && null != staticDataById11.getId() && StringUtils.isNotBlank(staticDataById11.getValue())){

                if(staticDataById11.getValue().equals("1")){
                    // 202108-02需求用户姓名生日重复为直接返回
                    JSONObject userData=new JSONObject();
                    userData.put("userFirstName",userFirstName);
                    userData.put("userLastName",userLastName);
                    userData.put("birth",birth);
                    List<UserDTO> userDTOS=serverService.findUserInfoByParam(userData);
                    if(userDTOS!=null&&userDTOS.size()>0) {
                        for (UserDTO dto : userDTOS) {
                            Long id = dto.getId();
                            if (id != null) {
                                if (!userId.equals(id)) {
                                    // 做过kyc驾驶证或者护照医保卡数据不为空
                                    if (dto.getMedicare() != null || dto.getDriverLicence() != null || dto.getPassport() != null) {
                                        // 身份信息重复
                                        userInfoUpdateLogDTO.setUpdateText(I18nUtils.get("duplicate.identity.information", getLang(request)));
                                        userInfoUpdateLogDTO.setData(null);
                                        userInfoUpdateLogService.saveUserInfoUpdateLog(userInfoUpdateLogDTO,request);
                                        throw new BizException(I18nUtils.get("duplicate.identity.information", getLang(request)));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // 查询最近一次提交记录
        JSONObject findParam=new JSONObject();
        findParam.put("userId",userId);
        int chance=0;
        KycSubmitLogDTO oneKycSubmitLog = kycSubmitLogService.findLatelyLog(findParam);
        if (oneKycSubmitLog!=null){
            chance=oneKycSubmitLog.getAccountSubmittedTimes();
        }
        //信息补充
        data.put("phone", userDTO.getPhone());
        // 上送KYC订单号
        Long kycNo = SnowflakeUtil.generateId();
        // 下一层需要kycno
        data.put("kycNo",kycNo);
        // 创建日志
        KycSubmitLogDTO kycSubmitLogDTO = new KycSubmitLogDTO();
        kycSubmitLogDTO.setId(kycNo);
        kycSubmitLogDTO.setKycData(JSONObject.toJSONString(data));
        kycSubmitLogDTO.setAccountSubmittedTimes(chance+1);
        // 设置默认未请求三方，
        kycSubmitLogDTO.setIsRequest(StaticDataEnum.KYC_SUBMIT_STATUS0.getCode());
        kycSubmitLogDTO.setPhone(userDTO.getPhone());
        //请求数据拼装
        JSONObject requestInfo = getRiskRequestInfo(data, userDTO);
        userDTO.setEmail(data.getString("email"));
        userDTO.setUserFirstName(data.getString("userFirstName"));
        userDTO.setUserLastName(data.getString("userLastName"));
        // 设置日志连接时间
        kycSubmitLogDTO.setDate(System.currentTimeMillis());
        // kyc风控校验
        try{
            log.info("初始化kyc记录对象,kycSubmitLogDTO:{}",kycSubmitLogDTO);
            kycSubmitLogService.insertKycSubmitLog(kycSubmitLogDTO,request);
        }catch (Exception e){
            log.error("记录kyc初始化对象异常:{}",e);
        }
        JSONObject riskResult =null;
        try{
            riskResult = kycRisk(requestInfo, request);
        }catch (Exception e){
            log.error("请求risk系统做kyc异常,e:{}",e);
            // 设置日志参数
            kycSubmitLogDTO.setIsRequest(StaticDataEnum.KYC_SUBMIT_STATUS0.getCode());
            kycSubmitLogDTO.setIsWatchlist(StaticDataEnum.WATCHLIST_STATUS_2.getCode());
            kycSubmitLogDTO.setKycStatus(StaticDataEnum.KYC_CHECK_STATE_4.getCode());
            kycSubmitLogDTO.setStatus(-1);
            this.updateKycSubmitLog(kycSubmitLogDTO,request);
            userStepService.userStepModifyAndUserStepLogSave(userDTO.getId(), StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_14.getCode(), null, null, data.toJSONString(), request);
            // 添加后台操作记录
            userInfoUpdateLogDTO.setUpdateText(I18nUtils.get("assist.kyc",getLang(request))+"Failed");
            userInfoUpdateLogService.saveUserInfoUpdateLog(userInfoUpdateLogDTO,request);
            throw e;
        }
        //添加一个记录
        log.info("risk result : {}", riskResult);
        //todo 撤除挡板
        HashMap<String, Object> kycFailed = Maps.newHashMapWithExpectedSize(1);
        kycFailed.put("code", "kyc(Failed)");
        StaticDataDTO blockerNew = staticDataService.findOneStaticData(kycFailed);
        if(null != blockerNew && StringUtils.isNotBlank(blockerNew.getName())){
            riskResult.put("code",blockerNew.getName());
        }
        if (ErrorCodeEnum.DATA_SYSTEM_FAILED.getCode().equals(riskResult.getString("code"))
                || ErrorCodeEnum.FAIL_CODE.getCode().equals(riskResult.getString("code"))) {
            // 设置日志参数
            kycSubmitLogDTO.setIsRequest(StaticDataEnum.KYC_SUBMIT_STATUS0.getCode());
            kycSubmitLogDTO.setIsWatchlist(StaticDataEnum.WATCHLIST_STATUS_2.getCode());
            kycSubmitLogDTO.setKycStatus(StaticDataEnum.KYC_CHECK_STATE_4.getCode());
            kycSubmitLogDTO.setStatus(-1);
            this.updateKycSubmitLog(kycSubmitLogDTO,request);
            userStepService.userStepModifyAndUserStepLogSave(userDTO.getId(), StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_14.getCode(), null, null, data.toJSONString(), request);
            // 用户后台操作记录
            userInfoUpdateLogDTO.setUpdateText(I18nUtils.get("assist.kyc",getLang(request))+"Failed");
            userInfoUpdateLogService.saveUserInfoUpdateLog(userInfoUpdateLogDTO,request);
            //系统异常
            resultOut.put("message",I18nUtils.get("assist.kyc", getLang(request))+"Failed");
            return resultOut;
        }
        JSONObject riskResultData = JSONObject.parseObject(EncryptUtil.decrypt(riskResult.getString("data"), EncryptUtil.aesKey, EncryptUtil.aesIv));
        log.info("risk result data : {}", riskResultData);
        // 风控审核批次
        String riskBatch = riskResultData.getString("batchNo");
        String result;
        HashMap<String, Object> objectObjectHashMap1 = Maps.newHashMapWithExpectedSize(1);
        objectObjectHashMap1.put("code", "kyc(REJECT/ACCEPT)");
        StaticDataDTO blocker = staticDataService.findOneStaticData(objectObjectHashMap1);
        if(null != blocker && StringUtils.isNotBlank(blocker.getName())){
            result = blocker.getName();
        }else {
            result = riskResultData.getJSONObject("rule").getString("decision");
        }
        if (StaticDataEnum.RISK_CHECK_STATE_0.getMessage().equals(result)) {
            userDTO.setInstallmentState(StaticDataEnum.USER_BUSINESS_5.getCode());
            //同步用户信息 账户系统
            serverService.infoSupplement(data, request);
            log.info("kyc update user info, user:{}", userDTO);
            // 更新用户状态
            updateUser(userDTO.getId(), userDTO, request);
            // 记录阶段日志
            userStepService.userStepModifyAndUserStepLogSave(userDTO.getId(), StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_11.getCode(), null, riskBatch, data.toJSONString(), request);
            //kyc成功 发送消息
            kycSuccessSendMessage(userDTO, request);
            // 添加用户分期付进行状态 2021-6-15 需求 KYC通过设置状态未录入信息
            JSONObject stateParam=new JSONObject();
            stateParam.put("userId",getUserId(request));
            stateParam.put("state",StaticDataEnum.ILLION_SUBMIT_LOG_STATUS_0.getCode());
            illionSubmitStatusLogService.addSubmitStatusLog(stateParam,request);
            userInfoUpdateLogDTO.setUpdateText(I18nUtils.get("assist.kyc",getLang(request))+"ACCEPT");
            // 删除用户登录token
            if (isModifyPhone){
                String key=userId + "_" + phoneOld + "_" + StaticDataEnum.USER_TYPE_10.getCode();
                redisUtils.del(key);
            }
            // 同步braze
            BrazeUtil.trackUser(userDTO);
        } else if (StaticDataEnum.RISK_CHECK_STATE_1.getMessage().equals(result)) {
            log.info("kyc update user info, user:{}", userDTO);
            // 记录阶段日志
            userStepService.userStepModifyAndUserStepLogSave(userDTO.getId(), StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_12.getCode(), null, riskBatch, data.toJSONString(), request);
            kycFailSendMessage(userDTO, request);
            userInfoUpdateLogDTO.setUpdateText(I18nUtils.get("assist.kyc",getLang(request))+"REJECT");
        } else if (StaticDataEnum.RISK_CHECK_STATE_2.getMessage().equals(result)) {
            // 创建理财审核记录
            RiskApproveLogDTO riskApproveLogDTO = new RiskApproveLogDTO();
            riskApproveLogDTO.setUserId(data.getLong("userId"));
            riskApproveLogDTO.setData(data.toJSONString());
            riskApproveLogService.saveRiskApproveLog(riskApproveLogDTO, request);
            userDTO.setInstallmentState(StaticDataEnum.USER_BUSINESS_3.getCode());
            // 同步用户信息
            log.info("kyc update user info, user:{}", userDTO);
            serverService.infoSupplement(data, request);
            updateUser(userDTO.getId(), userDTO, request);
            // 记录阶段日志
            userInfoUpdateLogDTO.setUpdateText(I18nUtils.get("assist.kyc",getLang(request))+"REVIEW");
            userStepService.userStepModifyAndUserStepLogSave(userDTO.getId(), StaticDataEnum.USER_STEP_1.getCode(), StaticDataEnum.USER_STEP_LOG_STATE_13.getCode(), null, riskBatch, data.toJSONString(), request);
            // 删除用户登录token
            if (isModifyPhone){
                String key=userId + "_" + phoneOld + "_" + StaticDataEnum.USER_TYPE_10.getCode();
                redisUtils.del(key);
            }
            // 同步braze
            BrazeUtil.trackUser(userDTO);
        }
        // 保存记录
        userInfoUpdateLogService.saveUserInfoUpdateLog(userInfoUpdateLogDTO,request);
        if (result.equals(StaticDataEnum.RISK_CHECK_STATE_0.getMessage())) {
            resultOut.put("message",I18nUtils.get("assist.kyc", getLang(request))+"ACCEPT");
        }else if (result.equals(StaticDataEnum.RISK_CHECK_STATE_2.getMessage())){
            resultOut.put("message",I18nUtils.get("assist.kyc", getLang(request))+"REVIEW");
        } else {
            resultOut.put("message",I18nUtils.get("assist.kyc", getLang(request))+"REJECT");
        }
        return resultOut;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void latePayment(JSONObject param, HttpServletRequest request) throws Exception {
        // 校验参数
        Long userId = param.getLong("userId");
        Long expectRepayTime = param.getLong("expectRepayTime");
        Long repayId = param.getLong("repayId");
        String delayDate = param.getString("delayDate");
        String delayDateStr = param.getString("delayDateStr");
        String expectRepayTimeStr = param.getString("expectRepayTimeStr");
        Integer type = param.getInteger("type");
        if (userId==null||StringUtils.isAllBlank(delayDate,delayDateStr)||type==null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        UserDTO userById = this.findUserById(userId);
        if (userById==null){
            throw new BizException(I18nUtils.get("user.doesnt.exist", getLang(request)));
        }
        // 添加记录
        UserInfoUpdateLogDTO userInfoUpdateLogDTO=new UserInfoUpdateLogDTO();
        userInfoUpdateLogDTO.setUserId(userId);
        userInfoUpdateLogDTO.setPhone(userById.getPhone());
        userInfoUpdateLogDTO.setUpdateId(StaticDataEnum.USER_INFO_UPDATE_14.getCode()+",");
        userInfoUpdateLogDTO.setUpdateText("Repayment date extension from "+expectRepayTimeStr+" to "+delayDateStr);
        userInfoUpdateLogDTO.setData(param.toJSONString());
        Long aLong = userInfoUpdateLogService.saveUserInfoUpdateLog(userInfoUpdateLogDTO, request);
        // 调用支付系统修改延迟还款
        JSONObject requestParam=new JSONObject();
        requestParam.put("repayId",repayId);
        requestParam.put("delayDate",delayDate);
        requestParam.put("expectRepayTime",expectRepayTime);
        requestParam.put("type",type);
        requestParam.put("userId",userId);
        requestParam.put("flowId",aLong);
        serverService.delayPayment(param,request);
        // borrowId不为空则按订单号延迟 订单号为空则按还款日延迟还款
    }

    @Override
    public void riskCheckParamsCheckBySystem(JSONObject data, HttpServletRequest request) throws BizException {
        //数据校验
        if (StringUtils.isEmpty(data.getString("userId"))) {
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }
        // 地址信息
        if (StringUtils.isAllBlank(data.getString("address"),data.getString("city"))){
            throw new BizException("Address cannot be empty!");
        }
        // 州 城市
        if (data.getInteger("postcode")==null||data.getInteger("state")==null||data.getInteger("postcode").intValue()>10000){
            throw new BizException("Postcode cannot be empty!");
        }
        if(data.getInteger("postcode") != null  ){
            if(!RegexUtils.isAuPostcode(data.getInteger("postcode").toString())){
                throw new BizException(I18nUtils.get("postcode.format.error", getLang(request)));
            }
        }
        if (data.getInteger("postcode").intValue()>10000){
            throw new BizException("Postcode to lang!");
        }
        if (data.getInteger("state")==null){
            throw new BizException("State cannot be empty!");
        }
        if (StringUtils.isEmpty(data.getString("userFirstName")) || data.getString("userFirstName").length() > Validator.TEXT_LENGTH_100) {
            throw new BizException(I18nUtils.get("name.length.error", getLang(request)));
        }

        if (StringUtils.isEmpty(data.getString("userLastName")) || data.getString("userLastName").length() > Validator.TEXT_LENGTH_100) {
            throw new BizException(I18nUtils.get("name.length.error", getLang(request)));
        }
        if (StringUtils.isNotEmpty(data.getString("userMiddleName"))) {
            if (data.getString("userMiddleName").length() > Validator.TEXT_LENGTH_100){
                throw new BizException(I18nUtils.get("name.length.error", getLang(request)));
            }
        }
        Integer idType = data.getInteger("idType");
        if (idType==null){
            throw new BizException("Please select ID Type");
        }
        if (idType.intValue()==2){
            if (StringUtils.isEmpty(data.getString("medicare"))){
                throw new BizException("Medicare cannot be empty!");
            }
            if (data.getInteger("medicareType")==null){
                throw new BizException("MedicareType cannot be empty!");
            }
            if (StringUtils.isEmpty(data.getString("medicareRefNo"))){
                throw new BizException("MedicareRefNo cannot be empty!");
            }
        }
        if (idType.intValue()==1){
            if (StringUtils.isEmpty(data.getString("driverLicence"))){
                throw new BizException("driverLicence cannot be empty!");
            }
            if (data.getInteger("driverLicenceState")==null){
                throw new BizException("DriverLicenceState cannot be empty!");
            }
        }
        if (idType.intValue()==0){
            if (StringUtils.isEmpty(data.getString("passport"))){
                throw new BizException("passport cannot be empty!");
            }
            if (data.getInteger("passportCountry")==null){
                throw new BizException("P   assportCountry cannot be empty!");
            }
            if (StringUtils.isEmpty(data.getString("passportIndate"))){
                throw new BizException("Date of expiry cannot be empty!");
            }
        }
        if (StringUtils.isNotEmpty(data.getString("medicare"))) {
            if (data.getString("medicare").length() > Validator.TEXT_LENGTH_10) {
                throw new BizException(I18nUtils.get("medical.length", getLang(request)));
            }
            if (StringUtils.isEmpty(data.getString("medicareRefNo")) || data.getString("medicareRefNo").length() > Validator.TEXT_LENGTH_3) {
                throw new BizException(" 3 Personal reference number!");
            }
        } else if (StringUtils.isNotEmpty(data.getString("driverLicence"))) {
            if (data.getString("driverLicence").length() > Validator.DRIVER_LICENSE_LENGTH) {
                throw new BizException(I18nUtils.get("driver.license.length", getLang(request)));
            }
        } else if (StringUtils.isNotEmpty(data.getString("passport"))) {
            if (data.getString("passport").length() > Validator.PASSPORT_LENGTH) {
                throw new BizException(I18nUtils.get("passport.length", getLang(request)));
            }
        } else if (StringUtils.isNotEmpty(data.getString("idNo"))) {
            if (StringUtils.isEmpty(data.getString("idNo")) || data.getString("idNo").length() != Validator.ID_NO) {
                throw new BizException(I18nUtils.get("id.length", getLang(request)));
            }
        }
        String email = data.getString("email");
        String phone = data.getString("phone");

        // 校验手机号 邮箱
        if (!Validator.isEmail(email)){
            throw new BizException(I18nUtils.get("email.format.error", getLang(request)));
        }
        if(StringUtils.isBlank(phone)||!phone.startsWith("61")){
            throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
        }
        if (phone.length()>Validator.PHONE_LENGTH_AU){
            throw new BizException(I18nUtils.get("phone.length.error", getLang(request)));
        }

        //性别不能为空
        if (null == data.getInteger("sex")) {
            throw new BizException(I18nUtils.get("sex.not.empty", getLang(request)));
        }
    }

    @Override
    public JSONObject getUserDetail( JSONObject data ,HttpServletRequest request) {
        // 判断用户是否存在权限
        JSONObject params=new JSONObject();
        Long userId1 = getUserId(request);
        params.put("userId",userId1);
        List<UserActionButtonDTO> userActionButtonDTOS = userActionButtonService.find(params, null, null);
        params.clear();
        if (userActionButtonDTOS!=null&&userActionButtonDTOS.size()!=0){
            for (UserActionButtonDTO userActionButtonDTO : userActionButtonDTOS) {
                if (userActionButtonDTO.getType()==StaticDataEnum.USER_FROZEN_TYPE_0.getCode()){
                    params.put("accountAction",1);
                }else if(userActionButtonDTO.getType()==StaticDataEnum.USER_FROZEN_TYPE_1.getCode()){
                    params.put("creditAction",1);
                }else if (userActionButtonDTO.getType()==StaticDataEnum.USER_FROZEN_TYPE_3.getCode()){
                    params.put("kycAction",1);
                }else if (userActionButtonDTO.getType()==StaticDataEnum.USER_FROZEN_TYPE_4.getCode()){
                    params.put("lateAction",1);
                }else if (userActionButtonDTO.getType()==StaticDataEnum.USER_FROZEN_TYPE_5.getCode()){
                    params.put("updateAction",1);
                }else if (userActionButtonDTO.getType()==StaticDataEnum.USER_FROZEN_TYPE_6.getCode()){
                    params.put("upAmountAction",1);
                }else if (userActionButtonDTO.getType()==StaticDataEnum.USER_FROZEN_TYPE_7.getCode()){
                    params.put("downAmountAction",1);
                }else if (userActionButtonDTO.getType()==StaticDataEnum.USER_FROZEN_TYPE_2.getCode()){
                    params.put("registerAction",1);
                }
            }
        }
        return params;
    }

    @Override
    public int findKycLogListCount(Map<String, Object> params) {
        return userStepLogService.findKycLogListCount(params);
    }

    @Override
    public List<UserStepLogDTO> findKycLogList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<UserStepLogDTO> kycLogList = userStepLogService.findKycLogList(params);
        for (UserStepLogDTO userStepLogDTO : kycLogList) {
            String kycInfo = userStepLogDTO.getKycInfo();
            if (StringUtils.isNotEmpty(kycInfo)){
                userStepLogDTO.setKycInfoObject(JSONObject.parseObject(kycInfo));
            }
        }
        return kycLogList;
    }

    @Override
    public Integer getStripeState(HttpServletRequest request) {
        Long userId = getUserId(request);
        if(userId == null){
            return StaticDataEnum.STATUS_0.getCode();
        }

        UserDTO userDTO = this.findUserById(userId);
        return userDTO.getStripeState();
    }

}




