package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.AccessPlatformDTO;
import com.uwallet.pay.main.model.dto.AccessPlatformInfoDTO;
import com.uwallet.pay.main.model.entity.AccessPlatform;
import com.uwallet.pay.main.service.AccessPlatformService;
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
 * 接入方平台表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 接入方平台表
 * @author: zhoutt
 * @date: Created in 2020-09-25 08:55:53
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/accessPlatform")
@Slf4j
@Api("接入方平台表")
public class AccessPlatformController extends BaseController<AccessPlatform> {

    @Autowired
    private AccessPlatformService accessPlatformService;

    @ActionFlag(detail = "AccessPlatform_list")
    @ApiOperation(value = "分页查询接入方平台表", notes = "分页查询接入方平台表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-接入方平台表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = accessPlatformService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<AccessPlatformDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = accessPlatformService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询接入方平台表", notes = "通过id查询接入方平台表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get accessPlatform Id:{}", id);
        return R.success(accessPlatformService.findAccessPlatformById(id));
    }

    @ApiOperation(value = "通过查询条件查询接入方平台表一条数据", notes = "通过查询条件查询接入方平台表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询接入方平台表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get accessPlatform findOne params:{}", params);
        int total = accessPlatformService.count(params);
        if (total > 1) {
            log.error("get accessPlatform findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        AccessPlatformDTO accessPlatformDTO = null;
        if (total == 1) {
            accessPlatformDTO = accessPlatformService.findOneAccessPlatform(params);
        }
        return R.success(accessPlatformDTO);
    }

    @ActionFlag(detail = "AccessMerchant_list")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增接入方", notes = "新增接入方")
    public Object create(@RequestBody AccessPlatformInfoDTO accessPlatformInfoDTO, HttpServletRequest request) {
        log.info("add accessPlatform DTO:{}", accessPlatformInfoDTO);
        try {
            accessPlatformService.saveAccessPlatformInfo(accessPlatformInfoDTO, request);
        } catch (BizException e) {
            log.error("add accessPlatform failed, accessPlatformDTO: {}, error message:{}, error all:{}", accessPlatformInfoDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "AccessMerchant_list")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改接入方平台表", notes = "修改接入方平台表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody AccessPlatformInfoDTO accessPlatformInfoDTO, HttpServletRequest request) {
        log.info("put modify id:{}, accessPlatformInfoDTO DTO:{}", id, accessPlatformInfoDTO);
        try {
            accessPlatformService.updateAccessPlatformInfo(accessPlatformInfoDTO, request);
        } catch (BizException e) {
            log.error("update accessPlatformInfoDTO failed, accessPlatformDTO: {}, error message:{}, error all:{}", accessPlatformInfoDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "AccessPlatform_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除接入方平台表", notes = "删除接入方平台表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete accessPlatform, id:{}", id);
        try {
            accessPlatformService.logicDeleteAccessPlatform(id, request);
        } catch (BizException e) {
            log.error("delete failed, accessPlatform id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "AccessMerchant_list")
    @PostMapping(value = "/updateMerchantState/{id}",name = "修改商户可用状态")
    @ApiOperation(value = "修改商户可用状态", notes = "修改商户可用状态")
    public Object updateMerchantState(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("accessPlatform updateMerchantState id:{}", id);
        try {
            accessPlatformService.updateMerchantState(id, request);
        } catch (BizException e) {
            log.error("accessPlatform updateMerchantState failed, id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PostMapping(value = "/getAllPlatform",name = "查询所有可用平台")
    @ApiOperation(value = "查询所有可用平台", notes = "查询所有可用平台")
    public Object getAllPlatform(HttpServletRequest request) {
        List<AccessPlatform> list = accessPlatformService.getAllPlatform();
        return R.success(list);
    }

    @ActionFlag(detail = "AccessMerchant_list")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @PostMapping(value = "/updateUuid/{id}", name="更新商户uuid")
    public Object updateUuid(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("updateUuid  Id:{}", id);
        try {
            accessPlatformService.updateUuid(id,request);
        } catch (BizException e) {
            log.error("updateUuid failed,  id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }

        return R.success();
    }

}
