package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.UserActionButtonDTO;
import com.uwallet.pay.main.model.entity.UserActionButton;
import com.uwallet.pay.main.service.UserActionButtonService;
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
 * 用户冻结表存在该表的用户可以被冻结和解冻
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 用户冻结表存在该表的用户可以被冻结和解冻
 * @author: xucl
 * @date: Created in 2021-09-10 09:35:21
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@RestController
@RequestMapping("/userActionButton")
@Slf4j
@Api("用户冻结表存在该表的用户可以被冻结和解冻")
public class UserActionButtonController extends BaseController<UserActionButton> {

    @Autowired
    private UserActionButtonService userActionButtonService;

    @ActionFlag(detail = "UserActionButton_list")
    @ApiOperation(value = "分页查询用户冻结表存在该表的用户可以被冻结和解冻", notes = "分页查询用户冻结表存在该表的用户可以被冻结和解冻以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-用户冻结表存在该表的用户可以被冻结和解冻列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = userActionButtonService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserActionButtonDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = userActionButtonService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询用户冻结表存在该表的用户可以被冻结和解冻", notes = "通过id查询用户冻结表存在该表的用户可以被冻结和解冻")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get userActionButton Id:{}", id);
        return R.success(userActionButtonService.findUserActionButtonById(id));
    }

    @ApiOperation(value = "通过查询条件查询用户冻结表存在该表的用户可以被冻结和解冻一条数据", notes = "通过查询条件查询用户冻结表存在该表的用户可以被冻结和解冻一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询用户冻结表存在该表的用户可以被冻结和解冻一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get userActionButton findOne params:{}", params);
        int total = userActionButtonService.count(params);
        if (total > 1) {
            log.error("get userActionButton findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        UserActionButtonDTO userActionButtonDTO = null;
        if (total == 1) {
            userActionButtonDTO = userActionButtonService.findOneUserActionButton(params);
        }
        return R.success(userActionButtonDTO);
    }

    @ActionFlag(detail = "UserActionButton_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增用户冻结表存在该表的用户可以被冻结和解冻", notes = "新增用户冻结表存在该表的用户可以被冻结和解冻")
    public Object create(@RequestBody UserActionButtonDTO userActionButtonDTO, HttpServletRequest request) {
        log.info("add userActionButton DTO:{}", userActionButtonDTO);
        try {
            userActionButtonService.saveUserActionButton(userActionButtonDTO, request);
        } catch (BizException e) {
            log.error("add userActionButton failed, userActionButtonDTO: {}, error message:{}, error all:{}", userActionButtonDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "UserActionButton_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改用户冻结表存在该表的用户可以被冻结和解冻", notes = "修改用户冻结表存在该表的用户可以被冻结和解冻")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody UserActionButtonDTO userActionButtonDTO, HttpServletRequest request) {
        log.info("put modify id:{}, userActionButton DTO:{}", id, userActionButtonDTO);
        try {
            userActionButtonService.updateUserActionButton(id, userActionButtonDTO, request);
        } catch (BizException e) {
            log.error("update userActionButton failed, userActionButtonDTO: {}, error message:{}, error all:{}", userActionButtonDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "UserActionButton_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户冻结表存在该表的用户可以被冻结和解冻", notes = "删除用户冻结表存在该表的用户可以被冻结和解冻")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete userActionButton, id:{}", id);
        try {
            userActionButtonService.logicDeleteUserActionButton(id, request);
        } catch (BizException e) {
            log.error("delete failed, userActionButton id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
