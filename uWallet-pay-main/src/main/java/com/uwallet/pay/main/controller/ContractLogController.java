package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.ContractLogDTO;
import com.uwallet.pay.main.model.entity.ContractLog;
import com.uwallet.pay.main.service.ContractLogService;
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
 * 合同记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 合同记录表
 * @author: xucl
 * @date: Created in 2021-04-27 10:13:42
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@RestController
@RequestMapping("/contractLog")
@Slf4j
@Api("合同记录表")
public class ContractLogController extends BaseController<ContractLog> {

    @Autowired
    private ContractLogService contractLogService;

    @ActionFlag(detail = "ContractLog_list")
    @ApiOperation(value = "分页查询合同记录表", notes = "分页查询合同记录表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-合同记录表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = contractLogService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ContractLogDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = contractLogService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询合同记录表", notes = "通过id查询合同记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get contractLog Id:{}", id);
        return R.success(contractLogService.findContractLogById(id));
    }

    @ApiOperation(value = "通过查询条件查询合同记录表一条数据", notes = "通过查询条件查询合同记录表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询合同记录表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get contractLog findOne params:{}", params);
        int total = contractLogService.count(params);
        if (total > 1) {
            log.error("get contractLog findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        ContractLogDTO contractLogDTO = null;
        if (total == 1) {
            contractLogDTO = contractLogService.findOneContractLog(params);
        }
        return R.success(contractLogDTO);
    }

    @ActionFlag(detail = "ContractLog_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增合同记录表", notes = "新增合同记录表")
    public Object create(@RequestBody ContractLogDTO contractLogDTO, HttpServletRequest request) {
        log.info("add contractLog DTO:{}", contractLogDTO);
        try {
            contractLogService.saveContractLog(contractLogDTO, request);
        } catch (BizException e) {
            log.error("add contractLog failed, contractLogDTO: {}, error message:{}, error all:{}", contractLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ContractLog_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改合同记录表", notes = "修改合同记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody ContractLogDTO contractLogDTO, HttpServletRequest request) {
        log.info("put modify id:{}, contractLog DTO:{}", id, contractLogDTO);
        try {
            contractLogService.updateContractLog(id, contractLogDTO, request);
        } catch (BizException e) {
            log.error("update contractLog failed, contractLogDTO: {}, error message:{}, error all:{}", contractLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ContractLog_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除合同记录表", notes = "删除合同记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete contractLog, id:{}", id);
        try {
            contractLogService.logicDeleteContractLog(id, request);
        } catch (BizException e) {
            log.error("delete failed, contractLog id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
