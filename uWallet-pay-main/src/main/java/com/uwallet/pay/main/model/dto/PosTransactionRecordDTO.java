package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 *
 * </p>
 *
 * @description:
 * @author: zhangzeyuan
 * @date: Created in 2021-03-22 15:46:35
 */
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PosTransactionRecordDTO implements Serializable {


    private static final long serialVersionUID = 1105088360302877107L;

    /**
     * pos机唯一ID
     */
    @ApiModelProperty(value = "pos机唯一ID")
    private String posId;

    /**
     * pos订单号
     */
    @ApiModelProperty(value = "pos订单号")
    private String posTransNo;


    /**
     * payo订单号
     */
    @ApiModelProperty(value = "payo订单号")
    private String payoTransNo;


    /**
     * 订单状态 0生成订单未支付 1支付成功 2支付失败 3支付超时
     */
    @ApiModelProperty(value = "订单状态 0生成订单未支付 1支付成功 2支付失败 3支付超时")
    private Integer orderStatus;


    /**
     * 交易金额
     */
    @ApiModelProperty(value = "交易金额")
    private BigDecimal transAmount;

    /**
     * 业务类型0：正常销售 1：整体销售 2：混合销售
     */
    private Integer saleType;

    /**
     * 平台服务费
     */
    private BigDecimal platformFee;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 订单创建时间戳
     */
    private Long createDate;

    /**
     * 支付时间戳
     */
    private Long payDate;
}
