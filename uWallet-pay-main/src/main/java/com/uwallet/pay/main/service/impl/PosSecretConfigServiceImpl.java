package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.PosSecretConfigDAO;
import com.uwallet.pay.main.model.dto.PosSecretConfigDTO;
import com.uwallet.pay.main.model.entity.PosSecretConfig;
import com.uwallet.pay.main.service.PosSecretConfigService;
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
 * pos商户秘钥配置表
 * </p>
 *
 * @package: com.fenmi.generator.service.impl
 * @description: pos商户秘钥配置表
 * @author: zhangzeyuan
 * @date: Created in 2021-03-24 14:32:28
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Service
@Slf4j
public class PosSecretConfigServiceImpl extends BaseServiceImpl implements PosSecretConfigService {

    @Autowired
    private PosSecretConfigDAO posSecretConfigDAO;

    @Override
    public void savePosSecretConfig(@NonNull PosSecretConfigDTO posSecretConfigDTO, HttpServletRequest request) throws BizException {
        PosSecretConfig posSecretConfig = BeanUtil.copyProperties(posSecretConfigDTO, new PosSecretConfig());
        log.info("save PosSecretConfig:{}", posSecretConfig);
        if (posSecretConfigDAO.insert((PosSecretConfig) this.packAddBaseProps(posSecretConfig, request)) != 1) {
            log.error("insert error, data:{}", posSecretConfig);
            throw new BizException("Insert posSecretConfig Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePosSecretConfigList(@NonNull List<PosSecretConfig> posSecretConfigList, HttpServletRequest request) throws BizException {
        if (posSecretConfigList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = posSecretConfigDAO.insertList(posSecretConfigList);
        if (rows != posSecretConfigList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, posSecretConfigList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updatePosSecretConfig(@NonNull Long id, @NonNull PosSecretConfigDTO posSecretConfigDTO, HttpServletRequest request) throws BizException {
        log.info("full update posSecretConfigDTO:{}", posSecretConfigDTO);
        PosSecretConfig posSecretConfig = BeanUtil.copyProperties(posSecretConfigDTO, new PosSecretConfig());
        posSecretConfig.setId(id);
        int cnt = posSecretConfigDAO.update((PosSecretConfig) this.packModifyBaseProps(posSecretConfig, request));
        if (cnt != 1) {
            log.error("update error, data:{}", posSecretConfigDTO);
            throw new BizException("update posSecretConfig Error!");
        }
    }

    @Override
    public void updatePosSecretConfigSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        posSecretConfigDAO.updatex(params);
    }

    @Override
    public void logicDeletePosSecretConfig(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = posSecretConfigDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deletePosSecretConfig(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = posSecretConfigDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public PosSecretConfigDTO findPosSecretConfigById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        PosSecretConfigDTO posSecretConfigDTO = posSecretConfigDAO.selectOneDTO(params);
        return posSecretConfigDTO;
    }

    @Override
    public PosSecretConfigDTO findOnePosSecretConfig(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        PosSecretConfig posSecretConfig = posSecretConfigDAO.selectOne(params);
        PosSecretConfigDTO posSecretConfigDTO = new PosSecretConfigDTO();
        if (null != posSecretConfig) {
            BeanUtils.copyProperties(posSecretConfig, posSecretConfigDTO);
        }
        return posSecretConfigDTO;
    }

    @Override
    public List<PosSecretConfigDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<PosSecretConfigDTO> resultList = posSecretConfigDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return posSecretConfigDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return posSecretConfigDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = posSecretConfigDAO.groupCount(conditions);
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
        return posSecretConfigDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = posSecretConfigDAO.groupSum(conditions);
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
