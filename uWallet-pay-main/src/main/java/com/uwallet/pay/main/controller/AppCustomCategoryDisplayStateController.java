package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONArray;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.AppCustomCategoryDisplayStateDTO;
import com.uwallet.pay.main.model.dto.AppCustomCategoryImageDTO;
import com.uwallet.pay.main.model.dto.MerchantAppHomePageDTO;
import com.uwallet.pay.main.model.dto.MerchantDTO;
import com.uwallet.pay.main.model.entity.AppCustomCategoryDisplayState;
import com.uwallet.pay.main.service.AppCustomCategoryDisplayStateService;
import com.uwallet.pay.main.service.MerchantService;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


/**
 * <p>
 * APP首页自定义分类 每个州展示商户、图片信息
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: APP首页自定义分类 每个州展示商户、图片信息
 * @author: zhangzeyuan
 * @date: Created in 2021-04-13 15:23:57
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@RestController
@RequestMapping("/appCustomCategoryDisplayState")
@Slf4j
@Api("APP首页自定义分类 每个州展示商户、图片信息")
public class AppCustomCategoryDisplayStateController extends BaseController<AppCustomCategoryDisplayState> {

    @Autowired
    private AppCustomCategoryDisplayStateService appCustomCategoryDisplayStateService;

    @Autowired
    private MerchantService merchantService;

    @ActionFlag(detail = "AppCustomCategoryDisplay_list")
    @ApiOperation(value = "分页查询APP首页自定义分类 每个州展示商户、图片信息", notes = "分页查询APP首页自定义分类 每个州展示商户、图片信息以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-APP首页自定义分类 每个州展示商户、图片信息列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = appCustomCategoryDisplayStateService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<AppCustomCategoryDisplayStateDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = appCustomCategoryDisplayStateService.find(params, scs, pc);
            //处理商户数据
            for(AppCustomCategoryDisplayStateDTO dto : list){
                String merchantIds = dto.getMerchantIds();
                if(StringUtils.isBlank(merchantIds)){
                    continue;
                }
                List<MerchantAppHomePageDTO> merchantData = merchantService.listCustomStateMerchantDataByIds(merchantIds);
                dto.setMerchantList(merchantData);
            }
            //处理图片数据

        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询APP首页自定义分类 每个州展示商户、图片信息", notes = "通过id查询APP首页自定义分类 每个州展示商户、图片信息")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get appCustomCategoryDisplayState Id:{}", id);
        return R.success(appCustomCategoryDisplayStateService.findAppCustomCategoryDisplayStateById(id));
    }


    @ApiOperation(value = "通过id查询APP首页自定义分类 每个州展示商户、图片信息", notes = "通过id查询APP首页自定义分类 每个州展示商户、图片信息")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/review/{id}", name="详情")
    public Object reviewAll(@PathVariable("id") Long id) {
        log.info("get appCustomCategoryDisplayState Id:{}", id);
        AppCustomCategoryDisplayStateDTO data = appCustomCategoryDisplayStateService.findAppCustomCategoryDisplayStateById(id);
        if(Objects.nonNull(data)){
            //处理图片数据
            String imagesJson = data.getImagesJson();
            if(StringUtils.isNotBlank(imagesJson)){
                List<AppCustomCategoryImageDTO> imageList = JSONArray.parseArray(imagesJson, AppCustomCategoryImageDTO.class);
                data.setImageList(imageList);
            }
            //处理商户数据
            String merchantIds = data.getMerchantIds();
            if(StringUtils.isNotBlank(merchantIds)){
                List<MerchantAppHomePageDTO> merchantData = merchantService.listCustomStateMerchantDataByIds(merchantIds);
                data.setMerchantList(merchantData);;
            }
        }
        return R.success(data);
    }


    @ApiOperation(value = "通过查询条件查询APP首页自定义分类 每个州展示商户、图片信息一条数据", notes = "通过查询条件查询APP首页自定义分类 每个州展示商户、图片信息一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询APP首页自定义分类 每个州展示商户、图片信息一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get appCustomCategoryDisplayState findOne params:{}", params);
        int total = appCustomCategoryDisplayStateService.count(params);
        if (total > 1) {
            log.error("get appCustomCategoryDisplayState findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        AppCustomCategoryDisplayStateDTO appCustomCategoryDisplayStateDTO = null;
        if (total == 1) {
            appCustomCategoryDisplayStateDTO = appCustomCategoryDisplayStateService.findOneAppCustomCategoryDisplayState(params);
        }
        return R.success(appCustomCategoryDisplayStateDTO);
    }

    @ActionFlag(detail = "AppCustomCategoryDisplay_list")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增APP首页自定义分类 每个州展示商户、图片信息", notes = "新增APP首页自定义分类 每个州展示商户、图片信息")
    public Object create(@RequestBody AppCustomCategoryDisplayStateDTO appCustomCategoryDisplayStateDTO, HttpServletRequest request) {
        log.info("add appCustomCategoryDisplayState DTO:{}", appCustomCategoryDisplayStateDTO);
        try {
            appCustomCategoryDisplayStateService.saveAppCustomCategoryDisplayState(appCustomCategoryDisplayStateDTO, request);
        } catch (BizException e) {
            log.error("add appCustomCategoryDisplayState failed, appCustomCategoryDisplayStateDTO: {}, error message:{}, error all:{}", appCustomCategoryDisplayStateDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "AppCustomCategoryDisplay_list")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改APP首页自定义分类 每个州展示商户、图片信息", notes = "修改APP首页自定义分类 每个州展示商户、图片信息")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody AppCustomCategoryDisplayStateDTO appCustomCategoryDisplayStateDTO, HttpServletRequest request) {
        log.info("put modify id:{}, appCustomCategoryDisplayState DTO:{}", id, appCustomCategoryDisplayStateDTO);
        try {
            appCustomCategoryDisplayStateService.updateAppCustomCategoryDisplayState(id, appCustomCategoryDisplayStateDTO, request);
        } catch (BizException e) {
            log.error("update appCustomCategoryDisplayState failed, appCustomCategoryDisplayStateDTO: {}, error message:{}, error all:{}", appCustomCategoryDisplayStateDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "AppCustomCategoryDisplayState_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除APP首页自定义分类 每个州展示商户、图片信息", notes = "删除APP首页自定义分类 每个州展示商户、图片信息")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete appCustomCategoryDisplayState, id:{}", id);
        try {
            appCustomCategoryDisplayStateService.logicDeleteAppCustomCategoryDisplayState(id, request);
        } catch (BizException e) {
            log.error("delete failed, appCustomCategoryDisplayState id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    //@ActionFlag(detail = "AppCustomCategoryDisplay_list")
    @GetMapping("/definition/{id}/{merchantDisplayType}")
    @ApiOperation(value = "修改自定义或者距离", notes = "修改自定义或者距离")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true),
            @ApiImplicitParam(name = "merchantDisplayType", value = "merchantDisplayType", dataType = "Integer", paramType = "path", required = true)
    })
    public Object updateDefinition(@PathVariable("id") Long id, @PathVariable("merchantDisplayType") Integer merchantDisplayType,HttpServletRequest request) throws BizException {
        log.info("get modify id:{}, appCustomCategoryDisplayState DTO:{}", id,merchantDisplayType);
        appCustomCategoryDisplayStateService.updatDefinition(id, merchantDisplayType);
        AppCustomCategoryDisplayStateDTO data = appCustomCategoryDisplayStateService.findAppCustomCategoryDisplayStateById(id);
        if(Objects.nonNull(data)){
            //处理图片数据
            String imagesJson = data.getImagesJson();
            if(StringUtils.isNotBlank(imagesJson)){
                List<AppCustomCategoryImageDTO> imageList = JSONArray.parseArray(imagesJson, AppCustomCategoryImageDTO.class);
                data.setImageList(imageList);
            }
            //处理商户数据
            String merchantIds = data.getMerchantIds();
            if(StringUtils.isNotBlank(merchantIds)){
                List<MerchantAppHomePageDTO> merchantData = merchantService.listCustomStateMerchantDataByIds(merchantIds);
                data.setMerchantList(merchantData);;
            }
        }
        return R.success(data);
    }
}
