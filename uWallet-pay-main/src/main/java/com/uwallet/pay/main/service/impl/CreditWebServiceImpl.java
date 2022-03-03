package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.ResultCode;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.CreditWebDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.*;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
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

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * @author: liming
 * @Date: 2020/10/19 16:03
 * @Description: 分期付web服务
 */
@Service
@Slf4j
public class CreditWebServiceImpl extends BaseServiceImpl implements CreditWebService {

    @Resource
    private CreditWebDAO creditWebDAO;
    @Resource
    private ClearBatchService clearBatchService;
    @Resource
    private ClearDetailService clearDetailService;
    @Resource
    private ClearFlowDetailService clearFlowDetailService;
    @Resource
    private WholeSalesFlowService wholeSalesFlowService;
    @Resource
    private WholeSalesFlowAndClearDetailService wholeSalesFlowAndClearDetailService;

    @Value("${spring.clearFilePath}")
    private String filePath;

    @Autowired
    private QrPayFlowService qrPayFlowService;
    @Autowired
    @Lazy
    private MerchantService merchantService;
    @Autowired
    private ServerService serverService;



    @Override
    public int searchMerchantListCount(Map<String, Object> params) {
        return creditWebDAO.searchMerchantListCount(params);
    }

    @Override
    public List<DiscountPackageInfoDTO> searchMerchantList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        scs = new Vector<>();
        SortingContext s = new SortingContext("um.id", "desc");
        scs.add(s);
        params = getUnionParams(params, scs, pc);
        return creditWebDAO.searchMerchantList(params);
    }

    @Override
    public int searchSettlementInfoCount(Map<String, Object> params) {
        params.put("clearType", StaticDataEnum.CLEAR_TYPE_2.getCode());
        return creditWebDAO.searchSettlementInfoCount(params);
    }

    @Override
    public List<SettlementInfoDTO> searchSettlementInfoList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        scs = new Vector<>();
        SortingContext s = new SortingContext("ucd.id", "desc");
        scs.add(s);
        params = getUnionParams(params, scs, pc);
        params.put("clearType", StaticDataEnum.CLEAR_TYPE_2.getCode());
        return creditWebDAO.searchSettlementInfo(params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void failedClearDetailInfo(Long flowId, HttpServletRequest request) throws BizException {

        Long now = System.currentTimeMillis();

        // 修改交易流水
        ClearDetailDTO clearDetailDTO = new ClearDetailDTO();
        clearDetailDTO.setState(StaticDataEnum.CLEAR_BATCH_STATE_2.getCode());
        clearDetailService.updateClearDetail(flowId, clearDetailDTO, request);

        ClearDetailDTO clearDetailDTO1 = clearDetailService.findClearDetailById(flowId);

        Map<String, Object> params = new HashMap<>(6);
        params.clear();
        params.put("modifiedDate",now);
        params.put("clearBatchId",clearDetailDTO1.getClearBatchId());
        params.put("recUserId", clearDetailDTO1.getUserId());
        clearFlowDetailService.updateClearBatchToFail(params);


        params.clear();
        params.put("batchId", clearDetailDTO1.getClearBatchId());
        params.put("settlementState", StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        params.put("userId", clearDetailDTO1.getUserId());
        params.put("modifiedDate",now);
        wholeSalesFlowService.updateClearBatchToFail(params);


    }

    @Override
    public List<MerchantWholeSalesFlowInfoDTO> searchMerchantWholeSalesFlowInfo(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        scs = new Vector<>();
        SortingContext s = new SortingContext("uwsf.id", "desc");
        scs.add(s);
        params = getUnionParams(params, scs, pc);
        return creditWebDAO.searchMerchantWholeSalesFlowInfo(params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClearBatchDTO export(List<Long> ids, HttpServletRequest request) throws BizException {


        if (ids.size() > 0) {

            ClearBatchDTO clearBatchDTO = this.saveClearBatchInfo(ids, request);

            Long now = System.currentTimeMillis();

            StringBuilder stringBuilder = new StringBuilder();

            ids.forEach(a -> {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(",");
                }
                stringBuilder.append("'").append(a).append("'");
            });

            Map<String, Object> params = new HashMap<>(8);
            params.put("settlementTime", now);
            params.put("settlementState", StaticDataEnum.CLEAR_STATE_TYPE_1.getCode());
            params.put("modifiedDate", now);
            params.put("ids", stringBuilder.toString());
            params.put("batchId",clearBatchDTO.getId());
            creditWebDAO.updateWholeSalesFlow(params);

            return clearBatchDTO;
        }

        throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
    }

    @Override
    public int searchMerchantWholeSalesFlowInfoCount(Map<String, Object> params) {
        return creditWebDAO.searchMerchantWholeSalesFlowInfoCount(params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSettlementDelay(Integer delay, List<String> ids) throws BizException {
        Map<String, Object > params = new HashMap<>(6);

        StringBuilder strings = new StringBuilder();
        ids.forEach(a -> {
            if (strings.length() > 0) {
                strings.append(",");
            }
            strings.append("'").append(a).append("'");
        });

        params.put("settlementDelay", delay);
        params.put("modifiedDate", System.currentTimeMillis());
        params.put("ids", strings.toString());

        int cnt = creditWebDAO.updateSettlementDelay(params);
        if (cnt != ids.size()) {
            throw new BizException("update u_whole_sales_flow Error!");
        }
    }

    @Override
    public ClearBatchDTO exportNew(Map<String, Object> params, HttpServletRequest request) throws Exception {
        //创建清算批次
        ClearBatchDTO clearBatchDTO = new ClearBatchDTO();
        clearBatchDTO.setClearType(StaticDataEnum.CLEAR_TYPE_2.getCode());
        if( params.get("start")!=null && StringUtils.isNotEmpty(params.get("start").toString()) ){
            clearBatchDTO.setClearEndDate(Long.valueOf(params.get("start").toString()));
        }else{
            params.remove("start");
        }
        if(params.get("end")!=null &&StringUtils.isNotEmpty(params.get("end").toString())){
            clearBatchDTO.setClearStartDate(Long.valueOf(params.get("end").toString()));
        }else{
            params.remove("end");
        }
        if(params.get("merchantIdList")!=null  ){
            String [] merchantIdList = params.get("merchantIdList").toString().split(",",-1);;
            if(merchantIdList.length < 1){
                params.remove("merchantIdList");
            }else{
                params.put("merchantIdList",merchantIdList);
            }
        }
        //查询清算数据
        int total = wholeSalesFlowService.countMerchantClearList(params);
        if (total == 0) {
            //无清算数据
            log.info("settleFileExport 无清算数据");
            return null;
        }

        try {
            //记录批次流水
            clearBatchDTO.setCreatedBy( Long.valueOf(params.get("optionUserId").toString() ));
            clearBatchDTO.setModifiedBy( Long.valueOf(params.get("optionUserId").toString() ));
            clearBatchDTO.setId(clearBatchService.saveClearBatch(clearBatchDTO, request));

            //原交易流水打批次号,更新状态
            wholeSalesFlowService.addClearBatchId(params, clearBatchDTO, request);
            //清算明细入清算流水明细表
            List<ClearFlowDetail> list = wholeSalesFlowService.getDataByBatchId(clearBatchDTO.getId());
            this.addClearFlowDetail(list,clearBatchDTO,request);
            //生成清算流水信息
            qrPayFlowService.updateQrPayClearBatch(params, clearBatchDTO, request);
            // 清算
            clearBatchDTO = this.clear(clearBatchDTO.getId(), request);
        } catch (Exception e) {
            log.info("exportNew 整体出售商户结算处理异常",e.getMessage(),e);
            if(clearBatchDTO.getId()!=null){
                clearBatchDTO.setState(StaticDataEnum.TRANS_STATE_2.getCode());
                clearBatchService.updateClearBatch(clearBatchDTO.getId(), clearBatchDTO, request);
            }
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }

        return clearBatchDTO;
    }

    @Override
    public List<Map<String, Object>> merchantClearMessageList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        // 查询商户信息
        List<MerchantDTO> merchantList = merchantService.getWholeSaleClearMerchantList(params);
        List<Long> merchantIdList = new ArrayList<>();

        JSONArray requestArray = new JSONArray();
        // 处理账户余额查询数据和商户查询数据
        for(MerchantDTO merchantDTO:merchantList){
            merchantIdList.add(merchantDTO.getId());
            JSONObject oneData = new JSONObject();
            oneData.put("userId",merchantDTO.getUserId());
            requestArray.add(oneData);
        }
        // 账户余额查询
        JSONArray resultData = new JSONArray();
        try {
            JSONObject requestData = new JSONObject();
            requestData.put("list",requestArray);
            requestData.put("type",1);
            resultData =  serverService.getSubAccountBalanceList(requestData);
        }catch (Exception e){

            log.error("整体出售余额查询失败 merchantClearMessageList ，exception："+e.getMessage(),e);
            return  null ;
        }

        Map<Long,BigDecimal> balanceMap = new HashMap<>();
        for(int i = 0; i<resultData.size() ;i++){
            JSONObject oneData = resultData.getJSONObject(i);
            balanceMap.put(oneData.getLong("userId"),oneData.getBigDecimal("balance"));
        }


        params.put("merchantIdList",merchantIdList);
        // 查询商户的所有订单信息
        List<WholeSalesFlowDTO> wholeSalesFlowDTOS = wholeSalesFlowService.merchantClearMessageList(params);
        List<Map<String ,Object>> resultList = new ArrayList<>();
        // 处理数据
        for(MerchantDTO merchantDTO : merchantList){
            // 一条数据信息
            Map<String ,Object> oneResult = new HashMap<>(16);
            // 商户信息
            oneResult.put("merchantId",merchantDTO.getId().toString());
            oneResult.put("merchantName",merchantDTO.getPracticalName());
            oneResult.put("merchantState",merchantDTO.getState());
            oneResult.put("abn",merchantDTO.getAbn());
            oneResult.put("city",merchantDTO.getCity());
            oneResult.put("balance",balanceMap.get(merchantDTO.getUserId()) == null ? BigDecimal.ZERO : balanceMap.get(merchantDTO.getUserId()));
            if(wholeSalesFlowDTOS != null && wholeSalesFlowDTOS.size() > 0){
                // 筛选商户所有订单
                List<WholeSalesFlowDTO> merchantData = wholeSalesFlowDTOS.stream().filter(flow->flow.getMerchantId().equals(merchantDTO.getId())).collect(Collectors.toList());
                if(merchantData != null && merchantData.size() > 0){
                    oneResult.put("totalNumber",merchantData.size());
                    BigDecimal totalAmount = merchantData.stream().map(WholeSalesFlowDTO::getSettlementAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                    oneResult.put("totalAmount",totalAmount);
                    // 筛选未结算订单
                    List<WholeSalesFlowDTO> unclearedData = merchantData.stream().filter(flow->flow.getSettlementState() == StaticDataEnum.CLEAR_STATE_TYPE_0.getCode() && flow.getSettlementDelay() == StaticDataEnum.STATUS_0.getCode()).collect(Collectors.toList());
                    if(unclearedData != null && unclearedData.size() >0){
                        oneResult.put("unclearedNumber",unclearedData.size());
                        BigDecimal unclearedAmount = unclearedData.stream().map(WholeSalesFlowDTO::getSettlementAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                        oneResult.put("unclearedAmount",unclearedAmount);
                    }else{
                        oneResult.put("unclearedNumber",0);
                        oneResult.put("unclearedAmount",BigDecimal.ZERO);
                    }
                    // 筛选已结算订单
                    List<WholeSalesFlowDTO> clearedData = merchantData.stream().filter(flow->flow.getSettlementState() == StaticDataEnum.CLEAR_STATE_TYPE_1.getCode()).collect(Collectors.toList());
                    if(clearedData != null && clearedData.size() >0){
                        oneResult.put("clearedNumber",clearedData.size());
                        BigDecimal clearedAmount = clearedData.stream().map(WholeSalesFlowDTO::getSettlementAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                        oneResult.put("clearedAmount",clearedAmount);
                    }else{
                        oneResult.put("clearedNumber",0);
                        oneResult.put("clearedAmount",BigDecimal.ZERO);
                    }
                    // 筛选延迟结算订单
                    List<WholeSalesFlowDTO> delayClearData = merchantData.stream().filter(flow->flow.getSettlementState() == StaticDataEnum.CLEAR_STATE_TYPE_0.getCode() && flow.getSettlementDelay() == StaticDataEnum.STATUS_1.getCode()).collect(Collectors.toList());
                    if(delayClearData != null && delayClearData.size() >0){
                        oneResult.put("delayClearNumber",delayClearData.size());
                        BigDecimal delayClearAmount = delayClearData.stream().map(WholeSalesFlowDTO::getSettlementAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
                        oneResult.put("delayClearAmount",delayClearAmount);
                    }else{
                        oneResult.put("delayClearNumber",0);
                        oneResult.put("delayClearAmount",BigDecimal.ZERO);
                    }
                }else{
                    oneResult.put("totalNumber",0);
                    oneResult.put("totalAmount",BigDecimal.ZERO);
                    oneResult.put("unclearedNumber",0);
                    oneResult.put("unclearedAmount",BigDecimal.ZERO);
                    oneResult.put("clearedNumber",0);
                    oneResult.put("clearedAmount",BigDecimal.ZERO);
                    oneResult.put("delayClearNumber",0);
                    oneResult.put("delayClearAmount",BigDecimal.ZERO);
                }
            }else{
                oneResult.put("totalNumber",0);
                oneResult.put("totalAmount",BigDecimal.ZERO);
                oneResult.put("unclearedNumber",0);
                oneResult.put("unclearedAmount",BigDecimal.ZERO);
                oneResult.put("clearedNumber",0);
                oneResult.put("clearedAmount",BigDecimal.ZERO);
                oneResult.put("delayClearNumber",0);
                oneResult.put("delayClearAmount",BigDecimal.ZERO);
            }
            resultList.add(oneResult);
        }

        return resultList;
    }

    @Override
    public List<WholeSalesFlowDTO> wholeSaleListDetails(Map<String, Object> params) {
        return wholeSalesFlowService.merchantClearMessageList(params);
    }

    private void addClearFlowDetail(List<ClearFlowDetail> list, ClearBatchDTO clearBatchDTO, HttpServletRequest request) throws BizException {
        Long now = System.currentTimeMillis();
        for(ClearFlowDetail clearFlowDetail:list){
            clearFlowDetail.setId(SnowflakeUtil.generateId());
            Long currentLoginId = getUserId(request);
            clearFlowDetail.setCreatedBy(clearBatchDTO.getCreatedBy());
            clearFlowDetail.setCreatedDate(now);
            clearFlowDetail.setModifiedBy(clearBatchDTO.getModifiedBy());
            clearFlowDetail.setModifiedDate(now);
            clearFlowDetail.setIp(getIp(request));
            clearFlowDetail.setStatus(1);
            clearFlowDetail.setClearAmount(clearFlowDetail.getTransAmount());
            clearFlowDetail.setState(StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
            clearFlowDetail.setBorrowAmount(clearFlowDetail.getBorrowAmount());
            clearFlowDetail.setTransAmount(clearFlowDetail.getTransAmount());
        }
        clearFlowDetailService.saveClearFlowDetailList(list,request);
    }

    private ClearBatchDTO clear(Long id, HttpServletRequest request) throws BizException {
        ClearBatchDTO clearBatchDTO = clearBatchService.findClearBatchById(id);

        // 处理结算结果
        clearDetailService.dealWholeSaleClear(id);
        // 更新清算流水记录
        clearFlowDetailService.dealWholeSaleClear(id);
        // 更新原订单清算信息
        wholeSalesFlowService.dealWholeSaleClear(id);
        //查询清算成功的总比数总金额
        Map<String,Object> clearMap = new HashMap<>();
        clearMap.put("clearBatchId",id);
        ClearFlowDetailDTO clearFlowDetailDTO = clearFlowDetailService.clearTotal(clearMap);
        clearBatchDTO.setTotalNumber(clearFlowDetailDTO.getCount().longValue());

        ClearDetailDTO  clearDetailDTO = clearDetailService.clearTotal(clearMap);
        clearBatchDTO.setTotalAmount(clearDetailDTO == null ? BigDecimal.ZERO : clearDetailDTO.getTransAmount());
        clearBatchDTO.setBorrowAmount(clearDetailDTO == null ? BigDecimal.ZERO : clearDetailDTO.getBorrowAmount());
        clearBatchDTO.setClearAmount(clearDetailDTO == null ? BigDecimal.ZERO : clearDetailDTO.getClearAmount());


        //LatPay商户清算

        //TODO 文明名未定
        String fileName = "Bill_"+id+".csv";
        log.info("开始生成平台清算Bill文件，文件名："+fileName);
        //记录清算状态
        clearBatchDTO.setFileName(fileName);

        clearBatchDTO.setUrl(filePath+fileName);
        clearBatchService.updateClearBatch(id,clearBatchDTO,request);
        //生成文件
        List<ClearBillCSVDTO> billList = clearDetailService.getClearBillList(id);
        clearBatchService.createClearBillFile(billList,fileName,filePath);

        //更新批次状态
        clearBatchDTO.setState(StaticDataEnum.TRANS_STATE_1.getCode());
        clearBatchService.updateClearBatch(id,clearBatchDTO,request);
        return clearBatchDTO;


    }



    public ClearBatchDTO saveClearBatchInfo(List<Long> ids, HttpServletRequest request) throws BizException {
        List<DiscountPackageInfoDTO> list = new ArrayList<>(ids.size());

        Map<String, Object> params = new HashMap<>(4);
        for (Long id : ids) {
            params.put("merchantId", id);
            list.addAll(creditWebDAO.searchMerchantList(params));
        }

        // 去重
        list = list.stream().collect(
                collectingAndThen(
                        toCollection(() -> new TreeSet<>(Comparator.comparing(DiscountPackageInfoDTO::getId))), ArrayList::new)
        );

        if (!list.isEmpty()) {
            // 清算总金额
            BigDecimal clearAmount = BigDecimal.ZERO;
            for (DiscountPackageInfoDTO discountPackageInfoDTO : list) {
                clearAmount = clearAmount.add(discountPackageInfoDTO.getUndelayed().getAmount());
            }

            // 清算批次
            ClearBatchDTO clearBatchDTO = new ClearBatchDTO();
            clearBatchDTO.setTotalNumber((long) list.size());
            clearBatchDTO.setTotalAmount(clearAmount);
            clearBatchDTO.setBorrowAmount(clearAmount);
            clearBatchDTO.setClearAmount(clearAmount);
            clearBatchDTO.setClearType(StaticDataEnum.CLEAR_TYPE_2.getCode());
            clearBatchDTO.setState(StaticDataEnum.CLEAR_BATCH_STATE_1.getCode());

            Long batchId = clearBatchService.saveClearBatch(clearBatchDTO, request);

            this.saveClearDetailInfo(list, batchId, request);

            //TODO 文明名未定
            String fileName = "Bill_" + batchId + ".csv";
            log.info("开始生成平台清算Bill文件，文件名：" + fileName);
            //记录清算状态
            clearBatchDTO.setFileName(fileName);
            clearBatchDTO.setUrl(filePath + fileName);
            clearBatchService.updateClearBatch(batchId, clearBatchDTO, request);
            //生成文件
            List<ClearBillCSVDTO> billList = clearDetailService.getClearBillList(batchId);
            clearBatchService.createClearBillFile(billList, fileName, filePath);

            return clearBatchDTO;

        }

        throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveClearDetailInfo(List<DiscountPackageInfoDTO> list, Long batchId, HttpServletRequest request) throws BizException {

        List<ClearDetail> clearDetails = new ArrayList<>(list.size());

        for (DiscountPackageInfoDTO discountPackageInfoDTO : list) {

            ClearDetail clearDetail = new ClearDetail();
            clearDetail.setClearBatchId(batchId);
            clearDetail.setUserId(discountPackageInfoDTO.getUserId());
            clearDetail.setMerchantId(discountPackageInfoDTO.getId());
            clearDetail.setClearNumber((long) discountPackageInfoDTO.getUndelayed().getNumber());
            clearDetail.setClearAmount(discountPackageInfoDTO.getUndelayed().getAmount());
            clearDetail.setTransAmount(discountPackageInfoDTO.getUndelayed().getAmount());
            clearDetail.setBsb(discountPackageInfoDTO.getBsb());
            clearDetail.setBorrowAmount(discountPackageInfoDTO.getUndelayed().getAmount());
            clearDetail.setAccountNo(discountPackageInfoDTO.getAccountNo());
            clearDetail.setBankName(discountPackageInfoDTO.getBankName());
            clearDetail.setState(StaticDataEnum.CLEAR_BATCH_STATE_1.getCode());

            clearDetail = (ClearDetail) super.packAddBaseProps(clearDetail, request);

            clearDetails.add(clearDetail);
        }

        if (!clearDetails.isEmpty()) {
            clearDetailService.saveClearDetailList(clearDetails, request);

            this.saveClearFlowDetailInfo(clearDetails, request);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveClearFlowDetailInfo(List<ClearDetail> clearDetails, HttpServletRequest request) throws BizException {

        List<ClearFlowDetail> clearFlowDetails = new ArrayList<>();
        List<WholeSalesFlowAndClearDetail> wholeSalesFlowAndClearDetails = new ArrayList<>();

        Map<String, Object> params = new HashMap<>(4);

        params.put("settlementState", StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        params.put("settlementDelay", StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());

        for (ClearDetail clearDetail : clearDetails) {

            params.put("merchantId", clearDetail.getMerchantId());

            List<WholeSalesFlowDTO> wholeSalesFlowDTOS = wholeSalesFlowService.find(params, null, null);

            for (WholeSalesFlowDTO wholeSalesFlowDTO : wholeSalesFlowDTOS) {

                ClearFlowDetail clearFlowDetail = new ClearFlowDetail();

                clearFlowDetail.setClearBatchId(clearDetail.getClearBatchId());
                clearFlowDetail.setFlowId(wholeSalesFlowDTO.getId());
                clearFlowDetail.setRecUserId(clearDetail.getUserId());
                clearFlowDetail.setClearAmount(wholeSalesFlowDTO.getSettlementAmount());
                clearFlowDetail.setTransAmount(wholeSalesFlowDTO.getSettlementAmount());
                clearFlowDetail.setBorrowAmount(wholeSalesFlowDTO.getAmount());
                clearFlowDetail.setState(StaticDataEnum.CLEAR_BATCH_STATE_1.getCode());
                clearFlowDetail.setTransType(wholeSalesFlowDTO.getTransType());

                clearFlowDetail = (ClearFlowDetail) super.packAddBaseProps(clearFlowDetail, request);

                clearFlowDetails.add(clearFlowDetail);

                WholeSalesFlowAndClearDetail wholeSalesFlowAndClearDetail = new WholeSalesFlowAndClearDetail();
                wholeSalesFlowAndClearDetail.setWholeSalesFlowId(wholeSalesFlowDTO.getId());
                wholeSalesFlowAndClearDetail.setClearBatchId(clearDetail.getClearBatchId());
                wholeSalesFlowAndClearDetail.setClearDetailId(clearDetail.getId());
                wholeSalesFlowAndClearDetail.setClearFlowDetailId(clearFlowDetail.getId());

                wholeSalesFlowAndClearDetail = (WholeSalesFlowAndClearDetail) super.packAddBaseProps(wholeSalesFlowAndClearDetail, request);

                wholeSalesFlowAndClearDetails.add(wholeSalesFlowAndClearDetail);
            }

        }

        if (clearDetails.size() > 0) {
            clearFlowDetailService.saveClearFlowDetailList(clearFlowDetails, request);

            wholeSalesFlowAndClearDetailService.saveWholeSalesFlowAndClearDetailList(wholeSalesFlowAndClearDetails, request);
        }
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }
}
