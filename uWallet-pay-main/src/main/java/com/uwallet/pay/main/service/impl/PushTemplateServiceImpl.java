package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.PushTemplateDAO;
import com.uwallet.pay.main.model.dto.PushTemplateDTO;
import com.uwallet.pay.main.model.entity.PushTemplate;
import com.uwallet.pay.main.service.PushTemplateService;
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
 * 模板
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 模板
 * @author: zhoutt
 * @date: Created in 2020-01-04 13:52:28
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class PushTemplateServiceImpl extends BaseServiceImpl implements PushTemplateService {

    @Autowired
    private PushTemplateDAO pushTemplateDAO;

    @Override
    public void savePushTemplate(@NonNull PushTemplateDTO pushTemplateDTO, HttpServletRequest request) throws BizException {
        PushTemplate pushTemplate = BeanUtil.copyProperties(pushTemplateDTO, new PushTemplate());
        log.info("save PushTemplate:{}", pushTemplate);
        if (pushTemplateDAO.insert((PushTemplate) this.packAddBaseProps(pushTemplate, request)) != 1) {
            log.error("insert error, data:{}", pushTemplate);
            throw new BizException("Insert pushTemplate Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePushTemplateList(@NonNull List<PushTemplate> pushTemplateList, HttpServletRequest request) throws BizException {
        if (pushTemplateList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = pushTemplateDAO.insertList(pushTemplateList);
        if (rows != pushTemplateList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, pushTemplateList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updatePushTemplate(@NonNull Long id, @NonNull PushTemplateDTO pushTemplateDTO, HttpServletRequest request) throws BizException {
        log.info("full update pushTemplateDTO:{}", pushTemplateDTO);
        PushTemplate pushTemplate = BeanUtil.copyProperties(pushTemplateDTO, new PushTemplate());
        pushTemplate.setId(id);
        int cnt = pushTemplateDAO.update((PushTemplate) this.packModifyBaseProps(pushTemplate, request));
        if (cnt != 1) {
            log.error("update error, data:{}", pushTemplateDTO);
            throw new BizException("update pushTemplate Error!");
        }
    }

    @Override
    public void updatePushTemplateSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        pushTemplateDAO.updatex(params);
    }

    @Override
    public void logicDeletePushTemplate(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = pushTemplateDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deletePushTemplate(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = pushTemplateDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public PushTemplateDTO findPushTemplateById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        PushTemplateDTO pushTemplateDTO = pushTemplateDAO.selectOneDTO(params);
        return pushTemplateDTO;
    }

    @Override
    public PushTemplateDTO findOnePushTemplate(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        PushTemplate pushTemplate = pushTemplateDAO.selectOne(params);
        PushTemplateDTO pushTemplateDTO = new PushTemplateDTO();
        if (null != pushTemplate) {
            BeanUtils.copyProperties(pushTemplate, pushTemplateDTO);
        }
        return pushTemplateDTO;
    }

    @Override
    public List<PushTemplateDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<PushTemplateDTO> resultList = pushTemplateDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return pushTemplateDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return pushTemplateDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = pushTemplateDAO.groupCount(conditions);
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
        return pushTemplateDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = pushTemplateDAO.groupSum(conditions);
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
