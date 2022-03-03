package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.SecondMerchantGatewayInfoDTO;
import com.uwallet.pay.main.model.entity.SecondMerchantGatewayInfo;
import com.uwallet.pay.main.service.SecondMerchantGatewayInfoService;
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
 * 二级商户渠道信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 二级商户渠道信息表
 * @author: baixinyue
 * @date: Created in 2019-12-26 17:02:13
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/secondMerchantGatewayInfo")
@Slf4j
@Api("二级商户渠道信息表")
public class SecondMerchantGatewayInfoController extends BaseController<SecondMerchantGatewayInfo> {

    @Autowired
    private SecondMerchantGatewayInfoService secondMerchantGatewayInfoService;
    
    @ApiOperation(value = "分页查询二级商户渠道信息表", notes = "分页查询二级商户渠道信息表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-二级商户渠道信息表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = secondMerchantGatewayInfoService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<SecondMerchantGatewayInfoDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = secondMerchantGatewayInfoService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询二级商户渠道信息表", notes = "通过id查询二级商户渠道信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get secondMerchantGatewayInfo Id:{}", id);
        return R.success(secondMerchantGatewayInfoService.findSecondMerchantGatewayInfoById(id));
    }

    @ApiOperation(value = "通过查询条件查询二级商户渠道信息表一条数据", notes = "通过查询条件查询二级商户渠道信息表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询二级商户渠道信息表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get secondMerchantGatewayInfo findOne params:{}", params);
        int total = secondMerchantGatewayInfoService.count(params);
        if (total > 1) {
            log.error("get secondMerchantGatewayInfo findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        SecondMerchantGatewayInfoDTO secondMerchantGatewayInfoDTO = null;
        if (total == 1) {
            secondMerchantGatewayInfoDTO = secondMerchantGatewayInfoService.findOneSecondMerchantGatewayInfo(params);
        }
        return R.success(secondMerchantGatewayInfoDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增二级商户渠道信息表", notes = "新增二级商户渠道信息表")
    public Object create(@RequestBody SecondMerchantGatewayInfoDTO secondMerchantGatewayInfoDTO, HttpServletRequest request) {
        log.info("add secondMerchantGatewayInfo DTO:{}", secondMerchantGatewayInfoDTO);
        try {
            secondMerchantGatewayInfoService.saveSecondMerchantGatewayInfo(secondMerchantGatewayInfoDTO, request);
        } catch (BizException e) {
            log.error("add secondMerchantGatewayInfo failed, secondMerchantGatewayInfoDTO: {}, error message:{}, error all:{}", secondMerchantGatewayInfoDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改二级商户渠道信息表", notes = "修改二级商户渠道信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody SecondMerchantGatewayInfoDTO secondMerchantGatewayInfoDTO, HttpServletRequest request) {
        log.info("put modify id:{}, secondMerchantGatewayInfo DTO:{}", id, secondMerchantGatewayInfoDTO);
        try {
            secondMerchantGatewayInfoService.updateSecondMerchantGatewayInfo(id, secondMerchantGatewayInfoDTO, request);
        } catch (BizException e) {
            log.error("update secondMerchantGatewayInfo failed, secondMerchantGatewayInfoDTO: {}, error message:{}, error all:{}", secondMerchantGatewayInfoDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除二级商户渠道信息表", notes = "删除二级商户渠道信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete secondMerchantGatewayInfo, id:{}", id);
        try {
            secondMerchantGatewayInfoService.logicDeleteSecondMerchantGatewayInfo(id, request);
        } catch (BizException e) {
            log.error("delete failed, secondMerchantGatewayInfo id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
