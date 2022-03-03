package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.CodeUpdateLogDTO;
import com.uwallet.pay.main.model.entity.CodeUpdateLog;
import com.uwallet.pay.main.service.CodeUpdateLogService;
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
 * 码操作记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 码操作记录表
 * @author: xucl
 * @date: Created in 2021-03-09 09:55:32
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@RestController
@RequestMapping("/codeUpdateLog")
@Slf4j
@Api("码操作记录表")
public class CodeUpdateLogController extends BaseController<CodeUpdateLog> {

    @Autowired
    private CodeUpdateLogService codeUpdateLogService;

    @ActionFlag(detail = "CodeUpdateLog_list")
    @ApiOperation(value = "分页查询码操作记录表", notes = "分页查询码操作记录表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-码操作记录表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = codeUpdateLogService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<CodeUpdateLogDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = codeUpdateLogService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询码操作记录表", notes = "通过id查询码操作记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get codeUpdateLog Id:{}", id);
        return R.success(codeUpdateLogService.findCodeUpdateLogById(id));
    }

    @ApiOperation(value = "通过查询条件查询码操作记录表一条数据", notes = "通过查询条件查询码操作记录表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询码操作记录表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get codeUpdateLog findOne params:{}", params);
        int total = codeUpdateLogService.count(params);
        if (total > 1) {
            log.error("get codeUpdateLog findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        CodeUpdateLogDTO codeUpdateLogDTO = null;
        if (total == 1) {
            codeUpdateLogDTO = codeUpdateLogService.findOneCodeUpdateLog(params);
        }
        return R.success(codeUpdateLogDTO);
    }

    @ActionFlag(detail = "CodeUpdateLog_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增码操作记录表", notes = "新增码操作记录表")
    public Object create(@RequestBody CodeUpdateLogDTO codeUpdateLogDTO, HttpServletRequest request) {
        log.info("add codeUpdateLog DTO:{}", codeUpdateLogDTO);
        try {
            codeUpdateLogService.saveCodeUpdateLog(codeUpdateLogDTO, request);
        } catch (BizException e) {
            log.error("add codeUpdateLog failed, codeUpdateLogDTO: {}, error message:{}, error all:{}", codeUpdateLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "CodeUpdateLog_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改码操作记录表", notes = "修改码操作记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody CodeUpdateLogDTO codeUpdateLogDTO, HttpServletRequest request) {
        log.info("put modify id:{}, codeUpdateLog DTO:{}", id, codeUpdateLogDTO);
        try {
            codeUpdateLogService.updateCodeUpdateLog(id, codeUpdateLogDTO, request);
        } catch (BizException e) {
            log.error("update codeUpdateLog failed, codeUpdateLogDTO: {}, error message:{}, error all:{}", codeUpdateLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "CodeUpdateLog_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除码操作记录表", notes = "删除码操作记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete codeUpdateLog, id:{}", id);
        try {
            codeUpdateLogService.logicDeleteCodeUpdateLog(id, request);
        } catch (BizException e) {
            log.error("delete failed, codeUpdateLog id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
