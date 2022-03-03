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
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Stripe-API PaymentIntent 支付意向
 * </p>
 *
 * @description:
 * @author: zhangzeyuan
 * @date: Created in 2022年1月10日15:50:38
 */
@ApiModel("Stripe-API PaymentIntent支付意向")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StripePaymentIntentDTO extends StripeBaseDTO{

    private static final long serialVersionUID = -3336371695706001831L;

    /**
     * 支付金额 REQUIRED
     */
    @ApiModelProperty(value = "支付金额")
    private BigDecimal amount;
    /**
     * 货币单位 REQUIRED
     */
    @ApiModelProperty(value = "货币单位")
    private String currency;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String description;

    /**
     * 允许使用的支付方法类型列表
     */
    @ApiModelProperty(value = " 允许使用的支付方法类型列表")
    private List<String> payment_method_types;
    /**
     * 附加到此 PaymentIntent 的付款方法(PaymentMethod、 Card 或兼容的 Source 对象)的 ID
     */
    @ApiModelProperty(value = "")
    private String payment_method;
    /**
     * 支付选项  payment_method_options.card.request_three_d_secure
     */
    @ApiModelProperty(value = "支付选项")
    private Map<String, Object> payment_method_options;

    /**
     * 创建时间 时间戳
     */
    @ApiModelProperty(value = "创建时间")
    private String return_url;


    private Boolean use_stripe_sdk;

    /**
     * 确认标识
     */
    private Boolean confirm;

    /**
     * 用户
     */
    private String customer;


}
