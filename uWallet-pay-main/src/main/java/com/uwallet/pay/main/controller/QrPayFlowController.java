package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.QrPayFlow;
import com.uwallet.pay.main.service.ClearFlowDetailService;
import com.uwallet.pay.main.service.MerchantService;
import com.uwallet.pay.main.service.QrPayFlowService;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.POIUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * <p>
 * 扫码支付交易流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 扫码支付交易流水表
 * @author: zhoutt
 * @date: Created in 2019-12-13 18:00:26
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/qrPayFlow")
@Slf4j
@Api("扫码支付交易流水表")
public class QrPayFlowController extends BaseController<QrPayFlow> {

    @Autowired
    private QrPayFlowService qrPayFlowService;
    @Autowired
    private ClearFlowDetailService clearFlowDetailService;
    @Autowired
    private MerchantService merchantService;


    @ApiOperation(value = "分页查询扫码支付交易流水表", notes = "分页查询扫码支付交易流水表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-扫码支付交易流水表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = qrPayFlowService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<QrPayFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrPayFlowService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ActionFlag(detail = "merchantClear_list")
    @ApiOperation(value = "商户资金清算", notes = "商户资金清算")
    @GetMapping(value = "/qrPayFlowList", name="商户资金清算")
    public Object qrPayFlowList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = qrPayFlowService.countQrPayFlowList(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<QrPayFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrPayFlowService.qrPayFlowList(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "商户资金清算详情", notes = "商户资金清算详情")
    @GetMapping(value = "/qrPayFlowListDetails", name="商户资金清算详情")
    public Object qrPayFlowListDetails(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        List<QrPayFlowDTO> qrPayFlowDTOS = qrPayFlowService.qrPayFlowListDetails(params);
        return R.success(qrPayFlowDTOS);
    }

    @ApiOperation(value = "通过id查询扫码支付交易流水表", notes = "通过id查询扫码支付交易流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get qrPayFlow Id:{}", id);
        return R.success(qrPayFlowService.findQrPayFlowById(id));
    }

    @ApiOperation(value = "通过查询条件查询扫码支付交易流水表一条数据", notes = "通过查询条件查询扫码支付交易流水表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询扫码支付交易流水表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get qrPayFlow findOne params:{}", params);
        int total = qrPayFlowService.count(params);
        if (total > 1) {
            log.error("get qrPayFlow findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        QrPayFlowDTO qrPayFlowDTO = null;
        if (total == 1) {
            qrPayFlowDTO = qrPayFlowService.findOneQrPayFlow(params);
        }
        return R.success(qrPayFlowDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增扫码支付交易流水表", notes = "新增扫码支付交易流水表")
    public Object create(@RequestBody QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) {
        log.info("add qrPayFlow DTO:{}", qrPayFlowDTO);
        try {
            qrPayFlowService.saveQrPayFlow(qrPayFlowDTO, request);
        } catch (BizException e) {
            log.error("add qrPayFlow failed, qrPayFlowDTO: {}, error message:{}, error all:{}", qrPayFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改扫码支付交易流水表", notes = "修改扫码支付交易流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody QrPayFlowDTO qrPayFlowDTO, HttpServletRequest request) {
        log.info("put modify id:{}, qrPayFlow DTO:{}", id, qrPayFlowDTO);
        try {
            qrPayFlowService.updateQrPayFlow(id, qrPayFlowDTO, request);
        } catch (BizException e) {
            log.error("update qrPayFlow failed, qrPayFlowDTO: {}, error message:{}, error all:{}", qrPayFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除扫码支付交易流水表", notes = "删除扫码支付交易流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete qrPayFlow, id:{}", id);
        try {
            qrPayFlowService.logicDeleteQrPayFlow(id, request);
        } catch (BizException e) {
            log.error("delete failed, qrPayFlow id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "payOrder_list")
    @ApiOperation(value = "分页查询支付订单", notes = "分页查支付订单以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单编号", paramType = "query", dataType = "long", required = false),
            @ApiImplicitParam(name = "email", value = "付款方账号", paramType = "query", dataType = "string", required = false),
            @ApiImplicitParam(name = "corporateName", value = "商户名", paramType = "query", dataType = "string", required = false),
            @ApiImplicitParam(name = "gatewayId", value = "渠道", paramType = "query", dataType = "long", required = false),
            @ApiImplicitParam(name = "state", value = "订单状态", paramType = "query", dataType = "int", required = false),
            @ApiImplicitParam(name = "clearState", value = "订单清算状态", paramType = "query", dataType = "int", required = false),
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/payBorrowList")
    public Object PayBorrowList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = qrPayFlowService.selectPayBorrowCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<PayBorrowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrPayFlowService.selectPayBorrow(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ActionFlag(detail = "transferOrder_list")
    @ApiOperation(value = "分页查询转账订单", notes = "分页查询转账订单以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单编号", paramType = "query", dataType = "long", required = false),
            @ApiImplicitParam(name = "payNo", value = "付款方账号", paramType = "query", dataType = "string", required = false),
            @ApiImplicitParam(name = "recNo", value = "收款方账号", paramType = "query", dataType = "string", required = false),
            @ApiImplicitParam(name = "gatewayId", value = "渠道", paramType = "query", dataType = "long", required = false),
            @ApiImplicitParam(name = "state", value = "订单状态", paramType = "query", dataType = "int", required = false),
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/transferBorrowList")
    public Object TransferBorrowList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = qrPayFlowService.selectTransferBorrowCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<TransferBorrowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrPayFlowService.selectTransferBorrow(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "清算记录第三列表订单", notes = "清算记录第三列表订单以及排序功能")
    @ApiImplicitParam(name = "id", value = "订单编号", paramType = "query", dataType = "long", required = false)
    @GetMapping("/batchBorrowList")
    public Object selectBatchBorrow( HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        params.put("clearBatchId",params.get("batchId"));
        params.put("recUserId",params.get("userId"));
        int total = clearFlowDetailService.selectBatchBorrowCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<PayBorrowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            params.put("recUserId", params.get("recUserId"));
            scs = getSortingContext(request);
            list = clearFlowDetailService.selectBatchBorrow(params, scs, pc);
        }
        return R.success(list, pc);
    }


    @ActionFlag(detail = "merchantClear_list")
    @ApiOperation(value = "商户资金清算列表查询", notes = "商户资金清算列表查询")
    @GetMapping(value = "/merchantClearList", name="商户资金清算列表查询")
    public Object merchantClearList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = qrPayFlowService.countMerchantClearList(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<QrPayFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrPayFlowService.merchantClearList(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "查询订单报告", notes = "查询订单报告")
    @GetMapping(value = "/orderReport", name="查询订单报告")
    public Object orderReport(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = qrPayFlowService.countDistinctMerchant(params);
        PagingContext pc;
        JSONObject result = new JSONObject();
        pc = getPagingContext(request, total);
        if (total > 0) {
            result = qrPayFlowService.getOrderReportTotal(params, pc);
        }
        return R.success(result, pc);
    }

    @PassToken
    @ApiOperation(value = "导出订单报告", notes = "导出订单报告")
    @GetMapping(value = "/exportOrderReport", name="导出订单报告")
    public void exportOrderReport(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> params = getConditionsMap(request);
        List<OrderReportDTO> list = qrPayFlowService.getOrderReport(params, null);
        try {
            String fileName = this.createFileName("order_report");
            List<ExcelOrderDTO> repack = qrPayFlowService.repackExcelOrderDTO(list);
            Workbook workbook = POIUtils.createExcel(repack, ExcelOrderDTO.class, fileName);
            this.exportFile(workbook, fileName, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ApiOperation(value = "卡支付列表", notes = "卡支付列表")
    @GetMapping(value = "/payFlowByCard", name="卡支付列表")
    public Object payFlowByCard(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = qrPayFlowService.countPayFlowByCard(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<QrPayFlowCardPayDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrPayFlowService.getPayFlowByCard(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @PassToken
    @ActionFlag(detail = "cardPaymentOrder_list")
    @ApiOperation(value = "导出卡支付列表", notes = "导出卡支付列表")
    @GetMapping(value = "/exportPayFlowByCard", name="导出卡支付列表")
    public void exportPayFlowByCard(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> params = getConditionsMap(request);
        Vector<SortingContext> scs = getSortingContext(request);
        List<QrPayFlowCardPayDTO> list = qrPayFlowService.getPayFlowByCard(params, scs, null);
        List<ExcelCardPayDTO> repack = qrPayFlowService.repackExportPayFlowByCard(list,request);
        try {
            String fileName = this.createFileName("pay_flow_by_card");
            Workbook workbook = POIUtils.createExcel(repack, ExcelCardPayDTO.class, fileName);
            this.exportFile(workbook, fileName, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createFileName(String fileNameSubFix) {
        String todayStr = new SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(System.currentTimeMillis());
        return fileNameSubFix+"-"+todayStr+".xlsx";
    }

    @ApiOperation(value = "分期付支付列表", notes = "分期付支付列表")
    @GetMapping(value = "/payFlowByInstalment", name="分期付支付列表")
    public Object payFlowByInstalment(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = qrPayFlowService.countPayFlowByInstalment(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<QrPayFlowInstalmentPayDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrPayFlowService.getPayFlowByInstalment(params, scs, pc, 0);
        }
        return R.success(list, pc);
    }
    @ApiOperation(value = "api分期付支付列表", notes = "分期付支付列表")
    @GetMapping(value = "/apiPayFlowByInstalment", name="分期付支付列表")
    public Object apiPayFlowByInstalment(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = qrPayFlowService.countPayFlowByInstalmenth5(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<QrPayFlowInstalmentPayDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrPayFlowService.getPayFlowByInstalmenth5(params, scs, pc, 0);
        }
        return R.success(list, pc);
    }

    @PassToken
    @ActionFlag(detail = "installmentOrder_list")
    @ApiOperation(value = "导出分期付支付列表", notes = "导出分期付支付列表")
    @GetMapping(value = "/exportPayFlowByInstalment", name="导出分期付支付列表")
    public void exportPayFlowByInstalment(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> params = getConditionsMap(request);
        Vector<SortingContext> scs = getSortingContext(request);
        List<QrPayFlowInstalmentPayDTO> list = qrPayFlowService.getPayFlowByInstalment(params, scs, null, 1);
        try {
            String fileName = this.createFileName("pay_flow_by_instalment");
            List<ExcelInstPayDTO> repack = qrPayFlowService.repackExportInstPayFlow(list,request);
            Workbook workbook = POIUtils.createExcel(repack, ExcelInstPayDTO.class, fileName);
            this.exportFile(workbook, fileName, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportFile(Workbook workbook, String fileName, HttpServletResponse response) throws Exception {
        response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
        response.setContentType("application/force-download");
        response.setHeader("FileName", fileName);
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        // response.setHeader("Access-Control-Expose-Headers", "FileName");
        OutputStream os = response.getOutputStream();
        workbook.write(os);
    }


//    @ActionFlag(detail = "merchantClear_list")
//    @ApiOperation(value = "商户资金清算列表查询", notes = "商户资金清算列表查询")
//    @GetMapping(value = "/merchantClearMessageList", name="商户资金清算列表查询")
//    public Object merchantClearMessageList(HttpServletRequest request) {
//        Map<String, Object> params = getConditionsMap(request);
//        int total = merchantService.getMerchantClearMessageCount(params);
//        PagingContext pc;
//        Vector<SortingContext> scs;
//        List<Map<String ,Object>> list = new ArrayList<>();
//        pc = getPagingContext(request, total);
//        if (total > 0) {
//            scs = getSortingContext(request);
//            list = qrPayFlowService.merchantClearMessageList(params, scs, pc);
//        }
//        return R.success(list, pc);
//    }

    @ApiOperation(value = "商户资金清算列表查询", notes = "商户资金清算列表查询")
    @GetMapping(value = "/merchantClearMessageList", name="商户资金清算列表查询")
    public Object merchantClearMessageList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("merchantClearMessageList,params:"+params);
        int total = qrPayFlowService.getClearMerchantListCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<Map<String ,Object>> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrPayFlowService.merchantClearMessageList(params, scs, pc,request);
        }
        return R.success(list, pc);
    }


//    @ActionFlag(detail = "merchantClear_list")
    @ApiOperation(value = "商户资金清算未清算和延迟清算明细查询", notes = "商户资金清算未清算和延迟清算明细查询")
    @GetMapping(value = "/merchantClearDetailList", name="商户资金清算未清算和延迟清算明细查询")
    public Object merchantUnclearDetailList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        List<String> merchantIdList = new ArrayList<>();
        merchantIdList.add(String.valueOf(params.get("merchantId")));
        params.put("merchantIdList",merchantIdList);
        int total = qrPayFlowService.merchantUnclearDetailCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<QrPayFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrPayFlowService.merchantUnclearDetailList(params, scs, pc);
        }
        return R.success(list, pc);
    }


    //    @ActionFlag(detail = "merchantClear_list")
    @ApiOperation(value = "api商户资金清算未清算和延迟清算明细查询", notes = "api商户资金清算未清算和延迟清算明细查询")
    @GetMapping(value = "/apiMerchantClearDetailList", name="api商户资金清算未清算和延迟清算明细查询")
    public Object apiMerchantClearDetailList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        List<String> merchantIdList = new ArrayList<>();
        merchantIdList.add(String.valueOf(params.get("merchantId")));
        params.put("merchantIdList",merchantIdList);
        int total = qrPayFlowService.apiMerchantUnclearDetailCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<QrPayFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrPayFlowService.apiMerchantUnclearDetailList(params, scs, pc);
        }
        return R.success(list, pc);
    }


    @ApiOperation(value = "api商户资金清算列表查询", notes = "api商户资金清算列表查询")
    @GetMapping(value = "/apiMerchantClearList", name="api商户资金清算列表查询")
    public Object apiMerchantClearList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("apiMerchantClearList,params:"+params);
        int total = qrPayFlowService.getApiMerchantClearListCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<Map<String ,Object>> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrPayFlowService.getApiMerchantClearList(params, scs, pc,request);
        }
        return R.success(list, pc);
    }
}
