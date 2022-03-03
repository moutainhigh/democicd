package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.SecondMerchantGatewayInfoDAO;
import com.uwallet.pay.main.model.dto.SecondMerchantGatewayInfoDTO;
import com.uwallet.pay.main.model.entity.SecondMerchantGatewayInfo;
import com.uwallet.pay.main.service.SecondMerchantGatewayInfoService;
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
import java.util.stream.Collectors;

/**
 * <p>
 * 二级商户渠道信息表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 二级商户渠道信息表
 * @author: baixinyue
 * @date: Created in 2019-12-26 17:02:13
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class SecondMerchantGatewayInfoServiceImpl extends BaseServiceImpl implements SecondMerchantGatewayInfoService {

    @Autowired
    private SecondMerchantGatewayInfoDAO secondMerchantGatewayInfoDAO;

    @Override
    public void saveSecondMerchantGatewayInfo(@NonNull SecondMerchantGatewayInfoDTO secondMerchantGatewayInfoDTO, HttpServletRequest request) throws BizException {
        SecondMerchantGatewayInfo secondMerchantGatewayInfo = BeanUtil.copyProperties(secondMerchantGatewayInfoDTO, new SecondMerchantGatewayInfo());
        log.info("save SecondMerchantGatewayInfo:{}", secondMerchantGatewayInfo);
        if (secondMerchantGatewayInfoDAO.insert((SecondMerchantGatewayInfo) this.packAddBaseProps(secondMerchantGatewayInfo, request)) != 1) {
            log.error("insert error, data:{}", secondMerchantGatewayInfo);
            throw new BizException("Insert secondMerchantGatewayInfo Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSecondMerchantGatewayInfoList(@NonNull List<SecondMerchantGatewayInfo> secondMerchantGatewayInfoList, HttpServletRequest request) throws BizException {
        if (secondMerchantGatewayInfoList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = secondMerchantGatewayInfoDAO.insertList(secondMerchantGatewayInfoList);
        if (rows != secondMerchantGatewayInfoList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, secondMerchantGatewayInfoList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateSecondMerchantGatewayInfo(@NonNull Long id, @NonNull SecondMerchantGatewayInfoDTO secondMerchantGatewayInfoDTO, HttpServletRequest request) throws BizException {
        log.info("full update secondMerchantGatewayInfoDTO:{}", secondMerchantGatewayInfoDTO);
        SecondMerchantGatewayInfo secondMerchantGatewayInfo = BeanUtil.copyProperties(secondMerchantGatewayInfoDTO, new SecondMerchantGatewayInfo());
        secondMerchantGatewayInfo.setId(id);
        int cnt = secondMerchantGatewayInfoDAO.update((SecondMerchantGatewayInfo) this.packModifyBaseProps(secondMerchantGatewayInfo, request));
        if (cnt != 1) {
            log.error("update error, data:{}", secondMerchantGatewayInfoDTO);
            throw new BizException("update secondMerchantGatewayInfo Error!");
        }
    }

    @Override
    public void updateSecondMerchantGatewayInfoSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        secondMerchantGatewayInfoDAO.updatex(params);
    }

    @Override
    public void logicDeleteSecondMerchantGatewayInfo(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = secondMerchantGatewayInfoDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteSecondMerchantGatewayInfo(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = secondMerchantGatewayInfoDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public SecondMerchantGatewayInfoDTO findSecondMerchantGatewayInfoById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        SecondMerchantGatewayInfoDTO secondMerchantGatewayInfoDTO = secondMerchantGatewayInfoDAO.selectOneDTO(params);
        return secondMerchantGatewayInfoDTO;
    }

    @Override
    public SecondMerchantGatewayInfoDTO findOneSecondMerchantGatewayInfo(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        SecondMerchantGatewayInfo secondMerchantGatewayInfo = secondMerchantGatewayInfoDAO.selectOne(params);
        SecondMerchantGatewayInfoDTO secondMerchantGatewayInfoDTO = new SecondMerchantGatewayInfoDTO();
        if (null != secondMerchantGatewayInfo) {
            BeanUtils.copyProperties(secondMerchantGatewayInfo, secondMerchantGatewayInfoDTO);
        }
        return secondMerchantGatewayInfoDTO;
    }

    @Override
    public List<SecondMerchantGatewayInfoDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<SecondMerchantGatewayInfoDTO> resultList = secondMerchantGatewayInfoDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return secondMerchantGatewayInfoDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return secondMerchantGatewayInfoDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = secondMerchantGatewayInfoDAO.groupCount(conditions);
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
        return secondMerchantGatewayInfoDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = secondMerchantGatewayInfoDAO.groupSum(conditions);
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
