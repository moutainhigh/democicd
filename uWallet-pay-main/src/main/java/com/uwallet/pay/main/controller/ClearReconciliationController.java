package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.ClearReconciliationDTO;
import com.uwallet.pay.main.model.entity.ClearReconciliation;
import com.uwallet.pay.main.service.ClearReconciliationService;
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
 * 清算对账表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 清算对账表
 * @author: baixinyue
 * @date: Created in 2020-03-06 09:00:14
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/clearReconciliation")
@Slf4j
@Api("清算对账表")
public class ClearReconciliationController extends BaseController<ClearReconciliation> {

    @Autowired
    private ClearReconciliationService clearReconciliationService;

    @ActionFlag(detail = "ClearReconciliation_list")
    @ApiOperation(value = "分页查询清算对账表", notes = "分页查询清算对账表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-清算对账表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = clearReconciliationService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ClearReconciliationDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = clearReconciliationService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询清算对账表", notes = "通过id查询清算对账表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get clearReconciliation Id:{}", id);
        return R.success(clearReconciliationService.findClearReconciliationById(id));
    }

    @ApiOperation(value = "通过查询条件查询清算对账表一条数据", notes = "通过查询条件查询清算对账表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询清算对账表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get clearReconciliation findOne params:{}", params);
        int total = clearReconciliationService.count(params);
        if (total > 1) {
            log.error("get clearReconciliation findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        ClearReconciliationDTO clearReconciliationDTO = null;
        if (total == 1) {
            clearReconciliationDTO = clearReconciliationService.findOneClearReconciliation(params);
        }
        return R.success(clearReconciliationDTO);
    }

    @ActionFlag(detail = "ClearReconciliation_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增清算对账表", notes = "新增清算对账表")
    public Object create(@RequestBody ClearReconciliationDTO clearReconciliationDTO, HttpServletRequest request) {
        log.info("add clearReconciliation DTO:{}", clearReconciliationDTO);
        try {
            clearReconciliationService.saveClearReconciliation(clearReconciliationDTO, request);
        } catch (BizException e) {
            log.error("add clearReconciliation failed, clearReconciliationDTO: {}, error message:{}, error all:{}", clearReconciliationDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ClearReconciliation_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改清算对账表", notes = "修改清算对账表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody ClearReconciliationDTO clearReconciliationDTO, HttpServletRequest request) {
        log.info("put modify id:{}, clearReconciliation DTO:{}", id, clearReconciliationDTO);
        try {
            clearReconciliationService.updateClearReconciliation(id, clearReconciliationDTO, request);
        } catch (BizException e) {
            log.error("update clearReconciliation failed, clearReconciliationDTO: {}, error message:{}, error all:{}", clearReconciliationDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ClearReconciliation_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除清算对账表", notes = "删除清算对账表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete clearReconciliation, id:{}", id);
        try {
            clearReconciliationService.logicDeleteClearReconciliation(id, request);
        } catch (BizException e) {
            log.error("delete failed, clearReconciliation id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ClearReconciliation_list")
    @PostMapping("/import/{type}")
    @ApiOperation(value = "导入对账文件", notes = "导入对账文件")
    public Object importClearFile(@PathVariable("type") Integer type, @RequestBody MultipartFile multipartFile, HttpServletRequest request) {
        log.info("import clear file");
        try {
            clearReconciliationService.importClearFile(type, multipartFile, request);
        } catch (Exception e) {
            log.error("import file failed, error message:{}, error all:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
