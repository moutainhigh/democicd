package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.Validator;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.AppAggrementDTO;
import com.uwallet.pay.main.model.entity.AppAggrement;
import com.uwallet.pay.main.service.AppAggrementService;
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
 * app 协议
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: app 协议
 * @author: baixinyue
 * @date: Created in 2019-12-19 11:28:23
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/appAggrement")
@Slf4j
@Api("app 协议")
public class AppAggrementController extends BaseController<AppAggrement> {

    @Autowired
    private AppAggrementService appAggrementService;

    @ActionFlag(detail = "agreementManagement_list")
    @ApiOperation(value = "分页查询app 协议", notes = "分页查询app 协议以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-app 协议列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = appAggrementService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<AppAggrementDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = appAggrementService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询app 协议", notes = "通过id查询app 协议")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get appAggrement Id:{}", id);
        return R.success(appAggrementService.findAppAggrementById(id));
    }

    @ApiOperation(value = "通过查询条件查询app 协议一条数据", notes = "通过查询条件查询app 协议一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询app 协议一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get appAggrement findOne params:{}", params);
        int total = appAggrementService.count(params);
        if (total > 1) {
            log.error("get appAggrement findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        AppAggrementDTO appAggrementDTO = null;
        if (total == 1) {
            appAggrementDTO = appAggrementService.findOneAppAggrement(params);
        }
        return R.success(appAggrementDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增app 协议", notes = "新增app 协议")
    public Object create(@RequestBody AppAggrementDTO appAggrementDTO, HttpServletRequest request) {
        log.info("add appAggrement DTO:{}", appAggrementDTO);
        if (appAggrementDTO.getName() != null && appAggrementDTO.getName().length() > Validator.TEXT_LENGTH_100) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("template.name.length.error", getLang(request)));
        }
        if (appAggrementDTO.getContent() != null && appAggrementDTO.getContent().length() > Validator.TEXT_LENGTH_1000) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("template.length.error", getLang(request)));
        }
        try {
            appAggrementService.saveAppAggrement(appAggrementDTO, request);
        } catch (BizException e) {
            log.error("add appAggrement failed, appAggrementDTO: {}, error message:{}, error all:{}", appAggrementDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改app 协议", notes = "修改app 协议")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody AppAggrementDTO appAggrementDTO, HttpServletRequest request) {
        log.info("put modify id:{}, appAggrement DTO:{}", id, appAggrementDTO);
        try {
            appAggrementService.updateAppAggrement(id, appAggrementDTO, request);
        } catch (BizException e) {
            log.error("update appAggrement failed, appAggrementDTO: {}, error message:{}, error all:{}", appAggrementDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除app 协议", notes = "删除app 协议")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete appAggrement, id:{}", id);
        try {
            appAggrementService.logicDeleteAppAggrement(id, request);
        } catch (BizException e) {
            log.error("delete failed, appAggrement id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
