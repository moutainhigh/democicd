package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 代收三方流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 代收三方流水表
 * @author: ztt
 * @date: Created in 2020-02-18 17:03:29
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "代收三方流水表")
public class RefundFlow extends BaseEntity implements Serializable {

    /**
     * 退款原交易订单流水
     */
    @ApiModelProperty(value = "退款原交易订单流水")
    private Long flowId;
    /**
     * 渠道id
     */
    @ApiModelProperty(value = "渠道id")
    private Long gatewayId;
    /**
     * 原交易收款方用户id
     */
    @ApiModelProperty(value = "原交易收款方用户id")
    private Long orgRecUserId;
    /**
     * 退款金额
     */
    @ApiModelProperty(value = "退款金额")
    private BigDecimal refundAmount;
    /**
     * 发送三方流水
     */
    @ApiModelProperty(value = "发送三方流水")
    private String ordreNo;
    /**
     * omipay返回订单号
     */
    @ApiModelProperty(value = "omipay返回订单号")
    private String omiPayRefundOrderNo;
    /**
     * latpay返回订单号
     */
    @ApiModelProperty(value = "latpay返回订单号")
    private String lpsRefundId;
    /**
     * latpay退款请求id
     */
    @ApiModelProperty(value = "latpay退款请求id")
    private String requestId;
    /**
     * latpay退款返回pwd
     */
    @ApiModelProperty(value = "latpay退款返回pwd")
    private String replyPwd;
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
     * 币种 澳币：AUS
     */
    @ApiModelProperty(value = "币种 澳币：AUS")
    private String currency;
    /**
     * 清算批次号
     */
    @ApiModelProperty(value = "清算批次号")
    private Long batchId;
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
     * 客户的ip地址
     */
    @ApiModelProperty(value = "客户的ip地址")
    private String customerIpaddress;
    /**
     *
     */
    @ApiModelProperty(value = "")
    private String crdstrgToken;
    /**
     * 对账状态 0：未对账 1：已对账  2：无需对账
     */
    @ApiModelProperty(value = "对账状态 0：未对账 1：已对账  2：无需对账")
    private Integer clearState;
    /**
     * 对账时间
     */
    @ApiModelProperty(value = "对账时间")
    private Long clearTime;
    /**
     * 待结算金额
     */
    @ApiModelProperty(value = "待结算金额")
    private BigDecimal notSettlementAmount;
    /**
     * 结算状态 0：未结算 1：已结算 2：结算中 3：待回退  4：已回退 5:回退中 6:部分结算
     */
    @ApiModelProperty(value = "结算状态 0：未结算 1：已结算 2：结算中 3：待回退  4：已回退 5:回退中 6:部分结算")
    private Integer settlementState;
    /**
     * 结算时间
     */
    @ApiModelProperty(value = "结算时间")
    private Long settlementTime;
    /**
     * 退款原因
     */
    @ApiModelProperty(value = "退款原因")
    private String reason;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;
    /**
     * 对账状态 0：未对账 1：已对账 2：无需对账
     */
    @ApiModelProperty(value = "对账状态 0：未对账 1：已对账 2：无需对账")
    private Integer checkState;
    /**
     * 对账时间
     */
    @ApiModelProperty(value = "对账时间")
    private Long checkTime;
    /**
     * 商户补钱
     */
    @ApiModelProperty(value = "商户补钱")
    private BigDecimal makeUpFee;
    /**
     * 0:未补 1:补上
     */
    @ApiModelProperty(value = "0:未补 1:补上")
    private Long makeUpState;
    /**
     * 原始发送三方的流水号
     */
    @ApiModelProperty(value = "原始发送三方的流水号")
    private String orgThirdNo;
    /**
     * 退款单号
     */
    @ApiModelProperty(value = "退款单号")
    private String refundNo;
    /**
     * stripe交易时单号(退款使用该单号退款)
     */
    @ApiModelProperty(value = "stripe交易时单号(退款使用该单号退款)")
    private String stripeRefundNo;
}
