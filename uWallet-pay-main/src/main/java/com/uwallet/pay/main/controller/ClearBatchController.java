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
import com.uwallet.pay.main.filter.SignVerify;
import com.uwallet.pay.main.model.dto.ChangeClearStateDTO;
import com.uwallet.pay.main.model.dto.ClearBatchDTO;
import com.uwallet.pay.main.model.dto.QrPayFlowDTO;
import com.uwallet.pay.main.model.entity.ClearBatch;
import com.uwallet.pay.main.service.ClearBatchService;
import com.uwallet.pay.main.service.QrPayFlowService;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;


/**
 * <p>
 * 清算表生成
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 清算表生成
 * @author: zhoutt
 * @date: Created in 2019-12-20 10:49:55
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/clearBatch")
@Slf4j
@Api("清算表生成")
public class ClearBatchController extends BaseController<ClearBatch> {

    @Autowired
    private ClearBatchService clearBatchService;
    @Autowired
    private QrPayFlowService qrPayFlowService;


    @ActionFlag(detail = "liquidationRecord_list")
    @ApiOperation(value = "分页查询清算表生成", notes = "分页查询清算表生成以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-清算表生成列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = clearBatchService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ClearBatchDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = clearBatchService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询清算表生成", notes = "通过id查询清算表生成")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get clearBatch Id:{}", id);
        return R.success(clearBatchService.findClearBatchById(id));
    }

    @ApiOperation(value = "通过查询条件查询清算表生成一条数据", notes = "通过查询条件查询清算表生成一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询清算表生成一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get clearBatch findOne params:{}", params);
        int total = clearBatchService.count(params);
        if (total > 1) {
            log.error("get clearBatch findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        ClearBatchDTO clearBatchDTO = null;
        if (total == 1) {
            clearBatchDTO = clearBatchService.findOneClearBatch(params);
        }
        return R.success(clearBatchDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增清算表生成", notes = "新增清算表生成")
    public Object create(@RequestBody ClearBatchDTO clearBatchDTO, HttpServletRequest request) {
        log.info("add clearBatch DTO:{}", clearBatchDTO);
        try {
            clearBatchService.saveClearBatch(clearBatchDTO, request);
        } catch (BizException e) {
            log.error("add clearBatch failed, clearBatchDTO: {}, error message:{}, error all:{}", clearBatchDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改清算表生成", notes = "修改清算表生成")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody ClearBatchDTO clearBatchDTO, HttpServletRequest request) {
        log.info("put modify id:{}, clearBatch DTO:{}", id, clearBatchDTO);
        try {
            clearBatchService.updateClearBatch(id, clearBatchDTO, request);
        } catch (BizException e) {
            log.error("update clearBatch failed, clearBatchDTO: {}, error message:{}, error all:{}", clearBatchDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除清算表生成", notes = "删除清算表生成")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete clearBatch, id:{}", id);
        try {
            clearBatchService.logicDeleteClearBatch(id, request);
        } catch (BizException e) {
            log.error("delete failed, clearBatch id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "商户资金清算", notes = "商户资金清算")
    @GetMapping (value = "/clearForMerchant", name="商户资金清算")
    public Object clearBatch(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("clearBatch clearForMerchant, data:{}", params);
        try{
            clearBatchService.clearBatchAction(params, request, response);
        }catch (BizException e){
            log.error("clearBatch clearForMerchant failed,  error message:{}, error all:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }

        return R.success();
    }


    @ApiOperation(value = "api平台资金清算", notes = "api平台资金清算")
//    @GetMapping (value = "/apiPlatformClear", name="api平台资金清算")
    //废弃方法
    public Object apiPlatformClear(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("clearBatch apiPlatformClear, data:{}", params);
        try{
            clearBatchService.apiPlatformClearAction(params, request, response);
        }catch (BizException e){
            log.error("clearBatch apiPlatformClear,  error message:{}, error all:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }

        return R.success();
    }


    @ActionFlag(detail = "api_merchantClear_list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @ApiOperation(value = "api平台服务器待c列表查询", notes = "api平台服务器待清算列表查询")
    @GetMapping (value = "/getApiPlatformClearList", name="api平台服务器待清算列表查询")
    public Object getApiPlatformClearList(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("clearBatch getApiPlatformClearList, data:{}", params);
        //需要结算平台服务费的类型
        int [] orderSource  = {StaticDataEnum.ORDER_SOURCE_1.getCode()};
        params.put("orderSources",orderSource);
        params.put("state",StaticDataEnum.TRANS_STATE_1.getCode());
        params.put("orgAccPltFeeClearState",StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        int total = qrPayFlowService.countApiPlatformClear(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ClearBatchDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrPayFlowService.apiPlatformClearList(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ActionFlag(detail = "api_merchantClear_list")
    @ApiOperation(value = "api平台服务器待清算详情", notes = "api平台服务器待清算详情")
    @GetMapping(value = "/getApiPlatformClearDetail", name="商户资金清算详情")
    public Object qrPayFlowListDetails(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        //需要结算平台服务费的类型
        int [] orderSource  = {StaticDataEnum.ORDER_SOURCE_1.getCode()};
        params.put("orderSources",orderSource);
        params.put("state",StaticDataEnum.TRANS_STATE_1.getCode());
        params.put("orgAccPltFeeClearState",StaticDataEnum.CLEAR_STATE_TYPE_0.getCode());
        List<QrPayFlowDTO> qrPayFlowDTOS = qrPayFlowService.getApiPlatformClearDetail(params);
        return R.success(qrPayFlowDTOS);
    }

    @PassToken
    @ApiOperation(value = "未结算/延迟转换", notes = "未结算/延迟转换")
    @PostMapping("/changeClearState")
    public Object changeClearState(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("changeClearState , data:{}", requestInfo);
        ChangeClearStateDTO req = JSONObject.parseObject(requestInfo.getJSONObject("data").toJSONString(), ChangeClearStateDTO.class);
        try {
            qrPayFlowService.changeClearState(req,request);
        }catch (Exception e) {
            log.error("changeClearState failed, data:{}, e:{}, error message:{}", requestInfo, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success();
    }

    @PassToken
    @ApiOperation(value = "已清算改清算失败", notes = "已清算改清算失败")
    @PostMapping("/clearFail/{id}")
    public Object clearFail(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("clearFail , id:{}", id);
        try {
            clearBatchService.clearFail(id,request);
        }catch (Exception e) {
            log.error("clearFail failed, id:{}, e:{}, error message:{}", id, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "获取用户id", notes = "获取用户id")
    @GetMapping (value = "/getUserId")
    public Object toGetUserId(HttpServletRequest request) {
        Long userId = getUserId(request);
        return R.success(userId.toString());
    }



    @PassToken
    @ApiOperation(value = "导出结算", notes = "导出结算")
    @GetMapping(name = "导出结算", value = "/settleFileExport")
    public void settleFileExport( HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("ClearBatch settleFileExport params:{}", params);
        ClearBatchDTO clearBatchDTO = new ClearBatchDTO();
        try{
            clearBatchDTO = clearBatchService.settleFileExport(params, request,response);
            // 批次处理成功了下载文件
            if(clearBatchDTO != null && clearBatchDTO.getState() == StaticDataEnum.CLEAR_BATCH_STATE_1.getCode()){

            }else{
                clearBatchDTO = clearBatchService.createNullClearBillFile();
            }

        }catch (Exception e){
            log.error("clearBatch clearForMerchant failed,  error message:{}, error all:{}", e.getMessage(), e);
//            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
            clearBatchDTO = clearBatchService.createNullClearBillFile();
        }finally {
            File file;
            FileInputStream fis;
            BufferedInputStream bis = null;
            try {

                file = new File(clearBatchDTO.getUrl());
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    return ;
                }
                byte[] b = new byte[1024];
                String fileName = URLEncoder.encode(file.getName(), "UTF-8");
                response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
                response.setContentType("application/download;charset=UTF-8");
                response.setContentType("application/csv;charset=UTF-8");
                response.setHeader("FileName", fileName);
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

    }


    @PassToken
    @ApiOperation(value = "H5商户导出结算校验", notes = "导出结算")
    @GetMapping(name = "H5商户导出结算校验", value = "/h5MerchantSettleCheck")
    public Object h5MerchantSettleCheck( HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("h5MerchantSettleCheck params:{}", params);
        try {
            clearBatchService.h5MerchantSettleCheck(params,request);
        }catch (Exception e) {
            log.error("h5MerchantSettleCheck failed, data:{}, e:{}, error message:{}", params, e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }


    @PassToken
    @ApiOperation(value = "H5商户导出结算", notes = "导出结算")
    @GetMapping(name = "H5商户导出结算", value = "/H5MerchantSettleFileExport")
    public void H5MerchantSettleFileExport( HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("H5 Merchant ClearBatch settleFileExport params:{}", params);
        ClearBatchDTO clearBatchDTO = new ClearBatchDTO();
        try{
            clearBatchDTO = clearBatchService.H5MerchantSettleFileExport(params, request,response);
            // 批次处理成功了下载文件
            if(clearBatchDTO != null && clearBatchDTO.getState() == StaticDataEnum.CLEAR_BATCH_STATE_1.getCode()){

            }else{
                clearBatchDTO = clearBatchService.createNullClearBillFile();
            }

        }catch (Exception e){
            log.error("clearBatch clearForMerchant failed,  error message:{}, error all:{}", e.getMessage(), e);
//            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
            clearBatchDTO = clearBatchService.createNullClearBillFile();
        }finally {
            File file;
            FileInputStream fis;
            BufferedInputStream bis = null;
            try {

                file = new File(clearBatchDTO.getUrl());
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    return ;
                }
                byte[] b = new byte[1024];
                String fileName = URLEncoder.encode(file.getName(), "UTF-8");
                response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
                response.setContentType("application/download;charset=UTF-8");
                response.setContentType("application/csv;charset=UTF-8");
                response.setHeader("FileName", fileName  );
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

    }
}
