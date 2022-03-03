package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.UserLocationRecordDAO;
import com.uwallet.pay.main.model.dto.UserLocationRecordDTO;
import com.uwallet.pay.main.model.entity.UserLocationRecord;
import com.uwallet.pay.main.service.UserLocationRecordService;
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
 * 用户地理位置信息记录表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 用户地理位置信息记录表
 * @author: xucl
 * @date: Created in 2021-05-15 10:22:46
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Service
@Slf4j
public class UserLocationRecordServiceImpl extends BaseServiceImpl implements UserLocationRecordService {

    @Autowired
    private UserLocationRecordDAO userLocationRecordDAO;

    @Override
    public void saveUserLocationRecord(@NonNull UserLocationRecordDTO userLocationRecordDTO, HttpServletRequest request) throws BizException {
        UserLocationRecord userLocationRecord = BeanUtil.copyProperties(userLocationRecordDTO, new UserLocationRecord());
        log.info("save UserLocationRecord:{}", userLocationRecord);
        if (userLocationRecordDAO.insert((UserLocationRecord) this.packAddBaseProps(userLocationRecord, request)) != 1) {
            log.error("insert error, data:{}", userLocationRecord);
            throw new BizException("Insert userLocationRecord Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserLocationRecordList(@NonNull List<UserLocationRecord> userLocationRecordList, HttpServletRequest request) throws BizException {
        if (userLocationRecordList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = userLocationRecordDAO.insertList(userLocationRecordList);
        if (rows != userLocationRecordList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, userLocationRecordList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateUserLocationRecord(@NonNull Long id, @NonNull UserLocationRecordDTO userLocationRecordDTO, HttpServletRequest request) throws BizException {
        log.info("full update userLocationRecordDTO:{}", userLocationRecordDTO);
        UserLocationRecord userLocationRecord = BeanUtil.copyProperties(userLocationRecordDTO, new UserLocationRecord());
        userLocationRecord.setId(id);
        int cnt = userLocationRecordDAO.update((UserLocationRecord) this.packModifyBaseProps(userLocationRecord, request));
        if (cnt != 1) {
            log.error("update error, data:{}", userLocationRecordDTO);
            throw new BizException("update userLocationRecord Error!");
        }
    }

    @Override
    public void updateUserLocationRecordSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        userLocationRecordDAO.updatex(params);
    }

    @Override
    public void logicDeleteUserLocationRecord(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = userLocationRecordDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteUserLocationRecord(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = userLocationRecordDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public UserLocationRecordDTO findUserLocationRecordById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        UserLocationRecordDTO userLocationRecordDTO = userLocationRecordDAO.selectOneDTO(params);
        return userLocationRecordDTO;
    }

    @Override
    public UserLocationRecordDTO findOneUserLocationRecord(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        UserLocationRecord userLocationRecord = userLocationRecordDAO.selectOne(params);
        UserLocationRecordDTO userLocationRecordDTO = new UserLocationRecordDTO();
        if (null != userLocationRecord) {
            BeanUtils.copyProperties(userLocationRecord, userLocationRecordDTO);
        }
        return userLocationRecordDTO;
    }

    @Override
    public List<UserLocationRecordDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<UserLocationRecordDTO> resultList = userLocationRecordDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return userLocationRecordDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return userLocationRecordDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = userLocationRecordDAO.groupCount(conditions);
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
        return userLocationRecordDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = userLocationRecordDAO.groupSum(conditions);
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
