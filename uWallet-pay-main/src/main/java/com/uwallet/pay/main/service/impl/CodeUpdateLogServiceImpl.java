package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.CodeUpdateLogDAO;
import com.uwallet.pay.main.model.dto.CodeUpdateLogDTO;
import com.uwallet.pay.main.model.entity.CodeUpdateLog;
import com.uwallet.pay.main.service.CodeUpdateLogService;
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
 * 码操作记录表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 码操作记录表
 * @author: xucl
 * @date: Created in 2021-03-09 09:55:32
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Service
@Slf4j
public class CodeUpdateLogServiceImpl extends BaseServiceImpl implements CodeUpdateLogService {

    @Autowired
    private CodeUpdateLogDAO codeUpdateLogDAO;

    @Override
    public void saveCodeUpdateLog(@NonNull CodeUpdateLogDTO codeUpdateLogDTO, HttpServletRequest request) throws BizException {
        CodeUpdateLog codeUpdateLog = BeanUtil.copyProperties(codeUpdateLogDTO, new CodeUpdateLog());
        log.info("save CodeUpdateLog:{}", codeUpdateLog);
        if (codeUpdateLogDAO.insert((CodeUpdateLog) this.packAddBaseProps(codeUpdateLog, request)) != 1) {
            log.error("insert error, data:{}", codeUpdateLog);
            throw new BizException("Insert codeUpdateLog Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCodeUpdateLogList(@NonNull List<CodeUpdateLog> codeUpdateLogList, HttpServletRequest request) throws BizException {
        if (codeUpdateLogList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = codeUpdateLogDAO.insertList(codeUpdateLogList);
        if (rows != codeUpdateLogList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, codeUpdateLogList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateCodeUpdateLog(@NonNull Long id, @NonNull CodeUpdateLogDTO codeUpdateLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update codeUpdateLogDTO:{}", codeUpdateLogDTO);
        CodeUpdateLog codeUpdateLog = BeanUtil.copyProperties(codeUpdateLogDTO, new CodeUpdateLog());
        codeUpdateLog.setId(id);
        int cnt = codeUpdateLogDAO.update((CodeUpdateLog) this.packModifyBaseProps(codeUpdateLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", codeUpdateLogDTO);
            throw new BizException("update codeUpdateLog Error!");
        }
    }

    @Override
    public void updateCodeUpdateLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        codeUpdateLogDAO.updatex(params);
    }

    @Override
    public void logicDeleteCodeUpdateLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = codeUpdateLogDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteCodeUpdateLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = codeUpdateLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public CodeUpdateLogDTO findCodeUpdateLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        CodeUpdateLogDTO codeUpdateLogDTO = codeUpdateLogDAO.selectOneDTO(params);
        return codeUpdateLogDTO;
    }

    @Override
    public CodeUpdateLogDTO findOneCodeUpdateLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        CodeUpdateLog codeUpdateLog = codeUpdateLogDAO.selectOne(params);
        CodeUpdateLogDTO codeUpdateLogDTO = new CodeUpdateLogDTO();
        if (null != codeUpdateLog) {
            BeanUtils.copyProperties(codeUpdateLog, codeUpdateLogDTO);
        }
        return codeUpdateLogDTO;
    }

    @Override
    public List<CodeUpdateLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<CodeUpdateLogDTO> resultList = codeUpdateLogDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return codeUpdateLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return codeUpdateLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = codeUpdateLogDAO.groupCount(conditions);
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
        return codeUpdateLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = codeUpdateLogDAO.groupSum(conditions);
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
    @Async("taskExecutor")
    @Override
    public void saveCodeUpdateInfo(CodeUpdateLogDTO codeUpdateLogDTO, HttpServletRequest request) throws BizException {
        this.saveCodeUpdateLog(codeUpdateLogDTO,request);
    }

}
