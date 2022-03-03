package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.NoticeMassDTO;
import com.uwallet.pay.main.model.entity.NoticeMass;
import com.uwallet.pay.main.service.NoticeMassService;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.main.service.ServerService;
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
 * 群发消息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 群发消息表
 * @author: baixinyue
 * @date: Created in 2020-02-21 08:50:22
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/noticeMass")
@Slf4j
@Api("群发消息表")
public class NoticeMassController extends BaseController<NoticeMass> {

    @Autowired
    private NoticeMassService noticeMassService;

    @Autowired
    private ServerService serverService;

    @ActionFlag(detail = "NoticeMass_list")
    @ApiOperation(value = "分页查询群发消息表", notes = "分页查询群发消息表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-群发消息表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = noticeMassService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<NoticeMassDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = noticeMassService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询群发消息表", notes = "通过id查询群发消息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get noticeMass Id:{}", id);
        return R.success(noticeMassService.findNoticeMassById(id));
    }

    @ApiOperation(value = "通过查询条件查询群发消息表一条数据", notes = "通过查询条件查询群发消息表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询群发消息表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get noticeMass findOne params:{}", params);
        int total = noticeMassService.count(params);
        if (total > 1) {
            log.error("get noticeMass findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        NoticeMassDTO noticeMassDTO = null;
        if (total == 1) {
            noticeMassDTO = noticeMassService.findOneNoticeMass(params);
        }
        return R.success(noticeMassDTO);
    }

//    @ActionFlag(detail = "NoticeMass_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增群发消息表", notes = "新增群发消息表")
    public Object create(@RequestBody NoticeMassDTO noticeMassDTO, HttpServletRequest request) {
        log.info("add noticeMass DTO:{}", noticeMassDTO);
        try {
            noticeMassService.saveNoticeMass(noticeMassDTO, request);
        } catch (BizException e) {
            log.error("add noticeMass failed, noticeMassDTO: {}, error message:{}, error all:{}", noticeMassDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

//    @ActionFlag(detail = "NoticeMass_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改群发消息表", notes = "修改群发消息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody NoticeMassDTO noticeMassDTO, HttpServletRequest request) {
        log.info("put modify id:{}, noticeMass DTO:{}", id, noticeMassDTO);
        try {
            noticeMassService.updateNoticeMass(id, noticeMassDTO, request);
        } catch (BizException e) {
            log.error("update noticeMass failed, noticeMassDTO: {}, error message:{}, error all:{}", noticeMassDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

//    @ActionFlag(detail = "NoticeMass_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除群发消息表", notes = "删除群发消息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete noticeMass, id:{}", id);
        try {
            noticeMassService.logicDeleteNoticeMass(id, request);
        } catch (BizException e) {
            log.error("delete failed, noticeMass id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "NoticeMass_list")
    @GetMapping("/findInvestProducts")
    @ApiOperation(value = "查询理财产品列表", notes = "查询理财产品列表")
    public Object findInvestProducts () {
        JSONObject data = null;
        try {
            //data = serverService.findInvestProducts();
        } catch (Exception e) {
            log.error("delete failed, , error message:{}, error all:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(data);
    }

}
