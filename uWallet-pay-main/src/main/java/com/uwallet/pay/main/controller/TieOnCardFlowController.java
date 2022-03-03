package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.TieOnCardFlowDTO;
import com.uwallet.pay.main.model.entity.TieOnCardFlow;
import com.uwallet.pay.main.service.TieOnCardFlowService;
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
 * 绑卡交易流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 绑卡交易流水表
 * @author: baixinyue
 * @date: Created in 2020-01-06 11:37:40
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/tieOnCardFlow")
@Slf4j
@Api("绑卡交易流水表")
public class TieOnCardFlowController extends BaseController<TieOnCardFlow> {

    @Autowired
    private TieOnCardFlowService tieOnCardFlowService;

    @ApiOperation(value = "分页查询绑卡交易流水表", notes = "分页查询绑卡交易流水表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-绑卡交易流水表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = tieOnCardFlowService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<TieOnCardFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = tieOnCardFlowService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询绑卡交易流水表", notes = "通过id查询绑卡交易流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get tieOnCardFlow Id:{}", id);
        return R.success(tieOnCardFlowService.findTieOnCardFlowById(id));
    }

    @ApiOperation(value = "通过查询条件查询绑卡交易流水表一条数据", notes = "通过查询条件查询绑卡交易流水表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询绑卡交易流水表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get tieOnCardFlow findOne params:{}", params);
        int total = tieOnCardFlowService.count(params);
        if (total > 1) {
            log.error("get tieOnCardFlow findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        TieOnCardFlowDTO tieOnCardFlowDTO = null;
        if (total == 1) {
            tieOnCardFlowDTO = tieOnCardFlowService.findOneTieOnCardFlow(params);
        }
        return R.success(tieOnCardFlowDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增绑卡交易流水表", notes = "新增绑卡交易流水表")
    public Object create(@RequestBody TieOnCardFlowDTO tieOnCardFlowDTO, HttpServletRequest request) {
        log.info("add tieOnCardFlow DTO:{}", tieOnCardFlowDTO);
        try {
            tieOnCardFlowService.saveTieOnCardFlow(tieOnCardFlowDTO, request);
        } catch (BizException e) {
            log.error("add tieOnCardFlow failed, tieOnCardFlowDTO: {}, error message:{}, error all:{}", tieOnCardFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改绑卡交易流水表", notes = "修改绑卡交易流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody TieOnCardFlowDTO tieOnCardFlowDTO, HttpServletRequest request) {
        log.info("put modify id:{}, tieOnCardFlow DTO:{}", id, tieOnCardFlowDTO);
        try {
            tieOnCardFlowService.updateTieOnCardFlow(id, tieOnCardFlowDTO, request);
        } catch (BizException e) {
            log.error("update tieOnCardFlow failed, tieOnCardFlowDTO: {}, error message:{}, error all:{}", tieOnCardFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除绑卡交易流水表", notes = "删除绑卡交易流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete tieOnCardFlow, id:{}", id);
        try {
            tieOnCardFlowService.logicDeleteTieOnCardFlow(id, request);
        } catch (BizException e) {
            log.error("delete failed, tieOnCardFlow id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
