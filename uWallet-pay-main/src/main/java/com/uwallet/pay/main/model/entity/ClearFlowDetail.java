package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 用户主表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 用户主表
 * @author: zhoutt
 * @date: Created in 2020-02-13 13:44:07
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "用户主表")
public class ClearFlowDetail extends BaseEntity implements Serializable {

  /**
   * 清算批次号
   */
  @ApiModelProperty(value = "清算批次号")
  private Long clearBatchId;
  /**
   * 清算交易流水
   */
  @ApiModelProperty(value = "清算交易流水")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long flowId;
  /**
   * userId
   */
  @ApiModelProperty(value = "收款userId")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long userId;
  /**
   * 收款userId
   */
  @ApiModelProperty(value = "收款userId")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long recUserId;
  /**
   * 商户id
   */
  @ApiModelProperty(value = "商户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long merchantId;
  /**
   * 实际清算总金额
   */
  @ApiModelProperty(value = "实际清算总金额")
  private BigDecimal clearAmount;
  /**
   * 清算金额
   */
  @ApiModelProperty(value = "清算金额")
  private BigDecimal transAmount;
  /**
   * 订单总金额
   */
  @ApiModelProperty(value = "订单总金额")
  private BigDecimal borrowAmount;
  /**
   * 交易类型
   */
  @ApiModelProperty(value = "交易类型")
  private Integer transType;
  /**
   * 清算状态 0：处理中 1：处理成功 2：失败
   */
  @ApiModelProperty(value = "清算状态 0：处理中 1：处理成功 2：失败")
  private Integer state;

}
