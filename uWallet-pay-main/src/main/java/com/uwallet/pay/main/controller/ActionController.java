package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.model.dto.ActionDTO;
import com.uwallet.pay.main.model.dto.ActionOnlyDTO;
import com.uwallet.pay.main.model.entity.Action;
import com.uwallet.pay.main.service.ActionService;
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
 * 权限表
 * </p>
 *
 * @package:  com.loancloud.rloan.main.controller
 * @description: 权限表
 * @author: Strong
 * @date: Created in 2019-09-16 17:55:12
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@RestController
@RequestMapping("/action")
@Slf4j
@Api("权限表")
public class ActionController extends BaseController<Action> {

    @Autowired
    private ActionService actionService;

    @ApiOperation(value = "分页查询权限表", notes = "分页查询权限表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-权限表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = actionService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ActionDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = actionService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询权限表", notes = "通过id查询权限表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get action Id:{}", id);
        return R.success(actionService.findActionById(id));
    }

    @ApiOperation(value = "通过查询条件查询权限表一条数据", notes = "通过查询条件查询权限表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询权限表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get action findOne params:{}", params);
        int total = actionService.count(params);
        if (total > 1) {
            log.error("get action findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("AccessManageController.rule._95", getLang(request)) );
        }
        ActionDTO actionDTO = null;
        if (total == 1) {
            actionDTO = actionService.findOneAction(params);
        }
        return R.success(actionDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增权限表", notes = "新增权限表")
    public Object create(@RequestBody ActionDTO actionDTO, HttpServletRequest request) {
        log.info("add action DTO:{}", actionDTO);
        try {
            actionService.saveAction(actionDTO, request);
        } catch (BizException e) {
            log.error("add action failed, actionDTO: {}, error message:{}, error all:{}", actionDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改权限表", notes = "修改权限表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody ActionDTO actionDTO, HttpServletRequest request) {
        log.info("put modify id:{}, action DTO:{}", id, actionDTO);
        try {
            actionService.updateAction(id, actionDTO, request);
        } catch (BizException e) {
            log.error("update action failed, actionDTO: {}, error message:{}, error all:{}", actionDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除权限表", notes = "删除权限表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete action, id:{}", id);
        try {
            actionService.logicDeleteAction(id, request);
        } catch (BizException e) {
            log.error("delete failed, action id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    /**
     * roleManage_list 角色管理列表
     * @return
     */
    @GetMapping(value = "/getActionTree", name = "权限树")
    public Object actionTree() {
        List<ActionOnlyDTO> trees = actionService.actionTree();
        return R.success(trees);
    }

}
