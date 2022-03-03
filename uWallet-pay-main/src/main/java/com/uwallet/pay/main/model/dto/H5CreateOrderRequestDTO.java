package com.uwallet.pay.main.model.dto;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

/**
 * <p>
 * 在商户终端创建未支付订单
 * </p>
 *
 * @description: 在商户终端创建未支付订单
 * @author: caisj
 * @date: Created in 2021-08-23 10:44:10
 */
@ApiModel("在商户终端创建未支付订单")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class H5CreateOrderRequestDTO{

    private H5CreateTotalRequestAmountDTO total;

    private H5RequestMerchantDTO merchant;
}
