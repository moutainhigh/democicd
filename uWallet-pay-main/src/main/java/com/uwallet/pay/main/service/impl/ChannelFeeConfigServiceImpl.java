package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.ChannelFeeConfigDAO;
import com.uwallet.pay.main.model.dto.ChannelFeeConfigDTO;
import com.uwallet.pay.main.model.entity.ChannelFeeConfig;
import com.uwallet.pay.main.service.ChannelFeeConfigService;
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
 * 通道手续费配置表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 通道手续费配置表
 * @author: zhoutt
 * @date: Created in 2021-03-08 10:12:26
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class ChannelFeeConfigServiceImpl extends BaseServiceImpl implements ChannelFeeConfigService {

    @Autowired
    private ChannelFeeConfigDAO channelFeeConfigDAO;

    @Override
    public void saveChannelFeeConfig(@NonNull ChannelFeeConfigDTO channelFeeConfigDTO, HttpServletRequest request) throws BizException {
        ChannelFeeConfig channelFeeConfig = BeanUtil.copyProperties(channelFeeConfigDTO, new ChannelFeeConfig());
        log.info("save ChannelFeeConfig:{}", channelFeeConfig);
        if (channelFeeConfigDAO.insert((ChannelFeeConfig) this.packAddBaseProps(channelFeeConfig, request)) != 1) {
            log.error("insert error, data:{}", channelFeeConfig);
            throw new BizException("Insert channelFeeConfig Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveChannelFeeConfigList(@NonNull List<ChannelFeeConfig> channelFeeConfigList, HttpServletRequest request) throws BizException {
        if (channelFeeConfigList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = channelFeeConfigDAO.insertList(channelFeeConfigList);
        if (rows != channelFeeConfigList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, channelFeeConfigList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateChannelFeeConfig(@NonNull Long id, @NonNull ChannelFeeConfigDTO channelFeeConfigDTO, HttpServletRequest request) throws BizException {
        log.info("full update channelFeeConfigDTO:{}", channelFeeConfigDTO);
        ChannelFeeConfig channelFeeConfig = BeanUtil.copyProperties(channelFeeConfigDTO, new ChannelFeeConfig());
        channelFeeConfig.setId(id);
        int cnt = channelFeeConfigDAO.update((ChannelFeeConfig) this.packModifyBaseProps(channelFeeConfig, request));
        if (cnt != 1) {
            log.error("update error, data:{}", channelFeeConfigDTO);
            throw new BizException("update channelFeeConfig Error!");
        }
    }

    @Override
    public void updateChannelFeeConfigSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        channelFeeConfigDAO.updatex(params);
    }

    @Override
    public void logicDeleteChannelFeeConfig(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = channelFeeConfigDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteChannelFeeConfig(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = channelFeeConfigDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public ChannelFeeConfigDTO findChannelFeeConfigById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        ChannelFeeConfigDTO channelFeeConfigDTO = channelFeeConfigDAO.selectOneDTO(params);
        return channelFeeConfigDTO;
    }

    @Override
    public ChannelFeeConfigDTO findOneChannelFeeConfig(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        ChannelFeeConfig channelFeeConfig = channelFeeConfigDAO.selectOne(params);
        ChannelFeeConfigDTO channelFeeConfigDTO = new ChannelFeeConfigDTO();
        if (null != channelFeeConfig) {
            BeanUtils.copyProperties(channelFeeConfig, channelFeeConfigDTO);
        }
        return channelFeeConfigDTO;
    }

    @Override
    public List<ChannelFeeConfigDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<ChannelFeeConfigDTO> resultList = channelFeeConfigDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return channelFeeConfigDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return channelFeeConfigDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = channelFeeConfigDAO.groupCount(conditions);
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
        return channelFeeConfigDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = channelFeeConfigDAO.groupSum(conditions);
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
