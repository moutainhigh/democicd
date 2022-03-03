package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.ApiApproveLogDTO;
import com.uwallet.pay.main.model.entity.ApiApproveLog;
import com.uwallet.pay.main.service.ApiApproveLogService;
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
 * 审核日志
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 审核日志
 * @author: zhoutt
 * @date: Created in 2021-09-23 15:39:54
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/apiApproveLog")
@Slf4j
@Api("审核日志")
public class ApiApproveLogController extends BaseController<ApiApproveLog> {

    @Autowired
    private ApiApproveLogService apiApproveLogService;

    @ActionFlag(detail = "ApiApproveLog_list")
    @ApiOperation(value = "分页查询审核日志", notes = "分页查询审核日志以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-审核日志列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = apiApproveLogService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ApiApproveLogDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = apiApproveLogService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询审核日志", notes = "通过id查询审核日志")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get apiApproveLog Id:{}", id);
        return R.success(apiApproveLogService.findApiApproveLogById(id));
    }

    @ApiOperation(value = "通过查询条件查询审核日志一条数据", notes = "通过查询条件查询审核日志一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询审核日志一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get apiApproveLog findOne params:{}", params);
        int total = apiApproveLogService.count(params);
        if (total > 1) {
            log.error("get apiApproveLog findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        ApiApproveLogDTO apiApproveLogDTO = null;
        if (total == 1) {
            apiApproveLogDTO = apiApproveLogService.findOneApiApproveLog(params);
        }
        return R.success(apiApproveLogDTO);
    }

    @ActionFlag(detail = "ApiApproveLog_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增审核日志", notes = "新增审核日志")
    public Object create(@RequestBody ApiApproveLogDTO apiApproveLogDTO, HttpServletRequest request) {
        log.info("add apiApproveLog DTO:{}", apiApproveLogDTO);
        try {
            apiApproveLogService.saveApiApproveLog(apiApproveLogDTO, request);
        } catch (BizException e) {
            log.error("add apiApproveLog failed, apiApproveLogDTO: {}, error message:{}, error all:{}", apiApproveLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ApiApproveLog_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改审核日志", notes = "修改审核日志")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody ApiApproveLogDTO apiApproveLogDTO, HttpServletRequest request) {
        log.info("put modify id:{}, apiApproveLog DTO:{}", id, apiApproveLogDTO);
        try {
            apiApproveLogService.updateApiApproveLog(id, apiApproveLogDTO, request);
        } catch (BizException e) {
            log.error("update apiApproveLog failed, apiApproveLogDTO: {}, error message:{}, error all:{}", apiApproveLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ApiApproveLog_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除审核日志", notes = "删除审核日志")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete apiApproveLog, id:{}", id);
        try {
            apiApproveLogService.logicDeleteApiApproveLog(id, request);
        } catch (BizException e) {
            log.error("delete failed, apiApproveLog id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }



    @ApiOperation(value = "分页查询审核记录列表", notes = "分页查询审核记录列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-审核记录列表", value = "/listMerchantApprove")
    public Object listMerchantApprove(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("H5 listMerchantApprove request info:"+params);
        int total = apiApproveLogService.countMerchantApprove(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ApiApproveLogDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = apiApproveLogService.findMerchantApprove(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "审核记录详情页面", notes = "审核记录详情页面")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "approveLogDetail/{id}", name="审核记录详情页面")
    public Object approveLogDetail(@PathVariable("id") Long id) {
        log.info("get h5 approveLog Id:{}", id);
        ApiApproveLogDTO approveLogDTO = apiApproveLogService.approveLogDetail(id);
        return R.success(approveLogDTO);
    }


}
