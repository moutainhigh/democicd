package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.TrusteeDAO;
import com.uwallet.pay.main.model.dto.TrusteeDTO;
import com.uwallet.pay.main.model.entity.Trustee;
import com.uwallet.pay.main.service.TrusteeService;
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
 * 受托人信息表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 受托人信息表
 * @author: baixinyue
 * @date: Created in 2020-04-21 14:25:22
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class TrusteeServiceImpl extends BaseServiceImpl implements TrusteeService {

    @Autowired
    private TrusteeDAO trusteeDAO;

    @Override
    public void saveTrustee(@NonNull TrusteeDTO trusteeDTO, HttpServletRequest request) throws BizException {
        Trustee trustee = BeanUtil.copyProperties(trusteeDTO, new Trustee());
        log.info("save Trustee:{}", trustee);
        if (trusteeDAO.insert((Trustee) this.packAddBaseProps(trustee, request)) != 1) {
            log.error("insert error, data:{}", trustee);
            throw new BizException("Insert trustee Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTrusteeList(@NonNull List<Trustee> trusteeList, HttpServletRequest request) throws BizException {
        if (trusteeList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = trusteeDAO.insertList(trusteeList);
        if (rows != trusteeList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, trusteeList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateTrustee(@NonNull Long id, @NonNull TrusteeDTO trusteeDTO, HttpServletRequest request) throws BizException {
        log.info("full update trusteeDTO:{}", trusteeDTO);
        Trustee trustee = BeanUtil.copyProperties(trusteeDTO, new Trustee());
        trustee.setId(id);
        int cnt = trusteeDAO.update((Trustee) this.packModifyBaseProps(trustee, request));
        if (cnt != 1) {
            log.error("update error, data:{}", trusteeDTO);
            throw new BizException("update trustee Error!");
        }
    }

    @Override
    public void updateTrusteeSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        trusteeDAO.updatex(params);
    }

    @Override
    public void logicDeleteTrustee(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = trusteeDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteTrustee(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = trusteeDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public TrusteeDTO findTrusteeById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        TrusteeDTO trusteeDTO = trusteeDAO.selectOneDTO(params);
        return trusteeDTO;
    }

    @Override
    public TrusteeDTO findOneTrustee(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        Trustee trustee = trusteeDAO.selectOne(params);
        TrusteeDTO trusteeDTO = new TrusteeDTO();
        if (null != trustee) {
            BeanUtils.copyProperties(trustee, trusteeDTO);
        }
        return trusteeDTO;
    }

    @Override
    public List<TrusteeDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<TrusteeDTO> resultList = trusteeDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return trusteeDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return trusteeDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = trusteeDAO.groupCount(conditions);
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
        return trusteeDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = trusteeDAO.groupSum(conditions);
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
    public void deleteTrusteeByMerchantId(Long merchantId) {
        log.info("delete trustee by merchantId, merchantId:{}", merchantId);
        int i = 0;
        try {
            i = trusteeDAO.deleteTrusteeByMerchantId(merchantId);
        } catch (Exception e) {
            log.info("delete trustee by merchantId failed, merchantId:{}, error message:{}, e:{}", merchantId, e.getMessage(), e);
        }
        log.info("delete trustee by merchantId successful, merchantId:{}", merchantId);
    }
}
