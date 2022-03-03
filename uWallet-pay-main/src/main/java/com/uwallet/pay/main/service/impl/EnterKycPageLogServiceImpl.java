package com.uwallet.pay.main.service.impl;

import com.google.common.collect.Maps;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.dao.EnterKycPageLogDAO;
import com.uwallet.pay.main.model.dto.EnterKycPageLogDTO;
import com.uwallet.pay.main.model.entity.EnterKycPageLog;
import com.uwallet.pay.main.service.EnterKycPageLogService;
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
 * @date: Created in 2021-08-13 15:47:46
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Service
@Slf4j
public class EnterKycPageLogServiceImpl extends BaseServiceImpl implements EnterKycPageLogService {

    @Autowired
    private EnterKycPageLogDAO enterKycPageLogDAO;

    @Override
    public void saveEnterKycPageLog(@NonNull EnterKycPageLogDTO enterKycPageLogDTO, HttpServletRequest request) throws BizException {
        EnterKycPageLog enterKycPageLog = BeanUtil.copyProperties(enterKycPageLogDTO, new EnterKycPageLog());
        log.info("save EnterKycPageLog:{}", enterKycPageLog);
        if (enterKycPageLogDAO.insert((EnterKycPageLog) this.packAddBaseProps(enterKycPageLog, request)) != 1) {
            log.error("insert error, data:{}", enterKycPageLog);
            throw new BizException("Insert enterKycPageLog Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEnterKycPageLogList(@NonNull List<EnterKycPageLog> enterKycPageLogList, HttpServletRequest request) throws BizException {
        if (enterKycPageLogList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = enterKycPageLogDAO.insertList(enterKycPageLogList);
        if (rows != enterKycPageLogList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, enterKycPageLogList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateEnterKycPageLog(@NonNull Long id, @NonNull EnterKycPageLogDTO enterKycPageLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update enterKycPageLogDTO:{}", enterKycPageLogDTO);
        EnterKycPageLog enterKycPageLog = BeanUtil.copyProperties(enterKycPageLogDTO, new EnterKycPageLog());
        enterKycPageLog.setId(id);
        int cnt = enterKycPageLogDAO.update((EnterKycPageLog) this.packModifyBaseProps(enterKycPageLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", enterKycPageLogDTO);
            throw new BizException("update enterKycPageLog Error!");
        }
    }

    @Override
    public void updateEnterKycPageLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        enterKycPageLogDAO.updatex(params);
    }

    @Override
    public void logicDeleteEnterKycPageLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = enterKycPageLogDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteEnterKycPageLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = enterKycPageLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public EnterKycPageLogDTO findEnterKycPageLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        EnterKycPageLogDTO enterKycPageLogDTO = enterKycPageLogDAO.selectOneDTO(params);
        return enterKycPageLogDTO;
    }

    @Override
    public EnterKycPageLogDTO findOneEnterKycPageLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        EnterKycPageLog enterKycPageLog = enterKycPageLogDAO.selectOne(params);
        EnterKycPageLogDTO enterKycPageLogDTO = new EnterKycPageLogDTO();
        if (null != enterKycPageLog) {
            BeanUtils.copyProperties(enterKycPageLog, enterKycPageLogDTO);
        }
        return enterKycPageLogDTO;
    }

    @Override
    public List<EnterKycPageLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<EnterKycPageLogDTO> resultList = enterKycPageLogDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return enterKycPageLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return enterKycPageLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = enterKycPageLogDAO.groupCount(conditions);
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
        return enterKycPageLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = enterKycPageLogDAO.groupSum(conditions);
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

    /**
     * 新增或者更新
     *
     * @param userId
     * @param request
     * @return java.lang.Integer
     * @author zhangzeyuan
     * @date 2021/8/13 15:56
     */
    @Override
    public Integer upsertLog(Long userId, HttpServletRequest request) throws BizException {
        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(4);
        map.put("id", SnowflakeUtil.generateId());
        map.put("userId", userId);
        map.put("time", 1);
        map.put("now", System.currentTimeMillis());
        return enterKycPageLogDAO.upsertLog(map);
    }

}
