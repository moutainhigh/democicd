package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.IpLocationDTO;
import com.uwallet.pay.main.model.entity.IpLocation;
import com.uwallet.pay.main.service.IpLocationService;
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
 * ip定位
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: ip定位
 * @author: baixinyue
 * @date: Created in 2021-01-12 13:54:55
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/ipLocation")
@Slf4j
@Api("ip定位")
public class IpLocationController extends BaseController<IpLocation> {

    @Autowired
    private IpLocationService ipLocationService;

    @ApiOperation(value = "分页查询ip定位", notes = "分页查询ip定位以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-ip定位列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = ipLocationService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<IpLocationDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = ipLocationService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询ip定位", notes = "通过id查询ip定位")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get ipLocation Id:{}", id);
        return R.success(ipLocationService.findIpLocationById(id));
    }

    @ApiOperation(value = "通过查询条件查询ip定位一条数据", notes = "通过查询条件查询ip定位一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询ip定位一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get ipLocation findOne params:{}", params);
        int total = ipLocationService.count(params);
        if (total > 1) {
            log.error("get ipLocation findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), "查询失败，返回多条数据");
        }
        IpLocationDTO ipLocationDTO = null;
        if (total == 1) {
            ipLocationDTO = ipLocationService.findOneIpLocation(params);
        }
        return R.success(ipLocationDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增ip定位", notes = "新增ip定位")
    public Object create(@RequestBody IpLocationDTO ipLocationDTO, HttpServletRequest request) {
        log.info("add ipLocation DTO:{}", ipLocationDTO);
        try {
            ipLocationService.saveIpLocation(ipLocationDTO, request);
        } catch (BizException e) {
            log.error("add ipLocation failed, ipLocationDTO: {}, error message:{}, error all:{}", ipLocationDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改ip定位", notes = "修改ip定位")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody IpLocationDTO ipLocationDTO, HttpServletRequest request) {
        log.info("put modify id:{}, ipLocation DTO:{}", id, ipLocationDTO);
        try {
            ipLocationService.updateIpLocation(id, ipLocationDTO, request);
        } catch (BizException e) {
            log.error("update ipLocation failed, ipLocationDTO: {}, error message:{}, error all:{}", ipLocationDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除ip定位", notes = "删除ip定位")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete ipLocation, id:{}", id);
        try {
            ipLocationService.logicDeleteIpLocation(id, request);
        } catch (BizException e) {
            log.error("delete failed, ipLocation id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
