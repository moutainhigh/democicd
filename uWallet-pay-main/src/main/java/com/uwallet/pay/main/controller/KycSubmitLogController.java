package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.KycSubmitLogDTO;
import com.uwallet.pay.main.model.dto.KycSubmitLogExcl;
import com.uwallet.pay.main.model.dto.OrderExcelDTO;
import com.uwallet.pay.main.model.dto.UserDTO;
import com.uwallet.pay.main.model.entity.KycSubmitLog;
import com.uwallet.pay.main.service.KycSubmitLogService;
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
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import com.uwallet.pay.main.util.POIUtils;
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
 *
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description:
 * @author: xucl
 * @date: Created in 2021-04-08 13:24:29
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@RestController
@RequestMapping("/kycSubmitLog")
@Slf4j
@Api("")
public class KycSubmitLogController extends BaseController<KycSubmitLog> {

    @Autowired
    private KycSubmitLogService kycSubmitLogService;

    @Value("${export.excel.marketingActivities}")
    private String marketingActivities;

    @ActionFlag(detail = "KycSubmitLog_list")
    @ApiOperation(value = "????????????", notes = "??????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "???????????????", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "???????????????", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "????????????????????????scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "??????-??????")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = kycSubmitLogService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<KycSubmitLogDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = kycSubmitLogService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "??????id??????", notes = "??????id??????")
    @ApiImplicitParam(name = "id", value = "??????id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="??????")
    public Object view(@PathVariable("id") Long id) {
        log.info("get kycSubmitLog Id:{}", id);
        return R.success(kycSubmitLogService.findKycSubmitLogById(id));
    }

    @ApiOperation(value = "????????????????????????????????????", notes = "????????????????????????????????????")
    @GetMapping(value = "/findOne", name="????????????????????????????????????")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get kycSubmitLog findOne params:{}", params);
        int total = kycSubmitLogService.count(params);
        if (total > 1) {
            log.error("get kycSubmitLog findOne params: {}, error message:{}", params, "?????????????????????????????????");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        KycSubmitLogDTO kycSubmitLogDTO = null;
        if (total == 1) {
            kycSubmitLogDTO = kycSubmitLogService.findOneKycSubmitLog(params);
        }
        return R.success(kycSubmitLogDTO);
    }

    @ActionFlag(detail = "KycSubmitLog_add")
    @PostMapping(name = "??????")
    @ApiOperation(value = "??????", notes = "??????")
    public Object create(@RequestBody KycSubmitLogDTO kycSubmitLogDTO, HttpServletRequest request) {
        log.info("add kycSubmitLog DTO:{}", kycSubmitLogDTO);
        try {
            kycSubmitLogService.saveKycSubmitLog(kycSubmitLogDTO, request);
        } catch (BizException e) {
            log.error("add kycSubmitLog failed, kycSubmitLogDTO: {}, error message:{}, error all:{}", kycSubmitLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "KycSubmitLog_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "??????", notes = "??????")
    @ApiImplicitParam(name = "id", value = "??????id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody KycSubmitLogDTO kycSubmitLogDTO, HttpServletRequest request) {
        log.info("put modify id:{}, kycSubmitLog DTO:{}", id, kycSubmitLogDTO);
        try {
            kycSubmitLogService.updateKycSubmitLog(id, kycSubmitLogDTO, request);
        } catch (BizException e) {
            log.error("update kycSubmitLog failed, kycSubmitLogDTO: {}, error message:{}, error all:{}", kycSubmitLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "KycSubmitLog_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "??????", notes = "??????")
    @ApiImplicitParam(name = "id", value = "??????id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete kycSubmitLog, id:{}", id);
        try {
            kycSubmitLogService.logicDeleteKycSubmitLog(id, request);
        } catch (BizException e) {
            log.error("delete failed, kycSubmitLog id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PostMapping("/update")
    @PassToken
    @ApiOperation(value = "??????", notes = "??????")
    public Object updateLog(@RequestBody JSONObject data, HttpServletRequest request) {
        log.info("put modify data:{}", data);
        try {
            Long id = data.getLong("id");
            if (id!=null&&id!=0){
               //JSONObject params=new JSONObject();
                Integer isWatchlist = data.getInteger("isWatchlist");
                //params.put("isWatchlist", isWatchlist);
                Integer isRequest = data.getInteger("isRequest");
                Integer kycStatus = data.getInteger("kycStatus");
                //params.put("isRequest", isRequest);
                //params.put("id",id);
                KycSubmitLogDTO dto = new KycSubmitLogDTO();
                if (kycStatus.compareTo(StaticDataEnum.KYC_CHECK_STATE_4.getCode())==0||kycStatus.compareTo(StaticDataEnum.KYC_CHECK_STATE_5.getCode())==0){
                    // ?????????-1???????????????
                    dto.setStatus(-1);
                }else {
                    dto.setStatus(1);
                }
                dto.setIsRequest(isRequest);
                dto.setIsWatchlist(isWatchlist);
                dto.setKycStatus(kycStatus);
                dto.setId(id);
                kycSubmitLogService.updateKycSubmitLog(id,dto,request);
                //kycSubmitLogService.updateKycSubmitLogNew(params,request);
            }else {
                throw new BizException("??????????????????id??????");
            }
        } catch (BizException e) {
            log.error("update kycSubmitLog failed, data: {}, error message:{}, error all:{}", data, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "kyc_detail")
    @ApiOperation(value = "??????????????????????????????", notes = "??????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "???????????????", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "???????????????", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "????????????????????????scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "??????-??????",value = "/getList")
    public Object getList(HttpServletRequest request) throws BizException {
        Map<String, Object> params = getConditionsMap(request);
        log.info("?????????????????????????????? data:{}",params);
        int total = kycSubmitLogService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<JSONObject> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = kycSubmitLogService.findLogList(params, scs, pc);
        }
        return R.success(list, pc);
    }
    @ActionFlag(detail = "kyc_detail")
    @ApiOperation(value = "????????????????????????excl", notes = "????????????????????????excl")
    @GetMapping(name = "??????-Excl??????",value = "/getListExcel")
    public void getListExcl(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> params = getConditionsMap(request);
        log.info("????????????????????????excl data:{}",params);
        List<JSONObject> list ;
        Vector<SortingContext> scs = getSortingContext(request);;
        list = kycSubmitLogService.findLogList(params, scs, null);
        List<KycSubmitLogExcl> logExcls=new ArrayList<>();
        for (JSONObject jsonObject : list) {
            KycSubmitLogExcl kycSubmitLogExcl = new KycSubmitLogExcl();
            kycSubmitLogExcl.setReferralCode(jsonObject.getLong("referralCode").toString());
            kycSubmitLogExcl.setName(jsonObject.getString("name"));
            kycSubmitLogExcl.setKycStatus(jsonObject.getString("kycStatus"));
            kycSubmitLogExcl.setDate(jsonObject.getString("date"));
            kycSubmitLogExcl.setCustomerAccount(jsonObject.getString("customerAccount"));
            kycSubmitLogExcl.setAccountSubmittedTimes(jsonObject.getInteger("accountSubmittedTimes").toString());
            logExcls.add(kycSubmitLogExcl);
        }
        Workbook workbook = POIUtils.createExcel(logExcls, KycSubmitLogExcl.class,"KycSubmitLogExcl.xlsx");
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
        Object start = params.get("start");
        String format="All";
        if (start!=null){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
            String substring= simpleDateFormat.format(new Date(Long.parseLong(start.toString())));
            format = substring.substring(0,4);
        }
        // ????????????????????????--???????????????
        response.setHeader("Content_Disposition", "attachment;filename="+format+"KycDetail.xls");
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.addHeader("FileName", format+"KycDetail.xls");
        // ??????????????????????????????
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
