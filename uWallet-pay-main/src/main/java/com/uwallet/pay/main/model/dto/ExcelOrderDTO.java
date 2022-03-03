package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
/**
 * <p>
 * 订单详情 excel 实体类
 * </p>
 *
 * @description: 订单详情 excel 实体类
 * @author: zhoutt
 * @date: Created in 2019-12-18 09:25:21
 */
@ApiModel("订单详情 excel 实体类")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExcelOrderDTO implements Serializable {

    /**
     * 商户id
     */
    @ApiModelProperty(value = "商户id")
    private String merchant_id;

    /**
     * 商户名称
     */
    @ApiModelProperty(value = "商户名称")
    private String merchant_name;

    private String city;
    /**
     * 收款方userId
     */
    @ApiModelProperty(value = "收款方userId")
    private String rec_user_id;

    /**
     * 正常模式订单总数量（卡支付加分期支付）
     */
    @ApiModelProperty(value = "正常模式订单总数量（卡支付加分期支付）")
    private String basic_sales_order_quantity;

    /**
     * 正常模式订单总金额（卡支付加分期支付）
     */
    @ApiModelProperty(value = "正常模式订单总金额（卡支付加分期支付）")
    private String basic_sales_order_amount;

    /**
     * 整体出售订单数（卡支付加分期支付）
     */
    @ApiModelProperty(value = "整体出售订单数（卡支付加分期支付）")
    private String whole_sales_order_quantity;

    /**
     * 整体出售订单总金额（卡支付加分期支付）
     */
    @ApiModelProperty(value = "整体出售订单总金额（卡支付加分期支付）")
    private String whole_sales_order_amount;

    /**
     * 混合业务订单数（卡支付加分期支付）
     */
    @ApiModelProperty(value = "混合业务订单数（卡支付加分期支付）")
    private String mix_sales_order_quantity;

    /**
     * 混合业务订单总金额（卡支付加分期支付）
     */
    @ApiModelProperty(value = "混合业务订单总金额（卡支付加分期支付）")
    private String mix_sales_order_amount;

    /**
     * 用户在统计的这些订单里使用的所有红包的金额
     */
    @ApiModelProperty(value = "用户在统计的这些订单里使用的所有红包的金额")
    private String red_envelope_amount;

    //----------------------------------------------------------------------------------------

    /**
     * 当前日期下正常业务的卡支付订单金额
     */
    @ApiModelProperty(value = "当前日期下正常业务的卡支付订单金额")
    private String basic_sales_order_amount_by_card;

    /**
     * 当前日期下正常业务的卡支付订单的客户付款金额
     */
    @ApiModelProperty(value = "当前日期下正常业务的卡支付订单的客户付款金额")
    private String basic_sales_customer_actual_payment_amount_by_card;

    /**
     * 当前日期下正常业务的卡支付订单需结算商户金额
     */
    @ApiModelProperty(value = "当前日期下正常业务的卡支付订单需结算商户金额")
    private String basic_sales_need_settled_amount_by_card;

    /**
     * 当前日期下正常业务的分期付订单金额
     */
    @ApiModelProperty(value = "当前日期下正常业务的分期付订单金额")
    private String basic_sales_order_amount_by_instalment;

    /**
     * 当前日期下正常业务的分期付订单的客户付款金额
     */
    @ApiModelProperty(value = "当前日期下正常业务的分期付订单的客户付款金额")
    private String basic_sales_customer_actual_payment_amount_by_instalment;

    /**
     * 当前日期下正常业务的分期付订单需结算商户金额
     */
    @ApiModelProperty(value = "当前日期下正常业务的分期付订单需结算商户金额")
    private String basic_sales_need_settled_amount_by_instalment;

    // ---------------------------------------------------------------------------------------

    /**
     * 整体出售卡支付的订单金额
     */
    @ApiModelProperty(value = "整体出售卡支付的订单金额")
    private String whole_sales_order_amount_by_card;

    /**
     * 整体出售卡支付订单的用户实付金额
     */
    @ApiModelProperty(value = "整体出售卡支付订单的用户实付金额")
    private String whole_sales_customer_actual_payment_amount_by_card;

    /**
     * 整体出售分期付的订单金额
     */
    @ApiModelProperty(value = "整体出售分期付的订单金额")
    private String whole_sales_order_amount_by_instalment;

    /**
     * 整体出售分期付用户实际分期金额
     */
    @ApiModelProperty(value = "整体出售分期付用户实际分期金额")
    private String whole_sales_customer_actual_payment_amount_by_instalment;

    // ------------------------------------------------------------------------------------

    /**
     * 当前日期下混合业务的卡支付订单金额
     */
    @ApiModelProperty(value = "当前日期下混合业务的卡支付订单金额")
    private String mix_sales_order_amount_by_card;

    /**
     * 当前日期下混合业务的卡支付订单的客户付款金额
     */
    @ApiModelProperty(value = "当前日期下混合业务的卡支付订单的客户付款金额")
    private String mix_sales_customer_actual_payment_amount_by_card;

    /**
     * 当前日期下混合业务的卡支付订单需结算商户金额
     */
    @ApiModelProperty(value = "当前日期下混合业务的卡支付订单需结算商户金额")
    private String mix_sales_needSettled_amount_by_card;

    /**
     * 当前日期下混合业务的分期付订单金额
     */
    @ApiModelProperty(value = "当前日期下混合业务的分期付订单金额")
    private String mix_sales_order_amount_by_instalment;

    /**
     * 当前日期下混合业务的分期付订单的客户付款金额
     */
    @ApiModelProperty(value = "当前日期下混合业务的分期付订单的客户付款金额")
    private String mix_sales_customer_actual_payment_amount_by_instalment;

    /**
     * 当前日期下混合业务的分期付订单需结算商户金额
     */
    @ApiModelProperty(value = "当前日期下混合业务的分期付订单需结算商户金额")
    private String mix_sales_need_settled_amount_by_instalment;

    // ---------------------------------------------------------------------------------------

    /**
     * 该商户已核准的整体出售额度
     */
    @ApiModelProperty(value = "该商户已核准的整体出售额度")
    private String merchant_total_whole_sales_amount = new String("0");

    /**
     * 该商户剩余整体出售额度
     */
    @ApiModelProperty(value = "该商户剩余整体出售额度")
    private String merchant_remaining_whole_sales_amount = new String("0");

    /**
     * 今日商户整体出售转让份额
     */
    @ApiModelProperty(value = "今日商户整体出售转让份额")
    private String today_merchant_whole_sales_amount;

}
