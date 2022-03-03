package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.main.dao.UserActionButtonDAO;
import com.uwallet.pay.main.model.dto.UserActionButtonDTO;
import com.uwallet.pay.main.model.entity.UserActionButton;
import com.uwallet.pay.main.service.UserActionButtonService;
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
 * 用户冻结表存在该表的用户可以被冻结和解冻
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 用户冻结表存在该表的用户可以被冻结和解冻
 * @author: xucl
 * @date: Created in 2021-09-10 09:35:21
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Service
@Slf4j
public class UserActionButtonServiceImpl extends BaseServiceImpl implements UserActionButtonService {

    @Autowired
    private UserActionButtonDAO userActionButtonDAO;

    @Override
    public void saveUserActionButton(@NonNull UserActionButtonDTO userActionButtonDTO, HttpServletRequest request) throws BizException {
        UserActionButton userActionButton = BeanUtil.copyProperties(userActionButtonDTO, new UserActionButton());
        log.info("save UserActionButton:{}", userActionButton);
        if (userActionButtonDAO.insert((UserActionButton) this.packAddBaseProps(userActionButton, request)) != 1) {
            log.error("insert error, data:{}", userActionButton);
            throw new BizException("Insert userActionButton Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserActionButtonList(@NonNull List<UserActionButton> userActionButtonList, HttpServletRequest request) throws BizException {
        if (userActionButtonList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = userActionButtonDAO.insertList(userActionButtonList);
        if (rows != userActionButtonList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, userActionButtonList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateUserActionButton(@NonNull Long id, @NonNull UserActionButtonDTO userActionButtonDTO, HttpServletRequest request) throws BizException {
        log.info("full update userActionButtonDTO:{}", userActionButtonDTO);
        UserActionButton userActionButton = BeanUtil.copyProperties(userActionButtonDTO, new UserActionButton());
        userActionButton.setId(id);
        int cnt = userActionButtonDAO.update((UserActionButton) this.packModifyBaseProps(userActionButton, request));
        if (cnt != 1) {
            log.error("update error, data:{}", userActionButtonDTO);
            throw new BizException("update userActionButton Error!");
        }
    }

    @Override
    public void updateUserActionButtonSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        userActionButtonDAO.updatex(params);
    }

    @Override
    public void logicDeleteUserActionButton(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = userActionButtonDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteUserActionButton(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = userActionButtonDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public UserActionButtonDTO findUserActionButtonById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        UserActionButtonDTO userActionButtonDTO = userActionButtonDAO.selectOneDTO(params);
        return userActionButtonDTO;
    }

    @Override
    public UserActionButtonDTO findOneUserActionButton(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        UserActionButton userActionButton = userActionButtonDAO.selectOne(params);
        UserActionButtonDTO userActionButtonDTO = new UserActionButtonDTO();
        if (null != userActionButton) {
            BeanUtils.copyProperties(userActionButton, userActionButtonDTO);
        }
        return userActionButtonDTO;
    }

    @Override
    public List<UserActionButtonDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<UserActionButtonDTO> resultList = userActionButtonDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return userActionButtonDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return userActionButtonDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = userActionButtonDAO.groupCount(conditions);
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
        return userActionButtonDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = userActionButtonDAO.groupSum(conditions);
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
