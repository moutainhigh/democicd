package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.TieOnCardFlowDAO;
import com.uwallet.pay.main.model.dto.CountryIsoDTO;
import com.uwallet.pay.main.model.dto.TieOnCardFlowDTO;
import com.uwallet.pay.main.model.entity.TieOnCardFlow;
import com.uwallet.pay.main.service.TieOnCardFlowService;
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
import java.util.stream.Collectors;

/**
 * <p>
 * 绑卡交易流水表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 绑卡交易流水表
 * @author: baixinyue
 * @date: Created in 2020-01-06 11:37:40
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class TieOnCardFlowServiceImpl extends BaseServiceImpl implements TieOnCardFlowService {

    @Autowired
    private TieOnCardFlowDAO tieOnCardFlowDAO;

    @Override
    public Long saveTieOnCardFlow(@NonNull TieOnCardFlowDTO tieOnCardFlowDTO, HttpServletRequest request) throws BizException {
        TieOnCardFlow tieOnCardFlow = BeanUtil.copyProperties(tieOnCardFlowDTO, new TieOnCardFlow());
        log.info("save TieOnCardFlow:{}", tieOnCardFlow);
        tieOnCardFlow = (TieOnCardFlow) this.packAddBaseProps(tieOnCardFlow, request);
        if (tieOnCardFlowDAO.insert(tieOnCardFlow) != 1) {
            log.error("insert error, data:{}", tieOnCardFlow);
            throw new BizException("Insert tieOnCardFlow Error!");
        }
        return tieOnCardFlow.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTieOnCardFlowList(@NonNull List<TieOnCardFlow> tieOnCardFlowList, HttpServletRequest request) throws BizException {
        if (tieOnCardFlowList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = tieOnCardFlowDAO.insertList(tieOnCardFlowList);
        if (rows != tieOnCardFlowList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, tieOnCardFlowList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateTieOnCardFlow(@NonNull Long id, @NonNull TieOnCardFlowDTO tieOnCardFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full update tieOnCardFlowDTO:{}", tieOnCardFlowDTO);
        TieOnCardFlow tieOnCardFlow = BeanUtil.copyProperties(tieOnCardFlowDTO, new TieOnCardFlow());
        tieOnCardFlow.setId(id);
        if (request != null) {
            tieOnCardFlow = (TieOnCardFlow) this.packModifyBaseProps(tieOnCardFlow, request);
        } else {
            tieOnCardFlow.setModifiedDate(System.currentTimeMillis());
        }
        int cnt = tieOnCardFlowDAO.update(tieOnCardFlow);
        if (cnt != 1) {
            log.error("update error, data:{}", tieOnCardFlowDTO);
            throw new BizException("update tieOnCardFlow Error!");
        }
    }

    @Override
    public void updateTieOnCardFlowSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        tieOnCardFlowDAO.updatex(params);
    }

    @Override
    public void logicDeleteTieOnCardFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = tieOnCardFlowDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteTieOnCardFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = tieOnCardFlowDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public TieOnCardFlowDTO findTieOnCardFlowById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        TieOnCardFlowDTO tieOnCardFlowDTO = tieOnCardFlowDAO.selectOneDTO(params);
        return tieOnCardFlowDTO;
    }

    @Override
    public TieOnCardFlowDTO findOneTieOnCardFlow(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        TieOnCardFlow tieOnCardFlow = tieOnCardFlowDAO.selectOne(params);
        TieOnCardFlowDTO tieOnCardFlowDTO = new TieOnCardFlowDTO();
        if (null != tieOnCardFlow) {
            BeanUtils.copyProperties(tieOnCardFlow, tieOnCardFlowDTO);
        }
        return tieOnCardFlowDTO;
    }

    @Override
    public List<TieOnCardFlowDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<TieOnCardFlowDTO> resultList = tieOnCardFlowDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return tieOnCardFlowDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return tieOnCardFlowDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = tieOnCardFlowDAO.groupCount(conditions);
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
        return tieOnCardFlowDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = tieOnCardFlowDAO.groupSum(conditions);
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
    public CountryIsoDTO selectCountryIso(String country) {
        return tieOnCardFlowDAO.selectCountryIso(country);
    }

}
