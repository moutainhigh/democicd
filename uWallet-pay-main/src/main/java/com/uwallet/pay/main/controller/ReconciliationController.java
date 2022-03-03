package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.ReconciliationDTO;
import com.uwallet.pay.main.model.dto.ReconciliationDetailDTO;
import com.uwallet.pay.main.model.entity.Reconciliation;
import com.uwallet.pay.main.service.ReconciliationService;
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
import org.springframework.web.multipart.MultipartFile;


/**
 * <p>
 * 对账
 * </p>
 *
 * @package:  com.uwallet.pay.main.main.controller
 * @description: 对账
 * @author: baixinyue
 * @date: Created in 2020-02-17 09:59:08
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/reconciliation")
@Slf4j
@Api("对账")
public class ReconciliationController extends BaseController<Reconciliation> {

    @Autowired
    private ReconciliationService reconciliationService;

    @ActionFlag(detail = "Reconciliation_list")
    @ApiOperation(value = "分页查询对账", notes = "分页查询对账以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-对账列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = reconciliationService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ReconciliationDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = reconciliationService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询对账", notes = "通过id查询对账")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get reconciliation Id:{}", id);
        return R.success(reconciliationService.findReconciliationById(id));
    }

    @ApiOperation(value = "通过查询条件查询对账一条数据", notes = "通过查询条件查询对账一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询对账一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get reconciliation findOne params:{}", params);
        int total = reconciliationService.count(params);
        if (total > 1) {
            log.error("get reconciliation findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        ReconciliationDTO reconciliationDTO = null;
        if (total == 1) {
            reconciliationDTO = reconciliationService.findOneReconciliation(params);
        }
        return R.success(reconciliationDTO);
    }

    @ActionFlag(detail = "Reconciliation_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增对账", notes = "新增对账")
    public Object create(@RequestBody ReconciliationDTO reconciliationDTO, HttpServletRequest request) {
        log.info("add reconciliation DTO:{}", reconciliationDTO);
        try {
            reconciliationService.saveReconciliation(reconciliationDTO, request);
        } catch (BizException e) {
            log.error("add reconciliation failed, reconciliationDTO: {}, error message:{}, error all:{}", reconciliationDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "Reconciliation_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改对账", notes = "修改对账")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody ReconciliationDTO reconciliationDTO, HttpServletRequest request) {
        log.info("put modify id:{}, reconciliation DTO:{}", id, reconciliationDTO);
        try {
            reconciliationService.updateReconciliation(id, reconciliationDTO, request);
        } catch (BizException e) {
            log.error("update reconciliation failed, reconciliationDTO: {}, error message:{}, error all:{}", reconciliationDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "Reconciliation_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除对账", notes = "删除对账")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete reconciliation, id:{}", id);
        try {
            reconciliationService.logicDeleteReconciliation(id, request);
        } catch (BizException e) {
            log.error("delete failed, reconciliation id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "Reconciliation_import")
    @PostMapping("/import/{type}")
    @ApiOperation(value = "导入对账文件", notes = "导入对账文件")
    public Object importReconciliation(@PathVariable("type") Integer type, @RequestBody MultipartFile multipartFile, HttpServletRequest request) {
        log.info("import file");
        try {
            reconciliationService.importReconciliation(type, multipartFile, request);
        } catch (Exception e) {
            log.error("import file failed, error message:{}, error all:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();

    }

    @ApiOperation(value = "订单对账详情列表", notes = "订单对账详情列表")
    @GetMapping(value = "/reconciliationDetail", name="订单对账详情列表")
    public Object reconciliationDetail(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("查询对账详情列表, params: {}", params);
        int total = reconciliationService.countReconciliationDetail(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ReconciliationDetailDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = reconciliationService.findReconciliationDetail(params, scs, pc);
        }
        return R.success(list, pc);
    }

}
