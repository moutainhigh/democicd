package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 支付渠道信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 支付渠道信息表
 * @author: baixinyue
 * @date: Created in 2019-12-26 16:11:10
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "支付渠道信息表")
public class Gateway extends BaseEntity implements Serializable {

  /**
   * 渠道提供的id
   */
  @ApiModelProperty(value = "渠道提供的id")
  private String channelMerchantId;
  /**
   * 渠道名称
   */
  @ApiModelProperty(value = "渠道名称")
  private String channelName;
  /**
   * 渠道交易类型（0：代扣）
   */
  @ApiModelProperty(value = "渠道交易类型（0：代扣）")
  private Long type;
  /**
   * 渠道类型（0：代扣，1：支付宝，2：微信）
   */
  @ApiModelProperty(value = "渠道类型（0：代扣，1：支付宝，2：微信）")
  private Integer gatewayType;
  /**
   * 费率类型  0：固定费率;1：按百分比
   */
  @ApiModelProperty(value = "费率类型  0：固定费率;1：按百分比")
  private Integer rateType;
  /**
   * 单笔限额最大金额
   */
  @ApiModelProperty(value = "单笔限额最大金额")
  private BigDecimal singleMaxAmount;
  /**
   * 单笔限额最小金额
   */
  @ApiModelProperty(value = "单笔限额最小金额")
  private BigDecimal singleMinAmount;
  /**
   * 日累计限额
   */
  @ApiModelProperty(value = "日累计限额")
  private BigDecimal dailyTotalAmount;
  /**
   * 费率值
   */
  @ApiModelProperty(value = "费率值")
  private BigDecimal rate;
  /**
   * 费率最大值
   */
  @ApiModelProperty(value = "费率最大值")
  private Double rateMax;
  /**
   * 费率最小值
   */
  @ApiModelProperty(value = "费率最小值")
  private Double rateMin;
  /**
   * 结算方式： 0：实时 1：T+1 
   */
  @ApiModelProperty(value = "结算方式： 0：实时 1：T+1 ")
  private Integer settlementModes;
  /**
   * 工作时间  0:自然日 1：工作日
   */
  @ApiModelProperty(value = "工作时间  0:自然日 1：工作日")
  private Integer workType;
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
   * 手续费结算方式 0：线上 1：线下
   */
  @ApiModelProperty(value = "手续费结算方式 0：线上 1：线下")
  private Integer feeSettlementModes;
  /**
   * 配置项
   */
  @ApiModelProperty(value = "配置项")
  private String configurationItem;
  /**
   * 证书路径
   */
  @ApiModelProperty(value = "证书路径")
  private String certificatePath;
  /**
   * 证书密码
   */
  @ApiModelProperty(value = "证书密码")
  private String certificatePassword;
  /**
   * 状态 0：禁用 1：可用
   */
  @ApiModelProperty(value = "状态 0：禁用 1：可用")
  private Integer state;
  /**
   * 备注
   */
  @ApiModelProperty(value = "备注")
  private String remark;
  /**
   * 用户名
   */
  @ApiModelProperty(value = "用户名")
  private String userName;
  /**
   * 密码
   */
  @ApiModelProperty(value = "密码")
  private String password;
  /**
   * 渠道IP
   */
  private String gatewayIp;

}
