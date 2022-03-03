package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.uwallet.pay.core.model.dto.StripeBaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * Stripe-API Customer 描述收款信息
 * </p>
 *
 * @description:
 * @author: zhangzeyuan
 * @date: Created in 2022年1月10日15:50:38
 */
@ApiModel("Stripe-API Charge收款信息")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StripeChargeDTO extends StripeBaseDTO{

    private static final long serialVersionUID = -4236819994090136105L;

    /**
     * 货币单位  REQUIRED
     */
    @ApiModelProperty(value = "货币单位")
    private String currency;
    /**
     * 收费金额  正整数，最多8位数字  REQUIRED
     */
    @ApiModelProperty(value = "收费金额")
    private BigDecimal amount;

    /**
     * 描述信息
     */
    @ApiModelProperty(value = "描述信息")
    private String description;

    /**
     * 客户ID
     */
    @ApiModelProperty(value = "客户ID")
    private String customer;

    /**
     * 付款来源  可以是 card ID/ bank account ID / card token / source ID / connected account ID 需要关联客户ID
     */
    @ApiModelProperty(value = "The customer’s full name or business name")
    private String source;

}
