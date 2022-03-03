package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 订单报告
 * </p>
 *
 * @description: 订单报告
 * @author: zhuxk
 * @date: Created in 2021-01-20 10:55:08
 */
@ApiModel("订单报告")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class OrderReportDTO implements Serializable {

    /**
     * 商户id
     */
    @ApiModelProperty(value = "商户id")
    private String merchantId;

    /**
     * 商户名称
     */
    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    /**
     * 收款方userId
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "收款方userId")
    private Long recUserId;

    /**
     * 正常模式订单总数量（卡支付加分期支付）
     */
    @ApiModelProperty(value = "正常模式订单总数量（卡支付加分期支付）")
    private Integer basicSalesOrderQuantity;

    /**
     * 正常模式订单总金额（卡支付加分期支付）
     */
    @ApiModelProperty(value = "正常模式订单总金额（卡支付加分期支付）")
    private BigDecimal basicSalesOrderAmount;

    /**
     * 整体出售订单数（卡支付加分期支付）
     */
    @ApiModelProperty(value = "整体出售订单数（卡支付加分期支付）")
    private Integer wholeSalesOrderQuantity;

    /**
     * 整体出售订单总金额（卡支付加分期支付）
     */
    @ApiModelProperty(value = "整体出售订单总金额（卡支付加分期支付）")
    private BigDecimal wholeSalesOrderAmount;

    /**
     * 混合业务订单数（卡支付加分期支付）
     */
    @ApiModelProperty(value = "混合业务订单数（卡支付加分期支付）")
    private Integer mixSalesOrderQuantity;

    /**
     * 混合业务订单总金额（卡支付加分期支付）
     */
    @ApiModelProperty(value = "混合业务订单总金额（卡支付加分期支付）")
    private BigDecimal mixSalesOrderAmount;

    /**
     * 用户在统计的这些订单里使用的所有红包的金额
     */
    @ApiModelProperty(value = "用户在统计的这些订单里使用的所有红包的金额")
    private BigDecimal redEnvelopeAmount;

    //----------------------------------------------------------------------------------------

    /**
     * 当前日期下正常业务的卡支付订单金额
     */
    @ApiModelProperty(value = "当前日期下正常业务的卡支付订单金额")
    private BigDecimal basicSalesOrderAmountByCard;

    /**
     * 当前日期下正常业务的卡支付订单的客户付款金额
     */
    @ApiModelProperty(value = "当前日期下正常业务的卡支付订单的客户付款金额")
    private BigDecimal basicSalesCustomerActualPaymentAmountByCard;

    /**
     * 当前日期下正常业务的卡支付订单需结算商户金额
     */
    @ApiModelProperty(value = "当前日期下正常业务的卡支付订单需结算商户金额")
    private BigDecimal basicSalesNeedSettledAmountByCard;

    /**
     * 当前日期下正常业务的分期付订单金额
     */
    @ApiModelProperty(value = "当前日期下正常业务的分期付订单金额")
    private BigDecimal basicSalesOrderAmountByInstalment;

    /**
     * 当前日期下正常业务的分期付订单的客户付款金额
     */
    @ApiModelProperty(value = "当前日期下正常业务的分期付订单的客户付款金额")
    private BigDecimal basicSalesCustomerActualPaymentAmountByInstalment;

    /**
     * 当前日期下正常业务的分期付订单需结算商户金额
     */
    @ApiModelProperty(value = "当前日期下正常业务的分期付订单需结算商户金额")
    private BigDecimal basicSalesNeedSettledAmountByInstalment;

    // ---------------------------------------------------------------------------------------

    /**
     * 整体出售卡支付的订单金额
     */
    @ApiModelProperty(value = "整体出售卡支付的订单金额")
    private BigDecimal wholeSalesOrderAmountByCard;

    /**
     * 整体出售卡支付订单的用户实付金额
     */
    @ApiModelProperty(value = "整体出售卡支付订单的用户实付金额")
    private BigDecimal wholeSalesCustomerActualPaymentAmountByCard;

    /**
     * 整体出售分期付的订单金额
     */
    @ApiModelProperty(value = "整体出售分期付的订单金额")
    private BigDecimal wholeSalesOrderAmountByInstalment;

    /**
     * 整体出售分期付用户实际分期金额
     */
    @ApiModelProperty(value = "整体出售分期付用户实际分期金额")
    private BigDecimal wholeSalesCustomerActualPaymentAmountByInstalment;

    // ------------------------------------------------------------------------------------

    /**
     * 当前日期下混合业务的卡支付订单金额
     */
    @ApiModelProperty(value = "当前日期下混合业务的卡支付订单金额")
    private BigDecimal mixSalesOrderAmountByCard;

    /**
     * 当前日期下混合业务的卡支付订单的客户付款金额
     */
    @ApiModelProperty(value = "当前日期下混合业务的卡支付订单的客户付款金额")
    private BigDecimal mixSalesCustomerActualPaymentAmountByCard;

    /**
     * 当前日期下混合业务的卡支付订单需结算商户金额
     */
    @ApiModelProperty(value = "当前日期下混合业务的卡支付订单需结算商户金额")
    private BigDecimal mixSalesNeedSettledAmountByCard;

    /**
     * 当前日期下混合业务的分期付订单金额
     */
    @ApiModelProperty(value = "当前日期下混合业务的分期付订单金额")
    private BigDecimal mixSalesOrderAmountByInstalment;

    /**
     * 当前日期下混合业务的分期付订单的客户付款金额
     */
    @ApiModelProperty(value = "当前日期下混合业务的分期付订单的客户付款金额")
    private BigDecimal mixSalesCustomerActualPaymentAmountByInstalment;

    /**
     * 当前日期下混合业务的分期付订单需结算商户金额
     */
    @ApiModelProperty(value = "当前日期下混合业务的分期付订单需结算商户金额")
    private BigDecimal mixSalesNeedSettledAmountByInstalment;

    // ---------------------------------------------------------------------------------------

    /**
     * 该商户已核准的整体出售额度
     */
    @ApiModelProperty(value = "该商户已核准的整体出售额度")
    private BigDecimal merchantTotalWholeSalesAmount = new BigDecimal("0");

    /**
     * 该商户剩余整体出售额度
     */
    @ApiModelProperty(value = "该商户剩余整体出售额度")
    private BigDecimal merchantRemainingWholeSalesAmount = new BigDecimal("0");

    /**
     * 今日商户整体出售转让份额
     */
    @ApiModelProperty(value = "今日商户整体出售转让份额")
    private BigDecimal todayMerchantWholeSalesAmount;

    private Integer city;
}
