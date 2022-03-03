package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.CreateCreditOrderFlowDAO;
import com.uwallet.pay.main.model.dto.CreateCreditOrderFlowDTO;
import com.uwallet.pay.main.model.entity.CreateCreditOrderFlow;
import com.uwallet.pay.main.service.CreateCreditOrderFlowService;
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
 * @author: zhangzeyuan
 * @date: Created in 2021-07-07 11:21:54
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Service
@Slf4j
public class CreateCreditOrderFlowServiceImpl extends BaseServiceImpl implements CreateCreditOrderFlowService {

    @Autowired
    private CreateCreditOrderFlowDAO createCreditOrderFlowDAO;

    @Override
    public Long saveCreateCreditOrderFlow(@NonNull CreateCreditOrderFlowDTO createCreditOrderFlowDTO, HttpServletRequest request) throws BizException {
        CreateCreditOrderFlow createCreditOrderFlow = BeanUtil.copyProperties(createCreditOrderFlowDTO, new CreateCreditOrderFlow());
        log.info("save CreateCreditOrderFlow:{}", createCreditOrderFlow);
        createCreditOrderFlow = (CreateCreditOrderFlow) this.packAddBaseProps(createCreditOrderFlow, request);
        if (createCreditOrderFlowDAO.insert(createCreditOrderFlow) != 1) {
            log.error("insert error, data:{}", createCreditOrderFlow);
            throw new BizException("Insert createCreditOrderFlow Error!");
        }
        return createCreditOrderFlow.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCreateCreditOrderFlowList(@NonNull List<CreateCreditOrderFlow> createCreditOrderFlowList, HttpServletRequest request) throws BizException {
        if (createCreditOrderFlowList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = createCreditOrderFlowDAO.insertList(createCreditOrderFlowList);
        if (rows != createCreditOrderFlowList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, createCreditOrderFlowList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateCreateCreditOrderFlow(@NonNull Long id, @NonNull CreateCreditOrderFlowDTO createCreditOrderFlowDTO, HttpServletRequest request) throws BizException {
        log.info("full update createCreditOrderFlowDTO:{}", createCreditOrderFlowDTO);
        CreateCreditOrderFlow createCreditOrderFlow = BeanUtil.copyProperties(createCreditOrderFlowDTO, new CreateCreditOrderFlow());
        createCreditOrderFlow.setId(id);
        int cnt = createCreditOrderFlowDAO.update((CreateCreditOrderFlow) this.packModifyBaseProps(createCreditOrderFlow, request));
        if (cnt != 1) {
            log.error("update error, data:{}", createCreditOrderFlowDTO);
            throw new BizException("update createCreditOrderFlow Error!");
        }
    }

    @Override
    public void updateCreateCreditOrderFlowSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        createCreditOrderFlowDAO.updatex(params);
    }

    @Override
    public void logicDeleteCreateCreditOrderFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = createCreditOrderFlowDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteCreateCreditOrderFlow(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = createCreditOrderFlowDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public CreateCreditOrderFlowDTO findCreateCreditOrderFlowById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        CreateCreditOrderFlowDTO createCreditOrderFlowDTO = createCreditOrderFlowDAO.selectOneDTO(params);
        return createCreditOrderFlowDTO;
    }

    @Override
    public CreateCreditOrderFlowDTO findOneCreateCreditOrderFlow(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        CreateCreditOrderFlow createCreditOrderFlow = createCreditOrderFlowDAO.selectOne(params);
        CreateCreditOrderFlowDTO createCreditOrderFlowDTO = new CreateCreditOrderFlowDTO();
        if (null != createCreditOrderFlow) {
            BeanUtils.copyProperties(createCreditOrderFlow, createCreditOrderFlowDTO);
        }
        return createCreditOrderFlowDTO;
    }

    @Override
    public List<CreateCreditOrderFlowDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<CreateCreditOrderFlowDTO> resultList = createCreditOrderFlowDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return createCreditOrderFlowDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return createCreditOrderFlowDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = createCreditOrderFlowDAO.groupCount(conditions);
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
        return createCreditOrderFlowDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = createCreditOrderFlowDAO.groupSum(conditions);
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
