package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.RechargeRouteDTO;
import com.uwallet.pay.main.model.entity.RechargeRoute;
import com.uwallet.pay.main.service.RechargeRouteService;
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
 * 充值转账路由表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 充值转账路由表
 * @author: zhoutt
 * @date: Created in 2019-12-17 10:36:56
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/rechargeRoute")
@Slf4j
@Api("充值转账路由表")
public class RechargeRouteController extends BaseController<RechargeRoute> {

    @Autowired
    private RechargeRouteService rechargeRouteService;

    @ApiOperation(value = "分页查询充值转账路由表", notes = "分页查询充值转账路由表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-充值转账路由表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = rechargeRouteService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<RechargeRouteDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = rechargeRouteService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询充值转账路由表", notes = "通过id查询充值转账路由表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get rechargeRoute Id:{}", id);
        return R.success(rechargeRouteService.findRechargeRouteById(id));
    }

    @ApiOperation(value = "通过查询条件查询充值转账路由表一条数据", notes = "通过查询条件查询充值转账路由表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询充值转账路由表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get rechargeRoute findOne params:{}", params);
        int total = rechargeRouteService.count(params);
        if (total > 1) {
            log.error("get rechargeRoute findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        RechargeRouteDTO rechargeRouteDTO = null;
        if (total == 1) {
            rechargeRouteDTO = rechargeRouteService.findOneRechargeRoute(params);
        }
        return R.success(rechargeRouteDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增充值转账路由表", notes = "新增充值转账路由表")
    public Object create(@RequestBody RechargeRouteDTO rechargeRouteDTO, HttpServletRequest request) {
        log.info("add rechargeRoute DTO:{}", rechargeRouteDTO);
        try {
            rechargeRouteService.saveRechargeRoute(rechargeRouteDTO, request);
        } catch (BizException e) {
            log.error("add rechargeRoute failed, rechargeRouteDTO: {}, error message:{}, error all:{}", rechargeRouteDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改充值转账路由表", notes = "修改充值转账路由表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody RechargeRouteDTO rechargeRouteDTO, HttpServletRequest request) {
        log.info("put modify id:{}, rechargeRoute DTO:{}", id, rechargeRouteDTO);
        try {
            rechargeRouteService.updateRechargeRoute(id, rechargeRouteDTO, request);
        } catch (BizException e) {
            log.error("update rechargeRoute failed, rechargeRouteDTO: {}, error message:{}, error all:{}", rechargeRouteDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除充值转账路由表", notes = "删除充值转账路由表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete rechargeRoute, id:{}", id);
        try {
            rechargeRouteService.logicDeleteRechargeRoute(id, request);
        } catch (BizException e) {
            log.error("delete failed, rechargeRoute id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
