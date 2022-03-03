package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.AppCustomCategoryDisplayDTO;
import com.uwallet.pay.main.model.entity.AppCustomCategoryDisplay;
import com.uwallet.pay.main.service.AppCustomCategoryDisplayService;
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
 * APP首页自定义分类展示信息
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: APP首页自定义分类展示信息
 * @author: zhangzeyuan
 * @date: Created in 2021-04-13 15:09:22
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@RestController
@RequestMapping("/appCustomCategoryDisplay")
@Slf4j
@Api("APP首页自定义分类展示信息")
public class AppCustomCategoryDisplayController extends BaseController<AppCustomCategoryDisplay> {

    @Autowired
    private AppCustomCategoryDisplayService appCustomCategoryDisplayService;

    @ActionFlag(detail = "AppCustomCategoryDisplay_list")
    @ApiOperation(value = "分页查询APP首页自定义分类展示信息", notes = "分页查询APP首页自定义分类展示信息以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-APP首页自定义分类展示信息列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = appCustomCategoryDisplayService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<AppCustomCategoryDisplayDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = appCustomCategoryDisplayService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询APP首页自定义分类展示信息", notes = "通过id查询APP首页自定义分类展示信息")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get appCustomCategoryDisplay Id:{}", id);
        return R.success(appCustomCategoryDisplayService.findAppCustomCategoryDisplayById(id));
    }

    @ApiOperation(value = "通过查询条件查询APP首页自定义分类展示信息一条数据", notes = "通过查询条件查询APP首页自定义分类展示信息一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询APP首页自定义分类展示信息一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get appCustomCategoryDisplay findOne params:{}", params);
        int total = appCustomCategoryDisplayService.count(params);
        if (total > 1) {
            log.error("get appCustomCategoryDisplay findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        AppCustomCategoryDisplayDTO appCustomCategoryDisplayDTO = null;
        if (total == 1) {
            appCustomCategoryDisplayDTO = appCustomCategoryDisplayService.findOneAppCustomCategoryDisplay(params);
        }
        return R.success(appCustomCategoryDisplayDTO);
    }

    @ActionFlag(detail = "AppCustomCategoryDisplay_list")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增APP首页自定义分类展示信息", notes = "新增APP首页自定义分类展示信息")
    public Object create(@RequestBody AppCustomCategoryDisplayDTO appCustomCategoryDisplayDTO, HttpServletRequest request) {
        log.info("add appCustomCategoryDisplay DTO:{}", appCustomCategoryDisplayDTO);
        try {
            appCustomCategoryDisplayService.saveAppCustomCategoryDisplay(appCustomCategoryDisplayDTO, request);
        } catch (BizException e) {
            log.error("add appCustomCategoryDisplay failed, appCustomCategoryDisplayDTO: {}, error message:{}, error all:{}", appCustomCategoryDisplayDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "AppCustomCategoryDisplay_list")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改APP首页自定义分类展示信息", notes = "修改APP首页自定义分类展示信息")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody AppCustomCategoryDisplayDTO appCustomCategoryDisplayDTO, HttpServletRequest request) {
        log.info("put modify id:{}, appCustomCategoryDisplay DTO:{}", id, appCustomCategoryDisplayDTO);
        try {
            appCustomCategoryDisplayService.updateAppCustomCategoryDisplay(id, appCustomCategoryDisplayDTO, request);
        } catch (BizException e) {
            log.error("update appCustomCategoryDisplay failed, appCustomCategoryDisplayDTO: {}, error message:{}, error all:{}", appCustomCategoryDisplayDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "AppCustomCategoryDisplay_list")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除APP首页自定义分类展示信息", notes = "删除APP首页自定义分类展示信息")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete appCustomCategoryDisplay, id:{}", id);
        try {
            appCustomCategoryDisplayService.logicDeleteAppCustomCategoryDisplay(id, request);
        } catch (BizException e) {
            log.error("delete failed, appCustomCategoryDisplay id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }



    @ApiOperation(value = "上移、下移操作", notes = "上移、下移操作")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "被操作id", paramType = "path"),
            @ApiImplicitParam(name = "upOrDown", value = "0：上移 1：下移", paramType = "path")
    })
    @PutMapping("/move/{id}/{upOrDown}")
    public Object moveUpOrdown(@PathVariable("id") Long id, @PathVariable("upOrDown") Integer upOrDown, HttpServletRequest request) {
        log.info("APP banner 上移、下移操作, id:{}", id);
        try {
            appCustomCategoryDisplayService.moveUpOrDown(id, upOrDown, request);
        } catch (BizException e) {
            log.error("shift failed, appBanner id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }


}
