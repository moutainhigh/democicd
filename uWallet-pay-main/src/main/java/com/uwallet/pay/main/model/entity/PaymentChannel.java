package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 三方支付通道表
 * </p>
 *
 * @package:  com.uwallet.pay.main.main.entity
 * @description: 三方支付通道表
 * @author: baixinyue
 * @date: Created in 2020-02-13 10:31:26
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "三方支付通道表")
public class PaymentChannel extends BaseEntity implements Serializable {

  /**
   * 通道名称
   */
  @ApiModelProperty(value = "通道名称")
  private String channelName;
  /**
   * 渠道类型：0：代扣，1：支付宝，2：微信
   */
  @ApiModelProperty(value = "渠道类型：0：代扣，1：支付宝，2：微信")
  private Integer gatewayType;
  /**
   * 状态 0：禁用 1：可用
   */
  @ApiModelProperty(value = "状态 0：禁用 1：可用")
  private Integer state;

}
