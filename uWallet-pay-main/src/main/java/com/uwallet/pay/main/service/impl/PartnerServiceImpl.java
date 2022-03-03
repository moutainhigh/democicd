package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.PartnerDAO;
import com.uwallet.pay.main.model.dto.PartnerDTO;
import com.uwallet.pay.main.model.entity.Partner;
import com.uwallet.pay.main.service.PartnerService;
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
 * 合伙人信息表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 合伙人信息表
 * @author: Rainc
 * @date: Created in 2019-12-16 10:55:17
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Rainc
 */
@Service
@Slf4j
public class PartnerServiceImpl extends BaseServiceImpl implements PartnerService {

    @Autowired
    private PartnerDAO partnerDAO;

    @Override
    public void savePartner(@NonNull PartnerDTO partnerDTO, HttpServletRequest request) throws BizException {
        Partner partner = BeanUtil.copyProperties(partnerDTO, new Partner());
        log.info("save Partner:{}", partner);
        if (partnerDAO.insert((Partner) this.packAddBaseProps(partner, request)) != 1) {
            log.error("insert error, data:{}", partner);
            throw new BizException("Insert partner Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePartnerList(@NonNull List<Partner> partnerList, HttpServletRequest request) throws BizException {
        if (partnerList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = partnerDAO.insertList(partnerList);
        if (rows != partnerList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, partnerList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updatePartner(@NonNull Long id, @NonNull PartnerDTO partnerDTO, HttpServletRequest request) throws BizException {
        log.info("full update partnerDTO:{}", partnerDTO);
        Partner partner = BeanUtil.copyProperties(partnerDTO, new Partner());
        partner.setId(id);
        int cnt = partnerDAO.update((Partner) this.packModifyBaseProps(partner, request));
        if (cnt != 1) {
            log.error("update error, data:{}", partnerDTO);
            throw new BizException("update partner Error!");
        }
    }

    @Override
    public void updatePartnerSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        partnerDAO.updatex(params);
    }

    @Override
    public void logicDeletePartner(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = partnerDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deletePartner(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = partnerDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public PartnerDTO findPartnerById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        PartnerDTO partnerDTO = partnerDAO.selectOneDTO(params);
        return partnerDTO;
    }

    @Override
    public PartnerDTO findOnePartner(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        Partner partner = partnerDAO.selectOne(params);
        PartnerDTO partnerDTO = new PartnerDTO();
        if (null != partner) {
            BeanUtils.copyProperties(partner, partnerDTO);
        }
        return partnerDTO;
    }

    @Override
    public List<PartnerDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<PartnerDTO> resultList = partnerDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return partnerDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return partnerDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = partnerDAO.groupCount(conditions);
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
        return partnerDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = partnerDAO.groupSum(conditions);
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
    public void deletePartnerByMerchantId(Long merchantId) {
        log.info("delete partner by merchantId, merchantId:{}", merchantId);
        int i = 0;
        try {
            i = partnerDAO.deletePartnerByMerchantId(merchantId);
        } catch (Exception e) {
            log.info("delete partner by merchantId failed, merchantId:{}, error message:{}, e:{}", merchantId, e.getMessage(), e);
        }
        log.info("delete partner by merchantId successful, merchantId:{}", merchantId);
    }
}
