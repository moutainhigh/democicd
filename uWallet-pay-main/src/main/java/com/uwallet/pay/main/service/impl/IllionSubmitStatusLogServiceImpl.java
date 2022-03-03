package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.dao.IllionSubmitStatusLogDAO;
import com.uwallet.pay.main.model.dto.IllionSubmitStatusLogDTO;
import com.uwallet.pay.main.model.entity.IllionSubmitStatusLog;
import com.uwallet.pay.main.service.IllionSubmitStatusLogService;
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
 *
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description:
 * @author: xucl
 * @date: Created in 2021-06-22 13:04:06
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Service
@Slf4j
public class IllionSubmitStatusLogServiceImpl extends BaseServiceImpl implements IllionSubmitStatusLogService {

    @Autowired
    private IllionSubmitStatusLogDAO illionSubmitStatusLogDAO;

    @Override
    public void saveIllionSubmitStatusLog(@NonNull IllionSubmitStatusLogDTO illionSubmitStatusLogDTO, HttpServletRequest request) throws BizException {
        IllionSubmitStatusLog illionSubmitStatusLog = BeanUtil.copyProperties(illionSubmitStatusLogDTO, new IllionSubmitStatusLog());
        log.info("save IllionSubmitStatusLog:{}", illionSubmitStatusLog);
        if (illionSubmitStatusLogDAO.insert((IllionSubmitStatusLog) this.packAddBaseProps(illionSubmitStatusLog, request)) != 1) {
            log.error("insert error, data:{}", illionSubmitStatusLog);
            throw new BizException("Insert illionSubmitStatusLog Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveIllionSubmitStatusLogList(@NonNull List<IllionSubmitStatusLog> illionSubmitStatusLogList, HttpServletRequest request) throws BizException {
        if (illionSubmitStatusLogList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = illionSubmitStatusLogDAO.insertList(illionSubmitStatusLogList);
        if (rows != illionSubmitStatusLogList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, illionSubmitStatusLogList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateIllionSubmitStatusLog(@NonNull Long id, @NonNull IllionSubmitStatusLogDTO illionSubmitStatusLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update illionSubmitStatusLogDTO:{}", illionSubmitStatusLogDTO);
        IllionSubmitStatusLog illionSubmitStatusLog = BeanUtil.copyProperties(illionSubmitStatusLogDTO, new IllionSubmitStatusLog());
        illionSubmitStatusLog.setId(id);
        int cnt = illionSubmitStatusLogDAO.update((IllionSubmitStatusLog) this.packModifyBaseProps(illionSubmitStatusLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", illionSubmitStatusLogDTO);
            throw new BizException("update illionSubmitStatusLog Error!");
        }
    }

    @Override
    public void updateIllionSubmitStatusLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        illionSubmitStatusLogDAO.updatex(params);
    }

    @Override
    public void logicDeleteIllionSubmitStatusLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = illionSubmitStatusLogDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteIllionSubmitStatusLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = illionSubmitStatusLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public IllionSubmitStatusLogDTO findIllionSubmitStatusLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        IllionSubmitStatusLogDTO illionSubmitStatusLogDTO = illionSubmitStatusLogDAO.selectOneDTO(params);
        return illionSubmitStatusLogDTO;
    }

    @Override
    public IllionSubmitStatusLogDTO findOneIllionSubmitStatusLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        IllionSubmitStatusLog illionSubmitStatusLog = illionSubmitStatusLogDAO.selectOne(params);
        IllionSubmitStatusLogDTO illionSubmitStatusLogDTO = new IllionSubmitStatusLogDTO();
        if (null != illionSubmitStatusLog) {
            BeanUtils.copyProperties(illionSubmitStatusLog, illionSubmitStatusLogDTO);
        }
        return illionSubmitStatusLogDTO;
    }

    @Override
    public List<IllionSubmitStatusLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<IllionSubmitStatusLogDTO> resultList = illionSubmitStatusLogDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return illionSubmitStatusLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return illionSubmitStatusLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = illionSubmitStatusLogDAO.groupCount(conditions);
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
        return illionSubmitStatusLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = illionSubmitStatusLogDAO.groupSum(conditions);
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
    public void addSubmitStatusLog(JSONObject param, HttpServletRequest request) {
        log.info("记录一条用户illion流程状态,param:{}",param);
        try{
            Integer state = param.getInteger("state");
            Long userId = param.getLong("userId");
            if (state==null||userId==null){
                throw new BizException(I18nUtils.get("parameters.null",getLang(request)));
            }
            IllionSubmitStatusLogDTO illionSubmitStatusLogDTO=new IllionSubmitStatusLogDTO();
            illionSubmitStatusLogDTO.setState(state);
            illionSubmitStatusLogDTO.setUserId(userId);
            illionSubmitStatusLogDTO.setErrorMessage(param.getString("errorMessage"));
            this.saveIllionSubmitStatusLog(illionSubmitStatusLogDTO,request);
        }catch (Exception e){
            log.error("记录一条用户illion流程状态异常,param:{},e:{}",param,e);
        }

    }

}
