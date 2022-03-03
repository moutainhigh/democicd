package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.dao.ApiApproveLogDAO;
import com.uwallet.pay.main.model.dto.ApiApproveLogDTO;
import com.uwallet.pay.main.model.dto.ApiMerchantDTO;
import com.uwallet.pay.main.model.dto.ApproveLogDTO;
import com.uwallet.pay.main.model.dto.MerchantDTO;
import com.uwallet.pay.main.model.entity.ApiApproveLog;
import com.uwallet.pay.main.service.ApiApproveLogService;
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
 * 审核日志
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 审核日志
 * @author: zhoutt
 * @date: Created in 2021-09-23 15:39:54
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class ApiApproveLogServiceImpl extends BaseServiceImpl implements ApiApproveLogService {

    @Autowired
    private ApiApproveLogDAO apiApproveLogDAO;

    @Override
    public void saveApiApproveLog(@NonNull ApiApproveLogDTO apiApproveLogDTO, HttpServletRequest request) throws BizException {
        ApiApproveLog apiApproveLog = BeanUtil.copyProperties(apiApproveLogDTO, new ApiApproveLog());
        log.info("save ApiApproveLog:{}", apiApproveLog);
        if (apiApproveLogDAO.insert((ApiApproveLog) this.packAddBaseProps(apiApproveLog, request)) != 1) {
            log.error("insert error, data:{}", apiApproveLog);
            throw new BizException("Insert apiApproveLog Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveApiApproveLogList(@NonNull List<ApiApproveLog> apiApproveLogList, HttpServletRequest request) throws BizException {
        if (apiApproveLogList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = apiApproveLogDAO.insertList(apiApproveLogList);
        if (rows != apiApproveLogList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, apiApproveLogList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateApiApproveLog(@NonNull Long id, @NonNull ApiApproveLogDTO apiApproveLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update apiApproveLogDTO:{}", apiApproveLogDTO);
        ApiApproveLog apiApproveLog = BeanUtil.copyProperties(apiApproveLogDTO, new ApiApproveLog());
        apiApproveLog.setId(id);
        int cnt = apiApproveLogDAO.update((ApiApproveLog) this.packModifyBaseProps(apiApproveLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", apiApproveLogDTO);
            throw new BizException("update apiApproveLog Error!");
        }
    }

    @Override
    public void updateApiApproveLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        apiApproveLogDAO.updatex(params);
    }

    @Override
    public void logicDeleteApiApproveLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = apiApproveLogDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteApiApproveLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = apiApproveLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ApiApproveLogDTO findApiApproveLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ApiApproveLogDTO apiApproveLogDTO = apiApproveLogDAO.selectOneDTO(params);
        return apiApproveLogDTO;
    }

    @Override
    public ApiApproveLogDTO findOneApiApproveLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        ApiApproveLog apiApproveLog = apiApproveLogDAO.selectOne(params);
        ApiApproveLogDTO apiApproveLogDTO = new ApiApproveLogDTO();
        if (null != apiApproveLog) {
            BeanUtils.copyProperties(apiApproveLog, apiApproveLogDTO);
        }
        return apiApproveLogDTO;
    }

    @Override
    public List<ApiApproveLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ApiApproveLogDTO> resultList = apiApproveLogDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return apiApproveLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return apiApproveLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = apiApproveLogDAO.groupCount(conditions);
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
        return apiApproveLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = apiApproveLogDAO.groupSum(conditions);
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
    public int countMerchantApprove(Map<String, Object> params) {
        return apiApproveLogDAO.countMerchantApprove(params);
    }

    @Override
    public List<ApiApproveLogDTO> findMerchantApprove(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ApiApproveLogDTO> resultList = apiApproveLogDAO.findMerchantApprove(params);
        for (ApiApproveLogDTO approveLogDTO : resultList) {
            String data = approveLogDTO.getData();
            JSONObject jsonObject = JSONObject.parseObject(data);
            ApiMerchantDTO merchantDTO = JSONObject.parseObject(jsonObject.toJSONString(), ApiMerchantDTO.class);
            approveLogDTO.setMerchantDTO(merchantDTO);
            approveLogDTO.setData(null);
        }
        return resultList;
    }

    @Override
    public ApiApproveLogDTO approveLogDetail(Long id) {
        Map<String, Object> map = new HashMap<>(1);
        map.put("id", id);
        ApiApproveLogDTO result = this.findApiApproveLogById(id);
        ApiApproveLogDTO approveLogDTO = new ApiApproveLogDTO();
        if (result != null && result.getId() != null) {
            String data = result.getData();
            JSONObject jsonObject = JSONObject.parseObject(data);
            ApiMerchantDTO merchantDTO = JSONObject.parseObject(jsonObject.toJSONString(), ApiMerchantDTO.class);
            approveLogDTO.setMerchantDTO(merchantDTO);
        }
        return approveLogDTO;
    }

}
