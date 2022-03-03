package com.uwallet.pay.main.model.dto;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

/**
 * <p>
 * 商户发起支付
 * </p>
 *
 * @description: 商户发起支付
 * @author: caisj
 * @date: Created in 2021-08-23 15:44:10
 */
@ApiModel("商户发起支付")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class H5PaymentsRequestDTO {

    private Long order;

    private H5RequestAmountDTO total;

}
