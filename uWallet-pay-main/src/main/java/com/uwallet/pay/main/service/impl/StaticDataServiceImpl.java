package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.StaticDataDAO;
import com.uwallet.pay.main.model.dto.StaticDataDTO;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.service.StaticDataService;
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
 * 数据字典
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 数据字典
 * @author: Strong
 * @date: Created in 2019-12-13 15:35:58
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@Service
@Slf4j
public class StaticDataServiceImpl extends BaseServiceImpl implements StaticDataService {

    @Autowired
    private StaticDataDAO staticDataDAO;

    @Autowired
    private I18nUtils i18nUtils;

    /**
     * 区域代码
     */
    private static final String AREA_CODE = "merchantState";

    @Override
    public void saveStaticData(@NonNull StaticDataDTO staticDataDTO, HttpServletRequest request) throws BizException {
        StaticData staticData = BeanUtil.copyProperties(staticDataDTO, new StaticData());
        log.info("save StaticData:{}", staticData);
        if (staticDataDAO.insert((StaticData) this.packAddBaseProps(staticData, request)) != 1) {
            log.error("insert error, data:{}", staticData);
            throw new BizException("Insert staticData Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveStaticDataList(@NonNull List<StaticData> staticDataList, HttpServletRequest request) throws BizException {
        if (staticDataList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = staticDataDAO.insertList(staticDataList);
        if (rows != staticDataList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, staticDataList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateStaticData(@NonNull Long id, @NonNull StaticDataDTO staticDataDTO, HttpServletRequest request) throws BizException {
        log.info("full update staticDataDTO:{}", staticDataDTO);
        StaticData staticData = BeanUtil.copyProperties(staticDataDTO, new StaticData());
        staticData.setId(id);
        int cnt = staticDataDAO.update((StaticData) this.packModifyBaseProps(staticData, request));
        if (cnt != 1) {
            log.error("update error, data:{}", staticDataDTO);
            throw new BizException("update staticData Error!");
        }
    }

    @Override
    public void updateStaticDataSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        staticDataDAO.updatex(params);
    }

    @Override
    public void logicDeleteStaticData(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = staticDataDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteStaticData(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = staticDataDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public StaticDataDTO findStaticDataById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        StaticDataDTO staticDataDTO = staticDataDAO.selectOneDTO(params);
        return staticDataDTO;
    }

    @Override
    public StaticDataDTO findOneStaticData(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        StaticData staticData = staticDataDAO.selectOne(params);
        StaticDataDTO staticDataDTO = new StaticDataDTO();
        if (null != staticData) {
            BeanUtils.copyProperties(staticData, staticDataDTO);
        }
        return staticDataDTO;
    }

    @Override
    public List<StaticDataDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<StaticDataDTO> resultList = staticDataDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return staticDataDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return staticDataDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = staticDataDAO.groupCount(conditions);
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
        return staticDataDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = staticDataDAO.groupSum(conditions);
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
    public Map<String, List<StaticData>> findByCodeList(String[] codeList) {
        Map<String, Object> params = new HashMap<>(1);
        Map<String, List<StaticData>> result = new HashMap<>(codeList.length);
        List<StaticData> list;
        for (String code : codeList) {
            params.put("code", code);
            list = staticDataDAO.select(params);
//            list = list.stream().sorted(Comparator.comparing(StaticData::getValue)).collect(Collectors.toList());
            result.put(code, list);
        }
        return result;
    }

    @Override
    public List<StaticDataDTO> findArea() {
        Map<String, Object> params = new HashMap<>(16);
        params.put("code", AREA_CODE);
        List<StaticDataDTO> states = find(params, null, null);
        if (states != null && !states.isEmpty()) {
            params.clear();
            states.stream().forEach(staticDataDTO -> {
                params.put("parent", staticDataDTO.getId());
                List<StaticDataDTO> cities = find(params, null, null);
                staticDataDTO.setStaticDataDTOList(cities);
            });
        }
        return states;
    }

    @Override
    public List<JSONObject> findAreaForWeb() {
        Map<String, Object> params = new HashMap<>(16);
        params.put("code", AREA_CODE);
        List<StaticDataDTO> states = find(params, null, null);
        List<JSONObject> cityState = null;
        if (states != null && !states.isEmpty()) {
            cityState = new ArrayList<>(states.size());
            params.clear();
            List<JSONObject> finalCityState = cityState;
            states.stream().forEach(staticDataDTO -> {
                JSONObject state = new JSONObject();
                state.put("value", staticDataDTO.getValue());
                state.put("label", staticDataDTO.getEnName());
                params.put("parent", staticDataDTO.getId());
                List<StaticDataDTO> cities = find(params, null, null);
                if (cities != null && !cities.isEmpty()) {
                    List<JSONObject> packageCities = new ArrayList<>(cities.size());
                    cities.stream().forEach(staticDataDTO1 -> {
                        JSONObject city = new JSONObject();
                        city.put("value", staticDataDTO1.getValue());
                        city.put("label", staticDataDTO1.getEnName());
                        packageCities.add(city);
                    });
                    state.put("children", packageCities);
                }
                finalCityState.add(state);
            });
            cityState = finalCityState;
        }
        return cityState;
    }

    /**
     * 城市模糊搜索
     *
     * @param keyword
     * @return java.util.List<com.uwallet.pay.main.model.dto.StaticDataDTO>
     * @author zhangzeyuan
     * @date 2021/11/22 10:46
     */
    @Override
    public List<StaticDataDTO> getCityListByKeywords(String keyword) {
        return null;
    }

    /**
     * 获取支持的卡列表品牌
     *
     * @param code
     * @return java.util.List<java.lang.String>
     * @author zhangzeyuan
     * @date 2022/1/28 10:44
     */
    @Override
    public List<String> getSupportedCardList(String code) {
        return staticDataDAO.getsupportedCardList(code);
    }

    /**
     * 根据条件查询得到第一条 staticData
     *
     * @param  code 查询条件
     * @return 符合条件的一个 staticData
     */
    @Override
    public String selectCountry(String code) {
        return staticDataDAO.selectCountry(code);
    }


}
