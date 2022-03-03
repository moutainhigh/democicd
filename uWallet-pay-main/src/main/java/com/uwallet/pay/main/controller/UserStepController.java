package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.UserStepDTO;
import com.uwallet.pay.main.model.dto.UserStepLogDTO;
import com.uwallet.pay.main.model.entity.UserStep;
import com.uwallet.pay.main.service.UserStepLogService;
import com.uwallet.pay.main.service.UserStepService;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.main.util.I18nUtils;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;


/**
 * <p>
 * 用户权限阶段
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 用户权限阶段
 * @author: baixinyue
 * @date: Created in 2020-06-30 16:51:35
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/userStep")
@Slf4j
@Api("用户权限阶段")
public class UserStepController extends BaseController<UserStep> {

    @Autowired
    private UserStepService userStepService;

    @Autowired
    private UserStepLogService userStepLogService;

    @ActionFlag(detail = "UserStep_list")
    @ApiOperation(value = "分页查询用户权限阶段", notes = "分页查询用户权限阶段以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-用户权限阶段列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = userStepService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserStepDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = userStepService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询用户权限阶段", notes = "通过id查询用户权限阶段")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get userStep Id:{}", id);
        return R.success(userStepService.findUserStepById(id));
    }

    @ApiOperation(value = "通过查询条件查询用户权限阶段一条数据", notes = "通过查询条件查询用户权限阶段一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询用户权限阶段一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get userStep findOne params:{}", params);
        int total = userStepService.count(params);
        if (total > 1) {
            log.error("get userStep findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        UserStepDTO userStepDTO = null;
        if (total == 1) {
            userStepDTO = userStepService.findOneUserStep(params);
        }
        return R.success(userStepDTO);
    }

    @ActionFlag(detail = "UserStep_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增用户权限阶段", notes = "新增用户权限阶段")
    public Object create(@RequestBody UserStepDTO userStepDTO, HttpServletRequest request) {
        log.info("add userStep DTO:{}", userStepDTO);
        try {
            userStepService.saveUserStep(userStepDTO, request);
        } catch (BizException e) {
            log.error("add userStep failed, userStepDTO: {}, error message:{}, error all:{}", userStepDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "UserStep_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改用户权限阶段", notes = "修改用户权限阶段")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody UserStepDTO userStepDTO, HttpServletRequest request) {
        log.info("put modify id:{}, userStep DTO:{}", id, userStepDTO);
        try {
            userStepService.updateUserStep(id, userStepDTO, request);
        } catch (BizException e) {
            log.error("update userStep failed, userStepDTO: {}, error message:{}, error all:{}", userStepDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "UserStep_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户权限阶段", notes = "删除用户权限阶段")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete userStep, id:{}", id);
        try {
            userStepService.logicDeleteUserStep(id, request);
        } catch (BizException e) {
            log.error("delete failed, userStep id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "userManagement_list")
    @ApiOperation(value = "获取用户认证阶段", notes = "获取用户认证阶段")
    @GetMapping(value = "/userStepGet/{userId}")
    public Object userStepGet(@PathVariable("userId") Long userId, HttpServletRequest request) {
        log.info("user step get, userId:{}", userId);
        Map<String, Object> params = new HashMap<>(1);
        params.put("userId", userId);
        List<UserStepDTO> userStepDTOList = userStepService.find(params, null, null);
        return R.success(userStepDTOList);
    }

    @ActionFlag(detail = "userManagement_list")
    @ApiOperation(value = "获取用户认证阶段", notes = "获取用户认证阶段")
    @GetMapping(value = "/userStepLogGet")
    public Object userStepLogGet(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("user step log get, data:{}", params);
        List<UserStepLogDTO> userStepLogDTOList = userStepLogService.find(params, null, null);
        return R.success(userStepLogDTOList);
    }

    @ActionFlag(detail = "userManagement_list")
    @ApiOperation(value = "获取用户认证阶段", notes = "获取用户认证阶段")
    @GetMapping(value = "/findStepLog/{stepId}")
    public Object findStepLog(@PathVariable("stepId") Long stepId, HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("user step log get, data:{}", params);
        List<UserStepLogDTO> userStepLogDTOList = userStepLogService.findStepLog(stepId);
        return R.success(userStepLogDTOList);
    }
    @ActionFlag(detail = "userManagement_list")
    @ApiOperation(value = "获取用户认证阶段", notes = "获取用户认证阶段")
    @GetMapping(value = "/findStepLogNew")
    public Object findStepLogNew(HttpServletRequest request) throws BizException {
        Map<String, Object> params = getConditionsMap(request);
        log.info("user step log get, data:{}", params);
        JSONObject result = userStepLogService.findStepLogNew(params,request);
        return R.success(result);
    }

}
