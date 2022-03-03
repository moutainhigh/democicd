package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.ApproveLogDAO;
import com.uwallet.pay.main.model.dto.ApproveLogDTO;
import com.uwallet.pay.main.model.dto.MerchantDTO;
import com.uwallet.pay.main.model.entity.ApproveLog;
import com.uwallet.pay.main.service.ApproveLogService;
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
 * 审核日志表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 审核日志表
 * @author: Rainc
 * @date: Created in 2019-12-11 16:34:12
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Rainc
 */
@Service
@Slf4j
public class ApproveLogServiceImpl extends BaseServiceImpl implements ApproveLogService {

    @Autowired
    private ApproveLogDAO approveLogDAO;

    @Override
    public void saveApproveLog(@NonNull ApproveLogDTO approveLogDTO, HttpServletRequest request) throws BizException {
        ApproveLog approveLog = BeanUtil.copyProperties(approveLogDTO, new ApproveLog());
        log.info("save ApproveLog:{}", approveLog);
        if (approveLogDAO.insert((ApproveLog) this.packAddBaseProps(approveLog, request)) != 1) {
            log.error("insert error, data:{}", approveLog);
            throw new BizException("Insert approveLog Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveApproveLogList(@NonNull List<ApproveLog> approveLogList, HttpServletRequest request) throws BizException {
        if (approveLogList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = approveLogDAO.insertList(approveLogList);
        if (rows != approveLogList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, approveLogList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateApproveLog(@NonNull Long id, @NonNull ApproveLogDTO approveLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update approveLogDTO:{}", approveLogDTO);
        ApproveLog approveLog = BeanUtil.copyProperties(approveLogDTO, new ApproveLog());
        approveLog.setId(id);
        int cnt = approveLogDAO.update((ApproveLog) this.packModifyBaseProps(approveLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", approveLogDTO);
            throw new BizException("update approveLog Error!");
        }
    }

    @Override
    public void updateApproveLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        approveLogDAO.updatex(params);
    }

    @Override
    public void logicDeleteApproveLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = approveLogDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteApproveLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = approveLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ApproveLogDTO findApproveLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ApproveLogDTO approveLogDTO = approveLogDAO.selectOneDTO(params);
        return approveLogDTO;
    }

    @Override
    public ApproveLogDTO findOneApproveLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        ApproveLog approveLog = approveLogDAO.selectOne(params);
        ApproveLogDTO approveLogDTO = new ApproveLogDTO();
        if (null != approveLog) {
            BeanUtils.copyProperties(approveLog, approveLogDTO);
        }
        return approveLogDTO;
    }

    @Override
    public List<ApproveLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ApproveLogDTO> resultList = approveLogDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return approveLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return approveLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = approveLogDAO.groupCount(conditions);
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
        return approveLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = approveLogDAO.groupSum(conditions);
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
    public int countMerchantApprove(@NonNull Map<String, Object> params) {
        return approveLogDAO.countMerchantApprove(params);
    }

    @Override
    public List<ApproveLogDTO> findMerchantApprove(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ApproveLogDTO> resultList = approveLogDAO.findMerchantApprove(params);
        for (ApproveLogDTO approveLogDTO : resultList) {
            String data = approveLogDTO.getData();
            JSONObject jsonObject = JSONObject.parseObject(data);
            MerchantDTO merchantDTO = JSONObject.parseObject(jsonObject.toJSONString(), MerchantDTO.class);
            approveLogDTO.setMerchantDTO(merchantDTO);
            approveLogDTO.setData(null);
        }
        return resultList;
    }

    @Override
    public ApproveLogDTO approveLogDetail(@NonNull Long id) {
        Map<String, Object> map = new HashMap<>(1);
        map.put("id", id);
        List<ApproveLogDTO> resultList = approveLogDAO.findMerchantApprove(map);
        ApproveLogDTO approveLogDTO = new ApproveLogDTO();
        if (resultList != null) {
            approveLogDTO = resultList.get(0);
            String data = approveLogDTO.getData();
            JSONObject jsonObject = JSONObject.parseObject(data);
            MerchantDTO merchantDTO = JSONObject.parseObject(jsonObject.toJSONString(), MerchantDTO.class);
            approveLogDTO.setMerchantDTO(merchantDTO);
            approveLogDTO.setData(null);
        }
        return approveLogDTO;
    }

}
