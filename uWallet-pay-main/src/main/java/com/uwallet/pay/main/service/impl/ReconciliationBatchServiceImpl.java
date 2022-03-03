package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.ReconciliationBatchDAO;
import com.uwallet.pay.main.model.dto.ReconciliationBatchDTO;
import com.uwallet.pay.main.model.entity.ReconciliationBatch;
import com.uwallet.pay.main.service.ReconciliationBatchService;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 对账表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 对账表
 * @author: aaronS
 * @date: Created in 2021-01-25 16:11:20
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: aaronS
 */
@Service
@Slf4j
public class ReconciliationBatchServiceImpl extends BaseServiceImpl implements ReconciliationBatchService {

    @Autowired
    private ReconciliationBatchDAO reconciliationBatchDAO;

    @Override
    public Long saveReconciliationBatch(@NonNull ReconciliationBatchDTO reconciliationBatchDTO, HttpServletRequest request) throws BizException {
        ReconciliationBatch reconciliationBatch = BeanUtil.copyProperties(reconciliationBatchDTO, new ReconciliationBatch());
        log.info("save ReconciliationBatch:{}", reconciliationBatch);
        ReconciliationBatch dto = (ReconciliationBatch) this.packAddBaseProps(reconciliationBatch, request);
        if (reconciliationBatchDAO.insert(dto) != 1) {
            log.error("insert error, data:{}", reconciliationBatch);
            throw new BizException("Insert reconciliationBatch Error!");
        }
        return dto.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveReconciliationBatchList(@NonNull List<ReconciliationBatch> reconciliationBatchList, HttpServletRequest request) throws BizException {
        if (reconciliationBatchList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = reconciliationBatchDAO.insertList(reconciliationBatchList);
        if (rows != reconciliationBatchList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, reconciliationBatchList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateReconciliationBatch(@NonNull Long id, @NonNull ReconciliationBatchDTO reconciliationBatchDTO, HttpServletRequest request) throws BizException {
        log.info("full update reconciliationBatchDTO:{}", reconciliationBatchDTO);
        ReconciliationBatch reconciliationBatch = BeanUtil.copyProperties(reconciliationBatchDTO, new ReconciliationBatch());
        reconciliationBatch.setId(id);
        int cnt = reconciliationBatchDAO.update((ReconciliationBatch) this.packModifyBaseProps(reconciliationBatch, request));
        if (cnt != 1) {
            log.error("update error, data:{}", reconciliationBatchDTO);
            throw new BizException("update reconciliationBatch Error!");
        }
    }

    @Override
    public void updateReconciliationBatchSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        reconciliationBatchDAO.updatex(params);
    }

    @Override
    public void logicDeleteReconciliationBatch(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = reconciliationBatchDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteReconciliationBatch(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = reconciliationBatchDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ReconciliationBatchDTO findReconciliationBatchById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ReconciliationBatchDTO reconciliationBatchDTO = reconciliationBatchDAO.selectOneDTO(params);
        return reconciliationBatchDTO;
    }

    @Override
    public ReconciliationBatchDTO findOneReconciliationBatch(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        ReconciliationBatch reconciliationBatch = reconciliationBatchDAO.selectOne(params);
        ReconciliationBatchDTO reconciliationBatchDTO = new ReconciliationBatchDTO();
        if (null != reconciliationBatch) {
            BeanUtils.copyProperties(reconciliationBatch, reconciliationBatchDTO);
        }
        return reconciliationBatchDTO;
    }

    @Override
    public List<ReconciliationBatchDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ReconciliationBatchDTO> resultList = reconciliationBatchDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return reconciliationBatchDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return reconciliationBatchDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = reconciliationBatchDAO.groupCount(conditions);
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
        return reconciliationBatchDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = reconciliationBatchDAO.groupSum(conditions);
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

}
