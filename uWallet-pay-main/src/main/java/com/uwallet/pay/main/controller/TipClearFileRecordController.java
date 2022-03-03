package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.TipClearFileRecordDTO;
import com.uwallet.pay.main.model.entity.TipClearFileRecord;
import com.uwallet.pay.main.service.TipClearFileRecordService;
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
 * 小费清算文件记录
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 小费清算文件记录
 * @author: zhangzeyuan
 * @date: Created in 2021-08-11 17:21:14
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@RestController
@RequestMapping("/tipClearFileRecord")
@Slf4j
@Api("小费清算文件记录")
public class TipClearFileRecordController extends BaseController<TipClearFileRecord> {

    @Autowired
    private TipClearFileRecordService tipClearFileRecordService;

    @ActionFlag(detail = "TipClearFileRecord_list")
    @ApiOperation(value = "分页查询小费清算文件记录", notes = "分页查询小费清算文件记录以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-小费清算文件记录列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = tipClearFileRecordService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<TipClearFileRecordDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = tipClearFileRecordService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询小费清算文件记录", notes = "通过id查询小费清算文件记录")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get tipClearFileRecord Id:{}", id);
        return R.success(tipClearFileRecordService.findTipClearFileRecordById(id));
    }

    @ApiOperation(value = "通过查询条件查询小费清算文件记录一条数据", notes = "通过查询条件查询小费清算文件记录一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询小费清算文件记录一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get tipClearFileRecord findOne params:{}", params);
        int total = tipClearFileRecordService.count(params);
        if (total > 1) {
            log.error("get tipClearFileRecord findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        TipClearFileRecordDTO tipClearFileRecordDTO = null;
        if (total == 1) {
            tipClearFileRecordDTO = tipClearFileRecordService.findOneTipClearFileRecord(params);
        }
        return R.success(tipClearFileRecordDTO);
    }

    @ActionFlag(detail = "TipClearFileRecord_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增小费清算文件记录", notes = "新增小费清算文件记录")
    public Object create(@RequestBody TipClearFileRecordDTO tipClearFileRecordDTO, HttpServletRequest request) {
        log.info("add tipClearFileRecord DTO:{}", tipClearFileRecordDTO);
        try {
            tipClearFileRecordService.saveTipClearFileRecord(tipClearFileRecordDTO, request);
        } catch (BizException e) {
            log.error("add tipClearFileRecord failed, tipClearFileRecordDTO: {}, error message:{}, error all:{}", tipClearFileRecordDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "TipClearFileRecord_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改小费清算文件记录", notes = "修改小费清算文件记录")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody TipClearFileRecordDTO tipClearFileRecordDTO, HttpServletRequest request) {
        log.info("put modify id:{}, tipClearFileRecord DTO:{}", id, tipClearFileRecordDTO);
        try {
            tipClearFileRecordService.updateTipClearFileRecord(id, tipClearFileRecordDTO, request);
        } catch (BizException e) {
            log.error("update tipClearFileRecord failed, tipClearFileRecordDTO: {}, error message:{}, error all:{}", tipClearFileRecordDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "TipClearFileRecord_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除小费清算文件记录", notes = "删除小费清算文件记录")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete tipClearFileRecord, id:{}", id);
        try {
            tipClearFileRecordService.logicDeleteTipClearFileRecord(id, request);
        } catch (BizException e) {
            log.error("delete failed, tipClearFileRecord id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
