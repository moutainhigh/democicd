package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.InvestApproveDTO;
import com.uwallet.pay.main.model.dto.RiskApproveLogDTO;
import com.uwallet.pay.main.model.entity.RiskApproveLog;
import com.uwallet.pay.main.service.RiskApproveLogService;
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
 * 用户风控审核日志
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 用户风控审核日志
 * @author: baixinyue
 * @date: Created in 2020-03-25 10:11:54
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/riskApproveLog")
@Slf4j
@Api("用户风控审核日志")
public class RiskApproveLogController extends BaseController<RiskApproveLog> {

    @Autowired
    private RiskApproveLogService riskApproveLogService;

    @ActionFlag(detail = "RiskApproveLog_list")
    @ApiOperation(value = "分页查询用户风控审核日志", notes = "分页查询用户风控审核日志以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-用户风控审核日志列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = riskApproveLogService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<RiskApproveLogDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = riskApproveLogService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询用户风控审核日志", notes = "通过id查询用户风控审核日志")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get riskApproveLog Id:{}", id);
        return R.success(riskApproveLogService.findRiskApproveLogById(id));
    }

    @ApiOperation(value = "通过查询条件查询用户风控审核日志一条数据", notes = "通过查询条件查询用户风控审核日志一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询用户风控审核日志一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get riskApproveLog findOne params:{}", params);
        int total = riskApproveLogService.count(params);
        if (total > 1) {
            log.error("get riskApproveLog findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        RiskApproveLogDTO riskApproveLogDTO = null;
        if (total == 1) {
            riskApproveLogDTO = riskApproveLogService.findOneRiskApproveLog(params);
        }
        return R.success(riskApproveLogDTO);
    }

    @ActionFlag(detail = "RiskApproveLog_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增用户风控审核日志", notes = "新增用户风控审核日志")
    public Object create(@RequestBody RiskApproveLogDTO riskApproveLogDTO, HttpServletRequest request) {
        log.info("add riskApproveLog DTO:{}", riskApproveLogDTO);
        try {
            riskApproveLogService.saveRiskApproveLog(riskApproveLogDTO, request);
        } catch (BizException e) {
            log.error("add riskApproveLog failed, riskApproveLogDTO: {}, error message:{}, error all:{}", riskApproveLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "RiskApproveLog_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改用户风控审核日志", notes = "修改用户风控审核日志")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody RiskApproveLogDTO riskApproveLogDTO, HttpServletRequest request) {
        log.info("put modify id:{}, riskApproveLog DTO:{}", id, riskApproveLogDTO);
        try {
            riskApproveLogService.updateRiskApproveLog(id, riskApproveLogDTO, request);
        } catch (BizException e) {
            log.error("update riskApproveLog failed, riskApproveLogDTO: {}, error message:{}, error all:{}", riskApproveLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "RiskApproveLog_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户风控审核日志", notes = "删除用户风控审核日志")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete riskApproveLog, id:{}", id);
        try {
            riskApproveLogService.logicDeleteRiskApproveLog(id, request);
        } catch (BizException e) {
            log.error("delete failed, riskApproveLog id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "获取用户审核信息", notes = "获取用户审核信息")
    @GetMapping("/getUserInfo/{userId}")
    public Object getUserInfo(@PathVariable("userId") Long userId, HttpServletRequest request) {
        JSONObject userInfo = null;
        try {
            userInfo = riskApproveLogService.getUserInfo(userId, request);
        } catch (Exception e) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(userInfo);
    }

    @ApiOperation(value = "审核", notes = "审核")
    @PostMapping("/check")
    public Object check(@RequestBody JSONObject checkData, HttpServletRequest request) {
        try {
            riskApproveLogService.check(checkData, request);
        } catch (Exception e) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "审核列表", notes = "审核列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/approveList")
    public Object approveList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = riskApproveLogService.approveListCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<InvestApproveDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = riskApproveLogService.approveList(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "审核记录", notes = "审核记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/approveLogList")
    public Object approveLogList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = riskApproveLogService.approveLogCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<InvestApproveDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = riskApproveLogService.approveLogList(params, scs, pc);
        }
        return R.success(list, pc);
    }

}
