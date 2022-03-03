package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 充值
 * @author baixinyue
 * @createDate 2019/12/16
 */
@ApiModel("充值")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RechargeDTO implements Serializable {

    /**
     * 用户
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;

    /**
     * 银行卡id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long cardId;

    /**
     * 充值金额
     */
    private BigDecimal amount;

    /**
     * 账户类型
     */
    private Integer accountType;

    /**
     * 渠道id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long gatewayId;

    /**
     * 渠道类型
     */
    private Integer gatewayType;

    /**
     * 手续费收取方
     */
    private Integer feeDirection;

    /**
     * 手续费金额
     */
    private BigDecimal feeAmt ;

    /**
     * 客户手续费率
     */
    private BigDecimal charge;

    /**
     * 支付类型
     */
    private Integer payType;

}
