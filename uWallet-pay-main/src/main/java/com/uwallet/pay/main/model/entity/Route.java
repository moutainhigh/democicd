package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 商户渠道中间表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 商户渠道中间表
 * @author: baixinyue
 * @date: Created in 2019-12-26 16:10:27
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "商户渠道中间表")
public class Route extends BaseEntity implements Serializable {

  /**
   * 商户id
   */
  @ApiModelProperty(value = "商户id")
  private Long merchantId;
  /**
   * 渠道类型（0：代扣，1：支付宝，2：微信）
   */
  @ApiModelProperty(value = "渠道类型（0：代扣，1：支付宝，2：微信）")
  private Integer gatewayType;
  /**
   * 费率类型  0：用户承担;1：商户承担
   */
  @ApiModelProperty(value = "费率类型  0：用户承担;1：商户承担")
  private Integer rateType;
  /**
   * 费率值
   */
  @ApiModelProperty(value = "费率值")
  private Double rate;
  /**
   * 工作时间  0:自然日 1：工作日
   */
  @ApiModelProperty(value = "工作时间  0:自然日 1：工作日")
  private Integer workType;
  /**
   * 最大金额
   */
  @ApiModelProperty(value = "最大金额")
  private BigDecimal maxAmount;
  /**
   * 最小金额
   */
  @ApiModelProperty(value = "最小金额")
  private BigDecimal minAmount;
  /**
   * 每日起始时间
   */
  @ApiModelProperty(value = "每日起始时间")
  private String startTime;
  /**
   * 每日截止时间
   */
  @ApiModelProperty(value = "每日截止时间")
  private String endTime;
  /**
   * 备注
   */
  @ApiModelProperty(value = "备注")
  private String remark;

}
