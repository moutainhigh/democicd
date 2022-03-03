package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.model.dto.AdminRoleDTO;
import com.uwallet.pay.main.model.entity.AdminRole;
import com.uwallet.pay.main.service.AdminRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 管理员 -角色关系表
 * </p>
 *
 * @package:  com.loancloud.rloan.main.controller
 * @description: 管理员 -角色关系表
 * @author: Strong
 * @date: Created in 2019-09-16 16:25:12
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@RestController
@RequestMapping("/adminRole")
@Slf4j
@Api("管理员 -角色关系表")
public class AdminRoleController extends BaseController<AdminRole> {

    @Autowired
    private AdminRoleService adminRoleService;

    @ApiOperation(value = "分页查询管理员 -角色关系表", notes = "分页查询管理员 -角色关系表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-管理员 -角色关系表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = adminRoleService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<AdminRoleDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = adminRoleService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询管理员 -角色关系表", notes = "通过id查询管理员 -角色关系表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get adminRole Id:{}", id);
        return R.success(adminRoleService.findAdminRoleById(id));
    }

    @ApiOperation(value = "通过查询条件查询管理员 -角色关系表一条数据", notes = "通过查询条件查询管理员 -角色关系表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询管理员 -角色关系表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get adminRole findOne params:{}", params);
        int total = adminRoleService.count(params);
        if (total > 1) {
            log.error("get adminRole findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("AccessManageController.rule._95", getLang(request)));
        }
        AdminRoleDTO adminRoleDTO = null;
        if (total == 1) {
            adminRoleDTO = adminRoleService.findOneAdminRole(params);
        }
        return R.success(adminRoleDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增管理员 -角色关系表", notes = "新增管理员 -角色关系表")
    public Object create(@RequestBody AdminRoleDTO adminRoleDTO, HttpServletRequest request) {
        log.info("add adminRole DTO:{}", adminRoleDTO);
        try {
            adminRoleService.saveAdminRole(adminRoleDTO, request);
        } catch (BizException e) {
            log.error("add adminRole failed, adminRoleDTO: {}, error message:{}, error all:{}", adminRoleDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改管理员 -角色关系表", notes = "修改管理员 -角色关系表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody AdminRoleDTO adminRoleDTO, HttpServletRequest request) {
        log.info("put modify id:{}, adminRole DTO:{}", id, adminRoleDTO);
        try {
            adminRoleService.updateAdminRole(id, adminRoleDTO, request);
        } catch (BizException e) {
            log.error("update adminRole failed, adminRoleDTO: {}, error message:{}, error all:{}", adminRoleDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除管理员 -角色关系表", notes = "删除管理员 -角色关系表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete adminRole, id:{}", id);
        try {
            adminRoleService.logicDeleteAdminRole(id, request);
        } catch (BizException e) {
            log.error("delete failed, adminRole id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
