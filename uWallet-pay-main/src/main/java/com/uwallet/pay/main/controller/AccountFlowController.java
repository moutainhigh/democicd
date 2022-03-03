package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.AccountFlowDTO;
import com.uwallet.pay.main.model.entity.AccountFlow;
import com.uwallet.pay.main.service.AccountFlowService;
import com.uwallet.pay.main.service.DocuSignService;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;


/**
 * <p>
 * 账户动账交易流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 账户动账交易流水表
 * @author: baixinyue
 * @date: Created in 2019-12-16 10:49:00
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/accountFlow")
@Slf4j
@Api("账户动账交易流水表")
public class AccountFlowController extends BaseController<AccountFlow> {

    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private DocuSignService docuSignService;

    @ActionFlag(detail = "AccountFlow_list")
    @ApiOperation(value = "分页查询账户动账交易流水表", notes = "分页查询账户动账交易流水表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-账户动账交易流水表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = accountFlowService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<AccountFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = accountFlowService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询账户动账交易流水表", notes = "通过id查询账户动账交易流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get accountFlow Id:{}", id);
        return R.success(accountFlowService.findAccountFlowById(id));
    }

    @ApiOperation(value = "通过查询条件查询账户动账交易流水表一条数据", notes = "通过查询条件查询账户动账交易流水表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询账户动账交易流水表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get accountFlow findOne params:{}", params);
        int total = accountFlowService.count(params);
        if (total > 1) {
            log.error("get accountFlow findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        AccountFlowDTO accountFlowDTO = null;
        if (total == 1) {
            accountFlowDTO = accountFlowService.findOneAccountFlow(params);
        }
        return R.success(accountFlowDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增账户动账交易流水表", notes = "新增账户动账交易流水表")
    public Object create(@RequestBody AccountFlowDTO accountFlowDTO, HttpServletRequest request) {
        log.info("add accountFlow DTO:{}", accountFlowDTO);
        try {
            accountFlowService.saveAccountFlow(accountFlowDTO, request);
        } catch (BizException e) {
            log.error("add accountFlow failed, accountFlowDTO: {}, error message:{}, error all:{}", accountFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改账户动账交易流水表", notes = "修改账户动账交易流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody AccountFlowDTO accountFlowDTO, HttpServletRequest request) {
        log.info("put modify id:{}, accountFlow DTO:{}", id, accountFlowDTO);
        try {
            accountFlowService.updateAccountFlow(id, accountFlowDTO, request);
        } catch (BizException e) {
            log.error("update accountFlow failed, accountFlowDTO: {}, error message:{}, error all:{}", accountFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除账户动账交易流水表", notes = "删除账户动账交易流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete accountFlow, id:{}", id);
        try {
            accountFlowService.logicDeleteAccountFlow(id, request);
        } catch (BizException e) {
            log.error("delete failed, accountFlow id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }
    @PostMapping("/download")
    public Object download(@RequestBody JSONObject jsonObject, HttpServletRequest request){
        try{
            return R.success(docuSignService.getDocument(jsonObject,request));
        }catch (Exception e){
            System.out.println(e.getMessage());
            return R.fail();
        }
    }
    @PassToken
    @PostMapping("/sign")
    public Object sign(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        JSONObject res;
        try {
           res = docuSignService.genSignUrl(jsonObject, request);
        } catch (Exception e) {
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(res);
    }
}
