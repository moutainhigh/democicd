package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.model.entity.BaseEntity;
import com.uwallet.pay.main.dao.UserInfoUpdateLogDAO;
import com.uwallet.pay.main.model.dto.UserInfoUpdateLogDTO;
import com.uwallet.pay.main.model.entity.UserInfoUpdateLog;
import com.uwallet.pay.main.service.UserInfoUpdateLogService;
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
 * 用户信息修改记录表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 用户信息修改记录表
 * @author: xucl
 * @date: Created in 2021-09-10 16:55:37
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Service
@Slf4j
public class UserInfoUpdateLogServiceImpl extends BaseServiceImpl implements UserInfoUpdateLogService {

    @Autowired
    private UserInfoUpdateLogDAO userInfoUpdateLogDAO;

    @Override
    public Long saveUserInfoUpdateLog(@NonNull UserInfoUpdateLogDTO userInfoUpdateLogDTO, HttpServletRequest request) throws BizException {
        UserInfoUpdateLog userInfoUpdateLog = BeanUtil.copyProperties(userInfoUpdateLogDTO, new UserInfoUpdateLog());
        log.info("save UserInfoUpdateLog:{}", userInfoUpdateLog);
        BaseEntity baseEntity = this.packAddBaseProps(userInfoUpdateLog, request);
        if (userInfoUpdateLogDAO.insert((UserInfoUpdateLog)baseEntity ) != 1) {
            log.error("insert error, data:{}", userInfoUpdateLog);
            throw new BizException("Insert userInfoUpdateLog Error!");
        }
        return baseEntity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserInfoUpdateLogList(@NonNull List<UserInfoUpdateLog> userInfoUpdateLogList, HttpServletRequest request) throws BizException {
        if (userInfoUpdateLogList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = userInfoUpdateLogDAO.insertList(userInfoUpdateLogList);
        if (rows != userInfoUpdateLogList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, userInfoUpdateLogList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateUserInfoUpdateLog(@NonNull Long id, @NonNull UserInfoUpdateLogDTO userInfoUpdateLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update userInfoUpdateLogDTO:{}", userInfoUpdateLogDTO);
        UserInfoUpdateLog userInfoUpdateLog = BeanUtil.copyProperties(userInfoUpdateLogDTO, new UserInfoUpdateLog());
        userInfoUpdateLog.setId(id);
        int cnt = userInfoUpdateLogDAO.update((UserInfoUpdateLog) this.packModifyBaseProps(userInfoUpdateLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", userInfoUpdateLogDTO);
            throw new BizException("update userInfoUpdateLog Error!");
        }
    }

    @Override
    public void updateUserInfoUpdateLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        userInfoUpdateLogDAO.updatex(params);
    }

    @Override
    public void logicDeleteUserInfoUpdateLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = userInfoUpdateLogDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteUserInfoUpdateLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = userInfoUpdateLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public UserInfoUpdateLogDTO findUserInfoUpdateLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        UserInfoUpdateLogDTO userInfoUpdateLogDTO = userInfoUpdateLogDAO.selectOneDTO(params);
        return userInfoUpdateLogDTO;
    }

    @Override
    public UserInfoUpdateLogDTO findOneUserInfoUpdateLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        UserInfoUpdateLog userInfoUpdateLog = userInfoUpdateLogDAO.selectOne(params);
        UserInfoUpdateLogDTO userInfoUpdateLogDTO = new UserInfoUpdateLogDTO();
        if (null != userInfoUpdateLog) {
            BeanUtils.copyProperties(userInfoUpdateLog, userInfoUpdateLogDTO);
        }
        return userInfoUpdateLogDTO;
    }

    @Override
    public List<UserInfoUpdateLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<UserInfoUpdateLogDTO> resultList = userInfoUpdateLogDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return userInfoUpdateLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return userInfoUpdateLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = userInfoUpdateLogDAO.groupCount(conditions);
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
        return userInfoUpdateLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = userInfoUpdateLogDAO.groupSum(conditions);
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
    public List<UserInfoUpdateLogDTO> findUpdateList(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<UserInfoUpdateLogDTO> resultList = userInfoUpdateLogDAO.findUpdateList(params);
        return resultList;
    }

    @Override
    public UserInfoUpdateLogDTO findOneUserInfoUpdateLogMax(@NonNull JSONObject param) {
        return userInfoUpdateLogDAO.findOneUserInfoUpdateLogMax(param);
    }

}
