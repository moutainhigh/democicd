package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.RefundOrderDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.RefundOrder;
import com.uwallet.pay.main.service.QrPayFlowService;
import com.uwallet.pay.main.service.RefundOrderService;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.service.ServerService;
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
import java.util.*;

/**
 * <p>
 * 退款订单
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 退款订单
 * @author: zhoutt
 * @date: Created in 2021-08-18 09:01:47
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class RefundOrderServiceImpl extends BaseServiceImpl implements RefundOrderService {

    @Autowired
    private RefundOrderDAO refundOrderDAO;
    @Autowired
    private QrPayFlowService qrPayFlowService;
    @Autowired
    private ServerService serverService;

    @Override
    public Long saveRefundOrder(@NonNull RefundOrderDTO refundOrderDTO, HttpServletRequest request) throws BizException {
        RefundOrder refundOrder = BeanUtil.copyProperties(refundOrderDTO, new RefundOrder());
        log.info("save RefundOrder:{}", refundOrder);
        refundOrder = (RefundOrder) this.packAddBaseProps(refundOrder, request);
        if (refundOrderDAO.insert(refundOrder) != 1) {
            log.error("insert error, data:{}", refundOrder);
            throw new BizException("Insert refundOrder Error!");
        }
        return refundOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRefundOrderList(@NonNull List<RefundOrder> refundOrderList, HttpServletRequest request) throws BizException {
        if (refundOrderList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = refundOrderDAO.insertList(refundOrderList);
        if (rows != refundOrderList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, refundOrderList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateRefundOrder(@NonNull Long id, @NonNull RefundOrderDTO refundOrderDTO, HttpServletRequest request) throws BizException {
        log.info("full update refundOrderDTO:{}", refundOrderDTO);
        RefundOrder refundOrder = BeanUtil.copyProperties(refundOrderDTO, new RefundOrder());
        refundOrder.setId(id);
        if(request == null){
            refundOrder.setModifiedDate(System.currentTimeMillis());
        }else{
            refundOrder = (RefundOrder) this.packModifyBaseProps(refundOrder, request);
        }
        int cnt = refundOrderDAO.update(refundOrder);
        if (cnt != 1) {
            log.error("update error, data:{}", refundOrderDTO);
            throw new BizException("update refundOrder Error!");
        }
    }

    @Override
    public void updateRefundOrderSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        refundOrderDAO.updatex(params);
    }

    @Override
    public void logicDeleteRefundOrder(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = refundOrderDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteRefundOrder(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = refundOrderDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public RefundOrderDTO findRefundOrderById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        RefundOrderDTO refundOrderDTO = refundOrderDAO.selectOneDTO(params);
        return refundOrderDTO;
    }

    @Override
    public RefundOrderDTO findOneRefundOrder(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        RefundOrder refundOrder = refundOrderDAO.selectOne(params);
        RefundOrderDTO refundOrderDTO = new RefundOrderDTO();
        if (null != refundOrder) {
            BeanUtils.copyProperties(refundOrder, refundOrderDTO);
        }
        return refundOrderDTO;
    }

    @Override
    public List<RefundOrderDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<RefundOrderDTO> resultList = refundOrderDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return refundOrderDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return refundOrderDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = refundOrderDAO.groupCount(conditions);
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
        return refundOrderDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = refundOrderDAO.groupSum(conditions);
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
    @Transactional(rollbackFor = Exception.class)
    public RefundOrderDTO createH5RefundOrderDTO(H5RefundsRequestDTO requestInfo, ApiQrPayFlowDTO apiQrPayFlowDTO, QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) throws Exception {
        // 修改原订单退款状态
        QrPayFlowDTO updateData = new QrPayFlowDTO();
        updateData.setRefundState(StaticDataEnum.REFUND_STATE_1.getCode());
        qrPayFlowService.updateQrPayFlow(qrPayFlowDTO.getId(),updateData,request);

        // 创建退款订单
        RefundOrderDTO refundOrderDTO = new RefundOrderDTO();
        refundOrderDTO.setCurrency(requestInfo.getAmount().getCurrency().toUpperCase());
        refundOrderDTO.setMerchantId(apiQrPayFlowDTO.getApiMerchantId());
        refundOrderDTO.setSuperMerchantId(apiQrPayFlowDTO.getSuperMerchantId());
        refundOrderDTO.setOrderSource(StaticDataEnum.ORDER_SOURCE_1.getCode());
        // 记录金额单位为元
        refundOrderDTO.setRefundAmount(BigDecimal.valueOf(requestInfo.getAmount().getAmount()).divide(new BigDecimal("100")));
        refundOrderDTO.setReason(requestInfo.getReason());
        refundOrderDTO.setOrgPayUserId(qrPayFlowDTO.getPayUserId());
        refundOrderDTO.setTransNo(qrPayFlowDTO.getTransNo());
        if(qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_22.getCode()){
            refundOrderDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_36.getCode());
        }else if(qrPayFlowDTO.getTransType() == StaticDataEnum.ACC_FLOW_TRANS_TYPE_2.getCode()){
            refundOrderDTO.setTransType(StaticDataEnum.ACC_FLOW_TRANS_TYPE_12.getCode());
        }
        refundOrderDTO.setReference(requestInfo.getMerchant().getReference());
        refundOrderDTO.setIdempotencyKey(requestInfo.getIdKey());
        refundOrderDTO.setState(StaticDataEnum.TRANS_STATE_0.getCode());
        refundOrderDTO.setId(this.saveRefundOrder(refundOrderDTO,request));

        return refundOrderDTO;
    }

    @Override
    public List<RefundOrderDTO> findCreditRefundDoubt() {

        return refundOrderDAO.findCreditRefundDoubt();
    }

    @Override
    public int getH5MerchantRefundUnclearedCount(Map<String, Object> params) {
        return refundOrderDAO.getH5MerchantRefundUnclearedCount(params);
    }

    @Override
    public void addClearBatchId(Map<String, Object> params, HttpServletRequest request) {
        if(request != null){
            params.put("modifiedBy",getUserId(request));
        }
        params.put("modifiedDate",System.currentTimeMillis());
        refundOrderDAO.addClearBatchId(params);
    }

    @Override
    public void clearData(Map<String, Object> map) {
        refundOrderDAO.clearData(map);
    }

    @Override
    public List<RefundOrderDTO> merchantClearMessageList(Map<String, Object> params) {
        return refundOrderDAO.merchantClearMsgList(params);
    }

    @Override
    public int rollbackSettlement(Long batchId, Long merchantId, HttpServletRequest request) {
        Map<String,Object> params = new HashMap<>();
        Long now = System.currentTimeMillis();
        params.put("now",now);
        params.put("batchId",batchId);
        params.put("merchantId",merchantId);
        params.put("settlementState",StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        if(request != null){
            params.put("ip",getIp(request));
            params.put("modifiedBy",getUserId(request));
        }

        return refundOrderDAO.rollbackSettlement(params);
    }

    @Override
    public int getH5RefundsListCount(Map<String, Object> params) {
        params.put("orderSource",StaticDataEnum.ORDER_SOURCE_1.getCode());
        return refundOrderDAO.getRefundsListCount(params);
    }

    @Override
    public List<RefundOrderListDTO> getH5RefundsList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params,scs,pc);
        params.put("orderSource",StaticDataEnum.ORDER_SOURCE_1.getCode());
        List<RefundOrderListDTO>  list = refundOrderDAO.getRefundsList(params);

        Map<String ,Object> creditParams = new HashMap<>();
        Set<Long> borrowIdSet = new HashSet<>();
        StringBuffer ids = new StringBuffer();
        for(RefundOrderListDTO refundOrderListDTO :list){
            ids.append(refundOrderListDTO.getBorrowId());
            borrowIdSet.add(refundOrderListDTO.getBorrowId());
        }
        creditParams.put("ids",ids.toString().substring(0, ids.toString().length() - 1) );


        List<Long> borrowIdList = new ArrayList<>(borrowIdSet);
        JSONArray borrowList = new JSONArray();
        try {
            borrowList = serverService.getBorrowList(borrowIdList, 0);
        } catch (Exception e) {
            log.error("get borrow info error, params: {}, error: {}", borrowIdList, e.getMessage());
        }

        for(RefundOrderListDTO refundOrderListDTO : list){
            for (Object datum : borrowList) {
                JSONObject obj = (JSONObject) datum;
                if (refundOrderListDTO.getBorrowId() != null && refundOrderListDTO.getBorrowId().equals(obj.getLong("id"))) {
                    // 分期笔数
                    refundOrderListDTO.setPeriod(obj.getInteger("periodQuantity"));
                    // 还款状态
                    refundOrderListDTO.setBorrowState(obj.getInteger("state"));
                    int paidPeriodQuantity = 0;
                    BigDecimal paidAmount = BigDecimal.ZERO;
                    JSONArray repayList = obj.getJSONArray("repayList");
                    for (Object o : repayList) {
                        JSONObject item = (JSONObject) o;
                        paidAmount = paidAmount.add(item.getBigDecimal("truelyRepayAmount"));
                        if (item.getInteger("state") == 1) {
                            paidPeriodQuantity += 1;
                        }
                    }
                    // 已还款期数
                    refundOrderListDTO.setPaidPeriod(paidPeriodQuantity);
                    // 已还款金额
                    refundOrderListDTO.setPaidAmount(paidAmount);
                    // 还款计划明细
                    refundOrderListDTO.setRepayPlan(obj.getJSONArray("repayList"));
                    break;
                }
            }
        }

        return list;
    }

}


























