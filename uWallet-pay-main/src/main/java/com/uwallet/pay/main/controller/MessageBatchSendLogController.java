package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.MessageBatchSendLogDTO;
import com.uwallet.pay.main.model.entity.MessageBatchSendLog;
import com.uwallet.pay.main.service.MessageBatchSendLogService;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.main.util.I18nUtils;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
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
 * 批量发送消息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 批量发送消息表
 * @author: xucl
 * @date: Created in 2021-05-11 14:18:13
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@RestController
@RequestMapping("/messageBatchSendLog")
@Slf4j
@Api("批量发送消息表")
public class MessageBatchSendLogController extends BaseController<MessageBatchSendLog> {

    @Autowired
    private MessageBatchSendLogService messageBatchSendLogService;

    @ActionFlag(detail = "MessageBatchSendLog_list")
    @ApiOperation(value = "分页查询批量发送消息表", notes = "分页查询批量发送消息表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-批量发送消息表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = messageBatchSendLogService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MessageBatchSendLogDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = messageBatchSendLogService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询批量发送消息表", notes = "通过id查询批量发送消息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get messageBatchSendLog Id:{}", id);
        return R.success(messageBatchSendLogService.findMessageBatchSendLogById(id));
    }

    @ApiOperation(value = "通过查询条件查询批量发送消息表一条数据", notes = "通过查询条件查询批量发送消息表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询批量发送消息表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get messageBatchSendLog findOne params:{}", params);
        int total = messageBatchSendLogService.count(params);
        if (total > 1) {
            log.error("get messageBatchSendLog findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        MessageBatchSendLogDTO messageBatchSendLogDTO = null;
        if (total == 1) {
            messageBatchSendLogDTO = messageBatchSendLogService.findOneMessageBatchSendLog(params);
        }
        return R.success(messageBatchSendLogDTO);
    }

    @ActionFlag(detail = "MessageBatchSendLog_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增批量发送消息表", notes = "新增批量发送消息表")
    public Object create(@RequestBody MessageBatchSendLogDTO messageBatchSendLogDTO, HttpServletRequest request) {
        log.info("add messageBatchSendLog DTO:{}", messageBatchSendLogDTO);
        try {
            messageBatchSendLogService.saveMessageBatchSendLog(messageBatchSendLogDTO, request);
        } catch (BizException e) {
            log.error("add messageBatchSendLog failed, messageBatchSendLogDTO: {}, error message:{}, error all:{}", messageBatchSendLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "MessageBatchSendLog_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改批量发送消息表", notes = "修改批量发送消息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody MessageBatchSendLogDTO messageBatchSendLogDTO, HttpServletRequest request) {
        log.info("put modify id:{}, messageBatchSendLog DTO:{}", id, messageBatchSendLogDTO);
        try {
            messageBatchSendLogService.updateMessageBatchSendLog(id, messageBatchSendLogDTO, request);
        } catch (BizException e) {
            log.error("update messageBatchSendLog failed, messageBatchSendLogDTO: {}, error message:{}, error all:{}", messageBatchSendLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "MessageBatchSendLog_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除批量发送消息表", notes = "删除批量发送消息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete messageBatchSendLog, id:{}", id);
        try {
            messageBatchSendLogService.logicDeleteMessageBatchSendLog(id, request);
        } catch (BizException e) {
            log.error("delete failed, messageBatchSendLog id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }
    @ActionFlag(detail = "MessageBatchSendLog_list")
    @ApiOperation(value = "分页查询批量发送消息表New   ", notes = "分页查询批量发送消息表以及排序功能New")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-批量发送消息表列表New",value = "/getList")
    public Object listNew(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = messageBatchSendLogService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MessageBatchSendLogDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = messageBatchSendLogService.findNew(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ActionFlag(detail = "MessageBatchSendLog_list")
    @PutMapping("/updateNew")
    @ApiOperation(value = "修改批量发送消息", notes = "修改批量发送消息")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object updateNew( @RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("修改批量发送消息 id:{}, param:{}",  jsonObject);
        try {
            messageBatchSendLogService.updateMessageBatchSendLogNew(jsonObject, request);
        } catch (BizException | ParseException e) {
            log.error("修改批量发送消息异常, param: {}, error message:{}, error all:{}", jsonObject, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }
    @ActionFlag(detail = "MessageBatchSendLog_list")
    @PutMapping("/updateState/{id}")
    @ApiOperation(value = "切换定时消息状态", notes = "切换定时消息状态")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object updateState( @PathVariable Long id,@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("修改批量发送消息 id:{}, param:{},id:{}",  jsonObject,id);
        try {
            messageBatchSendLogService.updateState(id,jsonObject, request);
        } catch (BizException e) {
            log.error("切换定时消息状态, param: {}, error message:{}, error all:{}", jsonObject, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }


    @PostMapping(name = "获取批量发送消息条数" ,value = "/getUserCount")
    @ApiOperation(value = "获取批量发送消息条数", notes = "获取批量发送消息条数")
    public Object getUserCount(@RequestBody MessageBatchSendLogDTO messageBatchSendLogDTO, HttpServletRequest request) {
        log.info("getUserCount DTO:{}", messageBatchSendLogDTO);
        int result = 0;
        try {
            result = messageBatchSendLogService.getUserCount(messageBatchSendLogDTO, request);
        } catch (Exception e) {
            log.error("getUserCount, messageBatchSendLogDTO: {}, error message:{}, error all:{}", messageBatchSendLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(result);
    }


}
