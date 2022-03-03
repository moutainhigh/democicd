package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.dao.UserActionDAO;
import com.uwallet.pay.main.model.dto.UserActionDTO;
import com.uwallet.pay.main.model.entity.UserAction;
import com.uwallet.pay.main.service.UserActionService;
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
 * 商户端用户-权限关系表
 * </p>
 *
 * @package: com.uwallet.pay.main.main.service.impl
 * @description: 商户端用户-权限关系表
 * @author: baixinyue
 * @date: Created in 2020-02-19 14:02:21
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Service
@Slf4j
public class UserActionServiceImpl extends BaseServiceImpl implements UserActionService {

    @Autowired
    private UserActionDAO userActionDAO;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void saveUserAction(@NonNull UserActionDTO userActionDTO, HttpServletRequest request) throws BizException {
        UserAction userAction = BeanUtil.copyProperties(userActionDTO, new UserAction());
        log.info("save UserAction:{}", userAction);
        if (userActionDAO.insert((UserAction) this.packAddBaseProps(userAction, request)) != 1) {
            log.error("insert error, data:{}", userAction);
            throw new BizException("Insert userAction Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserActionList(@NonNull List<UserAction> userActionList, HttpServletRequest request) throws BizException {
        if (userActionList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = userActionDAO.insertList(userActionList);
        if (rows != userActionList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, userActionList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateUserAction(@NonNull Long id, @NonNull UserActionDTO userActionDTO, HttpServletRequest request) throws BizException {
        log.info("full update userActionDTO:{}", userActionDTO);
        UserAction userAction = BeanUtil.copyProperties(userActionDTO, new UserAction());
        userAction.setId(id);
        int cnt = userActionDAO.update((UserAction) this.packModifyBaseProps(userAction, request));
        if (cnt != 1) {
            log.error("update error, data:{}", userActionDTO);
            throw new BizException("update userAction Error!");
        }
    }

    @Override
    public void updateUserActionSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        userActionDAO.updatex(params);
    }

    @Override
    public void logicDeleteUserAction(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = userActionDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteUserAction(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = userActionDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public UserActionDTO findUserActionById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        UserActionDTO userActionDTO = userActionDAO.selectOneDTO(params);
        return userActionDTO;
    }

    @Override
    public UserActionDTO findOneUserAction(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        UserAction userAction = userActionDAO.selectOne(params);
        UserActionDTO userActionDTO = new UserActionDTO();
        if (null != userAction) {
            BeanUtils.copyProperties(userAction, userActionDTO);
        }
        return userActionDTO;
    }

    @Override
    public List<UserActionDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<UserActionDTO> resultList = userActionDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return userActionDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return userActionDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = userActionDAO.groupCount(conditions);
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
        return userActionDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = userActionDAO.groupSum(conditions);
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
    public void setUserAction(JSONObject requestInfo, HttpServletRequest request) throws BizException {
        Long userId = requestInfo.getLong("userId");
        //若用户权限,则先将老权限删除
        Map<String, Object> params = new HashMap<>(1);
        params.put("userId", userId);
        int findCount = count(params);
        if (findCount > 0) {
            int delCount = userActionDAO.deleteActionByUserId(userId);
            if (findCount != delCount) {
                throw new BizException(I18nUtils.get("set.action.failed", getLang(request)));
            }
        }
        //设置权限
        JSONArray actionArray = requestInfo.getJSONArray("actions");
        List<UserAction> userActionList = new ArrayList<>(1);
        for (int i = 0; i < actionArray.size(); i ++) {
            Long actionId = actionArray.getLong(i);
            UserAction userAction = new UserAction();
            userAction.setUserId(userId);
            userAction.setActionId(actionId);
            userAction = (UserAction) packAddBaseProps(userAction, request);
            userActionList.add(userAction);
        }
        saveUserActionList(userActionList, request);
        //设置成功后，将权限放入缓存中
        Map<String, Object> userAction = new HashMap<>(1);
        userAction.put("action", userActionList);
        redisUtils.hmset(userId + "_action", userAction);
    }

    @Override
    public List<UserAction> getUserAction(Long userId, HttpServletRequest request) throws BizException {
        List<UserAction> userActionList = null;
        if (redisUtils.hasKey(userId + "_action")) {
            Map<Object, Object> userAction = redisUtils.hmget(userId + "_action");
            if (userAction != null) {
                userActionList = (List<UserAction>) userAction.get("action");
            } else {
                Map<String, Object> params = new HashMap<>(1);
                params.put("userId", userId);
                userActionList = userActionDAO.select(params);
            }
        } else {
            Map<String, Object> params = new HashMap<>(1);
            params.put("userId", userId);
            userActionList = userActionDAO.select(params);
        }
        return userActionList;
    }

}
