package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.TrusteeDTO;
import com.uwallet.pay.main.model.entity.Trustee;
import com.uwallet.pay.main.service.TrusteeService;
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
 * 受托人信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 受托人信息表
 * @author: baixinyue
 * @date: Created in 2020-04-21 14:25:22
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/trustee")
@Slf4j
@Api("受托人信息表")
public class TrusteeController extends BaseController<Trustee> {

    @Autowired
    private TrusteeService trusteeService;

    @ActionFlag(detail = "Trustee_list")
    @ApiOperation(value = "分页查询受托人信息表", notes = "分页查询受托人信息表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-受托人信息表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = trusteeService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<TrusteeDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = trusteeService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询受托人信息表", notes = "通过id查询受托人信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get trustee Id:{}", id);
        return R.success(trusteeService.findTrusteeById(id));
    }

    @ApiOperation(value = "通过查询条件查询受托人信息表一条数据", notes = "通过查询条件查询受托人信息表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询受托人信息表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get trustee findOne params:{}", params);
        int total = trusteeService.count(params);
        if (total > 1) {
            log.error("get trustee findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        TrusteeDTO trusteeDTO = null;
        if (total == 1) {
            trusteeDTO = trusteeService.findOneTrustee(params);
        }
        return R.success(trusteeDTO);
    }

    @ActionFlag(detail = "Trustee_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增受托人信息表", notes = "新增受托人信息表")
    public Object create(@RequestBody TrusteeDTO trusteeDTO, HttpServletRequest request) {
        log.info("add trustee DTO:{}", trusteeDTO);
        try {
            trusteeService.saveTrustee(trusteeDTO, request);
        } catch (BizException e) {
            log.error("add trustee failed, trusteeDTO: {}, error message:{}, error all:{}", trusteeDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "Trustee_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改受托人信息表", notes = "修改受托人信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody TrusteeDTO trusteeDTO, HttpServletRequest request) {
        log.info("put modify id:{}, trustee DTO:{}", id, trusteeDTO);
        try {
            trusteeService.updateTrustee(id, trusteeDTO, request);
        } catch (BizException e) {
            log.error("update trustee failed, trusteeDTO: {}, error message:{}, error all:{}", trusteeDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "Trustee_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除受托人信息表", notes = "删除受托人信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete trustee, id:{}", id);
        try {
            trusteeService.logicDeleteTrustee(id, request);
        } catch (BizException e) {
            log.error("delete failed, trustee id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
