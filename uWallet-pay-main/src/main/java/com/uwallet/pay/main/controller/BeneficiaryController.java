package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.BeneficiaryDTO;
import com.uwallet.pay.main.model.entity.Beneficiary;
import com.uwallet.pay.main.service.BeneficiaryService;
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
 * 受益人信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 受益人信息表
 * @author: Rainc
 * @date: Created in 2019-12-11 16:51:43
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Rainc
 */
@RestController
@RequestMapping("/beneficiary")
@Slf4j
@Api("受益人信息表")
public class BeneficiaryController extends BaseController<Beneficiary> {

    @Autowired
    private BeneficiaryService beneficiaryService;

    @ActionFlag(detail = "Beneficiary_list")
    @ApiOperation(value = "分页查询受益人信息表", notes = "分页查询受益人信息表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-受益人信息表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = beneficiaryService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<BeneficiaryDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = beneficiaryService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询受益人信息表", notes = "通过id查询受益人信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get beneficiary Id:{}", id);
        return R.success(beneficiaryService.findBeneficiaryById(id));
    }

    @ApiOperation(value = "通过查询条件查询受益人信息表一条数据", notes = "通过查询条件查询受益人信息表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询受益人信息表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get beneficiary findOne params:{}", params);
        int total = beneficiaryService.count(params);
        if (total > 1) {
            log.error("get beneficiary findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        BeneficiaryDTO beneficiaryDTO = null;
        if (total == 1) {
            beneficiaryDTO = beneficiaryService.findOneBeneficiary(params);
        }
        return R.success(beneficiaryDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增受益人信息表", notes = "新增受益人信息表")
    public Object create(@RequestBody BeneficiaryDTO beneficiaryDTO, HttpServletRequest request) {
        log.info("add beneficiary DTO:{}", beneficiaryDTO);
        try {
            beneficiaryService.saveBeneficiary(beneficiaryDTO, request);
        } catch (BizException e) {
            log.error("add beneficiary failed, beneficiaryDTO: {}, error message:{}, error all:{}", beneficiaryDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改受益人信息表", notes = "修改受益人信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody BeneficiaryDTO beneficiaryDTO, HttpServletRequest request) {
        log.info("put modify id:{}, beneficiary DTO:{}", id, beneficiaryDTO);
        try {
            beneficiaryService.updateBeneficiary(id, beneficiaryDTO, request);
        } catch (BizException e) {
            log.error("update beneficiary failed, beneficiaryDTO: {}, error message:{}, error all:{}", beneficiaryDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除受益人信息表", notes = "删除受益人信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete beneficiary, id:{}", id);
        try {
            beneficiaryService.logicDeleteBeneficiary(id, request);
        } catch (BizException e) {
            log.error("delete failed, beneficiary id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
