package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.TipClearFileRecordDAO;
import com.uwallet.pay.main.model.dto.TipClearFileRecordDTO;
import com.uwallet.pay.main.model.entity.TipClearFileRecord;
import com.uwallet.pay.main.service.TipClearFileRecordService;
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
 * 小费清算文件记录
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 小费清算文件记录
 * @author: zhangzeyuan
 * @date: Created in 2021-08-11 17:21:14
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Service
@Slf4j
public class TipClearFileRecordServiceImpl extends BaseServiceImpl implements TipClearFileRecordService {

    @Autowired
    private TipClearFileRecordDAO tipClearFileRecordDAO;

    @Override
    public void saveTipClearFileRecord(@NonNull TipClearFileRecordDTO tipClearFileRecordDTO, HttpServletRequest request) throws BizException {
        TipClearFileRecord tipClearFileRecord = BeanUtil.copyProperties(tipClearFileRecordDTO, new TipClearFileRecord());
        log.info("save TipClearFileRecord:{}", tipClearFileRecord);
        if (tipClearFileRecordDAO.insert((TipClearFileRecord) this.packAddBaseProps(tipClearFileRecord, request)) != 1) {
            log.error("insert error, data:{}", tipClearFileRecord);
            throw new BizException("Insert tipClearFileRecord Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTipClearFileRecordList(@NonNull List<TipClearFileRecord> tipClearFileRecordList, HttpServletRequest request) throws BizException {
        if (tipClearFileRecordList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = tipClearFileRecordDAO.insertList(tipClearFileRecordList);
        if (rows != tipClearFileRecordList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, tipClearFileRecordList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateTipClearFileRecord(@NonNull Long id, @NonNull TipClearFileRecordDTO tipClearFileRecordDTO, HttpServletRequest request) throws BizException {
        log.info("full update tipClearFileRecordDTO:{}", tipClearFileRecordDTO);
        TipClearFileRecord tipClearFileRecord = BeanUtil.copyProperties(tipClearFileRecordDTO, new TipClearFileRecord());
        tipClearFileRecord.setId(id);
        int cnt = tipClearFileRecordDAO.update((TipClearFileRecord) this.packModifyBaseProps(tipClearFileRecord, request));
        if (cnt != 1) {
            log.error("update error, data:{}", tipClearFileRecordDTO);
            throw new BizException("update tipClearFileRecord Error!");
        }
    }

    @Override
    public void updateTipClearFileRecordSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        tipClearFileRecordDAO.updatex(params);
    }

    @Override
    public void logicDeleteTipClearFileRecord(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = tipClearFileRecordDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteTipClearFileRecord(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = tipClearFileRecordDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public TipClearFileRecordDTO findTipClearFileRecordById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        TipClearFileRecordDTO tipClearFileRecordDTO = tipClearFileRecordDAO.selectOneDTO(params);
        return tipClearFileRecordDTO;
    }

    @Override
    public TipClearFileRecordDTO findOneTipClearFileRecord(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        TipClearFileRecord tipClearFileRecord = tipClearFileRecordDAO.selectOne(params);
        TipClearFileRecordDTO tipClearFileRecordDTO = new TipClearFileRecordDTO();
        if (null != tipClearFileRecord) {
            BeanUtils.copyProperties(tipClearFileRecord, tipClearFileRecordDTO);
        }
        return tipClearFileRecordDTO;
    }

    @Override
    public List<TipClearFileRecordDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<TipClearFileRecordDTO> resultList = tipClearFileRecordDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return tipClearFileRecordDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return tipClearFileRecordDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = tipClearFileRecordDAO.groupCount(conditions);
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
        return tipClearFileRecordDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = tipClearFileRecordDAO.groupSum(conditions);
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
