package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 用户详情支付订单DTO
 * </p>
 *
 * @description: 用户详情支付订单DTO
 * @author: zhangzeyuan
 * @date: Created in 2021年9月14日13:59:34
 */
@ApiModel("用户详情支付订单DTO")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailPayOrderDTO extends BaseDTO implements Serializable {


    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    private String transNo;

    /**
     * 商户名
     */
    @ApiModelProperty(value = "商户名")
    private String merchantName;

    /**
     * 订单金额
     */
    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;


    /**
     * 折扣金额
     */
    @ApiModelProperty(value = "折扣金额")
    private BigDecimal discountAmount;


    /**
     * 整体出售折扣率
     */
    @ApiModelProperty(value = "整体出售折扣率")
    private BigDecimal wholeSalesDiscount;

    /**
     * 基础折扣率
     */
    @ApiModelProperty(value = "基础折扣率")
    private BigDecimal baseDiscount;


    /**
     * 额外折扣率
     */
    @ApiModelProperty(value = "额外折扣率")
    private BigDecimal extraDiscount;


    /**
     * 营销折扣率
     */
    @ApiModelProperty(value = "营销折扣率")
    private BigDecimal markingDiscount;

    /**
     * 红包使用金额
     */
    @ApiModelProperty(value = "红包使用金额")
    private BigDecimal payoMoney;

    /**
     * total Order Amount
     */
    @ApiModelProperty(value = "totalOrderAmount")
    private BigDecimal totalOrderAmount;


    /**
     * 捐赠金额
     */
    @ApiModelProperty(value = "捐赠金额")
    private BigDecimal donationAmount;

    /**
     * 小费金额
     */
    @ApiModelProperty(value = "小费金额")
    private BigDecimal tipAmount;


    /**
     * 交易手续费
     */
    @ApiModelProperty(value = "交易手续费")
    private BigDecimal fee;


    /**
     * 交易手续费率
     */
    @ApiModelProperty(value = "交易手续费率")
    private BigDecimal rate;

    /**
     * total Amount
     */
    @ApiModelProperty(value = "total Amount")
    private BigDecimal totalAmount;


    /**
     * 支付状态
     */
    @ApiModelProperty(value = "支付状态")
    private Integer state;

    /**
     * 卡ID
     */
    @ApiModelProperty(value = "卡ID")
    private String cardId;


    /**
     * 支付时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    @ApiModelProperty(value = "支付时间")
    private Long paidTime;


    /**
     * 卡号
     */
    @ApiModelProperty(value = "卡号")
    private String cardNo;


    /**
     * 卡类型
     */
    @ApiModelProperty(value = "卡类型")
    private String cardCcType;


    /**
     * 错误原因
     */
    @ApiModelProperty(value = "错误原因")
    private String errorMessage;

    /**
     * 分期付 total order amount
     */
    @ApiModelProperty(value = "分期付 total order amount")
    private BigDecimal creditTotalAmt;

    /**
     * 卡支付 total order amount
     */
    @ApiModelProperty(value = "卡支付 total order amount")
    private BigDecimal cardTotalAmt;

    /**
     * Transaction fee（首期）
     */
    @ApiModelProperty(value = "Transaction fee（首期）")
    private BigDecimal creditFirstPayAmt;

    /**
     * Total amount(首期)
     */
    @ApiModelProperty(value = "Total amount(首期)")
    private BigDecimal firstPeriodAmt;

    /**
     * 总期数
     */
    @ApiModelProperty(value = "总期数")
    private Integer allPeriod;



    /**
     * 借款ID
     */
    @ApiModelProperty(value = "借款ID")
    private String borrowId;



    /**
     * 订单类型 2 卡支付  22分期付
     */
    @ApiModelProperty(value = "订单类型 2 卡支付  22分期付")
    private Integer transType;

    @ApiModelProperty(value = "订单来源 0 APP  1 API 3 POS")
        private Integer orderSource;
    /**
     * 整体折扣金额
     * */
    private BigDecimal wholeDiscountAmount;
    /**
     * 正常折扣金额
     * */
    private BigDecimal normalDiscountAmount;
}
