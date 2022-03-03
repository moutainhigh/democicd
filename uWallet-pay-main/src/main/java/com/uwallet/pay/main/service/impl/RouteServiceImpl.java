package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.RouteDAO;
import com.uwallet.pay.main.model.dto.RouteDTO;
import com.uwallet.pay.main.model.entity.Route;
import com.uwallet.pay.main.service.RouteService;
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
 * 路由表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 路由表
 * @author: Rainc
 * @date: Created in 2019-12-11 16:57:32
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Rainc
 */
@Service
@Slf4j
public class RouteServiceImpl extends BaseServiceImpl implements RouteService {

    @Autowired
    private RouteDAO routeDAO;

    @Override
    public void saveRoute(@NonNull RouteDTO routeDTO, HttpServletRequest request) throws BizException {
        Route route = BeanUtil.copyProperties(routeDTO, new Route());
        log.info("save Route:{}", route);
        if (routeDAO.insert((Route) this.packAddBaseProps(route, request)) != 1) {
            log.error("insert error, data:{}", route);
            throw new BizException("Insert route Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRouteList(@NonNull List<Route> routeList, HttpServletRequest request) throws BizException {
        if (routeList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = routeDAO.insertList(routeList);
        if (rows != routeList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, routeList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateRoute(@NonNull Long id, @NonNull RouteDTO routeDTO, HttpServletRequest request) throws BizException {
        log.info("full update routeDTO:{}", routeDTO);
        Route route = BeanUtil.copyProperties(routeDTO, new Route());
        route.setId(id);
        int cnt = routeDAO.update((Route) this.packModifyBaseProps(route, request));
        if (cnt != 1) {
            log.error("update error, data:{}", routeDTO);
            throw new BizException("update route Error!");
        }
    }

    @Override
    public void updateRouteSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        routeDAO.updatex(params);
    }

    @Override
    public void logicDeleteRoute(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = routeDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteRoute(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = routeDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public RouteDTO findRouteById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        RouteDTO routeDTO = routeDAO.selectOneDTO(params);
        return routeDTO;
    }

    @Override
    public RouteDTO findOneRoute(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        Route route = routeDAO.selectOne(params);
        RouteDTO routeDTO = new RouteDTO();
        if (null != route) {
            BeanUtils.copyProperties(route, routeDTO);
        }
        return routeDTO;
    }

    @Override
    public List<RouteDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<RouteDTO> resultList = routeDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<RouteDTO> findList(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<RouteDTO> resultList = routeDAO.findList(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return routeDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return routeDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = routeDAO.groupCount(conditions);
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
        return routeDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = routeDAO.groupSum(conditions);
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
    public void deleteRouteByMerchantId(@NonNull Long merchantId) {
        log.info("物理删除, merchantId:{}", merchantId);
        int i = routeDAO.deleteRouteByMerchantId(merchantId);
        log.info("实际删除了{}条数据", i);
    }

    @Override
    public RouteDTO findMaxMerRate(Long merchantId) {
        return routeDAO.findMaxMerRate(merchantId);
    }

}
