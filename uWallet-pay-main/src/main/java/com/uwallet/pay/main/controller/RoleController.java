package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.ActionOnlyDTO;
import com.uwallet.pay.main.model.dto.RoleDTO;
import com.uwallet.pay.main.model.entity.Role;
import com.uwallet.pay.main.service.ActionService;
import com.uwallet.pay.main.service.RoleActionService;
import com.uwallet.pay.main.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 角色表

 * </p>
 *
 * @package:  com.loancloud.rloan.main.controller
 * @description: 角色表

 * @author: Strong
 * @date: Created in 2019-09-16 17:34:46
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@RestController
@RequestMapping("/role")
@Slf4j
@Api("角色表")
public class RoleController extends BaseController<Role> {

    @Autowired
    private RoleService roleService;
    @Autowired
    private ActionService actionService;
    @Autowired
    private RoleActionService roleActionService;

    @ActionFlag(detail = "admin_list,role_list")
    @ApiOperation(value = "分页查询角色表", notes = "分页查询角色表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-角色表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        params.put("sign",request.getParameter("sign"));
        int total = roleService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<RoleDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = roleService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }


    @ApiOperation(value = "通过id查询角色表 ", notes = "通过id查询角色表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get role Id:{}", id);
        return R.success(roleService.findRoleById(id));
    }

    @ApiOperation(value = "通过查询条件查询角色表一条数据", notes = "通过查询条件查询角色表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询角色表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get role findOne params:{}", params);
        int total = roleService.count(params);
        if (total > 1) {
            log.error("get role findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("AccessManageController.rule._95", getLang(request)));
        }
        RoleDTO roleDTO = null;
        if (total == 1) {
            roleDTO = roleService.findOneRole(params);
        }
        return R.success(roleDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增角色表 ", notes = "新增角色表")
    public Object create(@RequestBody RoleDTO roleDTO, HttpServletRequest request) {
        log.info("add role DTO:{}", roleDTO);
        try {
            roleService.saveRole(roleDTO, request);
        } catch (BizException e) {
            log.error("add role failed, roleDTO: {}, error message:{}, error all:{}", roleDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改角色表", notes = "修改角色表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody RoleDTO roleDTO, HttpServletRequest request) {
        log.info("put modify id:{}, role DTO:{}", id, roleDTO);
        try {
            roleService.updateRole(id, roleDTO, request);
        } catch (BizException e) {
            log.error("update role failed, roleDTO: {}, error message:{}, error all:{}", roleDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除角色表", notes = "删除角色表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete role, id:{}", id);
        try {
            roleService.logicDeleteRole(id, request);
        } catch (BizException e) {
            log.error("delete failed, role id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/cusUpdate/{id}")
    @ApiOperation(value = "修改角色表或者角色权限表", notes = "修改角色表或者角色权限表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object cusUpdate(@PathVariable("id") Long id, @RequestBody RoleDTO RoleDTO, HttpServletRequest request){
        log.info("put modify id:{}, role DTO:{}", id, RoleDTO);
        try {
            roleService.updateRoleAndRoleAction(id,RoleDTO, request);
        } catch (BizException e) {
            e.printStackTrace();
            log.error("update role failed,  roleDTO: {}, error message:{}", RoleDTO, e.getMessage());
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(),e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "通过角色id查询", notes = "通过查询条件查询权限表一条数据")
    @GetMapping(value = "/findRoleAndAction", name="通过查询条件查询权限表一条数据")
    public Object findRoleAndAction(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get action findOne params:{}", params);
        int total = roleService.count(params);
        if (total > 1) {
            log.error("get action findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("AccessManageController.rule._95", getLang(request)) );
        }
        RoleDTO roleDTO;
        Map<String, Object> par = null;
        if (total == 1) {
            roleDTO = roleService.findOneRole(params);
            List<ActionOnlyDTO> trees = actionService.actionTree();
            //ID不为空 查出对应角色的树
            List<Long> idList;
            Map<String, Object> param = new HashMap<>(1);
            param.put("roleId", roleDTO.getId());
            idList = roleActionService.getActionByRoleId(param);
            if (idList.size() != 0) {
                //根据角色权限 选中setchecked
                setChecked(trees, idList);
            }
            par = new HashMap<>(1);
            par.put("trees", trees);
            par.put("roleDTO", roleDTO);
        }
        return R.success(par);
    }

    /**
     * 权限树 选中 方法
     * @param actionDTOs
     * @param roleActions
     */
    private void setChecked(List<ActionOnlyDTO> actionDTOs, List<Long> roleActions) {
        for (ActionOnlyDTO actionDTO : actionDTOs) {
            if (actionDTO.getChildren().size()!=0) {
                setChecked(actionDTO.getChildren(), roleActions);
            }
            if (roleActions.contains(actionDTO.getMenu_id())) {
                actionDTO.setChecked(true);
            }
        }
    }

}
