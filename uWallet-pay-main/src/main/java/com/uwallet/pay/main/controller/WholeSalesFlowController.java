package com.uwallet.pay.main.controller;

import autovalue.shaded.com.google$.auto.service.$AutoService;
import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.util.EasyPoiUtils;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.WholeSalesFlow;
import com.uwallet.pay.main.service.QrPayFlowService;
import com.uwallet.pay.main.service.WholeSalesFlowService;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.main.util.I18nUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import com.uwallet.pay.main.util.POIUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;


/**
 * <p>
 * 整体销售流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 整体销售流水表z
 * @author: zhoutt
 * @date: Created in 2020-10-17 14:34:00
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/wholeSalesFlow")
@Slf4j
@Api("整体销售流水表")
public class WholeSalesFlowController extends BaseController<WholeSalesFlow> {

    @Autowired
    private WholeSalesFlowService wholeSalesFlowService;

    @Autowired
    private QrPayFlowService qrPayFlowService;

    @Value("${export.excel.marketingActivities}")
    private String marketingActivities;

    @ActionFlag(detail = "WholeSalesFlow_list")
    @ApiOperation(value = "分页查询整体销售流水表", notes = "分页查询整体销售流水表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-整体销售流水表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = wholeSalesFlowService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<WholeSalesFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = wholeSalesFlowService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询整体销售流水表", notes = "通过id查询整体销售流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("get wholeSalesFlow Id:{}", id);
        WholeSalesFlowDTO wholeSalesFlowDTO = null;
        try {
            wholeSalesFlowDTO = wholeSalesFlowService.findWholeSalesFlowById(id, request);
        } catch (Exception e) {
            log.info("get wholeSaleFlow Id failed, id:{}, error message:{}, e:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(wholeSalesFlowDTO);
    }

    @ApiOperation(value = "通过查询条件查询整体销售流水表一条数据", notes = "通过查询条件查询整体销售流水表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询整体销售流水表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get wholeSalesFlow findOne params:{}", params);
        int total = wholeSalesFlowService.count(params);
        if (total > 1) {
            log.error("get wholeSalesFlow findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        WholeSalesFlowDTO wholeSalesFlowDTO = null;
        if (total == 1) {
            wholeSalesFlowDTO = wholeSalesFlowService.findOneWholeSalesFlow(params);
        }
        return R.success(wholeSalesFlowDTO);
    }

    @ActionFlag(detail = "WholeSalesFlow_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增整体销售流水表", notes = "新增整体销售流水表")
    public Object create(@RequestBody WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) {
        log.info("add wholeSalesFlow DTO:{}", wholeSalesFlowDTO);
        try {
            wholeSalesFlowService.saveWholeSalesFlow(wholeSalesFlowDTO, request);
        } catch (BizException e) {
            log.error("add wholeSalesFlow failed, wholeSalesFlowDTO: {}, error message:{}, error all:{}", wholeSalesFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "WholeSalesFlow_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改整体销售流水表", notes = "修改整体销售流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) {
        log.info("put modify id:{}, wholeSalesFlow DTO:{}", id, wholeSalesFlowDTO);
        try {
            wholeSalesFlowService.updateWholeSalesFlow(id, wholeSalesFlowDTO, request);
        } catch (BizException e) {
            log.error("update wholeSalesFlow failed, wholeSalesFlowDTO: {}, error message:{}, error all:{}", wholeSalesFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "WholeSalesFlow_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除整体销售流水表", notes = "删除整体销售流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete wholeSalesFlow, id:{}", id);
        try {
            wholeSalesFlowService.logicDeleteWholeSalesFlow(id, request);
        } catch (BizException e) {
            log.error("delete failed, wholeSalesFlow id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "分页查询整体销售意向流水表", notes = "分页查询整体销售流水表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/wholeSaleInterestOrderList")
    public Object wholeSaleInterestOrderList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = wholeSalesFlowService.wholeSalesInterestOrderCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<WholeSalesFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = wholeSalesFlowService.wholeSaleInterestOrderList(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "分页查询整体销售流水表", notes = "分页查询整体销售流水表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/wholeSaleOrderList")
    public Object wholeSaleOrderList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = wholeSalesFlowService.wholeSaleOrderCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<WholeSalesFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = wholeSalesFlowService.wholeSaleOrderList(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "意向订单审核", notes = "意向订单审核")
    @PostMapping("/wholeSaleOrderInterestAudit")
    public Object wholeSaleOrderInterestAudit(@RequestBody WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) {
        log.info("whole sale order interst audit, data: {}", wholeSalesFlowDTO);
        try {
            wholeSalesFlowService.interestOrderAudit(wholeSalesFlowDTO, request);
        } catch (Exception e) {
            log.info("whole sale order interest audit failed, data: {}, error message: {}, e: {}", wholeSalesFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "意向订单审核", notes = "意向订单审核")
    @PostMapping("/rateModify")
    public Object rateModify(@RequestBody WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) {
        log.info("rate modify, data: {}", wholeSalesFlowDTO);
        try {
            wholeSalesFlowService.rateModify(wholeSalesFlowDTO, request);
        } catch (Exception e) {
            log.info("rate modify failed, data: {}, error message: {}, e: {}", wholeSalesFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "订单审核", notes = "订单审核")
    @PostMapping("/wholeSaleOrderAudit")
    public Object wholeSaleOrderAudit(@RequestBody WholeSalesFlowDTO wholeSalesFlowDTO, HttpServletRequest request) {
        log.info("order audit, data: {}", wholeSalesFlowDTO);
        try {
            wholeSalesFlowService.wholeSaleOrderAudit(wholeSalesFlowDTO, request);
        } catch (Exception e) {
            log.info("order audit failed, data: {}, error message: {}, e: {}", wholeSalesFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "订单审核", notes = "订单审核")
    @GetMapping("/merchantOrderDetails/{merchantId}")
    public Object merchantOrderDetails(@PathVariable("merchantId") Long merchantId, HttpServletRequest request) {
        JSONObject result = null;
        try {
            result = wholeSalesFlowService.merchantOrderDetails(merchantId, request);
        } catch (Exception e) {
            log.info("merchant order details failed, error message: {}, e: {}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @ApiOperation(value = "整体出售商户订单查询", notes = "分页查询整体销售流水表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/wholeMerchantOrderSearch")
    public Object wholeMerchantOrderSearch(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = qrPayFlowService.wholeMerchantOrderSearchCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<JSONObject> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrPayFlowService.wholeMerchantOrderSearch(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "意向订单拒绝原因", notes = "意向订单拒绝原因")
    @GetMapping("/findInterestRejectInfo/{id}")
    public Object findInterestRejectInfo(@PathVariable("id") Long id, HttpServletRequest request) {
        JSONObject result = null;
        try {
            result = wholeSalesFlowService.findInterestRejectInfo(id, request);
        } catch (Exception e) {
            log.info("merchant order details failed, error message: {}, e: {}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }
    @ApiOperation(value = "整体出售商户订单查询", notes = "分页查询整体销售流水表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/wholeMerchantOrderExcl")
    public void wholeMerchantOrderExcl(HttpServletRequest request,HttpServletResponse response) throws Exception {
        Map<String, Object> params = getConditionsMap(request);
        int total = qrPayFlowService.wholeMerchantOrderSearchCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<JSONObject> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrPayFlowService.wholeMerchantOrderSearch(params, scs, pc);
        }
        List<OrderExcelDTO> listOrder = new ArrayList<>();
        BigDecimal amt=new BigDecimal(0);
        BigDecimal pay=new BigDecimal(0);
        for (JSONObject jsonObject : list) {
            Set<String> strings = jsonObject.keySet();
            for (String string : strings) {
                // 处理枚举类
                if (string.equals("saleType")){
                    Integer integer = jsonObject.getInteger(string);
                    if (integer==0){
                        jsonObject.put(string,"Normal sales");
                    }else if (integer==1){
                        jsonObject.put(string,"Whole sales");
                    }else if (integer==2){
                        jsonObject.put(string,"Mixed sales");
                    }
                }
                if (string.equals("transType")){
                    Integer integer = jsonObject.getInteger(string);
                    if (integer==2){
                        jsonObject.put(string,"Bank Card");
                    }else if (integer==22){
                        jsonObject.put(string,"Installment");
                    }
                }
                if (string.equals("transAmount")){
                    amt=amt.add(jsonObject.getBigDecimal(string));
                    jsonObject.put(string,"$"+jsonObject.getBigDecimal(string));
                }
                if (string.equals("payAmount")){
                    pay=pay.add(jsonObject.getBigDecimal(string));
                    jsonObject.put(string,"$"+jsonObject.getBigDecimal(string));
                }
                if (string.equals("wholeSalesDiscount")){
                    jsonObject.put(string,jsonObject.getBigDecimal(string).setScale(0,BigDecimal.ROUND_DOWN)+"%");
                }
                jsonObject.put(string,jsonObject.get(string)==null?"":jsonObject.get(string).toString());
            }

            OrderExcelDTO orderExcelDTO = jsonObject.toJavaObject(OrderExcelDTO.class);
            listOrder.add(orderExcelDTO);
        }
        String todayStr = new SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(System.currentTimeMillis());
       String name= "wholeSaleFlow"+"-"+todayStr+".xlsx";
        List<JSONObject> param = new ArrayList<>();
        JSONObject jsonObject1=new JSONObject(1);
        jsonObject1.put("Total Order",total);
        param.add(jsonObject1);
        JSONObject jsonObject2=new JSONObject(1);
        jsonObject2.put("Total Wholesale  Order Amount","$"+amt);
        param.add(jsonObject2);
        JSONObject jsonObject3=new JSONObject(1);
        jsonObject3.put("Total Wholesales Actual payment","$"+pay);
        param.add(jsonObject3);
        Object start1 = params.get("start");
        Object end1 = params.get("end");
        Long start = null;
        Long end = null;
        if (start1!=null&&end1!=null){
            start=Long.valueOf(start1.toString());
            end=Long.valueOf(end1.toString());
        }
        if (start!=null&&end!=null&&start!=0&&end!=0){
            String startTime = new SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(start);
            String endTime = new SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(end);
            JSONObject jsonObject4=new JSONObject(1);
            jsonObject4.put("Time",startTime+"~"+endTime);
            param.add(jsonObject4);
        }else {
            JSONObject jsonObject4=new JSONObject(1);
            jsonObject4.put("Time","");
            param.add(jsonObject4);
        }
        Workbook workbook = POIUtils.createExcelNew(listOrder, OrderExcelDTO.class, name,param);
        if(workbook == null) {
            return;
        }
        response.reset();
        String dateStr =null;
        try {
            dateStr = URLEncoder.encode(marketingActivities, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 指定下载的文件名--设置响应头
        response.setHeader("Content_Disposition", "attachment;filename=" +dateStr+".xls");
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.addHeader("FileName", dateStr);
        // 写出数据输出流到页面
        try {
            OutputStream output = response.getOutputStream();
            BufferedOutputStream bufferedOutPut = new BufferedOutputStream(output);
            workbook.write(bufferedOutPut);
            bufferedOutPut.flush();
            bufferedOutPut.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
