package com.uwallet.pay.main.controller;


import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.dao.ClearBatchDAO;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.QrPayDTO;
import com.uwallet.pay.main.model.entity.ClearBatch;
import com.uwallet.pay.main.service.ClearBatchService;
import com.uwallet.pay.main.service.ClearDetailService;
import com.uwallet.pay.main.service.QrPayService;
import com.uwallet.pay.main.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>
 * 二维码信息、绑定
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 扫码支付
 * @author: zhoutt
 * @date: Created in 2019-12-10 14:39:07
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */

@RestController
@RequestMapping("/qrPayService")
@Slf4j
@Api("扫码支付")
public class QrPayController extends BaseController {


    @Autowired
    private QrPayService qrPayService;
    @Autowired
    private UserService userService;
    @Autowired
    private ClearDetailService clearDetailService;

    @Autowired
    private ClearBatchService clearBatchService;

//    @PostMapping("/qrPay")
//    @ApiOperation(value = "扫码支付交易", notes = "扫码支付交易")
//    public Object qrPay(@RequestBody QrPayDTO qrPayDTO, HttpServletRequest request) {
//        log.info("qrPayService/qrPay  PayDTO:{}",  qrPayDTO);
//        try {
//
//            //入参校验
//            qrPayService.qrPayReqCheck(qrPayDTO,request);
//            //扫码支付交易
//
//            Long flowId = qrPayService.doQrPay(qrPayDTO,request);
//
//        } catch (BizException e) {
//            log.error("qrPayService/qrPay, qrPayDTO: {}, error message:{}, error all:{}", qrPayDTO, e.getMessage(), e);
//            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
//        }
//
//        return R.success();
//    }
//    @PostMapping("/qrPayCheck")
//    @ApiOperation(value = "扫码支付交易", notes = "扫码支付交易")
//    public Object qrPayCheck() {
//        Map<String, Object> params = new HashMap<>();
//        params.put("","2");
//        try {
//            clearBatchService.clearBatchAction(params,request);
//
//        } catch (Exception e) {
//            log.error("qrPayService/qrPayCheck, qrPayDTO: {}, error message:{}, error all:{}", e.getMessage(), e);
//            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
//        }
//
//        return R.success();
//    }


}



