package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 充值交易流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 充值交易流水表
 * @author: baixinyue
 * @date: Created in 2019-12-18 10:07:35
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "充值交易流水表")
public class RechargeFlow extends BaseEntity implements Serializable {

  /**
   * 
   */
  @ApiModelProperty(value = "")
  private Long userId;
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
   * 银行卡id
   */
  @ApiModelProperty(value = "银行卡id")
  private Long cardId;
  /**
   * 交易金额
   */
  @ApiModelProperty(value = "交易金额")
  private BigDecimal transAmount;
  /**
   * 收取商户手续费金额
   */
  @ApiModelProperty(value = "收取商户手续费金额")
  private BigDecimal fee;
  /**
   * 手续费收取方
   */
  @ApiModelProperty(value = "手续费收取方")
  private Integer feeDirection;
  /**
   * 手续费率
   */
  @ApiModelProperty(value = "手续费率")
  private BigDecimal charge;
  /**
   * 错误信息
   */
  @ApiModelProperty(value = "错误信息")
  private String errorMessage;
  /**
   * 错误码
   */
  @ApiModelProperty(value = "错误码")
  private String errorCode;
  /**
   * 交易类型
   */
  @ApiModelProperty(value = "交易类型")
  private Integer transType;
  /**
   * 备注
   */
  @ApiModelProperty(value = "备注")
  private String remark;
  /**
   * 交易状态：0:处理中， 1：交易成功 ，2：交易失败 ，3：交易可疑 
   */
  @ApiModelProperty(value = "交易状态：0:处理中， 1：交易成功 ，2：交易失败 ，3：交易可疑 ")
  private Integer state;

}
