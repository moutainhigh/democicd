package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.IllionSubmitStatusLogDTO;
import com.uwallet.pay.main.model.entity.IllionSubmitStatusLog;
import com.uwallet.pay.main.service.IllionSubmitStatusLogService;
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
 * @date: Created in 2021-06-22 13:04:06
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@RestController
@RequestMapping("/illionSubmitStatusLog")
@Slf4j
@Api("")
public class IllionSubmitStatusLogController extends BaseController<IllionSubmitStatusLog> {

    @Autowired
    private IllionSubmitStatusLogService illionSubmitStatusLogService;

    @ActionFlag(detail = "IllionSubmitStatusLog_list")
    @ApiOperation(value = "分页查询", notes = "分页查询以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = illionSubmitStatusLogService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<IllionSubmitStatusLogDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = illionSubmitStatusLogService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get illionSubmitStatusLog Id:{}", id);
        return R.success(illionSubmitStatusLogService.findIllionSubmitStatusLogById(id));
    }

    @ApiOperation(value = "通过查询条件查询一条数据", notes = "通过查询条件查询一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get illionSubmitStatusLog findOne params:{}", params);
        int total = illionSubmitStatusLogService.count(params);
        if (total > 1) {
            log.error("get illionSubmitStatusLog findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        IllionSubmitStatusLogDTO illionSubmitStatusLogDTO = null;
        if (total == 1) {
            illionSubmitStatusLogDTO = illionSubmitStatusLogService.findOneIllionSubmitStatusLog(params);
        }
        return R.success(illionSubmitStatusLogDTO);
    }

    @ActionFlag(detail = "IllionSubmitStatusLog_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增", notes = "新增")
    public Object create(@RequestBody IllionSubmitStatusLogDTO illionSubmitStatusLogDTO, HttpServletRequest request) {
        log.info("add illionSubmitStatusLog DTO:{}", illionSubmitStatusLogDTO);
        try {
            illionSubmitStatusLogService.saveIllionSubmitStatusLog(illionSubmitStatusLogDTO, request);
        } catch (BizException e) {
            log.error("add illionSubmitStatusLog failed, illionSubmitStatusLogDTO: {}, error message:{}, error all:{}", illionSubmitStatusLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "IllionSubmitStatusLog_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改", notes = "修改")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody IllionSubmitStatusLogDTO illionSubmitStatusLogDTO, HttpServletRequest request) {
        log.info("put modify id:{}, illionSubmitStatusLog DTO:{}", id, illionSubmitStatusLogDTO);
        try {
            illionSubmitStatusLogService.updateIllionSubmitStatusLog(id, illionSubmitStatusLogDTO, request);
        } catch (BizException e) {
            log.error("update illionSubmitStatusLog failed, illionSubmitStatusLogDTO: {}, error message:{}, error all:{}", illionSubmitStatusLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "IllionSubmitStatusLog_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除", notes = "删除")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete illionSubmitStatusLog, id:{}", id);
        try {
            illionSubmitStatusLogService.logicDeleteIllionSubmitStatusLog(id, request);
        } catch (BizException e) {
            log.error("delete failed, illionSubmitStatusLog id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
