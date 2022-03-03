package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.gson.Gson;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.service.SplitService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author baixinyue
 * @createdDate 2020/11/18
 * @description split回调通知接口
 */

@RestController
@RequestMapping("/split")
@Slf4j
@Api("Split应用Controller")
public class SplitCallBackController {

    @Autowired
    private SplitService splitService;

    @Value("${split.secret}")
    private String secret;

    private static final Long TIME_DIFF = 3000L;

    @PostMapping("/notify")
    @PassToken
    public Object directDebitNotify(@RequestBody Object requestInfo, HttpServletRequest request) throws Exception {
        String requestData = JSONObject.toJSONString(requestInfo, SerializerFeature.WriteMapNullValue);
        log.info("split call back, result:{}", requestData);
        try {
            String splitNature = request.getHeader("Split-Signature");
            checkSign(splitNature, requestData);
            splitService.splitTransactionNotify(JSONObject.parseObject(requestData), request);
        } catch (Exception e) {
            log.info("split call back failed, data:{} error message:{}, e:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    /**
     * 验签校验
     * @param splitNature
     * @param requestInfo
     * @throws Exception
     */
    private void checkSign(String splitNature, String requestInfo) throws Exception {
        String[] givenSignData = splitNature.split("\\.");
        String timestamp = givenSignData[0];
        String givenSign = givenSignData[1];
        String signedPayload = timestamp + "." + requestInfo;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        String expectedSignature = javax.xml.bind.DatatypeConverter.printHexBinary(sha256_HMAC.doFinal(signedPayload.getBytes())).toLowerCase();
        if (!givenSign.equals(expectedSignature)) {
            throw new BizException("Sign nature check failed");
        }
    }

}
