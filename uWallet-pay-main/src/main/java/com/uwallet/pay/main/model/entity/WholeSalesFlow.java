package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 整体销售流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 整体销售流水表
 * @author: zhoutt
 * @date: Created in 2020-10-17 14:33:51
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "整体销售流水表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WholeSalesFlow extends BaseEntity implements Serializable {

  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long userId;
  /**
   * 商户id
   */
  @ApiModelProperty(value = "商户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long merchantId;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private BigDecimal merchantDiscount;
  /**
   * 用户分期付折扣
   */
  @ApiModelProperty(value = "用户分期付折扣")
  private BigDecimal customerDiscount;
  /**
   * 用户支付折扣
   */
  @ApiModelProperty(value = "用户支付折扣")
  private BigDecimal customerPayDiscount;
  /**
   * 金额
   */
  @ApiModelProperty(value = "金额")
  private BigDecimal amount;
  /**
   * 待结算金额
   */
  @ApiModelProperty(value = "待结算金额")
  private BigDecimal settlementAmount;
  /**
   * 交易类型
   */
  @ApiModelProperty(value = "交易类型")
  private Integer transType;
  /**
   * 交易状态
   */
  @ApiModelProperty(value = "交易状态")
  private Integer state;
  /**
   * 返回信息
   */
  @ApiModelProperty(value = "返回信息")
  private String returnMessage;
  /**
   * 返回码
   */
  @ApiModelProperty(value = "返回码")
  private String returnCode;
  /**
   * 结算时间
   */
  @ApiModelProperty(value = "结算时间")
  @JsonSerialize(using = LongDateSerializer.class)
  private Long settlementTime;
  /**
   * 结算状态 0：未结算 1：已结算 2：结算中
   */
  @ApiModelProperty(value = "结算状态 0：未结算 1：已结算 2：结算中")
  private Integer settlementState;
  /**
   * 延时结算状态 0：不延时 1：延时
   */
  @ApiModelProperty(value = "延时结算状态 0：不延时 1：延时")
  private Integer settlementDelay;
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
  @JsonSerialize(using = LongDateSerializer.class)
  private Long checkTime;
  /**
   * 审核状态
   */
  @ApiModelProperty(value = "审核状态")
  private Integer approveState;

  @ApiModelProperty("订单状态， 0：意向 1：正式")
  private Integer orderType;

  @ApiModelProperty("订单申请通过日期")
  private Long passTime;

  /**
   * 批次号
   */
  @ApiModelProperty(value = "批次号")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long batchId;
}
