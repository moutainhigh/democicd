package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.uwallet.pay.core.model.dto.StripeBaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * Stripe-API SetupIntent 设置未来付款信息
 * </p>
 *
 * @description:
 * @author: zhangzeyuan
 * @date: Created in 2022年1月10日15:50:38
 */
@ApiModel("Stripe-API SetupIntent设置未来付款")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StripeSetupIntentDTO extends StripeBaseDTO{


    private static final long serialVersionUID = 7795909600872414911L;

    /**
     * 设置为 true，立即尝试确认此 SetupIntent。
     * 此参数默认为 false。
     * 如果附加的支付方式是一张卡片，则可能会提供一个 return _ url，以防需要额外的身份验证
     */
    @ApiModelProperty(value = "是否确认")
    private Boolean confirm;

    /**
     * 收费金额  正整数，最多8位数字  REQUIRED
     */
    @ApiModelProperty(value = "收费金额")
    private BigDecimal amount;

    /**
     * 客户ID
     */
    @ApiModelProperty(value = "描述信息")
    private String customer;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String description;
    /**
     * 附加到此 PaymentIntent 的付款方法(PaymentMethod、 Card 或兼容的 Source 对象)的 ID
     */
    @ApiModelProperty(value = "")
    private String payment_method;


    /**
     * 允许使用的支付方法类型列表
     */
    @ApiModelProperty(value = " 允许使用的支付方法类型列表")
    private List<String> payment_method_types;

    /**
     * 用法
     * 指示将来如何使用付款方法。如果没有提供，这个值默认为 off _ session。
     * on_session/off_session
     */
    @ApiModelProperty(value = "用法")
    private String usage;

}
