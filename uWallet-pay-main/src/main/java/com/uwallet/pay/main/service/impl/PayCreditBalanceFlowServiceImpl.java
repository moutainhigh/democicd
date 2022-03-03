package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.model.entity.BaseEntity;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.PayCreditBalanceFlowDAO;
import com.uwallet.pay.main.model.dto.PayCreditBalanceFlowDTO;
import com.uwallet.pay.main.model.entity.PayCreditBalanceFlow;
import com.uwallet.pay.main.service.PayCreditBalanceFlowService;
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
 * 
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 
 * @author: fenmi
 * @date: Created in 2021-07-07 10:38:54
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: fenmi
 */
@Service
@Slf4j
public class PayCreditBalanceFlowServiceImpl extends BaseServiceImpl implements PayCreditBalanceFlowService {

    @Autowired
    private PayCreditBalanceFlowDAO payCreditBalanceFlowDAO;

    @Override
    public Long savePayCreditBalanceFlow(@NonNull PayCreditBalanceFlowDTO payCreditBalanceFlowDTO, HttpServletRequest request) throws BizException {
        PayCreditBalanceFlow payCreditBalanceFlow = BeanUtil.copyProperties(payCreditBalanceFlowDTO, new PayCreditBalanceFlow());
        log.info("save PayCreditBalanceFlow:{}", payCreditBalanceFlow);
        payCreditBalanceFlow = (PayCreditBalanceFlow) this.packAddBaseProps(payCreditBalanceFlow, request);

        if (payCreditBalanceFlowDAO.insert(payCreditBalanceFlow) != 1) {
            log.error("insert error, data:{}", payCreditBalanceFlow);
            throw new BizException("Insert payCreditBalanceFlow Error!");
        }
        return payCreditBalanceFlow.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePayCreditBalanceFlowList(@NonNull List<PayCreditBalanceFlow> payCreditBalanceFlowList, HttpServletRequest request) throws BizException {
        if (payCreditBalanceFlowList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = payCreditBalanceFlowDAO.insertList(payCreditBalanceFlowList);
        if (rows != payCreditBalanceFlowList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, payCreditBalanceFlowList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updatePayCreditBalanceFlow(@NonNull Long id, @NonNull PayCreditBalanceFlowDTO payCreditBalanceFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full update payCreditBalanceFlowDTO:{}", payCreditBalanceFlowDTO);
        PayCreditBalanceFlow payCreditBalanceFlow = BeanUtil.copyProperties(payCreditBalanceFlowDTO, new PayCreditBalanceFlow());
        payCreditBalanceFlow.setId(id);
        int cnt = payCreditBalanceFlowDAO.update((PayCreditBalanceFlow) this.packModifyBaseProps(payCreditBalanceFlow, request));
        if (cnt != 1) {
            log.error("update error, data:{}", payCreditBalanceFlowDTO);
            throw new BizException("update payCreditBalanceFlow Error!");
        }
    }

    @Override
    public void updatePayCreditBalanceFlowSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        payCreditBalanceFlowDAO.updatex(params);
    }

    @Override
    public void logicDeletePayCreditBalanceFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = payCreditBalanceFlowDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deletePayCreditBalanceFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = payCreditBalanceFlowDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public PayCreditBalanceFlowDTO findPayCreditBalanceFlowById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        PayCreditBalanceFlowDTO payCreditBalanceFlowDTO = payCreditBalanceFlowDAO.selectOneDTO(params);
        return payCreditBalanceFlowDTO;
    }

    @Override
    public PayCreditBalanceFlowDTO findOnePayCreditBalanceFlow(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        PayCreditBalanceFlow payCreditBalanceFlow = payCreditBalanceFlowDAO.selectOne(params);
        PayCreditBalanceFlowDTO payCreditBalanceFlowDTO = new PayCreditBalanceFlowDTO();
        if (null != payCreditBalanceFlow) {
            BeanUtils.copyProperties(payCreditBalanceFlow, payCreditBalanceFlowDTO);
        }
        return payCreditBalanceFlowDTO;
    }

    @Override
    public List<PayCreditBalanceFlowDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<PayCreditBalanceFlowDTO> resultList = payCreditBalanceFlowDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return payCreditBalanceFlowDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return payCreditBalanceFlowDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = payCreditBalanceFlowDAO.groupCount(conditions);
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
        return payCreditBalanceFlowDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = payCreditBalanceFlowDAO.groupSum(conditions);
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
