package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.RequestAnalysisDTO;
import com.uwallet.pay.main.model.entity.RequestAnalysis;
import com.uwallet.pay.main.service.RequestAnalysisService;
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
 * 接口请求数据统计表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 接口请求数据统计表
 * @author: aaronS
 * @date: Created in 2021-02-06 14:03:58
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: aaronS
 */
@RestController
@RequestMapping("/requestAnalysis")
@Slf4j
@Api("接口请求数据统计表")
public class RequestAnalysisController extends BaseController<RequestAnalysis> {

    @Autowired
    private RequestAnalysisService requestAnalysisService;

    @ActionFlag(detail = "RequestAnalysis_list")
    @ApiOperation(value = "分页查询接口请求数据统计表", notes = "分页查询接口请求数据统计表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-接口请求数据统计表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = requestAnalysisService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<RequestAnalysisDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = requestAnalysisService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询接口请求数据统计表", notes = "通过id查询接口请求数据统计表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get requestAnalysis Id:{}", id);
        return R.success(requestAnalysisService.findRequestAnalysisById(id));
    }

    @ApiOperation(value = "通过查询条件查询接口请求数据统计表一条数据", notes = "通过查询条件查询接口请求数据统计表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询接口请求数据统计表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get requestAnalysis findOne params:{}", params);
        int total = requestAnalysisService.count(params);
        if (total > 1) {
            log.error("get requestAnalysis findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        RequestAnalysisDTO requestAnalysisDTO = null;
        if (total == 1) {
            requestAnalysisDTO = requestAnalysisService.findOneRequestAnalysis(params);
        }
        return R.success(requestAnalysisDTO);
    }

    @ActionFlag(detail = "RequestAnalysis_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增接口请求数据统计表", notes = "新增接口请求数据统计表")
    public Object create(@RequestBody RequestAnalysisDTO requestAnalysisDTO, HttpServletRequest request) {
        log.info("add requestAnalysis DTO:{}", requestAnalysisDTO);
        try {
            requestAnalysisService.saveRequestAnalysis(requestAnalysisDTO, request);
        } catch (BizException e) {
            log.error("add requestAnalysis failed, requestAnalysisDTO: {}, error message:{}, error all:{}", requestAnalysisDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "RequestAnalysis_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改接口请求数据统计表", notes = "修改接口请求数据统计表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody RequestAnalysisDTO requestAnalysisDTO, HttpServletRequest request) {
        log.info("put modify id:{}, requestAnalysis DTO:{}", id, requestAnalysisDTO);
        try {
            requestAnalysisService.updateRequestAnalysis(id, requestAnalysisDTO);
        } catch (BizException e) {
            log.error("update requestAnalysis failed, requestAnalysisDTO: {}, error message:{}, error all:{}", requestAnalysisDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "RequestAnalysis_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除接口请求数据统计表", notes = "删除接口请求数据统计表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete requestAnalysis, id:{}", id);
        try {
            requestAnalysisService.logicDeleteRequestAnalysis(id, request);
        } catch (BizException e) {
            log.error("delete failed, requestAnalysis id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
