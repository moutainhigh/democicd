package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.RechargeBorrowDTO;
import com.uwallet.pay.main.model.dto.RechargeFlowDTO;
import com.uwallet.pay.main.model.entity.RechargeFlow;
import com.uwallet.pay.main.service.RechargeFlowService;
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
 * 充值交易流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 充值交易流水表
 * @author: baixinyue
 * @date: Created in 2019-12-16 10:49:32
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/rechargeFlow")
@Slf4j
@Api("充值交易流水表")
public class RechargeFlowController extends BaseController<RechargeFlow> {

    @Autowired
    private RechargeFlowService rechargeFlowService;

    @ActionFlag(detail = "RechargeFlow_list")
    @ApiOperation(value = "分页查询充值交易流水表", notes = "分页查询充值交易流水表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-充值交易流水表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = rechargeFlowService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<RechargeFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = rechargeFlowService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询充值交易流水表", notes = "通过id查询充值交易流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get rechargeFlow Id:{}", id);
        return R.success(rechargeFlowService.findRechargeFlowById(id));
    }

    @ApiOperation(value = "通过查询条件查询充值交易流水表一条数据", notes = "通过查询条件查询充值交易流水表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询充值交易流水表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get rechargeFlow findOne params:{}", params);
        int total = rechargeFlowService.count(params);
        if (total > 1) {
            log.error("get rechargeFlow findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        RechargeFlowDTO rechargeFlowDTO = null;
        if (total == 1) {
            rechargeFlowDTO = rechargeFlowService.findOneRechargeFlow(params);
        }
        return R.success(rechargeFlowDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增充值交易流水表", notes = "新增充值交易流水表")
    public Object create(@RequestBody RechargeFlowDTO rechargeFlowDTO, HttpServletRequest request) {
        log.info("add rechargeFlow DTO:{}", rechargeFlowDTO);
        try {
            rechargeFlowService.saveRechargeFlow(rechargeFlowDTO, request);
        } catch (BizException e) {
            log.error("add rechargeFlow failed, rechargeFlowDTO: {}, error message:{}, error all:{}", rechargeFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改充值交易流水表", notes = "修改充值交易流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody RechargeFlowDTO rechargeFlowDTO, HttpServletRequest request) {
        log.info("put modify id:{}, rechargeFlow DTO:{}", id, rechargeFlowDTO);
        try {
            rechargeFlowService.updateRechargeFlow(id, rechargeFlowDTO, request);
        } catch (BizException e) {
            log.error("update rechargeFlow failed, rechargeFlowDTO: {}, error message:{}, error all:{}", rechargeFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除充值交易流水表", notes = "删除充值交易流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete rechargeFlow, id:{}", id);
        try {
            rechargeFlowService.logicDeleteRechargeFlow(id, request);
        } catch (BizException e) {
            log.error("delete failed, rechargeFlow id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "rechargeOrder_list")
    @ApiOperation(value = "分页查询充值交易订单", notes = "分页查询充值交易订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单编号", paramType = "query", dataType = "long", required = false),
            @ApiImplicitParam(name = "email", value = "充值账号", paramType = "query", dataType = "string", required = false),
            @ApiImplicitParam(name = "gatewayId", value = "渠道", paramType = "query", dataType = "long", required = false),
            @ApiImplicitParam(name = "state", value = "订单状态", paramType = "query", dataType = "int", required = false),
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/borrow")
    public Object borrowList(HttpServletRequest request) throws BizException {
        Map<String, Object> params = getConditionsMap(request);
        int total = rechargeFlowService.selectRechargeBorrowCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<RechargeBorrowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = rechargeFlowService.selectRechargeBorrow(params, scs, pc);
        }
        return R.success(list, pc);
    }

}
