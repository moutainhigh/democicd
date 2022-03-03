package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
 * app 交易明细
 * </p>
 *
 * @description: app 交易明细
 * @author: baixinyue
 * @date: Created in 2019-12-19 11:28:23
 */
@ApiModel("app 交易明细")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppTransactionDetailsDTO extends BaseDTO implements Serializable {

    /**
     * 交易类型
     */
    @ApiModelProperty(value = "交易类型")
    private Integer transType;
    /**
     * 交易金额
     */
    @ApiModelProperty(value = "交易金额")
    private BigDecimal transAmount;
    /**
     * 手续费金额
     */
    @ApiModelProperty(value = "手续费金额")
    private BigDecimal fee;
    /**
     * 手续费率
     */
    @ApiModelProperty(value = "手续费率")
    private BigDecimal rate;
    /**
     * 实付金额
     */
    @ApiModelProperty(value = "实付金额")
    private BigDecimal payAmount;

    /**
     * 用户付款方式类型 (0:银行卡支付 1:钱包余额)
     */
    @ApiModelProperty(value = "用户付款方式类型 (0:银行卡支付 1:钱包余额)")
    private Integer payType;

    /**
     * 用户交易类型 (0:充值 1:支付 2:收款)
     */
    @ApiModelProperty(value = "用户交易类型 (0:充值 1:支付 2:收款)")
    private Integer showType;

    /**
     * 商户图片
     */
    @ApiModelProperty(value = "商户图片")
    private String logoUrl;
    /**
     * 商户名称
     */
    @ApiModelProperty(value = "商户名称")
    private String tradingName;
    /**
     * 交易状态
     */
    @ApiModelProperty(value = "交易状态")
    private String transState;
    /**
     * 交易类型-文字
     */
    @ApiModelProperty(value = "交易类型-文字")
    private String transTypeStr;
    /**
     * 交易状态-文字
     */
    @ApiModelProperty(value = "交易状态-文字")
    private String transStateStr;
    /**
     * 分期付订单id
     */
    @ApiModelProperty(value = "分期付订单id")
    private String creditOrderNo;
    /**
     * 交易日期
     */
    @ApiModelProperty(value = "交易日期")
    private String monthYear;
    /**
     * 交易日期, mm-yy
     */
    @ApiModelProperty(value = "交易日期, mm-yy")
    private String monthYearStr;
    //
    /**
     * 展示用交易日期
     */
    @ApiModelProperty(value = "展示用交易日期")
    private String displayDate;
}
