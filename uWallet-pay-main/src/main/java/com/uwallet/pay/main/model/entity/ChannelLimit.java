package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 渠道日交易累计金额记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 渠道日交易累计金额记录表
 * @author: baixinyue
 * @date: Created in 2019-12-21 10:06:05
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "渠道日交易累计金额记录表")
public class ChannelLimit extends BaseEntity implements Serializable {

  /**
   * 渠道id
   */
  @ApiModelProperty(value = "渠道id")
  private Long channelId;
  /**
   * 累计金额
   */
  @ApiModelProperty(value = "累计金额")
  private BigDecimal accruingAmount;

  /**
   * 日累计上限
   */
  private BigDecimal dailyTotalAmount;

}
