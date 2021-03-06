package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.IllionSubmitLogDTO;
import com.uwallet.pay.main.model.dto.KycSubmitLogExcl;
import com.uwallet.pay.main.model.entity.IllionSubmitLog;
import com.uwallet.pay.main.model.excel.IllionLogExcel;
import com.uwallet.pay.main.service.IllionSubmitLogService;
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
 * @date: Created in 2021-04-13 11:11:34
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@RestController
@RequestMapping("/illionSubmitLog")
@Slf4j
@Api("")
public class IllionSubmitLogController extends BaseController<IllionSubmitLog> {

    @Autowired
    private IllionSubmitLogService illionSubmitLogService;
    @Value("${export.excel.marketingActivities}")
    private String marketingActivities;

    @ActionFlag(detail = "IllionSubmitLog_list")
    @ApiOperation(value = "????????????", notes = "??????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "???????????????", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "???????????????", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "????????????????????????scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "??????-??????")
    public Object list(HttpServletRequest request) throws BizException {
        Map<String, Object> params = getConditionsMap(request);
        int total = illionSubmitLogService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<IllionSubmitLogDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = illionSubmitLogService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "??????id??????", notes = "??????id??????")
    @ApiImplicitParam(name = "id", value = "??????id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="??????")
    public Object view(@PathVariable("id") Long id) {
        log.info("get illionSubmitLog Id:{}", id);
        return R.success(illionSubmitLogService.findIllionSubmitLogById(id));
    }

    @ApiOperation(value = "????????????????????????????????????", notes = "????????????????????????????????????")
    @GetMapping(value = "/findOne", name="????????????????????????????????????")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get illionSubmitLog findOne params:{}", params);
        int total = illionSubmitLogService.count(params);
        if (total > 1) {
            log.error("get illionSubmitLog findOne params: {}, error message:{}", params, "?????????????????????????????????");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        IllionSubmitLogDTO illionSubmitLogDTO = null;
        if (total == 1) {
            illionSubmitLogDTO = illionSubmitLogService.findOneIllionSubmitLog(params);
        }
        return R.success(illionSubmitLogDTO);
    }

    @ActionFlag(detail = "IllionSubmitLog_add")
    @PostMapping(name = "??????")
    @ApiOperation(value = "??????", notes = "??????")
    public Object create(@RequestBody IllionSubmitLogDTO illionSubmitLogDTO, HttpServletRequest request) {
        log.info("add illionSubmitLog DTO:{}", illionSubmitLogDTO);
        try {
            illionSubmitLogService.saveIllionSubmitLog(illionSubmitLogDTO, request);
        } catch (BizException e) {
            log.error("add illionSubmitLog failed, illionSubmitLogDTO: {}, error message:{}, error all:{}", illionSubmitLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "IllionSubmitLog_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "??????", notes = "??????")
    @ApiImplicitParam(name = "id", value = "??????id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody IllionSubmitLogDTO illionSubmitLogDTO, HttpServletRequest request) {
        log.info("put modify id:{}, illionSubmitLog DTO:{}", id, illionSubmitLogDTO);
        try {
            illionSubmitLogService.updateIllionSubmitLog(id, illionSubmitLogDTO, request);
        } catch (BizException e) {
            log.error("update illionSubmitLog failed, illionSubmitLogDTO: {}, error message:{}, error all:{}", illionSubmitLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "IllionSubmitLog_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "??????", notes = "??????")
    @ApiImplicitParam(name = "id", value = "??????id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete illionSubmitLog, id:{}", id);
        try {
            illionSubmitLogService.logicDeleteIllionSubmitLog(id, request);
        } catch (BizException e) {
            log.error("delete failed, illionSubmitLog id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }
    @ActionFlag(detail = "IllionSubmitLog_update")
    @PostMapping("/update")
    @PassToken
    @ApiOperation(value = "??????", notes = "??????")
    public Object updateLog(@RequestBody JSONObject param, HttpServletRequest request) throws BizException {
        log.info("put modify param:{}",param);
        Long submitId = param.getLong("submitId");
        JSONObject params=new JSONObject();
        params.put("id",submitId);
        IllionSubmitLogDTO dto=new IllionSubmitLogDTO();
        dto.setReportStatus(param.getInteger("reportStatus"));
        params.put("reportStatus",param.getInteger("reportStatus"));
        illionSubmitLogService.updateIllionSubmitLogNew(submitId,dto,request);
        return R.success();
    }

    @ActionFlag(detail = "IllionSubmitLog_list")
    @ApiOperation(value = "???????????????", notes = "?????????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "???????????????", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "???????????????", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "????????????????????????scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "??????-??????",value = "/getList")
    public Object listNew(HttpServletRequest request) throws BizException {
        Map<String, Object> params = getConditionsMap(request);
        log.info("?????????illion????????????????????????,param:{}",params);
        //customerAccount ?????????
        int total = illionSubmitLogService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<IllionSubmitLogDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = illionSubmitLogService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }


    @ActionFlag(detail = "IllionSubmitLog_list")
    @ApiOperation(value = "????????????????????????excl", notes = "????????????????????????excl")
    @GetMapping(name = "??????-Excl??????",value = "/getListExcel")
    public void getListExcl(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> params = getConditionsMap(request);
        log.info("????????????????????????excl data:{}",params);
        List<IllionSubmitLogDTO> list= illionSubmitLogService.find(params, null, null);
        List<IllionLogExcel> logExcls=new ArrayList<>();
        for (IllionSubmitLogDTO illionSubmitLogDTO : list) {
            IllionLogExcel illionLogExcel=new IllionLogExcel();
            illionLogExcel.setAccountsSubmittedTimes(illionSubmitLogDTO.getSubmitNumber());
            illionLogExcel.setName(illionSubmitLogDTO.getName());
            illionLogExcel.setCustomerAccount(illionSubmitLogDTO.getPhone());
            illionLogExcel.setBank(illionSubmitLogDTO.getBank());
            illionLogExcel.setReferralCode(illionSubmitLogDTO.getReferralCode());
            illionLogExcel.setBankConnectedStatusAndErrorMessage(illionSubmitLogDTO.getSubmitStr());
            illionLogExcel.setReportStatus(illionSubmitLogDTO.getReportStatusStr());
            illionLogExcel.setDate(illionSubmitLogDTO.getSimpleDate());
            logExcls.add(illionLogExcel);
        }

        Workbook workbook = POIUtils.createExcel(logExcls, IllionLogExcel.class,"illionSubmitLogExcl.xlsx");
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
        response.setHeader("Content_Disposition", "attachment;filename="+format+"IllionDetail.xlsx");
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.addHeader("FileName", format+"IllionDetail.xls");
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
