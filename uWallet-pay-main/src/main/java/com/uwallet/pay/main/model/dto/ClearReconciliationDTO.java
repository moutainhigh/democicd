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
 * 清算对账表
 * </p>
 *
 * @description: 清算对账表
 * @author: baixinyue
 * @date: Created in 2020-03-06 09:00:14
 */
@ApiModel("清算对账表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClearReconciliationDTO extends BaseDTO implements Serializable {

    /**
     * 清算日期
     */
    @ApiModelProperty(value = "清算日期")
    private Long financeDate;
    /**
     * 清算金额
     */
    @ApiModelProperty(value = "清算金额")
    private BigDecimal clearingAmount;
    /**
     * 清算编号
     */
    @ApiModelProperty(value = "清算编号")
    private String clearingNumber;
    /**
     * 支付笔数
     */
    @ApiModelProperty(value = "支付笔数")
    private Integer payCount;
    /**
     * 支付金额
     */
    @ApiModelProperty(value = "支付金额")
    private BigDecimal payAmount;
    /**
     * 退款笔数
     */
    @ApiModelProperty(value = "退款笔数")
    private Integer refundCount;
    /**
     * 退款金额
     */
    @ApiModelProperty(value = "退款金额")
    private BigDecimal refundAmount;
    /**
     * 对账状态：0、失败 1、成功
     */
    @ApiModelProperty(value = "对账状态：0、失败 1、成功")
    private Integer checkState;
    /**
     * 对账时间
     */
    @ApiModelProperty(value = "对账时间")
    private Long checkTime;
    /**
     * 通道ID
     */
    @ApiModelProperty(value = "通道ID")
    private Integer gatewayId;

}
