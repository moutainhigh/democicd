package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.ContractLogDAO;
import com.uwallet.pay.main.model.dto.ContractLogDTO;
import com.uwallet.pay.main.model.entity.ContractLog;
import com.uwallet.pay.main.service.ContractLogService;
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
 * 合同记录表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 合同记录表
 * @author: xucl
 * @date: Created in 2021-04-27 10:13:42
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Service
@Slf4j
public class ContractLogServiceImpl extends BaseServiceImpl implements ContractLogService {

    @Autowired
    private ContractLogDAO contractLogDAO;

    @Override
    public void saveContractLog(@NonNull ContractLogDTO contractLogDTO, HttpServletRequest request) throws BizException {
        ContractLog contractLog = BeanUtil.copyProperties(contractLogDTO, new ContractLog());
        log.info("save ContractLog:{}", contractLog);
        if (contractLogDAO.insert((ContractLog) this.packAddBaseProps(contractLog, request)) != 1) {
            log.error("insert error, data:{}", contractLog);
            throw new BizException("Insert contractLog Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveContractLogList(@NonNull List<ContractLog> contractLogList, HttpServletRequest request) throws BizException {
        if (contractLogList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = contractLogDAO.insertList(contractLogList);
        if (rows != contractLogList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, contractLogList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateContractLog(@NonNull Long id, @NonNull ContractLogDTO contractLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update contractLogDTO:{}", contractLogDTO);
        ContractLog contractLog = BeanUtil.copyProperties(contractLogDTO, new ContractLog());
        contractLog.setId(id);
        int cnt = contractLogDAO.update((ContractLog) this.packModifyBaseProps(contractLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", contractLogDTO);
            throw new BizException("update contractLog Error!");
        }
    }

    @Override
    public void updateContractLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        contractLogDAO.updatex(params);
    }

    @Override
    public void logicDeleteContractLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = contractLogDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteContractLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = contractLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ContractLogDTO findContractLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ContractLogDTO contractLogDTO = contractLogDAO.selectOneDTO(params);
        return contractLogDTO;
    }

    @Override
    public ContractLogDTO findOneContractLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        ContractLog contractLog = contractLogDAO.selectOne(params);
        ContractLogDTO contractLogDTO = new ContractLogDTO();
        if (null != contractLog) {
            BeanUtils.copyProperties(contractLog, contractLogDTO);
        }
        return contractLogDTO;
    }

    @Override
    public List<ContractLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ContractLogDTO> resultList = contractLogDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return contractLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return contractLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = contractLogDAO.groupCount(conditions);
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
        return contractLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = contractLogDAO.groupSum(conditions);
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
