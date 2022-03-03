package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.HolidaysConfigDAO;
import com.uwallet.pay.main.model.dto.HolidaysConfigDTO;
import com.uwallet.pay.main.model.entity.HolidaysConfig;
import com.uwallet.pay.main.service.HolidaysConfigService;
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
 * 节假日表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 节假日表
 * @author: baixinyue
 * @date: Created in 2020-09-08 11:24:52
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class HolidaysConfigServiceImpl extends BaseServiceImpl implements HolidaysConfigService {

    @Autowired
    private HolidaysConfigDAO holidaysConfigDAO;

    @Override
    public void saveHolidaysConfig(@NonNull HolidaysConfigDTO holidaysConfigDTO, HttpServletRequest request) throws BizException {
        HolidaysConfig holidaysConfig = BeanUtil.copyProperties(holidaysConfigDTO, new HolidaysConfig());
        log.info("save HolidaysConfig:{}", holidaysConfig);
        if (holidaysConfigDAO.insert((HolidaysConfig) this.packAddBaseProps(holidaysConfig, request)) != 1) {
            log.error("insert error, data:{}", holidaysConfig);
            throw new BizException("Insert holidaysConfig Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveHolidaysConfigList(@NonNull List<HolidaysConfig> holidaysConfigList, HttpServletRequest request) throws BizException {
        if (holidaysConfigList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = holidaysConfigDAO.insertList(holidaysConfigList);
        if (rows != holidaysConfigList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, holidaysConfigList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateHolidaysConfig(@NonNull Long id, @NonNull HolidaysConfigDTO holidaysConfigDTO, HttpServletRequest request) throws BizException {
        log.info("full update holidaysConfigDTO:{}", holidaysConfigDTO);
        HolidaysConfig holidaysConfig = BeanUtil.copyProperties(holidaysConfigDTO, new HolidaysConfig());
        holidaysConfig.setId(id);
        int cnt = holidaysConfigDAO.update((HolidaysConfig) this.packModifyBaseProps(holidaysConfig, request));
        if (cnt != 1) {
            log.error("update error, data:{}", holidaysConfigDTO);
            throw new BizException("update holidaysConfig Error!");
        }
    }

    @Override
    public void updateHolidaysConfigSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        holidaysConfigDAO.updatex(params);
    }

    @Override
    public void logicDeleteHolidaysConfig(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = holidaysConfigDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteHolidaysConfig(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = holidaysConfigDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public HolidaysConfigDTO findHolidaysConfigById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        HolidaysConfigDTO holidaysConfigDTO = holidaysConfigDAO.selectOneDTO(params);
        return holidaysConfigDTO;
    }

    @Override
    public HolidaysConfigDTO findOneHolidaysConfig(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        HolidaysConfig holidaysConfig = holidaysConfigDAO.selectOne(params);
        HolidaysConfigDTO holidaysConfigDTO = new HolidaysConfigDTO();
        if (null != holidaysConfig) {
            BeanUtils.copyProperties(holidaysConfig, holidaysConfigDTO);
        }
        return holidaysConfigDTO;
    }

    @Override
    public List<HolidaysConfigDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<HolidaysConfigDTO> resultList = holidaysConfigDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return holidaysConfigDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return holidaysConfigDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = holidaysConfigDAO.groupCount(conditions);
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
        return holidaysConfigDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = holidaysConfigDAO.groupSum(conditions);
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
    public Boolean isItAHoliday(String date) {
        String year = date.split("-")[0];
        Map<String, Object> params = new HashMap<>(1);
        params.put("year", year);
        HolidaysConfigDTO holidaysConfigDTO = findOneHolidaysConfig(params);
        String[] holidays = holidaysConfigDTO.getHolidays().split(",");
        Boolean isItAHoliday = false;
        for (String holiday : holidays) {
            if (holiday.equals(new StringBuilder(date).substring(date.indexOf("-") + 1, date.length()))) {
                isItAHoliday = true;
                break;
            }
        }
        return isItAHoliday;
    }
}
