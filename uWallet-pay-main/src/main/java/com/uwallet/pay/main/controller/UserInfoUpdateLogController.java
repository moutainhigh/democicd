package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.UserInfoUpdateLogDTO;
import com.uwallet.pay.main.model.entity.UserInfoUpdateLog;
import com.uwallet.pay.main.service.UserInfoUpdateLogService;
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
 * 用户信息修改记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 用户信息修改记录表
 * @author: xucl
 * @date: Created in 2021-09-10 16:55:37
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@RestController
@RequestMapping("/userInfoUpdateLog")
@Slf4j
@Api("用户信息修改记录表")
public class UserInfoUpdateLogController extends BaseController<UserInfoUpdateLog> {

    @Autowired
    private UserInfoUpdateLogService userInfoUpdateLogService;

    @ActionFlag(detail = "UserInfoUpdateLog_list")
    @ApiOperation(value = "分页查询用户信息修改记录表", notes = "分页查询用户信息修改记录表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-用户信息修改记录表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = userInfoUpdateLogService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserInfoUpdateLogDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = userInfoUpdateLogService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询用户信息修改记录表", notes = "通过id查询用户信息修改记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get userInfoUpdateLog Id:{}", id);
        return R.success(userInfoUpdateLogService.findUserInfoUpdateLogById(id));
    }

    @ApiOperation(value = "通过查询条件查询用户信息修改记录表一条数据", notes = "通过查询条件查询用户信息修改记录表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询用户信息修改记录表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get userInfoUpdateLog findOne params:{}", params);
        int total = userInfoUpdateLogService.count(params);
        if (total > 1) {
            log.error("get userInfoUpdateLog findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        UserInfoUpdateLogDTO userInfoUpdateLogDTO = null;
        if (total == 1) {
            userInfoUpdateLogDTO = userInfoUpdateLogService.findOneUserInfoUpdateLog(params);
        }
        return R.success(userInfoUpdateLogDTO);
    }

    @ActionFlag(detail = "UserInfoUpdateLog_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增用户信息修改记录表", notes = "新增用户信息修改记录表")
    public Object create(@RequestBody UserInfoUpdateLogDTO userInfoUpdateLogDTO, HttpServletRequest request) {
        log.info("add userInfoUpdateLog DTO:{}", userInfoUpdateLogDTO);
        try {
            userInfoUpdateLogService.saveUserInfoUpdateLog(userInfoUpdateLogDTO, request);
        } catch (BizException e) {
            log.error("add userInfoUpdateLog failed, userInfoUpdateLogDTO: {}, error message:{}, error all:{}", userInfoUpdateLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "UserInfoUpdateLog_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改用户信息修改记录表", notes = "修改用户信息修改记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody UserInfoUpdateLogDTO userInfoUpdateLogDTO, HttpServletRequest request) {
        log.info("put modify id:{}, userInfoUpdateLog DTO:{}", id, userInfoUpdateLogDTO);
        try {
            userInfoUpdateLogService.updateUserInfoUpdateLog(id, userInfoUpdateLogDTO, request);
        } catch (BizException e) {
            log.error("update userInfoUpdateLog failed, userInfoUpdateLogDTO: {}, error message:{}, error all:{}", userInfoUpdateLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "UserInfoUpdateLog_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户信息修改记录表", notes = "删除用户信息修改记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete userInfoUpdateLog, id:{}", id);
        try {
            userInfoUpdateLogService.logicDeleteUserInfoUpdateLog(id, request);
        } catch (BizException e) {
            log.error("delete failed, userInfoUpdateLog id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
