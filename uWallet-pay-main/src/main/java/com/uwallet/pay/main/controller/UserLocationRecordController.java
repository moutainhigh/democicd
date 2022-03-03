package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.UserLocationRecordDTO;
import com.uwallet.pay.main.model.entity.UserLocationRecord;
import com.uwallet.pay.main.service.UserLocationRecordService;
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
 * 用户地理位置信息记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 用户地理位置信息记录表
 * @author: xucl
 * @date: Created in 2021-05-15 10:22:46
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@RestController
@RequestMapping("/userLocationRecord")
@Slf4j
@Api("用户地理位置信息记录表")
public class UserLocationRecordController extends BaseController<UserLocationRecord> {

    @Autowired
    private UserLocationRecordService userLocationRecordService;

    @ActionFlag(detail = "UserLocationRecord_list")
    @ApiOperation(value = "分页查询用户地理位置信息记录表", notes = "分页查询用户地理位置信息记录表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-用户地理位置信息记录表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = userLocationRecordService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserLocationRecordDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = userLocationRecordService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询用户地理位置信息记录表", notes = "通过id查询用户地理位置信息记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get userLocationRecord Id:{}", id);
        return R.success(userLocationRecordService.findUserLocationRecordById(id));
    }

    @ApiOperation(value = "通过查询条件查询用户地理位置信息记录表一条数据", notes = "通过查询条件查询用户地理位置信息记录表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询用户地理位置信息记录表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get userLocationRecord findOne params:{}", params);
        int total = userLocationRecordService.count(params);
        if (total > 1) {
            log.error("get userLocationRecord findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        UserLocationRecordDTO userLocationRecordDTO = null;
        if (total == 1) {
            userLocationRecordDTO = userLocationRecordService.findOneUserLocationRecord(params);
        }
        return R.success(userLocationRecordDTO);
    }

    @ActionFlag(detail = "UserLocationRecord_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增用户地理位置信息记录表", notes = "新增用户地理位置信息记录表")
    public Object create(@RequestBody UserLocationRecordDTO userLocationRecordDTO, HttpServletRequest request) {
        log.info("add userLocationRecord DTO:{}", userLocationRecordDTO);
        try {
            userLocationRecordService.saveUserLocationRecord(userLocationRecordDTO, request);
        } catch (BizException e) {
            log.error("add userLocationRecord failed, userLocationRecordDTO: {}, error message:{}, error all:{}", userLocationRecordDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "UserLocationRecord_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改用户地理位置信息记录表", notes = "修改用户地理位置信息记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody UserLocationRecordDTO userLocationRecordDTO, HttpServletRequest request) {
        log.info("put modify id:{}, userLocationRecord DTO:{}", id, userLocationRecordDTO);
        try {
            userLocationRecordService.updateUserLocationRecord(id, userLocationRecordDTO, request);
        } catch (BizException e) {
            log.error("update userLocationRecord failed, userLocationRecordDTO: {}, error message:{}, error all:{}", userLocationRecordDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "UserLocationRecord_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户地理位置信息记录表", notes = "删除用户地理位置信息记录表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete userLocationRecord, id:{}", id);
        try {
            userLocationRecordService.logicDeleteUserLocationRecord(id, request);
        } catch (BizException e) {
            log.error("delete failed, userLocationRecord id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
