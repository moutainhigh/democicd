package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 充值订单列表页扩展类
 * @author baixinyue
 * @createDate 2019/12/18
 */

@ApiModel("充值交易流水表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RechargeBorrowDTO extends BaseDTO implements Serializable {

    /**
     * 用户账号
     */
    private String email;

    /**
     * 充值金额
     */
    private BigDecimal transAmount;

    /**
     * 费率
     */
    private BigDecimal charge;

    /**
     * 实际支付金额
     */
    private BigDecimal truelyPayAmount;

    /**
     * 渠道名
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long gatewayId;
    /**
     * 渠道名称
     */
    private String channelName;

    /**
     * 订单状态
     */
    private Integer state;

}
