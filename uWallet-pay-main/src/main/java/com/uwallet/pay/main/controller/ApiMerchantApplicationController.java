package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.AdminDTO;
import com.uwallet.pay.main.model.dto.ApiMerchantApplicationDTO;
import com.uwallet.pay.main.model.dto.MerchantApplicationDTO;
import com.uwallet.pay.main.model.entity.Admin;
import com.uwallet.pay.main.model.entity.ApiMerchantApplication;
import com.uwallet.pay.main.service.AdminService;
import com.uwallet.pay.main.service.ApiMerchantApplicationService;
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

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;


/**
 * <p>
 * h5 api 商户申请表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: h5 api 商户申请表
 * @author: zhoutt
 * @date: Created in 2021-09-23 10:25:50
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/apiMerchantApplication")
@Slf4j
@Api("h5 api 商户申请表")
public class ApiMerchantApplicationController extends BaseController<ApiMerchantApplication> {

    @Autowired
    private ApiMerchantApplicationService apiMerchantApplicationService;
    @Autowired
    private AdminService adminService;

    @ActionFlag(detail = "H5_Merchant_Application_list")
    @ApiOperation(value = "分页查询h5 api 商户申请表", notes = "分页查询h5 api 商户申请表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-h5 api 商户申请表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = apiMerchantApplicationService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ApiMerchantApplicationDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = apiMerchantApplicationService.find(params, scs, pc);
            for (ApiMerchantApplicationDTO merchantApplicationDTO : list) {
                Long createdBy = merchantApplicationDTO.getCreatedBy();
                if (createdBy!=null&&createdBy!=0){
                    AdminDTO adminById = adminService.findAdminById(createdBy);
                    merchantApplicationDTO.setOperator(adminById.getRealName());
                }
            }
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询h5 api 商户申请表", notes = "通过id查询h5 api 商户申请表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get apiMerchantApplication Id:{}", id);
        return R.success(apiMerchantApplicationService.findApiMerchantApplicationById(id));
    }

    @ApiOperation(value = "通过查询条件查询h5 api 商户申请表一条数据", notes = "通过查询条件查询h5 api 商户申请表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询h5 api 商户申请表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get apiMerchantApplication findOne params:{}", params);
        int total = apiMerchantApplicationService.count(params);
        if (total > 1) {
            log.error("get apiMerchantApplication findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        ApiMerchantApplicationDTO apiMerchantApplicationDTO = null;
        if (total == 1) {
            apiMerchantApplicationDTO = apiMerchantApplicationService.findOneApiMerchantApplication(params);
        }
        return R.success(apiMerchantApplicationDTO);
    }

    @ActionFlag(detail = "ApiMerchantApplication_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增h5 api 商户申请表", notes = "新增h5 api 商户申请表")
    public Object create(@RequestBody ApiMerchantApplicationDTO apiMerchantApplicationDTO, HttpServletRequest request) {
        log.info("add apiMerchantApplication DTO:{}", apiMerchantApplicationDTO);
        try {
            apiMerchantApplicationService.saveApiMerchantApplication(apiMerchantApplicationDTO, request);
        } catch (BizException e) {
            log.error("add apiMerchantApplication failed, apiMerchantApplicationDTO: {}, error message:{}, error all:{}", apiMerchantApplicationDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ApiMerchantApplication_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改h5 api 商户申请表", notes = "修改h5 api 商户申请表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody ApiMerchantApplicationDTO apiMerchantApplicationDTO, HttpServletRequest request) {
        log.info("put modify id:{}, apiMerchantApplication DTO:{}", id, apiMerchantApplicationDTO);
        try {
            apiMerchantApplicationService.updateApiMerchantApplication(id, apiMerchantApplicationDTO, request);
        } catch (BizException e) {
            log.error("update apiMerchantApplication failed, apiMerchantApplicationDTO: {}, error message:{}, error all:{}", apiMerchantApplicationDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ApiMerchantApplication_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除h5 api 商户申请表", notes = "删除h5 api 商户申请表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete apiMerchantApplication, id:{}", id);
        try {
            apiMerchantApplicationService.logicDeleteApiMerchantApplication(id, request);
        } catch (BizException e) {
            log.error("delete failed, apiMerchantApplication id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "保存新商户申请信息", notes = "保存新商户申请信息")
    @PostMapping("/saveH5Merchant")
    public Object saveH5Merchant(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("saveH5Merchant, requestInfo:{}", requestInfo);
        try {
            String id = apiMerchantApplicationService.saveH5MerchantMessage(requestInfo, request);
            return R.success(id);
        } catch (Exception e) {
            log.error("saveH5Merchant requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }

    }

    @ApiOperation(value = "新商户申请信息提交", notes = "新商户申请信息提交")
    @PostMapping("/submitAudit")
    public Object submitAudit(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("h5 merchant submitAudit, requestInfo:{}", requestInfo);
        try {
            apiMerchantApplicationService.newMerchantSubmitAudit(requestInfo, request);
        } catch (Exception e) {
            log.error("h5 merchant submitAudit requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "查询新商户信息", notes = "查询新商户信息")
    @GetMapping("/getNewMerchantMessage/{id}")
    public Object getNewMerchantMessage(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("查询API新商户信息, requestInfo id:{}", id);
        JSONObject result;
        try {
            result= apiMerchantApplicationService.getMerchantMessage(id, request);

        } catch (Exception e) {
            log.error("查询API新商户信息 requestInfo id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }
}
