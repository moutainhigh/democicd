package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.collect.Maps;
import com.stripe.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.uwallet.pay.core.common.*;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.util.*;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.LatPayErrorEnum;
import com.uwallet.pay.main.constant.SignErrorCode;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.ApiQrPayFlowDAO;
import com.uwallet.pay.main.exception.PosApiException;
import com.uwallet.pay.main.exception.SignException;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.ApiQrPayFlow;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.RegexUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * api交易订单流水表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: api交易订单流水表
 * @author: caishaojun
 * @date: Created in 2021-08-17 15:50:50
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: caishaojun
 */
@Service
@Slf4j
public class ApiQrPayFlowServiceImpl extends BaseServiceImpl implements ApiQrPayFlowService {


    @Autowired
    @Lazy
    private UserService userService;

    @Resource
    private CreateCreditOrderFlowService createCreditOrderFlowService;

    @Autowired
    private ChannelFeeConfigService channelFeeConfigService;

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
    private LatPayService latPayService;
    @Autowired
    private StaticDataService staticDataService;


    @Autowired
    private TieOnCardFlowService tieOnCardFlowService;

    @Autowired
    private MarketingFlowService marketingFlowService;

    @Autowired
    private RechargeFlowService rechargeFlowService;


    @Autowired
    @Lazy
    private  MerchantService merchantService;

    @Autowired
    private ApiQrPayFlowDAO apiQrPayFlowDAO;

    @Autowired
    private MailLogService mailLogService;
    @Autowired
    private ApiMerchantService apiMerchantService;
    @Autowired
    @Lazy
    private QrPayService qrPayService;

    @Autowired
    private PayCreditBalanceFlowService payCreditBalanceFlowService;

    @Resource
    private DonationInstituteService donationInstituteService;

    @Resource
    private DonationFlowService donationFlowService;

    @Resource
    private TipFlowService tipFlowService;

    @Value("${uWallet.credit}")
    private String creditMerchantUrl;

    @Value("${h5Pay.AUTHORISE}")
    private String AUTHORISE;

    @Autowired
    private ParametersConfigService parametersConfigService;

    @Autowired
    private StripeAPIService stripeService;

    @Resource
    @Lazy
    private StripeAPIService stripeAPIService;

    @Resource
    @Lazy
    private CardService cardService;


    /**
     * 老用户确认订单
     * @author zhangzeyuan
     * @date 2021/6/29 16:26
     * @param orderId 订单号
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    @Override
    public JSONObject postCheckOrderImpl(@NonNull Long orderId,HttpServletRequest request) throws Exception {


        Map<String, Object> params = new HashMap<>(1);
        params.put("id", orderId);
        ApiQrPayFlowDTO apiQrPayFlowDTO = apiQrPayFlowDAO.selectOneDTOV2(params);

        BigDecimal payAmount = apiQrPayFlowDTO.getTransAmount();
        Long userId = getUserId(request);
        //返回结果
        JSONObject resultJson = new JSONObject();

        //用户的分期付可用额度
        String urlAvailableAmount = creditMerchantUrl + "/payremote/getCreditUserAvailableAmountAndState/";
        JSONObject postJsonV = new JSONObject(1);
        JSONObject dataJsonV = new JSONObject(1);
        dataJsonV.put("userId", userId);
        postJsonV.put("data", dataJsonV);
        resultJson.put("tradingName",apiQrPayFlowDTO.getPracticalName());
        try {
            String repayPlanResultV = HttpClientUtils.post(urlAvailableAmount, postJsonV.toJSONString());
            if(StringUtils.isNotBlank(repayPlanResultV)){
                JSONObject repayPlanResultJsonV = JSONObject.parseObject(repayPlanResultV);
                String codeV = repayPlanResultJsonV.getString("code");
                if(StringUtils.isNotBlank(codeV) && codeV.equals(ResultCode.OK.getCode())){
                    JSONObject  repayPlanDaataJsonV = repayPlanResultJsonV.getJSONObject("data");
                    //冻结状态
                    String userState = repayPlanDaataJsonV.getString("userState");
                    resultJson.put("userState", userState);
                    if(Objects.nonNull(repayPlanDaataJsonV.getBigDecimal("userAmount"))){
                        BigDecimal userAmount = repayPlanDaataJsonV.getBigDecimal("userAmount");
                        resultJson.put("AvailableToSpend", userAmount);
                        //userAmount小于payAmount orderStatus 为2
                        BigDecimal DueTodays = payAmount.multiply(new BigDecimal(Constant.PAY_CREDIT_NEED_CARDPAY_AMOUNT_RATE)).setScale(2, BigDecimal.ROUND_HALF_UP);
                        BigDecimal payAvailableAmount = DueTodays.subtract(DueTodays);
                        if(userAmount.compareTo(payAvailableAmount) < 0){
                            ApiQrPayFlowDTO orderClose = new ApiQrPayFlowDTO();
                            orderClose.setOrderStatus(StaticDataEnum.H5_ORDER_TYPE_2.getCode());
                            updateApiQrPayFlow(orderId, orderClose, request);
                            throw new BizException("The available balance is less than the amount paid!");
                        }
                    }


                }
            }
        }catch (Exception e){
            log.error("用户的分期付可用额度出错",e.getMessage());
        }

        //获取使用卡或者默认卡信息 null表示为默认卡
        CardDTO cardInfo = getCardInfoById(null, userId, request);
        if(null == cardInfo || null == cardInfo.getId()){
            resultJson.put("presetCard", "");
            resultJson.put("ccType", "");
            resultJson.put("cardId", "");
        }else{
            resultJson.put("presetCard", cardInfo.getCardNo());
            resultJson.put("ccType", cardInfo.getCustomerCcType());
            resultJson.put("cardId", cardInfo.getId().toString());
        }

        resultJson.put("OrderAmount",payAmount);
        //获取分期付预览计划
        String url = creditMerchantUrl + "/payremote/previewRepayInfo/";
        JSONObject postJson = new JSONObject(1);
        JSONObject dataJson = new JSONObject(3);
        dataJson.put("allPayAmount", payAmount);
        BigDecimal DueToday = payAmount.multiply(new BigDecimal(Constant.PAY_CREDIT_NEED_CARDPAY_AMOUNT_RATE)).setScale(2, BigDecimal.ROUND_HALF_UP);
        dataJson.put("cardPayAmount", DueToday);
        postJson.put("data", dataJson);
        try {
            String repayPlanResult = HttpClientUtils.post(url, postJson.toJSONString());
            if(StringUtils.isNotBlank(repayPlanResult)){
                JSONObject repayPlanResultJson = JSONObject.parseObject(repayPlanResult);
                String code = repayPlanResultJson.getString("code");
                if(StringUtils.isNotBlank(code) && code.equals(ResultCode.OK.getCode())){
                    JSONObject  repayPlanDaataJson = repayPlanResultJson.getJSONObject("data");
                    //resultJson.put("creditAverageAmount", repayPlanDaataJson.getString("avgAmount"));//平均金额
                    resultJson.put("period", repayPlanDaataJson.getString("period"));//几期
                    resultJson.put("DueToday",repayPlanDaataJson.getString("avgAmount"));
                    //列表
                    resultJson.put("Interest-freePaymenntOf",  repayPlanDaataJson.getJSONArray("list"));

                }
            }
        }catch (Exception e){
            log.error("获取分期付预览计划出错",e.getMessage());
        }
        // 查询卡过期日
        if ( null != cardInfo ) {
            if( null != cardInfo.getCrdStrgToken()){
                try{
                    JSONObject result = userService.getCardDetails(cardInfo.getCrdStrgToken(), request);
                    String customerCcExpyr = result.getString("customerCcExpyr");
                    String customerCcExpmo = result.getString("customerCcExpmo");
                    resultJson.put("customerCcExpyr",customerCcExpyr);
                    resultJson.put("customerCcExpmo",customerCcExpmo);
                }catch (Exception e){
                    log.error("获取获取卡过期日异常,e:{}",e.getMessage());
                }
            }

        }
        if ( null != cardInfo ) {
            if(null != cardInfo.getStripeToken()){
                try{
                    //查询有效期
                    StripeAPIResponse stripeAPIResponse = stripeAPIService.retrieveCard(userId, cardInfo.getStripeToken());
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

                    resultJson.put("customerCcExpyr", expYear);
                    resultJson.put("customerCcExpmo", expMonth);
                }catch (Exception e){
                    log.error("获取获取卡过期日异常,e:{}",e.getMessage());
                }
            }
        }

        return resultJson;
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

    }


    @Override
    public Long saveApiQrPayFlow(@NonNull ApiQrPayFlowDTO apiQrPayFlowDTO, HttpServletRequest request) throws BizException {
        ApiQrPayFlow apiQrPayFlow = BeanUtil.copyProperties(apiQrPayFlowDTO, new ApiQrPayFlow());
        log.info("save ApiQrPayFlow:{}", apiQrPayFlow);
        apiQrPayFlow = (ApiQrPayFlow)this.packAddBaseProps(apiQrPayFlow, request);
        if (apiQrPayFlowDAO.insert((ApiQrPayFlow) this.packAddBaseProps(apiQrPayFlow, request)) != 1) {
            log.error("insert error, data:{}", apiQrPayFlow);
            throw new BizException("Insert apiQrPayFlow Error!");
        }
        return apiQrPayFlow.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveApiQrPayFlowList(@NonNull List<ApiQrPayFlow> apiQrPayFlowList, HttpServletRequest request) throws BizException {
        if (apiQrPayFlowList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = apiQrPayFlowDAO.insertList(apiQrPayFlowList);
        if (rows != apiQrPayFlowList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, apiQrPayFlowList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateApiQrPayFlow(@NonNull Long id, @NonNull ApiQrPayFlowDTO apiQrPayFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full update apiQrPayFlowDTO:{}", apiQrPayFlowDTO);
        ApiQrPayFlow apiQrPayFlow = BeanUtil.copyProperties(apiQrPayFlowDTO, new ApiQrPayFlow());
        apiQrPayFlow.setId(id);
        int cnt = apiQrPayFlowDAO.update((ApiQrPayFlow) this.packModifyBaseProps(apiQrPayFlow, request));
        if (cnt != 1) {
            log.error("update error, data:{}", apiQrPayFlowDTO);
            throw new BizException("update apiQrPayFlow Error!");
        }
    }

    @Override
    public void updateApiQrPayFlowSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        apiQrPayFlowDAO.updatex(params);
    }

    @Override
    public void logicDeleteApiQrPayFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = apiQrPayFlowDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteApiQrPayFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = apiQrPayFlowDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ApiQrPayFlowDTO findApiQrPayFlowById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ApiQrPayFlowDTO apiQrPayFlowDTO = apiQrPayFlowDAO.selectOneDTO(params);
        return apiQrPayFlowDTO;
    }

    @Override
    public ApiQrPayFlowDTO findOneApiQrPayFlow(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        ApiQrPayFlow apiQrPayFlow = apiQrPayFlowDAO.selectOne(params);
        ApiQrPayFlowDTO apiQrPayFlowDTO = new ApiQrPayFlowDTO();
        if (null != apiQrPayFlow) {
            BeanUtils.copyProperties(apiQrPayFlow, apiQrPayFlowDTO);
        }
        return apiQrPayFlowDTO;
    }

    @Override
    public List<ApiQrPayFlowDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ApiQrPayFlowDTO> resultList = apiQrPayFlowDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return apiQrPayFlowDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return apiQrPayFlowDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = apiQrPayFlowDAO.groupCount(conditions);
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
        return apiQrPayFlowDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = apiQrPayFlowDAO.groupSum(conditions);
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
    public Object getPayments(String from, Integer max, String reference, String to, String after,HttpServletRequest request) {
        String requestUrl = "https" //当前链接使用的协议
                +"://" + request.getServerName()//服务器地址
                + request.getContextPath() //应用名称，如果应用名称为
                + request.getServletPath()+"/"; //请求的相对url

        String next = "https"+"://" + request.getServerName()+ request.getContextPath()+ request.getServletPath()+ "?" + request.getQueryString();
        String refund = "https"+"://" + request.getServerName()+ request.getContextPath();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> obj = new HashMap<>(2);
        params.put("start", from);
        params.put("end", to);
        params.put("id", after);
        params.put("max", max);
        params.put("apiTransNo", reference);
        List<ApiQrPayFlowDTO> list = apiQrPayFlowDAO.selectMapPayments(params);
        Map<String, Object> paramsCount = new HashMap<>();
        paramsCount.put("start", from);
        paramsCount.put("end", to);
        paramsCount.put("id", after);
        paramsCount.put("apiTransNo", reference);
        Integer count = apiQrPayFlowDAO.counts(paramsCount);
        List resList = new ArrayList();
        for (ApiQrPayFlowDTO str:list) {
            Map<String, Object> result = new HashMap<>();
            result.put("$self", requestUrl+str.getId().toString());
            result.put("$type", "payment");
            result.put("id", str.getId().toString());
            SimpleDateFormat simp=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            result.put("created", simp.format(str.getCreatedDate()));
            Map<String, Object> param = new HashMap<>(2);
            param.put("reference", str.getApiTransNo());
            result.put("merchant", param);
            Map<String, Object> original = new HashMap<>(2);
            if( null == str.getPayAmount() ){
                original.put("amount", "");
            } else {
                original.put("amount", (str.getPayAmount().multiply(new BigDecimal("100"))).stripTrailingZeros().toPlainString());
            }
            original.put("currency", str.getCurrencyType());
            Map<String, Object> refunded = new HashMap<>(2);
            if( null == str.getRefundAmount() ){
                refunded.put("amount", "");
            } else {
                refunded.put("amount", (str.getRefundAmount().multiply(new BigDecimal("100"))).stripTrailingZeros().toPlainString());

            }
            refunded.put("currency", str.getCurrencyType());
            Map<String, Object> current = new HashMap<>(2);
            if(null != str.getPayAmount() && null !=str.getRefundAmount()){
                BigDecimal currentAmount = str.getPayAmount().subtract(str.getRefundAmount());
                current.put("amount", (currentAmount.multiply(new BigDecimal("100"))).stripTrailingZeros().toPlainString());
            }else {
                current.put("amount", "");
            }
            current.put("currency", str.getCurrencyType());
            result.put("original", original);
            result.put("refunded", refunded);
            result.put("current", current);
            Map<String, Object> param3 = new HashMap<>(2);
            param3.put("id", str.getId().toString());
            param3.put("uri", refund+"/refunds/"+str.getId().toString());
            result.put("refunds", param3);
            resList.add(result);
        }
        int maxInt = max.intValue();
        int countInt = count.intValue();
        if (maxInt < countInt){
            Long id = list.get(list.size() -1).getId();
            obj.put("$next", next+id);
        } else {
            obj.put("$next", "");
        }

        obj.put("items", resList);
        return obj;
    }

    @Override
    public ApiQrPayFlowDTO paymentsId(Long id) {
        return apiQrPayFlowDAO.paymentsId(id);
    }

    @Override
    public JSONObject insertOrders(H5CreateOrderRequestDTO h5CreateOrderRequestDTO, HttpServletRequest request)throws BizException {

        String requestUrl = "https"+"://" + request.getServerName()+ request.getContextPath()+ request.getServletPath()+"/";
        //String AUTHORISE = "https"+"://pwptest-web.loancloud.cn/#/login?p=";
        String authHeader = request.getHeader("Authorization");
        String idempotencyKey = request.getHeader("Idempotency-Key");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SignException(SignErrorCode.SIGN_ERROR.getCode(),"100403");
        }
        String tokens = authHeader.replaceFirst("Bearer ", "").trim();
        DecodedJWT verifyCode = JwtUtils.verifyCode(tokens);
        String apiMerchantId = verifyCode.getSubject();
        JSONObject params = new JSONObject(16);
        BigDecimal bignum2 = new BigDecimal("100");
        BigDecimal doubleValue =  new BigDecimal(h5CreateOrderRequestDTO.getTotal().getAmount()).divide(bignum2);
        String currency = h5CreateOrderRequestDTO.getTotal().getCurrency();
        String reference = h5CreateOrderRequestDTO.getMerchant().getReference();
        String confirmation_url = h5CreateOrderRequestDTO.getMerchant().getConfirmation_url();
        String cancellation_url = h5CreateOrderRequestDTO.getMerchant().getCancellation_url();
        Map<String, Object> paramsApi = new HashMap<>(1);
        paramsApi.put("idempotencyKey", idempotencyKey);
//        paramsApi.put("apiTransNo", reference);
        ApiQrPayFlow apiQrPayFlowApi = apiQrPayFlowDAO.selectOne(paramsApi);


        if (null != apiQrPayFlowApi && apiQrPayFlowApi.getId() != null ) {
            Long exps= apiQrPayFlowApi.getExpirationTime();
            Long now = System.currentTimeMillis();
            Boolean expDate = exps.longValue() < now.longValue();
            if(expDate){
                throw new SignException(SignErrorCode.SDK_TOKEN_ERROR.getCode(), "100404");
            }else{
                String authorise = AUTHORISE+apiQrPayFlowApi.getId();
                Map<String, Object> map = new HashMap<String, Object>(2);
                map.put("status", "pending");
                map.put("authorise", authorise);
                params.put("id", apiQrPayFlowApi.getId());
                params.put("$self",requestUrl+apiQrPayFlowApi.getId());
                params.put("$type", "order");
                H5RequestAmountDTO amountDTO = new H5RequestAmountDTO();
                amountDTO.setAmount(apiQrPayFlowApi.getTransAmount().multiply(new BigDecimal("100")).longValue());
                amountDTO.setCurrency(apiQrPayFlowApi.getCurrencyType().toLowerCase());

                params.put("total", amountDTO);
                H5RequestMerchantDTO merchantDTO = new H5RequestMerchantDTO();
                merchantDTO.setReference(apiQrPayFlowApi.getApiTransNo());
                merchantDTO.setCancellation_url(apiQrPayFlowApi.getCancellationUrl());
                merchantDTO.setConfirmation_url(apiQrPayFlowApi.getConfirmationUrl());
                params.put("merchant", merchantDTO);
                params.put("status", map);
                return params;
            }
        }

        paramsApi.clear();
        //paramsApi.put("apiTransNo", reference);
        paramsApi.put("idempotencyKey", idempotencyKey);
        ApiQrPayFlow apiQrPayFlow1 = apiQrPayFlowDAO.selectOne(paramsApi);
        if (null != apiQrPayFlow1 && apiQrPayFlow1.getId() != null ) {
            throw new BizException("Order already exists");
        }
        ParametersConfigDTO parametersConfigDTO = parametersConfigService.findParametersConfigById(1L);

        Integer exp = parametersConfigDTO.getValidTime()*60*60*1000;
        ApiQrPayFlowDTO apiQrPayFlow = new ApiQrPayFlowDTO();
        apiQrPayFlow.setApiTransNo(reference);
        apiQrPayFlow.setCurrencyType(currency);
        apiQrPayFlow.setTransAmount(doubleValue);
        apiQrPayFlow.setOrderStatus(StaticDataEnum.H5_ORDER_TYPE_0.getCode());
        apiQrPayFlow.setConfirmationUrl(confirmation_url);
        apiQrPayFlow.setCancellationUrl(cancellation_url);
        apiQrPayFlow.setIdempotencyKey(idempotencyKey);
        apiQrPayFlow.setExpirationTime(System.currentTimeMillis()+ exp);
        ApiMerchantDTO apiMerchantDTO = apiMerchantService.findApiMerchantById(Long.valueOf(apiMerchantId));
        apiQrPayFlow.setApiMerchantId(Long.valueOf(apiMerchantId));
        apiQrPayFlow.setSuperMerchantId(apiMerchantDTO.getSuperMerchantId());
        // 当前只有正常出售的分期付方式，之后多了付款方式单号要改
        apiQrPayFlow.setTransNo("BINS"+SnowflakeUtil.generateId());
        Long id = null;
        try{
             id = this.saveApiQrPayFlow(apiQrPayFlow, request);
        }catch ( Exception e){
            log.error( "insert api Order Excepiton:");
            throw new BizException("Order error");
        }

        String authorise = AUTHORISE+id;
        Map<String, Object> map = new HashMap<String, Object>(2);
        map.put("status", "pending");
        map.put("authorise", authorise);

        params.put("id", id);
        params.put("$self", requestUrl+id);
        params.put("$type", "order");
        params.put("total", h5CreateOrderRequestDTO.getTotal());
        params.put("merchant", h5CreateOrderRequestDTO.getMerchant());
        params.put("status", map);
        return params;
    }

    @Override
    public Object payments(H5PaymentsRequestDTO h5PaymentsRequestDTO, HttpServletRequest request) throws Exception {


        ApiQrPayFlowDTO h5pay = findApiQrPayFlowById(h5PaymentsRequestDTO.getOrder());
        QrPayDTO qrPayDTO = new QrPayDTO();
        qrPayDTO.setPayUserId(h5pay.getUserId());
//        qrPayDTO.setRecUserId(h5pay.getApiMerchantId());
        qrPayDTO.setTransAmount(h5pay.getTransAmount());
        qrPayDTO.setTrulyPayAmount(h5pay.getTransAmount());
        qrPayDTO.setPayType(StaticDataEnum.PAY_TYPE_4.getCode());
        qrPayDTO.setTransNo(h5pay.getTransNo());
        qrPayDTO.setCardId(h5pay.getCardId());
        qrPayDTO.setProductId("397253654985920512");
        qrPayDTO.setMerchantId(h5pay.getApiMerchantId());
        //扫码支付前置校验
        log.info("扫码支付前置校验==start, data:{}",qrPayDTO);
        qrPayReqCheckV2(qrPayDTO, request);
        log.info("扫码支付前置校验==success, data:{}",qrPayDTO);

        //获取交易双方user信息
        UserDTO payUser = userService.findUserInfoV3(qrPayDTO.getPayUserId());//付款用户
        log.info("获取交易双方user信息==success, data:{}",payUser);
//        UserDTO recUser = userService.findUserInfoV2(qrPayDTO.getRecUserId());//收款用户

        //判断用户是否存在
        if (null ==  payUser ||  null == payUser.getId() ) {
            throw new BizException(I18nUtils.get("user.inexistence", getLang(request)));
        }
//        if(null == recUser || null == recUser.getId() ){
//            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
//        }

        //封装订单流水信息--订单流水表
        QrPayFlowDTO qrPayFlowDTO = new QrPayFlowDTO();
        qrPayFlowDTO.setPayUserId(qrPayDTO.getPayUserId());
        qrPayFlowDTO.setPayUserType(payUser.getUserType());
        qrPayFlowDTO.setMerchantId(qrPayDTO.getMerchantId());
//        qrPayFlowDTO.setRecUserId(qrPayDTO.getRecUserId());
        qrPayFlowDTO.setRecUserType(StaticDataEnum.USER_TYPE_30.getCode());
        qrPayFlowDTO.setTransAmount(qrPayDTO.getTransAmount());
        qrPayFlowDTO.setRemark(qrPayDTO.getRemark());
        qrPayFlowDTO.setTransNo(qrPayDTO.getTransNo());
        qrPayFlowDTO.setPayUserIp(getIp(request));
        qrPayFlowDTO.setCreatedDate(System.currentTimeMillis());
        qrPayFlowDTO.setDonationAmount(h5pay.getDonateAmount());
        qrPayFlowDTO.setOrderSource(StaticDataEnum.ORDER_SOURCE_1.getCode());

        //返回结果
        Map<String, Object> resultData = Maps.newHashMapWithExpectedSize(8);

        resultData = creditPay(qrPayFlowDTO, qrPayDTO, payUser, request);

        HashMap<String, Object> resultMap = Maps.newHashMapWithExpectedSize(5);
        resultMap.put("flowId",qrPayFlowDTO.getTransNo());
        resultMap.put("id", qrPayFlowDTO.getId().toString());
        resultMap.put("resultState", StaticDataEnum.TRANS_STATE_1.getCode());
        resultMap.put("data", resultData);

        ApiQrPayFlowDTO str = paymentsId(h5PaymentsRequestDTO.getOrder());
        String requestUrl = "https"+"://" + request.getServerName()+ request.getContextPath()+ request.getServletPath();
        String uri = "https"+"://" + request.getServerName()+ request.getContextPath();
        Map<String, Object> result = new HashMap<>(16);
        result.put("$self", requestUrl);
        result.put("$type", "payment");
        result.put("id", str.getId().toString());
        SimpleDateFormat simp=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        result.put("created", simp.format(str.getCreatedDate()));
        Map<String, Object> param = new HashMap<>(2);
        param.put("reference", str.getApiTransNo());
        result.put("merchant", param);
        Map<String, Object> original = new HashMap<>(2);
        if( null == str.getPayAmount() ){
            original.put("amount", "");
        } else {
            original.put("amount", (str.getPayAmount().multiply(new BigDecimal("100"))).stripTrailingZeros().toPlainString());
        }
        original.put("currency", str.getCurrencyType());
        Map<String, Object> refunded = new HashMap<>(2);
        if( null == str.getRefundAmount() ){
            refunded.put("amount", "");
        } else {
            refunded.put("amount", (str.getRefundAmount().multiply(new BigDecimal("100"))).stripTrailingZeros().toPlainString());

        }
        refunded.put("currency", str.getCurrencyType());
        Map<String, Object> current = new HashMap<>(2);
        if(null != str.getPayAmount() && null !=str.getRefundAmount()){
            BigDecimal currentAmount = str.getPayAmount().subtract(str.getRefundAmount());
            current.put("amount", (currentAmount.multiply(new BigDecimal("100"))).stripTrailingZeros().toPlainString());
        }else {
            current.put("amount", "");
        }
        current.put("currency", str.getCurrencyType());

        result.put("original", original);
        result.put("refunded", refunded);
        result.put("current", current);
        Map<String, Object> param3 = new HashMap<>(2);
        param3.put("id", str.getId().toString());
        param3.put("uri", uri+"/refunds/"+str.getId().toString());
        result.put("refunds", param3);

        return result;

    }

    /**
     * 扫码支付前置校验
     *
     * @param qrPayDTO
     * @param request
     * @throws BizException
     */
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

        //卡ID
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
     * 分期付支付
     * @author zhangzeyuan
     * @date 2021/6/23 17:24
     * @param qrPayFlowDTO
     * @param qrPayDTO
     * @param payUser
     * @param request
     */
    private Map<String, Object> creditPay(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, UserDTO payUser,
                                          HttpServletRequest request) throws Exception {
        //付款用户必须是已开通分期付并且不需要补充信息的
        if(payUser.getInstallmentState() != StaticDataEnum.STATUS_1.getCode()
                || payUser.getSplitAddInfoState() == StaticDataEnum.STATUS_1.getCode()){
            throw new BizException(I18nUtils.get("transaction.illegality", getLang(request)));
        }
        log.info("付款用户必须是已开通分期付并且不需要补充信息的==success, data:{}",payUser);
        //是否逾期
        Map<String, Object> userCreditInfoMap = getCreditUserAvailableAmountAndState(payUser.getId());
        log.info("是否逾期==success, data:{}",userCreditInfoMap);
        //分期付额度
        BigDecimal creditAmount = (BigDecimal) userCreditInfoMap.get("creditAmount");
        log.info("分期付额度==success, data:{}",creditAmount);
        //分期付用户状态
        Integer userState = (Integer) userCreditInfoMap.get("userState");
        log.info("期付用户状态==success, data:{}",userState);
        //用户是否冻结
        if(userState.equals(StaticDataEnum.CREAT_USER_STATE_11.getCode())){
            throw new BizException(I18nUtils.get("pay.user.freeze", getLang(request)));
        }

        //设置交易类型
        qrPayFlowDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode());
        qrPayFlowDTO.setProductId(qrPayDTO.getProductId());
        log.info("设置交易类型==success, data:{}",qrPayFlowDTO);

        log.info("通道校验==start, data:{}",StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode(), qrPayDTO.getCardId(), qrPayFlowDTO);
        //通道校验
        Map<String, Object> channelResultMap = verifyAndGetPayGatewayCardInfo(StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode(), qrPayDTO.getCardId(), qrPayFlowDTO, request);
        log.info("通道校验==success, data:{}",channelResultMap);

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
        if(creditAmount.compareTo(remainingCreditAmount) < 0){
            throw new BizException(I18nUtils.get("pay.credit.amount.insufficient", getLang(request)));
        }

        //限额组件
        if(creditNeedCardPayNoFeeAmount.compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimit(0L, creditNeedCardPayNoFeeAmount, null);
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
        if(donationAmount.compareTo(BigDecimal.ZERO) > 0){
            // 查询机构
            DonationInstituteDTO donationInstituteDTO = donationInstituteService.findDonationInstituteById(1L);
            // 封装捐赠流水
            DonationFlowDTO donationFlowDTO = new DonationFlowDTO();
            donationFlowDTO.setFlowId(qrPayFlowDTO.getId());
            donationFlowDTO.setUserId(payUser.getId());
            donationFlowDTO.setUserName(payUser.getUserFirstName() + " " + payUser.getUserLastName());
            donationFlowDTO.setInstituteId(donationInstituteDTO.getId());
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

            boolean frozenResult =frozenCreditAmount(remainingCreditAmount, payUser.getId(), qrPayFlowDTO.getId(), qrPayFlowDTO.getId());

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
        qrPayService.updateFlow(qrPayFlowDTO, null, null, request);

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
//        sendCardPayLatPayRequest(qrPayFlowDTO, withholdFlowDTO, cardJsonObj, request);

        sendStripeRequest(qrPayFlowDTO, withholdFlowDTO, cardDTO, request);

        //根据三方返回状态进行处理
//        qrPayService.handleCardLatPayPostResult(withholdFlowDTO ,qrPayFlowDTO,null );

        qrPayService.handleCreditCardPayDataByThirdStatus(withholdFlowDTO, qrPayFlowDTO, cardJsonObj, creditNeedCardPayAmount, creditNeedCardPayNoFeeAmount,
                remainingCreditAmount, cardPayRate, cardPayFee, request);

        return amountResultMap;
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
     * 分期付 限额 出账 回滚
     * @author zhangzeyuan
     * @date 2021/6/27 23:02
     * @param qrPayFlowDTO
     * @param creditNeedCardPayNoFeeAmount
     * @param request
     */
    private void creditChannelLimitAndBatchAmountRollBack(QrPayFlowDTO qrPayFlowDTO, BigDecimal creditNeedCardPayNoFeeAmount, Integer state,
                                                          HttpServletRequest request) throws Exception{
        if(qrPayFlowDTO.getGatewayId() != null && creditNeedCardPayNoFeeAmount.compareTo(BigDecimal.ZERO) > 0){
            rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), qrPayFlowDTO.getGatewayId(), creditNeedCardPayNoFeeAmount,request);
        }
        doBatchAmountOutRollBack(qrPayFlowDTO, request);
        qrPayFlowDTO.setState(state);
        qrPayService.updateFlow(qrPayFlowDTO, null, null, request);
    }
    private void doBatchAmountOutRollBack(QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) {
        try {
            if(qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) == 0 &&qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) == 0){
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
            }
            // 增加备注方便对账
            jsonObject.put("remark","rollBack for "+ amountOutFlowList.get(0).getOrderNo());
            jsonObject.put("totalAmountOut", BigDecimal.ZERO);
            jsonObject.put("totalAmountIn",qrPayFlowDTO.getWholeSalesAmount().add(qrPayFlowDTO.getRedEnvelopeAmount()));
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
                    marketingFlowService.logicDeleteMarketingFlow(marketingFlowDTO.getId(),request);
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
        log.info("获取商户路由配置  data:{}", gatewayDTO);
        // todo 挡板
        resultMap.clear();
        if(null == gatewayDTO || null == gatewayDTO.getId()){
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        resultMap.put("gatewayDTO", gatewayDTO);

        //路由支付通道类型
        Long gatewayType = gatewayDTO.getType();
        log.info("路由支付通道类型  data:{}", gatewayType);
        //获取卡信息
        JSONObject cardJsonObj;
        CardDTO cardDTO;
        try {
            cardJsonObj = serverService.getCardInfo(cardId);
            log.info("获取卡信息  cardJsonObj:{}", cardJsonObj);
            if (cardJsonObj == null || StringUtils.isBlank(cardJsonObj.toJSONString())) {
                throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
            }

            cardDTO = JSONObject.parseObject(cardJsonObj.toJSONString(), CardDTO.class);

        } catch (Exception e) {
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        resultMap.put("cardDTO", cardDTO);
        resultMap.put("cardJsonObj", cardJsonObj);

        log.info("获取卡信息  CardDTO:{}", cardDTO);
        log.info("校验三方渠道  data:{}", gatewayType.intValue());
        //校验三方渠道
        if(gatewayType.intValue() == StaticDataEnum.GATEWAY_TYPE_0.getCode()){
            //如果是LatPay，判断卡表token不为空，
            if(StringUtils.isBlank(cardDTO.getCrdStrgToken())){
                throw new BizException(I18nUtils.get("channel.token.missing", getLang(request)));
            }
            // 用户和卡交易限制查询
            userService.verifyCardFailedTime(qrPayFlowDTO.getPayUserId(), cardId,request);

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

        qrPayFlowDTO.setCardId(cardId.toString());
        qrPayFlowDTO.setGatewayId(gatewayType.longValue());
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
    private Map<String, Object> verifyAndGetPayAmount(QrPayFlowDTO qrPayFlowDTO, QrPayDTO qrPayDTO, CardDTO cardDTO,
                                                      HttpServletRequest request) throws Exception {
        Integer payType = qrPayDTO.getPayType();
        Long payUserId = qrPayDTO.getPayUserId();
//        Long recUserId = qrPayDTO.getRecUserId();

        //查询商户配置信息
        HashMap<String, Object> queryParamsMap = Maps.newHashMapWithExpectedSize(3);
        queryParamsMap.put("id", qrPayDTO.getMerchantId());
        queryParamsMap.put("isAvailable", StaticDataEnum.AVAILABLE_1.getCode());
        //TODO 改为APImerchant
        ApiMerchantDTO merchantDTO = apiMerchantService.findOneApiMerchant(queryParamsMap);
        if(Objects.isNull(merchantDTO) || Objects.isNull(merchantDTO.getId())) {
            throw new BizException(I18nUtils.get("merchant.not.available", getLang(request)));
        }

        //订单金额
        BigDecimal transAmount = qrPayDTO.getTransAmount();
        //计算实付金额
        BigDecimal payAmount = transAmount;

        //整体出售使用额度
        BigDecimal useWholeSaleAmount = new BigDecimal(0);
        //正常出售使用额度
        BigDecimal useNormalSaleAmount = transAmount;



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


        //捐赠金额
        BigDecimal donationAmount = qrPayFlowDTO.getDonationAmount();

        //小费金额
        BigDecimal tipAmount = BigDecimal.ZERO;
        if(Objects.nonNull(qrPayDTO.getTipAmount())){
            tipAmount = qrPayDTO.getTipAmount();
        }


        //计算手续费
        if(payAmount.compareTo(BigDecimal.ZERO) > 0){
//            if(payType.equals(StaticDataEnum.CHANNEL_PAY_TYPE_4.getCode())){

            //分期付
            //分期付卡支付金额
            creditCardFirstPayAmount = payAmount.multiply(new BigDecimal(Constant.PAY_CREDIT_NEED_CARDPAY_AMOUNT_RATE)).setScale(2, BigDecimal.ROUND_HALF_UP);
            //分期付分期金额
            remainingCreditPayAmount = payAmount.subtract(creditCardFirstPayAmount);

            //分期付需要用卡支付的金额 =  25%卡支付金额 + 捐赠费 + 小费
            tempCardPayAllAmount = creditCardFirstPayAmount.add(donationAmount).add(tipAmount);
//            }else if(qrPayDTO.getPayType().equals(StaticDataEnum.CHANNEL_PAY_TYPE_0.getCode())){
//                //卡支付
//                tempCardPayAllAmount = payAmount.add(donationAmount).add(tipAmount);
//            }
        }


        if(tempCardPayAllAmount.compareTo(BigDecimal.ZERO) > 0){
            // 计算查询手续费
            Map<String, Object> feeMap = getCardPayTransactionFee(StaticDataEnum.GATEWAY_TYPE_0.getCode(), cardDTO.getCustomerCcType(), tempCardPayAllAmount, request);

            gateWayFee = (BigDecimal) feeMap.get("channelFeeAmount");

            transChannelFeeRate = (BigDecimal) feeMap.get("transChannelFeeRate");

            tempCardPayAllAmount = tempCardPayAllAmount.add(gateWayFee);
        }

        log.info("TrulyPayAmount:"+ tempCardPayAllAmount);

        //平台服务费
        BigDecimal platformFee = BigDecimal.ZERO;

        //实收金额 = 正常交易 - 平台服务费 不包括红包
        BigDecimal recAmount = payAmount.subtract(platformFee);

        //封装qrPayFlowDTO信息
        qrPayFlowDTO.setMerchantId(merchantDTO.getId());
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
        qrPayFlowDTO.setSaleType(0);

        qrPayFlowDTO.setPlatformFee(platformFee);
        qrPayFlowDTO.setRecAmount(recAmount);

        //整体出售额度
        qrPayFlowDTO.setWholeSalesAmount(useWholeSaleAmount);
        //正常出售额度
        qrPayFlowDTO.setNormalSaleAmount(useNormalSaleAmount);
        //红包
        qrPayFlowDTO.setRedEnvelopeAmount(BigDecimal.ZERO);

        //整体出售折扣 折扣金额
        qrPayFlowDTO.setWholeSalesDiscount(BigDecimal.ZERO);
        qrPayFlowDTO.setWholeSalesDiscountAmount(BigDecimal.ZERO);

        //固定、营销、额外折扣金额
        qrPayFlowDTO.setBaseDiscountAmount(BigDecimal.ZERO);
        qrPayFlowDTO.setExtraDiscountAmount(BigDecimal.ZERO);
        qrPayFlowDTO.setMarkingDiscountAmount(BigDecimal.ZERO);
        //固定、营销、额外折扣率
        qrPayFlowDTO.setBaseDiscount(BigDecimal.ZERO);
        qrPayFlowDTO.setExtraDiscount(BigDecimal.ZERO);
        qrPayFlowDTO.setMarkingDiscount(BigDecimal.ZERO);
        //捐赠费
        qrPayFlowDTO.setDonationAmount(donationAmount);
        //小费
        qrPayFlowDTO.setTipAmount(tipAmount);

        qrPayFlowDTO.setPayAmount(payAmount);
        //分期付borrowID
        qrPayFlowDTO.setCreditOrderNo(SnowflakeUtil.generateId().toString());

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

        resultMap.put("donationAmount", donationAmount.toString());

        resultMap.put("tipAmount", tipAmount.toString());

        resultMap.put("remainingCreditAmount",remainingCreditPayAmount);

        resultMap.put("cardPayFee", gateWayFee.toString());
        resultMap.put("cardPayRate", transChannelFeeRate.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
        resultMap.put("cardPayRateReal", transChannelFeeRate);


        //分期付
        resultMap.put("creditNeedCardPayAmount", creditCardFirstPayAmount.add(donationAmount).add(tipAmount).add(gateWayFee).toString());
        resultMap.put("creditNeedCardPayNoFeeAmount", creditCardFirstPayAmount.toString());
        resultMap.put("remainingCreditAmount", remainingCreditPayAmount.toString());

        return resultMap;
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
            if(qrPayFlowDTO.getOrderSource() == StaticDataEnum.ORDER_SOURCE_1.getCode()){
                qrPayFlowDTO.setIsShow(StaticDataEnum.ORDER_SHOW_STATE_0.getCode());
            }
            qrPayService.updateFlow(qrPayFlowDTO, null, null, request);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
        }

        //判断出账结果，如果失败，限额回滚，修改交易为失败状态
        if(!amountOutResult){
            rechargeFlowService.channelLimitRollback(System.currentTimeMillis(), gatewayDTO.getType(), qrPayFlowDTO.getPayAmount(),request);

            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_12.getCode());
            if(qrPayFlowDTO.getOrderSource() == StaticDataEnum.ORDER_SOURCE_1.getCode()){
                qrPayFlowDTO.setIsShow(StaticDataEnum.ORDER_SHOW_STATE_0.getCode());
            }
            qrPayService.updateFlow(qrPayFlowDTO, null, null, request);
            throw new BizException(I18nUtils.get("trans.failed", getLang(request)));
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
        // 红包出账
        if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0){
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
        }
        // 整体出售出账
        if(qrPayFlowDTO.getWholeSalesAmount().compareTo(BigDecimal.ZERO) > 0){
            // 账户出账记录
            AccountFlowDTO accountFlowDTO = new AccountFlowDTO();
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
        }
        jsonObject.put("totalAmountOut", qrPayFlowDTO.getWholeSalesAmount().add(qrPayFlowDTO.getRedEnvelopeAmount()));
        jsonObject.put("totalAmountIn", BigDecimal.ZERO);
        if (BigDecimal.ZERO.compareTo(qrPayFlowDTO.getWholeSalesAmount()) < 0 && BigDecimal.ZERO.compareTo(qrPayFlowDTO.getRedEnvelopeAmount()) < 0) {
            jsonObject.put("totalNumber", 2);
        } else {
            jsonObject.put("totalNumber", 1);
        }
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
            if(qrPayFlowDTO.getRedEnvelopeAmount().compareTo(BigDecimal.ZERO) > 0){
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
        BigDecimal payUserAmount = userService.getBalance(payUserId, StaticDataEnum.SUB_ACCOUNT_TYPE_0.getCode());
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
    private Map<String,Object> sendStripeRequest(QrPayFlowDTO qrPayFlowDTO, WithholdFlowDTO withholdFlowDTO, CardDTO cardDTO, HttpServletRequest request) {

        if(withholdFlowDTO.getTransAmount().compareTo(BigDecimal.ZERO) > 0){
            //渠道交易请求
            StripeAPIResponse stripeResult = null;
            try{
//                StripePaymentIntentDTO stripePaymentIntentDTO = new StripePaymentIntentDTO();
//                stripePaymentIntentDTO.setAmount(withholdFlowDTO.getTransAmount().multiply(new BigDecimal("100")).setScale(0,BigDecimal.ROUND_DOWN));
//                stripePaymentIntentDTO.setCurrency("aud");
//                stripePaymentIntentDTO.setConfirm(true);
//                // 成功：card_1KIpEuAgx3Fd2j3e0Mz1gZZr 失败：card_1KM49nAgx3Fd2j3elyLhTzhC
//                stripePaymentIntentDTO.setPayment_method(cardDTO.getStripeToken());
//                List<String>  paymentMethodTypes = new ArrayList<>();
//
//
//                paymentMethodTypes.add("card");
//                stripePaymentIntentDTO.setPayment_method_types(paymentMethodTypes);
//                stripePaymentIntentDTO.setCustomer(qrPayFlowDTO.getPayUserId().toString());
//                // todo 测试挡板
////                stripePaymentIntentDTO.setCustomer("638621894721458176");
//                Map<String, Object> s3dsMap = new HashMap<>();
//                s3dsMap.put("request_three_d_secure","any");
//                Map<String, Object> cards = new HashMap<>();
//                cards.put("card",s3dsMap);
//                stripePaymentIntentDTO.setPayment_method_options(cards);
//                Map<String,Object> metaData = new HashMap<>();
//                metaData.put("id",withholdFlowDTO.getOrdreNo());
//                stripePaymentIntentDTO.setMetadata(metaData);
//                log.info("stripe payment request:" + stripePaymentIntentDTO );
//
//                stripeResult = stripeService.createPaymentIntent(stripePaymentIntentDTO);
//
//                log.info("stripe payment Result:" + stripeResult);


                StripeChargeDTO stripeChargeDTO = new StripeChargeDTO();
                stripeChargeDTO.setAmount(withholdFlowDTO.getTransAmount().multiply(new BigDecimal("100")).setScale(0,BigDecimal.ROUND_DOWN));
//                stripeChargeDTO.setCustomer("638621894721458176");
                stripeChargeDTO.setCustomer(qrPayFlowDTO.getPayUserId().toString());
                stripeChargeDTO.setCurrency("aud");
                stripeChargeDTO.setSource(cardDTO.getStripeToken());
                stripeChargeDTO.setDescription(StaticDataEnum.STRIPE_ORDER_DESC_REPAYMENT.getMessage());
                HashMap<String, Object> metaMap = Maps.newHashMapWithExpectedSize(1);
                metaMap.put("id", withholdFlowDTO.getOrdreNo());
                stripeChargeDTO.setMetadata(metaMap);

                stripeResult = stripeService.createCharge(stripeChargeDTO);

                log.info("stripeAPIResponse :"+stripeResult);


            }catch (Exception e){
                log.error("send stripe pay ,fail:"+e.getMessage(), e);
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                if(stripeResult != null){
                    withholdFlowDTO.setReturnMessage(e.getMessage());
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
                withholdFlowDTO.setReturnMessage(message);
                return  null ;
            }

            log.info("stripeAPIResponse :"+stripeResult);
            if(stripeResult.isSuccess()){
                // 请求成功
                Charge charge = (Charge)stripeResult.getData();
                withholdFlowDTO.setStripeId(charge.getId());
                if(charge == null || StringUtils.isEmpty(charge.getStatus())){
                    withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                }
                if(StripeAPICodeEnum.CHARGE_RES_STATUS_SUCCEEDED.getMessage().equals(charge.getStatus())){
                    // 成功
                    withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
                }else if(StripeAPICodeEnum.CHARGE_RES_STATUS_FAILED.getMessage().equals(charge.getStatus())){
                    // 失败
                    withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                }else{
                    // 处理中
                    withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_3.getCode());
                }

            }else{
                withholdFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                withholdFlowDTO.setErrorMessage(stripeResult.getMessage());
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
     * 卡支付 根据三方状态进行交易处理
     * @author zhangzeyuan
     * @date 2021/7/1 9:58
     * @param withholdFlowDTO
     * @param qrPayFlowDTO
     * @param request
     */
    private void handleCardLatPayPostResult(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO,
                                            HttpServletRequest request) throws Exception{
        Integer status = withholdFlowDTO.getState();
        //判断三方交易状态
        if (status.equals(StaticDataEnum.TRANS_STATE_1.getCode())) {
            //成功
            handleLatPaySuccess(withholdFlowDTO, qrPayFlowDTO, request);

        } else if (status.equals(StaticDataEnum.TRANS_STATE_2.getCode())) {
            log.error("三方返回状态失败");
            //失败
            handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, qrPayFlowDTO.getPayAmount(), request);

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
            qrPayService.updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
            throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
        }
    }
    /**
     * 分期付 25%卡支付 根据三方状态进行交易处理
     * @author zhangzeyuan
     * @date 2021/7/1 9:59
     * @param withholdFlowDTO
     * @param qrPayFlowDTO
     * @param cardObj
     * @param creditNeedCardPayAmount
     * @param creditNeedCardPayNoFeeAmount
     * @param remainingCreditAmount
     * @param cardPayRate
     * @param cardPayFee
     * @param request
     */
    private void handleCreditCardPayDataByThirdStatus(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO, JSONObject cardObj,
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
                    jsonObject.put("cardId", cardObj.getString("id"));
                    try{
                        userService.presetCard(jsonObject, qrPayFlowDTO.getPayUserId(),request);
                    }catch (Exception e){
                        log.error("分期付第一次卡支付设置默认卡失败,e:{},userId",e,getUserId(request));
                    }

                }else{
                    //没有成功 可疑/失败 跑批处理
                    createCreditOrderFlowDTO.setState(StaticDataEnum.CREATE_CREDIT_ORDER_FLOW_STATE_3.getCode());
                    createCreditOrderFlowService.updateCreateCreditOrderFlow(createCreditOrderFlowId, createCreditOrderFlowDTO, request);

                    //将订单类型改为交易成功
                    qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
                    qrPayService.updateFlowForConcurrency(qrPayFlowDTO, null, null, request);
                    return;
                }
            }else {
                //实付金额为0  不生成分期付订单
                // 成功处理
                handleLatPaySuccess(withholdFlowDTO, qrPayFlowDTO, request);
            }

            //POS API 交易成功回调通知
            try {
                log.info("开始进行订单支付成功通知");
                //posApiService.posPaySuccessNotice(qrPayFlowDTO.getMerchantId(), qrPayFlowDTO.getTransNo(), request);
            }catch (PosApiException e){
                log.error("POS通知失败" + e.getMessage());
            }

            //分期付发送交易记录 邮件 todo 异步
            try{
                sendTransactionMail(qrPayFlowDTO.getCreditOrderNo(), request);
            }catch (Exception e){
                log.error("分期付发送交易记录邮件失败, CreditOrderNo:" + qrPayFlowDTO.getCreditOrderNo() + " || message:" + e.getMessage());
            }
        } else if (status.equals(StaticDataEnum.TRANS_STATE_2.getCode())) {
            //发送三方失败
            //失败处理
            handleLatPayFailed(withholdFlowDTO, qrPayFlowDTO, withholdFlowDTO.getOrderAmount(), request);
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
            qrPayService.updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
            throw new BizException(I18nUtils.get("trans.doubtful", getLang(request)));
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
//        MailTemplateDTO mailTemplate = mailTemplateService.findMailTemplateBySendNode(String.valueOf(StaticDataEnum.SEND_NODE_31.getCode()));
//        if(Objects.isNull(mailTemplate) || StringUtils.isBlank(mailTemplate.getEnSendContent())){
//            log.error("发送交易记录邮件 查询邮件模板失败");
//            return;
//        }

//        String enSendContent = mailTemplate.getEnSendContent();
//        String enMailTheme = mailTemplate.getEnMailTheme();

        //设置模板参数
        String[] titleParam = {merchantName, titleDate};
        //邮件内容
        String sendContent = null;
        String sendTitle = null;

//        sendContent = templateContentReplace(contentParam, enSendContent);
//        sendContent = enSendContent.replace("{merchantName}", merchantName).replace("{location}", fullAddress).replace("{userName}", userName).replace("borrowAmount", "$" + borrowAmount.toString())
//                .replace("{repayList}", instalmentsHtml).replace("{leftAmount}", leftAmount.toString());

        //sendTitle = templateContentReplace(titleParam, enMailTheme);


        //发送邮件
//        Session session = MailUtil.getSession(sysEmail);
//        MimeMessage mimeMessage = MailUtil.getMimeMessage(StaticDataEnum.U_WALLET.getMessage(), sysEmail, email, sendTitle, sendContent, null, session);
//        MailUtil.sendMail(session, mimeMessage, sysEmail, sysEmailPwd);

        //记录邮件流水
        MailLogDTO mailLogDTO = new MailLogDTO();
        mailLogDTO.setAddress(email);
        mailLogDTO.setContent(sendContent);
        mailLogDTO.setSendType(0);
        mailLogService.saveMailLog(mailLogDTO, request);
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
    private GatewayDTO getGateWay(QrPayDTO qrPayDTO, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws BizException {
        Map<String,Object> gateWayMap = new HashMap<>();
        gateWayMap.put("gatewayType",qrPayDTO.getPayType());
        gateWayMap.put("state",StaticDataEnum.STATUS_1.getCode());
        GatewayDTO gatewayDTO = gatewayService.findOneGateway(gateWayMap);
//        qrPayFlowDTO.setGatewayId(gatewayDTO.getType().longValue());
//        updateFlow(qrPayFlowDTO, null, null, request);
        return gatewayDTO;
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
        if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
            try {
                userService.logCardFailedTime(qrPayFlowDTO.getPayUserId(), Long.parseLong(qrPayFlowDTO.getCardId()),true,request);
            }catch ( Exception e){
                log.error("卡支付交易成功后，清除失败次数失败，flowId："+withholdFlowDTO.getFlowId()+",exception:"+e.getMessage(),e);
            }
        }

        // 不需要进行清算和清算金额为0的交易
        if(qrPayFlowDTO.getIsNeedClear() == StaticDataEnum.NEED_CLEAR_TYPE_0.getCode() || qrPayFlowDTO.getRecAmount().compareTo(BigDecimal.ZERO) == 0
        || qrPayFlowDTO.getOrderSource() != StaticDataEnum.ORDER_SOURCE_0.getCode()){
            //更新交易状态为成功
            qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_31.getCode());
            qrPayService.updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
        }else{
            //需要进行清算

            // 当交易状态不是31时，为正常交易，需要改变交易状态为入账处理中
            // 当状态为31时为跑批处理，不改变交易状态
//            if(qrPayFlowDTO.getState() != StaticDataEnum.TRANS_STATE_31.getCode() ){
//                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_30.getCode());
//                updateFlowForConcurrency(qrPayFlowDTO, null, withholdFlowDTO, request);
//                //发送成功消息
//                sendMessage(qrPayFlowDTO.getRecUserId(), qrPayFlowDTO.getId(), request);
//            }

            //创建账户交易流水
            AccountFlowDTO accountFlowDTO = qrPayService.getAccountFlowDTO(qrPayFlowDTO);
            //保存账户交易流水
            accountFlowDTO.setId(accountFlowService.saveAccountFlow(accountFlowDTO, request));
            try {
                qrPayService.doAmountTrans(accountFlowDTO, qrPayFlowDTO, request);
            } catch (Exception e) {
                log.error("qrPay dealThirdSuccess Exception:"+e.getMessage(),e);
                //交易失败
                qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_32.getCode());
                qrPayService.updateFlow(qrPayFlowDTO, null, null, request);
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
        }

    }

    /**
     *  处理latpay返回失败结果
     * @author zhangzeyuan
     * @date 2021/7/1 14:36
     * @param withholdFlowDTO
     * @param qrPayFlowDTO
     * @param request
     */
    private void handleLatPayFailed(WithholdFlowDTO withholdFlowDTO, QrPayFlowDTO qrPayFlowDTO, BigDecimal channelLimtRollbackAmt,  HttpServletRequest request) throws BizException{
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
            if(qrPayFlowDTO.getPayAmount().compareTo(BigDecimal.ZERO) > 0){
                log.error("分期付额度回滚开始");
                creditFrozenAmountRollback(qrPayFlowDTO.getPayUserId(), qrPayFlowDTO.getId() , qrPayFlowDTO.getPayAmount().subtract(channelLimtRollbackAmt), request);
            }
        }

        //更新交易失败状态
        qrPayFlowDTO.setState(StaticDataEnum.TRANS_STATE_22.getCode());
        qrPayService.updateFlow(qrPayFlowDTO, null, withholdFlowDTO, request);
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
        creditInfo.put("cardPayPercentage", Constant.PAY_CREDIT_NEED_CARDPAY_AMOUNT_RATE);//卡支付比例
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


    @Override
    public void redisApiqrPayFlowDoubtHandle() throws Exception {
            apiQrPayFlowDAO.redisUpdateDoubtHandle();

    }

}
