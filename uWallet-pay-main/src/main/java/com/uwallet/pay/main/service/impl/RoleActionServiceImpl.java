package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.RoleActionDAO;
import com.uwallet.pay.main.model.dto.RoleActionDTO;
import com.uwallet.pay.main.model.entity.RoleAction;
import com.uwallet.pay.main.service.RoleActionService;
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
 * 角色-权限关系表
 * </p>
 *
 * @package: com.loancloud.rloan.main.service.impl
 * @description: 角色-权限关系表
 * @author: Strong
 * @date: Created in 2019-09-16 17:51:57
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@Service
@Slf4j
public class RoleActionServiceImpl extends BaseServiceImpl implements RoleActionService {

    @Autowired
    private RoleActionDAO roleActionDAO;

    @Override
    public void saveRoleAction(@NonNull RoleActionDTO roleActionDTO, HttpServletRequest request) throws BizException {
        RoleAction roleAction = BeanUtil.copyProperties(roleActionDTO, new RoleAction());
        log.info("save RoleAction:{}", roleAction);
        if (roleActionDAO.insert((RoleAction) this.packAddBaseProps(roleAction, request)) != 1) {
            log.error("insert error, data:{}", roleAction);
            throw new BizException("Insert roleAction Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = BizException.class)
    public void saveRoleActionList(@NonNull List<RoleAction> roleActionList) throws BizException {
        if (roleActionList.size() == 0) {
            throw new BizException("Length of parameter can not be 0");
        }
        int rows = roleActionDAO.insertList(roleActionList);
        if (rows != roleActionList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, roleActionList.size());
            throw new BizException("Batch save exception");
        }
    }

    @Override
    public void updateRoleAction(@NonNull Long id, @NonNull RoleActionDTO roleActionDTO, HttpServletRequest request) throws BizException {
        log.info("full update roleActionDTO:{}", roleActionDTO);
        RoleAction roleAction = BeanUtil.copyProperties(roleActionDTO, new RoleAction());
        roleAction.setId(id);
        int cnt = roleActionDAO.update((RoleAction) this.packModifyBaseProps(roleAction, request));
        if (cnt != 1) {
            log.error("update error, data:{}", roleActionDTO);
            throw new BizException("update roleAction Error!");
        }
    }

    @Override
    public void updateRoleActionSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        roleActionDAO.updatex(params);
    }

    @Override
    public void logicDeleteRoleAction(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = roleActionDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException("Delete failed");
        }
    }

    @Override
    public void deleteRoleAction(@NonNull Long id) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = roleActionDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException("Delete failed");
        }
    }

    @Override
    public RoleActionDTO findRoleActionById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        RoleActionDTO roleActionDTO = roleActionDAO.selectOneDTO(params);
        return roleActionDTO;
    }

    @Override
    public RoleActionDTO findOneRoleAction(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        RoleAction roleAction = roleActionDAO.selectOne(params);
        RoleActionDTO roleActionDTO = new RoleActionDTO();
        if (null != roleAction) {
            BeanUtils.copyProperties(roleAction, roleActionDTO);
        }
        return roleActionDTO;
    }

    @Override
    public List<RoleActionDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<RoleActionDTO> resultList = roleActionDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("Columns cannot be 0 in length");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return roleActionDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return roleActionDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = roleActionDAO.groupCount(conditions);
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
        return roleActionDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = roleActionDAO.groupSum(conditions);
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
    public int deleteRoleActionByRoleId(Long roleId) {
        log.info("物理删除, roleId:{}", roleId);
        Map<String, Object> params = new HashMap<>(1);
        params.put("roleId", roleId);
        return roleActionDAO.deleteByRoleId(params);
    }

    @Override
    public List<Long> getActionByRoleId(Map<String, Object> params) {
        List<RoleAction> roleActions=roleActionDAO.select(params);
        List<Long> idList=new ArrayList<>();
        for(RoleAction roleAction:roleActions){
            idList.add(roleAction.getActionId());
        }
        return idList;
    }

}
