package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 扫码支付
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 扫码支付
 * @author: zhoutt
 * @date: Created in 2019-12-18 09:25:21
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "扫码支付")
public class QrPayFlow extends BaseEntity implements Serializable {

  /**
   * 三方渠道id
   */
  @ApiModelProperty(value = "三方渠道id")
  private Long gatewayId;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private Long payUserId;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private Integer payUserType;
  /**
   * 子账户类型（0：钱包余额子户，1：理财余额子户）
   */
  @ApiModelProperty(value = "子账户类型（0：钱包余额子户，1：理财余额子户）")
  private Integer payAccountType;
  /**
   * 渠道id
   */
  @ApiModelProperty(value = "渠道id")
  private Long recUserId;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private Integer recUserType;
  /**
   * 子账户类型（0：钱包余额子户，1：理财余额子户）
   */
  @ApiModelProperty(value = "子账户类型（0：钱包余额子户，1：理财余额子户）")
  private Integer recAccountType;
  /**
   * 商户id
   */
  @ApiModelProperty(value = "商户id")
  private Long merchantId;
  /**
   * 交易金额
   */
  @ApiModelProperty(value = "交易金额")
  private BigDecimal transAmount;
  /**
   * 实付金额
   */
  @ApiModelProperty(value = "实付金额")
  private BigDecimal payAmount;
  /**
   * 实收金额
   */
  @ApiModelProperty(value = "实收金额")
  private BigDecimal recAmount;
  /**
   * 手续费收取方向 0：收款方 ，1：付款方
   */
  @ApiModelProperty(value = "手续费收取方向 0：收款方 ，1：付款方")
  private Integer feeDirection;
  /**
   * 手续费金额
   */
  @ApiModelProperty(value = "手续费金额")
  private BigDecimal fee;
  /**
   * 平台手续费
   */
  @ApiModelProperty(value = "平台手续费")
  private BigDecimal platformFee;
  /**
   * 费率
   */
  @ApiModelProperty(value = "费率")
  private BigDecimal rate;
  /**
   * 清算批次号
   */
  @ApiModelProperty(value = "清算批次号")
  private Long batchId;
  /**
   * 是否需要支付系统清算 0：不需要  1：需要
   */
  @ApiModelProperty(value = "是否需要支付系统清算 0：不需要  1：需要")
  private Integer isNeedClear;
  /**
   * 清算状态 0：未清算 1：已清算 2：清算处理中
   */
  @ApiModelProperty(value = "清算状态 0：未清算 1：已清算 2：清算处理中")
  private Integer clearState;
  /**
   * 
   */
  @JsonSerialize(using = LongDateSerializer.class)
  @ApiModelProperty(value = "")
  private Long clearTime;
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
  /**
   * 退款状态：0 ：未退款 1：退款处理中 2：部分退款 3：全部退款
   */
  @ApiModelProperty(value = "退款状态：0 ：未退款 1：退款处理中 2：部分退款 3：全部退款")
  private Integer refundState;
  /**
   * 退款成功金额
   */
  @ApiModelProperty(value = "退款成功金额")
  private BigDecimal refundAmount;
  /**
   * 接入方订单号
   */
  @ApiModelProperty(value = "接入方订单号")
  private String accessPartyOrderNo;
  /**
   * 接入方通知url
   */
  @ApiModelProperty(value = "接入方通知url")
  private String accessPartyNotifyUrl;
  /**
   * 接入方服务费
   */
  @ApiModelProperty(value = "接入方服务费")
  private BigDecimal accessPartyServerFee;
  /**
   * 接入方服务费清算标志 0：未清算 1：已清算 2：清算处理中
   */
  @ApiModelProperty(value = "接入方服务费清算标志 0：未清算 1：已清算 2：清算处理中")
  private Integer accPltFeeClearState;
  /**
   * 接入方服务费清算批次
   */
  @ApiModelProperty(value = "接入方服务费清算批次")
  private Long accPltFeeClearBatch;
  /**
   * 接入方折扣
   */
  @ApiModelProperty(value = "接入方折扣")
  private BigDecimal accessPartyDiscount;
  /**
   * 分期付订单号
   */
  @ApiModelProperty(value = "分期付订单号")
  private String creditOrderNo;
  /**
   * 订单来源
   */
  @ApiModelProperty(value = "订单来源")
  private Integer orderSource;
  /**
   * 用户固定折扣金额
   */
  @ApiModelProperty(value = "用户固定折扣金额")
  private BigDecimal baseDiscountAmount;
  /**
   * 商户周期内营销折扣金额
   */
  @ApiModelProperty(value = "商户周期内营销折扣金额")
  private BigDecimal extraDiscountAmount;
  /**
   * 商户可配置营销折扣金额
   */
  @ApiModelProperty(value = "商户可配置营销折扣金额")
  private BigDecimal markingDiscountAmount;
  /**
   * 整体销售金额
   */
  @ApiModelProperty(value = "整体销售金额")
  private BigDecimal wholeSalesAmount;
  /**
   * 正常销售金额
   */
  @ApiModelProperty(value = "正常销售金额")
  private BigDecimal normalSaleAmount;
  /**
   * 红包抵用金额
   */
  @ApiModelProperty(value = "红包抵用金额")
  private BigDecimal redEnvelopeAmount;
  /**
   * 待清算金额
   */
  @ApiModelProperty(value = "待清算金额")
  private BigDecimal clearAmount;
  /**
   * 销售类型 0：正常销售 1：整体销售 2：混合销售
   */
  @ApiModelProperty(value = "销售类型 0：正常销售 1：整体销售 2：混合销售")
  private Integer saleType;
  /**
   * 交易单号
   */
  @ApiModelProperty(value = "交易单号")
  private String transNo;
  /**
   * 用户整体销售折扣
   */
  @ApiModelProperty(value = "用户整体销售折扣")
  private BigDecimal wholeSalesDiscount;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String cardId;
  /**
   * 分期付产品编号
   */
  @ApiModelProperty(value = "分期付产品编号")
  private String productId;

  /**
   *
   */
  @ApiModelProperty(value = "")
  private String payUserIp;


    /**
     * 整体出售折扣金额
     */
    @ApiModelProperty(value = "整体出售折扣金额")
    private BigDecimal wholeSalesDiscountAmount;


    /**
     * 商户固定折扣率
     */
    @ApiModelProperty(value = "商户固定折扣率")
    private BigDecimal baseDiscount;


    /**
     * 商户周期内营销折扣率
     */
    @ApiModelProperty(value = "商户周期内营销折扣率")
    private BigDecimal extraDiscount;

    /**
     * 商户可配置营销折扣率
     */
    @ApiModelProperty(value = "商户可配置营销折扣率")
    private BigDecimal markingDiscount;


    /**
     * 捐赠金额
     */
    @ApiModelProperty(value = "捐赠金额")
    private BigDecimal donationAmount;
    /**
     * 小费金额
     */
    @ApiModelProperty(value = "小费金额")
    private BigDecimal tipAmount;
    /**
     * 是否在交易明细显示, 0:不显示,1:显示
     */
    @ApiModelProperty(value = "是否在交易明细显示, 0:不显示,1:显示")
    private Integer isShow;
  /**
   * 营销券Id
   */
  @ApiModelProperty(value = "营销券Id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long marketingId;
  /**
   * 营销券面值
   */
  @ApiModelProperty(value = "营销券面值")
  private BigDecimal marketingBalance;

    /**
     * 营销规则ID
     */
    @ApiModelProperty(value = "营销规则ID")
    @Transient
    private Long marketingManageId;
}
