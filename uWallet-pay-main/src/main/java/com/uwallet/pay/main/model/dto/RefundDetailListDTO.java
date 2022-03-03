package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author baixinyue
 * @createDate 2020/03/10
 * @description 退款详情
 */

@ApiModel("退款详情")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefundDetailListDTO implements Serializable {

    /**
     * 退款订单
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long id;

    /**
     * 交易订单
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long flowId;

    /**
     * 买家账号
     */
    private String phone;

    /**
     * 交易金额
     */
    private BigDecimal transAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 创建时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    private Long createdDate;

}
