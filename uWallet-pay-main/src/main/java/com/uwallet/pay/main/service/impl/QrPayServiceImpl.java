package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.stripe.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.ResultCode;
import com.uwallet.pay.core.common.StripeAPICodeEnum;
import com.uwallet.pay.core.common.StripeAPIResponse;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.exception.LatpayFailedException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.HttpClientUtils;
import com.uwallet.pay.core.util.MathUtils;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.LatPayErrorEnum;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.AccountFlowDAO;
import com.uwallet.pay.main.exception.PosApiException;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.MailUtil;
import com.uwallet.pay.main.util.RegexUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QrPayServiceImpl extends BaseServiceImpl implements QrPayService {

    @Autowired
    @Lazy
    private UserService userService;




    @Autowired
    @Lazy
    private QrPayFlowService qrPayFlowService;

    @Autowired
    private AccountFlowService accountFlowService;

    @Autowired
    private WithholdFlowService withholdFlowService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private AccountFlowDAO accountFlowDAO;

    @Autowired
    private RechargeFlowService rechargeFlowService;

    @Autowired
    private  SecondMerchantGatewayInfoService secondMerchantGatewayInfoService;

    @Autowired
    @Lazy
    private  MerchantService merchantService;

    @Autowired
    private LatPayService latPayService;

    @Autowired
    private TieOnCardFlowService tieOnCardFlowService;

    @Autowired
    @Lazy
    private OmiPayService omiPayService;

    @Autowired
    private MailTemplateService mailTemplateService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private StaticDataService staticDataService;

    @Autowired
    private ParametersConfigService parametersConfigService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private UserActionService userActionService;

    @Value("${omipay.noticeUrl}")
    private String noticeUrl;

    @Value("${latpay.notifyUrl}")
    private String latpayNoticeUrl;

    @Value("${uWallet.sysEmail}")
    private String sysEmail;

    @Value("${uWallet.sysEmailPwd}")
    private String sysEmailPwd;


    @Value("${uWallet.credit}")
    private String creditMerchantUrl;

    @Autowired
    private  IntegraPayService integraPayService;

    @Autowired
    private RedisUtils  redisUtils;

    @Autowired
    private MarketingFlowService marketingFlowService;

    @Autowired
    private ChannelFeeConfigService channelFeeConfigService;

    @Resource
    private PosQrPayFlowService posQrPayFlowService;


    @Autowired
    private MailLogService mailLogService;

    @Resource
    private PosApiService posApiService;

    private static final String ALI_ZH = "支付宝";

    private static final String ALI_EN = "Alipay";

    private static final String WECHAT_ZH = "微信";

    private static final String WECHAT_EN = "WeChat Pay";

    @Autowired
    private PayCreditBalanceFlowService payCreditBalanceFlowService;

    @Resource
    private CreateCreditOrderFlowService createCreditOrderFlowService;


    @Resource
    private DonationInstituteService donationInstituteService;

    @Resource
    private DonationFlowService donationFlowService;

    @Resource
    private TipFlowService tipFlowService;


    @Value("${walletConsumption}")
    private String walletConsumption;

    @Autowired
    private MarketingManagementService marketingManagementService;

    @Autowired
    private StripeAPIService stripeService;

    @Resource
    @Lazy
    private CardService cardService ;


    @Value("${Stripe.3dsRedirectUrl}")
    private String stripe3dsReturnUrl;

    /**
     * 扫码支付前置校验
     *
     * @param qrPayDTO
     * @param request
     * @throws BizException
     */
    @Override
    public void qrPayReqCheck(QrPayDTO qrPayDTO, HttpServletRequest request) throws BizException {

        //交易金额不为空
        if (StringUtils.isEmpty(qrPayDTO.getTransAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.empty", getLang(request)));
        }


        //交易金额格式校验
        if (!RegexUtils.isTransAmt(qrPayDTO.getTransAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

//        if (qrPayDTO.getFeeAmt()!=null && (StringUtils.isNotEmpty(qrPayDTO.getFeeAmt().toString()))) {
//            //手续费金额格式校验
//            if (!RegexUtils.isTransAmt(qrPayDTO.getFeeAmt().toString())) {
//                throw new BizException(I18nUtils.get("feeAmount.error", getLang(request)));
//            }
//
//            //手续费收取方向为空
//            if (StringUtils.isEmpty(qrPayDTO.getFeeDirection().toString())) {
//                throw new BizException(I18nUtils.get("feeDirection.null", getLang(request)));
//            }
//        }

        //用户userId不能为空
        if (StringUtils.isEmpty(qrPayDTO.getPayUserId().toString()) || StringUtils.isEmpty(qrPayDTO.getRecUserId().toString())) {
            throw new BizException(I18nUtils.get("userId.null", getLang(request)));
        }

        //验证交易方式
        if (StringUtils.isEmpty(qrPayDTO.getPayType().toString())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }


        if (qrPayDTO.getPayType() == StaticDataEnum.PAY_TYPE_0.getCode()) {
            if (StringUtils.isEmpty(qrPayDTO.getCardId().toString())) {
                throw new BizException(I18nUtils.get("card.id.empty", getLang(request)));
            }
        }


        //如果有红包金额校验格式
        if(qrPayDTO.getRedEnvelopeAmount()!=null && qrPayDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) != 0){
            if (!RegexUtils.isTransAmt(qrPayDTO.getRedEnvelopeAmount().toString())) {
                throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
            }
        }
        //如果有整体出售金额校验格式
        if(qrPayDTO.getWholeSalesAmount()!=null && qrPayDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) != 0){
            if (!RegexUtils.isTransAmt(qrPayDTO.getWholeSalesAmount().toString())) {
                throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
            }
            //整体出售金额不能大于交易金额
            if(qrPayDTO.getWholeSalesAmount().compareTo(qrPayDTO.getTransAmount())>0){
                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
            }
        }

        //单号不为空
        if (StringUtils.isEmpty(qrPayDTO.getTransNo())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }
    }

    public static void main(String[] args) {
        BigDecimal n = new BigDecimal("07666.00");
        n = n.stripTrailingZeros();
        System.out.println(n.precision() - n.scale());
        System.out.println(n.signum() == 0 ? 1 : n.precision() - n.scale());


    }

    /**
     * 扫码支付前置校验
     *
     * @param qrPayDTO
     * @param request
     * @throws BizException
     */
    @Override
    public void qrPayReqCheckV2(QrPayDTO qrPayDTO, HttpServletRequest request) throws BizException {

        //交易金额不为空
        if (StringUtils.isEmpty(qrPayDTO.getTransAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.empty", getLang(request)));
        }

        //实付金额不为空
        if (StringUtils.isEmpty(qrPayDTO.getTrulyPayAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.empty", getLang(request)));
        }

        //交易金额格式校验
        if (!RegexUtils.isTransAmt(qrPayDTO.getTransAmount().toString()) || !RegexUtils.isTransAmt(qrPayDTO.getTrulyPayAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        if (null !=  qrPayDTO.getTipAmount() && !RegexUtils.isTransAmt(qrPayDTO.getTipAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        //用户userId不能为空
        if (StringUtils.isEmpty(qrPayDTO.getPayUserId().toString()) ) {
            throw new BizException(I18nUtils.get("userId.null", getLang(request)));
        }

        //验证交易方式
        if (StringUtils.isEmpty(qrPayDTO.getPayType().toString())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }


        if (null == qrPayDTO.getCardId() || StringUtils.isEmpty(qrPayDTO.getCardId().toString())) {
            throw new BizException(I18nUtils.get("card.id.empty", getLang(request)));
        }

        //单号不为空
        if (StringUtils.isEmpty(qrPayDTO.getTransNo())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //校验单号是否存在
        HashMap<String, Object> countMap = Maps.newHashMapWithExpectedSize(1);
        countMap.put("transNo" , qrPayDTO.getTransNo());
        int count = qrPayFlowService.count(countMap);
        if(count > 0){
            throw new BizException(I18nUtils.get("pay.order.same", getLang(request)));
        }

        if(qrPayDTO.getPayType().equals(StaticDataEnum.PAY_TYPE_4.getCode())){
            String productId = qrPayDTO.getProductId();
            if(StringUtils.isBlank(productId) || productId.equals("Null")){
                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
            }
        }

    }


    /**
     * 扫码支付前置校验
     *
     * @param qrPayDTO
     * @param request
     * @throws BizException
     */
    public void qrPayReqCheckV3(QrPayDTO qrPayDTO, HttpServletRequest request) throws BizException {

        //交易金额不为空
        if (StringUtils.isEmpty(qrPayDTO.getTransAmount().toString()) ) {
            throw new BizException(I18nUtils.get("transAmount.empty", getLang(request)));
        }

        //实付金额不为空
        if (StringUtils.isEmpty(qrPayDTO.getTrulyPayAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.empty", getLang(request)));
        }

        //交易金额格式校验
        if (!RegexUtils.isTransAmt(qrPayDTO.getTransAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        //用户userId不能为空
        if (StringUtils.isEmpty(qrPayDTO.getPayUserId().toString()) ) {
            throw new BizException(I18nUtils.get("userId.null", getLang(request)));
        }

        //验证交易方式
        if (StringUtils.isEmpty(qrPayDTO.getPayType().toString())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //单号不为空
        if (StringUtils.isEmpty(qrPayDTO.getTransNo())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //校验单号是否存在
        HashMap<String, Object> countMap = Maps.newHashMapWithExpectedSize(1);
        countMap.put("transNo" , qrPayDTO.getTransNo());
        int count = qrPayFlowService.count(countMap);
        if(count > 0){
            throw new BizException(I18nUtils.get("pay.order.same", getLang(request)));
        }

        if(qrPayDTO.getPayType().equals(StaticDataEnum.PAY_TYPE_4.getCode())){
            String productId = qrPayDTO.getProductId();
            if(StringUtils.isBlank(productId) || productId.equals("Null")){
                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
            }
        }

        // 如果用了营销券，查询券码是否可用
        if(qrPayDTO.getMarketingId() != null){
            JSONObject object;
            try{
                object = serverService.getMarketingMessage(qrPayDTO.getMarketingId(),qrPayDTO.getPayUserId());
                if(object == null){
                    throw new BizException(I18nUtils.get("marketing.not.exist", getLang(request)));
                }
            }catch (Exception e){
                throw new BizException(I18nUtils.get("marketing.not.exist", getLang(request)));
            }

            MarketingAccountDTO marketingAccountDTO = JSONObject.parseObject(object.toJSONString(),MarketingAccountDTO.class);
            if(marketingAccountDTO.getState() == StaticDataEnum.MARKETING_STATE_2.getCode()){
                throw new BizException(I18nUtils.get("marketing.not.exist", getLang(request)));
            }

            Integer[] stateList = {StaticDataEnum.TRANS_STATE_5.getCode(),StaticDataEnum.TRANS_STATE_4.getCode()};
            // 如果该卡券有流水处于回退中的，不能使用
            Map<String,Object> params = new HashMap<>();
            params.put("userId",qrPayDTO.getPayUserId());
            params.put("marketingId",qrPayDTO.getMarketingId());
            params.put("stateList",stateList);
            MarketingFlowDTO marketingFlowDTO = marketingFlowService.findOneMarketingFlow(params);
            if(marketingFlowDTO != null && marketingFlowDTO.getId() != null){
                throw new BizException(I18nUtils.get("marketing.not.exist", getLang(request)));
            }

            MarketingManagementDTO marketingManagementDTO = marketingManagementService.findMarketingManagementById(Long.parseLong(marketingAccountDTO.getMarkingId()));

            if(marketingManagementDTO == null || marketingManagementDTO.getId() == null || marketingManagementDTO.getActivityState() != StaticDataEnum.MARKETING_ACTIVITY_STATE_1.getCode()){
                throw new BizException(I18nUtils.get("marketing.not.exist", getLang(request)));
            }
            // 如果有时间限制，需要判断时间
            if(marketingManagementDTO.getValidityLimitState() == StaticDataEnum.MARKETING_VALIDITY_LIMIT_STATE_1.getCode()){
                Long now = System.currentTimeMillis();
                if(marketingManagementDTO.getValidEndTime() < now || marketingManagementDTO.getValidStartTime() > now ){
                    throw new BizException(I18nUtils.get("marketing.not.exist", getLang(request)));
                }
            }

            // 城市限制
            if(marketingManagementDTO.getCityLimitState() != null && marketingManagementDTO.getCityLimitState() > 0){
                MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayDTO.getMerchantId());
                if(merchantDTO == null || merchantDTO.getId() == null){
                    throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
                }

                if( !merchantDTO.getCity().equals(marketingManagementDTO.getCityLimitState()+"") ){
                    throw new BizException(I18nUtils.get("marketing.not.exist", getLang(request)));
                }
            }

            // 商户限制
            if(marketingManagementDTO.getRestaurantLimitState() != null && marketingManagementDTO.getRestaurantLimitState() > 0){
                MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayDTO.getMerchantId());
                if(merchantDTO == null || merchantDTO.getId() == null){
                    throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
                }

                if( !(merchantDTO.getId()+"").equals(marketingManagementDTO.getRestaurantLimitState()+"") ){
                    throw new BizException(I18nUtils.get("marketing.not.exist", getLang(request)));
                }
            }

            //消费金额限制

            //2022年1月11日16:31:53 新增需求 该时间戳领取之前的 没有限制最低消费金额的优惠券 最低消费金额为 面额 / 0.4
            String managementId = marketingManagementDTO.getId().toString();
            BigDecimal tempMinTranAmount = BigDecimal.ZERO;
            if(managementId.equals("620846989825331200") || managementId.equals("625123077544005632")){
                //拆分的券使用限制 优惠券金额 除 0.4
                tempMinTranAmount = marketingAccountDTO.getBalance().divide(new BigDecimal("0.4"), 2, BigDecimal.ROUND_HALF_UP);
            }else{
                Long createdDate = marketingAccountDTO.getCreatedDate();

                HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
                map.put("code", "updatePromoMinAmt");
                StaticDataDTO staticData = staticDataService.findOneStaticData(map);

                if(marketingManagementDTO.getAmountLimitState().intValue() == StaticDataEnum.MARKETING_AMOUNT_LIMIT_STATE_0.getCode()
                        && createdDate.compareTo(Long.parseLong(staticData.getValue())) <= 0) {
                    tempMinTranAmount = marketingAccountDTO.getBalance().divide(new BigDecimal("0.4"), 2, BigDecimal.ROUND_HALF_UP);
                }else{
                    tempMinTranAmount =  marketingManagementDTO.getMinTransAmount();
                }
            }

            /*String managementId = marketingManagementDTO.getId().toString();
            BigDecimal tempMinTranAmount = BigDecimal.ZERO;
            if(managementId.equals("620846989825331200") || managementId.equals("625123077544005632")){
                tempMinTranAmount = marketingAccountDTO.getBalance().divide(new BigDecimal("0.4"), 2, BigDecimal.ROUND_HALF_UP);
            }else{
                tempMinTranAmount =  marketingManagementDTO.getMinTransAmount();
            }*/
            if(qrPayDTO.getTransAmount().compareTo(tempMinTranAmount) < 0){
                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
            }
            qrPayDTO.setMarketingBalance(marketingAccountDTO.getBalance());
            qrPayDTO.setMarketingManageId(marketingManagementDTO.getId());
            qrPayDTO.setMarketingType(marketingManagementDTO.getType());
        }else{
            qrPayDTO.setMarketingBalance(BigDecimal.ZERO);
        }

    }



    /**
     * 支付交易
     *
     * @param qrPayDTO
     * @param request
     * @throws BizException
     * @return
     */
    @Override
    public Object doQrPay(QrPayDTO qrPayDTO, HttpServletRequest request) throws Exception {

        //获取交易双方user信息
        UserDTO payUser = userService.findUserById(qrPayDTO.getPayUserId());
        UserDTO recUser = userService.findUserById(qrPayDTO.getRecUserId());

        //判断用户是否存在
        if (payUser == null  ||  payUser.getId() == null ) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }

        if(recUser == null || recUser.getId() == null ){
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        Integer cardState = payUser.getCardState();
        if(qrPayDTO.getPayType() == StaticDataEnum.PAY_TYPE_4.getCode()){
            if(!cardState.equals(StaticDataEnum.USER_CARD_STATE_BINDED)){
                //todo 抛异常提示未绑卡
            }
        }

        //判断是否是POS订单
        boolean posOrder = false;
        //根据订单号查询POS订单
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
        map.put("sysTransNo", qrPayDTO.getTransNo());
        PosQrPayFlowDTO posQrPayFlow = posQrPayFlowService.findOnePosQrPayFlow(map);
        if(Objects.nonNull(posQrPayFlow) && Objects.nonNull(posQrPayFlow.getId())){
            posOrder = true;
        }

        //校验POS订单状态
        if(posOrder){
            boolean posCheckResult = checkPosOrderStatus(posQrPayFlow.getOrderStatus());
            if(!posCheckResult){
                //返回二维码失效
                throw new BizException(I18nUtils.get("pos.qrcode.disabled", getLang(request)));
            }
            //将POS订单改为处理中
            posQrPayFlowService.updateOrderStatusBySysTransNo(qrPayDTO.getTransNo(), StaticDataEnum.POS_ORDER_STATUS_INPROCESS.getCode(), getUserId(request), System.currentTimeMillis());
        }
        //根据订单号判断订单是否存在
        HashMap<String, Object> countMap = Maps.newHashMapWithExpectedSize(1);
        countMap.put("transNo" , qrPayDTO.getTransNo());
        int count = qrPayFlowService.count(countMap);
        if(count > 0){
            throw new BizException(I18nUtils.get("pay.order.same", getLang(request)));
        }
        //创建流水
        QrPayFlowDTO qrPayFlowDTO = new QrPayFlowDTO();
        qrPayFlowDTO.setFee(qrPayDTO.getFeeAmt());
        qrPayFlowDTO.setPayUserId(qrPayDTO.getPayUserId());
        qrPayFlowDTO.setPayUserType(payUser.getUserType());
        qrPayFlowDTO.setMerchantId(qrPayDTO.getMerchantId());
        qrPayFlowDTO.setRecUserId(qrPayDTO.getRecUserId());
        qrPayFlowDTO.setRecUserType(recUser.getUserType());
        qrPayFlowDTO.setTransAmount(qrPayDTO.getTransAmount());
        qrPayFlowDTO.setRemark(qrPayDTO.getRemark());
        //生成单号
        qrPayFlowDTO.setTransNo(qrPayDTO.getTransNo());
        qrPayFlowDTO.setPayUserIp(getIp(request));
        //判断用户是否支持该支付方式，并计算出售金额

        //设置是否是POS订单 下面用
        qrPayFlowDTO.setPosOrder(posOrder);

        //根据不同的通道进行配置校验  todo edit
        Map<String,Object> resultMap = doPayTypeCheck(qrPayFlowDTO, recUser, payUser, qrPayDTO, request);
        JSONObject cardObj = (JSONObject)resultMap.get("cardList");
        GatewayDTO gatewayDTO = (GatewayDTO) resultMap.get("gateWay");
        //收款用户是商户,计算各种金额
        if(recUser.getUserType()==StaticDataEnum.USER_TYPE_20.getCode()){
            getQrPayFlow(qrPayFlowDTO,qrPayDTO,gatewayDTO,null!=cardObj ? JSONObject.parseObject(cardObj.toJSONString(), CardDTO.class):null,request);
        }
        // 如果是支付通道的，需要进行限额判断 todo  edit
        if(qrPayDTO.getPayType() == StaticDataEnum.PAY_TYPE_0.getCode() && qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimit(gatewayDTO.getType(),qrPayFlowDTO.getPayAmount(),request);
        }
        // 如果支付方式是分期付，实付金额需要>4.4元
        if(qrPayDTO.getPayType() == StaticDataEnum.PAY_TYPE_4.getCode() && (qrPayFlowDTO.getPayAmount() == null || qrPayFlowDTO.getPayAmount().compareTo(new BigDecimal( StaticDataEnum.INSTALLMENT_MIN_AMT.getMessage())) < 0)){
            throw new BizException(I18nUtils.get("credit.amount.limit", getLang(request), new String[]{StaticDataEnum.INSTALLMENT_MIN_AMT.getMessage()}));
        }
        //设置POS订单来源
        if(posOrder){
            qrPayFlowDTO.setOrderSource(StaticDataEnum.ORDER_SOURCE_POS.getCode());
        }
        // 记录流水 todo  唯一校验
        qrPayFlowDTO.setId( qrPayFlowService.saveQrPayFlow(qrPayFlowDTO,request));


        // 返回参数
        Map<String,Object> resMap = new HashMap<>();

        // 判断是否有整体出售和红包金额，如果有，需要账户出账
        if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 ||qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            // 记录交易状态为出账初始状态
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_10.getCode());
            qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,request);

            boolean amountOutResult ;
            try {
                // 调用支付批量出入账接口，进行出账
                amountOutResult = doBatchAmountOut(qrPayFlowDTO,request);
//                if(TestEnvCheckerUtil.isTestEnv()){
//                    throw  new Exception("111");
//                }

            }catch (Exception e){
                log.error("账户出账 Exception："+e.getMessage(),e);
                //流程报错返回处理中
                if(gatewayDTO != null && gatewayDTO.getId() != null){
                    rechargeFlowService.channelLimitRollback(System.currentTimeMillis(),gatewayDTO.getType(),qrPayFlowDTO.getPayAmount(),request);
                }
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
                updateFlow(qrPayFlowDTO, null, null, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            //判断出账结果，如果失败，限额回滚，修改交易为失败状态
            if(!amountOutResult){
                if(gatewayDTO != null && gatewayDTO.getId() != null){
                    rechargeFlowService.channelLimitRollback(System.currentTimeMillis(),gatewayDTO.getType(),qrPayFlowDTO.getPayAmount(),request);
                }
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
                updateFlow(qrPayFlowDTO, null, null, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
        }

        // 按照各个支付防止走不同组件
        if (StaticDataEnum.PAY_TYPE_0.getCode() == (qrPayDTO.getPayType())) {
            //计算金额
//            qrPayFlowDTO = calculation(qrPayDTO, qrPayFlowDTO, request);
            //交易状态为三方初始状态
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
            //记录交易状态
            qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,request);
            //卡支付方法
            qrPayByCard(request, payUser, recUser, qrPayFlowDTO, gatewayDTO, JSONObject.parseObject(cardObj.toJSONString(), CardDTO.class) ,cardObj);

            // 交易成功推荐人处理
            dealFirstDeal(qrPayFlowDTO,payUser,request);

            //交易成功回调通知
            try {
                log.info("开始进行订单支付成功通知");
                posApiService.posPaySuccessNotice(qrPayFlowDTO.getMerchantId(), qrPayFlowDTO.getTransNo(), request);
            }catch (PosApiException e){
                log.error("POS通知失败" + e.getMessage());
            }


        }
//        else if (StaticDataEnum.PAY_TYPE_1.getCode() == (qrPayDTO.getPayType())) {
        //查询商户路由配置
//            GatewayDTO gatewayDTO = doRouteConfig(recUser,qrPayFlowDTO,qrPayDTO,request);
        //计算金额
//            qrPayFlowDTO = calculation(qrPayDTO, qrPayFlowDTO, request);
//            //账户余额支付
//            if (recUser.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
//                //余额转账
//                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_3.getCode());
//            } else {
//                //余额消费
//                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_1.getCode());
//                qrPayFlowDTO.setIsNeedClear(StaticDataEnum.NEED_CLEAR_TYPE_1.getCode());
//            }
//
//            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_30.getCode());
//            qrPayFlowDTO.setId(qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request));
//            qrPayByBalance(qrPayDTO, request, payUser, recUser, qrPayFlowDTO);
//            sendMessage(qrPayFlowDTO.getRecUserId(), qrPayFlowDTO.getId(), request);
//        }
//        else if (StaticDataEnum.PAY_TYPE_2.getCode() == (qrPayDTO.getPayType())) {
//            //查询商户路由配置
////            GatewayDTO gatewayDTO = doRouteConfig(recUser,qrPayFlowDTO,qrPayDTO,request);
//            //支付宝支付
//            if (recUser.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
//                //支付宝不可转账
//               // qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_10.getCode());
//                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
//            } else {
//                //支付宝消费
//                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_8.getCode());
//                MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayDTO.getMerchantId());
//                if(merchantDTO==null || merchantDTO.getId()==null ||  merchantDTO.getIsAvailable() !=1) {
//                    throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
//                }
//            }
//            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
//            qrPayFlowDTO.setIsNeedClear(StaticDataEnum.NEED_CLEAR_TYPE_1.getCode());
//            qrPayFlowDTO.setId(qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request));
//            WithholdFlowDTO res = qrPayByAliPay(qrPayDTO, request, payUser, recUser, qrPayFlowDTO);
//            resMap.put("noticeUrl",noticeUrl);
//            resMap.put("mNumber", res.getMNumber());
//            resMap.put("secretKey", res.getSecretKey());
//        }
//        else if (StaticDataEnum.PAY_TYPE_3.getCode() == (qrPayDTO.getPayType())) {
//            //查询商户路由配置
////            GatewayDTO gatewayDTO = doRouteConfig(recUser,qrPayFlowDTO,qrPayDTO,request);
//            //计算金额
//            qrPayFlowDTO = calculation(qrPayDTO, qrPayFlowDTO, request);
//            //微信支付
//            if (recUser.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
//                //微信不可转账
//                //qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_11.getCode());
//                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
//
//            } else {
//                //微信消费
//                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_9.getCode());
//                MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayDTO.getMerchantId());
//                if(merchantDTO==null || merchantDTO.getId()==null ||  merchantDTO.getIsAvailable() !=1) {
//                    throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
//                }
//            }
//            qrPayFlowDTO.setIsNeedClear(StaticDataEnum.NEED_CLEAR_TYPE_1.getCode());
//            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
//            qrPayFlowDTO.setId(qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request));
//            WithholdFlowDTO res = qrPayByWechatPay(qrPayDTO, request, payUser, recUser, qrPayFlowDTO);
//            resMap.put("noticeUrl",noticeUrl);
//            resMap.put("mNumber", res.getMNumber());
//            resMap.put("secretKey", res.getSecretKey());
//        }
        else if (StaticDataEnum.PAY_TYPE_4.getCode() == (qrPayDTO.getPayType())){
            //分期付
//            qrPayFlowDTO = checkCreditParams(qrPayFlowDTO,qrPayDTO,recUser,payUser,request);
            // 交易状态为三方初始状态
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
            // 记录交易状态
            qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,request);
            // 分期付交易 todo edit
            qrPayFlowDTO = doCredit(qrPayFlowDTO,request);
            // 交易成功推荐人处理
            dealFirstDeal(qrPayFlowDTO,payUser,request);

            //交易成功回调通知
            try {
                posApiService.posPaySuccessNotice(qrPayFlowDTO.getMerchantId(), qrPayFlowDTO.getTransNo(), request);
            }catch (PosApiException e){
                log.error("主动通知订单信息失败" + e.getMessage());
            }

            //分期付发送交易记录 邮件
            try{
                sendTransactionMail(qrPayFlowDTO.getCreditOrderNo(), request);
            }catch (Exception e){
                log.error("分期付发送交易记录邮件失败, CreditOrderNo:" + qrPayFlowDTO.getCreditOrderNo() + " || message:" + e.getMessage());
            }
        }
        else {
            //非法的交易方式
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }
        resMap.put("flowId",qrPayFlowDTO.getTransNo());
        resMap.put("id",qrPayFlowDTO.getId());
        resMap.put("orderCreateDate", new SimpleDateFormat("HH:mm dd-MM-yyyy").format(new Date(System.currentTimeMillis())));
        resMap.put("flowId",qrPayFlowDTO.getTransNo());
        resMap.put("resultState",StaticDataEnum.TRANS_STATE_1.getCode());



        return resMap;

    }


    /**
     * 支付交易
     *
     * @param qrPayDTO
     * @param request
     * @throws BizException
     * @return
     */
    @Override
    public Object doQrPayV2(QrPayDTO qrPayDTO, HttpServletRequest request) throws Exception {

        HashMap<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);

        //前置必要参数校验
        qrPayReqCheckV2(qrPayDTO, request);

        //获取交易双方user信息
        UserDTO payUser = userService.findUserInfoV2(qrPayDTO.getPayUserId());
        UserDTO recUser = userService.findUserInfoV2(qrPayDTO.getRecUserId());

        //判断用户是否存在
        if (payUser == null  ||  payUser.getId() == null ) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }
        if(recUser == null || recUser.getId() == null ){
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        //判断是否是POS订单
        /*boolean posOrder = false;
        //根据订单号查询POS订单
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
        map.put("sysTransNo", qrPayDTO.getTransNo());
        PosQrPayFlowDTO posQrPayFlow = posQrPayFlowService.findOnePosQrPayFlow(map);
        if(Objects.nonNull(posQrPayFlow) && Objects.nonNull(posQrPayFlow.getId())){
            posOrder = true;
        }*/

        //校验POS订单状态
/*        if(posOrder){
            boolean posCheckResult = checkPosOrderStatus(posQrPayFlow.getOrderStatus());
            if(!posCheckResult){
                //返回二维码失效
                throw new BizException(I18nUtils.get("pos.qrcode.disabled", getLang(request)));
            }
            //将POS订单改为处理中
            posQrPayFlowService.updateOrderStatusBySysTransNo(qrPayDTO.getTransNo(), StaticDataEnum.POS_ORDER_STATUS_INPROCESS.getCode(), getUserId(request), System.currentTimeMillis());
        }*/

        //封装订单流水信息
        QrPayFlowDTO qrPayFlowDTO = new QrPayFlowDTO();
        qrPayFlowDTO.setPayUserId(qrPayDTO.getPayUserId());
        qrPayFlowDTO.setPayUserType(payUser.getUserType());
        qrPayFlowDTO.setMerchantId(qrPayDTO.getMerchantId());
        qrPayFlowDTO.setRecUserId(qrPayDTO.getRecUserId());
        qrPayFlowDTO.setRecUserType(recUser.getUserType());
        qrPayFlowDTO.setTransAmount(qrPayDTO.getTransAmount());
        qrPayFlowDTO.setRemark(qrPayDTO.getRemark());
        qrPayFlowDTO.setTransNo(qrPayDTO.getTransNo());
        qrPayFlowDTO.setPayUserIp(getIp(request));
        qrPayFlowDTO.setCreatedDate(System.currentTimeMillis());
//        qrPayFlowDTO.setPosOrder(posOrder);
        qrPayFlowDTO.setOrderSource(StaticDataEnum.ORDER_SOURCE_0.getCode());

        //返回结果
        Map<String, Object> resultData = Maps.newHashMapWithExpectedSize(8);

        if(qrPayDTO.getPayType().equals(StaticDataEnum.PAY_TYPE_0.getCode())){
            //卡支付
            resultData = cardPay(qrPayFlowDTO, qrPayDTO, payUser, request);

/*            // 交易成功推荐人处理
            //首次交易成功推荐人处理
            userService.firstPaidSuccessAmountIn(payUser.getId(), payUser.getInviterId(), 19, qrPayFlowDTO.getId(), request);

            //POS API 交易成功回调通知
            try {
                log.info("开始进行订单支付成功通知");
                posApiService.posPaySuccessNotice(qrPayFlowDTO.getMerchantId(), qrPayFlowDTO.getTransNo(), request);
            }catch (PosApiException e){
                log.error("POS通知失败" + e.getMessage());
            }*/

        }else if(qrPayDTO.getPayType().equals(StaticDataEnum.PAY_TYPE_4.getCode())){
            //分期付
            resultData = creditPay(qrPayFlowDTO, qrPayDTO, payUser, recUser, request);

            //首次交易成功推荐人处理
//            userService.firstPaidSuccessAmountIn(payUser.getId(), payUser.getInviterId(), 19, qrPayFlowDTO.getId(), request);
        }

        try {
            // 交易成功推荐人处理
            userService.firstPaidSuccessAmountIn(payUser.getId(), payUser.getInviterId(), StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode(), qrPayFlowDTO.getId(), request);
        }catch (Exception e){
            log.error("交易成功推荐人处理出错,e:{}" , e);
        }

        resultMap.put("flowId",qrPayFlowDTO.getTransNo());
        resultMap.put("id", qrPayFlowDTO.getId().toString());
        resultMap.put("resultState", StaticDataEnum.TRANS_STATE_1.getCode());
        resultMap.put("data", resultData);
        return resultMap;
    }


    @Override
    public Object doQrPayV3(QrPayDTO qrPayDTO, HttpServletRequest request) throws Exception {

        HashMap<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);

        //前置必要参数校验
        qrPayReqCheckV3(qrPayDTO, request);

        //获取交易双方user信息
        UserDTO payUser = userService.findUserInfoV2(qrPayDTO.getPayUserId());
        UserDTO recUser = userService.findUserInfoV2(qrPayDTO.getRecUserId());

        //判断用户是否存在
        if (payUser == null  ||  payUser.getId() == null ) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }
        if(recUser == null || recUser.getId() == null ){
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        //封装订单流水信息
        QrPayFlowDTO qrPayFlowDTO = new QrPayFlowDTO();
        qrPayFlowDTO.setPayUserId(qrPayDTO.getPayUserId());
        qrPayFlowDTO.setPayUserType(payUser.getUserType());
        qrPayFlowDTO.setMerchantId(qrPayDTO.getMerchantId());
        qrPayFlowDTO.setRecUserId(qrPayDTO.getRecUserId());
        qrPayFlowDTO.setRecUserType(recUser.getUserType());
        qrPayFlowDTO.setTransAmount(qrPayDTO.getTransAmount());
        qrPayFlowDTO.setRemark(qrPayDTO.getRemark());
        qrPayFlowDTO.setTransNo(qrPayDTO.getTransNo());
        qrPayFlowDTO.setPayUserIp(getIp(request));
        qrPayFlowDTO.setCreatedDate(System.currentTimeMillis());
        qrPayFlowDTO.setOrderSource(StaticDataEnum.ORDER_SOURCE_0.getCode());
        qrPayFlowDTO.setMarketingId(qrPayDTO.getMarketingId());
        //红包金额校验时间已有
        qrPayFlowDTO.setMarketingBalance(qrPayDTO.getMarketingBalance());
        qrPayFlowDTO.setMarketingManageId(qrPayDTO.getMarketingManageId());

        qrPayFlowDTO.setMarketingType(qrPayDTO.getMarketingType());

        //返回结果
        Map<String, Object> resultData = Maps.newHashMapWithExpectedSize(8);

        if(qrPayDTO.getPayType().equals(StaticDataEnum.PAY_TYPE_0.getCode())){
            //卡支付
            resultData = cardPayV3(qrPayFlowDTO, qrPayDTO, payUser, request);
        }else if(qrPayDTO.getPayType().equals(StaticDataEnum.PAY_TYPE_4.getCode())){
            //分期付
            resultData = creditPayV3(qrPayFlowDTO, qrPayDTO, payUser, recUser, request);
        }else{
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        try {
            // 交易成功推荐人处理
            userService.firstPaidSuccessAmountInV2(payUser.getId(), payUser.getInviterId(), StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode(), qrPayFlowDTO.getId(), request);
        }catch (Exception e){
            log.error("交易成功推荐人处理出错,e:{}" , e);
        }

        resultMap.put("flowId",qrPayFlowDTO.getTransNo());
        resultMap.put("id", qrPayFlowDTO.getId()+"");
        resultMap.put("resultState", StaticDataEnum.TRANS_STATE_1.getCode());
        resultMap.put("data", resultData);
        resultMap.put("firstName", payUser.getUserFirstName());
        resultMap.put("lastName", payUser.getUserLastName());


        return resultMap;

    }


    @Override
    public Object doQrPayV4(QrPayDTO qrPayDTO, HttpServletRequest request) throws Exception {

        HashMap<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);

        //前置必要参数校验
        qrPayReqCheckV3(qrPayDTO, request);

        //获取交易双方user信息
        UserDTO payUser = userService.findUserById(qrPayDTO.getPayUserId());
        UserDTO recUser = userService.findUserInfoV2(qrPayDTO.getRecUserId());

        //判断用户是否存在
        if(recUser == null || recUser.getId() == null ){
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        //封装订单流水信息
        QrPayFlowDTO qrPayFlowDTO = new QrPayFlowDTO();
        qrPayFlowDTO.setPayUserId(qrPayDTO.getPayUserId());
        qrPayFlowDTO.setPayUserType(payUser.getUserType());
        qrPayFlowDTO.setMerchantId(qrPayDTO.getMerchantId());
        qrPayFlowDTO.setRecUserId(qrPayDTO.getRecUserId());
        qrPayFlowDTO.setRecUserType(recUser.getUserType());
        qrPayFlowDTO.setTransAmount(qrPayDTO.getTransAmount());
        qrPayFlowDTO.setRemark(qrPayDTO.getRemark());
        qrPayFlowDTO.setTransNo(qrPayDTO.getTransNo());
        qrPayFlowDTO.setPayUserIp(getIp(request));
        qrPayFlowDTO.setCreatedDate(System.currentTimeMillis());
        qrPayFlowDTO.setOrderSource(StaticDataEnum.ORDER_SOURCE_0.getCode());
        qrPayFlowDTO.setMarketingId(qrPayDTO.getMarketingId());
        //红包金额校验时间已有
        qrPayFlowDTO.setMarketingBalance(qrPayDTO.getMarketingBalance());
        qrPayFlowDTO.setMarketingManageId(qrPayDTO.getMarketingManageId());

        qrPayFlowDTO.setMarketingType(qrPayDTO.getMarketingType());

        //返回结果
        Map<String, Object> resultData = new HashMap<>(16);

        if(qrPayDTO.getPayType().equals(StaticDataEnum.PAY_TYPE_0.getCode())){
            //卡支付
            resultData = cardPayV4(qrPayFlowDTO, qrPayDTO, payUser, request);
        }else if(qrPayDTO.getPayType().equals(StaticDataEnum.PAY_TYPE_4.getCode())){
            //分期付
            resultData = creditPayV4(qrPayFlowDTO, qrPayDTO, payUser, recUser, request);
        }else{
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        // 返回了三方url
        if(resultData.get("url") != null ){
            resultMap.put("resultState", 9999);
        }else{
            try {
                // 交易成功推荐人处理
                userService.firstPaidSuccessAmountInV2(payUser.getId(), payUser.getInviterId(), StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode(), qrPayFlowDTO.getId(), request);
            }catch (Exception e){
                log.error("交易成功推荐人处理出错,e:{}" , e);
            }
            resultMap.put("resultState", StaticDataEnum.TRANS_STATE_1.getCode());
        }



        resultMap.put("flowId",qrPayFlowDTO.getTransNo());
        resultMap.put("id", qrPayFlowDTO.getId()+"");
        resultMap.put("data", resultData);
        resultMap.put("firstName", payUser.getUserFirstName());
        resultMap.put("lastName", payUser.getUserLastName());


        return resultMap;

    }




    /**
     * 卡支付
     * @author zhoutt
     * @date 2021/10/29 10:55
     * @param qrPayFlowDTO
     * @param qrPayDTO
     * @param payUser
     * @param request
     */
    private Map<String, Object> cardPayV3(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, UserDTO payUser, HttpServletRequest request) throws Exception {

        //校验是否绑卡
        Integer cardState = payUser.getCardState();
        if(!cardState.equals(StaticDataEnum.USER_CARD_STATE_BINDED.getCode())){
            throw new BizException(I18nUtils.get("pay.card.not.bound", getLang(request)));
        }

        //交易类型
        //卡消费
        qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode());

        //通道校验
        Map<String, Object> channelResultMap = verifyAndGetPayGatewayCardInfo(qrPayDTO.getPayType(), qrPayDTO.getCardId(), qrPayFlowDTO, request);

        //通道和卡信息
        GatewayDTO gatewayDTO = (GatewayDTO) channelResultMap.get("gatewayDTO");
        CardDTO cardDTO = (CardDTO) channelResultMap.get("cardDTO");
        JSONObject cardJsonObj = (JSONObject) channelResultMap.get("cardJsonObj");

        //收款方是商户 进行交易金额校验
        Map<String, Object> amountMap = verifyAndGetPayAmountV3(qrPayFlowDTO, qrPayDTO, cardDTO, request);

        //进行限额判断
        if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimit(gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(), request);
        }

        try{
            Long id = qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request);
            qrPayFlowDTO.setId(id);
        }catch (Exception e){
            log.error("支付保存订单流水出错",  qrPayDTO.toString());
            //限额回滚
            if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
            }
            throw new BizException(I18nUtils.get("ay.order.same", getLang(request)));
        }

        //账户出账
        if((qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 && qrPayFlowDTO.getMarketingId() == null ) ||
                qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0 ){
            doAccountOut(qrPayFlowDTO, gatewayDTO, request);
        }

        // 有使用卡券
        if(qrPayFlowDTO.getMarketingId() != null){
            boolean useMarketingResult = false;
            try{
                useMarketingResult = this.useOneMarketing(qrPayFlowDTO,request);
            }catch (Exception e){
                log.error("用户卡券使用失败");
                //限额回滚
                if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                    rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
                }
                // 出账回滚
                doBatchAmountOutRollBack(qrPayFlowDTO,request);
                //更改交易状态为交易失败
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                updateFlow(qrPayFlowDTO, null, null, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            if(!useMarketingResult){
                //限额回滚
                if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                    rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
                }
                //出账回滚
                doBatchAmountOutRollBack(qrPayFlowDTO,request);
                //更改交易状态为通道交易失败
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                updateFlow(qrPayFlowDTO, null, null, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
        }


        //捐赠金额
        BigDecimal donationAmount = new BigDecimal((String) amountMap.get("donationAmount"));
        if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
            // 封装捐赠流水
            DonationFlowDTO donationFlowDTO = new DonationFlowDTO();
            donationFlowDTO.setFlowId(qrPayFlowDTO.getId());
            donationFlowDTO.setUserId(payUser.getId());
            donationFlowDTO.setUserName(payUser.getUserFirstName() + " " + payUser.getUserLastName());
            donationFlowDTO.setInstituteId(Long.valueOf(qrPayDTO.getDonationInstiuteId()));
            donationFlowDTO.setAmount(donationAmount);
            donationFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
            donationFlowDTO.setSettlementAmount(donationAmount);
            donationFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_34.getCode());
            donationFlowDTO.setMerchantId(qrPayFlowDTO.getMerchantId());
            donationFlowService.saveDonationFlow(donationFlowDTO, request);
        }

        //生成小费流水记录
        BigDecimal tipAmount = qrPayDTO.getTipAmount();
        if(null != tipAmount && tipAmount.compareTo(BigDecimal.ZERO) > 0){
            // 封装小费流水
            TipFlowDTO tipFlowDTO = new TipFlowDTO();
            tipFlowDTO.setFlowId(qrPayFlowDTO.getId());
            tipFlowDTO.setMerchantId(qrPayFlowDTO.getMerchantId());
            tipFlowDTO.setSettlementAmount(tipAmount);
            tipFlowDTO.setTipAmount(tipAmount);
            tipFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_35.getCode());
            tipFlowDTO.setUserId(payUser.getId());
            tipFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
            tipFlowService.saveTipFlow(tipFlowDTO, request);
        }

        //交易状态为三方初始状态
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
        qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,request);

        //调用卡支付
        //保存三方代扣交易流水
        WithholdFlowDTO withholdFlowDTO = null;
        try {
            withholdFlowDTO = packageWithHoldFlow(qrPayFlowDTO, cardDTO, new BigDecimal((String) amountMap.get("cardPayFee")),  (BigDecimal) amountMap.get("cardPayRateReal"), gatewayDTO);
            withholdFlowDTO.setTransAmount(withholdFlowDTO.getTransAmount().add(qrPayFlowDTO.getTipAmount()).add(qrPayFlowDTO.getDonationAmount()));
            withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        } catch (Exception e) {
            log.error("保存三方代扣交易流水失败:"+e.getMessage(),e);
            //限额回滚
            if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
            }
            //出账回滚
            if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 ||
                    qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
                doBatchAmountOutRollBack(qrPayFlowDTO,request);
            }

            // 卡券回滚
            oneMarketingRollback(qrPayFlowDTO,request);

            //更改捐赠流水状态为失败
            if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
                donationFlowService.updateDonationFlowStateByFlowId(qrPayFlowDTO.getId(), StaticDataEnum.TRANS_STATE_22.getCode(), request);
            }

            //更改小费流水状态为失败
            if(null != qrPayFlowDTO.getTipAmount() && qrPayFlowDTO.getTipAmount().compareTo(BigDecimal.ZERO) > 0){
                tipFlowService.updateStateByFlowId(qrPayFlowDTO.getId(), StaticDataEnum.TRANS_STATE_22.getCode(), request);
            }

            //更改交易状态为通道交易失败5
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_22.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);


            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //发往三方请求
        sendCardPayLatPayRequest(qrPayFlowDTO, withholdFlowDTO, cardJsonObj, request);

        //根据三方返回状态进行处理
//        handleCardLatPayPostResult(withholdFlowDTO, qrPayFlowDTO,cardJsonObj, request);


        handleCardLatPayPostResultV3(withholdFlowDTO, qrPayFlowDTO,cardJsonObj, request);

        return amountMap;
    }

    /**
     * 卡支付
     * @author zhoutt
     * @date 2021/10/29 10:55
     * @param qrPayFlowDTO
     * @param qrPayDTO
     * @param payUser
     * @param request
     */
    private Map<String, Object> cardPayV4(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, UserDTO payUser, HttpServletRequest request) throws Exception {

        //校验是否绑卡
        Integer cardState = payUser.getCardState();
        if(!cardState.equals(StaticDataEnum.USER_CARD_STATE_BINDED.getCode())){
            throw new BizException(I18nUtils.get("pay.card.not.bound", getLang(request)));
        }

        //交易类型
        //卡消费
        qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode());

        //通道校验
        Map<String, Object> channelResultMap = verifyAndGetPayGatewayCardInfo(qrPayDTO.getPayType(), qrPayDTO.getCardId(), qrPayFlowDTO, request);

        //通道和卡信息
        GatewayDTO gatewayDTO = (GatewayDTO) channelResultMap.get("gatewayDTO");
        CardDTO cardDTO = (CardDTO) channelResultMap.get("cardDTO");
        JSONObject cardJsonObj = (JSONObject) channelResultMap.get("cardJsonObj");

        //收款方是商户 进行交易金额校验
        Map<String, Object> amountMap = verifyAndGetPayAmountV3(qrPayFlowDTO, qrPayDTO, cardDTO, request);

        //进行限额判断
        if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimit(gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(), request);
        }

        try{
            Long id = qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request);
            qrPayFlowDTO.setId(id);
        }catch (Exception e){
            log.error("支付保存订单流水出错",  qrPayDTO.toString());
            //限额回滚
            if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
            }
            throw new BizException(I18nUtils.get("ay.order.same", getLang(request)));
        }

        //账户出账
        if((qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 && qrPayFlowDTO.getMarketingId() == null ) ||
                qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0 ){
            doAccountOut(qrPayFlowDTO, gatewayDTO, request);
        }

        // 有使用卡券
        if(qrPayFlowDTO.getMarketingId() != null){
            boolean useMarketingResult = false;
            try{
                useMarketingResult = this.useOneMarketing(qrPayFlowDTO,request);
            }catch (Exception e){
                log.error("用户卡券使用失败");
                //限额回滚
                if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                    rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
                }
                // 出账回滚
                doBatchAmountOutRollBack(qrPayFlowDTO,request);
                //更改交易状态为交易失败
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                updateFlow(qrPayFlowDTO, null, null, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            if(!useMarketingResult){
                //限额回滚
                if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                    rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
                }
                //出账回滚
                doBatchAmountOutRollBack(qrPayFlowDTO,request);
                //更改交易状态为通道交易失败
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                updateFlow(qrPayFlowDTO, null, null, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
        }


        //捐赠金额
        BigDecimal donationAmount = new BigDecimal((String) amountMap.get("donationAmount"));
        if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
            // 封装捐赠流水
            DonationFlowDTO donationFlowDTO = new DonationFlowDTO();
            donationFlowDTO.setFlowId(qrPayFlowDTO.getId());
            donationFlowDTO.setUserId(payUser.getId());
            donationFlowDTO.setUserName(payUser.getUserFirstName() + " " + payUser.getUserLastName());
            donationFlowDTO.setInstituteId(Long.valueOf(qrPayDTO.getDonationInstiuteId()));
            donationFlowDTO.setAmount(donationAmount);
            donationFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
            donationFlowDTO.setSettlementAmount(donationAmount);
            donationFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_34.getCode());
            donationFlowDTO.setMerchantId(qrPayFlowDTO.getMerchantId());
            donationFlowService.saveDonationFlow(donationFlowDTO, request);
        }

        //生成小费流水记录
        BigDecimal tipAmount = qrPayDTO.getTipAmount();
        if(null != tipAmount && tipAmount.compareTo(BigDecimal.ZERO) > 0){
            // 封装小费流水
            TipFlowDTO tipFlowDTO = new TipFlowDTO();
            tipFlowDTO.setFlowId(qrPayFlowDTO.getId());
            tipFlowDTO.setMerchantId(qrPayFlowDTO.getMerchantId());
            tipFlowDTO.setSettlementAmount(tipAmount);
            tipFlowDTO.setTipAmount(tipAmount);
            tipFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_35.getCode());
            tipFlowDTO.setUserId(payUser.getId());
            tipFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
            tipFlowService.saveTipFlow(tipFlowDTO, request);
        }

        //交易状态为三方初始状态
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
        qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,request);

        //调用卡支付
        //保存三方代扣交易流水
        WithholdFlowDTO withholdFlowDTO = null;
        try {
            withholdFlowDTO = packageWithHoldFlow(qrPayFlowDTO, cardDTO, new BigDecimal((String) amountMap.get("cardPayFee")),  (BigDecimal) amountMap.get("cardPayRateReal"), gatewayDTO);
            withholdFlowDTO.setTransAmount(withholdFlowDTO.getTransAmount().add(qrPayFlowDTO.getTipAmount()).add(qrPayFlowDTO.getDonationAmount()));
            withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        } catch (Exception e) {
            log.error("保存三方代扣交易流水失败:"+e.getMessage(),e);
            //限额回滚
            if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
            }
            //出账回滚
            if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 ||
                    qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
                doBatchAmountOutRollBack(qrPayFlowDTO,request);
            }

            // 卡券回滚
            oneMarketingRollback(qrPayFlowDTO,request);

            //更改捐赠流水状态为失败
            if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
                donationFlowService.updateDonationFlowStateByFlowId(qrPayFlowDTO.getId(), StaticDataEnum.TRANS_STATE_22.getCode(), request);
            }

            //更改小费流水状态为失败
            if(null != qrPayFlowDTO.getTipAmount() && qrPayFlowDTO.getTipAmount().compareTo(BigDecimal.ZERO) > 0){
                tipFlowService.updateStateByFlowId(qrPayFlowDTO.getId(), StaticDataEnum.TRANS_STATE_22.getCode(), request);
            }

            //更改交易状态为通道交易失败5
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_22.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);


            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //发往三方请求
        if(Integer.parseInt(gatewayDTO.getType().toString()) == StaticDataEnum.GATEWAY_TYPE_0.getCode()){
            sendCardPayLatPayRequest(qrPayFlowDTO, withholdFlowDTO, cardJsonObj, request);
        }else {
            Map<String,Object> urlMap = sendStripeRequest(qrPayFlowDTO, withholdFlowDTO, cardDTO, request);
            if(urlMap != null && urlMap.size() > 0) {
                // 触发了3DS验证
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
                updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
                return urlMap;
            }
        }

        //根据三方返回状态进行处理
        handleCardLatPayPostResultV3(withholdFlowDTO, qrPayFlowDTO,cardJsonObj, request);

        return amountMap;
    }

    private Map<String,Object> sendStripeRequest(QrPayFlowDTO qrPayFlowDTO, WithholdFlowDTO withholdFlowDTO, CardDTO cardDTO, HttpServletRequest request) {

        if(withholdFlowDTO.getTransAmount().compareTo(BigDecimal.ZERO) > 0){
            //渠道交易请求
            StripeAPIResponse stripeResult = null;
            try{
                StripePaymentIntentDTO stripePaymentIntentDTO = new StripePaymentIntentDTO();
                stripePaymentIntentDTO.setAmount(withholdFlowDTO.getTransAmount().multiply(new BigDecimal("100")).setScale(0,BigDecimal.ROUND_DOWN));
                stripePaymentIntentDTO.setCurrency("aud");
                stripePaymentIntentDTO.setConfirm(true);
                // 成功：card_1KIpEuAgx3Fd2j3e0Mz1gZZr 失败：card_1KM49nAgx3Fd2j3elyLhTzhC
                stripePaymentIntentDTO.setPayment_method(cardDTO.getStripeToken());
                List<String>  paymentMethodTypes = new ArrayList<>();


                paymentMethodTypes.add("card");
                stripePaymentIntentDTO.setPayment_method_types(paymentMethodTypes);
                stripePaymentIntentDTO.setCustomer(qrPayFlowDTO.getPayUserId().toString());
                // todo 测试挡板
//                stripePaymentIntentDTO.setCustomer("638621894721458176");
                //todo 根据3ds规则进行相应修改
//                Map<String, Object> s3dsMap = new HashMap<>();
//                s3dsMap.put("request_three_d_secure","any");
//                Map<String, Object> cards = new HashMap<>();
//                cards.put("card",s3dsMap);
//                stripePaymentIntentDTO.setPayment_method_options(cards);
                Map<String,Object> metaData = new HashMap<>();
                metaData.put("id",withholdFlowDTO.getOrdreNo());
                stripePaymentIntentDTO.setMetadata(metaData);

                stripePaymentIntentDTO.setReturn_url(stripe3dsReturnUrl);
                stripePaymentIntentDTO.setUse_stripe_sdk(false);

                stripePaymentIntentDTO.setDescription(StaticDataEnum.STRIPE_ORDER_DESC_PAY.getMessage());

                log.info("stripe payment request:" + stripePaymentIntentDTO );

                stripeResult = stripeService.createPaymentIntent(stripePaymentIntentDTO);

                log.info("stripe payment Result:" + stripeResult);
            }catch (Exception e){
                log.error("send stripe pay ,fail:"+e.getMessage(), e);
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                if(stripeResult != null){
                    withholdFlowDTO.setReturnMessage(e.getMessage());
                    withholdFlowDTO.setErrorMessage(e.getMessage());
                }
                return null;
            }
            if(stripeResult == null ){
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                return  null ;
            }

            String code = stripeResult.getCode();
            String message = stripeResult.getMessage();

            if(!stripeResult.isSuccess() || stripeResult.getData() == null ){
                // 请求失败
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                withholdFlowDTO.setErrorMessage(message);
                return  null ;
            }

            PaymentIntent paymentIntent = (PaymentIntent)stripeResult.getData();

            if(paymentIntent == null && paymentIntent.getId() == null){
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                return  null ;
            }

            if(paymentIntent.getStatus() == null ){
                // 没有交易状态视为可疑
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                return  null ;
            }

            withholdFlowDTO.setReturnMessage(paymentIntent.getStatus());

            if(StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_CANCELED.getCode().equals(paymentIntent.getStatus())
                    || StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_REQUIRES_PAYMENT_METHOD.getCode().equals(paymentIntent.getStatus())
                    || StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_FAILED.getCode().equals(paymentIntent.getStatus())){
                // 失败
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                withholdFlowDTO.setErrorMessage(paymentIntent.getLastPaymentError().getMessage());
                withholdFlowDTO.setReturnCode(paymentIntent.getLastPaymentError().getCode());
            } else if(StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_REQUIRES_ACTION.getCode().equals(paymentIntent.getStatus())){
                // 触发3DS
                // 取出回到的URL 和 密钥 ，并返回
//                String url = (String) paymentIntent.getNextAction().getUseStripeSdk().get("stripe_js");
//                String url = (String) paymentIntent.getNextAction().getUseStripeSdk().get("three_ds_method_url");
                String url = (String) paymentIntent.getNextAction().getRedirectToUrl().getUrl();
                String clientSecret =  paymentIntent.getClientSecret();

                withholdFlowDTO.setStripeUrl(url);
                withholdFlowDTO.setStripeClientSecret(clientSecret);
                withholdFlowDTO.setStripeId(paymentIntent.getId());
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_44.getCode());

                Map<String ,Object> returnData = new HashMap<>();
                returnData.put("url", url);
                returnData.put("clientSecret", clientSecret);
                return  returnData;
            }else if(StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_SUCCEEDED.getCode().equals(paymentIntent.getStatus())){
                // 交易成功
                // 如果能取出三方单号，记录单号
                if(paymentIntent.getId() == null){
                    withholdFlowDTO.setStripeId(paymentIntent.getId());
                }
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            }else{
                // 返回其他视为可疑
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            }

        }else{
            //交易金额为0处理
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            withholdFlowDTO.setCheckState( StaticDataEnum.IS_CHECK_1.getCode());
            withholdFlowDTO.setOrdreNo(null);
        }
        return  null;
    }

    /**
     * 卡支付
     * @author zhangzeyuan
     * @date 2021/6/21 17:55
     * @param qrPayFlowDTO
     * @param qrPayDTO
     * @param payUser
     * @param request
     */
    private Map<String, Object> cardPay(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, UserDTO payUser, HttpServletRequest request) throws Exception {

        //校验是否绑卡
        Integer cardState = payUser.getCardState();
        if(!cardState.equals(StaticDataEnum.USER_CARD_STATE_BINDED.getCode())){
            throw new BizException(I18nUtils.get("pay.card.not.bound", getLang(request)));
        }

        //交易类型
        //卡消费
        qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode());

        //通道校验
        Map<String, Object> channelResultMap = verifyAndGetPayGatewayCardInfo(qrPayDTO.getPayType(), qrPayDTO.getCardId(), qrPayFlowDTO, request);

        //通道和卡信息
        GatewayDTO gatewayDTO = (GatewayDTO) channelResultMap.get("gatewayDTO");
        CardDTO cardDTO = (CardDTO) channelResultMap.get("cardDTO");
        JSONObject cardJsonObj = (JSONObject) channelResultMap.get("cardJsonObj");

        //收款方是商户 进行交易金额校验
        Map<String, Object> amountMap = verifyAndGetPayAmount(qrPayFlowDTO, qrPayDTO, cardDTO, request);

        //进行限额判断
        if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimit(gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(), request);
        }

        try{
            Long id = qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request);
            qrPayFlowDTO.setId(id);
        }catch (Exception e){
            log.error("支付保存订单流水出错",  qrPayDTO.toString());
            //限额回滚
            if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
            }
            throw new BizException(I18nUtils.get("pay.order.same", getLang(request)));
        }

        //账户出账
        if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 ||
                qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            doAccountOut(qrPayFlowDTO, gatewayDTO, request);
        }

        //捐赠金额
        BigDecimal donationAmount = new BigDecimal((String) amountMap.get("donationAmount"));
        if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
            // 封装捐赠流水
            DonationFlowDTO donationFlowDTO = new DonationFlowDTO();
            donationFlowDTO.setFlowId(qrPayFlowDTO.getId());
            donationFlowDTO.setUserId(payUser.getId());
            donationFlowDTO.setUserName(payUser.getUserFirstName() + " " + payUser.getUserLastName());
            donationFlowDTO.setInstituteId(Long.valueOf(qrPayDTO.getDonationInstiuteId()));
            donationFlowDTO.setAmount(donationAmount);
            donationFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
            donationFlowDTO.setSettlementAmount(donationAmount);
            donationFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_34.getCode());
            donationFlowDTO.setMerchantId(qrPayFlowDTO.getMerchantId());
            donationFlowService.saveDonationFlow(donationFlowDTO, request);
        }

        //生成小费流水记录
        BigDecimal tipAmount = qrPayDTO.getTipAmount();
        if(null != tipAmount && tipAmount.compareTo(BigDecimal.ZERO) > 0){
            // 封装小费流水
            TipFlowDTO tipFlowDTO = new TipFlowDTO();
            tipFlowDTO.setFlowId(qrPayFlowDTO.getId());
            tipFlowDTO.setMerchantId(qrPayFlowDTO.getMerchantId());
            tipFlowDTO.setSettlementAmount(tipAmount);
            tipFlowDTO.setTipAmount(tipAmount);
            tipFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_35.getCode());
            tipFlowDTO.setUserId(payUser.getId());
            tipFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
            tipFlowService.saveTipFlow(tipFlowDTO, request);
        }

        //交易状态为三方初始状态
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
        qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,request);

        //调用卡支付
        //保存三方代扣交易流水
        WithholdFlowDTO withholdFlowDTO = null;
        try {

            withholdFlowDTO = packageWithHoldFlow(qrPayFlowDTO, cardDTO, new BigDecimal((String) amountMap.get("cardPayFee")),  (BigDecimal) amountMap.get("cardPayRateReal"), gatewayDTO);
            withholdFlowDTO.setTransAmount(withholdFlowDTO.getTransAmount().add(qrPayFlowDTO.getTipAmount()).add(qrPayFlowDTO.getDonationAmount()));
            withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        } catch (Exception e) {
            log.error("保存三方代扣交易流水失败:"+e.getMessage(),e);
            //限额回滚
            if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
            }
            //出账回滚
            if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 ||
                    qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
                doBatchAmountOutRollBack(qrPayFlowDTO,request);
            }
            //更改交易状态为通道交易失败
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_22.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);

            //更改捐赠流水状态为失败
            if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
                donationFlowService.updateDonationFlowStateByFlowId(qrPayFlowDTO.getId(), StaticDataEnum.TRANS_STATE_22.getCode(), request);
            }

            //更改小费流水状态为失败
            if(null != qrPayFlowDTO.getTipAmount() && qrPayFlowDTO.getTipAmount().compareTo(BigDecimal.ZERO) > 0){
                tipFlowService.updateStateByFlowId(qrPayFlowDTO.getId(), StaticDataEnum.TRANS_STATE_22.getCode(), request);
            }

            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //发往三方请求
        sendCardPayLatPayRequest(qrPayFlowDTO, withholdFlowDTO, cardJsonObj, request);

        //根据三方返回状态进行处理
        handleCardLatPayPostResult(withholdFlowDTO, qrPayFlowDTO,null, request);

        return amountMap;
    }



    /**
     * 支付交易出账处理
     * @author zhangzeyuan
     * @date 2021/6/27 19:59
     * @param qrPayFlowDTO
     * @param gatewayDTO
     * @param request
     */
    private void doAccountOut(QrPayFlowDTO qrPayFlowDTO,GatewayDTO gatewayDTO, HttpServletRequest request) throws Exception {
        // 记录交易状态为出账初始状态
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_10.getCode());
        qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,request);

        //出账结果
        boolean amountOutResult;
        try {
            // 调用支付批量出入账接口，进行出账
            amountOutResult = doBatchAmountOut(qrPayFlowDTO,request);
        }catch (Exception e){
            log.error("账户出账 Exception："+e.getMessage(),e);
            //流程报错返回处理中
            if(gatewayDTO != null && gatewayDTO.getId() != null){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(), request);
            }

            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //判断出账结果，如果失败，限额回滚，修改交易为失败状态
        if(!amountOutResult){
            rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);

            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
    }


    /**
     * 校验支付金额并获取返回信息
     * @author zhangzeyuan
     * @date 2021/6/27 19:57
     * @param qrPayFlowDTO
     * @param qrPayDTO
     * @param cardDTO
     * @param request
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    private Map<String, Object> verifyAndGetPayAmount(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, CardDTO cardDTO,
                                                      HttpServletRequest request) throws Exception {
        Integer payType = qrPayDTO.getPayType();
        Long payUserId = qrPayDTO.getPayUserId();
        Long recUserId = qrPayDTO.getRecUserId();

        //查询商户配置信息
        HashMap<String, Object> queryParamsMap = Maps.newHashMapWithExpectedSize(3);
        queryParamsMap.put("id", qrPayDTO.getMerchantId());
        queryParamsMap.put("userId", recUserId);
        queryParamsMap.put("isAvailable", StaticDataEnum.AVAILABLE_1.getCode());
        MerchantDTO merchantDTO = merchantService.findOneMerchant(queryParamsMap);
        if(Objects.isNull(merchantDTO) || Objects.isNull(merchantDTO.getId())) {
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        //订单金额
        BigDecimal transAmount = qrPayDTO.getTransAmount();
        //计算实付金额
        BigDecimal tempTransAmount = transAmount;

        //使用红包金额
        BigDecimal redEnvelopeAmount = qrPayDTO.getRedEnvelopeAmount() == null ? BigDecimal.ZERO : qrPayDTO.getRedEnvelopeAmount();

        //查询用户可用红包金额
        BigDecimal payUserAmount = BigDecimal.ZERO;
        //查询商户整体出售剩余金额
        BigDecimal merchantAmount = BigDecimal.ZERO;
        //额外折扣
        BigDecimal extraDiscount = BigDecimal.ZERO;
        //营销折扣
        BigDecimal marketingDiscount = BigDecimal.ZERO;
        //固定折扣
        BigDecimal baseDiscount = BigDecimal.ZERO;

        //整体出售折扣
        BigDecimal wholeSaleDiscount = BigDecimal.ZERO;

        //从redis中取出折扣信息
        Map<?, Object> amountMap = redisUtils.hmget(Constant.getOrderAmountDetailRedisKey(payUserId.toString(), qrPayDTO.getTransNo()));
        if(null == amountMap || amountMap.isEmpty()){
            //为空重新计算
            amountMap = getOrderDiscount(payUserId, recUserId, merchantDTO, payType);
        }

        payUserAmount = (BigDecimal) amountMap.get("payUserAmount");
        merchantAmount = (BigDecimal) amountMap.get("merchantAmount");

        wholeSaleDiscount = (BigDecimal) amountMap.get("wholeSaleDiscount");

        extraDiscount = (BigDecimal) amountMap.get("extraDiscount");
        marketingDiscount = (BigDecimal) amountMap.get("marketingDiscount");
        baseDiscount = (BigDecimal) amountMap.get("baseDiscount");

        //使用额度超过可用红包金额


        if(redEnvelopeAmount.compareTo(payUserAmount) > 0){
            throw new BizException(I18nUtils.get("pay.red.envelope.over.limit", getLang(request)));
        }

        //整体出售使用额度
        BigDecimal useWholeSaleAmount = tempTransAmount.min(merchantAmount);
        //正常出售使用额度
        BigDecimal useNormalSaleAmount = tempTransAmount.subtract(useWholeSaleAmount);

        //整体出售使用红包金额
        BigDecimal wholeSaleUseRedAmount = BigDecimal.ZERO;
        //正常出售使用红包金额
        BigDecimal normalSaleUesRedAmount = BigDecimal.ZERO;

        if(redEnvelopeAmount.compareTo(BigDecimal.ZERO) > 0){
            if(useWholeSaleAmount.compareTo(redEnvelopeAmount) >= 0){
                wholeSaleUseRedAmount = redEnvelopeAmount;
            }else {
                wholeSaleUseRedAmount = useWholeSaleAmount;
            }
            normalSaleUesRedAmount = redEnvelopeAmount.subtract(wholeSaleUseRedAmount);
        }

        //整体出售折扣金额
        BigDecimal wholeSaleDiscountAmount = (useWholeSaleAmount.subtract(wholeSaleUseRedAmount)).multiply(wholeSaleDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        //正常出售折扣金额  固定折扣金额 +  营销折扣金额 + 额外折扣金额
        BigDecimal baseDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(baseDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal extraDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(extraDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal marketingDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(marketingDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);

        BigDecimal normalSaleDiscountAmount = baseDiscountAmount.add(extraDiscountAmount).add(marketingDiscountAmount);

        // 实付金额
        BigDecimal payAmount = useWholeSaleAmount.subtract(wholeSaleUseRedAmount).subtract(wholeSaleDiscountAmount).add(
                useNormalSaleAmount.subtract(normalSaleUesRedAmount).subtract(normalSaleDiscountAmount)
        );

        //实际支付总金额 卡支付 + 捐赠费 + 小费 + 手续费
        BigDecimal tempCardPayAllAmount = BigDecimal.ZERO;

        //分期付卡支付金额
        BigDecimal creditCardFirstPayAmount = BigDecimal.ZERO;
        //分期付分期金额
        BigDecimal remainingCreditPayAmount = BigDecimal.ZERO;

        //通道手续费
        BigDecimal gateWayFee = BigDecimal.ZERO;
        //通道手续费率
        BigDecimal transChannelFeeRate = BigDecimal.ZERO;
        //收取方向
        int direction = 0;

        //捐赠金额
        BigDecimal donationAmount = BigDecimal.ZERO;
        if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId())){
            DonationInstituteDataDTO donationData = donationInstituteService.getDonationDataById(qrPayDTO.getDonationInstiuteId());
            if(null != donationData && null != donationData.getId()){
                donationAmount = donationData.getDonationAmount();
            }
        }
        //小费金额
        BigDecimal tipAmount = BigDecimal.ZERO;
        if(Objects.nonNull(qrPayDTO.getTipAmount())){
            tipAmount = qrPayDTO.getTipAmount();
        }


        //计算手续费
        if(payAmount.compareTo(BigDecimal.ZERO) > 0){
            if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){
                //分期付
                //分期付卡支付金额
                // 20211028 分期付不足10元，按一期全额支付
                if(payAmount.compareTo(new BigDecimal("10")) < 0){
                    creditCardFirstPayAmount = payAmount;
                }else{
                    creditCardFirstPayAmount = payAmount.multiply(new BigDecimal(Constant.PAY_CREDIT_NEED_CARDPAY_AMOUNT_RATE)).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                //分期付分期金额
                remainingCreditPayAmount = payAmount.subtract(creditCardFirstPayAmount);

                //分期付需要用卡支付的金额 =  25%卡支付金额 + 捐赠费 + 小费
                tempCardPayAllAmount = creditCardFirstPayAmount.add(donationAmount).add(tipAmount);
            }else if(qrPayDTO.getPayType().equals(StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode())){
                //卡支付
                tempCardPayAllAmount = payAmount.add(donationAmount).add(tipAmount);
                payAmount = payAmount.add(donationAmount).add(tipAmount);
            }
        }else {

            tempCardPayAllAmount = donationAmount.add(tipAmount);
        }

        if(tempCardPayAllAmount.compareTo(BigDecimal.ZERO) > 0){
            // 计算查询手续费
            Map<String, Object> feeMap = getCardPayTransactionFee(0, cardDTO.getCustomerCcType(), tempCardPayAllAmount, request);

            gateWayFee = (BigDecimal) feeMap.get("channelFeeAmount");

            transChannelFeeRate = (BigDecimal) feeMap.get("transChannelFeeRate");

            tempCardPayAllAmount = tempCardPayAllAmount.add(gateWayFee);
        }

        log.info("TrulyPayAmount:"+ tempCardPayAllAmount);

        //验证使用整体出售额度 和 正常出售额度
        if(useWholeSaleAmount.compareTo(qrPayDTO.getWholeSalesAmount()) != 0
                || useNormalSaleAmount.compareTo(qrPayDTO.getNormalSalesAmount()) != 0){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //验证 当前计算实付金额 是否等于 之前获取金额接口计算出的金额 不包含手续费
        if(tempCardPayAllAmount.compareTo(qrPayDTO.getTrulyPayAmount()) != 0){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }



        //实际折扣
        BigDecimal realWholeSaleDiscount;
        //正常出售折扣
        BigDecimal realBaseDiscount;
        BigDecimal realMarkingDiscount;
        BigDecimal realExtraDiscount;

        //订单类型
        int saleType = 0;

        //计算实际折扣、订单号标识
        if(useNormalSaleAmount.compareTo(BigDecimal.ZERO) > 0){
            //使用了正常出售额度
            realBaseDiscount = baseDiscount;
            realMarkingDiscount = marketingDiscount;
            realExtraDiscount = extraDiscount;

            if(useWholeSaleAmount.compareTo(BigDecimal.ZERO) > 0){
                // 混合出售
                saleType = StaticDataEnum.SALE_TYPE_2.getCode();
                realWholeSaleDiscount = wholeSaleDiscount;
            }else{
                // 正常出售
                saleType = StaticDataEnum.SALE_TYPE_0.getCode();
                realWholeSaleDiscount = BigDecimal.ZERO;
            }
        }else{
            //整体出售
            saleType = StaticDataEnum.SALE_TYPE_1.getCode();

            realWholeSaleDiscount = wholeSaleDiscount;

            realBaseDiscount = BigDecimal.ZERO;
            realMarkingDiscount = BigDecimal.ZERO;
            realExtraDiscount = BigDecimal.ZERO;
        }

        //平台服务费
        BigDecimal platformFee = useNormalSaleAmount.multiply(merchantDTO.getAppChargeRate() == null ? BigDecimal.ZERO : merchantDTO.getAppChargeRate()).setScale(2,BigDecimal.ROUND_HALF_UP);

        //实收金额 = 正常交易 - 基础折扣 - 时间内营销折扣 -营销折扣 - 平台服务费 不包括红包
        BigDecimal recAmount = useNormalSaleAmount.subtract(normalSaleDiscountAmount).subtract(platformFee);

        //封装qrPayFlowDTO信息
        qrPayFlowDTO.setMerchantId(merchantDTO.getId());
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
        qrPayFlowDTO.setSaleType(saleType);

        qrPayFlowDTO.setPlatformFee(platformFee);
        qrPayFlowDTO.setRecAmount(recAmount);

        //整体出售额度
        qrPayFlowDTO.setWholeSalesAmount(useWholeSaleAmount);
        //正常出售额度
        qrPayFlowDTO.setNormalSaleAmount(useNormalSaleAmount);
        //红包
        qrPayFlowDTO.setRedEnvelopeAmount(redEnvelopeAmount);

        //整体出售折扣 折扣金额
        qrPayFlowDTO.setWholeSalesDiscount(realWholeSaleDiscount);
        qrPayFlowDTO.setWholeSalesDiscountAmount(wholeSaleDiscountAmount);

        //固定、营销、额外折扣金额
        qrPayFlowDTO.setBaseDiscountAmount(baseDiscountAmount);
        qrPayFlowDTO.setExtraDiscountAmount(extraDiscountAmount);
        qrPayFlowDTO.setMarkingDiscountAmount(marketingDiscountAmount);
        //固定、营销、额外折扣率
        qrPayFlowDTO.setBaseDiscount(realBaseDiscount);
        qrPayFlowDTO.setExtraDiscount(realExtraDiscount);
        qrPayFlowDTO.setMarkingDiscount(realMarkingDiscount);
        //捐赠费
        qrPayFlowDTO.setDonationAmount(donationAmount);
        //小费
        qrPayFlowDTO.setTipAmount(tipAmount);

        if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){
            qrPayFlowDTO.setPayAmount(payAmount);
            //分期付borrowID
            qrPayFlowDTO.setCreditOrderNo(SnowflakeUtil.generateId().toString());
        }else if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode())){
            qrPayFlowDTO.setFee(gateWayFee);
            qrPayFlowDTO.setRate(transChannelFeeRate);
            qrPayFlowDTO.setFeeDirection(direction);
            qrPayFlowDTO.setPayAmount(payAmount.add(gateWayFee));
        }

        qrPayFlowDTO.setIsNeedClear(StaticDataEnum.NEED_CLEAR_TYPE_1.getCode());
        qrPayFlowDTO.setClearState(StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        qrPayFlowDTO.setClearAmount(recAmount);

        //返回信息封装
        HashMap<String, Object> resultMap = Maps.newHashMapWithExpectedSize(13);

        //创建订单时间格式化
        Long orderCreatedDate = qrPayFlowDTO.getCreatedDate();
        SimpleDateFormat monthFormat = new SimpleDateFormat("dd MMM", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        String monthStr = monthFormat.format(orderCreatedDate);
        String timeStr = timeFormat.format(orderCreatedDate);
        resultMap.put("orderCreatedDate", monthStr + " at " + timeStr);

        resultMap.put("transAmount", transAmount.toString());
        resultMap.put("transNo", qrPayFlowDTO.getTransNo());
        resultMap.put("payAmount", qrPayFlowDTO.getPayAmount().toString());
        resultMap.put("totalAmount", qrPayFlowDTO.getPayAmount().add(qrPayFlowDTO.getDonationAmount()).add(tipAmount).toString());

        resultMap.put("merchantName", merchantDTO.getPracticalName());
        resultMap.put("payType", payType);
        resultMap.put("useRedEnvelopeAmount", redEnvelopeAmount.toString());

        resultMap.put("donationAmount", donationAmount.toString());

        resultMap.put("tipAmount", tipAmount.toString());


        if(qrPayDTO.getPayType().equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){
            //分期付
            resultMap.put("creditNeedCardPayAmount", creditCardFirstPayAmount.add(donationAmount).add(tipAmount).add(gateWayFee).toString());
            resultMap.put("creditNeedCardPayNoFeeAmount", creditCardFirstPayAmount.toString());
            resultMap.put("remainingCreditAmount", remainingCreditPayAmount.toString());

            //todo  改为调用生成分期付订单接口返回
            int period = 0;
            //下期还款金额
            BigDecimal creditNextRepayAmount = BigDecimal.ZERO;

            if(remainingCreditPayAmount.compareTo(BigDecimal.ZERO) == 0){
                period = 1;
                creditNextRepayAmount = remainingCreditPayAmount;
            }else if(remainingCreditPayAmount.compareTo(new BigDecimal("20")) <= 0){
                period = 2;
                creditNextRepayAmount = remainingCreditPayAmount;
            }else if(remainingCreditPayAmount.compareTo(new BigDecimal("40")) <= 0){
                period = 3;
                creditNextRepayAmount = remainingCreditPayAmount.divide(new BigDecimal("2"), 2, RoundingMode.FLOOR);
            }else{
                period = 4;
                creditNextRepayAmount = remainingCreditPayAmount.divide(new BigDecimal("3"), 2, RoundingMode.FLOOR);
            }
            resultMap.put("period", period);
            resultMap.put("creditNextRepayAmount", creditNextRepayAmount.toString());
        }

        resultMap.put("cardPayFee", gateWayFee.toString());
        resultMap.put("cardPayRate", transChannelFeeRate.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
        resultMap.put("cardPayRateReal", transChannelFeeRate);

        resultMap.put("cardNo", cardDTO.getCardNo());
        resultMap.put("cardId", cardDTO.getId().toString());
        resultMap.put("cardCcType", cardDTO.getCustomerCcType());

        resultMap.put("wholeSaleUserDiscountAmount", wholeSaleDiscountAmount.toString());
        resultMap.put("wholeSalesUserDiscount", realWholeSaleDiscount.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        resultMap.put("normalSaleUserDiscountAmount", normalSaleDiscountAmount.toString());
        resultMap.put("normalSalesUserDiscount", (realBaseDiscount.add(realExtraDiscount).add(realMarkingDiscount)).multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        return resultMap;
    }


    /**
     * 校验支付金额并获取返回信息
     * @author zhangzeyuan
     * @date 2021/6/27 19:57
     * @param qrPayFlowDTO
     * @param qrPayDTO
     * @param cardDTO
     * @param request
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    private Map<String, Object> verifyAndGetPayAmountV3(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, CardDTO cardDTO,
                                                        HttpServletRequest request) throws Exception {
        Integer payType = qrPayDTO.getPayType();
        Long payUserId = qrPayDTO.getPayUserId();
        Long recUserId = qrPayDTO.getRecUserId();

        //查询商户配置信息
        HashMap<String, Object> queryParamsMap = Maps.newHashMapWithExpectedSize(3);
        queryParamsMap.put("id", qrPayDTO.getMerchantId());
        queryParamsMap.put("userId", recUserId);
        queryParamsMap.put("isAvailable", StaticDataEnum.AVAILABLE_1.getCode());
        MerchantDTO merchantDTO = merchantService.findOneMerchant(queryParamsMap);
        if(Objects.isNull(merchantDTO) || Objects.isNull(merchantDTO.getId())) {
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        //订单金额
        BigDecimal transAmount = qrPayDTO.getTransAmount();
        //计算实付金额
        BigDecimal tempTransAmount = transAmount;

        //使用红包金额
//        BigDecimal redEnvelopeAmount = qrPayDTO.getRedEnvelopeAmount() == null ? BigDecimal.ZERO : qrPayDTO.getRedEnvelopeAmount();
        // 卡券金额有可能大于交易金额，卡券金额与交易金额取小
        // 实际使用红包额度
        BigDecimal redEnvelopeAmount = qrPayDTO.getMarketingBalance().min(transAmount);

        //查询用户可用红包金额
//        BigDecimal payUserAmount = BigDecimal.ZERO;
        //查询商户整体出售剩余金额
        BigDecimal merchantAmount = BigDecimal.ZERO;
        //额外折扣
        BigDecimal extraDiscount = BigDecimal.ZERO;
        //营销折扣
        BigDecimal marketingDiscount = BigDecimal.ZERO;
        //固定折扣
        BigDecimal baseDiscount = BigDecimal.ZERO;

        //整体出售折扣
        BigDecimal wholeSaleDiscount = BigDecimal.ZERO;

        //从redis中取出折扣信息
        Map<?, Object> amountMap = redisUtils.hmget(Constant.getOrderAmountDetailRedisKey(payUserId.toString(), qrPayDTO.getTransNo()));
        if(null == amountMap || amountMap.isEmpty()){
            //为空重新计算
            amountMap = getOrderDiscount(payUserId, recUserId, merchantDTO, payType);
        }

//        payUserAmount = (BigDecimal) amountMap.get("payUserAmount");
        merchantAmount = (BigDecimal) amountMap.get("merchantAmount");

        wholeSaleDiscount = (BigDecimal) amountMap.get("wholeSaleDiscount");

        extraDiscount = (BigDecimal) amountMap.get("extraDiscount");
        marketingDiscount = (BigDecimal) amountMap.get("marketingDiscount");
        baseDiscount = (BigDecimal) amountMap.get("baseDiscount");

        //使用额度超过可用红包金额
//        if(redEnvelopeAmount.compareTo(payUserAmount) > 0){
//            throw new BizException(I18nUtils.get("pay.red.envelope.over.limit", getLang(request)));
//        }

        //整体出售使用额度
        BigDecimal useWholeSaleAmount = tempTransAmount.min(merchantAmount);
        //正常出售使用额度
        BigDecimal useNormalSaleAmount = tempTransAmount.subtract(useWholeSaleAmount);

        //整体出售使用红包金额
        BigDecimal wholeSaleUseRedAmount = BigDecimal.ZERO;
        //正常出售使用红包金额
        BigDecimal normalSaleUesRedAmount = BigDecimal.ZERO;

        if(redEnvelopeAmount.compareTo(BigDecimal.ZERO) > 0){
            if(useWholeSaleAmount.compareTo(redEnvelopeAmount) >= 0){
                wholeSaleUseRedAmount = redEnvelopeAmount;
            }else {
                wholeSaleUseRedAmount = useWholeSaleAmount;
            }
            normalSaleUesRedAmount = redEnvelopeAmount.subtract(wholeSaleUseRedAmount);
        }

        //整体出售折扣金额
        BigDecimal wholeSaleDiscountAmount = (useWholeSaleAmount.subtract(wholeSaleUseRedAmount)).multiply(wholeSaleDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        //正常出售折扣金额  固定折扣金额 +  营销折扣金额 + 额外折扣金额
        BigDecimal baseDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(baseDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal extraDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(extraDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal marketingDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(marketingDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);

        BigDecimal normalSaleDiscountAmount = baseDiscountAmount.add(extraDiscountAmount).add(marketingDiscountAmount);

        // 实付金额
        BigDecimal payAmount = useWholeSaleAmount.subtract(wholeSaleUseRedAmount).subtract(wholeSaleDiscountAmount).add(
                useNormalSaleAmount.subtract(normalSaleUesRedAmount).subtract(normalSaleDiscountAmount)
        );

        //验证使用整体出售额度 和 正常出售额度
        if(useWholeSaleAmount.compareTo(qrPayDTO.getWholeSalesAmount()) != 0
                || useNormalSaleAmount.compareTo(qrPayDTO.getNormalSalesAmount()) != 0){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //实际支付总金额 卡支付 + 捐赠费 + 小费 + 手续费
        BigDecimal tempCardPayAllAmount = BigDecimal.ZERO;

        //分期付卡支付金额
        BigDecimal creditCardFirstPayAmount = BigDecimal.ZERO;
        //分期付分期金额
        BigDecimal remainingCreditPayAmount = BigDecimal.ZERO;

        //通道手续费
        BigDecimal gateWayFee = BigDecimal.ZERO;
        //通道手续费率
        BigDecimal transChannelFeeRate = BigDecimal.ZERO;
        //收取方向
        int direction = 0;

        //捐赠金额
        BigDecimal donationAmount = BigDecimal.ZERO;
        if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId())){
            DonationInstituteDataDTO donationData = donationInstituteService.getDonationDataById(qrPayDTO.getDonationInstiuteId());
            if(null != donationData && null != donationData.getId()){
                donationAmount = donationData.getDonationAmount();
            }
        }
        //小费金额
        BigDecimal tipAmount = BigDecimal.ZERO;
        if(Objects.nonNull(qrPayDTO.getTipAmount())){
            tipAmount = qrPayDTO.getTipAmount();
        }

        //计算手续费
        if(payAmount.compareTo(BigDecimal.ZERO) > 0){
            if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){

                //分期付
                //分期付卡支付金额
                // 20211028 分期付不足10元，按一期全额支付  未使用红包 实付金额不能小于10元
                if(null == qrPayDTO.getMarketingId() &&  payAmount.compareTo(new BigDecimal(StaticDataEnum.CREDIT_MIN_TRANSAMOUNT.getMessage())) < 0){
                    throw new BizException(I18nUtils.get("credit.amount.limit", getLang(request), new String[]{StaticDataEnum.CREDIT_MIN_TRANSAMOUNT.getMessage()}));
                }

                if(payAmount.compareTo(new BigDecimal("10")) < 0){
                    creditCardFirstPayAmount = payAmount;
                }else{
                    creditCardFirstPayAmount = payAmount.multiply(new BigDecimal(Constant.PAY_CREDIT_NEED_CARDPAY_AMOUNT_RATE)).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                //分期付分期金额
                remainingCreditPayAmount = payAmount.subtract(creditCardFirstPayAmount);

                //分期付需要用卡支付的金额 =  25%卡支付金额 + 捐赠费 + 小费
                tempCardPayAllAmount = creditCardFirstPayAmount.add(donationAmount).add(tipAmount);
            }else if(qrPayDTO.getPayType().equals(StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode())){
                //卡支付
                tempCardPayAllAmount = payAmount.add(donationAmount).add(tipAmount);
            }
        }else {
            tempCardPayAllAmount = donationAmount.add(tipAmount);
        }

        log.info("TrulyPayAmount:"+ payAmount);
        log.info("tempCardPayAllAmount:"+ tempCardPayAllAmount);

        //验证 当前计算实付金额 是否等于 之前获取金额接口计算出的金额 不包含手续费 不包含捐赠、小费
        if(tempCardPayAllAmount.compareTo(qrPayDTO.getTrulyPayAmount()) != 0){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        if(tempCardPayAllAmount.compareTo(BigDecimal.ZERO) > 0){
            // 计算查询手续费
            Map<String, Object> feeMap = getCardPayTransactionFee(Integer.parseInt(qrPayFlowDTO.getGatewayId().toString()), cardDTO.getCustomerCcType(), tempCardPayAllAmount, request);

            gateWayFee = (BigDecimal) feeMap.get("channelFeeAmount");

            transChannelFeeRate = (BigDecimal) feeMap.get("transChannelFeeRate");

            tempCardPayAllAmount = tempCardPayAllAmount.add(gateWayFee);
        }

        //实际折扣
        BigDecimal realWholeSaleDiscount;
        //正常出售折扣
        BigDecimal realBaseDiscount;
        BigDecimal realMarkingDiscount;
        BigDecimal realExtraDiscount;

        //订单类型
        int saleType = 0;

        //计算实际折扣、订单号标识
        if(useNormalSaleAmount.compareTo(BigDecimal.ZERO) > 0){
            //使用了正常出售额度
            realBaseDiscount = baseDiscount;
            realMarkingDiscount = marketingDiscount;
            realExtraDiscount = extraDiscount;

            if(useWholeSaleAmount.compareTo(BigDecimal.ZERO) > 0){
                // 混合出售
                saleType = StaticDataEnum.SALE_TYPE_2.getCode();
                realWholeSaleDiscount = wholeSaleDiscount;
            }else{
                // 正常出售
                saleType = StaticDataEnum.SALE_TYPE_0.getCode();
                realWholeSaleDiscount = BigDecimal.ZERO;
            }
        }else{
            //整体出售
            saleType = StaticDataEnum.SALE_TYPE_1.getCode();

            realWholeSaleDiscount = wholeSaleDiscount;

            realBaseDiscount = BigDecimal.ZERO;
            realMarkingDiscount = BigDecimal.ZERO;
            realExtraDiscount = BigDecimal.ZERO;
        }

        //平台服务费
        BigDecimal platformFee = useNormalSaleAmount.multiply(merchantDTO.getAppChargeRate() == null ? BigDecimal.ZERO : merchantDTO.getAppChargeRate()).setScale(2,BigDecimal.ROUND_HALF_UP);

        //实收金额 = 正常交易 - 基础折扣 - 时间内营销折扣 -营销折扣 - 平台服务费 不包括红包
        BigDecimal recAmount = useNormalSaleAmount.subtract(normalSaleDiscountAmount).subtract(platformFee);

        //封装qrPayFlowDTO信息
        qrPayFlowDTO.setMerchantId(merchantDTO.getId());
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
        qrPayFlowDTO.setSaleType(saleType);

        qrPayFlowDTO.setPlatformFee(platformFee);
        qrPayFlowDTO.setRecAmount(recAmount);

        qrPayFlowDTO.setRedEnvelopeAmount(redEnvelopeAmount);
        //整体出售额度
        qrPayFlowDTO.setWholeSalesAmount(useWholeSaleAmount);
        //正常出售额度
        qrPayFlowDTO.setNormalSaleAmount(useNormalSaleAmount);

        //整体出售折扣 折扣金额
        qrPayFlowDTO.setWholeSalesDiscount(realWholeSaleDiscount);
        qrPayFlowDTO.setWholeSalesDiscountAmount(wholeSaleDiscountAmount);

        //固定、营销、额外折扣金额
        qrPayFlowDTO.setBaseDiscountAmount(baseDiscountAmount);
        qrPayFlowDTO.setExtraDiscountAmount(extraDiscountAmount);
        qrPayFlowDTO.setMarkingDiscountAmount(marketingDiscountAmount);
        //固定、营销、额外折扣率
        qrPayFlowDTO.setBaseDiscount(realBaseDiscount);
        qrPayFlowDTO.setExtraDiscount(realExtraDiscount);
        qrPayFlowDTO.setMarkingDiscount(realMarkingDiscount);
        //捐赠费
        qrPayFlowDTO.setDonationAmount(donationAmount);
        //小费
        qrPayFlowDTO.setTipAmount(tipAmount);

        if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){
            qrPayFlowDTO.setPayAmount(payAmount);
            //分期付borrowID
            qrPayFlowDTO.setCreditOrderNo(SnowflakeUtil.generateId().toString());
        }else if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode())){
            qrPayFlowDTO.setFee(gateWayFee);
            qrPayFlowDTO.setRate(transChannelFeeRate);
            qrPayFlowDTO.setFeeDirection(direction);
            qrPayFlowDTO.setPayAmount(payAmount.add(gateWayFee));
        }

        qrPayFlowDTO.setIsNeedClear(StaticDataEnum.NEED_CLEAR_TYPE_1.getCode());
        qrPayFlowDTO.setClearState(StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        qrPayFlowDTO.setClearAmount(recAmount);

        //返回信息封装
        HashMap<String, Object> resultMap = Maps.newHashMapWithExpectedSize(13);

        //创建订单时间格式化
        Long orderCreatedDate = qrPayFlowDTO.getCreatedDate();
        SimpleDateFormat monthFormat = new SimpleDateFormat("dd MMM", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        String monthStr = monthFormat.format(orderCreatedDate);
        String timeStr = timeFormat.format(orderCreatedDate);
        resultMap.put("orderCreatedDate", monthStr + " at " + timeStr);

        resultMap.put("transAmount", transAmount.toString());
        resultMap.put("transNo", qrPayFlowDTO.getTransNo());
        resultMap.put("payAmount", qrPayFlowDTO.getPayAmount().toString());
        resultMap.put("totalAmount", tempCardPayAllAmount.toString());

        resultMap.put("merchantName", merchantDTO.getPracticalName());
        resultMap.put("payType", payType);
        resultMap.put("useRedEnvelopeAmount", redEnvelopeAmount.toString());

        resultMap.put("donationAmount", donationAmount.toString());

        resultMap.put("tipAmount", tipAmount.toString());

        if(qrPayDTO.getPayType().equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){
            //分期付
            resultMap.put("creditNeedCardPayAmount", creditCardFirstPayAmount.add(donationAmount).add(tipAmount).add(gateWayFee).toString());
            resultMap.put("creditNeedCardPayNoFeeAmount", creditCardFirstPayAmount.toString());
            resultMap.put("remainingCreditAmount", remainingCreditPayAmount.toString());

            //todo  改为调用生成分期付订单接口返回
            int period = 0;
            //下期还款金额
            BigDecimal creditNextRepayAmount = BigDecimal.ZERO;

            if(remainingCreditPayAmount.compareTo(BigDecimal.ZERO) == 0){
                period = 1;
                creditNextRepayAmount = remainingCreditPayAmount;
            }else if(remainingCreditPayAmount.compareTo(new BigDecimal("20")) <= 0){
                period = 2;
                creditNextRepayAmount = remainingCreditPayAmount;
            }else if(remainingCreditPayAmount.compareTo(new BigDecimal("40")) <= 0){
                period = 3;
                creditNextRepayAmount = remainingCreditPayAmount.divide(new BigDecimal("2"), 2, RoundingMode.FLOOR);
            }else{
                period = 4;
                creditNextRepayAmount = remainingCreditPayAmount.divide(new BigDecimal("3"), 2, RoundingMode.FLOOR);
            }
            resultMap.put("period", period);
            resultMap.put("creditNextRepayAmount", creditNextRepayAmount.toString());
        }

        resultMap.put("cardPayFee", gateWayFee.toString());
        resultMap.put("cardPayRate", transChannelFeeRate.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
        resultMap.put("cardPayRateReal", transChannelFeeRate);

        resultMap.put("cardNo", cardDTO.getCardNo());
        resultMap.put("cardId", cardDTO.getId().toString());
        resultMap.put("cardCcType", cardDTO.getCustomerCcType());

        resultMap.put("wholeSaleUserDiscountAmount", wholeSaleDiscountAmount.toString());
        resultMap.put("wholeSalesUserDiscount", realWholeSaleDiscount.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        resultMap.put("normalSaleUserDiscountAmount", normalSaleDiscountAmount.toString());
        resultMap.put("normalSalesUserDiscount", (realBaseDiscount.add(realExtraDiscount).add(realMarkingDiscount)).multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());



        return resultMap;
    }



    /**
     * 校验并获取 支付通道、卡信息
     * @author zhangzeyuan
     * @date 2021/6/24 16:14
     * @param gatewayPayType 支付类型
     * @param cardId 卡ID
     * @param qrPayFlowDTO
     * @param request
     * tail
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    private Map<String, Object>  verifyAndGetPayGatewayCardInfo(Integer gatewayPayType, Long cardId, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws Exception{
        HashMap<String, Object> resultMap = Maps.newHashMapWithExpectedSize(3);

        //获取商户路由配置
        resultMap.put("gatewayType", gatewayPayType);
        resultMap.put("state", StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(resultMap);
        // todo 挡板
        resultMap.clear();
        if(null == gatewayDTO || null == gatewayDTO.getId()){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        resultMap.put("gatewayDTO", gatewayDTO);

        //路由支付通道类型
        Long gatewayType = gatewayDTO.getType();
        //获取卡信息
        JSONObject cardJsonObj;
        CardDTO cardDTO;
        try {
            cardJsonObj = serverService.getCardInfo(cardId);

            if (cardJsonObj == null || StringUtils.isBlank(cardJsonObj.toJSONString())) {
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            cardDTO = JSONObject.parseObject(cardJsonObj.toJSONString(), CardDTO.class);

        } catch (Exception e) {
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        resultMap.put("cardDTO", cardDTO);
        resultMap.put("cardJsonObj", cardJsonObj);

        //校验三方渠道
        if(gatewayType.intValue() == StaticDataEnum.GATEWAY_TYPE_0.getCode()){
            //如果是LatPay，判断卡表token不为空，
            if(StringUtils.isBlank(cardDTO.getCrdStrgToken())){
                throw new BizException(I18nUtils.get("channel.token.missing", getLang(request)));
            }
        }else if(gatewayType.intValue() == StaticDataEnum.GATEWAY_TYPE_4.getCode()){
            //如果是integrapay 判断uniqueReference不为空
            if(cardDTO.getUniqueReference() == null){
                throw new BizException(I18nUtils.get("channel.token.missing", getLang(request)));
            }
        }else if(gatewayType.intValue() == StaticDataEnum.GATEWAY_TYPE_8.getCode()){
            //Stripe 判断StripeToken不为空
            if(cardDTO.getStripeToken() == null){
                throw new BizException(I18nUtils.get("channel.token.missing", getLang(request)));
            }
        } else {
                //未知的渠道
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        // 用户和卡交易限制查询
        userService.verifyCardFailedTime(qrPayFlowDTO.getPayUserId(), cardId,request);

        qrPayFlowDTO.setCardId(cardId.toString());
        qrPayFlowDTO.setGatewayId(gatewayType.longValue());
        return resultMap;
    }



    /**
     * 卡支付发往三方请求
     * @author zhangzeyuan
     * @date 2021/7/1 9:58
     * @param qrPayFlowDTO
     * @param withholdFlowDTO
     * @param cardObj
     * @param request
     */
    private void sendCardPayLatPayRequest(QrPayFlowDTO qrPayFlowDTO,  WithholdFlowDTO withholdFlowDTO,
                                          JSONObject cardObj, HttpServletRequest request){
        if(withholdFlowDTO.getTransAmount().compareTo(BigDecimal.ZERO) > 0){
            //渠道交易请求
            try{
                // LatPay Request
                withholdFlowDTO = latPayService.latPayRequest(withholdFlowDTO,withholdFlowDTO.getTransAmount(),qrPayFlowDTO.getPlatformFee(),cardObj,request, qrPayFlowDTO.getPayUserIp());
            }catch (Exception e){
                log.error("发送latpay请求异常:"+e.getMessage(),e);
                //置为可疑
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            }
        }else{
            //交易金额为0处理
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            withholdFlowDTO.setCheckState( StaticDataEnum.IS_CHECK_1.getCode());
            withholdFlowDTO.setOrdreNo(null);
        }
    }


    /**
     * 卡支付 根据三方状态进行交易处理
     * @author zhangzeyuan
     * @date 2021/7/1 9:58
     * @param withholdFlowDTO
     * @param qrPayFlowDTO
     * @param request
     */
    @Override
    public void handleCardLatPayPostResult(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO, JSONObject cardObj,
                                           HttpServletRequest request) throws Exception{
        Integer status = withholdFlowDTO.getState();
        //判断三方交易状态
        if (status.equals(StaticDataEnum.TRANS_STATE_1.getCode())) {
            //成功
            handleLatPaySuccess(withholdFlowDTO, qrPayFlowDTO, request);

        } else if (status.equals(StaticDataEnum.TRANS_STATE_2.getCode())) {
            log.error("三方返回状态失败");
            //失败
            handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, qrPayFlowDTO.getPayAmount(), null, request);

            if(request != null){
                // 返回错误码为银行失败
                String returnMessage = withholdFlowDTO.getReturnMessage();
                if(StaticDataEnum.LAT_PAY_BANK_STATUS_5.getMessage().equals(withholdFlowDTO.getReturnCode())){
                    if(LatPayErrorEnum.BANK_CODE_05.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_05.getMessage());
                    }else if(LatPayErrorEnum.BANK_CODE_06.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_06.getMessage());
                    }else if(LatPayErrorEnum.BANK_CODE_07.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_07.getMessage());
                    }else if(LatPayErrorEnum.BANK_CODE_41.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_41.getMessage());
                    }else if(LatPayErrorEnum.BANK_CODE_43.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_43.getMessage());
                    }else if(LatPayErrorEnum.BANK_CODE_51.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_51.getMessage());
                    }else{
                        throw new BizException(LatPayErrorEnum.BANK_CODE_OTHER.getMessage());
                    }
                }else if (StaticDataEnum.LP_RESPONSE_TYPE_0.getMessage().equals(withholdFlowDTO.getReturnCode())){
                    // 返回信息为反欺诈系统
                    if(LatPayErrorEnum.SCSS_CODE_1003.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.SCSS_CODE_1003.getMessage());
                    }else if(LatPayErrorEnum.SCSS_CODE_1004.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.SCSS_CODE_1004.getMessage());
                    }else if(LatPayErrorEnum.SCSS_CODE_1005.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.SCSS_CODE_1005.getMessage());
                    }else if(LatPayErrorEnum.SCSS_CODE_1006.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.SCSS_CODE_1006.getMessage());
                    }else{
                        throw new BizException(LatPayErrorEnum.SCSS_CODE_OTHER.getMessage());
                    }
                }else{
                    throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
                }
            }else{
                throw new BizException("We couldn't process your payment. Please try again.");
            }
        } else if(status.equals(StaticDataEnum.TRANS_STATE_3.getCode())){
            //可疑
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
            updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
            throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
        }
    }

    /**
     * 卡支付 根据三方状态进行交易处理
     * @author zhangzeyuan
     * @date 2021/7/1 9:58
     * @param withholdFlowDTO
     * @param qrPayFlowDTO
     * @param request
     */
    public void handleCardLatPayPostResultV3(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO, JSONObject cardObj,
                                           HttpServletRequest request) throws Exception{
        Integer status = withholdFlowDTO.getState();
        //判断三方交易状态
        if (status.equals(StaticDataEnum.TRANS_STATE_1.getCode())) {
            //成功
            handleLatPaySuccess(withholdFlowDTO, qrPayFlowDTO, request);
            // 设置卡支付成功标识
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("cardId", qrPayFlowDTO.getCardId());

            if(null != qrPayFlowDTO.getMarketingId() && null != qrPayFlowDTO.getMarketingManageId()){
                //增加使用次数
                HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
                paramMap.put("id", qrPayFlowDTO.getMarketingManageId());
                marketingManagementService.addUsedNumber(paramMap);
            }

            try{
                if(null != cardObj.getInteger("payState") && cardObj.getInteger("payState") == StaticDataEnum.STATUS_0.getCode()){
                    serverService.setCardSuccessPay(jsonObject);
                }
            }catch (Exception e){
                log.error("设置卡支付成功标识,userId:{},e:{},",getUserId(request),e.getMessage() ,e);
            }

            // 如果是stripe 的用户需要将用户弹窗标识修改
            /*if(qrPayFlowDTO.getGatewayId() == StaticDataEnum.GATEWAY_TYPE_8.getCode()){
                UserDTO userDTO = userService.findUserById(qrPayFlowDTO.getPayUserId());
                if(userDTO.getStripeState() == StaticDataEnum.STATUS_1.getCode()){
                    userDTO.setStripeState(StaticDataEnum.STATUS_0.getCode());
                }
            }*/


        } else if (status.equals(StaticDataEnum.TRANS_STATE_2.getCode())) {
            log.error("三方返回状态失败");
            // 失败
            qrPayFlowDTO = handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, qrPayFlowDTO.getPayAmount(), cardObj != null ? cardObj.getInteger("payState") : null, request);

            Integer deleteCardState = qrPayFlowDTO.getDeleteCardState();
            Integer cardCount = qrPayFlowDTO.getCardCount();
            if(request != null){
                // 返回错误码为银行失败
                String returnMessage = withholdFlowDTO.getReturnMessage();
                if(StaticDataEnum.LAT_PAY_BANK_STATUS_5.getMessage().equals(withholdFlowDTO.getReturnCode())){
                    if(LatPayErrorEnum.BANK_CODE_05.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.BANK_CODE_05.getMessage(), deleteCardState, cardCount);
                    }else if(LatPayErrorEnum.BANK_CODE_06.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.BANK_CODE_06.getMessage(), deleteCardState, cardCount);
                    }else if(LatPayErrorEnum.BANK_CODE_07.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.BANK_CODE_07.getMessage(), deleteCardState, cardCount);
                    }else if(LatPayErrorEnum.BANK_CODE_41.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.BANK_CODE_41.getMessage(), deleteCardState, cardCount);
                    }else if(LatPayErrorEnum.BANK_CODE_43.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.BANK_CODE_43.getMessage(), deleteCardState, cardCount);
                    }else if(LatPayErrorEnum.BANK_CODE_51.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.BANK_CODE_51.getMessage(), deleteCardState, cardCount);
                    }else{
                        throw new LatpayFailedException(LatPayErrorEnum.BANK_CODE_OTHER.getMessage(), deleteCardState, cardCount);
                    }
                }else if (StaticDataEnum.LP_RESPONSE_TYPE_0.getMessage().equals(withholdFlowDTO.getReturnCode())){
                    // 返回信息为反欺诈系统
                    if(LatPayErrorEnum.SCSS_CODE_1003.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.SCSS_CODE_1003.getMessage(), deleteCardState, cardCount);
                    }else if(LatPayErrorEnum.SCSS_CODE_1004.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.SCSS_CODE_1004.getMessage(), deleteCardState, cardCount);
                    }else if(LatPayErrorEnum.SCSS_CODE_1005.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.SCSS_CODE_1005.getMessage(), deleteCardState, cardCount);
                    }else if(LatPayErrorEnum.SCSS_CODE_1006.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.SCSS_CODE_1006.getMessage(), deleteCardState, cardCount);
                    }else{
                        throw new LatpayFailedException(LatPayErrorEnum.SCSS_CODE_OTHER.getMessage(), deleteCardState, cardCount);
                    }
                }else{
                    throw new LatpayFailedException(I18nUtils.get("trans.failed", getLang(request)), deleteCardState, cardCount);
                }
            }else{
                throw new BizException("We couldn't process your payment. Please try again.");
            }
        } else if(status.equals(StaticDataEnum.TRANS_STATE_3.getCode())){
            //可疑
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
            updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
            throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
        }else {
            // 3DS验证
            updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
        }
    }


    /**
     * 分期付 25%卡支付 根据三方状态进行交易处理
     * @author zhangzeyuan
     * @date 2021/7/1 9:59
     * @param withholdFlowDTO
     * @param qrPayFlowDTO
     * @param cardObj
     * @param creditNeedCardPayAmount   25%金额 + 捐赠费+ 消费 + 手续费
     * @param creditNeedCardPayNoFeeAmount    25%金额
     * @param remainingCreditAmount  剩余分期金额
     * @param cardPayRate
     * @param cardPayFee
     * @param request
     */
    public QrPayFlowDTO handleCreditCardPayDataByThirdStatusV3(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO, JSONObject cardObj,
                                                     BigDecimal creditNeedCardPayAmount, BigDecimal creditNeedCardPayNoFeeAmount, BigDecimal remainingCreditAmount,
                                                     BigDecimal cardPayRate, BigDecimal cardPayFee, HttpServletRequest request) throws Exception{
        Integer status = withholdFlowDTO.getState();

        //判断三方交易状态
        if (status.equals(StaticDataEnum.TRANS_STATE_1.getCode())) {
            //发送三方成功
            if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){

                CreateCreditOrderFlowDTO createCreditOrderFlowDTO = new CreateCreditOrderFlowDTO();
                createCreditOrderFlowDTO.setQrPayFlowId(qrPayFlowDTO.getId());
                createCreditOrderFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
                createCreditOrderFlowDTO.setState(StaticDataEnum.CREATE_CREDIT_ORDER_FLOW_STATE_0.getCode());
                createCreditOrderFlowDTO.setCardPayRate(cardPayRate);
                createCreditOrderFlowDTO.setCardFeeAmount(cardPayFee);
                createCreditOrderFlowDTO.setCardPayAmount(creditNeedCardPayNoFeeAmount);
                createCreditOrderFlowDTO.setCardAccountName(cardObj.getString("accountName"));
                createCreditOrderFlowDTO.setCardNo(cardObj.getString("cardNo"));
                Long createCreditOrderFlowId = createCreditOrderFlowService.saveCreateCreditOrderFlow(createCreditOrderFlowDTO, request);

                //生成分期付订单
                Integer orderState = createCreditInstallmentOrder(qrPayFlowDTO, cardPayRate, creditNeedCardPayNoFeeAmount, cardPayFee, creditNeedCardPayAmount, cardObj, request);

                //生成分期付订单成功
                if(orderState.equals(StaticDataEnum.API_ORDER_STATE_1.getCode())){

                    //更改流水记录表状态为成功
                    createCreditOrderFlowDTO.setState(StaticDataEnum.CREATE_CREDIT_ORDER_FLOW_STATE_1.getCode());
                    createCreditOrderFlowService.updateCreateCreditOrderFlow(createCreditOrderFlowId, createCreditOrderFlowDTO, request);
                    //成功相关操作处理
                    handleLatPaySuccess(withholdFlowDTO, qrPayFlowDTO, request);

                    try{
                        JSONObject jsonObject = new JSONObject();
                        // 更改该卡为默认卡 设置卡支付成功标识
                        jsonObject.put("cardId", qrPayFlowDTO.getCardId());
                        jsonObject.put("payState",StaticDataEnum.STATUS_1.getCode());
                        userService.presetCard(jsonObject,qrPayFlowDTO.getPayUserId(), request);
                    }catch (Exception e){
                        log.error("分期付第一次卡支付设置默认卡失败,e:{},userId",e,getUserId(request));
                    }

                    if(null != qrPayFlowDTO.getMarketingId() && null != qrPayFlowDTO.getMarketingManageId()){
                        //增加使用次数
                        HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
                        paramMap.put("id", qrPayFlowDTO.getMarketingManageId());
                        marketingManagementService.addUsedNumber(paramMap);
                    }
                }else{
                    //没有成功 可疑/失败 跑批处理
                    createCreditOrderFlowDTO.setState(StaticDataEnum.CREATE_CREDIT_ORDER_FLOW_STATE_3.getCode());
                    createCreditOrderFlowService.updateCreateCreditOrderFlow(createCreditOrderFlowId, createCreditOrderFlowDTO, request);

                    //将订单类型改为交易成功
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
                    updateFlowForConcurrency(qrPayFlowDTO, null, null, request);
                    return qrPayFlowDTO;
                }
            }else {
                if(withholdFlowDTO.getTransAmount().compareTo(BigDecimal.ZERO) > 0){
                    //设置默认卡
                    // 更改该卡为默认卡
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("cardId", qrPayFlowDTO.getCardId());
                    // 设置卡支付成功标识
                    jsonObject.put("payState",StaticDataEnum.STATUS_1.getCode());
                    try{
                        userService.presetCard(jsonObject,qrPayFlowDTO.getPayUserId(), request);
                    }catch (Exception e){
                        log.error("分期付第一次卡支付设置默认卡失败,e:{},userId",e,getUserId(request));
                    }
                }
                //实付金额为0  不生成分期付订单
                // 成功处理
                handleLatPaySuccess(withholdFlowDTO, qrPayFlowDTO, request);
            }

            //POS API 交易成功回调通知
            /*try {
                log.info("开始进行订单支付成功通知");
                posApiService.posPaySuccessNotice(qrPayFlowDTO.getMerchantId(), qrPayFlowDTO.getTransNo(), request);
            }catch (PosApiException e){
                log.error("POS通知失败" + e.getMessage());
            }*/

            //分期付发送交易记录 邮件 todo 异步
            try{
                sendTransactionMail(qrPayFlowDTO.getCreditOrderNo(), request);
            }catch (Exception e){
                log.error("分期付发送交易记录邮件失败, CreditOrderNo:" + qrPayFlowDTO.getCreditOrderNo() + " || message:" + e.getMessage());
            }
        } else if (status.equals(StaticDataEnum.TRANS_STATE_2.getCode())) {
            // 发送三方失败
            // 失败的如果没有传入卡信息，需要查询
            if( cardObj != null && cardObj.getInteger("payState") == null && qrPayFlowDTO.getCardId()!= null){
                //获取卡信息
                cardObj.clear();
                try {
                    cardObj = serverService.getCardInfo(Long.parseLong(qrPayFlowDTO.getCardId()));
                } catch (Exception e) {
                    log.error("交易失败，获取卡信息失败,flow_id:"+qrPayFlowDTO.getId()+",error:"+e.getMessage(),e);
//                    throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
                }
            }

            if (cardObj == null || StringUtils.isBlank(cardObj.toJSONString())) {
                // 如果卡已经删除
                qrPayFlowDTO = handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, withholdFlowDTO.getOrderAmount(), null,request);
            }else{
                qrPayFlowDTO = handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, withholdFlowDTO.getOrderAmount(), cardObj.getInteger("payState"),request);
            }

            Integer deleteCardState = qrPayFlowDTO.getDeleteCardState();
            Integer cardCount = qrPayFlowDTO.getCardCount();
            if(request != null){
                // 返回错误码为银行失败
                String returnMessage = withholdFlowDTO.getReturnMessage();
                if(StaticDataEnum.LAT_PAY_BANK_STATUS_5.getMessage().equals(withholdFlowDTO.getReturnCode())){
                    if(LatPayErrorEnum.BANK_CODE_05.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.BANK_CODE_05.getMessage(), deleteCardState, cardCount);
                    }else if(LatPayErrorEnum.BANK_CODE_06.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.BANK_CODE_06.getMessage(), deleteCardState, cardCount);
                    }else if(LatPayErrorEnum.BANK_CODE_07.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.BANK_CODE_07.getMessage(), deleteCardState, cardCount);
                    }else if(LatPayErrorEnum.BANK_CODE_41.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.BANK_CODE_41.getMessage(), deleteCardState, cardCount);
                    }else if(LatPayErrorEnum.BANK_CODE_43.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.BANK_CODE_43.getMessage(), deleteCardState, cardCount);
                    }else if(LatPayErrorEnum.BANK_CODE_51.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.BANK_CODE_51.getMessage(), deleteCardState, cardCount);
                    }else{
                        throw new LatpayFailedException(LatPayErrorEnum.BANK_CODE_OTHER.getMessage(), deleteCardState, cardCount);
                    }
                }else if (StaticDataEnum.LP_RESPONSE_TYPE_0.getMessage().equals(withholdFlowDTO.getReturnCode())){
                    // 返回信息为反欺诈系统
                    if(LatPayErrorEnum.SCSS_CODE_1003.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.SCSS_CODE_1003.getMessage(), deleteCardState, cardCount);
                    }else if(LatPayErrorEnum.SCSS_CODE_1004.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.SCSS_CODE_1004.getMessage(), deleteCardState, cardCount);
                    }else if(LatPayErrorEnum.SCSS_CODE_1005.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.SCSS_CODE_1005.getMessage(), deleteCardState, cardCount);
                    }else if(LatPayErrorEnum.SCSS_CODE_1006.getCode().equals(returnMessage)){
                        throw new LatpayFailedException(LatPayErrorEnum.SCSS_CODE_1006.getMessage(), deleteCardState, cardCount);
                    }else{
                        throw new LatpayFailedException(LatPayErrorEnum.SCSS_CODE_OTHER.getMessage(), deleteCardState, cardCount);
                    }
                }else{
                    throw new LatpayFailedException(I18nUtils.get("trans.failed", getLang(request)), deleteCardState, cardCount);
                }
            }else{
                throw new BizException("We couldn't process your payment. Please try again.");
            }
        }else if(status.equals(StaticDataEnum.TRANS_STATE_3.getCode())){
            //可疑
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
            updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
            throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
        }
        return  qrPayFlowDTO;
    }


    @Override
    public void handleCreditCardPayDataByThirdStatus(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO, JSONObject cardObj,
                                                     BigDecimal creditNeedCardPayAmount, BigDecimal creditNeedCardPayNoFeeAmount, BigDecimal remainingCreditAmount,
                                                     BigDecimal cardPayRate, BigDecimal cardPayFee, HttpServletRequest request) throws Exception{
        Integer status = withholdFlowDTO.getState();

        //判断三方交易状态
        if (status.equals(StaticDataEnum.TRANS_STATE_1.getCode())) {
            //发送三方成功
            if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                CreateCreditOrderFlowDTO createCreditOrderFlowDTO = new CreateCreditOrderFlowDTO();
                createCreditOrderFlowDTO.setQrPayFlowId(qrPayFlowDTO.getId());
                createCreditOrderFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
                createCreditOrderFlowDTO.setState(StaticDataEnum.CREATE_CREDIT_ORDER_FLOW_STATE_0.getCode());
                createCreditOrderFlowDTO.setCardPayRate(cardPayRate);
                createCreditOrderFlowDTO.setCardFeeAmount(cardPayFee);
                createCreditOrderFlowDTO.setCardPayAmount(creditNeedCardPayNoFeeAmount);
                createCreditOrderFlowDTO.setCardAccountName(cardObj.getString("accountName"));
                createCreditOrderFlowDTO.setCardNo(cardObj.getString("cardNo"));
                Long createCreditOrderFlowId = createCreditOrderFlowService.saveCreateCreditOrderFlow(createCreditOrderFlowDTO, request);

                //生成分期付订单
                Integer orderState = createCreditInstallmentOrder(qrPayFlowDTO, cardPayRate, creditNeedCardPayNoFeeAmount, cardPayFee, creditNeedCardPayAmount, cardObj, request);

                //生成分期付订单成功
                if(orderState.equals(StaticDataEnum.API_ORDER_STATE_1.getCode())){

                    //更改流水记录表状态为成功
                    createCreditOrderFlowDTO.setState(StaticDataEnum.CREATE_CREDIT_ORDER_FLOW_STATE_1.getCode());
                    createCreditOrderFlowService.updateCreateCreditOrderFlow(createCreditOrderFlowId, createCreditOrderFlowDTO, request);
                    //成功相关操作处理
                    handleLatPaySuccess(withholdFlowDTO, qrPayFlowDTO, request);

                    //更改该卡为默认卡
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("cardId", qrPayFlowDTO.getCardId());
                    try{
                        userService.presetCard(jsonObject, qrPayFlowDTO.getPayUserId(), request);
                    }catch (Exception e){
                        log.error("分期付第一次卡支付设置默认卡失败,e:{},userId",e,getUserId(request));
                    }

                }else{
                    //没有成功 可疑/失败 跑批处理
                    createCreditOrderFlowDTO.setState(StaticDataEnum.CREATE_CREDIT_ORDER_FLOW_STATE_3.getCode());
                    createCreditOrderFlowService.updateCreateCreditOrderFlow(createCreditOrderFlowId, createCreditOrderFlowDTO, request);

                    //将订单类型改为交易成功
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
                    updateFlowForConcurrency(qrPayFlowDTO, null, null, request);
                    return;
                }
            }else {
                if(withholdFlowDTO.getTransAmount().compareTo(BigDecimal.ZERO) > 0){
                    // 更改该卡为默认卡
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("cardId", qrPayFlowDTO.getCardId());
                    try{
                        userService.presetCard(jsonObject,qrPayFlowDTO.getPayUserId(), request);
                    }catch (Exception e){
                        log.error("分期付第一次卡支付设置默认卡失败,e:{},userId",e,getUserId(request));
                    }
                }
                //实付金额为0  不生成分期付订单
                // 成功处理
                handleLatPaySuccess(withholdFlowDTO, qrPayFlowDTO, request);
            }

            //POS API 交易成功回调通知
            /*try {
                log.info("开始进行订单支付成功通知");
                posApiService.posPaySuccessNotice(qrPayFlowDTO.getMerchantId(), qrPayFlowDTO.getTransNo(), request);
            }catch (PosApiException e){
                log.error("POS通知失败" + e.getMessage());
            }*/

            //分期付发送交易记录 邮件 todo 异步
            try{
                sendTransactionMail(qrPayFlowDTO.getCreditOrderNo(), request);
            }catch (Exception e){
                log.error("分期付发送交易记录邮件失败, CreditOrderNo:" + qrPayFlowDTO.getCreditOrderNo() + " || message:" + e.getMessage());
            }
        } else if (status.equals(StaticDataEnum.TRANS_STATE_2.getCode())) {
            //发送三方失败
            //失败处理
            handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, withholdFlowDTO.getOrderAmount(), null, request);
            if(request != null){
                // 返回错误码为银行失败
                String returnMessage = withholdFlowDTO.getReturnMessage();
                if(StaticDataEnum.LAT_PAY_BANK_STATUS_5.getMessage().equals(withholdFlowDTO.getReturnCode())){
                    if(LatPayErrorEnum.BANK_CODE_05.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_05.getMessage());
                    }else if(LatPayErrorEnum.BANK_CODE_06.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_06.getMessage());
                    }else if(LatPayErrorEnum.BANK_CODE_07.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_07.getMessage());
                    }else if(LatPayErrorEnum.BANK_CODE_41.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_41.getMessage());
                    }else if(LatPayErrorEnum.BANK_CODE_43.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_43.getMessage());
                    }else if(LatPayErrorEnum.BANK_CODE_51.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_51.getMessage());
                    }else{
                        throw new BizException(LatPayErrorEnum.BANK_CODE_OTHER.getMessage());
                    }
                }else if (StaticDataEnum.LP_RESPONSE_TYPE_0.getMessage().equals(withholdFlowDTO.getReturnCode())){
                    // 返回信息为反欺诈系统
                    if(LatPayErrorEnum.SCSS_CODE_1003.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.SCSS_CODE_1003.getMessage());
                    }else if(LatPayErrorEnum.SCSS_CODE_1004.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.SCSS_CODE_1004.getMessage());
                    }else if(LatPayErrorEnum.SCSS_CODE_1005.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.SCSS_CODE_1005.getMessage());
                    }else if(LatPayErrorEnum.SCSS_CODE_1006.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.SCSS_CODE_1006.getMessage());
                    }else{
                        throw new BizException(LatPayErrorEnum.SCSS_CODE_OTHER.getMessage());
                    }
                }else{
                    throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
                }
            }else{
                throw new BizException("We couldn't process your payment. Please try again.");
            }
        }else if(status.equals(StaticDataEnum.TRANS_STATE_3.getCode())){
            //可疑
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
            updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
            throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
        }
    }
    /**
     *  卡支付三方成功处理
     * @author zhangzeyuan
     * @date 2021/6/28 0:17
     * @param withholdFlowDTO
     * @param qrPayFlowDTO
     * @param request
     */
    private void handleLatPaySuccess(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException{
        //清楚用户卡失败次数
        if((qrPayFlowDTO.getPayAmount().add(qrPayFlowDTO.getTipAmount()).add(qrPayFlowDTO.getDonationAmount())).compareTo(BigDecimal.ZERO) > 0){
            try {
                userService.logCardFailedTime(qrPayFlowDTO.getPayUserId(), Long.parseLong(qrPayFlowDTO.getCardId()),true,request);
            }catch ( Exception e){
                log.error("卡支付交易成功后，清除失败次数失败，flowId："+withholdFlowDTO.getFlowId()+",exception:"+e.getMessage(),e);
            }
        }

        // 不需要进行清算和清算金额为0的交易

        if(qrPayFlowDTO.getIsNeedClear() == StaticDataEnum.NEED_CLEAR_TYPE_0.getCode() || qrPayFlowDTO.getRecAmount().compareTo(BigDecimal.ZERO) == 0
                || qrPayFlowDTO.getOrderSource() == StaticDataEnum.ORDER_SOURCE_1.getCode()){
            //更新交易状态为成功
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
            updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
        }else{
            //需要进行清算

            // 当交易状态不是31时，为正常交易，需要改变交易状态为入账处理中
            // 当状态为31时为跑批处理，不改变交易状态
            if(qrPayFlowDTO.getState() != StaticDataEnum.TRANS_STATE_31.getCode() ){
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_30.getCode());
                updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
                //发送成功消息
                sendMessage(qrPayFlowDTO.getRecUserId(), qrPayFlowDTO.getId(), request);
            }

            //创建账户交易流水
            AccountFlowDTO accountFlowDTO = getAccountFlowDTO(qrPayFlowDTO);
            //保存账户交易流水
            accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO, request));
            try {
                doAmountTrans(accountFlowDTO, qrPayFlowDTO, request);
            } catch (Exception e) {
                log.error("qrPay dealThirdSuccess Exception:"+e.getMessage(),e);
                //交易失败
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                updateFlow(qrPayFlowDTO, null, null, request);
            }
        }

        if(qrPayFlowDTO.getState() == StaticDataEnum.TRANS_STATE_31.getCode() && qrPayFlowDTO.getOrderSource() != StaticDataEnum.ORDER_SOURCE_1.getCode()){
            // 更新交易状态为成功
            //qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
            //updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
            // 查询是否是正常交易
            if(qrPayFlowDTO.getNormalSaleAmount().compareTo(BigDecimal.ZERO) > 0){
                // 查询商户是否是 无整体出售状态
                MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayFlowDTO.getMerchantId());
                // 商户有整体出售余额
                if(merchantDTO.getHaveWholeSell() == StaticDataEnum.MERCHANT_WHOLE_SELL_AMOUNT_NOT_ZERO.getCode()){
                    try {
                        // 查询整体出售余额
                        BigDecimal merchantAmount = userService.getBalance(qrPayFlowDTO.getRecUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
                        // 余额等于0 时，更新为无整体出售
                        if(merchantAmount.compareTo(BigDecimal.ZERO) == 0){
                            merchantService.updateHaveWholeSell(qrPayFlowDTO.getMerchantId(),0,request);
                        }
                    }catch (Exception e){
                        log.error("dealThirdSuccess 整体出售状态变更失败 ，id:"+qrPayFlowDTO.getId());
                    }
                }
            }
        }

    }

    /**
     *  处理latpay返回失败结果
     * @author zhangzeyuan
     * @date 2021/7/1 14:36
     * @param withholdFlowDTO
     * @param qrPayFlowDTO
     * @param payState
     * @param request
     */
    private QrPayFlowDTO handleLatPayFailed(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO, BigDecimal channelLimtRollbackAmt, Integer payState, HttpServletRequest request) throws BizException{
        //清楚卡失败次数
        if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
            try {
                userService.logCardFailedTime(qrPayFlowDTO.getPayUserId(),Long.parseLong(qrPayFlowDTO.getCardId()),false,request);
            }catch (Exception e){
                log.error("卡支付交易增加失败次数累计错误，flowId:" + qrPayFlowDTO.getId());
            }
        }

        //限额回滚
        if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), withholdFlowDTO.getGatewayId(), channelLimtRollbackAmt, request);
        }

        //出账回滚
        if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 ||
                qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            doBatchAmountOutRollBack(qrPayFlowDTO,request);
        }

        //分期付额度解冻
        if(qrPayFlowDTO.getTransType().equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode())){
            BigDecimal creditRollbackAmount = qrPayFlowDTO.getPayAmount().add(qrPayFlowDTO.getDonationAmount()).add(qrPayFlowDTO.getTipAmount()).subtract(channelLimtRollbackAmt);
            if(creditRollbackAmount.compareTo(BigDecimal.ZERO) > 0){
                log.error("分期付额度回滚开始");
                creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId() ,creditRollbackAmount , request);
            }
        }

        // 卡券回退
        if(qrPayFlowDTO.getMarketingId() != null){
            oneMarketingRollback(qrPayFlowDTO,request);
        }

        //如果该卡是首次支付失败 删除卡
        if(payState != null && payState == StaticDataEnum.USER_CARD_STATE_0.getCode()){
            //删卡
            JSONObject unbindCardObj = new JSONObject();
            unbindCardObj.put("cardId", qrPayFlowDTO.getCardId());
            unbindCardObj.put("lang", 1);
            unbindCardObj.put("userId", qrPayFlowDTO.getPayUserId());
            try{
                // 删除卡
                JSONObject result = userService.cardUnbundling(unbindCardObj, request);
                if(result == null || result.getInteger("state") != StaticDataEnum.CARD_UNBUNDLING_STATE_4.getCode()){
                    qrPayFlowDTO.setDeleteCardState(0);
                }else{
                    qrPayFlowDTO.setDeleteCardState(1);
                }

                // 查询剩余卡数量
                if(qrPayFlowDTO.getGatewayId().intValue() == StaticDataEnum.GATEWAY_TYPE_8.getCode()){

                    qrPayFlowDTO.setCardCount(cardService.getStripeCardCount(qrPayFlowDTO.getPayUserId(), request));
                }else{
                    qrPayFlowDTO.setCardCount(cardService.getLatpayCardCount(qrPayFlowDTO.getPayUserId(), request));

                }
            }catch (Exception e){
                log.error("v3支付-删卡失败, userId:{}, card:{}", qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getCardId() + e.getMessage(),e);
            }

        }

        //更新交易失败状态
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_22.getCode());
        if(qrPayFlowDTO.getOrderSource() == StaticDataEnum.ORDER_SOURCE_1.getCode()){
            qrPayFlowDTO.setIsShow(StaticDataEnum.ORDER_SHOW_STATE_0.getCode());
        }
        updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);

        return qrPayFlowDTO;
    }


    /**
     * 分期付支付
     * @author zhangzeyuan
     * @date 2021/6/23 17:24
     * @param qrPayFlowDTO
     * @param qrPayDTO
     * @param payUser
     * @param recUser
     * @param request
     */
    private Map<String, Object> creditPay(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, UserDTO payUser,
                                          UserDTO recUser, HttpServletRequest request) throws Exception {
        //付款用户必须是已开通分期付并且不需要补充信息的
        if(!payUser.getInstallmentState().equals(StaticDataEnum.STATUS_1.getCode())
                || payUser.getSplitAddInfoState().equals(StaticDataEnum.STATUS_1.getCode())){
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //是否逾期
        Map<String, Object> userCreditInfoMap = getCreditUserAvailableAmountAndState(payUser.getId());

        //分期付可用额度
        BigDecimal creditAmount = (BigDecimal) userCreditInfoMap.get("creditAmount");

        //分期付用户状态
        Integer userState = (Integer) userCreditInfoMap.get("userState");

        //用户是否冻结
        if(userState.equals(StaticDataEnum.CREAT_USER_STATE_11.getCode())){
            throw new BizException(I18nUtils.get("pay.user.freeze", getLang(request)));
        }

        //设置交易类型
        qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());
        qrPayFlowDTO.setProductId(qrPayDTO.getProductId());

        //通道校验
        Map<String, Object> channelResultMap = verifyAndGetPayGatewayCardInfo(0, qrPayDTO.getCardId(), qrPayFlowDTO, request);

        GatewayDTO gatewayDTO = (GatewayDTO) channelResultMap.get("gatewayDTO");
        CardDTO cardDTO = (CardDTO) channelResultMap.get("cardDTO");
        JSONObject cardJsonObj = (JSONObject) channelResultMap.get("cardJsonObj");

        //交易金额校验
        Map<String, Object> amountResultMap = verifyAndGetPayAmount(qrPayFlowDTO, qrPayDTO, cardDTO, request);

        //卡支付总金额
        BigDecimal creditNeedCardPayAmount = new BigDecimal((String) amountResultMap.get("creditNeedCardPayAmount"));
        //卡支付金额 不带手续费
        BigDecimal creditNeedCardPayNoFeeAmount = new BigDecimal((String) amountResultMap.get("creditNeedCardPayNoFeeAmount"));
        //剩余分期付金额
        BigDecimal remainingCreditAmount = new BigDecimal((String) amountResultMap.get("remainingCreditAmount"));
        //卡支付手续费 费率
        BigDecimal cardPayRate = new BigDecimal((String) amountResultMap.get("cardPayRate"));
        BigDecimal cardPayFee = new BigDecimal((String) amountResultMap.get("cardPayFee"));

        //校验额度是否足够
        if(creditAmount.compareTo(BigDecimal.ZERO) <= 0 || creditAmount.compareTo(remainingCreditAmount) < 0){
            throw new BizException(I18nUtils.get("pay.credit.amount.insufficient", getLang(request)));
        }

        //限额组件
        if(creditNeedCardPayNoFeeAmount.compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimit(0L, creditNeedCardPayNoFeeAmount, request);
        }

        try{
            Long id = qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request);
            qrPayFlowDTO.setId(id);
        }catch (Exception e){
            //限额回滚
            if(creditNeedCardPayNoFeeAmount.compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), creditNeedCardPayNoFeeAmount,request);
            }
            throw new BizException(I18nUtils.get("pay.order.same", getLang(request)));
        }

        //捐赠金额
        BigDecimal donationAmount = new BigDecimal((String) amountResultMap.get("donationAmount"));
        if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
            // 封装捐赠流水
            DonationFlowDTO donationFlowDTO = new DonationFlowDTO();
            donationFlowDTO.setFlowId(qrPayFlowDTO.getId());
            donationFlowDTO.setUserId(payUser.getId());
            donationFlowDTO.setUserName(payUser.getUserFirstName() + " " + payUser.getUserLastName());
            donationFlowDTO.setInstituteId(Long.valueOf(qrPayDTO.getDonationInstiuteId()));
            donationFlowDTO.setAmount(donationAmount);
            donationFlowDTO.setSettlementAmount(donationAmount);
            donationFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_34.getCode());
            donationFlowDTO.setMerchantId(qrPayFlowDTO.getMerchantId());
            donationFlowService.saveDonationFlow(donationFlowDTO, request);
        }

        //生成小费流水记录
        BigDecimal tipAmount = qrPayDTO.getTipAmount();
        if(null != tipAmount && tipAmount.compareTo(BigDecimal.ZERO) > 0){
            // 封装小费流水
            TipFlowDTO tipFlowDTO = new TipFlowDTO();
            tipFlowDTO.setFlowId(qrPayFlowDTO.getId());
            tipFlowDTO.setMerchantId(qrPayFlowDTO.getMerchantId());
            tipFlowDTO.setSettlementAmount(tipAmount);
            tipFlowDTO.setTipAmount(tipAmount);
            tipFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_35.getCode());
            tipFlowDTO.setUserId(payUser.getId());
            tipFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
            tipFlowService.saveTipFlow(tipFlowDTO, request);
        }

        //账户出账
        if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 ||
                qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            doAccountOut(qrPayFlowDTO, gatewayDTO, request);
        }

        //冻结75%额度
        if(remainingCreditAmount.compareTo(BigDecimal.ZERO) > 0){
            //保存冻结额度流水
            PayCreditBalanceFlowDTO payCreditBalanceFlowDTO = new PayCreditBalanceFlowDTO();
            payCreditBalanceFlowDTO.setQrPayFlowId(qrPayFlowDTO.getId());
            payCreditBalanceFlowDTO.setOperateType(StaticDataEnum.PAY_BALANCE_FLOW_OPERATE_TYPE_1.getCode());
            payCreditBalanceFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
            payCreditBalanceFlowDTO.setCreditQuotaAmount(remainingCreditAmount);
            payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_0.getCode());
            Long id = payCreditBalanceFlowService.savePayCreditBalanceFlow(payCreditBalanceFlowDTO, request);

            boolean frozenResult = frozenCreditAmount(remainingCreditAmount, payUser.getId(), qrPayFlowDTO.getId(), qrPayFlowDTO.getId());

            if(!frozenResult){
                log.error("冻结用户金额失败");
                //出账回滚
                //限额 出账回滚
                creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);
                //记录冻结额度流水为失败
                payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_2.getCode());
                payCreditBalanceFlowService.updatePayCreditBalanceFlow(id, payCreditBalanceFlowDTO, request);

                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_1.getCode());
            payCreditBalanceFlowService.updatePayCreditBalanceFlow(id, payCreditBalanceFlowDTO, request);
        }

        //交易状态为三方初始状态
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
        updateFlow(qrPayFlowDTO, null, null, request);

        //25%卡支付调用卡支付

        //保存三方代扣交易流水
        WithholdFlowDTO withholdFlowDTO = null;
        try {
            QrPayFlowDTO creditCardPayFlowDTO = new QrPayFlowDTO();
            BeanUtils.copyProperties(qrPayFlowDTO, creditCardPayFlowDTO);
            creditCardPayFlowDTO.setPayAmount(creditNeedCardPayAmount);

            withholdFlowDTO = packageWithHoldFlow(creditCardPayFlowDTO, cardDTO, new BigDecimal((String) amountResultMap.get("cardPayFee")),  (BigDecimal) amountResultMap.get("cardPayRateReal"), gatewayDTO);
            withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        } catch (Exception e) {
            log.error("保存三方代扣交易流水失败:"+e.getMessage(),e);
            //保存三方流水失败
            //限额 出账回滚
            creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);

            //分期付额度回滚
            creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId(), remainingCreditAmount, request);

            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //发往三方请求
        sendCardPayLatPayRequest(qrPayFlowDTO, withholdFlowDTO, cardJsonObj, request);

        //根据三方返回状态进行处理
        handleCreditCardPayDataByThirdStatus(withholdFlowDTO, qrPayFlowDTO, cardJsonObj, creditNeedCardPayAmount, creditNeedCardPayNoFeeAmount,
                remainingCreditAmount, cardPayRate, cardPayFee, request);

        return amountResultMap;
    }

    /**
     * 分期付支付
     * @author zhouttt
     * @date 2021/10/29 10:24
     * @param qrPayFlowDTO
     * @param qrPayDTO
     * @param payUser
     * @param recUser
     * @param request
     */
    private Map<String, Object> creditPayV3(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, UserDTO payUser,
                                            UserDTO recUser, HttpServletRequest request) throws Exception {
        //付款用户必须是已开通分期付并且不需要补充信息的
        if(!payUser.getInstallmentState().equals(StaticDataEnum.STATUS_1.getCode())){
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //是否逾期
        Map<String, Object> userCreditInfoMap = getCreditUserAvailableAmountAndState(payUser.getId());

        //分期付可用额度
        BigDecimal creditAmount = (BigDecimal) userCreditInfoMap.get("creditAmount");

        //分期付用户状态
        Integer userState = (Integer) userCreditInfoMap.get("userState");

        //用户是否冻结
        if(userState.equals(StaticDataEnum.CREAT_USER_STATE_11.getCode())){
            throw new BizException(I18nUtils.get("pay.user.freeze", getLang(request)));
        }

        //设置交易类型
        qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());
        qrPayFlowDTO.setProductId(qrPayDTO.getProductId());

        //通道校验
        Map<String, Object> channelResultMap = verifyAndGetPayGatewayCardInfo(0, qrPayDTO.getCardId(), qrPayFlowDTO, request);

        GatewayDTO gatewayDTO = (GatewayDTO) channelResultMap.get("gatewayDTO");
        CardDTO cardDTO = (CardDTO) channelResultMap.get("cardDTO");
        JSONObject cardJsonObj = (JSONObject) channelResultMap.get("cardJsonObj");

        //交易金额校验
        Map<String, Object> amountResultMap = verifyAndGetPayAmountV3(qrPayFlowDTO, qrPayDTO, cardDTO, request);

        //卡支付总金额
        BigDecimal creditNeedCardPayAmount = new BigDecimal((String) amountResultMap.get("creditNeedCardPayAmount"));
        //卡支付金额 不带手续费
        BigDecimal creditNeedCardPayNoFeeAmount = new BigDecimal((String) amountResultMap.get("creditNeedCardPayNoFeeAmount"));
        //剩余分期付金额
        BigDecimal remainingCreditAmount = new BigDecimal((String) amountResultMap.get("remainingCreditAmount"));
        //卡支付手续费 费率
        BigDecimal cardPayRate = new BigDecimal((String) amountResultMap.get("cardPayRate"));
        BigDecimal cardPayFee = new BigDecimal((String) amountResultMap.get("cardPayFee"));

        //校验额度是否足够
        if(creditAmount.compareTo(BigDecimal.ZERO) <= 0 || creditAmount.compareTo(remainingCreditAmount) < 0){
            throw new BizException(I18nUtils.get("pay.credit.amount.insufficient", getLang(request)));
        }

        //限额组件
        if(creditNeedCardPayNoFeeAmount.compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimit(0L, creditNeedCardPayNoFeeAmount, request);
        }

        try{
            Long id = qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request);
            qrPayFlowDTO.setId(id);
        }catch (Exception e){
            //限额回滚
            if(creditNeedCardPayNoFeeAmount.compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), creditNeedCardPayNoFeeAmount,request);
            }
            throw new BizException(I18nUtils.get("pay.order.same", getLang(request)));
        }

        //如果有整体出售，进行出账
        if(qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            doAccountOut(qrPayFlowDTO, gatewayDTO, request);
        }

        //冻结75%额度
        if(remainingCreditAmount.compareTo(BigDecimal.ZERO) > 0){
            //保存冻结额度流水
            PayCreditBalanceFlowDTO payCreditBalanceFlowDTO = new PayCreditBalanceFlowDTO();
            payCreditBalanceFlowDTO.setQrPayFlowId(qrPayFlowDTO.getId());
            payCreditBalanceFlowDTO.setOperateType(StaticDataEnum.PAY_BALANCE_FLOW_OPERATE_TYPE_1.getCode());
            payCreditBalanceFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
            payCreditBalanceFlowDTO.setCreditQuotaAmount(remainingCreditAmount);
            payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_0.getCode());
            Long id = payCreditBalanceFlowService.savePayCreditBalanceFlow(payCreditBalanceFlowDTO, request);

            boolean frozenResult = frozenCreditAmount(remainingCreditAmount, payUser.getId(), qrPayFlowDTO.getId(), qrPayFlowDTO.getId());

            if(!frozenResult){
                log.error("冻结用户金额失败");
                //出账回滚
                //限额 出账回滚
                creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);
                //记录冻结额度流水为失败
                payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_2.getCode());
                payCreditBalanceFlowService.updatePayCreditBalanceFlow(id, payCreditBalanceFlowDTO, request);

                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_1.getCode());
            payCreditBalanceFlowService.updatePayCreditBalanceFlow(id, payCreditBalanceFlowDTO, request);
        }

        // 有使用卡券
        if(qrPayFlowDTO.getMarketingId() != null){
            boolean useMarketingResult = false;
            try{
                useMarketingResult = this.useOneMarketing(qrPayFlowDTO,request);
            }catch (Exception e){
                log.error("冻结用户金额失败");
                //出账回滚
                //限额 出账回滚
                creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);
                //记录冻结额度流水为失败
                creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId(), remainingCreditAmount, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            if(!useMarketingResult){
                //限额 出账回滚
                creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);
                //记录冻结额度流水为失败
                creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId(), remainingCreditAmount, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
        }

        //捐赠金额
        BigDecimal donationAmount = new BigDecimal((String) amountResultMap.get("donationAmount"));
        if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
            // 封装捐赠流水
            DonationFlowDTO donationFlowDTO = new DonationFlowDTO();
            donationFlowDTO.setFlowId(qrPayFlowDTO.getId());
            donationFlowDTO.setUserId(payUser.getId());
            donationFlowDTO.setUserName(payUser.getUserFirstName() + " " + payUser.getUserLastName());
            donationFlowDTO.setInstituteId(Long.valueOf(qrPayDTO.getDonationInstiuteId()));
            donationFlowDTO.setAmount(donationAmount);
            donationFlowDTO.setSettlementAmount(donationAmount);
            donationFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_34.getCode());
            donationFlowDTO.setMerchantId(qrPayFlowDTO.getMerchantId());
            donationFlowService.saveDonationFlow(donationFlowDTO, request);
        }

        //生成小费流水记录
        BigDecimal tipAmount = qrPayDTO.getTipAmount();
        if(null != tipAmount && tipAmount.compareTo(BigDecimal.ZERO) > 0){
            // 封装小费流水
            TipFlowDTO tipFlowDTO = new TipFlowDTO();
            tipFlowDTO.setFlowId(qrPayFlowDTO.getId());
            tipFlowDTO.setMerchantId(qrPayFlowDTO.getMerchantId());
            tipFlowDTO.setSettlementAmount(tipAmount);
            tipFlowDTO.setTipAmount(tipAmount);
            tipFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_35.getCode());
            tipFlowDTO.setUserId(payUser.getId());
            tipFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
            tipFlowService.saveTipFlow(tipFlowDTO, request);
        }

        //交易状态为三方初始状态
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
        updateFlow(qrPayFlowDTO, null, null, request);

        //25%卡支付调用卡支付

        //保存三方代扣交易流水
        WithholdFlowDTO withholdFlowDTO = null;
        try {
            QrPayFlowDTO creditCardPayFlowDTO = new QrPayFlowDTO();
            BeanUtils.copyProperties(qrPayFlowDTO, creditCardPayFlowDTO);
            creditCardPayFlowDTO.setPayAmount(creditNeedCardPayAmount);

            withholdFlowDTO = packageWithHoldFlow(creditCardPayFlowDTO, cardDTO, new BigDecimal((String) amountResultMap.get("cardPayFee")),  (BigDecimal) amountResultMap.get("cardPayRateReal"), gatewayDTO);
            withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        } catch (Exception e) {
            log.error("保存三方代扣交易流水失败:"+e.getMessage(),e);
            //保存三方流水失败
            //限额 出账回滚
            creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);

            //分期付额度回滚
            creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId(), remainingCreditAmount, request);
            //卡券回退
            oneMarketingRollback(qrPayFlowDTO,request);

            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }


        //发往三方请求
        sendCardPayLatPayRequest(qrPayFlowDTO, withholdFlowDTO, cardJsonObj, request);

        //根据三方返回状态进行处理
        handleCreditCardPayDataByThirdStatusV3(withholdFlowDTO, qrPayFlowDTO, cardJsonObj, creditNeedCardPayAmount, creditNeedCardPayNoFeeAmount,
                remainingCreditAmount, cardPayRate, cardPayFee, request);

        return amountResultMap;
    }


    /**
     * 分期付支付
     * @author zhouttt
     * @date 2021/10/29 10:24
     * @param qrPayFlowDTO
     * @param qrPayDTO
     * @param payUser
     * @param recUser
     * @param request
     */
    private Map<String, Object> creditPayV4(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, UserDTO payUser,
                                            UserDTO recUser, HttpServletRequest request) throws Exception {
        //付款用户必须是已开通分期付并且不需要补充信息的
        if(!payUser.getInstallmentState().equals(StaticDataEnum.STATUS_1.getCode())){
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //是否逾期
        Map<String, Object> userCreditInfoMap = getCreditUserAvailableAmountAndState(payUser.getId());

        //分期付可用额度
        BigDecimal creditAmount = (BigDecimal) userCreditInfoMap.get("creditAmount");

        //分期付用户状态
        Integer userState = (Integer) userCreditInfoMap.get("userState");

        //用户是否冻结
        if(userState.equals(StaticDataEnum.CREAT_USER_STATE_11.getCode())){
            throw new BizException(I18nUtils.get("pay.user.freeze", getLang(request)));
        }

        //设置交易类型
        qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());
        qrPayFlowDTO.setProductId(qrPayDTO.getProductId());

        //通道校验
        Map<String, Object> channelResultMap = verifyAndGetPayGatewayCardInfo(0, qrPayDTO.getCardId(), qrPayFlowDTO, request);

        GatewayDTO gatewayDTO = (GatewayDTO) channelResultMap.get("gatewayDTO");
        CardDTO cardDTO = (CardDTO) channelResultMap.get("cardDTO");
        JSONObject cardJsonObj = (JSONObject) channelResultMap.get("cardJsonObj");

        //交易金额校验
        Map<String, Object> amountResultMap = verifyAndGetPayAmountV3(qrPayFlowDTO, qrPayDTO, cardDTO, request);

        //卡支付总金额
        BigDecimal creditNeedCardPayAmount = new BigDecimal((String) amountResultMap.get("creditNeedCardPayAmount"));
        //卡支付金额 不带手续费
        BigDecimal creditNeedCardPayNoFeeAmount = new BigDecimal((String) amountResultMap.get("creditNeedCardPayNoFeeAmount"));
        //剩余分期付金额
        BigDecimal remainingCreditAmount = new BigDecimal((String) amountResultMap.get("remainingCreditAmount"));
        //卡支付手续费 费率
        BigDecimal cardPayRate = new BigDecimal((String) amountResultMap.get("cardPayRate"));
        BigDecimal cardPayFee = new BigDecimal((String) amountResultMap.get("cardPayFee"));

        //校验额度是否足够
        if(creditAmount.compareTo(BigDecimal.ZERO) <= 0 || creditAmount.compareTo(remainingCreditAmount) < 0){
            throw new BizException(I18nUtils.get("pay.credit.amount.insufficient", getLang(request)));
        }

        //限额组件
        if(creditNeedCardPayNoFeeAmount.compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimit(0L, creditNeedCardPayNoFeeAmount, request);
        }

        try{
            Long id = qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request);
            qrPayFlowDTO.setId(id);
        }catch (Exception e){
            //限额回滚
            if(creditNeedCardPayNoFeeAmount.compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), creditNeedCardPayNoFeeAmount,request);
            }
            throw new BizException(I18nUtils.get("pay.order.same", getLang(request)));
        }

        //如果有整体出售，进行出账
        if(qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            doAccountOut(qrPayFlowDTO, gatewayDTO, request);
        }

        //冻结75%额度
        if(remainingCreditAmount.compareTo(BigDecimal.ZERO) > 0){
            //保存冻结额度流水
            PayCreditBalanceFlowDTO payCreditBalanceFlowDTO = new PayCreditBalanceFlowDTO();
            payCreditBalanceFlowDTO.setQrPayFlowId(qrPayFlowDTO.getId());
            payCreditBalanceFlowDTO.setOperateType(StaticDataEnum.PAY_BALANCE_FLOW_OPERATE_TYPE_1.getCode());
            payCreditBalanceFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
            payCreditBalanceFlowDTO.setCreditQuotaAmount(remainingCreditAmount);
            payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_0.getCode());
            Long id = payCreditBalanceFlowService.savePayCreditBalanceFlow(payCreditBalanceFlowDTO, request);

            boolean frozenResult = frozenCreditAmount(remainingCreditAmount, payUser.getId(), qrPayFlowDTO.getId(), qrPayFlowDTO.getId());

            if(!frozenResult){
                log.error("冻结用户金额失败");
                //出账回滚
                //限额 出账回滚
                creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);
                //记录冻结额度流水为失败
                payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_2.getCode());
                payCreditBalanceFlowService.updatePayCreditBalanceFlow(id, payCreditBalanceFlowDTO, request);

                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_1.getCode());
            payCreditBalanceFlowService.updatePayCreditBalanceFlow(id, payCreditBalanceFlowDTO, request);
        }

        // 有使用卡券
        if(qrPayFlowDTO.getMarketingId() != null){
            boolean useMarketingResult = false;
            try{
                useMarketingResult = this.useOneMarketing(qrPayFlowDTO,request);
            }catch (Exception e){
                log.error("冻结用户金额失败");
                //出账回滚
                //限额 出账回滚
                creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);
                //记录冻结额度流水为失败
                creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId(), remainingCreditAmount, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            if(!useMarketingResult){
                //限额 出账回滚
                creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);
                //记录冻结额度流水为失败
                creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId(), remainingCreditAmount, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
        }

        //捐赠金额
        BigDecimal donationAmount = new BigDecimal((String) amountResultMap.get("donationAmount"));
        if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
            // 封装捐赠流水
            DonationFlowDTO donationFlowDTO = new DonationFlowDTO();
            donationFlowDTO.setFlowId(qrPayFlowDTO.getId());
            donationFlowDTO.setUserId(payUser.getId());
            donationFlowDTO.setUserName(payUser.getUserFirstName() + " " + payUser.getUserLastName());
            donationFlowDTO.setInstituteId(Long.valueOf(qrPayDTO.getDonationInstiuteId()));
            donationFlowDTO.setAmount(donationAmount);
            donationFlowDTO.setSettlementAmount(donationAmount);
            donationFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_34.getCode());
            donationFlowDTO.setMerchantId(qrPayFlowDTO.getMerchantId());
            donationFlowService.saveDonationFlow(donationFlowDTO, request);
        }

        //生成小费流水记录
        BigDecimal tipAmount = qrPayDTO.getTipAmount();
        if(null != tipAmount && tipAmount.compareTo(BigDecimal.ZERO) > 0){
            // 封装小费流水
            TipFlowDTO tipFlowDTO = new TipFlowDTO();
            tipFlowDTO.setFlowId(qrPayFlowDTO.getId());
            tipFlowDTO.setMerchantId(qrPayFlowDTO.getMerchantId());
            tipFlowDTO.setSettlementAmount(tipAmount);
            tipFlowDTO.setTipAmount(tipAmount);
            tipFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_35.getCode());
            tipFlowDTO.setUserId(payUser.getId());
            tipFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
            tipFlowService.saveTipFlow(tipFlowDTO, request);
        }

        //交易状态为三方初始状态
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
        updateFlow(qrPayFlowDTO, null, null, request);

        //25%卡支付调用卡支付

        //保存三方代扣交易流水
        WithholdFlowDTO withholdFlowDTO = null;
        try {
            QrPayFlowDTO creditCardPayFlowDTO = new QrPayFlowDTO();
            BeanUtils.copyProperties(qrPayFlowDTO, creditCardPayFlowDTO);
            creditCardPayFlowDTO.setPayAmount(creditNeedCardPayAmount);

            withholdFlowDTO = packageWithHoldFlow(creditCardPayFlowDTO, cardDTO, new BigDecimal((String) amountResultMap.get("cardPayFee")),  (BigDecimal) amountResultMap.get("cardPayRateReal"), gatewayDTO);
            withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        } catch (Exception e) {
            log.error("保存三方代扣交易流水失败:"+e.getMessage(),e);
            //保存三方流水失败
            //限额 出账回滚
            creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);

            //分期付额度回滚
            creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId(), remainingCreditAmount, request);
            //卡券回退
            oneMarketingRollback(qrPayFlowDTO,request);

            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }


        if(Integer.parseInt(qrPayFlowDTO.getGatewayId()+"") == StaticDataEnum.GATEWAY_TYPE_0.getCode()){
            //发往三方请求
            sendCardPayLatPayRequest(qrPayFlowDTO, withholdFlowDTO, cardJsonObj, request);
        }else{

            Map<String,Object> urlMap = sendStripeRequest(qrPayFlowDTO, withholdFlowDTO, cardDTO, request);
            if(urlMap != null && urlMap.size() > 0) {
                //触发了3DS
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
                updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
                return urlMap;
            }
        }


        //根据三方返回状态进行处理
        handleCreditCardPayDataByThirdStatusV3(withholdFlowDTO, qrPayFlowDTO, cardJsonObj, creditNeedCardPayAmount, creditNeedCardPayNoFeeAmount,
                remainingCreditAmount, cardPayRate, cardPayFee, request);

        return amountResultMap;
    }
    // 营销券回退
    @Override
    public void oneMarketingRollback(QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException {

        try {
            //查询正向交易
            Map<String,Object> params =  new HashMap<>();
            params.put("flowId",qrPayFlowDTO.getId());
            params.put("transType",StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode());
//            params.put("state",StaticDataEnum.TRANS_STATE_1.getCode());
            MarketingFlowDTO marketingFlowDTO = marketingFlowService.findOneMarketingFlow(params);
            if(marketingFlowDTO == null || marketingFlowDTO.getId() == null){
                return;
            }
            // 修改流水为处理中的状态
            marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_4.getCode());
            marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(),marketingFlowDTO,request);
            // 请求账户回退卡券
            serverService.marketingRollBack(marketingFlowDTO.getId());
            marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(),marketingFlowDTO,request);
        }catch ( Exception e){
            log.error("useOneMarketingRollback id:"+ qrPayFlowDTO.getId()+",exception"+e.getMessage(),e);
        }
    }

    private boolean useOneMarketing(QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException {
        //保存流水
        MarketingFlowDTO marketingFlowDTO = new MarketingFlowDTO();
        marketingFlowDTO.setFlowId(qrPayFlowDTO.getId());
        marketingFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode());
        marketingFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
        marketingFlowDTO.setAmount(qrPayFlowDTO.getMarketingBalance());
        marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        marketingFlowDTO.setDirection(StaticDataEnum.DIRECTION_1.getCode());
        marketingFlowDTO.setMarketingId(qrPayFlowDTO.getMarketingId());
        marketingFlowDTO.setMarketingManageId(qrPayFlowDTO.getMarketingManageId());
        Long id = marketingFlowService.saveMarketingFlow(marketingFlowDTO, request);

        try {
            JSONObject requestData = new JSONObject();
            requestData.put("userId",marketingFlowDTO.getUserId());
            requestData.put("id",qrPayFlowDTO.getMarketingId());
            requestData.put("channelSerialnumber",id);
            serverService.useMarketing(requestData,request);
            marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            marketingFlowService.updateMarketingFlow(id,marketingFlowDTO,request);
            return true;
        }catch (Exception e){
            marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            marketingFlowService.updateMarketingFlow(id,marketingFlowDTO,request);
            return false;
        }


    }


    /**
     * 分期付 限额 出账 回滚
     * @author zhangzeyuan
     * @date 2021/6/27 23:02
     * @param qrPayFlowDTO
     * @param creditNeedCardPayNoFeeAmount
     * @param request
     */
    @Override
    public void creditChannelLimitAndBatchAmountRollBack(QrPayFlowDTO qrPayFlowDTO, BigDecimal creditNeedCardPayNoFeeAmount, Integer state,
                                                          HttpServletRequest request) throws Exception{
        if(qrPayFlowDTO.getGatewayId() != null && creditNeedCardPayNoFeeAmount.compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), qrPayFlowDTO.getGatewayId(), creditNeedCardPayNoFeeAmount,request);
        }
        doBatchAmountOutRollBack(qrPayFlowDTO, request);
        qrPayFlowDTO.setState(state);
        updateFlow(qrPayFlowDTO, null, null, request);
    }
    /**
     *  冻结分期付额度
     * @author zhangzeyuan
     * @date 2021/6/27 22:42
     * @param creditAmount
     * @param userId
     * @param flowId
     * @param rollbackFlowId
     * @return boolean
     */
    private boolean frozenCreditAmount(BigDecimal creditAmount, Long userId, Long flowId, Long rollbackFlowId){
        boolean result = false;

        String url = creditMerchantUrl + "/payremote/user/updateUserCreditAmount";

        //组装数据
        JSONObject postParamJson = new JSONObject();
        JSONObject dataJson = new JSONObject();
        dataJson.put("creditAmount", creditAmount);
        dataJson.put("userId", userId);
        dataJson.put("flowId", flowId);
        dataJson.put("rollbackFlowId", rollbackFlowId);
        postParamJson.put("data", dataJson);

        try {
            String resultData = HttpClientUtils.post(url, postParamJson.toJSONString());
            JSONObject resultDataJson = JSONObject.parseObject(resultData);
            String code = resultDataJson.getString("code");
            if(code != null && code.equals(ResultCode.OK.getCode())){
                result = true;
            }
        }catch (Exception e){
            log.error("分期付冻结额度出错" + flowId, e.getMessage());
        }
        return result;
    }

    /**
     *  分期付冻结额度回滚
     * @author zhangzeyuan
     * @date 2021/6/27 23:57
     * @param userId
     * @param flowId
     * @return boolean
     */
    private void creditFrozenAmountRollback(Long userId, Long flowId, BigDecimal transAmount, HttpServletRequest request) throws BizException {
        if(transAmount.compareTo(BigDecimal.ZERO) <= 0 ){
            return;
        }
        //记录分期付额度回滚流水
        //保存冻结额度流水
        PayCreditBalanceFlowDTO payCreditBalanceFlowDTO = new PayCreditBalanceFlowDTO();
        payCreditBalanceFlowDTO.setQrPayFlowId(flowId);
        payCreditBalanceFlowDTO.setOperateType(StaticDataEnum.PAY_BALANCE_FLOW_OPERATE_TYPE_2.getCode());
        payCreditBalanceFlowDTO.setUserId(userId);
        payCreditBalanceFlowDTO.setCreditQuotaAmount(transAmount);
        payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_0.getCode());
        Long id = payCreditBalanceFlowService.savePayCreditBalanceFlow(payCreditBalanceFlowDTO, request);

        String url = creditMerchantUrl + "/payremote/user/userAmountRollback";

        //组装数据
        JSONObject postParamJson = new JSONObject();
        JSONObject dataJson = new JSONObject();
        dataJson.put("userId", userId);
        dataJson.put("flowId", flowId);
        postParamJson.put("data", dataJson);
        try {
            String resultData = HttpClientUtils.post(url, postParamJson.toJSONString());
            JSONObject resultDataJson = JSONObject.parseObject(resultData);
            String code = resultDataJson.getString("code");
            if(code != null && code.equals(ResultCode.OK.getCode())){
                //记录流水状态成功
                payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_1.getCode());
                payCreditBalanceFlowService.updatePayCreditBalanceFlow(id,  payCreditBalanceFlowDTO, request);
            }else {
                payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_3.getCode());
                payCreditBalanceFlowService.updatePayCreditBalanceFlow(id,  payCreditBalanceFlowDTO, request);
            }
        }catch (Exception e){
            //记录流水为可疑 跑批处理
            payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_3.getCode());
            payCreditBalanceFlowService.updatePayCreditBalanceFlow(id,  payCreditBalanceFlowDTO, request);
        }
    }



    /**
     * 账户出账
     * @author zhangzeyuan
     * @date 2021/6/28 0:17
     * @param qrPayFlowDTO
     * @param request
     * @return boolean
     */
    private boolean doBatchAmountOut(QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws Exception{
        Long orderNo = SnowflakeUtil.generateId();
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        BigDecimal amountOutTotal = BigDecimal.ZERO;
        // 红包出账(而不是卡券)
        if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 && qrPayFlowDTO.getMarketingId()  == null ){
            // 账户出账记录
            AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
            accountFlowDTO.setFlowId(qrPayFlowDTO.getId());
            accountFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
            accountFlowDTO.setAccountType(StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
            accountFlowDTO.setTransAmount(qrPayFlowDTO.getRedEnvelopeAmount());
            accountFlowDTO.setOrderNo(orderNo);
            accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode());
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
            accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO, request));

            // 用户红包出账报文拼写
            JSONObject json1 = new JSONObject();
            json1.put("serialNumber", accountFlowDTO.getId());
            json1.put("transAmount", qrPayFlowDTO.getRedEnvelopeAmount());
            json1.put("userId", qrPayFlowDTO.getPayUserId());
            json1.put("subAccountType", StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
            // 交易方向（0：入账；1：出账）
            json1.put("transDirection", StaticDataEnum.DIRECTION_1.getCode());
            json1.put("channelTransType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode());
            jsonArray.add(json1);
            amountOutTotal = amountOutTotal.add(qrPayFlowDTO.getRedEnvelopeAmount());
        }
        // 整体出售出账
        if(qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
            // 账户出账记录
            accountFlowDTO.setFlowId(qrPayFlowDTO.getId());
            accountFlowDTO.setUserId(qrPayFlowDTO.getRecUserId());
            accountFlowDTO.setAccountType(StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
            accountFlowDTO.setTransAmount(qrPayFlowDTO.getWholeSalesAmount());
            accountFlowDTO.setOrderNo(orderNo);
            accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_26.getCode());
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
            accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO, request));

            // 商户出账
            JSONObject json2 = new JSONObject();
            json2.put("serialNumber", accountFlowDTO.getId());
            json2.put("transAmount",  qrPayFlowDTO.getWholeSalesAmount());
            json2.put("userId", qrPayFlowDTO.getRecUserId());
            json2.put("subAccountType", StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
            // 交易方向（0：入账；1：出账）
            json2.put("transDirection",  StaticDataEnum.DIRECTION_1.getCode());
            json2.put("channelTransType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_26.getCode());
            jsonArray.add(json2);
            amountOutTotal = amountOutTotal.add(qrPayFlowDTO.getWholeSalesAmount());
        }
        if(jsonArray != null && jsonArray.size() > 0){
            jsonObject.put("totalAmountOut",amountOutTotal);
            jsonObject.put("totalAmountIn", BigDecimal.ZERO);
//        if (BigDecimal.ZERO.compareTo(qrPayFlowDTO.getWholeSalesAmount()) < 0 && BigDecimal.ZERO.compareTo(qrPayFlowDTO.getRedEnvelopeAmount()) < 0) {
//            jsonObject.put("totalNumber", 2);
//        } else {
//            jsonObject.put("totalNumber", 1);
//        }
            jsonObject.put("totalNumber", jsonArray.size());
            jsonObject.put("channelSerialnumber", orderNo);
            jsonObject.put("channel", "0001");
            jsonObject.put("channelTransType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_29.getCode());
            jsonObject.put("dataList", jsonArray);
            //支付交易请求
            JSONObject result = null;
            try {
                result =serverService.batchChangeBalance(jsonObject);
            }catch (Exception e){
                log.error("doBatchAmountOut orderNo:" +orderNo+ ",Exception,errorMessage:"+e.getMessage()+",message:"+e);
                throw e;
            }
            //账户交易失败
            if(result != null && "2".equals(result.getString("errorState"))){
                // 更新出账记录为失败
                AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
                accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                accountFlowService.updateAccountFlowByOrderNo(orderNo,accountFlowDTO,request);
                return false;
            }else{
                // 更新出账记录为成功
                AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
                accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                accountFlowService.updateAccountFlowByOrderNo(orderNo,accountFlowDTO,request);
                // 钱包出账记录
                if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0  && qrPayFlowDTO.getMarketingId() == null){
                    MarketingFlowDTO marketingFlowDTO = new MarketingFlowDTO();
                    marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                    marketingFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
                    marketingFlowDTO.setFlowId(qrPayFlowDTO.getId());
                    marketingFlowDTO.setDirection(StaticDataEnum.DIRECTION_1.getCode());
                    marketingFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode());
                    marketingFlowDTO.setAmount(qrPayFlowDTO.getRedEnvelopeAmount());
                    marketingFlowDTO.setId(marketingFlowService.saveMarketingFlow(marketingFlowDTO,request));
                }
                return true;
            }
        }else{
            return true;
        }


    }

    private Map<String, Object> doPayTypeCheck(QrPayFlowDTO qrPayFlowDTO, UserDTO recUser, UserDTO payUser, QrPayDTO qrPayDTO, HttpServletRequest request)  throws Exception {
        Map<String,Object> result = new HashMap<>(8);
        if (StaticDataEnum.PAY_TYPE_0.getCode() == (qrPayDTO.getPayType())){
            //查询商户路由配置
            GatewayDTO gatewayDTO = getGateWay(qrPayDTO,qrPayFlowDTO,request);
            if(gatewayDTO==null ||gatewayDTO.getId()==null){
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            result.put("gateWay",gatewayDTO);
            qrPayFlowDTO.setGatewayId(gatewayDTO.getType().longValue());
//            gatewayDTO = doRouteConfig(recUser,qrPayFlowDTO,qrPayDTO,request);
            //如果是卡支付，查询用户是否绑卡
            //获取卡信息
            JSONObject cardObj;
            try {
                cardObj = serverService.getCardInfo(qrPayDTO.getCardId());
            } catch (Exception e) {
                //查询异常交易失败
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            if (cardObj == null) {
                //无信息返回，交易失败
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            //卡信息
            CardDTO cardDTO = JSONObject.parseObject(cardObj.toJSONString(), CardDTO.class);
            result.put("cardList",cardObj);
            qrPayFlowDTO.setCardId(qrPayDTO.getCardId().toString());
            //需要查询是否有二级商户信息，已废除
//            if(StaticDataEnum.GATEWAY_TYPE_0.getCode()==gatewayDTO.getType()||StaticDataEnum.GATEWAY_TYPE_4.getCode()==gatewayDTO.getType()){
//                //Latpay或者integrapay
//                //查询商户,是否配置商户号和密码
//                Map<String,Object> map = new HashedMap();
//                map.put("userId",qrPayFlowDTO.getRecUserId());
//                MerchantDTO merchant = merchantService.findMerchantById(qrPayDTO.getMerchantId());
//                map.put("merchantId",merchant.getId());
//                map.put("gatewayId",gatewayDTO.getType());
//                SecondMerchantGatewayInfoDTO  secondMerchantGatewayInfoDTO = secondMerchantGatewayInfoService.findOneSecondMerchantGatewayInfo(map);
//                if(secondMerchantGatewayInfoDTO==null ||secondMerchantGatewayInfoDTO.getId() ==null){
//                    throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
//                }
//            }

            //如果是LatPay，判断卡表token不为空，如果是integrapay 判断uniqueReference不为空
            if(StaticDataEnum.GATEWAY_TYPE_0.getCode()==gatewayDTO.getType()){
                if(cardDTO.getCrdStrgToken() == null){
                    throw new BizException(I18nUtils.get("channel.token.missing", getLang(request)));
                }
//                Long now = System.currentTimeMillis();
//                // TODO 卡类型截止时间
//                if( now - 1616256000000L > 0 && cardDTO.getCardCategory() == null ){
//                    throw new BizException(I18nUtils.get("card.type.not.found", getLang(request)));
//                }
                // 用户和卡交易限制查询
                userService.verifyCardFailedTime(qrPayFlowDTO.getPayUserId(),qrPayDTO.getCardId(),request);
            }else if(StaticDataEnum.GATEWAY_TYPE_4.getCode()==gatewayDTO.getType()){
                if(cardDTO.getUniqueReference() == null){
                    throw new BizException(I18nUtils.get("channel.token.missing", getLang(request)));
                }
            }else{
                //未知的渠道
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            //卡支付
            if (recUser.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
                //卡转账，暂时无此分支
                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_4.getCode());
            } else {
                //卡消费
                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode());
            }
        }
        else if (StaticDataEnum.PAY_TYPE_4.getCode() == (qrPayDTO.getPayType())){
            qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());
            qrPayFlowDTO.setProductId(qrPayDTO.getProductId());
            //付款用户必须是已开通分期付并且不需要补充信息的
            if(payUser.getInstallmentState() != StaticDataEnum.STATUS_1.getCode() || payUser.getSplitAddInfoState() == StaticDataEnum.STATUS_1.getCode()){
                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
            }
            //收款用户必须是商户
            if(recUser.getUserType()!=StaticDataEnum.USER_TYPE_20.getCode()){
                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
            }

        }else{
            //非法的交易方式
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }


        return result;
    }

    @Override
    public Object interestCredistOrder(QrPayDTO qrPayDTO, HttpServletRequest request) throws Exception {
        //获取交易双方user信息
//        UserDTO payUser = userService.findUserById(qrPayDTO.getPayUserId());
//        UserDTO recUser = userService.findUserById(qrPayDTO.getRecUserId());
//
//        //判断用户是否存在
//        if (payUser.getId() == null || recUser.getId() == null) {
//            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
//        }
//
//        //查询账户可用状态
//        if (!checkAccountState(payUser.getId())) {
//            throw new BizException(I18nUtils.get("account.error", getLang(request)));
//        }
//        if (!checkAccountState(recUser.getId())) {
//            throw new BizException(I18nUtils.get("account.error", getLang(request)));
//        }
//
//        //创建流水
//        QrPayFlowDTO qrPayFlowDTO = new QrPayFlowDTO();
//        qrPayFlowDTO.setFee(qrPayDTO.getFeeAmt());
//        qrPayFlowDTO.setPayUserId(qrPayDTO.getPayUserId());
//        qrPayFlowDTO.setPayUserType(payUser.getUserType());
//        qrPayFlowDTO.setMerchantId(qrPayDTO.getMerchantId());
//        qrPayFlowDTO.setRecUserId(qrPayDTO.getRecUserId());
//        qrPayFlowDTO.setRecUserType(recUser.getUserType());
//        qrPayFlowDTO.setTransAmount(qrPayDTO.getTransAmount());
//        qrPayFlowDTO.setRemark(qrPayDTO.getRemark());
//        //收款用户是商户,计算各种金额
//        if(recUser.getUserType()==StaticDataEnum.USER_TYPE_20.getCode()){
//            qrPayFlowDTO =  getQrPayFlow(qrPayFlowDTO,qrPayDTO, null, request);
//        }

//        Map<String,Object> resMap = new HashMap<>();
        JSONObject result = null;
        if (StaticDataEnum.PAY_TYPE_4.getCode() == (qrPayDTO.getPayType())){
            //分期付
//            qrPayFlowDTO = checkCreditParams(qrPayFlowDTO,qrPayDTO, recUser, payUser,request);
            // 同步分期付获取交易结果
            JSONObject creditInfo = new JSONObject();
            creditInfo.put("userId", qrPayDTO.getPayUserId());
            creditInfo.put("merchantId", qrPayDTO.getMerchantId());
//            creditInfo.put("merchantName", qrPayFlowDTO.getCorporateName());
            creditInfo.put("borrowAmount", qrPayDTO.getTrulyPayAmount());
            creditInfo.put("productId", qrPayDTO.getProductId());
            creditInfo.put("discountPackageAmount", qrPayDTO.getWholeSalesAmount());

            creditInfo.put("orderAmount", qrPayDTO.getTransAmount());
            creditInfo.put("redEnvelopeAmount", qrPayDTO.getRedEnvelopeAmount());
            JSONObject requestData = new JSONObject();
            requestData.put("createBorrowMessageDTO", creditInfo);
            requestData.put("accessSideId", Constant.CREDIT_MERCHANT_ID);
            requestData.put("isShow", StaticDataEnum.STATUS_1.getCode());
            requestData.put("tripartiteTransactionNo", SnowflakeUtil.generateId());
            try {
                result = serverService.apiCreditOrder(requestData, request);
            } catch (Exception e) {
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
        }
        else {
            //非法的交易方式
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }
        return result;
    }

    private QrPayFlowDTO getQrPayFlow(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, GatewayDTO gatewayDTO,CardDTO cardDTO, HttpServletRequest request) throws Exception {

        //查询商户配置信息
        MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayDTO.getMerchantId());
        if(merchantDTO==null || merchantDTO.getId()==null ||  merchantDTO.getIsAvailable() !=1) {
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));

        }

        //前面校验过，直接取来用
        BigDecimal wholeSalesAmount = qrPayDTO.getWholeSalesAmount() == null ?  BigDecimal.ZERO : qrPayDTO.getWholeSalesAmount();
        BigDecimal normalSaleAmount = qrPayDTO.getTransAmount().subtract(qrPayDTO.getWholeSalesAmount());
        BigDecimal redEnvelopeAmount = qrPayDTO.getRedEnvelopeAmount() == null ? BigDecimal.ZERO : qrPayDTO.getRedEnvelopeAmount();
        //整体出售折扣
        BigDecimal wholeSaleUserDiscount = BigDecimal.ZERO;
        //查询商户整体出售余额
        BigDecimal merchantAmount = userService.getBalance(qrPayDTO.getRecUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
        //金额计算
        if(normalSaleAmount.compareTo(BigDecimal.ZERO) > 0){
            // 营销折扣
            BigDecimal marketingDiscount = merchantDTO.getMarketingDiscount() == null ? BigDecimal.ZERO : merchantDTO.getMarketingDiscount();
            // 固定折扣
            BigDecimal baseDiscount ;
            // 额外折扣
            BigDecimal extraDiscount;

            if(qrPayDTO.getPayType() == StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode()){
                baseDiscount = merchantDTO.getBasePayRate() == null ? BigDecimal.ZERO : merchantDTO.getBasePayRate();
            }else{
                baseDiscount = merchantDTO.getBaseRate() == null ? BigDecimal.ZERO : merchantDTO.getBaseRate();
            }

            //必须先用整体出售的余额
            if(merchantAmount.compareTo(wholeSalesAmount) != 0 ){
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            //用户基础折扣金额
            BigDecimal baseDiscountAmount = normalSaleAmount.multiply(baseDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
            //用户时间内营销折扣金额
            Long today = System.currentTimeMillis();
            Long extraDiscountPeriod = merchantDTO.getExtraDiscountPeriod();
            if (today.longValue() < extraDiscountPeriod.longValue()) {
                extraDiscount = merchantDTO.getExtraDiscount();
            } else {
                extraDiscount = new BigDecimal("0.00");
            }
            BigDecimal extraDiscountAmount = normalSaleAmount.multiply(extraDiscount == null ? BigDecimal.ZERO :extraDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
            //计算营销折扣金额
            BigDecimal markingDiscountAmount = normalSaleAmount.multiply(marketingDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
            qrPayFlowDTO.setBaseDiscountAmount(baseDiscountAmount);
            qrPayFlowDTO.setExtraDiscountAmount(extraDiscountAmount);
            qrPayFlowDTO.setMarkingDiscountAmount(markingDiscountAmount);

            if(wholeSalesAmount.compareTo(BigDecimal.ZERO)>0){
                qrPayFlowDTO.setSaleType(StaticDataEnum.SALE_TYPE_2.getCode());
                qrPayFlowDTO.setWholeSalesDiscount(merchantDTO.getWholeSaleUserDiscount());
            }else{
                qrPayFlowDTO.setSaleType(StaticDataEnum.SALE_TYPE_0.getCode());
            }
        }
        if(wholeSalesAmount.compareTo(BigDecimal.ZERO) > 0){
            // 判断整体出售余额足够
            if(merchantAmount.compareTo(wholeSalesAmount) < 0){
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            if(normalSaleAmount.compareTo(BigDecimal.ZERO) > 0){
                qrPayFlowDTO.setSaleType(StaticDataEnum.SALE_TYPE_2.getCode());
                qrPayFlowDTO.setWholeSalesDiscount(merchantDTO.getWholeSaleUserDiscount());
            }else{
                qrPayFlowDTO.setSaleType(StaticDataEnum.SALE_TYPE_1.getCode());
            }

            if(qrPayDTO.getPayType() == StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode()){
                wholeSaleUserDiscount = merchantDTO.getWholeSaleUserPayDiscount() == null ? BigDecimal.ZERO : merchantDTO.getWholeSaleUserPayDiscount() ;
            }else{
                wholeSaleUserDiscount = merchantDTO.getWholeSaleUserDiscount() == null ? BigDecimal.ZERO : merchantDTO.getWholeSaleUserDiscount() ;
            }
            qrPayFlowDTO.setWholeSalesDiscount(wholeSaleUserDiscount);
        }


        qrPayFlowDTO.setExtraDiscountAmount(qrPayFlowDTO.getExtraDiscountAmount() == null ? BigDecimal.ZERO : qrPayFlowDTO.getExtraDiscountAmount());
        qrPayFlowDTO.setBaseDiscountAmount(qrPayFlowDTO.getBaseDiscountAmount() == null ? BigDecimal.ZERO : qrPayFlowDTO.getBaseDiscountAmount() );
        qrPayFlowDTO.setMarkingDiscountAmount(qrPayFlowDTO.getMarkingDiscountAmount() == null ? BigDecimal.ZERO : qrPayFlowDTO.getMarkingDiscountAmount());


        BigDecimal wholeSaleUserDiscountAmount = wholeSalesAmount.multiply(wholeSaleUserDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        // 实付金额 = 整体出售折后 + 正常交易折后（正常交易 - 基础折扣 - 时间内营销折扣 -营销折扣） - 红包金额
        BigDecimal payAmount = wholeSalesAmount.subtract(wholeSaleUserDiscountAmount).add(
                normalSaleAmount.subtract(qrPayFlowDTO.getBaseDiscountAmount())
                        .subtract(qrPayFlowDTO.getExtraDiscountAmount())
                        .subtract(qrPayFlowDTO.getMarkingDiscountAmount())
        );

        // 如果有实付金额而且通道是卡支付，需要计算通道手续费
        if(qrPayDTO.getPayType() == StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode()){
            if(payAmount.compareTo(BigDecimal.ZERO) > 0){
                if(qrPayFlowDTO.getGatewayId() == StaticDataEnum.GATEWAY_TYPE_0.getCode()){
                    //如果支付通道是LatPay
                    // 查询手续费
                    Map<String,Object> params = new HashMap<>(8);
                    params.put("gatewayId",gatewayDTO.getType());
                    params.put("code",cardDTO.getCardCategory() == null ? 11 : cardDTO.getCardCategory());
                    ChannelFeeConfigDTO channelFeeConfigDTO = channelFeeConfigService.findOneChannelFeeConfig(params);

                    if(channelFeeConfigDTO == null || channelFeeConfigDTO.getId() == null ){
                        throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
                    }

                    GatewayDTO gatewayDTO1 = new GatewayDTO();
                    gatewayDTO1.setRate(channelFeeConfigDTO.getRate());
                    gatewayDTO1.setRateType(channelFeeConfigDTO.getType());
                    BigDecimal gateWayFee = gatewayService.getGateWayFee(gatewayDTO1,payAmount);
                    payAmount = payAmount.add(gateWayFee);
                    qrPayFlowDTO.setFee(gateWayFee);
                    qrPayFlowDTO.setFeeDirection(channelFeeConfigDTO.getDirection());
                }else{
                    BigDecimal gateWayFee = gatewayService.getGateWayFee(gatewayDTO,payAmount);
                    payAmount = payAmount.add(gateWayFee);
                    qrPayFlowDTO.setFee(gateWayFee);
                    qrPayFlowDTO.setFeeDirection(StaticDataEnum.FEE_DIRECTION_1.getCode());
                }
//                qrPayFlowDTO.setRate(gatewayDTO.getRate());
            }
        }

        payAmount = payAmount.subtract(redEnvelopeAmount);

        log.info("TrulyPayAmount:"+payAmount);

        if(payAmount.compareTo(qrPayDTO.getTrulyPayAmount()) != 0){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //平台服务费
        BigDecimal platformFee = normalSaleAmount.multiply(merchantDTO.getAppChargeRate() == null ? BigDecimal.ZERO : merchantDTO.getAppChargeRate()).setScale(2,BigDecimal.ROUND_HALF_UP);
        //实收金额 = 正常交易 - 基础折扣 - 时间内营销折扣 -营销折扣 - 平台服务费
        BigDecimal recAmount = normalSaleAmount.subtract(qrPayFlowDTO.getBaseDiscountAmount()).subtract(qrPayFlowDTO.getExtraDiscountAmount()).subtract(qrPayFlowDTO.getMarkingDiscountAmount()).subtract(platformFee);

        qrPayFlowDTO.setIsNeedClear(StaticDataEnum.NEED_CLEAR_TYPE_1.getCode());
        qrPayFlowDTO.setClearState(StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
        qrPayFlowDTO.setCreditOrderNo(SnowflakeUtil.generateId().toString());
        qrPayFlowDTO.setMerchantId(merchantDTO.getId());

        qrPayFlowDTO.setPayAmount(payAmount);
        qrPayFlowDTO.setPlatformFee(platformFee);
        qrPayFlowDTO.setRecAmount(recAmount);
        qrPayFlowDTO.setClearAmount(recAmount);
        qrPayFlowDTO.setWholeSalesAmount(wholeSalesAmount);
        qrPayFlowDTO.setNormalSaleAmount(normalSaleAmount);
        qrPayFlowDTO.setRedEnvelopeAmount(redEnvelopeAmount);
        return  qrPayFlowDTO;
    }

    /**
     *  生成分期付订单
     * @author zhangzeyuan
     * @date 2021/6/27 23:49
     * @param qrPayFlowDTO
     * @param cardPayRate
     * @param creditNeedCardPayNoFeeAmount
     * @param cardPayFee
     * @param creditNeedCardPayAmount
     * @param request
     * @return com.uwallet.pay.main.model.dto.QrPayFlowDTO
     */
    private Integer createCreditInstallmentOrder(QrPayFlowDTO qrPayFlowDTO, BigDecimal cardPayRate, BigDecimal creditNeedCardPayNoFeeAmount,
                                                 BigDecimal cardPayFee, BigDecimal creditNeedCardPayAmount, JSONObject cardObj,
                                                 HttpServletRequest request) throws  Exception{
        // 同步分期付获取交易结果
        JSONObject creditInfo = new JSONObject();
        creditInfo.put("userId", qrPayFlowDTO.getPayUserId());
        creditInfo.put("merchantId", qrPayFlowDTO.getMerchantId());
        creditInfo.put("merchantName", qrPayFlowDTO.getCorporateName());
        creditInfo.put("borrowAmount", qrPayFlowDTO.getPayAmount());
        creditInfo.put("productId", qrPayFlowDTO.getProductId());
        creditInfo.put("discountPackageAmount", qrPayFlowDTO.getWholeSalesAmount());
        creditInfo.put("settleAmount", qrPayFlowDTO.getRecAmount());
        creditInfo.put("orderAmount", qrPayFlowDTO.getTransAmount());
        creditInfo.put("redEnvelopeAmount", qrPayFlowDTO.getRedEnvelopeAmount());

        creditInfo.put("cardPayFeeRate", cardPayRate);//卡支付费率
        if(qrPayFlowDTO.getPayAmount().compareTo(creditNeedCardPayNoFeeAmount) == 0){
            creditInfo.put("cardPayPercentage", 1);//卡支付比例
        }else{
            creditInfo.put("cardPayPercentage", Constant.PAY_CREDIT_NEED_CARDPAY_AMOUNT_RATE);//卡支付比例
        }

        creditInfo.put("cardPaySubtotal", creditNeedCardPayNoFeeAmount);//卡支付总金额(不含手续费)
        creditInfo.put("cardPayFee", cardPayFee);//卡支付手续费总额(卡支付金额*卡支付费率)
        creditInfo.put("cardPayTotal", creditNeedCardPayAmount);//卡支付总金额(订单金额*25% + 手续费)
        creditInfo.put("cardId", qrPayFlowDTO.getCardId());//账户系统 支付用的卡的ID(u_card表)
        creditInfo.put("qrPayFlowId", qrPayFlowDTO.getId());//支付系统订单ID(qr_pay_flow id)
        creditInfo.put("cardAccountName", cardObj.getString("accountName"));//支付系统订单ID(qr_pay_flow id)
        creditInfo.put("cardNo", cardObj.getString("cardNo"));//支付系统订单ID(qr_pay_flow id)
        creditInfo.put("businessType", qrPayFlowDTO.getSaleType());

        JSONObject requestData = new JSONObject();
        requestData.put("createBorrowMessageDTO", creditInfo);
        requestData.put("accessSideId", Constant.CREDIT_MERCHANT_ID);
        requestData.put("isShow", StaticDataEnum.STATUS_0.getCode());
        requestData.put("tripartiteTransactionNo", qrPayFlowDTO.getId());
        requestData.put("borrowId", qrPayFlowDTO.getCreditOrderNo());

        JSONObject creditResult = null;

        int code = StaticDataEnum.API_ORDER_STATE_3.getCode();
        try {
            String apiCreditOrderUrl = creditMerchantUrl + "/payremote/user/createBorrowV2";
            creditResult = JSONObject.parseObject(HttpClientUtils.sendPost(apiCreditOrderUrl, requestData.toJSONString()));
        } catch (Exception e) {
            return code;
        }

        if(StringUtils.isBlank(creditResult.toJSONString()) || StringUtils.isBlank(creditResult.getString("code"))
                || creditResult.getString("code").equals(ErrorCodeEnum.HTTP_SEND_ERROR.getCode())){
            return code;
        }

        // 获取结果跟新订单信息
        Integer orderState = creditResult.getJSONObject("data").getInteger("transactionResult");

        if(null == orderState){
            //将订单置为可疑  跑批处理
            return code;
        }

        return orderState;
    }



    private QrPayFlowDTO doCredit(QrPayFlowDTO qrPayFlowDTO,HttpServletRequest request) throws  Exception{
        // 同步分期付获取交易结果
        JSONObject creditInfo = new JSONObject();
        creditInfo.put("userId", qrPayFlowDTO.getPayUserId());
        creditInfo.put("merchantId", qrPayFlowDTO.getMerchantId());
        creditInfo.put("merchantName", qrPayFlowDTO.getCorporateName());
        creditInfo.put("borrowAmount", qrPayFlowDTO.getPayAmount());
        creditInfo.put("productId", qrPayFlowDTO.getProductId());
        creditInfo.put("discountPackageAmount", qrPayFlowDTO.getWholeSalesAmount());
        creditInfo.put("settleAmount", qrPayFlowDTO.getRecAmount());
        creditInfo.put("orderAmount", qrPayFlowDTO.getTransAmount());
        creditInfo.put("redEnvelopeAmount", qrPayFlowDTO.getRedEnvelopeAmount());
        JSONObject requestData = new JSONObject();
        requestData.put("createBorrowMessageDTO", creditInfo);
        requestData.put("accessSideId", Constant.CREDIT_MERCHANT_ID);
        requestData.put("isShow", StaticDataEnum.STATUS_0.getCode());
        requestData.put("tripartiteTransactionNo", qrPayFlowDTO.getId());

        JSONObject result ;
        try {
            result = serverService.apiCreditOrder(requestData, request);
        } catch (Exception e) {
            // 请求可疑全部置为失败，并返回通知
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            qrPayFlowService.updateQrPayFlowForConcurrency(qrPayFlowDTO.getId(), qrPayFlowDTO, request);
            creditInfo.clear();
            creditInfo.put("tripartiteTransactionNo", qrPayFlowDTO.getId());
//            creditInfo.put("accessSideId", accessMerchantDTO.getPlatformId());
            serverService.orderStateRollback(creditInfo, request);
            doBatchAmountOutRollBack(qrPayFlowDTO,request);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        // 获取结果跟新订单信息
        Integer orderState = result.getInteger("transactionResult");
        qrPayFlowService.dealCreditOrderResult(result,qrPayFlowDTO,orderState,request);
        if (orderState.intValue() == StaticDataEnum.API_ORDER_STATE_0.getCode()) {
            doQrPayRollBack(qrPayFlowDTO,StaticDataEnum.TRANS_STATE_12.getCode(),request);
            if(request != null){
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }else{
                throw new BizException("We couldn't process your payment. Please try again.");
            }
        } else  if (orderState.intValue() == StaticDataEnum.API_ORDER_STATE_3.getCode()){
            if(request != null){
                throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
            }else{
                throw new BizException("You payment is processing");
            }
        }
        //三方成功后的处理
        dealThirdSuccess(null, qrPayFlowDTO, request);
//        sendMessage(qrPayFlowDTO.getRecUserId(), qrPayFlowDTO.getId(), request);
        return qrPayFlowDTO;
    }

    private QrPayFlowDTO checkCreditParams(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, UserDTO recUser, UserDTO payUser, HttpServletRequest request) throws Exception {
        String transNoType = "INS";
        if(qrPayFlowDTO.getNormalSaleAmount().compareTo(BigDecimal.ZERO) > 0){
            if(qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO)>0){
                qrPayFlowDTO.setTransNo("M"+transNoType);
            }else{
                qrPayFlowDTO.setTransNo("N"+transNoType);
            }
        }else{

            qrPayFlowDTO.setTransNo("WS"+transNoType);
        }
        if (qrPayDTO.getIsShow().intValue() != StaticDataEnum.STATUS_1.getCode()) {
            qrPayFlowDTO.setId(qrPayFlowService.saveQrPayFlow(qrPayFlowDTO,request));
        }
        qrPayFlowDTO.setTransNo(qrPayFlowDTO.getTransNo()+qrPayFlowDTO.getId());
        qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,request);
        return  qrPayFlowDTO;
    }

    private GatewayDTO doRouteConfig(UserDTO recUser, QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, HttpServletRequest request) throws BizException {
        //查询路由信息
        GatewayDTO gatewayDTO = getGateWay(qrPayDTO,qrPayFlowDTO,request);
        if(gatewayDTO==null ||gatewayDTO.getId()==null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        qrPayFlowDTO.setGatewayId(gatewayDTO.getType().longValue());
        if(recUser.getUserType()==StaticDataEnum.USER_TYPE_20.getCode()){
//            Map<String,Object> params = new HashMap<>();
//            params.put("merchantId",recUser.getMerchantId());
//            params.put("gatewayType",gatewayDTO.getGatewayType());
//            RouteDTO routeDTO = routeService.findOneRoute(params);
//            qrPayDTO.setRate( gatewayDTO.getRate());
//            qrPayDTO.setFeeDirection(routeDTO.getRateType());
//            qrPayFlowDTO.setFee(gatewayService.getGateWayFee(gatewayDTO,qrPayFlowDTO.getPayAmount()));
//            qrPayFlowDTO.setFeeDirection(qrPayDTO.getFeeDirection());
        }else{
//            qrPayFlowDTO.setRate(qrPayDTO.getRate());
//            qrPayFlowDTO.setFeeDirection(qrPayDTO.getFeeDirection());
        }


        return gatewayDTO;
    }

    /**
     * 支付宝支付方法
     * @param qrPayDTO
     * @param request
     * @param payUser
     * @param recUser
     * @param qrPayFlowDTO
     */
    private WithholdFlowDTO qrPayByAliPay(QrPayDTO qrPayDTO, HttpServletRequest request, UserDTO payUser, UserDTO recUser, QrPayFlowDTO qrPayFlowDTO) throws BizException {
        //查询路由信息
        GatewayDTO gatewayDTO = getGateWay(qrPayDTO,qrPayFlowDTO,request);
        //限额判断
        rechargeFlowService.channelLimit(gatewayDTO.getType(),qrPayFlowDTO.getPayAmount(),request);
        WithholdFlowDTO withholdFlowDTO = null;
        if(1==gatewayDTO.getType()){
            //OmiPay支付信息记录
            withholdFlowDTO = getOmiPayWithholdFlowDTO(qrPayFlowDTO, request, BigDecimal.ZERO,gatewayDTO);
        }else{
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }
        //保存三方代扣交易流水
        withholdFlowService.saveWithholdFlow(withholdFlowDTO, request);
        return withholdFlowDTO;

    }


    /**
     * 微信支付方法
     * @param qrPayDTO
     * @param request
     * @param payUser
     * @param recUser
     * @param qrPayFlowDTO
     */
    private WithholdFlowDTO  qrPayByWechatPay(QrPayDTO qrPayDTO, HttpServletRequest request, UserDTO payUser, UserDTO recUser, QrPayFlowDTO qrPayFlowDTO) throws BizException {
        //查询路由信息
        GatewayDTO gatewayDTO = getGateWay(qrPayDTO,qrPayFlowDTO,request);
        //限额判断
        rechargeFlowService.channelLimit(gatewayDTO.getType(),qrPayFlowDTO.getPayAmount(),request);
        WithholdFlowDTO withholdFlowDTO = null;
        if(2==gatewayDTO.getType()){
            //Latpay
            withholdFlowDTO = getOmiPayWithholdFlowDTO(qrPayFlowDTO, request, BigDecimal.ZERO,gatewayDTO);
        }else{
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //保存三方代扣交易流水
        withholdFlowService.saveWithholdFlow(withholdFlowDTO, request);
        return withholdFlowDTO;

    }

    /**
     * 卡支付方法
     *
     * @param request
     * @param payUser
     * @param recUser
     * @param qrPayFlowDTO
     * @param cardDTO
     * @param cardObj
     * @throws BizException
     */
    @Override
    public void qrPayByCard( HttpServletRequest request, UserDTO payUser, UserDTO recUser, QrPayFlowDTO qrPayFlowDTO, GatewayDTO gatewayDTO, CardDTO cardDTO, JSONObject cardObj) throws BizException {
        //记录交易流水
        WithholdFlowDTO withholdFlowDTO = null;
        try {
            //创建三方流水
            //LatPay
            withholdFlowDTO = getLatPayWithholdFlowDTO(qrPayFlowDTO, request, cardDTO, qrPayFlowDTO.getFee(),gatewayDTO);
            //保存三方代扣交易流水
            withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        } catch (Exception e) {
            log.error("qrPayByCard create withholdFlowDTO Exception:"+e.getMessage(),e);
            //交易失败
            doQrPayRollBack(qrPayFlowDTO,StaticDataEnum.TRANS_STATE_12.getCode(),request);
            //交易失败 更改pos订单状态 todo?

            if(request != null){
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }else{
                throw new BizException("We couldn't process your payment. Please try again.");
            }
        }

        if(withholdFlowDTO.getTransAmount().compareTo(BigDecimal.ZERO)>0){
            //渠道交易请求
            try{
//            if(StaticDataEnum.GATEWAY_TYPE_0.getCode()==gatewayDTO.getType()){
                // LatPay
                withholdFlowDTO = latPayService.latPayRequest(withholdFlowDTO,withholdFlowDTO.getTransAmount(),qrPayFlowDTO.getPlatformFee(),cardObj,request, qrPayFlowDTO.getPayUserIp());
//            }else if(StaticDataEnum.GATEWAY_TYPE_4.getCode()==gatewayDTO.getType()){
//                // integrapay
//                String token = integraPayService.apiAccessToken();
//                log.info("token:"+token);
//                withholdFlowDTO = integraPayService.payByCard( withholdFlowDTO, token);
//            }

            }catch (Exception e){
                //请求异常，记录为可疑
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
                updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
                throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
            }
        }else{
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            withholdFlowDTO.setCheckState( StaticDataEnum.IS_CHECK_1.getCode());
            withholdFlowDTO.setOrdreNo(null);
        }

        withholdFlowDTO.setState(1);
        //todo 撤除支付可疑挡板
        /*StaticDataDTO staticDataById = staticDataService.findStaticDataById(989898L);
        if (null != staticDataById && null != staticDataById.getId()){
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
        }*/
        if (StaticDataEnum.TRANS_STATE_1.getCode() == withholdFlowDTO.getState()) {
            //三方成功后的处理
            dealThirdSuccess(withholdFlowDTO, qrPayFlowDTO, request);
        } else if (StaticDataEnum.TRANS_STATE_2.getCode() == withholdFlowDTO.getState()) {
            //失败
            dealThirdFail(withholdFlowDTO, qrPayFlowDTO, request);
            if(request != null){
                // 返回错误码为银行失败
                String returnMessage = withholdFlowDTO.getReturnMessage();
                if(StaticDataEnum.LAT_PAY_BANK_STATUS_5.getMessage().equals(withholdFlowDTO.getReturnCode())){
                    if(LatPayErrorEnum.BANK_CODE_05.equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_05.getMessage());
                    }else if(LatPayErrorEnum.BANK_CODE_06.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_06.getMessage());
                    }else if(LatPayErrorEnum.BANK_CODE_07.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_07.getMessage());
                    }else if(LatPayErrorEnum.BANK_CODE_41.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_41.getMessage());
                    }else if(LatPayErrorEnum.BANK_CODE_43.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_43.getMessage());
                    }else if(LatPayErrorEnum.BANK_CODE_51.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.BANK_CODE_51.getMessage());
                    }else{
                        throw new BizException(LatPayErrorEnum.BANK_CODE_OTHER.getMessage());
                    }
                }else if (StaticDataEnum.LP_RESPONSE_TYPE_0.getMessage().equals(withholdFlowDTO.getReturnCode())){
                    // 返回信息为反欺诈系统
                    if(LatPayErrorEnum.SCSS_CODE_1003.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.SCSS_CODE_1003.getMessage());
                    }else if(LatPayErrorEnum.SCSS_CODE_1004.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.SCSS_CODE_1004.getMessage());
                    }else if(LatPayErrorEnum.SCSS_CODE_1005.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.SCSS_CODE_1005.getMessage());
                    }else if(LatPayErrorEnum.SCSS_CODE_1006.getCode().equals(returnMessage)){
                        throw new BizException(LatPayErrorEnum.SCSS_CODE_1006.getMessage());
                    }else{
                        throw new BizException(LatPayErrorEnum.SCSS_CODE_OTHER.getMessage());
                    }
                }else{
                    throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
                }

            }else{
                throw new BizException("We couldn't process your payment. Please try again.");
            }

        } else {
            //可疑
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
            updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
            if(request != null){
                throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
            }else{
                throw new BizException("You payment is processing");
            }

        }
    }

    private void doQrPayRollBack(QrPayFlowDTO qrPayFlowDTO, int transType, HttpServletRequest request) throws BizException {
        if(qrPayFlowDTO.getGatewayId() != null){
            rechargeFlowService.channelLimitRollback(System.currentTimeMillis(),qrPayFlowDTO.getGatewayId(),qrPayFlowDTO.getPayAmount(),request);
        }
        doBatchAmountOutRollBack(qrPayFlowDTO,request);
        qrPayFlowDTO.setState(transType);
        updateFlow(qrPayFlowDTO, null, null, request);
    }

    private void dealThirdFail(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException {
        if(qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode() && qrPayFlowDTO.getCardId() != null ){
            try {
                userService.logCardFailedTime(qrPayFlowDTO.getPayUserId(),Long.parseLong(qrPayFlowDTO.getCardId()),false,request);
            }catch (Exception e){
                log.error("卡支付交易增加失败次数累计错误，flowId:" + qrPayFlowDTO.getId());
            }
        }
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_22.getCode());
        if(qrPayFlowDTO.getTransType() != StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode()){
            rechargeFlowService.channelLimitRollback(System.currentTimeMillis(),qrPayFlowDTO.getGatewayId(),qrPayFlowDTO.getPayAmount(),request);
        }
        doBatchAmountOutRollBack(qrPayFlowDTO,request);
        updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);

    }

    @Override
    public void dealFirstDeal(QrPayFlowDTO qrPayFlowDTO, UserDTO payUser, HttpServletRequest request) {
        try {
            // 更新用户状态为首次成功
            if(payUser.getFirstDealState() == StaticDataEnum.STATUS_0.getCode()){
                userService.updateFirstDealState(payUser.getId());
            }
            // 判断用户已有状态,是否有推荐人
            if(payUser.getFirstDealState() == StaticDataEnum.STATUS_1.getCode() ||payUser.getInviterId() == null ){
                return;
            }
            // 红包入账
            userService.walletBooked(payUser.getInviterId(), StaticDataEnum.ACC_FLOW_TRANS_TYPE_20.getCode(),null, request, qrPayFlowDTO.getId());
        }catch (Exception e){
            log.error("dealFirstDeal flowId:" +qrPayFlowDTO.getId()+ ",Exception:"+e.getMessage()+",message:"+e);
        }
    }


    @Override
    public JSONObject getQrPayTransAmount(JSONObject data, HttpServletRequest request) throws Exception {
        QrPayDTO qrPayDTO = JSONObject.parseObject(data.toJSONString(), QrPayDTO.class);

        //交易金额不为空
        if (StringUtils.isEmpty(qrPayDTO.getTransAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.empty", getLang(request)));
        }

        //交易金额格式校验
        if (!RegexUtils.isTransAmt(qrPayDTO.getTransAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        //用户userId不能为空
        if (StringUtils.isEmpty(qrPayDTO.getPayUserId().toString()) || StringUtils.isEmpty(qrPayDTO.getRecUserId().toString())) {
            throw new BizException(I18nUtils.get("userId.null", getLang(request)));
        }

        //验证交易方式
        if (StringUtils.isEmpty(qrPayDTO.getPayType().toString())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }
        //获取交易双方user信息
        UserDTO payUser = userService.findUserById(qrPayDTO.getPayUserId());
        UserDTO recUser = userService.findUserById(qrPayDTO.getRecUserId());

        //判断用户是否存在
        if (payUser == null || recUser == null || payUser.getId() == null || recUser.getId() == null) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }


        Map<String,Object> params = new HashMap<>();
        params.put("id",qrPayDTO.getMerchantId());
        params.put("userId",qrPayDTO.getRecUserId());
        MerchantDTO merchantDTO = merchantService.findOneMerchant(params);
        if(merchantDTO == null || merchantDTO .getId() == null || merchantDTO.getIsAvailable() != StaticDataEnum.AVAILABLE_1.getCode()){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        //查询用户红包金额
        BigDecimal payUserAmount = userService.getBalance(qrPayDTO.getPayUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
        //查询商户整体出售剩余金额
        BigDecimal merchantAmount = userService.getBalance(qrPayDTO.getRecUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());

        //整体出售金额
        BigDecimal wholeSalesAmount = qrPayDTO.getTransAmount().min(merchantAmount);
        //正常出售金额
        BigDecimal normalSaleAmount = qrPayDTO.getTransAmount().subtract(wholeSalesAmount);

        // 营销折扣
        BigDecimal marketingDiscount = merchantDTO.getMarketingDiscount() == null ? BigDecimal.ZERO : merchantDTO.getMarketingDiscount();
        // 固定折扣
        BigDecimal baseDiscount ;
        // 额外折扣
        BigDecimal extraDiscount;
        // 整体出售折扣
        BigDecimal wholeSaleUserDiscount;

        // 支付方式最小实付金额 卡支付：0 ，分期付 4
        BigDecimal mixPayAmount ;
        if(qrPayDTO.getPayType() == StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode()){
            baseDiscount = merchantDTO.getBaseRate() == null ? BigDecimal.ZERO : merchantDTO.getBaseRate();
            mixPayAmount = new BigDecimal(StaticDataEnum.INSTALLMENT_MIN_AMT.getMessage());
            if(qrPayDTO.getTransAmount().compareTo(mixPayAmount)<0){
                throw new BizException(I18nUtils.get("credit.amount.limit", getLang(request), new String[]{StaticDataEnum.INSTALLMENT_MIN_AMT.getMessage()}));
            }
        }else{
            baseDiscount = merchantDTO.getBasePayRate() == null ? BigDecimal.ZERO : merchantDTO.getBasePayRate();
            mixPayAmount = BigDecimal.ZERO;
        }
        if(qrPayDTO.getPayType() == StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode()){
            wholeSaleUserDiscount = merchantDTO.getWholeSaleUserPayDiscount() == null ? BigDecimal.ZERO : merchantDTO.getWholeSaleUserPayDiscount() ;
        }else{
            wholeSaleUserDiscount = merchantDTO.getWholeSaleUserDiscount() == null ? BigDecimal.ZERO : merchantDTO.getWholeSaleUserDiscount() ;
        }
        //用户基础折扣金额
        BigDecimal baseDiscountAmount = normalSaleAmount.multiply(baseDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        //用户时间内营销折扣金额
        Long today = System.currentTimeMillis();
        Long extraDiscountPeriod = merchantDTO.getExtraDiscountPeriod();
        if (today.longValue() < extraDiscountPeriod.longValue()) {
            extraDiscount = merchantDTO.getExtraDiscount();
        } else {
            extraDiscount = new BigDecimal("0.00");
        }
        BigDecimal extraDiscountAmount = normalSaleAmount.multiply(extraDiscount == null ? BigDecimal.ZERO :extraDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        // 计算营销折扣金额
        BigDecimal markingDiscountAmount = normalSaleAmount.multiply(marketingDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);

        // 整体出售折扣金额
        BigDecimal wholeSaleUserDiscountAmount = wholeSalesAmount.multiply(wholeSaleUserDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        // 实付金额(无红包) = 整体出售折后 + 正常交易折后（正常交易 - 基础折扣 - 时间内营销折扣 -营销折扣）
        BigDecimal payAmountNoRedEnvelope = wholeSalesAmount.subtract(wholeSaleUserDiscountAmount).add(
                normalSaleAmount.subtract(baseDiscountAmount).subtract(extraDiscountAmount).subtract(markingDiscountAmount)
        );
        // 实付金额(有红包) = 整体出售折后 + 正常交易折后（正常交易 - 基础折扣 - 时间内营销折扣 -营销折扣） - 红包金额
        BigDecimal redEnvelopeAmount = payAmountNoRedEnvelope.min(payUserAmount);
        BigDecimal payAmountRedEnvelope = payAmountNoRedEnvelope.subtract(redEnvelopeAmount);

        BigDecimal channelFeeRedEnvelope = BigDecimal.ZERO;
        BigDecimal channelFeeNoRedEnvelope = BigDecimal.ZERO;
        BigDecimal transChannelFeeRate = BigDecimal.ZERO;
        // 如果有实付金额而且通道是卡支付，需要计算通道手续费
        if(qrPayDTO.getPayType() == StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode() ){
            // 查询绑卡列表
            params.clear();
            params.put("gatewayType",qrPayDTO.getPayType());
            params.put("state",StaticDataEnum.STATUS_1.getCode());
            GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
            if(gatewayDTO == null || gatewayDTO.getId() == null){
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            List<CardDTO> cardDTOList = new ArrayList<>();
            if(gatewayDTO.getType().toString() .equals(StaticDataEnum.GATEWAY_TYPE_0.getCode()+"") ){
                // 如果是LayPay,查询卡费率
                CardDTO lastCard = new CardDTO();
                if(qrPayDTO.getCardId() == null ){
                    JSONObject cardInfo =serverService.getAccountInfo(qrPayDTO.getPayUserId());
                    JSONArray cardDTOListFromServer = cardInfo.getJSONArray("cardDTOList");
                    for (int i = 0; i < cardDTOListFromServer.size(); i ++) {
                        if (cardDTOListFromServer.getJSONObject(i).getInteger("type").intValue() == StaticDataEnum.TIE_CARD_1.getCode()) {
                            CardDTO cardDTO = JSONObject.parseObject(cardDTOListFromServer.getJSONObject(i).toJSONString(), CardDTO.class);
                            cardDTOList.add(cardDTO);
                        }
                    }

                    if(cardDTOList != null && cardDTOList.size() > 0){
                        List<CardDTO> listPreset = cardDTOList.stream().filter(dto->dto.getPreset() == StaticDataEnum.STATUS_1.getCode()).collect(Collectors.toList());
                        if(listPreset != null && listPreset.size() > 0 ){
                            lastCard = listPreset.get(0);
                        }else{
                            List<CardDTO> list = cardDTOList.stream().sorted(Comparator.comparing(CardDTO::getOrder,Comparator.reverseOrder())).collect(Collectors.toList());
                            lastCard = list.get(0);
                        }
                    }
                }else{
                    //获取卡信息
                    JSONObject cardObj;
                    try {
                        cardObj = serverService.getCardInfo(qrPayDTO.getCardId());
                    } catch (Exception e) {
                        //查询异常交易失败
                        throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
                    }
                    if (cardObj == null) {
                        //无信息返回，交易失败
                        throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
                    }
                    //卡信息
                    lastCard = JSONObject.parseObject(cardObj.toJSONString(), CardDTO.class);
                }

                params.clear();
                params.put("gatewayId",gatewayDTO.getType());
                params.put("code",lastCard.getCardCategory() == null ? 11 : lastCard.getCardCategory());
                ChannelFeeConfigDTO channelFeeConfigDTO = channelFeeConfigService.findOneChannelFeeConfig(params);
                if(channelFeeConfigDTO == null || channelFeeConfigDTO.getId() == null ){
                    throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
                }
                gatewayDTO = new GatewayDTO();
                gatewayDTO.setRate(channelFeeConfigDTO.getRate());
                gatewayDTO.setRateType(channelFeeConfigDTO.getType());
                transChannelFeeRate = gatewayDTO.getRate();
            }else{
                transChannelFeeRate = gatewayDTO.getRate();
            }


            if(payAmountRedEnvelope.compareTo(BigDecimal.ZERO) > 0){
                channelFeeRedEnvelope = gatewayService.getGateWayFee(gatewayDTO,payAmountRedEnvelope);
                payAmountRedEnvelope = payAmountRedEnvelope.add(channelFeeRedEnvelope);
            }
            if(payAmountNoRedEnvelope.compareTo(BigDecimal.ZERO) > 0){
                channelFeeNoRedEnvelope = gatewayService.getGateWayFee(gatewayDTO,payAmountNoRedEnvelope);
                payAmountNoRedEnvelope = payAmountNoRedEnvelope.add(channelFeeNoRedEnvelope);
            }
        }

        log.info("payAmountRedEnvelope:"+payAmountRedEnvelope+",payAmountNoRedEnvelope:"+payAmountNoRedEnvelope);

        String transNo = "";
        String transNoType = "";
        String orderNumber = SnowflakeUtil.generateId().toString();;

        BigDecimal wholeSalesUserDiscount ;
        BigDecimal normalSalesUserDiscount;
        if(qrPayDTO.getPayType() == StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode() ){
            // 支付
            transNoType = "TPC";
        }else if(qrPayDTO.getPayType() == StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode() ){
            // 分期付
            transNoType = "INS";
        }else{
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }
        if(normalSaleAmount.compareTo(BigDecimal.ZERO) > 0){
            if(wholeSalesAmount.compareTo(BigDecimal.ZERO)>0){
                // 混合出售
                transNo = "M"  + transNoType + orderNumber;
                wholeSalesUserDiscount = wholeSaleUserDiscount;
                normalSalesUserDiscount = marketingDiscount.add(baseDiscount).add(extraDiscount);
            }else{
                // 正常出售
                transNo = "B" + transNoType + orderNumber;
                wholeSalesUserDiscount = BigDecimal.ZERO;
                normalSalesUserDiscount = marketingDiscount.add(baseDiscount).add(extraDiscount);
            }
        }else{
            // 整体出售
            transNo = "W" + transNoType + orderNumber;
            wholeSalesUserDiscount = wholeSaleUserDiscount;
            normalSalesUserDiscount = BigDecimal.ZERO;
        }

        //edit by zhangzeyuan 将系统订单号更新到pos订单流水表中
        String posTransNo = qrPayDTO.getPosTransNo();
        if(StringUtils.isNotBlank(posTransNo) && Objects.isNull(qrPayDTO.getCardId())){
            //POS订单号不为空 并且 卡ID为空
            try {
                posQrPayFlowService.updateSysTransNoByThirdNo(transNo, posTransNo, getUserId(request), System.currentTimeMillis());
            }catch (Exception e){
                log.error("将系统订单号更新到pos订单流水表中出错");
                e.printStackTrace();
            }
        }

        Integer repeatSaleState = 0;
        // 正常扫商家码，判断5分钟之内有没有成功的付款
        if (StringUtils.isBlank(posTransNo)){
            Long now = System.currentTimeMillis();
            Long start = now - 5 * 60 * 1000;
            params.clear();
            params.put("start",start);
            params.put("end",now);
            params.put("payUserId",qrPayDTO.getPayUserId());
            params.put("merchantId",qrPayDTO.getMerchantId());
            params.put("transAmount",qrPayDTO.getTransAmount());
            List<QrPayFlowDTO> qrPayFlowDTOS = qrPayFlowService.find(params,null ,null );
            if(qrPayFlowDTOS != null && qrPayFlowDTOS.size() > 0){
                for (QrPayFlowDTO qrPayFlowDTO : qrPayFlowDTOS){
                    if(qrPayFlowDTO.getState() != StaticDataEnum.TRANS_STATE_2.getCode() &&
                            qrPayFlowDTO.getState() != StaticDataEnum.TRANS_STATE_12.getCode() &&
                            qrPayFlowDTO.getState() != StaticDataEnum.TRANS_STATE_22.getCode() ){
                        repeatSaleState = 1;
                        break;
                    }
                }
            }

        }

        // 返回结果
        JSONObject result = new JSONObject();
        result.put("transNo",transNo);
        result.put("transAmount",qrPayDTO.getTransAmount().toString());
        result.put("payAmount",payAmountNoRedEnvelope.toString());
        result.put("payAmountUseRedEnvelope",payAmountRedEnvelope.toString());
        result.put("totalRedEnvelopeAmount",payUserAmount.toString());
        result.put("redEnvelopeAmount",redEnvelopeAmount.toString());
        result.put("wholeSalesAmount",wholeSalesAmount.toString());
        result.put("normalSaleAmount",normalSaleAmount.toString());
        result.put("wholeSaleUserDiscountAmount",wholeSaleUserDiscountAmount.toString());
        result.put("normalSaleUserDiscountAmount",baseDiscountAmount.add(extraDiscountAmount).add(markingDiscountAmount).toString());
        result.put("channelFeeRedEnvelope",channelFeeRedEnvelope.toString());
        result.put("channelFeeNoRedEnvelope",channelFeeNoRedEnvelope.toString());
        result.put("transChannelFeeRate",transChannelFeeRate.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
        result.put("normalSalesUserDiscount",normalSalesUserDiscount.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
        result.put("wholeSalesUserDiscount",wholeSalesUserDiscount.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
        result.put("mixPayAmount",mixPayAmount.toString());
        result.put("repeatSaleState",repeatSaleState.toString());
        return result;
    }

    @Override
    public JSONObject getPayTransAmountDetail(@NonNull JSONObject data, HttpServletRequest request) throws Exception {
        QrPayDTO qrPayDTO = JSONObject.parseObject(data.toJSONString(), QrPayDTO.class);

        //交易金额
        BigDecimal transAmount = qrPayDTO.getTransAmount();
        //付款用户id
        Long payUserId = qrPayDTO.getPayUserId();
        //收款用户id
        Long recUserId = qrPayDTO.getRecUserId();
        //支付方式
        Integer payType = qrPayDTO.getPayType();
        //商户id
        Long merchantId = qrPayDTO.getMerchantId();

        //使用红包金额
        BigDecimal useRedEnvelopeAmount = BigDecimal.ZERO;
        if(Objects.nonNull(qrPayDTO.getRedEnvelopeAmount())){
            useRedEnvelopeAmount = qrPayDTO.getRedEnvelopeAmount();
        }

        //小费金额
        BigDecimal tipAmount = BigDecimal.ZERO;
        if(Objects.nonNull(qrPayDTO.getTipAmount())){
            tipAmount = qrPayDTO.getTipAmount();
        }


        //传参校验
        //交易金额不为空
        if (StringUtils.isEmpty(transAmount.toString()) || !RegexUtils.isTransAmt(transAmount.toString())) {
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        //用户userId不能为空
        if (StringUtils.isEmpty(payUserId.toString()) || StringUtils.isEmpty(recUserId.toString())) {
            throw new BizException(I18nUtils.get("userId.null", getLang(request)));
        }

        //验证交易方式
        if (StringUtils.isEmpty(payType.toString())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //验证商户ID
        if (Objects.isNull(merchantId)) {
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //获取交易双方user信息
        UserDTO payUser = userService.findUserInfoV2(qrPayDTO.getPayUserId());
        UserDTO recUser = userService.findUserInfoV2(qrPayDTO.getRecUserId());

        //判断用户是否存在
        if (payUser == null || recUser == null || payUser.getId() == null || recUser.getId() == null) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }

        //查询商户信息
        Map<String,Object> params = new HashMap<>();

        params.put("id", merchantId);
        params.put("userId",qrPayDTO.getRecUserId());
        params.put("isAvailable", StaticDataEnum.AVAILABLE_1.getCode());
        MerchantDTO merchantDTO = merchantService.findOneMerchant(params);
        if(merchantDTO == null || merchantDTO .getId() == null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode())){
            //卡支付
            return getCardPayTransAmountDetail(qrPayDTO.getTransNo(), transAmount, payUserId, recUserId, payType, merchantId, useRedEnvelopeAmount, payUser,
                    qrPayDTO.getCardId(), qrPayDTO.getPosTransNo(), merchantDTO, qrPayDTO.getDonationInstiuteId() , tipAmount, request);
        }else if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){
            //分期付
            return getCreditPayTransAmountDetail(qrPayDTO.getTransNo(), transAmount, payUserId, recUserId, payType, merchantId, useRedEnvelopeAmount, payUser,
                    qrPayDTO.getCardId(), qrPayDTO.getPosTransNo(), merchantDTO, qrPayDTO.getDonationInstiuteId(), tipAmount, request);
        }else{
            //未知交易类型
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }
    }



    @Override
    public JSONObject getPayTransAmountDetailV3(@NonNull JSONObject data, HttpServletRequest request) throws Exception {
        QrPayDTO qrPayDTO = JSONObject.parseObject(data.toJSONString(), QrPayDTO.class);

        //交易金额
        BigDecimal transAmount = qrPayDTO.getTransAmount();
        //付款用户id
        Long payUserId = qrPayDTO.getPayUserId();
        //收款用户id
        Long recUserId = qrPayDTO.getRecUserId();
        //支付方式
        Integer payType = qrPayDTO.getPayType();
        //商户id
        Long merchantId = qrPayDTO.getMerchantId();

        //小费金额
        BigDecimal tipAmount = BigDecimal.ZERO;
        if(Objects.nonNull(qrPayDTO.getTipAmount())){
            if (!RegexUtils.isTransAmt(qrPayDTO.getTipAmount().toString())) {
                throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
            }
            tipAmount = qrPayDTO.getTipAmount();
        }

        //使用红包金额
        BigDecimal useRedEnvelopeAmount = BigDecimal.ZERO;

        //传参校验
        //交易金额不为空
        if (StringUtils.isEmpty(transAmount.toString()) || !RegexUtils.isTransAmt(transAmount.toString())) {
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        //用户userId不能为空
        if (StringUtils.isEmpty(payUserId.toString()) || StringUtils.isEmpty(recUserId.toString())) {
            throw new BizException(I18nUtils.get("userId.null", getLang(request)));
        }

        //验证交易方式
        if (StringUtils.isEmpty(payType.toString())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //验证商户ID
        if (Objects.isNull(merchantId)) {
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //获取交易双方user信息
        UserDTO payUser = userService.findUserInfoV2(qrPayDTO.getPayUserId());
//        UserDTO recUser = userService.findUserInfoV2(qrPayDTO.getRecUserId());

        //判断用户是否存在
        if (payUser == null || payUser.getId() == null) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }


        if (null != qrPayDTO.getRedEnvelopeAmount()) {
            useRedEnvelopeAmount = qrPayDTO.getRedEnvelopeAmount();
        }

        //查询商户信息
        Map<String,Object> params = new HashMap<>();

        params.put("id", merchantId);
        params.put("userId",qrPayDTO.getRecUserId());
        params.put("isAvailable", StaticDataEnum.AVAILABLE_1.getCode());
        MerchantDTO merchantDTO = merchantService.findOneMerchant(params);
        if(merchantDTO == null || merchantDTO .getId() == null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode())){
            //卡支付
            return getCardPayTransAmountDetailV3(qrPayDTO.getTransNo(), transAmount, payUserId, recUserId, payType, merchantId, useRedEnvelopeAmount, payUser,
                    qrPayDTO.getCardId(), qrPayDTO.getPosTransNo(), merchantDTO, qrPayDTO.getDonationInstiuteId() , tipAmount, request);
        }else if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){
            //分期付
            return getCreditPayTransAmountDetailV3(qrPayDTO.getTransNo(), transAmount, payUserId, recUserId, payType, merchantId, useRedEnvelopeAmount, payUser,
                    qrPayDTO.getCardId(), qrPayDTO.getPosTransNo(), merchantDTO, qrPayDTO.getDonationInstiuteId(), tipAmount, request);
        }else{
            //未知交易类型
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }
    }


    /**
     * 获取卡支付交易金额信息
     * @author zhangzeyuan
     * @date 2021/6/18 17:11
     * @param transAmount
     * @param payUserId
     * @param recUserId
     * @param payType
     * @param merchantId
     * @param useRedEnvelopeAmount
     * @param payUser
     * @param cardId
     * @param posTransNumber
     * @param merchantDTO
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     */
    private JSONObject getCardPayTransAmountDetail(String transNo, BigDecimal transAmount, Long payUserId, Long recUserId,
                                                   Integer payType, Long merchantId, BigDecimal useRedEnvelopeAmount,
                                                   UserDTO payUser, Long cardId, String posTransNumber, MerchantDTO merchantDTO, String donationInstiuteId,
                                                   BigDecimal tipAmount, HttpServletRequest request)throws Exception{
        //返回结果
        JSONObject result = new JSONObject();

        //验证卡支付绑卡
        if(!payUser.getCardState().equals(StaticDataEnum.USER_CARD_STATE_BINDED.getCode())){
            throw new BizException(I18nUtils.get("pay.card.not.bound", getLang(request)));
        }

        //订单金额必须大于0
        if(transAmount.compareTo(BigDecimal.ZERO) <= 0){
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        //根据金额计算实付金额
        BigDecimal realPayAmount = getTransactionTruelyPayAmount(transNo, transAmount, payUserId, recUserId, useRedEnvelopeAmount,
                merchantDTO, payType, result, Constant.ORDER_TRANS_TYPE_CARD_FLAG, request);

        //捐赠金额
        BigDecimal donationAmount = BigDecimal.ZERO;
        DonationInstituteDataDTO donationInstituteData = donationInstituteService.getDonationDataById(donationInstiuteId);
        if(null != donationInstituteData && null != donationInstituteData.getId()){
            if(StringUtils.isNotBlank(donationInstiuteId)){
                donationAmount = donationInstituteData.getDonationAmount();
            }
            result.put("donationData", donationInstituteData);
        }else {
            result.put("donationData", new DonationInstituteDataDTO());
        }

        //根据订单金额获取小费列表
        JSONArray tipList = getTipListByOrderAmount(transAmount);
        result.put("tipList", tipList);


        //需要计算手续费的总金额  实付 + 捐赠费（如果有） + 小费（如果有）
        BigDecimal calculateFeeAmount = realPayAmount.add(donationAmount).add(tipAmount);

        //通道手续费金额
        BigDecimal channelFeeAmount = BigDecimal.ZERO;
        //手续费率
        BigDecimal transChannelFeeRate = BigDecimal.ZERO;

        //获取通道信息
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("gatewayType", payType);
        params.put("state", StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);

        if(gatewayDTO == null || gatewayDTO.getId() == null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //获取使用卡或者默认卡信息
        CardDTO cardInfo = getCardInfoById(cardId, payUserId, request);
        result.put("cardId", cardInfo.getId().toString());
        result.put("cardNo", cardInfo.getCardNo());
        result.put("cardCcType", cardInfo.getCustomerCcType());

        if(calculateFeeAmount.compareTo(BigDecimal.ZERO) > 0){
            //计算通到手续费
            Map<String, Object> feeMap = getCardPayTransactionFee(payType, cardInfo.getCustomerCcType(), calculateFeeAmount, request);

            channelFeeAmount = (BigDecimal) feeMap.get("channelFeeAmount");
            transChannelFeeRate = (BigDecimal) feeMap.get("transChannelFeeRate");
        }

        // POS订单号处理 POS订单号不为空 并且 卡ID为空 将系统订单号更新到pos订单流水表中
        if(StringUtils.isNotBlank(posTransNumber)){
            posQrPayFlowService.updateSysTransNoByThirdNo(result.getString("transNo"), posTransNumber, getUserId(request), System.currentTimeMillis());
        }

        // 正常扫商家码，判断5分钟之内有没有成功的付款
        Integer repeatSaleState = getPayUnSuccessStateInFiveMin(merchantId, transAmount, payUserId, posTransNumber);

        //订单实付 + 捐赠费 + 小费 + 手续费总金额
        result.put("allPayAmount", calculateFeeAmount.add(channelFeeAmount).toString());
        //订单金额
        result.put("transAmount", transAmount.toString());

        //订单实付金额 + 捐赠金额 + 小费
        result.put("toShowFeeAllAmount",  calculateFeeAmount.toString());

        //扣除完 红包 、 折扣的金额
        result.put("payAmount", realPayAmount.toString());
        //手续费金额
        result.put("channelFeeAmount", channelFeeAmount.toString());
        //通道手续费率
        result.put("transChannelFeeRate", transChannelFeeRate.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        result.put("repeatSaleState", repeatSaleState.toString());
        //卡支付最小支付金额
        result.put("mixPayAmount", "0");

        //捐赠金额
        result.put("donationAmount", donationAmount.toString());
        //小费金额
        result.put("tipAmount", tipAmount.toString());

        return result;
    }


    private JSONObject getCardPayTransAmountDetailV3(String transNo, BigDecimal transAmount, Long payUserId, Long recUserId,
                                                     Integer payType, Long merchantId, BigDecimal useRedEnvelopeAmount,
                                                     UserDTO payUser, Long cardId, String posTransNumber, MerchantDTO merchantDTO, String donationInstiuteId,
                                                     BigDecimal tipAmount, HttpServletRequest request)throws Exception{
        //返回结果
        JSONObject result = new JSONObject();

        //验证卡支付绑卡
        /*if(!payUser.getCardState().equals(StaticDataEnum.USER_CARD_STATE_BINDED.getCode())){
            throw new BizException(I18nUtils.get("pay.card.not.bound", getLang(request)));
        }*/

        //订单金额必须大于0
        if(transAmount.compareTo(BigDecimal.ZERO) <= 0){
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        //根据金额计算实付金额
        BigDecimal realPayAmount = getTransactionTruelyPayAmountV3(transNo, transAmount, payUserId, recUserId, useRedEnvelopeAmount,
                merchantDTO, payType, result, Constant.ORDER_TRANS_TYPE_CARD_FLAG, request);

        //捐赠金额
        BigDecimal donationAmount = BigDecimal.ZERO;
        DonationInstituteDataDTO donationInstituteData = donationInstituteService.getDonationDataById(donationInstiuteId);
        if(null != donationInstituteData && null != donationInstituteData.getId()){
            if(StringUtils.isNotBlank(donationInstiuteId)){
                donationAmount = donationInstituteData.getDonationAmount();
            }
            result.put("donationData", donationInstituteData);
        }else {
            result.put("donationData", new DonationInstituteDataDTO());
        }

        //根据订单金额获取小费列表
        JSONArray tipList = getTipListByOrderAmount(transAmount);
        result.put("tipList", tipList);


        //需要计算手续费的总金额  实付 + 捐赠费（如果有） + 小费（如果有）
        BigDecimal calculateFeeAmount = realPayAmount.add(donationAmount).add(tipAmount);

        /*//通道手续费金额
        BigDecimal channelFeeAmount = BigDecimal.ZERO;
        //手续费率
        BigDecimal transChannelFeeRate = BigDecimal.ZERO;

        //获取通道信息
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("gatewayType", payType);
        params.put("state", StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);

        if(gatewayDTO == null || gatewayDTO.getId() == null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }*/

        //获取使用卡或者默认卡信息
        CardDTO cardInfo = getCardInfoById(cardId, payUserId, request);
        result.put("cardId", Objects.isNull(cardInfo.getId()) ? "" :  cardInfo.getId().toString());
        result.put("cardNo", StringUtils.isNotBlank(cardInfo.getCardNo()) ? cardInfo.getCardNo() : "");
        result.put("cardCcType", StringUtils.isNotBlank(cardInfo.getCustomerCcType()) ? cardInfo.getCustomerCcType() : "");
        result.put("customerCcExpyr", StringUtils.isNotBlank(cardInfo.getCustomerCcExpyr()) ? cardInfo.getCustomerCcExpyr() : "");
        result.put("customerCcExpmo", StringUtils.isNotBlank(cardInfo.getCustomerCcExpmo()) ? cardInfo.getCustomerCcExpmo() : "");
        /*if(calculateFeeAmount.compareTo(BigDecimal.ZERO) > 0){
            //计算通到手续费
            Map<String, Object> feeMap = getCardPayTransactionFee(payType, cardInfo.getCustomerCcType(), calculateFeeAmount, request);

            channelFeeAmount = (BigDecimal) feeMap.get("channelFeeAmount");
            transChannelFeeRate = (BigDecimal) feeMap.get("transChannelFeeRate");
        }
*/
        // POS订单号处理 POS订单号不为空 并且 卡ID为空 将系统订单号更新到pos订单流水表中
        /*if(StringUtils.isNotBlank(posTransNumber)){
            posQrPayFlowService.updateSysTransNoByThirdNo(result.getString("transNo"), posTransNumber, getUserId(request), System.currentTimeMillis());
        }*/

        // 正常扫商家码，判断5分钟之内有没有成功的付款
        Integer repeatSaleState = getPayUnSuccessStateInFiveMin(merchantId, transAmount, payUserId, posTransNumber);

        //订单实付 + 捐赠费 + 小费 + 手续费总金额
        result.put("allPayAmount", calculateFeeAmount.toString());
        //订单金额
        result.put("transAmount", transAmount.toString());

        //订单实付金额 + 捐赠金额 + 小费
        result.put("toShowFeeAllAmount",  calculateFeeAmount.toString());

        //扣除完 红包 、 折扣的金额
        result.put("payAmount", realPayAmount.toString());
        /*//手续费金额
        result.put("channelFeeAmount", channelFeeAmount.toString());
        //通道手续费率
        result.put("transChannelFeeRate", transChannelFeeRate.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
*/
        result.put("repeatSaleState", repeatSaleState.toString());
        //卡支付最小支付金额
        result.put("mixPayAmount", "0");

        //捐赠金额
        result.put("donationAmount", donationAmount.toString());
        //小费金额
        result.put("tipAmount", tipAmount.toString());

        return result;
    }


    /**
     *
     * @author zhangzeyuan
     * @date 2021/6/23 16:51
     * @param transAmount 订单金额
     * @param payUserId 付款用户ID
     * @param recUserId 收款用户Id
     * @param useRedEnvelopeAmount 使用红包金额
     * @param merchantDTO 商户实体
     * @param payType 支付类型
     * @param result 返回信息
     * @param request
     * @return java.math.BigDecimal
     */
    private BigDecimal getTransactionTruelyPayAmount(String transNo, BigDecimal transAmount, Long payUserId, Long recUserId,
                                                     BigDecimal useRedEnvelopeAmount, MerchantDTO merchantDTO, Integer payType,
                                                     JSONObject result, String orderNumberTransTypeFlag, HttpServletRequest request) throws Exception{

        BigDecimal payUserAmount = BigDecimal.ZERO;
        //查询商户整体出售剩余金额
        BigDecimal merchantAmount = BigDecimal.ZERO;
        //固定折扣 营销折扣 额外折扣
        BigDecimal baseDiscount = BigDecimal.ZERO;
        BigDecimal marketingDiscount = BigDecimal.ZERO;
        BigDecimal extraDiscount = BigDecimal.ZERO;
        //整体出售折扣
        BigDecimal wholeSaleDiscount = BigDecimal.ZERO;


        Map<?, Object> amountMap = null;

        //需要放入redis数据状态
        boolean putDataRedisStatus = false;

        if(StringUtils.isBlank(transNo)){
            //第一次生成订单信息 查询计算生成折扣金额 额度信息
            amountMap = getOrderDiscount(payUserId, recUserId, merchantDTO, payType);

            putDataRedisStatus = true;
        }else {
            //输入红包调用接口 取redis信息
            amountMap = redisUtils.hmget(Constant.getOrderAmountDetailRedisKey(payUserId.toString(), transNo));
            if(null == amountMap || amountMap.isEmpty()){
                //为空重新计算
                amountMap = getOrderDiscount(payUserId, recUserId, merchantDTO, payType);
                putDataRedisStatus = true;
            }
        }

        payUserAmount = (BigDecimal) amountMap.get("payUserAmount");
        merchantAmount = (BigDecimal) amountMap.get("merchantAmount");

        wholeSaleDiscount = (BigDecimal) amountMap.get("wholeSaleDiscount");

        extraDiscount = (BigDecimal) amountMap.get("extraDiscount");
        marketingDiscount = (BigDecimal) amountMap.get("marketingDiscount");
        baseDiscount = (BigDecimal) amountMap.get("baseDiscount");

        BigDecimal tempTransAmount = transAmount;

        //校验 使用红包金额 是否大于 可用红包金额
        if(useRedEnvelopeAmount.compareTo(payUserAmount) > 0){
            throw new BizException(I18nUtils.get("pay.red.envelope.over.limit", getLang(request)));
        }

        //使用整体出售金额
        BigDecimal useWholeSalesAmount = tempTransAmount.min(merchantAmount);

        //使用正常出售金额
        BigDecimal useNormalSaleAmount = tempTransAmount.subtract(useWholeSalesAmount);

        //整体出售使用红包金额
        BigDecimal wholeSaleUseRedAmount = BigDecimal.ZERO;

        if(useRedEnvelopeAmount.compareTo(BigDecimal.ZERO) > 0){
            if(useWholeSalesAmount.compareTo(useRedEnvelopeAmount) >= 0){
                wholeSaleUseRedAmount = useRedEnvelopeAmount;
            }else {
                wholeSaleUseRedAmount = useWholeSalesAmount;
            }
        }
        //正常出售使用红包金额
        BigDecimal normalSaleUesRedAmount = useRedEnvelopeAmount.subtract(wholeSaleUseRedAmount);

        //正常出售折扣金额  固定折扣金额 +  营销折扣金额 + 额外折扣金额
        BigDecimal baseDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(baseDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal extraDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(extraDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal marketingDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(marketingDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);

        BigDecimal normalSaleDiscountAmount = baseDiscountAmount.add(extraDiscountAmount).add(marketingDiscountAmount);

        // 整体出售折扣金额 (整体出售金额 - 整体出售使用红包金额 ）* 整体出售折扣
        BigDecimal wholeSaleDiscountAmount = (useWholeSalesAmount.subtract(wholeSaleUseRedAmount)).multiply(wholeSaleDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);

        // 实付金额 = （整体出售金额 - 整体出售红包金额 - 整体出售折扣金额）  + （正常交易金额 - 正常使用红包金额  -  正常折扣金额）
        BigDecimal realPayAmount = useWholeSalesAmount.subtract(wholeSaleUseRedAmount).subtract(wholeSaleDiscountAmount)
                .add(useNormalSaleAmount.subtract(normalSaleUesRedAmount).subtract(normalSaleDiscountAmount));


        //业务类型订单号标识
        String orderNumberFlag = "";

        //实际折扣
        BigDecimal realWholeSaleDiscount;
        //正常出售折扣
        BigDecimal realNormalSaleDiscount;

        //计算实际折扣、订单号标识
        if(useNormalSaleAmount.compareTo(BigDecimal.ZERO) > 0){
            realNormalSaleDiscount = baseDiscount.add(extraDiscount).add(marketingDiscount);
            //使用了正常出售额度
            if(useWholeSalesAmount.compareTo(BigDecimal.ZERO) > 0){
                // 混合出售
                orderNumberFlag = "M";
                realWholeSaleDiscount = wholeSaleDiscount;
            }else{
                // 正常出售
                orderNumberFlag = "B";
                realWholeSaleDiscount = BigDecimal.ZERO;
            }
        }else{
            //整体出售
            orderNumberFlag = "W";
            realWholeSaleDiscount = wholeSaleDiscount;
            realNormalSaleDiscount = BigDecimal.ZERO;
        }

        if(StringUtils.isBlank(transNo)){
            //生成订单号
            transNo = orderNumberFlag + orderNumberTransTypeFlag + SnowflakeUtil.generateId().toString();
        }

        //订单号信息
        result.put("transNo", transNo);

        //扣除完 红包 、 折扣的金额
        result.put("payAmount", realPayAmount);

        //可用红包总金额
        result.put("totalRedEnvelopeAmount", payUserAmount.toString());

        //用户使用红包总金额
        result.put("redEnvelopeAmount", useRedEnvelopeAmount.toString());

        //整体出售金额
        result.put("wholeSalesAmount", useWholeSalesAmount.toString());
        //整体出售折扣金额
        result.put("wholeSaleUserDiscountAmount", wholeSaleDiscountAmount.toString());
        //整体出售折扣
        result.put("wholeSalesUserDiscount", realWholeSaleDiscount.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        //正常出售金额
        result.put("normalSaleAmount", useNormalSaleAmount.toString());
        //正常出售折扣金额
        result.put("normalSaleUserDiscountAmount", normalSaleDiscountAmount.toString());
        //正常出售折扣
        result.put("normalSalesUserDiscount", realNormalSaleDiscount.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        //将红包额度、商户额度、折扣信息放入redis
        if(putDataRedisStatus){
            redisUtils.hmset(Constant.getOrderAmountDetailRedisKey(payUserId.toString(), transNo), (Map<String, Object>) amountMap, 300);
        }

        return realPayAmount;
    }

    /**
     *
     * @author zhoutt
     * @date 2021/10/28 16:51
     * @param transAmount 订单金额
     * @param payUserId 付款用户ID
     * @param recUserId 收款用户Id
     * @param useRedEnvelopeAmount 使用红包金额
     * @param merchantDTO 商户实体
     * @param payType 支付类型
     * @param result 返回信息
     * @param request
     * @return java.math.BigDecimal
     */
    private BigDecimal getTransactionTruelyPayAmountV3(String transNo, BigDecimal transAmount, Long payUserId, Long recUserId,
                                                       BigDecimal useRedEnvelopeAmount, MerchantDTO merchantDTO, Integer payType,
                                                       JSONObject result, String orderNumberTransTypeFlag, HttpServletRequest request) throws Exception{

        BigDecimal payUserAmount = BigDecimal.ZERO;
        //查询商户整体出售剩余金额
        BigDecimal merchantAmount = BigDecimal.ZERO;
        //固定折扣 营销折扣 额外折扣
        BigDecimal baseDiscount = BigDecimal.ZERO;
        BigDecimal marketingDiscount = BigDecimal.ZERO;
        BigDecimal extraDiscount = BigDecimal.ZERO;
        //整体出售折扣
        BigDecimal wholeSaleDiscount = BigDecimal.ZERO;


        Map<?, Object> amountMap = null;

        //需要放入redis数据状态
        boolean putDataRedisStatus = false;

        if(StringUtils.isBlank(transNo)){
            //第一次生成订单信息 查询计算生成折扣金额 额度信息
            amountMap = getOrderDiscount(payUserId, recUserId, merchantDTO, payType);

            putDataRedisStatus = true;
        }else {
            //输入红包调用接口 取redis信息
            amountMap = redisUtils.hmget(Constant.getOrderAmountDetailRedisKey(payUserId.toString(), transNo));
            if(null == amountMap || amountMap.isEmpty()){
                //为空重新计算
                amountMap = getOrderDiscount(payUserId, recUserId, merchantDTO, payType);
                putDataRedisStatus = true;
            }
        }

//        payUserAmount = (BigDecimal) amountMap.get("payUserAmount");
        merchantAmount = (BigDecimal) amountMap.get("merchantAmount");

        wholeSaleDiscount = (BigDecimal) amountMap.get("wholeSaleDiscount");

        extraDiscount = (BigDecimal) amountMap.get("extraDiscount");
        marketingDiscount = (BigDecimal) amountMap.get("marketingDiscount");
        baseDiscount = (BigDecimal) amountMap.get("baseDiscount");

        BigDecimal tempTransAmount = transAmount;

        // 红包金额有可能超额，就全部抵扣,计算实际使用红包的金额
        BigDecimal realUseRedEnvelopeAmount = useRedEnvelopeAmount.min(transAmount);

        //校验 使用红包金额 是否大于 可用红包金额
//        if(useRedEnvelopeAmount.compareTo(payUserAmount) > 0){
//            throw new BizException(I18nUtils.get("pay.red.envelope.over.limit", getLang(request)));
//        }

        //使用整体出售金额
        BigDecimal useWholeSalesAmount = tempTransAmount.min(merchantAmount);

        //使用正常出售金额
        BigDecimal useNormalSaleAmount = tempTransAmount.subtract(useWholeSalesAmount);

        //整体出售使用红包金额
        BigDecimal wholeSaleUseRedAmount = BigDecimal.ZERO;

        if(realUseRedEnvelopeAmount.compareTo(BigDecimal.ZERO) > 0){
            if(useWholeSalesAmount.compareTo(realUseRedEnvelopeAmount) >= 0){
                wholeSaleUseRedAmount = realUseRedEnvelopeAmount;
            }else {
                wholeSaleUseRedAmount = useWholeSalesAmount;
            }
        }
        //正常出售使用红包金额
        BigDecimal normalSaleUesRedAmount = realUseRedEnvelopeAmount.subtract(wholeSaleUseRedAmount);

        //正常出售折扣金额  固定折扣金额 +  营销折扣金额 + 额外折扣金额
        BigDecimal baseDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(baseDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal extraDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(extraDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal marketingDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(marketingDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);

        BigDecimal normalSaleDiscountAmount = baseDiscountAmount.add(extraDiscountAmount).add(marketingDiscountAmount);

        // 整体出售折扣金额 (整体出售金额 - 整体出售使用红包金额 ）* 整体出售折扣
        BigDecimal wholeSaleDiscountAmount = (useWholeSalesAmount.subtract(wholeSaleUseRedAmount)).multiply(wholeSaleDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);

        // 实付金额 = （整体出售金额 - 整体出售红包金额 - 整体出售折扣金额）  + （正常交易金额 - 正常使用红包金额  -  正常折扣金额）
        BigDecimal realPayAmount = useWholeSalesAmount.subtract(wholeSaleUseRedAmount).subtract(wholeSaleDiscountAmount)
                .add(useNormalSaleAmount.subtract(normalSaleUesRedAmount).subtract(normalSaleDiscountAmount));


        //业务类型订单号标识
        String orderNumberFlag = "";

        //实际折扣
        BigDecimal realWholeSaleDiscount;
        //正常出售折扣
        BigDecimal realNormalSaleDiscount;

        //计算实际折扣、订单号标识
        if(useNormalSaleAmount.compareTo(BigDecimal.ZERO) > 0){
            realNormalSaleDiscount = baseDiscount.add(extraDiscount).add(marketingDiscount);
            //使用了正常出售额度
            if(useWholeSalesAmount.compareTo(BigDecimal.ZERO) > 0){
                // 混合出售
                orderNumberFlag = "M";
                realWholeSaleDiscount = wholeSaleDiscount;
            }else{
                // 正常出售
                orderNumberFlag = "B";
                realWholeSaleDiscount = BigDecimal.ZERO;
            }
        }else{
            //整体出售
            orderNumberFlag = "W";
            realWholeSaleDiscount = wholeSaleDiscount;
            realNormalSaleDiscount = BigDecimal.ZERO;
        }

        if(StringUtils.isBlank(transNo)){
            //生成订单号
            transNo = orderNumberFlag + orderNumberTransTypeFlag + SnowflakeUtil.generateId().toString();
        }

        //订单号信息
        result.put("transNo", transNo);

        //扣除完 红包 、 折扣的金额
        result.put("payAmount", realPayAmount);

        //可用红包总金额
//        result.put("totalRedEnvelopeAmount", payUserAmount.toString());

        //用户使用红包总金额
        result.put("redEnvelopeAmount", useRedEnvelopeAmount.toString());

        //整体出售金额
        result.put("wholeSalesAmount", useWholeSalesAmount.toString());
        //整体出售折扣金额
        result.put("wholeSaleUserDiscountAmount", wholeSaleDiscountAmount.toString());
        //整体出售折扣
        result.put("wholeSalesUserDiscount", realWholeSaleDiscount.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        //正常出售金额
        result.put("normalSaleAmount", useNormalSaleAmount.toString());
        //正常出售折扣金额
        result.put("normalSaleUserDiscountAmount", normalSaleDiscountAmount.toString());
        //正常出售折扣
        result.put("normalSalesUserDiscount", realNormalSaleDiscount.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        //将红包额度、商户额度、折扣信息放入redis
        if(putDataRedisStatus){
            redisUtils.hmset(Constant.getOrderAmountDetailRedisKey(payUserId.toString(), transNo), (Map<String, Object>) amountMap, 300);
        }

        return realPayAmount;
    }


    /**
     * 获取用户折扣信息和额度信息
     * @author zhangzeyuan
     * @date 2021/6/30 23:10
     * @param payUserId
     * @param recUserId
     * @param merchantDTO
     * @param payType
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    private Map<String, Object> getOrderDiscount(Long payUserId, Long recUserId, MerchantDTO merchantDTO, Integer payType) throws Exception{

        //用户红包额度
        // todo V3版本不再使用，V3版本正式上线之后可以删除该逻辑
//        BigDecimal payUserAmount = userService.getBalance(payUserId, StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
        BigDecimal payUserAmount = BigDecimal.ZERO;
        //商户额度
        BigDecimal merchantAmount = userService.getBalance(recUserId, StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());

        //整体出售折扣
        BigDecimal wholeSaleDiscount = BigDecimal.ZERO;
        //固定折扣
        BigDecimal baseDiscount = BigDecimal.ZERO;
        if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode())){
            //卡支付
            if(Objects.nonNull(merchantDTO.getBasePayRate())){
                baseDiscount = merchantDTO.getBasePayRate();
            }
            if(Objects.nonNull(merchantDTO.getWholeSaleUserPayDiscount())){
                wholeSaleDiscount = merchantDTO.getWholeSaleUserPayDiscount();
            }
        }else if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){
            //分期付
            if(Objects.nonNull(merchantDTO.getBaseRate())){
                baseDiscount = merchantDTO.getBaseRate();
            }
            if(Objects.nonNull(merchantDTO.getWholeSaleUserDiscount())){
                wholeSaleDiscount = merchantDTO.getWholeSaleUserDiscount();
            }
        }
        //营销折扣
        BigDecimal marketingDiscount = Objects.isNull(merchantDTO.getMarketingDiscount()) ? BigDecimal.ZERO : merchantDTO.getMarketingDiscount();

        //额外折扣
        BigDecimal extraDiscount;
        //用户时间内营销折扣金额
        Long extraDiscountPeriod = merchantDTO.getExtraDiscountPeriod();
        if (System.currentTimeMillis() < extraDiscountPeriod.longValue()) {
            extraDiscount = merchantDTO.getExtraDiscount();
        } else {
            extraDiscount = BigDecimal.ZERO;
        }
        HashMap<String, Object> resultMap = Maps.newHashMapWithExpectedSize(7);
        resultMap.put("wholeSaleDiscount", wholeSaleDiscount);

        resultMap.put("payUserAmount", payUserAmount);
        resultMap.put("merchantAmount", merchantAmount);

        resultMap.put("extraDiscount", extraDiscount);
        resultMap.put("marketingDiscount", marketingDiscount);
        resultMap.put("baseDiscount", baseDiscount);

        return resultMap;
    }
    /**
     * 获取卡信息
     * @author zhangzeyuan
     * @date 2021/6/23 16:53
     * @param cardId 卡ID
     * @param payUserId 付款用户ID
     * @param request
     * @return com.uwallet.pay.main.model.dto.CardDTO
     */
    private CardDTO getCardInfoById(Long cardId, Long payUserId, HttpServletRequest request) throws Exception{
        CardDTO cardDTO = new CardDTO();

        if(null == cardId){
            try{
                JSONObject defaultCardInfo = userService.getDefaultCardInfo(getUserId(request), request);
                cardDTO = JSONObject.parseObject(defaultCardInfo.toJSONString(), CardDTO.class);
                Long cardId1 = defaultCardInfo.getLong("cardId");
                cardDTO.setId(cardId1);
            }catch (Exception e){
                log.error("获取默认卡出错",e.getMessage());
            }
        }else{
            //获取卡信息
            JSONObject cardObj;
            try {
                cardObj = serverService.getCardInfo(cardId);
            } catch (Exception e) {
                //查询异常交易失败
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            if (cardObj == null) {
                //无信息返回，交易失败
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            String latpayToken = cardObj.getString("crdStrgToken");
            String stripeToken = cardObj.getString("stripeToken");

            String expMonth = "";
            String expYear = "";

            try {
                if(StringUtils.isNotBlank(stripeToken)){
                    //stripe获取卡的过期日
                    JSONObject cardExpirationDate = cardService.getStripeCardExpirationDate(stripeToken, payUserId, request);
                    expYear = cardExpirationDate.getString("customerCcExpyr");
                    expMonth = cardExpirationDate.getString("customerCcExpmo");
                }else if(StringUtils.isNotBlank(latpayToken)){
                    //latpay获取卡的过期日
                    JSONObject cardDetails = userService.getCardDetails(latpayToken, request);

                    expYear = cardDetails.getString("customerCcExpyr");
                    expMonth = cardDetails.getString("customerCcExpmo");
                }
            } catch (Exception e) {
                log.error("查询卡过期日出错, cardId:",cardObj.getString("id") + "|userId:" + payUserId);
            }
            cardObj.put("customerCcExpyr", expYear);
            cardObj.put("customerCcExpmo", expMonth);
            //卡信息
            cardDTO = JSONObject.parseObject(cardObj.toJSONString(), CardDTO.class);
        }
        return cardDTO;

       /* // 如果是LayPay,查询卡费率
        CardDTO lastCard = new CardDTO();
        if(cardId == null){
            //查询默认卡
            List<CardDTO> cardDTOList = new ArrayList<>();
            JSONObject cardInfo = serverService.getAccountInfo(payUserId);
            JSONArray cardDTOListFromServer = cardInfo.getJSONArray("cardDTOList");
            for (int i = 0; i < cardDTOListFromServer.size(); i ++) {
                if (cardDTOListFromServer.getJSONObject(i).getInteger("type").intValue() == StaticDataEnum.TIE_CARD_1.getCode()) {
                    CardDTO cardDTO = JSONObject.parseObject(cardDTOListFromServer.getJSONObject(i).toJSONString(), CardDTO.class);
                    cardDTOList.add(cardDTO);
                }
            }

            if(cardDTOList != null && cardDTOList.size() > 0){
                List<CardDTO> listPreset = cardDTOList.stream().filter(dto->dto.getPreset() == StaticDataEnum.STATUS_1.getCode()).collect(Collectors.toList());
                if(listPreset != null && listPreset.size() > 0 ){
                    lastCard = listPreset.get(0);
                }else{
                    List<CardDTO> list = cardDTOList.stream().sorted(Comparator.comparing(CardDTO::getOrder,Comparator.reverseOrder())).collect(Collectors.toList());
                    lastCard = list.get(0);
                }
            }
        }else{
            //获取卡信息
            JSONObject cardObj;
            try {
                cardObj = serverService.getCardInfo(cardId);
            } catch (Exception e) {
                //查询异常交易失败
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            if (cardObj == null) {
                //无信息返回，交易失败
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            //卡信息
            lastCard = JSONObject.parseObject(cardObj.toJSONString(), CardDTO.class);
        }

        //未查询到卡信息抛异常
        if (lastCard == null || StringUtils.isBlank(lastCard.getCardNo()) || StringUtils.isBlank(lastCard.getCustomerCcType())
                || StringUtils.isBlank(lastCard.getCrdStrgToken())) {
            //无信息返回，交易失败
//            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }else{
            try {
                JSONObject cardDetails = userService.getCardDetails(lastCard.getCrdStrgToken(), request);

                lastCard.setCustomerCcExpmo(cardDetails.getString("customerCcExpyr"));
                lastCard.setCustomerCcExpyr(cardDetails.getString("customerCcExpmo"));
            } catch (Exception e) {
                //查询异常
                log.info("latpay查询卡信息三方接口请求异常, e:{}", e.getMessage());
            }
        }
        return lastCard;*/
    }

    /**
     * 根据卡类型获取手续费率及金额
     * @author zhangzeyuan
     * @date 2021/6/23 16:54
     * @param payType
     * @param cardCcType
     * @param realPayAmount
     * @param request
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    private Map<String, Object> getCardPayTransactionFee(Integer payType, String cardCcType, BigDecimal realPayAmount, HttpServletRequest request) throws Exception{
        HashMap<String, Object> paramsMap = Maps.newHashMapWithExpectedSize(2);
        paramsMap.put("gatewayId", payType);
        paramsMap.put("code", Integer.valueOf(cardCcType) + 50);
        ChannelFeeConfigDTO channelFeeConfigDTO = channelFeeConfigService.findOneChannelFeeConfig(paramsMap);
        if(Objects.isNull(channelFeeConfigDTO) || Objects.isNull(channelFeeConfigDTO.getId())){
            //todo 异常信息提示
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        paramsMap.clear();
        paramsMap.put("transChannelFeeRate", channelFeeConfigDTO.getRate());

        GatewayDTO gatewayDTO = new GatewayDTO();
        gatewayDTO.setRate(channelFeeConfigDTO.getRate());
        gatewayDTO.setRateType(channelFeeConfigDTO.getType());
        paramsMap.put("channelFeeAmount", gatewayService.getGateWayFee(gatewayDTO,realPayAmount));

        return paramsMap;
    }

    /**
     * 获取分期付交易金额信息
     * @author zhangzeyuan
     * @date 2021/6/18 17:11
     * @param transAmount
     * @param payUserId
     * @param recUserId
     * @param payType
     * @param merchantId
     * @param useRedEnvelopeAmount
     * @param payUser
     * @param cardId
     * @param posTransNumber
     * @param merchantDTO
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     */
    private JSONObject getCreditPayTransAmountDetail(String transNo, BigDecimal transAmount, Long payUserId, Long recUserId,
                                                     Integer payType, Long merchantId, BigDecimal useRedEnvelopeAmount,
                                                     UserDTO payUser, Long cardId, String posTransNumber, MerchantDTO merchantDTO,
                                                     String donationInstiuteId, BigDecimal tipAmount, HttpServletRequest request)throws Exception{
        JSONObject result = new JSONObject(16);
        //分期付绑卡校验
        Integer creditCardState = payUser.getCreditCardState();
        if(null == creditCardState || !creditCardState.equals(1)){
            throw new BizException(I18nUtils.get("pay.card.not.bound", getLang(request), new String[]{StaticDataEnum.CREDIT_MIN_TRANSAMOUNT.getMessage()}));
        }

        //是否逾期
        Map<String, Object> userCreditInfoMap = getCreditUserAvailableAmountAndState(payUserId);

        //分期付额度
        BigDecimal creditAmount = (BigDecimal) userCreditInfoMap.get("creditAmount");

        //分期付用户状态
        Integer userState = (Integer) userCreditInfoMap.get("userState");

        //用户是否冻结
        if(userState.equals(StaticDataEnum.CREAT_USER_STATE_11.getCode())){
            throw new BizException(I18nUtils.get("pay.user.freeze", getLang(request)));
        }

        if(creditAmount.compareTo(BigDecimal.ZERO) <= 0){
            throw new BizException(I18nUtils.get("pay.credit.amount.insufficient", getLang(request)));
        }

        //根据金额计算实付金额
        BigDecimal realPayAmount = getTransactionTruelyPayAmount(transNo, transAmount, payUserId, recUserId, useRedEnvelopeAmount,
                merchantDTO, payType, result, Constant.ORDER_TRANS_TYPE_CREDIT_FLAG, request);

        //最小订单金额
        BigDecimal mixPayAmount = new BigDecimal(StaticDataEnum.CREDIT_MIN_TRANSAMOUNT.getCode());

        if(realPayAmount.compareTo(BigDecimal.ZERO) < 0){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        //校验金额 付款金额不能小于10
        if(realPayAmount.compareTo(BigDecimal.ZERO) > 0 &&  realPayAmount.compareTo(mixPayAmount) <  0){
            throw new BizException(I18nUtils.get("credit.amount.limit", getLang(request), new String[]{StaticDataEnum.CREDIT_MIN_TRANSAMOUNT.getMessage()}));
        }

        //捐赠金额
        BigDecimal donationAmount = BigDecimal.ZERO;
        DonationInstituteDataDTO donationInstituteData = donationInstituteService.getDonationDataById(donationInstiuteId);
        if(null != donationInstituteData && null != donationInstituteData.getId()){
            if(StringUtils.isNotBlank(donationInstiuteId)){
                donationAmount = donationInstituteData.getDonationAmount();
            }
            result.put("donationData", donationInstituteData);
        }else {
            result.put("donationData", new DonationInstituteDataDTO());
        }

        //根据订单金额获取小费列表
        JSONArray tipList = getTipListByOrderAmount(transAmount);
        result.put("tipList", tipList);

        //计算手续费
        BigDecimal channelFeeAmount = BigDecimal.ZERO;
        BigDecimal transChannelFeeRate = BigDecimal.ZERO;

        //用户使用分期付 需要第一次卡支付25% 金额
        BigDecimal creditFirstCardPayAmount = BigDecimal.ZERO;
        //剩余分期付金额
        BigDecimal creditRealPayAmount = BigDecimal.ZERO;

        //获取通道信息
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("gatewayType", 0);
        params.put("state", StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);

        if(gatewayDTO == null || gatewayDTO.getId() == null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        //获取卡ID信息
        CardDTO cardInfo = getCardInfoById(cardId, payUserId, request);

        result.put("cardId", cardInfo.getId().toString());
        result.put("cardNo", cardInfo.getCardNo());
        result.put("cardCcType", cardInfo.getCustomerCcType());

        //需要计算卡支付手续费的金额
        BigDecimal calculateCardFeeAmount = BigDecimal.ZERO;

        //实付大于0
        if(realPayAmount.compareTo(BigDecimal.ZERO) > 0){
            //首次卡支付金额
            creditFirstCardPayAmount = realPayAmount.multiply(new BigDecimal(Constant.PAY_CREDIT_NEED_CARDPAY_AMOUNT_RATE)).setScale(2, BigDecimal.ROUND_HALF_UP);
            //剩余分期付金额
            creditRealPayAmount = realPayAmount.subtract(creditFirstCardPayAmount);
            // 需要计算手续费的金额为 实付25% + 捐赠金额  + 小费
            calculateCardFeeAmount = creditFirstCardPayAmount.add(donationAmount).add(tipAmount);
        }else {
            //只用计算 捐赠费 + 小费 的卡支付手续费
            calculateCardFeeAmount = donationAmount.add(tipAmount);
        }


        if(calculateCardFeeAmount.compareTo(BigDecimal.ZERO) > 0){
            //计算通到手续费
            Map<String, Object> feeMap = getCardPayTransactionFee(0, cardInfo.getCustomerCcType(), calculateCardFeeAmount, request);

            channelFeeAmount = (BigDecimal) feeMap.get("channelFeeAmount");
            transChannelFeeRate = (BigDecimal) feeMap.get("transChannelFeeRate");
        }

        //验证分期付额度是否足够
        if(creditAmount.compareTo(creditRealPayAmount) < 0){
            throw new BizException(I18nUtils.get("pay.credit.amount.insufficient", getLang(request)));
        }

        //获取分期付还款计划预览与期数、分期金额信息
        this.previewRepayPlanData(realPayAmount, creditFirstCardPayAmount, result);

        //POS订单号处理
        // POS订单号不为空 并且 卡ID为空 将系统订单号更新到pos订单流水表中
        if(StringUtils.isNotBlank(posTransNumber)){
            posQrPayFlowService.updateSysTransNoByThirdNo(result.getString("transNo"), posTransNumber, getUserId(request), System.currentTimeMillis());
        }

        // 正常扫商家码，判断5分钟之内有没有成功的付款
        Integer repeatSaleState = getPayUnSuccessStateInFiveMin(merchantId, transAmount, payUserId, posTransNumber);

        //25%卡支付 + 捐赠费 + 小费 + 手续费 金额
        result.put("allPayAmount", calculateCardFeeAmount.add(channelFeeAmount).toString());

        //订单号信息
        result.put("transAmount", transAmount.toString());

        //扣除完 红包 、 折扣的金额
        result.put("payAmount", realPayAmount.toString());

        //手续费金额
        result.put("channelFeeAmount", channelFeeAmount.toString());
        //通道手续费率
        result.put("transChannelFeeRate", transChannelFeeRate.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        result.put("mixPayAmount",mixPayAmount.toString());
        result.put("repeatSaleState", repeatSaleState.toString());

        //25%卡支付金额
        result.put("creditFirstCardPayAmount", creditFirstCardPayAmount.toString());

        //25%卡支付金额 + 捐赠费
        result.put("toShowFeeAllAmount",  calculateCardFeeAmount.toString());

        //剩余分期付金额
        result.put("creditRealPayAmount", creditRealPayAmount.toString());

        //捐赠费金额
        result.put("donationAmount", donationAmount.toString());

        //小费金额
        result.put("tipAmount", tipAmount.toString());

        return result;
    }



    /**
     * 获取分期付交易金额信息
     * @author zhoutt
     * @date 2021/10/28 17:11
     * @param transAmount
     * @param payUserId
     * @param recUserId
     * @param payType
     * @param merchantId
     * @param useRedEnvelopeAmount
     * @param payUser
     * @param cardId
     * @param posTransNumber
     * @param merchantDTO
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     */
    private JSONObject getCreditPayTransAmountDetailV3(String transNo, BigDecimal transAmount, Long payUserId, Long recUserId,
                                                       Integer payType, Long merchantId, BigDecimal useRedEnvelopeAmount,
                                                       UserDTO payUser, Long cardId, String posTransNumber, MerchantDTO merchantDTO,
                                                       String donationInstiuteId, BigDecimal tipAmount, HttpServletRequest request)throws Exception{
        JSONObject result = new JSONObject(16);
        //分期付绑卡校验
        Integer creditCardState = payUser.getCreditCardState();
        if(null == creditCardState || !creditCardState.equals(1)){
            throw new BizException(I18nUtils.get("pay.card.not.bound", getLang(request), new String[]{StaticDataEnum.CREDIT_MIN_TRANSAMOUNT.getMessage()}));
        }

        //是否逾期
        Map<String, Object> userCreditInfoMap = getCreditUserAvailableAmountAndState(payUserId);

        //分期付额度
        BigDecimal creditAmount = (BigDecimal) userCreditInfoMap.get("creditAmount");

        //分期付用户状态
        Integer userState = (Integer) userCreditInfoMap.get("userState");

        //用户是否冻结
        if(userState.equals(StaticDataEnum.CREAT_USER_STATE_11.getCode())){
            throw new BizException(I18nUtils.get("pay.user.freeze", getLang(request)));
        }

        if(creditAmount.compareTo(BigDecimal.ZERO) <= 0){
            throw new BizException(I18nUtils.get("pay.credit.amount.insufficient", getLang(request)));
        }

        //根据金额计算实付金额
        BigDecimal realPayAmount = getTransactionTruelyPayAmountV3(transNo, transAmount, payUserId, recUserId, useRedEnvelopeAmount,
                merchantDTO, payType, result, Constant.ORDER_TRANS_TYPE_CREDIT_FLAG, request);

        //最小订单金额
        BigDecimal mixPayAmount = new BigDecimal(StaticDataEnum.CREDIT_MIN_TRANSAMOUNT.getCode());

        if(realPayAmount.compareTo(BigDecimal.ZERO) < 0){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        //校验金额  如果未使用券   付款金额不能小于10
        if(useRedEnvelopeAmount.compareTo(BigDecimal.ZERO) == 0  &&  realPayAmount.compareTo(mixPayAmount) <  0){
            throw new BizException(I18nUtils.get("credit.amount.limit", getLang(request), new String[]{StaticDataEnum.CREDIT_MIN_TRANSAMOUNT.getMessage()}));
        }

        //捐赠金额
        BigDecimal donationAmount = BigDecimal.ZERO;
        DonationInstituteDataDTO donationInstituteData = donationInstituteService.getDonationDataById(donationInstiuteId);
        if(null != donationInstituteData && null != donationInstituteData.getId()){
            if(StringUtils.isNotBlank(donationInstiuteId)){
                donationAmount = donationInstituteData.getDonationAmount();
            }
            result.put("donationData", donationInstituteData);
        }else {
            result.put("donationData", new DonationInstituteDataDTO());
        }

        //根据订单金额获取小费列表
        JSONArray tipList = getTipListByOrderAmount(transAmount);
        result.put("tipList", tipList);

        //计算手续费
        BigDecimal channelFeeAmount = BigDecimal.ZERO;
        BigDecimal transChannelFeeRate = BigDecimal.ZERO;

        //用户使用分期付 需要第一次卡支付25% 金额
        BigDecimal creditFirstCardPayAmount = BigDecimal.ZERO;
        //剩余分期付金额
        BigDecimal creditRealPayAmount = BigDecimal.ZERO;

        //获取通道信息
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("gatewayType", 0);
        params.put("state", StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);

        if(gatewayDTO == null || gatewayDTO.getId() == null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        //获取卡ID信息
        CardDTO cardInfo = getCardInfoById(cardId, payUserId, request);

//        result.put("cardId", cardInfo.getId().toString());
//        result.put("cardNo", cardInfo.getCardNo());
//        result.put("cardCcType", cardInfo.getCustomerCcType());
        result.put("cardId", Objects.isNull(cardInfo.getId()) ? "" :  cardInfo.getId().toString());
        result.put("cardNo", StringUtils.isNotBlank(cardInfo.getCardNo()) ? cardInfo.getCardNo() : "");
        result.put("cardCcType", StringUtils.isNotBlank(cardInfo.getCustomerCcType()) ? cardInfo.getCustomerCcType() : "");
        result.put("customerCcExpyr", StringUtils.isNotBlank(cardInfo.getCustomerCcExpyr()) ? cardInfo.getCustomerCcExpyr() : "");
        result.put("customerCcExpmo", StringUtils.isNotBlank(cardInfo.getCustomerCcExpmo()) ? cardInfo.getCustomerCcExpmo() : "");

        //需要计算卡支付手续费的金额
        BigDecimal calculateCardFeeAmount = BigDecimal.ZERO;

        //实付大于0
        if(realPayAmount.compareTo(BigDecimal.ZERO) > 0){
            //首次卡支付金额
            // 20211028修改，首次支付小于最小订单金额，分1期
            if(useRedEnvelopeAmount.compareTo(BigDecimal.ZERO) > 0 &&  realPayAmount.compareTo(mixPayAmount) <  0){
                creditFirstCardPayAmount = realPayAmount;
            }else{
                creditFirstCardPayAmount = realPayAmount.multiply(new BigDecimal(Constant.PAY_CREDIT_NEED_CARDPAY_AMOUNT_RATE)).setScale(2, BigDecimal.ROUND_HALF_UP);

            }
            //剩余分期付金额
            creditRealPayAmount = realPayAmount.subtract(creditFirstCardPayAmount);
            // 需要计算手续费的金额为 实付25% + 捐赠金额  + 小费
            calculateCardFeeAmount = creditFirstCardPayAmount.add(donationAmount).add(tipAmount);
        }else {
            //只用计算 捐赠费 + 小费 的卡支付手续费
            calculateCardFeeAmount = donationAmount.add(tipAmount);
        }

//        if(calculateCardFeeAmount.compareTo(BigDecimal.ZERO) > 0){
//            //计算通到手续费
//            Map<String, Object> feeMap = getCardPayTransactionFee(0, cardInfo.getCustomerCcType(), calculateCardFeeAmount, request);
//
//            channelFeeAmount = (BigDecimal) feeMap.get("channelFeeAmount");
//            transChannelFeeRate = (BigDecimal) feeMap.get("transChannelFeeRate");
//        }

        //验证分期付额度是否足够
        if(creditAmount.compareTo(creditRealPayAmount) < 0){
            throw new BizException(I18nUtils.get("pay.credit.amount.insufficient", getLang(request)));
        }

        //获取分期付还款计划预览与期数、分期金额信息
        this.previewRepayPlanData(realPayAmount, creditFirstCardPayAmount, result);

        //POS订单号处理
        // POS订单号不为空 并且 卡ID为空 将系统订单号更新到pos订单流水表中
        /*if(StringUtils.isNotBlank(posTransNumber)){
            posQrPayFlowService.updateSysTransNoByThirdNo(result.getString("transNo"), posTransNumber, getUserId(request), System.currentTimeMillis());
        }*/

        // 正常扫商家码，判断5分钟之内有没有成功的付款
        Integer repeatSaleState = getPayUnSuccessStateInFiveMin(merchantId, transAmount, payUserId, posTransNumber);

        //25%卡支付 + 捐赠费 + 小费 + 手续费 金额
        result.put("allPayAmount", calculateCardFeeAmount.add(channelFeeAmount).toString());

        //订单号信息
        result.put("transAmount", transAmount.toString());

        //扣除完 红包 、 折扣的金额
        result.put("payAmount", realPayAmount.toString());

        //手续费金额
//        result.put("channelFeeAmount", channelFeeAmount.toString());
        //通道手续费率
//        result.put("transChannelFeeRate", transChannelFeeRate.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        result.put("mixPayAmount",mixPayAmount.toString());
        result.put("repeatSaleState", repeatSaleState.toString());

        //25%卡支付金额

        result.put("creditFirstCardPayAmount", creditFirstCardPayAmount.toString());

        //25%卡支付金额 + 捐赠费
        result.put("toShowFeeAllAmount",  calculateCardFeeAmount.toString());
        //剩余分期付金额
        result.put("creditRealPayAmount", creditRealPayAmount.toString());

        //捐赠费金额
        result.put("donationAmount", donationAmount.toString());

        //小费金额
        result.put("tipAmount", tipAmount.toString());

        return result;
    }



    /**
     * 获取用户的分期付额度 和 逾期订单数量
     * @author zhangzeyuan
     * @date 2021/6/29 16:26
     * @param userId
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    private Map<String, Object> getCreditUserAvailableAmountAndState(Long userId){

        HashMap<String, Object> resultMap = Maps.newHashMapWithExpectedSize(2);

        BigDecimal creditAmount = BigDecimal.ZERO;

        int userState = 11;

        //获取分期付绑卡状态
        String url = creditMerchantUrl + "/payremote/getCreditUserAvailableAmountAndState/";

        JSONObject postJson = new JSONObject(1);
        JSONObject dataJson = new JSONObject(1);
        dataJson.put("userId", userId);
        postJson.put("data", dataJson);

        try {
            String repayPlanResult = HttpClientUtils.post(url, postJson.toJSONString());

            if(StringUtils.isNotBlank(repayPlanResult)){
                JSONObject repayPlanResultJson = JSONObject.parseObject(repayPlanResult);
                String code = repayPlanResultJson.getString("code");
                if(StringUtils.isNotBlank(code) && code.equals(ResultCode.OK.getCode())){
                    JSONObject  repayPlanDaataJson = repayPlanResultJson.getJSONObject("data");

                    if(Objects.nonNull(repayPlanDaataJson.getBigDecimal("userAmount"))){
                        creditAmount = repayPlanDaataJson.getBigDecimal("userAmount");
                    }

                    if(Objects.nonNull(repayPlanDaataJson.getInteger("userState"))){
                        userState = repayPlanDaataJson.getInteger("userState");
                    }
                }
            }
        }catch (Exception e){
            log.error("获取分期付绑卡状态出错",e.getMessage());
        }

        resultMap.put("creditAmount", creditAmount);
        resultMap.put("userState", userState);

        return resultMap;
    }
    /**
     * 预览分期付生成期数 和 还款计划
     * @author zhangzeyuan
     * @date 2021/6/27 18:15
     * @param realPayAmount
     * @param creditFirstCardPayAmount
     * @param result
     */
    private void previewRepayPlanData(BigDecimal realPayAmount, BigDecimal creditFirstCardPayAmount, JSONObject result){
        //如果金额为0 直接返回0
        if(realPayAmount.compareTo(BigDecimal.ZERO) == 0){
            result.put("creditAverageAmount", "0.00");
            result.put("period", "0");
            result.put("previewRepayPlanList",  Collections.emptyList());
            return;
        }

        //获取分期付预览计划
        String url = creditMerchantUrl + "/payremote/previewRepayInfo/";

        JSONObject postJson = new JSONObject(1);
        JSONObject dataJson = new JSONObject(3);
        dataJson.put("allPayAmount", realPayAmount);
        dataJson.put("cardPayAmount", creditFirstCardPayAmount);
        postJson.put("data", dataJson);

        try {
            String repayPlanResult = HttpClientUtils.post(url, postJson.toJSONString());

            if(StringUtils.isNotBlank(repayPlanResult)){
                JSONObject repayPlanResultJson = JSONObject.parseObject(repayPlanResult);
                String code = repayPlanResultJson.getString("code");
                if(StringUtils.isNotBlank(code) && code.equals(ResultCode.OK.getCode())){
                    JSONObject  repayPlanDataJson = repayPlanResultJson.getJSONObject("data");

                    result.put("creditAverageAmount", repayPlanDataJson.getString("avgAmount"));
                    result.put("period", repayPlanDataJson.getString("period"));
                    result.put("previewRepayPlanList",  repayPlanDataJson.getJSONArray("list"));
                }
            }
        }catch (Exception e){
            log.error("获取分期付绑卡状态出错",e.getMessage());
        }
    }



    /**
     * 根据订单金额获取app展示小费列表
     * @author zhangzeyuan
     * @date 2021/8/10 14:14
     * @param orderAmount
     * @return com.alibaba.fastjson.JSONArray
     */
    private JSONArray getTipListByOrderAmount(BigDecimal orderAmount){
        JSONArray resultArray = new JSONArray(3);

        BigDecimal tip1Amount = orderAmount.multiply(new BigDecimal("0.05")).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal tip2Amount = orderAmount.multiply(new BigDecimal("0.10")).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal tip3Amount = orderAmount.multiply(new BigDecimal("0.15")).setScale(2, BigDecimal.ROUND_HALF_UP);

        JSONObject tip1JsonObj = new JSONObject(1);
        tip1JsonObj.put("amount", tip1Amount.toString());
        tip1JsonObj.put("ratio", "5%");
        resultArray.add(tip1JsonObj);

        JSONObject tip2JsonObj = new JSONObject(1);
        tip2JsonObj.put("amount", tip2Amount.toString());
        tip2JsonObj.put("ratio", "10%");
        resultArray.add(tip2JsonObj);

        JSONObject tip3JsonObj = new JSONObject(1);
        tip3JsonObj.put("amount", tip3Amount.toString());
        tip3JsonObj.put("ratio", "15%");
        resultArray.add(tip3JsonObj);

        return resultArray;
    }
    /**
     * 正常扫商家码，判断5分钟之内有没有成功的付款
     * @author zhangzeyuan
     * @date 2021/6/18 17:11
     * @param merchantId
     * @param transAmount
     * @param payUserId
     * @param posTransNumber
     * @return java.lang.Integer
     */
    private Integer getPayUnSuccessStateInFiveMin(Long merchantId, BigDecimal transAmount, Long payUserId,
                                                  String posTransNumber){
        // 正常扫商家码，判断5分钟之内有没有成功的付款
        Integer repeatSaleState = 0;
        if (StringUtils.isNotBlank(posTransNumber)){
            return repeatSaleState;
        }

        Long now = System.currentTimeMillis();
        Long start = now - 5 * 60 * 1000;

        HashMap<String, Object> paramsMap = Maps.newHashMapWithExpectedSize(5);
        paramsMap.put("start", start);
        paramsMap.put("end", now);
        paramsMap.put("payUserId", payUserId);
        paramsMap.put("merchantId", merchantId);
        paramsMap.put("transAmount", transAmount);

        List<QrPayFlowDTO> qrPayFlowDTOS = qrPayFlowService.find(paramsMap,null ,null );

        if(CollectionUtils.isEmpty(qrPayFlowDTOS)){
            return repeatSaleState;
        }

        for (QrPayFlowDTO qrPayFlowDTO : qrPayFlowDTOS){
            if(qrPayFlowDTO.getState() != StaticDataEnum.TRANS_STATE_2.getCode() &&
                    qrPayFlowDTO.getState() != StaticDataEnum.TRANS_STATE_12.getCode() &&
                    qrPayFlowDTO.getState() != StaticDataEnum.TRANS_STATE_22.getCode() ){
                repeatSaleState = 1;
                break;
            }
        }
        return repeatSaleState;
    }


    @Override
    public void qrPayBatchAmountOutDoubtHandle() {
//        List<QrPayFlowDTO> list = qrPayFlowService.findBatchAmountOutDoubtFlow();
//        int[] transTypeList = {21,26};
        List<AccountFlowDTO> list = accountFlowService.getBatchAmountOutDoubtFlow();
        for(AccountFlowDTO accountFlowDTO : list){
            try {
                Map<String,Object> params = new HashMap<>();
//                params.put("flowId", accountFlowDTO.getFlowId());
//                params.put("transTypeList",transTypeList);
//                List<AccountFlowDTO> amountOutFlowList = accountFlowService.find(params,null,null);
                // 查询本交易状态
                if(accountFlowDTO.getState() != StaticDataEnum.TRANS_STATE_0.getCode() && accountFlowDTO.getState() != StaticDataEnum.TRANS_STATE_3.getCode()
                        && accountFlowDTO.getState() != StaticDataEnum.TRANS_STATE_1.getCode() ){
                    // 如果交易已经是失败的，继续下一条
                    continue;
                }
                QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(accountFlowDTO.getFlowId());
                if(accountFlowDTO.getState() == StaticDataEnum.TRANS_STATE_0.getCode() || accountFlowDTO.getState() == StaticDataEnum.TRANS_STATE_3.getCode() ){
                    JSONObject data;
                    data = serverService.transactionInfo(accountFlowDTO.getOrderNo().toString());

                    if (data != null ) {
                        if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                            // 成功
                            // 更新出账记录为成功
                            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                            accountFlowService.updateAccountFlowByOrderNo(accountFlowDTO.getOrderNo(),accountFlowDTO,null);
                            // 钱包出账记录
                            if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0){
                                params.clear();
                                params.put("flowId",qrPayFlowDTO.getId());
                                params.put("transType",StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode());
                                params.put("state",StaticDataEnum.TRANS_STATE_1.getCode());
                                MarketingFlowDTO checkFlow = marketingFlowService.findOneMarketingFlow(params);
                                if(checkFlow == null && checkFlow.getId() == null){
                                    MarketingFlowDTO marketingFlowDTO = new MarketingFlowDTO();
                                    marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                                    marketingFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
                                    marketingFlowDTO.setFlowId(qrPayFlowDTO.getId());
                                    marketingFlowDTO.setDirection(StaticDataEnum.DIRECTION_1.getCode());
                                    marketingFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode());
                                    marketingFlowDTO.setAmount(qrPayFlowDTO.getRedEnvelopeAmount());
                                    marketingFlowDTO.setId(marketingFlowService.saveMarketingFlow(marketingFlowDTO,null));
                                }
                            }


                            // 以下为正常交易进程
//                        GatewayDTO gatewayDTO = new GatewayDTO();
//                        // 查询通道信息
//                        if(qrPayFlowDTO.getTransType() != StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode()){
//                            params.clear();
//                            params.put("type",qrPayFlowDTO.getGatewayId());
//                            params.put("state",StaticDataEnum.STATUS_1.getCode());
//                            gatewayDTO = gatewayService.findOneGateway(params);
//                            if(gatewayDTO == null || gatewayDTO.getId() == null){
//                                //原通道不可用，回滚出账，交易失败
//                                log.info("扫码支付查证原通道不可用，交易失败，id:"+qrPayFlowDTO.getId());
//
//                                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(),gatewayDTO.getType(),qrPayFlowDTO.getPayAmount(),null);
//                                doBatchAmountOutRollBack(qrPayFlowDTO,null);
//                                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
//                                updateFlow(qrPayFlowDTO, null, null, null);
//                                continue;
//                            }
//                        }
//                        // 出入账用户
//                        UserDTO payUser = userService.findUserById(qrPayFlowDTO.getPayUserId());
//                        UserDTO recUser = userService.findUserById(qrPayFlowDTO.getRecUserId());
//                        // 根据不同的通道走不同的
//                        if(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode() == qrPayFlowDTO.getTransType()){
//                            // 查询绑卡信息
//                            //获取卡信息
//                            JSONObject cardObj;
//                            try {
//                                cardObj = serverService.getCardInfo(Long.parseLong(qrPayFlowDTO.getCardId()));
//                            } catch (Exception e) {
//                                //查询异常交易失败
//                               throw e;
//                            }
//                            if (cardObj == null) {
//                                //无信息返回，交易失败
//                                log.info("原卡信息不存在，交易失败，id："+qrPayFlowDTO.getId());
//                                doQrPayRollBack(qrPayFlowDTO,StaticDataEnum.TRANS_STATE_12.getCode(),null);
//                                continue;
//                            }
//                            //交易状态为三方初始状态
//                            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
//                            //记录交易状态
//                            qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,null);
//                            //卡支付方法
//                            qrPayByCard(null, payUser, recUser, qrPayFlowDTO, gatewayDTO, JSONObject.parseObject(cardObj.toJSONString(), CardDTO.class) ,cardObj );
//                            // 交易成功推荐人处理
//                            dealFirstDeal(qrPayFlowDTO,payUser,null);
//                        }else if(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode() == qrPayFlowDTO.getTransType()){
//                            //分期付
//                            if(payUser.getInstallmentState() != StaticDataEnum.STATUS_1.getCode() || payUser.getSplitAddInfoState() == StaticDataEnum.STATUS_1.getCode()){
//                                // 付款用户不可用分期付
//                                log.info("原卡信息不存在，交易失败，id："+qrPayFlowDTO.getId());
//                                doQrPayRollBack(qrPayFlowDTO,StaticDataEnum.TRANS_STATE_12.getCode(),null);
//                                continue;
//                            }
//
//                            qrPayFlowDTO = doCredit(qrPayFlowDTO,null);
//                            // 交易成功推荐人处理
//                            dealFirstDeal(qrPayFlowDTO,payUser,null);
//                        }
                        } else if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                            //账户交易失败
                            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                            accountFlowService.updateAccountFlowByOrderNo(accountFlowDTO.getOrderNo(),accountFlowDTO,null);

                            //失败
//                        if(qrPayFlowDTO.getGatewayId() != null ){
//                            rechargeFlowService.channelLimitRollback(System.currentTimeMillis(),qrPayFlowDTO.getGatewayId(),qrPayFlowDTO.getPayAmount(),null);
//                        }
//                        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
//                        updateFlow(qrPayFlowDTO, null, null, null);

                            continue;
                        }else{
                            // 下游无结果
                            continue;
                        }

                    }else{
                        // 查询无信息为失败
                        //账户交易失败
                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                        accountFlowService.updateAccountFlowByOrderNo(accountFlowDTO.getOrderNo(),accountFlowDTO,null);
                        continue;
                    }
                    // 如果出账成功，在查证流程中，进行回退，因为交易已经是失败状态了
                    this.doBatchAmountOutRollBack(qrPayFlowDTO,null);
                }

            }catch (Exception e){
                log.error("qrPayService.qrPayBatchAmountOutDoubtHandle Exception :"+e.getMessage(),e);
                continue;
            }
        }
    }

    @Override
    public void qrPayBatAmtOutRollbackDoubtHandle() {
        List<AccountFlowDTO> list = accountFlowService.getQrPayBatAmtOutRollbackDoubtFlow();
        for (AccountFlowDTO accountFlowDTO : list) {
            Map<String, Object> params = new HashMap<>(1);
            params.put("rollBackNo", accountFlowDTO.getOrderNo().toString());

            //调用账户系统查询是否存在入账信息，存在则查询设置状态，不存在则进行入账操作
            try {
                // 查询原流水，如果有状态，直接下一条
                AccountFlowDTO accountFlowCheck = accountFlowService.findAccountFlowById(accountFlowDTO.getId());
                if(accountFlowCheck.getState() == StaticDataEnum.TRANS_STATE_1.getCode() || accountFlowCheck.getState() == StaticDataEnum.TRANS_STATE_2.getCode()){
                    continue;
                }

                // 账户查证
                JSONObject accountResult = serverService.transactionInfo(accountFlowDTO.getRollBackNo().toString());
                AccountFlowDTO updateFlow = new AccountFlowDTO();
                if (accountResult != null) {
                    if (accountResult.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                        updateFlow.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    } else if (accountResult.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                        updateFlow.setState(StaticDataEnum.TRANS_STATE_5.getCode());
                    } else {
                        //其他为可疑状态
                        continue;
                    }
                } else {

                    // 无交易记录，为交易失败
                    updateFlow.setState(StaticDataEnum.TRANS_STATE_5.getCode());
                }
                accountFlowService.updateAccountFlowByOrderNo(accountFlowDTO.getOrderNo(), updateFlow, null);
            } catch (Exception e) {
                log.error("qrPayBatAmtOutRollbackDoubtHandle Exception:" + e.getMessage(), e);
                continue;
            }
        }
    }

    @Override
    public void qrPayBatAmtOutRollbackFailHandle() {
        List<AccountFlowDTO> list = accountFlowService.getQrPayBatAmtOutRollbackFailFlow();
        for (AccountFlowDTO accountFlowDTO : list) {
            Map<String, Object> params = new HashMap<>(1);
            //调用账户系统查询是否存在入账信息，存在则查询设置状态，不存在则进行入账操作
            try {

                // 查询原流水，如果不是回滚状态，直接下一条
                AccountFlowDTO accountFlowCheck = accountFlowService.findAccountFlowById(accountFlowDTO.getId());
                if(accountFlowCheck.getState() != StaticDataEnum.TRANS_STATE_5.getCode() ){
                    continue;
                }

                QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(accountFlowDTO.getFlowId());
                doBatchAmountOutRollBack(qrPayFlowDTO,null);
            } catch (Exception e) {
                //更新回滚流水
                continue;
            }
        }
    }

    @Override
    public void qrPayReqCheckOld(QrPayDTO qrPayDTO, HttpServletRequest request) throws Exception {

        //交易金额不为空
        if (StringUtils.isEmpty(qrPayDTO.getTransAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.empty", getLang(request)));
        }

        //交易金额格式校验
        if (!RegexUtils.isTransAmt(qrPayDTO.getTransAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        if (qrPayDTO.getFeeAmt()!=null && (StringUtils.isNotEmpty(qrPayDTO.getFeeAmt().toString()))) {
            //手续费金额格式校验
            if (!RegexUtils.isTransAmt(qrPayDTO.getFeeAmt().toString())) {
                throw new BizException(I18nUtils.get("feeAmount.error", getLang(request)));
            }

            //手续费收取方向为空
            if (StringUtils.isEmpty(qrPayDTO.getFeeDirection().toString())) {
                throw new BizException(I18nUtils.get("feeDirection.null", getLang(request)));
            }
        }

        //用户userId不能为空
        if (StringUtils.isEmpty(qrPayDTO.getPayUserId().toString()) || StringUtils.isEmpty(qrPayDTO.getRecUserId().toString())) {
            throw new BizException(I18nUtils.get("userId.null", getLang(request)));
        }

        //验证交易方式
        if (StringUtils.isEmpty(qrPayDTO.getPayType().toString())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }


        if (qrPayDTO.getPayType() == StaticDataEnum.PAY_TYPE_0.getCode()) {
            if (StringUtils.isEmpty(qrPayDTO.getCardId().toString())) {
                throw new BizException(I18nUtils.get("card.id.empty", getLang(request)));
            }
        }

        //如果有红包金额校验格式
        if(qrPayDTO.getRedEnvelopeAmount()!=null && qrPayDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) != 0){
            if (!RegexUtils.isTransAmt(qrPayDTO.getRedEnvelopeAmount().toString())) {
                throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
            }
        }
        //如果有整体出售金额校验格式
        if(qrPayDTO.getWholeSalesAmount()!=null && qrPayDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) != 0){
            if (!RegexUtils.isTransAmt(qrPayDTO.getWholeSalesAmount().toString())) {
                throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
            }
            //整体出售金额不能大于交易金额
            if(qrPayDTO.getWholeSalesAmount().compareTo(qrPayDTO.getTransAmount())>0){
                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
            }
        }
    }

    @Override
    public Object doQrPayOld(QrPayDTO qrPayDTO, HttpServletRequest request) throws Exception {

        //获取交易双方user信息
        UserDTO payUser = userService.findUserById(qrPayDTO.getPayUserId());
        UserDTO recUser = userService.findUserById(qrPayDTO.getRecUserId());

        //判断用户是否存在
        if (payUser.getId() == null || recUser.getId() == null) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }

        //查询账户可用状态
        if (!checkAccountState(payUser.getId())) {
            throw new BizException(I18nUtils.get("account.error", getLang(request)));
        }
        if (!checkAccountState(recUser.getId())) {
            throw new BizException(I18nUtils.get("account.error", getLang(request)));
        }

        //创建流水
        QrPayFlowDTO qrPayFlowDTO = new QrPayFlowDTO();
        qrPayFlowDTO.setFee(qrPayDTO.getFeeAmt());
        qrPayFlowDTO.setPayUserId(qrPayDTO.getPayUserId());
        qrPayFlowDTO.setPayUserType(payUser.getUserType());
        qrPayFlowDTO.setMerchantId(qrPayDTO.getMerchantId());
        qrPayFlowDTO.setRecUserId(qrPayDTO.getRecUserId());
        qrPayFlowDTO.setRecUserType(recUser.getUserType());
        qrPayFlowDTO.setTransAmount(qrPayDTO.getTransAmount());
        qrPayFlowDTO.setRemark(qrPayDTO.getRemark());
        //收款用户是商户,计算各种金额
        if(recUser.getUserType()==StaticDataEnum.USER_TYPE_20.getCode()){
            qrPayFlowDTO =  getQrPayFlowOld(qrPayFlowDTO,qrPayDTO,request);
        }

        Map<String,Object> resMap = new HashMap<>();
//        if (StaticDataEnum.PAY_TYPE_0.getCode() == (qrPayDTO.getPayType())) {
//            //查询商户路由配置
//            GatewayDTO gatewayDTO = doRouteConfig(recUser,qrPayFlowDTO,qrPayDTO,request);
//            //计算金额
//            qrPayFlowDTO = calculation(qrPayDTO, qrPayFlowDTO, request);
//            //卡支付
//            if (recUser.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
//                //卡转账
//                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_4.getCode());
//            } else {
//                //查询商户信息
//                MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayDTO.getMerchantId());
//                if(merchantDTO==null || merchantDTO.getId()==null ||  merchantDTO.getIsAvailable() !=1) {
//                    throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
//                }
//                //卡消费
//                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode());
//                if(StaticDataEnum.GATEWAY_TYPE_0.getCode()==gatewayDTO.getType()||StaticDataEnum.GATEWAY_TYPE_4.getCode()==gatewayDTO.getType()){
//                    //Latpay或者integrapay
//                    //查询商户,是否配置商户号和密码
//                    Map<String,Object> map = new HashedMap();
//                    map.put("userId",qrPayFlowDTO.getRecUserId());
//                    MerchantDTO merchant = merchantService.findMerchantById(qrPayDTO.getMerchantId());
//                    map.put("merchantId",merchant.getId());
//                    map.put("gatewayId",gatewayDTO.getType());
//                    SecondMerchantGatewayInfoDTO  secondMerchantGatewayInfoDTO = secondMerchantGatewayInfoService.findOneSecondMerchantGatewayInfo(map);
//                    if(secondMerchantGatewayInfoDTO==null ||secondMerchantGatewayInfoDTO.getId() ==null){
//                        throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
//                    }
//                }
//            }
//            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
//            qrPayFlowDTO.setId(qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request));
//
//            qrPayByCard(qrPayDTO, request, payUser, recUser, qrPayFlowDTO,gatewayDTO);
//        }
//        else if (StaticDataEnum.PAY_TYPE_1.getCode() == (qrPayDTO.getPayType())) {
//            //查询商户路由配置
//            GatewayDTO gatewayDTO = doRouteConfig(recUser,qrPayFlowDTO,qrPayDTO,request);
//            //计算金额
//            qrPayFlowDTO = calculation(qrPayDTO, qrPayFlowDTO, request);
//            //账户余额支付
//            if (recUser.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
//                //余额转账
//                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_3.getCode());
//            } else {
//                //余额消费
//                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_1.getCode());
//                qrPayFlowDTO.setIsNeedClear(StaticDataEnum.NEED_CLEAR_TYPE_1.getCode());
//            }
//
//            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_30.getCode());
//            qrPayFlowDTO.setId(qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request));
//            qrPayByBalance(qrPayDTO, request, payUser, recUser, qrPayFlowDTO);
//            sendMessage(qrPayFlowDTO.getRecUserId(), qrPayFlowDTO.getId(), request);
//        }
//        else if (StaticDataEnum.PAY_TYPE_2.getCode() == (qrPayDTO.getPayType())) {
//            //查询商户路由配置
//            GatewayDTO gatewayDTO = doRouteConfig(recUser,qrPayFlowDTO,qrPayDTO,request);
//            //支付宝支付
//            if (recUser.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
//                //支付宝不可转账
//                // qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_10.getCode());
//                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
//            } else {
//                //支付宝消费
//                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_8.getCode());
//                MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayDTO.getMerchantId());
//                if(merchantDTO==null || merchantDTO.getId()==null ||  merchantDTO.getIsAvailable() !=1) {
//                    throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
//                }
//            }
//            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
//            qrPayFlowDTO.setIsNeedClear(StaticDataEnum.NEED_CLEAR_TYPE_1.getCode());
//            qrPayFlowDTO.setId(qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request));
//            WithholdFlowDTO res = qrPayByAliPay(qrPayDTO, request, payUser, recUser, qrPayFlowDTO);
//            resMap.put("noticeUrl",noticeUrl);
//            resMap.put("mNumber", res.getMNumber());
//            resMap.put("secretKey", res.getSecretKey());
//        }
//        else if (StaticDataEnum.PAY_TYPE_3.getCode() == (qrPayDTO.getPayType())) {
//            //查询商户路由配置
//            GatewayDTO gatewayDTO = doRouteConfig(recUser,qrPayFlowDTO,qrPayDTO,request);
//            //计算金额
//            qrPayFlowDTO = calculation(qrPayDTO, qrPayFlowDTO, request);
//            //微信支付
//            if (recUser.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
//                //微信不可转账
//                //qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_11.getCode());
//                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
//
//            } else {
//                //微信消费
//                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_9.getCode());
//                MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayDTO.getMerchantId());
//                if(merchantDTO==null || merchantDTO.getId()==null ||  merchantDTO.getIsAvailable() !=1) {
//                    throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
//                }
//            }
//            qrPayFlowDTO.setIsNeedClear(StaticDataEnum.NEED_CLEAR_TYPE_1.getCode());
//            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
//            qrPayFlowDTO.setId(qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request));
//            WithholdFlowDTO res = qrPayByWechatPay(qrPayDTO, request, payUser, recUser, qrPayFlowDTO);
//            resMap.put("noticeUrl",noticeUrl);
//            resMap.put("mNumber", res.getMNumber());
//            resMap.put("secretKey", res.getSecretKey());
//        }
//        else
        if (StaticDataEnum.PAY_TYPE_4.getCode() == (qrPayDTO.getPayType())){
            //分期付
            qrPayFlowDTO = checkCreditParams(qrPayFlowDTO,qrPayDTO,recUser,payUser,request);
            qrPayFlowDTO = doCreditOld(qrPayFlowDTO,qrPayDTO,request);
        }
        else {
            //非法的交易方式
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));

        }
        resMap.put("flowId",qrPayFlowDTO.getTransNo());
        resMap.put("orderCreateDate", new SimpleDateFormat("HH:mm dd-MM-yyyy").format(new Date(System.currentTimeMillis())));
        return resMap;

    }

    private QrPayFlowDTO doCreditOld(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, HttpServletRequest request) throws Exception {
        // 同步分期付获取交易结果
        JSONObject creditInfo = new JSONObject();
        creditInfo.put("userId", qrPayFlowDTO.getPayUserId());
        creditInfo.put("merchantId", qrPayFlowDTO.getMerchantId());
        creditInfo.put("merchantName", qrPayFlowDTO.getCorporateName());
        creditInfo.put("borrowAmount", qrPayFlowDTO.getPayAmount());
        creditInfo.put("productId", qrPayDTO.getProductId());
        creditInfo.put("discountPackageAmount", qrPayFlowDTO.getWholeSalesAmount());

        creditInfo.put("orderAmount", qrPayFlowDTO.getTransAmount());
        creditInfo.put("redEnvelopeAmount", qrPayFlowDTO.getRedEnvelopeAmount());
        JSONObject requestData = new JSONObject();
        requestData.put("createBorrowMessageDTO", creditInfo);
        requestData.put("accessSideId", Constant.CREDIT_MERCHANT_ID);
        requestData.put("isShow", StaticDataEnum.STATUS_0.getCode());
        requestData.put("tripartiteTransactionNo", qrPayFlowDTO.getId());
        JSONObject result = null;
        String returnState = "";
        try {
            result = serverService.apiCreditOrderOld(requestData, request);
        } catch (Exception e) {
            // 请求可疑全部置为失败，并返回通知
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            qrPayFlowService.updateQrPayFlowForConcurrency(qrPayFlowDTO.getId(), qrPayFlowDTO, request);
            creditInfo.clear();
            creditInfo.put("tripartiteTransactionNo", qrPayFlowDTO.getId());
//            creditInfo.put("accessSideId", accessMerchantDTO.getPlatformId());
            serverService.orderStateRollback(creditInfo, request);

            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        // 获取结果跟新订单信息
        Integer orderState = result.getInteger("transactionResult");
        qrPayFlowService.dealCreditOrderResultOld(result,qrPayFlowDTO,orderState,request);
        if (orderState.intValue() == StaticDataEnum.API_ORDER_STATE_0.getCode()) {
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        } else  if (orderState.intValue() == StaticDataEnum.API_ORDER_STATE_3.getCode()){
            throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
        }
        sendMessage(qrPayFlowDTO.getRecUserId(), qrPayFlowDTO.getId(), request);
        return qrPayFlowDTO;
    }

    private QrPayFlowDTO getQrPayFlowOld(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, HttpServletRequest request) throws Exception {

        //查询商户配置信息
        MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayDTO.getMerchantId());
        if(merchantDTO==null || merchantDTO.getId()==null ||  merchantDTO.getIsAvailable() !=1) {
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //前面校验过，直接取来用
        BigDecimal wholeSalesAmount = qrPayDTO.getWholeSalesAmount() == null ?  BigDecimal.ZERO : qrPayDTO.getWholeSalesAmount();
        BigDecimal normalSaleAmount = qrPayDTO.getTransAmount().subtract(qrPayDTO.getWholeSalesAmount());
        BigDecimal redEnvelopeAmount = qrPayDTO.getRedEnvelopeAmount() == null ? BigDecimal.ZERO : qrPayDTO.getRedEnvelopeAmount();
        //查询商户整体出售余额
        BigDecimal merchantAmount = userService.getBalance(qrPayDTO.getRecUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
        //金额计算
        if(normalSaleAmount.compareTo(BigDecimal.ZERO) > 0){

            //必须先用整体出售的余额
            if(merchantAmount.compareTo(wholeSalesAmount) != 0 ){
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            //用户基础折扣金额
            BigDecimal baseDiscountAmount = normalSaleAmount.multiply(merchantDTO.getBaseRate() == null ? BigDecimal.ZERO : merchantDTO.getBaseRate()).setScale(2,BigDecimal.ROUND_HALF_UP);
            //用户时间内营销折扣金额
            Long today = System.currentTimeMillis();
            Long extraDiscountPeriod = merchantDTO.getExtraDiscountPeriod();
            BigDecimal extraDiscount = null;
            if (today.longValue() < extraDiscountPeriod.longValue()) {
                extraDiscount = merchantDTO.getExtraDiscount();
            } else {
                extraDiscount = new BigDecimal("0.00");
            }
            BigDecimal extraDiscountAmount = normalSaleAmount.multiply(extraDiscount == null ? BigDecimal.ZERO :extraDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
            //计算营销折扣金额
            BigDecimal markingDiscountAmount = normalSaleAmount.multiply(merchantDTO.getMarketingDiscount() == null ? BigDecimal.ZERO : merchantDTO.getMarketingDiscount()).setScale(2,BigDecimal.ROUND_HALF_UP);
            qrPayFlowDTO.setBaseDiscountAmount(baseDiscountAmount);
            qrPayFlowDTO.setExtraDiscountAmount(extraDiscountAmount);
            qrPayFlowDTO.setMarkingDiscountAmount(markingDiscountAmount);

            if(wholeSalesAmount.compareTo(BigDecimal.ZERO)>0){
                qrPayFlowDTO.setSaleType(StaticDataEnum.SALE_TYPE_2.getCode());
                qrPayFlowDTO.setWholeSalesDiscount(merchantDTO.getWholeSaleUserDiscount());
            }else{
                qrPayFlowDTO.setSaleType(StaticDataEnum.SALE_TYPE_0.getCode());
            }
        }else{
            //判断整体出售余额足够
            if(merchantAmount.compareTo(wholeSalesAmount)<0){
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            qrPayFlowDTO.setBaseDiscountAmount(BigDecimal.ZERO);
            qrPayFlowDTO.setExtraDiscountAmount(BigDecimal.ZERO);
            qrPayFlowDTO.setMarkingDiscountAmount(BigDecimal.ZERO);
            qrPayFlowDTO.setSaleType(StaticDataEnum.SALE_TYPE_1.getCode());
            qrPayFlowDTO.setWholeSalesDiscount(merchantDTO.getWholeSaleUserDiscount());
        }


        BigDecimal wholeSaleUserDiscountAmount = wholeSalesAmount.multiply(merchantDTO.getWholeSaleUserDiscount() == null ?BigDecimal.ZERO : merchantDTO.getWholeSaleUserDiscount()).setScale(2,BigDecimal.ROUND_HALF_UP);
        //实付金额 = 整体出售折后 + 正常交易折后（正常交易 - 基础折扣 - 时间内营销折扣 -营销折扣） - 红包金额
        BigDecimal payAmount = wholeSalesAmount.subtract(wholeSaleUserDiscountAmount).add(
                normalSaleAmount.subtract(qrPayFlowDTO.getBaseDiscountAmount()).subtract(qrPayFlowDTO.getExtraDiscountAmount()).subtract(qrPayFlowDTO.getMarkingDiscountAmount())
        ).subtract(redEnvelopeAmount);

        if(payAmount.compareTo(qrPayDTO.getTrulyPayAmount())!=0){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //平台服务费
        BigDecimal platformFee = normalSaleAmount.multiply(merchantDTO.getAppChargeRate() == null ? BigDecimal.ZERO : merchantDTO.getAppChargeRate()).setScale(2,BigDecimal.ROUND_HALF_UP);
        //实收金额 = 正常交易 - 基础折扣 - 时间内营销折扣 -营销折扣 - 平台服务费
        BigDecimal recAmount = normalSaleAmount.subtract(qrPayFlowDTO.getBaseDiscountAmount()).subtract(qrPayFlowDTO.getExtraDiscountAmount()).subtract(qrPayFlowDTO.getMarkingDiscountAmount()).subtract(platformFee);

        qrPayFlowDTO.setIsNeedClear(StaticDataEnum.NEED_CLEAR_TYPE_1.getCode());
        qrPayFlowDTO.setClearState(StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
        qrPayFlowDTO.setCreditOrderNo(SnowflakeUtil.generateId().toString());
        qrPayFlowDTO.setMerchantId(merchantDTO.getId());
        qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());

        qrPayFlowDTO.setPayAmount(payAmount);
        qrPayFlowDTO.setPlatformFee(platformFee);
        qrPayFlowDTO.setRecAmount(recAmount);
        qrPayFlowDTO.setClearAmount(recAmount);
        qrPayFlowDTO.setWholeSalesAmount(wholeSalesAmount);
        qrPayFlowDTO.setNormalSaleAmount(normalSaleAmount);
        qrPayFlowDTO.setRedEnvelopeAmount(redEnvelopeAmount);
        return  qrPayFlowDTO;
    }

    @Override
    public void doBatchAmountOutRollBack(QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) {
        try {
            if(qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) == 0 &&
                    (qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) == 0 || qrPayFlowDTO.getMarketingId() != null )){
                return;
            }
            //查询交易流水
            Map<String,Object> params = new HashMap<>(8);
            int[] transTypeList = {21,26};
            int[] stateList = {StaticDataEnum.TRANS_STATE_1.getCode(),StaticDataEnum.TRANS_STATE_5.getCode()};
            params.put("flowId",qrPayFlowDTO.getId());
            params.put("transTypeList",transTypeList);
            params.put("stateList",stateList);
            List<AccountFlowDTO> amountOutFlowList = accountFlowService.find(params,null,null);

            Long orderNo = SnowflakeUtil.generateId();
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            BigDecimal amountInAmount = BigDecimal.ZERO;
            for (AccountFlowDTO accountFlowDTO:amountOutFlowList){
                // 回滚报文拼写
                JSONObject json1 = new JSONObject();
                json1.put("serialNumber", accountFlowDTO.getId());
                json1.put("transAmount", accountFlowDTO.getTransAmount());
                json1.put("userId", accountFlowDTO.getUserId());
                json1.put("subAccountType",accountFlowDTO.getAccountType());
                // 交易方向（0：入账；1：出账）
                json1.put("transDirection", StaticDataEnum.DIRECTION_0.getCode());
                if(accountFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_26.getCode()){
                    json1.put("channelTransType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_28.getCode());
                }else if(accountFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode()){
                    json1.put("channelTransType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_27.getCode());
                }

                jsonArray.add(json1);
                AccountFlowDTO accountFlowDTO1 = new AccountFlowDTO();
                //添加回滚流水号，记录流水状态为回滚中
                accountFlowDTO1.setRollBackNo(orderNo);
                accountFlowDTO1.setState(StaticDataEnum.TRANS_STATE_4.getCode());
                accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO1,request);
                amountInAmount = amountInAmount.add(accountFlowDTO.getTransAmount());
            }
            // 增加备注方便对账
            jsonObject.put("remark","rollBack for "+ amountOutFlowList.get(0).getOrderNo());
            jsonObject.put("totalAmountOut", BigDecimal.ZERO);
            jsonObject.put("totalAmountIn",amountInAmount);
            jsonObject.put("totalNumber", amountOutFlowList.size());
            jsonObject.put("channelSerialnumber", orderNo);
            jsonObject.put("channel", "0001");
            jsonObject.put("dataList", jsonArray);
            jsonObject.put("channelTransType",StaticDataEnum.ACC_FLOW_TRANS_TYPE_30.getCode());
            //支付交易请求
            JSONObject result = null;
            try {
                result =serverService.batchChangeBalance(jsonObject);
            }catch (Exception e){
                log.error("doBatchAmountOutRollBack orderNo:" +orderNo+ ",Exception:"+e.getMessage()+",message:"+e);
                throw e;
            }
            //账户交易失败
            if(result != null && "2".equals(result.getString("errorState"))){
                // 更新出账记录为回滚失败
                for (AccountFlowDTO accountFlowDTO:amountOutFlowList){
                    AccountFlowDTO accountFlowDTO1 = new AccountFlowDTO();
                    accountFlowDTO1.setState(StaticDataEnum.TRANS_STATE_5.getCode());
                    accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO1,request);
                }
            }else{

                // 查询红包使用流水
                params.clear();
                params.put("flowId",qrPayFlowDTO.getId());
                params.put("transType",StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode());
                params.put("userId",qrPayFlowDTO.getPayUserId());

                MarketingFlowDTO marketingFlowDTO = marketingFlowService.findOneMarketingFlow(params);

                // 删除钱包出账记录
                if(marketingFlowDTO != null && marketingFlowDTO.getId() != null){

                    //交易失败 更改 出账记录为 失败
                    MarketingFlowDTO updateMarketingFlow = new MarketingFlowDTO();
                    updateMarketingFlow.setState(2);
                    marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(), updateMarketingFlow, request);
                    /*marketingFlowService.logicDeleteMarketingFlow(marketingFlowDTO.getId(),request);*/
                }
                // 更新出账记录为失败
                for (AccountFlowDTO accountFlowDTO:amountOutFlowList){
                    AccountFlowDTO accountFlowDTO1 = new AccountFlowDTO();
                    accountFlowDTO1.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO1,request);
                }

            }
        }catch (Exception e){
            log.error("flowId:"+qrPayFlowDTO.getId() +",doBatchAmountOutRollBack Exception :"+e.getMessage(),e);
        }
        // 查询是否有整体出售
        if(qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            // 查询商户是否是 无整体出售状态
            MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayFlowDTO.getMerchantId());
            // 如果商户无整体出售余额
            if(merchantDTO.getHaveWholeSell() == StaticDataEnum.MERCHANT_WHOLE_SELL_AMOUNT_ZERO.getCode()){
                try {
                    // 查询整体出售余额
//                    BigDecimal merchantAmount = userService.getBalance(qrPayFlowDTO.getRecUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
                    // 余额 > 0 时，更新为有整体出售
//                    if(merchantAmount.compareTo(BigDecimal.ZERO) > 0){
                    merchantService.updateHaveWholeSell(qrPayFlowDTO.getMerchantId(),StaticDataEnum.MERCHANT_WHOLE_SELL_AMOUNT_NOT_ZERO.getCode(),request);
//                    }

                }catch (Exception e){
                    log.error("doBatchAmountOutRollBack 整体出售状态变更失败 ，id:"+qrPayFlowDTO.getId());
                }
            }
        }
    }

    @Override
    public void qrPayByAccount(QrPayDTO qrPayDTO, UserDTO payUser, UserDTO recUser, QrPayFlowDTO qrPayFlowDTO, GatewayDTO gatewayDTO, HttpServletRequest request) throws Exception {
        //获取卡信息
        JSONObject cardObj = null;
        try {
            cardObj = serverService.getCardInfo(qrPayDTO.getCardId());
        } catch (Exception e) {
            //查询异常
            //交易失败
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        if (cardObj == null) {
            //无信息返回
            //交易失败
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        CardDTO cardDTO = JSONObject.parseObject(cardObj.toJSONString(), CardDTO.class);
        WithholdFlowDTO withholdFlowDTO = null;
        try {
            //TODO 手续费试算，未确定是否有试算接口
            BigDecimal channelCharge=BigDecimal.ZERO;
            if(gatewayDTO.getRate()!=null){
//                channelCharge = qrPayFlowDTO.getPayAmount().multiply(new BigDecimal(gatewayDTO.getRate())) ;
            }
            //创建三方流水
            if(StaticDataEnum.GATEWAY_TYPE_3.getCode() == gatewayDTO.getType()){
                //Latpay directPay
                withholdFlowDTO = getLatPayWithholdFlowDTO(qrPayFlowDTO, request, cardDTO, channelCharge,gatewayDTO);
            }else{
                //未知的渠道
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            //限额判断
            rechargeFlowService.channelLimit(gatewayDTO.getType(),qrPayFlowDTO.getPayAmount(),request);
            //保存三方代扣交易流水
            withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        } catch (Exception e) {
            //交易失败
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        //拼装请求信息
        JSONObject requestInfo = new JSONObject();
        //三方请求consumer信息
        JSONObject userInfo = serverService.userInfoByQRCode(payUser.getId());
        JSONObject consumer = new JSONObject();
        consumer.put("firstname", userInfo.getString("firstName"));
        consumer.put("lastname", userInfo.getString("lastName"));
        consumer.put("phone", userInfo.getString("phone"));
        consumer.put("email", userInfo.getString("email"));
        requestInfo.put("consumer", consumer);
        //三方请求order信息
        JSONObject order = new JSONObject();
        order.put("reference", "");
        order.put("currency", StaticDataEnum.CURRENCY_TYPE.getMessage());
        order.put("amount", qrPayDTO.getTrulyPayAmount());
        requestInfo.put("order", order);
        //三方请求bill信息
        JSONObject bill = new JSONObject();
        //bill内directDebit信息
        JSONObject directDebit = new JSONObject();
        directDebit.put("bsb", cardDTO.getBsb());
        directDebit.put("accountnumber", cardDTO.getCardNo());
        directDebit.put("accountname", cardDTO.getAccountName());
        bill.put("directdebit", directDebit);
        //bill内fee信息
        JSONObject fee = new JSONObject();
        fee.put("processingfee", "?????");
        bill.put("fees", fee);
        requestInfo.put("billing", bill);
        requestInfo.put("notifyurl", latpayNoticeUrl);
        //请求返回值
        Integer state = null;
        try {
//            state = latPayService.latPayDirectDebitRequest(requestInfo, request);
        } catch (Exception e) {
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
            updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
            throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
        }
        //修改流水状态。。。。。
    }


    private void dealThirdSuccess(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException{
        if(qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode() && qrPayFlowDTO.getCardId() != null && qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
            // 卡支付交易 , 并且为记录交易新数据，并且调用三方进行交易的
            // 增加用户卡失败次数清除
            try {
                userService.logCardFailedTime(qrPayFlowDTO.getPayUserId(),Long.parseLong(qrPayFlowDTO.getCardId()),true,request);
            }catch ( Exception e){
                log.error("卡支付交易成功后，清除失败次数失败，flowId："+withholdFlowDTO.getFlowId()+",exception:"+e.getMessage(),e);
            }

        }

        // 不需要进行清算和清算金额为0的交易
        if(qrPayFlowDTO.getIsNeedClear() == StaticDataEnum.NEED_CLEAR_TYPE_0.getCode() || qrPayFlowDTO.getRecAmount().compareTo(BigDecimal.ZERO) == 0
                || qrPayFlowDTO.getRecUserId() == null ){
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
            updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
//            updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
            //发送成功消息
            if(qrPayFlowDTO.getRecUserId() != null){
                sendMessage(qrPayFlowDTO.getRecUserId(), qrPayFlowDTO.getId(), request);
            }
        }else{
            // 当交易状态不是31时，为正常交易，需要改变交易状态为入账处理中
            // 当状态为31时为跑批处理，不改变交易状态
            if(qrPayFlowDTO.getState() != StaticDataEnum.TRANS_STATE_31.getCode() ){
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_30.getCode());
                updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
                //发送成功消息
                sendMessage(qrPayFlowDTO.getRecUserId(), qrPayFlowDTO.getId(), request);
            }
            //创建账户交易流水
            AccountFlowDTO accountFlowDTO = getAccountFlowDTO(qrPayFlowDTO);
            //保存账户交易流水
            accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO, request));
            try {
                doAmountTrans(accountFlowDTO, qrPayFlowDTO, request);
            } catch (Exception e) {
                log.error("qrPay dealThirdSuccess Exception:"+e.getMessage(),e);
                //交易失败
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                updateFlow(qrPayFlowDTO, null, null, request);
            }

        }
        if(qrPayFlowDTO.getState() == StaticDataEnum.TRANS_STATE_31.getCode() ){
            // 更新交易状态为成功
            //qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
            //updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
            // 查询是否是正常交易
            if(qrPayFlowDTO.getNormalSaleAmount().compareTo(BigDecimal.ZERO) > 0){
                // 查询商户是否是 无整体出售状态
                MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayFlowDTO.getMerchantId());
                // 商户有整体出售余额
                if(merchantDTO.getHaveWholeSell() == StaticDataEnum.MERCHANT_WHOLE_SELL_AMOUNT_NOT_ZERO.getCode()){
                    try {
                        // 查询整体出售余额
                        BigDecimal merchantAmount = userService.getBalance(qrPayFlowDTO.getRecUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
                        // 余额等于0 时，更新为无整体出售
                        if(merchantAmount.compareTo(BigDecimal.ZERO) == 0){
                            merchantService.updateHaveWholeSell(qrPayFlowDTO.getMerchantId(),0,request);
                        }

                    }catch (Exception e){
                        log.error("dealThirdSuccess 整体出售状态变更失败 ，id:"+qrPayFlowDTO.getId());
                    }
                }
            }

            try {
                // 交易成功推荐人处理
                UserDTO payUser = userService.findUserById(qrPayFlowDTO.getPayUserId());
                userService.firstPaidSuccessAmountInV2(payUser.getId(), payUser.getInviterId(), StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode(), qrPayFlowDTO.getId(), request);
            }catch (Exception e){
                log.error("交易成功推荐人处理出错,e:{}" , e);
            }
        }

    }

    /**
     * 账户入账
     *
     * @param accountFlowDTO
     * @param qrPayFlowDTO
     * @param request
     * @throws BizException
     */
    @Override
    public void doAmountTrans(AccountFlowDTO accountFlowDTO, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException {
        //交易请求
        JSONObject accTransObj = null;
        if (qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_3.getCode() ||
                qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_1.getCode()) {
            //暂时无互转分支
//            Map<String, Object> map = getAccTransMap(accountFlowDTO);
//            accTransObj = serverService.accountTransfer(JSONObject.parseObject(JSON.toJSONString(map)));
            return;
        } else {

            Map<String, Object> map = getAmountInMap(accountFlowDTO);
            try {
                accTransObj = serverService.amountIn(JSONObject.parseObject(JSON.toJSONString(map)));
            } catch (Exception e) {
                //交易异常
                accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_33.getCode());
                updateFlow(qrPayFlowDTO, accountFlowDTO, null, request);
                throw new BizException(I18nUtils.get("recharge.doubtful", getLang(request)));
            }

        }


        if (accTransObj == null) {
            //反回空，可疑
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_33.getCode());
            updateFlow(qrPayFlowDTO, accountFlowDTO, null, request);
            throw new BizException(I18nUtils.get("recharge.doubtful", getLang(request)));
        }

        //记录返回参数
        accountFlowDTO.setReturnCode(accTransObj.getString("code"));
        accountFlowDTO.setReturnMessage(accTransObj.getString("message"));
        qrPayFlowDTO.setErrorMessage(accTransObj.getString("message"));
        qrPayFlowDTO.setErrorCode(accTransObj.getString("code"));

        if (accTransObj.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            //交易失败
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
            updateFlow(qrPayFlowDTO, accountFlowDTO, null, request);
            throw new BizException(accTransObj.getString("message"));
        } else if (accTransObj.getString("code").equals(ResultCode.OK.getCode())) {
            //交易成功
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
            updateFlow(qrPayFlowDTO, accountFlowDTO, null, request);
        } else {
            //交易可疑
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_33.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);
            throw new BizException(I18nUtils.get("recharge.doubtful", getLang(request)));
        }
    }

    private GatewayDTO getGateWay(QrPayDTO qrPayDTO, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException {
        Map<String,Object> gateWayMap = new HashMap<>();
        gateWayMap.put("gatewayType",qrPayDTO.getPayType());
        gateWayMap.put("state",StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(gateWayMap);
//        qrPayFlowDTO.setGatewayId(gatewayDTO.getType().longValue());
//        updateFlow(qrPayFlowDTO, null, null, request);
        return gatewayDTO;
    }

    @Override
    public Map<String, Object> getAmountInMap(AccountFlowDTO accountFlowDTO) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", accountFlowDTO.getUserId());
        map.put("subAccountType", accountFlowDTO.getAccountType());
        map.put("channel", "pay");
        map.put("channelSerialnumber", accountFlowDTO.getOrderNo());
        map.put("channelTransType", accountFlowDTO.getTransType());
        map.put("transAmount", accountFlowDTO.getTransAmount());
        map.put("channelRemark", accountFlowDTO.getRemark());
        map.put("oppositeAccountNo", accountFlowDTO.getOppositeUserId());

        return map;
    }

    /**
     * 账户可疑查证
     */
    @Override
    public void qrPayAccountDoubtHandle() throws Exception {

        //查询账户可疑交易
        List<QrPayFlowDTO> doubleList = qrPayFlowService.findAccountDoubleFlow();
        for (QrPayFlowDTO qrPayFlowDTO : doubleList) {
            //查询账户流水
            Map<String,Object> params = new HashMap<>();
            params.put("flowId",qrPayFlowDTO.getId());
            int[]  transTypes = {StaticDataEnum.ACC_FLOW_TRANS_TYPE_3.getCode(),StaticDataEnum.ACC_FLOW_TRANS_TYPE_31.getCode()};
            int[]  states = {0,3};
            params.put("transTypes",transTypes);
            params.put("states",states);
            AccountFlowDTO accountFlowDTO = accountFlowDAO.selectByFlowId(params);
            try {
                if (accountFlowDTO.getId() ==null || accountFlowDTO.getState() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                    //无交易记录或者记录为失败
                    //交易失败
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                    updateFlow(qrPayFlowDTO, null, null, null);
                    continue;
                } else if (accountFlowDTO.getState() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                    //交易成功
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
                    updateFlow(qrPayFlowDTO, null, null, null);
                    continue;
                }
                JSONObject data;

                data = serverService.transactionInfo(accountFlowDTO.getOrderNo().toString());

                if (data != null ) {
                    if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                        //成功
                        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                        updateFlow(qrPayFlowDTO, accountFlowDTO, null, null);
                    } else if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                        //失败
                        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                        updateFlow(qrPayFlowDTO, accountFlowDTO, null, null);
                    }
                }else{
                    // 查询无信息为失败
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                    accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    updateFlow(qrPayFlowDTO, accountFlowDTO, null, null);
                }
            } catch (Exception e) {
                //交易异常，不处理交易结果
                continue;
            }


        }
    }

    /**
     * 账户失败处理
     */
    @Override
    public void qrPayAccountFailHandle() throws Exception {
        {

            //查询账户入账失败交易
            List<QrPayFlowDTO> failList = qrPayFlowService.findAccountFailFlow();
            for (QrPayFlowDTO qrPayFlowDTO : failList) {
                AccountFlowDTO accountFlowDTO = getAccountFlowDTO(qrPayFlowDTO);
                try {
                    //更新流水为入账处理中
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_30.getCode());
                    updateFlow(qrPayFlowDTO, null, null, null);
                    //保存账户交易流水
                    accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO, null));

                } catch (Exception e) {
                    //交易失败
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                    updateFlow(qrPayFlowDTO, null, null, null);
                    continue;
                }
                //账户入账
                try{
                    doAmountTrans(accountFlowDTO, qrPayFlowDTO, null);
                }catch (Exception e){
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_33.getCode());
                    accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                    updateFlow(qrPayFlowDTO, accountFlowDTO, null, null);
                }
            }
        }
    }

    /**
     * 三方可疑查证
     */
    @Override
    public void qrPayThirdDoubtHandle() throws Exception {
        List<QrPayFlowDTO> doubtList = qrPayFlowService.findThirdDoubtFlow();
        //查询路由信息
        for(QrPayFlowDTO qrPayFlowDTO:doubtList){

            dealOneThirdDoubtHandle(qrPayFlowDTO);

        }


    }

    @Override
    public Integer dealOneThirdDoubtHandle(QrPayFlowDTO qrPayFlowDTO) {
        try{
            Map<String,Object> params = new HashMap<>();
            params.put("flowId",qrPayFlowDTO.getId());
            //查询三方流水
            if(qrPayFlowDTO.getGatewayId() == null ){
                //分期付
            }else{

                JSONObject cardObj = null;
                try {
                    cardObj = serverService.getCardInfo(Long.parseLong(qrPayFlowDTO.getCardId()));
                } catch (Exception e) {
                    log.error("三方查证流程，获取卡信息失败,flow_id:"+qrPayFlowDTO.getId()+",error:"+e.getMessage(),e);
                }

                WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(params);
                if(withholdFlowDTO.getId()==null || withholdFlowDTO.getState() == StaticDataEnum.TRANS_STATE_2.getCode()){

                    Integer payState = (cardObj == null || StringUtils.isBlank(cardObj.toJSONString())) ? null : cardObj.getInteger("payState");
                    //失败
                    handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, withholdFlowDTO.getOrderAmount(), payState, null);

                    return  StaticDataEnum.TRANS_STATE_2.getCode();
                    //交易失败
                        /*qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_22.getCode());
                        rechargeFlowService.channelLimitRollback(qrPayFlowDTO.getCreatedDate(),qrPayFlowDTO.getGatewayId(),withholdFlowDTO.getTransAmount(),null);
                        updateFlow(qrPayFlowDTO, null, null, null);*/

                }else if(withholdFlowDTO.getState() == StaticDataEnum.TRANS_STATE_1.getCode()){

                    //如果三方流水已经是成功的，重新查询交易状态
                    QrPayFlowDTO qrPayFlowDTO_  = qrPayFlowService.findQrPayFlowById(qrPayFlowDTO.getId());
                    if(qrPayFlowDTO_.getState() == StaticDataEnum.TRANS_STATE_20.getCode() ||
                            qrPayFlowDTO_.getState() == StaticDataEnum.TRANS_STATE_23.getCode() ){
                        //交易卡在可疑状态
                        dealThirdSuccess(withholdFlowDTO,qrPayFlowDTO,null);
                        //成功
                        if(null != qrPayFlowDTO.getMarketingId() && null != qrPayFlowDTO.getMarketingManageId()){
                            //增加使用次数
                            HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
                            paramMap.put("id", qrPayFlowDTO.getMarketingManageId());
                            marketingManagementService.addUsedNumber(paramMap);
                        }
                        try{

                            if(null != cardObj.getInteger("payState") && cardObj.getInteger("payState") == StaticDataEnum.STATUS_0.getCode()){
                                // 设置卡支付成功标识
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("cardId", qrPayFlowDTO.getCardId());
                                serverService.setCardSuccessPay(jsonObject);
                            }
                        }catch (Exception e){
                            log.error("设置卡支付成功标识,e:{},userId",e);
                        }
                        return  StaticDataEnum.TRANS_STATE_1.getCode();
                    }
                }
                if (StaticDataEnum.GATEWAY_TYPE_0.getCode() == withholdFlowDTO.getGatewayId().intValue()){
                    //latpay渠道
                    withholdFlowDTO = latPayService.latPayDoubtHandle(withholdFlowDTO);
                } else if (StaticDataEnum.GATEWAY_TYPE_1.getCode() == withholdFlowDTO.getGatewayId().intValue()
                        || StaticDataEnum.GATEWAY_TYPE_2.getCode() == withholdFlowDTO.getGatewayId().intValue()) {
                    //支付宝、微信渠道
                    Integer orderStatus = omiPayService.statusCheck(withholdFlowDTO.getOmiPayOrderNo());
                    withholdFlowDTO.setState(orderStatus);
                }else if(StaticDataEnum.GATEWAY_TYPE_4.getCode() == withholdFlowDTO.getGatewayId().intValue()){
                    String token = integraPayService.apiAccessToken();
                    int state = integraPayService.payByCardStatusCheck(withholdFlowDTO.getGatewayMerchantId(),withholdFlowDTO.getOrdreNo(),token);
                    withholdFlowDTO.setState(state);
                }else if(StaticDataEnum.GATEWAY_TYPE_8.getCode() == withholdFlowDTO.getGatewayId().intValue()){
                    // stripe 通道 todo
                    int state = this.retrievePaymentIntent(withholdFlowDTO.getStripeId(), withholdFlowDTO);
                    withholdFlowDTO.setState(state);
                }
//                    withholdFlowDTO.setState(2);

                if (StaticDataEnum.TRANS_STATE_1.getCode() == withholdFlowDTO.getState()) {
                    //三方成功后的处理
                    //更新三方流水状态，防止并发处理
//                        dealThirdSuccess(withholdFlowDTO,qrPayFlowDTO,null);
                    //成功
                    dealThirdSuccess(withholdFlowDTO,qrPayFlowDTO,null);


                    try{
                        if(null != cardObj.getInteger("payState") && cardObj.getInteger("payState") == StaticDataEnum.STATUS_0.getCode()){
                            // 设置卡支付成功标识
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("cardId", qrPayFlowDTO.getCardId());
                            serverService.setCardSuccessPay(jsonObject);
                        }
                    }catch (Exception e){
                        log.error("设置卡支付成功标识,e:{},userId", e);
                    }

                    if(null != qrPayFlowDTO.getMarketingId() && null != qrPayFlowDTO.getMarketingManageId()){
                        //增加使用次数
                        HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
                        paramMap.put("id", qrPayFlowDTO.getMarketingManageId());
                        marketingManagementService.addUsedNumber(paramMap);
                    }
                    return withholdFlowDTO.getState();
                } else if (StaticDataEnum.TRANS_STATE_2.getCode() == withholdFlowDTO.getState()) {

                    Integer payState = (cardObj == null || StringUtils.isBlank(cardObj.toJSONString())) ? null : cardObj.getInteger("payState");
                    //失败
                    handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, withholdFlowDTO.getOrderAmount(), payState, null);
//                        dealThirdFail(withholdFlowDTO, qrPayFlowDTO, null);
                    return withholdFlowDTO.getState();
                } else {
                    //可疑
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
                    updateFlow(qrPayFlowDTO, null, withholdFlowDTO, null);
                    return StaticDataEnum.TRANS_STATE_3.getCode();
                }
            }

        }catch (Exception e){
            log.info("QrPayService.qrPayThirdDoubtHandle,交易异常:"+e.getMessage(),e);

        }
        return StaticDataEnum.TRANS_STATE_3.getCode();
    }

    private int retrievePaymentIntent(String stripeId, WithholdFlowDTO withholdFlowDTO) {

        // stripe 交易结果查证
        StripeAPIResponse response = stripeService.retrievePaymentIntent( stripeId );
        if(response == null ){
            // 返回结果失败
            withholdFlowDTO.setErrorMessage("Failed to request the Stripe API");

            return StaticDataEnum.TRANS_STATE_2.getCode();
        }


        if(response.isSuccess()  ){

            PaymentIntent paymentIntent = (PaymentIntent)response.getData();

            withholdFlowDTO.setReturnMessage(paymentIntent.getStatus());

            // 查证请求成功
            if(StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_REQUIRES_ACTION.getCode().equals(paymentIntent.getStatus())){
                // 3ds验证,返回3ds处理中
                return StaticDataEnum.TRANS_STATE_44.getCode();
            }else if(StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_SUCCEEDED.getCode().equals(paymentIntent.getStatus())){
                // 返回成功
                return StaticDataEnum.TRANS_STATE_1.getCode();
            }else if(StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_CANCELED.getCode().equals(paymentIntent.getStatus())
                    || StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_REQUIRES_PAYMENT_METHOD.getCode().equals(paymentIntent.getStatus())){

                withholdFlowDTO.setErrorMessage(paymentIntent.getLastPaymentError().getMessage());
                withholdFlowDTO.setReturnCode(paymentIntent.getLastPaymentError().getCode());
                // 返回失败
                withholdFlowDTO.setErrorMessage(paymentIntent.getStatus());

                return StaticDataEnum.TRANS_STATE_2.getCode();
            }else{
                // 其他情况返回处理中
                return StaticDataEnum.TRANS_STATE_3.getCode();
            }
        }else{
            // 返回结果失败
            withholdFlowDTO.setErrorMessage(response.getMessage());

            return StaticDataEnum.TRANS_STATE_2.getCode();
        }
//        return StaticDataEnum.TRANS_STATE_44.getCode();
    }


    /**
     * 余额支付方法
     *
     * @param qrPayDTO
     * @param request
     * @param payUser
     * @param recUser
     * @param qrPayFlowDTO
     * @throws BizException
     */
    @Override
    public void qrPayByBalance(QrPayDTO qrPayDTO, HttpServletRequest request, UserDTO payUser, UserDTO recUser, QrPayFlowDTO qrPayFlowDTO) throws BizException {

        //创建账户交易流水
        AccountFlowDTO accountFlowDTO = getAccountFlowDTO(qrPayFlowDTO);
        try {
            //保存账户交易流水
            accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO, request));
        } catch (Exception e) {
            //交易失败
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        //账户入账
        doAmountTrans(accountFlowDTO, qrPayFlowDTO, request);



    }


    /**
     * 账户交易状态校验
     *
     * @param userId
     * @return
     */
    @Override
    public boolean checkAccountState(Long userId) {
            try {
            JSONObject accObj = serverService.getAccountInfo(userId);
            if (accObj == null) {
                return false;
            }
            if (StaticDataEnum.ACCOUNT_STATE_1.getCode() == accObj.getInteger("state")) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateFlow(QrPayFlowDTO qrPayFlowDTO, AccountFlowDTO accountFlowDTO, WithholdFlowDTO withholdFlowDTO, HttpServletRequest request) throws BizException {

        if (accountFlowDTO != null) {
            accountFlowService.updateAccountFlow(accountFlowDTO.getId(), accountFlowDTO, request);
        }
        if (qrPayFlowDTO != null) {
            qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(), qrPayFlowDTO, request);
            //同步pos_qr_pay 订单状态
            if(null != qrPayFlowDTO.getPosOrder() && qrPayFlowDTO.getPosOrder()){
                try {
                    updatePosOrderStatus(qrPayFlowDTO.getTransNo(), qrPayFlowDTO.getState(), request);
                }catch (BizException e){
                    log.error("更新POS 订单出错，数据信息为：transNo: " + qrPayFlowDTO.getTransNo() + "|| state:" + qrPayFlowDTO.getState());
                }
            }

            Integer state = qrPayFlowDTO.getState();
            if(null != state && null != qrPayFlowDTO.getDonationAmount() && qrPayFlowDTO.getDonationAmount().compareTo(BigDecimal.ZERO) > 0){
                if(state.equals(StaticDataEnum.TRANS_STATE_31.getCode())){
                    state = StaticDataEnum.TRANS_STATE_1.getCode();
                }
                donationFlowService.updateDonationFlowStateByFlowId(qrPayFlowDTO.getId(), state, request);
            }

            if(null != state && null != qrPayFlowDTO.getTipAmount() && qrPayFlowDTO.getTipAmount().compareTo(BigDecimal.ZERO) > 0){
                if(state.equals(StaticDataEnum.TRANS_STATE_31.getCode())){
                    state = StaticDataEnum.TRANS_STATE_1.getCode();
                }
                tipFlowService.updateStateByFlowId(qrPayFlowDTO.getId(), state, request);
            }
        }
        if (withholdFlowDTO != null) {
            withholdFlowService.updateWithholdFlow(withholdFlowDTO.getId(), withholdFlowDTO, request);
        }

    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateFlowForConcurrency(QrPayFlowDTO qrPayFlowDTO, AccountFlowDTO accountFlowDTO, WithholdFlowDTO withholdFlowDTO, HttpServletRequest request) throws BizException {

        if (accountFlowDTO != null) {
            accountFlowService.updateAccountFlowForConcurrency(accountFlowDTO.getId(), accountFlowDTO, request);
        }
        if (qrPayFlowDTO != null) {
            qrPayFlowService.updateQrPayFlowForConcurrency(qrPayFlowDTO.getId(), qrPayFlowDTO, request);

            //同步pos_qr_pay 订单状态
            if(null != qrPayFlowDTO.getPosOrder() && qrPayFlowDTO.getPosOrder()){
                try {
                    updatePosOrderStatus(qrPayFlowDTO.getTransNo(), qrPayFlowDTO.getState(), request);
                }catch (BizException e){
                    log.error("更新POS 订单出错，数据信息为：transNo: " + qrPayFlowDTO.getTransNo() + "|| state:" + qrPayFlowDTO.getState());
                }
            }
            Integer state = qrPayFlowDTO.getState();
            if(null != state && null != qrPayFlowDTO.getDonationAmount() && qrPayFlowDTO.getDonationAmount().compareTo(BigDecimal.ZERO) > 0){
                if(state.equals(StaticDataEnum.TRANS_STATE_31.getCode())){
                    state = StaticDataEnum.TRANS_STATE_1.getCode();
                }
                donationFlowService.updateDonationFlowStateByFlowId(qrPayFlowDTO.getId(), state, request);
            }

            if(null != state && null != qrPayFlowDTO.getTipAmount() && qrPayFlowDTO.getTipAmount().compareTo(BigDecimal.ZERO) > 0){
                if(state.equals(StaticDataEnum.TRANS_STATE_31.getCode())){
                    state = StaticDataEnum.TRANS_STATE_1.getCode();
                }
                tipFlowService.updateStateByFlowId(qrPayFlowDTO.getId(), state, request);
            }

        }
        if (withholdFlowDTO != null) {
            withholdFlowService.updateWithholdFlowForConcurrency(withholdFlowDTO.getId(), withholdFlowDTO, request);
        }

    }

    private WithholdFlowDTO getOmiPayWithholdFlowDTO(QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request,  BigDecimal channelCharge, GatewayDTO gatewayDTO) {
        WithholdFlowDTO withholdFlowDTO = new WithholdFlowDTO();
        withholdFlowDTO.setMNumber(gatewayDTO.getChannelMerchantId());
        withholdFlowDTO.setSecretKey(gatewayDTO.getPassword());
        withholdFlowDTO.setNoticeUrl(noticeUrl);
        withholdFlowDTO.setFlowId(qrPayFlowDTO.getId());
        withholdFlowDTO.setGatewayId(gatewayDTO.getType().longValue());
        withholdFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
        withholdFlowDTO.setOrdreNo(qrPayFlowDTO.getId().toString());
        withholdFlowDTO.setTransAmount(qrPayFlowDTO.getPayAmount());
        withholdFlowDTO.setCharge(channelCharge);
        withholdFlowDTO.setCurrency("AUD");
        withholdFlowDTO.setCustomerIpaddress(request.getRequestURI());
        withholdFlowDTO.setTransType(qrPayFlowDTO.getTransType());
        withholdFlowDTO.setGatewayMerchantId(gatewayDTO.getChannelMerchantId());
        withholdFlowDTO.setGatewayMerchantPassword(gatewayDTO.getChannelMerchantId());
        return withholdFlowDTO;
    }



    private WithholdFlowDTO getLatPayWithholdFlowDTO(QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request, CardDTO cardDTO, BigDecimal channelCharge, GatewayDTO gatewayDTO) throws BizException {
        WithholdFlowDTO withholdFlowDTO = new WithholdFlowDTO();
        withholdFlowDTO.setFlowId(qrPayFlowDTO.getId());
        withholdFlowDTO.setGatewayId(gatewayDTO.getType().longValue());
        withholdFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
        withholdFlowDTO.setOrdreNo(SnowflakeUtil.generateId().toString());
        withholdFlowDTO.setTransAmount(qrPayFlowDTO.getPayAmount());
        withholdFlowDTO.setOrderAmount(qrPayFlowDTO.getPayAmount().subtract(channelCharge == null ? BigDecimal.ZERO : channelCharge));

        // 当支付渠道为卡支付时，直接记录折后金额
//        if (qrPayFlowDTO.getGatewayId().equals(new Long(StaticDataEnum.GATEWAY_TYPE_0.getCode())) && qrPayFlowDTO.getFeeDirection() == StaticDataEnum.FEE_DIRECTION_1.getCode()) {
////            withholdFlowDTO.setTransAmount(qrPayFlowDTO.getPayAmount());
////        } else {
////            withholdFlowDTO.setTransAmount(qrPayFlowDTO.getPayAmount());
////        }
        withholdFlowDTO.setCharge(qrPayFlowDTO.getFee());
//        withholdFlowDTO.setFee(qrPayFlowDTO.getPlatformFee());
        withholdFlowDTO.setFee(BigDecimal.ZERO);
        withholdFlowDTO.setBillAddress1(cardDTO.getAddress1());
        withholdFlowDTO.setBillAddress2(cardDTO.getAddress2());
        withholdFlowDTO.setBillCity(cardDTO.getCity());

        withholdFlowDTO.setBillZip(cardDTO.getZip());
        withholdFlowDTO.setBillState(cardDTO.getState());
        withholdFlowDTO.setBillFirstname(cardDTO.getFirstName());
        withholdFlowDTO.setBillMiddlename(cardDTO.getMiddleName());
        withholdFlowDTO.setBillLastname(cardDTO.getLastName());
        withholdFlowDTO.setCrdstrgToken(cardDTO.getCrdStrgToken());
        withholdFlowDTO.setCustomerEmail(cardDTO.getEmail());
        withholdFlowDTO.setCustomerIpaddress(qrPayFlowDTO.getPayUserIp());
        withholdFlowDTO.setRemark(qrPayFlowDTO.getRemark());
        withholdFlowDTO.setCustomerFirstname(cardDTO.getFirstName());
        withholdFlowDTO.setCustomerMiddlename(cardDTO.getMiddleName());
        withholdFlowDTO.setCustomerLastname(cardDTO.getLastName());
        withholdFlowDTO.setCustomerPhone(cardDTO.getPhone());
        withholdFlowDTO.setCurrency("AUD");
//        if(StaticDataEnum.ACC_FLOW_TRANS_TYPE_4.getCode()==qrPayFlowDTO.getTransType()){
        //卡转账
        withholdFlowDTO.setGatewayMerchantId(gatewayDTO.getChannelMerchantId());
        withholdFlowDTO.setGatewayMerchantPassword(gatewayDTO.getPassword());
//        }else{
//            //卡支付，查询商户
//            MerchantDTO merchant = merchantService.findMerchantById(qrPayFlowDTO.getMerchantId());
//            Map<String,Object> map = new HashedMap();
//            map.put("merchantId",merchant.getId());
//            map.put("gatewayId",gatewayDTO.getType());
//            SecondMerchantGatewayInfoDTO  secondMerchantGatewayInfoDTO = secondMerchantGatewayInfoService.findOneSecondMerchantGatewayInfo(map);
//            withholdFlowDTO.setGatewayMerchantId(secondMerchantGatewayInfoDTO.getGatewayMerchantId());
//            withholdFlowDTO.setGatewayMerchantPassword(secondMerchantGatewayInfoDTO.getGatewayMerchantPassword());
//        }
        withholdFlowDTO.setTransType(qrPayFlowDTO.getTransType());
//        withholdFlowDTO.setCheckState(0);
        //查询国家ISO代码
        if (cardDTO.getCountry() != null) {
            Map<String, Object> params = new HashMap<>(1);
            params.put("code", "county");
            params.put("value", cardDTO.getCountry());
            StaticDataDTO staticDataDTO = staticDataService.findOneStaticData(params);
            CountryIsoDTO countryIsoDTO = tieOnCardFlowService.selectCountryIso(staticDataDTO.getEnName());
            withholdFlowDTO.setBillCountry(countryIsoDTO.getTwoLettersCoding());
        }
        log.info("withhold, data:{}", withholdFlowDTO);
        return withholdFlowDTO;
    }




    /**
     * 封装三方流水记录
     * @author zhangzeyuan
     * @date 2021/7/10 11:05
     * @param qrPayFlowDTO
     * @param cardDTO
     * @param cardPayFee
     * @param cardPayFeeRate
     * @param gatewayDTO
     * @return com.uwallet.pay.main.model.dto.WithholdFlowDTO
     */
    private WithholdFlowDTO packageWithHoldFlow(QrPayFlowDTO qrPayFlowDTO, CardDTO cardDTO, BigDecimal cardPayFee, BigDecimal cardPayFeeRate, GatewayDTO gatewayDTO) throws BizException {
        WithholdFlowDTO withholdFlowDTO = new WithholdFlowDTO();
        withholdFlowDTO.setFlowId(qrPayFlowDTO.getId());
        withholdFlowDTO.setGatewayId(gatewayDTO.getType().longValue());
        withholdFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
        withholdFlowDTO.setOrdreNo(SnowflakeUtil.generateId().toString());
        withholdFlowDTO.setTransAmount(qrPayFlowDTO.getPayAmount());
        withholdFlowDTO.setOrderAmount(qrPayFlowDTO.getPayAmount().subtract(cardPayFee == null ? BigDecimal.ZERO : cardPayFee));

        // 当支付渠道为卡支付时，直接记录折后金额
//        if (qrPayFlowDTO.getGatewayId().equals(new Long(StaticDataEnum.GATEWAY_TYPE_0.getCode())) && qrPayFlowDTO.getFeeDirection() == StaticDataEnum.FEE_DIRECTION_1.getCode()) {
////            withholdFlowDTO.setTransAmount(qrPayFlowDTO.getPayAmount());
////        } else {
////            withholdFlowDTO.setTransAmount(qrPayFlowDTO.getPayAmount());
////        }
        //设置卡费率 卡手续费
        withholdFlowDTO.setCharge(cardPayFee);
        withholdFlowDTO.setFeeRate(cardPayFeeRate);
//        withholdFlowDTO.setFee(qrPayFlowDTO.getPlatformFee());
        withholdFlowDTO.setFee(BigDecimal.ZERO);

        withholdFlowDTO.setBillAddress1(cardDTO.getAddress1());
        withholdFlowDTO.setBillAddress2(cardDTO.getAddress2());
        withholdFlowDTO.setBillCity(cardDTO.getCity());

        withholdFlowDTO.setBillZip(cardDTO.getZip());
        withholdFlowDTO.setBillState(cardDTO.getState());
        withholdFlowDTO.setBillFirstname(cardDTO.getFirstName());
        withholdFlowDTO.setBillMiddlename(cardDTO.getMiddleName());
        withholdFlowDTO.setBillLastname(cardDTO.getLastName());
        withholdFlowDTO.setCrdstrgToken(cardDTO.getCrdStrgToken());
        withholdFlowDTO.setCustomerEmail(cardDTO.getEmail());
        withholdFlowDTO.setCustomerIpaddress(qrPayFlowDTO.getPayUserIp());
        withholdFlowDTO.setRemark(qrPayFlowDTO.getRemark());
        withholdFlowDTO.setCustomerFirstname(cardDTO.getFirstName());
        withholdFlowDTO.setCustomerMiddlename(cardDTO.getMiddleName());
        withholdFlowDTO.setCustomerLastname(cardDTO.getLastName());
        withholdFlowDTO.setCustomerPhone(cardDTO.getPhone());
        withholdFlowDTO.setCurrency("AUD");
//        if(StaticDataEnum.ACC_FLOW_TRANS_TYPE_4.getCode()==qrPayFlowDTO.getTransType()){
        //卡转账
        withholdFlowDTO.setGatewayMerchantId(gatewayDTO.getChannelMerchantId());
        withholdFlowDTO.setGatewayMerchantPassword(gatewayDTO.getPassword());
//        }else{
//            //卡支付，查询商户
//            MerchantDTO merchant = merchantService.findMerchantById(qrPayFlowDTO.getMerchantId());
//            Map<String,Object> map = new HashedMap();
//            map.put("merchantId",merchant.getId());
//            map.put("gatewayId",gatewayDTO.getType());
//            SecondMerchantGatewayInfoDTO  secondMerchantGatewayInfoDTO = secondMerchantGatewayInfoService.findOneSecondMerchantGatewayInfo(map);
//            withholdFlowDTO.setGatewayMerchantId(secondMerchantGatewayInfoDTO.getGatewayMerchantId());
//            withholdFlowDTO.setGatewayMerchantPassword(secondMerchantGatewayInfoDTO.getGatewayMerchantPassword());
//        }
        withholdFlowDTO.setTransType(qrPayFlowDTO.getTransType());
//        withholdFlowDTO.setCheckState(0);
        //查询国家ISO代码
        if (cardDTO.getCountry() != null) {
            Map<String, Object> params = new HashMap<>(1);
            params.put("code", "county");
            params.put("value", cardDTO.getCountry());
            StaticDataDTO staticDataDTO = staticDataService.findOneStaticData(params);
            CountryIsoDTO countryIsoDTO = tieOnCardFlowService.selectCountryIso(staticDataDTO.getEnName());
            withholdFlowDTO.setBillCountry(countryIsoDTO.getTwoLettersCoding());
        }
        withholdFlowDTO.setCardNo(cardDTO.getCardNo());
        withholdFlowDTO.setCardCcType(cardDTO.getCustomerCcType());
        log.info("withhold, data:{}", withholdFlowDTO);
        return withholdFlowDTO;
    }


    private Map<String, Object> getAccTransMap(AccountFlowDTO accountFlowDTO) {

        Map<String, Object> map = new HashMap<>();
        map.put("userId", accountFlowDTO.getUserId());
        map.put("subAccountType", accountFlowDTO.getAccountType());
        map.put("channel", "pay");
        map.put("channelSerialnumber", accountFlowDTO.getOrderNo());
        map.put("channelTransType", accountFlowDTO.getTransType());
        map.put("transAmount", accountFlowDTO.getTransAmount());
        map.put("amountInUserId", accountFlowDTO.getOppositeUserId());
        map.put("feeDirection", accountFlowDTO.getFeeDirection());
        map.put("feeAmount", accountFlowDTO.getFee());
        return map;
    }

    @Override
    public AccountFlowDTO getAccountFlowDTO(QrPayFlowDTO qrPayFlowDTO) {
        AccountFlowDTO accountFlowDTO = new AccountFlowDTO();

        if (qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_3.getCode() ||
                qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_1.getCode()) {
            //互转
            accountFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
            accountFlowDTO.setOppositeUserId(qrPayFlowDTO.getRecUserId());
            accountFlowDTO.setFee(qrPayFlowDTO.getFee());
            accountFlowDTO.setFeeDirection(qrPayFlowDTO.getFeeDirection());
            accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_3.getCode());
        } else {
            //入账
            accountFlowDTO.setUserId(qrPayFlowDTO.getRecUserId());
            accountFlowDTO.setOppositeUserId(qrPayFlowDTO.getPayUserId());
            accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_31.getCode());
        }
        accountFlowDTO.setTransAmount(qrPayFlowDTO.getRecAmount());
        accountFlowDTO.setFlowId(qrPayFlowDTO.getId());
        accountFlowDTO.setAccountType(StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
        accountFlowDTO.setOppositeAccountType(StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
        accountFlowDTO.setOrderNo(SnowflakeUtil.generateId());

        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        accountFlowDTO.setRemark(qrPayFlowDTO.getRemark());

        return accountFlowDTO;
    }

    @Override
    public JSONObject aliOrWechatOrderStatusCheck(Long flowId, HttpServletRequest request) throws Exception {
        JSONObject result = new JSONObject();
        log.info("check flow status, flowId:{}", flowId);
        QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(flowId);
        // 查询商户
        MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayFlowDTO.getMerchantId());
        //若最终结果不是账务流水成功
        log.info("check qr pay flow dto status, qrPayFlowDTO:{}", qrPayFlowDTO);
        if (qrPayFlowDTO.getState() != StaticDataEnum.TRANS_STATE_31.getCode()) {
            //三方流水失败(22)，则交易失败
            if (qrPayFlowDTO.getState() == StaticDataEnum.TRANS_STATE_22.getCode()) {
                result.put("state", StaticDataEnum.TRANS_STATE_2.getCode());
                result.put("msg", I18nUtils.get("trans.failed", getLang(request)));
                return result;
            } else if(qrPayFlowDTO.getState() == StaticDataEnum.TRANS_STATE_20.getCode() || qrPayFlowDTO.getState() == StaticDataEnum.TRANS_STATE_23.getCode()){
                //查询三方流水(20,23状态为三方交易处理中)
                Map<String, Object> params = new HashMap<>(1);
                params.put("flowId", flowId);
                WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(params);
                //依照订单号查询订单信息，根据状态更新订单状态
                Integer orderStatus = omiPayService.statusCheck(withholdFlowDTO.getOmiPayOrderNo());
                if (orderStatus == StaticDataEnum.TRANS_STATE_1.getCode()) {
                    withholdFlowDTO.setState(orderStatus);
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_21.getCode());
                    log.info("update success status flow dto, qrPayFlowDTO:{}, withholdFlowDTO:{}", qrPayFlowDTO, withholdFlowDTO);
                    try{
                        updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
                    }catch (Exception e){
                        log.info("QrPayService.AliOrWechatOrderStatusCheck更新交易状态并发拦截"+e.getMessage(),e);
                        //重新查询交易状态,若已成功，返回成功，否则返回处理中
                        QrPayFlowDTO qrPayFlowDTO_ = qrPayFlowService.findQrPayFlowById(flowId);
                        if(qrPayFlowDTO_.getState() == StaticDataEnum.TRANS_STATE_31.getCode()){
                            result = getResult(result, StaticDataEnum.TRANS_STATE_1.getCode(), qrPayFlowDTO, merchantDTO, I18nUtils.get("trans.success", getLang(request)), request);
                            return result;
                        }else{
                            result = getResult(result, StaticDataEnum.TRANS_STATE_0.getCode(), qrPayFlowDTO, merchantDTO, I18nUtils.get("trans.doubtful", getLang(request)), request);
                            return result;
                        }

                    }
                    //创建账户交易流水
                    AccountFlowDTO accountFlowDTO = getAccountFlowDTO(qrPayFlowDTO);
                    try {
                        //保存账户交易流水
                        accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO, request));
                    } catch (Exception e) {
                        //交易失败
                        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                        updateFlow(qrPayFlowDTO, null, null, request);
                        log.info("account amount in failed, data:{}, error message:{}, e:{}", accountFlowDTO, e.getMessage(), e);
                        //入账失败，返回omipay状态
                        result.put("state", StaticDataEnum.TRANS_STATE_1.getCode());
                        result.put("msg", "");
                        return result;
                    }
                    doAmountTrans(accountFlowDTO, qrPayFlowDTO, request);
                    // 交易成功消息发送
                    log.info("send success message");
                    sendMessage(qrPayFlowDTO.getRecUserId(), qrPayFlowDTO.getId(), request);
                    result = getResult(result, StaticDataEnum.TRANS_STATE_1.getCode(), qrPayFlowDTO, merchantDTO, "", request);
                    return result;
                } else if (orderStatus == StaticDataEnum.TRANS_STATE_2.getCode()) {
                    //限额回滚
                    rechargeFlowService.channelLimitRollback(qrPayFlowDTO.getCreatedDate(), qrPayFlowDTO.getGatewayId(), withholdFlowDTO.getTransAmount(), request);
                    withholdFlowDTO.setState(orderStatus);
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_22.getCode());
                    log.info("update fail status flow dto, qrPayFlowDTO:{}, withholdFlowDTO:{}", qrPayFlowDTO, withholdFlowDTO);
                    updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
                    result = getResult(result, StaticDataEnum.TRANS_STATE_2.getCode(), qrPayFlowDTO, merchantDTO, I18nUtils.get("trans.failed", getLang(request)), request);
                    return result;
                } else {
                    result = getResult(result, StaticDataEnum.TRANS_STATE_0.getCode(), qrPayFlowDTO, merchantDTO, I18nUtils.get("trans.doubtful", getLang(request)), request);
                    return result;
                }
            }else{
                //其他状态交易处理中
                result = getResult(result, StaticDataEnum.TRANS_STATE_0.getCode(), qrPayFlowDTO, merchantDTO, I18nUtils.get("trans.doubtful", getLang(request)), request);
                return result;
            }
        }
        result = getResult(result, StaticDataEnum.TRANS_STATE_1.getCode(), qrPayFlowDTO, merchantDTO, I18nUtils.get("trans.success", getLang(request)), request);
        return result;
    }

    /**
     * 发送消息
     * @param userId
     * @param orderId
     * @param request
     * @throws BizException
     */
    @Async("taskExecutor")
    public void sendMessage(Long userId, Long orderId, HttpServletRequest request) throws BizException {

        log.info("send message pay success, userId:{}, orderId:{}", userId, orderId);
//        MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(new Integer(StaticDataEnum.SEND_NODE_13.getCode()).toString());
        try {
            MailTemplateDTO mailTemplateDTO = mailTemplateService.findMailTemplateBySendNode(new Integer(StaticDataEnum.SEND_NODE_25.getCode()).toString());
            //设置模板参数
            String[]  param ={orderId+""} ;
            //发送内容
            String sendMsg = null;
            String sendTitle =  null;
            sendMsg = userService.templateContentReplace(param,mailTemplateDTO.getEnSendContent());
            sendTitle =  userService.templateContentReplace(param,mailTemplateDTO.getEnMailTheme());
            UserDTO userDTO = userService.findUserById(userId);
            Map<String, Object> params = new HashMap<>(4);
            params.put("merchantId", userDTO.getMerchantId());
            List<UserDTO> userDTOList = userService.find(params, null, null);
            if (!CollectionUtils.isEmpty(userDTOList)) {
                params.clear();
                long orderAction = 28L;
                for (UserDTO userDTO1 : userDTOList) {
                    params.put("userId", userDTO1.getId());
                    params.put("actionId", orderAction);
                    UserActionDTO userActionDTO = userActionService.findOneUserAction(params);
                    if (userActionDTO.getId() != null) {
                        try {
                            NoticeDTO noticeDTO = new NoticeDTO();
                            noticeDTO.setUserId(userDTO1.getId());
                            noticeDTO.setTitle(sendTitle);
                            noticeDTO.setContent(sendMsg);
                            noticeService.saveNotice(noticeDTO, request);
                            if (StringUtils.isNotEmpty(userDTO1.getPushToken())) {
                                FirebaseDTO firebaseDTO = new FirebaseDTO();
                                firebaseDTO.setAppName("UwalletM");
                                firebaseDTO.setUserId(userDTO1.getId());
                                firebaseDTO.setTitle(sendTitle);
                                firebaseDTO.setBody(sendMsg);
                                firebaseDTO.setVoice(mailTemplateDTO.getVoice());
                                firebaseDTO.setRoute(mailTemplateDTO.getRoute());
                                serverService.pushFirebase(firebaseDTO,request);
                                firebaseDTO.setVoice(StaticDataEnum.VOICE_0.getCode());
                                serverService.pushFirebase(firebaseDTO,request);
                            }
                        } catch (Exception e) {
                            log.info("send message push failed, error message:{}, e:{}", e.getMessage(), e);
                        }
                    }
                }
            }
        }catch (Exception e){
            log.error("send message pay Exception, userId:{}, orderId:{} ,Exception :{} ,e:{}", userId, orderId ,e.getMessage() ,e);
        }

    }

    /**
     * 费用计算
     * @param qrPayFlowDTO
     * @return
     */
    @Override
    public QrPayFlowDTO calculation(QrPayDTO qrPayDTO, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException {
        UserDTO recUser = userService.findUserById(qrPayDTO.getRecUserId());
        MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayDTO.getMerchantId());
        //计算支付金额、收款金额、支付手续费、平台手续费
        if (recUser.getUserType() == StaticDataEnum.USER_TYPE_20.getCode()) {
            BigDecimal payFee = null;
            BigDecimal payAmount = null;
            BigDecimal recAmount = null;
            BigDecimal platformFee = null;
            //折后金额
            BigDecimal discountedAmount = null;

            ParametersConfigDTO parametersConfigDTO = parametersConfigService.findParametersConfigById(1L);
            //折后金额 =  交易金额 * 支付折扣
            discountedAmount = qrPayDTO.getTransAmount().multiply(MathUtils.subtract(new BigDecimal(1), qrPayDTO.getPayDiscountRate())).setScale(2, RoundingMode.HALF_UP);
            //支付手续费 折后价格 * 支付渠道费率
            payFee = discountedAmount.multiply(qrPayDTO.getRate()).setScale(2, RoundingMode.HALF_UP);

            //实付金额 折后价格 + 支付手续费（当支付方付手续费时候加上）
            if (qrPayDTO.getFeeDirection() == StaticDataEnum.FEE_DIRECTION_1.getCode()) {
                payAmount = discountedAmount.add(payFee) ;
            } else {
                payAmount = discountedAmount;
            }

            //对比前端计算的实付金额对比，错误则交易失败
            if (payAmount.compareTo(qrPayDTO.getTrulyPayAmount()) != 0) {
                log.info("pay amount compare, getPayAmount:{}, selfPayAmount:{}", qrPayDTO.getTrulyPayAmount(), payAmount);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            //实收金额 交易金额 * （1 - （商户让利 + 平台所得 + 营销折扣 + 额外折扣）） - 支付手续费（当收款人付手续费时加上）
            Long extraDiscountPeriod = merchantDTO.getExtraDiscountPeriod();
            BigDecimal extraDiscount = null;
            Long today = System.currentTimeMillis();
            if (today.longValue() > extraDiscountPeriod.longValue()) {
                extraDiscount = merchantDTO.getExtraDiscount();
            } else {
                extraDiscount = new BigDecimal("0.00");
            }
            if (qrPayDTO.getFeeDirection() == StaticDataEnum.FEE_DIRECTION_1.getCode()) {
                recAmount = qrPayDTO.getTransAmount().multiply(
                        MathUtils.subtract(new BigDecimal(1), merchantDTO.getPaySellDiscount()
                                .add(merchantDTO.getPayRebateDiscount())
                                .add(merchantDTO.getMarketingDiscount()))
                                .add(extraDiscount).setScale(2, RoundingMode.HALF_UP)
                );
            } else {
                recAmount = qrPayDTO.getTransAmount().multiply(
                        MathUtils.subtract(new BigDecimal(1), merchantDTO.getPaySellDiscount()
                                .add(merchantDTO.getPayRebateDiscount())
                                .add(merchantDTO.getMarketingDiscount()))
                                .add(extraDiscount).setScale(2, RoundingMode.HALF_UP)
                );
                recAmount = recAmount.subtract(payFee).setScale(2, RoundingMode.HALF_UP);
            }
            //平台手续费 支付金额 - 收款金额
            platformFee = payAmount.subtract(recAmount).setScale(2, RoundingMode.HALF_UP);
            //将费用更新到流水中
            qrPayFlowDTO.setPayAmount(payAmount);
            qrPayFlowDTO.setRecAmount(recAmount);
            qrPayFlowDTO.setFee(payFee);
            qrPayFlowDTO.setPlatformFee(platformFee);
        }
        return qrPayFlowDTO;
    }

    /**
     * 获取支付结果
     * @param result
     * @param state
     * @param qrPayFlowDTO
     * @param merchantDTO
     * @param message
     * @param request
     * @return
     */
    private JSONObject getResult(JSONObject result, Integer state, QrPayFlowDTO qrPayFlowDTO, MerchantDTO merchantDTO, String message, HttpServletRequest request) throws Exception {
        result.put("state", state);
        result.put("msg", message);
        result.put("orderNo", qrPayFlowDTO.getId());
        result.put("orderTotal", qrPayFlowDTO.getTransAmount());
        result.put("amountPaid", qrPayFlowDTO.getPayAmount());
        result.put("serviceFee", qrPayFlowDTO.getFee());
        result.put("merchantName", merchantDTO.getPracticalName());
        result.put("orderTime", new SimpleDateFormat("HH:mm dd/MM/yyyy").format(new Date(qrPayFlowDTO.getCreatedDate())));
        String headerLang = request.getHeader("lang");
        if (qrPayFlowDTO.getGatewayId() == StaticDataEnum.GATEWAY_TYPE_1.getCode()) {
            if ("zh-CN".equals(headerLang)) {
                result.put("paymentChannel", ALI_ZH);
            } else {
                result.put("paymentChannel", ALI_EN);
            }
        } else {
            if ("zh-CN".equals(headerLang)) {
                result.put("paymentChannel", WECHAT_ZH);
            } else {
                result.put("paymentChannel", WECHAT_EN);
            }
        }
        return result;
    }


    /**
     * 支付检查订单状态
     * @author zhangzeyuan
     * @date 2021/4/1 17:53
     * @param orderStatus
     * @return boolean
     */
    private boolean checkPosOrderStatus(Integer orderStatus){
        if(Objects.nonNull(orderStatus)){
            if(orderStatus.equals(StaticDataEnum.POS_ORDER_STATUS_INPROCESS.getCode())
                    || orderStatus.equals(StaticDataEnum.POS_ORDER_STATUS_SUCCESS.getCode())
                    || orderStatus.equals(StaticDataEnum.POS_ORDER_STATUS_FAIL.getCode())
                    || orderStatus.equals(StaticDataEnum.POS_ORDER_STATUS_SUSPICIOUS.getCode())){
                //如果订单状态为 支付成功、支付失败、处理中、交易可疑 则返回失败
                return false;
            }
        }
        return true;
    }

    /**
     * 更新POS订单状态
     * @author zhangzeyuan
     * @date 2021/3/31 14:15
     * @param transNo
     * @param orderStatus
     * @param request
     */
    private void updatePosOrderStatus(String transNo, Integer orderStatus, HttpServletRequest request) throws BizException {
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
        map.put("sysTransNo", transNo);
        PosQrPayFlowDTO posQrPayFlow = posQrPayFlowService.findOnePosQrPayFlow(map);
        if(Objects.isNull(posQrPayFlow) || Objects.isNull(posQrPayFlow.getId())){
            return;
        }
        log.info("=======更新POS订单状态  transNo:" + transNo + "| orderStatus:" + orderStatus);
        PosQrPayFlowDTO updateRecord = new PosQrPayFlowDTO();
        if(orderStatus.equals(StaticDataEnum.TRANS_STATE_31.getCode()) || orderStatus.equals(StaticDataEnum.TRANS_STATE_1.getCode())){
            //交易成功
            updateRecord.setOrderStatus(StaticDataEnum.POS_ORDER_STATUS_SUCCESS.getCode());
            updateRecord.setPayDate(System.currentTimeMillis());
            posQrPayFlowService.updatePosQrPayFlow(posQrPayFlow.getId(), updateRecord, request);
        }else if(orderStatus.equals(StaticDataEnum.TRANS_STATE_2.getCode()) || orderStatus.equals(StaticDataEnum.TRANS_STATE_22.getCode())){
            //交易失败
            updateRecord.setOrderStatus(StaticDataEnum.POS_ORDER_STATUS_FAIL.getCode());
            posQrPayFlowService.updatePosQrPayFlow(posQrPayFlow.getId(), updateRecord, request);
        }else{
            //交易可疑
            updateRecord.setOrderStatus(StaticDataEnum.POS_ORDER_STATUS_SUSPICIOUS.getCode());
            posQrPayFlowService.updatePosQrPayFlow(posQrPayFlow.getId(), updateRecord, request);
        }

    }

    /**
     *  发送邮件 交易发票信息
     * @author zhangzeyuan
     * @date 2021/5/13 13:21
     * @param borrowId
     * @param request
     */
    public void sendTransactionMail(String borrowId, HttpServletRequest request) throws Exception{
        //调用分期付查询订单信息
        JSONObject queryParam = new JSONObject(1);
        queryParam.put("borrowId", borrowId);
        String url =  creditMerchantUrl +  "/payremote/getTransactionRecordEmailData";
        String responseResult = HttpClientUtils.sendPost(url, queryParam.toJSONString());
        if(StringUtils.isBlank(responseResult)){
            log.error("调用分期付查询交易记录失败, CreditOrderNo:" + borrowId);
            return;
        }
        JSONObject responseJsonObj = JSONObject.parseObject(responseResult);
        if(!ErrorCodeEnum.SUCCESS_CODE.getCode().equals(responseJsonObj.getString("code"))){
            log.error("调用分期付查询交易记录失败, CreditOrderNo:" + borrowId + "||response data:"+ responseJsonObj.toString());
            return;
        }
        //成功
        JSONObject borrowJsonData = responseJsonObj.getJSONObject("data");
        if(null == borrowJsonData){
            log.error("调用分期付查询交易记录失败, CreditOrderNo:" + borrowId + "||response data:"+ responseJsonObj.toString());
            return;
        }

        //转换为dto
        TransactionRecordEmailDataDTO borrowMailDto = JSONObject.parseObject(borrowJsonData.toJSONString(), TransactionRecordEmailDataDTO.class);
        if(Objects.isNull(borrowMailDto) || Objects.isNull(borrowMailDto.getId()) || StringUtils.isBlank(borrowMailDto.getEmail())){
            log.error("调用分期付查询交易记录失败, CreditOrderNo:" + borrowId + "||response data:"+ responseJsonObj.toString());
            return;
        }

        //商户名
        String merchantName = borrowMailDto.getMerchantName();
        //用户邮箱
        String email = borrowMailDto.getEmail();

        //处理商户地址
        String fullAddress = "";
        String address = borrowMailDto.getAddress();
        String city = borrowMailDto.getCity();
        String merchantState = borrowMailDto.getMerchantState();
        if(StringUtils.isNotBlank(address)){
            fullAddress = address;
        }

        if(StringUtils.isNotBlank(city)){

            HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
            paramMap.put("code", "city");
            paramMap.put("value", city);
            StaticDataDTO staticData = staticDataService.findOneStaticData(paramMap);

            if(Objects.nonNull(staticData) && StringUtils.isNotBlank(staticData.getEnName())){
                fullAddress = fullAddress +  " " + staticData.getEnName();
            }
        }

        if(StringUtils.isNotBlank(merchantState)){

            HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
            paramMap.put("code", "merchantState");
            paramMap.put("value", merchantState);
            StaticDataDTO staticData = staticDataService.findOneStaticData(paramMap);

            if(Objects.nonNull(staticData) && StringUtils.isNotBlank(staticData.getEnName())){
                fullAddress = fullAddress +  " " + staticData.getEnName();
            }
        }

        //用户名
        String userName = borrowMailDto.getUserName();
        //日期
        //毫秒时间戳转换为日 月 年
        //        13:38:23 13/05/2021

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        Date parse = simpleDateFormat.parse(borrowMailDto.getCreatedDate());
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
        String titleDate = simpleDateFormat1.format(parse);

        //总金额
        BigDecimal borrowAmount = borrowMailDto.getBorrowAmount();
        //剩余金额
        BigDecimal leftAmount = BigDecimal.ZERO;

        //还款计划
        JSONArray repayJsonArray = borrowJsonData.getJSONArray("repayList");

        log.info("repayJsonArray", repayJsonArray.toJSONString());

        List<TransactionRecordEmailDataDTO> repayList = JSONArray.parseArray(repayJsonArray.toJSONString(), TransactionRecordEmailDataDTO.class);
        String instalmentsHtml = " ";
        if(CollectionUtils.isNotEmpty(repayList)){
            for(int i = 0; i < repayList.size(); i++ ){
                TransactionRecordEmailDataDTO repay = repayList.get(i);
                if(i == 0){
                    //首期
                    instalmentsHtml +=  "$" + repay.getPaidAmount().toString() + "(25% already paid)" + "<br>";
                }else {
                    instalmentsHtml +=  "$" + repay.getShouldPayAmount().toString() + " due " + repay.getExpectRepayTime() + "<br>";
                }
            }

            leftAmount = borrowAmount.subtract(repayList.get(0).getPaidAmount());
        }
        //获取邮件模板
        MailTemplateDTO mailTemplate = mailTemplateService.findMailTemplateBySendNode(String.valueOf(StaticDataEnum.SEND_NODE_31.getCode()));
        if(Objects.isNull(mailTemplate) || StringUtils.isBlank(mailTemplate.getEnSendContent())){
            log.error("发送交易记录邮件 查询邮件模板失败");
            return;
        }

        String enSendContent = mailTemplate.getEnSendContent();
        String enMailTheme = mailTemplate.getEnMailTheme();

        //设置模板参数
        String[] titleParam = {merchantName, titleDate};
        //邮件内容
        String sendContent = null;
        String sendTitle = null;

//        sendContent = templateContentReplace(contentParam, enSendContent);
        sendContent = enSendContent.replace("{merchantName}", merchantName).replace("{location}", fullAddress).replace("{userName}", userName).replace("borrowAmount", "$" + borrowAmount.toString())
                .replace("{repayList}", instalmentsHtml).replace("{leftAmount}", leftAmount.toString());

        sendTitle = templateContentReplace(titleParam, enMailTheme);


        //发送邮件
        Session session = MailUtil.getSession(sysEmail);
        MimeMessage mimeMessage = MailUtil.getMimeMessage(StaticDataEnum.U_WALLET.getMessage(), sysEmail, email, sendTitle, sendContent, null, session);
        MailUtil.sendMail(session, mimeMessage, sysEmail, sysEmailPwd);

        //记录邮件流水
        MailLogDTO mailLogDTO = new MailLogDTO();
        mailLogDTO.setAddress(email);
        mailLogDTO.setContent(sendContent);
        mailLogDTO.setSendType(0);
        mailLogService.saveMailLog(mailLogDTO, request);
    }



    /**
     * 模板参数替换
     * @author zhangzeyuan
     * @date 2021/5/13 13:19
     * @param replaceContentParams
     * @param content
     * @return java.lang.String
     */
    private String templateContentReplace(Object[] replaceContentParams, String content) {

        if (replaceContentParams != null && replaceContentParams.length > 0) {
            content = MessageFormat.format(content, replaceContentParams);
        }
        return content;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JSONObject doPayByCard(JSONObject param, HttpServletRequest request) throws Exception {
        // 入参 userInfo cardInfo payAmount
        JSONObject userInfo = param.getJSONObject("userInfo");
        JSONObject cardInfo = param.getJSONObject("cardInfo");
        BigDecimal amount = param.getBigDecimal("amount");
        // 校验参数
        if (userInfo==null||cardInfo==null||amount==null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        Long userId = userInfo.getLong("userId");
        // 校验用户商户是否不可用
        UserDTO userById = userService.findUserById(userId);//412082561924812800
        if (userById==null){
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }
        // 查询通道 getwaytype=0 state=1
        Map<String,Object> gateWayMap = new HashMap<>();
        gateWayMap.put("gatewayType",StaticDataEnum.GATEWAY_TYPE_0.getCode());
        gateWayMap.put("state",StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(gateWayMap);
        if (gatewayDTO.getId()==null){
            throw new BizException(I18nUtils.get("gateway.disabled", getLang(request)));
        }
        // 查询通道费率 latPay需要该方式计算费率
        Map<String,Object> params = new HashMap<>(8);
        params.put("gatewayId",gatewayDTO.getType());
//        Integer cardCategory = cardInfo.getInteger("cardCategory");
        Integer cardCcType = cardInfo.getInteger("customerCcType");
        if (cardCcType==null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        params.put("code",cardCcType+50);
        ChannelFeeConfigDTO channelFeeConfigDTO = channelFeeConfigService.findOneChannelFeeConfig(params);
        if(channelFeeConfigDTO == null || channelFeeConfigDTO.getId() == null ){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        gatewayDTO.setRate(channelFeeConfigDTO.getRate());
        gatewayDTO.setRateType(channelFeeConfigDTO.getType());
        // 不需要进行前置处理，生成withHoldFlow 状态默认值为0 处理中 异常回滚
        WithholdFlowDTO withholdFlowDTO = this.getWithholdFlowDTO(param,request,userInfo,cardInfo,amount,gatewayDTO);

//        // 通道限额组件引用
//        rechargeFlowService.channelLimit(gatewayDTO.getType(),withholdFlowDTO.getTransAmount(),request);
        // 组装数据发往三方扣款
        withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        if(withholdFlowDTO.getTransAmount().compareTo(BigDecimal.ZERO)>0){
            //渠道交易请求
            try{

                withholdFlowDTO = latPayService.latPayRequest(withholdFlowDTO,withholdFlowDTO.getTransAmount(),withholdFlowDTO.getFee(),cardInfo,request,getIp(request));

            }catch (Exception e){
                //请求异常，记录为可疑
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
            }
        }else{
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            withholdFlowDTO.setCheckState( StaticDataEnum.IS_CHECK_1.getCode());
            withholdFlowDTO.setOrdreNo(null);
        }
        // 组装返回数据进行返回
        JSONObject returnData = new JSONObject();
        returnData.put("flowId", param.getLong("flowId"));
        returnData.put("orderNo", withholdFlowDTO.getOrdreNo());
        returnData.put("charge",withholdFlowDTO.getCharge());
        returnData.put("feeType",channelFeeConfigDTO.getType());
        returnData.put("feeRate",channelFeeConfigDTO.getRate());
        Integer state = withholdFlowDTO.getState();
        if (state==StaticDataEnum.TRANS_STATE_3.getCode()){
            returnData.put("code", ErrorCodeEnum.LAT_PAY_DOUBLE.getCode());
        }else if (state==StaticDataEnum.TRANS_STATE_2.getCode()){
            returnData.put("code", ErrorCodeEnum.FAIL_CODE.getCode());
        }else if (state==StaticDataEnum.TRANS_STATE_1.getCode()){
            returnData.put("code", ErrorCodeEnum.SUCCESS_CODE.getCode());
        }else {
            // todo 是否设置为可疑
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            returnData.put("code", ErrorCodeEnum.FAIL_CODE.getCode());
        }
        withholdFlowService.updateWithholdFlowForConcurrency(withholdFlowDTO.getId(), withholdFlowDTO, request);
        returnData.put("displayData",this.packCreditRepayResData(withholdFlowDTO.getId(),request));
        return returnData;
    }




    @Override
    public JSONObject doPayByCardV2(JSONObject param, HttpServletRequest request) throws Exception {
        // 入参 userInfo cardInfo payAmount
        JSONObject userInfo = param.getJSONObject("userInfo");
        JSONObject cardInfo = param.getJSONObject("cardInfo");
        BigDecimal amount = param.getBigDecimal("amount");
        // 校验参数
        if (userInfo==null||cardInfo==null||amount==null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        Long userId = userInfo.getLong("userId");
        // 校验用户商户是否不可用
        UserDTO userById = userService.findUserById(userId);//412082561924812800
        if (userById==null){
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }
        // 查询通道 getwaytype=0 state=1
        // 根据token类型，确定通道
        int type = 0;
        if(StringUtils.isNotEmpty(cardInfo.getString("stripeToken"))){
            type = StaticDataEnum.GATEWAY_TYPE_8.getCode();
        }else if(StringUtils.isNotEmpty(cardInfo.getString("crdStrgToken"))){
            type = StaticDataEnum.GATEWAY_TYPE_0.getCode();
        }else{
            throw new BizException(I18nUtils.get("gateway.disabled", getLang(request)));
        }

        Map<String,Object> gateWayMap = new HashMap<>();
        gateWayMap.put("gatewayType",StaticDataEnum.PAY_TYPE_0.getCode());
        gateWayMap.put("type",type);

        GatewayDTO gatewayDTO = gatewayService.findOneGateway(gateWayMap);
        if (gatewayDTO.getId()==null){
            throw new BizException(I18nUtils.get("gateway.disabled", getLang(request)));
        }


        // 查询通道费率 latPay需要该方式计算费率
        Map<String,Object> params = new HashMap<>(8);
        params.put("gatewayId",gatewayDTO.getType());
//        Integer cardCategory = cardInfo.getInteger("cardCategory");
        Integer cardCcType = cardInfo.getInteger("customerCcType");
        if (cardCcType==null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        params.put("code",cardCcType+50);
        ChannelFeeConfigDTO channelFeeConfigDTO = channelFeeConfigService.findOneChannelFeeConfig(params);
        if(channelFeeConfigDTO == null || channelFeeConfigDTO.getId() == null ){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        gatewayDTO.setRate(channelFeeConfigDTO.getRate());
        gatewayDTO.setRateType(channelFeeConfigDTO.getType());
        // 不需要进行前置处理，生成withHoldFlow 状态默认值为0 处理中 异常回滚
        WithholdFlowDTO withholdFlowDTO = this.getWithholdFlowDTO(param,request,userInfo,cardInfo,amount,gatewayDTO);

        // 组装数据发往三方扣款
        withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        if(withholdFlowDTO.getTransAmount().compareTo(BigDecimal.ZERO)>0){
            if( type == StaticDataEnum.GATEWAY_TYPE_0.getCode()){
                // Latpay渠道交易请求
                try{
                    withholdFlowDTO = latPayService.latPayRequest(withholdFlowDTO,withholdFlowDTO.getTransAmount(),withholdFlowDTO.getFee(),cardInfo,request,getIp(request));
                }catch (Exception e){
                    //请求异常，记录为可疑
                    withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                    throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
                }
            }else if ( type == StaticDataEnum.GATEWAY_TYPE_8.getCode()){
                // stripe渠道交易请求
                StripeChargeDTO stripeChargeDTO = new StripeChargeDTO();
                stripeChargeDTO.setAmount(withholdFlowDTO.getTransAmount().multiply(new BigDecimal("100")).setScale(0,BigDecimal.ROUND_DOWN));
//                stripeChargeDTO.setCustomer("638621894721458176");
                stripeChargeDTO.setCustomer(userId+"");
                stripeChargeDTO.setCurrency("aud");
                stripeChargeDTO.setSource(cardInfo.getString("stripeToken"));

//                stripeChargeDTO.setDescription(StaticDataEnum.STRIPE_ORDER_DESC_REPAYMENT.getMessage());
                HashMap<String, Object> metaMap = Maps.newHashMapWithExpectedSize(1);
                metaMap.put("id", withholdFlowDTO.getOrdreNo());
                stripeChargeDTO.setMetadata(metaMap);
                StripeAPIResponse stripeAPIResponse = stripeService.createCharge(stripeChargeDTO);

                log.info("stripeAPIResponse :"+stripeAPIResponse);
                if(stripeAPIResponse.isSuccess()){
                    // 请求成功
                    Charge charge = (Charge)stripeAPIResponse.getData();
                    withholdFlowDTO.setStripeId(charge.getId());
                    if(charge == null || StringUtils.isEmpty(charge.getStatus())){
                        withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                    }
                    if(StripeAPICodeEnum.CHARGE_RES_STATUS_SUCCEEDED.getMessage().equals(charge.getStatus())){
                        // 成功
                        withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                    }else if(StripeAPICodeEnum.CHARGE_RES_STATUS_FAILED.getMessage().equals(charge.getStatus())||
                            StripeAPICodeEnum.CHARGE_RES_STATUS_CANCELED.getMessage().equals(charge.getStatus())){
                        // 失败
                        withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    }else{
                        // 处理中
                        withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                    }

                }else{
                    withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    withholdFlowDTO.setErrorMessage(stripeAPIResponse.getMessage());
                }
            }
        }else{
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            withholdFlowDTO.setCheckState( StaticDataEnum.IS_CHECK_1.getCode());
            withholdFlowDTO.setOrdreNo(null);
        }
        // 组装返回数据进行返回
        JSONObject returnData = new JSONObject();
        returnData.put("flowId", param.getLong("flowId"));
        returnData.put("orderNo", withholdFlowDTO.getOrdreNo());
        returnData.put("charge",withholdFlowDTO.getCharge());
        returnData.put("feeType",channelFeeConfigDTO.getType());
        returnData.put("feeRate",channelFeeConfigDTO.getRate());
        Integer state = withholdFlowDTO.getState();
        if (state==StaticDataEnum.TRANS_STATE_3.getCode()){
            returnData.put("code", ErrorCodeEnum.LAT_PAY_DOUBLE.getCode());
        }else if (state==StaticDataEnum.TRANS_STATE_2.getCode()){
            returnData.put("code", ErrorCodeEnum.FAIL_CODE.getCode());
        }else if (state==StaticDataEnum.TRANS_STATE_1.getCode()){
            returnData.put("code", ErrorCodeEnum.SUCCESS_CODE.getCode());
        }else {
            // 其他情况设置为可疑
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            returnData.put("code", ErrorCodeEnum.LAT_PAY_DOUBLE.getCode());
        }
        withholdFlowService.updateWithholdFlowForConcurrency(withholdFlowDTO.getId(), withholdFlowDTO, request);
        returnData.put("displayData",this.packCreditRepayResData(withholdFlowDTO.getId(),request));
        return returnData;
    }



    private WithholdFlowDTO getWithholdFlowDTO(JSONObject requestInfo, HttpServletRequest request, JSONObject userInfo, JSONObject cardInfo, BigDecimal amonut, GatewayDTO gatewayDTO) {
        //计算通道手续费
        BigDecimal charge = gatewayService.getGateWayFee(gatewayDTO,amonut);
        WithholdFlowDTO withholdFlowDTO = new WithholdFlowDTO();
        withholdFlowDTO.setFeeRate(gatewayDTO.getRate());
        withholdFlowDTO.setFeeType(gatewayDTO.getRateType());
        withholdFlowDTO.setUserId(userInfo.getLong("userId"));
        withholdFlowDTO.setGatewayMerchantId(gatewayDTO.getChannelMerchantId());
        withholdFlowDTO.setGatewayMerchantPassword(gatewayDTO.getPassword());
        withholdFlowDTO.setFlowId(requestInfo.getLong("flowId"));
        withholdFlowDTO.setGatewayId(gatewayDTO.getType());
        withholdFlowDTO.setOrderAmount(amonut);
        withholdFlowDTO.setFee(BigDecimal.ZERO);
        withholdFlowDTO.setBillAddress2(cardInfo.getString("Address2"));
        withholdFlowDTO.setTransAmount(amonut.add(charge));
        withholdFlowDTO.setBillState(cardInfo.getString("state"));
        withholdFlowDTO.setBillMiddlename(cardInfo.getString("middleName"));
        if(StringUtils.isNotEmpty(cardInfo.getString("stripe_token"))){
            withholdFlowDTO.setCrdstrgToken(cardInfo.getString("stripe_token"));
        }else{
            withholdFlowDTO.setCrdstrgToken(cardInfo.getString("crdStrgToken"));
        }
        withholdFlowDTO.setCustomerMiddlename(cardInfo.getString("middleName"));
        withholdFlowDTO.setOrdreNo(SnowflakeUtil.generateId().toString());
        withholdFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_32.getCode());
        withholdFlowDTO.setCustomerEmail(userInfo.getString("email"));
        withholdFlowDTO.setCustomerPhone(userInfo.getString("phone"));
        withholdFlowDTO.setCustomerFirstname(userInfo.getString("firstName"));
        withholdFlowDTO.setCustomerLastname(userInfo.getString("lastName"));
        withholdFlowDTO.setCustomerIpaddress(getIp(request));
        withholdFlowDTO.setBillFirstname(cardInfo.getString("firstName"));
        withholdFlowDTO.setBillLastname(cardInfo.getString("lastName"));
        withholdFlowDTO.setBillAddress1(cardInfo.getString("address1"));
        withholdFlowDTO.setBillCity(cardInfo.getString("city"));
        withholdFlowDTO.setBillZip(cardInfo.getString("zip"));
        withholdFlowDTO.setCurrency(StaticDataEnum.CURRENCY_TYPE.getMessage());
        withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        withholdFlowDTO.setCharge(charge);
        withholdFlowDTO.setCardNo(cardInfo.getString("cardNo"));
        withholdFlowDTO.setCardCcType(cardInfo.getInteger("customerCcType").toString());
        //查询国家ISO代码
        String country = cardInfo.getString("country");
        if (country != null) {
            Map<String, Object> params = new HashMap<>(1);
            params.put("code", "county");
            params.put("value", country);
            StaticDataDTO staticDataDTO = staticDataService.findOneStaticData(params);
            CountryIsoDTO countryIsoDTO = tieOnCardFlowService.selectCountryIso(staticDataDTO.getEnName());
            withholdFlowDTO.setBillCountry(countryIsoDTO.getTwoLettersCoding());
        }

        return withholdFlowDTO;
    }
    private JSONObject packCreditRepayResData(Long withholdFlowId, HttpServletRequest request) {
        WithholdFlowDTO withholdFlowDTO = withholdFlowService.findWithholdFlowById(withholdFlowId);
        JSONObject displayData = new JSONObject();
        BigDecimal transAmount = withholdFlowDTO.getTransAmount();
        //还款总金额 还款金额+手续费
        displayData.put("totalAmount", transAmount);
        displayData.put("orderAmount",withholdFlowDTO.getOrderAmount());
        BigDecimal charge = withholdFlowDTO.getCharge();
        displayData.put("tansFee", charge);
        displayData.put("repayAmount",transAmount.subtract(charge));
//        SimpleDateFormat format = new SimpleDateFormat("dd MMM at hh:mm a", Locale.US);
        Long createdDate = withholdFlowDTO.getCreatedDate();
//        displayData.put("dateStr",null != createdDate  ? format.format(createdDate): "");
        SimpleDateFormat monthFormat = new SimpleDateFormat("dd MMM", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        String monthStr = monthFormat.format(createdDate);
        String timeStr = timeFormat.format(createdDate);
        displayData.put("dateStr", monthStr + " at " + timeStr);

        return displayData;
    }

    @Override
    public void doPayByCardHandle() throws Exception {
        log.info("分期付调用支付进行卡支付查证跑批");
        // 查询可疑订单 TRANS_STATE_3 ACC_FLOW_TRANS_TYPE_32 letpay
        Map<String, Object> params = new HashMap<>(5);
//        params.put("gatewayId", StaticDataEnum.GATEWAY_TYPE_0.getCode());
        params.put("state", StaticDataEnum.TRANS_STATE_3.getCode());
        params.put("transType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_32.getCode());
        List<WithholdFlowDTO> withholdFlowDTOList = withholdFlowService.find(params, null, null);
        if (CollectionUtils.isEmpty(withholdFlowDTOList)){
            return;
        }

        String thirdId = null;
        for (WithholdFlowDTO withholdFlowDTO : withholdFlowDTOList) {
            try {
                if(Integer.parseInt(withholdFlowDTO.getGatewayId()+"") == StaticDataEnum.GATEWAY_TYPE_0.getCode()){
                    // 通道是latpay的发往latpay做查证
                    withholdFlowDTO = latPayService.latPayDoubtHandle(withholdFlowDTO);
                    thirdId =  withholdFlowDTO.getLpsTransactionId();
                }else if (Integer.parseInt(withholdFlowDTO.getGatewayId()+"") == StaticDataEnum.GATEWAY_TYPE_8.getCode()){
                    // 通道是stripe的发往stripe做查证

                    if(withholdFlowDTO.getStripeId() == null){
                        withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    }else{
                        log.info("stripe charge check ,id:"+withholdFlowDTO.getStripeId());
                        StripeAPIResponse response = stripeService.retrieveCharge(withholdFlowDTO.getStripeId());
                        log.info("stripe charge check ,response:"+response);
                        if(response.isSuccess()){
                            Charge charge = (Charge)response.getData();
                            if(charge != null && charge.getStatus() != null){
                                if(StripeAPICodeEnum.CHARGE_RES_STATUS_SUCCEEDED.getCode().equals(charge.getStatus())){
                                    // 成功
                                    withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                                }else if(StripeAPICodeEnum.CHARGE_RES_STATUS_FAILED.getCode().equals(charge.getStatus())){
                                    withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                                }
                            }
                            thirdId =  withholdFlowDTO.getStripeId();
                        }else{
                            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                        }
                    }


                }

                if(withholdFlowDTO.getState() != StaticDataEnum.TRANS_STATE_3.getCode()){
                    withholdFlowService.updateWithholdFlow(withholdFlowDTO.getId(),withholdFlowDTO,null);
                    Map<String ,Object > map = new HashMap<>(6);
                    map.put("flowId",withholdFlowDTO.getFlowId());
                    map.put("latPayTransactionId",thirdId);
                    map.put("orderNo", withholdFlowDTO.getOrdreNo());
                    map.put("code",withholdFlowDTO.getState());
                    map.put("feeType",withholdFlowDTO.getFeeType());
                    map.put("feeRate",withholdFlowDTO.getFeeRate());
                    // 发往分期付
                    this.transTypeToCredit(map);
                }
            }catch (Exception e){
                log.error("分期付调用支付进行卡支付查证跑批异常 id:{}",withholdFlowDTO.getId());
            }
        }

    }
    /**
     * 通知分期付
     * @param map
     */
    private void transTypeToCredit(Map<String, Object> map) {
        try{
            JSONObject params =new JSONObject(map);
            serverService.transTypeToCredit(params);
        }catch (Exception e){
            log.error(" transTypeToCredit  Exception  flowId:{}, error message:{}, e:{} ",map.get("flowId"),e.getMessage(),e);
        }
    }



    @Override
    public void creditFirstCardPayDoubtHandle() throws Exception {
        //查询三方代扣可疑订单
        List<QrPayFlowDTO> qrPayFlowList = qrPayFlowService.getSuspiciousOrderFlowList();
        for(QrPayFlowDTO qrPayFlowDTO: qrPayFlowList){
            this.dealOneCreditFirstCardPayDoubt(qrPayFlowDTO);
        }
    }

    @Override
    public int dealOneCreditFirstCardPayDoubt(QrPayFlowDTO qrPayFlowDTO) {

        HashMap<String, Object> queryParamMap = Maps.newHashMapWithExpectedSize(1);
        JSONObject cardJsonObj = new JSONObject();
        log.info("开始处理可疑分期付订单",qrPayFlowDTO.getId());
        queryParamMap.put("flowId", qrPayFlowDTO.getId());
        //三方代扣流水
        try{
            WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(queryParamMap);
            //分期付
            if(null == withholdFlowDTO || null == withholdFlowDTO.getId()){
                //置为交易失败 ,无三方单号，不删卡
                handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, withholdFlowDTO.getOrderAmount(), null, null);
                return StaticDataEnum.TRANS_STATE_2.getCode();
            }

            if(Integer.parseInt(withholdFlowDTO.getGatewayId() + "") == StaticDataEnum.GATEWAY_TYPE_0.getCode()){
                //查询 latpay交易状态
                withholdFlowDTO = latPayService.latPayDoubtHandle(withholdFlowDTO);
//                log.info("withholdFlowDTO  state状态为",withholdFlowDTO.getState());
            }else if(Integer.parseInt(withholdFlowDTO.getGatewayId() + "") == StaticDataEnum.GATEWAY_TYPE_8.getCode()){

                int state = this.retrievePaymentIntent(withholdFlowDTO.getStripeId(), withholdFlowDTO);
                withholdFlowDTO.setState( state);
            }


            //根据三方返回状态进行处理
            cardJsonObj.put("id", qrPayFlowDTO.getCardId());

            //分期付25%第一期金额 不包含手续费
            BigDecimal cardPayRealAmount =  withholdFlowDTO.getOrderAmount().subtract(qrPayFlowDTO.getTipAmount()).subtract(qrPayFlowDTO.getDonationAmount());
            //分期付剩余分期金额
            BigDecimal installAmount = qrPayFlowDTO.getPayAmount().subtract(cardPayRealAmount);

            try{
                handleCreditCardPayDataByThirdStatusV3(withholdFlowDTO, qrPayFlowDTO, cardJsonObj, withholdFlowDTO.getTransAmount(), cardPayRealAmount,
                        installAmount, withholdFlowDTO.getFeeRate(), withholdFlowDTO.getFee(), null);
            }catch (BizException be){
                log.info("交易结果信息："+withholdFlowDTO.getState());
                return  withholdFlowDTO.getState();
            }catch (Exception e){
                log.info("可疑分期付订单,处理异常信息："+e.getMessage());
                return  withholdFlowDTO.getState();
            }

            // 交易成功推荐人处理
            UserDTO payUser = userService.findUserById(qrPayFlowDTO.getPayUserId());
            userService.firstPaidSuccessAmountInV2(payUser.getId(), payUser.getInviterId(), StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode(), qrPayFlowDTO.getId(), null);
            return  withholdFlowDTO.getState();
        }catch (Exception e){
            log.error("交易成功推荐人处理出错,e:{}" , e);
            return StaticDataEnum.TRANS_STATE_3.getCode();
        }

    }

    /**
     * 分期付冻结额度回滚可疑处理
     *
     * @author zhangzeyuan
     * @date 2021/7/7 14:47
     */
    @Override
    public void creditRollbackAmountDoubtHandle() throws Exception {
        //查询可疑的冻结额度回滚订单
        HashMap<String, Object> queryMap = Maps.newHashMapWithExpectedSize(2);
        queryMap.put("operateType", StaticDataEnum.PAY_BALANCE_FLOW_OPERATE_TYPE_2.getCode());
        queryMap.put("state", StaticDataEnum.PAY_BALANCE_FLOW_STATE_3.getCode());
        List<PayCreditBalanceFlowDTO> list = payCreditBalanceFlowService.find(queryMap, null, null);
        if(CollectionUtils.isEmpty(list)){
            return;
        }

        JSONObject postParamJson = new JSONObject();
        JSONObject dataJson = new JSONObject();
        String url = creditMerchantUrl + "/payremote/user/userAmountRollback";

        for (PayCreditBalanceFlowDTO flow: list){
            //调用分期付接口 重新回滚
            postParamJson.clear();
            dataJson.clear();

            //组装数据
            dataJson.put("userId", flow.getUserId());
            dataJson.put("flowId", flow.getQrPayFlowId());
            postParamJson.put("data", dataJson);
            try {
                String resultData = HttpClientUtils.post(url, postParamJson.toJSONString());
                JSONObject resultDataJson = JSONObject.parseObject(resultData);
                String code = resultDataJson.getString("code");
                if(code != null && code.equals(ResultCode.OK.getCode())){
                    //记录流水状态成功
                    flow.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_1.getCode());
                    payCreditBalanceFlowService.updatePayCreditBalanceFlow(flow.getId(),  flow,  null);
                }
            }catch (Exception e){
                log.error("跑批回滚额度异常" + flow.getQrPayFlowId(),e.getMessage());
            }
        }
    }

    /**
     * 分期付生成可疑订单处理
     *
     * @author zhangzeyuan
     * @date 2021/7/7 14:47
     */
    @Override
    public void creditCreateOrderDoubtHandle() throws Exception {
        HashMap<String, Object> queryMap = Maps.newHashMapWithExpectedSize(1);
        queryMap.put("state", StaticDataEnum.CREATE_CREDIT_ORDER_FLOW_STATE_3.getCode());
        List<CreateCreditOrderFlowDTO> list = createCreditOrderFlowService.find(queryMap, null, null);
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        JSONObject cardObj = new JSONObject();

        for(CreateCreditOrderFlowDTO flow: list){
            QrPayFlowDTO qrPayFlow = qrPayFlowService.findQrPayFlowById(flow.getQrPayFlowId());

            if(null == qrPayFlow || null == qrPayFlow.getId()){
                continue;
            }

            queryMap.clear();
            queryMap.put("flowId", qrPayFlow.getId());
            WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(queryMap);
            if(null == withholdFlowDTO || null == withholdFlowDTO.getId()){
                continue;
            }

            cardObj.clear();
            cardObj.put("accountName" , flow.getCardAccountName());
            cardObj.put("cardNo" , flow.getCardNo());
            //生成分期付订单
            if(qrPayFlow.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                Integer orderState = createCreditInstallmentOrder(qrPayFlow, flow.getCardPayRate(), flow.getCardPayAmount(), flow.getCardFeeAmount(), flow.getCardPayAmount().add(flow.getCardFeeAmount()), cardObj, null);

                //新增记录表
                if(orderState.equals(StaticDataEnum.API_ORDER_STATE_1.getCode())){

                    flow.setState(StaticDataEnum.CREATE_CREDIT_ORDER_FLOW_STATE_1.getCode());
                    createCreditOrderFlowService.updateCreateCreditOrderFlow(flow.getId(), flow, null);
                    //成功
                    handleLatPaySuccess(withholdFlowDTO, qrPayFlow, null);

                    //更改该卡为默认卡
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("cardId", qrPayFlow.getCardId());
                    try{
                        userService.presetCard(jsonObject, qrPayFlow.getPayUserId(),null);
                    }catch (Exception e){
                        log.error("分期付订单创建可疑跑批 分期付第一次卡支付设置默认卡失败,e:{}",e);
                    }

                    //POS API 交易成功回调通知
                    /*try {
                        log.info("开始进行订单支付成功通知");
                        posApiService.posPaySuccessNotice(qrPayFlow.getMerchantId(), qrPayFlow.getTransNo(), null);
                    }catch (PosApiException e){
                        log.error("POS通知失败" + e.getMessage());
                    }*/

                    //分期付发送交易记录 邮件
                    try{
                        sendTransactionMail(qrPayFlow.getCreditOrderNo(), null);
                    }catch (Exception e){
                        log.error("分期付发送交易记录邮件失败, CreditOrderNo:" + qrPayFlow.getCreditOrderNo() + " || message:" + e.getMessage());
                    }
                }
            }
        }
    }


}
