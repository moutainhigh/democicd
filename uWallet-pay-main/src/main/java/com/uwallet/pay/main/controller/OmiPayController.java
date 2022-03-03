package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.util.MD5FY;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.AccountFlowDTO;
import com.uwallet.pay.main.model.dto.QrPayFlowDTO;
import com.uwallet.pay.main.model.dto.WithholdFlowDTO;
import com.uwallet.pay.main.model.entity.QrPayFlow;
import com.uwallet.pay.main.service.*;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author baixinyue
 */

@RestController
@RequestMapping("/omiPay")
@Slf4j
@Api("OmiPay应用Controller")
public class OmiPayController extends BaseController {

    @Value("${omipay.mNumber}")
    private String mNumber;

    @Value("${omipay.secretKey}")
    private String secretKey;

    @Autowired
    private OmiPayService omiPayService;

    @PassToken
    @PostMapping("/getPaidOrderInfo")
    public Object getPaidOrderInfo(@RequestBody JSONObject data, HttpServletRequest request) {
        log.info("omiPay getPaidOrderInfo ,data{}",data);
        Long timestamp = data.getLong("timestamp");
        String nonceStr = data.getString("nonce_str");
        String requestSign = data.getString("sign");
        //验证请求是否超时
        Long diff = System.currentTimeMillis() - timestamp;
        if (diff > Constant.OMI_PAY_HOLD_SECOND) {
            return new JSONObject().put("return_code", StaticDataEnum.OMI_PAY_FAIL.getMessage());
        }
        //验签
        String verifySign = MD5FY.MD5Encode(mNumber + "&" + timestamp + "&"
                + nonceStr + "&" + secretKey);
        if (!requestSign.equals(verifySign.toUpperCase())) {
            return new JSONObject().put("return_code", StaticDataEnum.OMI_PAY_FAIL.getMessage());
        }
        JSONObject result = new JSONObject();
        //获取信息进行业务处理
        try {
            omiPayService.getPaidOrderInfo(data, request);
        } catch (Exception e) {
            result.put("return_code", StaticDataEnum.OMI_PAY_FAIL.getMessage());
            return result;
        }
        result.put("return_code", StaticDataEnum.OMI_PAY_SUCCESS.getMessage());
        return result;
    }

}
