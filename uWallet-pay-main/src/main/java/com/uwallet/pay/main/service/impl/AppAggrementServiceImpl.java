package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.dao.AppAggrementDAO;
import com.uwallet.pay.main.model.dto.AppAggrementDTO;
import com.uwallet.pay.main.model.entity.AppAggrement;
import com.uwallet.pay.main.service.AppAggrementService;
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
 * app 协议
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: app 协议
 * @author: baixinyue
 * @date: Created in 2019-12-19 11:28:23
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class AppAggrementServiceImpl extends BaseServiceImpl implements AppAggrementService {

    @Autowired
    private AppAggrementDAO appAggrementDAO;

    @Override
    public void saveAppAggrement(@NonNull AppAggrementDTO appAggrementDTO, HttpServletRequest request) throws BizException {
        AppAggrement appAggrement = BeanUtil.copyProperties(appAggrementDTO, new AppAggrement());
        log.info("save AppAggrement:{}", appAggrement);
        if (appAggrementDAO.insert((AppAggrement) this.packAddBaseProps(appAggrement, request)) != 1) {
            log.error("insert error, data:{}", appAggrement);
            throw new BizException("Insert appAggrement Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAppAggrementList(@NonNull List<AppAggrement> appAggrementList, HttpServletRequest request) throws BizException {
        if (appAggrementList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = appAggrementDAO.insertList(appAggrementList);
        if (rows != appAggrementList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, appAggrementList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateAppAggrement(@NonNull Long id, @NonNull AppAggrementDTO appAggrementDTO, HttpServletRequest request) throws BizException {
        log.info("full update appAggrementDTO:{}", appAggrementDTO);
        AppAggrement appAggrement = BeanUtil.copyProperties(appAggrementDTO, new AppAggrement());
        appAggrement.setId(id);
        int cnt = appAggrementDAO.update((AppAggrement) this.packModifyBaseProps(appAggrement, request));
        if (cnt != 1) {
            log.error("update error, data:{}", appAggrementDTO);
            throw new BizException("update appAggrement Error!");
        }
    }

    @Override
    public void updateAppAggrementSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        appAggrementDAO.updatex(params);
    }

    @Override
    public void logicDeleteAppAggrement(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = appAggrementDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteAppAggrement(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = appAggrementDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public AppAggrementDTO findAppAggrementById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AppAggrementDTO appAggrementDTO = appAggrementDAO.selectOneDTO(params);
        return appAggrementDTO;
    }

    @Override
    public AppAggrementDTO findOneAppAggrement(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        AppAggrement appAggrement = appAggrementDAO.selectOne(params);
        AppAggrementDTO appAggrementDTO = new AppAggrementDTO();
        if (null != appAggrement) {
            BeanUtils.copyProperties(appAggrement, appAggrementDTO);
        }
        return appAggrementDTO;
    }

    @Override
    public List<AppAggrementDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<AppAggrementDTO> resultList = appAggrementDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return appAggrementDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return appAggrementDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = appAggrementDAO.groupCount(conditions);
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
        return appAggrementDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = appAggrementDAO.groupSum(conditions);
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
