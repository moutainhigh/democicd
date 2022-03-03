package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.ClearFlowDetailDTO;
import com.uwallet.pay.main.model.entity.ClearFlowDetail;
import com.uwallet.pay.main.service.ClearFlowDetailService;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.controller.BaseController;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.uwallet.pay.main.util.I18nUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;


/**
 * <p>
 * 用户主表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 用户主表
 * @author: zhoutt
 * @date: Created in 2020-02-13 11:56:28
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/clearFlowDetail")
@Slf4j
@Api("用户主表")
public class ClearFlowDetailController extends BaseController<ClearFlowDetail> {

    @Autowired
    private ClearFlowDetailService clearFlowDetailService;

    @ActionFlag(detail = "ClearFlowDetail_list")
    @ApiOperation(value = "分页查询用户主表", notes = "分页查询用户主表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-用户主表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = clearFlowDetailService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ClearFlowDetailDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = clearFlowDetailService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询用户主表", notes = "通过id查询用户主表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get clearFlowDetail Id:{}", id);
        return R.success(clearFlowDetailService.findClearFlowDetailById(id));
    }

    @ApiOperation(value = "通过查询条件查询用户主表一条数据", notes = "通过查询条件查询用户主表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询用户主表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get clearFlowDetail findOne params:{}", params);
        int total = clearFlowDetailService.count(params);
        if (total > 1) {
            log.error("get clearFlowDetail findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        ClearFlowDetailDTO clearFlowDetailDTO = null;
        if (total == 1) {
            clearFlowDetailDTO = clearFlowDetailService.findOneClearFlowDetail(params);
        }
        return R.success(clearFlowDetailDTO);
    }

    @ActionFlag(detail = "ClearFlowDetail_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增用户主表", notes = "新增用户主表")
    public Object create(@RequestBody ClearFlowDetailDTO clearFlowDetailDTO, HttpServletRequest request) {
        log.info("add clearFlowDetail DTO:{}", clearFlowDetailDTO);
        try {
            clearFlowDetailService.saveClearFlowDetail(clearFlowDetailDTO, request);
        } catch (BizException e) {
            log.error("add clearFlowDetail failed, clearFlowDetailDTO: {}, error message:{}, error all:{}", clearFlowDetailDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ClearFlowDetail_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改用户主表", notes = "修改用户主表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody ClearFlowDetailDTO clearFlowDetailDTO, HttpServletRequest request) {
        log.info("put modify id:{}, clearFlowDetail DTO:{}", id, clearFlowDetailDTO);
        try {
            clearFlowDetailService.updateClearFlowDetail(id, clearFlowDetailDTO, request);
        } catch (BizException e) {
            log.error("update clearFlowDetail failed, clearFlowDetailDTO: {}, error message:{}, error all:{}", clearFlowDetailDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ClearFlowDetail_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户主表", notes = "删除用户主表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete clearFlowDetail, id:{}", id);
        try {
            clearFlowDetailService.logicDeleteClearFlowDetail(id, request);
        } catch (BizException e) {
            log.error("delete failed, clearFlowDetail id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
