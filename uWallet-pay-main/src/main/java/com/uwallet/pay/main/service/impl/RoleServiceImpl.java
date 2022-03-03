package com.uwallet.pay.main.service.impl;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.dao.AdminDAO;
import com.uwallet.pay.main.dao.AdminRoleDAO;
import com.uwallet.pay.main.dao.RoleDAO;
import com.uwallet.pay.main.model.dto.AdminDTO;
import com.uwallet.pay.main.model.dto.AdminRoleDTO;
import com.uwallet.pay.main.model.dto.RoleActionDTO;
import com.uwallet.pay.main.model.dto.RoleDTO;
import com.uwallet.pay.main.model.entity.Role;
import com.uwallet.pay.main.model.entity.RoleAction;
import com.uwallet.pay.main.service.RoleActionService;
import com.uwallet.pay.main.service.RoleService;
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
 * 角色表

 * </p>
 *
 * @package: com.loancloud.rloan.main.service.impl
 * @description: 角色表

 * @author: Strong
 * @date: Created in 2019-09-16 17:34:46
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@Service
@Slf4j
public class RoleServiceImpl extends BaseServiceImpl implements RoleService {

    @Autowired
    private RoleDAO roleDAO;
    @Autowired
    private RoleActionService roleActionService;
    @Autowired
    private AdminDAO adminDAO;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private AdminRoleDAO adminRoleDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRole(@NonNull RoleDTO roleDTO, HttpServletRequest request) throws BizException {
        if(StringUtils.isEmpty(roleDTO.getName())){
            throw new BizException(I18nUtils.get("role.rule.nameIsNULL",getLang(request)));
        }
        Map<String, Object> params = new HashMap<>(1);
        params.put("name", roleDTO.getName());
        RoleDTO rol= roleDAO.selectOneDTO(params);
        if (rol!=null) {
            throw new BizException(I18nUtils.get("role.rule.presence",getLang(request)));
        }
        Role role = BeanUtil.copyProperties(roleDTO, new Role());
        log.info("save Role:{}", role);
        if (roleDAO.insert((Role) this.packAddBaseProps(role, request)) != 1) {
            log.error("insert error, data:{}", role);
            throw new BizException("Insert role Error!");
        }
        /*角色添加 可以同时进行权限的勾选操作，存入数据库
          (1)判断是否接收到权限Id，不可以为空。
          (2)若权限Id不为空，则需要添加角色权限关联表数据，若添加失败，则需要回滚事务，让角色添加失败。
         */
        if(roleDTO.getActions() == null || roleDTO.getActions().isEmpty()) {
            throw new BizException(I18nUtils.get("action.rule.isNull",getLang(request)));
        }
        saveRoleAction(roleDTO, request, role);
    }

    private void saveRoleAction(@NonNull RoleDTO roleDTO, HttpServletRequest request, Role role) throws BizException {
        List<RoleAction> roleActionList = new ArrayList<>();
            RoleAction roleAction;
            for (Long action : roleDTO.getActions()) {
                roleAction = new RoleAction();
                roleAction.setRoleId(role.getId());
                roleAction.setActionId(action);
                roleActionList.add((RoleAction) this.packAddBaseProps(roleAction, request));
            }
            roleActionService.saveRoleActionList(roleActionList);
    }

    @Override
    @Transactional(rollbackFor = BizException.class)
    public void saveRoleList(@NonNull List<Role> roleList) throws BizException {
        if (roleList.size() == 0) {
            throw new BizException("Length of parameter can not be 0");
        }
        int rows = roleDAO.insertList(roleList);
        if (rows != roleList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, roleList.size());
            throw new BizException("Batch save exception");
        }
    }

    @Override
    public void updateRole(@NonNull Long id, @NonNull RoleDTO roleDTO, HttpServletRequest request) throws BizException {
        log.info("full update roleDTO:{}", roleDTO);
        checkRole(id, roleDTO, request);
        Role role = BeanUtil.copyProperties(roleDTO, new Role());
        role.setId(id);
        int cnt = roleDAO.update((Role) this.packModifyBaseProps(role, request));
        if (cnt != 1) {
            log.error("update error, data:{}", roleDTO);
            throw new BizException("update role Error!");
        }
    }

    private void checkRole(@NonNull Long id, @NonNull RoleDTO roleDTO, HttpServletRequest request) throws BizException {
        if (roleDTO.getStats() == 0) {
            Map<String, Object> para = new HashMap<>(1);
            para.put("roleId", id);
            List<AdminRoleDTO> resultList = adminRoleDAO.selectDTO(para);
            if (resultList.size() > 0 || !resultList.isEmpty()) {
                throw new BizException(I18nUtils.get("role.rule.isState",getLang(request)));
            }
        }
    }

    @Override
    public void updateRoleSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        roleDAO.updatex(params);
    }

    @Override
    public void logicDeleteRole(Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> para = new HashMap<>(1);
        para.put("roleId", id);
        List<AdminRoleDTO> resultList = adminRoleDAO.selectDTO(para);
        if (resultList.size()>0 || !resultList.isEmpty()) {
            throw new BizException(I18nUtils.get("role.rule.isStatus",getLang(request)));
        }
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = roleDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException("Delete failed");
        }
    }


    @Override
    public void deleteRole(@NonNull Long id) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = roleDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException("Delete failed");
        }
    }

    @Override
    public RoleDTO findRoleById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        RoleDTO roleDTO = roleDAO.selectOneDTO(params);
        return roleDTO;
    }


    @Override
    public RoleDTO findOneRole(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        Role role = roleDAO.selectOne(params);
        RoleDTO roleDTO = new RoleDTO();
        if (null != role) {
            BeanUtils.copyProperties(role, roleDTO);
        }
        return roleDTO;
    }

    @Override
    public List<RoleDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<RoleDTO> resultList = roleDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("Columns cannot be 0 in length");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return roleDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return roleDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = roleDAO.groupCount(conditions);
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
        return roleDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = roleDAO.groupSum(conditions);
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateRoleAndRoleAction(@NonNull Long id, @NonNull RoleDTO roleDTO, HttpServletRequest request) throws BizException {
        log.info("full update roleDTO:{}", roleDTO);
        if(StringUtils.isEmpty(roleDTO.getName())){
            throw new BizException(I18nUtils.get("role.rule.nameIsNULL",getLang(request)));
        }
        if(roleDTO.getActions() == null || roleDTO.getActions().isEmpty()) {
            throw new BizException(I18nUtils.get("action.rule.isNull",getLang(request)));
        }
        checkRole(id, roleDTO, request);
        //审计记录(修改角色前,先查出旧对象)
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        RoleDTO oldRoleDTO = roleDAO.selectOneDTO(params);
        if (oldRoleDTO == null) {
            log.error("selectOneDTO param error, id:{}", id);
            throw new BizException("param error!");
        }
        Role role = new Role();
        role.setId(id);

        int cnt = roleDAO.update((Role) this.packModifyBaseProps(BeanUtil.copyProperties(roleDTO, role), request));
        if(cnt !=1){
            log.error("update error, data:{}", roleDTO);
            throw new BizException("update role Error!");
        }
        /*角色修改 同时进行权限的勾选操作，存入数据库
          (1)判断是否接收到权限Id，不可以为空。
          (2)若权限Id不为空，则需要添加角色权限关联表数据，若添加失败，则需要回滚事务，让角色修改失败。
         */
        RoleActionDTO roleActionDTO=new RoleActionDTO();
        roleActionDTO.setRoleId(role.getId());
        /*
          修改权限配置
         (1)删除该角色在数据库已有的权限(角色权限关联表)
         (2)重新给该角色添加权限 前台重新分配的权限(前台会传）
         */
        roleActionService.deleteRoleActionByRoleId(id);
        saveRoleAction(roleDTO, request, role);
        //清空redis缓存中的权限(只要管理员涉及到该角色)
        Map<String, Object> para = new HashMap<>(1);
        para.put("roleId", id);
        List<AdminDTO> list = adminDAO.selectAdminByRoleId(para);
        for (AdminDTO adminDTO : list){
            //清空redis缓存中的权限
            if (redisUtils.hasKey(Constant.ADMIN+adminDTO.getUserName()+Constant.ACTION)) {
                redisUtils.del(Constant.ADMIN+adminDTO.getUserName()+Constant.ACTION);
            }
        }
    }
}
