package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 系统配置表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 系统配置表
 * @author: zhoutt
 * @date: Created in 2019-12-23 17:50:56
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "系统配置表")
public class ParametersConfig extends BaseEntity implements Serializable {

  /**
   * 分期付商户折扣率
   */
  @ApiModelProperty(value = "分期付商户折扣率")
  private BigDecimal discountRate;
  /**
   * 充值手续费
   */
  @ApiModelProperty(value = "充值手续费")
  private BigDecimal serviceCharge;
  /**
   * 小额免密金额
   */
  @ApiModelProperty(value = "小额免密金额")
  private BigDecimal avoidCloseAmount;
  /**
   * 支付商户折扣率平台占比
   */
  @ApiModelProperty(value = "支付商户折扣率平台占比")
  private BigDecimal merchantDiscountRatePlatformProportion;
  /**
   * 额外折扣支付平台占比
   */
  @ApiModelProperty(value = "额外折扣支付平台占比")
  private BigDecimal extraDiscountPayPlatform;
  /**
   * 额外折扣分期付平台占比
   */
  @ApiModelProperty(value = "额外折扣分期付平台占比")
  private BigDecimal extraDiscountCreditPlatform;
  /**
   * 钱包活动
   */
  @ApiModelProperty(value = "钱包活动")
  private Integer walletFavorable;

  @ApiModelProperty(value = "整体出售额度")
  private BigDecimal wholeSaleAmount;
  /**
   * 单卡失败次数
   */
  @ApiModelProperty(value = "单卡失败次数")
  private Integer cardFailedMax;
  /**
   * 当日最大失败次数
   */
  @ApiModelProperty(value = "当日最大失败次数")
  private Integer userCardFailedMax;
  /**
   * 退款最大时间
   */
  @ApiModelProperty(value = "退款最大时间")
  private Integer refundsLimitDate;
  /**
   * H5有效时间(小时)
   */
  @ApiModelProperty(value = "H5有效时间(小时)")
  private Integer validTime;
}
