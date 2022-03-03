package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.Validator;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.AppBannerDTO;
import com.uwallet.pay.main.model.entity.AppBanner;
import com.uwallet.pay.main.service.AppBannerService;
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
 * app banner
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: app banner
 * @author: baixinyue
 * @date: Created in 2019-12-19 11:29:08
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/appBanner")
@Slf4j
@Api("app banner")
public class AppBannerController extends BaseController<AppBanner> {

    @Autowired
    private AppBannerService appBannerService;

    @ActionFlag(detail = "banner_list")
    @ApiOperation(value = "分页查询app banner", notes = "分页查询app banner以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-app banner列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = appBannerService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<AppBannerDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = appBannerService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询app banner", notes = "通过id查询app banner")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get appBanner Id:{}", id);
        return R.success(appBannerService.findAppBannerById(id));
    }

    @ApiOperation(value = "通过查询条件查询app banner一条数据", notes = "通过查询条件查询app banner一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询app banner一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get appBanner findOne params:{}", params);
        int total = appBannerService.count(params);
        if (total > 1) {
            log.error("get appBanner findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        AppBannerDTO appBannerDTO = null;
        if (total == 1) {
            appBannerDTO = appBannerService.findOneAppBanner(params);
        }
        return R.success(appBannerDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增app banner", notes = "新增app banner")
    public Object create(@RequestBody AppBannerDTO appBannerDTO, HttpServletRequest request) {
        log.info("add appBanner DTO:{}", appBannerDTO);
        if (appBannerDTO.getName() != null && appBannerDTO.getName().length() > Validator.TEXT_LENGTH_100) {
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("banner.name.over.limit", getLang(request)));
        }
        if (appBannerDTO.getSkipRoute() != null && appBannerDTO.getSkipRoute().length() > Validator.TEXT_LENGTH_100) {
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("banner.url.over.limit", getLang(request)));
        }
        try {
            appBannerService.saveAppBanner(appBannerDTO, request);
        } catch (BizException e) {
            log.error("add appBanner failed, appBannerDTO: {}, error message:{}, error all:{}", appBannerDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }


    @PutMapping("/{id}")
    @ApiOperation(value = "修改app banner", notes = "修改app banner")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody AppBannerDTO appBannerDTO, HttpServletRequest request) {
        log.info("put modify id:{}, appBanner DTO:{}", id, appBannerDTO);
        if (appBannerDTO.getName() != null && appBannerDTO.getName().length() > Validator.TEXT_LENGTH_100) {
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("banner.name.over.limit", getLang(request)));
        }
        try {
            appBannerService.updateAppBanner(id, appBannerDTO, request);
        } catch (BizException e) {
            log.error("update appBanner failed, appBannerDTO: {}, error message:{}, error all:{}", appBannerDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除app banner", notes = "删除app banner")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete appBanner, id:{}", id);
        try {
            appBannerService.logicDeleteAppBanner(id, request);
        } catch (BizException e) {
            log.error("delete failed, appBanner id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "上移、下移操作", notes = "上移、下移操作")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "被操作id", paramType = "path"),
            @ApiImplicitParam(name = "upOrDown", value = "0：上移 1：下移", paramType = "path")
    })
    @PutMapping("/shift/{id}/{upOrDown}")
    public Object shiftUpOrDown(@PathVariable("id") Long id, @PathVariable("upOrDown") Integer upOrDown, HttpServletRequest request) {
        log.info("shift appBanner, id:{}", id);
        try {
            appBannerService.shiftUpOrDown(id, upOrDown, request);
        } catch (BizException e) {
            log.error("shift failed, appBanner id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

}
