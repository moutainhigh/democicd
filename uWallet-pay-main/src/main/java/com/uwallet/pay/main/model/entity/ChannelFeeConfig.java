package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
 * 通道手续费配置表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 通道手续费配置表
 * @author: zhoutt
 * @date: Created in 2021-03-08 10:12:26
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "通道手续费配置表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChannelFeeConfig extends BaseEntity implements Serializable {

  /**
   * 渠道id
   */
  @ApiModelProperty(value = "渠道id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long gatewayId;
  /**
   * 值
   */
  @ApiModelProperty(value = "值")
  private Integer code;
  /**
   * 描述
   */
  @ApiModelProperty(value = "描述")
  private String value;
  /**
   * 费率类型 0：固定费率 1 ：百分比
   */
  @ApiModelProperty(value = "费率类型 0：固定费率 1 ：百分比")
  private Integer type;
  /**
   * 费率值
   */
  @ApiModelProperty(value = "费率值")
  private BigDecimal rate;
  /**
   * 收取方向 0 ：收款方 1：付款方
   */
  @ApiModelProperty(value = "收取方向 0 ：收款方 1：付款方")
  private Integer direction;
  /**
   * 备注
   */
  @ApiModelProperty(value = "备注")
  private String remark;

}
