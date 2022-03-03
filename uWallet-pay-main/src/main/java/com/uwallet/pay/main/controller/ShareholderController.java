package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.ShareholderDTO;
import com.uwallet.pay.main.model.entity.Shareholder;
import com.uwallet.pay.main.service.ShareholderService;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.controller.BaseController;
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
 * 受益人信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 受益人信息表
 * @author: baixinyue
 * @date: Created in 2020-04-20 17:16:45
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/shareholder")
@Slf4j
@Api("受益人信息表")
public class ShareholderController extends BaseController<Shareholder> {

    @Autowired
    private ShareholderService shareholderService;

    @ApiOperation(value = "分页查询受益人信息表", notes = "分页查询受益人信息表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-受益人信息表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = shareholderService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ShareholderDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = shareholderService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询受益人信息表", notes = "通过id查询受益人信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get shareholder Id:{}", id);
        return R.success(shareholderService.findShareholderById(id));
    }

    @ApiOperation(value = "通过查询条件查询受益人信息表一条数据", notes = "通过查询条件查询受益人信息表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询受益人信息表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get shareholder findOne params:{}", params);
        int total = shareholderService.count(params);
        if (total > 1) {
            log.error("get shareholder findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), "查询失败，返回多条数据");
        }
        ShareholderDTO shareholderDTO = null;
        if (total == 1) {
            shareholderDTO = shareholderService.findOneShareholder(params);
        }
        return R.success(shareholderDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增受益人信息表", notes = "新增受益人信息表")
    public Object create(@RequestBody ShareholderDTO shareholderDTO, HttpServletRequest request) {
        log.info("add shareholder DTO:{}", shareholderDTO);
        try {
            shareholderService.saveShareholder(shareholderDTO, request);
        } catch (BizException e) {
            log.error("add shareholder failed, shareholderDTO: {}, error message:{}, error all:{}", shareholderDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改受益人信息表", notes = "修改受益人信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody ShareholderDTO shareholderDTO, HttpServletRequest request) {
        log.info("put modify id:{}, shareholder DTO:{}", id, shareholderDTO);
        try {
            shareholderService.updateShareholder(id, shareholderDTO, request);
        } catch (BizException e) {
            log.error("update shareholder failed, shareholderDTO: {}, error message:{}, error all:{}", shareholderDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除受益人信息表", notes = "删除受益人信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete shareholder, id:{}", id);
        try {
            shareholderService.logicDeleteShareholder(id, request);
        } catch (BizException e) {
            log.error("delete failed, shareholder id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
