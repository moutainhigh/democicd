package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.AccessMerchantDAO;
import com.uwallet.pay.main.model.dto.AccessMerchantDTO;
import com.uwallet.pay.main.model.dto.AccessPlatformInfoDTO;
import com.uwallet.pay.main.model.entity.AccessMerchant;
import com.uwallet.pay.main.service.AccessMerchantService;
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
 * 接入方商户表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 接入方商户表
 * @author: zhoutt
 * @date: Created in 2020-09-25 08:55:53
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class AccessMerchantServiceImpl extends BaseServiceImpl implements AccessMerchantService {

    @Autowired
    private AccessMerchantDAO accessMerchantDAO;

    @Override
    public Long saveAccessMerchant(@NonNull AccessMerchantDTO accessMerchantDTO, HttpServletRequest request) throws BizException {
        AccessMerchant accessMerchant = BeanUtil.copyProperties(accessMerchantDTO, new AccessMerchant());
        log.info("save AccessMerchant:{}", accessMerchant);
        accessMerchant = (AccessMerchant) this.packAddBaseProps(accessMerchant, request);
        if (accessMerchantDAO.insert(accessMerchant) != 1) {
            log.error("insert error, data:{}", accessMerchant);
            throw new BizException("Insert accessMerchant Error!");
        }
        return  accessMerchant.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAccessMerchantList(@NonNull List<AccessMerchant> accessMerchantList, HttpServletRequest request) throws BizException {
        if (accessMerchantList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = accessMerchantDAO.insertList(accessMerchantList);
        if (rows != accessMerchantList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, accessMerchantList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateAccessMerchant(@NonNull Long id, @NonNull AccessMerchantDTO accessMerchantDTO, HttpServletRequest request) throws BizException {
        log.info("full update accessMerchantDTO:{}", accessMerchantDTO);
        AccessMerchant accessMerchant = BeanUtil.copyProperties(accessMerchantDTO, new AccessMerchant());
        accessMerchant.setId(id);
        int cnt = accessMerchantDAO.update((AccessMerchant) this.packModifyBaseProps(accessMerchant, request));
        if (cnt != 1) {
            log.error("update error, data:{}", accessMerchantDTO);
            throw new BizException("update accessMerchant Error!");
        }
    }

    @Override
    public void updateAccessMerchantSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        accessMerchantDAO.updatex(params);
    }

    @Override
    public void logicDeleteAccessMerchant(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = accessMerchantDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteAccessMerchant(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = accessMerchantDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public AccessMerchantDTO findAccessMerchantById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AccessMerchantDTO accessMerchantDTO = accessMerchantDAO.selectOneDTO(params);
        return accessMerchantDTO;
    }

    @Override
    public AccessMerchantDTO findOneAccessMerchant(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        AccessMerchant accessMerchant = accessMerchantDAO.selectOne(params);
        AccessMerchantDTO accessMerchantDTO = new AccessMerchantDTO();
        if (null != accessMerchant) {
            BeanUtils.copyProperties(accessMerchant, accessMerchantDTO);
        }
        return accessMerchantDTO;
    }

    @Override
    public List<AccessMerchantDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<AccessMerchantDTO> resultList = accessMerchantDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return accessMerchantDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return accessMerchantDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = accessMerchantDAO.groupCount(conditions);
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
        return accessMerchantDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = accessMerchantDAO.groupSum(conditions);
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
    public List<AccessPlatformInfoDTO> getAccessMerchantList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        log.info("params:"+params);
        List<AccessPlatformInfoDTO> resultList = accessMerchantDAO.getAccessMerchantList(params);
        return resultList;
    }

    @Override
    public int getAccessMerchantListCount(Map<String, Object> params) {
        return accessMerchantDAO.getAccessMerchantListCount(params);
    }

    @Override
    public AccessPlatformInfoDTO getOne(Long id) {
        return null;
    }



}
