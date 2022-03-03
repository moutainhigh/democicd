package com.uwallet.pay.main.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.RefundDetailListDTO;
import com.uwallet.pay.main.model.dto.RefundFlowDTO;
import com.uwallet.pay.main.model.dto.RefundListDTO;
import com.uwallet.pay.main.model.entity.RefundFlow;
import com.uwallet.pay.main.service.RefundFlowService;
import com.uwallet.pay.main.util.I18nUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;


/**
 * <p>
 * 退款流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.main.controller
 * @description: 退款流水表
 * @author: baixinyue
 * @date: Created in 2020-02-07 15:56:50
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/refundFlow")
@Slf4j
@Api("退款流水表")
public class RefundFlowController extends BaseController<RefundFlow> {

    @Autowired
    private RefundFlowService refundFlowService;

    @ActionFlag(detail = "RefundFlow_list")
    @ApiOperation(value = "分页查询退款流水表", notes = "分页查询退款流水表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-退款流水表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = refundFlowService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<RefundFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = refundFlowService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询退款流水表", notes = "通过id查询退款流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get refundFlow Id:{}", id);
        return R.success(refundFlowService.findRefundFlowById(id));
    }

    @ApiOperation(value = "通过查询条件查询退款流水表一条数据", notes = "通过查询条件查询退款流水表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询退款流水表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get refundFlow findOne params:{}", params);
        int total = refundFlowService.count(params);
        if (total > 1) {
            log.error("get refundFlow findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        RefundFlowDTO refundFlowDTO = null;
        if (total == 1) {
            refundFlowDTO = refundFlowService.findOneRefundFlow(params);
        }
        return R.success(refundFlowDTO);
    }

    @ActionFlag(detail = "RefundFlow_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增退款流水表", notes = "新增退款流水表")
    public Object create(@RequestBody RefundFlowDTO refundFlowDTO, HttpServletRequest request) {
        log.info("add refundFlow DTO:{}", refundFlowDTO);
        try {
            refundFlowService.saveRefundFlow(refundFlowDTO, request);
        } catch (BizException e) {
            log.error("add refundFlow failed, refundFlowDTO: {}, error message:{}, error all:{}", refundFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "RefundFlow_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改退款流水表", notes = "修改退款流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody RefundFlowDTO refundFlowDTO, HttpServletRequest request) {
        log.info("put modify id:{}, refundFlow DTO:{}", id, refundFlowDTO);
        try {
            refundFlowService.updateRefundFlow(id, refundFlowDTO, request);
        } catch (BizException e) {
            log.error("update refundFlow failed, refundFlowDTO: {}, error message:{}, error all:{}", refundFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "RefundFlow_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除退款流水表", notes = "删除退款流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete refundFlow, id:{}", id);
        try {
            refundFlowService.logicDeleteRefundFlow(id, request);
        } catch (BizException e) {
            log.error("delete failed, refundFlow id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "RefundFlow_list")
    @ApiOperation(value = "分页查询退款流水表", notes = "分页查询退款流水表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query"),
            @ApiImplicitParam(name = "start", value = "开始日期", paramType = "query", dataType = "long", required = false),
            @ApiImplicitParam(name = "end", value = "结束日期", paramType = "query", dataType = "long", required = false)
    })
    @GetMapping("/refundList")
    public Object refundList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = refundFlowService.selectRefundCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<RefundListDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = refundFlowService.selectRefund(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ActionFlag(detail = "RefundFlow_list")
    @ApiOperation(value = "分页查询退款详情表", notes = "分页查询退款详情表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query"),
            @ApiImplicitParam(name = "merchantId", value = "商户id", paramType = "query", dataType = "long", required = true),
            @ApiImplicitParam(name = "start", value = "开始日期", paramType = "query", dataType = "long", required = false),
            @ApiImplicitParam(name = "end", value = "结束日期", paramType = "query", dataType = "long", required = false)
    })
    @GetMapping("/refundDetailList")
    public Object refundDetailList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = refundFlowService.selectRefundDetailCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<RefundDetailListDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = refundFlowService.selectRefundDetail(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "下载补款CSV", notes = "下载补款CSV")
    @PostMapping("/downloadMakeUpFile")
    public Object downloadMakeUpFile(HttpServletRequest request, HttpServletResponse response) {
        String filePath = null;
        try {
            filePath = refundFlowService.exportMakeUpFile(request);
        } catch (Exception e) {
            log.info("create file faile, error message:{}, e:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        try {
            File file = new File(filePath);
            String fileName = file.getName();
            OutputStream out = response.getOutputStream();
            byte[] b = new byte[10240];
            response.reset();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/octet-stream");
            response.setHeader("content-disposition", "attachment; filename=" + fileName);
            response.addHeader("FileName", fileName);
            FileInputStream in = new FileInputStream(file);
            int n;
            //为了保证excel打开csv不出现中文乱码
            out.write(new   byte []{( byte ) 0xEF ,( byte ) 0xBB ,( byte ) 0xBF });
            while ((n = in.read(b)) != -1) {
                //每次写入out1024字节
                out.write(b, 0, n);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            log.info("export faile, error message:{}, e:{}", e.getMessage(), e);
        }

        return response;
    }

}
