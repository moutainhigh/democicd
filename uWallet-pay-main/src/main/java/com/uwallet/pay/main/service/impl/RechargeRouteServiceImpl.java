package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.RechargeRouteDAO;
import com.uwallet.pay.main.model.dto.RechargeRouteDTO;
import com.uwallet.pay.main.model.entity.RechargeRoute;
import com.uwallet.pay.main.service.RechargeRouteService;
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
 * 充值转账路由表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 充值转账路由表
 * @author: zhoutt
 * @date: Created in 2019-12-17 10:36:56
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class RechargeRouteServiceImpl extends BaseServiceImpl implements RechargeRouteService {

    @Autowired
    private RechargeRouteDAO rechargeRouteDAO;

    @Override
    public void saveRechargeRoute(@NonNull RechargeRouteDTO rechargeRouteDTO, HttpServletRequest request) throws BizException {
        RechargeRoute rechargeRoute = BeanUtil.copyProperties(rechargeRouteDTO, new RechargeRoute());
        log.info("save RechargeRoute:{}", rechargeRoute);
        if (rechargeRouteDAO.insert((RechargeRoute) this.packAddBaseProps(rechargeRoute, request)) != 1) {
            log.error("insert error, data:{}", rechargeRoute);
            throw new BizException("Insert rechargeRoute Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRechargeRouteList(@NonNull List<RechargeRoute> rechargeRouteList, HttpServletRequest request) throws BizException {
        if (rechargeRouteList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = rechargeRouteDAO.insertList(rechargeRouteList);
        if (rows != rechargeRouteList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, rechargeRouteList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateRechargeRoute(@NonNull Long id, @NonNull RechargeRouteDTO rechargeRouteDTO, HttpServletRequest request) throws BizException {
        log.info("full update rechargeRouteDTO:{}", rechargeRouteDTO);
        RechargeRoute rechargeRoute = BeanUtil.copyProperties(rechargeRouteDTO, new RechargeRoute());
        rechargeRoute.setId(id);
        int cnt = rechargeRouteDAO.update((RechargeRoute) this.packModifyBaseProps(rechargeRoute, request));
        if (cnt != 1) {
            log.error("update error, data:{}", rechargeRouteDTO);
            throw new BizException("update rechargeRoute Error!");
        }
    }

    @Override
    public void update(Map<String, Object> params) throws BizException {
        int cnt = rechargeRouteDAO.updateRecharge(params);
        if (cnt == 0) {
            log.error("update error, data:{}", params);
            throw new BizException("update rechargeRoute Error!");
        }
    }

    @Override
    public void updateRechargeRouteSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        rechargeRouteDAO.updatex(params);
    }

    @Override
    public void logicDeleteRechargeRoute(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = rechargeRouteDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteRechargeRoute(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = rechargeRouteDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public RechargeRouteDTO findRechargeRouteById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        RechargeRouteDTO rechargeRouteDTO = rechargeRouteDAO.selectOneDTO(params);
        return rechargeRouteDTO;
    }

    @Override
    public RechargeRouteDTO findOneRechargeRoute(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        RechargeRoute rechargeRoute = rechargeRouteDAO.selectOne(params);
        RechargeRouteDTO rechargeRouteDTO = new RechargeRouteDTO();
        if (null != rechargeRoute) {
            BeanUtils.copyProperties(rechargeRoute, rechargeRouteDTO);
        }
        return rechargeRouteDTO;
    }

    @Override
    public List<RechargeRouteDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<RechargeRouteDTO> resultList = rechargeRouteDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return rechargeRouteDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return rechargeRouteDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = rechargeRouteDAO.groupCount(conditions);
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
        return rechargeRouteDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = rechargeRouteDAO.groupSum(conditions);
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
