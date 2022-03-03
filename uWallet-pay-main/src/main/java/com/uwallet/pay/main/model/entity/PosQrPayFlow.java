package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 *
 * </p>
 *
 * @package: com.fenmi.generator.entity
 * @description:
 * @author: zhangzeyuan
 * @date: Created in 2021-03-22 15:46:35
 * @copyright: Copyright (c) 2021
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PosQrPayFlow extends BaseEntity implements Serializable {

    /**
     * pos订单号
     */
    @ApiModelProperty(value = "pos订单号")
    private String posTransNo;
    /**
     * 货币类型 AUD/CNY
     */
    @ApiModelProperty(value = "货币类型 AUD/CNY")
    private String currencyType;
    /**
     * 交易金额
     */
    @ApiModelProperty(value = "交易金额")
    private BigDecimal transAmount;
    /**
     * 通知地址
     */
    @ApiModelProperty(value = "通知地址")
    private String notifyUrl;
    /**
     * base64 二维码信息
     */
    @ApiModelProperty(value = "base64 二维码信息")
    private String qrCode;
    /**
     * pos机唯一ID
     */
    @ApiModelProperty(value = "pos机唯一ID")
    private String posId;
    /**
     * 商户ID
     */
    @ApiModelProperty(value = "商户ID")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;
    /**
     * 系统订单号 对应qrpay flow表 transno字段
     */
    @ApiModelProperty(value = "系统订单号")
    private String sysTransNo;

    /**
     * 展示给三方的订单号
     */
    @ApiModelProperty(value = "展示给三方的订单号")
    private String showThirdTransNo;

    /**
     * 订单状态 0生成订单未支付 1支付成功 2支付失败 3支付超时
     */
    @ApiModelProperty(value = "订单状态 0生成订单未支付 1支付成功 2支付失败 3支付超时")
    private Integer orderStatus;


    /**
     * 回调通知状态 0 未通知 1 通知成功 2 通知失败
     */
    @ApiModelProperty(value = "回调通知状态 0 未通知 1 通知成功 2 通知失败")
    private Integer notifyStatus;


    /**
     * 支付时间
     */
    @ApiModelProperty(value = "支付时间")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long payDate;

}
