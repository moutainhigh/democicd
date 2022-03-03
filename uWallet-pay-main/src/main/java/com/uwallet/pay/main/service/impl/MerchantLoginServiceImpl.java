package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.MerchantLoginDAO;
import com.uwallet.pay.main.model.dto.MerchantLoginDTO;
import com.uwallet.pay.main.model.entity.MerchantLogin;
import com.uwallet.pay.main.service.MerchantLoginService;
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
 * 用户主表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 用户主表
 * @author: zhoutt
 * @date: Created in 2020-02-21 16:19:27
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class MerchantLoginServiceImpl extends BaseServiceImpl implements MerchantLoginService {

    @Autowired
    private MerchantLoginDAO merchantLoginDAO;

    @Override
    public Long saveMerchantLogin(@NonNull MerchantLoginDTO merchantLoginDTO, HttpServletRequest request) throws BizException {
        MerchantLogin merchantLogin = BeanUtil.copyProperties(merchantLoginDTO, new MerchantLogin());
        log.info("save MerchantLogin:{}", merchantLogin);
        MerchantLogin merchantLogin_ = (MerchantLogin) this.packAddBaseProps(merchantLogin, request);
        if (merchantLoginDAO.insert(merchantLogin_) != 1) {
            log.error("insert error, data:{}", merchantLogin);
            throw new BizException("Insert merchantLogin Error!");
        }
        return merchantLogin_.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMerchantLoginList(@NonNull List<MerchantLogin> merchantLoginList, HttpServletRequest request) throws BizException {
        if (merchantLoginList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = merchantLoginDAO.insertList(merchantLoginList);
        if (rows != merchantLoginList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, merchantLoginList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateMerchantLogin(@NonNull Long id, @NonNull MerchantLoginDTO merchantLoginDTO, HttpServletRequest request) throws BizException {
        log.info("full update merchantLoginDTO:{}", merchantLoginDTO);
        MerchantLogin merchantLogin = BeanUtil.copyProperties(merchantLoginDTO, new MerchantLogin());
        merchantLogin.setId(id);
        int cnt = merchantLoginDAO.update((MerchantLogin) this.packModifyBaseProps(merchantLogin, request));
        if (cnt != 1) {
            log.error("update error, data:{}", merchantLoginDTO);
            throw new BizException("update merchantLogin Error!");
        }
    }

    @Override
    public void updateMerchantLoginSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        merchantLoginDAO.updatex(params);
    }

    @Override
    public void logicDeleteMerchantLogin(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = merchantLoginDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteMerchantLogin(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = merchantLoginDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public MerchantLoginDTO findMerchantLoginById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        MerchantLoginDTO merchantLoginDTO = merchantLoginDAO.selectOneDTO(params);
        return merchantLoginDTO;
    }

    @Override
    public MerchantLoginDTO findOneMerchantLogin(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        MerchantLogin merchantLogin = merchantLoginDAO.selectOne(params);
        MerchantLoginDTO merchantLoginDTO = new MerchantLoginDTO();
        if (null != merchantLogin) {
            BeanUtils.copyProperties(merchantLogin, merchantLoginDTO);
        }
        return merchantLoginDTO;
    }

    @Override
    public List<MerchantLoginDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<MerchantLoginDTO> resultList = merchantLoginDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return merchantLoginDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return merchantLoginDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = merchantLoginDAO.groupCount(conditions);
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
        return merchantLoginDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = merchantLoginDAO.groupSum(conditions);
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
    public List<MerchantLoginDTO> findByEmail(Map<String, Object> params) {
        return merchantLoginDAO.findByEmail(params);
    }

}
