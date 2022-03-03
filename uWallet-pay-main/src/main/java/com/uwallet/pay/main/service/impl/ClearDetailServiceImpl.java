package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.ClearDetailDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.ClearDetail;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * 清算批次明细表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 清算批次明细表
 * @author: zhoutt
 * @date: Created in 2019-12-20 10:58:28
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class ClearDetailServiceImpl extends BaseServiceImpl implements ClearDetailService {

    @Autowired
    private ClearDetailDAO clearDetailDAO;
    @Autowired
    private ClearBatchService clearBatchService;
    @Autowired
    private QrPayFlowService qrPayFlowService;
    @Autowired
    private ApiMerchantService apiMerchantService;


    @Override
    public void saveClearDetail(@NonNull ClearDetailDTO clearDetailDTO, HttpServletRequest request) throws BizException {
        ClearDetail clearDetail = BeanUtil.copyProperties(clearDetailDTO, new ClearDetail());
        if(clearDetailDTO.getCreatedBy() != null  ){
            long now = System.currentTimeMillis();
            clearDetail.setId(SnowflakeUtil.generateId());
            clearDetail.setCreatedBy(clearDetailDTO.getCreatedBy());
            clearDetail.setCreatedDate(now);
            clearDetail.setModifiedBy(clearDetailDTO.getModifiedBy());
            clearDetail.setModifiedDate(now);
            clearDetail.setIp(getIp(request));
            clearDetail.setStatus(1);
        }else {
            clearDetail = (ClearDetail) this.packAddBaseProps(clearDetail, request);
        }
        log.info("save ClearDetail:{}", clearDetail);
        if (clearDetailDAO.insert(clearDetail) != 1) {
            log.error("insert error, data:{}", clearDetail);
            throw new BizException("Insert clearDetail Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveClearDetailList(@NonNull List<ClearDetail> clearDetailList, HttpServletRequest request) throws BizException {
        if (clearDetailList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = clearDetailDAO.insertList(clearDetailList);
        if (rows != clearDetailList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, clearDetailList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateClearDetail(@NonNull Long id, @NonNull ClearDetailDTO clearDetailDTO, HttpServletRequest request) throws BizException {
        log.info("full update clearDetailDTO:{}", clearDetailDTO);
        ClearDetail clearDetail = BeanUtil.copyProperties(clearDetailDTO, new ClearDetail());
        clearDetail.setId(id);
        if(request==null){
            clearDetail.setModifiedDate(System.currentTimeMillis());
        }else{
            clearDetail= (ClearDetail) this.packModifyBaseProps(clearDetail, request);
        }
        int cnt = clearDetailDAO.update(clearDetail);
        if (cnt != 1) {
            log.error("update error, data:{}", clearDetailDTO);
            throw new BizException("update clearDetail Error!");
        }
    }

    @Override
    public void updateClearDetailSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        clearDetailDAO.updatex(params);
    }

    @Override
    public void logicDeleteClearDetail(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = clearDetailDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteClearDetail(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = clearDetailDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ClearDetailDTO findClearDetailById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ClearDetailDTO clearDetailDTO = clearDetailDAO.selectOneDTO(params);
        return clearDetailDTO;
    }

    @Override
    public ClearDetailDTO findOneClearDetail(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        ClearDetail clearDetail = clearDetailDAO.selectOne(params);
        ClearDetailDTO clearDetailDTO = new ClearDetailDTO();
        if (null != clearDetail) {
            BeanUtils.copyProperties(clearDetail, clearDetailDTO);
        }
        return clearDetailDTO;
    }

    @Override
    public List<ClearDetailDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ClearDetailDTO> list = null;
        if(params.containsKey("clearBatchId")){
            ClearBatchDTO clearBatchDTO = clearBatchService.findClearBatchById(Long.parseLong(String.valueOf(params.get("clearBatchId"))));

            if(StaticDataEnum.CLEAR_TYPE_1.getCode() == clearBatchDTO.getClearType()){
                list = clearDetailDAO.selectApiPltClearBatchBorrow(params);
            }else if (StaticDataEnum.CLEAR_TYPE_6.getCode() == clearBatchDTO.getClearType()){
                list = clearDetailDAO.selectH5MerchantClearData(params);
            }else{
                list = clearDetailDAO.selectDTO(params);
            }
        }else{
            list = clearDetailDAO.selectDTO(params);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM.dd HH:mm", Locale.US);
        for(ClearDetailDTO clearDetailDTO:list){
            clearDetailDTO.setTransTime(simpleDateFormat.format(clearDetailDTO.getCreatedDate()));
        }
        return list;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return clearDetailDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return clearDetailDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = clearDetailDAO.groupCount(conditions);
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
        return clearDetailDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = clearDetailDAO.groupSum(conditions);
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
    public List<ClearDetailDTO> getClearBatch(Map<String, Object> params) {
        return clearDetailDAO.getClearBatch(params);
    }

    @Override
    public void clearDoubtHandle() {
//        List<ClearDetailDTO> doubtList = clearDetailDAO.getClearDoubt(StaticDataEnum.TRANS_STATE_0.getCode());
//        for(ClearDetailDTO clearDetailDTO : doubtList){
//            try{
//                //查询原交易流水
//                AccountFlowDTO accountFlowDTO = accountFlowService.selectLatestByFlowId(clearDetailDTO.getId());
//                if (accountFlowDTO.getId() ==null || accountFlowDTO.getState() == StaticDataEnum.TRANS_STATE_2.getCode()) {
//                    //无交易记录或者记录为失败
//                    //交易失败
//                    clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
//                    updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,null);
//
//                    continue;
//                } else if (accountFlowDTO.getState() == StaticDataEnum.TRANS_STATE_1.getCode()) {
//                    //交易成功
//                    //回滚操作
//                    clearBatchService.doClearRollback(accountFlowDTO,clearDetailDTO,null);
//                    continue;
//                }
//                JSONObject data;
//                data = serverService.transactionInfo(accountFlowDTO.getOrderNo().toString());
//                if (data != null ) {
//                    if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
//                        //成功
//                        //记录交易失败
//                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
//                        accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,null);
//                        //回滚
//                        clearBatchService.doClearRollback(accountFlowDTO,clearDetailDTO,null);
//                    } else if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
//                        //失败
//                        //记录交易失败
//                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
//                        accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,null);
//                        //记录状态
//                        clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
//                        updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,null);
//                    }
//                }else{
//                    // 查询无信息为失败
//                    //记录交易失败
//                    accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
//                    accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,null);
//                    //记录状态
//                    clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
//                    updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,null);
//                }
//            }catch (Exception e){
//                log.info("批次号：{}，流水：{}，userId:{},可疑查证异常：",clearDetailDTO.getClearBatchId(),
//                        clearDetailDTO.getId(),clearDetailDTO.getUserId(),e.getMessage(),e);
//
//                continue;
//            }
//
//        }
    }

    @Override
    public void clearRollbackDoubtHandle() {
        //
//        List<ClearDetailDTO> doubtList = clearDetailDAO.getClearDoubt(StaticDataEnum.TRANS_STATE_4.getCode());
//        for(ClearDetailDTO clearDetailDTO : doubtList){
//            try{
//                //查询原交易流水
//                Map<String ,Object> cMap = new HashMap<>();
//                AccountFlowDTO accountFlowDTO = accountFlowService.selectLatestByFlowId(clearDetailDTO.getId());
//                if (accountFlowDTO.getId() ==null || accountFlowDTO.getState() == StaticDataEnum.TRANS_STATE_2.getCode()) {
//                    //无交易记录或者记录为失败
//                    //交易失败
//                    clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_5.getCode());
//                    updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,null);
//                    continue;
//                } else if (accountFlowDTO.getState() == StaticDataEnum.TRANS_STATE_1.getCode()) {
//                    //交易成功
//                    clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
//                    updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,null);
//                    continue;
//                }
//                JSONObject data;
//                data = serverService.transactionInfo(accountFlowDTO.getOrderNo().toString());
//                if (data != null ) {
//                    if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_1.getCode()) {
//                        //成功
//                        //记录交易失败
//                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
//                        accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,null);
//                        clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
//                        updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,null);
//                    } else if (data.getInteger("state").intValue() == StaticDataEnum.TRANS_STATE_2.getCode()) {
//                        //失败
//                        //记录交易失败
//                        accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
//                        accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,null);
//                        //记录状态
//                        clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_5.getCode());
//                        updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,null);
//                    }
//                }else{
//                    // 查询无信息为失败
//                    //记录交易失败
//                    accountFlowDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
//                    accountFlowService.updateAccountFlow(accountFlowDTO.getId(),accountFlowDTO,null);
//                    //记录状态
//                    clearDetailDTO.setState(StaticDataEnum.TRANS_STATE_5.getCode());
//                    updateClearDetail(clearDetailDTO.getId(),clearDetailDTO,null);
//                }
//            }catch (Exception e){
//                log.info("批次号：{}，流水：{}，userId:{},回滚可疑查证异常：",clearDetailDTO.getClearBatchId(),
//                        clearDetailDTO.getId(),clearDetailDTO.getUserId(),e.getMessage(),e);
//
//                continue;
//            }
//        }
    }

    @Override
    public void clearRollbackFailHandle() {
//        List<ClearDetailDTO> failList = clearDetailDAO.getClearDoubt(StaticDataEnum.TRANS_STATE_5.getCode());
//        for(ClearDetailDTO clearDetailDTO : failList){
//            try {            //查询原交易成功流水
//                Map<String,Object> map = new HashMap<>();
//                map.put("state",StaticDataEnum.TRANS_STATE_1.getCode());
//                map.put("flowId",clearDetailDTO.getId());
//                map.put("transType",StaticDataEnum.ACC_FLOW_TRANS_TYPE_5.getCode());
//                AccountFlowDTO accountFlowDTO = accountFlowService.findOneAccountFlow(map);
//                clearBatchService.doClearRollback(accountFlowDTO,clearDetailDTO,null);
//
//            }catch (Exception e){
//                log.info("批次号：{}，流水：{}，userId:{},回滚失败处理异常：",clearDetailDTO.getClearBatchId(),
//                        clearDetailDTO.getId(),clearDetailDTO.getUserId(),e.getMessage(),e);
//
//                continue;
//            }
//
//        }

    }

    @Override
    public List<ClearBillCSVDTO> getClearBillList(Long clearBatchId) {
        return clearDetailDAO.getClearBillList(clearBatchId);
    }

    @Override
    public List<ClearDetailDTO> getClearABAList(Long clearBatchId) {
        return clearDetailDAO.getClearABAList(clearBatchId);
    }

    @Override
    public List<ClearDetailDTO> getClearBatchNew(Map<String, Object> map) {
        return clearDetailDAO.getClearBatchNew(map);
    }

    @Override
    public ClearDetailDTO clearTotal(Map<String, Object> map) {
        return clearDetailDAO.clearTotal(map);
    }

    @Override
    public List<ClearDetailDTO> getClearDoubt(int code) {
        return clearDetailDAO.getClearDoubt(code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createApiPlatformClearData(Map<String, Object> params, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("state", "0");
        updateMap.put("batchId",clearBatchDTO.getId());
        //查询要清算的交易列表，得到应清算金额和交易信息
        List<ClearDetailDTO> clearList = clearDetailDAO.geApiPlatformClearData(updateMap);
        for(ClearDetailDTO clearDetailDTO:clearList ){
            this.saveClearDetail(clearDetailDTO,request);
        }
    }

    @Override
    public void clearData(Map<String, Object> map) {
        clearDetailDAO.clearData(map);
    }

    @Override
    public List<ClearBillCSVDTO> getApiPltClearBillList(Long id) {
        return clearDetailDAO.getApiPltClearBillList( id);
    }

    @Override
    public List<Map<String, Object>> clearFlowListGroupByDate(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        List<Map<String, Object>> result = new ArrayList<>();
        //查询已清算
        List<ClearDetailDTO> clearDetailDTOS = this.find(params, scs, pc);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM.dd ,yyyy", Locale.US);

        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("MMM.dd HH:mm", Locale.US);
        String date = null;
        String format = null;
        Map<String ,Object> dateMap = new HashMap<>();
        List<ClearDetailDTO> dateList = new ArrayList<>();

        for(ClearDetailDTO clearDetailDTO:clearDetailDTOS){
            clearDetailDTO.setTransTime(simpleDateFormat2.format(clearDetailDTO.getCreatedDate()));
            format = simpleDateFormat.format(clearDetailDTO.getCreatedDate());
            if(!format.equals(date)){
                if(date != null){
                    //非首次
                    dateMap.put("list",dateList);
                    result.add(dateMap);
                    dateList = new ArrayList<>();
                    dateMap = new HashMap<>();
                }
                dateList.add(clearDetailDTO);
                date = format;
                //新日期新累计
                dateMap.put("date",date);

                //计算本日总金额和总笔数
                //获得本日起止时间
                Long now = clearDetailDTO.getCreatedDate();
                Map<String,Long> timeMap = qrPayFlowService.getDayStartTime(now);
                params.put("start",timeMap.get("start"));
                params.put("end",timeMap.get("end"));
                List<ClearDetailDTO> totalList = this.find(params,null,null);

                BigDecimal totalClearAmount =  totalList.stream().map(ClearDetailDTO::getClearAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                dateMap.put("totalClearAmount",totalClearAmount);
                dateMap.put("totalNum",totalList.size());
            }else{
                dateList.add(clearDetailDTO);
            }
        }
        dateMap.put("date",date);
        dateMap.put("list",dateList);
        result.add(dateMap);
        return result;
    }

    @Override
    public int getClearedDetailListCount(Map<String, Object> params) {
        return clearDetailDAO.getClearedDetailListCount(params);
    }

    @Override
    public List<ClearDetailDTO> getClearedDetailList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ClearDetailDTO> list = clearDetailDAO.getClearedDetailList(params);
        for (ClearDetailDTO clearDetailDTO : list){
            if(clearDetailDTO.getClearType() == StaticDataEnum.CLEAR_TYPE_6.getCode()){
                ApiMerchantDTO merchantDTO = apiMerchantService.findApiMerchantById(clearDetailDTO.getMerchantId());
                if(merchantDTO!=null && merchantDTO.getId() != null){
                    clearDetailDTO.setPracticalName(merchantDTO.getPracticalName());
                    clearDetailDTO.setCity(Integer.parseInt(merchantDTO.getCity()));
                }

            }
        }
        return  list;
    }

    /*@Override
    public Object clearedDetailTransFlowList(Long id ,HttpServletRequest request) throws BizException{
        ClearDetailDTO clearDetailDTO = this.findClearDetailById(id);

        if(null == clearDetailDTO || null  == clearDetailDTO.getId()){
            throw new BizException(I18nUtils.get("clear.data.not.exist", getLang(request)));
        }

        ClearBatchDTO clearBatchDTO = clearBatchService.findClearBatchById(clearDetailDTO.getClearBatchId());

        if(null == clearBatchDTO || null  == clearBatchDTO.getId()){
            throw new BizException(I18nUtils.get("clear.data.not.exist", getLang(request)));
        }

        if(clearBatchDTO.getClearType() == StaticDataEnum.CLEAR_TYPE_0.getCode() || clearBatchDTO.getClearType() == StaticDataEnum.CLEAR_TYPE_3.getCode()){
            List<QrPayFlowDTO> list = qrPayFlowService.clearedDetailTransFlowList(id);
            return list;
        }else if(clearBatchDTO.getClearType() == StaticDataEnum.CLEAR_TYPE_2.getCode()){
            List<WholeSalesFlowDTO> list = wholeSalesFlowService.clearedDetailTransFlowList(id);
            return list;
        }else if(clearBatchDTO.getClearType() == StaticDataEnum.CLEAR_TYPE_4.getCode()){
            return donationFlowService.getClearBatchList(clearBatchDTO.getId());
        }else{
            return null;
        }

    }*/

    @Override
    public void dealWholeSaleClear(Long id) {
        clearDetailDAO.dealWholeSaleClearSuccess(id);
        clearDetailDAO.dealWholeSaleClearFail(id);
    }

    @Override
    public List<ClearDetail> getDonationClearBatch(Map<String, Object> params) {
        return clearDetailDAO.getDonationClearBatch(params);
    }

    @Override
    public int updateStateByBatchId(Map<String, Object> params) {
        return clearDetailDAO.updateStateByBatchId(params);
    }

    @Override
    public List<ClearDetailDTO> getH5MerchantClearDetail(Map<String, Object> params) {
        return clearDetailDAO.getH5MerchantClearDetail(params);
    }

    @Override
    public List<ClearBillCSVDTO> getH5ClearBillList(Long id) {
        return clearDetailDAO.getH5ClearBillList(id);
    }

    @Override
    public List<QrPayFlowDTO> h5ClearCount(Long id) {
        List<QrPayFlowDTO> list = clearDetailDAO.h5ClearCount(id);
        return list;
    }

}
