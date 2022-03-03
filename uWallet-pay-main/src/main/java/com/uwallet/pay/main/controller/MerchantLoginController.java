package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.MerchantLoginDTO;
import com.uwallet.pay.main.model.entity.MerchantLogin;
import com.uwallet.pay.main.service.MerchantLoginService;
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
 * @date: Created in 2020-02-21 16:19:27
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/merchantLogin")
@Slf4j
@Api("用户主表")
public class MerchantLoginController extends BaseController<MerchantLogin> {

    @Autowired
    private MerchantLoginService merchantLoginService;

    @ActionFlag(detail = "MerchantLogin_list")
    @ApiOperation(value = "分页查询用户主表", notes = "分页查询用户主表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-用户主表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = merchantLoginService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MerchantLoginDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = merchantLoginService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询用户主表", notes = "通过id查询用户主表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get merchantLogin Id:{}", id);
        return R.success(merchantLoginService.findMerchantLoginById(id));
    }

    @ApiOperation(value = "通过查询条件查询用户主表一条数据", notes = "通过查询条件查询用户主表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询用户主表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get merchantLogin findOne params:{}", params);
        int total = merchantLoginService.count(params);
        if (total > 1) {
            log.error("get merchantLogin findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        MerchantLoginDTO merchantLoginDTO = null;
        if (total == 1) {
            merchantLoginDTO = merchantLoginService.findOneMerchantLogin(params);
        }
        return R.success(merchantLoginDTO);
    }

    @ActionFlag(detail = "MerchantLogin_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增用户主表", notes = "新增用户主表")
    public Object create(@RequestBody MerchantLoginDTO merchantLoginDTO, HttpServletRequest request) {
        log.info("add merchantLogin DTO:{}", merchantLoginDTO);
        try {
            merchantLoginService.saveMerchantLogin(merchantLoginDTO, request);
        } catch (BizException e) {
            log.error("add merchantLogin failed, merchantLoginDTO: {}, error message:{}, error all:{}", merchantLoginDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "MerchantLogin_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改用户主表", notes = "修改用户主表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody MerchantLoginDTO merchantLoginDTO, HttpServletRequest request) {
        log.info("put modify id:{}, merchantLogin DTO:{}", id, merchantLoginDTO);
        try {
            merchantLoginService.updateMerchantLogin(id, merchantLoginDTO, request);
        } catch (BizException e) {
            log.error("update merchantLogin failed, merchantLoginDTO: {}, error message:{}, error all:{}", merchantLoginDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "MerchantLogin_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户主表", notes = "删除用户主表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete merchantLogin, id:{}", id);
        try {
            merchantLoginService.logicDeleteMerchantLogin(id, request);
        } catch (BizException e) {
            log.error("delete failed, merchantLogin id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
