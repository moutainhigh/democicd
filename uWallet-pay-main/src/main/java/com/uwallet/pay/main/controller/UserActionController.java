package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.UserActionDTO;
import com.uwallet.pay.main.model.entity.UserAction;
import com.uwallet.pay.main.service.UserActionService;
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
 * 商户端用户-权限关系表
 * </p>
 *
 * @package:  com.uwallet.pay.main.main.controller
 * @description: 商户端用户-权限关系表
 * @author: baixinyue
 * @date: Created in 2020-02-19 14:02:21
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/userAction")
@Slf4j
@Api("商户端用户-权限关系表")
public class UserActionController extends BaseController<UserAction> {

    @Autowired
    private UserActionService userActionService;

    @ActionFlag(detail = "UserAction_list")
    @ApiOperation(value = "分页查询商户端用户-权限关系表", notes = "分页查询商户端用户-权限关系表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-商户端用户-权限关系表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = userActionService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserActionDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = userActionService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询商户端用户-权限关系表", notes = "通过id查询商户端用户-权限关系表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get userAction Id:{}", id);
        return R.success(userActionService.findUserActionById(id));
    }

    @ApiOperation(value = "通过查询条件查询商户端用户-权限关系表一条数据", notes = "通过查询条件查询商户端用户-权限关系表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询商户端用户-权限关系表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get userAction findOne params:{}", params);
        int total = userActionService.count(params);
        if (total > 1) {
            log.error("get userAction findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        UserActionDTO userActionDTO = null;
        if (total == 1) {
            userActionDTO = userActionService.findOneUserAction(params);
        }
        return R.success(userActionDTO);
    }

    @ActionFlag(detail = "UserAction_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增商户端用户-权限关系表", notes = "新增商户端用户-权限关系表")
    public Object create(@RequestBody UserActionDTO userActionDTO, HttpServletRequest request) {
        log.info("add userAction DTO:{}", userActionDTO);
        try {
            userActionService.saveUserAction(userActionDTO, request);
        } catch (BizException e) {
            log.error("add userAction failed, userActionDTO: {}, error message:{}, error all:{}", userActionDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "UserAction_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改商户端用户-权限关系表", notes = "修改商户端用户-权限关系表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody UserActionDTO userActionDTO, HttpServletRequest request) {
        log.info("put modify id:{}, userAction DTO:{}", id, userActionDTO);
        try {
            userActionService.updateUserAction(id, userActionDTO, request);
        } catch (BizException e) {
            log.error("update userAction failed, userActionDTO: {}, error message:{}, error all:{}", userActionDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "UserAction_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除商户端用户-权限关系表", notes = "删除商户端用户-权限关系表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete userAction, id:{}", id);
        try {
            userActionService.logicDeleteUserAction(id, request);
        } catch (BizException e) {
            log.error("delete failed, userAction id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
