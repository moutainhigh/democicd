package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author baixinyue
 * @description 订单退款信息
 * @createDate 2020/02/07
 */

@ApiModel("订单退款信息")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRefundDTO extends BaseDTO implements Serializable {

    /**
     * 交易流水id
     */
    private Long flowId;

    /**
     * 退款金额
     */
    private BigDecimal amount;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * latPay退款类型（0:撤销 1:退款）
     */
    private Integer latPayRefundType;

    /**
     * 渠道类型
     */
    private Integer gatewayType;



}
