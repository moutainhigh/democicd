package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.model.entity.BaseEntity;
import com.uwallet.pay.main.dao.RequestAnalysisDAO;
import com.uwallet.pay.main.model.dto.RequestAnalysisDTO;
import com.uwallet.pay.main.model.entity.RequestAnalysis;
import com.uwallet.pay.main.service.RequestAnalysisService;
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
 * 接口请求数据统计表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 接口请求数据统计表
 * @author: aaronS
 * @date: Created in 2021-02-06 14:03:58
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: aaronS
 */
@Service
@Slf4j
public class RequestAnalysisServiceImpl extends BaseServiceImpl implements RequestAnalysisService {

    @Autowired
    private RequestAnalysisDAO requestAnalysisDAO;

    @Override
    public Long saveRequestAnalysis(@NonNull RequestAnalysisDTO requestAnalysisDTO, HttpServletRequest request) throws BizException {
        RequestAnalysis requestAnalysis = BeanUtil.copyProperties(requestAnalysisDTO, new RequestAnalysis());
        RequestAnalysis analysis = (RequestAnalysis) this.packAddBaseProps(requestAnalysis, request);
        if (requestAnalysisDAO.insert(analysis) != 1) {
            log.error("insert error, data:{}", requestAnalysis);
            throw new BizException("Insert requestAnalysis Error!");
        }
        return analysis.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRequestAnalysisList(@NonNull List<RequestAnalysis> requestAnalysisList, HttpServletRequest request) throws BizException {
        if (requestAnalysisList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = requestAnalysisDAO.insertList(requestAnalysisList);
        if (rows != requestAnalysisList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, requestAnalysisList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateRequestAnalysis(@NonNull Long id, @NonNull RequestAnalysisDTO requestAnalysisDTO) throws BizException {
        RequestAnalysis requestAnalysis = BeanUtil.copyProperties(requestAnalysisDTO, new RequestAnalysis());
        requestAnalysis.setId(id);
        int cnt = requestAnalysisDAO.update(requestAnalysis);
        if (cnt != 1) {
            log.error("update error, data:{}", requestAnalysisDTO);
            throw new BizException("update requestAnalysis Error!");
        }
    }

    @Override
    public void updateRequestAnalysisSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        requestAnalysisDAO.updatex(params);
    }

    @Override
    public void logicDeleteRequestAnalysis(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = requestAnalysisDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteRequestAnalysis(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = requestAnalysisDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public RequestAnalysisDTO findRequestAnalysisById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        RequestAnalysisDTO requestAnalysisDTO = requestAnalysisDAO.selectOneDTO(params);
        return requestAnalysisDTO;
    }

    @Override
    public RequestAnalysisDTO findOneRequestAnalysis(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        RequestAnalysis requestAnalysis = requestAnalysisDAO.selectOne(params);
        RequestAnalysisDTO requestAnalysisDTO = new RequestAnalysisDTO();
        if (null != requestAnalysis) {
            BeanUtils.copyProperties(requestAnalysis, requestAnalysisDTO);
        }
        return requestAnalysisDTO;
    }

    @Override
    public List<RequestAnalysisDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<RequestAnalysisDTO> resultList = requestAnalysisDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return requestAnalysisDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return requestAnalysisDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = requestAnalysisDAO.groupCount(conditions);
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
        return requestAnalysisDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = requestAnalysisDAO.groupSum(conditions);
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
