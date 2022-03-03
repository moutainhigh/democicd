package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.UserEnterAppPageLogDTO;
import com.uwallet.pay.main.model.entity.UserEnterAppPageLog;
import com.uwallet.pay.main.service.UserEnterAppPageLogService;
import com.uwallet.pay.main.util.I18nUtils;
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
 * 用户APP页面流程记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 用户APP页面流程记录表
 * @author: zhangzeyuan
 * @date: Created in 2021-09-01 16:35:17
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@RestController
@RequestMapping("/userEnterAppPageLog")
@Slf4j
@Api("用户APP页面流程记录表")
public class UserEnterAppPageLogController extends BaseController<UserEnterAppPageLog> {

    @Autowired
    private UserEnterAppPageLogService userEnterAppPageLogService;

    @ActionFlag(detail = "UserEnterAppPageLog_list")
    @ApiOperation(value = "分页查询用户APP页面流程记录表", notes = "分页查询用户APP页面流程记录表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-用户APP页面流程记录表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = userEnterAppPageLogService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserEnterAppPageLogDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = userEnterAppPageLogService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询用户APP页面流程记录表", notes = "通过id查询用户APP页面流程记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get userEnterAppPageLog Id:{}", id);
        return R.success(userEnterAppPageLogService.findUserEnterAppPageLogById(id));
    }

    @ApiOperation(value = "通过查询条件查询用户APP页面流程记录表一条数据", notes = "通过查询条件查询用户APP页面流程记录表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询用户APP页面流程记录表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get userEnterAppPageLog findOne params:{}", params);
        int total = userEnterAppPageLogService.count(params);
        if (total > 1) {
            log.error("get userEnterAppPageLog findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        UserEnterAppPageLogDTO userEnterAppPageLogDTO = null;
        if (total == 1) {
            userEnterAppPageLogDTO = userEnterAppPageLogService.findOneUserEnterAppPageLog(params);
        }
        return R.success(userEnterAppPageLogDTO);
    }

    @ActionFlag(detail = "UserEnterAppPageLog_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增用户APP页面流程记录表", notes = "新增用户APP页面流程记录表")
    public Object create(@RequestBody UserEnterAppPageLogDTO userEnterAppPageLogDTO, HttpServletRequest request) {
        log.info("add userEnterAppPageLog DTO:{}", userEnterAppPageLogDTO);
        try {
            userEnterAppPageLogService.saveUserEnterAppPageLog(userEnterAppPageLogDTO, request);
        } catch (BizException e) {
            log.error("add userEnterAppPageLog failed, userEnterAppPageLogDTO: {}, error message:{}, error all:{}", userEnterAppPageLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "UserEnterAppPageLog_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改用户APP页面流程记录表", notes = "修改用户APP页面流程记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody UserEnterAppPageLogDTO userEnterAppPageLogDTO, HttpServletRequest request) {
        log.info("put modify id:{}, userEnterAppPageLog DTO:{}", id, userEnterAppPageLogDTO);
        try {
            userEnterAppPageLogService.updateUserEnterAppPageLog(id, userEnterAppPageLogDTO, request);
        } catch (BizException e) {
            log.error("update userEnterAppPageLog failed, userEnterAppPageLogDTO: {}, error message:{}, error all:{}", userEnterAppPageLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "UserEnterAppPageLog_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户APP页面流程记录表", notes = "删除用户APP页面流程记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete userEnterAppPageLog, id:{}", id);
        try {
            userEnterAppPageLogService.logicDeleteUserEnterAppPageLog(id, request);
        } catch (BizException e) {
            log.error("delete failed, userEnterAppPageLog id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
