package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.Director;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.model.entity.WholeSalesFlow;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javassist.bytecode.stackmap.BasicBlock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author: liming
 * @Date: 2020/10/19 16:01
 * @Description: 分期付Web调用
 */
@RestController
@RequestMapping("/creditWeb")
@Slf4j
@Api("分期付web调用")
public class CreditWebController extends BaseController<Director> {

    @Resource
    private CreditWebService creditWebService;

    @Resource
    private StaticDataService staticDataService;

    @Autowired
    private ClearBatchService clearBatchService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private WholeSalesFlowService wholeSalesFlowService;


    @PassToken
    @ApiOperation(value = "分页查询整体结算信息", notes = "分页查询整体结算信息以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "分页查询整体结算信息", value = "/getDiscountPackageList")
    public Object getDiscountPackageList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("CreditWebController: getDiscountPackageList call, params:{}", params);
        int total = creditWebService.searchMerchantListCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<DiscountPackageInfoDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = creditWebService.searchMerchantList(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @PassToken
    @ApiOperation(value = "结算记录", notes = "结算记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "结算记录", value = "/getSettlementInfo")
    public Object getSettlementInfo(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("CreditWebController: getSettlementInfo call, params:{}", params);
        int total = creditWebService.searchSettlementInfoCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<SettlementInfoDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = creditWebService.searchSettlementInfoList(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @PassToken
    @ApiOperation(value = "整体出售流水明细", notes = "整体出售流水明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "整体出售流水明细", value = "/getWholeSalesFlowInfo")
    public Object getWholeSalesFlowInfo(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("CreditWebController: getWholeSalesFlowInfo call, params:{}", params);
        int total = creditWebService.searchMerchantWholeSalesFlowInfoCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MerchantWholeSalesFlowInfoDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = creditWebService.searchMerchantWholeSalesFlowInfo(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @PassToken
    @ApiOperation(value = "置结算失败", notes = "置结算失败")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "flowId", value = "交易流水号", paramType = "query")
    })
    @PutMapping(name = "置结算失败", value = "/failedClearDetailInfo/{flowId}")
    public Object failedClearDetailInfo(@PathVariable("flowId") Long flowId, HttpServletRequest request) {
        log.info("CreditWebController: failedClearDetailInfo call, flowId:{}", flowId);
        try {
            creditWebService.failedClearDetailInfo(flowId, request);
        } catch (BizException e) {
            log.error(e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PassToken
    @ApiOperation(value = "整体出售导出结算", notes = "整体出售导出")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "list", paramType = "query")
    })
    @PostMapping(name = "整体出售导出结算", value = "/discountPackageSettlementExport")
    public void export(@RequestBody List<Long> ids, HttpServletRequest request, HttpServletResponse response) {
        log.info("CreditWebController: export call, ids:{}", ids);
        File file;
        FileInputStream fis;
        BufferedInputStream bis = null;
        try {
            ClearBatchDTO export = creditWebService.export(ids, request);
            file = new File(export.getUrl());
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                return ;
            }
            byte[] b = new byte[1024];
            String fileName = URLEncoder.encode(export.getFileName(), "UTF-8");
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            response.setContentType("application/force-download");
            response.setHeader("FileName", fileName + ".csv");
            response.setHeader("Access-Control-Expose-Headers", "FileName");
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            OutputStream os = response.getOutputStream();
            int i = bis.read(b);
            while (i != -1) {
                os.write(b, 0, i);
                i = bis.read(b);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    @PassToken
    @ApiOperation(value = "整体出售导出结算", notes = "整体出售导出")
    @GetMapping(name = "整体出售导出结算", value = "/wholeSaleSettlementExport")
    public void exportNew( HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> params = getConditionsMap(request);
        ClearBatchDTO export = new ClearBatchDTO();
        log.info("CreditWebController: export call, params:{}", params);
        File file;
        FileInputStream fis;
        BufferedInputStream bis = null;
        try {
            export = creditWebService.exportNew(params, request);
            // 批次处理成功了下载文件
            if(export != null && export.getState() == StaticDataEnum.CLEAR_BATCH_STATE_1.getCode()){

            }else{
                export = clearBatchService.createNullClearBillFile();
            }
        } catch (Exception e) {
            log.error("CreditWebController, export,Exception:"+e.getMessage(), e);
            export = clearBatchService.createNullClearBillFile();
        } finally {

            try {
                file = new File(export.getUrl());
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    return ;
                }
                byte[] b = new byte[1024];
                String fileName = URLEncoder.encode(export.getFileName(), "UTF-8");
                response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
                response.setContentType("application/force-download");
                response.setHeader("FileName", fileName + ".csv");
                response.setHeader("Access-Control-Expose-Headers", "FileName");
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(b);
                while (i != -1) {
                    os.write(b, 0, i);
                    i = bis.read(b);
                }
            } catch(Exception e){
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch (IOException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }

        }
    }

    @PassToken
    @ApiOperation(value = "修改延时结算状态", notes = "修改延时结算状态")
    @PostMapping(value = "/updateSettlementDelay",name = "修改延时结算状态")
    public Object updateSettlementDelay(@RequestBody JSONObject requestInfo) {
        log.info("CreditWebController: updateSettlementDelay call, requestInfo:{}", requestInfo);
        try {
            ChangeClearStateDTO req = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), ChangeClearStateDTO.class);
            creditWebService.updateSettlementDelay(req.getType(), req.getIdList());
        } catch (BizException e) {
            log.error(e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PassToken
    @ApiOperation(value = "通过多个code，查询数据字典数据", notes = "分页查询数据字典以及排序功能")
    @GetMapping(value = "/findByCodeList",name = "通过多个code，查询数据字典数据")
    public Object findByCodeList(HttpServletRequest request) {
        String code = request.getParameter("code");
        if(StringUtils.isEmpty(code)) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.error", getLang(request)));
        }
        final String[] codeList = code.split(",");
        Map<String, List<StaticData>> result = staticDataService.findByCodeList(codeList);
        return R.success(result);
    }

    @ApiOperation(value = "商户资金清算列表查询", notes = "商户资金清算列表查询")
    @GetMapping(value = "/getWholeSaleClearList", name="商户资金清算列表查询")
    public Object merchantClearMessageList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("getWholeSaleClearList params:"+params);
        if(params.containsKey("clearStateListType") && StringUtils.isNotEmpty(params.get("clearStateListType").toString())){
            if(params.get("clearStateListType").toString().equals(StaticDataEnum.CLEAR_STATE_TYPE_0.getCode()+"")){
                params.put("settlementState",StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
                params.put("approve_state",StaticDataEnum.STATUS_1.getCode());
                params.put("settlementDelay",StaticDataEnum.STATUS_0.getCode());
            }else if (params.get("clearStateListType").toString().equals(StaticDataEnum.CLEAR_STATE_TYPE_3.getCode()+"")){
                params.put("settlementState",StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
                params.put("approve_state",StaticDataEnum.STATUS_1.getCode());
                params.put("settlementDelay",StaticDataEnum.STATUS_1.getCode());
            }

        }
        int total = wholeSalesFlowService.getMerchantClearMessageCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<Map<String ,Object>> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = creditWebService.merchantClearMessageList(params, scs, pc);
        }
        return R.success(list, pc);
    }
    @ApiOperation(value = "商户整体出售资金清算详情", notes = "商户整体出售资金清算详情")
    @GetMapping(value = "/wholeSaleListDetails", name="商户整体出售资金清算详情")
    public Object wholeSaleListDetails(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        List<WholeSalesFlowDTO> wholeSalesFlowDTOS = creditWebService.wholeSaleListDetails(params);
        return R.success(wholeSalesFlowDTOS);
    }

}
