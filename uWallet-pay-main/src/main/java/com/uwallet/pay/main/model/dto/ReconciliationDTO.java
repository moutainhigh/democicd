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
 * 对账
 * </p>
 *
 * @description: 对账
 * @author: baixinyue
 * @date: Created in 2020-02-17 09:59:08
 */
@ApiModel("对账")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReconciliationDTO extends BaseDTO implements Serializable {

    /**
     * 渠道返回订单编号
     */
    @ApiModelProperty(value = "渠道返回订单编号")
    private String tripartiteOrderNo;
    /**
     * 我方订单编号
     */
    @ApiModelProperty(value = "我方订单编号")
    private String orderNo;
    /**
     * 渠道类型
     */
    @ApiModelProperty(value = "渠道类型")
    private Integer type;
    /**
     * 交易类型: 0、支付 1、退款
     */
    @ApiModelProperty(value = "交易类型: 0、支付 1、退款")
    private Integer transactionType;
    /**
     * 支付时间
     */
    @ApiModelProperty(value = "支付时间")
    private Long paymentTime;
    /**
     * 输入金额、交易金额
     */
    @ApiModelProperty(value = "输入金额、交易金额")
    private BigDecimal amountEntered;
    /**
     * 总金额
     */
    @ApiModelProperty(value = "总金额")
    private BigDecimal grossAmount;
    /**
     * '交易状态：0:待处理， 1：成功， 2：处理失败'
     */
    @ApiModelProperty(value = "'交易状态：0:待处理， 1：成功， 2：失败'")
    private Integer state;
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
     * 对账文件表id时间
     */
    @ApiModelProperty(value = "对账文件表id时间")
    private Long batchId;

}
