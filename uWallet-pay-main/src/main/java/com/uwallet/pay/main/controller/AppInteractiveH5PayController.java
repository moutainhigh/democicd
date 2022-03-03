package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.util.JwtUtils;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.filter.H5PayToken;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.service.ApiQrPayFlowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: caisj
 * @createDate: 2021/08/23
 * @description: H5专用交互商家
 */

@RestController
@Slf4j
@Api("对外开放接口--对接h5")
public class AppInteractiveH5PayController extends BaseController {

    @Autowired
    private RefundService refundService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ApiQrPayFlowService apiQrPayFlowService;
    @Autowired
    private ApiMerchantService apiMerchantService;

    @PassToken
    @ApiOperation(value = "生成商户请求Token", notes = "生成商户请求Token")
    @PostMapping("/merchant/token")
    public Object h5Login(@RequestBody JSONObject requestInfo, HttpServletRequest request,
                          HttpServletResponse response) {

        log.info("生成商户请求Token, data:{}", requestInfo);
        String result = null;
        Map<String, Object> codeMap = new HashMap<>(16);
        try {
            String subject = requestInfo.getString("key");
            String pwp = requestInfo.getString("secret");
            if(null == subject || null == pwp){
                Map<String, Object> codeMaps = new HashMap<>(16);
                codeMaps.put("code", "404");
                codeMaps.put("message", "The parameter cannot be null");
                response.reset();
                return ResponseEntity.status(401).body(codeMaps);
            }
            Map<String, Object> keys = new HashMap<>(16);
            keys.put("key", subject);
            keys.put("secret", pwp);
            ApiMerchantDTO apiMerchantDTOKey = apiMerchantService.findOneApiMerchant(keys);
            if(null == apiMerchantDTOKey.getId()){
                Map<String, Object> codeMaps = new HashMap<>(16);
                codeMaps.put("code", "404");
                codeMaps.put("message", "The key value is wrong or The secret value is wrong");
                response.reset();
                return ResponseEntity.status(401).body(codeMaps);
            }
            ApiMerchantDTO apiMerchantDTO = apiMerchantService.findApiMerchantById(apiMerchantDTOKey.getId());
            if(null == apiMerchantDTO){
                Map<String, Object> codeMaps = new HashMap<>(16);
                codeMaps.put("code", "404");
                codeMaps.put("message", "Merchant does not exist");
                response.reset();
                return ResponseEntity.status(401).body(codeMaps);
            }
            Long subjects = apiMerchantDTOKey.getId();
            //result="Bearer " + JwtUtils.h5PayQuestToken(subject, pwp, redisUtils);
            result=JwtUtils.h5PayQuestToken(String.valueOf(subjects), pwp, redisUtils);
            codeMap.put("token", result);
            codeMap.put("type", "Bearer");
            String tokens = result.trim();
            DecodedJWT verifyCode = JwtUtils.verifyCode(tokens);
            Date date = verifyCode.getExpiresAt();
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String dd = simpleDateFormat.format(date);
            codeMap.put("expires", dd);
        } catch (Exception e) {
            log.error("生成商户请求Token,error message:{},e:{}",e.getMessage(),e);
            Map<String, Object> codeMaps = new HashMap<>(16);
            codeMaps.put("code", "404");
            codeMaps.put("message", "payment not found");
            response.reset();
            return ResponseEntity.status(401).body(codeMaps);
        }
        response.reset();
        return ResponseEntity.ok().body(codeMap);
    }

    @H5PayToken
    @ApiOperation(value = "商户查询支付详情列表", notes = "商户查询支付详情列表")
    @GetMapping("/payments")
    public Object getPayments(@RequestParam(value="from",required = false) String from,
                              @RequestParam(value="max",required = false) Integer max,
                              @RequestParam(value="reference",required = false) String reference,
                              @RequestParam(value="to",required = false) String to,
                              @RequestParam(value="after",required = false) String after,
                              HttpServletRequest request,HttpServletResponse response) {
        log.info("商户查询支付详情列表, data:{}", from+","+max+","+reference+","+to);
        Object result = null;
        try {
            result = apiQrPayFlowService.getPayments(from, max, reference, to, after,request);
        } catch (Exception e) {
            log.error("商户查询支付详情列表,error message:{},e:{}",e.getMessage(),e);
            response.reset();
            return ResponseEntity.notFound().build();
        }
        response.reset();
        return ResponseEntity.ok().body(result);
    }
    @H5PayToken
    @ApiOperation(value = "通过ID查询付款数据列表", notes = "通过ID查询付款数据列表")
    @GetMapping("/payments/{id}")
    public Object getPaymentsId(@PathVariable(value="id") Long id,HttpServletRequest request,HttpServletResponse response) {
        String requestUrl = "https" //当前链接使用的协议
                +"://" + request.getServerName()//服务器地址
                + request.getContextPath() //应用名称，如果应用名称为
                + request.getServletPath(); //请求的相对url
        String uri = "https"+"://" + request.getServerName()+ request.getContextPath();
        log.info("通过ID查询付款数据列表, data:{}", id);
        Map<String, Object> result = new HashMap<>(16);
        try {
            ApiQrPayFlowDTO str = apiQrPayFlowService.paymentsId(id);
            result.put("$self", requestUrl);
            result.put("$type", "payment");
            result.put("id", str.getId().toString());
            SimpleDateFormat simp=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            result.put("created", simp.format(str.getCreatedDate()));
            Map<String, Object> param = new HashMap<>(2);
            param.put("reference", str.getApiTransNo());
            result.put("merchant", param);
            Map<String, Object> original = new HashMap<>(2);
            if( null == str.getPayAmount() ){
                original.put("amount", "");
            } else {
                original.put("amount", (str.getPayAmount().multiply(new BigDecimal("100"))).stripTrailingZeros().toPlainString());
            }
            original.put("currency", str.getCurrencyType());
            Map<String, Object> refunded = new HashMap<>(2);
            if( null == str.getRefundAmount() ){
                refunded.put("amount", "");
            } else {
                refunded.put("amount", (str.getRefundAmount().multiply(new BigDecimal("100"))).stripTrailingZeros().toPlainString());

            }
            refunded.put("currency", str.getCurrencyType());
            Map<String, Object> current = new HashMap<>(2);
            if(null != str.getPayAmount() && null !=str.getRefundAmount()){
                BigDecimal currentAmount = str.getPayAmount().subtract(str.getRefundAmount());
                current.put("amount", (currentAmount.multiply(new BigDecimal("100"))).stripTrailingZeros().toPlainString());
            }else {
                current.put("amount", "");
            }
            current.put("currency", str.getCurrencyType());

            result.put("original", original);
            result.put("refunded", refunded);
            result.put("current", current);
            Map<String, Object> param3 = new HashMap<>(2);
            param3.put("id", str.getId().toString());
            param3.put("uri", uri+"/refunds/"+str.getId().toString());
            result.put("refunds", param3);
        } catch (Exception e) {
            log.error("通过ID查询付款数据列表,error message:{},e:{}",e.getMessage(),e);
            Map<String, Object> codeMap = new HashMap<>(16);
            codeMap.put("code", "404");
            codeMap.put("message", "payment not found");
            response.reset();
            return ResponseEntity.status(404).body(codeMap);
            //return R.h5Fail(ErrorCodeEnum.H5_CODE_DOUBT_404.getCode(), "Payment Not Found");
        }
        response.reset();
        return ResponseEntity.ok().body(result);
    }

    @H5PayToken
    @ApiOperation(value = "在商户终端创建未支付订单", notes = "在商户终端创建未支付订单")
    @PostMapping("/orders")
    public Object postOrdersPay(@RequestBody  H5CreateOrderRequestDTO h5CreateOrderRequestDTO, HttpServletRequest request,HttpServletResponse response) {
        log.info("在商户终端创建未支付订单, data:{}", h5CreateOrderRequestDTO);
        JSONObject jSONObject = null;
        Map<String, Object> codeMap = new HashMap<>(16);
        try {
            if(null == h5CreateOrderRequestDTO.getTotal().getAmount()
                || null == h5CreateOrderRequestDTO.getTotal().getCurrency()
                || null == h5CreateOrderRequestDTO.getMerchant().getCancellation_url()
                || null == h5CreateOrderRequestDTO.getMerchant().getConfirmation_url())
            {
                codeMap.put("code", "404");
                codeMap.put("message", "The parameter cannot be null");
                response.reset();
                return ResponseEntity.badRequest().body(codeMap);
            }
            if(h5CreateOrderRequestDTO.getTotal().getAmount().equals("")
               || h5CreateOrderRequestDTO.getTotal().getCurrency().equals("")
               || h5CreateOrderRequestDTO.getMerchant().getCancellation_url().equals("")
               || h5CreateOrderRequestDTO.getMerchant().getConfirmation_url().equals(""))
            {
                codeMap.put("code", "404");
                codeMap.put("message", "The parameter cannot be null");
                response.reset();
                return ResponseEntity.badRequest().body(codeMap);
            }
            String con = h5CreateOrderRequestDTO.getTotal().getAmount().toString();
            if(con.contains(".")){
                codeMap.put("code", "404");
                codeMap.put("message", "Parameters are not");
                response.reset();
                return ResponseEntity.badRequest().body(codeMap);
            }
            if(Long.valueOf(con) < 1000){
                codeMap.put("code", "404");
                codeMap.put("message", "You can only pay in instalments for transactions over $10");
                response.reset();
                return ResponseEntity.badRequest().body(codeMap);
            }
            String authHeader = request.getHeader("Authorization");
            String tokens = authHeader.replaceFirst("Bearer ", "").trim();
            DecodedJWT verifyCode = JwtUtils.verifyCode(tokens);
            String apiMerchantId = verifyCode.getSubject();
            ApiMerchantDTO apiMerchantDTO = apiMerchantService.findApiMerchantById(Long.valueOf(apiMerchantId));

            if (0 == apiMerchantDTO.getIsAvailable()){
                log.error("u_api_merchant表，商户状态不可用");
                codeMap.put("code", "404");
                codeMap.put("message", "Merchant unavailable");
                response.reset();
                return ResponseEntity.badRequest().body(codeMap);
            }
            jSONObject = apiQrPayFlowService.insertOrders(h5CreateOrderRequestDTO,request);
        } catch (Exception e) {
            response.reset();
            log.error("在商户终端创建未支付订单,error message:{},e:{}",e.getMessage(),e);
            if("100404".equals(e.getMessage())){
                codeMap.put("code", "idempotency_key_required");
                codeMap.put("message", "idempotency key is required for mutating request");
                response.reset();
                return ResponseEntity.badRequest().body(codeMap);
            }
            if("100403".equals(e.getMessage())){
                codeMap.put("code", "forbidden");
                response.reset();
                return ResponseEntity.status(403).body(codeMap);
            }

            codeMap.put("code", "404");
            codeMap.put("message", e.getMessage());
            response.reset();
            return ResponseEntity.badRequest().body(codeMap);
        }
        String requestUrl = "https"+"://" + request.getServerName()+ request.getContextPath()+ request.getServletPath();
        URI location = null;
        try {
            location = new URI(requestUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        response.reset();
        return ResponseEntity.created(location).body(jSONObject);
    }

    @H5PayToken
    @ApiOperation(value = "通过订单ID获取订单详情", notes = "通过订单ID获取订单详情")
    @GetMapping("/orders/{id}")
    public Object postOrdersPay(@PathVariable("id") Long id, HttpServletRequest request,HttpServletResponse response) {

        String requestUrl = "https"+"://" + request.getServerName()+ request.getContextPath()+ request.getServletPath();
        String AUTHORISE = "https"+"://" + request.getServerName()+"/#/login?p=";
        log.info("在商户终端创建未支付订单, data:{}", id);
        JSONObject params = new JSONObject(16);
        try {
            ApiQrPayFlowDTO apiQrPayFlowDTO = apiQrPayFlowService.findApiQrPayFlowById(id);
            Map<String, Object> amount = new HashMap<>(2);
            amount.put("amount", (apiQrPayFlowDTO.getTransAmount().multiply(new BigDecimal("100"))).stripTrailingZeros().toPlainString());
            amount.put("currency", apiQrPayFlowDTO.getCurrencyType());
            Map<String, Object> merchant = new HashMap<>(3);
            merchant.put("reference", apiQrPayFlowDTO.getApiTransNo());
            merchant.put("confirmation_url", apiQrPayFlowDTO.getConfirmationUrl());
            merchant.put("cancellation_url", apiQrPayFlowDTO.getCancellationUrl());
            Map<String, Object> status = new HashMap<>(2);
            if(apiQrPayFlowDTO.getOrderStatus() == StaticDataEnum.H5_ORDER_TYPE_0.getCode()){
                status.put("status", StaticDataEnum.H5_ORDER_TYPE_0.getMessage());
            }
            if(apiQrPayFlowDTO.getOrderStatus() == StaticDataEnum.H5_ORDER_TYPE_1.getCode()){
                status.put("status", StaticDataEnum.H5_ORDER_TYPE_1.getMessage());
            }
            if(apiQrPayFlowDTO.getOrderStatus() == StaticDataEnum.H5_ORDER_TYPE_2.getCode() || apiQrPayFlowDTO.getOrderStatus() == StaticDataEnum.H5_ORDER_TYPE_3.getCode()){
                status.put("status", StaticDataEnum.H5_ORDER_TYPE_2.getMessage());
            }
            status.put("authorise", AUTHORISE+apiQrPayFlowDTO.getId());
            params.put("$self",requestUrl);
            params.put("$type", apiQrPayFlowDTO.getTypeOrder());
            params.put("id", id.toString());
            params.put("total", amount);
            params.put("merchant", merchant);
            params.put("status", status);
        } catch (Exception e) {
            log.error("在商户终端创建未支付订单,error message:{},e:{}",e.getMessage(),e);
            response.reset();
            return ResponseEntity.notFound().build();
            //return R.h5Fail(ErrorCodeEnum.H5_CODE_FAIL.getCode(), e.getMessage());
        }
        response.reset();
        return ResponseEntity.ok().body(params);
    }

//    @H5PayToken
    @PassToken
    @ApiOperation(value = "商户发起支付", notes = "商户发起支付")
    @PostMapping("/payments")
    public Object payments(@RequestBody H5PaymentsRequestDTO h5PaymentsRequestDTO, HttpServletRequest request,HttpServletResponse response) {
        log.info("商户发起支付, data:{}", h5PaymentsRequestDTO);
        Object params = null;
        Map<String, Object> codeMap = new HashMap<>(16);
        try {
            ApiQrPayFlowDTO api = apiQrPayFlowService.findApiQrPayFlowById(h5PaymentsRequestDTO.getOrder());
            //判断订单未确认
            if(api.getOrderStatus() == StaticDataEnum.H5_ORDER_TYPE_0.getCode()){
                codeMap.put("code", "404");
                codeMap.put("message", "The order is not confirmed");
                response.reset();
                return ResponseEntity.badRequest().body(codeMap);
            }
            //判断订单状态已过期
            if(api.getOrderStatus() == StaticDataEnum.H5_ORDER_TYPE_2.getCode() || api.getOrderStatus() == StaticDataEnum.H5_ORDER_TYPE_3.getCode()){
                codeMap.put("code", "404");
                codeMap.put("message", "The order closed");
                response.reset();
                return ResponseEntity.badRequest().body(codeMap);
            }
            //判断订单是否过期
            Long exps= api.getExpirationTime();
            Long now = System.currentTimeMillis();
            Boolean expDate = exps.longValue() < now.longValue();
            if(expDate){
                ApiQrPayFlowDTO dto = new ApiQrPayFlowDTO();
                dto.setOrderStatus(StaticDataEnum.H5_ORDER_TYPE_2.getCode());
                apiQrPayFlowService.updateApiQrPayFlow(api.getId(), dto, request);
                codeMap.put("code", "404");
                codeMap.put("message", "The order closed");
                response.reset();
                return ResponseEntity.badRequest().body(codeMap);
            }else{
                log.info("判断订单未确认and判断订单状态已过期and判断订单是否过期===success, data:{}",h5PaymentsRequestDTO);
                params = apiQrPayFlowService.payments(h5PaymentsRequestDTO, request);
            }

        } catch (Exception e) {
            log.error("商户发起支付,error message:{},e:{}",e.getMessage(),e);
            if(null == e.getMessage()){
                codeMap.put("code", "404");
                codeMap.put("message", "payment not found");
                response.reset();
                return ResponseEntity.badRequest().body(codeMap);
            }
            codeMap.put("code", "404");
            codeMap.put("message", e.getMessage());
            response.reset();
            return ResponseEntity.badRequest().body(codeMap);
        }
        String requestUrl = "https"+"://" + request.getServerName()+ request.getContextPath()+ request.getServletPath();
        URI location = null;
        try {
            location = new URI(requestUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        response.reset();
        return ResponseEntity.created(location).body(params);

    }

    @H5PayToken
    @ApiOperation(value = "退款", notes = "退款")
    @PostMapping("/refunds")
    public Object refunds(@RequestBody H5RefundsRequestDTO requestInfo, HttpServletRequest request,HttpServletResponse response) {
        log.info("h5 refunds, data:{}", requestInfo);
        JSONObject result = new JSONObject();
        try {
            result = refundService.h5Refund(requestInfo,request);
        } catch (Exception e) {
            log.info("refunds h5 exception ,message:" + e.getMessage(),e);
            if(null == e.getMessage()){
                result.put("code", "400");
                result.put("message", "refunds error");
                response.reset();
                return ResponseEntity.badRequest().body(result);
            }
            result.put("code", "400");
            result.put("message", e.getMessage());
            response.reset();
            return ResponseEntity.badRequest().body(result);
        }
        String requestUrl = "https"+"://" + request.getServerName()+ request.getContextPath()+ request.getServletPath();
        URI location = null;
        try {
            location = new URI(requestUrl);
        } catch (URISyntaxException e) {
            log.error("refunds error ,message:"+e.getMessage(),e);
        }
        response.reset();
        return ResponseEntity.created(location).body(result);
    }

    @H5PayToken
    @ApiOperation(value = "退款交易查询", notes = "退款交易查询")
    @GetMapping("/refunds/{id}")
    public Object getPayments(@PathVariable("id") String id, HttpServletRequest request,HttpServletResponse response) {
        log.info("退款交易查询, id:"+ id);

        JSONObject result = new JSONObject(16);
        try {
            result = refundService.getPayments(id , request);

        } catch (Exception e) {
            log.error("退款交易查询 ,error message:{},e:{}",e.getMessage(),e);
            if(null == e.getMessage()){
                result.put("code", "404");
                result.put("message", "other error");
                response.reset();
                return ResponseEntity.badRequest().body(result);
            }
            result.put("code", "404");
            result.put("message", e.getMessage());
            response.reset();
            return ResponseEntity.badRequest().body(result);
        }
        response.reset();
        return ResponseEntity.ok().body(result);


//        JSONObject result = new JSONObject();
//        try {
//            result = refundService.getPayments(id , request);
//        } catch (Exception e) {
//            log.error("退款交易查询,error message:{},e:{}",e.getMessage(),e);
//            //return R.h5Fail(ErrorCodeEnum.H5_CODE_FAIL.getCode(), e.getMessage());
//            response.reset();
//            return ResponseEntity.notFound().build();
//        }
//        response.reset();
//        return ResponseEntity.ok().body(result);
    }


}
