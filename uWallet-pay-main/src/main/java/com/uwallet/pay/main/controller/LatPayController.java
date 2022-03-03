package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.service.LatPayService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author baixinyue
 */

@RestController
@RequestMapping("/latPay")
@Slf4j
@Api("LatPay应用Controller")
public class LatPayController {

    @Autowired
    private LatPayService latPayService;

    @PostMapping("/notify")
    @PassToken
    public void directDebitNotify(@RequestBody JSONObject payResult, HttpServletRequest request) {
        log.info("latpay direct debit notify, result:{}", payResult);
        try {
            latPayService.directDebitNotify(payResult, request);
        } catch (BizException e) {
            log.info("latpay direct debit notify failed, error message:{}, e:{}", e.getMessage(), e);
        }
    }

}
