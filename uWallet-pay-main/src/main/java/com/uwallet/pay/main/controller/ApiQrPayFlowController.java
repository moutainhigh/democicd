package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.ApiQrPayFlowDTO;
import com.uwallet.pay.main.model.entity.ApiQrPayFlow;
import com.uwallet.pay.main.service.ApiQrPayFlowService;
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
 * api交易订单流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: api交易订单流水表
 * @author: caishaojun
 * @date: Created in 2021-08-17 15:50:50
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: caishaojun
 */
@RestController
@RequestMapping("/apiQrPayFlow")
@Slf4j
@Api("api交易订单流水表")
public class ApiQrPayFlowController extends BaseController<ApiQrPayFlow> {

    @Autowired
    private ApiQrPayFlowService apiQrPayFlowService;

    @ActionFlag(detail = "ApiQrPayFlow_list")
    @ApiOperation(value = "分页查询api交易订单流水表", notes = "分页查询api交易订单流水表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-api交易订单流水表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = apiQrPayFlowService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ApiQrPayFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = apiQrPayFlowService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询api交易订单流水表", notes = "通过id查询api交易订单流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get apiQrPayFlow Id:{}", id);
        return R.success(apiQrPayFlowService.findApiQrPayFlowById(id));
    }

    @ApiOperation(value = "通过查询条件查询api交易订单流水表一条数据", notes = "通过查询条件查询api交易订单流水表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询api交易订单流水表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get apiQrPayFlow findOne params:{}", params);
        int total = apiQrPayFlowService.count(params);
        if (total > 1) {
            log.error("get apiQrPayFlow findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        ApiQrPayFlowDTO apiQrPayFlowDTO = null;
        if (total == 1) {
            apiQrPayFlowDTO = apiQrPayFlowService.findOneApiQrPayFlow(params);
        }
        return R.success(apiQrPayFlowDTO);
    }

    @ActionFlag(detail = "ApiQrPayFlow_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增api交易订单流水表", notes = "新增api交易订单流水表")
    public Object create(@RequestBody ApiQrPayFlowDTO apiQrPayFlowDTO, HttpServletRequest request) {
        log.info("add apiQrPayFlow DTO:{}", apiQrPayFlowDTO);
        try {
            apiQrPayFlowService.saveApiQrPayFlow(apiQrPayFlowDTO, request);
        } catch (BizException e) {
            log.error("add apiQrPayFlow failed, apiQrPayFlowDTO: {}, error message:{}, error all:{}", apiQrPayFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ApiQrPayFlow_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改api交易订单流水表", notes = "修改api交易订单流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody ApiQrPayFlowDTO apiQrPayFlowDTO, HttpServletRequest request) {
        log.info("put modify id:{}, apiQrPayFlow DTO:{}", id, apiQrPayFlowDTO);
        try {
            apiQrPayFlowService.updateApiQrPayFlow(id, apiQrPayFlowDTO, request);
        } catch (BizException e) {
            log.error("update apiQrPayFlow failed, apiQrPayFlowDTO: {}, error message:{}, error all:{}", apiQrPayFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ApiQrPayFlow_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除api交易订单流水表", notes = "删除api交易订单流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete apiQrPayFlow, id:{}", id);
        try {
            apiQrPayFlowService.logicDeleteApiQrPayFlow(id, request);
        } catch (BizException e) {
            log.error("delete failed, apiQrPayFlow id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
