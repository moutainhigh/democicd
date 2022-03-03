package com.uwallet.pay.main.service.impl;

import com.google.common.collect.Maps;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.DonationFlowDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.*;
import com.uwallet.pay.main.service.ClearBatchService;
import com.uwallet.pay.main.service.ClearDetailService;
import com.uwallet.pay.main.service.ClearFlowDetailService;
import com.uwallet.pay.main.model.dto.DonationFlowDTO;
import com.uwallet.pay.main.model.dto.DonationUserListDTO;
import com.uwallet.pay.main.model.dto.QrPayFlowDTO;
import com.uwallet.pay.main.model.entity.DonationFlow;
import com.uwallet.pay.main.model.excel.DonationFlowExcel;
import com.uwallet.pay.main.service.DonationFlowService;
import com.uwallet.pay.main.service.ServerService;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.POIUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 捐赠流水
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 捐赠流水
 * @author: zhangzeyuan
 * @date: Created in 2021-07-22 08:37:50
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Service
@Slf4j
public class DonationFlowServiceImpl extends BaseServiceImpl implements DonationFlowService {

    @Autowired
    private DonationFlowDAO donationFlowDAO;
    @Autowired
    private ClearBatchService clearBatchService;
    @Autowired
    private ClearFlowDetailService clearFlowDetailService;
    @Autowired
    private ClearDetailService clearDetailService;
    @Autowired
    private ServerService serverService;

    @Override
    public void saveDonationFlow(@NonNull DonationFlowDTO donationFlowDTO, HttpServletRequest request) throws BizException {
        DonationFlow donationFlow = BeanUtil.copyProperties(donationFlowDTO, new DonationFlow());
        log.info("save DonationFlow:{}", donationFlow);
        if (donationFlowDAO.insert((DonationFlow) this.packAddBaseProps(donationFlow, request)) != 1) {
            log.error("insert error, data:{}", donationFlow);
            throw new BizException("Insert donationFlow Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDonationFlowList(@NonNull List<DonationFlow> donationFlowList, HttpServletRequest request) throws BizException {
        if (donationFlowList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = donationFlowDAO.insertList(donationFlowList);
        if (rows != donationFlowList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, donationFlowList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateDonationFlow(@NonNull Long id, @NonNull DonationFlowDTO donationFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full update donationFlowDTO:{}", donationFlowDTO);
        DonationFlow donationFlow = BeanUtil.copyProperties(donationFlowDTO, new DonationFlow());
        donationFlow.setId(id);
        int cnt = donationFlowDAO.update((DonationFlow) this.packModifyBaseProps(donationFlow, request));
        if (cnt != 1) {
            log.error("update error, data:{}", donationFlowDTO);
            throw new BizException("update donationFlow Error!");
        }
    }

    @Override
    public void updateDonationFlowSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        donationFlowDAO.updatex(params);
    }

    @Override
    public void logicDeleteDonationFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = donationFlowDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteDonationFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = donationFlowDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public DonationFlowDTO findDonationFlowById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        DonationFlowDTO donationFlowDTO = donationFlowDAO.selectOneDTO(params);
        return donationFlowDTO;
    }

    @Override
    public DonationFlowDTO findOneDonationFlow(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        DonationFlow donationFlow = donationFlowDAO.selectOne(params);
        DonationFlowDTO donationFlowDTO = new DonationFlowDTO();
        if (null != donationFlow) {
            BeanUtils.copyProperties(donationFlow, donationFlowDTO);
        }
        return donationFlowDTO;
    }

    @Override
    public List<DonationFlowDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<DonationFlowDTO> resultList = donationFlowDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return donationFlowDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return donationFlowDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = donationFlowDAO.groupCount(conditions);
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
        return donationFlowDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = donationFlowDAO.groupSum(conditions);
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
    public List<JSONObject> findOrderByUserId(JSONObject params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        List<JSONObject> resultList = donationFlowDAO.selectDTOList(params);
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
    public void updateSettlementState(JSONObject param, HttpServletRequest request) {
        int i=donationFlowDAO.updateSettlementState(param);
    }

    @Override
    public Workbook exportUserOrder(DonationFlowClearDTO param, HttpServletRequest request) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        JSONObject result=new JSONObject();
        List<JSONObject> excel=donationFlowDAO.exportUserOrder(param);
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
        List<JSONObject> total = donationFlowDAO.findTotal(param);
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
        String[] end=new String[]{"合计\nTotal","未结算捐赠笔数：0 \nOutstanding Donation","未结算捐赠金额：$0 \nOutstanding Donation Amount",
                "已结算捐赠笔数：0 \nSettlement Donation","已结算捐赠金额：$0 \nSettlement Donation Amount",
                "延迟结算捐赠笔数：0 \nDelay Settlement Donation","延迟结算捐赠金额：$0 \nDelay Settlement Donation Amount"};
        for (int i = 0; i < total.size(); i++) {
            if (total.get(i).getInteger("settlement_state")==StaticDataEnum.DONATION_CLEAR_STATUS_0.getCode()){
                end[1]="未结算捐赠笔数："+total.get(i).getInteger("amount")+"\nOutstanding Donation";
                end[2]="未结算捐赠金额：$"+total.get(i).getInteger("number")+"\nOutstanding Donation Amount";
            }
            else  if (total.get(i).getInteger("settlement_state")==StaticDataEnum.DONATION_CLEAR_STATUS_1.getCode()){
                end[3]="已结算捐赠笔数："+total.get(i).getInteger("amount")+"\nSettlement Donation";
                end[4]="已结算捐赠金额：$"+total.get(i).getInteger("number")+"\nSettlement Donation Amount";
            }
            else  if (total.get(i).getInteger("settlement_state")==StaticDataEnum.DONATION_CLEAR_STATUS_3.getCode()){
                end[5]="延迟结算捐赠笔数："+total.get(i).getInteger("amount")+"\nDelay Settlement Donation";
                end[6]="延迟结算捐赠金额：$"+total.get(i).getInteger("number")+"\nDelay Settlement Donation Amount";
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
     * 根据flow Id 更改状态值
     *
     * @param flowId
     * @param state
     * @param request
     * @author zhangzeyuan
     * @date 2021/7/22 16:43
     */
    @Override
    public void updateDonationFlowStateByFlowId(Long flowId, Integer state, HttpServletRequest request) {
        HashMap<String, Object> updateParams = Maps.newHashMapWithExpectedSize(5);
        updateParams.put("flowId", flowId);
        updateParams.put("state", state);
        updateParams.put("modifiedDate", System.currentTimeMillis());

        if(request != null){
            updateParams.put("modifiedBy", getUserId(request));
            updateParams.put("ip", getIp(request));
        }
        donationFlowDAO.updateStateByFlowId(updateParams);
    }

    @Override
    public Map<String,Object> getUserDonationList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        Map<String,Object> resultMap = new HashMap<>();
        params = getUnionParams(params, scs, pc);
        List<DonationUserListDTO> resultList = donationFlowDAO.getUserDonationList(params);
        List<DonationFlowDTO> dataList = donationFlowDAO.getDonationDataList(params);

        List<DonationFlowDTO> allUnSettledData = dataList.stream().filter(dto->dto.getSettlementState() == StaticDataEnum.CLEAR_STATE_TYPE_0.getCode()) .collect(Collectors.toList());
        List<DonationFlowDTO> allSettledData = dataList.stream().filter(dto->dto.getSettlementState() == StaticDataEnum.CLEAR_STATE_TYPE_1.getCode()) .collect(Collectors.toList());
        List<DonationFlowDTO> allDelayedSettledData = dataList.stream().filter(dto->dto.getSettlementState() == StaticDataEnum.CLEAR_STATE_TYPE_3.getCode()) .collect(Collectors.toList());

        if(allUnSettledData != null && allUnSettledData.size() >0 ){
            BigDecimal unSettledAmount = allUnSettledData.stream().map(DonationFlowDTO::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
            resultMap.put("totalUnSettledCount",allUnSettledData.size());
            resultMap.put("totalUnSettledAmount",unSettledAmount);
        }else{
            resultMap.put("totalUnSettledCount",0);
            resultMap.put("totalUnSettledAmount",BigDecimal.ZERO);
        }
        if(allSettledData != null && allSettledData.size() >0 ){
            BigDecimal allSettledAmount = allSettledData.stream().map(DonationFlowDTO::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
            resultMap.put("totalSettledCount",allSettledData.size());
            resultMap.put("totalSettledAmount",allSettledAmount);
        }else{
            resultMap.put("totalSettledCount",0);
            resultMap.put("totalSettledAmount",BigDecimal.ZERO);
        }
        if(allDelayedSettledData != null && allDelayedSettledData.size() >0 ){
            BigDecimal allDelayedSettledAmount = allDelayedSettledData.stream().map(DonationFlowDTO::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
            resultMap.put("totalDelayedSettledCount",allDelayedSettledData.size());
            resultMap.put("totalDelayedSettledAmount",allDelayedSettledAmount);
        }else{
            resultMap.put("totalDelayedSettledCount",0);
            resultMap.put("totalDelayedSettledAmount",BigDecimal.ZERO);
        }

        for (DonationUserListDTO donationUserListDTO : resultList){
            List<DonationFlowDTO> userData = dataList.stream().filter(dto->dto.getUserId().toString().equals(donationUserListDTO.getUserId() )) .collect(Collectors.toList());
            List<DonationFlowDTO> settledData = userData.stream().filter(dto->dto.getSettlementState() == StaticDataEnum.CLEAR_STATE_TYPE_1.getCode()) .collect(Collectors.toList());
            if(settledData != null && settledData.size() > 0){
                BigDecimal settledAmount = settledData.stream().map(DonationFlowDTO::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                donationUserListDTO.setSettledAmount(settledAmount);
                donationUserListDTO.setSettledCount(settledData.size());
            }else{
                donationUserListDTO.setSettledAmount(BigDecimal.ZERO);
                donationUserListDTO.setSettledCount(0);
            }

            List<DonationFlowDTO> unSettleData = userData.stream().filter(dto->dto.getSettlementState() == StaticDataEnum.CLEAR_STATE_TYPE_0.getCode()) .collect(Collectors.toList());
            if(unSettleData != null && unSettleData.size() > 0){
                BigDecimal unSettleAmount = unSettleData.stream().map(DonationFlowDTO::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                donationUserListDTO.setUnSettledAmount(unSettleAmount);
                donationUserListDTO.setUnSettledCount(unSettleData.size());
            }else{
                donationUserListDTO.setUnSettledAmount(BigDecimal.ZERO);
                donationUserListDTO.setUnSettledCount(0);
            }

            List<DonationFlowDTO> delaySettleData = userData.stream().filter(dto->dto.getSettlementState() == StaticDataEnum.CLEAR_STATE_TYPE_3.getCode()) .collect(Collectors.toList());
            if(delaySettleData != null && delaySettleData.size() > 0){
                BigDecimal delaySettleAmount = delaySettleData.stream().map(DonationFlowDTO::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                donationUserListDTO.setDelayAmount(delaySettleAmount);
                donationUserListDTO.setDelayCount(delaySettleData.size());
            }else{
                donationUserListDTO.setDelayAmount(BigDecimal.ZERO);
                donationUserListDTO.setDelayCount(0);
            }

        }
        resultMap.put("list",resultList);
        return resultMap;
    }

    @Override
    public int getUserDonationListCount(Map<String, Object> params) {
        return donationFlowDAO.getUserDonationListCount(params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clear(DonationFlowClearDTO donationFlowClearDTO, HttpServletRequest request) throws  Exception{
        ClearBatchDTO clearBatchDTO = new ClearBatchDTO();
        clearBatchDTO.setClearType(StaticDataEnum.CLEAR_TYPE_4.getCode());
        clearBatchDTO.setClearStartDate(donationFlowClearDTO.getStart());
        clearBatchDTO.setClearEndDate(donationFlowClearDTO.getEnd());
        clearBatchDTO.setId(clearBatchService.saveClearBatch(clearBatchDTO,request));
        JSONObject params =  (JSONObject) JSONObject.toJSON(donationFlowClearDTO);

        params.put("batchId",clearBatchDTO.getId());
        params.put("now",System.currentTimeMillis());
        params.put("modifiedBy",getUserId(request));
        params.put("ip",getIp(request));

        if ( donationFlowClearDTO.getUserIds() != null && donationFlowClearDTO.getUserIds().size() > 0){
            StringBuffer userIdList = new StringBuffer();
            for( int i = 0 ; i< donationFlowClearDTO.getUserIds().size() ;i ++){
                userIdList.append(donationFlowClearDTO.getUserIds().get(i)).append(",");
            }
            params.put("userIds",userIdList.toString().substring(0, userIdList.toString().length() - 1) );
        }
        int i = donationFlowDAO.clear(params);
        if(i == 0){
            log.error("捐献清算了0条数据");
            throw new BizException(I18nUtils.get("donation.clear.data.not.exist", getLang(request)));
        }
        // 记录清算交易明细流水
        createClearFlowDetail(clearBatchDTO,request);
        // 记录清算明细流水
        createClearDetail(clearBatchDTO,request);
        // 查询最终清算信息
        params.clear();
        params.put("clearBatchId",clearBatchDTO.getId());
        ClearFlowDetailDTO clearFlowDetailDTO = clearFlowDetailService.clearTotal(params);
        clearBatchDTO.setTotalNumber(clearFlowDetailDTO.getCount().longValue());

        ClearDetailDTO  clearDetailDTO = clearDetailService.clearTotal(params);
        clearBatchDTO.setTotalAmount(clearDetailDTO == null ? BigDecimal.ZERO : clearDetailDTO.getTransAmount());
        clearBatchDTO.setBorrowAmount(clearDetailDTO == null ? BigDecimal.ZERO : clearDetailDTO.getBorrowAmount());
        clearBatchDTO.setClearAmount(clearDetailDTO == null ? BigDecimal.ZERO : clearDetailDTO.getClearAmount());
        clearBatchDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
        clearBatchService.updateClearBatch(clearBatchDTO.getId(),clearBatchDTO,request);
    }

    /**
     * 捐赠清算记录 结算失败
     *
     * @param clearDetailDTO
     * @param clearBatchDTO
     * @param request
     * @author zhangzeyuan
     * @date 2021/7/28 11:25
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleFailed(ClearDetailDTO clearDetailDTO, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws Exception{
        //软删 ClearDetailDTO ClearBatchDTO

        //根据 批次号查询 捐赠订单流水
        HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        paramMap.put("batchId", clearBatchDTO.getId());
        List<DonationFlowDTO> donationDataList = donationFlowDAO.selectDTO(paramMap);
        if(CollectionUtils.isEmpty(donationDataList)){
            return;
        }
        String ids = "";
        for (DonationFlowDTO donationFlowDTO : donationDataList){
            ids += donationFlowDTO.getId() + ",";
        }
        paramMap.clear();;

        //更新捐赠流水状态为未清算
        paramMap.put("ids" , ids.substring(0, ids.length() - 1));
        paramMap.put("modifiedBy", getUserId(request));
        paramMap.put("modifiedDate", System.currentTimeMillis());
        paramMap.put("ip", getIp(request));

        donationFlowDAO.updateSettlementRollback(paramMap);

        //更新clear batch 状态 更新clear detail 状态
        clearDetailService.logicDeleteClearDetail(clearDetailDTO.getId(), request);

        clearBatchService.logicDeleteClearBatch(clearBatchDTO.getId(), request);
    }

    private void createClearDetail(ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException {
        Long batchId = clearBatchDTO.getId();
        String ip = getIp(request) ;
        Long createdBy = getUserId(request);
        Map<String, Object> params = new HashMap<>();
        params.put("state", StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
        params.put("batchId",clearBatchDTO.getId());
        //查询要清算的交易列表，得到应清算金额和交易信息
        List<ClearDetail> clearList = clearDetailService.getDonationClearBatch(params);
        Long now = System.currentTimeMillis();
        for(ClearDetail clearDetail:clearList ){
            clearDetail.setClearBatchId(batchId);
            clearDetail.setState(StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
            clearDetail.setId(SnowflakeUtil.generateId());
            clearDetail.setCreatedBy(createdBy);
            clearDetail.setCreatedDate(now);
            clearDetail.setModifiedBy(createdBy);
            clearDetail.setModifiedDate(now);
            clearDetail.setIp(ip);
            clearDetail.setStatus(1);
        }
        clearDetailService.saveClearDetailList(clearList,request);
    }

    private void createClearFlowDetail( ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException {
        List<ClearFlowDetail> list = clearFlowDetailService.getDonationDataByBatchId(clearBatchDTO.getId());
        String ip = getIp(request) ;
        Long createdBy = getUserId(request);

        Long now = System.currentTimeMillis();
        for(ClearFlowDetail clearFlowDetail:list){
            clearFlowDetail.setId(SnowflakeUtil.generateId());
            clearFlowDetail.setCreatedBy(createdBy);
            clearFlowDetail.setCreatedDate(now);
            clearFlowDetail.setModifiedBy(createdBy);
            clearFlowDetail.setModifiedDate(now);
            clearFlowDetail.setIp(ip);
            clearFlowDetail.setStatus(1);
            clearFlowDetail.setClearAmount(BigDecimal.ZERO);
            clearFlowDetail.setState(StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
            clearFlowDetail.setBorrowAmount(clearFlowDetail.getTransAmount());
            clearFlowDetail.setClearAmount(clearFlowDetail.getTransAmount());
        }

        clearFlowDetailService.saveClearFlowDetailList(list,request);
    }

    @Override
    public int countOrderByUserId(Map<String, Object> params) {
        return donationFlowDAO.countOrderByUserId(params);
    }

    /**
     * 获取已结算批次捐赠订单明细
     *
     * @param batchId
     * @return java.util.List<com.uwallet.pay.main.model.dto.DonationUserListDTO>
     * @author zhangzeyuan
     * @date 2021/7/27 9:24
     */
    @Override
    public List<QrPayFlowDTO> getClearBatchList(Long batchId) {
        return donationFlowDAO.getClearBatchList(batchId);
    }

}
