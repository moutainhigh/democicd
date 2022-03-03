package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.dao.AdminRoleDAO;
import com.uwallet.pay.main.model.dto.AdminRoleDTO;
import com.uwallet.pay.main.model.entity.AdminRole;
import com.uwallet.pay.main.service.AdminRoleService;
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
 * 管理员 -角色关系表
 * </p>
 *
 * @package: com.loancloud.rloan.main.service.impl
 * @description: 管理员 -角色关系表
 * @author: Strong
 * @date: Created in 2019-09-16 16:25:12
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@Service
@Slf4j
public class AdminRoleServiceImpl extends BaseServiceImpl implements AdminRoleService {

    @Autowired
    private AdminRoleDAO adminRoleDAO;

    @Override
    public void saveAdminRole(@NonNull AdminRoleDTO adminRoleDTO, HttpServletRequest request) throws BizException {
        AdminRole adminRole = BeanUtil.copyProperties(adminRoleDTO, new AdminRole());
        log.info("save AdminRole:{}", adminRole);
        if (adminRoleDAO.insert((AdminRole) this.packAddBaseProps(adminRole, request)) != 1) {
            log.error("insert error, data:{}", adminRole);
            throw new BizException("Insert adminRole Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = BizException.class)
    public void saveAdminRoleList(@NonNull List<AdminRole> adminRoleList) throws BizException {
        if (adminRoleList.size() == 0) {
            throw new BizException("Length of parameter can not be 0");
        }
        int rows = adminRoleDAO.insertList(adminRoleList);
        if (rows != adminRoleList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, adminRoleList.size());
            throw new BizException("Batch save exception");
        }
    }

    @Override
    public void updateAdminRole(@NonNull Long id, @NonNull AdminRoleDTO adminRoleDTO, HttpServletRequest request) throws BizException {
        log.info("full update adminRoleDTO:{}", adminRoleDTO);
        AdminRole adminRole = BeanUtil.copyProperties(adminRoleDTO, new AdminRole());
        adminRole.setId(id);
        int cnt = adminRoleDAO.update((AdminRole) this.packModifyBaseProps(adminRole, request));
        if (cnt != 1) {
            log.error("update error, data:{}", adminRoleDTO);
            throw new BizException("update adminRole Error!");
        }
    }

    @Override
    public void updateAdminRoleSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        adminRoleDAO.updatex(params);
    }

    @Override
    public void logicDeleteAdminRole(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = adminRoleDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException("Delete failed");
        }
    }

    @Override
    public void deleteAdminRole(@NonNull Long id) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = adminRoleDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException("Delete failed");
        }
    }

    @Override
    public AdminRoleDTO findAdminRoleById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AdminRoleDTO adminRoleDTO = adminRoleDAO.selectOneDTO(params);
        return adminRoleDTO;
    }

    @Override
    public AdminRoleDTO findOneAdminRole(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        AdminRole adminRole = adminRoleDAO.selectOne(params);
        AdminRoleDTO adminRoleDTO = new AdminRoleDTO();
        if (null != adminRole) {
            BeanUtils.copyProperties(adminRole, adminRoleDTO);
        }
        return adminRoleDTO;
    }

    @Override
    public List<AdminRoleDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<AdminRoleDTO> resultList = adminRoleDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("Columns cannot be 0 in length");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return adminRoleDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return adminRoleDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = adminRoleDAO.groupCount(conditions);
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
        return adminRoleDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = adminRoleDAO.groupSum(conditions);
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
    public int deleteByAdminId(Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        return adminRoleDAO.deleteByAdminId(params);
    }

}
