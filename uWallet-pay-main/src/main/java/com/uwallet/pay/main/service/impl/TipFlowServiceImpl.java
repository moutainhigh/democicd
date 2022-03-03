package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.TipFlowDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.ClearDetail;
import com.uwallet.pay.main.model.entity.ClearFlowDetail;
import com.uwallet.pay.main.model.entity.TipFlow;
import com.uwallet.pay.main.model.excel.DonationFlowExcel;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 小费流水表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 小费流水表
 * @author: zhangzeyuan
 * @date: Created in 2021-08-10 16:01:03
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Service
@Slf4j
public class TipFlowServiceImpl extends BaseServiceImpl implements TipFlowService {

    @Autowired
    private TipFlowDAO tipFlowDAO;

    @Resource
    private ClearFlowDetailService clearFlowDetailService;

    @Resource
    private ClearDetailService clearDetailService;

    @Resource
    private ClearBatchService clearBatchService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private CSVService cSVService;

    @Value("${spring.clearFilePath}")
    private String filePath;

    @Resource
    private TipClearFileRecordService tipClearFileRecordService;

    @Override
    public void saveTipFlow(@NonNull TipFlowDTO tipFlowDTO, HttpServletRequest request) throws BizException {
        TipFlow tipFlow = BeanUtil.copyProperties(tipFlowDTO, new TipFlow());
        log.info("save TipFlow:{}", tipFlow);
        if (tipFlowDAO.insert((TipFlow) this.packAddBaseProps(tipFlow, request)) != 1) {
            log.error("insert error, data:{}", tipFlow);
            throw new BizException("Insert tipFlow Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTipFlowList(@NonNull List<TipFlow> tipFlowList, HttpServletRequest request) throws BizException {
        if (tipFlowList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = tipFlowDAO.insertList(tipFlowList);
        if (rows != tipFlowList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, tipFlowList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateTipFlow(@NonNull Long id, @NonNull TipFlowDTO tipFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full update tipFlowDTO:{}", tipFlowDTO);
        TipFlow tipFlow = BeanUtil.copyProperties(tipFlowDTO, new TipFlow());
        tipFlow.setId(id);
        int cnt = tipFlowDAO.update((TipFlow) this.packModifyBaseProps(tipFlow, request));
        if (cnt != 1) {
            log.error("update error, data:{}", tipFlowDTO);
            throw new BizException("update tipFlow Error!");
        }
    }

    @Override
    public void updateTipFlowSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        tipFlowDAO.updatex(params);
    }

    @Override
    public void logicDeleteTipFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = tipFlowDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteTipFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = tipFlowDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public TipFlowDTO findTipFlowById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        TipFlowDTO tipFlowDTO = tipFlowDAO.selectOneDTO(params);
        return tipFlowDTO;
    }

    @Override
    public TipFlowDTO findOneTipFlow(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        TipFlow tipFlow = tipFlowDAO.selectOne(params);
        TipFlowDTO tipFlowDTO = new TipFlowDTO();
        if (null != tipFlow) {
            BeanUtils.copyProperties(tipFlow, tipFlowDTO);
        }
        return tipFlowDTO;
    }

    @Override
    public List<TipFlowDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<TipFlowDTO> resultList = tipFlowDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return tipFlowDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return tipFlowDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = tipFlowDAO.groupCount(conditions);
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
        return tipFlowDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = tipFlowDAO.groupSum(conditions);
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

    /**
     * 根据flowId修改状态
     *
     * @param flowId
     * @param state
     * @param request
     * @return java.lang.Integer
     * @author zhangzeyuan
     * @date 2021/8/10 17:22
     */
    @Override
    public Integer updateStateByFlowId(Long flowId, Integer state, HttpServletRequest request) {
        HashMap<String, Object> updateParams = Maps.newHashMapWithExpectedSize(5);
        updateParams.put("flowId", flowId);
        updateParams.put("state", state);
        updateParams.put("modifiedDate", System.currentTimeMillis());
        if(request!=null){
            updateParams.put("modifiedBy", getUserId(request));
            updateParams.put("ip", getIp(request));
        }
        return tipFlowDAO.updateStateByFlowId(updateParams);
    }


    /**
     * 小费结算
     *
     * @param merchantIds
     * @param request
     * @author zhangzeyuan
     * @date 2021/8/11 9:36
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClearBatchDTO tipSettlement(String merchantIds, HttpServletRequest request) throws BizException{

        Long userId = getUserId(request);
        long currentTimeMillis = System.currentTimeMillis();
        String ip = getIp(request);

        //根据商户ID查询未结算数据
        int count = tipFlowDAO.countUnsettledData(merchantIds);

        if(count < 1){
            throw new BizException(I18nUtils.get("tip.clear.data.not.exist", getLang(request)));
        }



        //生成clear batch 记录
        ClearBatchDTO clearBatch = new ClearBatchDTO();

        clearBatch.setClearType(StaticDataEnum.CLEAR_TYPE_5.getCode());
        Long batchId = clearBatchService.saveClearBatch(clearBatch, request);

        //关联 小费流水 batch id
        HashMap<String, Object> paramsMap = Maps.newHashMapWithExpectedSize(4);
        //处理中 todo
        paramsMap.put("settlementState", 2);
        paramsMap.put("batchId", batchId);
        paramsMap.put("modifiedBy", userId);
        paramsMap.put("modifiedDate", currentTimeMillis);
//        String flowIds = unsettledList.stream().map(e -> e.getId().toString()).collect(Collectors.joining(","));
//        paramsMap.put("ids", flowIds);
        paramsMap.put("merchantIds", merchantIds);
        tipFlowDAO.updateBatchIdByMerchantIds(paramsMap);

        //todo
        //查询满足记录的小费流水
        List<ClearFlowDetail> unsettledList = tipFlowDAO.getUnsettledIdList(merchantIds);

        if(CollectionUtils.isEmpty(unsettledList)){
            throw new BizException(I18nUtils.get("tip.clear.data.not.exist", getLang(request)));
        }
        //生成clear  flow detail 记录
        for(ClearFlowDetail clearFlowDetail: unsettledList){
            clearFlowDetail.setClearBatchId(batchId);
            clearFlowDetail.setId(SnowflakeUtil.generateId());
            clearFlowDetail.setCreatedBy(userId);
            clearFlowDetail.setCreatedDate(currentTimeMillis);
            clearFlowDetail.setModifiedDate(currentTimeMillis);
            clearFlowDetail.setModifiedBy(userId);
            clearFlowDetail.setIp(ip);
            clearFlowDetail.setStatus(1);
            clearFlowDetail.setState(0);
            clearFlowDetail.setBorrowAmount(clearFlowDetail.getBorrowAmount());
        }
        clearFlowDetailService.saveClearFlowDetailList(unsettledList, request);


        //总借款金额
        BigDecimal allBorrowAmount = BigDecimal.ZERO;
        //总清算金额
        BigDecimal allClearAmount = BigDecimal.ZERO;
        //总条数
        long totalAmount = 0;

        //生成clear  detail 记录
        List<ClearDetail> tipClearDetail = tipFlowDAO.getTipClearDetail(batchId);
        for (ClearDetail detail : tipClearDetail){
            allClearAmount = allClearAmount.add(detail.getClearAmount());
            allBorrowAmount = allBorrowAmount.add(detail.getBorrowAmount());
            totalAmount = totalAmount + detail.getClearNumber();
            detail.setId(SnowflakeUtil.generateId());
            detail.setCreatedBy(userId);
            detail.setCreatedDate(currentTimeMillis);
            detail.setModifiedBy(userId);
            detail.setModifiedDate(currentTimeMillis);
            detail.setIp(ip);
            detail.setStatus(1);
            detail.setState(0);
        }
        clearDetailService.saveClearDetailList(tipClearDetail ,request);

        //redis获取当天结算文件的序号
        Long fileNameNumber = redisUtils.incr(Constant.REDIS_KEY_TIP_SETTLEMENT_FILE_NUMBER, 1);
        //文件名
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmmssddMMyyyy");
        String fileName = Constant.TIP_SETTLEMENT_FILE_NAME_PREFIX + simpleDateFormat.format(new java.util.Date()) + "_" + autoGenericCode(fileNameNumber.toString(), 4) + ".csv";


        //更新clear batch 金额
        clearBatch.setBorrowAmount(allBorrowAmount);
        clearBatch.setClearAmount(allClearAmount);
        clearBatch.setTotalAmount(allClearAmount);
        clearBatch.setTotalNumber(totalAmount);
        clearBatch.setFileName(fileName);
        clearBatch.setUrl(filePath + fileName);
        clearBatch.setState(1);
        clearBatchService.updateClearBatch(batchId, clearBatch, request);

        //更新clear detail  clear flow detal 状态为成功

        paramsMap.clear();
        paramsMap.put("state", 1);
        paramsMap.put("clearBatchId", batchId);
        paramsMap.put("modifiedDate", System.currentTimeMillis());
        paramsMap.put("modifiedBy", userId);
        paramsMap.put("clearBatchId", batchId);

        clearDetailService.updateStateByBatchId(paramsMap);

        clearFlowDetailService.updateStateByBatchId(paramsMap);

        //导出csv文件
        List<ClearBillCSVDTO> billList = clearDetailService.getClearBillList(batchId);
        cSVService.createClearCsvFile(fileName, filePath, billList);

        //生成结算文件记录
        TipClearFileRecordDTO tipClearFileRecord = new TipClearFileRecordDTO();
        tipClearFileRecord.setFileName(fileName);
        tipClearFileRecord.setUrl(filePath + fileName);
        tipClearFileRecord.setSettlementDate(currentTimeMillis);
        tipClearFileRecord.setClearAmount(allClearAmount);
        tipClearFileRecord.setTotalNumber(totalAmount);
        tipClearFileRecord.setClearBatchId(batchId);
        tipClearFileRecordService.saveTipClearFileRecord(tipClearFileRecord, request);


        //更新 小费流水 为已清算
        paramsMap.clear();
        paramsMap.put("settlementState", 1);
        paramsMap.put("settlementTime", currentTimeMillis);
        paramsMap.put("modifiedBy", userId);
        paramsMap.put("modifiedDate", currentTimeMillis);
        paramsMap.put("batchId", batchId);
        tipFlowDAO.updateSettlementStateByBatchId(paramsMap);

        return clearBatch;
    }

    /**
     * 不够位数的在前面补0，保留num的长度位数字
     * @param code
     * @return
     */
    private String autoGenericCode(String code, int num) {
        return String.format("%0" + num + "d", Integer.parseInt(code) + 1);
    }


    @Override
    public Integer countTipMerchant(Map<String, Object> params) {
        List<TipMerchantsDTO> tipMerchantsDTO= tipFlowDAO.countTipMerchant(params);
        return tipMerchantsDTO.size();
    }

    @Override
    public List<TipMerchantsDTO> findTipMerchantData(Map<String, Object> params,Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<TipMerchantsDTO> tipMerchantsDTO= tipFlowDAO.countTipMerchant(params);
        List<Long> ids=new ArrayList<>();
        // 获取商户id
        for (TipMerchantsDTO merchantsDTO : tipMerchantsDTO) {
            Long merchantId = merchantsDTO.getMerchantId();
            if (merchantId!=null){
                ids.add(merchantId);
            }
        }
        params.put("ids",ids);
       List<JSONObject> data=tipFlowDAO.findTipMerchantData(params);
        for (TipMerchantsDTO merchantsDTO : tipMerchantsDTO) {
            for (JSONObject datum : data) {
                Long merchant_id = datum.getLong("merchant_id");
                Integer settlement_state = datum.getInteger("settlement_state");
                Integer counts = datum.getInteger("counts");
                BigDecimal sums = datum.getBigDecimal("sums");
                if (merchant_id!=null){
                    if (merchantsDTO.getMerchantId().equals(merchant_id)){
                        if (settlement_state!=null&&settlement_state.equals(StaticDataEnum.DONATION_CLEAR_STATUS_0.getCode())){
                            merchantsDTO.setUnclearedAmount(sums==null?BigDecimal.ZERO:sums);
                            merchantsDTO.setUnclearedNumber(counts==null?0:counts);
                        }else if(settlement_state!=null&&settlement_state.equals(StaticDataEnum.DONATION_CLEAR_STATUS_1.getCode())){
                            merchantsDTO.setClearedAmount(sums==null?BigDecimal.ZERO:sums);
                            merchantsDTO.setClearedNumber(counts==null?0:counts);
                        }else {
                            merchantsDTO.setDelayClearAmount(sums==null?BigDecimal.ZERO:sums);
                            merchantsDTO.setDelayClearNumber(counts==null?0:counts);
                        }
                    }
                }
            }
        }
        return tipMerchantsDTO;
    }

    @Override
    public int countOrderByMerchantId(JSONObject param) {
       int i= tipFlowDAO.countOrderByMerchantId(param);
        return i;
    }

    @Override
    public List<JSONObject> findOrderByUserId(JSONObject params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        List<JSONObject> resultList = tipFlowDAO.selectDTOList(params);
        if (resultList==null&&resultList.size()<=0){
            return new ArrayList<>();
        }
        for (JSONObject jsonObject : resultList) {
            // 查询卡信息
            try{
                jsonObject.put("cardType",jsonObject.getInteger("card_cc_type"));
                jsonObject.put("cardNo",jsonObject.getString("card_no"));
            }catch (Exception e){
                log.error("查询卡信息异常,:{}",e);
            }

            Long id = jsonObject.getLong("id");
            if (id!=null){
                jsonObject.put("id",id.toString());
            }
            Long created_date = jsonObject.getLong("created_date");
            if (created_date!=null){
                jsonObject.put("created_date",simpleDateFormat.format(created_date));
            }
            Long settlement_time = jsonObject.getLong("settlement_time");
            if (settlement_time!=null){
                jsonObject.put("settlement_time",simpleDateFormat.format(settlement_time));
            }
        }
        return resultList;
    }

    @Override
    public void updateSettlementState(JSONObject data, HttpServletRequest request) {
       int i= tipFlowDAO.updateSettlementState(data);
    }

    @Override
    public Workbook exportUserOrder(TipFlowClearDTO param, HttpServletRequest request) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        JSONObject result=new JSONObject();
        List<JSONObject> excel=tipFlowDAO.exportUserOrder(param);
        List<DonationFlowExcel> dtoList=new ArrayList<>();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        for (JSONObject jsonObject : excel) {
            DonationFlowExcel donationFlowExcel = new DonationFlowExcel();
            // 查询卡信息
            try{
                donationFlowExcel.setCardNo(jsonObject.getString("card_no"));
                Integer customerCcType = jsonObject.getInteger("card_cc_type");
                if (customerCcType!=null){
                    if (customerCcType.intValue()==StaticDataEnum.CARD_TYPE_10.getCode()){
                        donationFlowExcel.setCardType(StaticDataEnum.CARD_TYPE_10.getMessage());
                    }
                    else if (customerCcType.intValue()==StaticDataEnum.CARD_TYPE_20.getCode()){
                        donationFlowExcel.setCardType(StaticDataEnum.CARD_TYPE_20.getMessage());
                    }
                    else if (customerCcType.intValue()==StaticDataEnum.CARD_TYPE_30.getCode()){
                        donationFlowExcel.setCardType(StaticDataEnum.CARD_TYPE_30.getMessage());
                    }
                    else if (customerCcType.intValue()==StaticDataEnum.CARD_TYPE_40.getCode()){
                        donationFlowExcel.setCardType(StaticDataEnum.CARD_TYPE_40.getMessage());
                    }
                    else if (customerCcType.intValue()==StaticDataEnum.CARD_TYPE_50.getCode()){
                        donationFlowExcel.setCardType(StaticDataEnum.CARD_TYPE_50.getMessage());
                    }
                    else if (customerCcType.intValue()==StaticDataEnum.CARD_TYPE_60.getCode()){
                        donationFlowExcel.setCardType(StaticDataEnum.CARD_TYPE_60.getMessage());
                    }
                }
            }catch (Exception e){
                log.error("查询卡信息异常,:{}",e);
            }
            donationFlowExcel.setDiscount(jsonObject.getBigDecimal("discount")==null?"$0.00":"$"+jsonObject.getBigDecimal("discount")+"");
            donationFlowExcel.setFullName(jsonObject.getString("user_name")==null?"":jsonObject.getString("user_name"));
            donationFlowExcel.setMerchant(jsonObject.getString("practical_name"));
            donationFlowExcel.setMobile(jsonObject.getString("phone"));
            donationFlowExcel.setOrderAmount(jsonObject.getBigDecimal("trans_amount")==null?"$0.00":"$"+jsonObject.getBigDecimal("trans_amount")+"");
            donationFlowExcel.setOrderNo(jsonObject.getString("trans_no")==null?"":jsonObject.getString("trans_no"));
            donationFlowExcel.setOrderTime(jsonObject.getLong("created_date")==null?"":simpleDateFormat.format(jsonObject.getLong("created_date")));
            Integer transType = jsonObject.getInteger("trans_type");
            if (transType!=null){
                if (transType.intValue()==StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode()){
                    donationFlowExcel.setPaymentMode("Credit");
                }
                else if (transType.intValue()==StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode()){
                    donationFlowExcel.setPaymentMode("Card");
                }
            }
            donationFlowExcel.setPayoMoney(jsonObject.getBigDecimal("red_envelope_amount")==null?"$0.00":"$"+jsonObject.getBigDecimal("red_envelope_amount"));
            Integer settlementState = jsonObject.getInteger("settlement_state");
            if (settlementState!=null){
                // 0：未结算 1：已结算 2：结算中 3：延迟结算
                if (settlementState.intValue()==StaticDataEnum.DONATION_CLEAR_STATUS_0.getCode()){
                    donationFlowExcel.setSettlementStatus(StaticDataEnum.DONATION_CLEAR_STATUS_0.getMessage());
                }
                else if (settlementState.intValue()==StaticDataEnum.DONATION_CLEAR_STATUS_1.getCode()){
                    donationFlowExcel.setSettlementStatus(StaticDataEnum.DONATION_CLEAR_STATUS_1.getMessage());
                }
                else if (settlementState.intValue()==StaticDataEnum.DONATION_CLEAR_STATUS_2.getCode()){
                    donationFlowExcel.setSettlementStatus(StaticDataEnum.DONATION_CLEAR_STATUS_2.getMessage());
                }
                else if (settlementState.intValue()==StaticDataEnum.DONATION_CLEAR_STATUS_3.getCode()){
                    donationFlowExcel.setSettlementStatus(StaticDataEnum.DONATION_CLEAR_STATUS_3.getMessage());
                }
            }
            donationFlowExcel.setSettlementTime(jsonObject.getLong("settlement_time")==null?"":simpleDateFormat.format(jsonObject.getLong("settlement_time")));
            donationFlowExcel.setTip(jsonObject.getBigDecimal("tip")==null?"$0.00":"$"+jsonObject.getBigDecimal("tip")+"");
            donationFlowExcel.setTotalAmount(jsonObject.getBigDecimal("pay_amount")==null?"$0.00":"$"+jsonObject.getBigDecimal("pay_amount")+"");
            donationFlowExcel.setTxnFee(jsonObject.getBigDecimal("fee")==null?"$0.00":"$"+jsonObject.getBigDecimal("fee")+"");
            donationFlowExcel.setDonation(jsonObject.getBigDecimal("amount")==null?"$0.00":"$"+jsonObject.getBigDecimal("amount")+"");
            dtoList.add(donationFlowExcel);
        }
        result.put("excelList",dtoList);
        // 查询总金额 总条数
        List<JSONObject> total = tipFlowDAO.findTotal(param);
        result.put("total",total);
        // 文件名称
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("data");
        sheet.setColumnWidth(1, 256 * 25 + 184);
        sheet.setColumnWidth(0, 256 * 25 + 184);
        sheet.setColumnWidth(2, 256 * 10 + 184);
        sheet.setColumnWidth(3, 256 * 20 + 184);
        sheet.setColumnWidth(6, 256 * 25 + 184);
        sheet.setColumnWidth(6, 256 * 25 + 184);
        sheet.setColumnWidth(15, 256 * 25 + 184);
        sheet.setColumnWidth(16, 256 * 25 + 184);
        sheet.setColumnWidth(14, 256 * 25 + 184);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);
        Field[] declaredFields = DonationFlowExcel.class.getDeclaredFields();
        String[] head=new String[]{
                "支付订单号\nOrder No.",
                "商户名\nMerchant",
                "用户姓名\nFull Name",
                "用户手机号\nMobile",
                "付款方式\nPayment Mode",
                "卡类型\nCard Type",
                "卡号\nCard No.",
                "支付订单金额\nOrder Amount",
                "实付金额\nTotal Amount",
                "折扣金额\nDiscount",
                "红包金额\nPayo Money",
                "手续费\nTXN Fee",
                "小费金额\nTip",
                "捐赠金额\nDonation",
                "订单时间\nOrder Time",
                "结算时间\nSettlement Time",
                "结算状态\nSettlement Status"};
        Row row1 = sheet.createRow(0);
        for (int i = 0; i <declaredFields.length ; i++) {
            Cell cell = row1.createCell(i);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(head[i]);
        }
        for (int i = 0; i < dtoList.size(); i++) {
            Row row = sheet.createRow(i+1);
            for (int j = 0; j < declaredFields.length; j++) {
                Object property = PropertyUtils.getProperty(dtoList.get(i), declaredFields[j].getName());
                // 判断字段的数据类型
                if (null != property) {
                    if (java.sql.Date.class.equals(declaredFields[j].getType())) {
                        row.createCell(j).setCellValue((Date) property);
                    } else if (String.class.equals(declaredFields[j].getType())) {
                        row.createCell(j).setCellValue((String) property);
                    } else if (Integer.class.equals(declaredFields[j].getType())) {
                        row.createCell(j).setCellValue((Integer) property);
                    }
                }
            }
        }
        String[] end=new String[]{"合计\nTotal","未结算小费笔数：0 \nUnliquidated No. of Tip","未结算小费金额：$0 \nUnliquidated Tip Amt.",
                "已结算小费笔数：0 \nSettled No. of Tip","已结算小费金额：$0 \nSettled Tip Amt.",
                "延迟结算小费笔数：0 \nDelay Settle No. of Tip","延迟结算小费金额：$0 \nDelay Settle Tip Amt."};
        for (int i = 0; i < total.size(); i++) {
            if (total.get(i).getInteger("settlement_state")==StaticDataEnum.DONATION_CLEAR_STATUS_0.getCode()){
                end[1]="未结算小费笔数："+total.get(i).getInteger("number")+"\nUnliquidated No. of Tip";
                end[2]="未结算小费金额：$"+total.get(i).getBigDecimal("amount")+"\nUnliquidated Tip Amt.";
            }
            else  if (total.get(i).getInteger("settlement_state")==StaticDataEnum.DONATION_CLEAR_STATUS_1.getCode()){
                end[3]="已结算小费笔数："+total.get(i).getInteger("number")+"\nSettled No. of Tip";
                end[4]="已结算小费金额：$"+total.get(i).getBigDecimal("amount")+"\nSettled Tip Amt.";
            }
            else  if (total.get(i).getInteger("settlement_state")==StaticDataEnum.DONATION_CLEAR_STATUS_3.getCode()){
                end[5]="延迟结算小费笔数："+total.get(i).getInteger("number")+"\nDelay Settle No. of Tip";
                end[6]="延迟结算小费金额：$"+total.get(i).getBigDecimal("amount")+"\nDelay Settle Tip Amt.";
            }
        }
        Row row = sheet.createRow(dtoList.size() + 1);
        for (int i = 0; i <end.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(end[i]);
        }
        return workbook;
    }

    /**
     * 小费结算失败
     *
     * @param clearDetailDTO
     * @param clearBatchDTO
     * @param request
     * @author zhangzeyuan
     * @date 2021/8/12 16:27
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleFailed(ClearDetailDTO clearDetailDTO, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws Exception {

        //根据 批次号查询 捐赠小费流水
        HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        paramMap.put("batchId", clearBatchDTO.getId());
        paramMap.put("merchantId", clearDetailDTO.getMerchantId());

        List<TipFlowDTO> tipFlowList = tipFlowDAO.selectDTO(paramMap);
        if(CollectionUtils.isEmpty(tipFlowList)){
            return;
        }
        String ids = "";
        for (TipFlowDTO tipFlowDTO : tipFlowList){
            ids += tipFlowDTO.getId() + ",";
        }
        paramMap.clear();;

        //更新流水状态为未清算
        paramMap.put("ids" , ids.substring(0, ids.length() - 1));
        paramMap.put("modifiedBy", getUserId(request));
        paramMap.put("modifiedDate", System.currentTimeMillis());
        paramMap.put("ip", getIp(request));

        tipFlowDAO.updateSettledStateRollback(paramMap);

        // 更新clear detail 状态
        clearDetailService.logicDeleteClearDetail(clearDetailDTO.getId(), request);

        //更新 clear flow detail
        HashMap<String, Object> maps = Maps.newHashMapWithExpectedSize(4);
        maps.put("state", 2);
        maps.put("modifiedBy", getUserId(request));
        maps.put("modifiedDate", System.currentTimeMillis());
        maps.put("merchantId", clearDetailDTO.getMerchantId());
        maps.put("clearBatchId", clearBatchDTO.getId());
        clearFlowDetailService.updateStateByBatchId(maps);

        //todo  更新clear  batch
        maps.clear();
        maps.put("clearBatchId", clearBatchDTO.getId());
        ClearFlowDetailDTO clearFlowDetailDTO = clearFlowDetailService.clearTotal(maps);

        //更新clear batch 金额
        ClearBatchDTO clearBatch = new ClearBatchDTO();
        clearBatch.setBorrowAmount(clearFlowDetailDTO.getBorrowAmount());
        clearBatch.setClearAmount(clearFlowDetailDTO.getClearAmount());
        clearBatch.setTotalAmount(clearFlowDetailDTO.getTransAmount());
        clearBatch.setTotalNumber(Long.valueOf(clearFlowDetailDTO.getCount()));
        clearBatchService.updateClearBatch(clearBatchDTO.getId(), clearBatch, request);

    }


    @Override
    public List<QrPayFlowDTO> clearedDetailTransFlowList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        return tipFlowDAO.clearedDetailTransFlowList(params);
    }

    @Override
    public Integer clearedDetailTransFlowCount(Long id) {
        return tipFlowDAO.clearedDetailTransFlowCount(id);
    }

}
