package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 广告表
 * </p>
 *
 * @description: 对账详情
 * @author: Strong
 * @date: Created in 2020-01-11 09:45:03
 */
@ApiModel("对账详情")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReconciliationDetailDTO extends BaseDTO implements Serializable {

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    private String transNo;

    /**
     * 交易流水号
     */
    @ApiModelProperty(value = "交易流水号")
    private String orderNo;

    /**
     * 三方流水号
     */
    @ApiModelProperty(value = "三方流水号")
    private String tripartiteOrderNo;

    /**
     * 交易时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    @ApiModelProperty(value = "交易时间")
    private Long paymentTime;

    /**
     * 金额
     */
    @ApiModelProperty(value = "金额")
    private BigDecimal transAmount;

    /**
     * 交易状态 0:处理中， 1：交易成功 ，2：交易失败 ，3：交易可疑
     */
    @ApiModelProperty(value = "交易状态 0:处理中， 1：交易成功 ，2：交易失败 ，3：交易可疑")
    private Integer transState;

    /**
     * 对账状态：0-失败 1-成功
     */
    @ApiModelProperty(value = "对账状态：0-失败 1-成功")
    private Integer checkState;

    /**
     * 对账时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    @ApiModelProperty(value = "对账时间")
    private Long checkTime;

    /**
     * 文件名
     */
    @ApiModelProperty(value = "文件名")
    private String thirdFileName;

    /**
     * 是否异常：0/2-异常，1-正常
     */
    @ApiModelProperty(value = "是否异常：0/2-异常，1-正常")
    private Integer isNormal;

}
