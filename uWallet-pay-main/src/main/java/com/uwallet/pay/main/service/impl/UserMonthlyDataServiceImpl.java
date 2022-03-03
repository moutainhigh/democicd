package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.UserMonthlyDataDAO;
import com.uwallet.pay.main.model.dto.UserMonthlyDataDTO;
import com.uwallet.pay.main.model.entity.UserMonthlyData;
import com.uwallet.pay.main.service.UserMonthlyDataService;
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
 * 用户每月统计表
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 用户每月统计表
 * @author: zhoutt
 * @date: Created in 2021-04-08 16:40:22
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@Service
@Slf4j
public class UserMonthlyDataServiceImpl extends BaseServiceImpl implements UserMonthlyDataService {

    @Autowired
    private UserMonthlyDataDAO userMonthlyDataDAO;

    @Override
    public void saveUserMonthlyData(@NonNull UserMonthlyDataDTO userMonthlyDataDTO, HttpServletRequest request) throws BizException {
        UserMonthlyData userMonthlyData = BeanUtil.copyProperties(userMonthlyDataDTO, new UserMonthlyData());
        log.info("save UserMonthlyData:{}", userMonthlyData);
        if (userMonthlyDataDAO.insert((UserMonthlyData) this.packAddBaseProps(userMonthlyData, request)) != 1) {
            log.error("insert error, data:{}", userMonthlyData);
            throw new BizException("Insert userMonthlyData Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserMonthlyDataList(@NonNull List<UserMonthlyData> userMonthlyDataList, HttpServletRequest request) throws BizException {
        if (userMonthlyDataList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = userMonthlyDataDAO.insertList(userMonthlyDataList);
        if (rows != userMonthlyDataList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, userMonthlyDataList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateUserMonthlyData(@NonNull Long id, @NonNull UserMonthlyDataDTO userMonthlyDataDTO, HttpServletRequest request) throws BizException {
        log.info("full update userMonthlyDataDTO:{}", userMonthlyDataDTO);
        UserMonthlyData userMonthlyData = BeanUtil.copyProperties(userMonthlyDataDTO, new UserMonthlyData());
        userMonthlyData.setId(id);
        int cnt = userMonthlyDataDAO.update((UserMonthlyData) this.packModifyBaseProps(userMonthlyData, request));
        if (cnt != 1) {
            log.error("update error, data:{}", userMonthlyDataDTO);
            throw new BizException("update userMonthlyData Error!");
        }
    }

    @Override
    public void updateUserMonthlyDataSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        userMonthlyDataDAO.updatex(params);
    }

    @Override
    public void logicDeleteUserMonthlyData(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = userMonthlyDataDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteUserMonthlyData(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = userMonthlyDataDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public UserMonthlyDataDTO findUserMonthlyDataById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        UserMonthlyDataDTO userMonthlyDataDTO = userMonthlyDataDAO.selectOneDTO(params);
        return userMonthlyDataDTO;
    }

    @Override
    public UserMonthlyDataDTO findOneUserMonthlyData(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        UserMonthlyData userMonthlyData = userMonthlyDataDAO.selectOne(params);
        UserMonthlyDataDTO userMonthlyDataDTO = new UserMonthlyDataDTO();
        if (null != userMonthlyData) {
            BeanUtils.copyProperties(userMonthlyData, userMonthlyDataDTO);
        }
        return userMonthlyDataDTO;
    }

    @Override
    public List<UserMonthlyDataDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<UserMonthlyDataDTO> resultList = userMonthlyDataDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return userMonthlyDataDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return userMonthlyDataDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = userMonthlyDataDAO.groupCount(conditions);
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
        return userMonthlyDataDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = userMonthlyDataDAO.groupSum(conditions);
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
