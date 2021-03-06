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

    private static final String ALI_ZH = "?????????";

    private static final String ALI_EN = "Alipay";

    private static final String WECHAT_ZH = "??????";

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
     * ????????????????????????
     *
     * @param qrPayDTO
     * @param request
     * @throws BizException
     */
    @Override
    public void qrPayReqCheck(QrPayDTO qrPayDTO, HttpServletRequest request) throws BizException {

        //?????????????????????
        if (StringUtils.isEmpty(qrPayDTO.getTransAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.empty", getLang(request)));
        }


        //????????????????????????
        if (!RegexUtils.isTransAmt(qrPayDTO.getTransAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

//        if (qrPayDTO.getFeeAmt()!=null && (StringUtils.isNotEmpty(qrPayDTO.getFeeAmt().toString()))) {
//            //???????????????????????????
//            if (!RegexUtils.isTransAmt(qrPayDTO.getFeeAmt().toString())) {
//                throw new BizException(I18nUtils.get("feeAmount.error", getLang(request)));
//            }
//
//            //???????????????????????????
//            if (StringUtils.isEmpty(qrPayDTO.getFeeDirection().toString())) {
//                throw new BizException(I18nUtils.get("feeDirection.null", getLang(request)));
//            }
//        }

        //??????userId????????????
        if (StringUtils.isEmpty(qrPayDTO.getPayUserId().toString()) || StringUtils.isEmpty(qrPayDTO.getRecUserId().toString())) {
            throw new BizException(I18nUtils.get("userId.null", getLang(request)));
        }

        //??????????????????
        if (StringUtils.isEmpty(qrPayDTO.getPayType().toString())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }


        if (qrPayDTO.getPayType() == StaticDataEnum.PAY_TYPE_0.getCode()) {
            if (StringUtils.isEmpty(qrPayDTO.getCardId().toString())) {
                throw new BizException(I18nUtils.get("card.id.empty", getLang(request)));
            }
        }


        //?????????????????????????????????
        if(qrPayDTO.getRedEnvelopeAmount()!=null && qrPayDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) != 0){
            if (!RegexUtils.isTransAmt(qrPayDTO.getRedEnvelopeAmount().toString())) {
                throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
            }
        }
        //???????????????????????????????????????
        if(qrPayDTO.getWholeSalesAmount()!=null && qrPayDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) != 0){
            if (!RegexUtils.isTransAmt(qrPayDTO.getWholeSalesAmount().toString())) {
                throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
            }
            //??????????????????????????????????????????
            if(qrPayDTO.getWholeSalesAmount().compareTo(qrPayDTO.getTransAmount())>0){
                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
            }
        }

        //???????????????
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
     * ????????????????????????
     *
     * @param qrPayDTO
     * @param request
     * @throws BizException
     */
    @Override
    public void qrPayReqCheckV2(QrPayDTO qrPayDTO, HttpServletRequest request) throws BizException {

        //?????????????????????
        if (StringUtils.isEmpty(qrPayDTO.getTransAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.empty", getLang(request)));
        }

        //?????????????????????
        if (StringUtils.isEmpty(qrPayDTO.getTrulyPayAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.empty", getLang(request)));
        }

        //????????????????????????
        if (!RegexUtils.isTransAmt(qrPayDTO.getTransAmount().toString()) || !RegexUtils.isTransAmt(qrPayDTO.getTrulyPayAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        if (null !=  qrPayDTO.getTipAmount() && !RegexUtils.isTransAmt(qrPayDTO.getTipAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        //??????userId????????????
        if (StringUtils.isEmpty(qrPayDTO.getPayUserId().toString()) ) {
            throw new BizException(I18nUtils.get("userId.null", getLang(request)));
        }

        //??????????????????
        if (StringUtils.isEmpty(qrPayDTO.getPayType().toString())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }


        if (null == qrPayDTO.getCardId() || StringUtils.isEmpty(qrPayDTO.getCardId().toString())) {
            throw new BizException(I18nUtils.get("card.id.empty", getLang(request)));
        }

        //???????????????
        if (StringUtils.isEmpty(qrPayDTO.getTransNo())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //????????????????????????
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
     * ????????????????????????
     *
     * @param qrPayDTO
     * @param request
     * @throws BizException
     */
    public void qrPayReqCheckV3(QrPayDTO qrPayDTO, HttpServletRequest request) throws BizException {

        //?????????????????????
        if (StringUtils.isEmpty(qrPayDTO.getTransAmount().toString()) ) {
            throw new BizException(I18nUtils.get("transAmount.empty", getLang(request)));
        }

        //?????????????????????
        if (StringUtils.isEmpty(qrPayDTO.getTrulyPayAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.empty", getLang(request)));
        }

        //????????????????????????
        if (!RegexUtils.isTransAmt(qrPayDTO.getTransAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        //??????userId????????????
        if (StringUtils.isEmpty(qrPayDTO.getPayUserId().toString()) ) {
            throw new BizException(I18nUtils.get("userId.null", getLang(request)));
        }

        //??????????????????
        if (StringUtils.isEmpty(qrPayDTO.getPayType().toString())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //???????????????
        if (StringUtils.isEmpty(qrPayDTO.getTransNo())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //????????????????????????
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

        // ????????????????????????????????????????????????
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
            // ?????????????????????????????????????????????????????????
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
            // ??????????????????????????????????????????
            if(marketingManagementDTO.getValidityLimitState() == StaticDataEnum.MARKETING_VALIDITY_LIMIT_STATE_1.getCode()){
                Long now = System.currentTimeMillis();
                if(marketingManagementDTO.getValidEndTime() < now || marketingManagementDTO.getValidStartTime() > now ){
                    throw new BizException(I18nUtils.get("marketing.not.exist", getLang(request)));
                }
            }

            // ????????????
            if(marketingManagementDTO.getCityLimitState() != null && marketingManagementDTO.getCityLimitState() > 0){
                MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayDTO.getMerchantId());
                if(merchantDTO == null || merchantDTO.getId() == null){
                    throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
                }

                if( !merchantDTO.getCity().equals(marketingManagementDTO.getCityLimitState()+"") ){
                    throw new BizException(I18nUtils.get("marketing.not.exist", getLang(request)));
                }
            }

            // ????????????
            if(marketingManagementDTO.getRestaurantLimitState() != null && marketingManagementDTO.getRestaurantLimitState() > 0){
                MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayDTO.getMerchantId());
                if(merchantDTO == null || merchantDTO.getId() == null){
                    throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
                }

                if( !(merchantDTO.getId()+"").equals(marketingManagementDTO.getRestaurantLimitState()+"") ){
                    throw new BizException(I18nUtils.get("marketing.not.exist", getLang(request)));
                }
            }

            //??????????????????

            //2022???1???11???16:31:53 ???????????? ??????????????????????????? ?????????????????????????????????????????? ????????????????????? ?????? / 0.4
            String managementId = marketingManagementDTO.getId().toString();
            BigDecimal tempMinTranAmount = BigDecimal.ZERO;
            if(managementId.equals("620846989825331200") || managementId.equals("625123077544005632")){
                //???????????????????????? ??????????????? ??? 0.4
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
     * ????????????
     *
     * @param qrPayDTO
     * @param request
     * @throws BizException
     * @return
     */
    @Override
    public Object doQrPay(QrPayDTO qrPayDTO, HttpServletRequest request) throws Exception {

        //??????????????????user??????
        UserDTO payUser = userService.findUserById(qrPayDTO.getPayUserId());
        UserDTO recUser = userService.findUserById(qrPayDTO.getRecUserId());

        //????????????????????????
        if (payUser == null  ||  payUser.getId() == null ) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }

        if(recUser == null || recUser.getId() == null ){
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        Integer cardState = payUser.getCardState();
        if(qrPayDTO.getPayType() == StaticDataEnum.PAY_TYPE_4.getCode()){
            if(!cardState.equals(StaticDataEnum.USER_CARD_STATE_BINDED)){
                //todo ????????????????????????
            }
        }

        //???????????????POS??????
        boolean posOrder = false;
        //?????????????????????POS??????
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
        map.put("sysTransNo", qrPayDTO.getTransNo());
        PosQrPayFlowDTO posQrPayFlow = posQrPayFlowService.findOnePosQrPayFlow(map);
        if(Objects.nonNull(posQrPayFlow) && Objects.nonNull(posQrPayFlow.getId())){
            posOrder = true;
        }

        //??????POS????????????
        if(posOrder){
            boolean posCheckResult = checkPosOrderStatus(posQrPayFlow.getOrderStatus());
            if(!posCheckResult){
                //?????????????????????
                throw new BizException(I18nUtils.get("pos.qrcode.disabled", getLang(request)));
            }
            //???POS?????????????????????
            posQrPayFlowService.updateOrderStatusBySysTransNo(qrPayDTO.getTransNo(), StaticDataEnum.POS_ORDER_STATUS_INPROCESS.getCode(), getUserId(request), System.currentTimeMillis());
        }
        //???????????????????????????????????????
        HashMap<String, Object> countMap = Maps.newHashMapWithExpectedSize(1);
        countMap.put("transNo" , qrPayDTO.getTransNo());
        int count = qrPayFlowService.count(countMap);
        if(count > 0){
            throw new BizException(I18nUtils.get("pay.order.same", getLang(request)));
        }
        //????????????
        QrPayFlowDTO qrPayFlowDTO = new QrPayFlowDTO();
        qrPayFlowDTO.setFee(qrPayDTO.getFeeAmt());
        qrPayFlowDTO.setPayUserId(qrPayDTO.getPayUserId());
        qrPayFlowDTO.setPayUserType(payUser.getUserType());
        qrPayFlowDTO.setMerchantId(qrPayDTO.getMerchantId());
        qrPayFlowDTO.setRecUserId(qrPayDTO.getRecUserId());
        qrPayFlowDTO.setRecUserType(recUser.getUserType());
        qrPayFlowDTO.setTransAmount(qrPayDTO.getTransAmount());
        qrPayFlowDTO.setRemark(qrPayDTO.getRemark());
        //????????????
        qrPayFlowDTO.setTransNo(qrPayDTO.getTransNo());
        qrPayFlowDTO.setPayUserIp(getIp(request));
        //???????????????????????????????????????????????????????????????

        //???????????????POS?????? ?????????
        qrPayFlowDTO.setPosOrder(posOrder);

        //???????????????????????????????????????  todo edit
        Map<String,Object> resultMap = doPayTypeCheck(qrPayFlowDTO, recUser, payUser, qrPayDTO, request);
        JSONObject cardObj = (JSONObject)resultMap.get("cardList");
        GatewayDTO gatewayDTO = (GatewayDTO) resultMap.get("gateWay");
        //?????????????????????,??????????????????
        if(recUser.getUserType()==StaticDataEnum.USER_TYPE_20.getCode()){
            getQrPayFlow(qrPayFlowDTO,qrPayDTO,gatewayDTO,null!=cardObj ? JSONObject.parseObject(cardObj.toJSONString(), CardDTO.class):null,request);
        }
        // ??????????????????????????????????????????????????? todo  edit
        if(qrPayDTO.getPayType() == StaticDataEnum.PAY_TYPE_0.getCode() && qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimit(gatewayDTO.getType(),qrPayFlowDTO.getPayAmount(),request);
        }
        // ???????????????????????????????????????????????????>4.4???
        if(qrPayDTO.getPayType() == StaticDataEnum.PAY_TYPE_4.getCode() && (qrPayFlowDTO.getPayAmount() == null || qrPayFlowDTO.getPayAmount().compareTo(new BigDecimal( StaticDataEnum.INSTALLMENT_MIN_AMT.getMessage())) < 0)){
            throw new BizException(I18nUtils.get("credit.amount.limit", getLang(request), new String[]{StaticDataEnum.INSTALLMENT_MIN_AMT.getMessage()}));
        }
        //??????POS????????????
        if(posOrder){
            qrPayFlowDTO.setOrderSource(StaticDataEnum.ORDER_SOURCE_POS.getCode());
        }
        // ???????????? todo  ????????????
        qrPayFlowDTO.setId( qrPayFlowService.saveQrPayFlow(qrPayFlowDTO,request));


        // ????????????
        Map<String,Object> resMap = new HashMap<>();

        // ???????????????????????????????????????????????????????????????????????????
        if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 ||qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            // ???????????????????????????????????????
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_10.getCode());
            qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,request);

            boolean amountOutResult ;
            try {
                // ????????????????????????????????????????????????
                amountOutResult = doBatchAmountOut(qrPayFlowDTO,request);
//                if(TestEnvCheckerUtil.isTestEnv()){
//                    throw  new Exception("111");
//                }

            }catch (Exception e){
                log.error("???????????? Exception???"+e.getMessage(),e);
                //???????????????????????????
                if(gatewayDTO != null && gatewayDTO.getId() != null){
                    rechargeFlowService.channelLimitRollback(System.currentTimeMillis(),gatewayDTO.getType(),qrPayFlowDTO.getPayAmount(),request);
                }
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
                updateFlow(qrPayFlowDTO, null, null, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            //??????????????????????????????????????????????????????????????????????????????
            if(!amountOutResult){
                if(gatewayDTO != null && gatewayDTO.getId() != null){
                    rechargeFlowService.channelLimitRollback(System.currentTimeMillis(),gatewayDTO.getType(),qrPayFlowDTO.getPayAmount(),request);
                }
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
                updateFlow(qrPayFlowDTO, null, null, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
        }

        // ???????????????????????????????????????
        if (StaticDataEnum.PAY_TYPE_0.getCode() == (qrPayDTO.getPayType())) {
            //????????????
//            qrPayFlowDTO = calculation(qrPayDTO, qrPayFlowDTO, request);
            //?????????????????????????????????
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
            //??????????????????
            qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,request);
            //???????????????
            qrPayByCard(request, payUser, recUser, qrPayFlowDTO, gatewayDTO, JSONObject.parseObject(cardObj.toJSONString(), CardDTO.class) ,cardObj);

            // ???????????????????????????
            dealFirstDeal(qrPayFlowDTO,payUser,request);

            //????????????????????????
            try {
                log.info("????????????????????????????????????");
                posApiService.posPaySuccessNotice(qrPayFlowDTO.getMerchantId(), qrPayFlowDTO.getTransNo(), request);
            }catch (PosApiException e){
                log.error("POS????????????" + e.getMessage());
            }


        }
//        else if (StaticDataEnum.PAY_TYPE_1.getCode() == (qrPayDTO.getPayType())) {
        //????????????????????????
//            GatewayDTO gatewayDTO = doRouteConfig(recUser,qrPayFlowDTO,qrPayDTO,request);
        //????????????
//            qrPayFlowDTO = calculation(qrPayDTO, qrPayFlowDTO, request);
//            //??????????????????
//            if (recUser.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
//                //????????????
//                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_3.getCode());
//            } else {
//                //????????????
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
//            //????????????????????????
////            GatewayDTO gatewayDTO = doRouteConfig(recUser,qrPayFlowDTO,qrPayDTO,request);
//            //???????????????
//            if (recUser.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
//                //?????????????????????
//               // qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_10.getCode());
//                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
//            } else {
//                //???????????????
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
//            //????????????????????????
////            GatewayDTO gatewayDTO = doRouteConfig(recUser,qrPayFlowDTO,qrPayDTO,request);
//            //????????????
//            qrPayFlowDTO = calculation(qrPayDTO, qrPayFlowDTO, request);
//            //????????????
//            if (recUser.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
//                //??????????????????
//                //qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_11.getCode());
//                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
//
//            } else {
//                //????????????
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
            //?????????
//            qrPayFlowDTO = checkCreditParams(qrPayFlowDTO,qrPayDTO,recUser,payUser,request);
            // ?????????????????????????????????
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
            // ??????????????????
            qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,request);
            // ??????????????? todo edit
            qrPayFlowDTO = doCredit(qrPayFlowDTO,request);
            // ???????????????????????????
            dealFirstDeal(qrPayFlowDTO,payUser,request);

            //????????????????????????
            try {
                posApiService.posPaySuccessNotice(qrPayFlowDTO.getMerchantId(), qrPayFlowDTO.getTransNo(), request);
            }catch (PosApiException e){
                log.error("??????????????????????????????" + e.getMessage());
            }

            //??????????????????????????? ??????
            try{
                sendTransactionMail(qrPayFlowDTO.getCreditOrderNo(), request);
            }catch (Exception e){
                log.error("???????????????????????????????????????, CreditOrderNo:" + qrPayFlowDTO.getCreditOrderNo() + " || message:" + e.getMessage());
            }
        }
        else {
            //?????????????????????
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
     * ????????????
     *
     * @param qrPayDTO
     * @param request
     * @throws BizException
     * @return
     */
    @Override
    public Object doQrPayV2(QrPayDTO qrPayDTO, HttpServletRequest request) throws Exception {

        HashMap<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);

        //????????????????????????
        qrPayReqCheckV2(qrPayDTO, request);

        //??????????????????user??????
        UserDTO payUser = userService.findUserInfoV2(qrPayDTO.getPayUserId());
        UserDTO recUser = userService.findUserInfoV2(qrPayDTO.getRecUserId());

        //????????????????????????
        if (payUser == null  ||  payUser.getId() == null ) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }
        if(recUser == null || recUser.getId() == null ){
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        //???????????????POS??????
        /*boolean posOrder = false;
        //?????????????????????POS??????
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(1);
        map.put("sysTransNo", qrPayDTO.getTransNo());
        PosQrPayFlowDTO posQrPayFlow = posQrPayFlowService.findOnePosQrPayFlow(map);
        if(Objects.nonNull(posQrPayFlow) && Objects.nonNull(posQrPayFlow.getId())){
            posOrder = true;
        }*/

        //??????POS????????????
/*        if(posOrder){
            boolean posCheckResult = checkPosOrderStatus(posQrPayFlow.getOrderStatus());
            if(!posCheckResult){
                //?????????????????????
                throw new BizException(I18nUtils.get("pos.qrcode.disabled", getLang(request)));
            }
            //???POS?????????????????????
            posQrPayFlowService.updateOrderStatusBySysTransNo(qrPayDTO.getTransNo(), StaticDataEnum.POS_ORDER_STATUS_INPROCESS.getCode(), getUserId(request), System.currentTimeMillis());
        }*/

        //????????????????????????
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

        //????????????
        Map<String, Object> resultData = Maps.newHashMapWithExpectedSize(8);

        if(qrPayDTO.getPayType().equals(StaticDataEnum.PAY_TYPE_0.getCode())){
            //?????????
            resultData = cardPay(qrPayFlowDTO, qrPayDTO, payUser, request);

/*            // ???????????????????????????
            //?????????????????????????????????
            userService.firstPaidSuccessAmountIn(payUser.getId(), payUser.getInviterId(), 19, qrPayFlowDTO.getId(), request);

            //POS API ????????????????????????
            try {
                log.info("????????????????????????????????????");
                posApiService.posPaySuccessNotice(qrPayFlowDTO.getMerchantId(), qrPayFlowDTO.getTransNo(), request);
            }catch (PosApiException e){
                log.error("POS????????????" + e.getMessage());
            }*/

        }else if(qrPayDTO.getPayType().equals(StaticDataEnum.PAY_TYPE_4.getCode())){
            //?????????
            resultData = creditPay(qrPayFlowDTO, qrPayDTO, payUser, recUser, request);

            //?????????????????????????????????
//            userService.firstPaidSuccessAmountIn(payUser.getId(), payUser.getInviterId(), 19, qrPayFlowDTO.getId(), request);
        }

        try {
            // ???????????????????????????
            userService.firstPaidSuccessAmountIn(payUser.getId(), payUser.getInviterId(), StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode(), qrPayFlowDTO.getId(), request);
        }catch (Exception e){
            log.error("?????????????????????????????????,e:{}" , e);
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

        //????????????????????????
        qrPayReqCheckV3(qrPayDTO, request);

        //??????????????????user??????
        UserDTO payUser = userService.findUserInfoV2(qrPayDTO.getPayUserId());
        UserDTO recUser = userService.findUserInfoV2(qrPayDTO.getRecUserId());

        //????????????????????????
        if (payUser == null  ||  payUser.getId() == null ) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }
        if(recUser == null || recUser.getId() == null ){
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        //????????????????????????
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
        //??????????????????????????????
        qrPayFlowDTO.setMarketingBalance(qrPayDTO.getMarketingBalance());
        qrPayFlowDTO.setMarketingManageId(qrPayDTO.getMarketingManageId());

        qrPayFlowDTO.setMarketingType(qrPayDTO.getMarketingType());

        //????????????
        Map<String, Object> resultData = Maps.newHashMapWithExpectedSize(8);

        if(qrPayDTO.getPayType().equals(StaticDataEnum.PAY_TYPE_0.getCode())){
            //?????????
            resultData = cardPayV3(qrPayFlowDTO, qrPayDTO, payUser, request);
        }else if(qrPayDTO.getPayType().equals(StaticDataEnum.PAY_TYPE_4.getCode())){
            //?????????
            resultData = creditPayV3(qrPayFlowDTO, qrPayDTO, payUser, recUser, request);
        }else{
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        try {
            // ???????????????????????????
            userService.firstPaidSuccessAmountInV2(payUser.getId(), payUser.getInviterId(), StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode(), qrPayFlowDTO.getId(), request);
        }catch (Exception e){
            log.error("?????????????????????????????????,e:{}" , e);
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

        //????????????????????????
        qrPayReqCheckV3(qrPayDTO, request);

        //??????????????????user??????
        UserDTO payUser = userService.findUserById(qrPayDTO.getPayUserId());
        UserDTO recUser = userService.findUserInfoV2(qrPayDTO.getRecUserId());

        //????????????????????????
        if(recUser == null || recUser.getId() == null ){
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        //????????????????????????
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
        //??????????????????????????????
        qrPayFlowDTO.setMarketingBalance(qrPayDTO.getMarketingBalance());
        qrPayFlowDTO.setMarketingManageId(qrPayDTO.getMarketingManageId());

        qrPayFlowDTO.setMarketingType(qrPayDTO.getMarketingType());

        //????????????
        Map<String, Object> resultData = new HashMap<>(16);

        if(qrPayDTO.getPayType().equals(StaticDataEnum.PAY_TYPE_0.getCode())){
            //?????????
            resultData = cardPayV4(qrPayFlowDTO, qrPayDTO, payUser, request);
        }else if(qrPayDTO.getPayType().equals(StaticDataEnum.PAY_TYPE_4.getCode())){
            //?????????
            resultData = creditPayV4(qrPayFlowDTO, qrPayDTO, payUser, recUser, request);
        }else{
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        // ???????????????url
        if(resultData.get("url") != null ){
            resultMap.put("resultState", 9999);
        }else{
            try {
                // ???????????????????????????
                userService.firstPaidSuccessAmountInV2(payUser.getId(), payUser.getInviterId(), StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode(), qrPayFlowDTO.getId(), request);
            }catch (Exception e){
                log.error("?????????????????????????????????,e:{}" , e);
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
     * ?????????
     * @author zhoutt
     * @date 2021/10/29 10:55
     * @param qrPayFlowDTO
     * @param qrPayDTO
     * @param payUser
     * @param request
     */
    private Map<String, Object> cardPayV3(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, UserDTO payUser, HttpServletRequest request) throws Exception {

        //??????????????????
        Integer cardState = payUser.getCardState();
        if(!cardState.equals(StaticDataEnum.USER_CARD_STATE_BINDED.getCode())){
            throw new BizException(I18nUtils.get("pay.card.not.bound", getLang(request)));
        }

        //????????????
        //?????????
        qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode());

        //????????????
        Map<String, Object> channelResultMap = verifyAndGetPayGatewayCardInfo(qrPayDTO.getPayType(), qrPayDTO.getCardId(), qrPayFlowDTO, request);

        //??????????????????
        GatewayDTO gatewayDTO = (GatewayDTO) channelResultMap.get("gatewayDTO");
        CardDTO cardDTO = (CardDTO) channelResultMap.get("cardDTO");
        JSONObject cardJsonObj = (JSONObject) channelResultMap.get("cardJsonObj");

        //?????????????????? ????????????????????????
        Map<String, Object> amountMap = verifyAndGetPayAmountV3(qrPayFlowDTO, qrPayDTO, cardDTO, request);

        //??????????????????
        if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimit(gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(), request);
        }

        try{
            Long id = qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request);
            qrPayFlowDTO.setId(id);
        }catch (Exception e){
            log.error("??????????????????????????????",  qrPayDTO.toString());
            //????????????
            if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
            }
            throw new BizException(I18nUtils.get("ay.order.same", getLang(request)));
        }

        //????????????
        if((qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 && qrPayFlowDTO.getMarketingId() == null ) ||
                qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0 ){
            doAccountOut(qrPayFlowDTO, gatewayDTO, request);
        }

        // ???????????????
        if(qrPayFlowDTO.getMarketingId() != null){
            boolean useMarketingResult = false;
            try{
                useMarketingResult = this.useOneMarketing(qrPayFlowDTO,request);
            }catch (Exception e){
                log.error("????????????????????????");
                //????????????
                if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                    rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
                }
                // ????????????
                doBatchAmountOutRollBack(qrPayFlowDTO,request);
                //?????????????????????????????????
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                updateFlow(qrPayFlowDTO, null, null, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            if(!useMarketingResult){
                //????????????
                if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                    rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
                }
                //????????????
                doBatchAmountOutRollBack(qrPayFlowDTO,request);
                //???????????????????????????????????????
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                updateFlow(qrPayFlowDTO, null, null, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
        }


        //????????????
        BigDecimal donationAmount = new BigDecimal((String) amountMap.get("donationAmount"));
        if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
            // ??????????????????
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

        //????????????????????????
        BigDecimal tipAmount = qrPayDTO.getTipAmount();
        if(null != tipAmount && tipAmount.compareTo(BigDecimal.ZERO) > 0){
            // ??????????????????
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

        //?????????????????????????????????
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
        qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,request);

        //???????????????
        //??????????????????????????????
        WithholdFlowDTO withholdFlowDTO = null;
        try {
            withholdFlowDTO = packageWithHoldFlow(qrPayFlowDTO, cardDTO, new BigDecimal((String) amountMap.get("cardPayFee")),  (BigDecimal) amountMap.get("cardPayRateReal"), gatewayDTO);
            withholdFlowDTO.setTransAmount(withholdFlowDTO.getTransAmount().add(qrPayFlowDTO.getTipAmount()).add(qrPayFlowDTO.getDonationAmount()));
            withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        } catch (Exception e) {
            log.error("????????????????????????????????????:"+e.getMessage(),e);
            //????????????
            if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
            }
            //????????????
            if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 ||
                    qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
                doBatchAmountOutRollBack(qrPayFlowDTO,request);
            }

            // ????????????
            oneMarketingRollback(qrPayFlowDTO,request);

            //?????????????????????????????????
            if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
                donationFlowService.updateDonationFlowStateByFlowId(qrPayFlowDTO.getId(), StaticDataEnum.TRANS_STATE_22.getCode(), request);
            }

            //?????????????????????????????????
            if(null != qrPayFlowDTO.getTipAmount() && qrPayFlowDTO.getTipAmount().compareTo(BigDecimal.ZERO) > 0){
                tipFlowService.updateStateByFlowId(qrPayFlowDTO.getId(), StaticDataEnum.TRANS_STATE_22.getCode(), request);
            }

            //???????????????????????????????????????5
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_22.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);


            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //??????????????????
        sendCardPayLatPayRequest(qrPayFlowDTO, withholdFlowDTO, cardJsonObj, request);

        //????????????????????????????????????
//        handleCardLatPayPostResult(withholdFlowDTO, qrPayFlowDTO,cardJsonObj, request);


        handleCardLatPayPostResultV3(withholdFlowDTO, qrPayFlowDTO,cardJsonObj, request);

        return amountMap;
    }

    /**
     * ?????????
     * @author zhoutt
     * @date 2021/10/29 10:55
     * @param qrPayFlowDTO
     * @param qrPayDTO
     * @param payUser
     * @param request
     */
    private Map<String, Object> cardPayV4(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, UserDTO payUser, HttpServletRequest request) throws Exception {

        //??????????????????
        Integer cardState = payUser.getCardState();
        if(!cardState.equals(StaticDataEnum.USER_CARD_STATE_BINDED.getCode())){
            throw new BizException(I18nUtils.get("pay.card.not.bound", getLang(request)));
        }

        //????????????
        //?????????
        qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode());

        //????????????
        Map<String, Object> channelResultMap = verifyAndGetPayGatewayCardInfo(qrPayDTO.getPayType(), qrPayDTO.getCardId(), qrPayFlowDTO, request);

        //??????????????????
        GatewayDTO gatewayDTO = (GatewayDTO) channelResultMap.get("gatewayDTO");
        CardDTO cardDTO = (CardDTO) channelResultMap.get("cardDTO");
        JSONObject cardJsonObj = (JSONObject) channelResultMap.get("cardJsonObj");

        //?????????????????? ????????????????????????
        Map<String, Object> amountMap = verifyAndGetPayAmountV3(qrPayFlowDTO, qrPayDTO, cardDTO, request);

        //??????????????????
        if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimit(gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(), request);
        }

        try{
            Long id = qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request);
            qrPayFlowDTO.setId(id);
        }catch (Exception e){
            log.error("??????????????????????????????",  qrPayDTO.toString());
            //????????????
            if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
            }
            throw new BizException(I18nUtils.get("ay.order.same", getLang(request)));
        }

        //????????????
        if((qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 && qrPayFlowDTO.getMarketingId() == null ) ||
                qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0 ){
            doAccountOut(qrPayFlowDTO, gatewayDTO, request);
        }

        // ???????????????
        if(qrPayFlowDTO.getMarketingId() != null){
            boolean useMarketingResult = false;
            try{
                useMarketingResult = this.useOneMarketing(qrPayFlowDTO,request);
            }catch (Exception e){
                log.error("????????????????????????");
                //????????????
                if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                    rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
                }
                // ????????????
                doBatchAmountOutRollBack(qrPayFlowDTO,request);
                //?????????????????????????????????
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                updateFlow(qrPayFlowDTO, null, null, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            if(!useMarketingResult){
                //????????????
                if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                    rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
                }
                //????????????
                doBatchAmountOutRollBack(qrPayFlowDTO,request);
                //???????????????????????????????????????
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                updateFlow(qrPayFlowDTO, null, null, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
        }


        //????????????
        BigDecimal donationAmount = new BigDecimal((String) amountMap.get("donationAmount"));
        if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
            // ??????????????????
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

        //????????????????????????
        BigDecimal tipAmount = qrPayDTO.getTipAmount();
        if(null != tipAmount && tipAmount.compareTo(BigDecimal.ZERO) > 0){
            // ??????????????????
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

        //?????????????????????????????????
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
        qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,request);

        //???????????????
        //??????????????????????????????
        WithholdFlowDTO withholdFlowDTO = null;
        try {
            withholdFlowDTO = packageWithHoldFlow(qrPayFlowDTO, cardDTO, new BigDecimal((String) amountMap.get("cardPayFee")),  (BigDecimal) amountMap.get("cardPayRateReal"), gatewayDTO);
            withholdFlowDTO.setTransAmount(withholdFlowDTO.getTransAmount().add(qrPayFlowDTO.getTipAmount()).add(qrPayFlowDTO.getDonationAmount()));
            withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        } catch (Exception e) {
            log.error("????????????????????????????????????:"+e.getMessage(),e);
            //????????????
            if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
            }
            //????????????
            if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 ||
                    qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
                doBatchAmountOutRollBack(qrPayFlowDTO,request);
            }

            // ????????????
            oneMarketingRollback(qrPayFlowDTO,request);

            //?????????????????????????????????
            if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
                donationFlowService.updateDonationFlowStateByFlowId(qrPayFlowDTO.getId(), StaticDataEnum.TRANS_STATE_22.getCode(), request);
            }

            //?????????????????????????????????
            if(null != qrPayFlowDTO.getTipAmount() && qrPayFlowDTO.getTipAmount().compareTo(BigDecimal.ZERO) > 0){
                tipFlowService.updateStateByFlowId(qrPayFlowDTO.getId(), StaticDataEnum.TRANS_STATE_22.getCode(), request);
            }

            //???????????????????????????????????????5
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_22.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);


            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //??????????????????
        if(Integer.parseInt(gatewayDTO.getType().toString()) == StaticDataEnum.GATEWAY_TYPE_0.getCode()){
            sendCardPayLatPayRequest(qrPayFlowDTO, withholdFlowDTO, cardJsonObj, request);
        }else {
            Map<String,Object> urlMap = sendStripeRequest(qrPayFlowDTO, withholdFlowDTO, cardDTO, request);
            if(urlMap != null && urlMap.size() > 0) {
                // ?????????3DS??????
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
                updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
                return urlMap;
            }
        }

        //????????????????????????????????????
        handleCardLatPayPostResultV3(withholdFlowDTO, qrPayFlowDTO,cardJsonObj, request);

        return amountMap;
    }

    private Map<String,Object> sendStripeRequest(QrPayFlowDTO qrPayFlowDTO, WithholdFlowDTO withholdFlowDTO, CardDTO cardDTO, HttpServletRequest request) {

        if(withholdFlowDTO.getTransAmount().compareTo(BigDecimal.ZERO) > 0){
            //??????????????????
            StripeAPIResponse stripeResult = null;
            try{
                StripePaymentIntentDTO stripePaymentIntentDTO = new StripePaymentIntentDTO();
                stripePaymentIntentDTO.setAmount(withholdFlowDTO.getTransAmount().multiply(new BigDecimal("100")).setScale(0,BigDecimal.ROUND_DOWN));
                stripePaymentIntentDTO.setCurrency("aud");
                stripePaymentIntentDTO.setConfirm(true);
                // ?????????card_1KIpEuAgx3Fd2j3e0Mz1gZZr ?????????card_1KM49nAgx3Fd2j3elyLhTzhC
                stripePaymentIntentDTO.setPayment_method(cardDTO.getStripeToken());
                List<String>  paymentMethodTypes = new ArrayList<>();


                paymentMethodTypes.add("card");
                stripePaymentIntentDTO.setPayment_method_types(paymentMethodTypes);
                stripePaymentIntentDTO.setCustomer(qrPayFlowDTO.getPayUserId().toString());
                // todo ????????????
//                stripePaymentIntentDTO.setCustomer("638621894721458176");
                //todo ??????3ds????????????????????????
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
                // ????????????
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
                // ??????????????????????????????
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                return  null ;
            }

            withholdFlowDTO.setReturnMessage(paymentIntent.getStatus());

            if(StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_CANCELED.getCode().equals(paymentIntent.getStatus())
                    || StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_REQUIRES_PAYMENT_METHOD.getCode().equals(paymentIntent.getStatus())
                    || StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_FAILED.getCode().equals(paymentIntent.getStatus())){
                // ??????
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                withholdFlowDTO.setErrorMessage(paymentIntent.getLastPaymentError().getMessage());
                withholdFlowDTO.setReturnCode(paymentIntent.getLastPaymentError().getCode());
            } else if(StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_REQUIRES_ACTION.getCode().equals(paymentIntent.getStatus())){
                // ??????3DS
                // ???????????????URL ??? ?????? ????????????
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
                // ????????????
                // ??????????????????????????????????????????
                if(paymentIntent.getId() == null){
                    withholdFlowDTO.setStripeId(paymentIntent.getId());
                }
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            }else{
                // ????????????????????????
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            }

        }else{
            //???????????????0??????
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            withholdFlowDTO.setCheckState( StaticDataEnum.IS_CHECK_1.getCode());
            withholdFlowDTO.setOrdreNo(null);
        }
        return  null;
    }

    /**
     * ?????????
     * @author zhangzeyuan
     * @date 2021/6/21 17:55
     * @param qrPayFlowDTO
     * @param qrPayDTO
     * @param payUser
     * @param request
     */
    private Map<String, Object> cardPay(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, UserDTO payUser, HttpServletRequest request) throws Exception {

        //??????????????????
        Integer cardState = payUser.getCardState();
        if(!cardState.equals(StaticDataEnum.USER_CARD_STATE_BINDED.getCode())){
            throw new BizException(I18nUtils.get("pay.card.not.bound", getLang(request)));
        }

        //????????????
        //?????????
        qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode());

        //????????????
        Map<String, Object> channelResultMap = verifyAndGetPayGatewayCardInfo(qrPayDTO.getPayType(), qrPayDTO.getCardId(), qrPayFlowDTO, request);

        //??????????????????
        GatewayDTO gatewayDTO = (GatewayDTO) channelResultMap.get("gatewayDTO");
        CardDTO cardDTO = (CardDTO) channelResultMap.get("cardDTO");
        JSONObject cardJsonObj = (JSONObject) channelResultMap.get("cardJsonObj");

        //?????????????????? ????????????????????????
        Map<String, Object> amountMap = verifyAndGetPayAmount(qrPayFlowDTO, qrPayDTO, cardDTO, request);

        //??????????????????
        if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimit(gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(), request);
        }

        try{
            Long id = qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request);
            qrPayFlowDTO.setId(id);
        }catch (Exception e){
            log.error("??????????????????????????????",  qrPayDTO.toString());
            //????????????
            if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
            }
            throw new BizException(I18nUtils.get("pay.order.same", getLang(request)));
        }

        //????????????
        if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 ||
                qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            doAccountOut(qrPayFlowDTO, gatewayDTO, request);
        }

        //????????????
        BigDecimal donationAmount = new BigDecimal((String) amountMap.get("donationAmount"));
        if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
            // ??????????????????
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

        //????????????????????????
        BigDecimal tipAmount = qrPayDTO.getTipAmount();
        if(null != tipAmount && tipAmount.compareTo(BigDecimal.ZERO) > 0){
            // ??????????????????
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

        //?????????????????????????????????
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
        qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,request);

        //???????????????
        //??????????????????????????????
        WithholdFlowDTO withholdFlowDTO = null;
        try {

            withholdFlowDTO = packageWithHoldFlow(qrPayFlowDTO, cardDTO, new BigDecimal((String) amountMap.get("cardPayFee")),  (BigDecimal) amountMap.get("cardPayRateReal"), gatewayDTO);
            withholdFlowDTO.setTransAmount(withholdFlowDTO.getTransAmount().add(qrPayFlowDTO.getTipAmount()).add(qrPayFlowDTO.getDonationAmount()));
            withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        } catch (Exception e) {
            log.error("????????????????????????????????????:"+e.getMessage(),e);
            //????????????
            if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);
            }
            //????????????
            if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 ||
                    qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
                doBatchAmountOutRollBack(qrPayFlowDTO,request);
            }
            //???????????????????????????????????????
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_22.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);

            //?????????????????????????????????
            if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
                donationFlowService.updateDonationFlowStateByFlowId(qrPayFlowDTO.getId(), StaticDataEnum.TRANS_STATE_22.getCode(), request);
            }

            //?????????????????????????????????
            if(null != qrPayFlowDTO.getTipAmount() && qrPayFlowDTO.getTipAmount().compareTo(BigDecimal.ZERO) > 0){
                tipFlowService.updateStateByFlowId(qrPayFlowDTO.getId(), StaticDataEnum.TRANS_STATE_22.getCode(), request);
            }

            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //??????????????????
        sendCardPayLatPayRequest(qrPayFlowDTO, withholdFlowDTO, cardJsonObj, request);

        //????????????????????????????????????
        handleCardLatPayPostResult(withholdFlowDTO, qrPayFlowDTO,null, request);

        return amountMap;
    }



    /**
     * ????????????????????????
     * @author zhangzeyuan
     * @date 2021/6/27 19:59
     * @param qrPayFlowDTO
     * @param gatewayDTO
     * @param request
     */
    private void doAccountOut(QrPayFlowDTO qrPayFlowDTO,GatewayDTO gatewayDTO, HttpServletRequest request) throws Exception {
        // ???????????????????????????????????????
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_10.getCode());
        qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,request);

        //????????????
        boolean amountOutResult;
        try {
            // ????????????????????????????????????????????????
            amountOutResult = doBatchAmountOut(qrPayFlowDTO,request);
        }catch (Exception e){
            log.error("???????????? Exception???"+e.getMessage(),e);
            //???????????????????????????
            if(gatewayDTO != null && gatewayDTO.getId() != null){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(), request);
            }

            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //??????????????????????????????????????????????????????????????????????????????
        if(!amountOutResult){
            rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);

            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
    }


    /**
     * ???????????????????????????????????????
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

        //????????????????????????
        HashMap<String, Object> queryParamsMap = Maps.newHashMapWithExpectedSize(3);
        queryParamsMap.put("id", qrPayDTO.getMerchantId());
        queryParamsMap.put("userId", recUserId);
        queryParamsMap.put("isAvailable", StaticDataEnum.AVAILABLE_1.getCode());
        MerchantDTO merchantDTO = merchantService.findOneMerchant(queryParamsMap);
        if(Objects.isNull(merchantDTO) || Objects.isNull(merchantDTO.getId())) {
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        //????????????
        BigDecimal transAmount = qrPayDTO.getTransAmount();
        //??????????????????
        BigDecimal tempTransAmount = transAmount;

        //??????????????????
        BigDecimal redEnvelopeAmount = qrPayDTO.getRedEnvelopeAmount() == null ? BigDecimal.ZERO : qrPayDTO.getRedEnvelopeAmount();

        //??????????????????????????????
        BigDecimal payUserAmount = BigDecimal.ZERO;
        //????????????????????????????????????
        BigDecimal merchantAmount = BigDecimal.ZERO;
        //????????????
        BigDecimal extraDiscount = BigDecimal.ZERO;
        //????????????
        BigDecimal marketingDiscount = BigDecimal.ZERO;
        //????????????
        BigDecimal baseDiscount = BigDecimal.ZERO;

        //??????????????????
        BigDecimal wholeSaleDiscount = BigDecimal.ZERO;

        //???redis?????????????????????
        Map<?, Object> amountMap = redisUtils.hmget(Constant.getOrderAmountDetailRedisKey(payUserId.toString(), qrPayDTO.getTransNo()));
        if(null == amountMap || amountMap.isEmpty()){
            //??????????????????
            amountMap = getOrderDiscount(payUserId, recUserId, merchantDTO, payType);
        }

        payUserAmount = (BigDecimal) amountMap.get("payUserAmount");
        merchantAmount = (BigDecimal) amountMap.get("merchantAmount");

        wholeSaleDiscount = (BigDecimal) amountMap.get("wholeSaleDiscount");

        extraDiscount = (BigDecimal) amountMap.get("extraDiscount");
        marketingDiscount = (BigDecimal) amountMap.get("marketingDiscount");
        baseDiscount = (BigDecimal) amountMap.get("baseDiscount");

        //????????????????????????????????????


        if(redEnvelopeAmount.compareTo(payUserAmount) > 0){
            throw new BizException(I18nUtils.get("pay.red.envelope.over.limit", getLang(request)));
        }

        //????????????????????????
        BigDecimal useWholeSaleAmount = tempTransAmount.min(merchantAmount);
        //????????????????????????
        BigDecimal useNormalSaleAmount = tempTransAmount.subtract(useWholeSaleAmount);

        //??????????????????????????????
        BigDecimal wholeSaleUseRedAmount = BigDecimal.ZERO;
        //??????????????????????????????
        BigDecimal normalSaleUesRedAmount = BigDecimal.ZERO;

        if(redEnvelopeAmount.compareTo(BigDecimal.ZERO) > 0){
            if(useWholeSaleAmount.compareTo(redEnvelopeAmount) >= 0){
                wholeSaleUseRedAmount = redEnvelopeAmount;
            }else {
                wholeSaleUseRedAmount = useWholeSaleAmount;
            }
            normalSaleUesRedAmount = redEnvelopeAmount.subtract(wholeSaleUseRedAmount);
        }

        //????????????????????????
        BigDecimal wholeSaleDiscountAmount = (useWholeSaleAmount.subtract(wholeSaleUseRedAmount)).multiply(wholeSaleDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        //????????????????????????  ?????????????????? +  ?????????????????? + ??????????????????
        BigDecimal baseDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(baseDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal extraDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(extraDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal marketingDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(marketingDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);

        BigDecimal normalSaleDiscountAmount = baseDiscountAmount.add(extraDiscountAmount).add(marketingDiscountAmount);

        // ????????????
        BigDecimal payAmount = useWholeSaleAmount.subtract(wholeSaleUseRedAmount).subtract(wholeSaleDiscountAmount).add(
                useNormalSaleAmount.subtract(normalSaleUesRedAmount).subtract(normalSaleDiscountAmount)
        );

        //????????????????????? ????????? + ????????? + ?????? + ?????????
        BigDecimal tempCardPayAllAmount = BigDecimal.ZERO;

        //????????????????????????
        BigDecimal creditCardFirstPayAmount = BigDecimal.ZERO;
        //?????????????????????
        BigDecimal remainingCreditPayAmount = BigDecimal.ZERO;

        //???????????????
        BigDecimal gateWayFee = BigDecimal.ZERO;
        //??????????????????
        BigDecimal transChannelFeeRate = BigDecimal.ZERO;
        //????????????
        int direction = 0;

        //????????????
        BigDecimal donationAmount = BigDecimal.ZERO;
        if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId())){
            DonationInstituteDataDTO donationData = donationInstituteService.getDonationDataById(qrPayDTO.getDonationInstiuteId());
            if(null != donationData && null != donationData.getId()){
                donationAmount = donationData.getDonationAmount();
            }
        }
        //????????????
        BigDecimal tipAmount = BigDecimal.ZERO;
        if(Objects.nonNull(qrPayDTO.getTipAmount())){
            tipAmount = qrPayDTO.getTipAmount();
        }


        //???????????????
        if(payAmount.compareTo(BigDecimal.ZERO) > 0){
            if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){
                //?????????
                //????????????????????????
                // 20211028 ???????????????10???????????????????????????
                if(payAmount.compareTo(new BigDecimal("10")) < 0){
                    creditCardFirstPayAmount = payAmount;
                }else{
                    creditCardFirstPayAmount = payAmount.multiply(new BigDecimal(Constant.PAY_CREDIT_NEED_CARDPAY_AMOUNT_RATE)).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                //?????????????????????
                remainingCreditPayAmount = payAmount.subtract(creditCardFirstPayAmount);

                //???????????????????????????????????? =  25%??????????????? + ????????? + ??????
                tempCardPayAllAmount = creditCardFirstPayAmount.add(donationAmount).add(tipAmount);
            }else if(qrPayDTO.getPayType().equals(StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode())){
                //?????????
                tempCardPayAllAmount = payAmount.add(donationAmount).add(tipAmount);
                payAmount = payAmount.add(donationAmount).add(tipAmount);
            }
        }else {

            tempCardPayAllAmount = donationAmount.add(tipAmount);
        }

        if(tempCardPayAllAmount.compareTo(BigDecimal.ZERO) > 0){
            // ?????????????????????
            Map<String, Object> feeMap = getCardPayTransactionFee(0, cardDTO.getCustomerCcType(), tempCardPayAllAmount, request);

            gateWayFee = (BigDecimal) feeMap.get("channelFeeAmount");

            transChannelFeeRate = (BigDecimal) feeMap.get("transChannelFeeRate");

            tempCardPayAllAmount = tempCardPayAllAmount.add(gateWayFee);
        }

        log.info("TrulyPayAmount:"+ tempCardPayAllAmount);

        //?????????????????????????????? ??? ??????????????????
        if(useWholeSaleAmount.compareTo(qrPayDTO.getWholeSalesAmount()) != 0
                || useNormalSaleAmount.compareTo(qrPayDTO.getNormalSalesAmount()) != 0){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //?????? ???????????????????????? ???????????? ?????????????????????????????????????????? ??????????????????
        if(tempCardPayAllAmount.compareTo(qrPayDTO.getTrulyPayAmount()) != 0){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }



        //????????????
        BigDecimal realWholeSaleDiscount;
        //??????????????????
        BigDecimal realBaseDiscount;
        BigDecimal realMarkingDiscount;
        BigDecimal realExtraDiscount;

        //????????????
        int saleType = 0;

        //????????????????????????????????????
        if(useNormalSaleAmount.compareTo(BigDecimal.ZERO) > 0){
            //???????????????????????????
            realBaseDiscount = baseDiscount;
            realMarkingDiscount = marketingDiscount;
            realExtraDiscount = extraDiscount;

            if(useWholeSaleAmount.compareTo(BigDecimal.ZERO) > 0){
                // ????????????
                saleType = StaticDataEnum.SALE_TYPE_2.getCode();
                realWholeSaleDiscount = wholeSaleDiscount;
            }else{
                // ????????????
                saleType = StaticDataEnum.SALE_TYPE_0.getCode();
                realWholeSaleDiscount = BigDecimal.ZERO;
            }
        }else{
            //????????????
            saleType = StaticDataEnum.SALE_TYPE_1.getCode();

            realWholeSaleDiscount = wholeSaleDiscount;

            realBaseDiscount = BigDecimal.ZERO;
            realMarkingDiscount = BigDecimal.ZERO;
            realExtraDiscount = BigDecimal.ZERO;
        }

        //???????????????
        BigDecimal platformFee = useNormalSaleAmount.multiply(merchantDTO.getAppChargeRate() == null ? BigDecimal.ZERO : merchantDTO.getAppChargeRate()).setScale(2,BigDecimal.ROUND_HALF_UP);

        //???????????? = ???????????? - ???????????? - ????????????????????? -???????????? - ??????????????? ???????????????
        BigDecimal recAmount = useNormalSaleAmount.subtract(normalSaleDiscountAmount).subtract(platformFee);

        //??????qrPayFlowDTO??????
        qrPayFlowDTO.setMerchantId(merchantDTO.getId());
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
        qrPayFlowDTO.setSaleType(saleType);

        qrPayFlowDTO.setPlatformFee(platformFee);
        qrPayFlowDTO.setRecAmount(recAmount);

        //??????????????????
        qrPayFlowDTO.setWholeSalesAmount(useWholeSaleAmount);
        //??????????????????
        qrPayFlowDTO.setNormalSaleAmount(useNormalSaleAmount);
        //??????
        qrPayFlowDTO.setRedEnvelopeAmount(redEnvelopeAmount);

        //?????????????????? ????????????
        qrPayFlowDTO.setWholeSalesDiscount(realWholeSaleDiscount);
        qrPayFlowDTO.setWholeSalesDiscountAmount(wholeSaleDiscountAmount);

        //????????????????????????????????????
        qrPayFlowDTO.setBaseDiscountAmount(baseDiscountAmount);
        qrPayFlowDTO.setExtraDiscountAmount(extraDiscountAmount);
        qrPayFlowDTO.setMarkingDiscountAmount(marketingDiscountAmount);
        //?????????????????????????????????
        qrPayFlowDTO.setBaseDiscount(realBaseDiscount);
        qrPayFlowDTO.setExtraDiscount(realExtraDiscount);
        qrPayFlowDTO.setMarkingDiscount(realMarkingDiscount);
        //?????????
        qrPayFlowDTO.setDonationAmount(donationAmount);
        //??????
        qrPayFlowDTO.setTipAmount(tipAmount);

        if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){
            qrPayFlowDTO.setPayAmount(payAmount);
            //?????????borrowID
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

        //??????????????????
        HashMap<String, Object> resultMap = Maps.newHashMapWithExpectedSize(13);

        //???????????????????????????
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
            //?????????
            resultMap.put("creditNeedCardPayAmount", creditCardFirstPayAmount.add(donationAmount).add(tipAmount).add(gateWayFee).toString());
            resultMap.put("creditNeedCardPayNoFeeAmount", creditCardFirstPayAmount.toString());
            resultMap.put("remainingCreditAmount", remainingCreditPayAmount.toString());

            //todo  ?????????????????????????????????????????????
            int period = 0;
            //??????????????????
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
     * ???????????????????????????????????????
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

        //????????????????????????
        HashMap<String, Object> queryParamsMap = Maps.newHashMapWithExpectedSize(3);
        queryParamsMap.put("id", qrPayDTO.getMerchantId());
        queryParamsMap.put("userId", recUserId);
        queryParamsMap.put("isAvailable", StaticDataEnum.AVAILABLE_1.getCode());
        MerchantDTO merchantDTO = merchantService.findOneMerchant(queryParamsMap);
        if(Objects.isNull(merchantDTO) || Objects.isNull(merchantDTO.getId())) {
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        //????????????
        BigDecimal transAmount = qrPayDTO.getTransAmount();
        //??????????????????
        BigDecimal tempTransAmount = transAmount;

        //??????????????????
//        BigDecimal redEnvelopeAmount = qrPayDTO.getRedEnvelopeAmount() == null ? BigDecimal.ZERO : qrPayDTO.getRedEnvelopeAmount();
        // ???????????????????????????????????????????????????????????????????????????
        // ????????????????????????
        BigDecimal redEnvelopeAmount = qrPayDTO.getMarketingBalance().min(transAmount);

        //??????????????????????????????
//        BigDecimal payUserAmount = BigDecimal.ZERO;
        //????????????????????????????????????
        BigDecimal merchantAmount = BigDecimal.ZERO;
        //????????????
        BigDecimal extraDiscount = BigDecimal.ZERO;
        //????????????
        BigDecimal marketingDiscount = BigDecimal.ZERO;
        //????????????
        BigDecimal baseDiscount = BigDecimal.ZERO;

        //??????????????????
        BigDecimal wholeSaleDiscount = BigDecimal.ZERO;

        //???redis?????????????????????
        Map<?, Object> amountMap = redisUtils.hmget(Constant.getOrderAmountDetailRedisKey(payUserId.toString(), qrPayDTO.getTransNo()));
        if(null == amountMap || amountMap.isEmpty()){
            //??????????????????
            amountMap = getOrderDiscount(payUserId, recUserId, merchantDTO, payType);
        }

//        payUserAmount = (BigDecimal) amountMap.get("payUserAmount");
        merchantAmount = (BigDecimal) amountMap.get("merchantAmount");

        wholeSaleDiscount = (BigDecimal) amountMap.get("wholeSaleDiscount");

        extraDiscount = (BigDecimal) amountMap.get("extraDiscount");
        marketingDiscount = (BigDecimal) amountMap.get("marketingDiscount");
        baseDiscount = (BigDecimal) amountMap.get("baseDiscount");

        //????????????????????????????????????
//        if(redEnvelopeAmount.compareTo(payUserAmount) > 0){
//            throw new BizException(I18nUtils.get("pay.red.envelope.over.limit", getLang(request)));
//        }

        //????????????????????????
        BigDecimal useWholeSaleAmount = tempTransAmount.min(merchantAmount);
        //????????????????????????
        BigDecimal useNormalSaleAmount = tempTransAmount.subtract(useWholeSaleAmount);

        //??????????????????????????????
        BigDecimal wholeSaleUseRedAmount = BigDecimal.ZERO;
        //??????????????????????????????
        BigDecimal normalSaleUesRedAmount = BigDecimal.ZERO;

        if(redEnvelopeAmount.compareTo(BigDecimal.ZERO) > 0){
            if(useWholeSaleAmount.compareTo(redEnvelopeAmount) >= 0){
                wholeSaleUseRedAmount = redEnvelopeAmount;
            }else {
                wholeSaleUseRedAmount = useWholeSaleAmount;
            }
            normalSaleUesRedAmount = redEnvelopeAmount.subtract(wholeSaleUseRedAmount);
        }

        //????????????????????????
        BigDecimal wholeSaleDiscountAmount = (useWholeSaleAmount.subtract(wholeSaleUseRedAmount)).multiply(wholeSaleDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        //????????????????????????  ?????????????????? +  ?????????????????? + ??????????????????
        BigDecimal baseDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(baseDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal extraDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(extraDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal marketingDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(marketingDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);

        BigDecimal normalSaleDiscountAmount = baseDiscountAmount.add(extraDiscountAmount).add(marketingDiscountAmount);

        // ????????????
        BigDecimal payAmount = useWholeSaleAmount.subtract(wholeSaleUseRedAmount).subtract(wholeSaleDiscountAmount).add(
                useNormalSaleAmount.subtract(normalSaleUesRedAmount).subtract(normalSaleDiscountAmount)
        );

        //?????????????????????????????? ??? ??????????????????
        if(useWholeSaleAmount.compareTo(qrPayDTO.getWholeSalesAmount()) != 0
                || useNormalSaleAmount.compareTo(qrPayDTO.getNormalSalesAmount()) != 0){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //????????????????????? ????????? + ????????? + ?????? + ?????????
        BigDecimal tempCardPayAllAmount = BigDecimal.ZERO;

        //????????????????????????
        BigDecimal creditCardFirstPayAmount = BigDecimal.ZERO;
        //?????????????????????
        BigDecimal remainingCreditPayAmount = BigDecimal.ZERO;

        //???????????????
        BigDecimal gateWayFee = BigDecimal.ZERO;
        //??????????????????
        BigDecimal transChannelFeeRate = BigDecimal.ZERO;
        //????????????
        int direction = 0;

        //????????????
        BigDecimal donationAmount = BigDecimal.ZERO;
        if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId())){
            DonationInstituteDataDTO donationData = donationInstituteService.getDonationDataById(qrPayDTO.getDonationInstiuteId());
            if(null != donationData && null != donationData.getId()){
                donationAmount = donationData.getDonationAmount();
            }
        }
        //????????????
        BigDecimal tipAmount = BigDecimal.ZERO;
        if(Objects.nonNull(qrPayDTO.getTipAmount())){
            tipAmount = qrPayDTO.getTipAmount();
        }

        //???????????????
        if(payAmount.compareTo(BigDecimal.ZERO) > 0){
            if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){

                //?????????
                //????????????????????????
                // 20211028 ???????????????10???????????????????????????  ??????????????? ????????????????????????10???
                if(null == qrPayDTO.getMarketingId() &&  payAmount.compareTo(new BigDecimal(StaticDataEnum.CREDIT_MIN_TRANSAMOUNT.getMessage())) < 0){
                    throw new BizException(I18nUtils.get("credit.amount.limit", getLang(request), new String[]{StaticDataEnum.CREDIT_MIN_TRANSAMOUNT.getMessage()}));
                }

                if(payAmount.compareTo(new BigDecimal("10")) < 0){
                    creditCardFirstPayAmount = payAmount;
                }else{
                    creditCardFirstPayAmount = payAmount.multiply(new BigDecimal(Constant.PAY_CREDIT_NEED_CARDPAY_AMOUNT_RATE)).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                //?????????????????????
                remainingCreditPayAmount = payAmount.subtract(creditCardFirstPayAmount);

                //???????????????????????????????????? =  25%??????????????? + ????????? + ??????
                tempCardPayAllAmount = creditCardFirstPayAmount.add(donationAmount).add(tipAmount);
            }else if(qrPayDTO.getPayType().equals(StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode())){
                //?????????
                tempCardPayAllAmount = payAmount.add(donationAmount).add(tipAmount);
            }
        }else {
            tempCardPayAllAmount = donationAmount.add(tipAmount);
        }

        log.info("TrulyPayAmount:"+ payAmount);
        log.info("tempCardPayAllAmount:"+ tempCardPayAllAmount);

        //?????? ???????????????????????? ???????????? ?????????????????????????????????????????? ?????????????????? ????????????????????????
        if(tempCardPayAllAmount.compareTo(qrPayDTO.getTrulyPayAmount()) != 0){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        if(tempCardPayAllAmount.compareTo(BigDecimal.ZERO) > 0){
            // ?????????????????????
            Map<String, Object> feeMap = getCardPayTransactionFee(Integer.parseInt(qrPayFlowDTO.getGatewayId().toString()), cardDTO.getCustomerCcType(), tempCardPayAllAmount, request);

            gateWayFee = (BigDecimal) feeMap.get("channelFeeAmount");

            transChannelFeeRate = (BigDecimal) feeMap.get("transChannelFeeRate");

            tempCardPayAllAmount = tempCardPayAllAmount.add(gateWayFee);
        }

        //????????????
        BigDecimal realWholeSaleDiscount;
        //??????????????????
        BigDecimal realBaseDiscount;
        BigDecimal realMarkingDiscount;
        BigDecimal realExtraDiscount;

        //????????????
        int saleType = 0;

        //????????????????????????????????????
        if(useNormalSaleAmount.compareTo(BigDecimal.ZERO) > 0){
            //???????????????????????????
            realBaseDiscount = baseDiscount;
            realMarkingDiscount = marketingDiscount;
            realExtraDiscount = extraDiscount;

            if(useWholeSaleAmount.compareTo(BigDecimal.ZERO) > 0){
                // ????????????
                saleType = StaticDataEnum.SALE_TYPE_2.getCode();
                realWholeSaleDiscount = wholeSaleDiscount;
            }else{
                // ????????????
                saleType = StaticDataEnum.SALE_TYPE_0.getCode();
                realWholeSaleDiscount = BigDecimal.ZERO;
            }
        }else{
            //????????????
            saleType = StaticDataEnum.SALE_TYPE_1.getCode();

            realWholeSaleDiscount = wholeSaleDiscount;

            realBaseDiscount = BigDecimal.ZERO;
            realMarkingDiscount = BigDecimal.ZERO;
            realExtraDiscount = BigDecimal.ZERO;
        }

        //???????????????
        BigDecimal platformFee = useNormalSaleAmount.multiply(merchantDTO.getAppChargeRate() == null ? BigDecimal.ZERO : merchantDTO.getAppChargeRate()).setScale(2,BigDecimal.ROUND_HALF_UP);

        //???????????? = ???????????? - ???????????? - ????????????????????? -???????????? - ??????????????? ???????????????
        BigDecimal recAmount = useNormalSaleAmount.subtract(normalSaleDiscountAmount).subtract(platformFee);

        //??????qrPayFlowDTO??????
        qrPayFlowDTO.setMerchantId(merchantDTO.getId());
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
        qrPayFlowDTO.setSaleType(saleType);

        qrPayFlowDTO.setPlatformFee(platformFee);
        qrPayFlowDTO.setRecAmount(recAmount);

        qrPayFlowDTO.setRedEnvelopeAmount(redEnvelopeAmount);
        //??????????????????
        qrPayFlowDTO.setWholeSalesAmount(useWholeSaleAmount);
        //??????????????????
        qrPayFlowDTO.setNormalSaleAmount(useNormalSaleAmount);

        //?????????????????? ????????????
        qrPayFlowDTO.setWholeSalesDiscount(realWholeSaleDiscount);
        qrPayFlowDTO.setWholeSalesDiscountAmount(wholeSaleDiscountAmount);

        //????????????????????????????????????
        qrPayFlowDTO.setBaseDiscountAmount(baseDiscountAmount);
        qrPayFlowDTO.setExtraDiscountAmount(extraDiscountAmount);
        qrPayFlowDTO.setMarkingDiscountAmount(marketingDiscountAmount);
        //?????????????????????????????????
        qrPayFlowDTO.setBaseDiscount(realBaseDiscount);
        qrPayFlowDTO.setExtraDiscount(realExtraDiscount);
        qrPayFlowDTO.setMarkingDiscount(realMarkingDiscount);
        //?????????
        qrPayFlowDTO.setDonationAmount(donationAmount);
        //??????
        qrPayFlowDTO.setTipAmount(tipAmount);

        if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){
            qrPayFlowDTO.setPayAmount(payAmount);
            //?????????borrowID
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

        //??????????????????
        HashMap<String, Object> resultMap = Maps.newHashMapWithExpectedSize(13);

        //???????????????????????????
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
            //?????????
            resultMap.put("creditNeedCardPayAmount", creditCardFirstPayAmount.add(donationAmount).add(tipAmount).add(gateWayFee).toString());
            resultMap.put("creditNeedCardPayNoFeeAmount", creditCardFirstPayAmount.toString());
            resultMap.put("remainingCreditAmount", remainingCreditPayAmount.toString());

            //todo  ?????????????????????????????????????????????
            int period = 0;
            //??????????????????
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
     * ??????????????? ????????????????????????
     * @author zhangzeyuan
     * @date 2021/6/24 16:14
     * @param gatewayPayType ????????????
     * @param cardId ???ID
     * @param qrPayFlowDTO
     * @param request
     * tail
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    private Map<String, Object>  verifyAndGetPayGatewayCardInfo(Integer gatewayPayType, Long cardId, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws Exception{
        HashMap<String, Object> resultMap = Maps.newHashMapWithExpectedSize(3);

        //????????????????????????
        resultMap.put("gatewayType", gatewayPayType);
        resultMap.put("state", StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(resultMap);
        // todo ??????
        resultMap.clear();
        if(null == gatewayDTO || null == gatewayDTO.getId()){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        resultMap.put("gatewayDTO", gatewayDTO);

        //????????????????????????
        Long gatewayType = gatewayDTO.getType();
        //???????????????
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

        //??????????????????
        if(gatewayType.intValue() == StaticDataEnum.GATEWAY_TYPE_0.getCode()){
            //?????????LatPay???????????????token????????????
            if(StringUtils.isBlank(cardDTO.getCrdStrgToken())){
                throw new BizException(I18nUtils.get("channel.token.missing", getLang(request)));
            }
        }else if(gatewayType.intValue() == StaticDataEnum.GATEWAY_TYPE_4.getCode()){
            //?????????integrapay ??????uniqueReference?????????
            if(cardDTO.getUniqueReference() == null){
                throw new BizException(I18nUtils.get("channel.token.missing", getLang(request)));
            }
        }else if(gatewayType.intValue() == StaticDataEnum.GATEWAY_TYPE_8.getCode()){
            //Stripe ??????StripeToken?????????
            if(cardDTO.getStripeToken() == null){
                throw new BizException(I18nUtils.get("channel.token.missing", getLang(request)));
            }
        } else {
                //???????????????
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        // ??????????????????????????????
        userService.verifyCardFailedTime(qrPayFlowDTO.getPayUserId(), cardId,request);

        qrPayFlowDTO.setCardId(cardId.toString());
        qrPayFlowDTO.setGatewayId(gatewayType.longValue());
        return resultMap;
    }



    /**
     * ???????????????????????????
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
            //??????????????????
            try{
                // LatPay Request
                withholdFlowDTO = latPayService.latPayRequest(withholdFlowDTO,withholdFlowDTO.getTransAmount(),qrPayFlowDTO.getPlatformFee(),cardObj,request, qrPayFlowDTO.getPayUserIp());
            }catch (Exception e){
                log.error("??????latpay????????????:"+e.getMessage(),e);
                //????????????
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            }
        }else{
            //???????????????0??????
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            withholdFlowDTO.setCheckState( StaticDataEnum.IS_CHECK_1.getCode());
            withholdFlowDTO.setOrdreNo(null);
        }
    }


    /**
     * ????????? ????????????????????????????????????
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
        //????????????????????????
        if (status.equals(StaticDataEnum.TRANS_STATE_1.getCode())) {
            //??????
            handleLatPaySuccess(withholdFlowDTO, qrPayFlowDTO, request);

        } else if (status.equals(StaticDataEnum.TRANS_STATE_2.getCode())) {
            log.error("????????????????????????");
            //??????
            handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, qrPayFlowDTO.getPayAmount(), null, request);

            if(request != null){
                // ??????????????????????????????
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
                    // ??????????????????????????????
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
            //??????
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
            updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
            throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
        }
    }

    /**
     * ????????? ????????????????????????????????????
     * @author zhangzeyuan
     * @date 2021/7/1 9:58
     * @param withholdFlowDTO
     * @param qrPayFlowDTO
     * @param request
     */
    public void handleCardLatPayPostResultV3(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO, JSONObject cardObj,
                                           HttpServletRequest request) throws Exception{
        Integer status = withholdFlowDTO.getState();
        //????????????????????????
        if (status.equals(StaticDataEnum.TRANS_STATE_1.getCode())) {
            //??????
            handleLatPaySuccess(withholdFlowDTO, qrPayFlowDTO, request);
            // ???????????????????????????
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("cardId", qrPayFlowDTO.getCardId());

            if(null != qrPayFlowDTO.getMarketingId() && null != qrPayFlowDTO.getMarketingManageId()){
                //??????????????????
                HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
                paramMap.put("id", qrPayFlowDTO.getMarketingManageId());
                marketingManagementService.addUsedNumber(paramMap);
            }

            try{
                if(null != cardObj.getInteger("payState") && cardObj.getInteger("payState") == StaticDataEnum.STATUS_0.getCode()){
                    serverService.setCardSuccessPay(jsonObject);
                }
            }catch (Exception e){
                log.error("???????????????????????????,userId:{},e:{},",getUserId(request),e.getMessage() ,e);
            }

            // ?????????stripe ??????????????????????????????????????????
            /*if(qrPayFlowDTO.getGatewayId() == StaticDataEnum.GATEWAY_TYPE_8.getCode()){
                UserDTO userDTO = userService.findUserById(qrPayFlowDTO.getPayUserId());
                if(userDTO.getStripeState() == StaticDataEnum.STATUS_1.getCode()){
                    userDTO.setStripeState(StaticDataEnum.STATUS_0.getCode());
                }
            }*/


        } else if (status.equals(StaticDataEnum.TRANS_STATE_2.getCode())) {
            log.error("????????????????????????");
            // ??????
            qrPayFlowDTO = handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, qrPayFlowDTO.getPayAmount(), cardObj != null ? cardObj.getInteger("payState") : null, request);

            Integer deleteCardState = qrPayFlowDTO.getDeleteCardState();
            Integer cardCount = qrPayFlowDTO.getCardCount();
            if(request != null){
                // ??????????????????????????????
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
                    // ??????????????????????????????
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
            //??????
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
            updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
            throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
        }else {
            // 3DS??????
            updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
        }
    }


    /**
     * ????????? 25%????????? ????????????????????????????????????
     * @author zhangzeyuan
     * @date 2021/7/1 9:59
     * @param withholdFlowDTO
     * @param qrPayFlowDTO
     * @param cardObj
     * @param creditNeedCardPayAmount   25%?????? + ?????????+ ?????? + ?????????
     * @param creditNeedCardPayNoFeeAmount    25%??????
     * @param remainingCreditAmount  ??????????????????
     * @param cardPayRate
     * @param cardPayFee
     * @param request
     */
    public QrPayFlowDTO handleCreditCardPayDataByThirdStatusV3(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO, JSONObject cardObj,
                                                     BigDecimal creditNeedCardPayAmount, BigDecimal creditNeedCardPayNoFeeAmount, BigDecimal remainingCreditAmount,
                                                     BigDecimal cardPayRate, BigDecimal cardPayFee, HttpServletRequest request) throws Exception{
        Integer status = withholdFlowDTO.getState();

        //????????????????????????
        if (status.equals(StaticDataEnum.TRANS_STATE_1.getCode())) {
            //??????????????????
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

                //?????????????????????
                Integer orderState = createCreditInstallmentOrder(qrPayFlowDTO, cardPayRate, creditNeedCardPayNoFeeAmount, cardPayFee, creditNeedCardPayAmount, cardObj, request);

                //???????????????????????????
                if(orderState.equals(StaticDataEnum.API_ORDER_STATE_1.getCode())){

                    //????????????????????????????????????
                    createCreditOrderFlowDTO.setState(StaticDataEnum.CREATE_CREDIT_ORDER_FLOW_STATE_1.getCode());
                    createCreditOrderFlowService.updateCreateCreditOrderFlow(createCreditOrderFlowId, createCreditOrderFlowDTO, request);
                    //????????????????????????
                    handleLatPaySuccess(withholdFlowDTO, qrPayFlowDTO, request);

                    try{
                        JSONObject jsonObject = new JSONObject();
                        // ???????????????????????? ???????????????????????????
                        jsonObject.put("cardId", qrPayFlowDTO.getCardId());
                        jsonObject.put("payState",StaticDataEnum.STATUS_1.getCode());
                        userService.presetCard(jsonObject,qrPayFlowDTO.getPayUserId(), request);
                    }catch (Exception e){
                        log.error("????????????????????????????????????????????????,e:{},userId",e,getUserId(request));
                    }

                    if(null != qrPayFlowDTO.getMarketingId() && null != qrPayFlowDTO.getMarketingManageId()){
                        //??????????????????
                        HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
                        paramMap.put("id", qrPayFlowDTO.getMarketingManageId());
                        marketingManagementService.addUsedNumber(paramMap);
                    }
                }else{
                    //???????????? ??????/?????? ????????????
                    createCreditOrderFlowDTO.setState(StaticDataEnum.CREATE_CREDIT_ORDER_FLOW_STATE_3.getCode());
                    createCreditOrderFlowService.updateCreateCreditOrderFlow(createCreditOrderFlowId, createCreditOrderFlowDTO, request);

                    //?????????????????????????????????
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
                    updateFlowForConcurrency(qrPayFlowDTO, null, null, request);
                    return qrPayFlowDTO;
                }
            }else {
                if(withholdFlowDTO.getTransAmount().compareTo(BigDecimal.ZERO) > 0){
                    //???????????????
                    // ????????????????????????
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("cardId", qrPayFlowDTO.getCardId());
                    // ???????????????????????????
                    jsonObject.put("payState",StaticDataEnum.STATUS_1.getCode());
                    try{
                        userService.presetCard(jsonObject,qrPayFlowDTO.getPayUserId(), request);
                    }catch (Exception e){
                        log.error("????????????????????????????????????????????????,e:{},userId",e,getUserId(request));
                    }
                }
                //???????????????0  ????????????????????????
                // ????????????
                handleLatPaySuccess(withholdFlowDTO, qrPayFlowDTO, request);
            }

            //POS API ????????????????????????
            /*try {
                log.info("????????????????????????????????????");
                posApiService.posPaySuccessNotice(qrPayFlowDTO.getMerchantId(), qrPayFlowDTO.getTransNo(), request);
            }catch (PosApiException e){
                log.error("POS????????????" + e.getMessage());
            }*/

            //??????????????????????????? ?????? todo ??????
            try{
                sendTransactionMail(qrPayFlowDTO.getCreditOrderNo(), request);
            }catch (Exception e){
                log.error("???????????????????????????????????????, CreditOrderNo:" + qrPayFlowDTO.getCreditOrderNo() + " || message:" + e.getMessage());
            }
        } else if (status.equals(StaticDataEnum.TRANS_STATE_2.getCode())) {
            // ??????????????????
            // ???????????????????????????????????????????????????
            if( cardObj != null && cardObj.getInteger("payState") == null && qrPayFlowDTO.getCardId()!= null){
                //???????????????
                cardObj.clear();
                try {
                    cardObj = serverService.getCardInfo(Long.parseLong(qrPayFlowDTO.getCardId()));
                } catch (Exception e) {
                    log.error("????????????????????????????????????,flow_id:"+qrPayFlowDTO.getId()+",error:"+e.getMessage(),e);
//                    throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
                }
            }

            if (cardObj == null || StringUtils.isBlank(cardObj.toJSONString())) {
                // ?????????????????????
                qrPayFlowDTO = handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, withholdFlowDTO.getOrderAmount(), null,request);
            }else{
                qrPayFlowDTO = handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, withholdFlowDTO.getOrderAmount(), cardObj.getInteger("payState"),request);
            }

            Integer deleteCardState = qrPayFlowDTO.getDeleteCardState();
            Integer cardCount = qrPayFlowDTO.getCardCount();
            if(request != null){
                // ??????????????????????????????
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
                    // ??????????????????????????????
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
            //??????
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

        //????????????????????????
        if (status.equals(StaticDataEnum.TRANS_STATE_1.getCode())) {
            //??????????????????
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

                //?????????????????????
                Integer orderState = createCreditInstallmentOrder(qrPayFlowDTO, cardPayRate, creditNeedCardPayNoFeeAmount, cardPayFee, creditNeedCardPayAmount, cardObj, request);

                //???????????????????????????
                if(orderState.equals(StaticDataEnum.API_ORDER_STATE_1.getCode())){

                    //????????????????????????????????????
                    createCreditOrderFlowDTO.setState(StaticDataEnum.CREATE_CREDIT_ORDER_FLOW_STATE_1.getCode());
                    createCreditOrderFlowService.updateCreateCreditOrderFlow(createCreditOrderFlowId, createCreditOrderFlowDTO, request);
                    //????????????????????????
                    handleLatPaySuccess(withholdFlowDTO, qrPayFlowDTO, request);

                    //????????????????????????
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("cardId", qrPayFlowDTO.getCardId());
                    try{
                        userService.presetCard(jsonObject, qrPayFlowDTO.getPayUserId(), request);
                    }catch (Exception e){
                        log.error("????????????????????????????????????????????????,e:{},userId",e,getUserId(request));
                    }

                }else{
                    //???????????? ??????/?????? ????????????
                    createCreditOrderFlowDTO.setState(StaticDataEnum.CREATE_CREDIT_ORDER_FLOW_STATE_3.getCode());
                    createCreditOrderFlowService.updateCreateCreditOrderFlow(createCreditOrderFlowId, createCreditOrderFlowDTO, request);

                    //?????????????????????????????????
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
                    updateFlowForConcurrency(qrPayFlowDTO, null, null, request);
                    return;
                }
            }else {
                if(withholdFlowDTO.getTransAmount().compareTo(BigDecimal.ZERO) > 0){
                    // ????????????????????????
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("cardId", qrPayFlowDTO.getCardId());
                    try{
                        userService.presetCard(jsonObject,qrPayFlowDTO.getPayUserId(), request);
                    }catch (Exception e){
                        log.error("????????????????????????????????????????????????,e:{},userId",e,getUserId(request));
                    }
                }
                //???????????????0  ????????????????????????
                // ????????????
                handleLatPaySuccess(withholdFlowDTO, qrPayFlowDTO, request);
            }

            //POS API ????????????????????????
            /*try {
                log.info("????????????????????????????????????");
                posApiService.posPaySuccessNotice(qrPayFlowDTO.getMerchantId(), qrPayFlowDTO.getTransNo(), request);
            }catch (PosApiException e){
                log.error("POS????????????" + e.getMessage());
            }*/

            //??????????????????????????? ?????? todo ??????
            try{
                sendTransactionMail(qrPayFlowDTO.getCreditOrderNo(), request);
            }catch (Exception e){
                log.error("???????????????????????????????????????, CreditOrderNo:" + qrPayFlowDTO.getCreditOrderNo() + " || message:" + e.getMessage());
            }
        } else if (status.equals(StaticDataEnum.TRANS_STATE_2.getCode())) {
            //??????????????????
            //????????????
            handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, withholdFlowDTO.getOrderAmount(), null, request);
            if(request != null){
                // ??????????????????????????????
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
                    // ??????????????????????????????
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
            //??????
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
            updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
            throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
        }
    }
    /**
     *  ???????????????????????????
     * @author zhangzeyuan
     * @date 2021/6/28 0:17
     * @param withholdFlowDTO
     * @param qrPayFlowDTO
     * @param request
     */
    private void handleLatPaySuccess(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException{
        //???????????????????????????
        if((qrPayFlowDTO.getPayAmount().add(qrPayFlowDTO.getTipAmount()).add(qrPayFlowDTO.getDonationAmount())).compareTo(BigDecimal.ZERO) > 0){
            try {
                userService.logCardFailedTime(qrPayFlowDTO.getPayUserId(), Long.parseLong(qrPayFlowDTO.getCardId()),true,request);
            }catch ( Exception e){
                log.error("??????????????????????????????????????????????????????flowId???"+withholdFlowDTO.getFlowId()+",exception:"+e.getMessage(),e);
            }
        }

        // ???????????????????????????????????????0?????????

        if(qrPayFlowDTO.getIsNeedClear() == StaticDataEnum.NEED_CLEAR_TYPE_0.getCode() || qrPayFlowDTO.getRecAmount().compareTo(BigDecimal.ZERO) == 0
                || qrPayFlowDTO.getOrderSource() == StaticDataEnum.ORDER_SOURCE_1.getCode()){
            //???????????????????????????
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
            updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
        }else{
            //??????????????????

            // ?????????????????????31??????????????????????????????????????????????????????????????????
            // ????????????31??????????????????????????????????????????
            if(qrPayFlowDTO.getState() != StaticDataEnum.TRANS_STATE_31.getCode() ){
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_30.getCode());
                updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
                //??????????????????
                sendMessage(qrPayFlowDTO.getRecUserId(), qrPayFlowDTO.getId(), request);
            }

            //????????????????????????
            AccountFlowDTO accountFlowDTO = getAccountFlowDTO(qrPayFlowDTO);
            //????????????????????????
            accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO, request));
            try {
                doAmountTrans(accountFlowDTO, qrPayFlowDTO, request);
            } catch (Exception e) {
                log.error("qrPay dealThirdSuccess Exception:"+e.getMessage(),e);
                //????????????
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                updateFlow(qrPayFlowDTO, null, null, request);
            }
        }

        if(qrPayFlowDTO.getState() == StaticDataEnum.TRANS_STATE_31.getCode() && qrPayFlowDTO.getOrderSource() != StaticDataEnum.ORDER_SOURCE_1.getCode()){
            // ???????????????????????????
            //qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
            //updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
            // ???????????????????????????
            if(qrPayFlowDTO.getNormalSaleAmount().compareTo(BigDecimal.ZERO) > 0){
                // ????????????????????? ?????????????????????
                MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayFlowDTO.getMerchantId());
                // ???????????????????????????
                if(merchantDTO.getHaveWholeSell() == StaticDataEnum.MERCHANT_WHOLE_SELL_AMOUNT_NOT_ZERO.getCode()){
                    try {
                        // ????????????????????????
                        BigDecimal merchantAmount = userService.getBalance(qrPayFlowDTO.getRecUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
                        // ????????????0 ??????????????????????????????
                        if(merchantAmount.compareTo(BigDecimal.ZERO) == 0){
                            merchantService.updateHaveWholeSell(qrPayFlowDTO.getMerchantId(),0,request);
                        }
                    }catch (Exception e){
                        log.error("dealThirdSuccess ?????????????????????????????? ???id:"+qrPayFlowDTO.getId());
                    }
                }
            }
        }

    }

    /**
     *  ??????latpay??????????????????
     * @author zhangzeyuan
     * @date 2021/7/1 14:36
     * @param withholdFlowDTO
     * @param qrPayFlowDTO
     * @param payState
     * @param request
     */
    private QrPayFlowDTO handleLatPayFailed(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO, BigDecimal channelLimtRollbackAmt, Integer payState, HttpServletRequest request) throws BizException{
        //?????????????????????
        if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
            try {
                userService.logCardFailedTime(qrPayFlowDTO.getPayUserId(),Long.parseLong(qrPayFlowDTO.getCardId()),false,request);
            }catch (Exception e){
                log.error("????????????????????????????????????????????????flowId:" + qrPayFlowDTO.getId());
            }
        }

        //????????????
        if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), withholdFlowDTO.getGatewayId(), channelLimtRollbackAmt, request);
        }

        //????????????
        if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 ||
                qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            doBatchAmountOutRollBack(qrPayFlowDTO,request);
        }

        //?????????????????????
        if(qrPayFlowDTO.getTransType().equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode())){
            BigDecimal creditRollbackAmount = qrPayFlowDTO.getPayAmount().add(qrPayFlowDTO.getDonationAmount()).add(qrPayFlowDTO.getTipAmount()).subtract(channelLimtRollbackAmt);
            if(creditRollbackAmount.compareTo(BigDecimal.ZERO) > 0){
                log.error("???????????????????????????");
                creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId() ,creditRollbackAmount , request);
            }
        }

        // ????????????
        if(qrPayFlowDTO.getMarketingId() != null){
            oneMarketingRollback(qrPayFlowDTO,request);
        }

        //????????????????????????????????? ?????????
        if(payState != null && payState == StaticDataEnum.USER_CARD_STATE_0.getCode()){
            //??????
            JSONObject unbindCardObj = new JSONObject();
            unbindCardObj.put("cardId", qrPayFlowDTO.getCardId());
            unbindCardObj.put("lang", 1);
            unbindCardObj.put("userId", qrPayFlowDTO.getPayUserId());
            try{
                // ?????????
                JSONObject result = userService.cardUnbundling(unbindCardObj, request);
                if(result == null || result.getInteger("state") != StaticDataEnum.CARD_UNBUNDLING_STATE_4.getCode()){
                    qrPayFlowDTO.setDeleteCardState(0);
                }else{
                    qrPayFlowDTO.setDeleteCardState(1);
                }

                // ?????????????????????
                if(qrPayFlowDTO.getGatewayId().intValue() == StaticDataEnum.GATEWAY_TYPE_8.getCode()){

                    qrPayFlowDTO.setCardCount(cardService.getStripeCardCount(qrPayFlowDTO.getPayUserId(), request));
                }else{
                    qrPayFlowDTO.setCardCount(cardService.getLatpayCardCount(qrPayFlowDTO.getPayUserId(), request));

                }
            }catch (Exception e){
                log.error("v3??????-????????????, userId:{}, card:{}", qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getCardId() + e.getMessage(),e);
            }

        }

        //????????????????????????
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_22.getCode());
        if(qrPayFlowDTO.getOrderSource() == StaticDataEnum.ORDER_SOURCE_1.getCode()){
            qrPayFlowDTO.setIsShow(StaticDataEnum.ORDER_SHOW_STATE_0.getCode());
        }
        updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);

        return qrPayFlowDTO;
    }


    /**
     * ???????????????
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
        //?????????????????????????????????????????????????????????????????????
        if(!payUser.getInstallmentState().equals(StaticDataEnum.STATUS_1.getCode())
                || payUser.getSplitAddInfoState().equals(StaticDataEnum.STATUS_1.getCode())){
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //????????????
        Map<String, Object> userCreditInfoMap = getCreditUserAvailableAmountAndState(payUser.getId());

        //?????????????????????
        BigDecimal creditAmount = (BigDecimal) userCreditInfoMap.get("creditAmount");

        //?????????????????????
        Integer userState = (Integer) userCreditInfoMap.get("userState");

        //??????????????????
        if(userState.equals(StaticDataEnum.CREAT_USER_STATE_11.getCode())){
            throw new BizException(I18nUtils.get("pay.user.freeze", getLang(request)));
        }

        //??????????????????
        qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());
        qrPayFlowDTO.setProductId(qrPayDTO.getProductId());

        //????????????
        Map<String, Object> channelResultMap = verifyAndGetPayGatewayCardInfo(0, qrPayDTO.getCardId(), qrPayFlowDTO, request);

        GatewayDTO gatewayDTO = (GatewayDTO) channelResultMap.get("gatewayDTO");
        CardDTO cardDTO = (CardDTO) channelResultMap.get("cardDTO");
        JSONObject cardJsonObj = (JSONObject) channelResultMap.get("cardJsonObj");

        //??????????????????
        Map<String, Object> amountResultMap = verifyAndGetPayAmount(qrPayFlowDTO, qrPayDTO, cardDTO, request);

        //??????????????????
        BigDecimal creditNeedCardPayAmount = new BigDecimal((String) amountResultMap.get("creditNeedCardPayAmount"));
        //??????????????? ???????????????
        BigDecimal creditNeedCardPayNoFeeAmount = new BigDecimal((String) amountResultMap.get("creditNeedCardPayNoFeeAmount"));
        //?????????????????????
        BigDecimal remainingCreditAmount = new BigDecimal((String) amountResultMap.get("remainingCreditAmount"));
        //?????????????????? ??????
        BigDecimal cardPayRate = new BigDecimal((String) amountResultMap.get("cardPayRate"));
        BigDecimal cardPayFee = new BigDecimal((String) amountResultMap.get("cardPayFee"));

        //????????????????????????
        if(creditAmount.compareTo(BigDecimal.ZERO) <= 0 || creditAmount.compareTo(remainingCreditAmount) < 0){
            throw new BizException(I18nUtils.get("pay.credit.amount.insufficient", getLang(request)));
        }

        //????????????
        if(creditNeedCardPayNoFeeAmount.compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimit(0L, creditNeedCardPayNoFeeAmount, request);
        }

        try{
            Long id = qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request);
            qrPayFlowDTO.setId(id);
        }catch (Exception e){
            //????????????
            if(creditNeedCardPayNoFeeAmount.compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), creditNeedCardPayNoFeeAmount,request);
            }
            throw new BizException(I18nUtils.get("pay.order.same", getLang(request)));
        }

        //????????????
        BigDecimal donationAmount = new BigDecimal((String) amountResultMap.get("donationAmount"));
        if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
            // ??????????????????
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

        //????????????????????????
        BigDecimal tipAmount = qrPayDTO.getTipAmount();
        if(null != tipAmount && tipAmount.compareTo(BigDecimal.ZERO) > 0){
            // ??????????????????
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

        //????????????
        if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 ||
                qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            doAccountOut(qrPayFlowDTO, gatewayDTO, request);
        }

        //??????75%??????
        if(remainingCreditAmount.compareTo(BigDecimal.ZERO) > 0){
            //????????????????????????
            PayCreditBalanceFlowDTO payCreditBalanceFlowDTO = new PayCreditBalanceFlowDTO();
            payCreditBalanceFlowDTO.setQrPayFlowId(qrPayFlowDTO.getId());
            payCreditBalanceFlowDTO.setOperateType(StaticDataEnum.PAY_BALANCE_FLOW_OPERATE_TYPE_1.getCode());
            payCreditBalanceFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
            payCreditBalanceFlowDTO.setCreditQuotaAmount(remainingCreditAmount);
            payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_0.getCode());
            Long id = payCreditBalanceFlowService.savePayCreditBalanceFlow(payCreditBalanceFlowDTO, request);

            boolean frozenResult = frozenCreditAmount(remainingCreditAmount, payUser.getId(), qrPayFlowDTO.getId(), qrPayFlowDTO.getId());

            if(!frozenResult){
                log.error("????????????????????????");
                //????????????
                //?????? ????????????
                creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);
                //?????????????????????????????????
                payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_2.getCode());
                payCreditBalanceFlowService.updatePayCreditBalanceFlow(id, payCreditBalanceFlowDTO, request);

                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_1.getCode());
            payCreditBalanceFlowService.updatePayCreditBalanceFlow(id, payCreditBalanceFlowDTO, request);
        }

        //?????????????????????????????????
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
        updateFlow(qrPayFlowDTO, null, null, request);

        //25%????????????????????????

        //??????????????????????????????
        WithholdFlowDTO withholdFlowDTO = null;
        try {
            QrPayFlowDTO creditCardPayFlowDTO = new QrPayFlowDTO();
            BeanUtils.copyProperties(qrPayFlowDTO, creditCardPayFlowDTO);
            creditCardPayFlowDTO.setPayAmount(creditNeedCardPayAmount);

            withholdFlowDTO = packageWithHoldFlow(creditCardPayFlowDTO, cardDTO, new BigDecimal((String) amountResultMap.get("cardPayFee")),  (BigDecimal) amountResultMap.get("cardPayRateReal"), gatewayDTO);
            withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        } catch (Exception e) {
            log.error("????????????????????????????????????:"+e.getMessage(),e);
            //????????????????????????
            //?????? ????????????
            creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);

            //?????????????????????
            creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId(), remainingCreditAmount, request);

            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //??????????????????
        sendCardPayLatPayRequest(qrPayFlowDTO, withholdFlowDTO, cardJsonObj, request);

        //????????????????????????????????????
        handleCreditCardPayDataByThirdStatus(withholdFlowDTO, qrPayFlowDTO, cardJsonObj, creditNeedCardPayAmount, creditNeedCardPayNoFeeAmount,
                remainingCreditAmount, cardPayRate, cardPayFee, request);

        return amountResultMap;
    }

    /**
     * ???????????????
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
        //?????????????????????????????????????????????????????????????????????
        if(!payUser.getInstallmentState().equals(StaticDataEnum.STATUS_1.getCode())){
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //????????????
        Map<String, Object> userCreditInfoMap = getCreditUserAvailableAmountAndState(payUser.getId());

        //?????????????????????
        BigDecimal creditAmount = (BigDecimal) userCreditInfoMap.get("creditAmount");

        //?????????????????????
        Integer userState = (Integer) userCreditInfoMap.get("userState");

        //??????????????????
        if(userState.equals(StaticDataEnum.CREAT_USER_STATE_11.getCode())){
            throw new BizException(I18nUtils.get("pay.user.freeze", getLang(request)));
        }

        //??????????????????
        qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());
        qrPayFlowDTO.setProductId(qrPayDTO.getProductId());

        //????????????
        Map<String, Object> channelResultMap = verifyAndGetPayGatewayCardInfo(0, qrPayDTO.getCardId(), qrPayFlowDTO, request);

        GatewayDTO gatewayDTO = (GatewayDTO) channelResultMap.get("gatewayDTO");
        CardDTO cardDTO = (CardDTO) channelResultMap.get("cardDTO");
        JSONObject cardJsonObj = (JSONObject) channelResultMap.get("cardJsonObj");

        //??????????????????
        Map<String, Object> amountResultMap = verifyAndGetPayAmountV3(qrPayFlowDTO, qrPayDTO, cardDTO, request);

        //??????????????????
        BigDecimal creditNeedCardPayAmount = new BigDecimal((String) amountResultMap.get("creditNeedCardPayAmount"));
        //??????????????? ???????????????
        BigDecimal creditNeedCardPayNoFeeAmount = new BigDecimal((String) amountResultMap.get("creditNeedCardPayNoFeeAmount"));
        //?????????????????????
        BigDecimal remainingCreditAmount = new BigDecimal((String) amountResultMap.get("remainingCreditAmount"));
        //?????????????????? ??????
        BigDecimal cardPayRate = new BigDecimal((String) amountResultMap.get("cardPayRate"));
        BigDecimal cardPayFee = new BigDecimal((String) amountResultMap.get("cardPayFee"));

        //????????????????????????
        if(creditAmount.compareTo(BigDecimal.ZERO) <= 0 || creditAmount.compareTo(remainingCreditAmount) < 0){
            throw new BizException(I18nUtils.get("pay.credit.amount.insufficient", getLang(request)));
        }

        //????????????
        if(creditNeedCardPayNoFeeAmount.compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimit(0L, creditNeedCardPayNoFeeAmount, request);
        }

        try{
            Long id = qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request);
            qrPayFlowDTO.setId(id);
        }catch (Exception e){
            //????????????
            if(creditNeedCardPayNoFeeAmount.compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), creditNeedCardPayNoFeeAmount,request);
            }
            throw new BizException(I18nUtils.get("pay.order.same", getLang(request)));
        }

        //????????????????????????????????????
        if(qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            doAccountOut(qrPayFlowDTO, gatewayDTO, request);
        }

        //??????75%??????
        if(remainingCreditAmount.compareTo(BigDecimal.ZERO) > 0){
            //????????????????????????
            PayCreditBalanceFlowDTO payCreditBalanceFlowDTO = new PayCreditBalanceFlowDTO();
            payCreditBalanceFlowDTO.setQrPayFlowId(qrPayFlowDTO.getId());
            payCreditBalanceFlowDTO.setOperateType(StaticDataEnum.PAY_BALANCE_FLOW_OPERATE_TYPE_1.getCode());
            payCreditBalanceFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
            payCreditBalanceFlowDTO.setCreditQuotaAmount(remainingCreditAmount);
            payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_0.getCode());
            Long id = payCreditBalanceFlowService.savePayCreditBalanceFlow(payCreditBalanceFlowDTO, request);

            boolean frozenResult = frozenCreditAmount(remainingCreditAmount, payUser.getId(), qrPayFlowDTO.getId(), qrPayFlowDTO.getId());

            if(!frozenResult){
                log.error("????????????????????????");
                //????????????
                //?????? ????????????
                creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);
                //?????????????????????????????????
                payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_2.getCode());
                payCreditBalanceFlowService.updatePayCreditBalanceFlow(id, payCreditBalanceFlowDTO, request);

                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_1.getCode());
            payCreditBalanceFlowService.updatePayCreditBalanceFlow(id, payCreditBalanceFlowDTO, request);
        }

        // ???????????????
        if(qrPayFlowDTO.getMarketingId() != null){
            boolean useMarketingResult = false;
            try{
                useMarketingResult = this.useOneMarketing(qrPayFlowDTO,request);
            }catch (Exception e){
                log.error("????????????????????????");
                //????????????
                //?????? ????????????
                creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);
                //?????????????????????????????????
                creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId(), remainingCreditAmount, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            if(!useMarketingResult){
                //?????? ????????????
                creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);
                //?????????????????????????????????
                creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId(), remainingCreditAmount, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
        }

        //????????????
        BigDecimal donationAmount = new BigDecimal((String) amountResultMap.get("donationAmount"));
        if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
            // ??????????????????
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

        //????????????????????????
        BigDecimal tipAmount = qrPayDTO.getTipAmount();
        if(null != tipAmount && tipAmount.compareTo(BigDecimal.ZERO) > 0){
            // ??????????????????
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

        //?????????????????????????????????
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
        updateFlow(qrPayFlowDTO, null, null, request);

        //25%????????????????????????

        //??????????????????????????????
        WithholdFlowDTO withholdFlowDTO = null;
        try {
            QrPayFlowDTO creditCardPayFlowDTO = new QrPayFlowDTO();
            BeanUtils.copyProperties(qrPayFlowDTO, creditCardPayFlowDTO);
            creditCardPayFlowDTO.setPayAmount(creditNeedCardPayAmount);

            withholdFlowDTO = packageWithHoldFlow(creditCardPayFlowDTO, cardDTO, new BigDecimal((String) amountResultMap.get("cardPayFee")),  (BigDecimal) amountResultMap.get("cardPayRateReal"), gatewayDTO);
            withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        } catch (Exception e) {
            log.error("????????????????????????????????????:"+e.getMessage(),e);
            //????????????????????????
            //?????? ????????????
            creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);

            //?????????????????????
            creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId(), remainingCreditAmount, request);
            //????????????
            oneMarketingRollback(qrPayFlowDTO,request);

            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }


        //??????????????????
        sendCardPayLatPayRequest(qrPayFlowDTO, withholdFlowDTO, cardJsonObj, request);

        //????????????????????????????????????
        handleCreditCardPayDataByThirdStatusV3(withholdFlowDTO, qrPayFlowDTO, cardJsonObj, creditNeedCardPayAmount, creditNeedCardPayNoFeeAmount,
                remainingCreditAmount, cardPayRate, cardPayFee, request);

        return amountResultMap;
    }


    /**
     * ???????????????
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
        //?????????????????????????????????????????????????????????????????????
        if(!payUser.getInstallmentState().equals(StaticDataEnum.STATUS_1.getCode())){
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //????????????
        Map<String, Object> userCreditInfoMap = getCreditUserAvailableAmountAndState(payUser.getId());

        //?????????????????????
        BigDecimal creditAmount = (BigDecimal) userCreditInfoMap.get("creditAmount");

        //?????????????????????
        Integer userState = (Integer) userCreditInfoMap.get("userState");

        //??????????????????
        if(userState.equals(StaticDataEnum.CREAT_USER_STATE_11.getCode())){
            throw new BizException(I18nUtils.get("pay.user.freeze", getLang(request)));
        }

        //??????????????????
        qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());
        qrPayFlowDTO.setProductId(qrPayDTO.getProductId());

        //????????????
        Map<String, Object> channelResultMap = verifyAndGetPayGatewayCardInfo(0, qrPayDTO.getCardId(), qrPayFlowDTO, request);

        GatewayDTO gatewayDTO = (GatewayDTO) channelResultMap.get("gatewayDTO");
        CardDTO cardDTO = (CardDTO) channelResultMap.get("cardDTO");
        JSONObject cardJsonObj = (JSONObject) channelResultMap.get("cardJsonObj");

        //??????????????????
        Map<String, Object> amountResultMap = verifyAndGetPayAmountV3(qrPayFlowDTO, qrPayDTO, cardDTO, request);

        //??????????????????
        BigDecimal creditNeedCardPayAmount = new BigDecimal((String) amountResultMap.get("creditNeedCardPayAmount"));
        //??????????????? ???????????????
        BigDecimal creditNeedCardPayNoFeeAmount = new BigDecimal((String) amountResultMap.get("creditNeedCardPayNoFeeAmount"));
        //?????????????????????
        BigDecimal remainingCreditAmount = new BigDecimal((String) amountResultMap.get("remainingCreditAmount"));
        //?????????????????? ??????
        BigDecimal cardPayRate = new BigDecimal((String) amountResultMap.get("cardPayRate"));
        BigDecimal cardPayFee = new BigDecimal((String) amountResultMap.get("cardPayFee"));

        //????????????????????????
        if(creditAmount.compareTo(BigDecimal.ZERO) <= 0 || creditAmount.compareTo(remainingCreditAmount) < 0){
            throw new BizException(I18nUtils.get("pay.credit.amount.insufficient", getLang(request)));
        }

        //????????????
        if(creditNeedCardPayNoFeeAmount.compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimit(0L, creditNeedCardPayNoFeeAmount, request);
        }

        try{
            Long id = qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request);
            qrPayFlowDTO.setId(id);
        }catch (Exception e){
            //????????????
            if(creditNeedCardPayNoFeeAmount.compareTo(BigDecimal.ZERO) > 0){
                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), creditNeedCardPayNoFeeAmount,request);
            }
            throw new BizException(I18nUtils.get("pay.order.same", getLang(request)));
        }

        //????????????????????????????????????
        if(qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            doAccountOut(qrPayFlowDTO, gatewayDTO, request);
        }

        //??????75%??????
        if(remainingCreditAmount.compareTo(BigDecimal.ZERO) > 0){
            //????????????????????????
            PayCreditBalanceFlowDTO payCreditBalanceFlowDTO = new PayCreditBalanceFlowDTO();
            payCreditBalanceFlowDTO.setQrPayFlowId(qrPayFlowDTO.getId());
            payCreditBalanceFlowDTO.setOperateType(StaticDataEnum.PAY_BALANCE_FLOW_OPERATE_TYPE_1.getCode());
            payCreditBalanceFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
            payCreditBalanceFlowDTO.setCreditQuotaAmount(remainingCreditAmount);
            payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_0.getCode());
            Long id = payCreditBalanceFlowService.savePayCreditBalanceFlow(payCreditBalanceFlowDTO, request);

            boolean frozenResult = frozenCreditAmount(remainingCreditAmount, payUser.getId(), qrPayFlowDTO.getId(), qrPayFlowDTO.getId());

            if(!frozenResult){
                log.error("????????????????????????");
                //????????????
                //?????? ????????????
                creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);
                //?????????????????????????????????
                payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_2.getCode());
                payCreditBalanceFlowService.updatePayCreditBalanceFlow(id, payCreditBalanceFlowDTO, request);

                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_1.getCode());
            payCreditBalanceFlowService.updatePayCreditBalanceFlow(id, payCreditBalanceFlowDTO, request);
        }

        // ???????????????
        if(qrPayFlowDTO.getMarketingId() != null){
            boolean useMarketingResult = false;
            try{
                useMarketingResult = this.useOneMarketing(qrPayFlowDTO,request);
            }catch (Exception e){
                log.error("????????????????????????");
                //????????????
                //?????? ????????????
                creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);
                //?????????????????????????????????
                creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId(), remainingCreditAmount, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            if(!useMarketingResult){
                //?????? ????????????
                creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);
                //?????????????????????????????????
                creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId(), remainingCreditAmount, request);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
        }

        //????????????
        BigDecimal donationAmount = new BigDecimal((String) amountResultMap.get("donationAmount"));
        if(StringUtils.isNotBlank(qrPayDTO.getDonationInstiuteId()) && donationAmount.compareTo(BigDecimal.ZERO) > 0){
            // ??????????????????
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

        //????????????????????????
        BigDecimal tipAmount = qrPayDTO.getTipAmount();
        if(null != tipAmount && tipAmount.compareTo(BigDecimal.ZERO) > 0){
            // ??????????????????
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

        //?????????????????????????????????
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
        updateFlow(qrPayFlowDTO, null, null, request);

        //25%????????????????????????

        //??????????????????????????????
        WithholdFlowDTO withholdFlowDTO = null;
        try {
            QrPayFlowDTO creditCardPayFlowDTO = new QrPayFlowDTO();
            BeanUtils.copyProperties(qrPayFlowDTO, creditCardPayFlowDTO);
            creditCardPayFlowDTO.setPayAmount(creditNeedCardPayAmount);

            withholdFlowDTO = packageWithHoldFlow(creditCardPayFlowDTO, cardDTO, new BigDecimal((String) amountResultMap.get("cardPayFee")),  (BigDecimal) amountResultMap.get("cardPayRateReal"), gatewayDTO);
            withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        } catch (Exception e) {
            log.error("????????????????????????????????????:"+e.getMessage(),e);
            //????????????????????????
            //?????? ????????????
            creditChannelLimitAndBatchAmountRollBack(qrPayFlowDTO, creditNeedCardPayNoFeeAmount, StaticDataEnum.TRANS_STATE_12.getCode(),request);

            //?????????????????????
            creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId(), remainingCreditAmount, request);
            //????????????
            oneMarketingRollback(qrPayFlowDTO,request);

            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }


        if(Integer.parseInt(qrPayFlowDTO.getGatewayId()+"") == StaticDataEnum.GATEWAY_TYPE_0.getCode()){
            //??????????????????
            sendCardPayLatPayRequest(qrPayFlowDTO, withholdFlowDTO, cardJsonObj, request);
        }else{

            Map<String,Object> urlMap = sendStripeRequest(qrPayFlowDTO, withholdFlowDTO, cardDTO, request);
            if(urlMap != null && urlMap.size() > 0) {
                //?????????3DS
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
                updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
                return urlMap;
            }
        }


        //????????????????????????????????????
        handleCreditCardPayDataByThirdStatusV3(withholdFlowDTO, qrPayFlowDTO, cardJsonObj, creditNeedCardPayAmount, creditNeedCardPayNoFeeAmount,
                remainingCreditAmount, cardPayRate, cardPayFee, request);

        return amountResultMap;
    }
    // ???????????????
    @Override
    public void oneMarketingRollback(QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException {

        try {
            //??????????????????
            Map<String,Object> params =  new HashMap<>();
            params.put("flowId",qrPayFlowDTO.getId());
            params.put("transType",StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode());
//            params.put("state",StaticDataEnum.TRANS_STATE_1.getCode());
            MarketingFlowDTO marketingFlowDTO = marketingFlowService.findOneMarketingFlow(params);
            if(marketingFlowDTO == null || marketingFlowDTO.getId() == null){
                return;
            }
            // ?????????????????????????????????
            marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_4.getCode());
            marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(),marketingFlowDTO,request);
            // ????????????????????????
            serverService.marketingRollBack(marketingFlowDTO.getId());
            marketingFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(),marketingFlowDTO,request);
        }catch ( Exception e){
            log.error("useOneMarketingRollback id:"+ qrPayFlowDTO.getId()+",exception"+e.getMessage(),e);
        }
    }

    private boolean useOneMarketing(QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException {
        //????????????
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
     * ????????? ?????? ?????? ??????
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
     *  ?????????????????????
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

        //????????????
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
            log.error("???????????????????????????" + flowId, e.getMessage());
        }
        return result;
    }

    /**
     *  ???????????????????????????
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
        //?????????????????????????????????
        //????????????????????????
        PayCreditBalanceFlowDTO payCreditBalanceFlowDTO = new PayCreditBalanceFlowDTO();
        payCreditBalanceFlowDTO.setQrPayFlowId(flowId);
        payCreditBalanceFlowDTO.setOperateType(StaticDataEnum.PAY_BALANCE_FLOW_OPERATE_TYPE_2.getCode());
        payCreditBalanceFlowDTO.setUserId(userId);
        payCreditBalanceFlowDTO.setCreditQuotaAmount(transAmount);
        payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_0.getCode());
        Long id = payCreditBalanceFlowService.savePayCreditBalanceFlow(payCreditBalanceFlowDTO, request);

        String url = creditMerchantUrl + "/payremote/user/userAmountRollback";

        //????????????
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
                //????????????????????????
                payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_1.getCode());
                payCreditBalanceFlowService.updatePayCreditBalanceFlow(id,  payCreditBalanceFlowDTO, request);
            }else {
                payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_3.getCode());
                payCreditBalanceFlowService.updatePayCreditBalanceFlow(id,  payCreditBalanceFlowDTO, request);
            }
        }catch (Exception e){
            //????????????????????? ????????????
            payCreditBalanceFlowDTO.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_3.getCode());
            payCreditBalanceFlowService.updatePayCreditBalanceFlow(id,  payCreditBalanceFlowDTO, request);
        }
    }



    /**
     * ????????????
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
        // ????????????(???????????????)
        if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0 && qrPayFlowDTO.getMarketingId()  == null ){
            // ??????????????????
            AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
            accountFlowDTO.setFlowId(qrPayFlowDTO.getId());
            accountFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
            accountFlowDTO.setAccountType(StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
            accountFlowDTO.setTransAmount(qrPayFlowDTO.getRedEnvelopeAmount());
            accountFlowDTO.setOrderNo(orderNo);
            accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode());
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
            accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO, request));

            // ??????????????????????????????
            JSONObject json1 = new JSONObject();
            json1.put("serialNumber", accountFlowDTO.getId());
            json1.put("transAmount", qrPayFlowDTO.getRedEnvelopeAmount());
            json1.put("userId", qrPayFlowDTO.getPayUserId());
            json1.put("subAccountType", StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
            // ???????????????0????????????1????????????
            json1.put("transDirection", StaticDataEnum.DIRECTION_1.getCode());
            json1.put("channelTransType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode());
            jsonArray.add(json1);
            amountOutTotal = amountOutTotal.add(qrPayFlowDTO.getRedEnvelopeAmount());
        }
        // ??????????????????
        if(qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
            // ??????????????????
            accountFlowDTO.setFlowId(qrPayFlowDTO.getId());
            accountFlowDTO.setUserId(qrPayFlowDTO.getRecUserId());
            accountFlowDTO.setAccountType(StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
            accountFlowDTO.setTransAmount(qrPayFlowDTO.getWholeSalesAmount());
            accountFlowDTO.setOrderNo(orderNo);
            accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_26.getCode());
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
            accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO, request));

            // ????????????
            JSONObject json2 = new JSONObject();
            json2.put("serialNumber", accountFlowDTO.getId());
            json2.put("transAmount",  qrPayFlowDTO.getWholeSalesAmount());
            json2.put("userId", qrPayFlowDTO.getRecUserId());
            json2.put("subAccountType", StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
            // ???????????????0????????????1????????????
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
            //??????????????????
            JSONObject result = null;
            try {
                result =serverService.batchChangeBalance(jsonObject);
            }catch (Exception e){
                log.error("doBatchAmountOut orderNo:" +orderNo+ ",Exception,errorMessage:"+e.getMessage()+",message:"+e);
                throw e;
            }
            //??????????????????
            if(result != null && "2".equals(result.getString("errorState"))){
                // ???????????????????????????
                AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
                accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                accountFlowService.updateAccountFlowByOrderNo(orderNo,accountFlowDTO,request);
                return false;
            }else{
                // ???????????????????????????
                AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
                accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                accountFlowService.updateAccountFlowByOrderNo(orderNo,accountFlowDTO,request);
                // ??????????????????
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
            //????????????????????????
            GatewayDTO gatewayDTO = getGateWay(qrPayDTO,qrPayFlowDTO,request);
            if(gatewayDTO==null ||gatewayDTO.getId()==null){
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            result.put("gateWay",gatewayDTO);
            qrPayFlowDTO.setGatewayId(gatewayDTO.getType().longValue());
//            gatewayDTO = doRouteConfig(recUser,qrPayFlowDTO,qrPayDTO,request);
            //?????????????????????????????????????????????
            //???????????????
            JSONObject cardObj;
            try {
                cardObj = serverService.getCardInfo(qrPayDTO.getCardId());
            } catch (Exception e) {
                //????????????????????????
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            if (cardObj == null) {
                //??????????????????????????????
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            //?????????
            CardDTO cardDTO = JSONObject.parseObject(cardObj.toJSONString(), CardDTO.class);
            result.put("cardList",cardObj);
            qrPayFlowDTO.setCardId(qrPayDTO.getCardId().toString());
            //???????????????????????????????????????????????????
//            if(StaticDataEnum.GATEWAY_TYPE_0.getCode()==gatewayDTO.getType()||StaticDataEnum.GATEWAY_TYPE_4.getCode()==gatewayDTO.getType()){
//                //Latpay??????integrapay
//                //????????????,??????????????????????????????
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

            //?????????LatPay???????????????token?????????????????????integrapay ??????uniqueReference?????????
            if(StaticDataEnum.GATEWAY_TYPE_0.getCode()==gatewayDTO.getType()){
                if(cardDTO.getCrdStrgToken() == null){
                    throw new BizException(I18nUtils.get("channel.token.missing", getLang(request)));
                }
//                Long now = System.currentTimeMillis();
//                // TODO ?????????????????????
//                if( now - 1616256000000L > 0 && cardDTO.getCardCategory() == null ){
//                    throw new BizException(I18nUtils.get("card.type.not.found", getLang(request)));
//                }
                // ??????????????????????????????
                userService.verifyCardFailedTime(qrPayFlowDTO.getPayUserId(),qrPayDTO.getCardId(),request);
            }else if(StaticDataEnum.GATEWAY_TYPE_4.getCode()==gatewayDTO.getType()){
                if(cardDTO.getUniqueReference() == null){
                    throw new BizException(I18nUtils.get("channel.token.missing", getLang(request)));
                }
            }else{
                //???????????????
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            //?????????
            if (recUser.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
                //??????????????????????????????
                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_4.getCode());
            } else {
                //?????????
                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode());
            }
        }
        else if (StaticDataEnum.PAY_TYPE_4.getCode() == (qrPayDTO.getPayType())){
            qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());
            qrPayFlowDTO.setProductId(qrPayDTO.getProductId());
            //?????????????????????????????????????????????????????????????????????
            if(payUser.getInstallmentState() != StaticDataEnum.STATUS_1.getCode() || payUser.getSplitAddInfoState() == StaticDataEnum.STATUS_1.getCode()){
                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
            }
            //???????????????????????????
            if(recUser.getUserType()!=StaticDataEnum.USER_TYPE_20.getCode()){
                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
            }

        }else{
            //?????????????????????
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }


        return result;
    }

    @Override
    public Object interestCredistOrder(QrPayDTO qrPayDTO, HttpServletRequest request) throws Exception {
        //??????????????????user??????
//        UserDTO payUser = userService.findUserById(qrPayDTO.getPayUserId());
//        UserDTO recUser = userService.findUserById(qrPayDTO.getRecUserId());
//
//        //????????????????????????
//        if (payUser.getId() == null || recUser.getId() == null) {
//            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
//        }
//
//        //????????????????????????
//        if (!checkAccountState(payUser.getId())) {
//            throw new BizException(I18nUtils.get("account.error", getLang(request)));
//        }
//        if (!checkAccountState(recUser.getId())) {
//            throw new BizException(I18nUtils.get("account.error", getLang(request)));
//        }
//
//        //????????????
//        QrPayFlowDTO qrPayFlowDTO = new QrPayFlowDTO();
//        qrPayFlowDTO.setFee(qrPayDTO.getFeeAmt());
//        qrPayFlowDTO.setPayUserId(qrPayDTO.getPayUserId());
//        qrPayFlowDTO.setPayUserType(payUser.getUserType());
//        qrPayFlowDTO.setMerchantId(qrPayDTO.getMerchantId());
//        qrPayFlowDTO.setRecUserId(qrPayDTO.getRecUserId());
//        qrPayFlowDTO.setRecUserType(recUser.getUserType());
//        qrPayFlowDTO.setTransAmount(qrPayDTO.getTransAmount());
//        qrPayFlowDTO.setRemark(qrPayDTO.getRemark());
//        //?????????????????????,??????????????????
//        if(recUser.getUserType()==StaticDataEnum.USER_TYPE_20.getCode()){
//            qrPayFlowDTO =  getQrPayFlow(qrPayFlowDTO,qrPayDTO, null, request);
//        }

//        Map<String,Object> resMap = new HashMap<>();
        JSONObject result = null;
        if (StaticDataEnum.PAY_TYPE_4.getCode() == (qrPayDTO.getPayType())){
            //?????????
//            qrPayFlowDTO = checkCreditParams(qrPayFlowDTO,qrPayDTO, recUser, payUser,request);
            // ?????????????????????????????????
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
            //?????????????????????
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }
        return result;
    }

    private QrPayFlowDTO getQrPayFlow(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, GatewayDTO gatewayDTO,CardDTO cardDTO, HttpServletRequest request) throws Exception {

        //????????????????????????
        MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayDTO.getMerchantId());
        if(merchantDTO==null || merchantDTO.getId()==null ||  merchantDTO.getIsAvailable() !=1) {
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));

        }

        //?????????????????????????????????
        BigDecimal wholeSalesAmount = qrPayDTO.getWholeSalesAmount() == null ?  BigDecimal.ZERO : qrPayDTO.getWholeSalesAmount();
        BigDecimal normalSaleAmount = qrPayDTO.getTransAmount().subtract(qrPayDTO.getWholeSalesAmount());
        BigDecimal redEnvelopeAmount = qrPayDTO.getRedEnvelopeAmount() == null ? BigDecimal.ZERO : qrPayDTO.getRedEnvelopeAmount();
        //??????????????????
        BigDecimal wholeSaleUserDiscount = BigDecimal.ZERO;
        //??????????????????????????????
        BigDecimal merchantAmount = userService.getBalance(qrPayDTO.getRecUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
        //????????????
        if(normalSaleAmount.compareTo(BigDecimal.ZERO) > 0){
            // ????????????
            BigDecimal marketingDiscount = merchantDTO.getMarketingDiscount() == null ? BigDecimal.ZERO : merchantDTO.getMarketingDiscount();
            // ????????????
            BigDecimal baseDiscount ;
            // ????????????
            BigDecimal extraDiscount;

            if(qrPayDTO.getPayType() == StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode()){
                baseDiscount = merchantDTO.getBasePayRate() == null ? BigDecimal.ZERO : merchantDTO.getBasePayRate();
            }else{
                baseDiscount = merchantDTO.getBaseRate() == null ? BigDecimal.ZERO : merchantDTO.getBaseRate();
            }

            //?????????????????????????????????
            if(merchantAmount.compareTo(wholeSalesAmount) != 0 ){
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            //????????????????????????
            BigDecimal baseDiscountAmount = normalSaleAmount.multiply(baseDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
            //?????????????????????????????????
            Long today = System.currentTimeMillis();
            Long extraDiscountPeriod = merchantDTO.getExtraDiscountPeriod();
            if (today.longValue() < extraDiscountPeriod.longValue()) {
                extraDiscount = merchantDTO.getExtraDiscount();
            } else {
                extraDiscount = new BigDecimal("0.00");
            }
            BigDecimal extraDiscountAmount = normalSaleAmount.multiply(extraDiscount == null ? BigDecimal.ZERO :extraDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
            //????????????????????????
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
            // ??????????????????????????????
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
        // ???????????? = ?????????????????? + ????????????????????????????????? - ???????????? - ????????????????????? -??????????????? - ????????????
        BigDecimal payAmount = wholeSalesAmount.subtract(wholeSaleUserDiscountAmount).add(
                normalSaleAmount.subtract(qrPayFlowDTO.getBaseDiscountAmount())
                        .subtract(qrPayFlowDTO.getExtraDiscountAmount())
                        .subtract(qrPayFlowDTO.getMarkingDiscountAmount())
        );

        // ???????????????????????????????????????????????????????????????????????????
        if(qrPayDTO.getPayType() == StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode()){
            if(payAmount.compareTo(BigDecimal.ZERO) > 0){
                if(qrPayFlowDTO.getGatewayId() == StaticDataEnum.GATEWAY_TYPE_0.getCode()){
                    //?????????????????????LatPay
                    // ???????????????
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

        //???????????????
        BigDecimal platformFee = normalSaleAmount.multiply(merchantDTO.getAppChargeRate() == null ? BigDecimal.ZERO : merchantDTO.getAppChargeRate()).setScale(2,BigDecimal.ROUND_HALF_UP);
        //???????????? = ???????????? - ???????????? - ????????????????????? -???????????? - ???????????????
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
     *  ?????????????????????
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
        // ?????????????????????????????????
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

        creditInfo.put("cardPayFeeRate", cardPayRate);//???????????????
        if(qrPayFlowDTO.getPayAmount().compareTo(creditNeedCardPayNoFeeAmount) == 0){
            creditInfo.put("cardPayPercentage", 1);//???????????????
        }else{
            creditInfo.put("cardPayPercentage", Constant.PAY_CREDIT_NEED_CARDPAY_AMOUNT_RATE);//???????????????
        }

        creditInfo.put("cardPaySubtotal", creditNeedCardPayNoFeeAmount);//??????????????????(???????????????)
        creditInfo.put("cardPayFee", cardPayFee);//????????????????????????(???????????????*???????????????)
        creditInfo.put("cardPayTotal", creditNeedCardPayAmount);//??????????????????(????????????*25% + ?????????)
        creditInfo.put("cardId", qrPayFlowDTO.getCardId());//???????????? ??????????????????ID(u_card???)
        creditInfo.put("qrPayFlowId", qrPayFlowDTO.getId());//??????????????????ID(qr_pay_flow id)
        creditInfo.put("cardAccountName", cardObj.getString("accountName"));//??????????????????ID(qr_pay_flow id)
        creditInfo.put("cardNo", cardObj.getString("cardNo"));//??????????????????ID(qr_pay_flow id)
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

        // ??????????????????????????????
        Integer orderState = creditResult.getJSONObject("data").getInteger("transactionResult");

        if(null == orderState){
            //?????????????????????  ????????????
            return code;
        }

        return orderState;
    }



    private QrPayFlowDTO doCredit(QrPayFlowDTO qrPayFlowDTO,HttpServletRequest request) throws  Exception{
        // ?????????????????????????????????
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
            // ????????????????????????????????????????????????
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            qrPayFlowService.updateQrPayFlowForConcurrency(qrPayFlowDTO.getId(), qrPayFlowDTO, request);
            creditInfo.clear();
            creditInfo.put("tripartiteTransactionNo", qrPayFlowDTO.getId());
//            creditInfo.put("accessSideId", accessMerchantDTO.getPlatformId());
            serverService.orderStateRollback(creditInfo, request);
            doBatchAmountOutRollBack(qrPayFlowDTO,request);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        // ??????????????????????????????
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
        //????????????????????????
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
        //??????????????????
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
     * ?????????????????????
     * @param qrPayDTO
     * @param request
     * @param payUser
     * @param recUser
     * @param qrPayFlowDTO
     */
    private WithholdFlowDTO qrPayByAliPay(QrPayDTO qrPayDTO, HttpServletRequest request, UserDTO payUser, UserDTO recUser, QrPayFlowDTO qrPayFlowDTO) throws BizException {
        //??????????????????
        GatewayDTO gatewayDTO = getGateWay(qrPayDTO,qrPayFlowDTO,request);
        //????????????
        rechargeFlowService.channelLimit(gatewayDTO.getType(),qrPayFlowDTO.getPayAmount(),request);
        WithholdFlowDTO withholdFlowDTO = null;
        if(1==gatewayDTO.getType()){
            //OmiPay??????????????????
            withholdFlowDTO = getOmiPayWithholdFlowDTO(qrPayFlowDTO, request, BigDecimal.ZERO,gatewayDTO);
        }else{
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }
        //??????????????????????????????
        withholdFlowService.saveWithholdFlow(withholdFlowDTO, request);
        return withholdFlowDTO;

    }


    /**
     * ??????????????????
     * @param qrPayDTO
     * @param request
     * @param payUser
     * @param recUser
     * @param qrPayFlowDTO
     */
    private WithholdFlowDTO  qrPayByWechatPay(QrPayDTO qrPayDTO, HttpServletRequest request, UserDTO payUser, UserDTO recUser, QrPayFlowDTO qrPayFlowDTO) throws BizException {
        //??????????????????
        GatewayDTO gatewayDTO = getGateWay(qrPayDTO,qrPayFlowDTO,request);
        //????????????
        rechargeFlowService.channelLimit(gatewayDTO.getType(),qrPayFlowDTO.getPayAmount(),request);
        WithholdFlowDTO withholdFlowDTO = null;
        if(2==gatewayDTO.getType()){
            //Latpay
            withholdFlowDTO = getOmiPayWithholdFlowDTO(qrPayFlowDTO, request, BigDecimal.ZERO,gatewayDTO);
        }else{
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //??????????????????????????????
        withholdFlowService.saveWithholdFlow(withholdFlowDTO, request);
        return withholdFlowDTO;

    }

    /**
     * ???????????????
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
        //??????????????????
        WithholdFlowDTO withholdFlowDTO = null;
        try {
            //??????????????????
            //LatPay
            withholdFlowDTO = getLatPayWithholdFlowDTO(qrPayFlowDTO, request, cardDTO, qrPayFlowDTO.getFee(),gatewayDTO);
            //??????????????????????????????
            withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        } catch (Exception e) {
            log.error("qrPayByCard create withholdFlowDTO Exception:"+e.getMessage(),e);
            //????????????
            doQrPayRollBack(qrPayFlowDTO,StaticDataEnum.TRANS_STATE_12.getCode(),request);
            //???????????? ??????pos???????????? todo?

            if(request != null){
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }else{
                throw new BizException("We couldn't process your payment. Please try again.");
            }
        }

        if(withholdFlowDTO.getTransAmount().compareTo(BigDecimal.ZERO)>0){
            //??????????????????
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
                //??????????????????????????????
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
        //todo ????????????????????????
        /*StaticDataDTO staticDataById = staticDataService.findStaticDataById(989898L);
        if (null != staticDataById && null != staticDataById.getId()){
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
        }*/
        if (StaticDataEnum.TRANS_STATE_1.getCode() == withholdFlowDTO.getState()) {
            //????????????????????????
            dealThirdSuccess(withholdFlowDTO, qrPayFlowDTO, request);
        } else if (StaticDataEnum.TRANS_STATE_2.getCode() == withholdFlowDTO.getState()) {
            //??????
            dealThirdFail(withholdFlowDTO, qrPayFlowDTO, request);
            if(request != null){
                // ??????????????????????????????
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
                    // ??????????????????????????????
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
            //??????
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
                log.error("????????????????????????????????????????????????flowId:" + qrPayFlowDTO.getId());
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
            // ?????????????????????????????????
            if(payUser.getFirstDealState() == StaticDataEnum.STATUS_0.getCode()){
                userService.updateFirstDealState(payUser.getId());
            }
            // ????????????????????????,??????????????????
            if(payUser.getFirstDealState() == StaticDataEnum.STATUS_1.getCode() ||payUser.getInviterId() == null ){
                return;
            }
            // ????????????
            userService.walletBooked(payUser.getInviterId(), StaticDataEnum.ACC_FLOW_TRANS_TYPE_20.getCode(),null, request, qrPayFlowDTO.getId());
        }catch (Exception e){
            log.error("dealFirstDeal flowId:" +qrPayFlowDTO.getId()+ ",Exception:"+e.getMessage()+",message:"+e);
        }
    }


    @Override
    public JSONObject getQrPayTransAmount(JSONObject data, HttpServletRequest request) throws Exception {
        QrPayDTO qrPayDTO = JSONObject.parseObject(data.toJSONString(), QrPayDTO.class);

        //?????????????????????
        if (StringUtils.isEmpty(qrPayDTO.getTransAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.empty", getLang(request)));
        }

        //????????????????????????
        if (!RegexUtils.isTransAmt(qrPayDTO.getTransAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        //??????userId????????????
        if (StringUtils.isEmpty(qrPayDTO.getPayUserId().toString()) || StringUtils.isEmpty(qrPayDTO.getRecUserId().toString())) {
            throw new BizException(I18nUtils.get("userId.null", getLang(request)));
        }

        //??????????????????
        if (StringUtils.isEmpty(qrPayDTO.getPayType().toString())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }
        //??????????????????user??????
        UserDTO payUser = userService.findUserById(qrPayDTO.getPayUserId());
        UserDTO recUser = userService.findUserById(qrPayDTO.getRecUserId());

        //????????????????????????
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
        //????????????????????????
        BigDecimal payUserAmount = userService.getBalance(qrPayDTO.getPayUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
        //????????????????????????????????????
        BigDecimal merchantAmount = userService.getBalance(qrPayDTO.getRecUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());

        //??????????????????
        BigDecimal wholeSalesAmount = qrPayDTO.getTransAmount().min(merchantAmount);
        //??????????????????
        BigDecimal normalSaleAmount = qrPayDTO.getTransAmount().subtract(wholeSalesAmount);

        // ????????????
        BigDecimal marketingDiscount = merchantDTO.getMarketingDiscount() == null ? BigDecimal.ZERO : merchantDTO.getMarketingDiscount();
        // ????????????
        BigDecimal baseDiscount ;
        // ????????????
        BigDecimal extraDiscount;
        // ??????????????????
        BigDecimal wholeSaleUserDiscount;

        // ?????????????????????????????? ????????????0 ???????????? 4
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
        //????????????????????????
        BigDecimal baseDiscountAmount = normalSaleAmount.multiply(baseDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        //?????????????????????????????????
        Long today = System.currentTimeMillis();
        Long extraDiscountPeriod = merchantDTO.getExtraDiscountPeriod();
        if (today.longValue() < extraDiscountPeriod.longValue()) {
            extraDiscount = merchantDTO.getExtraDiscount();
        } else {
            extraDiscount = new BigDecimal("0.00");
        }
        BigDecimal extraDiscountAmount = normalSaleAmount.multiply(extraDiscount == null ? BigDecimal.ZERO :extraDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        // ????????????????????????
        BigDecimal markingDiscountAmount = normalSaleAmount.multiply(marketingDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);

        // ????????????????????????
        BigDecimal wholeSaleUserDiscountAmount = wholeSalesAmount.multiply(wholeSaleUserDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        // ????????????(?????????) = ?????????????????? + ????????????????????????????????? - ???????????? - ????????????????????? -???????????????
        BigDecimal payAmountNoRedEnvelope = wholeSalesAmount.subtract(wholeSaleUserDiscountAmount).add(
                normalSaleAmount.subtract(baseDiscountAmount).subtract(extraDiscountAmount).subtract(markingDiscountAmount)
        );
        // ????????????(?????????) = ?????????????????? + ????????????????????????????????? - ???????????? - ????????????????????? -??????????????? - ????????????
        BigDecimal redEnvelopeAmount = payAmountNoRedEnvelope.min(payUserAmount);
        BigDecimal payAmountRedEnvelope = payAmountNoRedEnvelope.subtract(redEnvelopeAmount);

        BigDecimal channelFeeRedEnvelope = BigDecimal.ZERO;
        BigDecimal channelFeeNoRedEnvelope = BigDecimal.ZERO;
        BigDecimal transChannelFeeRate = BigDecimal.ZERO;
        // ???????????????????????????????????????????????????????????????????????????
        if(qrPayDTO.getPayType() == StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode() ){
            // ??????????????????
            params.clear();
            params.put("gatewayType",qrPayDTO.getPayType());
            params.put("state",StaticDataEnum.STATUS_1.getCode());
            GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);
            if(gatewayDTO == null || gatewayDTO.getId() == null){
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            List<CardDTO> cardDTOList = new ArrayList<>();
            if(gatewayDTO.getType().toString() .equals(StaticDataEnum.GATEWAY_TYPE_0.getCode()+"") ){
                // ?????????LayPay,???????????????
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
                    //???????????????
                    JSONObject cardObj;
                    try {
                        cardObj = serverService.getCardInfo(qrPayDTO.getCardId());
                    } catch (Exception e) {
                        //????????????????????????
                        throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
                    }
                    if (cardObj == null) {
                        //??????????????????????????????
                        throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
                    }
                    //?????????
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
            // ??????
            transNoType = "TPC";
        }else if(qrPayDTO.getPayType() == StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode() ){
            // ?????????
            transNoType = "INS";
        }else{
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }
        if(normalSaleAmount.compareTo(BigDecimal.ZERO) > 0){
            if(wholeSalesAmount.compareTo(BigDecimal.ZERO)>0){
                // ????????????
                transNo = "M"  + transNoType + orderNumber;
                wholeSalesUserDiscount = wholeSaleUserDiscount;
                normalSalesUserDiscount = marketingDiscount.add(baseDiscount).add(extraDiscount);
            }else{
                // ????????????
                transNo = "B" + transNoType + orderNumber;
                wholeSalesUserDiscount = BigDecimal.ZERO;
                normalSalesUserDiscount = marketingDiscount.add(baseDiscount).add(extraDiscount);
            }
        }else{
            // ????????????
            transNo = "W" + transNoType + orderNumber;
            wholeSalesUserDiscount = wholeSaleUserDiscount;
            normalSalesUserDiscount = BigDecimal.ZERO;
        }

        //edit by zhangzeyuan ???????????????????????????pos??????????????????
        String posTransNo = qrPayDTO.getPosTransNo();
        if(StringUtils.isNotBlank(posTransNo) && Objects.isNull(qrPayDTO.getCardId())){
            //POS?????????????????? ?????? ???ID??????
            try {
                posQrPayFlowService.updateSysTransNoByThirdNo(transNo, posTransNo, getUserId(request), System.currentTimeMillis());
            }catch (Exception e){
                log.error("???????????????????????????pos????????????????????????");
                e.printStackTrace();
            }
        }

        Integer repeatSaleState = 0;
        // ???????????????????????????5????????????????????????????????????
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

        // ????????????
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

        //????????????
        BigDecimal transAmount = qrPayDTO.getTransAmount();
        //????????????id
        Long payUserId = qrPayDTO.getPayUserId();
        //????????????id
        Long recUserId = qrPayDTO.getRecUserId();
        //????????????
        Integer payType = qrPayDTO.getPayType();
        //??????id
        Long merchantId = qrPayDTO.getMerchantId();

        //??????????????????
        BigDecimal useRedEnvelopeAmount = BigDecimal.ZERO;
        if(Objects.nonNull(qrPayDTO.getRedEnvelopeAmount())){
            useRedEnvelopeAmount = qrPayDTO.getRedEnvelopeAmount();
        }

        //????????????
        BigDecimal tipAmount = BigDecimal.ZERO;
        if(Objects.nonNull(qrPayDTO.getTipAmount())){
            tipAmount = qrPayDTO.getTipAmount();
        }


        //????????????
        //?????????????????????
        if (StringUtils.isEmpty(transAmount.toString()) || !RegexUtils.isTransAmt(transAmount.toString())) {
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        //??????userId????????????
        if (StringUtils.isEmpty(payUserId.toString()) || StringUtils.isEmpty(recUserId.toString())) {
            throw new BizException(I18nUtils.get("userId.null", getLang(request)));
        }

        //??????????????????
        if (StringUtils.isEmpty(payType.toString())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //????????????ID
        if (Objects.isNull(merchantId)) {
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //??????????????????user??????
        UserDTO payUser = userService.findUserInfoV2(qrPayDTO.getPayUserId());
        UserDTO recUser = userService.findUserInfoV2(qrPayDTO.getRecUserId());

        //????????????????????????
        if (payUser == null || recUser == null || payUser.getId() == null || recUser.getId() == null) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }

        //??????????????????
        Map<String,Object> params = new HashMap<>();

        params.put("id", merchantId);
        params.put("userId",qrPayDTO.getRecUserId());
        params.put("isAvailable", StaticDataEnum.AVAILABLE_1.getCode());
        MerchantDTO merchantDTO = merchantService.findOneMerchant(params);
        if(merchantDTO == null || merchantDTO .getId() == null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode())){
            //?????????
            return getCardPayTransAmountDetail(qrPayDTO.getTransNo(), transAmount, payUserId, recUserId, payType, merchantId, useRedEnvelopeAmount, payUser,
                    qrPayDTO.getCardId(), qrPayDTO.getPosTransNo(), merchantDTO, qrPayDTO.getDonationInstiuteId() , tipAmount, request);
        }else if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){
            //?????????
            return getCreditPayTransAmountDetail(qrPayDTO.getTransNo(), transAmount, payUserId, recUserId, payType, merchantId, useRedEnvelopeAmount, payUser,
                    qrPayDTO.getCardId(), qrPayDTO.getPosTransNo(), merchantDTO, qrPayDTO.getDonationInstiuteId(), tipAmount, request);
        }else{
            //??????????????????
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }
    }



    @Override
    public JSONObject getPayTransAmountDetailV3(@NonNull JSONObject data, HttpServletRequest request) throws Exception {
        QrPayDTO qrPayDTO = JSONObject.parseObject(data.toJSONString(), QrPayDTO.class);

        //????????????
        BigDecimal transAmount = qrPayDTO.getTransAmount();
        //????????????id
        Long payUserId = qrPayDTO.getPayUserId();
        //????????????id
        Long recUserId = qrPayDTO.getRecUserId();
        //????????????
        Integer payType = qrPayDTO.getPayType();
        //??????id
        Long merchantId = qrPayDTO.getMerchantId();

        //????????????
        BigDecimal tipAmount = BigDecimal.ZERO;
        if(Objects.nonNull(qrPayDTO.getTipAmount())){
            if (!RegexUtils.isTransAmt(qrPayDTO.getTipAmount().toString())) {
                throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
            }
            tipAmount = qrPayDTO.getTipAmount();
        }

        //??????????????????
        BigDecimal useRedEnvelopeAmount = BigDecimal.ZERO;

        //????????????
        //?????????????????????
        if (StringUtils.isEmpty(transAmount.toString()) || !RegexUtils.isTransAmt(transAmount.toString())) {
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        //??????userId????????????
        if (StringUtils.isEmpty(payUserId.toString()) || StringUtils.isEmpty(recUserId.toString())) {
            throw new BizException(I18nUtils.get("userId.null", getLang(request)));
        }

        //??????????????????
        if (StringUtils.isEmpty(payType.toString())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }

        //????????????ID
        if (Objects.isNull(merchantId)) {
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //??????????????????user??????
        UserDTO payUser = userService.findUserInfoV2(qrPayDTO.getPayUserId());
//        UserDTO recUser = userService.findUserInfoV2(qrPayDTO.getRecUserId());

        //????????????????????????
        if (payUser == null || payUser.getId() == null) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }


        if (null != qrPayDTO.getRedEnvelopeAmount()) {
            useRedEnvelopeAmount = qrPayDTO.getRedEnvelopeAmount();
        }

        //??????????????????
        Map<String,Object> params = new HashMap<>();

        params.put("id", merchantId);
        params.put("userId",qrPayDTO.getRecUserId());
        params.put("isAvailable", StaticDataEnum.AVAILABLE_1.getCode());
        MerchantDTO merchantDTO = merchantService.findOneMerchant(params);
        if(merchantDTO == null || merchantDTO .getId() == null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode())){
            //?????????
            return getCardPayTransAmountDetailV3(qrPayDTO.getTransNo(), transAmount, payUserId, recUserId, payType, merchantId, useRedEnvelopeAmount, payUser,
                    qrPayDTO.getCardId(), qrPayDTO.getPosTransNo(), merchantDTO, qrPayDTO.getDonationInstiuteId() , tipAmount, request);
        }else if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){
            //?????????
            return getCreditPayTransAmountDetailV3(qrPayDTO.getTransNo(), transAmount, payUserId, recUserId, payType, merchantId, useRedEnvelopeAmount, payUser,
                    qrPayDTO.getCardId(), qrPayDTO.getPosTransNo(), merchantDTO, qrPayDTO.getDonationInstiuteId(), tipAmount, request);
        }else{
            //??????????????????
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }
    }


    /**
     * ?????????????????????????????????
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
        //????????????
        JSONObject result = new JSONObject();

        //?????????????????????
        if(!payUser.getCardState().equals(StaticDataEnum.USER_CARD_STATE_BINDED.getCode())){
            throw new BizException(I18nUtils.get("pay.card.not.bound", getLang(request)));
        }

        //????????????????????????0
        if(transAmount.compareTo(BigDecimal.ZERO) <= 0){
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        //??????????????????????????????
        BigDecimal realPayAmount = getTransactionTruelyPayAmount(transNo, transAmount, payUserId, recUserId, useRedEnvelopeAmount,
                merchantDTO, payType, result, Constant.ORDER_TRANS_TYPE_CARD_FLAG, request);

        //????????????
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

        //????????????????????????????????????
        JSONArray tipList = getTipListByOrderAmount(transAmount);
        result.put("tipList", tipList);


        //?????????????????????????????????  ?????? + ???????????????????????? + ?????????????????????
        BigDecimal calculateFeeAmount = realPayAmount.add(donationAmount).add(tipAmount);

        //?????????????????????
        BigDecimal channelFeeAmount = BigDecimal.ZERO;
        //????????????
        BigDecimal transChannelFeeRate = BigDecimal.ZERO;

        //??????????????????
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("gatewayType", payType);
        params.put("state", StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);

        if(gatewayDTO == null || gatewayDTO.getId() == null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //????????????????????????????????????
        CardDTO cardInfo = getCardInfoById(cardId, payUserId, request);
        result.put("cardId", cardInfo.getId().toString());
        result.put("cardNo", cardInfo.getCardNo());
        result.put("cardCcType", cardInfo.getCustomerCcType());

        if(calculateFeeAmount.compareTo(BigDecimal.ZERO) > 0){
            //?????????????????????
            Map<String, Object> feeMap = getCardPayTransactionFee(payType, cardInfo.getCustomerCcType(), calculateFeeAmount, request);

            channelFeeAmount = (BigDecimal) feeMap.get("channelFeeAmount");
            transChannelFeeRate = (BigDecimal) feeMap.get("transChannelFeeRate");
        }

        // POS??????????????? POS?????????????????? ?????? ???ID?????? ???????????????????????????pos??????????????????
        if(StringUtils.isNotBlank(posTransNumber)){
            posQrPayFlowService.updateSysTransNoByThirdNo(result.getString("transNo"), posTransNumber, getUserId(request), System.currentTimeMillis());
        }

        // ???????????????????????????5????????????????????????????????????
        Integer repeatSaleState = getPayUnSuccessStateInFiveMin(merchantId, transAmount, payUserId, posTransNumber);

        //???????????? + ????????? + ?????? + ??????????????????
        result.put("allPayAmount", calculateFeeAmount.add(channelFeeAmount).toString());
        //????????????
        result.put("transAmount", transAmount.toString());

        //?????????????????? + ???????????? + ??????
        result.put("toShowFeeAllAmount",  calculateFeeAmount.toString());

        //????????? ?????? ??? ???????????????
        result.put("payAmount", realPayAmount.toString());
        //???????????????
        result.put("channelFeeAmount", channelFeeAmount.toString());
        //??????????????????
        result.put("transChannelFeeRate", transChannelFeeRate.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        result.put("repeatSaleState", repeatSaleState.toString());
        //???????????????????????????
        result.put("mixPayAmount", "0");

        //????????????
        result.put("donationAmount", donationAmount.toString());
        //????????????
        result.put("tipAmount", tipAmount.toString());

        return result;
    }


    private JSONObject getCardPayTransAmountDetailV3(String transNo, BigDecimal transAmount, Long payUserId, Long recUserId,
                                                     Integer payType, Long merchantId, BigDecimal useRedEnvelopeAmount,
                                                     UserDTO payUser, Long cardId, String posTransNumber, MerchantDTO merchantDTO, String donationInstiuteId,
                                                     BigDecimal tipAmount, HttpServletRequest request)throws Exception{
        //????????????
        JSONObject result = new JSONObject();

        //?????????????????????
        /*if(!payUser.getCardState().equals(StaticDataEnum.USER_CARD_STATE_BINDED.getCode())){
            throw new BizException(I18nUtils.get("pay.card.not.bound", getLang(request)));
        }*/

        //????????????????????????0
        if(transAmount.compareTo(BigDecimal.ZERO) <= 0){
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        //??????????????????????????????
        BigDecimal realPayAmount = getTransactionTruelyPayAmountV3(transNo, transAmount, payUserId, recUserId, useRedEnvelopeAmount,
                merchantDTO, payType, result, Constant.ORDER_TRANS_TYPE_CARD_FLAG, request);

        //????????????
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

        //????????????????????????????????????
        JSONArray tipList = getTipListByOrderAmount(transAmount);
        result.put("tipList", tipList);


        //?????????????????????????????????  ?????? + ???????????????????????? + ?????????????????????
        BigDecimal calculateFeeAmount = realPayAmount.add(donationAmount).add(tipAmount);

        /*//?????????????????????
        BigDecimal channelFeeAmount = BigDecimal.ZERO;
        //????????????
        BigDecimal transChannelFeeRate = BigDecimal.ZERO;

        //??????????????????
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("gatewayType", payType);
        params.put("state", StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);

        if(gatewayDTO == null || gatewayDTO.getId() == null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }*/

        //????????????????????????????????????
        CardDTO cardInfo = getCardInfoById(cardId, payUserId, request);
        result.put("cardId", Objects.isNull(cardInfo.getId()) ? "" :  cardInfo.getId().toString());
        result.put("cardNo", StringUtils.isNotBlank(cardInfo.getCardNo()) ? cardInfo.getCardNo() : "");
        result.put("cardCcType", StringUtils.isNotBlank(cardInfo.getCustomerCcType()) ? cardInfo.getCustomerCcType() : "");
        result.put("customerCcExpyr", StringUtils.isNotBlank(cardInfo.getCustomerCcExpyr()) ? cardInfo.getCustomerCcExpyr() : "");
        result.put("customerCcExpmo", StringUtils.isNotBlank(cardInfo.getCustomerCcExpmo()) ? cardInfo.getCustomerCcExpmo() : "");
        /*if(calculateFeeAmount.compareTo(BigDecimal.ZERO) > 0){
            //?????????????????????
            Map<String, Object> feeMap = getCardPayTransactionFee(payType, cardInfo.getCustomerCcType(), calculateFeeAmount, request);

            channelFeeAmount = (BigDecimal) feeMap.get("channelFeeAmount");
            transChannelFeeRate = (BigDecimal) feeMap.get("transChannelFeeRate");
        }
*/
        // POS??????????????? POS?????????????????? ?????? ???ID?????? ???????????????????????????pos??????????????????
        /*if(StringUtils.isNotBlank(posTransNumber)){
            posQrPayFlowService.updateSysTransNoByThirdNo(result.getString("transNo"), posTransNumber, getUserId(request), System.currentTimeMillis());
        }*/

        // ???????????????????????????5????????????????????????????????????
        Integer repeatSaleState = getPayUnSuccessStateInFiveMin(merchantId, transAmount, payUserId, posTransNumber);

        //???????????? + ????????? + ?????? + ??????????????????
        result.put("allPayAmount", calculateFeeAmount.toString());
        //????????????
        result.put("transAmount", transAmount.toString());

        //?????????????????? + ???????????? + ??????
        result.put("toShowFeeAllAmount",  calculateFeeAmount.toString());

        //????????? ?????? ??? ???????????????
        result.put("payAmount", realPayAmount.toString());
        /*//???????????????
        result.put("channelFeeAmount", channelFeeAmount.toString());
        //??????????????????
        result.put("transChannelFeeRate", transChannelFeeRate.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
*/
        result.put("repeatSaleState", repeatSaleState.toString());
        //???????????????????????????
        result.put("mixPayAmount", "0");

        //????????????
        result.put("donationAmount", donationAmount.toString());
        //????????????
        result.put("tipAmount", tipAmount.toString());

        return result;
    }


    /**
     *
     * @author zhangzeyuan
     * @date 2021/6/23 16:51
     * @param transAmount ????????????
     * @param payUserId ????????????ID
     * @param recUserId ????????????Id
     * @param useRedEnvelopeAmount ??????????????????
     * @param merchantDTO ????????????
     * @param payType ????????????
     * @param result ????????????
     * @param request
     * @return java.math.BigDecimal
     */
    private BigDecimal getTransactionTruelyPayAmount(String transNo, BigDecimal transAmount, Long payUserId, Long recUserId,
                                                     BigDecimal useRedEnvelopeAmount, MerchantDTO merchantDTO, Integer payType,
                                                     JSONObject result, String orderNumberTransTypeFlag, HttpServletRequest request) throws Exception{

        BigDecimal payUserAmount = BigDecimal.ZERO;
        //????????????????????????????????????
        BigDecimal merchantAmount = BigDecimal.ZERO;
        //???????????? ???????????? ????????????
        BigDecimal baseDiscount = BigDecimal.ZERO;
        BigDecimal marketingDiscount = BigDecimal.ZERO;
        BigDecimal extraDiscount = BigDecimal.ZERO;
        //??????????????????
        BigDecimal wholeSaleDiscount = BigDecimal.ZERO;


        Map<?, Object> amountMap = null;

        //????????????redis????????????
        boolean putDataRedisStatus = false;

        if(StringUtils.isBlank(transNo)){
            //??????????????????????????? ?????????????????????????????? ????????????
            amountMap = getOrderDiscount(payUserId, recUserId, merchantDTO, payType);

            putDataRedisStatus = true;
        }else {
            //???????????????????????? ???redis??????
            amountMap = redisUtils.hmget(Constant.getOrderAmountDetailRedisKey(payUserId.toString(), transNo));
            if(null == amountMap || amountMap.isEmpty()){
                //??????????????????
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

        //?????? ?????????????????? ???????????? ??????????????????
        if(useRedEnvelopeAmount.compareTo(payUserAmount) > 0){
            throw new BizException(I18nUtils.get("pay.red.envelope.over.limit", getLang(request)));
        }

        //????????????????????????
        BigDecimal useWholeSalesAmount = tempTransAmount.min(merchantAmount);

        //????????????????????????
        BigDecimal useNormalSaleAmount = tempTransAmount.subtract(useWholeSalesAmount);

        //??????????????????????????????
        BigDecimal wholeSaleUseRedAmount = BigDecimal.ZERO;

        if(useRedEnvelopeAmount.compareTo(BigDecimal.ZERO) > 0){
            if(useWholeSalesAmount.compareTo(useRedEnvelopeAmount) >= 0){
                wholeSaleUseRedAmount = useRedEnvelopeAmount;
            }else {
                wholeSaleUseRedAmount = useWholeSalesAmount;
            }
        }
        //??????????????????????????????
        BigDecimal normalSaleUesRedAmount = useRedEnvelopeAmount.subtract(wholeSaleUseRedAmount);

        //????????????????????????  ?????????????????? +  ?????????????????? + ??????????????????
        BigDecimal baseDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(baseDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal extraDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(extraDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal marketingDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(marketingDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);

        BigDecimal normalSaleDiscountAmount = baseDiscountAmount.add(extraDiscountAmount).add(marketingDiscountAmount);

        // ???????????????????????? (?????????????????? - ?????????????????????????????? ???* ??????????????????
        BigDecimal wholeSaleDiscountAmount = (useWholeSalesAmount.subtract(wholeSaleUseRedAmount)).multiply(wholeSaleDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);

        // ???????????? = ????????????????????? - ???????????????????????? - ???????????????????????????  + ????????????????????? - ????????????????????????  -  ?????????????????????
        BigDecimal realPayAmount = useWholeSalesAmount.subtract(wholeSaleUseRedAmount).subtract(wholeSaleDiscountAmount)
                .add(useNormalSaleAmount.subtract(normalSaleUesRedAmount).subtract(normalSaleDiscountAmount));


        //???????????????????????????
        String orderNumberFlag = "";

        //????????????
        BigDecimal realWholeSaleDiscount;
        //??????????????????
        BigDecimal realNormalSaleDiscount;

        //????????????????????????????????????
        if(useNormalSaleAmount.compareTo(BigDecimal.ZERO) > 0){
            realNormalSaleDiscount = baseDiscount.add(extraDiscount).add(marketingDiscount);
            //???????????????????????????
            if(useWholeSalesAmount.compareTo(BigDecimal.ZERO) > 0){
                // ????????????
                orderNumberFlag = "M";
                realWholeSaleDiscount = wholeSaleDiscount;
            }else{
                // ????????????
                orderNumberFlag = "B";
                realWholeSaleDiscount = BigDecimal.ZERO;
            }
        }else{
            //????????????
            orderNumberFlag = "W";
            realWholeSaleDiscount = wholeSaleDiscount;
            realNormalSaleDiscount = BigDecimal.ZERO;
        }

        if(StringUtils.isBlank(transNo)){
            //???????????????
            transNo = orderNumberFlag + orderNumberTransTypeFlag + SnowflakeUtil.generateId().toString();
        }

        //???????????????
        result.put("transNo", transNo);

        //????????? ?????? ??? ???????????????
        result.put("payAmount", realPayAmount);

        //?????????????????????
        result.put("totalRedEnvelopeAmount", payUserAmount.toString());

        //???????????????????????????
        result.put("redEnvelopeAmount", useRedEnvelopeAmount.toString());

        //??????????????????
        result.put("wholeSalesAmount", useWholeSalesAmount.toString());
        //????????????????????????
        result.put("wholeSaleUserDiscountAmount", wholeSaleDiscountAmount.toString());
        //??????????????????
        result.put("wholeSalesUserDiscount", realWholeSaleDiscount.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        //??????????????????
        result.put("normalSaleAmount", useNormalSaleAmount.toString());
        //????????????????????????
        result.put("normalSaleUserDiscountAmount", normalSaleDiscountAmount.toString());
        //??????????????????
        result.put("normalSalesUserDiscount", realNormalSaleDiscount.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        //???????????????????????????????????????????????????redis
        if(putDataRedisStatus){
            redisUtils.hmset(Constant.getOrderAmountDetailRedisKey(payUserId.toString(), transNo), (Map<String, Object>) amountMap, 300);
        }

        return realPayAmount;
    }

    /**
     *
     * @author zhoutt
     * @date 2021/10/28 16:51
     * @param transAmount ????????????
     * @param payUserId ????????????ID
     * @param recUserId ????????????Id
     * @param useRedEnvelopeAmount ??????????????????
     * @param merchantDTO ????????????
     * @param payType ????????????
     * @param result ????????????
     * @param request
     * @return java.math.BigDecimal
     */
    private BigDecimal getTransactionTruelyPayAmountV3(String transNo, BigDecimal transAmount, Long payUserId, Long recUserId,
                                                       BigDecimal useRedEnvelopeAmount, MerchantDTO merchantDTO, Integer payType,
                                                       JSONObject result, String orderNumberTransTypeFlag, HttpServletRequest request) throws Exception{

        BigDecimal payUserAmount = BigDecimal.ZERO;
        //????????????????????????????????????
        BigDecimal merchantAmount = BigDecimal.ZERO;
        //???????????? ???????????? ????????????
        BigDecimal baseDiscount = BigDecimal.ZERO;
        BigDecimal marketingDiscount = BigDecimal.ZERO;
        BigDecimal extraDiscount = BigDecimal.ZERO;
        //??????????????????
        BigDecimal wholeSaleDiscount = BigDecimal.ZERO;


        Map<?, Object> amountMap = null;

        //????????????redis????????????
        boolean putDataRedisStatus = false;

        if(StringUtils.isBlank(transNo)){
            //??????????????????????????? ?????????????????????????????? ????????????
            amountMap = getOrderDiscount(payUserId, recUserId, merchantDTO, payType);

            putDataRedisStatus = true;
        }else {
            //???????????????????????? ???redis??????
            amountMap = redisUtils.hmget(Constant.getOrderAmountDetailRedisKey(payUserId.toString(), transNo));
            if(null == amountMap || amountMap.isEmpty()){
                //??????????????????
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

        // ?????????????????????????????????????????????,?????????????????????????????????
        BigDecimal realUseRedEnvelopeAmount = useRedEnvelopeAmount.min(transAmount);

        //?????? ?????????????????? ???????????? ??????????????????
//        if(useRedEnvelopeAmount.compareTo(payUserAmount) > 0){
//            throw new BizException(I18nUtils.get("pay.red.envelope.over.limit", getLang(request)));
//        }

        //????????????????????????
        BigDecimal useWholeSalesAmount = tempTransAmount.min(merchantAmount);

        //????????????????????????
        BigDecimal useNormalSaleAmount = tempTransAmount.subtract(useWholeSalesAmount);

        //??????????????????????????????
        BigDecimal wholeSaleUseRedAmount = BigDecimal.ZERO;

        if(realUseRedEnvelopeAmount.compareTo(BigDecimal.ZERO) > 0){
            if(useWholeSalesAmount.compareTo(realUseRedEnvelopeAmount) >= 0){
                wholeSaleUseRedAmount = realUseRedEnvelopeAmount;
            }else {
                wholeSaleUseRedAmount = useWholeSalesAmount;
            }
        }
        //??????????????????????????????
        BigDecimal normalSaleUesRedAmount = realUseRedEnvelopeAmount.subtract(wholeSaleUseRedAmount);

        //????????????????????????  ?????????????????? +  ?????????????????? + ??????????????????
        BigDecimal baseDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(baseDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal extraDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(extraDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
        BigDecimal marketingDiscountAmount = (useNormalSaleAmount.subtract(normalSaleUesRedAmount)).multiply(marketingDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);

        BigDecimal normalSaleDiscountAmount = baseDiscountAmount.add(extraDiscountAmount).add(marketingDiscountAmount);

        // ???????????????????????? (?????????????????? - ?????????????????????????????? ???* ??????????????????
        BigDecimal wholeSaleDiscountAmount = (useWholeSalesAmount.subtract(wholeSaleUseRedAmount)).multiply(wholeSaleDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);

        // ???????????? = ????????????????????? - ???????????????????????? - ???????????????????????????  + ????????????????????? - ????????????????????????  -  ?????????????????????
        BigDecimal realPayAmount = useWholeSalesAmount.subtract(wholeSaleUseRedAmount).subtract(wholeSaleDiscountAmount)
                .add(useNormalSaleAmount.subtract(normalSaleUesRedAmount).subtract(normalSaleDiscountAmount));


        //???????????????????????????
        String orderNumberFlag = "";

        //????????????
        BigDecimal realWholeSaleDiscount;
        //??????????????????
        BigDecimal realNormalSaleDiscount;

        //????????????????????????????????????
        if(useNormalSaleAmount.compareTo(BigDecimal.ZERO) > 0){
            realNormalSaleDiscount = baseDiscount.add(extraDiscount).add(marketingDiscount);
            //???????????????????????????
            if(useWholeSalesAmount.compareTo(BigDecimal.ZERO) > 0){
                // ????????????
                orderNumberFlag = "M";
                realWholeSaleDiscount = wholeSaleDiscount;
            }else{
                // ????????????
                orderNumberFlag = "B";
                realWholeSaleDiscount = BigDecimal.ZERO;
            }
        }else{
            //????????????
            orderNumberFlag = "W";
            realWholeSaleDiscount = wholeSaleDiscount;
            realNormalSaleDiscount = BigDecimal.ZERO;
        }

        if(StringUtils.isBlank(transNo)){
            //???????????????
            transNo = orderNumberFlag + orderNumberTransTypeFlag + SnowflakeUtil.generateId().toString();
        }

        //???????????????
        result.put("transNo", transNo);

        //????????? ?????? ??? ???????????????
        result.put("payAmount", realPayAmount);

        //?????????????????????
//        result.put("totalRedEnvelopeAmount", payUserAmount.toString());

        //???????????????????????????
        result.put("redEnvelopeAmount", useRedEnvelopeAmount.toString());

        //??????????????????
        result.put("wholeSalesAmount", useWholeSalesAmount.toString());
        //????????????????????????
        result.put("wholeSaleUserDiscountAmount", wholeSaleDiscountAmount.toString());
        //??????????????????
        result.put("wholeSalesUserDiscount", realWholeSaleDiscount.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        //??????????????????
        result.put("normalSaleAmount", useNormalSaleAmount.toString());
        //????????????????????????
        result.put("normalSaleUserDiscountAmount", normalSaleDiscountAmount.toString());
        //??????????????????
        result.put("normalSalesUserDiscount", realNormalSaleDiscount.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        //???????????????????????????????????????????????????redis
        if(putDataRedisStatus){
            redisUtils.hmset(Constant.getOrderAmountDetailRedisKey(payUserId.toString(), transNo), (Map<String, Object>) amountMap, 300);
        }

        return realPayAmount;
    }


    /**
     * ???????????????????????????????????????
     * @author zhangzeyuan
     * @date 2021/6/30 23:10
     * @param payUserId
     * @param recUserId
     * @param merchantDTO
     * @param payType
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    private Map<String, Object> getOrderDiscount(Long payUserId, Long recUserId, MerchantDTO merchantDTO, Integer payType) throws Exception{

        //??????????????????
        // todo V3?????????????????????V3?????????????????????????????????????????????
//        BigDecimal payUserAmount = userService.getBalance(payUserId, StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
        BigDecimal payUserAmount = BigDecimal.ZERO;
        //????????????
        BigDecimal merchantAmount = userService.getBalance(recUserId, StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());

        //??????????????????
        BigDecimal wholeSaleDiscount = BigDecimal.ZERO;
        //????????????
        BigDecimal baseDiscount = BigDecimal.ZERO;
        if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode())){
            //?????????
            if(Objects.nonNull(merchantDTO.getBasePayRate())){
                baseDiscount = merchantDTO.getBasePayRate();
            }
            if(Objects.nonNull(merchantDTO.getWholeSaleUserPayDiscount())){
                wholeSaleDiscount = merchantDTO.getWholeSaleUserPayDiscount();
            }
        }else if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){
            //?????????
            if(Objects.nonNull(merchantDTO.getBaseRate())){
                baseDiscount = merchantDTO.getBaseRate();
            }
            if(Objects.nonNull(merchantDTO.getWholeSaleUserDiscount())){
                wholeSaleDiscount = merchantDTO.getWholeSaleUserDiscount();
            }
        }
        //????????????
        BigDecimal marketingDiscount = Objects.isNull(merchantDTO.getMarketingDiscount()) ? BigDecimal.ZERO : merchantDTO.getMarketingDiscount();

        //????????????
        BigDecimal extraDiscount;
        //?????????????????????????????????
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
     * ???????????????
     * @author zhangzeyuan
     * @date 2021/6/23 16:53
     * @param cardId ???ID
     * @param payUserId ????????????ID
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
                log.error("?????????????????????",e.getMessage());
            }
        }else{
            //???????????????
            JSONObject cardObj;
            try {
                cardObj = serverService.getCardInfo(cardId);
            } catch (Exception e) {
                //????????????????????????
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            if (cardObj == null) {
                //??????????????????????????????
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            String latpayToken = cardObj.getString("crdStrgToken");
            String stripeToken = cardObj.getString("stripeToken");

            String expMonth = "";
            String expYear = "";

            try {
                if(StringUtils.isNotBlank(stripeToken)){
                    //stripe?????????????????????
                    JSONObject cardExpirationDate = cardService.getStripeCardExpirationDate(stripeToken, payUserId, request);
                    expYear = cardExpirationDate.getString("customerCcExpyr");
                    expMonth = cardExpirationDate.getString("customerCcExpmo");
                }else if(StringUtils.isNotBlank(latpayToken)){
                    //latpay?????????????????????
                    JSONObject cardDetails = userService.getCardDetails(latpayToken, request);

                    expYear = cardDetails.getString("customerCcExpyr");
                    expMonth = cardDetails.getString("customerCcExpmo");
                }
            } catch (Exception e) {
                log.error("????????????????????????, cardId:",cardObj.getString("id") + "|userId:" + payUserId);
            }
            cardObj.put("customerCcExpyr", expYear);
            cardObj.put("customerCcExpmo", expMonth);
            //?????????
            cardDTO = JSONObject.parseObject(cardObj.toJSONString(), CardDTO.class);
        }
        return cardDTO;

       /* // ?????????LayPay,???????????????
        CardDTO lastCard = new CardDTO();
        if(cardId == null){
            //???????????????
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
            //???????????????
            JSONObject cardObj;
            try {
                cardObj = serverService.getCardInfo(cardId);
            } catch (Exception e) {
                //????????????????????????
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            if (cardObj == null) {
                //??????????????????????????????
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            //?????????
            lastCard = JSONObject.parseObject(cardObj.toJSONString(), CardDTO.class);
        }

        //??????????????????????????????
        if (lastCard == null || StringUtils.isBlank(lastCard.getCardNo()) || StringUtils.isBlank(lastCard.getCustomerCcType())
                || StringUtils.isBlank(lastCard.getCrdStrgToken())) {
            //??????????????????????????????
//            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }else{
            try {
                JSONObject cardDetails = userService.getCardDetails(lastCard.getCrdStrgToken(), request);

                lastCard.setCustomerCcExpmo(cardDetails.getString("customerCcExpyr"));
                lastCard.setCustomerCcExpyr(cardDetails.getString("customerCcExpmo"));
            } catch (Exception e) {
                //????????????
                log.info("latpay???????????????????????????????????????, e:{}", e.getMessage());
            }
        }
        return lastCard;*/
    }

    /**
     * ??????????????????????????????????????????
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
            //todo ??????????????????
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
     * ?????????????????????????????????
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
        //?????????????????????
        Integer creditCardState = payUser.getCreditCardState();
        if(null == creditCardState || !creditCardState.equals(1)){
            throw new BizException(I18nUtils.get("pay.card.not.bound", getLang(request), new String[]{StaticDataEnum.CREDIT_MIN_TRANSAMOUNT.getMessage()}));
        }

        //????????????
        Map<String, Object> userCreditInfoMap = getCreditUserAvailableAmountAndState(payUserId);

        //???????????????
        BigDecimal creditAmount = (BigDecimal) userCreditInfoMap.get("creditAmount");

        //?????????????????????
        Integer userState = (Integer) userCreditInfoMap.get("userState");

        //??????????????????
        if(userState.equals(StaticDataEnum.CREAT_USER_STATE_11.getCode())){
            throw new BizException(I18nUtils.get("pay.user.freeze", getLang(request)));
        }

        if(creditAmount.compareTo(BigDecimal.ZERO) <= 0){
            throw new BizException(I18nUtils.get("pay.credit.amount.insufficient", getLang(request)));
        }

        //??????????????????????????????
        BigDecimal realPayAmount = getTransactionTruelyPayAmount(transNo, transAmount, payUserId, recUserId, useRedEnvelopeAmount,
                merchantDTO, payType, result, Constant.ORDER_TRANS_TYPE_CREDIT_FLAG, request);

        //??????????????????
        BigDecimal mixPayAmount = new BigDecimal(StaticDataEnum.CREDIT_MIN_TRANSAMOUNT.getCode());

        if(realPayAmount.compareTo(BigDecimal.ZERO) < 0){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        //???????????? ????????????????????????10
        if(realPayAmount.compareTo(BigDecimal.ZERO) > 0 &&  realPayAmount.compareTo(mixPayAmount) <  0){
            throw new BizException(I18nUtils.get("credit.amount.limit", getLang(request), new String[]{StaticDataEnum.CREDIT_MIN_TRANSAMOUNT.getMessage()}));
        }

        //????????????
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

        //????????????????????????????????????
        JSONArray tipList = getTipListByOrderAmount(transAmount);
        result.put("tipList", tipList);

        //???????????????
        BigDecimal channelFeeAmount = BigDecimal.ZERO;
        BigDecimal transChannelFeeRate = BigDecimal.ZERO;

        //????????????????????? ????????????????????????25% ??????
        BigDecimal creditFirstCardPayAmount = BigDecimal.ZERO;
        //?????????????????????
        BigDecimal creditRealPayAmount = BigDecimal.ZERO;

        //??????????????????
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("gatewayType", 0);
        params.put("state", StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);

        if(gatewayDTO == null || gatewayDTO.getId() == null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        //?????????ID??????
        CardDTO cardInfo = getCardInfoById(cardId, payUserId, request);

        result.put("cardId", cardInfo.getId().toString());
        result.put("cardNo", cardInfo.getCardNo());
        result.put("cardCcType", cardInfo.getCustomerCcType());

        //???????????????????????????????????????
        BigDecimal calculateCardFeeAmount = BigDecimal.ZERO;

        //????????????0
        if(realPayAmount.compareTo(BigDecimal.ZERO) > 0){
            //?????????????????????
            creditFirstCardPayAmount = realPayAmount.multiply(new BigDecimal(Constant.PAY_CREDIT_NEED_CARDPAY_AMOUNT_RATE)).setScale(2, BigDecimal.ROUND_HALF_UP);
            //?????????????????????
            creditRealPayAmount = realPayAmount.subtract(creditFirstCardPayAmount);
            // ????????????????????????????????? ??????25% + ????????????  + ??????
            calculateCardFeeAmount = creditFirstCardPayAmount.add(donationAmount).add(tipAmount);
        }else {
            //???????????? ????????? + ?????? ?????????????????????
            calculateCardFeeAmount = donationAmount.add(tipAmount);
        }


        if(calculateCardFeeAmount.compareTo(BigDecimal.ZERO) > 0){
            //?????????????????????
            Map<String, Object> feeMap = getCardPayTransactionFee(0, cardInfo.getCustomerCcType(), calculateCardFeeAmount, request);

            channelFeeAmount = (BigDecimal) feeMap.get("channelFeeAmount");
            transChannelFeeRate = (BigDecimal) feeMap.get("transChannelFeeRate");
        }

        //?????????????????????????????????
        if(creditAmount.compareTo(creditRealPayAmount) < 0){
            throw new BizException(I18nUtils.get("pay.credit.amount.insufficient", getLang(request)));
        }

        //???????????????????????????????????????????????????????????????
        this.previewRepayPlanData(realPayAmount, creditFirstCardPayAmount, result);

        //POS???????????????
        // POS?????????????????? ?????? ???ID?????? ???????????????????????????pos??????????????????
        if(StringUtils.isNotBlank(posTransNumber)){
            posQrPayFlowService.updateSysTransNoByThirdNo(result.getString("transNo"), posTransNumber, getUserId(request), System.currentTimeMillis());
        }

        // ???????????????????????????5????????????????????????????????????
        Integer repeatSaleState = getPayUnSuccessStateInFiveMin(merchantId, transAmount, payUserId, posTransNumber);

        //25%????????? + ????????? + ?????? + ????????? ??????
        result.put("allPayAmount", calculateCardFeeAmount.add(channelFeeAmount).toString());

        //???????????????
        result.put("transAmount", transAmount.toString());

        //????????? ?????? ??? ???????????????
        result.put("payAmount", realPayAmount.toString());

        //???????????????
        result.put("channelFeeAmount", channelFeeAmount.toString());
        //??????????????????
        result.put("transChannelFeeRate", transChannelFeeRate.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        result.put("mixPayAmount",mixPayAmount.toString());
        result.put("repeatSaleState", repeatSaleState.toString());

        //25%???????????????
        result.put("creditFirstCardPayAmount", creditFirstCardPayAmount.toString());

        //25%??????????????? + ?????????
        result.put("toShowFeeAllAmount",  calculateCardFeeAmount.toString());

        //?????????????????????
        result.put("creditRealPayAmount", creditRealPayAmount.toString());

        //???????????????
        result.put("donationAmount", donationAmount.toString());

        //????????????
        result.put("tipAmount", tipAmount.toString());

        return result;
    }



    /**
     * ?????????????????????????????????
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
        //?????????????????????
        Integer creditCardState = payUser.getCreditCardState();
        if(null == creditCardState || !creditCardState.equals(1)){
            throw new BizException(I18nUtils.get("pay.card.not.bound", getLang(request), new String[]{StaticDataEnum.CREDIT_MIN_TRANSAMOUNT.getMessage()}));
        }

        //????????????
        Map<String, Object> userCreditInfoMap = getCreditUserAvailableAmountAndState(payUserId);

        //???????????????
        BigDecimal creditAmount = (BigDecimal) userCreditInfoMap.get("creditAmount");

        //?????????????????????
        Integer userState = (Integer) userCreditInfoMap.get("userState");

        //??????????????????
        if(userState.equals(StaticDataEnum.CREAT_USER_STATE_11.getCode())){
            throw new BizException(I18nUtils.get("pay.user.freeze", getLang(request)));
        }

        if(creditAmount.compareTo(BigDecimal.ZERO) <= 0){
            throw new BizException(I18nUtils.get("pay.credit.amount.insufficient", getLang(request)));
        }

        //??????????????????????????????
        BigDecimal realPayAmount = getTransactionTruelyPayAmountV3(transNo, transAmount, payUserId, recUserId, useRedEnvelopeAmount,
                merchantDTO, payType, result, Constant.ORDER_TRANS_TYPE_CREDIT_FLAG, request);

        //??????????????????
        BigDecimal mixPayAmount = new BigDecimal(StaticDataEnum.CREDIT_MIN_TRANSAMOUNT.getCode());

        if(realPayAmount.compareTo(BigDecimal.ZERO) < 0){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        //????????????  ??????????????????   ????????????????????????10
        if(useRedEnvelopeAmount.compareTo(BigDecimal.ZERO) == 0  &&  realPayAmount.compareTo(mixPayAmount) <  0){
            throw new BizException(I18nUtils.get("credit.amount.limit", getLang(request), new String[]{StaticDataEnum.CREDIT_MIN_TRANSAMOUNT.getMessage()}));
        }

        //????????????
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

        //????????????????????????????????????
        JSONArray tipList = getTipListByOrderAmount(transAmount);
        result.put("tipList", tipList);

        //???????????????
        BigDecimal channelFeeAmount = BigDecimal.ZERO;
        BigDecimal transChannelFeeRate = BigDecimal.ZERO;

        //????????????????????? ????????????????????????25% ??????
        BigDecimal creditFirstCardPayAmount = BigDecimal.ZERO;
        //?????????????????????
        BigDecimal creditRealPayAmount = BigDecimal.ZERO;

        //??????????????????
        HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("gatewayType", 0);
        params.put("state", StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(params);

        if(gatewayDTO == null || gatewayDTO.getId() == null){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        //?????????ID??????
        CardDTO cardInfo = getCardInfoById(cardId, payUserId, request);

//        result.put("cardId", cardInfo.getId().toString());
//        result.put("cardNo", cardInfo.getCardNo());
//        result.put("cardCcType", cardInfo.getCustomerCcType());
        result.put("cardId", Objects.isNull(cardInfo.getId()) ? "" :  cardInfo.getId().toString());
        result.put("cardNo", StringUtils.isNotBlank(cardInfo.getCardNo()) ? cardInfo.getCardNo() : "");
        result.put("cardCcType", StringUtils.isNotBlank(cardInfo.getCustomerCcType()) ? cardInfo.getCustomerCcType() : "");
        result.put("customerCcExpyr", StringUtils.isNotBlank(cardInfo.getCustomerCcExpyr()) ? cardInfo.getCustomerCcExpyr() : "");
        result.put("customerCcExpmo", StringUtils.isNotBlank(cardInfo.getCustomerCcExpmo()) ? cardInfo.getCustomerCcExpmo() : "");

        //???????????????????????????????????????
        BigDecimal calculateCardFeeAmount = BigDecimal.ZERO;

        //????????????0
        if(realPayAmount.compareTo(BigDecimal.ZERO) > 0){
            //?????????????????????
            // 20211028???????????????????????????????????????????????????1???
            if(useRedEnvelopeAmount.compareTo(BigDecimal.ZERO) > 0 &&  realPayAmount.compareTo(mixPayAmount) <  0){
                creditFirstCardPayAmount = realPayAmount;
            }else{
                creditFirstCardPayAmount = realPayAmount.multiply(new BigDecimal(Constant.PAY_CREDIT_NEED_CARDPAY_AMOUNT_RATE)).setScale(2, BigDecimal.ROUND_HALF_UP);

            }
            //?????????????????????
            creditRealPayAmount = realPayAmount.subtract(creditFirstCardPayAmount);
            // ????????????????????????????????? ??????25% + ????????????  + ??????
            calculateCardFeeAmount = creditFirstCardPayAmount.add(donationAmount).add(tipAmount);
        }else {
            //???????????? ????????? + ?????? ?????????????????????
            calculateCardFeeAmount = donationAmount.add(tipAmount);
        }

//        if(calculateCardFeeAmount.compareTo(BigDecimal.ZERO) > 0){
//            //?????????????????????
//            Map<String, Object> feeMap = getCardPayTransactionFee(0, cardInfo.getCustomerCcType(), calculateCardFeeAmount, request);
//
//            channelFeeAmount = (BigDecimal) feeMap.get("channelFeeAmount");
//            transChannelFeeRate = (BigDecimal) feeMap.get("transChannelFeeRate");
//        }

        //?????????????????????????????????
        if(creditAmount.compareTo(creditRealPayAmount) < 0){
            throw new BizException(I18nUtils.get("pay.credit.amount.insufficient", getLang(request)));
        }

        //???????????????????????????????????????????????????????????????
        this.previewRepayPlanData(realPayAmount, creditFirstCardPayAmount, result);

        //POS???????????????
        // POS?????????????????? ?????? ???ID?????? ???????????????????????????pos??????????????????
        /*if(StringUtils.isNotBlank(posTransNumber)){
            posQrPayFlowService.updateSysTransNoByThirdNo(result.getString("transNo"), posTransNumber, getUserId(request), System.currentTimeMillis());
        }*/

        // ???????????????????????????5????????????????????????????????????
        Integer repeatSaleState = getPayUnSuccessStateInFiveMin(merchantId, transAmount, payUserId, posTransNumber);

        //25%????????? + ????????? + ?????? + ????????? ??????
        result.put("allPayAmount", calculateCardFeeAmount.add(channelFeeAmount).toString());

        //???????????????
        result.put("transAmount", transAmount.toString());

        //????????? ?????? ??? ???????????????
        result.put("payAmount", realPayAmount.toString());

        //???????????????
//        result.put("channelFeeAmount", channelFeeAmount.toString());
        //??????????????????
//        result.put("transChannelFeeRate", transChannelFeeRate.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        result.put("mixPayAmount",mixPayAmount.toString());
        result.put("repeatSaleState", repeatSaleState.toString());

        //25%???????????????

        result.put("creditFirstCardPayAmount", creditFirstCardPayAmount.toString());

        //25%??????????????? + ?????????
        result.put("toShowFeeAllAmount",  calculateCardFeeAmount.toString());
        //?????????????????????
        result.put("creditRealPayAmount", creditRealPayAmount.toString());

        //???????????????
        result.put("donationAmount", donationAmount.toString());

        //????????????
        result.put("tipAmount", tipAmount.toString());

        return result;
    }



    /**
     * ?????????????????????????????? ??? ??????????????????
     * @author zhangzeyuan
     * @date 2021/6/29 16:26
     * @param userId
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    private Map<String, Object> getCreditUserAvailableAmountAndState(Long userId){

        HashMap<String, Object> resultMap = Maps.newHashMapWithExpectedSize(2);

        BigDecimal creditAmount = BigDecimal.ZERO;

        int userState = 11;

        //???????????????????????????
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
            log.error("?????????????????????????????????",e.getMessage());
        }

        resultMap.put("creditAmount", creditAmount);
        resultMap.put("userState", userState);

        return resultMap;
    }
    /**
     * ??????????????????????????? ??? ????????????
     * @author zhangzeyuan
     * @date 2021/6/27 18:15
     * @param realPayAmount
     * @param creditFirstCardPayAmount
     * @param result
     */
    private void previewRepayPlanData(BigDecimal realPayAmount, BigDecimal creditFirstCardPayAmount, JSONObject result){
        //???????????????0 ????????????0
        if(realPayAmount.compareTo(BigDecimal.ZERO) == 0){
            result.put("creditAverageAmount", "0.00");
            result.put("period", "0");
            result.put("previewRepayPlanList",  Collections.emptyList());
            return;
        }

        //???????????????????????????
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
            log.error("?????????????????????????????????",e.getMessage());
        }
    }



    /**
     * ????????????????????????app??????????????????
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
     * ???????????????????????????5????????????????????????????????????
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
        // ???????????????????????????5????????????????????????????????????
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
                // ?????????????????????
                if(accountFlowDTO.getState() != StaticDataEnum.TRANS_STATE_0.getCode() && accountFlowDTO.getState() != StaticDataEnum.TRANS_STATE_3.getCode()
                        && accountFlowDTO.getState() != StaticDataEnum.TRANS_STATE_1.getCode() ){
                    // ????????????????????????????????????????????????
                    continue;
                }
                QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(accountFlowDTO.getFlowId());
                if(accountFlowDTO.getState() == StaticDataEnum.TRANS_STATE_0.getCode() || accountFlowDTO.getState() == StaticDataEnum.TRANS_STATE_3.getCode() ){
                    JSONObject data;
                    data = serverService.transactionInfo(accountFlowDTO.getOrderNo().toString());

                    if (data != null ) {
                        if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                            // ??????
                            // ???????????????????????????
                            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                            accountFlowService.updateAccountFlowByOrderNo(accountFlowDTO.getOrderNo(),accountFlowDTO,null);
                            // ??????????????????
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


                            // ???????????????????????????
//                        GatewayDTO gatewayDTO = new GatewayDTO();
//                        // ??????????????????
//                        if(qrPayFlowDTO.getTransType() != StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode()){
//                            params.clear();
//                            params.put("type",qrPayFlowDTO.getGatewayId());
//                            params.put("state",StaticDataEnum.STATUS_1.getCode());
//                            gatewayDTO = gatewayService.findOneGateway(params);
//                            if(gatewayDTO == null || gatewayDTO.getId() == null){
//                                //????????????????????????????????????????????????
//                                log.info("??????????????????????????????????????????????????????id:"+qrPayFlowDTO.getId());
//
//                                rechargeFlowService.channelLimitRollback(System.currentTimeMillis(),gatewayDTO.getType(),qrPayFlowDTO.getPayAmount(),null);
//                                doBatchAmountOutRollBack(qrPayFlowDTO,null);
//                                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
//                                updateFlow(qrPayFlowDTO, null, null, null);
//                                continue;
//                            }
//                        }
//                        // ???????????????
//                        UserDTO payUser = userService.findUserById(qrPayFlowDTO.getPayUserId());
//                        UserDTO recUser = userService.findUserById(qrPayFlowDTO.getRecUserId());
//                        // ?????????????????????????????????
//                        if(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode() == qrPayFlowDTO.getTransType()){
//                            // ??????????????????
//                            //???????????????
//                            JSONObject cardObj;
//                            try {
//                                cardObj = serverService.getCardInfo(Long.parseLong(qrPayFlowDTO.getCardId()));
//                            } catch (Exception e) {
//                                //????????????????????????
//                               throw e;
//                            }
//                            if (cardObj == null) {
//                                //??????????????????????????????
//                                log.info("???????????????????????????????????????id???"+qrPayFlowDTO.getId());
//                                doQrPayRollBack(qrPayFlowDTO,StaticDataEnum.TRANS_STATE_12.getCode(),null);
//                                continue;
//                            }
//                            //?????????????????????????????????
//                            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_20.getCode());
//                            //??????????????????
//                            qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),qrPayFlowDTO,null);
//                            //???????????????
//                            qrPayByCard(null, payUser, recUser, qrPayFlowDTO, gatewayDTO, JSONObject.parseObject(cardObj.toJSONString(), CardDTO.class) ,cardObj );
//                            // ???????????????????????????
//                            dealFirstDeal(qrPayFlowDTO,payUser,null);
//                        }else if(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode() == qrPayFlowDTO.getTransType()){
//                            //?????????
//                            if(payUser.getInstallmentState() != StaticDataEnum.STATUS_1.getCode() || payUser.getSplitAddInfoState() == StaticDataEnum.STATUS_1.getCode()){
//                                // ??????????????????????????????
//                                log.info("???????????????????????????????????????id???"+qrPayFlowDTO.getId());
//                                doQrPayRollBack(qrPayFlowDTO,StaticDataEnum.TRANS_STATE_12.getCode(),null);
//                                continue;
//                            }
//
//                            qrPayFlowDTO = doCredit(qrPayFlowDTO,null);
//                            // ???????????????????????????
//                            dealFirstDeal(qrPayFlowDTO,payUser,null);
//                        }
                        } else if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                            //??????????????????
                            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                            accountFlowService.updateAccountFlowByOrderNo(accountFlowDTO.getOrderNo(),accountFlowDTO,null);

                            //??????
//                        if(qrPayFlowDTO.getGatewayId() != null ){
//                            rechargeFlowService.channelLimitRollback(System.currentTimeMillis(),qrPayFlowDTO.getGatewayId(),qrPayFlowDTO.getPayAmount(),null);
//                        }
//                        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
//                        updateFlow(qrPayFlowDTO, null, null, null);

                            continue;
                        }else{
                            // ???????????????
                            continue;
                        }

                    }else{
                        // ????????????????????????
                        //??????????????????
                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                        accountFlowService.updateAccountFlowByOrderNo(accountFlowDTO.getOrderNo(),accountFlowDTO,null);
                        continue;
                    }
                    // ?????????????????????????????????????????????????????????????????????????????????????????????
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

            //???????????????????????????????????????????????????????????????????????????????????????????????????????????????
            try {
                // ???????????????????????????????????????????????????
                AccountFlowDTO accountFlowCheck = accountFlowService.findAccountFlowById(accountFlowDTO.getId());
                if(accountFlowCheck.getState() == StaticDataEnum.TRANS_STATE_1.getCode() || accountFlowCheck.getState() == StaticDataEnum.TRANS_STATE_2.getCode()){
                    continue;
                }

                // ????????????
                JSONObject accountResult = serverService.transactionInfo(accountFlowDTO.getRollBackNo().toString());
                AccountFlowDTO updateFlow = new AccountFlowDTO();
                if (accountResult != null) {
                    if (accountResult.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                        updateFlow.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    } else if (accountResult.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                        updateFlow.setState(StaticDataEnum.TRANS_STATE_5.getCode());
                    } else {
                        //?????????????????????
                        continue;
                    }
                } else {

                    // ?????????????????????????????????
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
            //???????????????????????????????????????????????????????????????????????????????????????????????????????????????
            try {

                // ????????????????????????????????????????????????????????????
                AccountFlowDTO accountFlowCheck = accountFlowService.findAccountFlowById(accountFlowDTO.getId());
                if(accountFlowCheck.getState() != StaticDataEnum.TRANS_STATE_5.getCode() ){
                    continue;
                }

                QrPayFlowDTO qrPayFlowDTO = qrPayFlowService.findQrPayFlowById(accountFlowDTO.getFlowId());
                doBatchAmountOutRollBack(qrPayFlowDTO,null);
            } catch (Exception e) {
                //??????????????????
                continue;
            }
        }
    }

    @Override
    public void qrPayReqCheckOld(QrPayDTO qrPayDTO, HttpServletRequest request) throws Exception {

        //?????????????????????
        if (StringUtils.isEmpty(qrPayDTO.getTransAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.empty", getLang(request)));
        }

        //????????????????????????
        if (!RegexUtils.isTransAmt(qrPayDTO.getTransAmount().toString())) {
            throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
        }

        if (qrPayDTO.getFeeAmt()!=null && (StringUtils.isNotEmpty(qrPayDTO.getFeeAmt().toString()))) {
            //???????????????????????????
            if (!RegexUtils.isTransAmt(qrPayDTO.getFeeAmt().toString())) {
                throw new BizException(I18nUtils.get("feeAmount.error", getLang(request)));
            }

            //???????????????????????????
            if (StringUtils.isEmpty(qrPayDTO.getFeeDirection().toString())) {
                throw new BizException(I18nUtils.get("feeDirection.null", getLang(request)));
            }
        }

        //??????userId????????????
        if (StringUtils.isEmpty(qrPayDTO.getPayUserId().toString()) || StringUtils.isEmpty(qrPayDTO.getRecUserId().toString())) {
            throw new BizException(I18nUtils.get("userId.null", getLang(request)));
        }

        //??????????????????
        if (StringUtils.isEmpty(qrPayDTO.getPayType().toString())) {
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }


        if (qrPayDTO.getPayType() == StaticDataEnum.PAY_TYPE_0.getCode()) {
            if (StringUtils.isEmpty(qrPayDTO.getCardId().toString())) {
                throw new BizException(I18nUtils.get("card.id.empty", getLang(request)));
            }
        }

        //?????????????????????????????????
        if(qrPayDTO.getRedEnvelopeAmount()!=null && qrPayDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) != 0){
            if (!RegexUtils.isTransAmt(qrPayDTO.getRedEnvelopeAmount().toString())) {
                throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
            }
        }
        //???????????????????????????????????????
        if(qrPayDTO.getWholeSalesAmount()!=null && qrPayDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) != 0){
            if (!RegexUtils.isTransAmt(qrPayDTO.getWholeSalesAmount().toString())) {
                throw new BizException(I18nUtils.get("transAmount.error", getLang(request)));
            }
            //??????????????????????????????????????????
            if(qrPayDTO.getWholeSalesAmount().compareTo(qrPayDTO.getTransAmount())>0){
                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
            }
        }
    }

    @Override
    public Object doQrPayOld(QrPayDTO qrPayDTO, HttpServletRequest request) throws Exception {

        //??????????????????user??????
        UserDTO payUser = userService.findUserById(qrPayDTO.getPayUserId());
        UserDTO recUser = userService.findUserById(qrPayDTO.getRecUserId());

        //????????????????????????
        if (payUser.getId() == null || recUser.getId() == null) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }

        //????????????????????????
        if (!checkAccountState(payUser.getId())) {
            throw new BizException(I18nUtils.get("account.error", getLang(request)));
        }
        if (!checkAccountState(recUser.getId())) {
            throw new BizException(I18nUtils.get("account.error", getLang(request)));
        }

        //????????????
        QrPayFlowDTO qrPayFlowDTO = new QrPayFlowDTO();
        qrPayFlowDTO.setFee(qrPayDTO.getFeeAmt());
        qrPayFlowDTO.setPayUserId(qrPayDTO.getPayUserId());
        qrPayFlowDTO.setPayUserType(payUser.getUserType());
        qrPayFlowDTO.setMerchantId(qrPayDTO.getMerchantId());
        qrPayFlowDTO.setRecUserId(qrPayDTO.getRecUserId());
        qrPayFlowDTO.setRecUserType(recUser.getUserType());
        qrPayFlowDTO.setTransAmount(qrPayDTO.getTransAmount());
        qrPayFlowDTO.setRemark(qrPayDTO.getRemark());
        //?????????????????????,??????????????????
        if(recUser.getUserType()==StaticDataEnum.USER_TYPE_20.getCode()){
            qrPayFlowDTO =  getQrPayFlowOld(qrPayFlowDTO,qrPayDTO,request);
        }

        Map<String,Object> resMap = new HashMap<>();
//        if (StaticDataEnum.PAY_TYPE_0.getCode() == (qrPayDTO.getPayType())) {
//            //????????????????????????
//            GatewayDTO gatewayDTO = doRouteConfig(recUser,qrPayFlowDTO,qrPayDTO,request);
//            //????????????
//            qrPayFlowDTO = calculation(qrPayDTO, qrPayFlowDTO, request);
//            //?????????
//            if (recUser.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
//                //?????????
//                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_4.getCode());
//            } else {
//                //??????????????????
//                MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayDTO.getMerchantId());
//                if(merchantDTO==null || merchantDTO.getId()==null ||  merchantDTO.getIsAvailable() !=1) {
//                    throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
//                }
//                //?????????
//                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode());
//                if(StaticDataEnum.GATEWAY_TYPE_0.getCode()==gatewayDTO.getType()||StaticDataEnum.GATEWAY_TYPE_4.getCode()==gatewayDTO.getType()){
//                    //Latpay??????integrapay
//                    //????????????,??????????????????????????????
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
//            //????????????????????????
//            GatewayDTO gatewayDTO = doRouteConfig(recUser,qrPayFlowDTO,qrPayDTO,request);
//            //????????????
//            qrPayFlowDTO = calculation(qrPayDTO, qrPayFlowDTO, request);
//            //??????????????????
//            if (recUser.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
//                //????????????
//                qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_3.getCode());
//            } else {
//                //????????????
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
//            //????????????????????????
//            GatewayDTO gatewayDTO = doRouteConfig(recUser,qrPayFlowDTO,qrPayDTO,request);
//            //???????????????
//            if (recUser.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
//                //?????????????????????
//                // qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_10.getCode());
//                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
//            } else {
//                //???????????????
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
//            //????????????????????????
//            GatewayDTO gatewayDTO = doRouteConfig(recUser,qrPayFlowDTO,qrPayDTO,request);
//            //????????????
//            qrPayFlowDTO = calculation(qrPayDTO, qrPayFlowDTO, request);
//            //????????????
//            if (recUser.getUserType() == StaticDataEnum.USER_TYPE_10.getCode()) {
//                //??????????????????
//                //qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_11.getCode());
//                throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
//
//            } else {
//                //????????????
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
            //?????????
            qrPayFlowDTO = checkCreditParams(qrPayFlowDTO,qrPayDTO,recUser,payUser,request);
            qrPayFlowDTO = doCreditOld(qrPayFlowDTO,qrPayDTO,request);
        }
        else {
            //?????????????????????
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));

        }
        resMap.put("flowId",qrPayFlowDTO.getTransNo());
        resMap.put("orderCreateDate", new SimpleDateFormat("HH:mm dd-MM-yyyy").format(new Date(System.currentTimeMillis())));
        return resMap;

    }

    private QrPayFlowDTO doCreditOld(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, HttpServletRequest request) throws Exception {
        // ?????????????????????????????????
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
            // ????????????????????????????????????????????????
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            qrPayFlowService.updateQrPayFlowForConcurrency(qrPayFlowDTO.getId(), qrPayFlowDTO, request);
            creditInfo.clear();
            creditInfo.put("tripartiteTransactionNo", qrPayFlowDTO.getId());
//            creditInfo.put("accessSideId", accessMerchantDTO.getPlatformId());
            serverService.orderStateRollback(creditInfo, request);

            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        // ??????????????????????????????
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

        //????????????????????????
        MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayDTO.getMerchantId());
        if(merchantDTO==null || merchantDTO.getId()==null ||  merchantDTO.getIsAvailable() !=1) {
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //?????????????????????????????????
        BigDecimal wholeSalesAmount = qrPayDTO.getWholeSalesAmount() == null ?  BigDecimal.ZERO : qrPayDTO.getWholeSalesAmount();
        BigDecimal normalSaleAmount = qrPayDTO.getTransAmount().subtract(qrPayDTO.getWholeSalesAmount());
        BigDecimal redEnvelopeAmount = qrPayDTO.getRedEnvelopeAmount() == null ? BigDecimal.ZERO : qrPayDTO.getRedEnvelopeAmount();
        //??????????????????????????????
        BigDecimal merchantAmount = userService.getBalance(qrPayDTO.getRecUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
        //????????????
        if(normalSaleAmount.compareTo(BigDecimal.ZERO) > 0){

            //?????????????????????????????????
            if(merchantAmount.compareTo(wholeSalesAmount) != 0 ){
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            //????????????????????????
            BigDecimal baseDiscountAmount = normalSaleAmount.multiply(merchantDTO.getBaseRate() == null ? BigDecimal.ZERO : merchantDTO.getBaseRate()).setScale(2,BigDecimal.ROUND_HALF_UP);
            //?????????????????????????????????
            Long today = System.currentTimeMillis();
            Long extraDiscountPeriod = merchantDTO.getExtraDiscountPeriod();
            BigDecimal extraDiscount = null;
            if (today.longValue() < extraDiscountPeriod.longValue()) {
                extraDiscount = merchantDTO.getExtraDiscount();
            } else {
                extraDiscount = new BigDecimal("0.00");
            }
            BigDecimal extraDiscountAmount = normalSaleAmount.multiply(extraDiscount == null ? BigDecimal.ZERO :extraDiscount).setScale(2,BigDecimal.ROUND_HALF_UP);
            //????????????????????????
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
            //??????????????????????????????
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
        //???????????? = ?????????????????? + ????????????????????????????????? - ???????????? - ????????????????????? -??????????????? - ????????????
        BigDecimal payAmount = wholeSalesAmount.subtract(wholeSaleUserDiscountAmount).add(
                normalSaleAmount.subtract(qrPayFlowDTO.getBaseDiscountAmount()).subtract(qrPayFlowDTO.getExtraDiscountAmount()).subtract(qrPayFlowDTO.getMarkingDiscountAmount())
        ).subtract(redEnvelopeAmount);

        if(payAmount.compareTo(qrPayDTO.getTrulyPayAmount())!=0){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //???????????????
        BigDecimal platformFee = normalSaleAmount.multiply(merchantDTO.getAppChargeRate() == null ? BigDecimal.ZERO : merchantDTO.getAppChargeRate()).setScale(2,BigDecimal.ROUND_HALF_UP);
        //???????????? = ???????????? - ???????????? - ????????????????????? -???????????? - ???????????????
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
            //??????????????????
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
                // ??????????????????
                JSONObject json1 = new JSONObject();
                json1.put("serialNumber", accountFlowDTO.getId());
                json1.put("transAmount", accountFlowDTO.getTransAmount());
                json1.put("userId", accountFlowDTO.getUserId());
                json1.put("subAccountType",accountFlowDTO.getAccountType());
                // ???????????????0????????????1????????????
                json1.put("transDirection", StaticDataEnum.DIRECTION_0.getCode());
                if(accountFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_26.getCode()){
                    json1.put("channelTransType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_28.getCode());
                }else if(accountFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode()){
                    json1.put("channelTransType", StaticDataEnum.ACC_FLOW_TRANS_TYPE_27.getCode());
                }

                jsonArray.add(json1);
                AccountFlowDTO accountFlowDTO1 = new AccountFlowDTO();
                //??????????????????????????????????????????????????????
                accountFlowDTO1.setRollBackNo(orderNo);
                accountFlowDTO1.setState(StaticDataEnum.TRANS_STATE_4.getCode());
                accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO1,request);
                amountInAmount = amountInAmount.add(accountFlowDTO.getTransAmount());
            }
            // ????????????????????????
            jsonObject.put("remark","rollBack for "+ amountOutFlowList.get(0).getOrderNo());
            jsonObject.put("totalAmountOut", BigDecimal.ZERO);
            jsonObject.put("totalAmountIn",amountInAmount);
            jsonObject.put("totalNumber", amountOutFlowList.size());
            jsonObject.put("channelSerialnumber", orderNo);
            jsonObject.put("channel", "0001");
            jsonObject.put("dataList", jsonArray);
            jsonObject.put("channelTransType",StaticDataEnum.ACC_FLOW_TRANS_TYPE_30.getCode());
            //??????????????????
            JSONObject result = null;
            try {
                result =serverService.batchChangeBalance(jsonObject);
            }catch (Exception e){
                log.error("doBatchAmountOutRollBack orderNo:" +orderNo+ ",Exception:"+e.getMessage()+",message:"+e);
                throw e;
            }
            //??????????????????
            if(result != null && "2".equals(result.getString("errorState"))){
                // ?????????????????????????????????
                for (AccountFlowDTO accountFlowDTO:amountOutFlowList){
                    AccountFlowDTO accountFlowDTO1 = new AccountFlowDTO();
                    accountFlowDTO1.setState(StaticDataEnum.TRANS_STATE_5.getCode());
                    accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO1,request);
                }
            }else{

                // ????????????????????????
                params.clear();
                params.put("flowId",qrPayFlowDTO.getId());
                params.put("transType",StaticDataEnum.ACC_FLOW_TRANS_TYPE_21.getCode());
                params.put("userId",qrPayFlowDTO.getPayUserId());

                MarketingFlowDTO marketingFlowDTO = marketingFlowService.findOneMarketingFlow(params);

                // ????????????????????????
                if(marketingFlowDTO != null && marketingFlowDTO.getId() != null){

                    //???????????? ?????? ??????????????? ??????
                    MarketingFlowDTO updateMarketingFlow = new MarketingFlowDTO();
                    updateMarketingFlow.setState(2);
                    marketingFlowService.updateMarketingFlow(marketingFlowDTO.getId(), updateMarketingFlow, request);
                    /*marketingFlowService.logicDeleteMarketingFlow(marketingFlowDTO.getId(),request);*/
                }
                // ???????????????????????????
                for (AccountFlowDTO accountFlowDTO:amountOutFlowList){
                    AccountFlowDTO accountFlowDTO1 = new AccountFlowDTO();
                    accountFlowDTO1.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO1,request);
                }

            }
        }catch (Exception e){
            log.error("flowId:"+qrPayFlowDTO.getId() +",doBatchAmountOutRollBack Exception :"+e.getMessage(),e);
        }
        // ???????????????????????????
        if(qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            // ????????????????????? ?????????????????????
            MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayFlowDTO.getMerchantId());
            // ?????????????????????????????????
            if(merchantDTO.getHaveWholeSell() == StaticDataEnum.MERCHANT_WHOLE_SELL_AMOUNT_ZERO.getCode()){
                try {
                    // ????????????????????????
//                    BigDecimal merchantAmount = userService.getBalance(qrPayFlowDTO.getRecUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
                    // ?????? > 0 ??????????????????????????????
//                    if(merchantAmount.compareTo(BigDecimal.ZERO) > 0){
                    merchantService.updateHaveWholeSell(qrPayFlowDTO.getMerchantId(),StaticDataEnum.MERCHANT_WHOLE_SELL_AMOUNT_NOT_ZERO.getCode(),request);
//                    }

                }catch (Exception e){
                    log.error("doBatchAmountOutRollBack ?????????????????????????????? ???id:"+qrPayFlowDTO.getId());
                }
            }
        }
    }

    @Override
    public void qrPayByAccount(QrPayDTO qrPayDTO, UserDTO payUser, UserDTO recUser, QrPayFlowDTO qrPayFlowDTO, GatewayDTO gatewayDTO, HttpServletRequest request) throws Exception {
        //???????????????
        JSONObject cardObj = null;
        try {
            cardObj = serverService.getCardInfo(qrPayDTO.getCardId());
        } catch (Exception e) {
            //????????????
            //????????????
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        if (cardObj == null) {
            //???????????????
            //????????????
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        CardDTO cardDTO = JSONObject.parseObject(cardObj.toJSONString(), CardDTO.class);
        WithholdFlowDTO withholdFlowDTO = null;
        try {
            //TODO ????????????????????????????????????????????????
            BigDecimal channelCharge=BigDecimal.ZERO;
            if(gatewayDTO.getRate()!=null){
//                channelCharge = qrPayFlowDTO.getPayAmount().multiply(new BigDecimal(gatewayDTO.getRate())) ;
            }
            //??????????????????
            if(StaticDataEnum.GATEWAY_TYPE_3.getCode() == gatewayDTO.getType()){
                //Latpay directPay
                withholdFlowDTO = getLatPayWithholdFlowDTO(qrPayFlowDTO, request, cardDTO, channelCharge,gatewayDTO);
            }else{
                //???????????????
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            //????????????
            rechargeFlowService.channelLimit(gatewayDTO.getType(),qrPayFlowDTO.getPayAmount(),request);
            //??????????????????????????????
            withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        } catch (Exception e) {
            //????????????
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        //??????????????????
        JSONObject requestInfo = new JSONObject();
        //????????????consumer??????
        JSONObject userInfo = serverService.userInfoByQRCode(payUser.getId());
        JSONObject consumer = new JSONObject();
        consumer.put("firstname", userInfo.getString("firstName"));
        consumer.put("lastname", userInfo.getString("lastName"));
        consumer.put("phone", userInfo.getString("phone"));
        consumer.put("email", userInfo.getString("email"));
        requestInfo.put("consumer", consumer);
        //????????????order??????
        JSONObject order = new JSONObject();
        order.put("reference", "");
        order.put("currency", StaticDataEnum.CURRENCY_TYPE.getMessage());
        order.put("amount", qrPayDTO.getTrulyPayAmount());
        requestInfo.put("order", order);
        //????????????bill??????
        JSONObject bill = new JSONObject();
        //bill???directDebit??????
        JSONObject directDebit = new JSONObject();
        directDebit.put("bsb", cardDTO.getBsb());
        directDebit.put("accountnumber", cardDTO.getCardNo());
        directDebit.put("accountname", cardDTO.getAccountName());
        bill.put("directdebit", directDebit);
        //bill???fee??????
        JSONObject fee = new JSONObject();
        fee.put("processingfee", "?????");
        bill.put("fees", fee);
        requestInfo.put("billing", bill);
        requestInfo.put("notifyurl", latpayNoticeUrl);
        //???????????????
        Integer state = null;
        try {
//            state = latPayService.latPayDirectDebitRequest(requestInfo, request);
        } catch (Exception e) {
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
            updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
            throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
        }
        //?????????????????????????????????
    }


    private void dealThirdSuccess(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException{
        if(qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode() && qrPayFlowDTO.getCardId() != null && qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
            // ??????????????? , ??????????????????????????????????????????????????????????????????
            // ?????????????????????????????????
            try {
                userService.logCardFailedTime(qrPayFlowDTO.getPayUserId(),Long.parseLong(qrPayFlowDTO.getCardId()),true,request);
            }catch ( Exception e){
                log.error("??????????????????????????????????????????????????????flowId???"+withholdFlowDTO.getFlowId()+",exception:"+e.getMessage(),e);
            }

        }

        // ???????????????????????????????????????0?????????
        if(qrPayFlowDTO.getIsNeedClear() == StaticDataEnum.NEED_CLEAR_TYPE_0.getCode() || qrPayFlowDTO.getRecAmount().compareTo(BigDecimal.ZERO) == 0
                || qrPayFlowDTO.getRecUserId() == null ){
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
            updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
//            updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
            //??????????????????
            if(qrPayFlowDTO.getRecUserId() != null){
                sendMessage(qrPayFlowDTO.getRecUserId(), qrPayFlowDTO.getId(), request);
            }
        }else{
            // ?????????????????????31??????????????????????????????????????????????????????????????????
            // ????????????31??????????????????????????????????????????
            if(qrPayFlowDTO.getState() != StaticDataEnum.TRANS_STATE_31.getCode() ){
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_30.getCode());
                updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
                //??????????????????
                sendMessage(qrPayFlowDTO.getRecUserId(), qrPayFlowDTO.getId(), request);
            }
            //????????????????????????
            AccountFlowDTO accountFlowDTO = getAccountFlowDTO(qrPayFlowDTO);
            //????????????????????????
            accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO, request));
            try {
                doAmountTrans(accountFlowDTO, qrPayFlowDTO, request);
            } catch (Exception e) {
                log.error("qrPay dealThirdSuccess Exception:"+e.getMessage(),e);
                //????????????
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                updateFlow(qrPayFlowDTO, null, null, request);
            }

        }
        if(qrPayFlowDTO.getState() == StaticDataEnum.TRANS_STATE_31.getCode() ){
            // ???????????????????????????
            //qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
            //updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
            // ???????????????????????????
            if(qrPayFlowDTO.getNormalSaleAmount().compareTo(BigDecimal.ZERO) > 0){
                // ????????????????????? ?????????????????????
                MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayFlowDTO.getMerchantId());
                // ???????????????????????????
                if(merchantDTO.getHaveWholeSell() == StaticDataEnum.MERCHANT_WHOLE_SELL_AMOUNT_NOT_ZERO.getCode()){
                    try {
                        // ????????????????????????
                        BigDecimal merchantAmount = userService.getBalance(qrPayFlowDTO.getRecUserId(), StaticDataEnum.SUB_ACCOUNT_TYPE_1.getCode());
                        // ????????????0 ??????????????????????????????
                        if(merchantAmount.compareTo(BigDecimal.ZERO) == 0){
                            merchantService.updateHaveWholeSell(qrPayFlowDTO.getMerchantId(),0,request);
                        }

                    }catch (Exception e){
                        log.error("dealThirdSuccess ?????????????????????????????? ???id:"+qrPayFlowDTO.getId());
                    }
                }
            }

            try {
                // ???????????????????????????
                UserDTO payUser = userService.findUserById(qrPayFlowDTO.getPayUserId());
                userService.firstPaidSuccessAmountInV2(payUser.getId(), payUser.getInviterId(), StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode(), qrPayFlowDTO.getId(), request);
            }catch (Exception e){
                log.error("?????????????????????????????????,e:{}" , e);
            }
        }

    }

    /**
     * ????????????
     *
     * @param accountFlowDTO
     * @param qrPayFlowDTO
     * @param request
     * @throws BizException
     */
    @Override
    public void doAmountTrans(AccountFlowDTO accountFlowDTO, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException {
        //????????????
        JSONObject accTransObj = null;
        if (qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_3.getCode() ||
                qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_1.getCode()) {
            //?????????????????????
//            Map<String, Object> map = getAccTransMap(accountFlowDTO);
//            accTransObj = serverService.accountTransfer(JSONObject.parseObject(JSON.toJSONString(map)));
            return;
        } else {

            Map<String, Object> map = getAmountInMap(accountFlowDTO);
            try {
                accTransObj = serverService.amountIn(JSONObject.parseObject(JSON.toJSONString(map)));
            } catch (Exception e) {
                //????????????
                accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_33.getCode());
                updateFlow(qrPayFlowDTO, accountFlowDTO, null, request);
                throw new BizException(I18nUtils.get("recharge.doubtful", getLang(request)));
            }

        }


        if (accTransObj == null) {
            //??????????????????
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_33.getCode());
            updateFlow(qrPayFlowDTO, accountFlowDTO, null, request);
            throw new BizException(I18nUtils.get("recharge.doubtful", getLang(request)));
        }

        //??????????????????
        accountFlowDTO.setReturnCode(accTransObj.getString("code"));
        accountFlowDTO.setReturnMessage(accTransObj.getString("message"));
        qrPayFlowDTO.setErrorMessage(accTransObj.getString("message"));
        qrPayFlowDTO.setErrorCode(accTransObj.getString("code"));

        if (accTransObj.getString("code").equals(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode())) {
            //????????????
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
            updateFlow(qrPayFlowDTO, accountFlowDTO, null, request);
            throw new BizException(accTransObj.getString("message"));
        } else if (accTransObj.getString("code").equals(ResultCode.OK.getCode())) {
            //????????????
            accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
            updateFlow(qrPayFlowDTO, accountFlowDTO, null, request);
        } else {
            //????????????
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
     * ??????????????????
     */
    @Override
    public void qrPayAccountDoubtHandle() throws Exception {

        //????????????????????????
        List<QrPayFlowDTO> doubleList = qrPayFlowService.findAccountDoubleFlow();
        for (QrPayFlowDTO qrPayFlowDTO : doubleList) {
            //??????????????????
            Map<String,Object> params = new HashMap<>();
            params.put("flowId",qrPayFlowDTO.getId());
            int[]  transTypes = {StaticDataEnum.ACC_FLOW_TRANS_TYPE_3.getCode(),StaticDataEnum.ACC_FLOW_TRANS_TYPE_31.getCode()};
            int[]  states = {0,3};
            params.put("transTypes",transTypes);
            params.put("states",states);
            AccountFlowDTO accountFlowDTO = accountFlowDAO.selectByFlowId(params);
            try {
                if (accountFlowDTO.getId() ==null || accountFlowDTO.getState() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                    //????????????????????????????????????
                    //????????????
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                    updateFlow(qrPayFlowDTO, null, null, null);
                    continue;
                } else if (accountFlowDTO.getState() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                    //????????????
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
                    updateFlow(qrPayFlowDTO, null, null, null);
                    continue;
                }
                JSONObject data;

                data = serverService.transactionInfo(accountFlowDTO.getOrderNo().toString());

                if (data != null ) {
                    if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
                        //??????
                        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                        updateFlow(qrPayFlowDTO, accountFlowDTO, null, null);
                    } else if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
                        //??????
                        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                        updateFlow(qrPayFlowDTO, accountFlowDTO, null, null);
                    }
                }else{
                    // ????????????????????????
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                    accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    updateFlow(qrPayFlowDTO, accountFlowDTO, null, null);
                }
            } catch (Exception e) {
                //????????????????????????????????????
                continue;
            }


        }
    }

    /**
     * ??????????????????
     */
    @Override
    public void qrPayAccountFailHandle() throws Exception {
        {

            //??????????????????????????????
            List<QrPayFlowDTO> failList = qrPayFlowService.findAccountFailFlow();
            for (QrPayFlowDTO qrPayFlowDTO : failList) {
                AccountFlowDTO accountFlowDTO = getAccountFlowDTO(qrPayFlowDTO);
                try {
                    //??????????????????????????????
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_30.getCode());
                    updateFlow(qrPayFlowDTO, null, null, null);
                    //????????????????????????
                    accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO, null));

                } catch (Exception e) {
                    //????????????
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                    updateFlow(qrPayFlowDTO, null, null, null);
                    continue;
                }
                //????????????
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
     * ??????????????????
     */
    @Override
    public void qrPayThirdDoubtHandle() throws Exception {
        List<QrPayFlowDTO> doubtList = qrPayFlowService.findThirdDoubtFlow();
        //??????????????????
        for(QrPayFlowDTO qrPayFlowDTO:doubtList){

            dealOneThirdDoubtHandle(qrPayFlowDTO);

        }


    }

    @Override
    public Integer dealOneThirdDoubtHandle(QrPayFlowDTO qrPayFlowDTO) {
        try{
            Map<String,Object> params = new HashMap<>();
            params.put("flowId",qrPayFlowDTO.getId());
            //??????????????????
            if(qrPayFlowDTO.getGatewayId() == null ){
                //?????????
            }else{

                JSONObject cardObj = null;
                try {
                    cardObj = serverService.getCardInfo(Long.parseLong(qrPayFlowDTO.getCardId()));
                } catch (Exception e) {
                    log.error("??????????????????????????????????????????,flow_id:"+qrPayFlowDTO.getId()+",error:"+e.getMessage(),e);
                }

                WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(params);
                if(withholdFlowDTO.getId()==null || withholdFlowDTO.getState() == StaticDataEnum.TRANS_STATE_2.getCode()){

                    Integer payState = (cardObj == null || StringUtils.isBlank(cardObj.toJSONString())) ? null : cardObj.getInteger("payState");
                    //??????
                    handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, withholdFlowDTO.getOrderAmount(), payState, null);

                    return  StaticDataEnum.TRANS_STATE_2.getCode();
                    //????????????
                        /*qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_22.getCode());
                        rechargeFlowService.channelLimitRollback(qrPayFlowDTO.getCreatedDate(),qrPayFlowDTO.getGatewayId(),withholdFlowDTO.getTransAmount(),null);
                        updateFlow(qrPayFlowDTO, null, null, null);*/

                }else if(withholdFlowDTO.getState() == StaticDataEnum.TRANS_STATE_1.getCode()){

                    //???????????????????????????????????????????????????????????????
                    QrPayFlowDTO qrPayFlowDTO_  = qrPayFlowService.findQrPayFlowById(qrPayFlowDTO.getId());
                    if(qrPayFlowDTO_.getState() == StaticDataEnum.TRANS_STATE_20.getCode() ||
                            qrPayFlowDTO_.getState() == StaticDataEnum.TRANS_STATE_23.getCode() ){
                        //????????????????????????
                        dealThirdSuccess(withholdFlowDTO,qrPayFlowDTO,null);
                        //??????
                        if(null != qrPayFlowDTO.getMarketingId() && null != qrPayFlowDTO.getMarketingManageId()){
                            //??????????????????
                            HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
                            paramMap.put("id", qrPayFlowDTO.getMarketingManageId());
                            marketingManagementService.addUsedNumber(paramMap);
                        }
                        try{

                            if(null != cardObj.getInteger("payState") && cardObj.getInteger("payState") == StaticDataEnum.STATUS_0.getCode()){
                                // ???????????????????????????
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("cardId", qrPayFlowDTO.getCardId());
                                serverService.setCardSuccessPay(jsonObject);
                            }
                        }catch (Exception e){
                            log.error("???????????????????????????,e:{},userId",e);
                        }
                        return  StaticDataEnum.TRANS_STATE_1.getCode();
                    }
                }
                if (StaticDataEnum.GATEWAY_TYPE_0.getCode() == withholdFlowDTO.getGatewayId().intValue()){
                    //latpay??????
                    withholdFlowDTO = latPayService.latPayDoubtHandle(withholdFlowDTO);
                } else if (StaticDataEnum.GATEWAY_TYPE_1.getCode() == withholdFlowDTO.getGatewayId().intValue()
                        || StaticDataEnum.GATEWAY_TYPE_2.getCode() == withholdFlowDTO.getGatewayId().intValue()) {
                    //????????????????????????
                    Integer orderStatus = omiPayService.statusCheck(withholdFlowDTO.getOmiPayOrderNo());
                    withholdFlowDTO.setState(orderStatus);
                }else if(StaticDataEnum.GATEWAY_TYPE_4.getCode() == withholdFlowDTO.getGatewayId().intValue()){
                    String token = integraPayService.apiAccessToken();
                    int state = integraPayService.payByCardStatusCheck(withholdFlowDTO.getGatewayMerchantId(),withholdFlowDTO.getOrdreNo(),token);
                    withholdFlowDTO.setState(state);
                }else if(StaticDataEnum.GATEWAY_TYPE_8.getCode() == withholdFlowDTO.getGatewayId().intValue()){
                    // stripe ?????? todo
                    int state = this.retrievePaymentIntent(withholdFlowDTO.getStripeId(), withholdFlowDTO);
                    withholdFlowDTO.setState(state);
                }
//                    withholdFlowDTO.setState(2);

                if (StaticDataEnum.TRANS_STATE_1.getCode() == withholdFlowDTO.getState()) {
                    //????????????????????????
                    //?????????????????????????????????????????????
//                        dealThirdSuccess(withholdFlowDTO,qrPayFlowDTO,null);
                    //??????
                    dealThirdSuccess(withholdFlowDTO,qrPayFlowDTO,null);


                    try{
                        if(null != cardObj.getInteger("payState") && cardObj.getInteger("payState") == StaticDataEnum.STATUS_0.getCode()){
                            // ???????????????????????????
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("cardId", qrPayFlowDTO.getCardId());
                            serverService.setCardSuccessPay(jsonObject);
                        }
                    }catch (Exception e){
                        log.error("???????????????????????????,e:{},userId", e);
                    }

                    if(null != qrPayFlowDTO.getMarketingId() && null != qrPayFlowDTO.getMarketingManageId()){
                        //??????????????????
                        HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
                        paramMap.put("id", qrPayFlowDTO.getMarketingManageId());
                        marketingManagementService.addUsedNumber(paramMap);
                    }
                    return withholdFlowDTO.getState();
                } else if (StaticDataEnum.TRANS_STATE_2.getCode() == withholdFlowDTO.getState()) {

                    Integer payState = (cardObj == null || StringUtils.isBlank(cardObj.toJSONString())) ? null : cardObj.getInteger("payState");
                    //??????
                    handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, withholdFlowDTO.getOrderAmount(), payState, null);
//                        dealThirdFail(withholdFlowDTO, qrPayFlowDTO, null);
                    return withholdFlowDTO.getState();
                } else {
                    //??????
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_23.getCode());
                    updateFlow(qrPayFlowDTO, null, withholdFlowDTO, null);
                    return StaticDataEnum.TRANS_STATE_3.getCode();
                }
            }

        }catch (Exception e){
            log.info("QrPayService.qrPayThirdDoubtHandle,????????????:"+e.getMessage(),e);

        }
        return StaticDataEnum.TRANS_STATE_3.getCode();
    }

    private int retrievePaymentIntent(String stripeId, WithholdFlowDTO withholdFlowDTO) {

        // stripe ??????????????????
        StripeAPIResponse response = stripeService.retrievePaymentIntent( stripeId );
        if(response == null ){
            // ??????????????????
            withholdFlowDTO.setErrorMessage("Failed to request the Stripe API");

            return StaticDataEnum.TRANS_STATE_2.getCode();
        }


        if(response.isSuccess()  ){

            PaymentIntent paymentIntent = (PaymentIntent)response.getData();

            withholdFlowDTO.setReturnMessage(paymentIntent.getStatus());

            // ??????????????????
            if(StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_REQUIRES_ACTION.getCode().equals(paymentIntent.getStatus())){
                // 3ds??????,??????3ds?????????
                return StaticDataEnum.TRANS_STATE_44.getCode();
            }else if(StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_SUCCEEDED.getCode().equals(paymentIntent.getStatus())){
                // ????????????
                return StaticDataEnum.TRANS_STATE_1.getCode();
            }else if(StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_CANCELED.getCode().equals(paymentIntent.getStatus())
                    || StripeAPICodeEnum.PAYMENTINTENT_RES_STATUS_REQUIRES_PAYMENT_METHOD.getCode().equals(paymentIntent.getStatus())){

                withholdFlowDTO.setErrorMessage(paymentIntent.getLastPaymentError().getMessage());
                withholdFlowDTO.setReturnCode(paymentIntent.getLastPaymentError().getCode());
                // ????????????
                withholdFlowDTO.setErrorMessage(paymentIntent.getStatus());

                return StaticDataEnum.TRANS_STATE_2.getCode();
            }else{
                // ???????????????????????????
                return StaticDataEnum.TRANS_STATE_3.getCode();
            }
        }else{
            // ??????????????????
            withholdFlowDTO.setErrorMessage(response.getMessage());

            return StaticDataEnum.TRANS_STATE_2.getCode();
        }
//        return StaticDataEnum.TRANS_STATE_44.getCode();
    }


    /**
     * ??????????????????
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

        //????????????????????????
        AccountFlowDTO accountFlowDTO = getAccountFlowDTO(qrPayFlowDTO);
        try {
            //????????????????????????
            accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO, request));
        } catch (Exception e) {
            //????????????
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
            updateFlow(qrPayFlowDTO, null, null, request);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }
        //????????????
        doAmountTrans(accountFlowDTO, qrPayFlowDTO, request);



    }


    /**
     * ????????????????????????
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
            //??????pos_qr_pay ????????????
            if(null != qrPayFlowDTO.getPosOrder() && qrPayFlowDTO.getPosOrder()){
                try {
                    updatePosOrderStatus(qrPayFlowDTO.getTransNo(), qrPayFlowDTO.getState(), request);
                }catch (BizException e){
                    log.error("??????POS ?????????????????????????????????transNo: " + qrPayFlowDTO.getTransNo() + "|| state:" + qrPayFlowDTO.getState());
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

            //??????pos_qr_pay ????????????
            if(null != qrPayFlowDTO.getPosOrder() && qrPayFlowDTO.getPosOrder()){
                try {
                    updatePosOrderStatus(qrPayFlowDTO.getTransNo(), qrPayFlowDTO.getState(), request);
                }catch (BizException e){
                    log.error("??????POS ?????????????????????????????????transNo: " + qrPayFlowDTO.getTransNo() + "|| state:" + qrPayFlowDTO.getState());
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

        // ?????????????????????????????????????????????????????????
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
        //?????????
        withholdFlowDTO.setGatewayMerchantId(gatewayDTO.getChannelMerchantId());
        withholdFlowDTO.setGatewayMerchantPassword(gatewayDTO.getPassword());
//        }else{
//            //????????????????????????
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
        //????????????ISO??????
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
     * ????????????????????????
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

        // ?????????????????????????????????????????????????????????
//        if (qrPayFlowDTO.getGatewayId().equals(new Long(StaticDataEnum.GATEWAY_TYPE_0.getCode())) && qrPayFlowDTO.getFeeDirection() == StaticDataEnum.FEE_DIRECTION_1.getCode()) {
////            withholdFlowDTO.setTransAmount(qrPayFlowDTO.getPayAmount());
////        } else {
////            withholdFlowDTO.setTransAmount(qrPayFlowDTO.getPayAmount());
////        }
        //??????????????? ????????????
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
        //?????????
        withholdFlowDTO.setGatewayMerchantId(gatewayDTO.getChannelMerchantId());
        withholdFlowDTO.setGatewayMerchantPassword(gatewayDTO.getPassword());
//        }else{
//            //????????????????????????
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
        //????????????ISO??????
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
            //??????
            accountFlowDTO.setUserId(qrPayFlowDTO.getPayUserId());
            accountFlowDTO.setOppositeUserId(qrPayFlowDTO.getRecUserId());
            accountFlowDTO.setFee(qrPayFlowDTO.getFee());
            accountFlowDTO.setFeeDirection(qrPayFlowDTO.getFeeDirection());
            accountFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_3.getCode());
        } else {
            //??????
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
        // ????????????
        MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayFlowDTO.getMerchantId());
        //???????????????????????????????????????
        log.info("check qr pay flow dto status, qrPayFlowDTO:{}", qrPayFlowDTO);
        if (qrPayFlowDTO.getState() != StaticDataEnum.TRANS_STATE_31.getCode()) {
            //??????????????????(22)??????????????????
            if (qrPayFlowDTO.getState() == StaticDataEnum.TRANS_STATE_22.getCode()) {
                result.put("state", StaticDataEnum.TRANS_STATE_2.getCode());
                result.put("msg", I18nUtils.get("trans.failed", getLang(request)));
                return result;
            } else if(qrPayFlowDTO.getState() == StaticDataEnum.TRANS_STATE_20.getCode() || qrPayFlowDTO.getState() == StaticDataEnum.TRANS_STATE_23.getCode()){
                //??????????????????(20,23??????????????????????????????)
                Map<String, Object> params = new HashMap<>(1);
                params.put("flowId", flowId);
                WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(params);
                //??????????????????????????????????????????????????????????????????
                Integer orderStatus = omiPayService.statusCheck(withholdFlowDTO.getOmiPayOrderNo());
                if (orderStatus == StaticDataEnum.TRANS_STATE_1.getCode()) {
                    withholdFlowDTO.setState(orderStatus);
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_21.getCode());
                    log.info("update success status flow dto, qrPayFlowDTO:{}, withholdFlowDTO:{}", qrPayFlowDTO, withholdFlowDTO);
                    try{
                        updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
                    }catch (Exception e){
                        log.info("QrPayService.AliOrWechatOrderStatusCheck??????????????????????????????"+e.getMessage(),e);
                        //????????????????????????,???????????????????????????????????????????????????
                        QrPayFlowDTO qrPayFlowDTO_ = qrPayFlowService.findQrPayFlowById(flowId);
                        if(qrPayFlowDTO_.getState() == StaticDataEnum.TRANS_STATE_31.getCode()){
                            result = getResult(result, StaticDataEnum.TRANS_STATE_1.getCode(), qrPayFlowDTO, merchantDTO, I18nUtils.get("trans.success", getLang(request)), request);
                            return result;
                        }else{
                            result = getResult(result, StaticDataEnum.TRANS_STATE_0.getCode(), qrPayFlowDTO, merchantDTO, I18nUtils.get("trans.doubtful", getLang(request)), request);
                            return result;
                        }

                    }
                    //????????????????????????
                    AccountFlowDTO accountFlowDTO = getAccountFlowDTO(qrPayFlowDTO);
                    try {
                        //????????????????????????
                        accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO, request));
                    } catch (Exception e) {
                        //????????????
                        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                        updateFlow(qrPayFlowDTO, null, null, request);
                        log.info("account amount in failed, data:{}, error message:{}, e:{}", accountFlowDTO, e.getMessage(), e);
                        //?????????????????????omipay??????
                        result.put("state", StaticDataEnum.TRANS_STATE_1.getCode());
                        result.put("msg", "");
                        return result;
                    }
                    doAmountTrans(accountFlowDTO, qrPayFlowDTO, request);
                    // ????????????????????????
                    log.info("send success message");
                    sendMessage(qrPayFlowDTO.getRecUserId(), qrPayFlowDTO.getId(), request);
                    result = getResult(result, StaticDataEnum.TRANS_STATE_1.getCode(), qrPayFlowDTO, merchantDTO, "", request);
                    return result;
                } else if (orderStatus == StaticDataEnum.TRANS_STATE_2.getCode()) {
                    //????????????
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
                //???????????????????????????
                result = getResult(result, StaticDataEnum.TRANS_STATE_0.getCode(), qrPayFlowDTO, merchantDTO, I18nUtils.get("trans.doubtful", getLang(request)), request);
                return result;
            }
        }
        result = getResult(result, StaticDataEnum.TRANS_STATE_1.getCode(), qrPayFlowDTO, merchantDTO, I18nUtils.get("trans.success", getLang(request)), request);
        return result;
    }

    /**
     * ????????????
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
            //??????????????????
            String[]  param ={orderId+""} ;
            //????????????
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
     * ????????????
     * @param qrPayFlowDTO
     * @return
     */
    @Override
    public QrPayFlowDTO calculation(QrPayDTO qrPayDTO, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException {
        UserDTO recUser = userService.findUserById(qrPayDTO.getRecUserId());
        MerchantDTO merchantDTO = merchantService.findMerchantById(qrPayDTO.getMerchantId());
        //?????????????????????????????????????????????????????????????????????
        if (recUser.getUserType() == StaticDataEnum.USER_TYPE_20.getCode()) {
            BigDecimal payFee = null;
            BigDecimal payAmount = null;
            BigDecimal recAmount = null;
            BigDecimal platformFee = null;
            //????????????
            BigDecimal discountedAmount = null;

            ParametersConfigDTO parametersConfigDTO = parametersConfigService.findParametersConfigById(1L);
            //???????????? =  ???????????? * ????????????
            discountedAmount = qrPayDTO.getTransAmount().multiply(MathUtils.subtract(new BigDecimal(1), qrPayDTO.getPayDiscountRate())).setScale(2, RoundingMode.HALF_UP);
            //??????????????? ???????????? * ??????????????????
            payFee = discountedAmount.multiply(qrPayDTO.getRate()).setScale(2, RoundingMode.HALF_UP);

            //???????????? ???????????? + ?????????????????????????????????????????????????????????
            if (qrPayDTO.getFeeDirection() == StaticDataEnum.FEE_DIRECTION_1.getCode()) {
                payAmount = discountedAmount.add(payFee) ;
            } else {
                payAmount = discountedAmount;
            }

            //???????????????????????????????????????????????????????????????
            if (payAmount.compareTo(qrPayDTO.getTrulyPayAmount()) != 0) {
                log.info("pay amount compare, getPayAmount:{}, selfPayAmount:{}", qrPayDTO.getTrulyPayAmount(), payAmount);
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }
            //???????????? ???????????? * ???1 - ??????????????? + ???????????? + ???????????? + ?????????????????? - ??????????????????????????????????????????????????????
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
            //??????????????? ???????????? - ????????????
            platformFee = payAmount.subtract(recAmount).setScale(2, RoundingMode.HALF_UP);
            //???????????????????????????
            qrPayFlowDTO.setPayAmount(payAmount);
            qrPayFlowDTO.setRecAmount(recAmount);
            qrPayFlowDTO.setFee(payFee);
            qrPayFlowDTO.setPlatformFee(platformFee);
        }
        return qrPayFlowDTO;
    }

    /**
     * ??????????????????
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
     * ????????????????????????
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
                //????????????????????? ?????????????????????????????????????????????????????? ???????????????
                return false;
            }
        }
        return true;
    }

    /**
     * ??????POS????????????
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
        log.info("=======??????POS????????????  transNo:" + transNo + "| orderStatus:" + orderStatus);
        PosQrPayFlowDTO updateRecord = new PosQrPayFlowDTO();
        if(orderStatus.equals(StaticDataEnum.TRANS_STATE_31.getCode()) || orderStatus.equals(StaticDataEnum.TRANS_STATE_1.getCode())){
            //????????????
            updateRecord.setOrderStatus(StaticDataEnum.POS_ORDER_STATUS_SUCCESS.getCode());
            updateRecord.setPayDate(System.currentTimeMillis());
            posQrPayFlowService.updatePosQrPayFlow(posQrPayFlow.getId(), updateRecord, request);
        }else if(orderStatus.equals(StaticDataEnum.TRANS_STATE_2.getCode()) || orderStatus.equals(StaticDataEnum.TRANS_STATE_22.getCode())){
            //????????????
            updateRecord.setOrderStatus(StaticDataEnum.POS_ORDER_STATUS_FAIL.getCode());
            posQrPayFlowService.updatePosQrPayFlow(posQrPayFlow.getId(), updateRecord, request);
        }else{
            //????????????
            updateRecord.setOrderStatus(StaticDataEnum.POS_ORDER_STATUS_SUSPICIOUS.getCode());
            posQrPayFlowService.updatePosQrPayFlow(posQrPayFlow.getId(), updateRecord, request);
        }

    }

    /**
     *  ???????????? ??????????????????
     * @author zhangzeyuan
     * @date 2021/5/13 13:21
     * @param borrowId
     * @param request
     */
    public void sendTransactionMail(String borrowId, HttpServletRequest request) throws Exception{
        //?????????????????????????????????
        JSONObject queryParam = new JSONObject(1);
        queryParam.put("borrowId", borrowId);
        String url =  creditMerchantUrl +  "/payremote/getTransactionRecordEmailData";
        String responseResult = HttpClientUtils.sendPost(url, queryParam.toJSONString());
        if(StringUtils.isBlank(responseResult)){
            log.error("???????????????????????????????????????, CreditOrderNo:" + borrowId);
            return;
        }
        JSONObject responseJsonObj = JSONObject.parseObject(responseResult);
        if(!ErrorCodeEnum.SUCCESS_CODE.getCode().equals(responseJsonObj.getString("code"))){
            log.error("???????????????????????????????????????, CreditOrderNo:" + borrowId + "||response data:"+ responseJsonObj.toString());
            return;
        }
        //??????
        JSONObject borrowJsonData = responseJsonObj.getJSONObject("data");
        if(null == borrowJsonData){
            log.error("???????????????????????????????????????, CreditOrderNo:" + borrowId + "||response data:"+ responseJsonObj.toString());
            return;
        }

        //?????????dto
        TransactionRecordEmailDataDTO borrowMailDto = JSONObject.parseObject(borrowJsonData.toJSONString(), TransactionRecordEmailDataDTO.class);
        if(Objects.isNull(borrowMailDto) || Objects.isNull(borrowMailDto.getId()) || StringUtils.isBlank(borrowMailDto.getEmail())){
            log.error("???????????????????????????????????????, CreditOrderNo:" + borrowId + "||response data:"+ responseJsonObj.toString());
            return;
        }

        //?????????
        String merchantName = borrowMailDto.getMerchantName();
        //????????????
        String email = borrowMailDto.getEmail();

        //??????????????????
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

        //?????????
        String userName = borrowMailDto.getUserName();
        //??????
        //??????????????????????????? ??? ???
        //        13:38:23 13/05/2021

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        Date parse = simpleDateFormat.parse(borrowMailDto.getCreatedDate());
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
        String titleDate = simpleDateFormat1.format(parse);

        //?????????
        BigDecimal borrowAmount = borrowMailDto.getBorrowAmount();
        //????????????
        BigDecimal leftAmount = BigDecimal.ZERO;

        //????????????
        JSONArray repayJsonArray = borrowJsonData.getJSONArray("repayList");

        log.info("repayJsonArray", repayJsonArray.toJSONString());

        List<TransactionRecordEmailDataDTO> repayList = JSONArray.parseArray(repayJsonArray.toJSONString(), TransactionRecordEmailDataDTO.class);
        String instalmentsHtml = " ";
        if(CollectionUtils.isNotEmpty(repayList)){
            for(int i = 0; i < repayList.size(); i++ ){
                TransactionRecordEmailDataDTO repay = repayList.get(i);
                if(i == 0){
                    //??????
                    instalmentsHtml +=  "$" + repay.getPaidAmount().toString() + "(25% already paid)" + "<br>";
                }else {
                    instalmentsHtml +=  "$" + repay.getShouldPayAmount().toString() + " due " + repay.getExpectRepayTime() + "<br>";
                }
            }

            leftAmount = borrowAmount.subtract(repayList.get(0).getPaidAmount());
        }
        //??????????????????
        MailTemplateDTO mailTemplate = mailTemplateService.findMailTemplateBySendNode(String.valueOf(StaticDataEnum.SEND_NODE_31.getCode()));
        if(Objects.isNull(mailTemplate) || StringUtils.isBlank(mailTemplate.getEnSendContent())){
            log.error("???????????????????????? ????????????????????????");
            return;
        }

        String enSendContent = mailTemplate.getEnSendContent();
        String enMailTheme = mailTemplate.getEnMailTheme();

        //??????????????????
        String[] titleParam = {merchantName, titleDate};
        //????????????
        String sendContent = null;
        String sendTitle = null;

//        sendContent = templateContentReplace(contentParam, enSendContent);
        sendContent = enSendContent.replace("{merchantName}", merchantName).replace("{location}", fullAddress).replace("{userName}", userName).replace("borrowAmount", "$" + borrowAmount.toString())
                .replace("{repayList}", instalmentsHtml).replace("{leftAmount}", leftAmount.toString());

        sendTitle = templateContentReplace(titleParam, enMailTheme);


        //????????????
        Session session = MailUtil.getSession(sysEmail);
        MimeMessage mimeMessage = MailUtil.getMimeMessage(StaticDataEnum.U_WALLET.getMessage(), sysEmail, email, sendTitle, sendContent, null, session);
        MailUtil.sendMail(session, mimeMessage, sysEmail, sysEmailPwd);

        //??????????????????
        MailLogDTO mailLogDTO = new MailLogDTO();
        mailLogDTO.setAddress(email);
        mailLogDTO.setContent(sendContent);
        mailLogDTO.setSendType(0);
        mailLogService.saveMailLog(mailLogDTO, request);
    }



    /**
     * ??????????????????
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
        // ?????? userInfo cardInfo payAmount
        JSONObject userInfo = param.getJSONObject("userInfo");
        JSONObject cardInfo = param.getJSONObject("cardInfo");
        BigDecimal amount = param.getBigDecimal("amount");
        // ????????????
        if (userInfo==null||cardInfo==null||amount==null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        Long userId = userInfo.getLong("userId");
        // ?????????????????????????????????
        UserDTO userById = userService.findUserById(userId);//412082561924812800
        if (userById==null){
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }
        // ???????????? getwaytype=0 state=1
        Map<String,Object> gateWayMap = new HashMap<>();
        gateWayMap.put("gatewayType",StaticDataEnum.GATEWAY_TYPE_0.getCode());
        gateWayMap.put("state",StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(gateWayMap);
        if (gatewayDTO.getId()==null){
            throw new BizException(I18nUtils.get("gateway.disabled", getLang(request)));
        }
        // ?????????????????? latPay???????????????????????????
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
        // ????????????????????????????????????withHoldFlow ??????????????????0 ????????? ????????????
        WithholdFlowDTO withholdFlowDTO = this.getWithholdFlowDTO(param,request,userInfo,cardInfo,amount,gatewayDTO);

//        // ????????????????????????
//        rechargeFlowService.channelLimit(gatewayDTO.getType(),withholdFlowDTO.getTransAmount(),request);
        // ??????????????????????????????
        withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        if(withholdFlowDTO.getTransAmount().compareTo(BigDecimal.ZERO)>0){
            //??????????????????
            try{

                withholdFlowDTO = latPayService.latPayRequest(withholdFlowDTO,withholdFlowDTO.getTransAmount(),withholdFlowDTO.getFee(),cardInfo,request,getIp(request));

            }catch (Exception e){
                //??????????????????????????????
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
            }
        }else{
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
            withholdFlowDTO.setCheckState( StaticDataEnum.IS_CHECK_1.getCode());
            withholdFlowDTO.setOrdreNo(null);
        }
        // ??????????????????????????????
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
            // todo ?????????????????????
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
            returnData.put("code", ErrorCodeEnum.FAIL_CODE.getCode());
        }
        withholdFlowService.updateWithholdFlowForConcurrency(withholdFlowDTO.getId(), withholdFlowDTO, request);
        returnData.put("displayData",this.packCreditRepayResData(withholdFlowDTO.getId(),request));
        return returnData;
    }




    @Override
    public JSONObject doPayByCardV2(JSONObject param, HttpServletRequest request) throws Exception {
        // ?????? userInfo cardInfo payAmount
        JSONObject userInfo = param.getJSONObject("userInfo");
        JSONObject cardInfo = param.getJSONObject("cardInfo");
        BigDecimal amount = param.getBigDecimal("amount");
        // ????????????
        if (userInfo==null||cardInfo==null||amount==null){
            throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
        }
        Long userId = userInfo.getLong("userId");
        // ?????????????????????????????????
        UserDTO userById = userService.findUserById(userId);//412082561924812800
        if (userById==null){
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }
        // ???????????? getwaytype=0 state=1
        // ??????token?????????????????????
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


        // ?????????????????? latPay???????????????????????????
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
        // ????????????????????????????????????withHoldFlow ??????????????????0 ????????? ????????????
        WithholdFlowDTO withholdFlowDTO = this.getWithholdFlowDTO(param,request,userInfo,cardInfo,amount,gatewayDTO);

        // ??????????????????????????????
        withholdFlowDTO.setId(withholdFlowService.saveWithholdFlow(withholdFlowDTO, request));
        if(withholdFlowDTO.getTransAmount().compareTo(BigDecimal.ZERO)>0){
            if( type == StaticDataEnum.GATEWAY_TYPE_0.getCode()){
                // Latpay??????????????????
                try{
                    withholdFlowDTO = latPayService.latPayRequest(withholdFlowDTO,withholdFlowDTO.getTransAmount(),withholdFlowDTO.getFee(),cardInfo,request,getIp(request));
                }catch (Exception e){
                    //??????????????????????????????
                    withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                    throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
                }
            }else if ( type == StaticDataEnum.GATEWAY_TYPE_8.getCode()){
                // stripe??????????????????
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
                    // ????????????
                    Charge charge = (Charge)stripeAPIResponse.getData();
                    withholdFlowDTO.setStripeId(charge.getId());
                    if(charge == null || StringUtils.isEmpty(charge.getStatus())){
                        withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                    }
                    if(StripeAPICodeEnum.CHARGE_RES_STATUS_SUCCEEDED.getMessage().equals(charge.getStatus())){
                        // ??????
                        withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                    }else if(StripeAPICodeEnum.CHARGE_RES_STATUS_FAILED.getMessage().equals(charge.getStatus())||
                            StripeAPICodeEnum.CHARGE_RES_STATUS_CANCELED.getMessage().equals(charge.getStatus())){
                        // ??????
                        withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                    }else{
                        // ?????????
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
        // ??????????????????????????????
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
            // ???????????????????????????
            withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
            returnData.put("code", ErrorCodeEnum.LAT_PAY_DOUBLE.getCode());
        }
        withholdFlowService.updateWithholdFlowForConcurrency(withholdFlowDTO.getId(), withholdFlowDTO, request);
        returnData.put("displayData",this.packCreditRepayResData(withholdFlowDTO.getId(),request));
        return returnData;
    }



    private WithholdFlowDTO getWithholdFlowDTO(JSONObject requestInfo, HttpServletRequest request, JSONObject userInfo, JSONObject cardInfo, BigDecimal amonut, GatewayDTO gatewayDTO) {
        //?????????????????????
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
        //????????????ISO??????
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
        //??????????????? ????????????+?????????
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
        log.info("????????????????????????????????????????????????");
        // ?????????????????? TRANS_STATE_3 ACC_FLOW_TRANS_TYPE_32 letpay
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
                    // ?????????latpay?????????latpay?????????
                    withholdFlowDTO = latPayService.latPayDoubtHandle(withholdFlowDTO);
                    thirdId =  withholdFlowDTO.getLpsTransactionId();
                }else if (Integer.parseInt(withholdFlowDTO.getGatewayId()+"") == StaticDataEnum.GATEWAY_TYPE_8.getCode()){
                    // ?????????stripe?????????stripe?????????

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
                                    // ??????
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
                    // ???????????????
                    this.transTypeToCredit(map);
                }
            }catch (Exception e){
                log.error("?????????????????????????????????????????????????????? id:{}",withholdFlowDTO.getId());
            }
        }

    }
    /**
     * ???????????????
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
        //??????????????????????????????
        List<QrPayFlowDTO> qrPayFlowList = qrPayFlowService.getSuspiciousOrderFlowList();
        for(QrPayFlowDTO qrPayFlowDTO: qrPayFlowList){
            this.dealOneCreditFirstCardPayDoubt(qrPayFlowDTO);
        }
    }

    @Override
    public int dealOneCreditFirstCardPayDoubt(QrPayFlowDTO qrPayFlowDTO) {

        HashMap<String, Object> queryParamMap = Maps.newHashMapWithExpectedSize(1);
        JSONObject cardJsonObj = new JSONObject();
        log.info("?????????????????????????????????",qrPayFlowDTO.getId());
        queryParamMap.put("flowId", qrPayFlowDTO.getId());
        //??????????????????
        try{
            WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(queryParamMap);
            //?????????
            if(null == withholdFlowDTO || null == withholdFlowDTO.getId()){
                //?????????????????? ,???????????????????????????
                handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, withholdFlowDTO.getOrderAmount(), null, null);
                return StaticDataEnum.TRANS_STATE_2.getCode();
            }

            if(Integer.parseInt(withholdFlowDTO.getGatewayId() + "") == StaticDataEnum.GATEWAY_TYPE_0.getCode()){
                //?????? latpay????????????
                withholdFlowDTO = latPayService.latPayDoubtHandle(withholdFlowDTO);
//                log.info("withholdFlowDTO  state?????????",withholdFlowDTO.getState());
            }else if(Integer.parseInt(withholdFlowDTO.getGatewayId() + "") == StaticDataEnum.GATEWAY_TYPE_8.getCode()){

                int state = this.retrievePaymentIntent(withholdFlowDTO.getStripeId(), withholdFlowDTO);
                withholdFlowDTO.setState( state);
            }


            //????????????????????????????????????
            cardJsonObj.put("id", qrPayFlowDTO.getCardId());

            //?????????25%??????????????? ??????????????????
            BigDecimal cardPayRealAmount =  withholdFlowDTO.getOrderAmount().subtract(qrPayFlowDTO.getTipAmount()).subtract(qrPayFlowDTO.getDonationAmount());
            //???????????????????????????
            BigDecimal installAmount = qrPayFlowDTO.getPayAmount().subtract(cardPayRealAmount);

            try{
                handleCreditCardPayDataByThirdStatusV3(withholdFlowDTO, qrPayFlowDTO, cardJsonObj, withholdFlowDTO.getTransAmount(), cardPayRealAmount,
                        installAmount, withholdFlowDTO.getFeeRate(), withholdFlowDTO.getFee(), null);
            }catch (BizException be){
                log.info("?????????????????????"+withholdFlowDTO.getState());
                return  withholdFlowDTO.getState();
            }catch (Exception e){
                log.info("?????????????????????,?????????????????????"+e.getMessage());
                return  withholdFlowDTO.getState();
            }

            // ???????????????????????????
            UserDTO payUser = userService.findUserById(qrPayFlowDTO.getPayUserId());
            userService.firstPaidSuccessAmountInV2(payUser.getId(), payUser.getInviterId(), StaticDataEnum.ACC_FLOW_TRANS_TYPE_19.getCode(), qrPayFlowDTO.getId(), null);
            return  withholdFlowDTO.getState();
        }catch (Exception e){
            log.error("?????????????????????????????????,e:{}" , e);
            return StaticDataEnum.TRANS_STATE_3.getCode();
        }

    }

    /**
     * ???????????????????????????????????????
     *
     * @author zhangzeyuan
     * @date 2021/7/7 14:47
     */
    @Override
    public void creditRollbackAmountDoubtHandle() throws Exception {
        //???????????????????????????????????????
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
            //????????????????????? ????????????
            postParamJson.clear();
            dataJson.clear();

            //????????????
            dataJson.put("userId", flow.getUserId());
            dataJson.put("flowId", flow.getQrPayFlowId());
            postParamJson.put("data", dataJson);
            try {
                String resultData = HttpClientUtils.post(url, postParamJson.toJSONString());
                JSONObject resultDataJson = JSONObject.parseObject(resultData);
                String code = resultDataJson.getString("code");
                if(code != null && code.equals(ResultCode.OK.getCode())){
                    //????????????????????????
                    flow.setState(StaticDataEnum.PAY_BALANCE_FLOW_STATE_1.getCode());
                    payCreditBalanceFlowService.updatePayCreditBalanceFlow(flow.getId(),  flow,  null);
                }
            }catch (Exception e){
                log.error("????????????????????????" + flow.getQrPayFlowId(),e.getMessage());
            }
        }
    }

    /**
     * ?????????????????????????????????
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
            //?????????????????????
            if(qrPayFlow.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                Integer orderState = createCreditInstallmentOrder(qrPayFlow, flow.getCardPayRate(), flow.getCardPayAmount(), flow.getCardFeeAmount(), flow.getCardPayAmount().add(flow.getCardFeeAmount()), cardObj, null);

                //???????????????
                if(orderState.equals(StaticDataEnum.API_ORDER_STATE_1.getCode())){

                    flow.setState(StaticDataEnum.CREATE_CREDIT_ORDER_FLOW_STATE_1.getCode());
                    createCreditOrderFlowService.updateCreateCreditOrderFlow(flow.getId(), flow, null);
                    //??????
                    handleLatPaySuccess(withholdFlowDTO, qrPayFlow, null);

                    //????????????????????????
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("cardId", qrPayFlow.getCardId());
                    try{
                        userService.presetCard(jsonObject, qrPayFlow.getPayUserId(),null);
                    }catch (Exception e){
                        log.error("????????????????????????????????? ????????????????????????????????????????????????,e:{}",e);
                    }

                    //POS API ????????????????????????
                    /*try {
                        log.info("????????????????????????????????????");
                        posApiService.posPaySuccessNotice(qrPayFlow.getMerchantId(), qrPayFlow.getTransNo(), null);
                    }catch (PosApiException e){
                        log.error("POS????????????" + e.getMessage());
                    }*/

                    //??????????????????????????? ??????
                    try{
                        sendTransactionMail(qrPayFlow.getCreditOrderNo(), null);
                    }catch (Exception e){
                        log.error("???????????????????????????????????????, CreditOrderNo:" + qrPayFlow.getCreditOrderNo() + " || message:" + e.getMessage());
                    }
                }
            }
        }
    }


}
