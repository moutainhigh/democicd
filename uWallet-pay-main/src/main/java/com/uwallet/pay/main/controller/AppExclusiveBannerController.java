package com.uwallet.pay.main.controller;

import com.google.common.collect.Maps;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.AppExclusiveBannerDTO;
import com.uwallet.pay.main.model.entity.AppExclusiveBanner;
import com.uwallet.pay.main.service.AppExclusiveBannerService;
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
 * APP首页banner、市场推广图片配置表
 * </p>
 *
 * @package: com.uwallet.pay.main.model.generator.controller
 * @description: APP首页banner、市场推广图片配置表
 * @author: zhangzeyuan
 * @date: Created in 2021-04-12 14:08:04
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@RestController
@RequestMapping("/appExclusiveBanner")
@Slf4j
@Api("APP首页banner、市场推广图片配置表")
public class AppExclusiveBannerController extends BaseController<AppExclusiveBanner> {

    @Autowired
    private AppExclusiveBannerService appExclusiveBannerService;

    @ActionFlag(detail = "AppExclusiveBanner_list")
    @ApiOperation(value = "分页查询APP首页banner、市场推广图片配置表", notes = "分页查询APP首页banner、市场推广图片配置表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-APP首页banner、市场推广图片配置表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = appExclusiveBannerService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<AppExclusiveBannerDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = appExclusiveBannerService.find(params, scs, pc);
            //查询可用的数量
            /*params.put("state", 1);
            int enbleTotal = appExclusiveBannerService.count(params);

            if(enbleTotal > 0){
                list.get(enbleTotal - 1).setEnableTotal(enbleTotal);
            }*/
        }

        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询APP首页banner、市场推广图片配置表", notes = "通过id查询APP首页banner、市场推广图片配置表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name = "详情")

    public Object view(@PathVariable("id") Long id) {
        log.info("get appExclusiveBanner Id:{}", id);
        return R.success(appExclusiveBannerService.findAppExclusiveBannerById(id));
    }

    @ApiOperation(value = "通过查询条件查询APP首页banner、市场推广图片配置表一条数据", notes = "通过查询条件查询APP首页banner、市场推广图片配置表一条数据")
    @GetMapping(value = "/findOne", name = "通过查询条件查询APP首页banner、市场推广图片配置表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get appExclusiveBanner findOne params:{}", params);
        int total = appExclusiveBannerService.count(params);
        if (total > 1) {
            log.error("get appExclusiveBanner findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        AppExclusiveBannerDTO appExclusiveBannerDTO = null;
        if (total == 1) {
            appExclusiveBannerDTO = appExclusiveBannerService.findOneAppExclusiveBanner(params);
        }
        return R.success(appExclusiveBannerDTO);
    }

    @ActionFlag(detail = "AppExclusiveBanner_list")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增APP首页banner、市场推广图片配置表", notes = "新增APP首页banner、市场推广图片配置表")
    public Object create(@RequestBody AppExclusiveBannerDTO appExclusiveBannerDTO, HttpServletRequest request) {
        log.info("add appExclusiveBanner DTO:{}", appExclusiveBannerDTO);
        try {
            appExclusiveBannerService.saveAppExclusiveBanner(appExclusiveBannerDTO, request);
        } catch (BizException e) {
            log.error("add appExclusiveBanner failed, appExclusiveBannerDTO: {}, error message:{}, error all:{}", appExclusiveBannerDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "AppExclusiveBanner_list")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改APP首页banner、市场推广图片配置表", notes = "修改APP首页banner、市场推广图片配置表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody AppExclusiveBannerDTO appExclusiveBannerDTO, HttpServletRequest request) {
        log.info("put modify id:{}, appExclusiveBanner DTO:{}", id, appExclusiveBannerDTO);
        try {
            appExclusiveBannerService.updateAppExclusiveBanner(id, appExclusiveBannerDTO, request);
        } catch (BizException e) {
            log.error("update appExclusiveBanner failed, appExclusiveBannerDTO: {}, error message:{}, error all:{}", appExclusiveBannerDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "AppExclusiveBanner_list")
    @DeleteMapping("/{id}/{type}")
    @ApiOperation(value = "删除APP首页banner、市场推广图片配置表", notes = "删除APP首页banner、市场推广图片配置表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, @PathVariable("type") Integer type, HttpServletRequest request) {
        log.info("delete appExclusiveBanner, id:{}", id);
        try {
            appExclusiveBannerService.logicDeleteAppExclusiveBanner(id, type, request);
        } catch (BizException e) {
            log.error("delete failed, appExclusiveBanner id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }


    /**
     * 修改banner可用 不可用状态
     *
     * @param id
     * @param appExclusiveBannerDTO
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/4/12 14:49
     */
    @PutMapping("/changeStatus/{id}")
    public Object changeEnableStatus(@PathVariable("id") Long id, @RequestBody AppExclusiveBannerDTO appExclusiveBannerDTO, HttpServletRequest request) {
        log.info("修改banner可用 不可用状态接口、modify id:{}, appExclusiveBanner DTO:{}", id, appExclusiveBannerDTO);
        try {
            appExclusiveBannerService.updateBannerEnableStatus(id, appExclusiveBannerDTO, request);
        } catch (BizException e) {
            log.error("修改banner可用 不可用状态接口 failed, appExclusiveBannerDTO: {}, error message:{}, error all:{}", appExclusiveBannerDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
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
            appExclusiveBannerService.moveUpOrDown(id, upOrDown, request);
        } catch (BizException e) {
            log.error("shift failed, appBanner id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }


    /**
     * 新增获取下一个order
     *
     * @param request
     * @param type
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/4/27 15:38
     */
    @GetMapping("/getNextAddOrder/{type}")
    public Object getNextAddOrder(HttpServletRequest request, @PathVariable("type") Integer type) {
        try {
            return R.success(appExclusiveBannerService.getAddNextOrder(type));
        } catch (Exception e) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

}
