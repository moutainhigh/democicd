package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>
 * 扫码支付
 * </p>
 *
 * @description: 卡支付订单
 * @author: zhoutt
 * @date: Created in 2019-12-18 09:25:21
 */
@ApiModel("卡支付订单")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExcelCardPayDTO extends BaseDTO implements Serializable {
    @ApiModelProperty(value = "订单号")
    private String order_no;
    @ApiModelProperty(value = "customer_no ")
    private String customer_no;
    @ApiModelProperty(value = " merchant_trading_name")
    private String merchant_trading_name;
    @ApiModelProperty(value = " order_amount")
    private String order_amount;
    @ApiModelProperty(value = " ")
    private String actual_payment;
    @ApiModelProperty(value = " ")
    private String discount_amount;
    @ApiModelProperty(value = " ")
    private String pocket_money;
    @ApiModelProperty(value = " ")
    private String service_fee;
    @ApiModelProperty(value = " ")
    private String merchant_settlement;
    @ApiModelProperty(value = " ")
    private String order_time;
    //
    @ApiModelProperty(value = " ")
    private String clear_time;
    @ApiModelProperty(value = " ")
    private String business_type;
    @ApiModelProperty(value = " ")
    private String payment_method;
    @ApiModelProperty(value = " ")
    private String need_clear_to_merchant;
    @ApiModelProperty(value = " ")
    private String payment_channel;
    @ApiModelProperty(value = " ")
    private String reconciliation_status;
    @ApiModelProperty(value = " ")
    private String reconciliation_time;
}
