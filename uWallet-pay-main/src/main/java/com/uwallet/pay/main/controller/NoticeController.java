package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.NoticeDTO;
import com.uwallet.pay.main.model.entity.Notice;
import com.uwallet.pay.main.service.NoticeService;
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
 * 消息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 消息表
 * @author: baixinyue
 * @date: Created in 2019-12-11 16:54:08
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/notice")
@Slf4j
@Api("消息表")
public class NoticeController extends BaseController<Notice> {

    @Autowired
    private NoticeService noticeService;

    @ApiOperation(value = "分页查询消息表", notes = "分页查询消息表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-消息表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = noticeService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<NoticeDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = noticeService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询消息表", notes = "通过id查询消息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get notice Id:{}", id);
        return R.success(noticeService.findNoticeById(id));
    }

    @ApiOperation(value = "通过查询条件查询消息表一条数据", notes = "通过查询条件查询消息表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询消息表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get notice findOne params:{}", params);
        int total = noticeService.count(params);
        if (total > 1) {
            log.error("get notice findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        NoticeDTO noticeDTO = null;
        if (total == 1) {
            noticeDTO = noticeService.findOneNotice(params);
        }
        return R.success(noticeDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增消息表", notes = "新增消息表")
    public Object create(@RequestBody NoticeDTO noticeDTO, HttpServletRequest request) {
        log.info("add notice DTO:{}", noticeDTO);
        try {
            noticeService.saveNotice(noticeDTO, request);
        } catch (BizException e) {
            log.error("add notice failed, noticeDTO: {}, error message:{}, error all:{}", noticeDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改消息表", notes = "修改消息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody NoticeDTO noticeDTO, HttpServletRequest request) {
        log.info("put modify id:{}, notice DTO:{}", id, noticeDTO);
        try {
            noticeService.updateNotice(id, noticeDTO, request);
        } catch (BizException e) {
            log.error("update notice failed, noticeDTO: {}, error message:{}, error all:{}", noticeDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除消息表", notes = "删除消息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete notice, id:{}", id);
        try {
            noticeService.logicDeleteNotice(id, request);
        } catch (BizException e) {
            log.error("delete failed, notice id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
