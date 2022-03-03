package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.BeneficiaryDAO;
import com.uwallet.pay.main.model.dto.BeneficiaryDTO;
import com.uwallet.pay.main.model.entity.Beneficiary;
import com.uwallet.pay.main.service.BeneficiaryService;
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
 * 受益人信息表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 受益人信息表
 * @author: Rainc
 * @date: Created in 2019-12-11 16:51:43
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Rainc
 */
@Service
@Slf4j
public class BeneficiaryServiceImpl extends BaseServiceImpl implements BeneficiaryService {

    @Autowired
    private BeneficiaryDAO beneficiaryDAO;

    @Override
    public void saveBeneficiary(@NonNull BeneficiaryDTO beneficiaryDTO, HttpServletRequest request) throws BizException {
        Beneficiary beneficiary = BeanUtil.copyProperties(beneficiaryDTO, new Beneficiary());
        log.info("save Beneficiary:{}", beneficiary);
        if (beneficiaryDAO.insert((Beneficiary) this.packAddBaseProps(beneficiary, request)) != 1) {
            log.error("insert error, data:{}", beneficiary);
            throw new BizException("Insert beneficiary Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBeneficiaryList(@NonNull List<Beneficiary> beneficiaryList, HttpServletRequest request) throws BizException {
        if (beneficiaryList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = beneficiaryDAO.insertList(beneficiaryList);
        if (rows != beneficiaryList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, beneficiaryList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateBeneficiary(@NonNull Long id, @NonNull BeneficiaryDTO beneficiaryDTO, HttpServletRequest request) throws BizException {
        log.info("full update beneficiaryDTO:{}", beneficiaryDTO);
        Beneficiary beneficiary = BeanUtil.copyProperties(beneficiaryDTO, new Beneficiary());
        beneficiary.setId(id);
        int cnt = beneficiaryDAO.update((Beneficiary) this.packModifyBaseProps(beneficiary, request));
        if (cnt != 1) {
            log.error("update error, data:{}", beneficiaryDTO);
            throw new BizException("update beneficiary Error!");
        }
    }

    @Override
    public void updateBeneficiarySelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        beneficiaryDAO.updatex(params);
    }

    @Override
    public void logicDeleteBeneficiary(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = beneficiaryDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteBeneficiary(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = beneficiaryDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public BeneficiaryDTO findBeneficiaryById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        BeneficiaryDTO beneficiaryDTO = beneficiaryDAO.selectOneDTO(params);
        return beneficiaryDTO;
    }

    @Override
    public BeneficiaryDTO findOneBeneficiary(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        Beneficiary beneficiary = beneficiaryDAO.selectOne(params);
        BeneficiaryDTO beneficiaryDTO = new BeneficiaryDTO();
        if (null != beneficiary) {
            BeanUtils.copyProperties(beneficiary, beneficiaryDTO);
        }
        return beneficiaryDTO;
    }

    @Override
    public List<BeneficiaryDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<BeneficiaryDTO> resultList = beneficiaryDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return beneficiaryDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return beneficiaryDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = beneficiaryDAO.groupCount(conditions);
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
        return beneficiaryDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = beneficiaryDAO.groupSum(conditions);
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
    public void deleteBeneficiaryByMerchantId(Long merchantId) {
        log.info("delete beneficiary by merchantId, merchantId:{}", merchantId);
        int i = 0;
        try {
            i = beneficiaryDAO.deleteBeneficiaryByMerchantId(merchantId);
        } catch (Exception e) {
            log.info("delete beneficiary by merchantId failed, merchantId:{}, error message:{}, e:{}", merchantId, e.getMessage(), e);
        }
        log.info("delete beneficiary by merchantId successful, merchantId:{}", merchantId);
    }
}
