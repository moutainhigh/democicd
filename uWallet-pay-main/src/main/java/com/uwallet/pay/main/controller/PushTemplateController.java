package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.PushTemplateDTO;
import com.uwallet.pay.main.model.entity.PushTemplate;
import com.uwallet.pay.main.service.PushTemplateService;
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
 * 模板
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 模板
 * @author: zhoutt
 * @date: Created in 2020-01-04 13:52:28
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/pushTemplate")
@Slf4j
@Api("模板")
public class PushTemplateController extends BaseController<PushTemplate> {

    @Autowired
    private PushTemplateService pushTemplateService;

    @ApiOperation(value = "分页查询模板", notes = "分页查询模板以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-模板列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = pushTemplateService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<PushTemplateDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = pushTemplateService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询模板", notes = "通过id查询模板")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get pushTemplate Id:{}", id);
        return R.success(pushTemplateService.findPushTemplateById(id));
    }

    @ApiOperation(value = "通过查询条件查询模板一条数据", notes = "通过查询条件查询模板一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询模板一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get pushTemplate findOne params:{}", params);
        int total = pushTemplateService.count(params);
        if (total > 1) {
            log.error("get pushTemplate findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        PushTemplateDTO pushTemplateDTO = null;
        if (total == 1) {
            pushTemplateDTO = pushTemplateService.findOnePushTemplate(params);
        }
        return R.success(pushTemplateDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增模板", notes = "新增模板")
    public Object create(@RequestBody PushTemplateDTO pushTemplateDTO, HttpServletRequest request) {
        log.info("add pushTemplate DTO:{}", pushTemplateDTO);
        try {
            pushTemplateService.savePushTemplate(pushTemplateDTO, request);
        } catch (BizException e) {
            log.error("add pushTemplate failed, pushTemplateDTO: {}, error message:{}, error all:{}", pushTemplateDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改模板", notes = "修改模板")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody PushTemplateDTO pushTemplateDTO, HttpServletRequest request) {
        log.info("put modify id:{}, pushTemplate DTO:{}", id, pushTemplateDTO);
        try {
            pushTemplateService.updatePushTemplate(id, pushTemplateDTO, request);
        } catch (BizException e) {
            log.error("update pushTemplate failed, pushTemplateDTO: {}, error message:{}, error all:{}", pushTemplateDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除模板", notes = "删除模板")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete pushTemplate, id:{}", id);
        try {
            pushTemplateService.logicDeletePushTemplate(id, request);
        } catch (BizException e) {
            log.error("delete failed, pushTemplate id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
