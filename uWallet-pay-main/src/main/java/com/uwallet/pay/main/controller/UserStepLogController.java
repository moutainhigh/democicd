package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.UserStepLogDTO;
import com.uwallet.pay.main.model.entity.UserStepLog;
import com.uwallet.pay.main.service.UserStepLogService;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.main.util.I18nUtils;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;


/**
 * <p>
 * 用户权限阶段记录
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 用户权限阶段记录
 * @author: baixinyue
 * @date: Created in 2020-06-30 16:52:45
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/userStepLog")
@Slf4j
@Api("用户权限阶段记录")
public class UserStepLogController extends BaseController<UserStepLog> {

    @Autowired
    private UserStepLogService userStepLogService;

    @ActionFlag(detail = "UserStepLog_list")
    @ApiOperation(value = "分页查询用户权限阶段记录", notes = "分页查询用户权限阶段记录以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-用户权限阶段记录列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = userStepLogService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserStepLogDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = userStepLogService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询用户权限阶段记录", notes = "通过id查询用户权限阶段记录")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get userStepLog Id:{}", id);
        return R.success(userStepLogService.findUserStepLogById(id));
    }

    @ApiOperation(value = "通过查询条件查询用户权限阶段记录一条数据", notes = "通过查询条件查询用户权限阶段记录一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询用户权限阶段记录一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get userStepLog findOne params:{}", params);
        int total = userStepLogService.count(params);
        if (total > 1) {
            log.error("get userStepLog findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        UserStepLogDTO userStepLogDTO = null;
        if (total == 1) {
            userStepLogDTO = userStepLogService.findOneUserStepLog(params);
        }
        return R.success(userStepLogDTO);
    }

    @ActionFlag(detail = "UserStepLog_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增用户权限阶段记录", notes = "新增用户权限阶段记录")
    public Object create(@RequestBody UserStepLogDTO userStepLogDTO, HttpServletRequest request) {
        log.info("add userStepLog DTO:{}", userStepLogDTO);
        try {
            userStepLogService.saveUserStepLog(userStepLogDTO, request);
        } catch (BizException e) {
            log.error("add userStepLog failed, userStepLogDTO: {}, error message:{}, error all:{}", userStepLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "UserStepLog_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改用户权限阶段记录", notes = "修改用户权限阶段记录")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody UserStepLogDTO userStepLogDTO, HttpServletRequest request) {
        log.info("put modify id:{}, userStepLog DTO:{}", id, userStepLogDTO);
        try {
            userStepLogService.updateUserStepLog(id, userStepLogDTO, request);
        } catch (BizException e) {
            log.error("update userStepLog failed, userStepLogDTO: {}, error message:{}, error all:{}", userStepLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "UserStepLog_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户权限阶段记录", notes = "删除用户权限阶段记录")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete userStepLog, id:{}", id);
        try {
            userStepLogService.logicDeleteUserStepLog(id, request);
        } catch (BizException e) {
            log.error("delete failed, userStepLog id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
