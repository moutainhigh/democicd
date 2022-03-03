package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.ReconciliationBatchDTO;
import com.uwallet.pay.main.model.entity.ReconciliationBatch;
import com.uwallet.pay.main.service.ReconciliationBatchService;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.main.util.I18nUtils;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;


/**
 * <p>
 * 对账表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 对账表
 * @author: aaronS
 * @date: Created in 2021-01-25 16:11:20
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: aaronS
 */
@RestController
@RequestMapping("/reconciliationBatch")
@Slf4j
@Api("对账表")
public class ReconciliationBatchController extends BaseController<ReconciliationBatch> {

    @Autowired
    private ReconciliationBatchService reconciliationBatchService;

    //@ActionFlag(detail = "ReconciliationBatch_list")
    @ApiOperation(value = "分页查询对账表", notes = "分页查询对账表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-对账表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        formatMonth(params);
        int total = reconciliationBatchService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ReconciliationBatchDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = reconciliationBatchService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }
    /**
     * 根据查询的月份，格式化 start，end 查询条件
     * @param params
     * @return
     */
    private Map<String, Object> formatMonth(Map<String, Object> params) {
        if (null != params.get("queryDate") && StringUtils.isNotBlank(params.get("queryDate").toString())) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            String queryDate = params.get("queryDate").toString();
            String[] split = queryDate.split("-");

            calendar.set(Calendar.YEAR, Integer.parseInt(split[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(split[1]) - 1);

            params.put("start", calendar.getTimeInMillis());

            calendar.add(Calendar.MONTH, 1);
            params.put("end", calendar.getTimeInMillis());
        }
        return params;
    }

    @ApiOperation(value = "通过id查询对账表", notes = "通过id查询对账表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get reconciliationBatch Id:{}", id);
        return R.success(reconciliationBatchService.findReconciliationBatchById(id));
    }

    @ApiOperation(value = "通过查询条件查询对账表一条数据", notes = "通过查询条件查询对账表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询对账表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get reconciliationBatch findOne params:{}", params);
        int total = reconciliationBatchService.count(params);
        if (total > 1) {
            log.error("get reconciliationBatch findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        ReconciliationBatchDTO reconciliationBatchDTO = null;
        if (total == 1) {
            reconciliationBatchDTO = reconciliationBatchService.findOneReconciliationBatch(params);
        }
        return R.success(reconciliationBatchDTO);
    }

    //@ActionFlag(detail = "ReconciliationBatch_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增对账表", notes = "新增对账表")
    public Object create(@RequestBody ReconciliationBatchDTO reconciliationBatchDTO, HttpServletRequest request) {
        log.info("add reconciliationBatch DTO:{}", reconciliationBatchDTO);
        try {
            reconciliationBatchService.saveReconciliationBatch(reconciliationBatchDTO, request);
        } catch (BizException e) {
            log.error("add reconciliationBatch failed, reconciliationBatchDTO: {}, error message:{}, error all:{}", reconciliationBatchDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    //@ActionFlag(detail = "ReconciliationBatch_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改对账表", notes = "修改对账表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody ReconciliationBatchDTO reconciliationBatchDTO, HttpServletRequest request) {
        log.info("put modify id:{}, reconciliationBatch DTO:{}", id, reconciliationBatchDTO);
        try {
            reconciliationBatchService.updateReconciliationBatch(id, reconciliationBatchDTO, request);
        } catch (BizException e) {
            log.error("update reconciliationBatch failed, reconciliationBatchDTO: {}, error message:{}, error all:{}", reconciliationBatchDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    //@ActionFlag(detail = "ReconciliationBatch_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除对账表", notes = "删除对账表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete reconciliationBatch, id:{}", id);
        try {
            reconciliationBatchService.logicDeleteReconciliationBatch(id, request);
        } catch (BizException e) {
            log.error("delete failed, reconciliationBatch id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
