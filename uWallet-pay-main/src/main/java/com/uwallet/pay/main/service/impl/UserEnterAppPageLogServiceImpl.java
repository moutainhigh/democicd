package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.UserEnterAppPageLogDAO;
import com.uwallet.pay.main.model.dto.UserEnterAppPageLogDTO;
import com.uwallet.pay.main.model.entity.UserEnterAppPageLog;
import com.uwallet.pay.main.service.UserEnterAppPageLogService;
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
 * 用户APP页面流程记录表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 用户APP页面流程记录表
 * @author: zhangzeyuan
 * @date: Created in 2021-09-01 16:35:17
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Service
@Slf4j
public class UserEnterAppPageLogServiceImpl extends BaseServiceImpl implements UserEnterAppPageLogService {

    @Autowired
    private UserEnterAppPageLogDAO userEnterAppPageLogDAO;

    @Override
    public void saveUserEnterAppPageLog(@NonNull UserEnterAppPageLogDTO userEnterAppPageLogDTO, HttpServletRequest request) throws BizException {
        UserEnterAppPageLog userEnterAppPageLog = BeanUtil.copyProperties(userEnterAppPageLogDTO, new UserEnterAppPageLog());
        log.info("save UserEnterAppPageLog:{}", userEnterAppPageLog);
        if (userEnterAppPageLogDAO.insert((UserEnterAppPageLog) this.packAddBaseProps(userEnterAppPageLog, request)) != 1) {
            log.error("insert error, data:{}", userEnterAppPageLog);
            throw new BizException("Insert userEnterAppPageLog Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserEnterAppPageLogList(@NonNull List<UserEnterAppPageLog> userEnterAppPageLogList, HttpServletRequest request) throws BizException {
        if (userEnterAppPageLogList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = userEnterAppPageLogDAO.insertList(userEnterAppPageLogList);
        if (rows != userEnterAppPageLogList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, userEnterAppPageLogList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateUserEnterAppPageLog(@NonNull Long id, @NonNull UserEnterAppPageLogDTO userEnterAppPageLogDTO, HttpServletRequest request) throws BizException {
        log.info("full update userEnterAppPageLogDTO:{}", userEnterAppPageLogDTO);
        UserEnterAppPageLog userEnterAppPageLog = BeanUtil.copyProperties(userEnterAppPageLogDTO, new UserEnterAppPageLog());
        userEnterAppPageLog.setId(id);
        int cnt = userEnterAppPageLogDAO.update((UserEnterAppPageLog) this.packModifyBaseProps(userEnterAppPageLog, request));
        if (cnt != 1) {
            log.error("update error, data:{}", userEnterAppPageLogDTO);
            throw new BizException("update userEnterAppPageLog Error!");
        }
    }

    @Override
    public void updateUserEnterAppPageLogSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        userEnterAppPageLogDAO.updatex(params);
    }

    @Override
    public void logicDeleteUserEnterAppPageLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = userEnterAppPageLogDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteUserEnterAppPageLog(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = userEnterAppPageLogDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public UserEnterAppPageLogDTO findUserEnterAppPageLogById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        UserEnterAppPageLogDTO userEnterAppPageLogDTO = userEnterAppPageLogDAO.selectOneDTO(params);
        return userEnterAppPageLogDTO;
    }

    @Override
    public UserEnterAppPageLogDTO findOneUserEnterAppPageLog(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        UserEnterAppPageLog userEnterAppPageLog = userEnterAppPageLogDAO.selectOne(params);
        UserEnterAppPageLogDTO userEnterAppPageLogDTO = new UserEnterAppPageLogDTO();
        if (null != userEnterAppPageLog) {
            BeanUtils.copyProperties(userEnterAppPageLog, userEnterAppPageLogDTO);
        }
        return userEnterAppPageLogDTO;
    }

    @Override
    public List<UserEnterAppPageLogDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<UserEnterAppPageLogDTO> resultList = userEnterAppPageLogDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return userEnterAppPageLogDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return userEnterAppPageLogDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = userEnterAppPageLogDAO.groupCount(conditions);
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
        return userEnterAppPageLogDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = userEnterAppPageLogDAO.groupSum(conditions);
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
