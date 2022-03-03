package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Tolerate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * NFC信息、绑定表
 * </p>
 *
 * @description: 二维码信息、绑定表
 * @author: zhoutt
 * @date: Created in 2020-03-23 14:31:21
 */
@ApiModel("NFC信息、绑定表")
@Builder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NfcCodeInfoDTO extends BaseDTO implements Serializable {

    /**
     * 关联用户
     */
    @ApiModelProperty(value = "关联用户")
    private Long userId;
    /**
     * 商户id
     */
    @ApiModelProperty(value = "商户id")
    private Long merchantId;
    /**
     * 二维码
     */
    @ApiModelProperty(value = "二维码")
    private String qrCode;
    /**
     * nfc码
     */
    @ApiModelProperty(value = "nfc码")
    private String code;
    /**
     * 0:未关联 1:关联
     */
    @ApiModelProperty(value = "0:未关联 1:关联")
    private Integer state;
    /**
     * 关联时间
     */
    @ApiModelProperty(value = "关联时间")
    @JsonSerialize(using = LongDateSerializer.class)
    private Long correlationTime;

    /**
     * 商户账号
     */
    private String merchantNo;

    /**
     * 商户名称
     */
    private String merchantName;


    private List<RouteDTO> routeDTOS;

//    private Double payDiscountRate;

    private BigDecimal balance;

    private Integer  installmentState;
    /**
     * 商户整体出售余额
     */
    private BigDecimal wholeSaleBalance;

    /**
     * 商户整体出售用户折扣
     */
    private BigDecimal wholeSaleUserDiscount;

    /**
     * 固定折扣
     */
    private BigDecimal baseRate;
    /**
     * 额外折扣
     */
    private BigDecimal extraDiscount;
    /**
     * 商户营销折扣
     */
    private BigDecimal marketingDiscount;

    @Tolerate
    public NfcCodeInfoDTO(){};
    /**
     * 用户stripe弹窗标识
     */
    private Integer stripeState;

}
