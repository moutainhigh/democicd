package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.TopDealDAO;
import com.uwallet.pay.main.model.dto.TopDealDTO;
import com.uwallet.pay.main.model.entity.TopDeal;
import com.uwallet.pay.main.service.TopDealService;
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
 * top deal
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: top deal
 * @author: zhoutt
 * @date: Created in 2020-03-12 14:34:50
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class TopDealServiceImpl extends BaseServiceImpl implements TopDealService {

    @Autowired
    private TopDealDAO topDealDAO;

    @Override
    public void saveTopDeal(@NonNull TopDealDTO topDealDTO, HttpServletRequest request) throws BizException {
        // 先查询上架的deal数量是否够4个
        Map<String, Object> params = new HashMap<>(1);
        params.put("state", 1);
        if (count(params) == StaticDataEnum.TOP_DEAL_UPPER_LIMIT.getCode()) {
            throw new BizException(I18nUtils.get("banner.enough", getLang(request)));
        }
        // 查询最大排序数
        Integer maxSort = topDealDAO.selectMaxSort();
        if (maxSort != null) {
            topDealDTO.setSort(maxSort += 1);
        } else {
            topDealDTO.setSort(1);
        }
        TopDeal topDeal = BeanUtil.copyProperties(topDealDTO, new TopDeal());
        log.info("save TopDeal:{}", topDeal);
        if (topDealDAO.insert((TopDeal) this.packAddBaseProps(topDeal, request)) != 1) {
            log.error("insert error, data:{}", topDeal);
            throw new BizException("Insert topDeal Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTopDealList(@NonNull List<TopDeal> topDealList, HttpServletRequest request) throws BizException {
        if (topDealList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = topDealDAO.insertList(topDealList);
        if (rows != topDealList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, topDealList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateTopDeal(@NonNull Long id, @NonNull TopDealDTO topDealDTO, HttpServletRequest request) throws BizException {
        log.info("full update topDealDTO:{}", topDealDTO);
        TopDeal topDeal = BeanUtil.copyProperties(topDealDTO, new TopDeal());
        topDeal.setId(id);
        int cnt = topDealDAO.update((TopDeal) this.packModifyBaseProps(topDeal, request));
        if (cnt != 1) {
            log.error("update error, data:{}", topDealDTO);
            throw new BizException("update topDeal Error!");
        }
    }

    @Override
    public void updateTopDealSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        topDealDAO.updatex(params);
    }

    @Override
    public void logicDeleteTopDeal(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = topDealDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteTopDeal(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = topDealDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public TopDealDTO findTopDealById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        TopDealDTO topDealDTO = topDealDAO.selectOneDTO(params);
        return topDealDTO;
    }

    @Override
    public TopDealDTO findOneTopDeal(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        TopDeal topDeal = topDealDAO.selectOne(params);
        TopDealDTO topDealDTO = new TopDealDTO();
        if (null != topDeal) {
            BeanUtils.copyProperties(topDeal, topDealDTO);
        }
        return topDealDTO;
    }

    @Override
    public List<TopDealDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<TopDealDTO> resultList = topDealDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return topDealDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return topDealDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = topDealDAO.groupCount(conditions);
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
        return topDealDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = topDealDAO.groupSum(conditions);
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
    @Transactional(rollbackFor = Exception.class)
    public void shiftUpOrDown(Long id, Integer upOrDown, HttpServletRequest request) throws BizException {
        log.info("移动操作");
        TopDealDTO original = findTopDealById(id);
        TopDealDTO passivity = topDealDAO.shiftUpOrDown(id, upOrDown);

        if (passivity == null) {
            log.info("移动操作失败");
            throw new BizException(I18nUtils.get("move.failed=", getLang(request)));
        }

        Integer originalSort = original.getSort();
        original.setSort(passivity.getSort());
        passivity.setSort(originalSort);

        updateTopDeal(id, original, request);
        updateTopDeal(passivity.getId(), passivity, request);

    }

    @Override
    public List<TopDealDTO> getToDealsImg() {
        return topDealDAO.getTopDealImg();
    }

}
