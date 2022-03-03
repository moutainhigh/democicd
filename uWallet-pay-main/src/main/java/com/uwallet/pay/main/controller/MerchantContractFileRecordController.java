package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.MerchantContractFileRecordDTO;
import com.uwallet.pay.main.model.entity.MerchantContractFileRecord;
import com.uwallet.pay.main.service.MerchantContractFileRecordService;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;


/**
 * <p>
 * 合同记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 合同记录表
 * @author: fenmi
 * @date: Created in 2021-04-29 10:11:38
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: fenmi
 */
@RestController
@RequestMapping("/merchantContractFileRecord")
@Slf4j
@Api("合同记录表")
public class MerchantContractFileRecordController extends BaseController<MerchantContractFileRecord> {

    @Autowired
    private MerchantContractFileRecordService merchantContractFileRecordService;


    @ActionFlag(detail = "Merchant_list")
    @ApiOperation(value = "分页查询合同记录表", notes = "分页查询合同记录表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-合同记录表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = merchantContractFileRecordService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MerchantContractFileRecordDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = merchantContractFileRecordService.listContractFile(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询合同记录表", notes = "通过id查询合同记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get merchantContractFileRecord Id:{}", id);
        return R.success(merchantContractFileRecordService.findMerchantContractFileRecordById(id));
    }

    @ApiOperation(value = "通过查询条件查询合同记录表一条数据", notes = "通过查询条件查询合同记录表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询合同记录表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get merchantContractFileRecord findOne params:{}", params);
        int total = merchantContractFileRecordService.count(params);
        if (total > 1) {
            log.error("get merchantContractFileRecord findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        MerchantContractFileRecordDTO merchantContractFileRecordDTO = null;
        if (total == 1) {
            merchantContractFileRecordDTO = merchantContractFileRecordService.findOneMerchantContractFileRecord(params);
        }
        return R.success(merchantContractFileRecordDTO);
    }

    @ActionFlag(detail = "Merchant_list")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增合同记录表", notes = "新增合同记录表")
    public Object create(@RequestBody MerchantContractFileRecordDTO merchantContractFileRecordDTO, HttpServletRequest request) {
        log.info("add merchantContractFileRecord DTO:{}", merchantContractFileRecordDTO);
        try {
            merchantContractFileRecordService.saveMerchantContractFileRecord(merchantContractFileRecordDTO, request);
        } catch (BizException e) {
            log.error("add merchantContractFileRecord failed, merchantContractFileRecordDTO: {}, error message:{}, error all:{}", merchantContractFileRecordDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "MerchantContractFileRecord_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改合同记录表", notes = "修改合同记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody MerchantContractFileRecordDTO merchantContractFileRecordDTO, HttpServletRequest request) {
        log.info("put modify id:{}, merchantContractFileRecord DTO:{}", id, merchantContractFileRecordDTO);
        try {
            merchantContractFileRecordService.updateMerchantContractFileRecord(id, merchantContractFileRecordDTO, request);
        } catch (BizException e) {
            log.error("update merchantContractFileRecord failed, merchantContractFileRecordDTO: {}, error message:{}, error all:{}", merchantContractFileRecordDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "Merchant_list")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除合同记录表", notes = "删除合同记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete merchantContractFileRecord, id:{}", id);
        try {
            merchantContractFileRecordService.logicDeleteMerchantContractFileRecord(id, request);
        } catch (BizException e) {
            log.error("delete failed, merchantContractFileRecord id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
