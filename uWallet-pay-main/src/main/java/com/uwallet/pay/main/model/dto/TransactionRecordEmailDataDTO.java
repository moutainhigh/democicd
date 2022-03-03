package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;


@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionRecordEmailDataDTO implements Serializable {


    private Long id;

    private String createdDate;

    /**
     * 用户Id
     */
    @ApiModelProperty(value = "用户Id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;
    /**
     * 用户姓名
     */
    @ApiModelProperty(value = "用户姓名")
    private String userName;
    /**
     * 商户Id
     */
    @ApiModelProperty(value = "商户Id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;
    /**
     * 商户名
     */
    @ApiModelProperty(value = "商户名")
    private String merchantName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 地址
     */
    private String address;

    /**
     * 城市code
     */
    private String city;

    /**
     * 州名code
     */
    private String merchantState;


    /**
     * 借款金额
     */
    @ApiModelProperty(value = "借款金额")
    private BigDecimal borrowAmount;

    /**
     * 订单状态：10:进行中，20:已逾期，30:已结清
     */
    @ApiModelProperty(value = "订单状态： 10:进行中 ,20:已逾期,30:已结清")
    private Integer state;

    /**
     * 总期数
     */
    @ApiModelProperty(value = "总期数")
    private Integer periodQuantity;

    /**
     * 预期还款时间
     */
    private String expectRepayTime;
    /**
     * 本期应还金额
     */
    @ApiModelProperty(value = "本期应还金额")
    private BigDecimal shouldPayAmount;
    /**
     * 当前已还金额
     */
    @ApiModelProperty(value = "当前已还金额")
    private BigDecimal paidAmount;

}
