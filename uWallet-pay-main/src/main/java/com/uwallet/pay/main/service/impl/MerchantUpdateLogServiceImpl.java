package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.MerchantUpdateLogDAO;
import com.uwallet.pay.main.model.dto.MerchantUpdateLogDTO;
import com.uwallet.pay.main.model.entity.MerchantUpdateLog;
import com.uwallet.pay.main.service.MerchantUpdateLogService;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 商户修改记录表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 商户修改记录表
 * @author: xucl
 * @date: Created in 2021-03-15 08:39:48
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Service
@Slf4j
public class MerchantUpdateLogServiceImpl extends BaseServiceImpl implements MerchantUpdateLogService {

    @Autowired
    private MerchantUpdateLogDAO merchantUpdateLogDAO;

    @Async("taskExecutor")
    @Override
    public void saveMerchantUpdateLog(@NonNull MerchantUpdateLogDTO merchantUpdateLogDTO, HttpServletRequest request) throws BizException {
        MerchantUpdateLog merchantUpdateLog = BeanUtil.copyProperties(merchantUpdateLogDTO, new MerchantUpdateLog());
        log.info("save MerchantUpdateLog:{}", merchantUpdateLog);
        if (merchantUpdateLogDAO.insert((MerchantUpdateLog) this.packAddBaseProps(merchantUpdateLog, request)) != 1) {
            log.error("insert error, data:{}", merchantUpdateLog);
            throw new BizException("Insert merchantUpdateLog Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMerchantUpdateLogList(@NonNull List<MerchantUpdateLog> merchantUpdateLogList, HttpServletRequest request) throws BizException {
        if (merchantUpdateLogList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = merchantUpdateLogDAO.insertList(merchantUpdateLogList);
        if (rows != merchantUpdateLogList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, merchantUpdateLogList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateMerchantUpdateLog(@NonNull Long id, @NonNull MerchantUpdateLogDTO merchantUpdateLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update merchantUpdateLogDTO:{}", merchantUpdateLogDTO);
        MerchantUpdateLog merchantUpdateLog = BeanUtil.copyProperties(merchantUpdateLogDTO, new MerchantUpdateLog());
        merchantUpdateLog.setId(id);
        int cnt = merchantUpdateLogDAO.update((MerchantUpdateLog) this.packModifyBaseProps(merchantUpdateLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", merchantUpdateLogDTO);
            throw new BizException("update merchantUpdateLog Error!");
        }
    }

    @Override
    public void updateMerchantUpdateLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        merchantUpdateLogDAO.updatex(params);
    }

    @Override
    public void logicDeleteMerchantUpdateLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = merchantUpdateLogDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteMerchantUpdateLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = merchantUpdateLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public MerchantUpdateLogDTO findMerchantUpdateLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        MerchantUpdateLogDTO merchantUpdateLogDTO = merchantUpdateLogDAO.selectOneDTO(params);
        return merchantUpdateLogDTO;
    }

    @Override
    public MerchantUpdateLogDTO findOneMerchantUpdateLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        MerchantUpdateLog merchantUpdateLog = merchantUpdateLogDAO.selectOne(params);
        MerchantUpdateLogDTO merchantUpdateLogDTO = new MerchantUpdateLogDTO();
        if (null != merchantUpdateLog) {
            BeanUtils.copyProperties(merchantUpdateLog, merchantUpdateLogDTO);
        }
        return merchantUpdateLogDTO;
    }

    @Override
    public List<MerchantUpdateLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MerchantUpdateLogDTO> resultList = merchantUpdateLogDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return merchantUpdateLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return merchantUpdateLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = merchantUpdateLogDAO.groupCount(conditions);
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
        return merchantUpdateLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = merchantUpdateLogDAO.groupSum(conditions);
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
