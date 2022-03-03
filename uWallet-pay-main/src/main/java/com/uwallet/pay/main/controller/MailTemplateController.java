package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.MailTemplateDTO;
import com.uwallet.pay.main.model.entity.MailTemplate;
import com.uwallet.pay.main.service.MailTemplateService;
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
 * @date: Created in 2020-01-04 13:56:55
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/mailTemplate")
@Slf4j
@Api("模板")
public class MailTemplateController extends BaseController<MailTemplate> {

    @Autowired
    private MailTemplateService mailTemplateService;

    @ApiOperation(value = "分页查询模板", notes = "分页查询模板以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-模板列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = mailTemplateService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MailTemplateDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = mailTemplateService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询模板", notes = "通过id查询模板")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get mailTemplate Id:{}", id);
        return R.success(mailTemplateService.findMailTemplateById(id));
    }

    @ApiOperation(value = "通过查询条件查询模板一条数据", notes = "通过查询条件查询模板一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询模板一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get mailTemplate findOne params:{}", params);
        int total = mailTemplateService.count(params);
        if (total > 1) {
            log.error("get mailTemplate findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        MailTemplateDTO mailTemplateDTO = null;
        if (total == 1) {
            mailTemplateDTO = mailTemplateService.findOneMailTemplate(params);
        }
        return R.success(mailTemplateDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增模板", notes = "新增模板")
    public Object create(@RequestBody MailTemplateDTO mailTemplateDTO, HttpServletRequest request) {
        log.info("add mailTemplate DTO:{}", mailTemplateDTO);
        try {
            mailTemplateService.saveMailTemplate(mailTemplateDTO, request);
        } catch (BizException e) {
            log.error("add mailTemplate failed, mailTemplateDTO: {}, error message:{}, error all:{}", mailTemplateDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改模板", notes = "修改模板")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody MailTemplateDTO mailTemplateDTO, HttpServletRequest request) {
        log.info("put modify id:{}, mailTemplate DTO:{}", id, mailTemplateDTO);
        try {
            mailTemplateService.updateMailTemplate(id, mailTemplateDTO, request);
        } catch (BizException e) {
            log.error("update mailTemplate failed, mailTemplateDTO: {}, error message:{}, error all:{}", mailTemplateDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除模板", notes = "删除模板")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete mailTemplate, id:{}", id);
        try {
            mailTemplateService.logicDeleteMailTemplate(id, request);
        } catch (BizException e) {
            log.error("delete failed, mailTemplate id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
