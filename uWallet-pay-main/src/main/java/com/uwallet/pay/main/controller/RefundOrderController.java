package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.RefundOrderDTO;
import com.uwallet.pay.main.model.dto.RefundOrderListDTO;
import com.uwallet.pay.main.model.entity.RefundOrder;
import com.uwallet.pay.main.service.RefundOrderService;
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
 * 退款订单
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 退款订单
 * @author: zhoutt
 * @date: Created in 2021-09-10 14:07:12
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/refundOrder")
@Slf4j
@Api("退款订单")
public class RefundOrderController extends BaseController<RefundOrder> {

    @Autowired
    private RefundOrderService refundOrderService;

    @ActionFlag(detail = "RefundOrder_list")
    @ApiOperation(value = "分页查询退款订单", notes = "分页查询退款订单以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-退款订单列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = refundOrderService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<RefundOrderDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = refundOrderService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询退款订单", notes = "通过id查询退款订单")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get refundOrder Id:{}", id);
        return R.success(refundOrderService.findRefundOrderById(id));
    }

    @ApiOperation(value = "通过查询条件查询退款订单一条数据", notes = "通过查询条件查询退款订单一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询退款订单一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get refundOrder findOne params:{}", params);
        int total = refundOrderService.count(params);
        if (total > 1) {
            log.error("get refundOrder findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        RefundOrderDTO refundOrderDTO = null;
        if (total == 1) {
            refundOrderDTO = refundOrderService.findOneRefundOrder(params);
        }
        return R.success(refundOrderDTO);
    }

    @ActionFlag(detail = "RefundOrder_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增退款订单", notes = "新增退款订单")
    public Object create(@RequestBody RefundOrderDTO refundOrderDTO, HttpServletRequest request) {
        log.info("add refundOrder DTO:{}", refundOrderDTO);
        try {
            refundOrderService.saveRefundOrder(refundOrderDTO, request);
        } catch (BizException e) {
            log.error("add refundOrder failed, refundOrderDTO: {}, error message:{}, error all:{}", refundOrderDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "RefundOrder_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改退款订单", notes = "修改退款订单")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody RefundOrderDTO refundOrderDTO, HttpServletRequest request) {
        log.info("put modify id:{}, refundOrder DTO:{}", id, refundOrderDTO);
        try {
            refundOrderService.updateRefundOrder(id, refundOrderDTO, request);
        } catch (BizException e) {
            log.error("update refundOrder failed, refundOrderDTO: {}, error message:{}, error all:{}", refundOrderDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "RefundOrder_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除退款订单", notes = "删除退款订单")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete refundOrder, id:{}", id);
        try {
            refundOrderService.logicDeleteRefundOrder(id, request);
        } catch (BizException e) {
            log.error("delete failed, refundOrder id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }


    @GetMapping("/getRefundsList")
    @ApiOperation(value = "退款列表", notes = "退款列表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    public Object getH5RefundsList( HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = refundOrderService.getH5RefundsListCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<RefundOrderListDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = refundOrderService.getH5RefundsList(params, scs, pc);
        }
        return R.success(list, pc);
    }


}
