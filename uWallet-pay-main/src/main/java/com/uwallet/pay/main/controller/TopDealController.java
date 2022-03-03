package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.TopDealDTO;
import com.uwallet.pay.main.model.entity.TopDeal;
import com.uwallet.pay.main.service.TopDealService;
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
 * top deal
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: top deal
 * @author: zhoutt
 * @date: Created in 2020-03-12 14:34:50
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/topDeal")
@Slf4j
@Api("top deal")
public class TopDealController extends BaseController<TopDeal> {

    @Autowired
    private TopDealService topDealService;

    @ActionFlag(detail = "TopDeal_list")
    @ApiOperation(value = "分页查询top deal", notes = "分页查询top deal以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-top deal列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = topDealService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<TopDealDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = topDealService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询top deal", notes = "通过id查询top deal")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get topDeal Id:{}", id);
        return R.success(topDealService.findTopDealById(id));
    }

    @ApiOperation(value = "通过查询条件查询top deal一条数据", notes = "通过查询条件查询top deal一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询top deal一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get topDeal findOne params:{}", params);
        int total = topDealService.count(params);
        if (total > 1) {
            log.error("get topDeal findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        TopDealDTO topDealDTO = null;
        if (total == 1) {
            topDealDTO = topDealService.findOneTopDeal(params);
        }
        return R.success(topDealDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增top deal", notes = "新增top deal")
    public Object create(@RequestBody TopDealDTO topDealDTO, HttpServletRequest request) {
        log.info("add topDeal DTO:{}", topDealDTO);
        try {
            topDealService.saveTopDeal(topDealDTO, request);
        } catch (BizException e) {
            log.error("add topDeal failed, topDealDTO: {}, error message:{}, error all:{}", topDealDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改top deal", notes = "修改top deal")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody TopDealDTO topDealDTO, HttpServletRequest request) {
        log.info("put modify id:{}, topDeal DTO:{}", id, topDealDTO);
        try {
            topDealService.updateTopDeal(id, topDealDTO, request);
        } catch (BizException e) {
            log.error("update topDeal failed, topDealDTO: {}, error message:{}, error all:{}", topDealDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除top deal", notes = "删除top deal")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete topDeal, id:{}", id);
        try {
            topDealService.logicDeleteTopDeal(id, request);
        } catch (BizException e) {
            log.error("delete failed, topDeal id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }


    @ApiOperation(value = "上移、下移操作", notes = "上移、下移操作")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "被操作id", paramType = "path"),
            @ApiImplicitParam(name = "upOrDown", value = "0：上移 1：下移", paramType = "path")
    })
    @PutMapping("/shift/{id}/{upOrDown}")
    public Object shiftUpOrDown(@PathVariable("id") Long id, @PathVariable("upOrDown") Integer upOrDown, HttpServletRequest request) {
        log.info("shift appBanner, id:{}", id);
        try {
            topDealService.shiftUpOrDown(id, upOrDown, request);
        } catch (BizException e) {
            log.error("shift failed, appBanner id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }
}
