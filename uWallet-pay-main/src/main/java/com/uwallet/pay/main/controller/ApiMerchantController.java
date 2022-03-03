package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.ApiMerchantDTO;
import com.uwallet.pay.main.model.dto.MerchantDTO;
import com.uwallet.pay.main.model.dto.MerchantDetailDTO;
import com.uwallet.pay.main.model.dto.MerchantDetailH5DTO;
import com.uwallet.pay.main.model.entity.ApiMerchant;
import com.uwallet.pay.main.service.ApiMerchantService;
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
 * api商户信息表j
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: api商户信息表j
 * @author: zhoutt
 * @date: Created in 2021-09-02 17:32:33
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/apiMerchant")
@Slf4j
@Api("api商户信息表j")
public class ApiMerchantController extends BaseController<ApiMerchant> {

    @Autowired
    private ApiMerchantService apiMerchantService;

    @ActionFlag(detail = "ApiMerchant_list")
    @ApiOperation(value = "分页查询api商户信息表j", notes = "分页查询api商户信息表j以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-api商户信息表j列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = apiMerchantService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ApiMerchantDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = apiMerchantService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询api商户信息表j", notes = "通过id查询api商户信息表j")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get apiMerchant Id:{}", id);
        return R.success(apiMerchantService.findApiMerchantById(id));
    }

    @ApiOperation(value = "通过查询条件查询api商户信息表j一条数据", notes = "通过查询条件查询api商户信息表j一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询api商户信息表j一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get apiMerchant findOne params:{}", params);
        int total = apiMerchantService.count(params);
        if (total > 1) {
            log.error("get apiMerchant findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        ApiMerchantDTO apiMerchantDTO = null;
        if (total == 1) {
            apiMerchantDTO = apiMerchantService.findOneApiMerchant(params);
        }
        return R.success(apiMerchantDTO);
    }

    @ActionFlag(detail = "ApiMerchant_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增api商户信息表j", notes = "新增api商户信息表j")
    public Object create(@RequestBody ApiMerchantDTO apiMerchantDTO, HttpServletRequest request) {
        log.info("add apiMerchant DTO:{}", apiMerchantDTO);
        try {
            apiMerchantService.saveApiMerchant(apiMerchantDTO, request);
        } catch (BizException e) {
            log.error("add apiMerchant failed, apiMerchantDTO: {}, error message:{}, error all:{}", apiMerchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    //@ActionFlag(detail = "ApiMerchant_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改api商户信息表j", notes = "修改api商户信息表j")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody ApiMerchantDTO apiMerchantDTO, HttpServletRequest request) {
        log.info("put modify id:{}, apiMerchant DTO:{}", id, apiMerchantDTO);
        try {
            apiMerchantService.updateApiMerchant(id, apiMerchantDTO, request);
        } catch (BizException e) {
            log.error("update apiMerchant failed, apiMerchantDTO: {}, error message:{}, error all:{}", apiMerchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ApiMerchant_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除api商户信息表j", notes = "删除api商户信息表j")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete apiMerchant, id:{}", id);
        try {
            apiMerchantService.logicDeleteApiMerchant(id, request);
        } catch (BizException e) {
            log.error("delete failed, apiMerchant id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @GetMapping(value = "/findSuperMerchant", name="查询一级商户列表")
    @ApiOperation(value = "查询一级商户列表", notes = "查询一级商户列表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object findSuperMerchant( HttpServletRequest request) {
        log.info("findSuperMerchant Service" );
        return R.success(apiMerchantService.findSuperMerchant());

    }

    @PutMapping("/refuse/{id}")
    @ApiOperation(value = "商户入网审核拒绝", notes = "商户入网审核拒绝")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true),
            @ApiImplicitParam(name = "remark", value = "审核结果备注", dataType = "String", required = true)
    })
    public Object refuseMerchant(@PathVariable("id") Long id,  @RequestBody MerchantDTO merchantDTO, HttpServletRequest request) {
        log.info("H5 merchant Refuse id:{}, merchant DTO:{}", id );
        try {
            apiMerchantService.refuseMerchant(id, merchantDTO.getRemark(), request);
        } catch (BizException e) {
            log.error("H5 merchant Refuse failed, id: {}, error message:{}", e.getMessage());
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PostMapping("/pass")
    @ApiOperation(value = "商户入网审核通过", notes = "商户入网审核通过")
    public Object passMerchant(@RequestBody MerchantDetailH5DTO merchantDetailDTO, HttpServletRequest request) {
        try {
            apiMerchantService.passMerchant(merchantDetailDTO.getId(), merchantDetailDTO, request);
        } catch (Exception e) {
            log.error("pass h5 merchant failed, id: {}, error message:{}", merchantDetailDTO.getId(), e.getMessage(),e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "分页查询api审核商户信息表", notes = "分页查询api审核商户信息表j以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping( value = "getAuditList",name = "查询-api商户审核信息表j列表")
    public Object auditList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = apiMerchantService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ApiMerchantDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = apiMerchantService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }
}
