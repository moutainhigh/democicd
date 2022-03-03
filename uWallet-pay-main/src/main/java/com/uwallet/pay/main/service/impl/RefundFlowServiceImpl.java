package com.uwallet.pay.main.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.RefundFlowDAO;
import com.uwallet.pay.main.model.dto.ClearBillCSVDTO;
import com.uwallet.pay.main.model.dto.RefundDetailListDTO;
import com.uwallet.pay.main.model.dto.RefundFlowDTO;
import com.uwallet.pay.main.model.dto.RefundListDTO;
import com.uwallet.pay.main.model.entity.RefundFlow;
import com.uwallet.pay.main.service.CSVService;
import com.uwallet.pay.main.service.RefundFlowService;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 退款流水表
 * </p>
 *
 * @package: com.uwallet.pay.main.main.service.impl
 * @description: 退款流水表
 * @author: baixinyue
 * @date: Created in 2020-02-07 15:56:50
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class RefundFlowServiceImpl extends BaseServiceImpl implements RefundFlowService {

    @Autowired
    private RefundFlowDAO refundFlowDAO;

    @Autowired
    private CSVService csvService;

    @Value("${spring.clearFilePath}")
    private String filePath;

    @Override
    public Long saveRefundFlow(@NonNull RefundFlowDTO refundFlowDTO, HttpServletRequest request) throws BizException {
        RefundFlow refundFlow = BeanUtil.copyProperties(refundFlowDTO, new RefundFlow());
        refundFlow = (RefundFlow) this.packAddBaseProps(refundFlow, request);
        log.info("save RefundFlow:{}", refundFlow);
        if (refundFlowDAO.insert(refundFlow) != 1) {
            log.error("insert error, data:{}", refundFlow);
            throw new BizException("Insert refundFlow Error!");
        }
        return refundFlow.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRefundFlowList(@NonNull List<RefundFlow> refundFlowList, HttpServletRequest request) throws BizException {
        if (refundFlowList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = refundFlowDAO.insertList(refundFlowList);
        if (rows != refundFlowList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, refundFlowList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateRefundFlow(@NonNull Long id, @NonNull RefundFlowDTO refundFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full update refundFlowDTO:{}", refundFlowDTO);
        RefundFlow refundFlow = BeanUtil.copyProperties(refundFlowDTO, new RefundFlow());
        refundFlow.setId(id);
        if (request != null) {
            refundFlow = (RefundFlow) this.packModifyBaseProps(refundFlow, request);
        } else {
            refundFlow.setModifiedDate(System.currentTimeMillis());
        }
        int cnt = refundFlowDAO.update(refundFlow);
        if (cnt != 1) {
            log.error("update error, data:{}", refundFlowDTO);
            throw new BizException("update refundFlow Error!");
        }
    }

    @Override
    public void updateRefundFlowSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        refundFlowDAO.updatex(params);
    }

    @Override
    public void logicDeleteRefundFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = refundFlowDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteRefundFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = refundFlowDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public RefundFlowDTO findRefundFlowById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        RefundFlowDTO refundFlowDTO = refundFlowDAO.selectOneDTO(params);
        return refundFlowDTO;
    }

    @Override
    public RefundFlowDTO findOneRefundFlow(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        RefundFlow refundFlow = refundFlowDAO.selectOne(params);
        RefundFlowDTO refundFlowDTO = new RefundFlowDTO();
        if (null != refundFlow) {
            BeanUtils.copyProperties(refundFlow, refundFlowDTO);
        }
        return refundFlowDTO;
    }

    @Override
    public List<RefundFlowDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<RefundFlowDTO> resultList = refundFlowDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return refundFlowDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return refundFlowDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = refundFlowDAO.groupCount(conditions);
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
        return refundFlowDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = refundFlowDAO.groupSum(conditions);
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
    public List<RefundFlowDTO> selectFlowDTO() {
        return refundFlowDAO.selectFlowDTO();
    }

    @Override
    public int addQrPayClearBatchId(Map<String, Object> updateMap) {
        return refundFlowDAO.addQrPayClearBatchId(updateMap);
    }

    @Override
    public int countClearList(Map<String, Object> params) {
        return refundFlowDAO.countClearList(params);
    }

    @Override
    public int clearData(Map<String, Object> map) {
        return refundFlowDAO.clearData(map);
    }

    @Override
    public int updateAmountOut(Map<String, Object> map) {
        return refundFlowDAO.updateAmountOut(map);
    }

    @Override
    public List<RefundFlowDTO> selectReason(Long flowId) {
        return refundFlowDAO.selectReason(flowId);
    }

    @Override
    public int updateRefundFlowToCheckFail(Long id, long updateTime) {
        return refundFlowDAO.updateRefundFlowToCheckFail(id ,updateTime);
    }

    @Override
    public RefundFlowDTO getUnCleared(Long userId,int gateWayId) {
        return refundFlowDAO.getUnCleared(userId,gateWayId);
    }

    @Override
    public List<RefundFlowDTO> findAllUnClearedRefundFlow(Map<String, Object> map) {
        return refundFlowDAO.findAllUnClearedRefundFlow(map);
    }

    @Override
    public List<RefundListDTO> selectRefund(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<RefundListDTO> resultList = refundFlowDAO.selectRefund(params);
        return resultList;
    }

    @Override
    public int selectRefundCount(Map<String, Object> params) {
        return refundFlowDAO.selectRefundCount(params);
    }

    @Override
    public List<RefundDetailListDTO> selectRefundDetail(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<RefundDetailListDTO> resultList = refundFlowDAO.selectRefundDetail(params);
        return resultList;
    }

    @Override
    public int selectRefundDetailCount(Map<String, Object> params) {
        return refundFlowDAO.selectRefundDetailCount(params);
    }

    @Override
    public synchronized String exportMakeUpFile(HttpServletRequest request) throws Exception {
        log.info("make up file export");
        //查询未补款的退款交易，将状态设置为正在补款
        refundFlowDAO.updateMakeUpState(StaticDataEnum.MAKE_UP_STATE_2.getCode());
        //查询补款信息生成补款csv文件
        Long id = SnowflakeUtil.generateId();
        List<ClearBillCSVDTO> list = refundFlowDAO.makeUpCSVData();
        csvService.createClearCsvFile("Bill_" + id + ".csv", filePath, list);
        refundFlowDAO.updateMakeUpState(StaticDataEnum.MAKE_UP_STATE_1.getCode());
        return filePath + "Bill_" + id + ".csv";
    }

    @Override
    public int updateClearBatch(Map<String, Object> map) {
        return refundFlowDAO.updateClearBatch(map);
    }

    @Override
    public JSONObject creditThirdRefundCheck(String refundNo, HttpServletRequest request) {
        Map<String,Object> params = new HashMap<>(4);

        params.put("refundNo",refundNo);
        RefundFlowDTO refundFlowDTO = this.findOneRefundFlow(params);
        JSONObject result = new JSONObject();

        if(refundFlowDTO == null || refundFlowDTO.getId() == null ){
            result.put("state",StaticDataEnum.TRANS_STATE_2.getCode());
        }else{
            result.put("state",refundFlowDTO.getState());
        }

        return result;
    }

}
