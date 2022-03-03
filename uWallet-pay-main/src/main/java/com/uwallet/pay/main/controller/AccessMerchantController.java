package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.AccessMerchantDTO;
import com.uwallet.pay.main.model.dto.AccessPlatformInfoDTO;
import com.uwallet.pay.main.model.entity.AccessMerchant;
import com.uwallet.pay.main.model.entity.AccessPlatform;
import com.uwallet.pay.main.service.AccessMerchantService;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.main.service.DocuSignService;
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
 * 接入方商户表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 接入方商户表
 * @author: zhoutt
 * @date: Created in 2020-09-25 08:55:53
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/accessMerchant")
@Slf4j
@Api("接入方商户表")
public class AccessMerchantController extends BaseController<AccessMerchant> {

    @Autowired
    private AccessMerchantService accessMerchantService;

    @ActionFlag(detail = "AccessMerchant_list")
    @ApiOperation(value = "分页查询接入方商户表", notes = "分页查询接入方商户表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-接入方商户表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);

        int total = accessMerchantService.getAccessMerchantListCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<AccessPlatformInfoDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = accessMerchantService.getAccessMerchantList(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询接入方商户表", notes = "通过id查询接入方商户表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get accessMerchant Id:{}", id);
        return R.success(accessMerchantService.findAccessMerchantById(id));
    }

    @ApiOperation(value = "通过查询条件查询接入方商户表一条数据", notes = "通过查询条件查询接入方商户表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询接入方商户表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get accessMerchant findOne params:{}", params);
        int total = accessMerchantService.count(params);
        if (total > 1) {
            log.error("get accessMerchant findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        AccessMerchantDTO accessMerchantDTO = null;
        if (total == 1) {
            accessMerchantDTO = accessMerchantService.findOneAccessMerchant(params);
        }
        return R.success(accessMerchantDTO);
    }

    @ActionFlag(detail = "AccessMerchant_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增接入方商户表", notes = "新增接入方商户表")
    public Object create(@RequestBody AccessMerchantDTO accessMerchantDTO, HttpServletRequest request) {
        log.info("add accessMerchant DTO:{}", accessMerchantDTO);
        try {
            accessMerchantService.saveAccessMerchant(accessMerchantDTO, request);
        } catch (BizException e) {
            log.error("add accessMerchant failed, accessMerchantDTO: {}, error message:{}, error all:{}", accessMerchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "AccessMerchant_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改接入方商户表", notes = "修改接入方商户表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody AccessMerchantDTO accessMerchantDTO, HttpServletRequest request) {
        log.info("put modify id:{}, accessMerchant DTO:{}", id, accessMerchantDTO);
        try {
            accessMerchantService.updateAccessMerchant(id, accessMerchantDTO, request);
        } catch (BizException e) {
            log.error("update accessMerchant failed, accessMerchantDTO: {}, error message:{}, error all:{}", accessMerchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "AccessMerchant_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除接入方商户表", notes = "删除接入方商户表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete accessMerchant, id:{}", id);
        try {
            accessMerchantService.logicDeleteAccessMerchant(id, request);
        } catch (BizException e) {
            log.error("delete failed, accessMerchant id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/getOne/{id}", name="详情")
    public Object getOne(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("getOne accessMerchant Id:{}", id);
        return R.success(accessMerchantService.getOne(id));
    }
}
