package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.MarketingLogDAO;
import com.uwallet.pay.main.model.dto.MarketingLogDTO;
import com.uwallet.pay.main.model.entity.MarketingLog;
import com.uwallet.pay.main.service.MarketingLogService;
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
 *
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description:
 * @author: xucl
 * @date: Created in 2021-04-26 16:00:46
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Service
@Slf4j
public class MarketingLogServiceImpl extends BaseServiceImpl implements MarketingLogService {

    @Autowired
    private MarketingLogDAO marketingLogDAO;

    @Override
    public void saveMarketingLog(@NonNull MarketingLogDTO marketingLogDTO, HttpServletRequest request) throws BizException {
        MarketingLog marketingLog = BeanUtil.copyProperties(marketingLogDTO, new MarketingLog());
        log.info("save MarketingLog:{}", marketingLog);
        if (marketingLogDAO.insert((MarketingLog) this.packAddBaseProps(marketingLog, request)) != 1) {
            log.error("insert error, data:{}", marketingLog);
            throw new BizException("Insert marketingLog Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMarketingLogList(@NonNull List<MarketingLog> marketingLogList, HttpServletRequest request) throws BizException {
        if (marketingLogList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = marketingLogDAO.insertList(marketingLogList);
        if (rows != marketingLogList.size()) {
            log.error("??????????????????????????????({})????????????({})?????????", rows, marketingLogList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateMarketingLog(@NonNull Long id, @NonNull MarketingLogDTO marketingLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update marketingLogDTO:{}", marketingLogDTO);
        MarketingLog marketingLog = BeanUtil.copyProperties(marketingLogDTO, new MarketingLog());
        marketingLog.setId(id);
        int cnt = marketingLogDAO.update((MarketingLog) this.packModifyBaseProps(marketingLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", marketingLogDTO);
            throw new BizException("update marketingLog Error!");
        }
    }

    @Override
    public void updateMarketingLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        marketingLogDAO.updatex(params);
    }

    @Override
    public void logicDeleteMarketingLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("?????????????????????id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = marketingLogDAO.delete(params);
        if (rows != 1) {
            log.error("??????????????????, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteMarketingLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("????????????, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = marketingLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("????????????, ???????????????{}?????????", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public MarketingLogDTO findMarketingLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        MarketingLogDTO marketingLogDTO = marketingLogDAO.selectOneDTO(params);
        return marketingLogDTO;
    }

    @Override
    public MarketingLogDTO findOneMarketingLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        MarketingLog marketingLog = marketingLogDAO.selectOne(params);
        MarketingLogDTO marketingLogDTO = new MarketingLogDTO();
        if (null != marketingLog) {
            BeanUtils.copyProperties(marketingLog, marketingLogDTO);
        }
        return marketingLogDTO;
    }

    @Override
    public List<MarketingLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MarketingLogDTO> resultList = marketingLogDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns???????????????0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return marketingLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return marketingLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = marketingLogDAO.groupCount(conditions);
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
        return marketingLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = marketingLogDAO.groupSum(conditions);
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
    public MarketingLogDTO findMaxTime(Map<String, Object> params) {
        MarketingLogDTO marketingLogDTO=marketingLogDAO.findMaxTime(params);
        return marketingLogDTO;
    }

}
