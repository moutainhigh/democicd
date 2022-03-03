package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * api交易订单流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: api交易订单流水表
 * @author: caishaojun
 * @date: Created in 2021-08-17 15:50:50
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "api交易订单流水表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiQrPayFlow extends BaseEntity implements Serializable {

  /**
   * pos订单号
   */
  @ApiModelProperty(value = "pos订单号")
  private String apiTransNo;
  /**
   * 货币类型 AUD/CNY
   */
  @ApiModelProperty(value = "货币类型 AUD/CNY")
  private String currencyType;
  /**
   * 交易金额
   */
  @ApiModelProperty(value = "交易金额")
  private BigDecimal transAmount;
  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long userId;
  /**
   * 交易商户id
   */
  @ApiModelProperty(value = "交易商户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long apiMerchantId;
  /**
   * 上级商户id
   */
  @ApiModelProperty(value = "上级商户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long superMerchantId;
  /**
   * 展示给三方的订单号
   */
  @ApiModelProperty(value = "展示给三方的订单号")
  private String showThirdTransNo;
  /**
   * 系统订单号关联qrpay flow表 transno字段
   */
  @ApiModelProperty(value = "系统订单号关联qrpay flow表 transno字段")
  private String transNo;
  /**
   * 交易状态： 0:pending（已提交未支付） ， 1：confirmed（订单确认） ，2：expired（关单/订单拒绝）
   */
  @ApiModelProperty(value = "交易状态： 0:pending（已提交未支付） ， 1：confirmed（订单确认） ，2：expired（关单/订单拒绝）")
  private Integer orderStatus;
  /**
   * 交易状态 0：未支付  1：支付成功 2：支付失败 3：支付处理中
   */
  @ApiModelProperty(value = "交易状态 0：未支付  1：支付成功 2：支付失败 3：支付处理中")
  private Integer transStatus;
  /**
   * 回调通知状态 0 未通知 1 通知成功 2 通知失败
   */
  @ApiModelProperty(value = "回调通知状态 0 未通知 1 通知成功 2 通知失败")
  private Integer notifyStatus;
  /**
   * 支付成功时间
   */
  @ApiModelProperty(value = "支付成功时间")
  @JsonSerialize(using = LongDateSerializer.class)
  private Long payDate;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private String confirmationUrl;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private String cancellationUrl;
  /**
   * 捐赠金额
   */
  @ApiModelProperty(value = "捐赠金额")
  private BigDecimal donateAmount;
  /**
   * 小费金额
   */
  @ApiModelProperty(value = "小费金额")
  private BigDecimal tipAmount;
  /**
   * 交易幂等串
   */
  @ApiModelProperty(value = "交易幂等串")
  private String idempotencyKey;

  /**
   * 商户固定值order
   */
  @ApiModelProperty(value = "商户固定值order")
  private String typeOrder;

  /**
   * 过期时间
   */
  @ApiModelProperty(value = "过期时间")
  @JsonSerialize(using = LongDateSerializer.class)
  private Long expirationTime;

  /**
   * 卡id
   */
  @ApiModelProperty(value = "卡id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long cardId;

}
