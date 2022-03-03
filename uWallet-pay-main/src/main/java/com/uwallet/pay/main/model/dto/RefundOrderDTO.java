package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 退款订单
 * </p>
 *
 * @description: 退款订单
 * @author: zhoutt
 * @date: Created in 2021-08-18 09:01:47
 */
@ApiModel("退款订单")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefundOrderDTO extends BaseDTO implements Serializable {

    /**
     * 原交易付款方用户id
     */
    @ApiModelProperty(value = "原交易付款方用户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long orgPayUserId;
    /**
     * 
     */
    @ApiModelProperty(value = "")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;
    /**
     * 
     */
    @ApiModelProperty(value = "")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long superMerchantId;
    /**
     * 退款金额
     */
    @ApiModelProperty(value = "退款金额")
    private BigDecimal refundAmount;
    /**
     * qr_pay_flow中的trans_no
     */
    @ApiModelProperty(value = "qr_pay_flow中的trans_no")
    private String transNo;
    /**
     * 原退款交易类型
     */
    @ApiModelProperty(value = "原退款交易类型")
    private Integer transType;
    /**
     * 交易状态：0:处理中， 1：交易成功 ，2：交易失败 ，3：交易可疑 
     */
    @ApiModelProperty(value = "交易状态：0:处理中， 1：交易成功 ，2：交易失败 ，3：交易可疑 ")
    private Integer state;
    /**
     * 订单来源
     */
    @ApiModelProperty(value = "订单来源")
    private Integer orderSource;
    /**
     * 币种 澳币：AUD
     */
    @ApiModelProperty(value = "币种 澳币：AUD")
    private String currency;
    /**
     * 交易幂等串
     */
    @ApiModelProperty(value = "交易幂等串")
    private String idempotencyKey;
    /**
     * 
     */
    @ApiModelProperty(value = "")
    private String reference;
    /**
     * 渠道返回码
     */
    @ApiModelProperty(value = "渠道返回码")
    private String returnCode;
    /**
     * 渠道返回信息
     */
    @ApiModelProperty(value = "渠道返回信息")
    private String returnMessage;
    /**
     * 结算状态 0：未结算 1：已结算 2：结算中 3：待回退  4：已回退 5:回退中 6:部分结算
     */
    @ApiModelProperty(value = "结算状态 0：未结算 1：已结算 2：结算中 3：待回退  4：已回退 5:回退中 6:部分结算")
    private Integer settlementState;
    /**
     * 结算时间
     */
    @ApiModelProperty(value = "结算时间")
    @JsonSerialize(using = LongDateSerializer.class)
    private Long settlementTime;
    /**
     * 商户补钱
     */
    @ApiModelProperty(value = "商户补钱")
    private BigDecimal makeUpFee;
    /**
     * 0:未补 1:补上
     */
    @ApiModelProperty(value = "0:未补 1:补上")
    private Integer makeUpState;
    /**
     * 退款理由
     */
    @ApiModelProperty(value = "退款理由")
    private String reason;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;
    /**
     * 清算批次号
     */
    @ApiModelProperty(value = "清算批次号")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long batchId;
    /**
     * 待结算金额
     */
    @ApiModelProperty(value = "待结算金额")
    private BigDecimal notSettlementAmount;
    /**
     * 对账状态 0：未对账 1：已对账 2：无需对账
     */
    @ApiModelProperty(value = "对账状态 0：未对账 1：已对账 2：无需对账")
    private Integer checkState;
    /**
     * 对账时间
     */
    @ApiModelProperty(value = "对账时间")
    @JsonSerialize(using = LongDateSerializer.class)
    private Long checkTime;

}
