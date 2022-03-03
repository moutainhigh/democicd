package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.WithholdFlowDTO;
import com.uwallet.pay.main.model.entity.WithholdFlow;
import com.uwallet.pay.main.service.WithholdFlowService;
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
 * 代收三方流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 代收三方流水表
 * @author: baixinyue
 * @date: Created in 2019-12-16 10:50:03
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/withholdFlow")
@Slf4j
@Api("代收三方流水表")
public class WithholdFlowController extends BaseController<WithholdFlow> {

    @Autowired
    private WithholdFlowService withholdFlowService;

    @ActionFlag(detail = "WithholdFlow_list")
    @ApiOperation(value = "分页查询代收三方流水表", notes = "分页查询代收三方流水表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-代收三方流水表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = withholdFlowService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<WithholdFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = withholdFlowService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ActionFlag(detail = "transactionManagement_list")
    @ApiOperation(value = "交易查询", notes = "交易查询")
    @GetMapping(value = "/withholdFlowList", name="交易查询")
    public Object withholdFlowList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        if (params.get(Constant.FLOW_ID) != null) {
            params.put(Constant.FLOW_ID,params.get(Constant.FLOW_ID).toString().trim());
        }
        if (params.get(Constant.ORDRE_NO) != null) {
            params.put(Constant.ORDRE_NO,params.get(Constant.ORDRE_NO).toString().trim());
        }
        int total = withholdFlowService.countWithholdFlowList(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<WithholdFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = withholdFlowService.withholdFlowList(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询代收三方流水表", notes = "通过id查询代收三方流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get withholdFlow Id:{}", id);
        return R.success(withholdFlowService.findWithholdFlowById(id));
    }

    @ApiOperation(value = "通过查询条件查询代收三方流水表一条数据", notes = "通过查询条件查询代收三方流水表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询代收三方流水表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get withholdFlow findOne params:{}", params);
        int total = withholdFlowService.count(params);
        if (total > 1) {
            log.error("get withholdFlow findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        WithholdFlowDTO withholdFlowDTO = null;
        if (total == 1) {
            withholdFlowDTO = withholdFlowService.findOneWithholdFlow(params);
        }
        return R.success(withholdFlowDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增代收三方流水表", notes = "新增代收三方流水表")
    public Object create(@RequestBody WithholdFlowDTO withholdFlowDTO, HttpServletRequest request) {
        log.info("add withholdFlow DTO:{}", withholdFlowDTO);
        try {
            withholdFlowService.saveWithholdFlow(withholdFlowDTO, request);
        } catch (BizException e) {
            log.error("add withholdFlow failed, withholdFlowDTO: {}, error message:{}, error all:{}", withholdFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改代收三方流水表", notes = "修改代收三方流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody WithholdFlowDTO withholdFlowDTO, HttpServletRequest request) {
        log.info("put modify id:{}, withholdFlow DTO:{}", id, withholdFlowDTO);
        try {
            withholdFlowService.updateWithholdFlow(id, withholdFlowDTO, request);
        } catch (BizException e) {
            log.error("update withholdFlow failed, withholdFlowDTO: {}, error message:{}, error all:{}", withholdFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除代收三方流水表", notes = "删除代收三方流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete withholdFlow, id:{}", id);
        try {
            withholdFlowService.logicDeleteWithholdFlow(id, request);
        } catch (BizException e) {
            log.error("delete failed, withholdFlow id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
