package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.ChannelLimitDAO;
import com.uwallet.pay.main.model.dto.ChannelLimitDTO;
import com.uwallet.pay.main.model.entity.ChannelLimit;
import com.uwallet.pay.main.service.ChannelLimitService;
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
 * 渠道日交易累计金额记录表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 渠道日交易累计金额记录表
 * @author: baixinyue
 * @date: Created in 2019-12-21 10:06:05
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class ChannelLimitServiceImpl extends BaseServiceImpl implements ChannelLimitService {

    @Autowired
    private ChannelLimitDAO channelLimitDAO;

    @Override
    public void saveChannelLimit(@NonNull ChannelLimitDTO channelLimitDTO, HttpServletRequest request) throws BizException {
        ChannelLimit channelLimit = BeanUtil.copyProperties(channelLimitDTO, new ChannelLimit());
        log.info("save ChannelLimit:{}", channelLimit);
        if (channelLimitDAO.insert((ChannelLimit) this.packAddBaseProps(channelLimit, request)) != 1) {
            log.error("insert error, data:{}", channelLimit);
            throw new BizException("Insert channelLimit Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveChannelLimitList(@NonNull List<ChannelLimit> channelLimitList, HttpServletRequest request) throws BizException {
        if (channelLimitList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = channelLimitDAO.insertList(channelLimitList);
        if (rows != channelLimitList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, channelLimitList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateChannelLimit(@NonNull Long id, @NonNull ChannelLimitDTO channelLimitDTO, HttpServletRequest request) throws BizException {
        log.info("full update channelLimitDTO:{}", channelLimitDTO);
        ChannelLimit channelLimit = BeanUtil.copyProperties(channelLimitDTO, new ChannelLimit());
        if (request != null) {
            channelLimit = (ChannelLimit) this.packModifyBaseProps(channelLimit, request);
        } else {
            channelLimit.setModifiedDate(System.currentTimeMillis());
        }
        channelLimit.setId(id);
        int cnt = channelLimitDAO.update(channelLimit);
        if (cnt != 1) {
            log.error("update error, data:{}", channelLimitDTO);
            throw new BizException("update channelLimit Error!");
        }
    }

    @Override
    public void updateChannelLimitSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        channelLimitDAO.updatex(params);
    }

    @Override
    public void logicDeleteChannelLimit(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = channelLimitDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteChannelLimit(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = channelLimitDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ChannelLimitDTO findChannelLimitById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ChannelLimitDTO channelLimitDTO = channelLimitDAO.selectOneDTO(params);
        return channelLimitDTO;
    }

    @Override
    public ChannelLimitDTO findOneChannelLimit(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        ChannelLimit channelLimit = channelLimitDAO.selectOne(params);
        ChannelLimitDTO channelLimitDTO = new ChannelLimitDTO();
        if (null != channelLimit) {
            BeanUtils.copyProperties(channelLimit, channelLimitDTO);
        }
        return channelLimitDTO;
    }

    @Override
    public List<ChannelLimitDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ChannelLimitDTO> resultList = channelLimitDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return channelLimitDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return channelLimitDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = channelLimitDAO.groupCount(conditions);
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
        return channelLimitDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = channelLimitDAO.groupSum(conditions);
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
    public void updateAmount(ChannelLimit channelLimit, HttpServletRequest request) throws BizException {
        log.info("update channelLimit, data:{}", channelLimit);
        if (request != null) {
            channelLimit = (ChannelLimit) this.packModifyBaseProps(channelLimit, request);
        } else {
            channelLimit.setModifiedDate(System.currentTimeMillis());
        }
        channelLimit.setIp("1");
        int cnt = channelLimitDAO.updateAmount(channelLimit);
        if (cnt != 1) {
            log.error("update error, data:{}", channelLimit);
            throw new BizException("update channelLimit Error!");
        }
    }

    @Override
    public void channelLimitRollback(Map<String, Object> map) {
        channelLimitDAO.channelLimitRollback(map);
    }

}
