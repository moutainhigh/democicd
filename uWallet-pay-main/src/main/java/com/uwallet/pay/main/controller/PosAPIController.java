package com.uwallet.pay.main.controller;

/**
 * @author: zhangzeyuan
 * @Date: 2021/3/19 10:47
 * @Description: POS支付API
 */

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PosApiResponse;
import com.uwallet.pay.main.exception.PosApiException;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.service.MerchantService;
import com.uwallet.pay.main.service.PosApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * POS支付API
 * </p>
 *
 * @package: com.uwallet.pay.main.controller
 * @description: POS支付API
 * @author: zhangzeyuan
 * @date: Created in 2021-03-19 10:48:56
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */

@RestController
@RequestMapping("/pos/v1/")
@Slf4j
public class PosAPIController {

    /**
     * pos  API service
     */
    @Resource
    private PosApiService posApiService;

    /**
     * 商户service
     */
    @Resource
    private MerchantService merchantService;

    /**
     * 生成二维码接口
     *
     * @param requestInfo
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/3/24 11:04
     */
    @PassToken
    @PostMapping("/createQrCode")
    public Object createPosQrCode(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("POS 生成二维码接口,data:{}", requestInfo);
        try {
            return PosApiResponse.success(posApiService.createPosQrCode(requestInfo, request));
        } catch (PosApiException e) {
            log.error("POS 生成二维码接口异常, data: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return PosApiResponse.fail(e.getCode(), e.getMessage());
        }
    }


    /**
     * 根据ABN信息获取商户列表
     *
     * @param requestInfo
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/3/23 13:26
     */
    @PassToken
    @PostMapping("/queryMerchant")
    public Object getMerchantDataByABN(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("POS 根据ABN信息获取商户列表,data:{}", requestInfo);
        try {
            return PosApiResponse.success(merchantService.getMerchantListByABN(requestInfo));
        } catch (PosApiException e) {
            log.error("POS 根据ABN信息获取商户列表异常, data: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return PosApiResponse.fail(e.getCode(), e.getMessage());
        }
    }


    /**
     * pos获取订单记录
     *
     * @param requestInfo
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/3/23 14:29
     */
    @PassToken
    @PostMapping("/queryOrder")
    public Object getPosOrderTransactionRecord(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        //参数 分页page limit  //时间区间  startDate  endDate //订单状态 orderStatus
        log.info("查询POS订单记录信息,data:{}", requestInfo);
        try {
            return PosApiResponse.success(posApiService.getPosTransactionRecord(requestInfo, request));
        } catch (PosApiException e) {
            log.error("查询POS订单记录信息出错，  data: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return PosApiResponse.fail(e.getCode(), e.getMessage());
        }
    }


    @PassToken
    @PostMapping("/notifyTest")
    public Object nofityTest(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        //参数 分页page limit  //时间区间  startDate  endDate //订单状态 orderStatus
        log.info("POS通知测试,data:{}", requestInfo);
        return PosApiResponse.success("success");
    }

}
