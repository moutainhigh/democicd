package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.filter.AppToken;
import com.uwallet.pay.main.filter.SignVerify;
import com.uwallet.pay.main.model.dto.FirebaseDTO;
import com.uwallet.pay.main.model.dto.PushAndSendMessageLogDTO;
import com.uwallet.pay.main.model.entity.PushAndSendMessageLog;
import com.uwallet.pay.main.service.PushAndSendMessageLogService;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.main.service.ServerService;
import com.uwallet.pay.main.util.FireBaseUtil;
import com.uwallet.pay.main.util.I18nUtils;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
 * @date: Created in 2021-04-16 15:56:48
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@RestController
@RequestMapping("/pushAndSendMessageLog")
@Slf4j
@Api("")
public class PushAndSendMessageLogController extends BaseController<PushAndSendMessageLog> {

    @Autowired
    private PushAndSendMessageLogService pushAndSendMessageLogService;
    @Autowired
    @Lazy
    private ServerService serverService;

    @ActionFlag(detail = "PushAndSendMessageLog_list")
    @ApiOperation(value = "????????????", notes = "??????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "???????????????", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "???????????????", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "????????????????????????scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "??????-??????")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = pushAndSendMessageLogService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<PushAndSendMessageLogDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = pushAndSendMessageLogService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "??????id??????", notes = "??????id??????")
    @ApiImplicitParam(name = "id", value = "??????id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="??????")
    public Object view(@PathVariable("id") Long id) {
        log.info("get pushAndSendMessageLog Id:{}", id);
        return R.success(pushAndSendMessageLogService.findPushAndSendMessageLogById(id));
    }

    @ApiOperation(value = "????????????????????????????????????", notes = "????????????????????????????????????")
    @GetMapping(value = "/findOne", name="????????????????????????????????????")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get pushAndSendMessageLog findOne params:{}", params);
        int total = pushAndSendMessageLogService.count(params);
        if (total > 1) {
            log.error("get pushAndSendMessageLog findOne params: {}, error message:{}", params, "?????????????????????????????????");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        PushAndSendMessageLogDTO pushAndSendMessageLogDTO = null;
        if (total == 1) {
            pushAndSendMessageLogDTO = pushAndSendMessageLogService.findOnePushAndSendMessageLog(params);
        }
        return R.success(pushAndSendMessageLogDTO);
    }

    @ActionFlag(detail = "PushAndSendMessageLog_add")
    @PostMapping(name = "??????")
    @ApiOperation(value = "??????", notes = "??????")
    public Object create(@RequestBody PushAndSendMessageLogDTO pushAndSendMessageLogDTO, HttpServletRequest request) {
        log.info("add pushAndSendMessageLog DTO:{}", pushAndSendMessageLogDTO);
        try {
            pushAndSendMessageLogService.savePushAndSendMessageLog(pushAndSendMessageLogDTO, request);
        } catch (BizException e) {
            log.error("add pushAndSendMessageLog failed, pushAndSendMessageLogDTO: {}, error message:{}, error all:{}", pushAndSendMessageLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "PushAndSendMessageLog_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "??????", notes = "??????")
    @ApiImplicitParam(name = "id", value = "??????id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody PushAndSendMessageLogDTO pushAndSendMessageLogDTO, HttpServletRequest request) {
        log.info("put modify id:{}, pushAndSendMessageLog DTO:{}", id, pushAndSendMessageLogDTO);
        try {
            pushAndSendMessageLogService.updatePushAndSendMessageLog(id, pushAndSendMessageLogDTO, request);
        } catch (BizException e) {
            log.error("update pushAndSendMessageLog failed, pushAndSendMessageLogDTO: {}, error message:{}, error all:{}", pushAndSendMessageLogDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "PushAndSendMessageLog_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "??????", notes = "??????")
    @ApiImplicitParam(name = "id", value = "??????id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete pushAndSendMessageLog, id:{}", id);
        try {
            pushAndSendMessageLogService.logicDeletePushAndSendMessageLog(id, request);
        } catch (BizException e) {
            log.error("delete failed, pushAndSendMessageLog id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }
//    @SignVerify
//    @AppToken
//    @ApiOperation(value = "??????", notes = "??????")
//    @PostMapping("/test")
//    public Object test(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
//        log.info("?????????????????????, data:{}",requestInfo);
//        try{
//            FirebaseDTO firebaseDTO = new FirebaseDTO();
//
//            firebaseDTO.setUserId(403437613582831616L);
//            firebaseDTO.setVoice(0);
//            firebaseDTO.setTitle("222");
//            firebaseDTO.setBody("222");
//            Integer a = requestInfo.getInteger("a");
//            if (a==1){
//                serverService.pushFirebase(firebaseDTO,request);
//            }else {
//                serverService.pushFirebaseList(firebaseDTO,null);
//            }
//            return null;
//        }catch (Exception e){
//            log.error("?????????????????????,error message:{},e:{}",e.getMessage(),e);
//            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
//        }
//    }
        @ActionFlag(detail = "BillingRecord_list")
        @ApiOperation(value = "????????????", notes = "??????????????????????????????")
        @GetMapping(name = "??????-??????",value = "/getList")
        public Object listNew(HttpServletRequest request) throws Exception {
            PagingContext pc = new PagingContext();
            pc.setTotal(1);
            pc.setPageSize(10);
            Map<String, Object> params = getConditionsMap(request);
            List<JSONObject> billingRecord = pushAndSendMessageLogService.getBillingRecord(params,request);
            return R.success(billingRecord,pc);
        }
}
