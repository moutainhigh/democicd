package com.uwallet.pay.main.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.LatPayErrorEnum;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.QrPayFlowDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 交易详情
 * </p>
 *
 * @package:  com.uwallet.pay.main.service
 * @description: 接入方平台表
 * @author: SHAO
 * @date: Created in 2021-01-20 18:55:53
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: SHAO
 */
@Service
@Slf4j
public class TransRecordServiceImpl extends BaseServiceImpl implements TransRecordService{


    @Autowired
    private MerchantService merchantService;

    @Resource
    private QrPayFlowDAO qrPayFlowDAO;

    @Resource
    private ServerService serverService;

    @Resource
    private WithholdFlowService withholdFlowService;

    @Resource
    private DonationFlowService donationFlowService;

    @Resource
    private UserService userService;

    @Autowired
    private QrPayService qrPayService;

    @Resource
    private CardService cardService;

    private static final String RECORD_DATE_FORMAT = "HH:mm:ss dd/MM/yyyy";

    @Override
    public int countTransactionRecord(JSONObject param) {
        return qrPayFlowDAO.countTransactionRecord(param);
    }

    @Override
    public int countRecordNew(JSONObject data) {
        return qrPayFlowDAO.countRecordNew(data);
    }

    @Override
    public Object transactionDetailsNew(JSONObject params, Vector<SortingContext> scs, PagingContext pc, HttpServletRequest request) {
        getUnionParams(params, scs, pc);
        if (null == scs || scs.size() == 0){
            params.put("defaultOrder",true);
            params.remove("scs");
        }
        List<JSONObject> res = qrPayFlowDAO.transactionDetailsNew(params);
        res.forEach(dto->{
            Integer transState = dto.getInteger("transState");
            dto.put("transStateStr",this.groupTransState(transState,request));
        });
        return res;
    }

    @Override
    public Object transactionDetails(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc,HttpServletRequest request) throws Exception {
        getUnionParams(params, scs, pc);
        formatMonth(params);
        List<AppTransactionDetailsDTO> res = qrPayFlowDAO.getTransRecord(params);
        if (CollectionUtil.isNotEmpty(res)) {
            List<JSONObject> jsonArray = new ArrayList<>();
            Map<String, List<AppTransactionDetailsDTO>> collect = res.stream().collect(Collectors.groupingBy(AppTransactionDetailsDTO::getMonthYear));

            for (String key : collect.keySet()) {
                JSONObject data= new JSONObject(5);
                data.put("date",key);
                List<AppTransactionDetailsDTO> list = collect.get(key);
//                list.forEach(dto->{
//                    //支付状态很多种, 前端只需要 成功,失败,处理中三种, 根据支付状态将支付状态分类
//                    dto.setTransStateStr(this.groupTransState(Integer.parseInt(dto.getTransState()),request));
//                    dto.setDisplayDate(this.reFormatDate(dto.getCreatedDate(),RECORD_DATE_FORMAT));
//                });
                BigDecimal totalAmt = BigDecimal.ZERO;
                for(AppTransactionDetailsDTO detailsDTO : list){
                    //支付状态很多种, 前端只需要 成功,失败,处理中三种, 根据支付状态将支付状态分类
                    detailsDTO.setTransStateStr(this.groupTransState(Integer.parseInt(detailsDTO.getTransState()),request));
                    detailsDTO.setDisplayDate(this.reFormatDate(detailsDTO.getCreatedDate(),RECORD_DATE_FORMAT));
                    //计算实付总计
                    if(Objects.nonNull(detailsDTO.getPayAmount())){
                        totalAmt =  totalAmt.add(detailsDTO.getPayAmount());
                    }

                }
                data.put("dateStr",this.formatDate(list.get(0).getMonthYearStr()));
                data.put("list", list);
//                data.put("totalAmt",list.stream().map(AppTransactionDetailsDTO::getPayAmount).reduce(BigDecimal.ZERO,BigDecimal::add));
                data.put("totalAmt", totalAmt);
                jsonArray.add(data);
            }

            jsonArray = jsonArray.stream()
                    .sorted(Comparator.comparing(json -> json.getLong("dateStr")))
                    .collect(Collectors.toList());
            Collections.reverse(jsonArray);
            return jsonArray;

        }
        return res;
    }

    /**
     * 转化时间
     * @param createdDate
     * @param s
     * @return
     */
    private String reFormatDate(Long createdDate, String s) {
       SimpleDateFormat simpleDateFormat = new SimpleDateFormat(RECORD_DATE_FORMAT,Locale.US);
       return simpleDateFormat.format(createdDate);
    }

    /**
     * 支付状态很多种, 前端只需要 成功,失败,处理中三种
     * 根据支付状态将支付状态分类
     * @param transState
     * @return
     */
    private String groupTransState(int transState,HttpServletRequest request) {
        String displayName = "";
        if (Arrays.asList(1,31).contains(transState)){
            //交易成功
            displayName = I18nUtils.get("trans.type.success",getLang(request));
        }else if (Arrays.asList(2,12,22).contains(transState)){
            //交易失败
            displayName = I18nUtils.get("trans.type.failed",getLang(request));
        }else if (Arrays.asList(104).contains(transState)){
            //Refund
            displayName = "Refund";
        }else if (Arrays.asList(105).contains(transState)){
            //Cancelled
            displayName = "Cancelled";
        }else {
            //交易处理中
            displayName = I18nUtils.get("trans.type.processing",getLang(request));
        }
        return displayName;
    }


    private Object formatDate(String key) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-yy");
        Date date = simpleDateFormat.parse(key);
        return date.getTime();
    }

    @Override
    public Object updateRecordIsShow(Long id, HttpServletRequest request) {
        JSONObject param = new JSONObject(7);
        param.put("time",System.currentTimeMillis());
        param.put("ip",getIp(request));
        param.put("id",id);
        param.put("userId",getUserId(request));
        qrPayFlowDAO.updateRecordIsShow(param);
        return null;
    }

    @Override
    public Object getRecordDetail(Long id,HttpServletRequest request) throws Exception{
        QrPayFlowDTO dto = qrPayFlowDAO.getRecordDetail(id, id.toString());
        if(null != dto){
            String cardNo = "";
            String cardCcType = "";
            try{
                // 查询账户系统卡信息
                JSONObject cardInfo = serverService.getCardNoAndTypeByCardId(Long.parseLong(dto.getCardId()));
                if(null != cardInfo){
                    if(StringUtils.isNotBlank(cardInfo.getString("customerCcType"))){
                        cardCcType = cardInfo.getString("customerCcType");
                    }
                    if(StringUtils.isNotBlank(cardInfo.getString("cardNo"))){
                        cardNo = cardInfo.getString("cardNo");
                    }
                }
            }catch (Exception e){
                log.error("查询账户卡信息异常,e:{}",e);
            }
            dto.setCardNo(cardNo);
            dto.setCardCcType(cardCcType);
        }
        return this.processOrderInfo(dto, request);
    }

    private QrPayFlowDTO processOrderInfo(QrPayFlowDTO dto,HttpServletRequest request) throws Exception{


        if (null != dto && null != dto.getId()){
            //计算折扣金额
            dto.setDiscountAmt(this.calDiscountAmt(dto));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy",Locale.US);
            //如果是分期付订单, 请求分期付系统,获取分期付订单
            if (dto.getTransType().equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode())){
                if (null == dto.getPayUserId() || null == dto.getCreditOrderNo()){
                    throw new BizException(I18nUtils.get("data.missing",getLang(request)));
                }
                JSONArray repayList = serverService.getRepayList(dto.getPayUserId().toString(), dto.getCreditOrderNo());
                /*List<JSONObject> res = new ArrayList<>(6);
                repayList.forEach(obj->{
                    JSONObject repay = JSONObject.parseObject(obj.toString());
                    if (null!=repay.getLong("createdDateStr"));
                    repay.put("createdDateStr",simpleDateFormat.format(repay.getLong("createdDateStr")));
                    res.add(repay);
                });*/
                //dto.setRepayList(JSONArray.parseArray(res.stream().sorted(Comparator.comparing(jsonObject -> jsonObject.getInteger("periodSort"))).collect(Collectors.toList()).toString()));
                dto.setRepayList(repayList);
                dto.setTransFee(new BigDecimal("0.22"));

            }

            dto.setPayAmount(dto.getPayAmount().add(dto.getDonationAmount()).add(dto.getTipAmount()));

            //设置展示交易状态
            dto.setTransStateStr(this.groupTransState(dto.getState(),request));

            SimpleDateFormat monthFormat = new SimpleDateFormat("dd MMM", Locale.US);
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            String monthStr = monthFormat.format(dto.getCreatedDate());
            String timeStr = timeFormat.format(dto.getCreatedDate());
            dto.setDisplayDate(monthStr + " at " + timeStr);
            //计算当前订单的折扣率
            dto = this.calDiscountRate(dto);

            return dto;
        }else {
            throw new BizException(I18nUtils.get("trans.record.not.found",getLang(request)));
        }
    }


    @Override
    public Object getRecordDetailByTransNo(String transNo, HttpServletRequest request) throws Exception {
        if (StringUtils.isBlank(transNo)){
            throw new BizException(I18nUtils.get("parameters.null",getLang(request)));
        }
        JSONObject data = new JSONObject(3);
        data.put("transNo",transNo);
        QrPayFlowDTO qrPayFlowDTO = qrPayFlowDAO.selectOneDTO(data);
        if (null == qrPayFlowDTO || null == qrPayFlowDTO.getId()){
            throw new BizException(I18nUtils.get("trans.record.not.found",getLang(request)));
        }
        data.clear();
        data.put("id", qrPayFlowDTO.getId());
        //封装交易结果显示名称
        String transStateStr =this.groupTransState(qrPayFlowDTO.getState(),request);
        data.put("transStateStr",transStateStr);
        if (StringUtils.isNotBlank(transStateStr) && transStateStr.equalsIgnoreCase(I18nUtils.get("trans.type.failed",getLang(request)))){
            data.put("failedMsg",this.packFailedMsg(qrPayFlowDTO,request));
        }
        return data;
    }

    /**
     * 获取交易详情结果
     * @author zhangzeyuan
     * @date 2021/7/2 13:46
     * @param transNo
     * @param request
     * @return java.lang.Object
     */
    @Override
    public Object getRecordDetailByTransNoV2(String transNo, HttpServletRequest request) throws Exception {
        if (StringUtils.isBlank(transNo)){
            throw new BizException(I18nUtils.get("parameters.null",getLang(request)));
        }
        JSONObject data = new JSONObject(3);
        data.put("transNo",transNo);
        QrPayFlowDTO qrPayFlowDTO = qrPayFlowDAO.selectOneDTO(data);
        if (null == qrPayFlowDTO || null == qrPayFlowDTO.getId()){
            throw new BizException(I18nUtils.get("trans.record.not.found",getLang(request)));
        }

        JSONObject result = new JSONObject(6);

        //交易状态
        Integer state = qrPayFlowDTO.getState();

        if (state.equals(StaticDataEnum.TRANS_STATE_1.getCode()) || state.equals(StaticDataEnum.TRANS_STATE_31.getCode())){
            //交易成功 组装数据
            result = successMessage(transNo,qrPayFlowDTO,result,request);

        }else if (state.equals(StaticDataEnum.TRANS_STATE_22.getCode())){

            //通道交易失败 封装通道信息
            result = thirdFailMessage(qrPayFlowDTO,result,request);

        }else if(state.equals(StaticDataEnum.TRANS_STATE_2.getCode()) || state.equals(StaticDataEnum.TRANS_STATE_12.getCode())){
            //交易失败
            result.put("resultState", 2);
            result.put("errorMessage", I18nUtils.get("trans.failed", getLang(request)));
        }else {
            //订单可疑
            // 如果交易通道是stripe，交易可疑的情况，需要到三方去查证
            if(Integer.parseInt(qrPayFlowDTO.getGatewayId()+"") == StaticDataEnum.GATEWAY_TYPE_8.getCode()){
                if(qrPayFlowDTO.getState() == StaticDataEnum.TRANS_STATE_20.getCode() || qrPayFlowDTO.getState() == StaticDataEnum.TRANS_STATE_23.getCode()){
                    // 调用单次三方查证
                    if(qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode()){
                        qrPayService.dealOneCreditFirstCardPayDoubt(qrPayFlowDTO);
                    }else if(qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode()){
                        qrPayService.dealOneThirdDoubtHandle(qrPayFlowDTO);
                    }

                    // 再次查询订单状态
                    QrPayFlowDTO newData = qrPayFlowDAO.selectOneDTO(data);

                    if (newData.getState().equals(StaticDataEnum.TRANS_STATE_1.getCode()) || newData.getState().equals(StaticDataEnum.TRANS_STATE_31.getCode())){
                        //交易成功 组装数据
                        result = successMessage(transNo,qrPayFlowDTO,result,request);

                    }else if (newData.getState().equals(StaticDataEnum.TRANS_STATE_22.getCode())){

                        //通道交易失败 封装通道信息
                        result = thirdFailMessage(qrPayFlowDTO,result,request);

                    }else{
                        result.put("resultState", 0);
                        result.put("errorMessage", I18nUtils.get("pay.order.suspicious.message", getLang(request)));
                    }
                }
            }else{

                result.put("resultState", 0);
                result.put("errorMessage", I18nUtils.get("pay.order.suspicious.message", getLang(request)));
            }

        }
        return result;
    }

    private JSONObject thirdFailMessage(QrPayFlowDTO qrPayFlowDTO, JSONObject result, HttpServletRequest request) {
        JSONObject data = new JSONObject(3);
        data.put("transNo",qrPayFlowDTO.getTransNo());

        result.put("resultState", 2);
        result.put("errorMessage", this.getTransChannelFailedMessage(qrPayFlowDTO, request));

        try{

            //卡删除状态
            int deleteCardPayState  = 1;
            //卡数量
            int cardCount = 0;

            // 查询剩余卡数量
            if(qrPayFlowDTO.getGatewayId().intValue() == StaticDataEnum.GATEWAY_TYPE_8.getCode()){
                //查询stripe卡
                JSONArray stripeCardList = cardService.getStripeCardList(qrPayFlowDTO.getPayUserId(), request);

                for(int i = 0; i < stripeCardList.size(); i ++){
                    cardCount += 1;

                    if (stripeCardList.getJSONObject(i).getString("id").equals(qrPayFlowDTO.getCardId())) {
                        //卡存在 没删卡
                        deleteCardPayState = 0;
                    }
                }

            }else{
                //latpay
                JSONArray latpayCardList = cardService.getLatpayCardList(qrPayFlowDTO.getPayUserId(), request);
                for(int i = 0; i < latpayCardList.size(); i ++){
                    cardCount += 1;

                    if (latpayCardList.getJSONObject(i).getString("id").equals(qrPayFlowDTO.getCardId())) {
                        //卡存在 没删卡
                        deleteCardPayState = 0;
                    }
                }
                /*JSONObject accountInfo = serverService.getAccountInfo(qrPayFlowDTO.getPayUserId());
                JSONArray latpayCardList = accountInfo.getJSONArray("cardDTOList");
                for(int i = 0; i < latpayCardList.size(); i ++){
                    if (latpayCardList.getJSONObject(i).getInteger("type") == StaticDataEnum.TIE_CARD_1.getCode()) {
                        cardCount += 1;
                        if (latpayCardList.getJSONObject(i).getString("id").equals(qrPayFlowDTO.getCardId())) {
                            //卡存在 没删卡
                            deleteCardPayState = 0;
                        }
                    }
                }*/
            }
            result.put("deleteCardPayState", deleteCardPayState);
            result.put("cardCount",  cardCount);
        }catch (Exception e){
            log.info("获取卡信息失败, e:" + e.getMessage(),e);
        }
        return  result;
    }

    private JSONObject successMessage(String transNo, QrPayFlowDTO qrPayFlowDTO, JSONObject result, HttpServletRequest request) throws BizException {
        result.put("resultState", 1);
        result.put("flowId", transNo);
        result.put("id", qrPayFlowDTO.getId());
        JSONObject dataJson = new JSONObject();
        dataJson.put("transNo", qrPayFlowDTO.getTransNo());
        dataJson.put("cardId", qrPayFlowDTO.getCardId().toString());
        dataJson.put("transAmount", qrPayFlowDTO.getTransAmount().toString());
        dataJson.put("useRedEnvelopeAmount", qrPayFlowDTO.getRedEnvelopeAmount().toString());
        dataJson.put("totalAmount", qrPayFlowDTO.getPayAmount().add(qrPayFlowDTO.getDonationAmount()).add(qrPayFlowDTO.getTipAmount()).toString());
        dataJson.put("payAmount", qrPayFlowDTO.getPayAmount().toString());

        //查询商户
        HashMap<String, Object> queryParamsMap = Maps.newHashMapWithExpectedSize(3);
        queryParamsMap.put("id", qrPayFlowDTO.getMerchantId());
        queryParamsMap.put("isAvailable", StaticDataEnum.AVAILABLE_1.getCode());
        MerchantDTO merchantDTO = merchantService.findOneMerchant(queryParamsMap);
        if(Objects.isNull(merchantDTO) || Objects.isNull(merchantDTO.getId())) {
            dataJson.put("merchantName", "");
        }else {
            dataJson.put("merchantName", merchantDTO.getPracticalName());
        }

        //整体出售折扣、折扣金额
        dataJson.put("wholeSalesUserDiscount", qrPayFlowDTO.getWholeSalesDiscount().multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
        dataJson.put("wholeSaleUserDiscountAmount", qrPayFlowDTO.getWholeSalesDiscountAmount().toString());

        //正常出售折扣、折扣金额
        BigDecimal baseDiscountAmount = qrPayFlowDTO.getBaseDiscountAmount();
        BigDecimal extraDiscountAmount = qrPayFlowDTO.getExtraDiscountAmount();
        BigDecimal markingDiscountAmount = qrPayFlowDTO.getMarkingDiscountAmount();
        BigDecimal normalSaleDiscountAmount = baseDiscountAmount.add(extraDiscountAmount).add(markingDiscountAmount);

        BigDecimal baseDiscount = qrPayFlowDTO.getBaseDiscount();
        BigDecimal extraDiscount = qrPayFlowDTO.getExtraDiscount();
        BigDecimal markingDiscount = qrPayFlowDTO.getMarkingDiscount();
        BigDecimal normalSaleDiscount = baseDiscount.add(extraDiscount).add(markingDiscount);

        dataJson.put("normalSaleUserDiscountAmount", normalSaleDiscountAmount.toString());
        dataJson.put("normalSalesUserDiscount", normalSaleDiscount.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());

        //订单创建时间
        Long orderCreatedDate = qrPayFlowDTO.getCreatedDate();
        SimpleDateFormat monthFormat = new SimpleDateFormat("dd MMM", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
        String monthStr = monthFormat.format(orderCreatedDate);
        String timeStr = timeFormat.format(orderCreatedDate);
        dataJson.put("orderCreatedDate", monthStr + " at " + timeStr);

        //查询三方流水
        JSONObject queryParam = new JSONObject(2);
        queryParam.put("flowId", qrPayFlowDTO.getId());
        WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(queryParam);

        if(null == withholdFlowDTO || null == withholdFlowDTO.getId()){
            throw new BizException(I18nUtils.get("trans.record.not.found",getLang(request)));
        }

        if(qrPayFlowDTO.getTransType().equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode())){
            dataJson.put("payType", 0);

            dataJson.put("cardPayRate", qrPayFlowDTO.getRate().multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
            dataJson.put("cardPayFee", qrPayFlowDTO.getFee());
        }else if(qrPayFlowDTO.getTransType().equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode())){

            dataJson.put("payType", 4);

            BigDecimal creditFirstPeriodAmount = withholdFlowDTO.getOrderAmount().subtract(qrPayFlowDTO.getTipAmount()).subtract(qrPayFlowDTO.getDonationAmount());

            BigDecimal remainingCreditPayAmount = qrPayFlowDTO.getPayAmount().subtract(withholdFlowDTO.getOrderAmount());
            //分期付
            dataJson.put("creditNeedCardPayAmount", withholdFlowDTO.getTransAmount().toString());

            dataJson.put("creditNeedCardPayNoFeeAmount", creditFirstPeriodAmount);
            dataJson.put("remainingCreditAmount", remainingCreditPayAmount.toString());

            //todo 分期付规则改动此处需要改动
            int period = 0;
            //下期还款金额
            BigDecimal creditNextRepayAmount = BigDecimal.ZERO;
            if(remainingCreditPayAmount.compareTo(new BigDecimal("20")) <= 0){
                period = 2;
                creditNextRepayAmount = remainingCreditPayAmount;
            }else if(remainingCreditPayAmount.compareTo(new BigDecimal("40")) <= 0){
                period = 3;
                creditNextRepayAmount = remainingCreditPayAmount.divide(new BigDecimal("2"), 2, RoundingMode.FLOOR);
            }else{
                period = 4;
                creditNextRepayAmount = remainingCreditPayAmount.divide(new BigDecimal("3"), 2, RoundingMode.FLOOR);
            }
            dataJson.put("period", period);
            dataJson.put("creditNextRepayAmount", creditNextRepayAmount.toString());

            dataJson.put("cardPayRate", withholdFlowDTO.getFeeRate().multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
            dataJson.put("cardPayFee", withholdFlowDTO.getCharge());
        }

        dataJson.put("cardNo", withholdFlowDTO.getCardNo());
        dataJson.put("cardCcType", withholdFlowDTO.getCardCcType());

        dataJson.put("donationAmount", qrPayFlowDTO.getDonationAmount());
        dataJson.put("tipAmount", qrPayFlowDTO.getTipAmount());

        result.put("data", dataJson);
        HashMap<String, Object> userQueryMap = Maps.newHashMapWithExpectedSize(1);
        userQueryMap.put("id", qrPayFlowDTO.getPayUserId());
        UserDTO oneUser = userService.findOneUser(userQueryMap);
        result.put("firstName", oneUser.getUserFirstName());
        result.put("lastName", oneUser.getUserLastName());


        return  result;
    }


    /**
     * 获取通道交易失败信息
     * @author zhangzeyuan
     * @date 2021/7/2 10:26
     * @return java.lang.String
     */
    private String getTransChannelFailedMessage(QrPayFlowDTO dto, HttpServletRequest request){
        String failedMsg = "";
        JSONObject queryParam = new JSONObject(2);
        queryParam.put("flowId",dto.getId());
        WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(queryParam);
        if (null != withholdFlowDTO && null != withholdFlowDTO.getId()) {
            String returnMessage = withholdFlowDTO.getReturnMessage();
            if (StaticDataEnum.LAT_PAY_BANK_STATUS_5.getMessage().equals(withholdFlowDTO.getReturnCode())) {
                if (LatPayErrorEnum.BANK_CODE_05.equals(returnMessage)) {
                    failedMsg = LatPayErrorEnum.BANK_CODE_05.getMessage();
                } else if (LatPayErrorEnum.BANK_CODE_06.getCode().equals(returnMessage)) {
                    failedMsg = LatPayErrorEnum.BANK_CODE_06.getMessage();
                } else if (LatPayErrorEnum.BANK_CODE_07.getCode().equals(returnMessage)) {
                    failedMsg = LatPayErrorEnum.BANK_CODE_07.getMessage();
                } else if (LatPayErrorEnum.BANK_CODE_41.getCode().equals(returnMessage)) {
                    failedMsg = LatPayErrorEnum.BANK_CODE_41.getMessage();
                } else if (LatPayErrorEnum.BANK_CODE_43.getCode().equals(returnMessage)) {
                    failedMsg = LatPayErrorEnum.BANK_CODE_43.getMessage();
                } else if (LatPayErrorEnum.BANK_CODE_51.getCode().equals(returnMessage)) {
                    failedMsg = LatPayErrorEnum.BANK_CODE_51.getMessage();
                } else {
                    failedMsg = LatPayErrorEnum.BANK_CODE_OTHER.getMessage();
                }
            } else if (StaticDataEnum.LP_RESPONSE_TYPE_0.getMessage().equals(withholdFlowDTO.getReturnCode())) {
                // 返回信息为反欺诈系统
                if (LatPayErrorEnum.SCSS_CODE_1003.getCode().equals(returnMessage)) {
                    failedMsg = LatPayErrorEnum.SCSS_CODE_1003.getMessage();
                } else if (LatPayErrorEnum.SCSS_CODE_1004.getCode().equals(returnMessage)) {
                    failedMsg = LatPayErrorEnum.SCSS_CODE_1004.getMessage();
                } else if (LatPayErrorEnum.SCSS_CODE_1005.getCode().equals(returnMessage)) {
                    failedMsg = LatPayErrorEnum.SCSS_CODE_1005.getMessage();
                } else if (LatPayErrorEnum.SCSS_CODE_1006.getCode().equals(returnMessage)) {
                    failedMsg = LatPayErrorEnum.SCSS_CODE_1006.getMessage();
                } else {
                    failedMsg = LatPayErrorEnum.SCSS_CODE_OTHER.getMessage();
                }
            } else {
                failedMsg = I18nUtils.get("trans.failed", getLang(request));
            }
        }
        return failedMsg;
    }

    /**
     * 封装错误信息
     * @param dto
     * @param request
     * @return
     */
    private String packFailedMsg(QrPayFlowDTO dto, HttpServletRequest request) {
        String failedMsg = "We couldn't process your payment. Please try again.";
        if (dto.getTransType().equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode())){

        }else {
            JSONObject queryParam = new JSONObject(2);
            queryParam.put("flowId",dto.getId());
            WithholdFlowDTO withholdFlowDTO = withholdFlowService.findOneWithholdFlow(queryParam);
            if (null != withholdFlowDTO && null != withholdFlowDTO.getId()) {

                //卡支付订单
                String returnMessage = withholdFlowDTO.getReturnMessage();
                if (StaticDataEnum.LAT_PAY_BANK_STATUS_5.getMessage().equals(withholdFlowDTO.getReturnCode())) {
                    if (LatPayErrorEnum.BANK_CODE_05.equals(returnMessage)) {
                        failedMsg = LatPayErrorEnum.BANK_CODE_05.getMessage();
                    } else if (LatPayErrorEnum.BANK_CODE_06.getCode().equals(returnMessage)) {
                        failedMsg = LatPayErrorEnum.BANK_CODE_06.getMessage();
                    } else if (LatPayErrorEnum.BANK_CODE_07.getCode().equals(returnMessage)) {
                        failedMsg = LatPayErrorEnum.BANK_CODE_07.getMessage();
                    } else if (LatPayErrorEnum.BANK_CODE_41.getCode().equals(returnMessage)) {
                        failedMsg = LatPayErrorEnum.BANK_CODE_41.getMessage();
                    } else if (LatPayErrorEnum.BANK_CODE_43.getCode().equals(returnMessage)) {
                        failedMsg = LatPayErrorEnum.BANK_CODE_43.getMessage();
                    } else if (LatPayErrorEnum.BANK_CODE_51.getCode().equals(returnMessage)) {
                        failedMsg = LatPayErrorEnum.BANK_CODE_51.getMessage();
                    } else {
                        failedMsg = LatPayErrorEnum.BANK_CODE_OTHER.getMessage();
                    }
                } else if (StaticDataEnum.LP_RESPONSE_TYPE_0.getMessage().equals(withholdFlowDTO.getReturnCode())) {
                    // 返回信息为反欺诈系统
                    if (LatPayErrorEnum.SCSS_CODE_1003.getCode().equals(returnMessage)) {
                        failedMsg = LatPayErrorEnum.SCSS_CODE_1003.getMessage();
                    } else if (LatPayErrorEnum.SCSS_CODE_1004.getCode().equals(returnMessage)) {
                        failedMsg = LatPayErrorEnum.SCSS_CODE_1004.getMessage();
                    } else if (LatPayErrorEnum.SCSS_CODE_1005.getCode().equals(returnMessage)) {
                        failedMsg = LatPayErrorEnum.SCSS_CODE_1005.getMessage();
                    } else if (LatPayErrorEnum.SCSS_CODE_1006.getCode().equals(returnMessage)) {
                        failedMsg = LatPayErrorEnum.SCSS_CODE_1006.getMessage();
                    } else {
                        failedMsg = LatPayErrorEnum.SCSS_CODE_OTHER.getMessage();
                    }
                } else {
                    failedMsg = I18nUtils.get("trans.failed", getLang(request));
                }
            }
        }
        return failedMsg;
    }

    /**
     * 计算订单折扣率 2021-04-14
     * @param dto
     * @return
     */
    private QrPayFlowDTO calDiscountRate(QrPayFlowDTO dto) {
        //整体出售折扣
        dto.setWholeSaleDiscountRate(dto.getWholeSalesDiscount().multiply(new BigDecimal("100")).setScale(0,BigDecimal.ROUND_HALF_UP));
        dto.setWholeSaleOffAmt(dto.getWholeSalesDiscountAmount());

        //正常出售折扣
        BigDecimal baseDiscountAmount = dto.getBaseDiscountAmount();
        BigDecimal extraDiscountAmount = dto.getExtraDiscountAmount();
        BigDecimal markingDiscountAmount = dto.getMarkingDiscountAmount();
        BigDecimal normalSaleDiscountAmount = baseDiscountAmount.add(extraDiscountAmount).add(markingDiscountAmount);

        BigDecimal baseDiscount = dto.getBaseDiscount();
        BigDecimal extraDiscount = dto.getExtraDiscount();
        BigDecimal markingDiscount = dto.getMarkingDiscount();
        BigDecimal normalSaleDiscount = baseDiscount.add(extraDiscount).add(markingDiscount);

        dto.setNormalSaleOffAmt(normalSaleDiscountAmount);
        dto.setNormalSaleDiscountRate(normalSaleDiscount.multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP));

        /*
          正常出售折扣率: --废弃
          固定折扣金额 = base_discount_amount/normal_sales_amount
          额外折扣率extra_discount_amount/normal_sales_amount
          折扣率 = marking_discount_amount/normal_sales_amount
          ==>3者相加 为正常出售折扣率
          整体出售折扣率: whole_sales_discount
         */
        /*if (dto.getWholeSalesAmount().compareTo(BigDecimal.ZERO) >0){
            //整体出售折扣率
            BigDecimal wholeSalesDiscount = null != dto.getWholeSalesDiscount() ? dto.getWholeSalesDiscount() : BigDecimal.ZERO;
            BigDecimal offAmt = wholeSalesDiscount.multiply(dto.getWholeSalesAmount());
            dto.setWholeSaleOffAmt(offAmt.setScale(2,BigDecimal.ROUND_HALF_UP));
            dto.setWholeSaleDiscountRate(wholeSalesDiscount.multiply(new BigDecimal("100")).setScale(0,BigDecimal.ROUND_HALF_UP));
        }else {
            dto.setWholeSaleDiscountRate(BigDecimal.ZERO);
            dto.setWholeSaleOffAmt(BigDecimal.ZERO);
        }
        if (dto.getNormalSaleAmount().compareTo(BigDecimal.ZERO) >0){
            BigDecimal baseDiscountAmount = null != dto.getBaseDiscountAmount() ? dto.getBaseDiscountAmount() : BigDecimal.ZERO;
            BigDecimal extraDiscountAmount = null != dto.getExtraDiscountAmount() ? dto.getExtraDiscountAmount() : BigDecimal.ZERO;
            BigDecimal markingDiscountAmount = null != dto.getMarkingDiscountAmount() ? dto.getMarkingDiscountAmount() : BigDecimal.ZERO;
            BigDecimal normalSaleAmount = dto.getNormalSaleAmount();
            //计算折扣率
            BigDecimal offAmt = baseDiscountAmount.add(extraDiscountAmount).add(markingDiscountAmount);
            BigDecimal rate = offAmt.multiply(new BigDecimal("100")).divide(normalSaleAmount,0,BigDecimal.ROUND_HALF_UP);
            //设置折扣金额, 折扣率
            dto.setNormalSaleOffAmt(offAmt);
            dto.setNormalSaleDiscountRate(rate);
        }else {
            dto.setNormalSaleOffAmt(BigDecimal.ZERO);
            dto.setNormalSaleDiscountRate(BigDecimal.ZERO);
        }*/
        return dto;
    }


    /**
     * 计算订单折扣金额
     * 折扣金额计算公式:
     *  discountAmt = `base_discount_amount`
     *                 +`extra_discount_amount`
     *                 +`marking_discount_amount`
     *                 +`whole_sales_amount` * `whole_sales_discount`
     * @param dto
     * @return
     */
    private BigDecimal calDiscountAmt(QrPayFlowDTO dto) {
        BigDecimal discountAmt = BigDecimal.ZERO;
        BigDecimal baseDiscountAmount = dto.getBaseDiscountAmount();
        BigDecimal extraDiscountAmount = dto.getExtraDiscountAmount();
        BigDecimal markingDiscountAmount = dto.getMarkingDiscountAmount();
        BigDecimal wholeSalesAmount = dto.getWholeSalesAmount();
        BigDecimal wholeSalesDiscount = dto.getWholeSalesDiscount();

        discountAmt = discountAmt.add(baseDiscountAmount != null? baseDiscountAmount : BigDecimal.ZERO)
                .add(extraDiscountAmount != null? extraDiscountAmount : BigDecimal.ZERO)
                .add(markingDiscountAmount != null? markingDiscountAmount : BigDecimal.ZERO)
                .add((wholeSalesAmount!= null? wholeSalesAmount : BigDecimal.ZERO).multiply(wholeSalesDiscount != null? wholeSalesDiscount : BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP));
        return discountAmt;
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    /**
     * 根据查询的月份，格式化 start，end 查询条件
     * @param params
     * @return
     */
    public Map<String, Object> formatMonth(Map<String, Object> params) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (null != params.get("queryDate") && StringUtils.isNotBlank(params.get("queryDate").toString())) {
            String queryDate = params.get("queryDate").toString();
            String[] split = queryDate.split("-");

            calendar.set(Calendar.YEAR, Integer.parseInt(split[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(split[1]) - 1);

            params.put("start", calendar.getTimeInMillis());

            calendar.add(Calendar.MONTH, 1);
            params.put("end", calendar.getTimeInMillis());
        }
        return params;
    }
    /**
     * 格式化 查询条件
     * @param params
     * @return
     */
    @Override
    public JSONObject formatParamJson(JSONObject params) {
        /*
           根据查询的月份，格式化 start，end 查询条件
         */
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (null != params.get("queryDate") && StringUtils.isNotBlank(params.get("queryDate").toString())) {
            //格式: dd/mm/yyyy-dd/mm/yyyy
            String queryDate = params.get("queryDate").toString();
            String[] split = queryDate.split("-");

            if (split.length >1){
                for (int i = 0; i < split.length; i++) {
                    String date = split[i];
                    String[] currentDate = date.split("/");
                    calendar.set(Calendar.YEAR,Integer.parseInt(currentDate[2]));
                    calendar.set(Calendar.MONTH,Integer.parseInt(currentDate[1])-1);
                    calendar.set(Calendar.DATE,Integer.parseInt(currentDate[0]));
                    if (i == 0){
                        params.put("start", calendar.getTimeInMillis());
                    }else {
                        params.put("end",calendar.getTimeInMillis()+86400000L);
                    }
                }
            }
        }
        //触发过去x天搜索条件
        BigDecimal lastDays = params.getBigDecimal("lastDays");
        if (null != lastDays){
            //获取明天00:00时间戳
            long now = DateUtil.beginOfDay(new Date(System.currentTimeMillis())).getTime()+86400000L;
            BigDecimal lastDate = new BigDecimal(now).subtract(lastDays.multiply(new BigDecimal("86400000")));
            params.put("now", now);
            params.put("lastDate",lastDate);
        }
        return params;
    }

    @Override
    public int countRecordTwo(JSONObject data,HttpServletRequest request) throws Exception {
        // 分期付+卡支付+逾期费订单
        int i = qrPayFlowDAO.countRecordNew(data);
        // 调用分期付查询逾期费订单
        Long userId = data.getLong("userId");
        JSONObject param=new JSONObject();
//        param.put("userId",userId);
        param.put("userId",getUserId(request));
        List<JSONObject> overdueFeeList=serverService.getOverdueFeeList(param);
        if (overdueFeeList==null){
            return i;
        }
        Integer transType = data.getInteger("transType");
        Long start = data.getLong("start");
        Long end = data.getLong("end");
        BigDecimal amtStart = data.getBigDecimal("amtStart");
        BigDecimal amtEnd = data.getBigDecimal("amtEnd");
        Integer p = data.getInteger("p");
        Integer s = data.getInteger("s");
        String scs1 = data.getString("scs");
        Long now = data.getLong("now");
        Long lastDate = data.getLong("lastDate");
        // 交易类型 2卡支付 22 分期付 33逾期费订单
        if (transType!=null){
            //分期付+逾期费
            if (transType.equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode())){
                overdueFeeList=overdueFeeList.stream().filter(dto -> ((dto.getInteger("transType").equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode()))||(dto.getInteger("transType").equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_33.getCode())))).collect(Collectors.toList());
            }else if (transType.equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode())) {
                overdueFeeList=overdueFeeList.stream().filter(dto -> ((dto.getInteger("transType").equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode())))).collect(Collectors.toList());
            }

        }
        // 时间返回
        if (start!=null&&end!=null){
            overdueFeeList=overdueFeeList.stream().filter(dto -> (start<=dto.getLong("createdDate")&&end>=dto.getLong("createdDate"))).collect(Collectors.toList());
        }
        // 金额范围
        if (amtStart!=null&&amtEnd!=null){
            overdueFeeList=overdueFeeList.stream().filter(dto -> (amtStart.compareTo(dto.getBigDecimal("payAmount"))<=0&&amtEnd.compareTo(dto.getBigDecimal("payAmount"))>=0)).collect(Collectors.toList());
        }
        // 过去多少天
        if (now!=null&&lastDate!=null){
            overdueFeeList=overdueFeeList.stream().filter(dto -> (lastDate<dto.getLong("createdDate")&&now>dto.getLong("createdDate"))).collect(Collectors.toList());
        }

        return overdueFeeList.size()+i;
    }

    @Override
    public Object transactionDetailsTwo(JSONObject data, Vector<SortingContext> scs, PagingContext pc, HttpServletRequest request) throws Exception {
        // 分期付+卡支付+逾期费订单
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId",getUserId(request));
//        jsonObject.put("userId",data.getLong("userId"));
        List<JSONObject> res = qrPayFlowDAO.transactionDetailsNew(jsonObject);
        List<JSONObject> overdueFeeList=serverService.getOverdueFeeList(jsonObject);
        res.addAll(overdueFeeList);
        log.info("合并该用户逾期费订单+卡支付订单+分期付订单,data:{}",res);
        Integer transType = data.getInteger("transType");
        Long start = data.getLong("start");
        Long end = data.getLong("end");
        BigDecimal amtStart = data.getBigDecimal("amtStart");
        BigDecimal amtEnd = data.getBigDecimal("amtEnd");
        Integer p = data.getInteger("p");
        Integer s = data.getInteger("s");
        String scs1 = data.getString("scs");
        Long now = data.getLong("now");
        Long lastDate = data.getLong("lastDate");
        // 交易类型 2卡支付 22 分期付 33逾期费订单
        if (transType!=null){
            //分期付+逾期费
            if (transType.equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode())){
                res=res.stream().filter(dto -> ((dto.getInteger("transType").equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode()))||(dto.getInteger("transType").equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_33.getCode())))).collect(Collectors.toList());
            }else if (transType.equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode())) {
                res=res.stream().filter(dto -> ((dto.getInteger("transType").equals(StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode())))).collect(Collectors.toList());
            }

        }
        // 时间返回
        if (start!=null&&end!=null){
            res=res.stream().filter(dto -> (start<dto.getLong("createdDate")&&end>dto.getLong("createdDate"))).collect(Collectors.toList());
        }
        // 金额范围
        if (amtStart!=null&&amtEnd!=null){
            res=res.stream().filter(dto -> (amtStart.compareTo(dto.getBigDecimal("payAmount"))<=0&&amtEnd.compareTo(dto.getBigDecimal("payAmount"))>=0)).collect(Collectors.toList());
        }
        // 过去多少天
        if (now!=null&&lastDate!=null){
            res=res.stream().filter(dto -> (lastDate<=dto.getLong("createdDate")&&now>=dto.getLong("createdDate"))).collect(Collectors.toList());
        }
        // 时间倒叙排序
        res=res.stream().sorted(Comparator.comparing((dto->(dto.getLong("createdDate")==null?0L:dto.getLong("createdDate"))),Comparator.reverseOrder())).collect(Collectors.toList());
        //分页
        if (p!=null&&s!=null){
            res = res.stream().skip((p - 1) * s).limit(s).collect(Collectors.toList());
        }

        res.forEach(dto->{
            Integer transState = dto.getInteger("transState");
            if (transState!=null){
                dto.put("transStateStr",this.groupTransState(transState,request));
            }
            dto.remove("createdDate");
        });
        return res;
    }
}
