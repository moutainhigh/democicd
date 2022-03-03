package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.IllionInstitutionsDTO;
import com.uwallet.pay.main.model.entity.IllionInstitutions;
import com.uwallet.pay.main.service.IllionInstitutionsService;
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
 * 
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 
 * @author: xucl
 * @date: Created in 2021-03-19 09:37:47
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@RestController
@RequestMapping("/illionInstitutions")
@Slf4j
@Api("")
public class IllionInstitutionsController extends BaseController<IllionInstitutions> {

    @Autowired
    private IllionInstitutionsService illionInstitutionsService;

    @ActionFlag(detail = "IllionInstitutions_list")
    @ApiOperation(value = "分页查询", notes = "分页查询以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = illionInstitutionsService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<IllionInstitutionsDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = illionInstitutionsService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get illionInstitutions Id:{}", id);
        return R.success(illionInstitutionsService.findIllionInstitutionsById(id));
    }

    @ApiOperation(value = "通过查询条件查询一条数据", notes = "通过查询条件查询一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get illionInstitutions findOne params:{}", params);
        int total = illionInstitutionsService.count(params);
        if (total > 1) {
            log.error("get illionInstitutions findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        IllionInstitutionsDTO illionInstitutionsDTO = null;
        if (total == 1) {
            illionInstitutionsDTO = illionInstitutionsService.findOneIllionInstitutions(params);
        }
        return R.success(illionInstitutionsDTO);
    }

    @ActionFlag(detail = "IllionInstitutions_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增", notes = "新增")
    public Object create(@RequestBody IllionInstitutionsDTO illionInstitutionsDTO, HttpServletRequest request) {
        log.info("add illionInstitutions DTO:{}", illionInstitutionsDTO);
        try {
            illionInstitutionsService.saveIllionInstitutions(illionInstitutionsDTO, request);
        } catch (BizException e) {
            log.error("add illionInstitutions failed, illionInstitutionsDTO: {}, error message:{}, error all:{}", illionInstitutionsDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "IllionInstitutions_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改", notes = "修改")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody IllionInstitutionsDTO illionInstitutionsDTO, HttpServletRequest request) {
        log.info("put modify id:{}, illionInstitutions DTO:{}", id, illionInstitutionsDTO);
        try {
            illionInstitutionsService.updateIllionInstitutions(id, illionInstitutionsDTO, request);
        } catch (BizException e) {
            log.error("update illionInstitutions failed, illionInstitutionsDTO: {}, error message:{}, error all:{}", illionInstitutionsDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "IllionInstitutions_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除", notes = "删除")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete illionInstitutions, id:{}", id);
        try {
            illionInstitutionsService.logicDeleteIllionInstitutions(id, request);
        } catch (BizException e) {
            log.error("delete failed, illionInstitutions id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
