package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.PosInfoDAO;
import com.uwallet.pay.main.model.dto.PosInfoDTO;
import com.uwallet.pay.main.model.entity.PosInfo;
import com.uwallet.pay.main.service.PosInfoService;
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
 * pos基本信息
 * </p>
 *
 * @package: com.fenmi.generator.service.impl
 * @description: pos基本信息
 * @author: zhangzeyuan
 * @date: Created in 2021-03-19 15:17:59
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Service
@Slf4j
public class PosInfoServiceImpl extends BaseServiceImpl implements PosInfoService {

    @Autowired
    private PosInfoDAO posInfoDAO;

    @Override
    public void savePosInfo(@NonNull PosInfoDTO posInfoDTO, HttpServletRequest request) throws BizException {
        PosInfo posInfo = BeanUtil.copyProperties(posInfoDTO, new PosInfo());
        log.info("save PosInfo:{}", posInfo);
        if (posInfoDAO.insert((PosInfo) this.packAddBaseProps(posInfo, request)) != 1) {
            log.error("insert error, data:{}", posInfo);
            throw new BizException("Insert posInfo Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePosInfoList(@NonNull List<PosInfo> posInfoList, HttpServletRequest request) throws BizException {
        if (posInfoList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = posInfoDAO.insertList(posInfoList);
        if (rows != posInfoList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, posInfoList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updatePosInfo(@NonNull Long id, @NonNull PosInfoDTO posInfoDTO, HttpServletRequest request) throws BizException {
        log.info("full update posInfoDTO:{}", posInfoDTO);
        PosInfo posInfo = BeanUtil.copyProperties(posInfoDTO, new PosInfo());
        posInfo.setId(id);
        int cnt = posInfoDAO.update((PosInfo) this.packModifyBaseProps(posInfo, request));
        if (cnt != 1) {
            log.error("update error, data:{}", posInfoDTO);
            throw new BizException("update posInfo Error!");
        }
    }

    @Override
    public void updatePosInfoSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        posInfoDAO.updatex(params);
    }

    @Override
    public void logicDeletePosInfo(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = posInfoDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deletePosInfo(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = posInfoDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public PosInfoDTO findPosInfoById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        PosInfoDTO posInfoDTO = posInfoDAO.selectOneDTO(params);
        return posInfoDTO;
    }

    @Override
    public PosInfoDTO findOnePosInfo(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        PosInfo posInfo = posInfoDAO.selectOne(params);
        PosInfoDTO posInfoDTO = new PosInfoDTO();
        if (null != posInfo) {
            BeanUtils.copyProperties(posInfo, posInfoDTO);
        }
        return posInfoDTO;
    }

    @Override
    public List<PosInfoDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<PosInfoDTO> resultList = posInfoDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return posInfoDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return posInfoDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = posInfoDAO.groupCount(conditions);
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
        return posInfoDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = posInfoDAO.groupSum(conditions);
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
