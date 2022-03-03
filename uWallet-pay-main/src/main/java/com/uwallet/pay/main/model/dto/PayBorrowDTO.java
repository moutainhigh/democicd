package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付订单列表页
 * @author baixinyue
 * @createDate 2019/12/18
 *
 */
@ApiModel("支付订单列表页")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayBorrowDTO extends BaseDTO implements Serializable {

    /**
     * 支付账号
     */
    private String phone;

    /**
     * 商家名
     */
    private String corporateName;

    /**
     * 订单金额
     */
    private BigDecimal transAmount;

    /**
     * 实付金额
     */
    private BigDecimal truelyPayAmount;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 渠道费率
     */
    private BigDecimal rate;

    /**
     * 渠道名
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long gatewayId;

    /**
     * 渠道名
     */
    private String channelName;

    /**
     * 订单状态
     */
    private Integer state;

    /**
     * 清算状态
     */
    private Integer clearState;

    private Integer transType;

    /**
     * 创建日期
     */
    @JsonSerialize(using = LongDateSerializer.class)
    private Long createdDate;

    /**
     * 订单金额
     */
    private BigDecimal borrowAmount;

}
