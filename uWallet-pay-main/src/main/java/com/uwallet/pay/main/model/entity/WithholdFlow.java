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
 * @author: baixinyue
 * @date: Created in 2019-12-16 10:50:03
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "代收三方流水表")
public class WithholdFlow extends BaseEntity implements Serializable {

  /**
   *
   */
  @ApiModelProperty(value = "")
  private Long userId;
  /**
   * 渠道方颁发的商户id
   */
  @ApiModelProperty(value = "渠道方颁发的商户id")
  private String gatewayMerchantId;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String gatewayMerchantPassword;
  /**
   * 10:PAY 20:INVEST 30:CREDIT 40:ACCOUNT
   */
  @ApiModelProperty(value = "10:PAY 20:INVEST 30:CREDIT 40:ACCOUNT")
  private String system;
  /**
   * 前置交易流水
   */
  @ApiModelProperty(value = "前置交易流水")
  private Long flowId;
  /**
   * 子账户类型（0：钱包余额子户，1：理财余额子户）
   */
  @ApiModelProperty(value = "子账户类型（0：钱包余额子户，1：理财余额子户）")
  private Integer accountType;
  /**
   * 渠道id
   */
  @ApiModelProperty(value = "渠道id")
  private Long gatewayId;
  /**
   * 交易金额
   */
  @ApiModelProperty(value = "交易金额")
  private BigDecimal transAmount;
  /**
   * 渠道手续费
   */
  @ApiModelProperty(value = "渠道手续费")
  private BigDecimal charge;
  /**
   * latpay商户加工费
   */
  @ApiModelProperty(value = "latpay商户加工费")
  private BigDecimal fee;
  /**
   * 发送三方流水
   */
  @ApiModelProperty(value = "发送三方流水")
  private String ordreNo;
  /**
   * 交易类型
   */
  @ApiModelProperty(value = "交易类型")
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
   * 服务器的静态ip地址
   */
  @ApiModelProperty(value = "服务器的静态ip地址")
  private String merchantIpaddress;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String customerFirstname;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String customerMiddlename;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String customerLastname;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String customerPhone;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String customerEmail;
  /**
   * 客户的ip地址
   */
  @ApiModelProperty(value = "客户的ip地址")
  private String customerIpaddress;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String billFirstname;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String billMiddlename;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String billLastname;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String billAddress1;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String billAddress2;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String billCity;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String billCountry;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String billState;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String billZip;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String crdstrgToken;
  /**
   * integrapay唯一id
   */
  @ApiModelProperty(value = "integrapay唯一id")
  private String uniqueReference;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String customerCcCvc;
  /**
   * 备注
   */
  @ApiModelProperty(value = "备注")
  private String remark;

  /**
   * 渠道名称
   */
  @ApiModelProperty(value = "渠道名称")
  private String channelName;

  /**
   * 回调url
   */
  private String noticeUrl;

  /**
   * 渠道名称
   */
  @ApiModelProperty(value = "omipay返回订单号")
  private String omiPayOrderNo;

  @ApiModelProperty(value = "latpay订单编号")
  private String lpsTransactionId;

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
   * 结算状态
   */
  @ApiModelProperty(value = "结算状态")
  private Integer settlementState;
  /**
   * 结算时间
   */
  @ApiModelProperty(value = "结算时间")
  private Long settlementTime;

  @ApiModelProperty(value = "split订单号")
  private String splitNo;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String splitContactId;
  /**
   * 订单金额
   */
  @ApiModelProperty(value = "订单金额")
  private BigDecimal orderAmount;
  /**
   * 费率值
   */
  @ApiModelProperty(value = "费率值")
  private BigDecimal feeRate;
  /**
   * 费率类型
   */
  @ApiModelProperty(value = "费率类型")
  private Integer feeType;

    /**
     * 卡号
     */
    @ApiModelProperty(value = "卡号")
    private String cardNo;
    /**
     * 卡类型，允许的值：10、VISA, 20、MAST, 30、 SWITCH,  40、SOLO,  50、DELTA, 60、 AMEX
     */
    @ApiModelProperty(value = "卡类型，允许的值：10、VISA, 20、MAST, 30、 SWITCH,  40、SOLO,  50、DELTA, 60、 AMEX")
    private String cardCcType;


    /**
     * 返回错误信息
     */
    @ApiModelProperty(value = "返回错误信息")
    private String errorMessage;
    /**
     * stripe跳转url
     */
    @ApiModelProperty(value = "stripe跳转url")
    private String stripeUrl;
    /**
     * stripe三方返回交易id
     */
    @ApiModelProperty(value = "stripe三方返回交易id")
    private String stripeId;
    /**
     * stripe跳转密钥
     */
    @ApiModelProperty(value = "stripe跳转密钥")
    private String stripeClientSecret;
}
