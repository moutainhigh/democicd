package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.HolidaysConfigDTO;
import com.uwallet.pay.main.model.entity.HolidaysConfig;
import com.uwallet.pay.main.service.HolidaysConfigService;
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
 * 节假日表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 节假日表
 * @author: baixinyue
 * @date: Created in 2020-09-08 11:24:52
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/holidaysConfig")
@Slf4j
@Api("节假日表")
public class HolidaysConfigController extends BaseController<HolidaysConfig> {

    @Autowired
    private HolidaysConfigService holidaysConfigService;

    @ActionFlag(detail = "HolidaysConfig_list")
    @ApiOperation(value = "分页查询节假日表", notes = "分页查询节假日表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-节假日表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = holidaysConfigService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<HolidaysConfigDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = holidaysConfigService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询节假日表", notes = "通过id查询节假日表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get holidaysConfig Id:{}", id);
        return R.success(holidaysConfigService.findHolidaysConfigById(id));
    }

    @ApiOperation(value = "通过查询条件查询节假日表一条数据", notes = "通过查询条件查询节假日表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询节假日表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get holidaysConfig findOne params:{}", params);
        int total = holidaysConfigService.count(params);
        if (total > 1) {
            log.error("get holidaysConfig findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        HolidaysConfigDTO holidaysConfigDTO = null;
        if (total == 1) {
            holidaysConfigDTO = holidaysConfigService.findOneHolidaysConfig(params);
        }
        return R.success(holidaysConfigDTO);
    }

    @ActionFlag(detail = "HolidaysConfig_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增节假日表", notes = "新增节假日表")
    public Object create(@RequestBody HolidaysConfigDTO holidaysConfigDTO, HttpServletRequest request) {
        log.info("add holidaysConfig DTO:{}", holidaysConfigDTO);
        try {
            holidaysConfigService.saveHolidaysConfig(holidaysConfigDTO, request);
        } catch (BizException e) {
            log.error("add holidaysConfig failed, holidaysConfigDTO: {}, error message:{}, error all:{}", holidaysConfigDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "HolidaysConfig_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改节假日表", notes = "修改节假日表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody HolidaysConfigDTO holidaysConfigDTO, HttpServletRequest request) {
        log.info("put modify id:{}, holidaysConfig DTO:{}", id, holidaysConfigDTO);
        try {
            holidaysConfigService.updateHolidaysConfig(id, holidaysConfigDTO, request);
        } catch (BizException e) {
            log.error("update holidaysConfig failed, holidaysConfigDTO: {}, error message:{}, error all:{}", holidaysConfigDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "HolidaysConfig_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除节假日表", notes = "删除节假日表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete holidaysConfig, id:{}", id);
        try {
            holidaysConfigService.logicDeleteHolidaysConfig(id, request);
        } catch (BizException e) {
            log.error("delete failed, holidaysConfig id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
