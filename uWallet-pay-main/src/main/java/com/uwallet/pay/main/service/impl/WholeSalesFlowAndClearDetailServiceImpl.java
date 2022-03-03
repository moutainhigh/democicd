package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.WholeSalesFlowAndClearDetailDAO;
import com.uwallet.pay.main.model.dto.WholeSalesFlowAndClearDetailDTO;
import com.uwallet.pay.main.model.entity.WholeSalesFlowAndClearDetail;
import com.uwallet.pay.main.service.WholeSalesFlowAndClearDetailService;
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
 * 整体出售清算中间表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 整体出售清算中间表
 * @author: joker
 * @date: Created in 2020-10-22 09:28:22
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: joker
 */
@Service
@Slf4j
public class WholeSalesFlowAndClearDetailServiceImpl extends BaseServiceImpl implements WholeSalesFlowAndClearDetailService {

    @Autowired
    private WholeSalesFlowAndClearDetailDAO wholeSalesFlowAndClearDetailDAO;

    @Override
    public void saveWholeSalesFlowAndClearDetail(@NonNull WholeSalesFlowAndClearDetailDTO wholeSalesFlowAndClearDetailDTO, HttpServletRequest request) throws BizException {
        WholeSalesFlowAndClearDetail wholeSalesFlowAndClearDetail = BeanUtil.copyProperties(wholeSalesFlowAndClearDetailDTO, new WholeSalesFlowAndClearDetail());
        log.info("save WholeSalesFlowAndClearDetail:{}", wholeSalesFlowAndClearDetail);
        if (wholeSalesFlowAndClearDetailDAO.insert((WholeSalesFlowAndClearDetail) this.packAddBaseProps(wholeSalesFlowAndClearDetail, request)) != 1) {
            log.error("insert error, data:{}", wholeSalesFlowAndClearDetail);
            throw new BizException("Insert wholeSalesFlowAndClearDetail Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWholeSalesFlowAndClearDetailList(@NonNull List<WholeSalesFlowAndClearDetail> wholeSalesFlowAndClearDetailList, HttpServletRequest request) throws BizException {
        if (wholeSalesFlowAndClearDetailList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = wholeSalesFlowAndClearDetailDAO.insertList(wholeSalesFlowAndClearDetailList);
        if (rows != wholeSalesFlowAndClearDetailList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, wholeSalesFlowAndClearDetailList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateWholeSalesFlowAndClearDetail(@NonNull Long id, @NonNull WholeSalesFlowAndClearDetailDTO wholeSalesFlowAndClearDetailDTO, HttpServletRequest request) throws BizException {
        log.info("full update wholeSalesFlowAndClearDetailDTO:{}", wholeSalesFlowAndClearDetailDTO);
        WholeSalesFlowAndClearDetail wholeSalesFlowAndClearDetail = BeanUtil.copyProperties(wholeSalesFlowAndClearDetailDTO, new WholeSalesFlowAndClearDetail());
        wholeSalesFlowAndClearDetail.setId(id);
        int cnt = wholeSalesFlowAndClearDetailDAO.update((WholeSalesFlowAndClearDetail) this.packModifyBaseProps(wholeSalesFlowAndClearDetail, request));
        if (cnt != 1) {
            log.error("update error, data:{}", wholeSalesFlowAndClearDetailDTO);
            throw new BizException("update wholeSalesFlowAndClearDetail Error!");
        }
    }

    @Override
    public void updateWholeSalesFlowAndClearDetailSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        wholeSalesFlowAndClearDetailDAO.updatex(params);
    }

    @Override
    public void logicDeleteWholeSalesFlowAndClearDetail(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = wholeSalesFlowAndClearDetailDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteWholeSalesFlowAndClearDetail(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = wholeSalesFlowAndClearDetailDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public WholeSalesFlowAndClearDetailDTO findWholeSalesFlowAndClearDetailById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        WholeSalesFlowAndClearDetailDTO wholeSalesFlowAndClearDetailDTO = wholeSalesFlowAndClearDetailDAO.selectOneDTO(params);
        return wholeSalesFlowAndClearDetailDTO;
    }

    @Override
    public WholeSalesFlowAndClearDetailDTO findOneWholeSalesFlowAndClearDetail(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        WholeSalesFlowAndClearDetail wholeSalesFlowAndClearDetail = wholeSalesFlowAndClearDetailDAO.selectOne(params);
        WholeSalesFlowAndClearDetailDTO wholeSalesFlowAndClearDetailDTO = new WholeSalesFlowAndClearDetailDTO();
        if (null != wholeSalesFlowAndClearDetail) {
            BeanUtils.copyProperties(wholeSalesFlowAndClearDetail, wholeSalesFlowAndClearDetailDTO);
        }
        return wholeSalesFlowAndClearDetailDTO;
    }

    @Override
    public List<WholeSalesFlowAndClearDetailDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<WholeSalesFlowAndClearDetailDTO> resultList = wholeSalesFlowAndClearDetailDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return wholeSalesFlowAndClearDetailDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return wholeSalesFlowAndClearDetailDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = wholeSalesFlowAndClearDetailDAO.groupCount(conditions);
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
        return wholeSalesFlowAndClearDetailDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = wholeSalesFlowAndClearDetailDAO.groupSum(conditions);
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
